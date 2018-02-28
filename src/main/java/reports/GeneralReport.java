package reports;

import java.util.ArrayList;
import java.util.List;

public class GeneralReport {
	
	private String targetRelation;
	private String sparqlQuery;
	private int DBpediaResults;
	private int foundInAbstract;
	private int foundInSection;
	private int filesWithAnnotation;
	private int filesWithoutAnnotation;
	private List<String> filesNotFounded;
	private int numberOffilesNotFound;
	private String processingTime;

	public GeneralReport() {
		this.filesNotFounded = new ArrayList<String>();
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

	public List<String> getFilesNotFounded() {
		return filesNotFounded;
	}

	public void setFilesNotFounded(List<String> filesNotFounded) {
		this.filesNotFounded = filesNotFounded;
	}

	public int getNumberOffilesNotFound() {
		return numberOffilesNotFound;
	}

	public void setNumberOffilesNotFound(int numberOffilesNotFound) {
		this.numberOffilesNotFound = numberOffilesNotFound;
	}

	@Override
	public String toString() {
		return "GeneralReport \n ProcessingTime\t" + processingTime + "\ntargetRelation\t" + targetRelation
				+ "\nsparqlQuery\t" + sparqlQuery 
				+ "\nDBpediaResults\t" + DBpediaResults 
				+ "\n\tfilesWithAnnotations\t" + filesWithAnnotation
				+ "\n\t\tAnnotationsfoundInAbstract\t" + foundInAbstract
				+ "\n\t\tAnnotationsfoundInSection\t" + foundInSection
				+ "\n\tfilesWithoutAnnotations\t" + filesWithoutAnnotation
				+ "\nnumberOffilesNotFound\t" + numberOffilesNotFound
				+ "\n\tfilesNotFounded\t" + filesNotFounded;
	}


	public String getProcessingTime() {
		return processingTime;
	}


	public void setProcessingTime(String processingTime) {
		this.processingTime = processingTime;
	}

	
}
