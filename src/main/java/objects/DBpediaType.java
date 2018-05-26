package objects;

public class DBpediaType {
	
	private String subject;
	private String object;
	private String predicate;
	private String type;
	
	public DBpediaType() {
		
	}
	public DBpediaType(String type) {
		this.type = type;
	}
	
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	public String getObject() {
		return object;
	}
	public void setObject(String object) {
		this.object = object;
	}
	public String getPredicate() {
		return predicate;
	}
	public void setPredicate(String predicate) {
		this.predicate = predicate;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	
	

}
