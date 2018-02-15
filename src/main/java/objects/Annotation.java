package objects;

public class Annotation {

	private String anchor;
	private String URI;
	private String paragraphURI;
	private String sectionURI;
	private String notation;
	
	public void printAnnotation() {
		System.out.println("Anchor = " + this.anchor + "\n"
								+ " URI = " + this.URI + "\n"
									+ "paragraphURI = " + this.paragraphURI + "\n"
										+ "sectionURI = " + this.sectionURI + "\n"
												+ "notation = " + this.notation + "\n");
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


	public String getNotation() {
		return notation;
	}


	public void setNotation(String notation) {
		this.notation = notation;
	}
}
