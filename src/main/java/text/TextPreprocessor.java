package text;

import java.io.IOException;

import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.InvalidFormatException;

public class TextPreprocessor {

	private String[] punctuationMarks = {",","'",":","-","!","~","\\(","\\)","\\.","\"",";","\""};
	
	public TextPreprocessor() {
		// TODO Auto-generated constructor stub
	}

	public String[] sentenceDetector(SentenceModel model, String paragraph) throws IOException {
		SentenceDetectorME sdetector = new SentenceDetectorME(model);
		String[] sentences = sdetector.sentDetect(paragraph);
		return sentences;
	}
	
	public String[] tokenExtraction(TokenizerModel model, String sbjAnchor)
			throws InvalidFormatException, IOException {

		TokenizerME tokenizer = new TokenizerME(model);
		//int numCharacters = 0;

		String tokens[] = tokenizer.tokenize(sbjAnchor);
		//double tokenProbs[] = tokenizer.getTokenProbabilities();

		return tokens;

	}
	
	public String removeStopWords(String target) {
//		System.out.println(stopWordsPattern);
		//Pattern pattern = Pattern.compile("\\b(?:"+stopWordsPattern+")\\b\\s*",Pattern.CASE_INSENSITIVE);
//		Matcher matcher = pattern.matcher(target);
		for(String mark : punctuationMarks) {
			target = target.replaceAll(mark, " ");
		}
//		System.out.println(target);
		return target;
	}
}
