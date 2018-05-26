package sparql;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
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
import objects.DBpediaType;
import objects.Paragraph;
import objects.REStats;
import objects.Section;

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
				annotation.setUri(sol.getResource("?s").getURI());
				annotation.setParagraphURI(sol.getResource("?lsuper").getURI()); //linkSuperString --> Paragraph
				annotation.setSectionURI(sol.getResource("?psuper").getURI());//paragraphSuperString --> Section
				annotation.setNotation(sol.getLiteral("?notation").getString());
				annotation.setTaIdentRef(sol.getResource("?taIdentRef").getURI().replace("http://nif.dbpedia.org/wiki/en/", "http://dbpedia.org/resource/"));
				annotation.setBeginIndex(sol.getLiteral("?beginIndex").getInt());
				annotation.setEndIndex(sol.getLiteral("?endIndex").getInt());
				listAnnotations.add(annotation);
			}
		}
		return listAnnotations;
	}
	
	public List<Annotation> queryOrderAnnotation(Model model, String paragraph){
		List<Annotation> listAnnotation = new ArrayList<Annotation>();
		String queryString = Queries.ANNOTATIONORDERBYINDEX.query().replace("**PARAGRAPH**", paragraph);
		Query query = QueryFactory.create(queryString);
		try(QueryExecution qexec = QueryExecutionFactory.create(query, model)){
			ResultSet rs = qexec.execSelect();
			int counter = 0;
			while(rs.hasNext()){
				QuerySolution qs = rs.next();
				Annotation ann = new Annotation();
				int bi = qs.getLiteral("?bi").getInt();
				int ei = qs.getLiteral("?ei").getInt();
				String anchor = qs.getLiteral("?anchor").getString();
				String annotation = qs.getResource("?annotation").getURI();
				
				ann.setAnchor(anchor);
				ann.setBeginIndex(bi);
				ann.setEndIndex(ei);
				ann.setUri(annotation);
				ann.setId(counter++);
			}
		}
		
		return listAnnotation;
	}
	
	public List<Section> queryOrderSection(Model model){
		List<Section> listSections = new ArrayList<Section>();
		
		String queryString = Queries.SECTIONODERBYINDEX.query();
		Query query =  QueryFactory.create(queryString);
		try(QueryExecution qexec = QueryExecutionFactory.create(query, model)){
			ResultSet rs = qexec.execSelect();
			int counterId = 0;
			while(rs.hasNext()){
				Section s = new Section();
				QuerySolution qs = rs.next();
				int bi = qs.getLiteral("?bi").getInt();
				s.setBeginIndex(bi);
				s.setEndIndex(qs.getLiteral("?ei").getInt());
				s.setSectionURI(qs.getResource("?section").getURI());
				s.setId(counterId++);
				listSections.add(s);
			}
		}
		
		return listSections;
	}
	
	public List<Paragraph> queryOrdererParagraph(Model model, String section){
		List<Paragraph> listParagraph = new ArrayList<Paragraph>();
		String queryString = Queries.PARAGRAOHODERBYINDEX.query().replace("**SECTION**", "<"+section + ">");
		Query query = QueryFactory.create(queryString);
		try(QueryExecution qexec = QueryExecutionFactory.create(query, model)){
			ResultSet rs = qexec.execSelect();
			int counterId = 0;
			while(rs.hasNext()){
				Paragraph p = new Paragraph();
				QuerySolution qs = rs.next();
				int bi = qs.getLiteral("?bi").getInt();
				p.setBeginIndex(bi);
				p.setEndIndex(qs.getLiteral("?ei").getInt());
				p.setParagraphURI(qs.getResource("paragraph").getURI());
				p.setId(counterId++);
				listParagraph.add(p);
			}
		}
		return listParagraph;
	}
	
	/*
	 * SPARQL DBpedia
	 */
	public List<DBpediaType> queryDBpediaType(String queryString, String targetType) throws UnsupportedEncodingException {
		List<DBpediaType> listDBRelations = new ArrayList<DBpediaType>();
		
		Query query = QueryFactory.create(queryString);
		
		try(QueryExecution qexec = QueryExecutionFactory.sparqlService(service, query)){
			ResultSet result = qexec.execSelect();
			int counterResults = 0;
			while(result.hasNext()) {
				DBpediaType type = new DBpediaType(targetType);
				
				QuerySolution qs = result.next();
				
				String s = new String(qs.getResource("?s").getURI().getBytes(),"UTF-8");
				String o = new String(qs.getResource("?o").getURI().getBytes(),"UTF-8");
				
				type.setSubject(s);
				type.setObject(o);
				
				listDBRelations.add(type);
			}
		}
		return listDBRelations;
	}
	
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
				
				String s = new String(qs.getResource("?s").getURI().getBytes(),"UTF-8");
				String o = new String(qs.getResource("?o").getURI().getBytes(),"UTF-8");
				String p = qs.getResource("?p").getURI();
				
				relation.setId(counterResults++);
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
	
	
	
	public String queryContext(Model model) throws FileNotFoundException {
		String context = "";
		String queryString = Queries.CONTEXT.query();
		Query query = QueryFactory.create(queryString);
		PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream("test.ttl")));
		model.write(pw,"NTRIPLES");
		try(QueryExecution qexec = QueryExecutionFactory.create(query, model)){
			ResultSet result = qexec.execSelect();
			while(result.hasNext()) {
				QuerySolution sol = result.next();
				context = sol.getLiteral("?context").getString().replace("\\\"", "\"");
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
				annotation.setUri(sol.getResource("?s").getURI());
				annotation.setParagraphURI(sol.getResource("?lsuper").getURI()); //linkSuperString --> Paragraph
				annotation.setSectionURI(sol.getResource("?psuper").getURI());//paragraphSuperString --> Section
				annotation.setNotation(sol.getLiteral("?notation").getString());
				listAnnotations.add(annotation);
			}
		}
		return listAnnotations;
	}

}
