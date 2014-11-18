package mysql.data.filter;

import java.util.Map;
import java.util.regex.Pattern;

import mysql.data.analysis.CommentAnalyzer;

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
	
	public static void main(String[] args) {
		CommentAnalyzer ca = new CommentAnalyzer(true);
		Map<String, String> allComments = ca.getAllComments();
		String path = "/mm/page_alloc.c/get_page_from_freelist(1783)(linux-3.5.4)";
		FilterBase filter = new CategoryTagFilter(new HtmlFilter(new IscasLinkFilter(new DoNothingFilter())));
//		System.out.println(filter.getText(allComments.get(path)));
		
		IscasChineseCommentExtractor cf = new IscasChineseCommentExtractor();
		for(String s: cf.getText(filter.getText(allComments.get(path)))){
			System.out.println(s);
		}
		
	}

}
