package mysql.data.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class ConnectionUtil {
	private static boolean USE_LOCAL = true;
	private static Connection JUDGE_CONN;
	private static Connection COMMENT_CONN;
	private static Connection COMMENT_ANA_CONN;
	private static Connection LXR_CONN;

	public static Connection getJudgeConnection() {
		if (JUDGE_CONN == null) {
			StringBuilder url = new StringBuilder();
			if (USE_LOCAL) {
				url.append("jdbc:mysql://")
						.append("localhost")
						.append(":")
						.append(PropertiesUtil.getProperty(
								"mysql.data.DataSource.dbserver.port", "3306"))
						.append("/")
						.append(PropertiesUtil
								.getProperty(
										"mysql.data.analysis.CommentAnalyzer.commentAnalysisdb",
										"012pku_analysis"))
						.append("?user=")
						.append(PropertiesUtil.getProperty(
								"mysql.data.DataSource.dbserver.user", "root"))
						.append("&password=")
						.append(PropertiesUtil
								.getProperty(
										"mysql.data.DataSource.dbserver.pass",
										"123123"));
			} else {
				url.append("jdbc:mysql://")
						.append(PropertiesUtil.getProperty(
								"mysql.data.DataSource.dbserver.ip",
								"192.168.160.131"))
						.append(":")
						.append(PropertiesUtil.getProperty(
								"mysql.data.DataSource.dbserver.port", "3306"))
						.append("/")
						.append(PropertiesUtil.getProperty(
								"mysql.data.gui.EvaluationPreparation.judgedb",
								"pkuJudge"))
						.append("?user=")
						.append(PropertiesUtil.getProperty(
								"mysql.data.DataSource.dbserver.user", "root"))
						.append("&password=")
						.append(PropertiesUtil
								.getProperty(
										"mysql.data.DataSource.dbserver.pass",
										"123123"));
			}

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
			StringBuilder url = new StringBuilder();
			if(USE_LOCAL) {
				url.append("jdbc:mysql://")
				.append("localhost")
				.append(":")
				.append(PropertiesUtil.getProperty(
						"mysql.data.DataSource.dbserver.port", "3306"))
				.append("/")
				.append(PropertiesUtil.getProperty(
						"mysql.data.DataSource.commentdb", "pku_comment"))
				.append("?user=")
				.append(PropertiesUtil.getProperty(
						"mysql.data.DataSource.dbserver.user", "root"))
				.append("&password=")
				.append(PropertiesUtil.getProperty(
						"mysql.data.DataSource.dbserver.pass", "123123"));
			} else {
				url.append("jdbc:mysql://")
				.append(PropertiesUtil.getProperty(
						"mysql.data.DataSource.dbserver.ip",
						"192.168.160.131"))
				.append(":")
				.append(PropertiesUtil.getProperty(
						"mysql.data.DataSource.dbserver.port", "3306"))
				.append("/")
				.append(PropertiesUtil.getProperty(
						"mysql.data.DataSource.commentdb", "pku_comment"))
				.append("?user=")
				.append(PropertiesUtil.getProperty(
						"mysql.data.DataSource.dbserver.user", "root"))
				.append("&password=")
				.append(PropertiesUtil.getProperty(
						"mysql.data.DataSource.dbserver.pass", "123123"));
			}

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
			StringBuilder url = new StringBuilder();
			if(USE_LOCAL) {
				url.append("jdbc:mysql://")
				.append("localhost")
				.append(":")
				.append(PropertiesUtil.getProperty(
						"mysql.data.DataSource.dbserver.port", "3306"))
				.append("/")
				.append(PropertiesUtil
						.getProperty(
								"mysql.data.analysis.CommentAnalyzer.commentAnalysisdb",
								"012pku_analysis"))
				.append("?user=")
				.append(PropertiesUtil.getProperty(
						"mysql.data.DataSource.dbserver.user", "root"))
				.append("&password=")
				.append(PropertiesUtil.getProperty(
						"mysql.data.DataSource.dbserver.pass", "123123"));
			} else {
				url.append("jdbc:mysql://")
				 .append(PropertiesUtil.getProperty(
				 "mysql.data.DataSource.dbserver.ip",
				 "192.168.160.131"))
				.append(":")
				.append(PropertiesUtil.getProperty(
						"mysql.data.DataSource.dbserver.port", "3306"))
				.append("/")
				.append(PropertiesUtil
						.getProperty(
								"mysql.data.analysis.CommentAnalyzer.commentAnalysisdb",
								"012pku_analysis"))
				.append("?user=")
				.append(PropertiesUtil.getProperty(
						"mysql.data.DataSource.dbserver.user", "root"))
				.append("&password=")
				.append(PropertiesUtil.getProperty(
						"mysql.data.DataSource.dbserver.pass", "123123"));
			}
			

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
			StringBuilder url = new StringBuilder();
			if(USE_LOCAL) {
				url.append("jdbc:mysql://")
				.append("localhost")
				.append(":")
				.append(PropertiesUtil.getProperty(
						"mysql.data.DataSource.dbserver.port", "3306"))
				.append("/")
				.append(PropertiesUtil.getProperty(
						"mysql.data.CommentClassifier.lxrdb", "lxr"))
				.append("?user=")
				.append(PropertiesUtil.getProperty(
						"mysql.data.DataSource.dbserver.user", "root"))
				.append("&password=")
				.append(PropertiesUtil.getProperty(
						"mysql.data.DataSource.dbserver.pass", "123123"));
			} else {
				url.append("jdbc:mysql://")
				.append(PropertiesUtil.getProperty(
						"mysql.data.DataSource.dbserver.ip",
						"192.168.160.131"))
				.append(":")
				.append(PropertiesUtil.getProperty(
						"mysql.data.DataSource.dbserver.port", "3306"))
				.append("/")
				.append(PropertiesUtil.getProperty(
						"mysql.data.CommentClassifier.lxrdb", "lxr"))
				.append("?user=")
				.append(PropertiesUtil.getProperty(
						"mysql.data.DataSource.dbserver.user", "root"))
				.append("&password=")
				.append(PropertiesUtil.getProperty(
						"mysql.data.DataSource.dbserver.pass", "123123"));
			}
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

	public static void close() {
		try {
			if(COMMENT_CONN != null && !COMMENT_CONN.isClosed())
				COMMENT_CONN.close();
			
			if(LXR_CONN != null && !LXR_CONN.isClosed())
				LXR_CONN.close();
			
			if(COMMENT_ANA_CONN != null && !COMMENT_ANA_CONN.isClosed())
				COMMENT_ANA_CONN.close();
			
			if(JUDGE_CONN != null && !JUDGE_CONN.isClosed())
				JUDGE_CONN.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
