package reports;

public class FileReport {
	private String filePath;
	private String subjectURI;
	private String objectURI;
	private String subjectAnchor;
	private String objectAnchor;
	private int numAnnotations;
	private boolean real = true;
	
	public FileReport() {
		// TODO Auto-generated constructor stub
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public String getSubjectURI() {
		return subjectURI;
	}

	public void setSubjectURI(String subjectURI) {
		this.subjectURI = subjectURI;
	}

	public String getObjectURI() {
		return objectURI;
	}

	public void setObjectURI(String objectURI) {
		this.objectURI = objectURI;
	}

	public String getSubjectAnchor() {
		return subjectAnchor;
	}

	public void setSubjectAnchor(String subjectAnchor) {
		this.subjectAnchor = subjectAnchor;
	}

	public String getObjectAnchor() {
		return objectAnchor;
	}

	public void setObjectAnchor(String objectAnchor) {
		this.objectAnchor = objectAnchor;
	}

	public int getNumAnnotations() {
		return numAnnotations;
	}

	public void setNumAnnotations(int numAnnotations) {
		this.numAnnotations = numAnnotations;
	}

	public boolean isReal() {
		return real;
	}

	public void setReal(boolean real) {
		this.real = real;
	}

}
