package reeval;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.InvalidFormatException;

public class TestIDeas {
	
	private String[] punctuationMarks = {",","'",":","-","!","~","\\(","\\)","\\.","\"",";"};
//	private String stopWordsPattern = String.join("|", punctuationMarks);
	
	
	public static void main(String[] args) throws InvalidFormatException, IOException {
		TestIDeas id = new TestIDeas();
		String paragraph = "Diane Legault (born July 21, 1956) is a Quebec dentist and politician. She was the member of the National Assembly for Chambly in Quebec. She represented the Liberal Party of Quebec and resigned on November 15, 2006. On November 7, 2006, she was elected President of the Ordre des dentistes du Québec, the professional order representing all the dentists in Quebec, for a five-year term. She was the first woman to hold this position. Born in Montreal, Quebec, she received a Doctor in Dentistry from Université de Montréal in 1979 and a M.B.A. from Université de Sherbrooke in 1995. She was Director of Professional Services for the Ordre des dentistes from 1996 to 1998 and Executive Director and Secretary from 1998 to 2003.\n";
		String subject = "Diane Legault";
		String anchor = "Montreal"; 
		
		FileInputStream enTokenizerModel = id.readOpenNLPModel("OpenNLP/Models/Tokenizer/en-token.bin");
//		FileInputStream enSentenceModel = id.readOpenNLPModel("OpenNLP/Models/SentenceDetector/en-sent.bin");
		TokenizerModel tokenModel = new TokenizerModel(enTokenizerModel);
//		SentenceModel sntModel = new SentenceModel(enSentenceModel);
		
		System.out.println("Processing paragraph: " + paragraph);
		String sentence = "";
		int indexSbj = id.lookIndex(tokenModel, paragraph, anchor, subject);
		int indexObj = id.lookObjIndex(tokenModel, paragraph, anchor);
		
		System.out.println("indexSbj = " + indexSbj);
		System.out.println("indexObj = " + indexObj);
		
		if(indexSbj != -1 ) {
			if(indexSbj > indexObj)
				sentence = "";
			else 
				sentence = paragraph.substring(indexSbj, indexObj+anchor.length());
			System.out.println("sentence length = " + sentence.length());
			if(sentence.length() == 0) {
				System.out.println("\n\nLooking for pronoun");
				sentence = id.detectPronounInSentence(tokenModel, paragraph, anchor, subject);
			}
			if(sentence.length() == 0) {
				System.out.println("\n\n looking only the object");
				sentence = id.detectOnlyObjectInSentence(tokenModel, paragraph, anchor);
			}
			
			
			System.out.println("Final = " + sentence);
		}
		
		
		
	}
	
	public String detectPronounInSentence(TokenizerModel model, String snt, String objAnchor, 
			String subject)	throws InvalidFormatException, IOException {
		/*
		 * The relation is hold by a pronoun (she is the mother of ...) Subject must
		 * match a pronoun as he, she, his or her Object must match complete to avoid
		 * inconsistencies
		 */

		System.out.println("Object = " + objAnchor);
		String sentence = "";
		String index = lookPronounSbjIndex(model, snt);
		int sIndex = Integer.parseInt(index.split("--")[0]);
		String pronoun = "";
		if(!(sIndex == -1))
			pronoun = index.split("-")[1];
		int oIndex = lookObjIndex(model, snt, objAnchor);
		System.out.println("sIndex = " + sIndex + " - oIndex = " + oIndex);
		if (sIndex >= 0 && sIndex < oIndex) {
			if (oIndex > 0)
				oIndex += objAnchor.length();
			if (oIndex > snt.length())
				oIndex = snt.length();
			System.out.println(sIndex + " -- " + oIndex + "--" + snt.length());
			if (sIndex < oIndex) {
				sentence = snt.substring(sIndex, oIndex);
				sentence = "(**" + pronoun + "=" + subject + "**): " + sentence;
				System.out.println("Sentence:\n" + sentence);
				System.out.println();
			}
		}
		System.out.println("\n\n");
		return sentence;
	}
	
	public String detectOnlyObjectInSentence(TokenizerModel model, String snt, 
			String objAnchor)throws InvalidFormatException, IOException {
		/*
		 * The relation is hold by a pronoun (she is the mother of ...) Subject must
		 * match a pronoun as he, she, his or her Object must match complete to avoid
		 * inconsistencies
		 */

		System.out.println("Object = " + objAnchor);
		String sentence = "";
		int oIndex = lookObjIndex(model, snt, objAnchor);
		System.out.println("oIndex = " + oIndex);
		if (oIndex > 0) {
			oIndex += objAnchor.length();
			if (oIndex > snt.length())
				oIndex = snt.length();
			System.out.println(oIndex + "--" + snt.length());
			sentence = snt.substring(oIndex);
			System.out.println("Sentence:\n" + sentence);
			System.out.println();
		}
		System.out.println("\n\n");
		return sentence;
	}
	
	public int lookIndex(TokenizerModel tokenModel, String paragraph, String anchor, String subject) throws InvalidFormatException, IOException {
		int index = 0;
		paragraph = removeStopWords(paragraph).toLowerCase();
		subject = removeStopWords(subject).toLowerCase();
		anchor = removeStopWords(anchor).toLowerCase();
		
		String[] pt = tokenExtraction(tokenModel,paragraph);
		String[] a = tokenExtraction(tokenModel, subject);
		
		int pPos = 0;
		int indexTemp = 0;
		String target = "";
		boolean onlyOne = false;
		System.out.println("====SUBJECT====");
		ext:for(int i = 0; i < pt.length; i++) {
			for(int j = 0;  j < a.length; j++) {
				System.out.println(pt[i] + " vs. " + a[j] + " ? " + pt[i].contains(a[j]));
				if(pt[i].contains(a[j])) {//add she, he, his, her
					pPos = i;
					target = pt[i];
					onlyOne = true;
					break ext;
				}
			}
		}
		
		if(onlyOne) {
			for(int i = 0 ; i < pPos; i++)
				indexTemp += pt[i].length();
			System.out.println(paragraph);
			System.out.println(target);
			System.out.println(indexTemp);
			
			index = paragraph.indexOf(target,indexTemp);
			System.out.println(index);
			System.out.println();
		}else
			index = -1;
		
		System.out.println("\n===END SUBJECT===\n\n");
		return index;
	}
	
	public int lookObjIndex(TokenizerModel tokenModel, String paragraph, String anchor) throws InvalidFormatException, IOException {
		int index = 0;
		paragraph = removeStopWords(paragraph).toLowerCase();
		anchor = removeStopWords(anchor).toLowerCase();
		String[] pt = tokenExtraction(tokenModel,paragraph);
		String[] a = tokenExtraction(tokenModel, anchor);
		
		int pPos = 0;
		int indexTemp = 0;
		String target = "";
		boolean allIn = false;

		System.out.println("===OBJECT===");
		ext:for(int i = 0; i < pt.length; i++) {
			if(a.length == 1) {
				if(pt[i].contains(a[0])) {
					pPos = i;
					target = pt[i];
					allIn = true;
					break;
				}
			}else {
				for(int j = 1;  j < a.length; j++) {
					System.out.println(pt[i] + " - " + a[0] + " contains? = " + pt[i].contains(a[0]));
					if(!pt[i].contains(a[0]))
						break;
					pPos = i;
					target = pt[i];
					for(int k = i+1; k < pt.length && j < a.length; k++, j++) {
						System.out.println(pt[k] + " - " + a[j] + " contains? = " + pt[k].contains(a[j]));
						if(pt[k].contains(a[j])) {
							allIn = true;
						}else {
							allIn = false;
							break;
						}
					}
				}
			}
			
			if(allIn)
				break ext;
			
		}
		if(allIn) {
			
			for(int i = 0; i < pPos; i++)
				indexTemp += pt[i].length();
			System.out.println("\n\n"+paragraph);
			System.out.println(target);
			System.out.println(indexTemp);
			
			index = paragraph.indexOf(anchor.toLowerCase(),indexTemp);
			
			System.out.println(index);
			System.out.println();
		}else
			index = -1;
		System.out.println(index);
		System.out.println(allIn);
		System.out.println(paragraph.substring(0,index+anchor.length()));
		System.out.println("\n===END OBJECT===\n\n");
		return index;
	}
	
	public String removeStopWords(String target) {
		//System.out.println(stopWordsPattern);
		//Pattern pattern = Pattern.compile("\\b(?:"+stopWordsPattern+")\\b\\s*",Pattern.CASE_INSENSITIVE);
//		Matcher matcher = pattern.matcher(target);
//		String temp = "";
		for(String mark : punctuationMarks) {
//			System.out.println(mark);
			target = target.replaceAll(mark, " ");
			
//			System.out.println(target);
		}
//		System.out.println(target);
		return target;
	}
	
	public String lookPronounSbjIndex(TokenizerModel model, String paragraph) throws InvalidFormatException, IOException {
		String index = "";
		int indexTemp = 0;
		
		paragraph = removeStopWords(paragraph).toLowerCase();
//		System.out.println(paragraph);
		String[] pt = tokenExtraction(model, paragraph);
		
		String pronoun = detectPronoun(pt);
		
		int pPos = 0;
		String target = "";
		boolean onlyOne = false;
//		System.out.println("SUBJECT");
		for (int i = 0; i < pt.length; i++) {
			// System.out.println(pt[i] + " vs. " + a[j] + " ? " + pt[i].contains(a[j]));
			if (pt[i].equals(pronoun)) {// add she, he, his, her
				pPos = i;
				target = pt[i];
				onlyOne = true;
				break;
			}
		}
		
		if(onlyOne) {
			for(int i = 0 ; i < pPos; i++)
				indexTemp += pt[i].length();
//			System.out.println(paragraph);
//			System.out.println(target);
//			System.out.println(indexTemp);
			
			index = paragraph.indexOf(target,indexTemp)+"--"+pronoun;
//			System.out.println(index);
//			System.out.println();
		}else
			index = -1 + "--000";
		
		return index;
	}
	
	public String detectPronoun(String[] tokens) {
		String pronoun = "";
		for(String token : tokens) {
			System.out.println(token);
			if(token.equals("he")) {
				pronoun = "he";
				break;
			}else if(token.equals("she")) {
				pronoun = "she";
				break;
			}else if(token.equals("his")) { 
				pronoun = "his";
				break;
			}else if(token.equals("her")) {
				pronoun = "her";
				break;
			}
		}
		System.out.println("Pronoun = " + pronoun);
		return pronoun;
	}
	
	public FileInputStream readOpenNLPModel(String pathToModel) throws FileNotFoundException {
		ClassLoader classLoader = getClass().getClassLoader();
		FileInputStream fileModel = null;
		fileModel = new FileInputStream(classLoader.getResource(pathToModel).getFile());
		return fileModel;
	}
	
	public String[] tokenExtraction(TokenizerModel model, String sbjAnchor)
			throws InvalidFormatException, IOException {

		TokenizerME tokenizer = new TokenizerME(model);
//		int numCharacters = 0;

		String tokens[] = tokenizer.tokenize(sbjAnchor);

		return tokens;

	}

}
