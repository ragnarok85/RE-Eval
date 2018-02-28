package reports;

public class FileReport {
	private String filePath;
	private int numAnnotations;
	private boolean real = true;
	
	public FileReport() {
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
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
