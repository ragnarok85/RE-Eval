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
import java.util.List;
import java.util.Map;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.FileManager;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.SKOS;

public class RelationExtraction {
	/*
	 * possible input:
	 *   A file with the SPARQL query for each relation
	 *   args[0] - path with SPARQL files
	 *   args[1] - nif path
	 *   Restrictions:
	 *    1. Subject and object types must start with a upper case word
	 *    2. Predicate must start with a lower case word
	 *    3. The name file must be the same as the relation and must be ended with .rq
	 */
	
	private static String service = "http://dbpedia.org/sparql";
	
	public static void main(String[] args) throws NoSuchAlgorithmException {
		File sparqlPath = new File(args[0]);
		File[] sparqlFiles = sparqlPath.listFiles();
		String nifPath = args[1];
				
		RelationExtraction re = new RelationExtraction();
		Map<String,String> mapSparqlQueries = new HashMap<String,String>();
		List<REStats> listStats = new ArrayList<REStats>();
		Map<String,List<DBpediaRelation>> mapRelations = new HashMap<String,List<DBpediaRelation>>();
		
		for(File file : sparqlFiles) {
			String query = re.readSparqlQueries(file);
			System.out.println(query);
			mapSparqlQueries.put(file.getName().replace(".rq", ""),query);
			
		}
		
		for(Map.Entry<String, String> entry : mapSparqlQueries.entrySet()) {
			List<DBpediaRelation> listRelations = new ArrayList<DBpediaRelation>();
			listRelations.addAll(re.queryRelations(entry.getValue(),entry.getKey(),listStats));
			re.process(listRelations, nifPath);
		}
		
		for(REStats stat : listStats) {
			System.out.println("Relation = " + stat.getPredicate() + " #Results = " + stat.getNumberResults());
		}
	}
	
	
	/*
	 * SPARQL DBpedia
	 */
	
	public List<DBpediaRelation> queryRelations(String queryString, String targetRelation, List<REStats> listStats) {
		List<DBpediaRelation> listDBRelations = new ArrayList<DBpediaRelation>();
		REStats stat = new REStats();
		
		stat.setQueryString(queryString);
		
		Query query = QueryFactory.create(queryString);
		
		try(QueryExecution qexec = QueryExecutionFactory.sparqlService(service, query)){
			ResultSet result = qexec.execSelect();
			int counterResults = 0;
			while(result.hasNext()) {
				DBpediaRelation relation = new DBpediaRelation(targetRelation);
				
				QuerySolution qs = result.next();
				counterResults++;
				
				String s = qs.getResource("?s").getURI();
				String o = qs.getResource("?o").getURI();
				
				relation.setSbjURI(s);
				relation.setObjURI(o);
				
				listDBRelations.add(relation);
			}
			stat.setPredicate(targetRelation);
			stat.setNumberResults(counterResults);
		}
		listStats.add(stat);
		return listDBRelations;
	}
	
	/*
	 * JENA
	 * 
	 */
	
	public Model createJenaModel(BZip2CompressorInputStream filePath) {
		Model model = ModelFactory.createDefaultModel();
		model.read(filePath,null,"TURTLE");
		//setPrefixes(model);
		return model;
		
	}
	
	public void setPrefixes(Model model) {
		model.setNsPrefix("rdf", RDF.uri);
		model.setNsPrefix("nif", "http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#");
		model.setNsPrefix("skos", SKOS.uri);
	}
	
	// retrieve all annotations from abstract section from a given model
	public List<Annotation> queryAbstractAnnotations(Model model) {
		List<Annotation> listAnnotations = new ArrayList<Annotation>();
		
		String queryString = Queries.ABSTRACTLINKS.query();
		Query query = QueryFactory.create(queryString);
		try(QueryExecution qexec = QueryExecutionFactory.create(query, model)){
			ResultSet result = qexec.execSelect();
			while(result.hasNext()) {
				Annotation annotation = new Annotation();
				QuerySolution sol = result.next();
				annotation.setAnchor(sol.getLiteral("?anchor").getString());
				annotation.setURI(sol.getResource("s").getURI());
				annotation.setParagraphURI(sol.getResource("?lsuper").getURI()); //linkSuperString --> Paragraph
				annotation.setSectionURI(sol.getResource("?psuper").getURI());//paragraphSuperString --> Section
				listAnnotations.add(annotation);
			}
		}
		return listAnnotations;
	}
	
	//retrieve the whole context
	public String queryContext(Model model) {
		String context = "";
		String queryString = Queries.CONTEXT.query();
		Query query = QueryFactory.create(queryString);
		try(QueryExecution qexec = QueryExecutionFactory.create(query, model)){
			ResultSet result = qexec.execSelect();
			while(result.hasNext()) {
				QuerySolution sol = result.next();
				context = sol.getLiteral("?context").getString();
			}
		}
		return context;
	}
	
	/*
	 * NIF
	 */
	//look for the nif file of the subject resource
	public void process(List<DBpediaRelation> listRelations, String nifPath) throws NoSuchAlgorithmException {
		int counterEquals = 0;
		int counterContains = 0;
		List<Report> listReport = new ArrayList<Report>();
		
		System.out.println(listRelations.get(0).getTagetRelation());
		
		for(DBpediaRelation rel : listRelations) {
			String[] sbjSplit = rel.getSbjURI().split("/");
			String sbj = sbjSplit[sbjSplit.length-1];
			String sbjAnchor = sbj.replaceAll("_", " ");
			
			String[] objSplit = rel.getObjURI().split("/");
			String objAnchor = objSplit[objSplit.length-1].replaceAll("_", " ");
			
			//System.out.println(sbjAnchor + "-" + objAnchor);
			File filePath = lookNIFFile(sbj, nifPath);
			
			
			//QUERIES
			List<Annotation> listLinkAnnotations = new ArrayList<Annotation>();
			BZip2CompressorInputStream inputStream = createBz2Reader(filePath);
			if(inputStream == null) {
				System.out.println("File \"" + filePath.getAbsolutePath() + "\" does not exist");
				continue;
			}
			Model model = createJenaModel(inputStream);
			listLinkAnnotations.addAll(queryAbstractAnnotations(model));
			
			for(Annotation ann : listLinkAnnotations) {
				//ann.printAnnotation();
				if(ann.getAnchor().equalsIgnoreCase(objAnchor)) {
					Report report = new Report();
					report.setSubject(sbjAnchor);
					report.setObject(objAnchor);
					report.setRelation(rel.getTagetRelation());
					System.out.println(sbjAnchor);
					System.out.println(ann.getAnchor() + " is equal to " + objAnchor);
					System.out.println(ann.getParagraphURI());
					String context = queryContext(model);
					String[] indexes = extractIndexes(ann.getParagraphURI()).split(","); //begin,end
					report.setContext(context.substring(Integer.parseInt(indexes[0]), Integer.parseInt(indexes[1])));
					System.out.println(context.substring(Integer.parseInt(indexes[0]), Integer.parseInt(indexes[1])));
					counterEquals++;
					listReport.add(report);
				}else if(ann.getAnchor().contains(objAnchor)) {
					System.out.println(ann.getAnchor() + " contains " + objAnchor);
					System.out.println(ann.getParagraphURI());
					counterContains++;
				}
			}
			System.out.println("\n============");
			//break;
		}
		
		writeReport(listReport, counterEquals, counterContains);
		
		System.out.println("number of equals: " + counterEquals);
		System.out.println("number of contains: " + counterContains);
	}
	
	public File lookNIFFile(String sbj, String nifPath) throws NoSuchAlgorithmException {
		String md5 = md5(sbj);
		File filePath = new File(nifPath+"/"+mdf5ToPath(md5)+".ttl.bz2");
		//System.out.println(filePath + " -- file exists? = " + filePath.exists());
		return filePath;
	}
	
	/*
	 * Utilities
	 */
	
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
	
	public String defineRelation(String relation) {
		String URI = "";
		String splitRelation[] = relation.split(":");
		if(splitRelation.length == 2)
			URI = "http://dbpedia.org/property/"+splitRelation[1];
		else
			URI = "http://dbpedia.org/ontology/"+relation;
		return URI;
	}
	
	public String extractIndexes(String paragraphURI) {
		return paragraphURI.split("char=")[1];
	}
	
	public String readSparqlQueries(File inputFile){
		String sparqlQuery = "";
		try(BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile),StandardCharsets.UTF_8))){
			String line = "";
			while((line = br.readLine()) != null) {
				sparqlQuery += line +" "; 
			}
			br.close();
		}catch(IOException e) {
			e.printStackTrace();
		}
		return sparqlQuery;
	}
	
	public String md5(String name) throws NoSuchAlgorithmException {
		String md5 = "";
		String base = "<http://nif.dbpedia.org/wiki/en/"+name;
		byte[] fileName = base.getBytes();
		MessageDigest md = MessageDigest.getInstance("MD5");
		md5 = toHexString(md.digest(fileName));
		return md5;
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
	
	public String mdf5ToPath(String md5) {
		return md5.substring(0, 2) + "/" + md5.substring(2,4) + "/" + md5.substring(4);
	}
	
	public void writeReport(List<Report> listReport, int numEquals, int numContains) {
		try(PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream("report.csv"),StandardCharsets.UTF_8))) {
			pw.write("number of equals = " + numEquals + "\n");
			pw.write("number of contains = " + numContains + "\n");
			
			pw.write("Subject\tRelation\tObject\tContext\n");
			for(Report r : listReport) {
				pw.write(r.getSubject()+"\t"+r.getRelation()+"\t"+r.getObject()+"\t"+r.getContext()+"\n");
			}
			pw.close();
		}catch(IOException e) {
			e.printStackTrace();
		}
	}
	

}
