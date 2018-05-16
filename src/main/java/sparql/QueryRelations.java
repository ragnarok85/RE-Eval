package sparql;

public enum QueryRelations {
	AWARDS("SELECT ?o WHERE { =?S= <http://dbpedia.org/ontology/award> ?o. ?o a <http://dbpedia.org/ontology/Award>. }"),
	CHILDREN("SELECT ?o WHERE { =?S= <http://dbpedia.org/ontology/child> ?o. ?o a <http://dbpedia.org/ontology/Person>. }"),
	CITYOFBIRTH(""),
	CITYOFRESIDENCE(""),
	EMPLOYER(""),
	NATIONALITY(""),
	OCCUPATION(""),
	PARENTS(""),
	PARTYAFFILIATION(""),
	SCHOOLATTENDED(""),
	SIBLINGS(""),
	SPOUSE("");
	
	private String query;
	
	QueryRelations(String query){
		this.query = query;
	}
	
	public String query() {
		return this.query;
	}
}
