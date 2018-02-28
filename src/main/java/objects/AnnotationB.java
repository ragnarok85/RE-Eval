package objects;

public class AnnotationB {
	
	private int id;
	private int beginIndex; 
	private int endIndex;
	private String anchor;
	private String annotation; //URI
	private String textSegment;
	private Paragraph paragraph;
	private Section section;
	private DBpediaRelation rel;
	private String notation;
	private String foundIn; //opts = Abstract/Section
	
	public AnnotationB(){
		this.paragraph = new Paragraph();
		this.section = new Section();
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

	public Paragraph getParagraph() {
		return paragraph;
	}

	public void setParagraph(Paragraph paragraphURI) {
		this.paragraph = paragraphURI;
	}

	@Override
	public String toString() {
		return rel.getId() + "\t" + rel.getSbjLabel()
				+ "\t" + rel.getTargetRelation() + "\t" + rel.getObjLabel()
				+ "\t" + foundIn + "\t" + notation + "\t" + anchor + "\t" 
				+ textSegment + "\t" + annotation;
	}

	public String getTextSegment() {
		return textSegment;
	}

	public void setTextSegment(String textSegment) {
		this.textSegment = textSegment;
	}

	public Section getSection() {
		return section;
	}

	public void setSection(Section section) {
		this.section = section;
	}

	public DBpediaRelation getRel() {
		return rel;
	}

	public void setRel(DBpediaRelation rel) {
		this.rel = rel;
	}

	public String getFoundIn() {
		return foundIn;
	}

	public void setFoundIn(String foundIn) {
		this.foundIn = foundIn;
	}

	public String getNotation() {
		return notation;
	}

	public void setNotation(String notation) {
		this.notation = notation;
	}

}
