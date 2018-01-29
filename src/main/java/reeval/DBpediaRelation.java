package reeval;

public class DBpediaRelation {
	
	String sbjURI;
	String objURI;
	String sbjLabel;
	String objLabel;
	String targetRelation;
	String sbjAbstract;
	
	public DBpediaRelation() {
		
	}
	
	public DBpediaRelation(String targetRelation) {
		this.targetRelation = targetRelation;
	}
	
	public String getSbjURI() {
		return sbjURI;
	}
	
	public void setSbjURI(String sbjURI) {
		this.sbjURI = sbjURI;
	}
	
	public String getObjURI() {
		return objURI;
	}
	public void setObjURI(String objURI) {
		this.objURI = objURI;
	}
	public String getSbjLabel() {
		return sbjLabel;
	}
	public void setSbjLabel(String sbjLabel) {
		this.sbjLabel = sbjLabel;
	}
	public String getObjLabel() {
		return objLabel;
	}
	public void setObjLabel(String objLabel) {
		this.objLabel = objLabel;
	}
	public String getTagetRelation() {
		return targetRelation;
	}
	public void setTargetRelation(String targetRelation) {
		this.targetRelation = targetRelation;
	}
	public String getSbjAbstract() {
		return sbjAbstract;
	}
	public void setSbjAbstract(String sbjAbstract) {
		this.sbjAbstract = sbjAbstract;
	}

}
