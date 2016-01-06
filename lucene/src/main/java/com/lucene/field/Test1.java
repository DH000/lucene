package com.lucene.field;

public class Test1 {
	public static void main(String[] args) {
		int[] arr = new int[]{1, -2, 3, 10, -4, 7, 2, -5};
		
		int max = 0;
		int maxSum = 0;
		int temp = 0;
		
		for(int i=0; i<arr.length; i++){
			temp += arr[i];
			
			max = Math.max(max, arr[i]);
			
			if(temp > maxSum){
				maxSum = temp;
			}
			
			if(temp < 0){
				temp = 0;
			}
		}
		
		if(max > maxSum){
			maxSum = max;
		}
		
		System.out.println(maxSum);
	}
}
