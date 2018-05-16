package reeval;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;


import org.apache.commons.compress.compressors.CompressorException;
import org.apache.log4j.Logger;

import objects.DBpediaRelation;
import objects.REStats;
import opennlp.tools.util.InvalidFormatException;
import sparql.SparqlQueries;

public class RelationExtractionByPerson {
private static Logger logger = Logger.getLogger(RelationExtractionByPerson.class);

private SparqlQueries sq = new SparqlQueries();


	public static void main(String[] args) throws NoSuchAlgorithmException, InvalidFormatException, IOException, InterruptedException, CompressorException {
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
		
		RelationExtractionByPerson re = new RelationExtractionByPerson();
		
//		listProcessed.addAll(re.processedFiles(outputFiles));

		List<DBpediaRelation> listPersons = new ArrayList<DBpediaRelation>();
		String queryPerson = "SELECT ?s WHERE { ?s a <http://dbpedia.org/ontology/Person> } LIMIT 1000";
		listPersons.addAll(re.sq.queryDBpediaRelations(queryPerson,"Person",listStats));
		
		for(File file : sparqlFiles) {
			String query = re.readSparqlQueries(file);
			logger.info(query);
			mapSparqlQueries.put(file.getName().replace(".rq", ""),query);
			
		}
		
		//for each person
		
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

//			logger.info("Begin Section");
//			initialTime = System.currentTimeMillis();
//			if(!notInAbstractList.isEmpty()) {
//				notInSectionList.addAll(re.lookRelationsInSection(notInAbstractList, nifPath, 
//					entry.getKey(), outputSections));
//				re.writeNotInAbstractRelations(outputNotInSection, entry.getKey(), 
//						notInSectionList);
//			}
//			endTime = System.currentTimeMillis() - initialTime;
//			timeElapsed = String.format("TOTAL TIME = %d min, %d sec", TimeUnit.MILLISECONDS.toMinutes(endTime),
//	    			TimeUnit.MILLISECONDS.toSeconds(endTime) - 
//	    		    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(endTime)));
//			re.writeTime(outputFolder, "SectionTime.csv", entry.getKey(), timeElapsed);
//			logger.info("End Section");
		}
		
		for(REStats stat : listStats) {
			logger.info("Relation = " + stat.getPredicate() + " #Results = " + stat.getNumberResults());
		}
		
		
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
}
