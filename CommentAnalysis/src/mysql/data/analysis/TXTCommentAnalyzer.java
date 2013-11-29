package mysql.data.analysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import mysql.data.filter.TemplateCandidateFilter;
import mysql.data.util.FileUtil;
import mysql.data.util.LxrType;

public class TXTCommentAnalyzer {
	public static final String DEFAULTPATH = "D:\\research_gxw\\1_4comment\\data\\CLASSIFIED";
	public static final String DEFAULTSPLITER = "##**##";
	private static final Logger log = Logger.getLogger(TXTCommentAnalyzer.class);
	
	/**
	 * ��ȡģ����Ե��ϸ񼶱�
	 * strict���ԣ�����ж�������ע�ͣ���ô������һ���ע���г��֣����ֻ������ע�ͣ���ô�ͱ���2����ȫ������      ����֤һ����׼ȷ�ʣ������ٻ��ʲ���֤��������ģ��δʶ�������
	 * middle���ԣ�����ע���������٣���ֻҪ��һ����Ŀ��ע���г��ּ��ɣ�����һ��ע�͵�������϶���ʶ������ˣ�������ע�͵������ֻҪ��һ���г��־�ʶ��  
	 * loose���ԣ����г�����ð��ǰ�������ʶ��     ���ٻ��ʸߣ����ǿ���׼ȷ���½���ֻҪ��ð��ǰ������ĺ��֣���ʶ���Ϊģ���ˡ�
	 */
	private static final int EXTRACTPOLICY_STRICT=1;
	private static final int EXTRACTPOLICY_MIDDLE=2;
	private static final int EXTRACTPOLICY_LOOSE=3;
	
	private int lxr_type;
	private String txtCommentFilePath;
	private String commentSpliter;
	private PrintWriter txtWriter;
	private PrintWriter csvWriter;
	private PrintWriter noTemplateCommentWriter;
	//��map����ע���ļ���ģ����Ϣ����Ϊ�ļ�����ֵΪģ�幹�ɵļ��ϡ�ע�⣬��������ļ�������Ϊģ���ǻ����ļ������ֵģ�����һ��ע�͵�ע��·����
	private Map<String,Set<String>> fileTemplates;
	
	
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
		this.fileTemplates = new HashMap<String,Set<String>>();
		
		try {
			this.txtWriter = new PrintWriter(FileUtil.writeableFile(this.txtCommentFilePath+"\\txt\\"+LxrType.getTypeName(lxr_type)+".txt"));
			this.csvWriter = new PrintWriter(FileUtil.writeableFile(this.txtCommentFilePath+"\\csv\\"+LxrType.getTypeName(lxr_type)+".csv"));
			this.noTemplateCommentWriter = new PrintWriter(FileUtil.writeableFile(this.txtCommentFilePath+"\\noTemplate\\"+LxrType.getTypeName(lxr_type)+".txt"));
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		try {
			File file = FileUtil.readableFile(this.txtCommentFilePath+"\\CLASSIFICATION_"+LxrType.getTypeName(lxr_type)+".txt");
			this.comments = readContentToMap(file,this.commentSpliter);
			this.fileset = getAllCommentedFiles(this.comments);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	public TXTCommentAnalyzer(File file){
		this(file,DEFAULTSPLITER);
	}
	
	//����δ������ļ�
	public TXTCommentAnalyzer(File file,String spliter){
		this.lxr_type = -1;
		this.txtCommentFilePath="";
		this.commentSpliter = spliter;
		this.comments = readContentToMap(file,this.commentSpliter);
		this.fileset = getAllCommentedFiles(this.comments);
		this.fileTemplates = new HashMap<String,Set<String>>();
		try {
			this.txtWriter = new PrintWriter(FileUtil.writeableFile(file.getParent()+"\\txt\\allfiltered.txt"));
			this.csvWriter = new PrintWriter(FileUtil.writeableFile(file.getParent()+"\\csv\\allfiltered.csv"));
			this.noTemplateCommentWriter = new PrintWriter(FileUtil.writeableFile(this.txtCommentFilePath+"\\noTemplate\\noTemplateComments.txt"));
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	
	public Map<String, Set<String>> getFileTemplates() {
		return fileTemplates;
	}

	public Map<String, String> getComments() {
		return comments;
	}

	public Set<String> getFileset() {
		return fileset;
	}

	public void closeWriter(){
		if(this.txtWriter!=null){
			this.txtWriter.flush();
			this.txtWriter.close();
			this.txtWriter=null;
		}
		if(this.csvWriter!=null){
			this.csvWriter.flush();
			this.csvWriter.close();
			this.csvWriter=null;
		}
		if(this.noTemplateCommentWriter!=null){
			this.noTemplateCommentWriter.flush();
			this.noTemplateCommentWriter.close();
			this.noTemplateCommentWriter = null;
		}
	}
	/**
	 * ���ƶ����͵�ע���ļ�������ȫ�����뵽comments���map��
	 * ��Ϊ ע�͵�Դ������ļ�·��  ֵΪ ע�͵�����
	 */
	public Map<String,String> readContentToMap(File file,String commentSpliter){
		Map<String,String> allComments = new TreeMap<String, String>();
		try {
			BufferedReader reader = new BufferedReader(
					new FileReader(file));
			String line = "";
			String key = "";
			StringBuilder content = new StringBuilder();
			while ((line = reader.readLine()) != null) {
				//������ض��ķָ�����ͷ���ͱ�ʾ��һ���µ�ע��
				if(line.startsWith(commentSpliter)){
					//�����ǰkey��content�ж������ݣ�����map��д��
					if(!key.equals("")){
						allComments.put(key, content.toString());
						key = "";
						content = new StringBuilder();
					}
					// ȥ���Զ���ָ���##**##
					key = line.substring(commentSpliter.length());
				}else{
					content.append(line+"\n");
				}
			}
			//�����һ��ע�ͷ���map
			allComments.put(key, content.toString());
			
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return allComments;
	}
	
	public Set<String> getAllCommentedFiles(Map<String,String> allComments){
		Set<String> fileset = new HashSet<String>();
		for (Map.Entry<String, String> entry : allComments.entrySet()) {
			String key = entry.getKey();
			fileset.add(getCommentFileName(key));
		}
		return fileset;
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
					c.add(entry.getValue().trim());
				}
			}
		}
		return c;
	}
	
	public void extractTemplate(String filename,boolean outputTemplate){
		extractTemplate(filename, outputTemplate,TXTCommentAnalyzer.EXTRACTPOLICY_STRICT);
	}
	
	
	/**
	 * ʶ��ǰ����ע���£������linuxԴ������ļ���������ע���У��Ƿ���ģ����ڣ���ȡ��ģ��
	 * ���ڵ�ģ����ȡ�������ȶ�ÿ��ע�����д������á������ָ���ʶ��ÿ��������ǰ������ĺ���Ϊ��ѡ��
	 * �����ѡ���е��ַ������ֵĴ��������˸�Դ�����ļ���ע��������һ�룬�����
	 * @param filename  �����linuxԴ������ļ���
	 * @param outputTemplate �Ƿ�ģ����Ϣ������ļ�
	 */
	public void extractTemplate(String filename,boolean outputTemplate,int extractPolicy){
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
		Set<String> templates = new HashSet<String>();
		int count = fileComments.size();
		for(Map.Entry<String, Integer> entry:candidateCount.entrySet()){
			if(extractPolicy==TXTCommentAnalyzer.EXTRACTPOLICY_STRICT){
				if(entry.getValue()>=2&&entry.getValue()>=count/2){
					templates.add(entry.getKey());
				}
			}else if(extractPolicy==TXTCommentAnalyzer.EXTRACTPOLICY_MIDDLE){
				if(entry.getValue()>=count/2){
					templates.add(entry.getKey());
				}
			}else if(extractPolicy==TXTCommentAnalyzer.EXTRACTPOLICY_LOOSE){
				templates.add(entry.getKey());
			}
		}
		if(outputTemplate){
			outputTemplateToFile(filename, count, templates);
		}
		if(templates.size()!=0){
			this.fileTemplates.put(filename, templates);
			log.info(filename+":"+templates);
			for(String comment:fileComments){
				log.info("******************************************");
				log.info("##ԭʼע��Ϊ��");
				log.info(comment);
				log.info(templates);
				
				log.info("##��ϴ�Ժ��ע��Ϊ��");
				log.info(removeTemplateFromComment(comment, templates));
			}
		}
	}
	
	private String removeTemplateFromComment(String comment,Set<String> templates){
		comment = comment.trim();
		for(String template:templates){
			comment = comment.replaceAll("n?[[\\s|\\pP]&&[^\\n]]*"+template+"(\\s)*[:|��]", "");
		}
		return comment;
	}

	/**
	 *  * ����ȡ����ע��ģ��������ļ�
	 * @param filename   Դ�����ļ�·��
	 * @param count   ������ע������
	 * @param templates  ��ȡ������ģ��
	 */
	private void outputTemplateToFile(String filename,int count, Set<String> templates) {
		
		this.txtWriter.write("Դ�����ļ�·��: "+filename+"\t"+"������ע��������"+count+"\r\n");
		this.csvWriter.write(filename+","+count+",");
		for(String c:templates){
			this.txtWriter.write(c+"\r\n");
			this.csvWriter.write(c+",");
		}
		this.txtWriter.write("###################\r\n\r\n");
		this.csvWriter.write("\r\n");
	}
	
	/**
	 * ��ÿ��ע�����д�����ȡ����ѡ��
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
	public String getCommentFileName(String line){
		if(this.lxr_type!=LxrType.file){
			return line.substring(0, line.lastIndexOf("/"));
		}
		return line;
	}
	
	/**
	 * ��ע����Ϣ����ģ���Ժ�������ļ�
	 * @param allComments   ���е�ע�����ݹ��ɵ�map����Ϊע��·����ֵΪע������
	 * @param fileTemplates    ���е�ģ�幹�ɵ�map����ΪԴ�����ļ�����ֵΪģ�弯��
	 * @param writer    ����õ�writer
	 * @param commentSpliter   ÿ��ע��ǰ�����Զ���ķָ��� 
	 */
	public void outputNoTemplateCommentToFile(Map<String,String> allComments,Map<String,Set<String>> fileTemplates,PrintWriter writer,String commentSpliter){
		for(Map.Entry<String, String> entry:allComments.entrySet()){
			//�ȸ��ݱ�ע�͵�·������ȡ��ע�͵��ļ���
			String fileName = getCommentFileName(entry.getKey());
			writer.write(commentSpliter+entry.getKey().trim()+"\r\n");
			//����ļ�ģ���У���ǰ�ļ�����ģ�壬�ͶԵ�ǰע�͹���ģ���Ժ����������Ͷ�ע�ͽ���ԭ�����
			if(fileTemplates.containsKey(fileName)){
				writer.write(removeTemplateFromComment(entry.getValue(), fileTemplates.get(fileName)).trim()+"\r\n");
			}else{
				writer.write(entry.getValue().trim()+"\r\n");
			}
			writer.write("\r\n");
		}
	}
	
	
	public static void main(String[] args) throws IOException{
//		File file = FileUtil.readableFile("D:\\research_gxw\\1_4comment\\data\\filteredComment.txt");
//		TXTCommentAnalyzer a = new TXTCommentAnalyzer(file);
		log.setLevel(Level.INFO);
		for(int lxrtype : LxrType.getTypeValues()){
			TXTCommentAnalyzer a = new TXTCommentAnalyzer(lxrtype);
			//		System.out.println(a.getCommentsNumber());
			//		System.out.println(a.getFileNumber());

			boolean outputTemplate = false;
			boolean outputNoTemplateComments = false;
			for(String f:a.fileset){
				//			System.out.println(file+":"+a.getFileComments(file).size());
				//			System.out.println("########################");
				a.extractTemplate(f,outputTemplate,TXTCommentAnalyzer.EXTRACTPOLICY_STRICT);
			}
			if(outputNoTemplateComments){
				a.outputNoTemplateCommentToFile(a.comments, a.fileTemplates, a.noTemplateCommentWriter, a.commentSpliter);
			}

			a.closeWriter();
		}
		System.out.println("done");
	}
	
}
