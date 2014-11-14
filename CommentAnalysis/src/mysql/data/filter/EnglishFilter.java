package mysql.data.filter;

import java.util.regex.Pattern;

/**
 * 过滤掉所有的英文字母
 * @author Xiaowei GAO
 * @date 2014年11月14日
 * @description TODO
 * @ClassName EnglishFilter
 *
 */
public class EnglishFilter implements FilterBase {
	
	private FilterBase filter;
	
	public EnglishFilter(FilterBase filter) {
		this.filter = filter;
	}
	
	@Override
	public String getText(String inputStr) {
		String regex = "[A-Za-z]";
		String inprocess = Pattern.compile(regex).matcher(inputStr).replaceAll("");
		
		return filter.getText(inprocess.trim());
	}

}
