package objects;

import java.util.ArrayList;
import java.util.List;

public class Paragraph {

	private List<Annotation> listAnnotations;
	private String id; //uri
	private int beginIndex;
	private int endIndex;
	
	public Paragraph() {
		this.listAnnotations = new ArrayList<Annotation>();
	}

}
