package reeval;

public class Annotation {

	private String anchor;
	private String URI;
	private String paragraphURI;
	private String sectionURI;
	
	public void printAnnotation() {
		System.out.println("Anchor = " + this.anchor + "\n"
				+ " URI = " + this.URI + "\n"
						+ "paragraphURI = " + this.paragraphURI + "\n"
								+ "sectionURI = " + this.sectionURI);
	}
	
	
	public String getAnchor() {
		return anchor;
	}
	public void setAnchor(String anchor) {
		this.anchor = anchor;
	}
	public String getURI() {
		return URI;
	}
	public void setURI(String uRI) {
		URI = uRI;
	}
	public String getParagraphURI() {
		return paragraphURI;
	}
	public void setParagraphURI(String paragraphURI) {
		this.paragraphURI = paragraphURI;
	}
	public String getSectionURI() {
		return sectionURI;
	}
	public void setSectionURI(String sectionURI) {
		this.sectionURI = sectionURI;
	}
}
