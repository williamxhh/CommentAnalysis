package mysql.data.analysis.quality;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import tool.nlpir.WordSeg;
import mysql.data.analysis.CommentAnalyzer;
import mysql.data.analysisDB.entity.JudgeTableInfo;
import mysql.data.analysisDB.entity.StructurizedComment;
import mysql.data.gui.EvaluationPreparation;

public class SubjectiveViewQuality {
	private static Logger logger = Logger.getLogger(SubjectiveViewQuality.class);
	private final int VALID_MIN_LEN = 5;
	private final int VALID_MIN_NOUN_COUNT = 1;
	
	public static final int TRUE_POSITIVE = 1;
	public static final int TRUE_NEGATIVE = 2;
	public static final int FALSE_POSITIVE = 3;
	public static final int FALSE_NEGATIVE = 4;
	
	private CommentAnalyzer ca;
	private WordSeg wordSeg;
	private EvaluationPreparation ep;
	
	private Map<String, LxrTypeQualityInfo> info_map;
	
	//保存每一个word在多少个文件的注释中出现，这里的统计以源码文件为单位，不细化到每一条注释
	private Map<String, Integer> word_idf_count_map;
	
	public SubjectiveViewQuality() {
		ca = new CommentAnalyzer(true);
		wordSeg = new WordSeg();
		ep = new EvaluationPreparation();
		info_map = new TreeMap<String, LxrTypeQualityInfo>();
	}
	
	public void addValid(String type) {
		if(info_map.containsKey(type)) {
			info_map.get(type).addValid_count();
		} else {
			LxrTypeQualityInfo info = new LxrTypeQualityInfo();
			info.addValid_count();
			info_map.put(type, info);
		}
	}
	
	public int getValidCount(String type) {
		if(info_map.containsKey(type)) {
			return info_map.get(type).getValid_count();
		}
		return 0;
	}
	
	public void addInformative(String type) {
		if(info_map.containsKey(type)) {
			info_map.get(type).addInfomative_count();
		} else {
			LxrTypeQualityInfo info = new LxrTypeQualityInfo();
			info.addInfomative_count();
			info_map.put(type, info);
		}
	}
	
	public int getInformativeCount(String type) {
		if(info_map.containsKey(type)) {
			return info_map.get(type).getInfomative_count();
		}
		return 0;
	}
	
	public void addComplete(String type) {
		if(info_map.containsKey(type)) {
			info_map.get(type).addComplete_count();
		} else {
			LxrTypeQualityInfo info = new LxrTypeQualityInfo();
			info.addComplete_count();
			info_map.put(type, info);
		}
	}
	
	public int getCompleteCount(String type) {
		if(info_map.containsKey(type)) {
			return info_map.get(type).getComplete_count();
		}
		return 0;
	}
	
	public int getValidState(String path) {
		if(ep.getJudgeInfo(path).getValidation_state() == JudgeTableInfo.VALID) {
			if(isValid(path)) {
				return TRUE_POSITIVE;
			} else {
//				logger.warn(path);
				return FALSE_NEGATIVE;
			}
		} else if (ep.getJudgeInfo(path).getValidation_state() == JudgeTableInfo.INVALID){
			if(isValid(path)) {
				return FALSE_POSITIVE;
			} else {
				return TRUE_NEGATIVE;
			}
		}
		//返回0实际上是没评的，没什么意义
		return 0;
	}
	
	public int getInfomativeState(String path) {
		if(ep.getJudgeInfo(path).getValidation_state() != JudgeTableInfo.NON_JUDGE) {
			if(ep.getJudgeInfo(path).getInformation_score() == JudgeTableInfo.INFOMATION_GOOD) {
				if(isInformative(path)) {
					return TRUE_POSITIVE;
				} else {
					return FALSE_NEGATIVE;
				}
			} else if(ep.getJudgeInfo(path).getInformation_score() == JudgeTableInfo.INFOMATION_BAD) {
				if(isInformative(path)) {
					return FALSE_POSITIVE;
				} else {
					return TRUE_NEGATIVE;
				}
			}
		}
		
		return 0;
	}
	
	/**
	 * 是否有效
	 * @return
	 */
	public boolean isValid(String path) {
		if(isMinLenValid(path) && isMinNounValid(path) && !isRedundant(path)) { 
			return true;
		}
		logger.warn("invalid: " + path + "\t" + ca.loadCommentsTypes().get(path));
		return false;
	}
	
	protected boolean isMinLenValid(String path) {
		return ca.getAllComments().get(path).length() >= VALID_MIN_LEN;
	}
	
	protected boolean isMinNounValid(String path) {
		return getNounCount(path) >= VALID_MIN_NOUN_COUNT;
	}
	
	protected int getNounCount(String path) {
		int count = 0;
		
		List<String> seg_list = wordSeg.segmentation(ca.getAllComments().get(path),
				WordSeg.POS_TAG, WordSeg.SEG_FILTER);
		
		for(String word : seg_list) {
			int index = word.lastIndexOf('/');
			if(index != -1 && word.charAt(index + 1) == 'n'){
				++count;
			}
		}
		
		return count;
	}
	
	protected boolean isRedundant(String path) {
		JudgeTableInfo jti = ep.getJudgeInfo(path);
		return jti.getIs_redundant() == 1;
	}
	
	/**
	 * 是否有信息量
	 * @return
	 */
	public boolean isInformative(String path){
		//TODO:  idf还没有加
//		prepareFileWordset();
//		double file_count = ca.getAllCommentedFilepath().size();
//		logger.info(path);
//		
//		Map<String, Integer> word_count = new HashMap<String, Integer>();
//		for(String word : wordSeg.segmentation(ca.getAllComments().get(path), WordSeg.NO_POS_TAG, WordSeg.SEG_FILTER)) {
//			if(word_count.containsKey(word)) {
//				word_count.put(word, word_count.get(word) + 1);
//			} else {
//				word_count.put(word, 1);
//			}
//		}
//		
//		double max_tfidf = 0;
//		for(String word : word_count.keySet()) {
//			double tfidf = word_count.get(word) * Math.log(file_count / word_idf_count_map.get(word));
//			if(tfidf > max_tfidf) {
//				max_tfidf = tfidf;
//			}
//			logger.info(String.format("%s : %.2f", word, tfidf));
//		}
//		logger.info(String.format("max_tfidf : %.2f", max_tfidf));
		
		return isValid(path) || hasImageOrUrl(path);
	}
	
	protected void prepareFileWordset() {
		if(word_idf_count_map == null) {
			word_idf_count_map = new HashMap<String, Integer>();
			for(String file_name : ca.getAllCommentedFilepath()) {
				//该文件下的所有注释中的词的集合
				Set<String> word_set = new HashSet<String>();
				for(String comments : ca.getFileComments(file_name, "ALL")) {
					word_set.addAll(wordSeg.segmentation(comments, WordSeg.NO_POS_TAG, WordSeg.SEG_FILTER));
				}
				for(String word : word_set) {
					if(word_idf_count_map.containsKey(word)) {
						word_idf_count_map.put(word, word_idf_count_map.get(word) + 1);
					} else {
						word_idf_count_map.put(word, 1);
					}
				}
			}
		}
	}
	
	protected boolean hasImageOrUrl(String path) {
		JudgeTableInfo jti = ep.getJudgeInfo(path);
		return jti.getImage_count() > 0 || jti.getUrl_count() > 0;
	}
	
	/**
	 * 是否可读性好
	 * @return
	 */
	public boolean isReadable(String path){
		return false;
	}
	
	/**
	 * 内容是否完整
	 * @return
	 */
	public boolean isComplete(String path){
		StructurizedComment sc = new StructurizedComment(path, ca.loadCommentsTypes().get(path), ca.loadNewTemplate(path, CommentAnalyzer.TEMPLATE_SAME_TYPE), ca.getAllComments().get(path));
		return sc.isComplete();
	}
}
