package mysql.data.filter;

import java.util.regex.Pattern;

public class CategoryTagFilter implements FilterBase{
	
	private FilterBase filter;
	
	public CategoryTagFilter(FilterBase filter){
		this.filter = filter;
	}

	@Override
	public String getText(String inputStr) {
		// �������������ݣ�   [[Category:Linux��ϵ�ṹ���ں˹���ģ����ϵ����|3.5.4)]]
		String regex = "\\[\\[Category:[^\\]]*\\]\\]";
		String inprocess = Pattern.compile(regex).matcher(inputStr).replaceAll("");
		
		//�������������ݣ�     *[[/arch/arm/boot/compressed/ofw-shark.c/create_params(0020)(linux-3.5.4)|create_params(0020)]]
		regex = "\\*\\[\\[/[^\\]]*(linux-3.5.4)[^\\]]*\\]\\]";
		inprocess = Pattern.compile(regex).matcher(inprocess).replaceAll("");
		
		//���˶�ʧ��File���     [[File:]]
		regex = "\\[\\[File[:|��].*\\]\\]";
		inprocess = Pattern.compile(regex).matcher(inprocess).replaceAll("");
		
		return filter.getText(inprocess.trim());
	}

}
