package reeval;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;

public class RelationExtraction {
	/*
	 * possible input:
	 *   A list of types relation separate by comma (predicate/relation,subject,object) e.g. Employer,Person,Organization
	 *   
	 *   DBpedia ontology rule: dbo:Person refers to a class and dbo:person refers to a property
	 *   
	 *   Restrictions:
	 *    1. Subject and object types must start with a upper case word
	 *    2. Predicate must start with a lower case word
	 *    	2.1 if the predicate starts with dbp, the property is defined as an RDFS property
	 *    3. In DBpedia types Organization must be changes for Organisation
	 */
	
	private static String service = "http://dbpedia.org/sparql";
	
	public static void main(String[] args) {
		File inputFile = new File(args[0]);
		RelationExtraction re = new RelationExtraction();
		List<String> listLines = new ArrayList<String>();
		List<REStats> listStats = new ArrayList<REStats>();
		Map<String,List<DBpediaRelation>> mapRelations = new HashMap<String,List<DBpediaRelation>>();
		
		listLines.addAll(re.readLines(inputFile));
		
		for(String line : listLines) {
			String[] triple = line.split(",");
			if(triple.length == 3) {
				List<DBpediaRelation> listRelations = new ArrayList<DBpediaRelation>();
				String sbjType = triple[1];
				String targetRelation = re.defineRelation(triple[0]); 
				String objType = triple[2];
				listRelations.addAll(re.queryCompleteAllRelations(sbjType, targetRelation, objType,listStats));
				mapRelations.put(targetRelation, listRelations);
				//break;
			}
		}
		for(REStats stat : listStats) {
			System.out.println("Relation = " + stat.getPredicate() + " #Results = " + stat.getNumberResults());
		}
	}
	
	/*
	 * Jena
	 */
	
	public List<DBpediaRelation> querySimpleAllRelations(String sbjType, String targetRelation, String objType, List<REStats> listStats) {
		List<DBpediaRelation> listDBRelations = new ArrayList<DBpediaRelation>();
		REStats stat = new REStats();
		
		stat.setSubject(sbjType);
		stat.setObject(objType);
		stat.setPredicate(targetRelation);
		
		String queryString = " SELECT ?s ?o WHERE { "
				+ " ?s a <http://dbpedia.org/ontology/" + sbjType + ">."
				+ " ?s <" + targetRelation + "> ?o."
				+ " ?o a <http://dbpedia.org/ontology/" + objType + ">."
				+ " } ";
		stat.setQueryString(queryString);
		
		Query query = QueryFactory.create(queryString);
		
		try(QueryExecution qexec = QueryExecutionFactory.sparqlService(service, query)){
			ResultSet result = qexec.execSelect();
			int counterResults = 0;
			while(result.hasNext()) {
				DBpediaRelation relation = new DBpediaRelation(targetRelation);
				
				QuerySolution qs = result.next();
				counterResults++;
				
				String s = qs.getResource("?s").getURI();
				String o = qs.getResource("?o").getURI();
				
				relation.setSbjURI(s);
				relation.setObjURI(o);
				listDBRelations.add(relation);
			}
			stat.setNumberResults(counterResults);
		}
		listStats.add(stat);
		return listDBRelations;
	}
	
	public List<DBpediaRelation> queryCompleteAllRelations(String sbjType, String targetRelation, String objType, List<REStats> listStats) {
		List<DBpediaRelation> listDBRelations = new ArrayList<DBpediaRelation>();
		REStats stat = new REStats();
		
		stat.setSubject(sbjType);
		stat.setObject(objType);
		stat.setPredicate(targetRelation);
		
		String queryString = " SELECT ?s ?o ?ls ?lo ?abstract WHERE { "
				+ " ?s a <http://dbpedia.org/ontology/" + sbjType + ">."
				+ " ?s <" + targetRelation + "> ?o."
				+ " ?s <http://www.w3.org/2000/01/rdf-schema#label> ?ls. "
				+ " ?s <http://dbpedia.org/ontology/abstract> ?abstract. "
				+ " ?o a <http://dbpedia.org/ontology/" + objType + ">."
				+ " ?o <http://www.w3.org/2000/01/rdf-schema#label> ?lo. "
				+ " FILTER(LANG(?ls) = \"\" || LANGMATCHES(LANG(?ls), \"en\")) "
				+ " FILTER(LANG(?lo) = \"\" || LANGMATCHES(LANG(?lo), \"en\")) "
				+ " FILTER(LANG(?abstract) = \"\" || LANGMATCHES(LANG(?abstract), \"en\")) "
				+ " } ";
		stat.setQueryString(queryString);
		
		Query query = QueryFactory.create(queryString);
		
		try(QueryExecution qexec = QueryExecutionFactory.sparqlService(service, query)){
			ResultSet result = qexec.execSelect();
			int counterResults = 0;
			while(result.hasNext()) {
				DBpediaRelation relation = new DBpediaRelation(targetRelation);
				
				QuerySolution qs = result.next();
				counterResults++;
				
				String s = qs.getResource("?s").getURI();
				String o = qs.getResource("?o").getURI();
				String objLabel = qs.getLiteral("?lo").getString();
				String sbjLabel = qs.getLiteral("?ls").getString();
				String sbjAbstract = qs.getLiteral("?abstract").getString();
				
				relation.setSbjURI(s);
				relation.setObjURI(o);
				relation.setObjLabel(objLabel);
				relation.setSbjLabel(sbjLabel);
				relation.setSbjAbstract(sbjAbstract);
				
				listDBRelations.add(relation);
			}
			stat.setNumberResults(counterResults);
		}
		listStats.add(stat);
		return listDBRelations;
	}
	
	/*
	 * Utilities
	 */
	
	public String defineRelation(String relation) {
		String URI = "";
		String splitRelation[] = relation.split(":");
		if(splitRelation.length == 2)
			URI = "http://dbpedia.org/property/"+splitRelation[1];
		else
			URI = "http://dbpedia.org/ontology/"+relation;
		return URI;
	}
	
	public List<String> readLines(File inputFile){
		List<String> listLines = new ArrayList<String>();
		try(BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile),StandardCharsets.UTF_8))){
			String line;
			while((line = br.readLine()) != null) {
				listLines.add(line);
			}
			br.close();
		}catch(IOException e) {
			e.printStackTrace();
		}
		return listLines;
	}
	

}
