package mysql.data.analysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import mysql.data.util.FileUtil;
import mysql.data.util.LxrType;

public class TXTCommentAnalyzer {
	private static final String DEFAULTPATH = "D:\\research_gxw\\1_4comment\\data\\CLASSIFIED";
	private static final String DEFAULTSPLITER = "##**##";
	private int lxr_type;
	private String txtCommentFilePath;
	private String commentSpliter;
	private Map<String,String> comments;
	private Set<String> fileset;
	
	public TXTCommentAnalyzer(int type){
		this(type,DEFAULTPATH,DEFAULTSPLITER);
	}

	public TXTCommentAnalyzer(int type,String path,String spliter){
		this.lxr_type = type;
		this.txtCommentFilePath = path;
		this.commentSpliter = spliter;
		readContentToMap();
	}
	
	private void readContentToMap(){
		this.comments = new TreeMap<String, String>();
			
		try {
			File file = FileUtil.readableFile(this.txtCommentFilePath+"\\CLASSIFICATION_"+LxrType.getTypeName(lxr_type)+".txt");
			BufferedReader reader = new BufferedReader(
					new FileReader(file));
			String line = "";
			String key = "";
			StringBuilder content = new StringBuilder();
			while ((line = reader.readLine()) != null) {
				//如果以特定的分隔符开头，就表示是一条新的注释
				if(line.startsWith(this.commentSpliter)){
					//如果当前key和content中都有内容，就向map中写入
					if(!key.equals("")){
						comments.put(key, content.toString());
						key = "";
						content = new StringBuilder();
					}
					key = line.substring(this.commentSpliter.length());
				}else{
					content.append(line);
				}
			}
			//将最后一条注释放入map
			comments.put(key, content.toString());
			
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		fileset = new HashSet<String>();
		for (Map.Entry<String, String> entry : this.comments.entrySet()) {
			String key = entry.getKey();
			fileset.add(getCommentFileName(key));
		}
	}
	
	public int getCommentsNumber(){
		return this.comments.size();
	}
	
	public int getFileNumber(){
		if (fileset == null) {
			fileset = new HashSet<String>();
			for (Map.Entry<String, String> entry : this.comments.entrySet()) {
				String key = entry.getKey();
				fileset.add(getCommentFileName(key));
			}
		}
		return this.fileset.size();
	}
	
	public List<String> getFileComments(String filename){
		List<String> c = new ArrayList<String>();
		if(fileset.contains(filename)){
			for (Map.Entry<String, String> entry : this.comments.entrySet()) {
				if(entry.getKey().startsWith(filename)){
					c.add(entry.getValue());
				}
			}
		}
		return c;
	}
	
	private String getCommentFileName(String line){
		if(this.lxr_type!=LxrType.file){
			return line.substring(0, line.lastIndexOf("/"));
		}
		return line;
	}
	
	public static void main(String[] args) {
		TXTCommentAnalyzer a = new TXTCommentAnalyzer(LxrType.function_definition);
		System.out.println(a.getCommentsNumber());
		System.out.println(a.getFileNumber());
		
		for(String c:a.getFileComments("/arch/arm/boot/compressed/string.c")){
			System.out.println(c);
		}
		
//		for(String file:a.fileset){
//			System.out.println(file+":"+a.getFileComments(file).size());
//		}
	}
	
}
