package mysql.data.filter;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IscasChineseCommentExtractor {
	//匹配多行注释/* */
//	private String regex1 = "/\\*(\\s)*([^\\s]+)(\\s)*\\*/";
	private String regex1 = "/\\*(\\s)*([[0-9a-zA-Z<>=+\\-\\*/][\u4e00-\u9fa5]\\pP]+)(\\s)*\\*/";
	//匹配单行注释 //
//	private String regex2 = "//(\\s)*([^\\s]+)(\\s)*";
	private String regex2 = "//(\\s)*([[0-9a-zA-Z<>=+\\-\\*/][\u4e00-\u9fa5]\\pP]+)(\\s)*";
	
	//匹配包含中文的行
	private String regex3 = "(\\s)*([[0-9a-zA-Z<>=+\\-\\*/][\u4e00-\u9fa5]\\pP]+)(\\s)*";

	public List<String> getText(String inputStr) {
		List<String> comments = new ArrayList<String>();
		inputStr = inputStr.replaceAll(" ", "");
		
		Matcher m = Pattern.compile("[\u4e00-\u9fa5]").matcher(inputStr);
		
		//如果包含中文，就认为不是完全粘贴的原始代码
		
		if(m.find()){
			Matcher m1 = Pattern.compile(regex1).matcher(inputStr);
			Matcher m2 = Pattern.compile(regex2).matcher(inputStr);
			Matcher m3 = Pattern.compile(regex3).matcher(inputStr);
			while (m1.find()) {
				if (!Pattern.matches("^[a-zA-Z0-9\\pP<>]*$", m1.group(2)))
					comments.add(m1.group(2).replaceAll("[\\*|/]", ""));
			}
			while (m2.find()) {
				if (!Pattern.matches("^[a-zA-Z0-9\\pP<>]*$", m2.group(2)))
					comments.add(m2.group(2));
			}
			
			//前面已经确定了包含中文，这里如果通过前两种模式的匹配都没有找到的话，就证明不是按标准的注释格式//或者/* */写的，就将包含中文的行放入
			if(comments.size()==0){
				while (m3.find()) {
					if (!Pattern.matches("^[a-zA-Z0-9\\pP<>]*$", m3.group(2)))
						comments.add(m3.group(2));
				}
			}
		}else{
			comments.add("EMPTY!!!");
		}
		
		return comments;
	}
}
