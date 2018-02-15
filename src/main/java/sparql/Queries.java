package sparql;

public enum Queries {

	CONTEXT("SELECT ?context WHERE { "
			+ " ?s <http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#isString> ?context "
			+ " }"),
	ABSTRACTLINKS("SELECT * WHERE {"
			+ " ?s a ?type."
			+ " FILTER (?type=<http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#Phrase> || ?type=<http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#Word>) "
			+ " ?s <http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#anchorOf> ?anchor; "
			+ "    <http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#superString> ?lsuper. "
			+ " ?lsuper <http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#superString> ?psuper. "
			+ " ?psuper <http://www.w3.org/2004/02/skos/core#notation> ?notation. "
			+ " FILTER(str(?notation)='0') "
			+ " }"),
	NUMSECTIONS("SELECT * WHERE {"
			+ " ?s <http://www.w3.org/2004/02/skos/core#notation> ?notation."
			+ " }ORDER BY DESC(?notation)"),
	TITLEANDNUMSECTION("SELECT * WHERE {"
			+ " ?s <http://www.w3.org/2004/02/skos/core#notation> ?notation. "
			+ " ?o <http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#superString> ?s. "
			+ " ?o a ?type. "
			+ " FILTER (?type=<http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#Title>)"
			+ " ?o <http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#anchorOf> ?title."
			+ " }ORDER BY ASC(?notation) "),
	SECTIONLINKS("SELECT * WHERE {"
			+ " ?s ?a ?type. "
			+ " FILTER (?type=<http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#Phrase> || ?type=<http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#Word>) "
			+ " ?s <http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#anchorOf> ?anchor;"
			+ "    <http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#superString> ?lsuper. "
			+ " ?lsuper <http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#superString> ?psuper. "
			+ " ?psuper <http://www.w3.org/2004/02/skos/core#notation> ?notation. "); //The query must be completed in 
								//the code section with "FILTER(str(?notation)="+VAR+") }" "
	private String query;
	
	Queries(String query){
		this.query = query;
	}
	
	public String query() {
		return this.query;
	}
}
