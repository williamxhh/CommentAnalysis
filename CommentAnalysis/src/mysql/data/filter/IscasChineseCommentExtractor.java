package mysql.data.filter;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IscasChineseCommentExtractor {
	//ƥ�����ע��/* */
//	private String regex1 = "/\\*(\\s)*([[0-9a-zA-Z<>=+\\-\\*/][\u4e00-\u9fa5]\\pP]+)(\\s)*\\*/";
	//ƥ�䵥��ע�� //
//	private String regex2 = "//(\\s)*([[0-9a-zA-Z<>=+\\-\\*/][\u4e00-\u9fa5]\\pP]+)(\\s)*";
	
	//ƥ��������ĵ���
	private String regex3 = "(\\s)*([[0-9a-zA-Z<>=+\\-\\*/][\u4e00-\u9fa5]\\pP]+)(\\s)*";
	
	private String chinese = "[\u4e00-\u9fa5]";

	public List<String> getText(String inputStr) {
		List<String> result = new ArrayList<String>();
//		List<String> line_by_line_comments = new ArrayList<String>();
		List<String> comments = new ArrayList<String>();
		inputStr = inputStr.replaceAll(" ", "");
		
		Matcher m = Pattern.compile(chinese).matcher(inputStr);
		
		//����������ģ�����Ϊ������ȫճ����ԭʼ����
		
		if(m.find()){
//			Matcher m1 = Pattern.compile(regex1).matcher(inputStr);
//			Matcher m2 = Pattern.compile(regex2).matcher(inputStr);
			Matcher m3 = Pattern.compile(regex3).matcher(inputStr);
			
//			while (m1.find()) {
//				if (!Pattern.matches("^[a-zA-Z0-9\\pP<>]*$", m1.group(2)))
//					line_by_line_comments.add(m1.group(2).replaceAll("[\\*|/]", ""));
//			}
//			
//			while (m2.find()) {
//				if (!Pattern.matches("^[a-zA-Z0-9\\pP<>]*$", m2.group(2)))
//					line_by_line_comments.add(m2.group(2));
//			}
			
			//ǰ���Ѿ�ȷ���˰������ģ��������ͨ��ǰ����ģʽ��ƥ�䶼û���ҵ��Ļ�����֤�����ǰ���׼��ע�͸�ʽ//����/* */д�ģ��ͽ��������ĵ��з���
			while (m3.find()) {
				if (!Pattern.matches("^[a-zA-Z0-9\\pP<>]*$", m3.group(2)) && !comments.contains(m3.group(2)))
					comments.add(m3.group(2));
			}
			
//			int index1 = 0;
			int index2 = 0;
			
//			while(index1 < line_by_line_comments.size() && index2 < comments.size()) {
//				String s1 = line_by_line_comments.get(index1);
//				String s2 = comments.get(index2);
//				
//				if(!Pattern.compile(chinese).matcher(s1).find()){
//					++index1;
//					continue;
//				}
//				
//				if(!Pattern.compile(chinese).matcher(s2).find()) {
//					++index2;
//					continue;
//				}
//				
//				if(s2.contains(s1)) {
//					result.add(s1);
//					++index1;
//					++index2;
//				} else {
//					result.add(s2);
//					++index2;
//				}
//				
//			}
			
			while(index2 < comments.size()) {
				String s2 = comments.get(index2);
				if(Pattern.compile(chinese).matcher(s2).find()) {
					s2 = s2.replaceAll("/\\*", "");
					s2 = s2.replaceAll("//", "");
					s2 = s2.replaceAll("\\*/", "");
					result.add(s2);
				}
				++index2;
			}
		}
		
		
		
		
		return result;
	}
}
