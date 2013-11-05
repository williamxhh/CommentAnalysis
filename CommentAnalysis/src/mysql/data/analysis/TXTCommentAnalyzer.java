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
	
	//保存注释的内容，键为 注释的源代码的文件路径  值为 注释的内容
	private Map<String,String> comments;
	//保存当前所选的lxr_type类型的所有注释所涉及的所有 源代码文件名
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
	 * 将制定类型的注释文件的内容全部读入到comments这个map中
	 * 键为 注释的源代码的文件路径  值为 注释的内容
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
				//如果以特定的分隔符开头，就表示是一条新的注释
				if(line.startsWith(this.commentSpliter)){
					//如果当前key和content中都有内容，就向map中写入
					if(!key.equals("")){
						comments.put(key, content.toString());
						key = "";
						content = new StringBuilder();
					}
					// 去掉自定义分隔符##**##
					key = line.substring(this.commentSpliter.length());
				}else{
					content.append(line);
				}
			}
			//将最后一条注释放入map
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
	 * 获取当前所选的lxr_type类型的注释文件中所包含的注释数目
	 * @return
	 */
	public int getCommentsNumber(){
		return this.comments.size();
	}
	
	/**
	 * 获取当前所选的lxr_type类型的注释文件中所包含的注释 所涉及的源代码文件数目
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
	 * 输入一个linux源代码的文件名，查找出当前分类下所有该文件的注释
	 * @param filename	传入的linux源代码的文件名
	 * @return   注释列表，每一条是一条注释
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
	 * 识别当前类别的注释下，传入的linux源代码的文件名的所有注释中，是否有模板存在，提取出模板
	 * 现在的模板提取策略是先对每条注释逐行处理，先用“：”分隔，识别每个“：”前面的中文汉字为候选集
	 * 如果候选集中的字符串出现的次数超过了该源代码文件总注释条数的一半，则输出
	 * @param filename  传入的linux源代码的文件名
	 */
	public void extractTemplate(String filename){
		//先拿到该文件的所有注释
		List<String> fileComments = this.getFileComments(filename);
		Map<String,Integer> candidateCount = new HashMap<String, Integer>();
		for(String c:fileComments){
			//对每条注释逐行处理，抽取出候选集
			Set<String> splits = splitComments(c);
			//处理不同的注释时，才累加，一条注释内的高频词，不会重复统计
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
	 * 因为一条comment可能包含多行，此函数将comment逐行处理，每一行再判断用：分隔以后的结果，最后用set保存结果，以保证在一条注释内的高频词，不会重复统计
	 * @param comment
	 * @return
	 */
	private static Set<String> splitComments(String comment){
		Set<String> splits = new HashSet<String>();
		//使用了一个过滤器，这个过滤器会保留最后连续的中文字符串，将其他的标点，英文等去掉  传给过滤器的已经是被：分隔处理过了字符串了，所以最后的连续中文字符串就是原文中紧挨着：的描述
		TemplateCandidateFilter filter = new TemplateCandidateFilter();
		String[] lines = comment.split("\n");
		for(String line:lines){
			line = line.trim();
			//中文冒号或者英文冒号都要采用
			String[] sp = line.split("：|:");
			for(String s:sp){
				s = filter.getText(s);
				if(s.length()>0)
					splits.add(s);
			}
		}
		return splits;
	}
	
	/**
	 * 获取一个title描述所实际涉及的源代码文件名
	 * @param line   一个title描述，如   /virt/kvm/kvm_main.c/kvm_set_pfn_dirty(1258)(linux-3.5.4)
	 * @return     源代码文件名  如 /virt/kvm/kvm_main.c
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
