package mysql.data.filter;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 * 过滤器
 * 这个过滤器会保留最后连续的中文字符串，将其他的标点，英文等去掉 
 * 传给过滤器的已经是被：分隔处理过了字符串了，所以最后的连续中文字符串就是原文中紧挨着：的描述
 * @author dell
 *
 */
public class TemplateCandidateFilter{
//	private String regex = "^(.*[\\pP|a-zA-Z|\\s|0-9])(.*)$";
	private String regex = "[a-zA-Z|\\s|0-9|\\pP]*([^\\pPa-zA-Z\\s0-9]+)[:|：]";

	public List<String> getText(String inputStr) {
		List<String> cands = new ArrayList<String>();
		inputStr = inputStr.trim();
		Matcher m = Pattern.compile(regex).matcher(inputStr);
		while(m.find()){
			cands.add(m.group(1));
		}
		return cands;
	}
}
