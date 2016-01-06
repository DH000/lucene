package com.lucene.search;

import java.io.IOException;
import java.nio.file.Paths;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;

import com.lucene.config.LuceneConfig;

public class SearchIndex {
	
	public static void readIndex() throws IOException, ParseException{
		IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(LuceneConfig.INDEX_PATH)));
		IndexSearcher searcher = new IndexSearcher(reader);
		Analyzer analyzer = new StandardAnalyzer();
		QueryParser parser = new QueryParser("int-0", analyzer);
		Query query = parser.parse("0");
		TopDocs hits = searcher.search(query, 10);
		
		ScoreDoc[] docs = hits.scoreDocs;
		
		for(ScoreDoc scoreDoc : docs){
			Document doc = searcher.doc(scoreDoc.doc);
			System.out.println(doc);
		}
		
		reader.close();
	}
	
	public static void main(String[] args) throws IOException, ParseException {
		readIndex();
	}
	
}




















