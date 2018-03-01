package objects;

import java.util.ArrayList;
import java.util.List;

public class Paragraph {

	private List<Annotation> listAnnotations;
	private int id; 
	private int beginIndex;
	private int endIndex;
	private String paragraphURI;//uri
	private String paragraphText;
	private Section section;
	
	public Paragraph() {
		this.listAnnotations = new ArrayList<Annotation>();
		this.section = new Section();
	}

	public List<Annotation> getListAnnotations() {
		return listAnnotations;
	}

	public void setListAnnotations(List<Annotation> listAnnotations) {
		this.listAnnotations = listAnnotations;
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

	public String getParagraphURI() {
		return paragraphURI;
	}

	public void setParagraphURI(String paragraphURI) {
		this.paragraphURI = paragraphURI;
	}

	public String getParagraphText() {
		return paragraphText;
	}

	public void setParagraphText(String paragraphText) {
		this.paragraphText = paragraphText;
	}

	public Section getSection() {
		return section;
	}

	public void setSection(Section section) {
		this.section = section;
	}

	@Override
	public String toString() {
		return "Paragraph [listAnnotations=" + listAnnotations + ", id=" + id
				+ ", beginIndex=" + beginIndex + ", endIndex=" + endIndex
				+ ", paragraphURI=" + paragraphURI + ", paragraphText="
				+ paragraphText + ", sectionURI=" + section + "]";
	}

}
