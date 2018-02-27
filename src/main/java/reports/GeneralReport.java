package reports;

public class GeneralReport {
	
	private String filePath;
	private String targetRelation;
	private String sparqlQuery;
	private int DBpediaResults;
	private int foundInAbstract;
	private int notFoundInAbstract;
	private int numberOffilesNotFound;

	public GeneralReport() {
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
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

	public int getNotFoundInAbstract() {
		return notFoundInAbstract;
	}

	public void setNotFoundInAbstract(int notFoundInAbstract) {
		this.notFoundInAbstract = notFoundInAbstract;
	}

	public int getNumberOffilesNotFound() {
		return numberOffilesNotFound;
	}

	public void setNumberOffilesNotFound(int numberOffilesNotFound) {
		this.numberOffilesNotFound = numberOffilesNotFound;
	}

}
