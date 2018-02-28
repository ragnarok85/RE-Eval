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
			+ " ?psuper <http://www.w3.org/2004/02/skos/core#notation> ?notation. "), //The query must be completed in 
								//the code section with "FILTER(str(?notation)="+VAR+") }" "
	SECTIONODERBYINDEX("SELECT ?bi ?ei ?section ? paragraph WHERE { "
			+ " ?section <http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#hasParagraph> ?paragraph; "
			+ " 		 <http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#beginIndex> ?bi. "
			+ "			 <http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#endIndex> ?ei. "
			+ " } ORDER BY ?bi "),
	PARAGRAOHODERBYINDEX("SELECT ?bi ?ei ?paragraph WHERE { "
			+ " **SECTION** <http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#hasParagraph> ?paragraph. "
			+ " ?paragraph   <http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#beginIndex> ?bi; "
			+ "				 <http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#endIndex> ?ei. "
			+ " } ORDER BY ?bi "),
	SECTIONSANNOTATIONS("PREFIX nif: <http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#> "
			+ " SELECT ?bi ?ei ?anchor ?annotation ?section ?paragraph WHERE { "
			+ " ?section nif:hasParagraph ?paragraph. "
			+ " ?annotation  nif:superString ?paragraph; "
			+ "				nif:anchorOf ?anchor; "
			+ "				nif:beginIndex ?bi; "
			+ "				nif:endIndex ?ei. "
			+ " FILTER regex(?anchor,\"**ANNOTATION**\", 'i') "
			+ " FILTER regex(str(?notation), \"[1-9].*(?!0)\",'i') "
			+ " } "),
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
	ANNOTATIONORDERBYINDEX("PREFIX nif: <http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#>"
					+ " SELECT ?bi ?ei ?anchor ?annotation WHERE { "
					+ " ?annotation  nif:superString <**PARAGRAPH**>; "
					+ "				nif:anchorOf ?anchor; "
					+ "				nif:beginIndex ?bi; "
					+ "				nif:endIndex ?ei. "
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
