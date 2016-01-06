package com.lucene.directory;

class Article {
	private Integer id;
	private String title;
	private String content;
	
	public Article() {
		super();
	}
	
	public Article(Integer id, String title, String content) {
		super();
		this.id = id;
		this.title = title;
		this.content = content;
	}
	
	public synchronized Integer getId() {
		return id;
	}
	
	public synchronized void setId(Integer id) {
		this.id = id;
	}
	
	public synchronized String getTitle() {
		return title;
	}
	
	public synchronized void setTitle(String title) {
		this.title = title;
	}
	
	public synchronized String getContent() {
		return content;
	}
	
	public synchronized void setContent(String content) {
		this.content = content;
	}
	
	@Override
	public String toString() {
		return "Article [id=" + id + ", title=" + title + ", content=" + content + "]";
	}
}
