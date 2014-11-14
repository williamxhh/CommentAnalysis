package mysql.data.filter;

import java.util.regex.Pattern;

public class PunctuationFilter implements FilterBase {
	
	private FilterBase filter;
	
	public PunctuationFilter(FilterBase filter) {
		this.filter = filter;
	}

	@Override
	public String getText(String inputStr) {
		String regex = "\\p{Punct}";
		String inprocess = Pattern.compile(regex).matcher(inputStr).replaceAll("");
		
		regex = "\\pP";
		inprocess = Pattern.compile(regex).matcher(inprocess).replaceAll("");
		
		regex = "\\p{P}";
		inprocess = Pattern.compile(regex).matcher(inprocess).replaceAll("");
		
		return filter.getText(inprocess.trim());
	}

}
