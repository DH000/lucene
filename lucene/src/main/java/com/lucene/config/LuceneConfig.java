package com.lucene.config;

import org.apache.lucene.index.IndexWriterConfig.OpenMode;

/**
 * 
 * desc:   lucene配置信息
 * @author xuelin
 * @date   Dec 30, 2015
 */
public final class LuceneConfig {
	/**
	 * 追加方式
	 */
	public static final OpenMode MODE = OpenMode.CREATE_OR_APPEND;
	
	public static final String INDEX_PATH = "/Users/Harlin/lucene/index";
	
	public static final String INDEX_DOCS_PATH = "/Users/Harlin/lucene/index/docs";
	
	public static final int count = 10000;
	
}
