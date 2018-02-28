package reports;

public class GeneralReport {
	
	private String targetRelation;
	private String sparqlQuery;
	private int DBpediaResults;
	private int foundInAbstract;
	private int foundInSection;
	private int filesWithAnnotation;
	private int filesWithoutAnnotation;
	private int filesNotFounded;
	
	private int numberOffilesNotFound;

	public GeneralReport() {
	}

	public String getTargetRelation() {
		return targetRelation;
	}

	public void setTargetRelation(String targetRelation) {
		this.targetRelation = targetRelation;
	}

	public String getSparqlQuery() {
		return sparqlQuery;
	}

	public void setSparqlQuery(String sparqlQuery) {
		this.sparqlQuery = sparqlQuery;
	}

	public int getDBpediaResults() {
		return DBpediaResults;
	}

	public void setDBpediaResults(int dBpediaResults) {
		DBpediaResults = dBpediaResults;
	}

	public int getFoundInAbstract() {
		return foundInAbstract;
	}

	public void setFoundInAbstract(int foundInAbstract) {
		this.foundInAbstract = foundInAbstract;
	}

	public int getFoundInSection() {
		return foundInSection;
	}

	public void setFoundInSection(int foundInSection) {
		this.foundInSection = foundInSection;
	}

	public int getFilesWithAnnotation() {
		return filesWithAnnotation;
	}

	public void setFilesWithAnnotation(int filesWithAnnotation) {
		this.filesWithAnnotation = filesWithAnnotation;
	}

	public int getFilesWithoutAnnotation() {
		return filesWithoutAnnotation;
	}

	public void setFilesWithoutAnnotation(int filesWithoutAnnotation) {
		this.filesWithoutAnnotation = filesWithoutAnnotation;
	}

	public int getFilesNotFounded() {
		return filesNotFounded;
	}

	public void setFilesNotFounded(int filesNotFounded) {
		this.filesNotFounded = filesNotFounded;
	}

	public int getNumberOffilesNotFound() {
		return numberOffilesNotFound;
	}

	public void setNumberOffilesNotFound(int numberOffilesNotFound) {
		this.numberOffilesNotFound = numberOffilesNotFound;
	}

	
}
