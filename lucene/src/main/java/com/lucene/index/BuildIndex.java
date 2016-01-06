package com.lucene.index;

import java.io.IOException;
import java.nio.file.Paths;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.DoubleField;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FloatField;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import com.lucene.config.LuceneConfig;

/**
 * 
 * desc:   创建索引
 * @author xuelin
 * @date   Dec 30, 2015
 */
public class BuildIndex {
	
	public static synchronized void buildForFSD() throws IOException{
		// 索引目录
		Directory indexDir = FSDirectory.open(Paths.get(LuceneConfig.INDEX_PATH));
		// 标准分词器
		Analyzer analyzer = new StandardAnalyzer();
		// 索引配置
		IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
		// 追加方式
		iwc.setOpenMode(LuceneConfig.MODE);
		
		writeField(indexDir, iwc);
	}
	
	public static void writeField(Directory indexDir, IndexWriterConfig iwc){
		// write
		IndexWriter writer = null;
		try {
			writer = new IndexWriter(indexDir, iwc);
			// 创建doc
			Document doc = null;
			for(int i=0; i<LuceneConfig.count; i++){
				doc = new Document();
				doc.add(new StringField("string-" + i, "" + i, Field.Store.YES));
				doc.add(new IntField("int-" + i, i, Field.Store.YES));
				doc.add(new FloatField("float-" + i, i * 0.1f, Field.Store.YES));
				doc.add(new DoubleField("double-" + i, i * 0.1, Field.Store.YES));
				doc.add(new LongField("long-" + i, i, Field.Store.YES));
				writer.addDocument(doc);
			}
			System.out.println("nums: " + writer.numDocs());
			writer.commit();
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			if(null != writer){
				try {
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public static void main(String[] args) {
		try {
			buildForFSD();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}




