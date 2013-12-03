package mysql.data.analysisDB.entity;

import java.nio.charset.Charset;
import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CommentTableInfo {
	private int id;
	private String comment_path;
	private String origin_comment;
	private String filtered_comment;
	private int filetag_count;
	private String lxr_type;
	private String path_file;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getComment_path() {
		return comment_path;
	}
	public void setComment_path(String comment_path) {
		this.comment_path = comment_path;
	}
	public String getOrigin_comment() {
		return origin_comment;
	}
	public void setOrigin_comment(String origin_comment) {
		this.origin_comment = origin_comment;
	}
	public String getFiltered_comment() {
		return filtered_comment;
	}
	public void setFiltered_comment(String filtered_comment) {
		this.filtered_comment = filtered_comment;
	}
	public int getFiletag_count() {
		return filetag_count;
	}
	public void setFiletag_count(int filetag_count) {
		this.filetag_count = filetag_count;
	}
	public String getLxr_type() {
		return lxr_type;
	}
	public void setLxr_type(String lxr_type) {
		this.lxr_type = lxr_type;
	}
	public String getPath_file() {
		return path_file;
	}
	public void setPath_file(String path_file) {
		this.path_file = path_file;
	}

	public void format(ResultSet rs) throws SQLException{
		this.setId(rs.getInt(1));
		this.setComment_path(rs.getString(2));
		
		Blob blob = rs.getBlob(3);
		int len = (int) blob.length();
		byte[] data = blob.getBytes(1, len);
		this.setOrigin_comment(new String(data, Charset.forName("utf-8")));
		
		blob = rs.getBlob(4);
		len = (int) blob.length();
		data = blob.getBytes(1, len);
		this.setFiltered_comment(new String(data, Charset.forName("utf-8")));
		
		this.setFiletag_count(rs.getInt(5));
		this.setLxr_type(rs.getString(6));
		this.setPath_file(rs.getString(7));
	}
	
}
