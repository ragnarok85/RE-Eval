package reports;

public class Report {
	private String anchor;
	private String subject;
	private String relation;
	private String object;
	private String context;
	private String fileName;
	private String sentence;
	private String kindOfMatch; // Equals, Contain
	private String approach; //Sbj-Obj, Pronoun-Obj
	private String extraction; //In Sentence, In Paragraph
	private String blankSentence = ""; // X - only if the relation have no sentence
	private String sectionTitle = "";
	private String sectionNumber = "";
	
	public String printReport() {
		return this.kindOfMatch + "\t"
				+ this.sectionNumber + "\t"
					+ this.sectionTitle + "\t"
						+ this.extraction + "\t"
							+ this.approach + "\t"
								+ this.blankSentence + "\t"
									+ this.anchor + "\t"
										+ this.subject + "\t"
											+ this.relation +"\t"
												+ this.object + "\t"
													+ this.sentence + "\t"
														+ this.context + "\t"; 
	}
	
	public String printMilanReport() {
		return  this.sentence + "\t"
					+ this.relation +"\t"
						+ this.subject + "\t"
							+ this.object;
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

	public String getAnchor() {
		return anchor;
	}

	public void setAnchor(String anchor) {
		this.anchor = anchor;
	}

	public String getApproach() {
		return approach;
	}

	public void setApproach(String approach) {
		this.approach = approach;
	}

	public String getExtraction() {
		return extraction;
	}

	public void setExtraction(String extraction) {
		this.extraction = extraction;
	}

	public String getBlankSentence() {
		return blankSentence;
	}

	public void setBlankSentence(String blankSentence) {
		this.blankSentence = blankSentence;
	}

	public String getSectionTitle() {
		return sectionTitle;
	}

	public void setSectionTitle(String sectionTitle) {
		this.sectionTitle = sectionTitle;
	}

	public String getSectionNumber() {
		return sectionNumber;
	}

	public void setSectionNumber(String sectionNumber) {
		this.sectionNumber = sectionNumber;
	}

}
