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
			System.err.println("初始化失败！fail reason is "+nativeBytes);
			return;
		}

//		String sInput = "据 悉，质检总局已将最新有关情况再次通报美方，要求美方加强对输华玉米的产地来源、运输及仓储等环节的管控措施，有效避免输华玉米被未经我国农业部安全评估并批准的转基因品系污染。";
//		String sInput = "定义宏PORT86CR。\r\n宏功能：定义一个指向一个I/O的内存空间的地址__iomem为0xe6050056。";
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

//			System.out.println("分词结果为： " + nativeBytes);
//			
//			CLibrary.Instance.NLPIR_AddUserWord("要求美方加强对输 n");
//			CLibrary.Instance.NLPIR_AddUserWord("华玉米的产地来源 n");
//			nativeBytes = CLibrary.Instance.NLPIR_ParagraphProcess(sInput, 1);
//			System.out.println("增加用户词典后分词结果为： " + nativeBytes);
//			
//			CLibrary.Instance.NLPIR_DelUsrWord("要求美方加强对输");
//			nativeBytes = CLibrary.Instance.NLPIR_ParagraphProcess(sInput, 1);
//			System.out.println("删除用户词典后分词结果为： " + nativeBytes);
//			
//			
//			int nCountKey = 0;
//			String nativeByte = CLibrary.Instance.NLPIR_GetKeyWords(sInput, 10,false);
//
//			System.out.print("关键词提取结果是：" + nativeByte);
//
//			nativeByte = CLibrary.Instance.NLPIR_GetFileKeyWords("D:\\NLPIR\\feedback\\huawei\\5341\\5341\\产经广场\\2012\\5\\16766.txt", 10,false);
//
//			System.out.print("关键词提取结果是：" + nativeByte);

			

			CLibrary.Instance.NLPIR_Exit();

		} catch (Exception ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
		}

	}
}
