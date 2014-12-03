package mysql.data.analysis.quality;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import tool.nlpir.WordSeg;

import java.sql.Connection;

import mysql.data.analysis.CommentAnalyzer;
import mysql.data.analysis.TXTCommentAnalyzer;
import mysql.data.analysisDB.entity.FileCommentTypeCount;
import mysql.data.filter.DoNothingFilter;
import mysql.data.filter.FilterBase;
import mysql.data.filter.IscasChineseCommentExtractor;
import mysql.data.filter.IscasLinkFilter;
import mysql.data.util.CommentEntryCount;
import mysql.data.util.FileUtil;
import mysql.data.util.NLPAnalysisResult;
import mysql.data.util.PropertiesUtil;

public class CommentsQualityAnalysis {
	private static final Logger log = Logger.getLogger(CommentsQualityAnalysis.class);
	private static Properties props = PropertiesUtil.getProperties();
	private static String NONE_OF_THIS_TYPE = "None of This Type";
	
	private CommentAnalyzer ca = null;
	private Map<String, CommentEntryCount> entry_map = null;
	private Map<String, NLPAnalysisResult> nlp_result_map = null;
	
	public static void main(String[] args)  throws IOException, SQLException, ClassNotFoundException{
		log.setLevel(Level.WARN);
		CommentsQualityAnalysis cqa = new CommentsQualityAnalysis(true);

		
//		cqa.FilterLinkAndImagemap(type);
		
//		log.info("##validity##");
//		cqa.validityRedundantComment(type);
//		cqa.validityPasteOriginSourceCode(type);
//		cqa.validityChineseTooShort(type, 5);
		
//		log.info("##usefulness##");
//		cqa.usefulnessLeakRelatedCommentsCount(type);
		
//		log.info("##completeness##");
//		cqa.completenessCommentedTypeRatio();
//		cqa.completenessFileCommentedRatio(type);
		
		log.info("##stat##");
		String path = "/";
		String type = "function definition";
//		cqa.statCommentTypeDistribution(path);
//		cqa.statCommentedRatio(path);
//		cqa.statNLPAnalysis(path,type);
//		log.info(cqa.getAllEntries().get("/virt/kvm/kvm_main.c"));
		cqa.outputNLPResultToFile(type);
		
		log.info("done");
//		cqa.completeness(type);
	}
	
	public CommentsQualityAnalysis(boolean loadFromFile) {
		ca = new CommentAnalyzer(loadFromFile);
	}
	
	/**
	 * ��ò�ͬlxr����ע�͵ķֲ������key��lxr���ͣ�value��ע������
	 * @param prefix   ע��·��ǰ׺
	 * @return
	 */
	public Map<String,Integer> statCommentTypeDistribution(String prefix) {
		Map<String,String> comment_types = ca.loadCommentsTypes();
		Map<String, Integer> type_counter = new TreeMap<String, Integer>();
		int total_count = 0;
		for(Map.Entry<String, String> entry : comment_types.entrySet()) {
			if(entry.getKey().startsWith(prefix)){
				String type = entry.getValue();
				if(type_counter.containsKey(type)) {
					type_counter.put(type, type_counter.get(type) + 1);
				} else {
					type_counter.put(type, 1);
				}
				++total_count;
			}
		}
		
		for(Map.Entry<String, Integer> entry: type_counter.entrySet()) {
			log.info(entry.getKey() + "," + entry.getValue());
		}
		log.info("total comments count : " + total_count);
		return type_counter;
	}
	
	/**
	 * ��ȡ��ͬlxr���͵�ע�͵���ע��������ע������������õ�ע�͸��Ƕ�
	 * @param prefix  ע��·��ǰ׺
	 * @return
	 * @throws SQLException
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public Map<String, String> statCommentedRatio(String prefix) throws SQLException, IOException, ClassNotFoundException {
		Map<String, Integer> commented_count = new TreeMap<String, Integer>();
		Map<String, Integer> total_count = new TreeMap<String, Integer>();
		Set<String> file_set = new TreeSet<String>();
		Map<String, String> comment_types = ca.loadCommentsTypes();
		
		Map<String, String> all_comments = ca.getAllComments();
		for(Map.Entry<String, String> entry : all_comments.entrySet()) {
			String path = entry.getKey();
			if(path.startsWith(prefix)){
				file_set.add(ca.getCommentFileName(path));
				String type = comment_types.get(path);
				if(commented_count.containsKey(type)) {
					commented_count.put(type, commented_count.get(type) + 1);
				} else {
					commented_count.put(type, 1);
				}
			}
		}
			
		log.info("total file: " + file_set.size());
		for(String filename : file_set) {
			log.info(filename);
		}
		
		for(String file : file_set) {
			Map<String, Integer> file_count = getAllEntries(file);
			for(String type: file_count.keySet()) {
				if(total_count.containsKey(type)) {
					total_count.put(type, total_count.get(type) + file_count.get(type));
				} else {
					total_count.put(type, file_count.get(type));
				}
			}
		}
		
		Map<String, String> result = new TreeMap<String, String>();
		for(String type: total_count.keySet()) {
			if(commented_count.containsKey(type)) {
				result.put(type, commented_count.get(type) + "/" + total_count.get(type));
			} else {
				result.put(type, "0/" + total_count.get(type));
			}
		}
		
		for(Map.Entry<String, String> entry: result.entrySet()) {
			log.info(entry.getKey() + ":\t" + entry.getValue());
		}
		return result;
	}
	
	private void outputNLPResultToFile(String lxr_type) {
		log.setLevel(Level.INFO);
		try {
			PrintWriter writer = new PrintWriter(props.getProperty("mysql.data.DataSource.rootPath") + "/" + lxr_type + "_" +props.getProperty("mysql.data.analysis.quality.CommentsQualityAnalysis.lxrNLP"));
		
			for (String file_name : ca.getAllCommentedFilepath()) {
				log.info(file_name);
				String info = statNLPAnalysis(file_name, lxr_type);
				if(!info.equals(NONE_OF_THIS_TYPE)) {
					writer.write(file_name + "," + info + "\r\n");
				}
			}
			
			writer.flush();
			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * ͳ��ָ��·���£�ָ��lxr����ע�͵�ƽ���ı����ȣ�ƽ�����鳤�ȣ�ƽ�����ʱ��ʣ����ʱ��ʣ������ʱ��ʵ�
	 * @param prefix
	 * @param lxr_type
	 */
	public String statNLPAnalysis(String prefix, String lxr_type) {
		Set<NLPAnalysisResult> result_set = nlpAnalysis(prefix, lxr_type);
		
		double sum_content_len = 0;
		double sum_word_count = 0;
		double sum_noun_count = 0;
		double sum_verb_count = 0;
		double sum_other_count = 0;
		
		for(NLPAnalysisResult r : result_set) {
			sum_content_len += r.getContent_len();
			sum_word_count += r.getWord_list().size();
			sum_noun_count += r.getNoun_list().size();
			sum_verb_count += r.getVerb_list().size();
			sum_other_count += r.getOther_list().size();
		}
		
		if(result_set.size() == 0 || sum_word_count == 0) {
			return NONE_OF_THIS_TYPE;
		}
		
		StringBuilder sb = new StringBuilder();
		
		sb.append(sum_content_len/result_set.size()).append(",")
		  .append(sum_word_count/result_set.size()).append(",")
		  .append(sum_noun_count/sum_word_count).append(",")
		  .append(sum_verb_count/sum_word_count).append(",")
		  .append(sum_other_count/sum_word_count).append(",")
		  .append(result_set.size());
		
		log.info("Content length , Word count , Noun ratio , Verb ratio , Other ratio , Number of Comments ");
		log.info(sb.toString());
		
		return sb.toString();
	}
	
	public Set<NLPAnalysisResult> nlpAnalysis(String prefix) {
		return nlpAnalysis(prefix, "ALL");
	}
	
	/**
	 * ��ȡָ��·����ָ��lxr���͵�ȫ��ע�͵�nlp�������
	 * @param prefix
	 * @param lxr_type
	 * @return
	 */
	public Set<NLPAnalysisResult> nlpAnalysis(String prefix, String lxr_type) {
		Set<NLPAnalysisResult> result_set = new HashSet<NLPAnalysisResult>();
		
		if(nlp_result_map == null) {
			File nlp_file = new File(props.getProperty("mysql.data.analysis.quality.CommentsQualityAnalysis.nlpResult"));
			if(nlp_file.exists()) {
				log.debug("load from file");
				try {
					ObjectInputStream ois = new ObjectInputStream(new FileInputStream(nlp_file));
					nlp_result_map = (Map<String, NLPAnalysisResult>)ois.readObject();
					ois.close();
				} catch (IOException | ClassNotFoundException e) {
					e.printStackTrace();
				}
				
			} else {
				log.debug("compute");
				nlp_result_map = new TreeMap<String, NLPAnalysisResult>();
				WordSeg wordSeg = new WordSeg();
				for (Map.Entry<String, String> entry : ca.getAllComments().entrySet()) {
					String path = entry.getKey();
					String content = entry.getValue();
					log.info(content);
					NLPAnalysisResult result = new NLPAnalysisResult(path);
					result.setContent_len(content.length());
					result.setWord_list(wordSeg.segmentation(content,
							WordSeg.POS_TAG, WordSeg.SEG_FILTER_WITHPUNC));
					for (String w : result.getWord_list()) {
						int index = w.lastIndexOf('/');
						if (w.charAt(index + 1) == WordSeg.NOUN) {
							result.getNoun_list().add(w.substring(0, index));
						} else if (w.charAt(index + 1) == WordSeg.VERB) {
							result.getVerb_list().add(w.substring(0, index));
						} else if (w.charAt(index + 1) != WordSeg.PUNCTUATION) {
							result.getOther_list().add(w.substring(0, index));
						}
					}
					log.info(result.getNoun_list().toString());
					log.info(result.getVerb_list().toString());
					log.info(result.getOther_list().toString());

					nlp_result_map.put(path, result);
				}
				
				try {
					ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(nlp_file));
					oos.writeObject(nlp_result_map);
					oos.flush();
					oos.close();
					
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		for(String path : nlp_result_map.keySet()) {
			if(path.startsWith(prefix) && (lxr_type.equals("ALL") || ca.loadCommentsTypes().get(path).equals(lxr_type))) {
				result_set.add(nlp_result_map.get(path));
			}
		}
		
		return result_set;
		
	}
	
	public int validityRedundantComment(String type) throws IOException{
		Map<String,String> comments = TXTCommentAnalyzer.readContentToMap(FileUtil.readableFile(TXTCommentAnalyzer.DEFAULTPATH + "\\validity\\noLinkAndImageMap\\CLASSIFICATION_"+type+".txt"), TXTCommentAnalyzer.DEFAULTSPLITER);
		List<String> uniqueComments = new ArrayList<String>();
		//����ÿ���ļ��ܹ���ע����
		int fileCommentsTotal = 0;
		int totalRedundant = 0;
		String currentFile = "";
		for(Map.Entry<String, String> entry:comments.entrySet()){
			String fileName = entry.getKey().substring(0,entry.getKey().lastIndexOf("/"));
			String commentContent = entry.getValue().replaceAll(" ", "");
			if(currentFile.equals("") || currentFile.equals(fileName)){
				currentFile = fileName;
				fileCommentsTotal++;
				if(!uniqueComments.contains(commentContent)){
					uniqueComments.add(commentContent);
				}
			}else{
				log.debug(currentFile + "\t fileCommentsTotal:" + fileCommentsTotal + "\t uniqueComments:" + uniqueComments.size());
				if(fileCommentsTotal > uniqueComments.size()){
					log.debug(currentFile + " ������ע�� ######################");
				}
				totalRedundant += fileCommentsTotal - uniqueComments.size();
				currentFile = fileName;
				fileCommentsTotal = 1;
				
				uniqueComments.clear();
				uniqueComments.add(commentContent);
			}
		}
		log.debug(currentFile + "\t fileCommentsTotal:" + fileCommentsTotal + "\t uniqueComments:" + uniqueComments.size());
		totalRedundant += fileCommentsTotal - uniqueComments.size();
		log.info("�ܹ�������ע������Ϊ:" + totalRedundant);
		return totalRedundant;
	}
	
	/**
	 * ����ճ��ԭʼ���������ע��������Ϊ *_withSourceCode.txt �е�ע������
	 * ��ճ��ԭʼ���룬δ�����κ�ע�͵�ע��������Ϊ *_FilterSourceCode.txt ���ַ���   EMPTY!!!  ����Ŀ
	 * 
	 * 
	 * 
	 * @param type
	 * @throws IOException
	 */
	public void validityPasteOriginSourceCode(String type) throws IOException{
		String SourceCodePath = props.getProperty("linuxSource.dir");
		
		Map<String,String> comments = TXTCommentAnalyzer.readContentToMap(FileUtil.readableFile(TXTCommentAnalyzer.DEFAULTPATH + "\\validity\\noLinkAndImageMap\\CLASSIFICATION_"+type+".txt"), TXTCommentAnalyzer.DEFAULTSPLITER);
		//������ճ��Դ���ע��д���һ���ļ�   *_withSourceCode
		PrintWriter writer1 = new PrintWriter(FileUtil.writeableFile(TXTCommentAnalyzer.DEFAULTPATH+"\\validity\\"+type+"_withSourceCode.txt"));
		//��������ճ��Դ���ע��д��ڶ����ļ�   *_withoutSourceCode
		PrintWriter writer2 = new PrintWriter(FileUtil.writeableFile(TXTCommentAnalyzer.DEFAULTPATH+"\\validity\\"+type+"_withoutSourceCode.txt"));
		//������ճ��Դ���ע���� ��ճ����Դ����й����Ժ��ע������д��������ļ�  *_FilterSourceCode
		PrintWriter writer3 = new PrintWriter(FileUtil.writeableFile(TXTCommentAnalyzer.DEFAULTPATH+"\\validity\\"+type+"_FilterSourceCode.txt"));
		
		IscasChineseCommentExtractor extractor = new IscasChineseCommentExtractor();
		
		int count = 0;   //����ճ��ԭʼ���������ע������
		int emptyCount = 0;   //��ճ��ԭʼ���룬δ�����κ�ע�͵�ע������
		for(Map.Entry<String, String> entry:comments.entrySet()){
			String comment = entry.getValue();
			
			String path = entry.getKey();
			String file = path.substring(0,path.lastIndexOf("/"));
			int index = path.indexOf("(");
			int lineNo = Integer.parseInt(path.substring(index+1,index+5));
			String functionPrototype = FileUtil.readAllLines(FileUtil.readableFile(SourceCodePath + file),"UTF-8").get(lineNo-1);
			if((comment.contains(functionPrototype)) && (comment.lastIndexOf(functionPrototype)>10)){
				writer1.write(TXTCommentAnalyzer.DEFAULTSPLITER + entry.getKey() + "\r\n");
				writer3.write(TXTCommentAnalyzer.DEFAULTSPLITER + entry.getKey() + "\r\n");
				writer1.write(comment + "\r\n\r\n");
				writer3.write(comment.substring(0,comment.lastIndexOf(functionPrototype))+"\r\n");
				String str = comment.substring(comment.lastIndexOf(functionPrototype));
				for(String s:extractor.getText(str)){
					writer3.write(s+"\r\n");
					if(s.equals("EMPTY!!!"))
						emptyCount++;
				}
				writer3.write("\r\n\r\n");
				
				count++;
			}else if(comment.contains("ע��") && (computeEnglishRatio(comment.substring(comment.lastIndexOf("ע��")))>0.7)){
				writer1.write(TXTCommentAnalyzer.DEFAULTSPLITER + entry.getKey() + "\r\n");
				writer3.write(TXTCommentAnalyzer.DEFAULTSPLITER + entry.getKey() + "\r\n");
				writer1.write(comment + "\r\n\r\n");
				writer3.write(comment.substring(0,comment.lastIndexOf("ע��")+2)+"\r\n");
				String str = comment.substring(comment.lastIndexOf("ע��")+2);
				for(String s:extractor.getText(str)){
					writer3.write(s+"\r\n");
					if(s.equals("EMPTY!!!"))
						emptyCount++;
				}
				writer3.write("\r\n\r\n");
				
				count++;
			}else{
				writer2.write(TXTCommentAnalyzer.DEFAULTSPLITER + entry.getKey() + "\r\n");
				writer2.write(comment + "\r\n\r\n");
			}
				
		}
		log.info("����ճ��ԭʼ���������ע������:" + count);
		log.info("��ճ��ԭʼ���룬δ�����κ�ע�͵�ע������:" + emptyCount);
		writer1.flush();
		writer2.flush();
		writer3.flush();
		writer1.close();
		writer2.close();
		writer3.close();
		
	}
	
	/**
	 * �˷�����֤ע�ͳ��ȵ���Ч�ԣ�ע�ͳ���̫�̵���Ϊ��Ч
	 * @param type Ҫ������ע�����ͣ���function definition
	 * @param minLength  ��С��Ч����
	 * @return
	 * @throws IOException 
	 */
	public int validityChineseTooShort(String type,int minLength) throws IOException {
		int invalidCount = 0;
		Map<String,String> commentsWithSourceCode = TXTCommentAnalyzer.readContentToMap(FileUtil.readableFile(TXTCommentAnalyzer.DEFAULTPATH+"\\validity\\"+type+"_FilterSourceCode.txt"), TXTCommentAnalyzer.DEFAULTSPLITER);
		Map<String,String> commentsWithoutSourceCode = TXTCommentAnalyzer.readContentToMap(FileUtil.readableFile(TXTCommentAnalyzer.DEFAULTPATH+"\\validity\\"+type+"_withoutSourceCode.txt"), TXTCommentAnalyzer.DEFAULTSPLITER);
		for(Map.Entry<String, String> entry:commentsWithSourceCode.entrySet()){
			String content = entry.getValue();
			String chineseContent = content.replaceAll("[^\u4e00-\u9fa5]", "");
			if(chineseContent.length()<minLength){
				log.debug(content);
				invalidCount++;
			}
		}
		for(Map.Entry<String, String> entry:commentsWithoutSourceCode.entrySet()){
			String content = entry.getValue();
			String chineseContent = content.replaceAll("[^\u4e00-\u9fa5]", "");
			if(chineseContent.length()<minLength){
				log.debug(content);
				invalidCount++;
			}
		}
		log.info("ע���������ַ�������" + minLength + "��ע����Ϊ��Ч����" + invalidCount + "��");
		return invalidCount;
	}
	
	public int usefulnessLeakRelatedCommentsCount(String type) throws IOException{
		int leakCount = 0;
		Map<String,String> comments = TXTCommentAnalyzer.readContentToMap(FileUtil.readableFile(TXTCommentAnalyzer.DEFAULTPATH + "\\validity\\noLinkAndImageMap\\CLASSIFICATION_"+type+".txt"), TXTCommentAnalyzer.DEFAULTSPLITER);
		for(Map.Entry<String, String> entry:comments.entrySet()){
//			if(entry.getValue().contains("©��")||entry.getValue().contains("ȱ��")||entry.getValue().contains("����")){
			if(entry.getValue().contains("����©��")){
				log.debug(entry.getValue());
				leakCount++;
			}
		}
		log.info("©����ص�ע������Ϊ��"+leakCount);
		return leakCount;
	}
	
	/**
	 * ��֮ǰ��analysisDB�л�ȡһ����λ������ע�ͣ�Ȼ��lxr���ݿ���ȥ��ѯ������Ҫע�͵�ע����ڣ��õ�ÿһ��lxr���͵�ע�������ȣ�д��CommentsAndTypesRatio_*.txt�ļ���
	 * 
	 * @throws IOException
	 * @throws SQLException
	 * @throws ClassNotFoundException 
	 */
	public void completenessCommentedTypeRatio() throws IOException, SQLException, ClassNotFoundException{
		String spliter = props.getProperty("mysql.data.analysis.TXTCommentAnalyzer.commentSpliter");
		List<FileCommentTypeCount> commented = loadCommentedEntryFromDB();
		Map<String,Integer> fileAllEntries = null;
		PrintWriter writer = new PrintWriter(new FileWriter(props.getProperty("mysql.data.analysis.quality.CommentsQualityAnalysis.commentsTypesRatio")));
		String commentedFilePath = "";
		for(FileCommentTypeCount fileCommentType:commented){
			if(commentedFilePath.equals("")){
				commentedFilePath = fileCommentType.getFilePath();
				fileAllEntries = getAllEntries(commentedFilePath);
				writer.write(spliter + commentedFilePath + "\r\n");
			}
			//ͬһ���ļ�
			if(commentedFilePath.equals(fileCommentType.getFilePath())){
				writer.write(fileCommentType.getLxrType() +": " + fileCommentType.getCount() + "/" + fileAllEntries.get(fileCommentType.getLxrType()) + "\r\n");
			}else{
				commentedFilePath = fileCommentType.getFilePath();
				fileAllEntries = getAllEntries(commentedFilePath);
				writer.write("\r\n\r\n" + spliter + commentedFilePath + "\r\n");
				writer.write(fileCommentType.getLxrType() +": " + fileCommentType.getCount() + "/" + fileAllEntries.get(fileCommentType.getLxrType()) + "\r\n");
			}
		}
		writer.flush();
		writer.close();
	}
	
	/**
	 * ��ѯһ����λ������ע����һ���ض�lxr���͵�ע���ڸ���ע���ļ��е�ע��������
	 * 
	 * @param lxrType
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	public void completenessFileCommentedRatio(String lxrType) throws IOException{
		Map<String,String> typeRatioMap = TXTCommentAnalyzer.readContentToMap(new File(props.getProperty("mysql.data.analysis.quality.CommentsQualityAnalysis.commentsTypesRatio")), TXTCommentAnalyzer.DEFAULTSPLITER);
		PrintWriter writer = new PrintWriter(FileUtil.writeableFile(TXTCommentAnalyzer.DEFAULTPATH+"//completenesss//fileCommentedRatioBylxrType//" + lxrType + ".csv"));
		for(Map.Entry<String, String> entry:typeRatioMap.entrySet()){
			if(entry.getValue().contains(lxrType)){
				StringTokenizer st = new StringTokenizer(entry.getValue(), "\r\n");
				while(st.hasMoreTokens()){
					String line = st.nextToken();
					if(line.contains(lxrType)){
						String result = entry.getKey()+","+line.substring(line.indexOf(":")+2).replaceAll("/", ",");
						log.debug(result);
						writer.write(result+"\r\n");
					}
				}
			}
		}
		writer.flush();
		writer.close();
	}
	
	/**
	 * ���ָ���ļ��е�ע�������
	 * @param filePath
	 * @return  key��lxr���ͣ�value����Ӧ���͵�ע�������
	 * @throws SQLException
	 * @throws FileNotFoundException
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	public Map<String,Integer> getAllEntries(String filePath) throws SQLException, FileNotFoundException, ClassNotFoundException, IOException{
		if(entry_map == null) {
			entry_map = getAllEntries();
		}
		
		CommentEntryCount cec = entry_map.get(filePath);
		if(cec != null) {
			return cec.getType_count();
		}
		
		return null;
	}
	
	/**
	 * ��ȡȫ�������ע����ڣ���һ�δ�lxr���ݿ��ȡ������������л����ļ����棬����ֱ�Ӵ��ļ������л��õ�
	 * @return
	 * @throws SQLException
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public Map<String,CommentEntryCount> getAllEntries() throws SQLException, FileNotFoundException, IOException, ClassNotFoundException{
		File all_entries_file = new File(props.getProperty("mysql.data.analysis.quality.CommentsQualityAnalysis.allEntries"));
		if(all_entries_file.exists()) {
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(all_entries_file));
			entry_map = (Map<String,CommentEntryCount>)ois.readObject();
			ois.close();
		} else {
			entry_map = new HashMap<String,CommentEntryCount>();
			Connection conn = connectDB("lxr");
			Statement stmt = conn.createStatement();
			String sql = "select f.filename, d.declaration, count(*) as number from lxr_files f, lxr_declarations d, lxr_indexes i " +
					"where f.fileid=i.fileid and i.type=d.declid and d.declaration != 'local variable' and f.fileid in (select r.fileid from " + 
							"lxr_releases r where r.releaseid='linux-3.5.4') GROUP BY f.filename, d.declaration;";
			ResultSet rs = stmt.executeQuery(sql);
			while(rs.next()){
				String path = rs.getString(1);
				String type = rs.getString(2);
				int count = rs.getInt(3);
				CommentEntryCount entry = null;
				if(entry_map.containsKey(path)) {
					entry = entry_map.get(path);
				} else {
					entry = new CommentEntryCount(path);
					entry_map.put(path, entry);
				}
				entry.addType(type, count);
			}
			rs.close();
			stmt.close();
			conn.close();
			
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(all_entries_file));
			oos.writeObject(entry_map);
			oos.flush();
			oos.close();
		}
		
		return entry_map;
		
	}
	
	public List<FileCommentTypeCount> loadCommentedEntryFromDB() throws SQLException{
		List<FileCommentTypeCount> commented = new ArrayList<FileCommentTypeCount>();
		Connection conn = connectDB("comment");
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery("select path_file,lxr_type,count(*) as number from comment GROUP BY path_file, lxr_type;");
		while(rs.next()){
			commented.add(new FileCommentTypeCount(rs.getString(1), rs.getString(2), rs.getInt(3)));
		}
		rs.close();
		stmt.close();
		conn.close();
		return commented;
	}
	
	public Connection connectDB(String type){
		StringBuilder url = new StringBuilder();
		if(type.equals("comment")){
			url.append("jdbc:mysql://")
				.append(props.getProperty("mysql.data.DataSource.dbserver.ip","192.168.160.131"))
				.append(":")
				.append(props.getProperty("mysql.data.DataSource.dbserver.port","3306"))
				.append("/")
				.append(props.getProperty("mysql.data.analysis.CommentAnalyzer.commentAnalysisdb","012pku_analysis"))
				.append("?user=")
				.append(props.getProperty("mysql.data.DataSource.dbserver.user","root"))
				.append("&password=")
				.append(props.getProperty("mysql.data.DataSource.dbserver.pass","123123"));
		}else if(type.equals("lxr")){
			url.append("jdbc:mysql://")
			.append(props.getProperty("mysql.data.DataSource.dbserver.ip","192.168.160.131"))
			.append(":")
			.append(props.getProperty("mysql.data.DataSource.dbserver.port","3306"))
			.append("/")
			.append(props.getProperty("mysql.data.CommentClassifier.lxrdb","lxr"))
			.append("?user=")
			.append(props.getProperty("mysql.data.DataSource.dbserver.user","root"))
			.append("&password=")
			.append(props.getProperty("mysql.data.DataSource.dbserver.pass","123123"));
		}
		
		try {
			Class.forName("com.mysql.jdbc.Driver");
			Connection conn = DriverManager.getConnection(url.toString());
			return conn;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public void completeness(String type) throws IOException{
		int yuanxingCount = 0;
		int gongnengCount = 0;
		int canshuCount = 0;
		int fanhuizhiCount = 0;
		int diaoyongguanxiCount = 0;
		int zhushiCount = 0;
		
		Map<String,String> comments = TXTCommentAnalyzer.readContentToMap(FileUtil.readableFile(TXTCommentAnalyzer.DEFAULTPATH + "\\validity\\noLinkAndImageMap\\CLASSIFICATION_"+type+".txt"), TXTCommentAnalyzer.DEFAULTSPLITER);
		for(Map.Entry<String, String> entry:comments.entrySet()){
			String content = entry.getValue().replaceAll(" ", "");
			if(content.contains("ԭ��")){
				yuanxingCount++;
			}
			if(content.contains("����")){
				gongnengCount++;
			}
			if(content.contains("����")){
				canshuCount++;
			}
			if(content.contains("����")){
				fanhuizhiCount++;
			}
			if(content.contains("����λ��")){
				diaoyongguanxiCount++;
			}
			if(content.contains("ע��")){
				zhushiCount++;
			}
		}
		log.info("ԭ��:"+yuanxingCount);
		log.info("��������:"+gongnengCount);
		log.info("����:"+canshuCount);
		log.info("����ֵ:"+fanhuizhiCount);
		log.info("���ù�ϵ:"+diaoyongguanxiCount);
		log.info("����ע��:"+zhushiCount);
	}
	
	/**
	 * ��ԭʼע���е�url��imagemap���˵���Ȼ��ѹ����Ժ��ע��д�뵽\\validity\\noLinkAndImageMap\\CLASSIFICATION_function definition.txt
	 * @throws IOException
	 */
	public void FilterLinkAndImagemap(String type)  throws IOException{
		Map<String,String> comments = TXTCommentAnalyzer.readContentToMap(FileUtil.readableFile(TXTCommentAnalyzer.DEFAULTPATH + "\\CLASSIFICATION_"+type+".txt"), TXTCommentAnalyzer.DEFAULTSPLITER);
		PrintWriter writer = new PrintWriter(FileUtil.writeableFile(TXTCommentAnalyzer.DEFAULTPATH+"\\validity\\noLinkAndImageMap\\CLASSIFICATION_"+type+".txt"));
		FilterBase filter = new IscasLinkFilter(new DoNothingFilter());
		for(Map.Entry<String, String> entry:comments.entrySet()){
			writer.write(TXTCommentAnalyzer.DEFAULTSPLITER + entry.getKey() + "\r\n");
			writer.write(filter.getText(entry.getValue()) + "\r\n\r\n");
		}
		writer.flush();
		writer.close();
	}
	
	public static double computeEnglishRatio(String inputStr){
		String str = inputStr.replaceAll("[\u4e00-\u9fa5]", "");
		return (double)str.length()/inputStr.length();
	}
	
}
