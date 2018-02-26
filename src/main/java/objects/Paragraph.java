package objects;

import java.util.ArrayList;
import java.util.List;

public class Paragraph {

	private List<AnnotationB> listAnnotations;
	private int id; 
	private int beginIndex;
	private int endIndex;
	private String paragraphURI;//uri
	private String paragraphText;
	
	public Paragraph() {
		this.listAnnotations = new ArrayList<AnnotationB>();
	}

	public List<AnnotationB> getListAnnotations() {
		return listAnnotations;
	}

	public void setListAnnotations(List<AnnotationB> listAnnotations) {
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

}
