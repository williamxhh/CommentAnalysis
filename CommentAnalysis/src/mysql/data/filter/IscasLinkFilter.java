package mysql.data.filter;

import java.util.regex.Pattern;

public class IscasLinkFilter implements FilterBase {
	private FilterBase filter;
	
	public IscasLinkFilter(FilterBase filter){
		this.filter = filter;
	}
	
	@Override
	public String getText(String inputStr) {
		//∆•≈‰ {| class="identref" ...|}
		String regex = "\\{\\| class=[^\\}]+\\|\\}";
		String inprocess = Pattern.compile(regex).matcher(inputStr).replaceAll("");
		
		//∆•≈‰  [http://124.16.141.166/lxr-0702/ident?v=linux-3.5.4&_i=memcpy&_remember=1&x=0&y=0]
		regex = "\\[http://[^\\]]+\\]";
		inprocess = Pattern.compile(regex).matcher(inprocess).replaceAll("");
		
		//∆•≈‰ {{#tag:imagemap ...}}
		regex = "\\{(\\s)*\\{(\\s)*#tag:imagemap[^\\}]+\\}(\\s)*\\}";
		inprocess = Pattern.compile(regex).matcher(inprocess).replaceAll("");
		
		regex = "\\{\\|[^\\}]+\\|\\}";
		inprocess = Pattern.compile(regex).matcher(inprocess).replaceAll("");
		
		return filter.getText(inprocess.trim());
	}

}
