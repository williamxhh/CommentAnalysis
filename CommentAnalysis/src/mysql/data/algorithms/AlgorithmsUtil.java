package mysql.data.algorithms;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import mysql.data.util.PropertiesUtil;

public class AlgorithmsUtil {
	
	private Set<String> stopWordsSet = null;
	
	public int editDistance(List<String> template1, List<String> template2) {
		List<String> l1 = removeStopWords(template1);
		List<String> l2 = removeStopWords(template2);
		
		int len1 = l1.size();
		int len2 = l2.size();
		
		int[][] distance = new int[len1 + 1][len2 + 1];
		
		for(int i = 0; i <= len1; ++i) {
			distance[i][0] = i;
		}
		
		for(int j = 0; j <= len2; ++j) {
			distance[0][j] = j;
		}
		
		for(int i = 1; i <= len1; ++i) {
			for(int j = 1; j <= len2; ++j) {
				if(l1.get(i-1).equals(l2.get(j-1))) {
					distance[i][j] = distance[i-1][j-1];
				} else {
					distance[i][j] = distance[i-1][j-1] + 1;
				}
				
				distance[i][j] = Math.min(distance[i][j], Math.min(distance[i-1][j] + 1, distance[i][j-1] + 1));
			}
		}
		
		return distance[len1][len2];
	}
	
	public List<String> removeStopWords(List<String> origin) {
		List<String> result = new ArrayList<String>();
//		stopWordsSet = loadStopWords();
//		for(String word : origin) {
//			if(!stopWordsSet.contains(word) && !word.replaceAll("[^\u4e00-\u9fa5]", "").equals("")) {
//				result.add(word);
//			}
//		}
		
		Set<String> reservedWords = loadReservedTemplateWords();
		for(String word : origin) {
			if(reservedWords.contains(word)) {
				result.add(word);
			}
		}
		return result;
	}
	
	//加载配置的模板词候选项，将用最长公共子序列提取的模板中非模板候选项的词去掉
	public Set<String> loadReservedTemplateWords() {
		Set<String> reservedWords = new HashSet<String>();
		reservedWords.addAll(loadReservedTemplateWordsEssential());
		reservedWords.addAll(loadReservedTemplateWordsOptional());
		return reservedWords;
	}
	
	public Set<String> loadReservedTemplateWordsEssential() {
		Set<String> reservedWordsEssential = new HashSet<String>();
		for(String word : PropertiesUtil.getProperty("mysql.data.algorithms.AlgorithmsUtil.reservedWordsEssential").toString().split(",")) {
			reservedWordsEssential.add(word);
		}
		return reservedWordsEssential;
	}
	
	public Set<String> loadReservedTemplateWordsOptional() {
		Set<String> reservedWordsOptional = new HashSet<String>();
		for(String word : PropertiesUtil.getProperty("mysql.data.algorithms.AlgorithmsUtil.reservedWordsOptional").toString().split(",")) {
			reservedWordsOptional.add(word);
		}
		return reservedWordsOptional;
	}
	
	protected Set<String> loadStopWords() {
		if(stopWordsSet == null) {
			stopWordsSet = new HashSet<String>();
			try {
				BufferedReader reader = new BufferedReader(new FileReader(PropertiesUtil.getProperty("mysql.data.analysis.quality.CommentsQualityAnalysis.stopWords")));
				String line = "";
				while((line = reader.readLine()) != null) {
					stopWordsSet.add(line.trim());
				}
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return stopWordsSet;
	}
	
	public List<String> longestCommonString(List<String> l1, List<String> l2) {
		List<String> common = new ArrayList<String>();
		if(l1 == null || l2 == null || l1.size() == 0 || l2.size() == 0) {
			return common;
		}
		int len1 = l1.size();
		int len2 = l2.size();
		
		int[][] c = new int[len1 + 1][len2 + 1];
		// l stands for left, u stands for upper, b stands for back
		char[][] b = new char[len1 + 1][len2 + 1];
		
		for(int i = 1; i <= len1; i++) {
			c[i][0] = 0;
		}
		
		for(int j = 0; j <= len2; j++) {
			c[0][j] = 0;
		}
		
		for(int i = 1; i <= len1; i++) {
			for(int j = 1; j <= len2; j++) {
				if(l1.get(i-1).equals(l2.get(j-1))) {
					c[i][j] = c[i-1][j-1] + l1.get(i-1).length();
					b[i][j] = 'b';
				} else if(c[i-1][j] >= c[i][j-1]) {
					c[i][j] = c[i-1][j];
					b[i][j] = 'u';
				} else {
					c[i][j] = c[i][j-1];
					b[i][j] = 'l';
				}
			}
		}
		
		common = getLongest(l1, b, len1, len2);
		return common;
	}
	
	private List<String> getLongest(List<String> l1,char[][] b, int i, int j) {
		List<String> common = new ArrayList<String>();
		if(i == 0 || j == 0) {
			return common;
		}
		if(b[i][j] == 'b'){
			common.addAll(getLongest(l1, b, i-1, j-1));
			common.add(l1.get(i-1));
		}else if(b[i][j] == 'l') {
			common.addAll(getLongest(l1, b, i, j-1));
		}else {
			common.addAll(getLongest(l1, b, i-1, j));
		}
		
		return common;
	}
	
	public int sumLen(List<String> input) {
		int sum = 0;
		if(input == null)
			return sum;
		
		for(String s: input) {
			sum += s.length();
		}
		return sum;
	}
	
	public static void main(String[] args) {
		AlgorithmsUtil ins = new AlgorithmsUtil();
		System.out.println(ins.loadReservedTemplateWords());
//		List<String> l1 = new ArrayList<String>();
//		List<String> l2 = new ArrayList<String>();
//		//  功能 从 驱动 中 读取 虚拟地址
//		l1.add("功能");
//		l1.add("从");
//		l1.add("驱动");
//		l1.add("中");
//		l1.add("读取");
//		l1.add("虚拟地址");
//		//  函数功能 从 驱动 中  读取
//		l2.add("函数功能");
//		l2.add("从");
//		l2.add("驱动");
//		l2.add("中");
//		l2.add("读取");
//		AlgorithmsUtil ins = new AlgorithmsUtil();
//		for(String s: ins.longestCommonString(l1, l2)){
//			System.out.print(s + "，");
//		}
//		System.out.println();
//		System.out.println(ins.editDistance(l1, l2));
	}
}
