package reeval;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import objects.AnnotationB;
import objects.DBpediaRelation;
import objects.Paragraph;
import objects.REStats;
import objects.Section;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.FileManager;
import org.apache.log4j.Logger;

import reports.FileReport;
import reports.GeneralReport;
import sparql.SparqlQueries;
import text.TextSearcher;

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
		TextSearcher ts = new TextSearcher();
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
		int foundInAbstract = 0;
		int foundInSection = 0;
		int filesWithAnnotation = 0;
		int filesWithoutAnnotation = 0;
		int filesNotFounded = 0;
		boolean firstTime = true;
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
				rel.setSbjLabel(sbjAnchor);
				
				String[] objSplit = rel.getObjURI().split("/");
				String objAnchor = objSplit[objSplit.length-1].split("\\(")[0];
				objAnchor = objAnchor.replaceAll("_", " ");
				rel.setObjLabel(objAnchor);
				
				File filePath = re.lookNIFFile(sbj, nifPath);
				
				BZip2CompressorInputStream inputStream = re.createBz2Reader(filePath);
				if (inputStream == null) {
					filesNotFounded++;
					fr.setReal(false);
					listFileReports.add(fr);
					continue;
				}
				
				Model model = re.createJenaModel(inputStream);
				String context = re.sq.queryContext(model);
				List<AnnotationB> listAnnotations = re.sq.queryAnnotationAbstract(model, objAnchor);
				
				for(AnnotationB ann : listAnnotations){
					Paragraph p = ann.getParagraph();
					Section s = ann.getSection();
					re.sq.queryParagraph(model, p);
					re.sq.querySection(model, s);
					p.setParagraphText(ts.textExtractor(context, p.getBeginIndex(), p.getEndIndex()));
					s.setSectionText(ts.textExtractor(context, s.getBeginIndex(), s.getEndIndex()));
					
					ann.setRel(rel);
					ann.setParagraph(p);
					ann.getParagraph().setSection(s);
					ann.setTextSegment(ts.textExtractor(context, p.getBeginIndex(), ann.getEndIndex()).replace("\n", " "));
					ann.setId(filesWithAnnotation);
					if(ann.getFoundIn().equals("Abstract"))
						foundInAbstract++;
					else
						foundInSection++;
					
				}
				
				filesWithoutAnnotation += (listAnnotations.size() == 0) ? 1 : 0 ;
				filesWithAnnotation += (listAnnotations.size() > 0) ? 1: 0;
				
				
				
				
				fr.setFilePath(filePath.getCanonicalPath());
				fr.setNumAnnotations(listAnnotations.size());
				re.writeResults(outputFolder+"/"+"output.tsv", listAnnotations, firstTime);
				firstTime = false;
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
			logger.info("Files processed = " + filesWithAnnotation);
			logger.info("Files with no annotations = " + filesWithoutAnnotation);
			logger.info("File does not exist = " + filesNotFounded);
			logger.info("Annotations founded in Abstract = " + foundInAbstract);
			logger.info("Annotations founded in Section = " + foundInSection);
			
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
	
	public void writeResults(String output, List<AnnotationB> listAnnotations, boolean firstTime){
		if(firstTime){
			File o = new File(output);
			if(o.exists())
				o.delete();
		}
		
		
		try(PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(output,true),StandardCharsets.UTF_8))){
			if(firstTime)
				pw.write("Id\tS\tP\tO\tFound In\tSection\tAnchor\tText Segment\tAnchor-URI\n");
			for(AnnotationB ann : listAnnotations){
				pw.write(ann.toString() + "\n");
			}
		}catch(IOException e){
			
		}
	}
}
