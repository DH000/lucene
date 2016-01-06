package com.lucene.directory;

import java.io.IOException;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.SimpleFSDirectory;
import org.wltea.analyzer.lucene.IKAnalyzer;

import com.lucene.config.LuceneConfig;

/**
 * 
 * desc: 索引操作
 * 
 * @author xuelin
 * @date 2016年1月4日
 */
public class OperateIndexDemo {
	
	/* 创建简单中文分析器 创建索引使用的分词器必须和查询时候使用的分词器一样，否则查询不到想要的结果 */
	private Analyzer analyzer = null;
	// 目录对象，因为操作索引文件都要用到它，所以定义为全局变量
	private Directory directory = null;
	// 创建IndexWriter索引写入器
	IndexWriterConfig indexWriterConfig = null;
	SimpleDateFormat simpleDateFormat = null;
	
	/**
	 * 初始化方法
	 * 
	 * @throws IOException
	 */
	public void init() throws IOException {
		analyzer = new IKAnalyzer(true);
		directory = new SimpleFSDirectory(Paths.get(LuceneConfig.INDEX_PATH));
		
		simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		System.out.println("*****************初始化成功**********************");
	}
	
	/**
	 * 将article对象的属性全部封装成document对象，重构之后方便创建索引
	 * 
	 * @param article
	 * @return
	 */
	public Document createDocument(Article article) {
		Document document = new Document();
		document.add(new IntField("id", article.getId(), Field.Store.YES));
		document.add(new StringField("title", article.getTitle().toString(), Field.Store.YES));
		document.add(new StringField("content", article.getContent().toString(), Field.Store.YES));
		
		return document;
	}
	
	/**
	 * 获得指定格式的时间字符串
	 * 
	 * @return
	 */
	public String getDate() {
		return simpleDateFormat.format(new Date());
	}
	
	/**
	 * 为了如实反映操作索引文件之后的效果，每次操作之后查询索引目录下所有的索引内容
	 * 
	 * @throws IOException
	 * @throws CorruptIndexException
	 */
	public void openIndexFile() throws CorruptIndexException, IOException {
		System.out.println("*****************读取索引开始**********************");
		IndexReader indexReader = DirectoryReader.open(directory);
		int docLength = indexReader.maxDoc();
		for (int i = 0; i < docLength; i++) {
			Document doc = indexReader.document(i);
			Article article = new Article();
			if (doc.get("id") == null) {
				System.out.println("id为空");
			} else {
				article.setId(Integer.parseInt(doc.get("id")));
				article.setTitle(doc.get("title"));
				article.setContent(doc.get("content"));
			}
			System.out.println(article);
		}
		System.out.println("*****************读取索引结束**********************\n");
	}
	
	/**
	 * 创建索引到索引文件中
	 * 
	 * @param article
	 * @throws IOException
	 * @throws LockObtainFailedException
	 * @throws CorruptIndexException
	 */
	public void createIndex(Article article) throws CorruptIndexException, LockObtainFailedException, IOException {
		indexWriterConfig = new IndexWriterConfig(analyzer);
		IndexWriter indexWriter = new IndexWriter(directory, indexWriterConfig);
		indexWriter.addDocument(createDocument(article));
		
		indexWriter.close();
		System.out.println("[ " + getDate() + " ] Lucene写入索引到 [" + LuceneConfig.INDEX_PATH + "] 成功。");
	}
	
	/**
	 * 根据文件中的id删除对应的索引文件
	 * 
	 * @param contentId
	 * @throws IOException
	 * @throws ParseException
	 */
	public void deleteIndex(String contentId) throws IOException, ParseException {
		// 判断索引文件目录内容是否有索引，有返回true ,没有返回false
		if (DirectoryReader.indexExists(directory)) {
			indexWriterConfig = new IndexWriterConfig(analyzer);
			IndexWriter indexWriter = new IndexWriter(directory, indexWriterConfig);
			// 封装term去检索字段名为id ，具体值为contentId的记录，如果存在就会删除，否则什么都不做
			indexWriter.deleteDocuments(new Term("id", contentId));
			/*
			 * QueryParser queryParser = new QueryParser(Version.LUCENE_36,
			 * "id", analyzer); Query query = queryParser.parse(contentId);
			 * indexWriter.deleteDocuments(query);
			 */
			indexWriter.close();
			System.out.println("[ " + getDate() + " ] Lucene删除索引到 [" + LuceneConfig.INDEX_PATH + "] 成功。");
		} else {
			throw new IOException("[ " + getDate() + " ] Lucene删除索引失败，在 " + LuceneConfig.INDEX_PATH + "目录中没有找到索引文件。");
		}
	}
	
	/**
	 * 由于实际的内容修改，所以索引文件也要跟着修改 ,具体实现方式就是先删除索引，然后重新添加
	 * 
	 * @param article
	 * @throws IOException
	 * @throws ParseException
	 */
	public void updateIndex(Article article) throws IOException, ParseException {
		deleteIndex(article.getId().toString());
		createIndex(article);
	}
	
	/**
	 * 销毁当前的操作类的实现,主要关闭资源的连接
	 * 
	 * @throws IOException
	 */
	public void destory() throws IOException {
		analyzer.close();
		directory.close();
		System.out.println("*****************销毁成功**********************");
	}
	
	public static void main(String[] args) {
		OperateIndexDemo luceneInstance = new OperateIndexDemo();
		try {
			luceneInstance.init();// 初始化
			
			Article article = new Article(1, "标题不是很长", "内容也不长。但是句号很长。。。。。。。。。。");
			luceneInstance.createIndex(article);
			luceneInstance.openIndexFile();
			
			luceneInstance.deleteIndex("1");
			luceneInstance.openIndexFile();
			
			// article = new Article(1,"修改之后的标题","内容变短了");
			// luceneInstance.updateIndex(article);
			// luceneInstance.openIndexFile();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		} finally {
			try {
				luceneInstance.destory();// 销毁
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
}
