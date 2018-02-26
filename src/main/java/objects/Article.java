package objects;

import java.util.ArrayList;
import java.util.List;

public class Article {

	private List<Section> listSections;
	private String name;
	private String context;
	
	public Article() {
		this.listSections = new ArrayList<Section>();
	}

	public List<Section> getListSections() {
		return listSections;
	}

	public void setListSections(List<Section> listSections) {
		this.listSections = listSections;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getContext() {
		return context;
	}

	public void setContext(String context) {
		this.context = context;
	}
	
	

}
