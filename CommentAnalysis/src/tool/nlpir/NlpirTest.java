package tool.nlpir;

import java.util.Map;

import mysql.data.analysis.CommentAnalyzer;
import mysql.data.filter.CategoryTagFilter;
import mysql.data.filter.DoNothingFilter;
import mysql.data.filter.FilterBase;
import mysql.data.filter.HtmlFilter;

public class NlpirTest {


	public static void main(String[] args) throws Exception {
		String argu = "";
		// String system_charset = "GBK";//GBK----0
		String system_charset = "UTF-8";
		int charset_type = 1;
		
		int init_flag = CLibrary.Instance.NLPIR_Init(argu, charset_type, "0");
		String nativeBytes = null;

		if (0 == init_flag) {
			nativeBytes = CLibrary.Instance.NLPIR_GetLastErrorMsg();
			System.err.println("��ʼ��ʧ�ܣ�fail reason is "+nativeBytes);
			return;
		}

//		String sInput = "�� Ϥ���ʼ��ܾ��ѽ������й�����ٴ�ͨ��������Ҫ��������ǿ���仪���׵Ĳ�����Դ�����估�ִ��Ȼ��ڵĹܿش�ʩ����Ч�����仪���ױ�δ���ҹ�ũҵ����ȫ��������׼��ת����Ʒϵ��Ⱦ��";
//		String sInput = "�����PORT86CR��\r\n�깦�ܣ�����һ��ָ��һ��I/O���ڴ�ռ�ĵ�ַ__iomemΪ0xe6050056��";
		CommentAnalyzer ca = new CommentAnalyzer(true);
		Map<String, String> allComments = ca.getAllComments();
		
		String path = "/arch/arm/kernel/perf_event_xscale.c/XSCALE1_COUNT1_INT_EN(0177)(linux-3.5.4)";
		
		String sInput = allComments.get(path);
		FilterBase filter = new CategoryTagFilter(new HtmlFilter(new DoNothingFilter()));
		//String nativeBytes = null;
		try {
			nativeBytes = CLibrary.Instance.NLPIR_ParagraphProcess(filter.getText(sInput), 0);
			System.out.println(nativeBytes);
			for(String s : nativeBytes.split(" ")){
				if(s.equals("")){
					continue;
				}
				System.out.print(s + "  ");
			}

//			System.out.println("�ִʽ��Ϊ�� " + nativeBytes);
//			
//			CLibrary.Instance.NLPIR_AddUserWord("Ҫ��������ǿ���� n");
//			CLibrary.Instance.NLPIR_AddUserWord("�����׵Ĳ�����Դ n");
//			nativeBytes = CLibrary.Instance.NLPIR_ParagraphProcess(sInput, 1);
//			System.out.println("�����û��ʵ��ִʽ��Ϊ�� " + nativeBytes);
//			
//			CLibrary.Instance.NLPIR_DelUsrWord("Ҫ��������ǿ����");
//			nativeBytes = CLibrary.Instance.NLPIR_ParagraphProcess(sInput, 1);
//			System.out.println("ɾ���û��ʵ��ִʽ��Ϊ�� " + nativeBytes);
//			
//			
//			int nCountKey = 0;
//			String nativeByte = CLibrary.Instance.NLPIR_GetKeyWords(sInput, 10,false);
//
//			System.out.print("�ؼ�����ȡ����ǣ�" + nativeByte);
//
//			nativeByte = CLibrary.Instance.NLPIR_GetFileKeyWords("D:\\NLPIR\\feedback\\huawei\\5341\\5341\\�����㳡\\2012\\5\\16766.txt", 10,false);
//
//			System.out.print("�ؼ�����ȡ����ǣ�" + nativeByte);

			

			CLibrary.Instance.NLPIR_Exit();

		} catch (Exception ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
		}

	}
}
