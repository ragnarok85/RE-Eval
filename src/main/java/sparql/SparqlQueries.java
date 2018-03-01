package sparql;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import objects.AnnotationB;
import objects.DBpediaRelation;
import objects.Paragraph;
import objects.Section;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.log4j.Logger;

public class SparqlQueries {
	
	private static Logger logger = Logger.getLogger(SparqlQueries.class);
	
	private static String service = "http://dbpedia.org/sparql";

	public SparqlQueries() {
	}
	
	public List<AnnotationB> queryAnnotationAbstract(Model model, String obj){
		List<AnnotationB> listAnnotations = new ArrayList<AnnotationB>();
		obj = obj.replace("+", ""); //replacing the pattern symbol +
		String queryString = Queries.ABSTRACTANNOTATIONS.query().replace("**ANNOTATION**", obj);
//		System.out.println(queryString);
		Query query = QueryFactory.create(queryString);
		
		try(QueryExecution qexec = QueryExecutionFactory.create(query, model)){
			ResultSet rs = qexec.execSelect();
			while(rs.hasNext()){
				QuerySolution qs = rs.next();
				AnnotationB ann = new AnnotationB();
				//?bi ?ei ?anchor ?annotation ?section ?paragraph
				int bi = qs.getLiteral("?bi").getInt();
				int ei = qs.getLiteral("?ei").getInt();
				String anchor = qs.getLiteral("?anchor").getString();
				String annotation = qs.getResource("?annotation").getURI();
				String section = qs.getResource("?section").getURI();
				String paragraph = qs.getResource("?paragraph").getURI();
				String notation = qs.getLiteral("?notation").getString();
				
				if(!notation.substring(0, 1).equals("0"))
					ann.setFoundIn("Section");
				else
					ann.setFoundIn("Abstract");
				
				if(obj.equalsIgnoreCase(anchor))
					ann.setKindOfMatch("exact");
				else
					ann.setKindOfMatch("partial");
				
				ann.setAnchor(anchor);
				ann.setAnnotation(annotation);
				ann.setBeginIndex(bi);
				ann.setEndIndex(ei);
				ann.getParagraph().setParagraphURI(paragraph);;
				ann.getSection().setSectionURI(section);
				ann.setNotation(notation);
				listAnnotations.add(ann);
				//listAnnotations.add( bi + "\t" + ei + "\t" + anchor + "\t" + annotation + "\t" + section + "\t" + paragraph);
			}
		}
		return listAnnotations;
	}
	
	public void queryParagraph(Model model, Paragraph p){
		String queryString = Queries.PARAGRAPH.query().replace("**PARAGRAPH**", p.getParagraphURI());
		Query query = QueryFactory.create(queryString);
		try(QueryExecution qexec = QueryExecutionFactory.create(query, model)){
			ResultSet rs = qexec.execSelect();
			while(rs.hasNext()){
				QuerySolution qs = rs.next();
				p.setBeginIndex(qs.getLiteral("?bi").getInt());
				p.setEndIndex(qs.getLiteral("?ei").getInt());
			}
		}
	}
	
	public void querySection(Model model, Section s){
		String queryString = Queries.SECTION.query().replace("**SECTION**", s.getSectionURI());
		Query query = QueryFactory.create(queryString);
		try(QueryExecution qexec = QueryExecutionFactory.create(query, model)){
			ResultSet rs = qexec.execSelect();
			while(rs.hasNext()){
				QuerySolution qs = rs.next();
				s.setBeginIndex(qs.getLiteral("?bi").getInt());
				s.setEndIndex(qs.getLiteral("?ei").getInt());
			}
		}
	}
	
	
	
	/*
	 * SPARQL DBpedia
	 */
	
	public List<DBpediaRelation> queryDBpediaRelations(String queryString, String targetRelation) throws UnsupportedEncodingException {
		List<DBpediaRelation> listDBRelations = new ArrayList<DBpediaRelation>();
		
		
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
		}
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
}
