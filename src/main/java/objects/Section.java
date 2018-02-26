package objects;

import java.util.ArrayList;
import java.util.List;

public class Section {

	private List<Paragraph> listParagraphs;
	private int id; 
	private int beginIndex;
	private int endIndex;
	private String sectionURI; //uri
	private String sectionText;
	public Section() {
		this.listParagraphs = new ArrayList<Paragraph>();
	}

	public List<Paragraph> getListParagraphs() {
		return listParagraphs;
	}

	public void setListParagraphs(List<Paragraph> listParagraphs) {
		this.listParagraphs = listParagraphs;
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

	public String getSectionURI() {
		return sectionURI;
	}

	public void setSectionURI(String sectionURI) {
		this.sectionURI = sectionURI;
	}

	public String getSectionText() {
		return sectionText;
	}

	public void setSectionText(String sectionText) {
		this.sectionText = sectionText;
	}

}
