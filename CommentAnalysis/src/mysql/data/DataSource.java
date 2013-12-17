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
import java.util.Properties;

import org.apache.log4j.Logger;

import mysql.data.util.PropertiesUtil;

public class DataSource {
	private static final Logger log = Logger.getLogger(DataSource.class);
	private Properties props = new Properties();
	private Connection conn;
	private String rootPath;
	//包含所有已注释的函数，宏，变量的路径的文件
	private String pathFile;
	//保存所有注释内容的文件
	private String allCommentsFile;

	public static void main(String[] args) {
		try {
			DataSource ds = new DataSource();
			log.info("call ds.loadCommentToFile()");
			ds.loadCommentToFile();
			ds.closeDBConnection();
			System.out.println("DataSource done");
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}
	
	public DataSource(){
		props = PropertiesUtil.getProperties();
		StringBuilder url = new StringBuilder();
		url.append("jdbc:mysql://")
			.append(props.getProperty("mysql.data.DataSource.dbserver.ip","192.168.160.131"))
			.append(":")
			.append(props.getProperty("mysql.data.DataSource.dbserver.port","3306"))
			.append("/")
			.append(props.getProperty("mysql.data.DataSource.commentdb","pku_comment"))
			.append("?user=")
			.append(props.getProperty("mysql.data.DataSource.dbserver.user","root"))
			.append("&password=")
			.append(props.getProperty("mysql.data.DataSource.dbserver.pass","123123"));
		
		try {
			Class.forName("com.mysql.jdbc.Driver");
			this.conn = DriverManager.getConnection(url.toString());
			
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		rootPath = props.getProperty("mysql.data.DataSource.rootPath","commentData/");
		pathFile = props.getProperty("mysql.data.DataSource.pathFile","path.txt");
		allCommentsFile = props.getProperty("mysql.data.DataSource.allCommentsFile","allComments.txt");
		
	}
	
	public Connection getConn() {
		return conn;
	}

	public void closeDBConnection(){
		try {
			if(this.conn!=null&&this.conn.isClosed()){
				conn.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void loadCommentToFile() throws SQLException{
		File allModulePath = new File(pathFile);
		if(!allModulePath.exists()){
			log.info("call getModules()");
			getModules();
		}
		log.info("call loadAllCommentInOneFile()");
		loadAllCommentInOneFile();
	}
	
	/**
	 * 将数据库中的源代码注释取出,存入到一个文件allComments.txt中
	 * @throws SQLException 
	 */
	private void loadAllCommentInOneFile() throws SQLException{
		Statement stmt = conn.createStatement();
		PrintWriter writer = null;
		try {
			BufferedReader reader = new BufferedReader(new FileReader(pathFile));
			String line = "";
			
			File file = new File(rootPath+"/"+allCommentsFile);
			File parent = file.getParentFile();
			if(parent!=null&&!parent.exists()){
				parent.mkdirs();
			}
			file.createNewFile();
			
			log.info("create file "+rootPath+"/"+allCommentsFile);
			
			writer = new PrintWriter(file);
			while((line = reader.readLine())!=null){
				log.info("call getComment() "+line);
				writer.write("##**##"+line+"\r\n\r\n"+getComment(stmt, line)+"\r\n\r\n");
			}
			writer.flush();
			writer.close();
			reader.close();
			stmt.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
//	/**
//	 * 将数据库中的源代码注释取出，按照源代码的路径放到相应的文件中
//	 * @param stmt
//	 * @throws SQLException 
//	 */
//	private void loadData() throws SQLException{
//		Statement stmt = conn.createStatement();
//		PrintWriter writer = null;
//		try {
//			BufferedReader reader = new BufferedReader(new FileReader("path.txt"));
//			String line = "";
//			while((line = reader.readLine())!=null){
//				File file = new File(rootPath+line+".txt");
//				File parent = file.getParentFile();
//				if(parent!=null&&!parent.exists()){
//					parent.mkdirs();
//				}
//				file.createNewFile();
//				
//				writer = new PrintWriter(file);
//				writer.write(getComment(stmt, line));
//				writer.flush();
//			}
//			writer.close();
//			reader.close();
//			stmt.close();
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}

	/**
	 * 得到所有已有analyst写的文件和标识符注释 的 源代码路径，写入到文件path中
	 * 文件和标识符注释内容的page_namespace=0。而模块注释的page_namespace=14
	 * @throws SQLException 
	 */
	private void getModules() throws SQLException {
		Statement stmt = conn.createStatement();
		ResultSet rs;
		try {
			rs = stmt
					.executeQuery("select distinct(a.page_title) from page as a inner join revision as b "
							+ "on a.page_id=b.rev_page where a.page_namespace=0 and b.rev_user in "
							+ "(select ug_user from user_groups where ug_group = 'analyst');");

			PrintWriter writer = new PrintWriter(new FileWriter(pathFile));

			while (rs.next()) {
				if(rs.getString(1).startsWith("/")){
					writer.print(rs.getString(1) + "\r\n");
					log.info("#write# "+rs.getString(1));
				}
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
	 * @param pageTitle   要获取注释内容的源代码路径
	 * @return
	 * @throws SQLException 
	 */
	private String getComment(Statement stmt,String pageTitle) throws SQLException {
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
