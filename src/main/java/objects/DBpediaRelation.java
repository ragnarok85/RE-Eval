package objects;


public class DBpediaRelation {
	
	private String sbjURI;
	private String objURI;
	private String sbjLabel;
	private String objLabel;
	private String targetRelation;
	private String sbjAbstract;
	private String prdURI;
	private int id;
	
	@Override
	public boolean equals(Object other){
		if(other == null) return false;
		if(other == this) return true;
		if(!(other instanceof DBpediaRelation)) return false;
		DBpediaRelation otherRel = (DBpediaRelation) other;
		if (otherRel.getSbjURI().equals(this.getSbjURI()) && 
				otherRel.getObjURI().equals(this.getObjURI()))
			return true;
		return false;
	}
	
	public DBpediaRelation() {
		
	}
	
	public String printRelation() {
		return this.sbjURI + "\t"
				+ this.targetRelation + "\t"
						+ this.objURI + "\t"
								+ this.sbjAbstract + "\t";
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
	public void setTargetRelation(String targetRelation) {
		this.targetRelation = targetRelation;
	}
	public String getSbjAbstract() {
		return sbjAbstract;
	}
	public void setSbjAbstract(String sbjAbstract) {
		this.sbjAbstract = sbjAbstract;
	}

	public String getPrdURI() {
		return prdURI;
	}

	public void setPrdURI(String prdURI) {
		this.prdURI = prdURI;
	}

	public String getTargetRelation() {
		return targetRelation;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

}
