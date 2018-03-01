package sparql;

public enum Queries {

	CONTEXT("SELECT ?context WHERE { "
			+ " ?s <http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#isString> ?context "
			+ " }"),
	TITLEANDNUMSECTION("SELECT * WHERE {"
			+ " ?s <http://www.w3.org/2004/02/skos/core#notation> ?notation. "
			+ " ?o <http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#superString> ?s. "
			+ " ?o a ?type. "
			+ " FILTER (?type=<http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#Title>)"
			+ " ?o <http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#anchorOf> ?title."
			+ " }ORDER BY ASC(?notation) "),
	ABSTRACTANNOTATIONS(" PREFIX skos: <http://www.w3.org/2004/02/skos/core#> "
					+ "PREFIX nif: <http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#>"
					+ " SELECT ?bi ?ei ?anchor ?annotation ?section ?paragraph ?notation WHERE { "
					+ " ?section nif:hasParagraph ?paragraph;"
					+ " 		 skos:notation ?notation. "
					+ " ?annotation  nif:superString ?paragraph; "
					+ "				nif:anchorOf ?anchor; "
					+ "				nif:beginIndex ?bi; "
					+ "				nif:endIndex ?ei. "
					+ " FILTER regex(?anchor,\"**ANNOTATION**\", 'i') "
				//	+ " FILTER regex(str(?notation), \"0\",'i') "
					+ " }"),
	PARAGRAPH(" PREFIX nif: <http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#> "
			+ " SELECT ?bi ?ei WHERE { "
			+ " <**PARAGRAPH**> nif:beginIndex ?bi; "
			+ "  				nif:endIndex ?ei. "
			+ " } "),
	SECTION(" PREFIX nif: <http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#> "
			+ " SELECT ?bi ?ei WHERE { "
			+ " <**SECTION**> nif:beginIndex ?bi; "
			+ "               nif:endIndex ?ei. "
			+ " } ");
	
	
	private String query;
	
	Queries(String query){
		this.query = query;
	}
	
	public String query() {
		return this.query;
	}
}
