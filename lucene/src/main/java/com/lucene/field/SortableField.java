package com.lucene.field;

import java.io.Reader;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.DocValuesType;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.util.BytesRef;

/**
 * 
 * desc: 可排序
 * 
 * @author xuelin
 * @date 2016年1月4日
 */
public class SortableField extends Field {
	
	/** Indexed, tokenized, sorted, not stored. */
	public static final FieldType TYPE_NOT_STORED = new FieldType();
	
	/** Indexed, tokenized, sorted, stored. */
	public static final FieldType TYPE_STORED = new FieldType();
	
	static {
		TYPE_NOT_STORED.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS);
		TYPE_NOT_STORED.setTokenized(false);
		TYPE_NOT_STORED.setDocValuesType(DocValuesType.SORTED);
		TYPE_NOT_STORED.freeze();
		
		TYPE_STORED.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS);
		TYPE_STORED.setTokenized(false);
		TYPE_STORED.setDocValuesType(DocValuesType.SORTED);
		TYPE_STORED.setStored(true);
		TYPE_STORED.freeze();
	}
	
	public SortableField(String name, byte[] value, Store store) {
		super(name, value, Store.YES == store ? TYPE_STORED : TYPE_NOT_STORED);
	}
	
	public SortableField(String name, String value, Store store) {
		super(name, value.getBytes(), Store.YES == store ? TYPE_STORED : TYPE_NOT_STORED);
	}
	
	public SortableField(String name, BytesRef bytes, Store store) {
		super(name, bytes, Store.YES == store ? TYPE_STORED : TYPE_NOT_STORED);
	}
	
	public SortableField(String name, Reader reader, Store store) {
		super(name, reader, Store.YES == store ? TYPE_STORED : TYPE_NOT_STORED);
	}
	
	public SortableField(String name, TokenStream tokenStream, Store store) {
		super(name, tokenStream, Store.YES == store ? TYPE_STORED : TYPE_NOT_STORED);
	}
	
	@Override
	public String stringValue() {
		if (fieldsData instanceof String || fieldsData instanceof Number) {
			return fieldsData.toString();
		} else if (fieldsData instanceof BytesRef) {
			BytesRef bytesRef = (BytesRef) fieldsData;
			if (bytesRef.length > 0) {
				return new String(bytesRef.bytes, 0, bytesRef.length);
			}
		}
		
		return null;
	}
	
}
