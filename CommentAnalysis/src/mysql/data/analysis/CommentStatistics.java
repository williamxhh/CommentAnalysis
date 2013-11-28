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
		for(int lxrtype : LxrType.getTypeValues()){
			log.info(lxrtype + " Lxy_type:"+LxrType.getTypeName(lxrtype));
			
			TXTCommentAnalyzer a = new TXTCommentAnalyzer(lxrtype);
			Map<String,String> comments = a.getComments();
			Map<String,String> noTemplateStrictComments = a.readContentToMap(FileUtil.readableFile(TXTCommentAnalyzer.DEFAULTPATH+"\\noTemplate_EXTRACTPOLICY_STRICT\\"+LxrType.getTypeName(lxrtype)+".txt"), TXTCommentAnalyzer.DEFAULTSPLITER);
			Map<String,String> noTemplateMiddleComments = a.readContentToMap(FileUtil.readableFile(TXTCommentAnalyzer.DEFAULTPATH+"\\noTemplate_EXTRACTPOLICY_MIDDLE\\"+LxrType.getTypeName(lxrtype)+".txt"), TXTCommentAnalyzer.DEFAULTSPLITER);
			Map<String,String> noTemplateLooseComments = a.readContentToMap(FileUtil.readableFile(TXTCommentAnalyzer.DEFAULTPATH+"\\noTemplate_EXTRACTPOLICY_LOOSE\\"+LxrType.getTypeName(lxrtype)+".txt"), TXTCommentAnalyzer.DEFAULTSPLITER);
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
	
	public static void outputStatResult(int lxrtype,Map<String,String> comments,Map<String,String> noTemplateStrictComments,Map<String,String> noTemplateMiddleComments,Map<String,String> noTemplateLooseComments){
		try {
			PrintWriter writer = new PrintWriter(FileUtil.writeableFile(TXTCommentAnalyzer.DEFAULTPATH+"\\statistic\\"+LxrType.getTypeName(lxrtype)+".csv"));
			writer.write("注释路径,注释长度,中文字符数,过滤strict型模板后长度,strict中文字符数,过滤middle型模板后长度,middle中文字符数,过滤loose型模板后长度,loose中文字符数\r\n");
			for(Map.Entry<String,String> entry:comments.entrySet()){
				String commentContent = entry.getValue().trim();
				String chinesePart = commentContent.replaceAll("[a-zA-Z]+[\\s\\pP<>]*", "");
				String strict = noTemplateStrictComments.get(entry.getKey()).trim();
				String chineseStrict = strict.replaceAll("[a-zA-Z]+[\\s\\pP<>]*", "");
				String middle = noTemplateMiddleComments.get(entry.getKey()).trim();
				String chineseMiddle = middle.replaceAll("[a-zA-Z]+[\\s\\pP<>]*", "");
				String loose = noTemplateLooseComments.get(entry.getKey()).trim();
				String chineseLoose = loose.replaceAll("[a-zA-Z]+[\\s\\pP<>]*", "");
				writer.write(entry.getKey()+","+commentContent.length()+","+chinesePart.length()+","+strict.length()+","+chineseStrict.length()+","+middle.length()+","+chineseMiddle.length()+","+loose.length()+","+chineseLoose.length()+"\r\n");
			}
			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
