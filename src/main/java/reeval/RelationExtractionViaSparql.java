package reeval;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.FileManager;
import org.apache.log4j.Logger;

import objects.AnnotationB;
import objects.DBpediaRelation;
import objects.REStats;
import reports.FileReport;
import reports.GeneralReport;
import sparql.SparqlQueries;

public class RelationExtractionViaSparql {
	
	private static Logger logger = Logger.getLogger(RelationExtractionViaSparql.class);

	SparqlQueries sq = new SparqlQueries();
	
	public static void main(String[] args) throws NoSuchAlgorithmException, IOException{
		
		File sparqlPath = new File(args[0]);
		File[] sparqlFiles = sparqlPath.listFiles();
		File outputFolder = new File(args[2]);
		File outputAbstract = new File(outputFolder.getAbsoluteFile()+"/Abstract");
		File outputNotInAbstract = new File(outputFolder.getAbsoluteFile()+"/notInAbstract");
		File outputNotInSection = new File(outputFolder.getAbsoluteFile()+"/notInSection");
		File outputSections = new File(outputFolder.getAbsoluteFile()+"/Sections");
		File[] outputFiles = outputFolder.listFiles();
		String nifPath = args[1];
		
		List<String> listProcessed = new ArrayList<String>();
		List<DBpediaRelation> notInAbstractList = new ArrayList<DBpediaRelation>();
		List<DBpediaRelation> notInSectionList = new ArrayList<DBpediaRelation>();
		Map<String,String> mapSparqlQueries = new HashMap<String,String>();
		List<REStats> listStats = new ArrayList<REStats>();
		
		RelationExtractionViaSparql re = new RelationExtractionViaSparql();
		
		GeneralReport gr = new GeneralReport();
		
		for(File file : sparqlFiles) {
			if(!file.getName().contains("Employer"))
				continue;
//			if(listProcessed.contains(file.getName().replace(".rq", ""))) {
//				System.out.println("File " + file.getName().replace(".rq", "") + " was already processed.");
//				continue;
//			}
			String query = re.readSparqlQueries(file);
			logger.info(query);
			mapSparqlQueries.put(file.getName().replace(".rq", ""),query);
			
		}
		int annotationCounter = 0;
		int notInAbstractCounter = 0;
		int nullCounter = 0;
		for(Map.Entry<String, String> entry : mapSparqlQueries.entrySet()) {
			logger.info("Processing file: " + entry.getKey());
			gr.setTargetRelation(entry.getKey());
			gr.setSparqlQuery(entry.getValue());
			
			Long initialTime = System.currentTimeMillis();
			Long endTime = 0L;
			String timeElapsed = "";
			List<DBpediaRelation> listRelations = new ArrayList<DBpediaRelation>();
			listRelations.addAll(re.sq.queryDBpediaRelations(entry.getValue(),entry.getKey(),listStats));
			
//			notInSectionList.addAll(re.lookRelationsInSection(listRelations, nifPath, 
//					sentenceModel, tokenizerModel, entry.getKey(), outputSections));
			logger.info("Beging Abstract");
			List<FileReport> listFileReports = new ArrayList<FileReport>();
			for(DBpediaRelation rel : listRelations){
				FileReport fr = new FileReport();
				
				String[] sbjSplit = rel.getSbjURI().split("/");
				String sbj = sbjSplit[sbjSplit.length-1];
				String sbjAnchor = sbj.replaceAll("_", " ");

				String[] objSplit = rel.getObjURI().split("/");
				String objAnchor = objSplit[objSplit.length-1].split("\\(")[0];
				objAnchor = objAnchor.replaceAll("_", " ");
				
				File filePath = re.lookNIFFile(sbj, nifPath);
				fr.setFilePath(filePath.getCanonicalPath());
				fr.setSubjectURI(rel.getSbjURI());
				fr.setSubjectAnchor(rel.getSbjLabel());
				fr.setObjectURI(rel.getObjURI());
				fr.setObjectAnchor(rel.getObjLabel());
				
				BZip2CompressorInputStream inputStream = re.createBz2Reader(filePath);
				if (inputStream == null) {
					nullCounter++;
					fr.setReal(false);
					listFileReports.add(fr);
					continue;
				}
				
				Model model = re.createJenaModel(inputStream);
				
				List<AnnotationB> listAnnotations = re.sq.queryAnnotations(model, objAnchor);
				
				notInAbstractCounter += (listAnnotations.size() == 0) ? 1 : 0 ;
				annotationCounter += (listAnnotations.size() > 0) ? 1: 0;
				for(AnnotationB ann : listAnnotations){
					System.out.println(ann.toString());
				}
			}
			logger.info("relations not found in abstract = " + notInAbstractList.size());

			endTime = System.currentTimeMillis() - initialTime;
			timeElapsed = String.format("TOTAL TIME = %d min, %d sec", TimeUnit.MILLISECONDS.toMinutes(endTime),
	    			TimeUnit.MILLISECONDS.toSeconds(endTime) - 
	    		    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(endTime)));
			logger.info(timeElapsed);
			logger.info("End Abstract");
		}
		
		for(REStats stat : listStats) {
			
			logger.info("Relation = " + stat.getPredicate() + " #Results = " + stat.getNumberResults());
			logger.info("Relations found in Abstract = " + annotationCounter);
			logger.info("Relations not found in Abstract = " + notInAbstractCounter);
			logger.info("File does not exist = " + nullCounter);
		}
}
	
	public BZip2CompressorInputStream createBz2Reader(File source) {
		BZip2CompressorInputStream pageStruct = null;
		try{
			if(!source.exists()) {
				return null;
			}
			InputStream pageStructure = FileManager.get().open(source.getAbsolutePath());
	    	pageStruct = new BZip2CompressorInputStream(pageStructure);
		}catch(IOException e) {
			e.printStackTrace();
		}
		return pageStruct;
	}
	
	public String readSparqlQueries(File inputFile){
		String sparqlQuery = "";
		try(BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile),StandardCharsets.UTF_8))){
			String line = "";
			while((line = br.readLine()) != null) {
				sparqlQuery += line +" "; 
			}
			br.close();
		}catch(IOException e) {
			e.printStackTrace();
		}
		return sparqlQuery;
	}
	
	public File lookNIFFile(String sbj, String nifPath) throws NoSuchAlgorithmException {
		String md5 = md5(sbj);
		File filePath = new File(nifPath+"/"+mdf5ToPath(md5)+".ttl.bz2");
		//System.out.println(filePath + " -- file exists? = " + filePath.exists());
		return filePath;
	}
	
	public String md5(String name) throws NoSuchAlgorithmException {
		String md5 = "";
		String base = "<http://nif.dbpedia.org/wiki/en/"+name;
		byte[] fileName = base.getBytes();
		MessageDigest md = MessageDigest.getInstance("MD5");
		md5 = toHexString(md.digest(fileName));
		return md5;
	}
	
	public String toHexString(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();

        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }

        return hexString.toString();
    }
	
	public String mdf5ToPath(String md5) {
		return md5.substring(0, 2) + "/" + md5.substring(2,4) + "/" + md5.substring(4);
	}
	
	public Model createJenaModel(BZip2CompressorInputStream filePath) {
		Model model = ModelFactory.createDefaultModel();
		model.read(filePath,null,"TURTLE");
		//setPrefixes(model);
		return model;
		
	}
}
