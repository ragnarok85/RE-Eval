package objects;

public class AnnotationB {
	
	private int id;
	private int beginIndex; 
	private int endIndex;
	private String anchor;
	private String annotation; //URI
	private String paragraphURI;
	private String sectionURI;
	
	public AnnotationB(){
		
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
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

	public String getAnchor() {
		return anchor;
	}

	public void setAnchor(String anchor) {
		this.anchor = anchor;
	}

	public String getAnnotation() {
		return annotation;
	}

	public void setAnnotation(String annotation) {
		this.annotation = annotation;
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

	@Override
	public String toString() {
		return "AnnotationB [id=" + id + ", beginIndex=" + beginIndex + ", endIndex=" + endIndex + ", anchor=" + anchor
				+ ", annotation=" + annotation + ", paragraphURI=" + paragraphURI + ", sectionURI=" + sectionURI + "]";
	}
}
