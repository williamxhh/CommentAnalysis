package mysql.data.analysisDB.entity;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

public class TemplateTableInfo {
	private int id;
	private String path_file;
	private String lxr_type;
	private Set<String> strict_template;
	private Set<String> middle_template;
	private Set<String> loose_template;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getPath_file() {
		return path_file;
	}
	public void setPath_file(String path_file) {
		this.path_file = path_file;
	}
	public String getLxr_type() {
		return lxr_type;
	}
	public void setLxr_type(String lxr_type) {
		this.lxr_type = lxr_type;
	}
	public Set<String> getStrict_template() {
		return strict_template;
	}
	public void setStrict_template(Set<String> strict_template) {
		this.strict_template = strict_template;
	}
	public Set<String> getMiddle_template() {
		return middle_template;
	}
	public void setMiddle_template(Set<String> middle_template) {
		this.middle_template = middle_template;
	}
	public Set<String> getLoose_template() {
		return loose_template;
	}
	public void setLoose_template(Set<String> loose_template) {
		this.loose_template = loose_template;
	}
	
	public void format(ResultSet rs) throws SQLException{
		this.setId(rs.getInt(1));
		this.setPath_file(rs.getString(2));
		this.setLxr_type(rs.getString(3));
		String[] st = rs.getString(4).split(";");
		Set<String> strict_template = new HashSet<String>();
		for(String s:st){
			strict_template.add(s);
		}
		this.setStrict_template(strict_template);
		
		String[] mt = rs.getString(5).split(";");
		Set<String> middle_template = new HashSet<String>();
		for(String m:mt){
			middle_template.add(m);
		}
		this.setMiddle_template(middle_template);
		
		String[] lt = rs.getString(6).split(";");
		Set<String> loose_template = new HashSet<String>();
		for(String l:lt){
			loose_template.add(l);
		}
		this.setLoose_template(loose_template);
	}
}
