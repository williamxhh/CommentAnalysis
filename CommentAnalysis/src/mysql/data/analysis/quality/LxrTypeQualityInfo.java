package mysql.data.analysis.quality;

public class LxrTypeQualityInfo {
	private int comments_count;
	private int entries_count;
	private int redundant_comments_count;
	private int total_consistency_score;
	private int consistency_count;
	private int total_length;
	private int length_count;
	
	public LxrTypeQualityInfo() {
		this.comments_count = 0;
		this.entries_count = 0;
		this.redundant_comments_count = 0;
		this.total_consistency_score = 0;
		this.consistency_count = 0;
		this.total_length = 0;
		this.length_count = 0;
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

	public int getTotal_consistency_score() {
		return total_consistency_score;
	}

	public void addTotal_consistency_score(int score) {
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
	
}
