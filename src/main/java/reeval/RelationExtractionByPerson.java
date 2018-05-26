package reeval;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.FileManager;
import org.apache.log4j.Logger;

import objects.Annotation;
import objects.Article;
import objects.DBpediaRelation;
import objects.DBpediaType;
import objects.Paragraph;
import objects.REStats;
import objects.Section;
import opennlp.tools.util.InvalidFormatException;
import reports.Report;
import sparql.QueryRelations;
import sparql.SparqlQueries;
import sparql.SparqlRelationsByPerson;

public class RelationExtractionByPerson {

	private static Logger logger = Logger.getLogger(RelationExtractionByPerson.class);

	private SparqlQueries sq = new SparqlQueries();
	private SparqlRelationsByPerson sqbp = new SparqlRelationsByPerson();

	static List<Report> listReport = new ArrayList<Report>();
	private static Map<String,String> mapSparqlQueries = new HashMap<String,String>();
	static Set<DBpediaRelation> notInList = new HashSet<DBpediaRelation>();
	static Integer counterEquals = 0;
	static Integer counterContains = 0;

	public static void main(String[] args) throws NoSuchAlgorithmException, InvalidFormatException, IOException,
			InterruptedException, CompressorException {
		File sparqlPath = new File(args[0]);
		File[] sparqlFiles = sparqlPath.listFiles();
		File outputFolder = new File(args[2]);
		File outputAbstract = new File(outputFolder.getAbsoluteFile() + "/Abstract");
		File outputNotInAbstract = new File(outputFolder.getAbsoluteFile() + "/notInAbstract");
		File outputNotInSection = new File(outputFolder.getAbsoluteFile() + "/notInSection");
		File outputSections = new File(outputFolder.getAbsoluteFile() + "/Sections");
		File[] outputFiles = outputFolder.listFiles();
		String nifPath = args[1];

		List<String> listProcessed = new ArrayList<String>();
		List<DBpediaRelation> notInAbstractList = new ArrayList<DBpediaRelation>();
		List<DBpediaRelation> notInSectionList = new ArrayList<DBpediaRelation>();
		List<REStats> listStats = new ArrayList<REStats>();

		RelationExtractionByPerson re = new RelationExtractionByPerson();

		re.populateQueries();
		// listProcessed.addAll(re.processedFiles(outputFiles));

		List<DBpediaType> listPersons = new ArrayList<DBpediaType>();
		String queryPerson = "SELECT ?s (<http://dbpedia.org/ontology/Person> as ?o) WHERE { ?s a <http://dbpedia.org/ontology/Person> } LIMIT 1000";
		listPersons.addAll(re.sq.queryDBpediaType(queryPerson, "Person"));

		List<String> listArticlesNotFound = new ArrayList<String>();
		Long initialTime = System.currentTimeMillis();
		Long endTime = 0L;
		String timeElapsed = "";
		for (DBpediaType person : listPersons) {
			logger.info("reports (outside)= " + listReport.size());
			for (Map.Entry<String, String> entry : mapSparqlQueries.entrySet()) {
				logger.info("reports (inside)= " + listReport.size());
				logger.info("Processing file: " + entry.getKey());
				List<DBpediaRelation> listRelations = new ArrayList<DBpediaRelation>();
				String personRelationQuery = entry.getValue().replaceAll("=S=", "<"+person.getSubject()+">");
				// TODO check if person uri is replace in query
//				System.out.println(personRelationQuery);
				listRelations.addAll(re.sqbp.queryDBpediaRelations(personRelationQuery, entry.getKey(), person.getSubject(),
						listStats));

				logger.info("Beging Abstract");
				listArticlesNotFound.addAll(re.lookRelationsInAbstract(listRelations, nifPath, entry.getKey(), outputAbstract));
				notInAbstractList.addAll(notInList);
				notInList.clear();
				logger.info("relations not found in abstract = " + notInAbstractList.size());
				
				// logger.info("Begin Section");
				// initialTime = System.currentTimeMillis();
				// if(!notInAbstractList.isEmpty()) {
				// notInSectionList.addAll(re.lookRelationsInSection(notInAbstractList, nifPath,
				// entry.getKey(), outputSections));
				// re.writeNotInAbstractRelations(outputNotInSection, entry.getKey(),
				// notInSectionList);
				// }
				// endTime = System.currentTimeMillis() - initialTime;
				// timeElapsed = String.format("TOTAL TIME = %d min, %d sec",
				// TimeUnit.MILLISECONDS.toMinutes(endTime),
				// TimeUnit.MILLISECONDS.toSeconds(endTime) -
				// TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(endTime)));
				// re.writeTime(outputFolder, "SectionTime.csv", entry.getKey(), timeElapsed);
				// logger.info("End Section");
			}
		}
		re.writeNotInAbstractRelations(outputNotInAbstract, "Person", notInAbstractList);
		endTime = System.currentTimeMillis() - initialTime;
		timeElapsed = String.format("TOTAL TIME = %d min, %d sec", TimeUnit.MILLISECONDS.toMinutes(endTime),
				TimeUnit.MILLISECONDS.toSeconds(endTime)
						- TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(endTime)));
		re.writeTime(outputFolder, "AbstractTime.csv", "Person", timeElapsed);
		logger.info("End Abstract");
		String[] counterApproach = re.countApproachFromReport(listReport).split("--");// sbj-obj--pronoun-obj
		String[] counterExtraction = re.countExtractionFromReport(listReport).split("--");// in sentence -- in paragraph
		int counterBlank = re.countBlankSentences(listReport);
		re.writeMilanReport(outputFolder, "All.tsv", listReport, counterEquals, counterContains, notInList.size(),
				counterApproach[0], counterApproach[1], counterExtraction[0], counterExtraction[1], counterBlank,
				listArticlesNotFound);

		logger.info("number of equals: " + counterEquals);
		logger.info("number of contains: " + counterContains);
		logger.info("number of remaining relations: " + notInAbstractList.size());
		// for each person

//		for (REStats stat : listStats) {
//			logger.info("Relation = " + stat.getPredicate() + " #Results = " + stat.getNumberResults());
//		}

	}
	
	public void populateQueries() {
		mapSparqlQueries.put("Awards",QueryRelations.AWARDS.query());
		mapSparqlQueries.put("Children",QueryRelations.CHILDREN.query());
		mapSparqlQueries.put("CityOfBirth",QueryRelations.CITYOFBIRTH.query());
		mapSparqlQueries.put("CityOfResidence",QueryRelations.CITYOFRESIDENCE.query());
		mapSparqlQueries.put("Employer",QueryRelations.EMPLOYER.query());
		mapSparqlQueries.put("Nationality",QueryRelations.NATIONALITY.query());
		mapSparqlQueries.put("Occupation",QueryRelations.OCCUPATION.query());
		mapSparqlQueries.put("Parents",QueryRelations.PARENTS.query());
		mapSparqlQueries.put("PartyAffiliation",QueryRelations.PARTYAFFILIATION.query());
		mapSparqlQueries.put("SchoolAttended",QueryRelations.SCHOOLATTENDED.query());
		mapSparqlQueries.put("Siblings",QueryRelations.SIBLINGS.query());
		mapSparqlQueries.put("Spouse",QueryRelations.SPOUSE.query());

	}

	public String countApproachFromReport(List<Report> listReport) {
		String counterApproach = "0--0"; //Sbj-Obj--Pronoun-Obj
		int sbjObj = 0;
		int pronounObj = 0;
		for(Report r : listReport) {
			if(r.getApproach().equals("Sbj-Obj"))
				sbjObj++;
			else if(r.getApproach().equals("Pronoun-Obj"))
				pronounObj++;
		}
		counterApproach = sbjObj+"--"+pronounObj;
		return counterApproach;
	}
	
	public String countExtractionFromReport(List<Report> listReport) {
		String counterExtraction = "0--0"; //In Sentence--In Paragraph
		int sentence = 0;
		int paragraph = 0;
		for(Report r : listReport) {
			if(r.getExtraction().equals("In Sentence"))
				sentence++;
			else if(r.getExtraction().equals("In Paragraph"))
				paragraph++;
		}
		counterExtraction = sentence+"--"+paragraph;
		return counterExtraction;
	}
	
	public int countBlankSentences(List<Report> listReport) {
		int counterBlank = 0;
			for(Report r : listReport) {
				if(r.getBlankSentence().equals("X"))
					counterBlank++;
			}
		return counterBlank;
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
		model.read(filePath,null,"NTRIPLES");
		//setPrefixes(model);
		return model;
		
	}

	public String readSparqlQueries(File inputFile) {
		String sparqlQuery = "";
		try (BufferedReader br = new BufferedReader(
				new InputStreamReader(new FileInputStream(inputFile), StandardCharsets.UTF_8))) {
			String line = "";
			while ((line = br.readLine()) != null) {
				sparqlQuery += line + " ";
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return sparqlQuery;
	}
	
	public File lookNIFFile(String sbj, String nifPath) throws NoSuchAlgorithmException {
		String md5 = md5(sbj);
		File filePath = new File(nifPath+"/"+mdf5ToPath(md5)+".ttl.bz2");
		return filePath;
	}
	
	public String md5(String name) throws NoSuchAlgorithmException {
		String md5 = "";
		//String base = "<http://nif.dbpedia.org/wiki/en/"+name;
		String base = name;
		byte[] fileName = base.getBytes();
		MessageDigest md = MessageDigest.getInstance("MD5");
		md5 = toHexString(md.digest(fileName));
		return md5;
	}
	
	public String mdf5ToPath(String md5) {
		return md5.substring(0, 2) + "/" + md5.substring(2,4) + "/" + md5.substring(4);
	}
	
	public String toHexString(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();

        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }

        return hexString.toString();
    }

	public List<String> lookRelationsInAbstract(List<DBpediaRelation> listRelations, String nifPath, String fileName,
			File outputFolder) throws NoSuchAlgorithmException, IOException, InterruptedException, CompressorException {

		//listReport.clear();
		//counterEquals = 0;
		//counterContains = 0;

		List<Thread> listThreads = new ArrayList<Thread>();
		List<String> listArticlesNotFound = new ArrayList<String>();
		SparqlQueries sq = new SparqlQueries();

		int counterRels = 0;
		for (DBpediaRelation rel : listRelations) {

			Article article = new Article();

			String[] sbjSplit = rel.getSbjURI().split("/");
			String sbj = sbjSplit[sbjSplit.length - 1];
			String sbjAnchor = sbj.replaceAll("_", " ");

			// if(!sbj.equals("Afsaneh_Najmabadi")) {
			// continue;
			// }

			String[] objSplit = rel.getObjURI().split("/");
			String objAnchor = objSplit[objSplit.length - 1].replaceAll("_", " ");

			String objUri = rel.getObjURI();
			// TODO Create an Article object with name attribute as the file name

			// System.out.println(sbjAnchor + "-" + objAnchor);
			File filePath = lookNIFFile(sbj, nifPath);

			// QUERIES
			List<Annotation> listLinkAnnotations = new ArrayList<Annotation>();
			BZip2CompressorInputStream inputStream = createBz2Reader(filePath);

			if (inputStream == null) {
				logger.info("File \"" + filePath.getAbsolutePath() + "\"(" + sbj + ") does not exist");
				listArticlesNotFound.add(sbj + "--" + filePath.getName());
				continue;
			}

			Model model = createJenaModel(inputStream);

			// TODO query for context
			article.setName(fileName);
			article.setContext(sq.queryContext(model));
			// TODO query for sections
			article.setListSections(sq.queryOrderSection(model));
			// TODO query for paragraphs
			for (Section s : article.getListSections()) {
				s.setListParagraphs(sq.queryOrdererParagraph(model, s.getSectionURI()));
			}
			// TODO The query for annotations is made in the following lines
			for (Section s : article.getListSections()) {
				for (Paragraph p : s.getListParagraphs()) {
					p.setListAnnotations(sq.queryOrderAnnotation(model, p.getParagraphURI()));
				}
			}
			// Abstract
			listLinkAnnotations.addAll(sq.queryAbstractAnnotations(model));

			// TODO filter annotations to those which contains or are equal to the object

			AbstractRelationExtractorByPerson are = new AbstractRelationExtractorByPerson(sbjAnchor, objAnchor, objUri, "0", "Abstract", model,
					rel, listLinkAnnotations);
			listThreads.add(are);
			are.start();
			// System.out.println("threads running: " + Thread.activeCount());
			System.out.println("Relation processing/processed = " + counterRels++ + "/" + listRelations.size());
			if (listThreads.size() > 100) {
				out: while (true) {
					for (Thread e : listThreads) {
						if (e.isAlive())
							continue out;
					}
					break;
				}
				listThreads.clear();
			}
		}
		for (Thread t : listThreads) {
			t.join();
		}
		return listArticlesNotFound;
		

	}
	
	public void writeMilanReport(File outputFolder,String fileName,List<Report> listReport, int numEquals, 
			int numContains, int notInAbstractList, String sbjObj, String pronounObj,
			String sentence, String paragraph, int counterBlank, List<String> listArticlesNotFound) {
		try(PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(outputFolder+"/"+fileName+"-report.csv"),StandardCharsets.UTF_8))) {
			pw.write("id\t"
					+ "text\t"
					+ "property\t"
					+ "subject\t"
					+ "object\n");
			int id = 0;
			for(Report r : listReport) {
				//TODO
				pw.write(id++ + "\t" + r.printMilanReport()+"\n");
			}
			pw.close();
		}catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public void writeNotInAbstractRelations(File output, String fileName, List<DBpediaRelation> notInAbstractList) {
		try(PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(output+"/"+fileName), StandardCharsets.UTF_8))){
			pw.write("subject\tpredicate\tobject\tcontext\n");
			for(DBpediaRelation rel : notInAbstractList) {
				pw.write(rel.printRelation() + "\n");
			}
			pw.close();
		}catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public void writeTime(File output, String fileName, String relation, String timeElapsed) {
		try(PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(output+"/"+fileName,true),StandardCharsets.UTF_8))){
			pw.write(relation+"\t"+timeElapsed+"\n");
		}catch(IOException e) {
			e.printStackTrace();
		}
	}
}
