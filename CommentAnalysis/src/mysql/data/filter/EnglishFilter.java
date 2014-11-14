package mysql.data.filter;

import java.util.regex.Pattern;

/**
 * ���˵����е�Ӣ����ĸ
 * @author Xiaowei GAO
 * @date 2014��11��14��
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
