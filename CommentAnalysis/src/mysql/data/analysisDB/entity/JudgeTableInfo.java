package mysql.data.analysisDB.entity;

import java.sql.ResultSet;
import java.sql.SQLException;

public class JudgeTableInfo implements Comparable<JudgeTableInfo>{
	private String comment_path;
	private int validation_state;
	private int is_redundant;
	private String validation_content;
	private int completeness_score;
	private int consistency_score;
	private int information_score;
	private int readability_score;
	private int objectivity_score;
	private int verifiability_score;
	private int relativity_score;
	
	public String getComment_path() {
		return comment_path;
	}
	public void setComment_path(String comment_path) {
		this.comment_path = comment_path;
	}
	public int getValidation_state() {
		return validation_state;
	}
	public void setValidation_state(int validation_state) {
		this.validation_state = validation_state;
	}
	public int getIs_redundant() {
		return is_redundant;
	}
	public void setIs_redundant(int is_redundant) {
		this.is_redundant = is_redundant;
	}
	public String getValidation_content() {
		return validation_content;
	}
	public void setValidation_content(String validation_content) {
		this.validation_content = validation_content;
	}
	public int getCompleteness_score() {
		return completeness_score;
	}
	public void setCompleteness_score(int completeness_score) {
		this.completeness_score = completeness_score;
	}
	public int getConsistency_score() {
		return consistency_score;
	}
	public void setConsistency_score(int consistency_score) {
		this.consistency_score = consistency_score;
	}
	public int getInformation_score() {
		return information_score;
	}
	public void setInformation_score(int information_score) {
		this.information_score = information_score;
	}
	public int getReadability_score() {
		return readability_score;
	}
	public void setReadability_score(int readability_score) {
		this.readability_score = readability_score;
	}
	public int getObjectivity_score() {
		return objectivity_score;
	}
	public void setObjectivity_score(int objectivity_score) {
		this.objectivity_score = objectivity_score;
	}
	public int getVerifiability_score() {
		return verifiability_score;
	}
	public void setVerifiability_score(int verifiability_score) {
		this.verifiability_score = verifiability_score;
	}
	
	public int getRelativity_score() {
		return relativity_score;
	}
	public void setRelativity_score(int relativity_score) {
		this.relativity_score = relativity_score;
	}
	public void format(ResultSet rs) throws SQLException{
		this.setComment_path(rs.getString(1));
		this.setValidation_state(rs.getInt(2));
		this.setIs_redundant(rs.getInt(3));
		this.setValidation_content(rs.getString(4));
		this.setCompleteness_score(rs.getInt(5));
		this.setConsistency_score(rs.getInt(6));
		this.setInformation_score(rs.getInt(7));
		this.setReadability_score(rs.getInt(8));
		this.setObjectivity_score(rs.getInt(9));
		this.setVerifiability_score(rs.getInt(10));
		this.setRelativity_score(rs.getInt(11));
	}
	@Override
	public int compareTo(JudgeTableInfo o) {
		return this.getComment_path().compareTo(o.getComment_path());
	}
	
}
