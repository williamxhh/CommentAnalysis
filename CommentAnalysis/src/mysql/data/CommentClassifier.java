package mysql.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mysql.data.util.FileUtil;

public class CommentClassifier {
	// 这个map是分类的时候用，有多少种注释种类，就用type名作为键，建立不同的writer，往不同的文件里面写
	static Map<String,PrintWriter> WRITERMAP = new HashMap<String, PrintWriter>(); 
	//这个Set是在不知道要处理的注释有多少种type的时候，做统计用的，因为set不会保存重复的类型
	//static Set<String> TYPESET = new HashSet<String>(); 
	
	public static void main(String[] args) {
		
		try {
			classifyComment();
//			for(String s:TYPESET){
//				System.out.println(s);
//			}
//			System.out.println(TYPESET.size());
			System.out.println("done");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * 将不同类别的writer初始化
	 */
	static void prepareWriter(){
		List<String> typeList = new ArrayList<String>();
		typeList.add("enumeration name");
		typeList.add("variable definition");
		typeList.add("extern or forward variable declaration");
		typeList.add("file");
		typeList.add("enumerator");
		typeList.add("macro (un)definition");
		typeList.add("union name");
		typeList.add("class, struct, or union member");
		typeList.add("function definition");
		typeList.add("function prototype or declaration");
		typeList.add("structure name");
		typeList.add("typedef");
		
		//注释里面有两条android的测试注释，为了程序健壮性，特加上这个分类，这实际上不是一个有实际意义的分类
		typeList.add("exception_android");
		
		for(String s:typeList){
			try {
				File file = FileUtil.writeableFile(DataSource.ROOTPATH+"CLASSIFIED/CLASSIFICATION_"+s+".txt");
				WRITERMAP.put(s, new PrintWriter(file));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 释放所有的writer资源
	 */
	static void closeWriter(){
		for(Map.Entry<String, PrintWriter> entry:WRITERMAP.entrySet()){
			entry.getValue().flush();
			entry.getValue().close();
		}
	}

	
	/**
	 * 从过滤处理以后的注释文件filteredComment.txt中读取，然后判断被注释对象的类别，将不同类的注释保存到不同的文件中
	 * @throws SQLException
	 */
	@SuppressWarnings("resource")
	static void classifyComment() throws SQLException{
		//将不同类别的writer初始化
		prepareWriter();
		
		//通过不断读取保存一条注释内容，读满一条就写出
		StringBuilder comment = new StringBuilder();
		try {
			BufferedReader reader = new BufferedReader(
					new FileReader(DataSource.ROOTPATH+"/filteredComment.txt"));
			
			//先把首行读出来，判断一个类别，初始化好 currentWriter
			String line = reader.readLine();
			comment.append(line+"\r\n");
			//去掉前面的 ##**## 再处理 ，获取相应的type值，拿到对应的writer
			PrintWriter currentWriter = WRITERMAP.get(getType(line.substring(6)));
			
			while ((line = reader.readLine()) != null) {
				//每条注释都用##**##开头，所以一行不是以##**##开头的话，那就表示它与前一行同属于一条注释，所以直接在comment里面追加即可
				if(!line.startsWith("##**##")){
					if(line.trim().length()!=0)
						comment.append(line+"\r\n");
				}else{
				//如果当前这条注释已经完成，那么就输出保存，然后重置comment，开始保存下一条注释
					currentWriter.write(comment.toString()+"\r\n");
					currentWriter.flush();
					currentWriter = WRITERMAP.get(getType(line.substring(6)));
					comment = new StringBuilder();
					comment.append(line+"\r\n");
				}
			}
			reader.close();
			currentWriter.write(comment.toString()+"\r\n");
			currentWriter.flush();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//释放所有的writer资源
		closeWriter();
	}
	
	/**
	 * 基于一个注释的标题，抽取其中的文件名，变量名，行号等信息，进而查出接下来的注释的类型
	 * 
	 * 
	 * @param line  一行的内容类似于这样  /arch/arm/boot/compressed/atags_to_fdt.c/atags_to_fdt(0047)(linux-3.5.4)
	 * @return
	 * @throws SQLException
	 */
	static String getType(String line) throws SQLException{
//		PrintWriter writer = new PrintWriter("Filename_Symbolname_LineNo_Type.txt");
		String comm_filename = "";
		String comm_symname = "";
		int comm_linenoInFile = -1;
		String type = "";
		
		if (line.contains("(linux-3.5.4)")) {
			int pos = line.lastIndexOf("(linux-3.5.4)");
			if(line.charAt(pos-1)!=')'){
				comm_filename = line.substring(0,pos);
			}else{
				line = line.substring(0,pos);
				int pos_lineNo = line.lastIndexOf("(")+1;
				comm_linenoInFile = Integer.parseInt(line.substring(pos_lineNo,line.length()-1));
				int pos_slash = line.lastIndexOf("/");
				comm_filename = line.substring(0,pos_slash);
				comm_symname = line.substring(pos_slash+1,pos_lineNo-1);
			}
			
			if (!comm_symname.equals("") && comm_linenoInFile != -1) {
				type = getType(comm_filename, comm_symname, comm_linenoInFile);
			}else{
				type = "file";
			}
			
//			TYPESET.add(type);
//			writer.write(line + "\tFileName:" + comm_filename
//					+ "\tSymbol Name:" + comm_symname + "\tLineNo:"
//					+ comm_linenoInFile+"\tType:"+type+"\r\n");
		}else{
			type = "exception_android";
		}
//		writer.flush();
//		writer.close();
		return type;
	}
	
	
	
	/**
	 * 根据传入的文件名，符号名和行号到lxr数据库中查出type信息返回
	 * @param filename
	 * @param symbolname
	 * @param lineNo
	 * @return
	 * @throws SQLException
	 */
	static String getType(String filename,String symbolname,int lineNo) throws SQLException{
		String type = "";
		String url = "jdbc:mysql://192.168.160.131:3306/lxr?user=root&password=123123";
		Connection conn = DriverManager.getConnection(url);
		Statement stmt = conn.createStatement();
		
		ResultSet rs = stmt
				.executeQuery("select d.declaration from lxr_declarations AS d WHERE d.declid = (SELECT i.type from lxr_indexes AS i WHERE i.symid = (SELECT s.symid from lxr_symbols AS s WHERE s.symname='"
						+ symbolname
						+ "') and i.fileid = (SELECT f.fileid from lxr_files AS f WHERE f.filename='"
						+ filename
						+ "') and i.line = "
						+ lineNo + ");");
		rs.next();
		type = rs.getString(1);
		rs.close();
		
		
		stmt.close();
		conn.close();
		
		return type;
	}
	
}
