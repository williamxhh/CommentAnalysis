package mysql.data.filter;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 * ������
 * ����������ᱣ����������������ַ������������ı�㣬Ӣ�ĵ�ȥ�� 
 * �������������Ѿ��Ǳ����ָ���������ַ����ˣ������������������ַ�������ԭ���н����ţ�������
 * @author dell
 *
 */
public class TemplateCandidateFilter{
//	private String regex = "^(.*[\\pP|a-zA-Z|\\s|0-9])(.*)$";
	private String regex = "[a-zA-Z|\\s|0-9|\\pP]*([^\\pPa-zA-Z\\s0-9]+)[:|��]";

	public List<String> getText(String inputStr) {
		List<String> cands = new ArrayList<String>();
		inputStr = inputStr.trim();
		Matcher m = Pattern.compile(regex).matcher(inputStr);
		while(m.find()){
			cands.add(m.group(1));
		}
		return cands;
	}
}
