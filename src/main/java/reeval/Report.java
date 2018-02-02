package reeval;

public class Report {
	private String subject;
	private String relation;
	private String object;
	private String context;
	private String fileName;
	private String sentence;
	private String kindOfMatch;
	
	public String printReport() {
		return this.kindOfMatch + "\t"
				+ this.subject + "\t"
					+ this.relation +"\t"
						+ this.object + "\t"
								+ this.sentence + "\t"
									+ this.context + "\t"; 
	}
	
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	public String getRelation() {
		return relation;
	}
	public void setRelation(String relation) {
		this.relation = relation;
	}
	public String getObject() {
		return object;
	}
	public void setObject(String object) {
		this.object = object;
	}
	public String getContext() {
		return context;
	}
	public void setContext(String context) {
		this.context = context;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getKindOfMatch() {
		return kindOfMatch;
	}

	public void setKindOfMatch(String kindOfMatch) {
		this.kindOfMatch = kindOfMatch;
	}

	public String getSentence() {
		return sentence;
	}

	public void setSentence(String sentence) {
		this.sentence = sentence;
	}

}
