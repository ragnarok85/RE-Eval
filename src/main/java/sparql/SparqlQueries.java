package sparql;

import java.io.UnsupportedEncodingException;
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
import org.apache.jena.rdf.model.Model;
import org.apache.log4j.Logger;

import objects.Annotation;
import objects.DBpediaRelation;
import objects.REStats;

public class SparqlQueries {
	
	private static Logger logger = Logger.getLogger(SparqlQueries.class);
	
	private static String service = "http://dbpedia.org/sparql";

	public SparqlQueries() {
		// TODO Auto-generated constructor stub
	}
	
	
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
				annotation.setNotation(sol.getLiteral("?notation").getString());
				listAnnotations.add(annotation);
			}
		}
		return listAnnotations;
	}
	
	/*
	 * SPARQL DBpedia
	 */
	
	public List<DBpediaRelation> queryDBpediaRelations(String queryString, String targetRelation, List<REStats> listStats) throws UnsupportedEncodingException {
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
//				if(counterResults > 500)
//					break;
			}
			stat.setPredicate(targetRelation);
			stat.setNumberResults(counterResults);
		}
		listStats.add(stat);
		return listDBRelations;
	}
	
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
	
	public List<String> queryNumberSections(Model model) {
		List<String> listSections = new ArrayList<String>();
		
		String queryString = Queries.NUMSECTIONS.query();
		Query query = QueryFactory.create(queryString);
		try(QueryExecution qexec = QueryExecutionFactory.create(query, model)){
			ResultSet result = qexec.execSelect();
			while(result.hasNext()) {
				QuerySolution sol = result.next();
				listSections.add(sol.getLiteral("?notation").getString());
			}
		}
		return listSections;
	}
	
	public Map<String,String> queryNumberAndTitleSections(Model model) {
		Map<String,String> mapSections = new HashMap<String,String>();
		
		String queryString = Queries.TITLEANDNUMSECTION.query();
		Query query = QueryFactory.create(queryString);
		try(QueryExecution qexec = QueryExecutionFactory.create(query, model)){
			ResultSet result = qexec.execSelect();
			while(result.hasNext()) {
				QuerySolution sol = result.next();
				String numSection = sol.getLiteral("?notation").getString();
				String titleSection = sol.getLiteral("?title").getString();
				mapSections.put(numSection,titleSection);
			}
		}
		return mapSections;
	}
	
	public List<Annotation> querySectionAnnotations(Model model, String section) {
		List<Annotation> listAnnotations = new ArrayList<Annotation>();
		
		String queryString = Queries.SECTIONLINKS.query() + " FILTER (str(?notation)='"+section+"') }";
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
				annotation.setNotation(sol.getLiteral("?notation").getString());
				listAnnotations.add(annotation);
			}
		}
		return listAnnotations;
	}

}
