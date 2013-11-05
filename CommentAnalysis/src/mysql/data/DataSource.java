package mysql.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import mysql.data.filter.CategoryTagFilter;
import mysql.data.filter.DoNothingFilter;
import mysql.data.filter.FilterBase;
import mysql.data.filter.HtmlFilter;

public class DataSource {
	static final String ROOTPATH = "D:/research_gxw/1_4comment/data/";
	//���Set���ڲ�֪��Ҫ�����ע���ж�����type��ʱ����ͳ���õģ���Ϊset���ᱣ���ظ�������
//	static Set<String> TYPESET = new HashSet<String>(); 
	
	// ���map�Ƿ����ʱ���ã��ж�����ע�����࣬����type����Ϊ����������ͬ��writer������ͬ���ļ�����д
	static Map<String,PrintWriter> WRITERMAP = new HashMap<String, PrintWriter>(); 
	
	public static void main(String[] args) {
		try {
			Class.forName("com.mysql.jdbc.Driver");

//			String url = "jdbc:mysql://192.168.160.131:3306/pku_comment?user=root&password=123123";
//			Connection conn = DriverManager.getConnection(url);
//			Statement stmt = conn.createStatement();

//			getModules(stmt);
//			loadData(stmt);
//			loadAllCommentInOneFile(stmt);
			
			//��ԭע���е�html��ǩ��   �Լ� [[Category:]]     [[File:]]�����Ķ��������
//			FilterBase filter = new CategoryTagFilter(new HtmlFilter(new DoNothingFilter()));
//			filterComment(filter);
			
			classifyComment();
			
//			for(String s:TYPESET){
//				System.out.println(s);
//			}
//			System.out.println(TYPESET.size());

//			stmt.close();
//			conn.close();
			System.out.println("done");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}
	
	@SuppressWarnings("resource")
	/**
	 * �ӹ��˴����Ժ��ע���ļ��ж�ȡ��Ȼ���жϱ�ע�Ͷ������𣬽���ͬ���ע�ͱ��浽��ͬ���ļ���
	 * @throws SQLException
	 */
	static void classifyComment() throws SQLException{
		//����ͬ����writer��ʼ��
		prepareWriter();
		
		//ͨ�����϶�ȡ����һ��ע�����ݣ�����һ����д��
		StringBuilder comment = new StringBuilder();
		try {
			BufferedReader reader = new BufferedReader(
					new FileReader(ROOTPATH+"/filteredComment.txt"));
			
			//�Ȱ����ж��������ж�һ����𣬳�ʼ���� currentWriter
			String line = reader.readLine();
			comment.append(line+"\r\n");
			//ȥ��ǰ��� ##**## �ٴ���
			PrintWriter currentWriter = WRITERMAP.get(getType(line.substring(6)));
			
			while ((line = reader.readLine()) != null) {
				//ÿ��ע�Ͷ���##**##��ͷ������һ�в�����##**##��ͷ�Ļ����Ǿͱ�ʾ����ǰһ��ͬ����һ��ע�ͣ�����ֱ����comment����׷�Ӽ���
				if(!line.startsWith("##**##")){
					if(line.trim().length()!=0)
						comment.append(line+"\r\n");
				}else{
				//�����ǰ����ע���Ѿ���ɣ���ô��������棬Ȼ������comment����ʼ������һ��ע��
					currentWriter.write(comment.toString()+"\r\n");
					currentWriter.flush();
					currentWriter = WRITERMAP.get(getType(line.substring(6)));
					comment = new StringBuilder();
					comment.append(line+"\r\n");
				}
				
			}
			reader.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//�ͷ����е�writer��Դ
		closeWriter();
	}
	
	/**
	 * ����ͬ����writer��ʼ��
	 */
	static void prepareWriter(){
		List<String> typeList = new ArrayList<String>();
		typeList.add("enumeration name");
		typeList.add("variable definition");
		typeList.add("extern or forward variable declaration");
		typeList.add("file");
		typeList.add("enumerator");
		typeList.add("macro (un)definition");
		typeList.add("union name");
		typeList.add("class, struct, or union member");
		typeList.add("function definition");
		typeList.add("function prototype or declaration");
		typeList.add("structure name");
		typeList.add("typedef");
		
		//ע������������android�Ĳ���ע�ͣ�Ϊ�˳���׳�ԣ��ؼ���������࣬��ʵ���ϲ���һ����ʵ������ķ���
		typeList.add("exception_android");
		
		for(String s:typeList){
			try {
				WRITERMAP.put(s, new PrintWriter(ROOTPATH+"CLASSIFIED/CLASSIFICATION_"+s+".txt"));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * �ͷ����е�writer��Դ
	 */
	static void closeWriter(){
		for(Map.Entry<String, PrintWriter> entry:WRITERMAP.entrySet()){
			entry.getValue().flush();
			entry.getValue().close();
		}
	}
	
	
	/**
	 * ����һ��ע�͵ı��⣬��ȡ���е��ļ��������������кŵ���Ϣ�����������������ע�͵�����
	 * 
	 * 
	 * @param line  һ�е���������������  /arch/arm/boot/compressed/atags_to_fdt.c/atags_to_fdt(0047)(linux-3.5.4)
	 * @return
	 * @throws SQLException
	 */
	static String getType(String line) throws SQLException{
//		PrintWriter writer = new PrintWriter("Filename_Symbolname_LineNo_Type.txt");
		String comm_filename = "";
		String comm_symname = "";
		int comm_linenoInFile = -1;
		String type = "";
		
		if (line.contains("(linux-3.5.4)")) {
			int pos = line.lastIndexOf("(linux-3.5.4)");
			if(line.charAt(pos-1)!=')'){
				comm_filename = line.substring(0,pos);
			}else{
				line = line.substring(0,pos);
				int pos_lineNo = line.lastIndexOf("(")+1;
				comm_linenoInFile = Integer.parseInt(line.substring(pos_lineNo,line.length()-1));
				int pos_slash = line.lastIndexOf("/");
				comm_filename = line.substring(0,pos_slash);
				comm_symname = line.substring(pos_slash+1,pos_lineNo-1);
			}
			
			if (!comm_symname.equals("") && comm_linenoInFile != -1) {
				type = getType(comm_filename, comm_symname, comm_linenoInFile);
			}else{
				type = "file";
			}
			
//			TYPESET.add(type);
//			writer.write(line + "\tFileName:" + comm_filename
//					+ "\tSymbol Name:" + comm_symname + "\tLineNo:"
//					+ comm_linenoInFile+"\tType:"+type+"\r\n");
		}else{
			type = "exception_android";
		}
//		writer.flush();
//		writer.close();
		return type;
	}
	
	/**
	 * ���ݴ�����ļ��������������кŵ�lxr���ݿ��в��type��Ϣ����
	 * @param filename
	 * @param symbolname
	 * @param lineNo
	 * @return
	 * @throws SQLException
	 */
	static String getType(String filename,String symbolname,int lineNo) throws SQLException{
		String type = "";
		String url = "jdbc:mysql://192.168.160.131:3306/lxr?user=root&password=123123";
		Connection conn = DriverManager.getConnection(url);
		Statement stmt = conn.createStatement();
		
		ResultSet rs = stmt
				.executeQuery("select d.declaration from lxr_declarations AS d WHERE d.declid = (SELECT i.type from lxr_indexes AS i WHERE i.symid = (SELECT s.symid from lxr_symbols AS s WHERE s.symname='"
						+ symbolname
						+ "') and i.fileid = (SELECT f.fileid from lxr_files AS f WHERE f.filename='"
						+ filename
						+ "') and i.line = "
						+ lineNo + ");");
		rs.next();
		type = rs.getString(1);
		rs.close();
		
		
		stmt.close();
		conn.close();
		
		return type;
	}
	
	/**
	 * ʹ�ô���Ĺ�������ԭʼ��ע����Ϣ���й��ˣ���allComments.txt���룬д����filteredComment.txt
	 * @param filter
	 */
	static void filterComment(FilterBase filter){
		try {
			BufferedReader reader = new BufferedReader(new FileReader(ROOTPATH+"/allComments.txt"));
			PrintWriter writer = new PrintWriter(ROOTPATH+"/filteredComment.txt");
			StringBuilder comment = new StringBuilder();
			comment.append(reader.readLine()+"\r\n");
			String line = "";
			while((line= reader.readLine())!=null){
				//ÿ��ע�Ͷ���##**##��ͷ������һ�в�����##**##��ͷ�Ļ����Ǿͱ�ʾ����ǰһ��ͬ����һ��ע�ͣ�����ֱ����comment����׷�Ӽ���
				if(!line.startsWith("##**##")){
					if(line.trim().length()!=0)
						comment.append(line+"\r\n");
				}else{
				//�����ǰ����ע���Ѿ���ɣ���ô��������棬Ȼ������comment����ʼ������һ��ע��
					writer.write(filter.getText(comment.toString()));
					writer.flush();
					comment = new StringBuilder();
					comment.append(line+"\r\n");
				}
			}
			
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
	
	
	static void loadAllCommentInOneFile(Statement stmt){
		PrintWriter writer = null;
		try {
			BufferedReader reader = new BufferedReader(new FileReader("path.txt"));
			String line = "";
			
			File file = new File(ROOTPATH+"/allComments.txt");
			File parent = file.getParentFile();
			if(parent!=null&&!parent.exists()){
				parent.mkdirs();
			}
			file.createNewFile();
			
			writer = new PrintWriter(file);
			while((line = reader.readLine())!=null){
				
				writer.write("##**##"+line+"<br/><br/>\r\n\r\n"+getComment(stmt, line)+"<br/><br/>\r\n\r\n");
			}
			writer.flush();
			writer.close();
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	static void loadData(Statement stmt){
		PrintWriter writer = null;
		try {
			BufferedReader reader = new BufferedReader(new FileReader("path.txt"));
			String line = "";
			while((line = reader.readLine())!=null){
				File file = new File(ROOTPATH+line+".txt");
				File parent = file.getParentFile();
				if(parent!=null&&!parent.exists()){
					parent.mkdirs();
				}
				file.createNewFile();
				
				writer = new PrintWriter(file);
				writer.write(getComment(stmt, line));
				writer.flush();
			}
			writer.close();
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	static void getModules(Statement stmt) {
		ResultSet rs;
		try {
			rs = stmt
					.executeQuery("select distinct(a.page_title) from page as a inner join revision as b "
							+ "on a.page_id=b.rev_page where a.page_namespace=0 and b.rev_user in "
							+ "(select ug_user from user_groups where ug_group = 'analyst');");

			PrintWriter writer = new PrintWriter(new FileWriter("path.txt"));

			while (rs.next()) {
				writer.print(rs.getString(1) + "\r\n");
			}
			writer.flush();

			rs.close();
			writer.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	static String getComment(Statement stmt,String pageTitle) {
		StringBuilder sb = new StringBuilder();
		try {
			ResultSet rs = stmt
					.executeQuery("select c.old_text as content from "
							+ "(revision as b inner join text as c on b.rev_text_id = c.old_id) "
							+ "inner join page as a on a.page_latest = b.rev_id where a.page_title='"+pageTitle+"'");
			while (rs.next()) {
				Blob blob = rs.getBlob(1);
				int len = (int) blob.length();
				byte[] data = blob.getBytes(1, len);
				sb.append(new String(data, Charset.forName("utf-8"))+"\r\n\r\n\r\n");
			}
//			ResultSet rs = stmt.executeQuery("select count(*) from page where page_title='"+pageTitle+"'");
//			while(rs.next()){
//				int count = rs.getInt(1);
//				assert count==1:"�ж���1�����";
//			}
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return sb.toString();
	}
}
