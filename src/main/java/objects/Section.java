package objects;

import java.util.ArrayList;
import java.util.List;

public class Section {

	private List<Paragraph> listParagraphs;
	private int id; //uri
	private int beginIndex;
	private int endIndex;
	
	public Section() {
		this.listParagraphs = new ArrayList<Paragraph>();
	}

}
