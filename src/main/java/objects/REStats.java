package objects;

public class REStats {
	
	String queryString;
	String subject;
	String predicate;
	String object;
	int numberResults;

	
	
	public String getQueryString() {
		return queryString;
	}
	public void setQueryString(String queryString) {
		this.queryString = queryString;
	}
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	public String getPredicate() {
		return predicate;
	}
	public void setPredicate(String predicate) {
		this.predicate = predicate;
	}
	public String getObject() {
		return object;
	}
	public void setObject(String object) {
		this.object = object;
	}
	public int getNumberResults() {
		return numberResults;
	}
	public void setNumberResults(int numberResults) {
		this.numberResults = numberResults;
	}
	

}
