package mysql.data.gui;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;

import mysql.data.analysisDB.entity.JudgeTableInfo;
import mysql.data.util.PropertiesUtil;

public class EvaluationPreparation {
	private static Logger logger = Logger
			.getLogger(EvaluationPreparation.class);
	private Connection conn;
	private Properties props;

	public EvaluationPreparation() {
		props = PropertiesUtil.getProperties();
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
			this.conn = DriverManager.getConnection(url.toString());

		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public Connection getConn() {
		return conn;
	}

	public boolean hasJudgeInfo() throws SQLException {
		Statement stmt = conn.createStatement();

		ResultSet rs = stmt.executeQuery("select count(*) from judge;");
		rs.next();
		int count = rs.getInt(1);
		rs.close();
		stmt.close();
		if (count == 0) {
			return false;
		}
		return true;
	}
	
	public Map<String, JudgeTableInfo> loadJudgeInfoFromDB() throws SQLException {
		Map<String, JudgeTableInfo> judgeList = new HashMap<String, JudgeTableInfo>();
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery("select * from judge;");
		while(rs.next()) {
			JudgeTableInfo ins = new JudgeTableInfo();
			ins.format(rs);
			judgeList.put(ins.getComment_path(),ins);
		}
		rs.close();
		stmt.close();
		return judgeList;
	}

	public void insertAllPathToJudgeDB(Set<String> paths) throws SQLException {
		Statement stmt = conn.createStatement();

		int counter = 0;
		for (String l : paths) {
			stmt.executeUpdate("insert into judge(comment_path) values(\"" + l
					+ "\");");
			logger.info(++counter);
		}
		logger.info("finish insert");

		stmt.close();

	}
	
	public void updateJudgeInfo(JudgeTableInfo jti) throws SQLException {
		Statement stmt = conn.createStatement();
		StringBuilder sql = new StringBuilder();
		sql.append("update judge ")
			.append("set validation_state=")
			.append(jti.getValidation_state())
			.append(",")
			.append("completeness_score=")
			.append(jti.getCompleteness_score())
			.append(",")
			.append("consistency_score=")
			.append(jti.getConsistency_score())
			.append(",")
			.append("information_score=")
			.append(jti.getInformation_score())
			.append(",")
			.append("readability_score=")
			.append(jti.getReadability_score())
			.append(",")
			.append("objectivity_score=")
			.append(jti.getObjectivity_score())
			.append(",")
			.append("verifiability_score=")
			.append(jti.getVerifiability_score())
			.append(",")
			.append("relativity_score=")
			.append(jti.getRelativity_score())
			.append(" where comment_path='")
			.append(jti.getComment_path())
			.append("';");
		
		stmt.executeUpdate(sql.toString());
		
		logger.info("update " + jti.getComment_path());
		stmt.close();
		
	}

}
