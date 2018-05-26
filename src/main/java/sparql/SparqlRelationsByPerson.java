package sparql;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;

import objects.DBpediaRelation;
import objects.DBpediaType;
import objects.REStats;

public class SparqlRelationsByPerson {
	
	private static String service = "http://dbpedia.org/sparql";

	public List<DBpediaRelation> queryDBpediaRelations(String queryString, String targetRelation, String sbjUri, List<REStats> listStats) throws UnsupportedEncodingException {
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
				
				String o = new String(qs.getResource("?o").getURI().getBytes(),"UTF-8");
				String p = qs.getResource("?p").getURI();
				
				relation.setId(counterResults++);
				relation.setSbjURI(sbjUri);
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
}
