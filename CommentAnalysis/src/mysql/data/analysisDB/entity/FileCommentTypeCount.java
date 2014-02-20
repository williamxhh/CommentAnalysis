package mysql.data.analysisDB.entity;

public class FileCommentTypeCount {
	private String filePath;
	private String lxrType;
	private int count;
	public String getFilePath() {
		return filePath;
	}
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
	public String getLxrType() {
		return lxrType;
	}
	public void setLxrType(String lxrType) {
		this.lxrType = lxrType;
	}
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	
	public FileCommentTypeCount(String path,String type,int count){
		this.filePath = path;
		this.lxrType = type;
		this.count = count;
	}
}
