package text;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.InvalidFormatException;

public class TextPreprocessor {

	private String[] punctuationMarks = {",","'",":","-","!","~","\\(","\\)","\\.","\"",";","\""};
	private SentenceModel sentenceModel = null;
	private TokenizerModel tokenizerModel = null;
//	public TextPreprocessor() {
//		// TODO Auto-generated constructor stub
//	}
	
	public TextPreprocessor() {
		FileInputStream enTokenizerModel = null;
		try {
			enTokenizerModel = readOpenNLPModel("OpenNLP/Models/Tokenizer/en-token.bin");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		FileInputStream enSentenceModel = null;
		try {
			enSentenceModel = readOpenNLPModel("OpenNLP/Models/SentenceDetector/en-sent.bin");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			sentenceModel = new SentenceModel(enSentenceModel);
			tokenizerModel = new TokenizerModel(enTokenizerModel);
		} catch (InvalidFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private FileInputStream readOpenNLPModel(String pathToModel) throws FileNotFoundException {
		ClassLoader classLoader = getClass().getClassLoader();
		FileInputStream fileModel = null;
		fileModel = new FileInputStream(classLoader.getResource(pathToModel).getFile());
		return fileModel;
	}

	public String[] sentenceDetector(String paragraph) throws IOException {
		SentenceDetectorME sdetector = new SentenceDetectorME(sentenceModel);
		String[] sentences = sdetector.sentDetect(paragraph);
		return sentences;
	}
	
	public String selectSentence(String[] sentences, int beginParagraph, int endIndex) {
		String sentence = "";
		int length = beginParagraph;
//		System.out.println("beginParagraph = " + beginParagraph);
//		System.out.println("endIndex = " + endIndex);
		for(int i = 0; i < sentences.length ; i++) {
//			System.out.println("endIndex (" + endIndex+") vs sentence.length (" + (sentences[i].length()+length)+")");
			if(endIndex <= (sentences[i].length()+length)) {
				sentence = sentences[i];
//				System.out.println("sentence = " + sentence);
				break;
			}
			length += sentences[i].length();
		}
		return sentence;
	}
	
	public String[] tokenExtraction(String sbjAnchor)
			throws InvalidFormatException, IOException {

		TokenizerME tokenizer = new TokenizerME(tokenizerModel);
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
