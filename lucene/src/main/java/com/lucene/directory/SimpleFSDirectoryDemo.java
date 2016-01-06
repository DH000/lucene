package com.lucene.directory;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleFragmenter;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.wltea.analyzer.lucene.IKAnalyzer;

import com.lucene.config.LuceneConfig;

public class SimpleFSDirectoryDemo {
	/* 创建简单中文分析器 创建索引使用的分词器必须和查询时候使用的分词器一样，否则查询不到想要的结果 */
	private Analyzer analyzer = new IKAnalyzer(true);
	
	/**
	 * 
	 * desc: 创建索引文件
	 */
	public void createIndexFile() {
		long startTime = System.currentTimeMillis();
		System.out.println("*****************创建索引开始**********************");
		Directory directory = null;
		IndexWriter indexWriter = null;
		
		try {
			IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
			directory = new SimpleFSDirectory(Paths.get(LuceneConfig.INDEX_PATH));
			indexWriter = new IndexWriter(directory, iwc);
			
			// 为了避免重复插入数据，每次测试前 先删除之前的索引
			indexWriter.deleteAll();
			
			// 获取实体对象
			List<Article> articleList = getArticles();
			for (int i = 0; i < articleList.size(); i++) {
				Article article = articleList.get(i);
				// indexWriter添加索引
				Document doc = new Document();
				
				doc.add(new IntField("id", article.getId(), Field.Store.YES));
				doc.add(new TextField("title", article.getTitle(), Field.Store.YES));
				doc.add(new TextField("content", article.getContent(), Field.Store.YES));
				// 添加到索引中去
				indexWriter.addDocument(doc);
				System.out.println("索引添加成功：第" + (i + 1) + "次！！");
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (indexWriter != null) {
				try {
					indexWriter.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (directory != null) {
				try {
					directory.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		long endTime = System.currentTimeMillis();
		System.out.println("创建索引文件成功，总共花费" + (endTime - startTime) + "毫秒。");
		System.out.println("*****************创建索引结束**********************");
	}
	
	/**
	 * 
	 * desc: 打开索引文件
	 */
	public void openIndexFile() {
		long startTime = System.currentTimeMillis();
		System.out.println("*****************读取索引开始**********************");
		List<Article> articles = new ArrayList<Article>();
		// 得到索引的目录
		Directory directory = null;
		IndexReader indexReader = null;
		
		try {
			directory = new SimpleFSDirectory(Paths.get(LuceneConfig.INDEX_PATH));
			indexReader = DirectoryReader.open(directory);
			
			System.out.println("在索引文件中总共插入了" + indexReader.maxDoc() + "条记录。");
			
			int len = indexReader.maxDoc();
			
			// 获取第一个插入的document对象
			Document minDoc = indexReader.document(0);
			// 获取最后一个插入的document对象
			Document maxDoc = indexReader.document(len - 1);
			
			// document对象的get(字段名称)方法获取字段的值
			System.out.println("第一个插入的document对象的标题是：" + minDoc.get("title"));
			System.out.println("最后一个插入的document对象的标题是：" + maxDoc.get("title"));
			
			for (int i = 0; i < len; i++) {
				Document doc = indexReader.document(i);
				Article article = new Article();
				if (doc.get("id") == null) {
					System.out.println("id为空");
				} else {
					article.setId(Integer.parseInt(doc.get("id")));
					article.setTitle(doc.get("title"));
					article.setContent(doc.get("content"));
					articles.add(article);
				}
			}
			
			System.out.println("显示所有插入的索引记录：");
			for (Article article : articles) {
				System.out.println(article);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (indexReader != null) {
				try {
					indexReader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (directory != null) {
				try {
					directory.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		long endTime = System.currentTimeMillis();
		System.out.println("直接读取索引文件成功，总共花费" + (endTime - startTime) + "毫秒。");
		System.out.println("*****************读取索引结束**********************");
	}
	
	/**
	 * 根据关键字实现全文检索
	 */
	public void searchIndexFile(String keyword) {
		long startTime = System.currentTimeMillis();
		System.out.println("*****************查询索引开始**********************");
		IndexReader indexReader = null;
		IndexSearcher indexSearcher = null;
		List<Article> articleList = new ArrayList<Article>();
		
		try {
			indexReader = DirectoryReader.open(FSDirectory.open(Paths.get(LuceneConfig.INDEX_PATH)));
			
			// 创建一个排序对象，其中SortField构造方法中，第一个是排序的字段，第二个是指定字段的类型，第三个是是否升序排列，true：升序，false：降序。
			/**
			 * 必须支持Sorted
			 */
			Sort sort = new Sort(new SortField[] { new SortField("title", SortField.Type.STRING, false), new SortField("content", SortField.Type.STRING, false) });
			// 创建搜索类
			indexSearcher = new IndexSearcher(indexReader);
			// QueryParser支持单个字段的查询，但是MultiFieldQueryParser可以支持多个字段查询，建议用后者这样可以实现全文检索的功能。
			// QueryParser queryParser = new QueryParser("title", analyzer);
			// 利用queryParser解析传递过来的检索关键字，完成Query对象的封装
			Query query = MultiFieldQueryParser.parse(keyword, new String[] { "title", "content" }, new Occur[] { Occur.SHOULD, Occur.SHOULD }, analyzer);
			
//			splitWord(keyword, true); // 显示拆分结果
			
			// 执行检索操作
			TopDocs topDocs = indexSearcher.search(query, 5);
			System.out.println("一共查到:" + topDocs.totalHits + "记录");
			ScoreDoc[] scoreDoc = topDocs.scoreDocs;
			// 像百度，谷歌检索出来的关键字如果有，除了显示在列表中之外还会高亮显示。Lucenen也支持高亮功能，正常应该是<font
			// color='red'></font>这里用【】替代，使效果更加明显
			
			for (int i = 0; i < scoreDoc.length; i++) {
				// 内部编号 ,和数据库表中的唯一标识列一样
				int doc = scoreDoc[i].doc;
				// 根据文档id找到文档
				Document mydoc = indexSearcher.doc(doc);
				
				String id = mydoc.get("id");
				String title = mydoc.get("title");
				String content = mydoc.get("content");
				Highlighter highlighter = new Highlighter(new SimpleHTMLFormatter("【", "】"), new QueryScorer(query));
				if (title != null && !title.equals("")) {
					title = highlighter.getBestFragment(analyzer.tokenStream("title", new StringReader(title)), title);
				}
				if (content != null && !content.equals("")) {
					// 传递的长度表示检索之后匹配长度，这个会导致返回的内容不全
					highlighter.setTextFragmenter(new SimpleFragmenter(content.length()));
					content = highlighter.getBestFragment(analyzer.tokenStream("content", new StringReader(content)), content);
				}
				// 需要注意的是 如果使用了高亮显示的操作，查询的字段中没有需要高亮显示的内容 highlighter会返回一个null回来。
				articleList.add(new Article(Integer.valueOf(id), title == null ? mydoc.get("title") : title, content == null ? mydoc.get("content") : content));
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (InvalidTokenOffsetsException e) {
			e.printStackTrace();
		} finally {
			if (indexReader != null) {
				try {
					indexReader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		System.out.println("根据关键字" + keyword + "检索到的结果如下：");
		for (Article article : articleList) {
			System.out.println(article);
		}
		long endTime = System.currentTimeMillis();
		System.out.println("全文索引文件成功，总共花费" + (endTime - startTime) + "毫秒。");
		System.out.println("*****************查询索引结束**********************");
	}
	
	/**
	 * 查看IKAnalyzer 分词器是如何将一个完整的词组进行分词的
	 * 
	 * @param text
	 * @param isMaxWordLength
	 */
	public void splitWord(String text, boolean isMaxWordLength) {
		try (Analyzer analyzer1 = new IKAnalyzer(isMaxWordLength)) {
			// 创建分词对象
			StringReader reader = new StringReader(text);
			// 分词
			TokenStream ts = analyzer1.tokenStream("", reader);
			CharTermAttribute term = ts.getAttribute(CharTermAttribute.class);
			// 遍历分词数据
			System.out.print("IKAnalyzer把关键字拆分的结果是：");
			while (ts.incrementToken()) {
				System.out.print("【" + term.toString() + "】");
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println();
	}
	
	/**
	 * 
	 * desc: 源数据列表
	 * 
	 * @return
	 */
	public List<Article> getArticles() {
		Article article0 = new Article(1, "Simple Analyzer", "这个分词是一段一段话进行分 ");
		Article article1 = new Article(2, "Standard Analyzer", "标准分词拿来分中文和ChineseAnalyzer一样的效果");
		Article article2 = new Article(3, "PerField AnalyzerWrapper", "这个很有意思，可以封装很多分词方式，还可以于先设置field用那个分词分！牛 ");
		Article article3 = new Article(4, "CJK Analyzer", "这个分词方式是正向退一分词(二分法分词)，同一个字会和它的左边和右边组合成一个次，每个人出现两次，除了首字和末字 ");
		Article article4 = new Article(5, "Chinese Analyzer", "这个是专业的中文分词器，一个一个字分 ");
		Article article5 = new Article(6, " BrazilianAnalyzer", "巴西语言分词 ");
		Article article6 = new Article(7, " CzechAnalyzer", "捷克语言分词 ");
		Article article7 = new Article(8, "DutchAnalyzer", "荷兰语言分词 ");
		Article article8 = new Article(9, "FrenchAnalyzer", "法国语言分词 ");
		Article article9 = new Article(10, "沪K123", "这是一个车牌号，包含中文，字母，数字");
		Article article10 = new Article(11, "沪K345", "上海~！@~！@");
		Article article11 = new Article(12, "沪B678", "京津沪");
		Article article12 = new Article(13, "沪A3424", "沪K345 沪K3 沪K123 沪K111111111 沪ABC");
		Article article13 = new Article(14, "沪 B2222", "");
		Article article14 = new Article(15, "沪K3454653", "沪K345");
		Article article15 = new Article(16, "123 123 1 2 23 3", "沪K123");
		List<Article> articleList = new ArrayList<Article>();
		articleList.add(article0);
		articleList.add(article1);
		articleList.add(article2);
		articleList.add(article3);
		articleList.add(article4);
		articleList.add(article5);
		articleList.add(article6);
		articleList.add(article7);
		articleList.add(article8);
		articleList.add(article9);
		articleList.add(article10);
		articleList.add(article11);
		articleList.add(article12);
		articleList.add(article13);
		articleList.add(article14);
		articleList.add(article15);
		
		return articleList;
	}
	
	public static void main(String[] args) {
		SimpleFSDirectoryDemo demo = new SimpleFSDirectoryDemo();
		demo.createIndexFile();
		demo.openIndexFile();
		String[] searchKeywords = new String[] { "analyzer", "沪B123", "沪K123", "沪K123 上海", "沪K3454653", "g" };
		demo.searchIndexFile(searchKeywords[1]);
	}
}
