package mysql.data.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 对注释做分词分析以后的结果
 * 	path是注释路径
 * 	word_list是分词以后的词列表
 * 	noun_list是名词列表
 *  verb_list是动词列表
 *  other_list是其他词性的词的列表，除去标点符号
 *  
 *  
 * @author Xiaowei GAO
 * @date 2014年12月1日
 * @description TODO
 * @ClassName NLPAnalysisResult
 *
 */
public class NLPAnalysisResult implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String path;
	private int content_len;
	private List<String> word_list;
	private List<String> noun_list;
	private List<String> verb_list;
	private List<String> other_list;
	
	public NLPAnalysisResult(String path){
		this.path = path;
		content_len = 0;
		word_list = new ArrayList<String>();
		noun_list = new ArrayList<String>();
		verb_list = new ArrayList<String>();
		other_list = new ArrayList<String>();
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public int getContent_len() {
		return content_len;
	}

	public void setContent_len(int content_len) {
		this.content_len = content_len;
	}

	public List<String> getWord_list() {
		return word_list;
	}

	public void setWord_list(List<String> word_list) {
		this.word_list = word_list;
	}

	public List<String> getNoun_list() {
		return noun_list;
	}

	public void setNoun_list(List<String> noun_list) {
		this.noun_list = noun_list;
	}

	public List<String> getVerb_list() {
		return verb_list;
	}

	public void setVerb_list(List<String> verb_list) {
		this.verb_list = verb_list;
	}

	public List<String> getOther_list() {
		return other_list;
	}

	public void setOther_list(List<String> other_list) {
		this.other_list = other_list;
	}
	
	
}
