package com.lucene.directory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Test {
	public static void main(String[] args) {
	
		List<String> vals = new ArrayList<>(Arrays.asList(new String[]{"1", "2", "3"}));
		System.out.println(vals.toArray(new String[0]).length);
	}
}
