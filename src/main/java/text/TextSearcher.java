package text;

import org.apache.log4j.Logger;

import sparql.SparqlQueries;

public class TextSearcher {

	private static Logger logger = Logger.getLogger(TextSearcher.class);
	
	private TextPreprocessor tp = new TextPreprocessor();
	private SparqlQueries sq = new SparqlQueries();
	
	public TextSearcher() {
		// TODO Auto-generated constructor stub
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
	
	public String extractIndexes(String paragraphURI) {
		return paragraphURI.split("char=")[1];
	}
	
	public String textExtractor(String context, int bi, int ei){
		if(ei > context.length())
			ei = context.length();
		return context.substring(bi,ei);
	}
	
}
