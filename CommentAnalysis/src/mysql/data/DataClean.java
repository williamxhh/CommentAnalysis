package mysql.data;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

import mysql.data.filter.CategoryTagFilter;
import mysql.data.filter.DoNothingFilter;
import mysql.data.filter.FilterBase;
import mysql.data.filter.HtmlFilter;
import mysql.data.util.PropertiesUtil;

public class DataClean {
	
	public static void main(String[] args) {
		try {
			filterComment();
		} catch (IOException e) {
			e.printStackTrace();	
		}
		System.out.println("DataClean done");
	}
	
	static void filterComment() throws IOException{
		//��ԭע���е�html��ǩ��   �Լ� [[Category:]]     [[File:]]�����Ķ��������
		FilterBase filter = new CategoryTagFilter(new HtmlFilter(new DoNothingFilter()));
		filterComment(filter);
	}
	
	/**
	 * ʹ�ô���Ĺ�������ԭʼ��ע����Ϣ���й��ˣ���allComments.txt���룬д����filteredComment.txt
	 * @param filter
	 * @throws IOException 
	 */
	static void filterComment(FilterBase filter) throws IOException{
		String rootPath = PropertiesUtil.getProperty("mysql.data.DataSource.rootPath","commentData/");
		String allCommentsFile = PropertiesUtil.getProperty("mysql.data.DataSource.allCommentsFile","allComments.txt");
		String filteredCommentsFile = PropertiesUtil.getProperty("mysql.data.DataClean.filteredCommentsFile","filteredComment.txt");
		try {
//			BufferedReader reader = new BufferedReader(new FileReader(rootPath+"/allComments.txt"));
//			PrintWriter writer = new PrintWriter(rootPath+"/filteredComment.txt");
			BufferedReader reader = new BufferedReader(new FileReader(rootPath+"/"+allCommentsFile));
			PrintWriter writer = new PrintWriter(rootPath+"/"+filteredCommentsFile);
			StringBuilder comment = new StringBuilder();
			comment.append(reader.readLine()+"\r\n");
			String line = "";
			while((line= reader.readLine())!=null){
				//ÿ��ע�Ͷ���##**##��ͷ������һ�в�����##**##��ͷ�Ļ����Ǿͱ�ʾ����ǰһ��ͬ����һ��ע�ͣ�����ֱ����comment����׷�Ӽ���
				if(!line.startsWith("##**##")){
					//�������ȫ�հ׵���Ҳ���˵���
					if(line.trim().length()!=0)
						comment.append(line+"\r\n");
				}else{
				//�����ǰ����ע���Ѿ���ɣ���ô��ʹ�ù���������һ�£�Ȼ��������棬Ȼ������comment����ʼ������һ��ע��
					writer.write(filter.getText(comment.toString())+"\r\n\r\n");
					writer.flush();
					comment = new StringBuilder();
					comment.append(line+"\r\n");
				}
			}
			
			//�������һ��ע������
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
