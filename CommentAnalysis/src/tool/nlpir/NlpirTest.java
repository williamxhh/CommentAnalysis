package tool.nlpir;

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
		String sInput = "�����PORT86CR���깦�ܣ�����һ��ָ��һ��I/O���ڴ�ռ�ĵ�ַ__iomemΪ0xe6050056��";

		//String nativeBytes = null;
		try {
			nativeBytes = CLibrary.Instance.NLPIR_ParagraphProcess(sInput, 0);
			System.out.println(nativeBytes);
			for(String s : nativeBytes.split("\\s")){
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
