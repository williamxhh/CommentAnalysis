package mysql.data.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mysql.data.CommentClassifier;
import mysql.data.analysis.TXTCommentAnalyzer;

import org.apache.log4j.Logger;

public class PathAnalyzer {
	static Logger logger = Logger.getLogger(PathAnalyzer.class);
	public static void main(String[] args) throws IOException, SQLException {
		PathAnalyzer pa = new PathAnalyzer();
		//寻找两家单位的公共注释path
////		List<String> commonPath = pa.findCommonCommentedPath("path_012pku.txt", "path_013qinghua.txt");
////		String outputFile = "012_pku&013qinghua.txt";
////		List<String> commonPath = pa.findCommonCommentedPath("path_012pku.txt", "path_023iscas.txt");
////		String outputFile = "012_pku&023iscas.txt";
////		List<String> commonPath = pa.findCommonCommentedPath("path_023iscas.txt", "path_013qinghua.txt");
////		String outputFile = "013qinghua&023iscas.txt";
//		List<String> commonPath = pa.findCommonCommentedPath("path_023iscas.txt", "012_pku&013qinghua.txt");
//		String outputFile = "pku_qinghua_iscas.txt";
//		logger.info(commonPath.size());
//		PrintWriter writer = new PrintWriter(new FileWriter(outputFile));
//		for(String path:commonPath){
//			writer.write(path+"\r\n");
//		}
//		writer.flush();
//		writer.close();
		
		//查看三家单位的公共注释path的注释内容
		String spliter = PropertiesUtil.getProperty("mysql.data.analysis.TXTCommentAnalyzer.commentSpliter");
		Map<String, String> pkuComment = TXTCommentAnalyzer.readContentToMap(FileUtil.readableFile("commentData_012pku/filteredComment_012pku.txt"), spliter);
		Map<String, String> qinghuaComment = TXTCommentAnalyzer.readContentToMap(FileUtil.readableFile("commentData_013qinghua/filteredComment_013qinghua.txt"), spliter);
		Map<String, String> iscasComment = TXTCommentAnalyzer.readContentToMap(FileUtil.readableFile("commentData_023iscas/filteredComment_023iscas.txt"), spliter);
		logger.info(pa.getCommonComment(pkuComment, qinghuaComment, iscasComment, "/arch/x86/mm/fault.c/do_page_fault(1004)(linux-3.5.4)"));
		
		//查看注释路径中，不同类型注释的统计情况，按类型统计
//		String commonPathFile = "path_023iscas.txt";
//		List<String> paths = pa.readPathFile(commonPathFile);
//		Map<String,Integer> typeCount = new HashMap<String,Integer>();
//		CommentClassifier cc = new CommentClassifier();
//		for(String p:paths){
//			String type = cc.getType(p);
//			if(typeCount.containsKey(type)){
//				typeCount.put(type, typeCount.get(type)+1);
//			}else{
//				typeCount.put(type, 1);
//			}
//		}
//		int total = 0;
//		for(Map.Entry<String, Integer> entry:typeCount.entrySet()){
//			logger.info(entry.getKey()+":"+entry.getValue());
//			total+=entry.getValue();
//		}
//		logger.info("\ttotal:"+total);
		
		
	}
	
	public List<String> findCommonCommentedPath(String path1,String path2) throws IOException{
		List<String> containedPath1 = readPathFile(path1);
		List<String> containedPath2 = readPathFile(path2);
		List<String> commonPath = new ArrayList<String>();
		for(String p1:containedPath1){
			if(containedPath2.contains(p1)){
				commonPath.add(p1);
			}
		}
		return commonPath;
	}
	
	public List<String> readPathFile(String path) throws IOException{
		File pathfile1 = FileUtil.readableFile(path);
		List<String> containedPaths = new ArrayList<String>();
		BufferedReader reader = new BufferedReader(new FileReader(pathfile1));
		String line = "";
		while((line=reader.readLine())!=null){
			containedPaths.add(line);
		}
		reader.close();
		return containedPaths;
	}
	
	public String getCommonComment(Map<String,String> pkuComment,Map<String,String> qinghuaComment,Map<String,String> iscasComment,String path){
		StringBuilder comment = new StringBuilder();
		comment.append(path+"\r\n");
		comment.append("###pku###"+"\r\n");
		comment.append(pkuComment.get(path)+"\r\n");
		comment.append("###qinghua###"+"\r\n");
		comment.append(qinghuaComment.get(path)+"\r\n");
		comment.append("###iscas###"+"\r\n");
		comment.append(iscasComment.get(path)+"\r\n");
		
		return comment.toString();
	}
}
