package mysql.data.filter;

import java.util.regex.Pattern;

public class CategoryTagFilter implements FilterBase{
	
	private FilterBase filter;
	
	public CategoryTagFilter(FilterBase filter){
		this.filter = filter;
	}

	@Override
	public String getText(String inputStr) {
		// 过滤这样的内容：   [[Category:Linux体系结构及内核功能模块间关系分析|3.5.4)]]
		String regex = "\\[\\[Category:[^\\]]*\\]\\]";
		String inprocess = Pattern.compile(regex).matcher(inputStr).replaceAll("");
		
		//过滤这样的内容：     *[[/arch/arm/boot/compressed/ofw-shark.c/create_params(0020)(linux-3.5.4)|create_params(0020)]]
		regex = "\\*\\[\\[/[^\\]]*(linux-3.5.4)[^\\]]*\\]\\]";
		inprocess = Pattern.compile(regex).matcher(inprocess).replaceAll("");
		
		//过滤丢失的File标记     [[File:]]
		regex = "\\[\\[File[:|：].*\\]\\]";
		inprocess = Pattern.compile(regex).matcher(inprocess).replaceAll("");
		
		return filter.getText(inprocess.trim());
	}

}
