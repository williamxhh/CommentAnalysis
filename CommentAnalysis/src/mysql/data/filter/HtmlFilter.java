package mysql.data.filter;

import java.util.regex.Pattern;

public class HtmlFilter implements FilterBase {
	
	private FilterBase filter;
	
	public HtmlFilter(FilterBase filter){
		this.filter = filter;
	}

	@Override
	public String getText(String inputStr) {
		String regex = "&nbsp;";
		String inprocess = Pattern.compile(regex).matcher(inputStr).replaceAll(" ");
		
		regex = "&lt;";
		inprocess = Pattern.compile(regex).matcher(inprocess).replaceAll("<");
		
		regex = "&gt;";
		inprocess = Pattern.compile(regex).matcher(inprocess).replaceAll(">");
		
		regex = "'''";
		inprocess = Pattern.compile(regex).matcher(inprocess).replaceAll(" ");
		
		regex = "<[a-zA-Z/][^>]*[^(\\.h)]>";
		inprocess = Pattern.compile(regex).matcher(inprocess).replaceAll("");
		
		return filter.getText(inprocess);
	}

}
