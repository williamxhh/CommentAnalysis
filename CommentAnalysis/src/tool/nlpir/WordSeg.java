package tool.nlpir;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import mysql.data.filter.FilterBase;

public class WordSeg {
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
		int charset_type = 1;
		int init_flag = CLibrary.Instance.NLPIR_Init("", charset_type, "0");
		String nativeBytes = null;

		if (0 == init_flag) {
			nativeBytes = CLibrary.Instance.NLPIR_GetLastErrorMsg();
			throw new Exception("初始化失败！fail reason is "+nativeBytes);
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
	 * @param input   待分词的字符串
	 * @param bPosTagged   是否标注词性，0表示不标注，1表示标注
	 * @param filter  对传入的字符串进行必要的分词前的预处理，如果不需要预处理，可以传入DoNothingFilter
	 * @return
	 */
	public List<String> segmentation(String input, int bPosTagged, FilterBase filter) {
		List<String> result = new ArrayList<String>();
		for(String s: CLibrary.Instance.NLPIR_ParagraphProcess(filter.getText(input), bPosTagged).split("\\s")) {
			if(s != null && !s.equals(""))
				result.add(s);
		}
		return result;
	}
	
	public void exit() {
		CLibrary.Instance.NLPIR_Exit();
	}
	
}
