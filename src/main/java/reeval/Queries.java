package reeval;

public enum Queries {

	CONTEXT("SELECT ?context WHERE { ?s <http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#isString> ?context }"),
	ABSTRACTLINKS("SELECT * WHERE {"
			+ " ?s a ?type."
			+ " FILTER (?type=<http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#Phrase> || ?type=<http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#Word>) "
			+ " ?s <http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#anchorOf> ?anchor; "
			+ "    <http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#superString> ?lsuper. "
			+ " ?lsuper <http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#superString> ?psuper. "
			+ " ?psuper <http://www.w3.org/2004/02/skos/core#notation> ?notation. "
			+ " FILTER(str(?notation)='0') "
			+ " }");
	
	private String query;
	
	Queries(String query){
		this.query = query;
	}
	
	public String query() {
		return this.query;
	}
}
