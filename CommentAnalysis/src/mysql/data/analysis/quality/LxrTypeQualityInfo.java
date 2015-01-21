package mysql.data.analysis.quality;

public class LxrTypeQualityInfo {
	private int comments_count;
	private int entries_count;
	private int redundant_comments_count;
	private double total_consistency_score;
	private int consistency_count;
	private int total_length;
	private int length_count;
	private int image_count;
	private int url_count;
	
	//感知质量属性的技术
	private int valid_count;
	private int infomative_count;
	private int complete_count;
	
	public LxrTypeQualityInfo() {
		this.comments_count = 0;
		this.entries_count = 0;
		this.redundant_comments_count = 0;
		this.total_consistency_score = 0;
		this.consistency_count = 0;
		this.total_length = 0;
		this.length_count = 0;
		this.image_count = 0;
		this.url_count = 0;
		this.valid_count = 0;
		this.infomative_count = 0;
		this.complete_count = 0;
	}

	public int getComments_count() {
		return comments_count;
	}

	public void setComments_count(int comments_count) {
		this.comments_count = comments_count;
	}

	public int getEntries_count() {
		return entries_count;
	}

	public void setEntries_count(int entries_count) {
		this.entries_count = entries_count;
	}

	public int getRedundant_comments_count() {
		return redundant_comments_count;
	}

	public void addRedundant_comments_count() {
		this.redundant_comments_count += 1;
	}

	public double getTotal_consistency_score() {
		return total_consistency_score;
	}

	public void addTotal_consistency_score(double score) {
		this.consistency_count += 1;
		this.total_consistency_score += score;
	}


	public int getTotal_length() {
		return total_length;
	}

	public void addTotal_length(int len) {
		this.length_count += 1;
		this.total_length += len ;
	}

	public int getConsistency_count() {
		return consistency_count;
	}

	public int getLength_count() {
		return length_count;
	}

	public int getImage_count() {
		return image_count;
	}
	
	public void addImage_count(int count) {
		this.image_count += count;
	}

	public int getUrl_count() {
		return url_count;
	}
	
	public void addUrl_count(int count) {
		this.url_count += count;
	}
	
	public void addValid_count() {
		this.valid_count += 1;
	}
	
	public int getValid_count() {
		return this.valid_count;
	}
	
	public void addInfomative_count() {
		this.infomative_count += 1;
	}
	
	public int getInfomative_count() {
		return this.infomative_count;
	}
	
	public void addComplete_count() {
		this.complete_count += 1;
	}
	
	public int getComplete_count() {
		return this.complete_count;
	}
	
}
