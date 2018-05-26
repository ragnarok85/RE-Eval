package objects;

public class Annotation {

	private String anchor;
	private String uri;// uri
	private int id;
	private String paragraphURI;
	private String sectionURI;
	private String notation;
	private int beginIndex;
	private int endIndex;
	private String taIdentRef;

	public void printAnnotation() {
		System.out.println(
				"Anchor = " + this.anchor + "\n" + " URI = " + this.uri + "\n" + "paragraphURI = " + this.paragraphURI
						+ "\n" + "sectionURI = " + this.sectionURI + "\n" + "notation = " + this.notation + "\n");
	}

	public String getAnchor() {
		return anchor;
	}

	public void setAnchor(String anchor) {
		this.anchor = anchor;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
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

	public int getBeginIndex() {
		return beginIndex;
	}

	public void setBeginIndex(int beginIndex) {
		this.beginIndex = beginIndex;
	}

	public int getEndIndex() {
		return endIndex;
	}

	public void setEndIndex(int endIndex) {
		this.endIndex = endIndex;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getTaIdentRef() {
		return taIdentRef;
	}

	public void setTaIdentRef(String taIdentRef) {
		this.taIdentRef = taIdentRef;
	}
}
