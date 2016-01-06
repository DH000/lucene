package com.lucene.ramd;

import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.RAMDirectory;

import com.lucene.config.LuceneConfig;

/**
 * 
 * desc:   内存索引
 * @author xuelin
 * @date   Dec 30, 2015
 */
public class RAMDirectoryDemo {
	public static void main(String[] args) throws IOException {
		// 创建目录
		RAMDirectory directory = new RAMDirectory();
		
		// 分词器
		Analyzer analyzer = new SimpleAnalyzer();
		
		// 创建write
		IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
		IndexWriter writer = new IndexWriter(directory, iwc);
		
		// 创建doc
		for(int i=0; i<LuceneConfig.count; i++){
			Document doc = new Document();
			FieldType indexType = new FieldType();
			indexType.setStored(true);
//			doc.add(new Field("username", "tom" + i, indexType));
			doc.add(new StringField("username", "好男人" + i, Field.Store.YES));
			doc.add(new StringField("password", "ttttt" + i, Field.Store.YES));
			doc.add(new IntField("age", i, Field.Store.YES));
			writer.addDocument(doc);
		}
		
		writer.commit();
		writer.close();
		
		System.out.println("/************************开始检索**************************/");
		long startTime = System.currentTimeMillis();
		IndexReader reader = DirectoryReader.open(directory);
		IndexSearcher searcher = new IndexSearcher(reader);
		
		// 全匹配
		Query query = new TermQuery(new Term("username", "好男人0"));
		
		// 模糊查询
//		Query query = new WildcardQuery(new Term("username", "*男*"));
		TopDocs topDocs = searcher.search(query, 10);
		long endTime = System.currentTimeMillis();
		System.out.println(endTime - startTime);
		for(int i=0; i<topDocs.scoreDocs.length; i++){
			Document doc = searcher.doc(topDocs.scoreDocs[i].doc);
			System.out.println("username: " + doc.getField("username").stringValue() + " password: " + doc.getField("password").stringValue() + " age: " + doc.getField("age").numericValue());
		}
		reader.close();
		directory.close();
		System.out.println("/************************结束检索**************************/");
	}
}
