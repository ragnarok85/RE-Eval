package sparql;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;

import objects.Annotation;

public class SparqlMoreRelations {

	public List<Annotation> queryAbstractAnnotations(Model model, String subject) {
		List<Annotation> listAnnotations = new ArrayList<Annotation>();
		
		String queryString = QueryRelations.AWARDS.query().replace("=?S=",subject);
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
