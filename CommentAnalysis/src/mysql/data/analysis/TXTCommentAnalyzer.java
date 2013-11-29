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
	 * 抽取模板策略的严格级别：
	 * strict策略：如果有多于两条注释，那么至少在一半的注释中出现；如果只有两条注释，那么就必须2条中全部出现      （保证一定的准确率，但是召回率不保证，可能有模板未识别出来）
	 * middle策略：不论注释条数多少，都只要在一半数目的注释中出现即可（即对一条注释的情况，肯定都识别出来了，对两条注释的情况，只要在一条中出现就识别）  
	 * loose策略：所有出现在冒号前的情况都识别     （召回率高，但是可能准确率下降，只要用冒号前面的中文汉字，都识别成为模板了。
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
	//此map保存注释文件的模板信息，键为文件名，值为模板构成的集合。注意，这个键是文件名，因为模板是基于文件级别发现的，不是一条注释的注释路径。
	private Map<String,Set<String>> fileTemplates;
	
	
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
	
	//处理未分类的文件
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
	 * 将制定类型的注释文件的内容全部读入到comments这个map中
	 * 键为 注释的源代码的文件路径  值为 注释的内容
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
				//如果以特定的分隔符开头，就表示是一条新的注释
				if(line.startsWith(commentSpliter)){
					//如果当前key和content中都有内容，就向map中写入
					if(!key.equals("")){
						allComments.put(key, content.toString());
						key = "";
						content = new StringBuilder();
					}
					// 去掉自定义分隔符##**##
					key = line.substring(commentSpliter.length());
				}else{
					content.append(line+"\n");
				}
			}
			//将最后一条注释放入map
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
	 * 识别当前类别的注释下，传入的linux源代码的文件名的所有注释中，是否有模板存在，提取出模板
	 * 现在的模板提取策略是先对每条注释逐行处理，先用“：”分隔，识别每个“：”前面的中文汉字为候选集
	 * 如果候选集中的字符串出现的次数超过了该源代码文件总注释条数的一半，则输出
	 * @param filename  传入的linux源代码的文件名
	 * @param outputTemplate 是否将模板信息输出到文件
	 */
	public void extractTemplate(String filename,boolean outputTemplate,int extractPolicy){
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
				log.info("##原始注释为：");
				log.info(comment);
				log.info(templates);
				
				log.info("##清洗以后的注释为：");
				log.info(removeTemplateFromComment(comment, templates));
			}
		}
	}
	
	private String removeTemplateFromComment(String comment,Set<String> templates){
		comment = comment.trim();
		for(String template:templates){
			comment = comment.replaceAll("n?[[\\s|\\pP]&&[^\\n]]*"+template+"(\\s)*[:|：]", "");
		}
		return comment;
	}

	/**
	 *  * 将抽取出的注释模板输出到文件
	 * @param filename   源代码文件路径
	 * @param count   包含的注释条数
	 * @param templates  抽取出来的模板
	 */
	private void outputTemplateToFile(String filename,int count, Set<String> templates) {
		
		this.txtWriter.write("源代码文件路径: "+filename+"\t"+"包含的注释条数："+count+"\r\n");
		this.csvWriter.write(filename+","+count+",");
		for(String c:templates){
			this.txtWriter.write(c+"\r\n");
			this.csvWriter.write(c+",");
		}
		this.txtWriter.write("###################\r\n\r\n");
		this.csvWriter.write("\r\n");
	}
	
	/**
	 * 对每条注释逐行处理，抽取出候选集
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
	 * 获取一个title描述所实际涉及的源代码文件名
	 * @param line   一个title描述，如   /virt/kvm/kvm_main.c/kvm_set_pfn_dirty(1258)(linux-3.5.4)
	 * @return     源代码文件名  如 /virt/kvm/kvm_main.c
	 */
	public String getCommentFileName(String line){
		if(this.lxr_type!=LxrType.file){
			return line.substring(0, line.lastIndexOf("/"));
		}
		return line;
	}
	
	/**
	 * 将注释信息过滤模板以后，输出到文件
	 * @param allComments   所有的注释内容构成的map，键为注释路径，值为注释内容
	 * @param fileTemplates    所有的模板构成的map，键为源代码文件名，值为模板集合
	 * @param writer    输出用的writer
	 * @param commentSpliter   每条注释前跟的自定义的分隔符 
	 */
	public void outputNoTemplateCommentToFile(Map<String,String> allComments,Map<String,Set<String>> fileTemplates,PrintWriter writer,String commentSpliter){
		for(Map.Entry<String, String> entry:allComments.entrySet()){
			//先根据被注释的路径名获取被注释的文件名
			String fileName = getCommentFileName(entry.getKey());
			writer.write(commentSpliter+entry.getKey().trim()+"\r\n");
			//如果文件模板中，当前文件存在模板，就对当前注释过滤模板以后输出，否则就对注释进行原样输出
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
