package mysql.data.analysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream.GetField;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import mysql.data.filter.TemplateCandidateFilter;
import mysql.data.util.FileUtil;
import mysql.data.util.LxrType;

public class TXTCommentAnalyzer {
	private static final String DEFAULTPATH = "D:\\research_gxw\\1_4comment\\data\\CLASSIFIED";
	private static final String DEFAULTSPLITER = "##**##";
	
	private int lxr_type;
	private String txtCommentFilePath;
	private String commentSpliter;
	
	//����ע�͵����ݣ���Ϊ ע�͵�Դ������ļ�·��  ֵΪ ע�͵�����
	private Map<String,String> comments;
	//���浱ǰ��ѡ��lxr_type���͵�����ע�����漰������ Դ�����ļ���
	private Set<String> fileset;
	
	public TXTCommentAnalyzer(int type){
		this(type,DEFAULTPATH,DEFAULTSPLITER);
	}

	public TXTCommentAnalyzer(int type,String path,String spliter){
		this.lxr_type = type;
		this.txtCommentFilePath = path;
		this.commentSpliter = spliter;
		readContentToMap();
	}
	
	/**
	 * ���ƶ����͵�ע���ļ�������ȫ�����뵽comments���map��
	 * ��Ϊ ע�͵�Դ������ļ�·��  ֵΪ ע�͵�����
	 */
	private void readContentToMap(){
		this.comments = new TreeMap<String, String>();
			
		try {
			File file = FileUtil.readableFile(this.txtCommentFilePath+"\\CLASSIFICATION_"+LxrType.getTypeName(lxr_type)+".txt");
			BufferedReader reader = new BufferedReader(
					new FileReader(file));
			String line = "";
			String key = "";
			StringBuilder content = new StringBuilder();
			while ((line = reader.readLine()) != null) {
				//������ض��ķָ�����ͷ���ͱ�ʾ��һ���µ�ע��
				if(line.startsWith(this.commentSpliter)){
					//�����ǰkey��content�ж������ݣ�����map��д��
					if(!key.equals("")){
						comments.put(key, content.toString());
						key = "";
						content = new StringBuilder();
					}
					// ȥ���Զ���ָ���##**##
					key = line.substring(this.commentSpliter.length());
				}else{
					content.append(line);
				}
			}
			//�����һ��ע�ͷ���map
			comments.put(key, content.toString());
			
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		fileset = new HashSet<String>();
		for (Map.Entry<String, String> entry : this.comments.entrySet()) {
			String key = entry.getKey();
			fileset.add(getCommentFileName(key));
		}
	}
	
	/**
	 * ��ȡ��ǰ��ѡ��lxr_type���͵�ע���ļ�����������ע����Ŀ
	 * @return
	 */
	public int getCommentsNumber(){
		return this.comments.size();
	}
	
	/**
	 * ��ȡ��ǰ��ѡ��lxr_type���͵�ע���ļ�����������ע�� ���漰��Դ�����ļ���Ŀ
	 */
	public int getFileNumber(){
		if (fileset == null) {
			fileset = new HashSet<String>();
			for (Map.Entry<String, String> entry : this.comments.entrySet()) {
				String key = entry.getKey();
				fileset.add(getCommentFileName(key));
			}
		}
		return this.fileset.size();
	}
	
	/**
	 * ����һ��linuxԴ������ļ��������ҳ���ǰ���������и��ļ���ע��
	 * @param filename	�����linuxԴ������ļ���
	 * @return   ע���б�ÿһ����һ��ע��
	 */
	public List<String> getFileComments(String filename){
		List<String> c = new ArrayList<String>();
		if(fileset.contains(filename)){
			for (Map.Entry<String, String> entry : this.comments.entrySet()) {
				if(entry.getKey().startsWith(filename)){
					c.add(entry.getValue());
				}
			}
		}
		return c;
	}
	
	/**
	 * ʶ��ǰ����ע���£������linuxԴ������ļ���������ע���У��Ƿ���ģ����ڣ���ȡ��ģ��
	 * ���ڵ�ģ����ȡ�������ȶ�ÿ��ע�����д������á������ָ���ʶ��ÿ��������ǰ������ĺ���Ϊ��ѡ��
	 * �����ѡ���е��ַ������ֵĴ��������˸�Դ�����ļ���ע��������һ�룬�����
	 * @param filename  �����linuxԴ������ļ���
	 */
	public void extractTemplate(String filename){
		//���õ����ļ�������ע��
		List<String> fileComments = this.getFileComments(filename);
		Map<String,Integer> candidateCount = new HashMap<String, Integer>();
		for(String c:fileComments){
			//��ÿ��ע�����д�����ȡ����ѡ��
			Set<String> splits = splitComments(c);
			//����ͬ��ע��ʱ�����ۼӣ�һ��ע���ڵĸ�Ƶ�ʣ������ظ�ͳ��
			for(String s:splits){
				if(candidateCount.containsKey(s)){
					candidateCount.put(s, candidateCount.get(s)+1);
				}else{
					candidateCount.put(s, 1);
				}
			}
		}
		int count = fileComments.size();
		for(Map.Entry<String, Integer> entry:candidateCount.entrySet()){
			if(entry.getValue()>=count/2){
				System.out.println(entry.getKey());
			}
		}
	}
	
	/**
	 * ��Ϊһ��comment���ܰ������У��˺�����comment���д���ÿһ�����ж��ã��ָ��Ժ�Ľ���������set���������Ա�֤��һ��ע���ڵĸ�Ƶ�ʣ������ظ�ͳ��
	 * @param comment
	 * @return
	 */
	private static Set<String> splitComments(String comment){
		Set<String> splits = new HashSet<String>();
		//ʹ����һ��������������������ᱣ����������������ַ������������ı�㣬Ӣ�ĵ�ȥ��  �������������Ѿ��Ǳ����ָ���������ַ����ˣ������������������ַ�������ԭ���н����ţ�������
		TemplateCandidateFilter filter = new TemplateCandidateFilter();
		String[] lines = comment.split("\n");
		for(String line:lines){
			line = line.trim();
			//����ð�Ż���Ӣ��ð�Ŷ�Ҫ����
			String[] sp = line.split("��|:");
			for(String s:sp){
				s = filter.getText(s);
				if(s.length()>0)
					splits.add(s);
			}
		}
		return splits;
	}
	
	/**
	 * ��ȡһ��title������ʵ���漰��Դ�����ļ���
	 * @param line   һ��title��������   /virt/kvm/kvm_main.c/kvm_set_pfn_dirty(1258)(linux-3.5.4)
	 * @return     Դ�����ļ���  �� /virt/kvm/kvm_main.c
	 */
	private String getCommentFileName(String line){
		if(this.lxr_type!=LxrType.file){
			return line.substring(0, line.lastIndexOf("/"));
		}
		return line;
	}
	
	public static void main(String[] args) {
		TXTCommentAnalyzer a = new TXTCommentAnalyzer(LxrType.function_definition);
		System.out.println(a.getCommentsNumber());
		System.out.println(a.getFileNumber());
		
//		String file = "/kernel/trace/trace.c";
//		
//		for(String c:a.getFileComments(file)){
//			System.out.println(c);
//		}
//		System.out.println("########################");
//		
//		a.extractTemplate(file);
		
		for(String file:a.fileset){
			System.out.println(file+":"+a.getFileComments(file).size());
			System.out.println("########################");
			a.extractTemplate(file);
//			for(String c:a.getFileComments(file)){
//				System.out.println(c);
//			}
		}
	}
	
}
