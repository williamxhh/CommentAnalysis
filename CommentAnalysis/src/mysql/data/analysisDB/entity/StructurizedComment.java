package mysql.data.analysisDB.entity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import mysql.data.algorithms.AlgorithmsUtil;
import tool.nlpir.WordSeg;

public class StructurizedComment {
	private static Logger logger = Logger.getLogger(StructurizedComment.class);
	private AlgorithmsUtil algo;
	private String path;
	private String type;
	private List<String> template;
	private String comment;
	private List<String> comment_seg;
	
	private Map<String, String> structurizedContent;
	
	public StructurizedComment(String path, String type, List<String> template, String comment) {
		this.path = path;
		this.type = type;
		algo = new AlgorithmsUtil();
		this.template = algo.removeStopWords(template);
		this.comment = comment;
		structurize();
	}
	
	/**
	 * 将注释模板与注释文本分词以后的结果进行比对，将紧随模板词以后的文本，作为该模板词的内容
	 * 默认的key为default，应对模板为空的情况
	 */
	
	private void structurize() {
		
		structurizedContent = new HashMap<String, String>();
		WordSeg wordSeg = new WordSeg();
		this.comment_seg = wordSeg.segmentation(this.comment, WordSeg.NO_POS_TAG, WordSeg.SEG_FILTER_WITHPUNC);
		
		int nextIndex = 0;
		int nextIndexInTemplate = 0;
		
		StringBuilder content = new StringBuilder();
		String lastKey = "default";

		while (nextIndex < comment_seg.size()
				&& nextIndexInTemplate < template.size()) {
			if (!WordSeg.SEG_FILTER.getText(comment_seg.get(nextIndex)).equals(template.get(nextIndexInTemplate))) {
				content.append(comment_seg.get(nextIndex));
				++nextIndex;
			} else {
				if(!lastKey.equals("") && content.length() != 0) {
					structurizedContent.put(lastKey, content.toString());
					lastKey = comment_seg.get(nextIndex);
					content = new StringBuilder();
				}
				++nextIndex;
				++nextIndexInTemplate;
			}
		}

		while (nextIndex < comment_seg.size()) {
			content.append(comment_seg.get(nextIndex));
			++nextIndex;
		}
		
		if(!lastKey.equals("")  && content.length() != 0) {
			structurizedContent.put(lastKey, content.toString());
		}
	}
	
	
	public Map<String, String> getStructurizedContent() {
		return structurizedContent;
	}

	//函数注释：包含必要元素以及至少两种可选元素为完整
	//变量注释：包含必要元素以及至少一种可选元素为完整
	public boolean isComplete() {
		boolean containsEssential = false;
		int optionalCount = 0;
		Set<String> essential = algo.loadReservedTemplateWordsEssential();
		Set<String> optional = algo.loadReservedTemplateWordsOptional();
		
		logger.info(path);
		
		for(String word : this.comment_seg) {
			if(essential.contains(word)) {
				containsEssential = true;
			} else if (optional.contains(word)) {
				++optionalCount;
			}
		}
		
		if(containsEssential) {
			if(type.equals("function definition")) {
				if(optionalCount >= 2) {
					return true;
				}
			} else {
				if(optionalCount >= 1) {
					return true;
				}
			}
		}
		
		return false;
	}

}
