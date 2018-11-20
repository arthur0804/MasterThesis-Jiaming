import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path; 
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader; 
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException; 
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.classic.QueryParser.Operator;
import org.apache.lucene.search.IndexSearcher; 
import org.apache.lucene.search.Query; 
import org.apache.lucene.search.ScoreDoc; 
import org.apache.lucene.search.TopDocs ;
import org.apache.lucene.store.Directory; 
import org.apache.lucene.store.FSDirectory;


public class SearchWithoutDuplicate {
	public static void SearchMethod(ArrayList<String> queries) throws ParseException, IOException {
		
		// search for the CONTENT field (abstract text) and Title field , which is defined in CreateIndex
		String[] fields ={"title", "content"};
		Map<String, Float> boosts = new  HashMap();
		boosts.put("title", (float) 3.0);
		boosts.put("content", (float) 0.5);
		
		// set directory of indexes
		Path indexPath = Paths.get("/proj/wangyue/jiamingfolder/index");
		Directory dir = FSDirectory.open(indexPath);
				
		// create index reader, writer, analyzer, query parser
		IndexReader reader = DirectoryReader.open(dir);
		IndexSearcher searcher = new IndexSearcher(reader);
		Analyzer analyzer = new StandardAnalyzer();
		MultiFieldQueryParser parser = new MultiFieldQueryParser (fields, analyzer, boosts);
		parser.setDefaultOperator(Operator.AND);
		
		// execute queries and write the result into a text file			
		// create headers in the result log
		String header = "TOPIC_NO" + " " + " Q0" + " " + "ID" + " " + "RANK" + " " + "SCORE" + " " + "RUN_NAME" + "\n";
		Files.write(Paths.get("/proj/wangyue/jiamingfolder/dat/basic_query_result_withoutduplicate.txt"), header.getBytes(), StandardOpenOption.APPEND);
			
		// iterate through the queries list to execute
		int topic_no = 1;
		for(String query : queries) {
			Query multifieldquery = parser.parse("query");
			ArrayList<String> documentidlist = new ArrayList<>();
			
			// top 1000 results
			TopDocs tds = searcher.search(multifieldquery, 1001);
			// document rank in the retrieval result
			int rank = 1; 
			
			for(ScoreDoc sd : tds.scoreDocs) {
				Document document = searcher.doc(sd.doc);
				String ID = document.get("id");
				
				if(!documentidlist.contains(ID)) {
					String TOPIC_NO = String.valueOf(topic_no);
					String Q0 = "0";
					String RANK = String.valueOf(rank);
					String SCORE = String.valueOf(sd.score);
					String RUN_NAME = "my_run";
					String NEW_RECORD = TOPIC_NO + " " + Q0 + " " + ID + " " + RANK + " " + SCORE + " " + RUN_NAME + "\n";
					Files.write(Paths.get("/proj/wangyue/jiamingfolder/dat/basic_query_result_withoutduplicate.txt"), NEW_RECORD.getBytes(), StandardOpenOption.APPEND);
					documentidlist.add(ID);		
					rank ++;
				}else {
					continue;
				}	
				// end of the loop for 1k documents
			}
		topic_no ++ ;
		// end of the loop for 30 queries
		}	
	}
}