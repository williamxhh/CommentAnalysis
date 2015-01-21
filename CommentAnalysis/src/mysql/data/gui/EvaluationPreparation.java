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
import mysql.data.util.ConnectionUtil;
import mysql.data.util.PropertiesUtil;

public class EvaluationPreparation {
	private static Logger logger = Logger
			.getLogger(EvaluationPreparation.class);

	public boolean hasJudgeInfo() throws SQLException {
		Statement stmt = ConnectionUtil.getJudgeConnection().createStatement();

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
	
	public Map<String, JudgeTableInfo> loadJudgeInfoFromDB(){
		Map<String, JudgeTableInfo> judgeList = new HashMap<String, JudgeTableInfo>();
		try {
			Statement stmt = ConnectionUtil.getJudgeConnection().createStatement();

			ResultSet rs = stmt.executeQuery("select * from judge;");
			while (rs.next()) {
				JudgeTableInfo ins = new JudgeTableInfo();
				ins.format(rs);
				judgeList.put(ins.getComment_path(), ins);
			}
			rs.close();
			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return judgeList;
	}
	
	public JudgeTableInfo getJudgeInfo(String path) {
		JudgeTableInfo ins = new JudgeTableInfo();
		
		try {
			Statement stmt = ConnectionUtil.getJudgeConnection().createStatement();
			ResultSet rs = stmt.executeQuery("select * from judge where comment_path='" + path + "';");
			if(rs.next()) {
				ins.format(rs);
			}
			rs.close();
			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return ins;
	}

	public void insertAllPathToJudgeDB(Set<String> paths) throws SQLException {
		Statement stmt = ConnectionUtil.getJudgeConnection().createStatement();

		int counter = 0;
		for (String l : paths) {
			stmt.executeUpdate("insert into judge(comment_path) values(\"" + l
					+ "\");");
			logger.info(++counter);
		}
		logger.info("finish insert");

		stmt.close();

	}
	
	public void updateJudgeInfo(JudgeTableInfo jti){
		try {
			Statement stmt = ConnectionUtil.getJudgeConnection().createStatement();
		
			StringBuilder sql = new StringBuilder();
			sql.append("update judge ").append("set validation_state=")
					.append(jti.getValidation_state()).append(",")
					.append("is_redundant=")
					.append(jti.getIs_redundant()).append(",")
					.append("completeness_score=")
					.append(jti.getCompleteness_score()).append(",")
					.append("consistency_score=")
					.append(jti.getConsistency_score()).append(",")
					.append("information_score=")
					.append(jti.getInformation_score()).append(",")
					.append("readability_score=")
					.append(jti.getReadability_score()).append(",")
					.append("objectivity_score=")
					.append(jti.getObjectivity_score()).append(",")
					.append("verifiability_score=")
					.append(jti.getVerifiability_score()).append(",")
					.append("relativity_score=")
					.append(jti.getRelativity_score()).append(",")
					.append("image_count=")
					.append(jti.getImage_count()).append(",")
					.append("url_count=")
					.append(jti.getUrl_count())
					.append(" where comment_path='")
					.append(jti.getComment_path()).append("';");

			stmt.executeUpdate(sql.toString());

			logger.info("update " + jti.getComment_path());
			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}

}
