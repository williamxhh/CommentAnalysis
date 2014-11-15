package tool.nlpir;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import mysql.data.filter.FilterBase;

public class WordSeg {
	public static Logger logger = Logger.getLogger(WordSeg.class);
	public static int NO_POS_TAG = 0;
	public static int POS_TAG = 1;
	
	public static String transString(String aidString, String ori_encoding,
			String new_encoding) {
		try {
			return new String(aidString.getBytes(ori_encoding), new_encoding);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public WordSeg() throws Exception {
		logger.setLevel(Level.WARN);
		int charset_type = 1;
		int init_flag = CLibrary.Instance.NLPIR_Init("", charset_type, "0");
		String nativeBytes = null;

		if (0 == init_flag) {
			nativeBytes = CLibrary.Instance.NLPIR_GetLastErrorMsg();
			throw new Exception("��ʼ��ʧ�ܣ�fail reason is "+nativeBytes);
		}
		addUserDictWords();
	}
	
	private void addUserDictWords() {
		String user_dict = SystemParas.user_dict;
		for(String word: user_dict.split(" ")){
			CLibrary.Instance.NLPIR_AddUserWord(word + " n");
		}
	}
	
	/**
	 * 
	 * @param input   ���ִʵ��ַ���
	 * @param bPosTagged   �Ƿ��ע���ԣ�0��ʾ����ע��1��ʾ��ע
	 * @param filter  �Դ�����ַ������б�Ҫ�ķִ�ǰ��Ԥ�����������ҪԤ�������Դ���DoNothingFilter
	 * @return
	 */
	public List<String> segmentation(String input, int bPosTagged, FilterBase filter) {
		return segmentation(input, bPosTagged, filter, false);
	}
	
	/**
	 * 
	 * @param input   ���ִʵ��ַ���
	 * @param bPosTagged   �Ƿ��ע���ԣ�0��ʾ����ע��1��ʾ��ע
	 * @param filter  �Դ�����ַ������б�Ҫ�ķִ�ǰ��Ԥ�����������ҪԤ�������Դ���DoNothingFilter
	 * @param reserveLineBreak  �Ƿ�������
	 * @return
	 */
	public List<String> segmentation(String input, int bPosTagged, FilterBase filter, boolean reserveLineBreak) {
		List<String> result = new ArrayList<String>();
		String[] seg_result = null;
		
		logger.info("#input#:\t" + filter.getText(input));
		if(reserveLineBreak) {
			seg_result = CLibrary.Instance.NLPIR_ParagraphProcess(filter.getText(input), bPosTagged).split(" ");
		}else {
			seg_result = CLibrary.Instance.NLPIR_ParagraphProcess(filter.getText(input), bPosTagged).split("\\s");
		}
		for(String s: seg_result) {
			if(s != null && !s.equals("")){
				result.add(s.replaceAll("\\s", "<br/>"));
			} else if(reserveLineBreak) {
				result.add(" ");
			}
		}
		logger.info("#result#:\t" + result.toString());
		
		return result;
	}
	
	public void exit() {
		CLibrary.Instance.NLPIR_Exit();
	}
	
}
