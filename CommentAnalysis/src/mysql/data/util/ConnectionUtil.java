package mysql.data.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class ConnectionUtil {
	private static Connection JUDGE_CONN;
	
	public static Connection getJudgeConnection() {
		if(JUDGE_CONN == null) {
			Properties props = PropertiesUtil.getProperties();
			StringBuilder url = new StringBuilder();
			url.append("jdbc:mysql://")
					.append(props.getProperty("mysql.data.DataSource.dbserver.ip",
							"192.168.160.131"))
					.append(":")
					.append(props.getProperty(
							"mysql.data.DataSource.dbserver.port", "3306"))
					.append("/")
					.append(props.getProperty(
							"mysql.data.gui.EvaluationPreparation.judgedb",
							"pkuJudge"))
					.append("?user=")
					.append(props.getProperty(
							"mysql.data.DataSource.dbserver.user", "root"))
					.append("&password=")
					.append(props.getProperty(
							"mysql.data.DataSource.dbserver.pass", "123123"));

			try {
				Class.forName("com.mysql.jdbc.Driver");
				JUDGE_CONN = DriverManager.getConnection(url.toString());

			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return JUDGE_CONN;
	}
}
