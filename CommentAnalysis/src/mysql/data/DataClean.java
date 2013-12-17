package mysql.data;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Properties;

import mysql.data.filter.CategoryTagFilter;
import mysql.data.filter.DoNothingFilter;
import mysql.data.filter.FilterBase;
import mysql.data.filter.HtmlFilter;
import mysql.data.util.PropertiesUtil;

public class DataClean {
	static Properties props ;
	
	
	public static void main(String[] args) {
		try {
			filterComment();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("DataClean done");
	}
	
	static void filterComment() throws IOException{
		//将原注释中的html标签，   以及 [[Category:]]     [[File:]]这样的东西处理掉
		FilterBase filter = new CategoryTagFilter(new HtmlFilter(new DoNothingFilter()));
		filterComment(filter);
	}
	
	/**
	 * 使用传入的过滤器对原始的注释信息进行过滤，从allComments.txt读入，写出到filteredComment.txt
	 * @param filter
	 * @throws IOException 
	 */
	static void filterComment(FilterBase filter) throws IOException{
		props = PropertiesUtil.getProperties();
		String rootPath = props.getProperty("mysql.data.DataSource.rootPath","commentData/");
		String allCommentsFile = props.getProperty("mysql.data.DataSource.allCommentsFile","allComments.txt");
		String filteredCommentsFile = props.getProperty("mysql.data.DataClean.filteredCommentsFile","filteredComment.txt");
		try {
//			BufferedReader reader = new BufferedReader(new FileReader(rootPath+"/allComments.txt"));
//			PrintWriter writer = new PrintWriter(rootPath+"/filteredComment.txt");
			BufferedReader reader = new BufferedReader(new FileReader(rootPath+"/"+allCommentsFile));
			PrintWriter writer = new PrintWriter(rootPath+"/"+filteredCommentsFile);
			StringBuilder comment = new StringBuilder();
			comment.append(reader.readLine()+"\r\n");
			String line = "";
			while((line= reader.readLine())!=null){
				//每条注释都用##**##开头，所以一行不是以##**##开头的话，那就表示它与前一行同属于一条注释，所以直接在comment里面追加即可
				if(!line.startsWith("##**##")){
					//这里把完全空白的行也过滤掉了
					if(line.trim().length()!=0)
						comment.append(line+"\r\n");
				}else{
				//如果当前这条注释已经完成，那么就使用过滤器过滤一下，然后输出保存，然后重置comment，开始保存下一条注释
					writer.write(filter.getText(comment.toString())+"\r\n\r\n");
					writer.flush();
					comment = new StringBuilder();
					comment.append(line+"\r\n");
				}
			}
			
			//保存最后一条注释内容
			writer.write(filter.getText(comment.toString()));
			writer.flush();
			
			reader.close();
			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
