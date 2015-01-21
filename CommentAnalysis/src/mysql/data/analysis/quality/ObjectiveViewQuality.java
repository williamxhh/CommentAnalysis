package mysql.data.analysis.quality;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class ObjectiveViewQuality {
	private Map<String, LxrTypeQualityInfo> info_map;
	
	//用注释比例的map来初始化，key为lxr_type，value为 已注释数/入口数
	public ObjectiveViewQuality(Map<String, String> commentedRatioInfo) {
		info_map = new TreeMap<String, LxrTypeQualityInfo>();
		for(String type: commentedRatioInfo.keySet()) {
			String value = commentedRatioInfo.get(type);
			int comments_count = Integer.parseInt(value.substring(0,value.indexOf('/')));
			int entries_count = Integer.parseInt(value.substring(value.indexOf('/') + 1));
			
			LxrTypeQualityInfo info  = new LxrTypeQualityInfo();
			info.setComments_count(comments_count);
			info.setEntries_count(entries_count);
			
			info_map.put(type, info);
		}
	}
	
	public LxrTypeQualityInfo getLxrTypeQualityInfo(String type) {
		return info_map.get(type);
	}
	
	public Set<String> getAllTypes() {
		return info_map.keySet();
	}
	
	public int getCommentsCount(String type) {
		return info_map.get(type).getComments_count();
	}
	
	public int getTotalCommentsCount() {
		int total = 0;
		for(LxrTypeQualityInfo info : info_map.values()) {
			total += info.getComments_count();
		}
		return total;
	}
	
	//该类型的注释占注释总体的比例，分布占比
	public String getCommentsRatio(String type) {
		double ratio = (double)getCommentsCount(type)/getTotalCommentsCount();
		if(Double.isNaN(ratio)) {
			return String.format("%.2f%%", 0.0);
		}
		return String.format("%.2f%%", ratio * 100);
	}
	
	public int getEntriesCount(String type) {
		return info_map.get(type).getEntries_count();
	}
	
	//该类型的注释数占该类型注释入口的比例，覆盖占比
	public String getCoverageRatio(String type) {
		double ratio = (double)getCommentsCount(type)/getEntriesCount(type);
		if(Double.isNaN(ratio)) {
			return String.format("%.2f%%", 0.0);
		}
		return String.format("%.2f%%", ratio * 100);
	}
	
	public int getRedundantCount(String type) {
		return info_map.get(type).getRedundant_comments_count();
	}
	
	public String getRedundantRatio(String type) {
		double ratio = (double)getRedundantCount(type)/getCommentsCount(type);
		if(Double.isNaN(ratio)) {
			return String.format("%.2f%%", 0.0);
		}
		return String.format("%.2f%%", ratio * 100);
	}
	
	public double getConsistencyScore(String type) {
		LxrTypeQualityInfo info = info_map.get(type);
		double score = (double)info.getTotal_consistency_score()/info.getConsistency_count();
		if(Double.isNaN(score)) {
			return 0.0;
		}
		return Double.parseDouble(String.format("%.2f",score));
	}
	
	public double getAverageLength(String type) {
		LxrTypeQualityInfo info = info_map.get(type);
		double avg_len = (double)info.getTotal_length()/info.getLength_count();
		if(Double.isNaN(avg_len)) {
			return 0.0;
		}
		return Double.parseDouble(String.format("%.2f",avg_len));
	}
	
	public int getImageCount(String type) {
		return info_map.get(type).getImage_count();
	}
	
	public int getUrlCount(String type) {
		return info_map.get(type).getUrl_count();
	}
	
}
