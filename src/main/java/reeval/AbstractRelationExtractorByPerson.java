package reeval;

import static reeval.RelationExtractionByPerson.counterContains;
import static reeval.RelationExtractionByPerson.counterEquals;
import static reeval.RelationExtractionByPerson.listReport;
import static reeval.RelationExtractionByPerson.notInList;

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
import objects.DBpediaType;
import sparql.SparqlQueries;
import text.TextSearcher;


public class AbstractRelationExtractorByPerson extends Thread{
	
	private static Logger logger = Logger.getLogger(AbstractRelationExtractorByPerson.class);

	private String sbjAnchor;
	private String objAnchor;
	private String objUri;
	private String numSection;
	private String titleSection;
	
	private List<Annotation> listLinkAnnotations = new ArrayList<Annotation>();
	
	private DBpediaRelation relation;
	private DBpediaType type;
	private Model model;
	private TextSearcher ts = new TextSearcher();
	private SparqlQueries sq = new SparqlQueries();
	
	public AbstractRelationExtractorByPerson() {
	}
	
	public AbstractRelationExtractorByPerson(String sbjAnchor, String objAnchor, String objUri, String numSection,
			String titleSection, Model model, DBpediaRelation relation, List<Annotation> listLinkAnnotations) {
		this.sbjAnchor = sbjAnchor;
		this.objAnchor = objAnchor;
		this.objUri = objUri;
		this.numSection = numSection;
		this.titleSection = titleSection;
		this.model = model;
		this.relation = relation;
		this.listLinkAnnotations.addAll(listLinkAnnotations);
		
	}
	
//	public AbstractRelationExtractorByPerson(String sbjAnchor, String objAnchor, String numSection,
//			String titleSection, Model model, DBpediaType relation, List<Annotation> listLinkAnnotations) {
//		this.sbjAnchor = sbjAnchor;
//		this.objAnchor = objAnchor;
//		this.numSection = numSection;
//		this.titleSection = titleSection;
//		this.model = model;
//		this.type = relation;
//		this.listLinkAnnotations.addAll(listLinkAnnotations);
//		
//	}
	
	@Override
	public void run() {
		String counters = "";
		int equals = 0;
		int contains = 0;
			logger.info("Processing: " + sbjAnchor);
			try {
//				System.out.println("NumSection = " + numSection + "\nTitleSection = " + titleSection);
				counters = ts.detectRelationsInSentence(listLinkAnnotations,listReport,model,relation,
						sbjAnchor,objAnchor,objUri, numSection,titleSection);
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
