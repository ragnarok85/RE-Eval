package reeval;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import objects.Annotation;
import objects.Article;
import objects.DBpediaRelation;
import objects.Paragraph;
import objects.REStats;
import reports.Report;
import objects.Section;
import opennlp.tools.util.InvalidFormatException;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.FileManager;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.SKOS;
import org.apache.log4j.Logger;

import sparql.SparqlQueries;

public class RelationExtraction {
	/*
	 * possible input:
	 *   A path to SPARQL files with the queries for each relation
	 *   args[0] - path to SPARQL files
	 *   args[1] - nif path
	 *   args[2] - output folder
	 *   Restrictions (queries):
	 *    1. Subject and object types must start with a upper case word
	 *    2. Predicate must start with a lower case word
	 *    3. The name file must be the same as the relation and must be ended with .rq
	 */
	
	private static Logger logger = Logger.getLogger(RelationExtraction.class);
	
	static List<Report> listReport = new ArrayList<Report>();
	static Set<DBpediaRelation> notInList = new HashSet<DBpediaRelation>();
	static Integer counterEquals = 0;
	static Integer counterContains = 0;
	
	private SparqlQueries sq = new SparqlQueries();
	
	public static void main(String[] args) throws NoSuchAlgorithmException, InvalidFormatException, IOException, InterruptedException {
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
		
		RelationExtraction re = new RelationExtraction();
		
		listProcessed.addAll(re.processedFiles(outputFiles));
		
		for(File file : sparqlFiles) {
			if(!file.getName().contains("PartyAffiliation"))
				continue;
//			if(listProcessed.contains(file.getName().replace(".rq", ""))) {
//				System.out.println("File " + file.getName().replace(".rq", "") + " was already processed.");
//				continue;
//			}
			String query = re.readSparqlQueries(file);
			logger.info(query);
			mapSparqlQueries.put(file.getName().replace(".rq", ""),query);
			
		}
		
		for(Map.Entry<String, String> entry : mapSparqlQueries.entrySet()) {
			logger.info("Processing file: " + entry.getKey());
			Long initialTime = System.currentTimeMillis();
			Long endTime = 0L;
			String timeElapsed = "";
			List<DBpediaRelation> listRelations = new ArrayList<DBpediaRelation>();
			listRelations.addAll(re.sq.queryDBpediaRelations(entry.getValue(),entry.getKey(),listStats));
			
//			notInSectionList.addAll(re.lookRelationsInSection(listRelations, nifPath, 
//					sentenceModel, tokenizerModel, entry.getKey(), outputSections));
			logger.info("Beging Abstract");
			re.lookRelationsInAbstract(listRelations, nifPath, 
					entry.getKey(), outputAbstract);
			notInAbstractList.addAll(notInList);
			notInList.clear();
			logger.info("relations not found in abstract = " + notInAbstractList.size());
			re.writeNotInAbstractRelations(outputNotInAbstract, entry.getKey(), 
					notInAbstractList);

			endTime = System.currentTimeMillis() - initialTime;
			timeElapsed = String.format("TOTAL TIME = %d min, %d sec", TimeUnit.MILLISECONDS.toMinutes(endTime),
	    			TimeUnit.MILLISECONDS.toSeconds(endTime) - 
	    		    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(endTime)));
			re.writeTime(outputFolder, "AbstractTime.csv", entry.getKey(), timeElapsed);
			logger.info("End Abstract");

			logger.info("Begin Section");
			initialTime = System.currentTimeMillis();
			if(!notInAbstractList.isEmpty()) {
				notInSectionList.addAll(re.lookRelationsInSection(notInAbstractList, nifPath, 
					entry.getKey(), outputSections));
				re.writeNotInAbstractRelations(outputNotInSection, entry.getKey(), 
						notInSectionList);
			}
			endTime = System.currentTimeMillis() - initialTime;
			timeElapsed = String.format("TOTAL TIME = %d min, %d sec", TimeUnit.MILLISECONDS.toMinutes(endTime),
	    			TimeUnit.MILLISECONDS.toSeconds(endTime) - 
	    		    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(endTime)));
			re.writeTime(outputFolder, "SectionTime.csv", entry.getKey(), timeElapsed);
			logger.info("End Section");
		}
		
		for(REStats stat : listStats) {
			logger.info("Relation = " + stat.getPredicate() + " #Results = " + stat.getNumberResults());
		}
		
		
	}
	
	/*
	 * NIF
	 */
	//look for the nif file of the subject resource
	public void lookRelationsInAbstract(List<DBpediaRelation> listRelations, String nifPath, 
			String fileName, File outputFolder) throws NoSuchAlgorithmException, IOException, InterruptedException {
		
		listReport.clear();
		counterEquals = 0;
		counterContains = 0;
		
		List<Thread> listThreads = new ArrayList<Thread>();
		List<String> listArticlesNotFound = new ArrayList<String>();
		SparqlQueries sq = new SparqlQueries();
		
		logger.info(listRelations.get(0).getTargetRelation());
		int counterRels = 0;
		for(DBpediaRelation rel : listRelations) {
			
			Article article = new Article();
			
			String[] sbjSplit = rel.getSbjURI().split("/");
			String sbj = sbjSplit[sbjSplit.length-1];
			String sbjAnchor = sbj.replaceAll("_", " ");
			
//			if(!sbj.equals("Afsaneh_Najmabadi")) {
//				continue;
//			}
			
			String[] objSplit = rel.getObjURI().split("/");
			String objAnchor = objSplit[objSplit.length-1].replaceAll("_", " ");

			//TODO Create an Article object with name attribute as the file name
			
			// System.out.println(sbjAnchor + "-" + objAnchor);
			File filePath = lookNIFFile(sbj, nifPath);
			
			
			
			// QUERIES
			List<Annotation> listLinkAnnotations = new ArrayList<Annotation>();
			BZip2CompressorInputStream inputStream = createBz2Reader(filePath);
			if (inputStream == null) {
				logger.info("File \"" + filePath.getAbsolutePath() + "\"(" + sbj + ") does not exist");
				listArticlesNotFound.add(sbj+"--"+filePath.getName());
				continue;
			}
			
			Model model = createJenaModel(inputStream);
			
			//TODO query for context
			article.setName(fileName);
			article.setContext(sq.queryContext(model));
			//TODO query for sections
			article.setListSections(sq.queryOrderSection(model));
			//TODO query for paragraphs
			for(Section s : article.getListSections()){
				s.setListParagraphs(sq.queryOrdererParagraph(model, s.getSectionURI()));
			}
			//TODO The query for annotations is made in the following lines
			for(Section s: article.getListSections()){
				for(Paragraph p : s.getListParagraphs()){
					p.setListAnnotations(sq.queryOrderAnnotation(model, p.getParagraphURI()));
				}
			}
			//Abstract
			listLinkAnnotations.addAll(sq.queryAbstractAnnotations(model));
			
			//TODO filter annotations to those which contains or are equal to the object
			
			AbstractRelationExtractor are = new AbstractRelationExtractor(sbjAnchor,objAnchor,"0","Abstract",
					model, rel, listLinkAnnotations);
			listThreads.add(are);
			are.start();
			//System.out.println("threads running: " + Thread.activeCount());
			System.out.println("Relation processing/processed = " + counterRels++ +"/" + listRelations.size());
		}
		for(Thread t : listThreads) {
			t.join();
		}
		// break;
//		System.out.println("File Name = " + fileName);
		String[] counterApproach = countApproachFromReport(listReport).split("--");//sbj-obj--pronoun-obj
		String[] counterExtraction = countExtractionFromReport(listReport).split("--");//in sentence -- in paragraph
		int counterBlank = countBlankSentences(listReport);
		writeReport(outputFolder,fileName,listReport, counterEquals, counterContains, notInList.size(),
				counterApproach[0], counterApproach[1], counterExtraction[0], counterExtraction[1],
				counterBlank, listArticlesNotFound);
		
		
		logger.info("number of equals: " + counterEquals);
		logger.info("number of contains: " + counterContains);
		logger.info("number of remaining relations: " + notInList.size());
		
	}
	
	public Set<DBpediaRelation> lookRelationsInSection(List<DBpediaRelation> listRelations, String nifPath, 
			String fileName, File outputFolder) throws NoSuchAlgorithmException, IOException, InterruptedException {
		
		listReport.clear();
		counterEquals = 0;
		counterContains = 0;
		
		List<String> listArticlesNotFound = new ArrayList<String>();
		List<Thread> listThreads = new ArrayList<Thread>();
		Set<DBpediaRelation> setNotInSection = new HashSet<DBpediaRelation>();
		
		logger.info(listRelations.get(0).getTargetRelation());
		
		int fileCounter = 0;
		logger.info("number of relations found = " + listRelations.size());
		int counterRels = 0;
		for(DBpediaRelation rel : listRelations) {
			
			listThreads.clear();
			String[] sbjSplit = rel.getSbjURI().split("/");
			String sbj = sbjSplit[sbjSplit.length-1];
			String sbjAnchor = sbj.replaceAll("_", " ");
			
//			if(!sbj.contains("Abigail_Breslin"))
//				continue;
			
			String[] objSplit = rel.getObjURI().split("/");
			String objAnchor = objSplit[objSplit.length-1].replaceAll("_", " ");

			Map<String,String> mapSections = new HashMap<String,String>();
			
			// System.out.println(sbjAnchor + "-" + objAnchor);
			File filePath = lookNIFFile(sbj, nifPath);

			// QUERIES
			
			BZip2CompressorInputStream inputStream = createBz2Reader(filePath);
			if (inputStream == null) {
				logger.info("File \"" + filePath.getAbsolutePath() + "\"(" + sbj + ") does not exist");
				listArticlesNotFound.add(sbj+"--"+filePath.getName());
				continue;
			}
			logger.info(fileCounter++ + "/" + listRelations.size() + ".- processing file = " + filePath.getAbsolutePath() + " ( " + sbj + ")");
			Model model = createJenaModel(inputStream);
			//Abstract
			
			mapSections.putAll(sq.queryNumberAndTitleSections(model));
			logger.info("number of sections = " + mapSections.size());
			
			int sectionCounter = 0;
			System.out.println("Relation processing/processed = " + counterRels++ +"/" + listRelations.size());
			for(Map.Entry<String,String> entry : mapSections.entrySet()) { 
				List<Annotation> listLinkAnnotations = new ArrayList<Annotation>();
				listLinkAnnotations.addAll(sq.querySectionAnnotations(model,entry.getKey()));
				logger.info("number of annotations for section "+ entry.getKey() +" = " + listLinkAnnotations.size());
				//System.out.println("processing section number = " + entry.getKey() + " (" + entry.getValue() + ")");
				if(listLinkAnnotations.size() == 0)
					continue;
				AbstractRelationExtractor are = new AbstractRelationExtractor(sbjAnchor,objAnchor,entry.getKey(),
						entry.getValue(), model, rel, listLinkAnnotations);
				listThreads.add(are);
				are.start(); 
				System.out.println("\tSections processing/processed = " + sectionCounter++ +"/" + mapSections.size());
			}
			for(Thread t : listThreads) {
				t.join();
			}
			logger.info(counterEquals + " -- " + counterContains + "\tequals and contains\n\n");
			if(counterEquals== 0 && counterContains== 0) {
				setNotInSection.add(rel);
			}
			model.close();
		}
		// break;
		logger.info("File Name = " + fileName);
		String[] counterApproach = countApproachFromReport(listReport).split("--");//sbj-obj--pronoun-obj
		String[] counterExtraction = countExtractionFromReport(listReport).split("--");//in sentence -- in paragraph
		int counterBlank = countBlankSentences(listReport);
		writeReport(outputFolder,fileName+"-Section",listReport, counterEquals, counterContains, notInList.size(),
				counterApproach[0], counterApproach[1], counterExtraction[0], counterExtraction[1],
				counterBlank, listArticlesNotFound);
		logger.info("number of equals: " + counterEquals);
		logger.info("number of contains: " + counterContains);
		logger.info("number of remaining relations: " + notInList.size());
		return setNotInSection;
	}
	
	/*
	 * JENA
	 * 
	 */
	
	public Model createJenaModel(BZip2CompressorInputStream filePath) {
		Model model = ModelFactory.createDefaultModel();
		model.read(filePath,null,"TURTLE");
		//setPrefixes(model);
		return model;
		
	}
	
	public void setPrefixes(Model model) {
		model.setNsPrefix("rdf", RDF.uri);
		model.setNsPrefix("nif", "http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#");
		model.setNsPrefix("skos", SKOS.uri);
	}
		
	/*
	 * Utilities
	 */
	
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
	
	public String countApproachFromReport(List<Report> listReport) {
		String counterApproach = "0--0"; //Sbj-Obj--Pronoun-Obj
		int sbjObj = 0;
		int pronounObj = 0;
		for(Report r : listReport) {
			if(r.getApproach().equals("Sbj-Obj"))
				sbjObj++;
			else if(r.getApproach().equals("Pronoun-Obj"))
				pronounObj++;
		}
		counterApproach = sbjObj+"--"+pronounObj;
		return counterApproach;
	}
	
	public String countExtractionFromReport(List<Report> listReport) {
		String counterExtraction = "0--0"; //In Sentence--In Paragraph
		int sentence = 0;
		int paragraph = 0;
		for(Report r : listReport) {
			if(r.getExtraction().equals("In Sentence"))
				sentence++;
			else if(r.getExtraction().equals("In Paragraph"))
				paragraph++;
		}
		counterExtraction = sentence+"--"+paragraph;
		return counterExtraction;
	}
	
	public int countBlankSentences(List<Report> listReport) {
		int counterBlank = 0;
			for(Report r : listReport) {
				if(r.getBlankSentence().equals("X"))
					counterBlank++;
			}
		return counterBlank;
	}
	
	public String defineRelation(String relation) {
		String URI = "";
		String splitRelation[] = relation.split(":");
		if(splitRelation.length == 2)
			URI = "http://dbpedia.org/property/"+splitRelation[1];
		else
			URI = "http://dbpedia.org/ontology/"+relation;
		return URI;
	}
	
	public File lookNIFFile(String sbj, String nifPath) throws NoSuchAlgorithmException {
		String md5 = md5(sbj);
		File filePath = new File(nifPath+"/"+mdf5ToPath(md5)+".ttl.bz2");
		//System.out.println(filePath + " -- file exists? = " + filePath.exists());
		return filePath;
	}
	
	
	public List<String> processedFiles(File[] listFiles){
		List<String> listProcessed = new ArrayList<String>();
		for(File f : listFiles) {
			if(f.getName().endsWith(".csv"))
				listProcessed.add(f.getName().split("-")[0]);
		}
		return listProcessed;
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
	
	public FileInputStream readOpenNLPModel(String pathToModel) throws FileNotFoundException {
		ClassLoader classLoader = getClass().getClassLoader();
		FileInputStream fileModel = null;
		fileModel = new FileInputStream(classLoader.getResource(pathToModel).getFile());
		return fileModel;
	}
	
	public void writeReport(File outputFolder,String fileName,List<Report> listReport, int numEquals, 
			int numContains, int notInAbstractList, String sbjObj, String pronounObj,
			String sentence, String paragraph, int counterBlank, List<String> listArticlesNotFound) {
		try(PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(outputFolder+"/"+fileName+"-report.csv"),StandardCharsets.UTF_8))) {
			pw.write("number of equals =\t" + numEquals + "\n");
			pw.write("number of contains =\t" + numContains + "\n");
			pw.write("notInAbstractList =\t" + notInAbstractList + "\n");
			pw.write("Appoaches:\n");
			pw.write("\tSbj-Obj =\t" + sbjObj + "\n");
			pw.write("\tPronoun-Obj =\t" + pronounObj + "\n");
			pw.write("Extraction:\n");
			pw.write("\tIn Sentence =\t" + sentence + "\n");
			pw.write("\tIn Paragraph =\t" + paragraph + "\n");
			pw.write("Blank Sentences =\t" + counterBlank + "\n");
			pw.write("Number of articles not found in 2016-10 wikipedia nif version =\t" + listArticlesNotFound.size() + "\t");
			for(String article : listArticlesNotFound) {
				pw.write(article.split("--")[0]+",");
			}
			pw.write("\n");
			pw.write("Kind of Match\t"
					+ "Section\t"
					+ "Section Title\t"
					+ "Extraction\t"
					+ "Approach\t"
					+ "Blank Sentence\t"
					+ "NIF-Anchor\t"
					+ "Subject\t"
					+ "Relation\t"
					+ "Object\t"
					+ "Sentence\t"
					+ "Context\n");
			for(Report r : listReport) {
				pw.write(r.printReport()+"\n");
			}
			pw.close();
		}catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public void writeNotInAbstractRelations(File output, String fileName, List<DBpediaRelation> notInAbstractList) {
		try(PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(output+"/"+fileName), StandardCharsets.UTF_8))){
			pw.write("subject\tpredicate\tobject\tcontext\n");
			for(DBpediaRelation rel : notInAbstractList) {
				pw.write(rel.printRelation() + "\n");
			}
			pw.close();
		}catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public void writeTime(File output, String fileName, String relation, String timeElapsed) {
		try(PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(output+"/"+fileName,true),StandardCharsets.UTF_8))){
			pw.write(relation+"\t"+timeElapsed+"\n");
		}catch(IOException e) {
			e.printStackTrace();
		}
	}
}
