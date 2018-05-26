package sparql;

public enum QueryRelations {
	AWARDS("SELECT ?o (<http://dbpedia.org/ontology/award> as ?p) WHERE { "
			+ "=S= <http://dbpedia.org/ontology/award> ?o. "
			+ " ?o a <http://dbpedia.org/ontology/Award>. }"),
	CHILDREN("SELECT ?o (<http://dbpedia.org/ontology/child> as ?p) WHERE { "
			+ "=S= <http://dbpedia.org/ontology/child> ?o. "
			+ "?o a <http://dbpedia.org/ontology/Person>. }"),
	CITYOFBIRTH("select ?o (<http://dbpedia.org/ontology/birthPlace> as ?p) where { "
			+ " =S= <http://dbpedia.org/ontology/birthPlace> ?o . "
			+ " ?o a <http://dbpedia.org/ontology/City> . }"),
	CITYOFRESIDENCE("select ?o (<http://dbpedia.org/ontology/residence> as ?p) where { " + 
			"=S= <http://dbpedia.org/ontology/residence> ?o . " + 
			"?o a <http://dbpedia.org/ontology/City> . }"),
	EMPLOYER("select ?o (<http://dbpedia.org/ontology/employer> as ?p) where { " + 
			"=S= <http://dbpedia.org/ontology/employer> ?o.  " + 
			"?o a <http://dbpedia.org/ontology/Organisation>. }"),
	NATIONALITY("select ?o (<http://dbpedia.org/ontology/nationality> as ?p) where { " + 
			"=S= <http://dbpedia.org/ontology/nationality> ?o . " + 
			"?o a <http://dbpedia.org/ontology/Country> . }"),
	OCCUPATION("select ?o (<http://dbpedia.org/ontology/occupation> as ?p) where {  " + 
			"=S= <http://dbpedia.org/ontology/occupation> ?ot . " + 
			"?ot a <http://dbpedia.org/ontology/PersonFunction> . " + 
			"=S= <http://purl.org/linguistics/gold/hypernym> ?o .}"),
	PARENTS("select ?o (<http://dbpedia.org/ontology/parent> as ?p) where { " + 
			"=S= <http://dbpedia.org/ontology/parent> ?o . " + 
			"?o a <http://dbpedia.org/ontology/Person> . }"),
	PARTYAFFILIATION("select ?o (<http://dbpedia.org/ontology/party> as ?p) where { " + 
			"=S= <http://dbpedia.org/ontology/party> ?o . " + 
			"?o a <http://dbpedia.org/ontology/PoliticalParty> . }"),
	SCHOOLATTENDED("select ?o (<http://dbpedia.org/ontology/almaMater> as ?p) where { " + 
			"=S= <http://dbpedia.org/ontology/almaMater> ?o . " + 
			"?o a <http://dbpedia.org/ontology/EducationalInstitution>. }"),
	SIBLINGS("select ?o (<http://dbpedia.org/ontology/child> as ?p) where { " + 
			"=S= <http://dbpedia.org/ontology/child> ?o . " + 
			"?o a <http://dbpedia.org/ontology/Person> . }"),
	SPOUSE("select ?o (<http://dbpedia.org/ontology/spouse> as ?p) where { " + 
			"=S= <http://dbpedia.org/ontology/spouse> ?o . " + 
			"?o a <http://dbpedia.org/ontology/Person> . }");
	
	private String query;
	
	QueryRelations(String query){
		this.query = query;
	}
	
	public String query() {
		return this.query;
	}
}
