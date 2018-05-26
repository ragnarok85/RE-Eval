package text;

import java.io.IOException;
import java.util.List;

import org.apache.jena.rdf.model.Model;
import org.apache.log4j.Logger;

import objects.Annotation;
import objects.DBpediaRelation;
import reports.Report;
import opennlp.tools.util.InvalidFormatException;
import sparql.SparqlQueries;

public class TextSearcher {

	private static Logger logger = Logger.getLogger(TextSearcher.class);
	
	private TextPreprocessor tp = new TextPreprocessor();
	private SparqlQueries sq = new SparqlQueries();
	
	public TextSearcher() {
		// TODO Auto-generated constructor stub
	}
	
	public String detectRelationsInSentence(List<Annotation> listLinkAnnotations, List<Report> listReport,
			Model model, DBpediaRelation rel, String sbjAnchor, String objAnchor, String objUri, String notation, 
			String secTitle) throws IOException {
		int counterEquals = 0; 
		int counterContains = 0;
		String approach = "";
		String extraction = "";
		String context = sq.queryContext(model).replace("\n", " ");
		for(Annotation ann : listLinkAnnotations) {
//			if(!ann.getNotation().equals(notation))
//				continue;
//			ann.printAnnotation();
//			logger.info("Comparing object Uri (" + objUri + ") with annotation Uri (" + ann.getTaIdentRef());
			if(!ann.getTaIdentRef().equals(objUri))
				continue;
			
			String[] listSentences;
			String object = ann.getAnchor();
			
			Report report = new Report();
			report.setSubject(rel.getSbjURI());
			report.setObject(rel.getObjURI());
			report.setRelation(rel.getPrdURI());
			
			String[] paragraphIndexes = extractIndexes(ann.getParagraphURI()).split(","); //begin,end
			String[] sectionIndexes = extractIndexes(ann.getSectionURI()).split(",");
			int paragraphBeginIndex = Integer.parseInt(paragraphIndexes[0]);
			int paragraphEndIndex = Integer.parseInt(paragraphIndexes[1]);
			int sectionEndIndex = Integer.parseInt(sectionIndexes[1]);
			
			if(paragraphEndIndex > context.length()) {
				logger.info("Index inconsistency = " + rel.getSbjURI());
				paragraphEndIndex = context.length();
			}
			if(sectionEndIndex > context.length()) {
				logger.info("Index inconsistency = " + rel.getSbjURI());
				sectionEndIndex = context.length();
			}
			if(paragraphEndIndex < ann.getEndIndex())
				continue;
			
			String sectionText = context.substring(Integer.parseInt(sectionIndexes[0]),sectionEndIndex);
			String paragraph = context.substring(Integer.parseInt(paragraphIndexes[0]), paragraphEndIndex); 
			listSentences = tp.sentenceDetector(paragraph);
			String sentence = tp.selectSentence(listSentences, paragraphBeginIndex, ann.getEndIndex());
			if(sentence.isEmpty())
				sentence = "NO_SENTENCE_SELECTED_:::" + paragraph + "----" + paragraphBeginIndex + "-"+paragraphBeginIndex+" ----EndIndex----" + ann.getEndIndex() + " ---- Mention ---" + ann.getAnchor();
			rel.setSbjAbstract(sectionText);
			
//			System.out.println("Detect sentence for " + ann.getAnchor() + " and " + sbjAnchor);
			
//			System.out.println("detecting subject ("+sbjAnchor+") in sentence: ");
			
//			String selectSentences = detectInSentence(listSentences, sbjAnchor, object);

			approach = "Sbj-Obj";
			extraction = "In Sentence";
//			if(selectSentences.length() == 0) {
////				System.out.println("Detecting pronoun (he,she,his,her) in sentence: ");
//				approach = "Pronoun-Obj";
//				extraction = "In Sentence";
//				selectSentences = detectPronounInSentence(listSentences, objAnchor, sbjAnchor);
//			}
//			
//			if(selectSentences.length() == 0) {
////				System.out.println("Detecting relation using the whole paragraph. ");
//				approach = "Sbj-Obj";
//				extraction = "In Paragraph";
//				selectSentences = detectInParagraph(context, object, sbjAnchor);
//			}
			
//			if(selectSentences.length() == 0) {
//				report.setBlankSentence("X");
//			}
			
//			if(selectSentences.length() > 0) {
			
				report.setContext(sectionText);
//				report.setSentence(selectSentences);
				report.setSentence(sentence);
				
				report.setExtraction(extraction);
				report.setApproach(approach);
				report.setSectionNumber(notation);
				report.setSectionTitle(secTitle);
				
				if(ann.getAnchor().equalsIgnoreCase(objAnchor)) {
//					System.out.println("Section Index: " + Integer.parseInt(sectionIndexes[0]) + "---" +sectionEndIndex);
//					System.out.println("Paragraph Index: " + Integer.parseInt(paragraphIndexes[0]) + "---" +paragraphEndIndex);
//					System.out.println("Equal = " + ann.getAnchor());
//					System.out.println("Sentence = " + selectSentences);
					report.setKindOfMatch("Equal");
					report.setAnchor(ann.getAnchor());
					counterEquals++;
					listReport.add(report);
				}else if(ann.getAnchor().contains(objAnchor)) {
//					System.out.println("Section Index: " + Integer.parseInt(sectionIndexes[0]) + "---" +sectionEndIndex);
//					System.out.println("Paragraph Index: " + Integer.parseInt(paragraphIndexes[0]) + "---" +paragraphEndIndex);
//					System.out.println("Contains = " + ann.getAnchor());
//					System.out.println("Sentence = " + selectSentences);
					report.setKindOfMatch("Contains");
					report.setAnchor(ann.getAnchor());
					counterContains++;
					listReport.add(report);
				}
			}
			
//		}
//		System.out.println(counterEquals+"-"+counterContains);
		return counterEquals+"-"+counterContains;
	}

	public String detectPronoun(String[] tokens) {
		String pronoun = "";
		for(String token : tokens) {
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
		return pronoun;
	}
	
	public String detectPronounInSentence(String[] listSentences, 
			String objAnchor, String sbjAnchor) throws InvalidFormatException, IOException {
		/*
		 * The relation is hold by a pronoun (she is the mother of ...)
		 * Subject must match a pronoun as he, she, his or her
		 * Object must match complete to avoid inconsistencies
		 */
		
//		System.out.println("Object = " + objAnchor);
		String sentence = "";
		for(String snt : listSentences) {
			String index = lookPronounSbjIndex(snt);
			//System.out.println("----Index " + index);
			int sIndex = Integer.parseInt(index.split("--")[0]);
			String pronoun = "";
			
			if(!(sIndex == -1))
				pronoun = index.split("--")[1];
			
			int oIndex = lookObjIndex(snt,objAnchor);
//			System.out.println("sIndex = " + sIndex + " - oIndex = " + oIndex);
			if(sIndex >= 0 && sIndex < oIndex) {
				if(oIndex > 0)
					oIndex += objAnchor.length();
				if(oIndex > snt.length())
					oIndex = snt.length();
//				System.out.println(sIndex + " -- " + oIndex + "--" + snt.length());
				if(sIndex < oIndex ) {
					sentence = snt.substring(sIndex, oIndex);
					sentence = "(**" + pronoun + "=" + sbjAnchor + "**): " + sentence;
//					System.out.println("Sentence:\n"+sentence);
//					System.out.println();
				}
			}
//			System.out.println("\n\n");
		}
		return sentence;
	}
	
//	public String detectInSentence(String[] listSentences, String sbjAnchor, 
//			String objAnchor, int endIndex) throws InvalidFormatException, IOException {
//		/*
//		 * A sentence is considered only if contains the subject and object
//		 * Subject must match one of their elements
//		 * Object must match complete to avoid inconsistencies
//		 */
////		System.out.println("Subject = " + sbjAnchor);
////		System.out.println("Object = " + objAnchor);
//		String sentence = "";
////		int counter = 0;
//		for(String snt : listSentences) {
////			System.out.println(counter++ + ".- sentence: \n" + snt);
////			System.out.println("Paragraph = " + snt);
//			int sIndex = lookSbjIndex(snt,sbjAnchor);
//			int oIndex = lookObjIndex(snt,objAnchor);
////			System.out.println("sIndex = " + sIndex + " - oIndex = " + oIndex);
//			if(sIndex >= 0 && sIndex < oIndex) {
//				if(oIndex > 0)
//					oIndex += objAnchor.length();
//				if(oIndex > snt.length())
//					oIndex = snt.length();
////				System.out.println(sIndex + " -- " + oIndex + "--" + snt.length());
//				//creating a substring
//				if(sIndex < oIndex ) {
////					sentence = snt.substring(sIndex, oIndex);
//					sentence = snt;
////					System.out.println("Sentence:\n"+sentence);
////					System.out.println();
//				}
//			}
////			System.out.println("\n\n");
//		}
//		return sentence;
//	}
	
	public String detectInParagraph(String paragraph, String objAnchor, 
			String sbjAnchor) throws InvalidFormatException, IOException {
		/*
		 * A sentence is considered only if contains the subject and object
		 * Subject must match one of their elements
		 * Object must match complete to avoid inconsistencies
		 */
//		System.out.println("Subject = " + sbjAnchor);
//		System.out.println("Object = " + objAnchor);
		String sentence = "";
		int sIndex = lookSbjIndex(paragraph, sbjAnchor);
		int oIndex = lookObjIndex(paragraph, objAnchor);
//		System.out.println("sIndex = " + sIndex + " - oIndex = " + oIndex);
		if (sIndex >= 0 && sIndex < oIndex) {
			if (oIndex > 0)
				oIndex += objAnchor.length();
			if (oIndex > paragraph.length())
				oIndex = paragraph.length();
//			System.out.println(sIndex + " -- " + oIndex + "--" + paragraph.length());
			if (sIndex < oIndex) {
				sentence = paragraph.substring(sIndex, oIndex);
//				System.out.println("Sentence:\n" + sentence);
//				System.out.println();
			}
		}
//		System.out.println("\n\n");
		return sentence;
	}
	
	public String extractIndexes(String paragraphURI) {
		return paragraphURI.split("char=")[1];
	}
	
	public int lookObjIndex(String sentence, String anchor) throws InvalidFormatException, IOException {
		int index = 0;
		int indexTemp = 0;
		
		sentence = tp.removeStopWords(sentence).toLowerCase();
		
		String[] st = tp.tokenExtraction(sentence);
		
		//String clean = anchor.split("\\(")[0];
		//clean = removeStopWords(clean).toLowerCase();
		
		//String[] a = tokenExtraction(model,clean);
		anchor = tp.removeStopWords(anchor).toLowerCase();
		String[] a = tp.tokenExtraction(anchor);
		int pPos = 0;
		String target = "";
		boolean allIn = false;
		
//		System.out.println("===OBJECT===");
		
		for(int i = 0; i < st.length; i++) {
			//for only one word
			if(a.length == 1) {
				if(st[i].contains(a[0])) {
					pPos = i;
					target = st[i];
					allIn = true;
					break;
				}
			//more than one word
			}else {
				for(int j = 1;  j < a.length; j++) {
//					System.out.println(pt[i] + " - " + a[0] + " contains? = " + pt[i].contains(a[0]));
					if(!st[i].contains(a[0]))
						break;
					pPos = i;
					target = st[i];
					for(int k = i+1; k < st.length && j < a.length; k++, j++) {
//						System.out.println(pt[k] + " - " + a[j] + " contains? = " + pt[k].contains(a[j]));
						if(st[k].contains(a[j])) {
							allIn = true;
						}else {
							allIn = false;
							break;
						}
					}
				}
			}
			
			if(allIn)
				break;
			
		}
		if(allIn) {
			
			for(int i = 0; i < pPos; i++)
				indexTemp += st[i].length();
//			System.out.println("\n\n"+paragraph);
//			System.out.println(target);
//			System.out.println(indexTemp);
			
			index = sentence.indexOf(anchor.toLowerCase(),indexTemp);
			
//			System.out.println(index);
//			System.out.println();
		}else
			index = -1;
//		System.out.println("===End Object===\n\n");
//		System.out.println("index returned = " + index);
		return index;
	}
	
	public int lookSbjIndex(String sentence, String anchor) throws InvalidFormatException, IOException {
		int index = 0;
		int indexTemp = 0;
		
		sentence = tp.removeStopWords(sentence).toLowerCase();
//		System.out.println(paragraph);
		String[] pt = tp.tokenExtraction(sentence);
		
		String clean = anchor.split("\\(")[0]; //remove parenthesis
		clean = tp.removeStopWords(clean).toLowerCase(); //replace stop words by empty spaces
		String[] a = tp.tokenExtraction(clean.toLowerCase());
		
		int pPos = 0;
		String target = "";
		boolean onlyOne = false;
//		System.out.println("SUBJECT");
		//look for the first coincidence of the subject 
		ext:for(int i = 0; i < pt.length; i++) {
			for(int j = 0;  j < a.length; j++) {
//				System.out.println(pt[i] + " vs. " + a[j] + " ? " + pt[i].contains(a[j]));
				if(pt[i].equalsIgnoreCase(a[j])) {
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
//			System.out.println(paragraph);
//			System.out.println(target);
//			System.out.println(indexTemp);
			
			index = sentence.indexOf(target,indexTemp);
//			System.out.println(index);
//			System.out.println();
		}else
			index = -1;
		
		return index;
	}
	
	public String lookPronounSbjIndex(String paragraph) throws InvalidFormatException, IOException {
		String index = "";
		int indexTemp = 0;
		
		paragraph = tp.removeStopWords(paragraph).toLowerCase();
//		System.out.println(paragraph);
		String[] pt = tp.tokenExtraction(paragraph);
		
		String pronoun = detectPronoun(pt); //he, she, his, her
		
		int pPos = 0;
		String target = "";
		boolean onlyOne = false;
//		System.out.println("SUBJECT");
		
		if(pronoun.length() == 0) {
			index = -1 + "--000";
		}else {
			for (int i = 0; i < pt.length; i++) {
				// System.out.println(pt[i] + " vs. " + a[j] + " ? " + pt[i].contains(a[j]));
				if (pt[i].equals(pronoun)) {
					pPos = i;
					target = pt[i];
					onlyOne = true;
					break;
				}
			}
			
			if(onlyOne) {
				for(int i = 0 ; i < pPos; i++)
					indexTemp += pt[i].length();
//				System.out.println(paragraph);
//				System.out.println(target);
//				System.out.println(indexTemp);
				
				index = paragraph.indexOf(target,indexTemp)+"--"+pronoun;
//				System.out.println(index);
//				System.out.println();
			}else
				index = -1 + "--000";
		}
		return index;
	}
	
	public String textExtractor(String context, int bi, int ei){
		if(ei > context.length())
			ei = context.length();
		return context.substring(bi,ei);
	}
	
}
