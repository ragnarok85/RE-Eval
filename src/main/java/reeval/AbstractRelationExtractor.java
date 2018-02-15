package reeval;

import static reeval.RelationExtraction.counterContains;
import static reeval.RelationExtraction.counterEquals;
import static reeval.RelationExtraction.listReport;
import static reeval.RelationExtraction.notInList;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.FileManager;
import org.apache.log4j.Logger;

import objects.Annotation;
import objects.DBpediaRelation;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.TokenizerModel;
import sparql.SparqlQueries;
import text.TextSearcher;


public class AbstractRelationExtractor extends Thread{
	
	private static Logger logger = Logger.getLogger(AbstractRelationExtractor.class);

	private String sbjAnchor;
	private String objAnchor;
	private String numSection;
	private String titleSection;
//	private File filePath;
	
	private List<Annotation> listLinkAnnotations = new ArrayList<Annotation>();
//	private static List<DBpediaRelation> notInAbstractList = new ArrayList<DBpediaRelation>();
//	private List<Report> listReport = new ArrayList<Report>();
	
	private SentenceModel sentenceModel;
	private TokenizerModel tokenizerModel;
	private DBpediaRelation relation;
	private Model model;
	private TextSearcher ts = new TextSearcher();
	private SparqlQueries sq = new SparqlQueries();
	
	public AbstractRelationExtractor() {
		// TODO Auto-generated constructor stub
	}
	
	public AbstractRelationExtractor(String sbjAnchor, String objAnchor, String numSection,
			String titleSection, Model model, SentenceModel sentenceModel,
			TokenizerModel tokenizerModel, DBpediaRelation relation, List<Annotation> listLinkAnnotations) {
		this.sbjAnchor = sbjAnchor;
		this.objAnchor = objAnchor;
		this.numSection = numSection;
		this.titleSection = titleSection;
		this.sentenceModel = sentenceModel;
		this.tokenizerModel = tokenizerModel;
		this.model = model;
//		this.filePath = filePath;
		this.relation = relation;
		this.listLinkAnnotations.addAll(listLinkAnnotations);
		
	}
	
	@Override
	public void run() {
		String counters = "";
		int equals = 0;
		int contains = 0;
			logger.info("Processing: " + sbjAnchor);
			try {
//				System.out.println("NumSection = " + numSection + "\nTitleSection = " + titleSection);
				counters = ts.detectRelationsInSentence(listLinkAnnotations,listReport,model,relation,
						sentenceModel,tokenizerModel,sbjAnchor,objAnchor,numSection,titleSection);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			equals = Integer.parseInt(counters.split("-")[0]);
			contains = Integer.parseInt(counters.split("-")[1]);
			
			if(equals == 0 && contains == 0) {
				notInList.add(relation);
			}else {
				counterEquals += equals;
				counterContains += contains;
				logger.info(equals + " -- " + contains + "\tequals and contains\n\n");
			}
			
			logger.info(sbjAnchor + " was processed");
//		}
		
		
	}
	
	public BZip2CompressorInputStream createBz2Reader(File source) {
		BZip2CompressorInputStream pageStruct = null;
		try{
			if(!source.exists()) {
				return null;
			}
			InputStream pageStructure = FileManager.get().open(source.getAbsolutePath());
	    	pageStruct = new BZip2CompressorInputStream(pageStructure);
		}catch(IOException e) {
			e.printStackTrace();
		}
		return pageStruct;
	}
	
	public Model createJenaModel(BZip2CompressorInputStream filePath) {
		Model model = ModelFactory.createDefaultModel();
		model.read(filePath,null,"TURTLE");
		//setPrefixes(model);
		return model;
		
	}
}
