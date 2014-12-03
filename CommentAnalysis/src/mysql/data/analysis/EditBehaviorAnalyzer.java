package mysql.data.analysis;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

import mysql.data.util.PropertiesUtil;


public class EditBehaviorAnalyzer {
	private static Logger logger = Logger.getLogger(EditBehaviorAnalyzer.class);
	private Properties props;
	private Connection source_conn;
	Map<String, String> comment_types = null;
	
	public EditBehaviorAnalyzer() {
		props = PropertiesUtil.getProperties();
		StringBuilder sourceurl = new StringBuilder();
		sourceurl.append("jdbc:mysql://")
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
			this.source_conn = DriverManager.getConnection(sourceurl.toString());
			
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void closeDBConnection(){
		try {
			if(this.source_conn!=null&&!this.source_conn.isClosed()){
				source_conn.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	
	public static void main(String[] args) {
		EditBehaviorAnalyzer eba = new EditBehaviorAnalyzer();
		try {
			eba.loadCommentsTypes();
			eba.loadCommentBehaviorTimeline();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		logger.info("EditBehaviorAnalyzer done");
	}
	
	public void loadCommentBehaviorTimeline() throws SQLException, FileNotFoundException {
		Statement stmt = source_conn.createStatement();
		String sql = "select p.page_title, r.rev_user_text, r.rev_timestamp, p.page_counter from page as p INNER JOIN revision as r on p.page_id = r.rev_page where p.page_namespace = 0 ORDER BY rev_timestamp ASC;";
		ResultSet rs = stmt.executeQuery(sql);
		
		PrintWriter writer = new PrintWriter(props.getProperty("mysql.data.DataSource.commentdb") + ".csv");
		while(rs.next()) {
			String path = rs.getString(1);
			String editor = rs.getString(2);
			String time = rs.getString(3);
			int view_count = rs.getInt(4);
			String type = comment_types.get(path);
			if(type != null){
				type = type.replaceAll(",", " ");
			}
			writer.write(path  + "," + type+ "," + editor + "," + view_count + "," +  time + "," + time.substring(0, 6) + "," + time.substring(0, 8) + "," + time.substring(8,10) + "," + time.substring(8) + "\r\n");
		}
		
		rs.close();
		writer.flush();
		writer.close();
		stmt.close();
	}
	
	private void loadCommentsTypes() {
		try {
			comment_types = new HashMap<String, String>();
			BufferedReader reader = new BufferedReader(new FileReader(PropertiesUtil.getProperties().getProperty("mysql.data.CommentClassifier.commentsAndTypes")));
			String oneline = "";
			while((oneline = reader.readLine()) != null) {
				String[] splits = oneline.split(",");
				if(splits.length == 2)
					comment_types.put(splits[0].trim(), splits[1].trim());
				else
					comment_types.put(splits[0].trim(), oneline.substring(splits[0].length() + 1));
			}
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
