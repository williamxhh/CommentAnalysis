package mysql.data.util;

import java.nio.charset.Charset;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class RunSql {
	public static void main(String[] args) {
		try{
			Class.forName("com.mysql.jdbc.Driver");

			String url = "jdbc:mysql://192.168.160.131:3306/pku_comment?user=root&password=123123";
			Connection conn = DriverManager.getConnection(url);
			Statement stmt = conn.createStatement();
			String sql = "select c.old_text as content from "
					+ "(revision as b inner join text as c on b.rev_text_id = c.old_id) "
					+ "inner join page as a on a.page_latest = b.rev_id where a.page_title='/mm/fremap.c(linux-3.5.4)'";
			String result = readBlob(stmt, sql);
			System.out.println(result.length());
			stmt.close();
			conn.close();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public static String readBlob(Statement stmt,String sql){
		StringBuilder sb = new StringBuilder();
		try {
			ResultSet rs = stmt
					.executeQuery(sql);
			//注释内容在数据库中是以Blob对象存的，取出来的时候，要把blob对象再转成文本
			while (rs.next()) {
				Blob blob = rs.getBlob(1);
				int len = (int) blob.length();
				byte[] data = blob.getBytes(1, len);
				sb.append(new String(data, Charset.forName("utf-8")));
			}
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return sb.toString();
	}
}
