package mysql.data.filter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 * ������
 * ����������ᱣ����������������ַ������������ı�㣬Ӣ�ĵ�ȥ�� 
 * �������������Ѿ��Ǳ����ָ���������ַ����ˣ������������������ַ�������ԭ���н����ţ�������
 * @author dell
 *
 */
public class TemplateCandidateFilter implements FilterBase {
	private String regex = "^(.*[\\pP|a-zA-Z|\\s|0-9])(.*)$";

	@Override
	public String getText(String inputStr) {
		inputStr = inputStr.trim();
		Matcher m = Pattern.compile(regex).matcher(inputStr);
		if (m.matches()) {
			return m.group(2);
		}
		return inputStr;
	}
}
