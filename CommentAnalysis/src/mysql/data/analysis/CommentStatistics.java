package mysql.data.analysis;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import mysql.data.util.FileUtil;
import mysql.data.util.LxrType;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class CommentStatistics {
	private static final Logger log = Logger.getLogger(CommentStatistics.class);
	
	public static void main(String[] args) throws IOException {
		log.setLevel(Level.INFO);
		for(String lxrtype : LxrType.getTypeList()){
			log.info("Lxy_type:"+lxrtype);
			
			TXTCommentAnalyzer a = new TXTCommentAnalyzer(lxrtype,false);
			Map<String,String> comments = a.getComments();
			Map<String,String> noTemplateStrictComments = a.readContentToMap(FileUtil.readableFile(TXTCommentAnalyzer.DEFAULTPATH+"\\noTemplate_"+TXTCommentAnalyzer.EXTRACTPOLICY[TXTCommentAnalyzer.EXTRACTPOLICY_STRICT]+"\\"+lxrtype+".txt"), TXTCommentAnalyzer.DEFAULTSPLITER);
			Map<String,String> noTemplateMiddleComments = a.readContentToMap(FileUtil.readableFile(TXTCommentAnalyzer.DEFAULTPATH+"\\noTemplate_"+TXTCommentAnalyzer.EXTRACTPOLICY[TXTCommentAnalyzer.EXTRACTPOLICY_MIDDLE]+"\\"+lxrtype+".txt"), TXTCommentAnalyzer.DEFAULTSPLITER);
			Map<String,String> noTemplateLooseComments = a.readContentToMap(FileUtil.readableFile(TXTCommentAnalyzer.DEFAULTPATH+"\\noTemplate_"+TXTCommentAnalyzer.EXTRACTPOLICY[TXTCommentAnalyzer.EXTRACTPOLICY_LOOSE]+"\\"+lxrtype+".txt"), TXTCommentAnalyzer.DEFAULTSPLITER);
			outputStatResult(lxrtype, comments,noTemplateStrictComments,noTemplateMiddleComments,noTemplateLooseComments);
			
//			for(Map.Entry<String,String> entry:comments.entrySet()){
//				String commentContent = entry.getValue().trim();
////				if(commentContent.matches("[.*[\\r\\n]*]*while[\\s]*\\([.*[\\r\\n]*]*")||commentContent.matches("[.*[\\r\\n]*]*if[\\s]*\\([.*[\\r\\n]*]*")||commentContent.matches("[.*[\\r\\n]*]*else[.*[\\r\\n]*]*")){
//				String chinesePart = commentContent.replaceAll("[a-zA-Z]+[\\s\\pP<>]*", "");
//				double ratio = (double)chinesePart.length()/commentContent.length();
//				if(ratio<0.5){
//					log.info(entry.getKey());
//					log.info(commentContent);
//					log.info("中文比例："+ratio);
//					log.info("#####################################################");
//				}
//			}
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
}
