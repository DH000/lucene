package com.lucene.directory;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.wltea.analyzer.lucene.IKAnalyzer;

import com.lucene.config.LuceneConfig;

public class LuceneSearchDemo {
	
	/* 创建简单中文分析器 创建索引使用的分词器必须和查询时候使用的分词器一样，否则查询不到想要的结果 */
	private Analyzer analyzer = null;
	// 目录对象，因为操作索引文件都要用到它，所以定义为全局变量
	private Directory directory = null;
	// 读
	private IndexReader reader = null;
	// 索引搜索对象
	private IndexSearcher indexSearcher;
	
	/**
	 * 初始化方法
	 * 
	 * @throws IOException
	 */
	public void init() throws IOException {
		analyzer = new IKAnalyzer(true);
		directory = new SimpleFSDirectory(Paths.get(LuceneConfig.INDEX_PATH));
		reader = DirectoryReader.open(directory);
		indexSearcher = new IndexSearcher(reader);
		
		System.out.println("*****************初始化成功**********************");
	}
	
	/**
	 * 根据传递的结果集 封装成集合后显示出来
	 * 
	 * @param scoreDocs
	 * @throws IOException
	 * @throws CorruptIndexException
	 */
	public void showResult(ScoreDoc[] scoreDocs) throws CorruptIndexException, IOException {
		List<Article> articles = new ArrayList<Article>();
		for (int i = 0; i < scoreDocs.length; i++) {
			int doc = scoreDocs[i].doc;// 索引id
			Document document = indexSearcher.doc(doc);
			
			Article article = new Article();
			if (document.get("id") == null) {
				System.out.println("id为空");
			} else {
				article.setId(Integer.parseInt(document.get("id")));
				article.setTitle(document.get("title"));
				article.setContent(document.get("content"));
				articles.add(article);
			}
		}
		if (articles.size() != 0) {
			for (Article article : articles) {
				System.out.println(article);
			}
		} else {
			System.out.println("没有查到记录。");
		}
	}
	
	/**
	 * <1> 添加索引时用的什么分词器，那么检索的时候也必须使用这个分词器，否则查询不到任何记录。因为插入索引的时候是按照分词器规则插入的，
	 * 所以检索的时候会以这种规则为匹配方式； <2> 由于次查询方式是使用的标题为查询条件，所以内容中出现的任何字符不会作为匹配元素。
	 * 如果要使用多字段作为查询条件那么就要使用MultiQueryParser绑定多个字段封装Query查询对象了。
	 * 
	 * 通过QueryParser绑定单个字段来检索索引记录
	 * 
	 * @param keyword
	 * @throws ParseException
	 * @throws IOException
	 * @throws CorruptIndexException
	 */
	public void searchByQueryParser(String keyword) throws ParseException, CorruptIndexException, IOException {
		System.out.println("*****************通过QueryParser来检索索引记录**********************");
		QueryParser queryParser = new QueryParser("title", analyzer);
		Query query = queryParser.parse(keyword);
		// public TopFieldDocs search(Query query, int n, Sort sort)
		// 参数分别表示 Query查询对象，返回的查询数目，排序对象
		TopDocs topDocs = indexSearcher.search(query, 10, new Sort());
		showResult(topDocs.scoreDocs);
	}
	
	/**
	 * <1>实际开发如果是全文检索就不建议使用排序，因为排序会导致最优匹配的结果错乱，但是如果是使用单个字段查询的话 倒是没有问题
	 * 
	 * 通过MultiQueryParser绑定多个字段来检索索引记录
	 * 
	 * @param keyword
	 * @throws ParseException
	 * @throws IOException
	 * @throws CorruptIndexException
	 */
	public void searchByMultiFieldQueryParser(String keyword) throws ParseException, CorruptIndexException, IOException {
		System.out.println("*****************通过MultiQueryParser绑定多个字段来检索索引记录**********************");
		QueryParser queryParser = new MultiFieldQueryParser(new String[] { "title", "content" }, analyzer);
		Query query = queryParser.parse(keyword);
		Sort sort = new Sort(); // 不传参数则按照最优匹配来排序
		// Sort sort = new Sort(new SortField("title", SortField.STRING,
		// false)); //最后一个参数很关键，默认是降序的，如果要指定为升序改为true即可。
		
		TopDocs topDocs = indexSearcher.search(query, 10, sort);
		showResult(topDocs.scoreDocs);
	}
	
	/**
	 * 这种查询方式有点怪，关键字只能是常用的词组，比如“上海”，“中文” 等等，不是常用的词组则什么都不会显示，比如：“文和”，“京津沪”等等
	 * 
	 * 通过term组合检索索引记录
	 * 
	 * @param keyword
	 * @throws IOException
	 */
	public void searchByTerm(String keyword) throws IOException {
		System.out.println("*****************通过term组合检索索引记录**********************");
		// Term term = new Term("title", keyword);
		Term term = new Term("content", keyword);
		// MultiTermQuery
		Query query = new TermQuery(term);
		
		TopDocs topDocs = indexSearcher.search(query, 10);
		showResult(topDocs.scoreDocs);
	}
	
	/**
	 * <1> *代表0个或多个字符,?代表0个或一个字符 ； <2> 这种查询方式根据通配符针对中文有效,
	 * 对英文和数字完全没有效果，任何英文和数字都不行； <3> 该检索方式对空格不敏感，就是说
	 * 如果最后一个字符为空格，然后匹配的时候空格不会作为匹配内容。
	 * 
	 * 通过wildcard使用通配符组合检索索引记录
	 * 
	 * @param keyword
	 * @throws IOException
	 */
	public void searchByWildcard(String keyword) throws IOException {
		System.out.println("*****************通过wildcard使用通配符组合检索索引记录**********************");
		// Term term = new Term("title", "*" + keyword + "*");
		// Term term = new Term("content", "*" + keyword + "*");
		Term term = new Term("content", "*" + keyword + "*");
		
		Query query = new WildcardQuery(term);
		TopDocs topDocs = indexSearcher.search(query, 10);
		showResult(topDocs.scoreDocs);
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
	
	/**
	 * <1>如果使用的分词器是SimpleAnalyzer，那么会严格按照把关键字作为前缀去检索，但是如果使用的分词器是IKAanlayzer，
	 * 那么会模糊匹配查询 <2>同样该方式针对中文有效, 对英文和数字完全没有效果，任何英文和数字都不行；
	 * 
	 * 通过prefix作为前缀组合检索索引记录
	 * 
	 * @param keyword
	 * @throws IOException
	 */
	public void searchByPrefix(String keyword) throws IOException {
		System.out.println("*****************通过prefix作为前缀组合检索索引记录**********************");
		// Term term = new Term("title", keyword);
		Term term = new Term("content", keyword);
		Query query = new PrefixQuery(term);
		
		TopDocs topDocs = indexSearcher.search(query, 10);
		showResult(topDocs.scoreDocs);
	}
	
	/**
	 * 通过filter过滤条件组合检索索引记录
	 * 
	 * @param keywords
	 * @throws IOException
	 */
	public void searchByFilter(String[] keywords) throws IOException {
		System.out.println("*****************通过filter过滤条件组合检索索引记录**********************");
		Term term = new Term("content", "*" + keywords[0] + "*");
		Query query = new WildcardQuery(term);
		Query contFilter1 = new WildcardQuery(new Term("content", "*" + keywords[1] + "*"));
		Query contFilter2 = new WildcardQuery(new Term("content", "*" + keywords[2] + "*"));
		
		BooleanQuery booleanQuery = new BooleanQuery.Builder().add(query, Occur.SHOULD).add(contFilter1, Occur.FILTER).add(contFilter2, Occur.FILTER).setDisableCoord(true).build();
		
		TopDocs topDocs = indexSearcher.search(booleanQuery, 10);
		showResult(topDocs.scoreDocs);
	}
	
	/** 
	 * 通过boolean检索索引记录 
	 * @param keyword 
	 * @throws IOException 
	 */  
	public void searchByBoolean(String[] keywords) throws IOException{  
	    System.out.println("*****************通过boolean检索索引记录**********************");  
	    Query query1 = new WildcardQuery(new Term("content","*" + keywords[0] + "*"));  
	    Query query2 = new WildcardQuery(new Term("content","*" + keywords[1] + "*"));  
	      
	    BooleanQuery query = new BooleanQuery.Builder()
	    .add(query1, BooleanClause.Occur.MUST)
	    .add(query2, BooleanClause.Occur.MUST)
	    .build();  
	    //query.add(query2, BooleanClause.Occur.MUST_NOT);  
	    //query.add(query2, BooleanClause.Occur.SHOULD);  
	    System.out.println(query);  
	    TopDocs topDocs = indexSearcher.search(query, 10);  
	    showResult(topDocs.scoreDocs);        
	}
	
	public static void main(String[] args) {
		LuceneSearchDemo luceneInstance = new LuceneSearchDemo();
		try {
			luceneInstance.init();
			luceneInstance.searchByQueryParser("沪K");
			luceneInstance.searchByMultiFieldQueryParser("沪K");
			luceneInstance.searchByTerm("上海");
			luceneInstance.searchByWildcard("沪");
			luceneInstance.searchByPrefix("沪");
			luceneInstance.searchByFilter(new String[] { "分词", "语言", "" });
			luceneInstance.searchByBoolean(new String[]{"分词","一个"});
		} catch (CorruptIndexException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				luceneInstance.destory();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
}
