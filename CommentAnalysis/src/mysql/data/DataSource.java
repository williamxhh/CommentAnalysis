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

public class DataSource {
	static final String ROOTPATH = "D:/research_gxw/1_4comment/data/";

	public static void main(String[] args) {
		try {
			Class.forName("com.mysql.jdbc.Driver");

			String url = "jdbc:mysql://192.168.160.131:3306/pku_comment?user=root&password=123123";
			Connection conn = DriverManager.getConnection(url);
			Statement stmt = conn.createStatement();

//			getModules(stmt);
//			loadData(stmt);
//			loadAllCommentInOneFile(stmt);
			
			stmt.close();
			conn.close();
			System.out.println("done");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}
	
	
	/**
	 * 将数据库中的源代码注释取出,存入到一个文件allComments.txt中
	 * @param stmt
	 */
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
	
	/**
	 * 将数据库中的源代码注释取出，按照源代码的路径放到相应的文件中
	 * @param stmt
	 */
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

	/**
	 * 得到所有已有analyst写的文件和标识符注释 的 源代码路径，写入到文件path中
	 * 文件和标识符注释内容的page_namespace=0。而模块注释的page_namespace=14
	 * @param stmt
	 */
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

	
	/**
	 * 获取指定 源代码路径的注释内容
	 * @param stmt
	 * @param pageTitle   要获取注释内容的源代码路径
	 * @return
	 */
	static String getComment(Statement stmt,String pageTitle) {
		StringBuilder sb = new StringBuilder();
		try {
			ResultSet rs = stmt
					.executeQuery("select c.old_text as content from "
							+ "(revision as b inner join text as c on b.rev_text_id = c.old_id) "
							+ "inner join page as a on a.page_latest = b.rev_id where a.page_title='"+pageTitle+"'");
			//注释内容在数据库中是以Blob对象存的，取出来的时候，要把blob对象再转成文本
			while (rs.next()) {
				Blob blob = rs.getBlob(1);
				int len = (int) blob.length();
				byte[] data = blob.getBytes(1, len);
				sb.append(new String(data, Charset.forName("utf-8"))+"\r\n\r\n\r\n");
			}
//			ResultSet rs = stmt.executeQuery("select count(*) from page where page_title='"+pageTitle+"'");
//			while(rs.next()){
//				int count = rs.getInt(1);
//				assert count==1:"有多于1的情况";
//			}
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return sb.toString();
	}
}
