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
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.FileManager;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.SKOS;

import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.InvalidFormatException;

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
	
	private static String service = "http://dbpedia.org/sparql";
	
	private String[] punctuationMarks = {",","'",":","-","!","~","\\(","\\)","\\.","\"",";","\""};
	private String stopWordsPattern = String.join("|", punctuationMarks);
	
	public static void main(String[] args) throws NoSuchAlgorithmException, InvalidFormatException, IOException {
		File sparqlPath = new File(args[0]);
		File[] sparqlFiles = sparqlPath.listFiles();
		File outputFolder = new File(args[2]);
		File[] outputFiles = outputFolder.listFiles();
		List<String> listProcessed = new ArrayList<String>();
		
		String nifPath = args[1];
				
		RelationExtraction re = new RelationExtraction();
		FileInputStream enTokenizerModel = re.readOpenNLPModel("OpenNLP/Models/Tokenizer/en-token.bin");
		FileInputStream enSentenceModel = re.readOpenNLPModel("OpenNLP/Models/SentenceDetector/en-sent.bin");
		SentenceModel sentenceModel = new SentenceModel(enSentenceModel);
		TokenizerModel tokenizerModel = new TokenizerModel(enTokenizerModel);
		
		Map<String,String> mapSparqlQueries = new HashMap<String,String>();
		List<REStats> listStats = new ArrayList<REStats>();
		//Map<String,List<DBpediaRelation>> mapRelations = new HashMap<String,List<DBpediaRelation>>();
		
		listProcessed.addAll(re.processedFiles(outputFiles));
		
		for(File file : sparqlFiles) {
//			if(!file.getName().contains("Employer"))
//				continue;
			if(listProcessed.contains(file.getName().replace(".rq", ""))) {
				System.out.println("File " + file.getName().replace(".rq", "") + " was already processed.");
				continue;
			}
			String query = re.readSparqlQueries(file);
			System.out.println(query);
			mapSparqlQueries.put(file.getName().replace(".rq", ""),query);
			
		}
		for(Map.Entry<String, String> entry : mapSparqlQueries.entrySet()) {
			System.out.println("Processing file: " + entry.getKey());
			List<DBpediaRelation> listRelations = new ArrayList<DBpediaRelation>();
			listRelations.addAll(re.queryRelations(entry.getValue(),entry.getKey(),listStats));
			re.process(listRelations, nifPath, sentenceModel, tokenizerModel, entry.getKey(), outputFolder.getPath());
			//break;
		}
		
		for(REStats stat : listStats) {
			System.out.println("Relation = " + stat.getPredicate() + " #Results = " + stat.getNumberResults());
		}
	}
	
	
	/*
	 * SPARQL DBpedia
	 */
	
	public List<DBpediaRelation> queryRelations(String queryString, String targetRelation, List<REStats> listStats) throws UnsupportedEncodingException {
		List<DBpediaRelation> listDBRelations = new ArrayList<DBpediaRelation>();
		REStats stat = new REStats();
		
		stat.setQueryString(queryString);
		
		Query query = QueryFactory.create(queryString);
		
		try(QueryExecution qexec = QueryExecutionFactory.sparqlService(service, query)){
			ResultSet result = qexec.execSelect();
			int counterResults = 0;
			while(result.hasNext()) {
				DBpediaRelation relation = new DBpediaRelation(targetRelation);
				
				QuerySolution qs = result.next();
				counterResults++;
				
				String s = new String(qs.getResource("?s").getURI().getBytes(),"UTF-8");
				String o = new String(qs.getResource("?o").getURI().getBytes(),"UTF-8");
				String p = qs.getResource("?p").getURI();
				
				relation.setSbjURI(s);
				relation.setObjURI(o);
				relation.setPrdURI(p);
				
				listDBRelations.add(relation);
			}
			stat.setPredicate(targetRelation);
			stat.setNumberResults(counterResults);
		}
		listStats.add(stat);
		return listDBRelations;
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
	
	// retrieve all annotations from abstract section from a given model
	public List<Annotation> queryAbstractAnnotations(Model model) {
		List<Annotation> listAnnotations = new ArrayList<Annotation>();
		
		String queryString = Queries.ABSTRACTLINKS.query();
		Query query = QueryFactory.create(queryString);
		try(QueryExecution qexec = QueryExecutionFactory.create(query, model)){
			ResultSet result = qexec.execSelect();
			while(result.hasNext()) {
				Annotation annotation = new Annotation();
				QuerySolution sol = result.next();
				annotation.setAnchor(sol.getLiteral("?anchor").getString());
				annotation.setURI(sol.getResource("?s").getURI());
				annotation.setParagraphURI(sol.getResource("?lsuper").getURI()); //linkSuperString --> Paragraph
				annotation.setSectionURI(sol.getResource("?psuper").getURI());//paragraphSuperString --> Section
				listAnnotations.add(annotation);
			}
		}
		return listAnnotations;
	}
	
	//retrieve the whole context
	public String queryContext(Model model) {
		String context = "";
		String queryString = Queries.CONTEXT.query();
		Query query = QueryFactory.create(queryString);
		try(QueryExecution qexec = QueryExecutionFactory.create(query, model)){
			ResultSet result = qexec.execSelect();
			while(result.hasNext()) {
				QuerySolution sol = result.next();
				context = sol.getLiteral("?context").getString();
			}
		}
		return context;
	}
	
	/*
	 * NIF
	 */
	//look for the nif file of the subject resource
	public void process(List<DBpediaRelation> listRelations, String nifPath, 
			SentenceModel sentenceModel, TokenizerModel tokenizerModel, 
			String fileName, String outputFolder) throws NoSuchAlgorithmException, IOException {
		int counterEquals = 0;
		int counterContains = 0;
		String counters = "";
		List<Report> listReport = new ArrayList<Report>();
		Set<DBpediaRelation> notInAbstractList = new HashSet<DBpediaRelation>();
		
		System.out.println(listRelations.get(0).getTagetRelation());
		
		for(DBpediaRelation rel : listRelations) {
			String[] sbjSplit = rel.getSbjURI().split("/");
			String sbj = sbjSplit[sbjSplit.length-1];
			String sbjAnchor = sbj.replaceAll("_", " ");
			
			String[] objSplit = rel.getObjURI().split("/");
			String objAnchor = objSplit[objSplit.length-1].replaceAll("_", " ");

			// System.out.println(sbjAnchor + "-" + objAnchor);
			File filePath = lookNIFFile(sbj, nifPath);

			// QUERIES
			List<Annotation> listLinkAnnotations = new ArrayList<Annotation>();
			BZip2CompressorInputStream inputStream = createBz2Reader(filePath);
			if (inputStream == null) {
				System.out.println("File \"" + filePath.getAbsolutePath() + "\"(" + sbj + ") does not exist");
				continue;
			}
			Model model = createJenaModel(inputStream);
			listLinkAnnotations.addAll(queryAbstractAnnotations(model));
			counters = detectRelationsInSentence(listLinkAnnotations, listReport,
					model,rel, sentenceModel, tokenizerModel, sbjAnchor, objAnchor);
			int equals = Integer.parseInt(counters.split("-")[0]);
			int contains = Integer.parseInt(counters.split("-")[1]);
			counterEquals += equals;
			counterContains += contains;
			System.out.println("equals = " + equals + " \ncounterCountains = " + contains);
			if(equals == 0 && contains == 0) {
				notInAbstractList.add(rel);
			}
		}
		// break;
		System.out.println("File Name = " + fileName);
		String[] counterApproach = countApproachFromReport(listReport).split("--");//sbj-obj--pronoun-obj
		String[] counterExtraction = countExtractionFromReport(listReport).split("--");//in sentence -- in paragraph
		int counterBlank = countBlankSentences(listReport);
		writeReport(outputFolder,fileName,listReport, counterEquals, counterContains, notInAbstractList.size(),
				counterApproach[0], counterApproach[1], counterExtraction[0], counterExtraction[1],
				counterBlank);
		
		
		System.out.println("number of equals: " + counterEquals);
		System.out.println("number of contains: " + counterContains);
		System.out.println("number of remaining relations: " + notInAbstractList.size());
		
	}

	public String detectRelationsInSentence(List<Annotation> listLinkAnnotations, List<Report> listReport,
			Model model, DBpediaRelation rel, SentenceModel sentenceModel, TokenizerModel tokenizerModel, 
			String sbjAnchor, String objAnchor) throws IOException {
		int counterEquals = 0; 
		int counterContains = 0;
		String approach = "";
		String extraction = "";
		String context = queryContext(model).replace("\n", " ");
		for(Annotation ann : listLinkAnnotations) {
			//ann.printAnnotation();
			String[] listSentences;

			Report report = new Report();
			report.setSubject(rel.getSbjURI());
			report.setObject(rel.getObjURI());
			report.setRelation(rel.getPrdURI());
			
			report.setSection("Abstract");
			
			String[] paragraphIndexes = extractIndexes(ann.getParagraphURI()).split(","); //begin,end
			String[] sectionIndexes = extractIndexes(ann.getSectionURI()).split(",");
			
			String abstractSec = context.substring(Integer.parseInt(sectionIndexes[0]),Integer.parseInt(sectionIndexes[1]));
			String paragraph = context.substring(Integer.parseInt(paragraphIndexes[0]), Integer.parseInt(paragraphIndexes[1])); 
			listSentences = sentenceDetector(sentenceModel,paragraph);
//			System.out.println("Detect sentence for " + ann.getAnchor() + " and " + sbjAnchor);
			
			System.out.println("detecting subject ("+sbjAnchor+") in sentence: ");
			
			String selectSentences = detectInSentence(tokenizerModel, listSentences, ann.getAnchor(), sbjAnchor);
			approach = "Sbj-Obj";
			extraction = "In Sentence";
			if(selectSentences.length() == 0) {
				System.out.println("Detecting pronoun (he,she,his,her) in sentence: ");
				approach = "Pronoun-Obj";
				extraction = "In Sentence";
				selectSentences = detectPronounInSentence(tokenizerModel, listSentences, objAnchor, sbjAnchor);
			}
			
			if(selectSentences.length() == 0) {
				System.out.println("Detecting relation using the whole paragraph. ");
				approach = "Sbj-Obj";
				extraction = "In Paragraph";
				selectSentences = detectInParagraph(tokenizerModel, context, ann.getAnchor(), sbjAnchor);
			}
			
			if(selectSentences.length() == 0) {
				report.setBlankSentence("X");
			}
			
			report.setContext(abstractSec);
			report.setSentence(selectSentences);
			
			report.setExtraction(extraction);
			report.setApproach(approach);
			
			if(ann.getAnchor().equalsIgnoreCase(objAnchor)) {
				System.out.println("Equal = " + ann.getAnchor());
				System.out.println("Sentence = " + selectSentences);
				report.setKindOfMatch("Equal");
				report.setAnchor(ann.getAnchor());
				counterEquals++;
				listReport.add(report);
			}else if(ann.getAnchor().contains(objAnchor)) {
				System.out.println("Contains = " + ann.getAnchor());
				System.out.println("Sentence = " + selectSentences);
				report.setKindOfMatch("Contains");
				report.setAnchor(ann.getAnchor());
				counterContains++;
				listReport.add(report);
			}
		}
//		System.out.println(counterEquals+"-"+counterContains);
		return counterEquals+"-"+counterContains;
	}
	
	
	public File lookNIFFile(String sbj, String nifPath) throws NoSuchAlgorithmException {
		String md5 = md5(sbj);
		File filePath = new File(nifPath+"/"+mdf5ToPath(md5)+".ttl.bz2");
		//System.out.println(filePath + " -- file exists? = " + filePath.exists());
		return filePath;
	}
	
	/*
	 * OpenNLP
	 */
	
	public String[] tokenExtraction(TokenizerModel model, String sbjAnchor)
			throws InvalidFormatException, IOException {

		TokenizerME tokenizer = new TokenizerME(model);
		//int numCharacters = 0;

		String tokens[] = tokenizer.tokenize(sbjAnchor);
		//double tokenProbs[] = tokenizer.getTokenProbabilities();

		return tokens;

	}
	
	public String[] sentenceDetector(SentenceModel model, String paragraph) throws IOException {
		SentenceDetectorME sdetector = new SentenceDetectorME(model);
		String[] sentences = sdetector.sentDetect(paragraph);
		return sentences;
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
	
	public String detectInSentence(TokenizerModel model, String[] listSentences, String objAnchor, 
			String sbjAnchor) throws InvalidFormatException, IOException {
		/*
		 * A sentence is considered only if contains the subject and object
		 * Subject must match one of their elements
		 * Object must match complete to avoid inconsistencies
		 */
		System.out.println("Subject = " + sbjAnchor);
		System.out.println("Object = " + objAnchor);
		String sentence = "";
		//int counter = 0;
		for(String snt : listSentences) {
//			System.out.println(counter++ + ".- sentence: \n" + snt);
//			System.out.println("Paragraph = " + snt);
			int sIndex = lookSbjIndex(model,snt,sbjAnchor);
			int oIndex = lookObjIndex(model,snt,objAnchor);
			System.out.println("sIndex = " + sIndex + " - oIndex = " + oIndex);
			if(sIndex >= 0 && sIndex < oIndex) {
				if(oIndex > 0)
					oIndex += objAnchor.length();
				if(oIndex > snt.length())
					oIndex = snt.length();
				System.out.println(sIndex + " -- " + oIndex + "--" + snt.length());
				if(sIndex < oIndex ) {
					sentence = snt.substring(sIndex, oIndex);
					System.out.println("Sentence:\n"+sentence);
					System.out.println();
				}
			}
			System.out.println("\n\n");
		}
		return sentence;
	}
	
	public String detectInParagraph(TokenizerModel model, String paragraph, String objAnchor, 
			String sbjAnchor) throws InvalidFormatException, IOException {
		/*
		 * A sentence is considered only if contains the subject and object
		 * Subject must match one of their elements
		 * Object must match complete to avoid inconsistencies
		 */
		System.out.println("Subject = " + sbjAnchor);
		System.out.println("Object = " + objAnchor);
		String sentence = "";
		int sIndex = lookSbjIndex(model, paragraph, sbjAnchor);
		int oIndex = lookObjIndex(model, paragraph, objAnchor);
		System.out.println("sIndex = " + sIndex + " - oIndex = " + oIndex);
		if (sIndex >= 0 && sIndex < oIndex) {
			if (oIndex > 0)
				oIndex += objAnchor.length();
			if (oIndex > paragraph.length())
				oIndex = paragraph.length();
			System.out.println(sIndex + " -- " + oIndex + "--" + paragraph.length());
			if (sIndex < oIndex) {
				sentence = paragraph.substring(sIndex, oIndex);
				System.out.println("Sentence:\n" + sentence);
				System.out.println();
			}
		}
		System.out.println("\n\n");
		return sentence;
	}
	
	public String detectPronounInSentence(TokenizerModel model, String[] listSentences, 
			String objAnchor, String sbjAnchor) throws InvalidFormatException, IOException {
		/*
		 * The relation is hold by a pronoun (she is the mother of ...)
		 * Subject must match a pronoun as he, she, his or her
		 * Object must match complete to avoid inconsistencies
		 */
		
		System.out.println("Object = " + objAnchor);
		String sentence = "";
		for(String snt : listSentences) {
			String index = lookPronounSbjIndex(model, snt);
			//System.out.println("----Index " + index);
			int sIndex = Integer.parseInt(index.split("--")[0]);
			String pronoun = "";
			
			if(!(sIndex == -1))
				pronoun = index.split("--")[1];
			
			int oIndex = lookObjIndex(model,snt,objAnchor);
			System.out.println("sIndex = " + sIndex + " - oIndex = " + oIndex);
			if(sIndex >= 0 && sIndex < oIndex) {
				if(oIndex > 0)
					oIndex += objAnchor.length();
				if(oIndex > snt.length())
					oIndex = snt.length();
				System.out.println(sIndex + " -- " + oIndex + "--" + snt.length());
				if(sIndex < oIndex ) {
					sentence = snt.substring(sIndex, oIndex);
					sentence = "(**" + pronoun + "=" + sbjAnchor + "**): " + sentence;
					System.out.println("Sentence:\n"+sentence);
					System.out.println();
				}
			}
			System.out.println("\n\n");
		}
		return sentence;
	}
	
	public int lookSbjIndex(TokenizerModel model, String paragraph, String anchor) throws InvalidFormatException, IOException {
		int index = 0;
		int indexTemp = 0;
		
		paragraph = removeStopWords(paragraph).toLowerCase();
//		System.out.println(paragraph);
		String[] pt = tokenExtraction(model, paragraph);
		
		String clean = anchor.split("\\(")[0]; 
		clean = removeStopWords(clean).toLowerCase();
		String[] a = tokenExtraction(model,clean.toLowerCase());
		
		int pPos = 0;
		String target = "";
		boolean onlyOne = false;
//		System.out.println("SUBJECT");
		ext:for(int i = 0; i < pt.length; i++) {
			for(int j = 0;  j < a.length; j++) {
//				System.out.println(pt[i] + " vs. " + a[j] + " ? " + pt[i].contains(a[j]));
				if(pt[i].equalsIgnoreCase(a[j])) {//add she, he, his, her
					pPos = i;
					target = pt[i];
					onlyOne = true;
					break ext;
				}
			}
		}
		
		if(onlyOne) {
			for(int i = 0 ; i < pPos; i++)
				indexTemp += pt[i].length();
//			System.out.println(paragraph);
//			System.out.println(target);
//			System.out.println(indexTemp);
			
			index = paragraph.indexOf(target,indexTemp);
//			System.out.println(index);
//			System.out.println();
		}else
			index = -1;
		
		return index;
	}
	
	public String lookPronounSbjIndex(TokenizerModel model, String paragraph) throws InvalidFormatException, IOException {
		String index = "";
		int indexTemp = 0;
		
		paragraph = removeStopWords(paragraph).toLowerCase();
//		System.out.println(paragraph);
		String[] pt = tokenExtraction(model, paragraph);
		
		String pronoun = detectPronoun(pt);
		
		int pPos = 0;
		String target = "";
		boolean onlyOne = false;
//		System.out.println("SUBJECT");
		for (int i = 0; i < pt.length; i++) {
			// System.out.println(pt[i] + " vs. " + a[j] + " ? " + pt[i].contains(a[j]));
			if (pt[i].equals(pronoun)) {// add she, he, his, her
				pPos = i;
				target = pt[i];
				onlyOne = true;
				break;
			}
		}
		
		if(onlyOne) {
			for(int i = 0 ; i < pPos; i++)
				indexTemp += pt[i].length();
//			System.out.println(paragraph);
//			System.out.println(target);
//			System.out.println(indexTemp);
			
			index = paragraph.indexOf(target,indexTemp)+"--"+pronoun;
//			System.out.println(index);
//			System.out.println();
		}else
			index = -1 + "--000";
		
		return index;
	}
	
	public int lookObjIndex(TokenizerModel model, String paragraph, String anchor) throws InvalidFormatException, IOException {
		int index = 0;
		int indexTemp = 0;
		
		paragraph = removeStopWords(paragraph).toLowerCase();
		
		String[] pt = tokenExtraction(model, paragraph);
		
		//String clean = anchor.split("\\(")[0];
		//clean = removeStopWords(clean).toLowerCase();
		
		//String[] a = tokenExtraction(model,clean);
		anchor = removeStopWords(anchor).toLowerCase();
		String[] a = tokenExtraction(model,anchor);
		int pPos = 0;
		String target = "";
		boolean allIn = false;
		
		System.out.println("===OBJECT===");
		
		ext:for(int i = 0; i < pt.length; i++) {
			if(a.length == 1) {
				if(pt[i].contains(a[0])) {
					pPos = i;
					target = pt[i];
					allIn = true;
					break;
				}
			}else {
				for(int j = 1;  j < a.length; j++) {
					System.out.println(pt[i] + " - " + a[0] + " contains? = " + pt[i].contains(a[0]));
					if(!pt[i].contains(a[0]))
						break;
					pPos = i;
					target = pt[i];
					for(int k = i+1; k < pt.length && j < a.length; k++, j++) {
						System.out.println(pt[k] + " - " + a[j] + " contains? = " + pt[k].contains(a[j]));
						if(pt[k].contains(a[j])) {
							allIn = true;
						}else {
							allIn = false;
							break;
						}
					}
				}
			}
			
			if(allIn)
				break ext;
			
		}
		if(allIn) {
			
			for(int i = 0; i < pPos; i++)
				indexTemp += pt[i].length();
			System.out.println("\n\n"+paragraph);
			System.out.println(target);
			System.out.println(indexTemp);
			
			index = paragraph.indexOf(anchor.toLowerCase(),indexTemp);
			
			System.out.println(index);
			System.out.println();
		}else
			index = -1;
		System.out.println("===End Object===\n\n");
//		System.out.println("index returned = " + index);
		return index;
	}
	
	public String detectPronoun(String[] tokens) {
		String pronoun = "";
		for(String token : tokens) {
			if(token.equals("he")) {
				pronoun = "he";
				break;
			}else if(token.equals("she")) {
				pronoun = "she";
				break;
			}else if(token.equals("his")) { 
				pronoun = "his";
				break;
			}else if(token.equals("her")) {
				pronoun = "her";
				break;
			}
		}
		return pronoun;
	}
	
	public List<String> processedFiles(File[] listFiles){
		List<String> listProcessed = new ArrayList<String>();
		for(File f : listFiles) {
			if(f.getName().endsWith(".csv"))
				listProcessed.add(f.getName().split("-")[0]);
		}
		return listProcessed;
	}
	
	public String removeStopWords(String target) {
//		System.out.println(stopWordsPattern);
		//Pattern pattern = Pattern.compile("\\b(?:"+stopWordsPattern+")\\b\\s*",Pattern.CASE_INSENSITIVE);
//		Matcher matcher = pattern.matcher(target);
		for(String mark : punctuationMarks) {
			target = target.replaceAll(mark, " ");
		}
//		System.out.println(target);
		return target;
	}
	
	public String extractIndexes(String paragraphURI) {
		return paragraphURI.split("char=")[1];
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
	
	public void writeReport(String outputFolder,String fileName,List<Report> listReport, int numEquals, 
			int numContains, int notInAbstractList, String sbjObj, String pronounObj,
			String sentence, String paragraph, int counterBlank) {
		try(PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(outputFolder+"/"+fileName+"-report.csv"),StandardCharsets.UTF_8))) {
			pw.write("number of equals = " + numEquals + "\n");
			pw.write("number of contains = " + numContains + "\n");
			pw.write("notInAbstractList = " + notInAbstractList + "\n");
			pw.write("Appoaches:\n");
			pw.write("\tSbj-Obj = " + sbjObj + "\n");
			pw.write("\tPronoun-Obj = " + pronounObj + "\n");
			pw.write("Extraction:\n");
			pw.write("\tIn Sentence = " + sentence + "\n");
			pw.write("\tIn Paragraph = " + paragraph + "\n");
			pw.write("Blank Sentences = " + counterBlank + "\n");
			
			pw.write("Kind of Match\t"
					+ "Section\t"
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
	

}
