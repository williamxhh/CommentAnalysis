package mysql.data.analysis;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import mysql.data.analysisDB.entity.CommentTableInfo;
import mysql.data.filter.DoNothingFilter;
import mysql.data.filter.FilterBase;
import mysql.data.filter.IscasChineseCommentExtractor;
import mysql.data.filter.IscasLinkFilter;
import mysql.data.util.FileUtil;
import mysql.data.util.LxrType;
import mysql.data.util.PropertiesUtil;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class CommentStatistics {
	private static final Logger log = Logger.getLogger(CommentStatistics.class);
	
//	public static void main(String[] args) throws IOException {
//		log.setLevel(Level.INFO);
//		for(String lxrtype : LxrType.getTypeList()){
//			log.info("Lxy_type:"+lxrtype);
//			
//			TXTCommentAnalyzer a = new TXTCommentAnalyzer(lxrtype,false);
//			Map<String,String> comments = a.getComments();
//			Map<String,String> noTemplateStrictComments = a.readContentToMap(FileUtil.readableFile(TXTCommentAnalyzer.DEFAULTPATH+"\\noTemplate_"+TXTCommentAnalyzer.EXTRACTPOLICY[TXTCommentAnalyzer.EXTRACTPOLICY_STRICT]+"\\"+lxrtype+".txt"), TXTCommentAnalyzer.DEFAULTSPLITER);
//			Map<String,String> noTemplateMiddleComments = a.readContentToMap(FileUtil.readableFile(TXTCommentAnalyzer.DEFAULTPATH+"\\noTemplate_"+TXTCommentAnalyzer.EXTRACTPOLICY[TXTCommentAnalyzer.EXTRACTPOLICY_MIDDLE]+"\\"+lxrtype+".txt"), TXTCommentAnalyzer.DEFAULTSPLITER);
//			Map<String,String> noTemplateLooseComments = a.readContentToMap(FileUtil.readableFile(TXTCommentAnalyzer.DEFAULTPATH+"\\noTemplate_"+TXTCommentAnalyzer.EXTRACTPOLICY[TXTCommentAnalyzer.EXTRACTPOLICY_LOOSE]+"\\"+lxrtype+".txt"), TXTCommentAnalyzer.DEFAULTSPLITER);
//			outputStatResult(lxrtype, comments,noTemplateStrictComments,noTemplateMiddleComments,noTemplateLooseComments);
//			
////			for(Map.Entry<String,String> entry:comments.entrySet()){
////				String commentContent = entry.getValue().trim();
//////				if(commentContent.matches("[.*[\\r\\n]*]*while[\\s]*\\([.*[\\r\\n]*]*")||commentContent.matches("[.*[\\r\\n]*]*if[\\s]*\\([.*[\\r\\n]*]*")||commentContent.matches("[.*[\\r\\n]*]*else[.*[\\r\\n]*]*")){
////				String chinesePart = commentContent.replaceAll("[a-zA-Z]+[\\s\\pP<>]*", "");
////				double ratio = (double)chinesePart.length()/commentContent.length();
////				if(ratio<0.5){
////					log.info(entry.getKey());
////					log.info(commentContent);
////					log.info("中文比例："+ratio);
////					log.info("#####################################################");
////				}
////			}
//		}
//	}
	
	public static void main(String[] args) throws IOException  {
		log.setLevel(Level.INFO);
//		LinkAndImageMapFilter();
		IscasCommentWithoutSourceCode();
		log.info("done");
	}
	
	public static void IscasCommentWithoutSourceCode() throws IOException{
		String SourceCodePath = PropertiesUtil.getProperties().getProperty("linuxSource.dir");
		
		Map<String,String> comments = TXTCommentAnalyzer.readContentToMap(FileUtil.readableFile(TXTCommentAnalyzer.DEFAULTPATH + "\\noLinkAndImageMap\\CLASSIFICATION_function definition.txt"), TXTCommentAnalyzer.DEFAULTSPLITER);
		PrintWriter writer = new PrintWriter(FileUtil.writeableFile(TXTCommentAnalyzer.DEFAULTPATH+"\\fd_noSourceCode.txt"));
		PrintWriter writer1 = new PrintWriter(FileUtil.writeableFile(TXTCommentAnalyzer.DEFAULTPATH+"\\fd_noSourceCode1.txt"));
		PrintWriter writer2 = new PrintWriter(FileUtil.writeableFile(TXTCommentAnalyzer.DEFAULTPATH+"\\fd_noSourceCode2.txt"));
		int count = 0;
		for(Map.Entry<String, String> entry:comments.entrySet()){
			String comment = entry.getValue();
			
			String path = entry.getKey();
			String file = path.substring(0,path.lastIndexOf("/"));
			int index = path.indexOf("(");
			int lineNo = Integer.parseInt(path.substring(index+1,index+5));
			String functionPrototype = FileUtil.readAllLines(FileUtil.readableFile(SourceCodePath + file),"UTF-8").get(lineNo-1);
			if((comment.contains(functionPrototype)) && (comment.lastIndexOf(functionPrototype)>10)){
				writer1.write(TXTCommentAnalyzer.DEFAULTSPLITER + entry.getKey() + "\r\n");
				writer1.write(comment + "\r\n\r\n");
				count++;
			}else if(comment.contains("注释") && (computeEnglishRatio(comment.substring(comment.lastIndexOf("注释")))>0.7)){
				writer1.write(TXTCommentAnalyzer.DEFAULTSPLITER + entry.getKey() + "\r\n");
				writer1.write(comment + "\r\n\r\n");
				count++;
			}else{
				writer2.write(TXTCommentAnalyzer.DEFAULTSPLITER + entry.getKey() + "\r\n");
				writer2.write(comment + "\r\n\r\n");
			}
				
				
			
			
			if(comment.contains("注释")){
				String str = comment.substring(0,comment.lastIndexOf("注释")+2);
				writer.write(TXTCommentAnalyzer.DEFAULTSPLITER + entry.getKey() + "\r\n");
				writer.write(str + "\r\n\r\n");
			}else{
				writer.write(TXTCommentAnalyzer.DEFAULTSPLITER + entry.getKey() + "\r\n");
				writer.write(comment + "\r\n\r\n");
			}
		}
		log.info(count);
		writer.flush();
		writer1.flush();
		writer2.flush();
		writer.close();
		writer1.close();
		writer2.close();
	}
	
	public static void extractChineseCommentFromSourceCode() throws IOException{
		Map<String,String> comments = TXTCommentAnalyzer.readContentToMap(FileUtil.readableFile(TXTCommentAnalyzer.DEFAULTPATH + "\\noLinkAndImageMap\\CLASSIFICATION_function definition.txt"), TXTCommentAnalyzer.DEFAULTSPLITER);
		PrintWriter writer = new PrintWriter(FileUtil.writeableFile(TXTCommentAnalyzer.DEFAULTPATH+"\\testExctract.txt"));
		IscasChineseCommentExtractor extractor = new IscasChineseCommentExtractor();
		for(Map.Entry<String, String> entry:comments.entrySet()){
			String comment = entry.getValue();
			if(comment.contains("注释")){
				String str = comment.substring(comment.lastIndexOf("注释")+2);
				writer.write(TXTCommentAnalyzer.DEFAULTSPLITER + entry.getKey() + "\r\n");
				writer.write(str + "\r\n\r\n");
				writer.write("###FOLLOWING###\r\n");
				for(String s:extractor.getText(str)){
					writer.write(s+"\r\n");
				}
				writer.write("\r\n\r\n");
			}
		}
		writer.flush();
		writer.close();
	}
	
	
	public static void commentLengthAndChineseRatio()  throws IOException{
		for(String lxrtype:LxrType.getTypeList()){
			log.info("Lxy_type:"+lxrtype);
			PrintWriter writer = new PrintWriter(FileUtil.writeableFile(TXTCommentAnalyzer.DEFAULTPATH+"\\length_statistic\\"+lxrtype+".csv"));
			Map<String,String> comments = TXTCommentAnalyzer.readContentToMap(FileUtil.readableFile(TXTCommentAnalyzer.DEFAULTPATH + "\\CLASSIFICATION_"+lxrtype+".txt"), TXTCommentAnalyzer.DEFAULTSPLITER);
			for(Map.Entry<String,String> entry:comments.entrySet()){
				String commentContent = entry.getValue().trim();
				String chinesePart = commentContent.replaceAll("[a-zA-Z]+[\\s\\pP<>]*", "");
				double ratio = 0;
				if(commentContent.length() != 0)
					ratio = (double)chinesePart.length()/commentContent.length();
				writer.write(commentContent.length()+","+chinesePart.length()+","+ratio+"\r\n");
			}
			writer.close();
			log.info("done");
		}
	}
	
	public static void outputStatResult(String lxrtype,Map<String,String> comments,Map<String,String> noTemplateStrictComments,Map<String,String> noTemplateMiddleComments,Map<String,String> noTemplateLooseComments){
		try {
			PrintWriter writer = new PrintWriter(FileUtil.writeableFile(TXTCommentAnalyzer.DEFAULTPATH+"\\statistic\\"+LxrType.getTypeIndex(lxrtype)+".csv"));
			PrintWriter matlab_writer = new PrintWriter(FileUtil.writeableFile(TXTCommentAnalyzer.DEFAULTPATH+"\\statistic\\m"+LxrType.getTypeIndex(lxrtype)+".csv"));
			writer.write("注释路径,注释长度,中文字符数,过滤strict型模板后长度,strict中文字符数,strict型模板长度,过滤middle型模板后长度,middle中文字符数,middle型模板长度,过滤loose型模板后长度,loose中文字符数,loose型模板长度\r\n");
			for(Map.Entry<String,String> entry:comments.entrySet()){
				String commentContent = entry.getValue().trim();
				String chinesePart = commentContent.replaceAll("[a-zA-Z]+[\\s\\pP<>]*", "");
				String strict = noTemplateStrictComments.get(entry.getKey()).trim();
				String chineseStrict = strict.replaceAll("[a-zA-Z]+[\\s\\pP<>]*", "");
				String middle = noTemplateMiddleComments.get(entry.getKey()).trim();
				String chineseMiddle = middle.replaceAll("[a-zA-Z]+[\\s\\pP<>]*", "");
				String loose = noTemplateLooseComments.get(entry.getKey()).trim();
				String chineseLoose = loose.replaceAll("[a-zA-Z]+[\\s\\pP<>]*", "");
				writer.write(entry.getKey()+","+commentContent.length()+","+chinesePart.length()+","+strict.length()+","+chineseStrict.length()+","+(commentContent.length()-strict.length())+","+middle.length()+","+chineseMiddle.length()+","+(commentContent.length()-middle.length())+","+loose.length()+","+chineseLoose.length()+","+(commentContent.length()-loose.length())+"\r\n");
				matlab_writer.write(commentContent.length()+","+chinesePart.length()+","+strict.length()+","+chineseStrict.length()+","+(commentContent.length()-strict.length())+","+middle.length()+","+chineseMiddle.length()+","+(commentContent.length()-middle.length())+","+loose.length()+","+chineseLoose.length()+","+(commentContent.length()-loose.length())+"\r\n");
			}
			writer.close();
			matlab_writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public String statisticInfo(String path,String lxrtype) throws IOException{
		TXTCommentAnalyzer a = new TXTCommentAnalyzer(lxrtype,false);
		Map<String,String> comments = a.getComments();
		Map<String,String> noTemplateStrictComments = a.readContentToMap(FileUtil.readableFile(TXTCommentAnalyzer.DEFAULTPATH+"\\noTemplate_"+TXTCommentAnalyzer.EXTRACTPOLICY[TXTCommentAnalyzer.EXTRACTPOLICY_STRICT]+"\\"+lxrtype+".txt"), TXTCommentAnalyzer.DEFAULTSPLITER);
		String commentContent = comments.get(path).trim();
		String chinesePart = commentContent.replaceAll("[a-zA-Z]+[\\s\\pP<>]*", "");
		String strict = noTemplateStrictComments.get(path).trim();
		String chineseStrict = strict.replaceAll("[a-zA-Z]+[\\s\\pP<>]*", "");
		
		StringBuilder stat = new StringBuilder();
		stat.append("注释长度:"+commentContent.length()+"\r\n");
		stat.append("中文字符数:"+chinesePart.length()+"\r\n");
		stat.append("过滤strict型模板后长度:"+strict.length()+"\r\n");
		stat.append("过滤strict型模板后中文字符数:"+chineseStrict.length()+"\r\n");
		
		return stat.toString();
	}
	
	
	public static double computeEnglishRatio(String inputStr){
		String str = inputStr.replaceAll("[\u4e00-\u9fa5]", "");
		return (double)str.length()/inputStr.length();
	}
}
