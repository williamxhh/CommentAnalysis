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
			Statement stmt = ConnectionUtil.getCommentConnection().createStatement();
			String sql = "select c.old_text as content from "
					+ "(revision as b inner join text as c on b.rev_text_id = c.old_id) "
					+ "inner join page as a on a.page_latest = b.rev_id where a.page_title='/mm/fremap.c(linux-3.5.4)'";
			String result = readBlob(stmt, sql);
			System.out.println(result.length());
			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public static String readBlob(Statement stmt,String sql){
		StringBuilder sb = new StringBuilder();
		try {
			ResultSet rs = stmt
					.executeQuery(sql);
			//ע�����������ݿ�������Blob�����ģ�ȡ������ʱ��Ҫ��blob������ת���ı�
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
