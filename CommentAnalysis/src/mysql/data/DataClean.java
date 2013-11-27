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

public class DataClean {
	public static void main(String[] args) {
		//��ԭע���е�html��ǩ��   �Լ� [[Category:]]     [[File:]]�����Ķ��������
		FilterBase filter = new CategoryTagFilter(new HtmlFilter(new DoNothingFilter()));
		filterComment(filter);
		System.out.println("done");
	}
	
	/**
	 * ʹ�ô���Ĺ�������ԭʼ��ע����Ϣ���й��ˣ���allComments.txt���룬д����filteredComment.txt
	 * @param filter
	 */
	static void filterComment(FilterBase filter){
		try {
			BufferedReader reader = new BufferedReader(new FileReader(DataSource.ROOTPATH+"/allComments.txt"));
			PrintWriter writer = new PrintWriter(DataSource.ROOTPATH+"/filteredComment.txt");
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
