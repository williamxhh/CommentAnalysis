package mysql.data.filter;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SourceCodeLineByLineCommentFilter implements FilterBase {

	private FilterBase filter;

	// 匹配包含中文的行
	private String regex3 = "(\\s)*([[0-9a-zA-Z<>=+\\-\\*/][\u4e00-\u9fa5]\\pP]+)(\\s)*";

	private String chinese = "[\u4e00-\u9fa5]";

	public SourceCodeLineByLineCommentFilter(FilterBase filter) {
		this.filter = filter;
	}

	@Override
	public String getText(String inputStr) {
		StringBuilder result = new StringBuilder();
		
		List<String> comments = new ArrayList<String>();
		inputStr = inputStr.replaceAll(" ", "");
		
		Matcher m = Pattern.compile(chinese).matcher(inputStr);
		
		//如果包含中文，就认为不是完全粘贴的原始代码
		
		if(m.find()){
			Matcher m3 = Pattern.compile(regex3).matcher(inputStr);
			
			
			//前面已经确定了包含中文，这里如果通过前两种模式的匹配都没有找到的话，就证明不是按标准的注释格式//或者/* */写的，就将包含中文的行放入
			while (m3.find()) {
				if (!Pattern.matches("^[a-zA-Z0-9\\pP<>]*$", m3.group(2)) && !comments.contains(m3.group(2)))
					comments.add(m3.group(2));
			}
			
			int index = 0;
			
			
			while(index < comments.size()) {
				String s = comments.get(index);
				if(Pattern.compile(chinese).matcher(s).find()) {
					s = s.replaceAll("/\\*", "");
					s = s.replaceAll("//", "");
					s = s.replaceAll("\\*/", "");
					result.append(s + "\r\n");
				}
				++index;
			}
		}
		
		return filter.getText(result.toString());
	}

}
