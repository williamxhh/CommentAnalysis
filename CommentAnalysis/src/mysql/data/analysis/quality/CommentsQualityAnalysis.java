package mysql.data.analysis.quality;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.sql.Connection;

import mysql.data.analysis.TXTCommentAnalyzer;
import mysql.data.analysisDB.entity.FileCommentTypeCount;
import mysql.data.filter.DoNothingFilter;
import mysql.data.filter.FilterBase;
import mysql.data.filter.IscasChineseCommentExtractor;
import mysql.data.filter.IscasLinkFilter;
import mysql.data.util.FileUtil;
import mysql.data.util.PropertiesUtil;

public class CommentsQualityAnalysis {
	private static final Logger log = Logger.getLogger(CommentsQualityAnalysis.class);
	private static Properties props = PropertiesUtil.getProperties();
	
	public static void main(String[] args)  throws IOException, SQLException{
		log.setLevel(Level.DEBUG);
		CommentsQualityAnalysis cqa = new CommentsQualityAnalysis();
		String type = "function definition";
		
		cqa.FilterLinkAndImagemap(type);
		
//		log.info("##validity##");
//		cqa.validityRedundantComment(type);
//		cqa.validityPasteOriginSourceCode(type);
//		cqa.validityChineseTooShort(type, 5);
		
//		log.info("##usefulness##");
//		cqa.usefulnessLeakRelatedCommentsCount(type);
		
		log.info("##completeness##");
//		cqa.completenessCommentedTypeRatio();
		cqa.completenessFileCommentedRatio(type);
		log.info("done");
//		cqa.completeness(type);
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
	 */
	public void completenessCommentedTypeRatio() throws IOException, SQLException{
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
	
	public Map<String,Integer> getAllEntries(String filePath) throws SQLException{
		Map<String,Integer> typeAndCount = new HashMap<String,Integer>();
		Connection conn = connectDB("lxr");
		Statement stmt = conn.createStatement();
		String sql = "select d.declaration,count(*) as number from lxr_symbols s, lxr_declarations d, lxr_indexes i " +
				"where s.symid=i.symid and i.type=d.declid and d.declaration != 'local variable' and i.fileid=(select f.fileid from " +
				"lxr_files f, lxr_releases r where f.filename='"+filePath+"' and r.releaseid='linux-3.5.4' and f.fileid=r.fileid order by f.fileid asc limit 1)  GROUP BY d.declaration;";
		log.info(sql);
		ResultSet rs = stmt.executeQuery(sql);
		while(rs.next()){
			typeAndCount.put(rs.getString(1), rs.getInt(2));
		}
		rs.close();
		stmt.close();
		conn.close();
		
		return typeAndCount;
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
