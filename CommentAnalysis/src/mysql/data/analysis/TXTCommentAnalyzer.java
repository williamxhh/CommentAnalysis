package mysql.data.analysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream.GetField;
import java.io.PrintWriter;
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
	private PrintWriter txtWriter;
	private PrintWriter csvWriter;
	
	
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
		
		try {
			this.txtWriter = new PrintWriter(FileUtil.writeableFile(this.txtCommentFilePath+"\\txt\\"+LxrType.getTypeName(lxr_type)+".txt"));
			this.csvWriter = new PrintWriter(FileUtil.writeableFile(this.txtCommentFilePath+"\\csv\\"+LxrType.getTypeName(lxr_type)+".csv"));
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		try {
			File file = FileUtil.readableFile(this.txtCommentFilePath+"\\CLASSIFICATION_"+LxrType.getTypeName(lxr_type)+".txt");
			readContentToMap(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	public TXTCommentAnalyzer(File file){
		this(file,DEFAULTSPLITER);
	}
	
	public TXTCommentAnalyzer(File file,String spliter){
		this.lxr_type = -1;
		this.txtCommentFilePath="";
		this.commentSpliter = spliter;
		this.readContentToMap(file);
		
		try {
			this.txtWriter = new PrintWriter(FileUtil.writeableFile(file.getParent()+"\\txt\\allfiltered.txt"));
			this.csvWriter = new PrintWriter(FileUtil.writeableFile(file.getParent()+"\\csv\\allfiltered.csv"));
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	
	
	public void closeWriter(){
		if(this.txtWriter!=null){
			this.txtWriter.close();
			this.txtWriter=null;
		}
		if(this.csvWriter!=null){
			this.csvWriter.close();
			this.csvWriter=null;
		}
	}
	/**
	 * ���ƶ����͵�ע���ļ�������ȫ�����뵽comments���map��
	 * ��Ϊ ע�͵�Դ������ļ�·��  ֵΪ ע�͵�����
	 */
	private void readContentToMap(File file){
		this.comments = new TreeMap<String, String>();
		try {
			
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
					content.append(line+"\n");
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
		this.txtWriter.write("Դ�����ļ�·��: "+filename+"\t"+"������ע��������"+count+"\r\n");
		this.csvWriter.write(filename+","+count+",");
		for(Map.Entry<String, Integer> entry:candidateCount.entrySet()){
			/*
			 * ����ģ����ֵ����޴�����Ϊ2��˼�����һ���ļ��ܹ�������ע�͵Ļ����ͱ�ȥȫ������ģ�壬��ʶ�����
			 * ����ж�������ע�ͣ���ô������һ���ע���г���
			 */
			if(entry.getValue()>=2&&entry.getValue()>=count/2){
				String c = entry.getKey();
//				System.out.println(c);
				this.txtWriter.write(c+"\r\n");
				this.csvWriter.write(c+",");
			}
		}
		this.txtWriter.write("###################\r\n\r\n");
		this.csvWriter.write("\r\n");
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
			List<String> cands = filter.getText(line);
			for(String c:cands){
				if(c.length()>0){
					splits.add(c);
				}
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
	
	public static void main(String[] args) throws IOException{
//		File file = FileUtil.readableFile("D:\\research_gxw\\1_4comment\\data\\filteredComment.txt");
//		TXTCommentAnalyzer a = new TXTCommentAnalyzer(file);
		TXTCommentAnalyzer a = new TXTCommentAnalyzer(LxrType.variable_definition);
		System.out.println(a.getCommentsNumber());
		System.out.println(a.getFileNumber());
		
		for(String f:a.fileset){
//			System.out.println(file+":"+a.getFileComments(file).size());
//			System.out.println("########################");
			a.extractTemplate(f);
		}
		a.closeWriter();
	}
	
}
