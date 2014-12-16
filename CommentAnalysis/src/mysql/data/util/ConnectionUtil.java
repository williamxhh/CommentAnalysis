package mysql.data.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class ConnectionUtil {
	private static Connection JUDGE_CONN;
	private static Connection COMMENT_CONN;
	private static Connection COMMENT_ANA_CONN;
	private static Connection LXR_CONN;

	public static Connection getJudgeConnection() {
		if (JUDGE_CONN == null) {
			Properties props = PropertiesUtil.getProperties();
			StringBuilder url = new StringBuilder();
			url.append("jdbc:mysql://")
					.append(props.getProperty(
							"mysql.data.DataSource.dbserver.ip",
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

	public static Connection getCommentConnection() {
		if (COMMENT_CONN == null) {
			Properties props = PropertiesUtil.getProperties();
			StringBuilder url = new StringBuilder();
			url.append("jdbc:mysql://")
					.append(props.getProperty(
							"mysql.data.DataSource.dbserver.ip",
							"192.168.160.131"))
					.append(":")
					.append(props.getProperty(
							"mysql.data.DataSource.dbserver.port", "3306"))
					.append("/")
					.append(props.getProperty(
							"mysql.data.DataSource.commentdb", "pku_comment"))
					.append("?user=")
					.append(props.getProperty(
							"mysql.data.DataSource.dbserver.user", "root"))
					.append("&password=")
					.append(props.getProperty(
							"mysql.data.DataSource.dbserver.pass", "123123"));

			try {
				Class.forName("com.mysql.jdbc.Driver");
				COMMENT_CONN = DriverManager.getConnection(url.toString());

			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return COMMENT_CONN;
	}

	public static Connection getCommentAnalysisConnection() {
		if (COMMENT_ANA_CONN == null) {
			Properties props = PropertiesUtil.getProperties();
			StringBuilder url = new StringBuilder();
			url.append("jdbc:mysql://")
					.append(props.getProperty(
							"mysql.data.DataSource.dbserver.ip",
							"192.168.160.131"))
					.append(":")
					.append(props.getProperty(
							"mysql.data.DataSource.dbserver.port", "3306"))
					.append("/")
					.append(props
							.getProperty(
									"mysql.data.analysis.CommentAnalyzer.commentAnalysisdb",
									"012pku_analysis"))
					.append("?user=")
					.append(props.getProperty(
							"mysql.data.DataSource.dbserver.user", "root"))
					.append("&password=")
					.append(props.getProperty(
							"mysql.data.DataSource.dbserver.pass", "123123"));

			try {
				Class.forName("com.mysql.jdbc.Driver");
				COMMENT_ANA_CONN = DriverManager.getConnection(url.toString());

			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return COMMENT_ANA_CONN;
	}

	public static Connection getLxrConnection() {
		if (LXR_CONN == null) {
			Properties props = PropertiesUtil.getProperties();
			StringBuilder url = new StringBuilder();
			url.append("jdbc:mysql://")
					.append(props.getProperty(
							"mysql.data.DataSource.dbserver.ip",
							"192.168.160.131"))
					.append(":")
					.append(props.getProperty(
							"mysql.data.DataSource.dbserver.port", "3306"))
					.append("/")
					.append(props.getProperty(
							"mysql.data.CommentClassifier.lxrdb", "lxr"))
					.append("?user=")
					.append(props.getProperty(
							"mysql.data.DataSource.dbserver.user", "root"))
					.append("&password=")
					.append(props.getProperty(
							"mysql.data.DataSource.dbserver.pass", "123123"));
			try {
				Class.forName("com.mysql.jdbc.Driver");
				LXR_CONN = DriverManager.getConnection(url.toString());

			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return LXR_CONN;
	}
}
