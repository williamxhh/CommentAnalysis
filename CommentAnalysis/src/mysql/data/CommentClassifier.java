package mysql.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import mysql.data.util.FileUtil;
import mysql.data.util.PropertiesUtil;

public class CommentClassifier {
	private Properties props;
	private Connection conn;
	private String rootPath;
	private String classifiedCommentPath;
	private String commentAndTypes;
	
	// 这个map是分类的时候用，有多少种注释种类，就用type名作为键，建立不同的writer，往不同的文件里面写
	static Map<String,PrintWriter> WRITERMAP = new HashMap<String, PrintWriter>(); 

	
	public CommentClassifier(){
		props = PropertiesUtil.getProperties();
		StringBuilder url = new StringBuilder();
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
		
		try {
			Class.forName("com.mysql.jdbc.Driver");
			this.conn = DriverManager.getConnection(url.toString());
			
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		this.rootPath = props.getProperty("mysql.data.DataSource.rootPath","commentData/");
		this.classifiedCommentPath = rootPath+props.getProperty("mysql.data.analysis.TXTCommentAnalyzer.classifiedCommentPath","CLASSIFIED");
		this.commentAndTypes = props.getProperty("mysql.data.CommentClassifier.commentsAndTypes");
	}
	
	public Connection getConn() {
		return conn;
	}

	public void closeDBConnection(){
		try {
			if(this.conn!=null&&this.conn.isClosed()){
				conn.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public Set<String> getAllCommentedTypes() throws IOException{
		File typeFile = new File(commentAndTypes);
		if(!typeFile.exists()){
			getAllCommentedTypes(true);
		}
		return loadTypeSet();
	}
	
	private Set<String> loadTypeSet() throws IOException{
		File typeFile = new File(commentAndTypes);
		BufferedReader typereader = new BufferedReader(new FileReader(typeFile));
		Set<String> typeset = new HashSet<String>();
		String oneline = "";
		while((oneline=typereader.readLine())!=null){
			String[] str = oneline.split(",");
			typeset.add(str[1]);
		}
		typereader.close();
		return typeset;
	}
	
	private Map<String,String> loadCommentTypeMap() throws IOException{
		File typeFile = new File(commentAndTypes);
		BufferedReader typereader = new BufferedReader(new FileReader(typeFile));
		Map<String, String> commentTypeMap = new HashMap<String,String>();
		String oneline = "";
		while((oneline=typereader.readLine())!=null){
			String[] str = oneline.split(",");
			commentTypeMap.put(str[0], str[1]);
		}
		typereader.close();
		return commentTypeMap;
	}
	
	public void getAllCommentedTypes(boolean genNewFile){
		if(genNewFile){
			try {
				BufferedReader reader = new BufferedReader(new FileReader(props.getProperty("mysql.data.DataSource.pathFile")));
				PrintWriter writer = new PrintWriter(new FileWriter(commentAndTypes));
				String line ="";
				while((line=reader.readLine())!=null){
					writer.write(line+","+getType(line)+"\r\n");
				}
				reader.close();
				writer.flush();
				writer.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	@SuppressWarnings("resource")
	public void classifyComment() throws IOException{
		File typeFile = new File(commentAndTypes);
		if(!typeFile.exists()){
			getAllCommentedTypes(true);
		}
		Set<String> typeset = loadTypeSet();
		Map<String,String> commentTypeMap = loadCommentTypeMap();

		prepareWriter(typeset);

		//通过不断读取保存一条注释内容，读满一条就写出
		StringBuilder comment = new StringBuilder();
		BufferedReader reader = new BufferedReader(
				new FileReader(rootPath+"/"+props.getProperty("mysql.data.DataClean.filteredCommentsFile")));

		//先把首行读出来，判断一个类别，初始化好 currentWriter
		String line = reader.readLine();
		comment.append(line+"\r\n");
		//去掉前面的 ##**## 再处理 ，获取相应的type值，拿到对应的writer
		PrintWriter currentWriter = WRITERMAP.get(commentTypeMap.get(line.substring(6)));

		while ((line = reader.readLine()) != null) {
			//每条注释都用##**##开头，所以一行不是以##**##开头的话，那就表示它与前一行同属于一条注释，所以直接在comment里面追加即可
			if(!line.startsWith("##**##")){
				if(line.trim().length()!=0)
					comment.append(line+"\r\n");
			}else{
				//如果当前这条注释已经完成，那么就输出保存，然后重置comment，开始保存下一条注释
				currentWriter.write(comment.toString()+"\r\n");
				currentWriter.flush();
				currentWriter = WRITERMAP.get(commentTypeMap.get(line.substring(6)));
				comment = new StringBuilder();
				comment.append(line+"\r\n");
			}
		}
		reader.close();
		currentWriter.write(comment.toString()+"\r\n");
		currentWriter.flush();

		//释放所有的writer资源
		closeWriter();
	}
	
	
	/**
	 * 将不同类别的writer初始化
	 */
	private void prepareWriter(Set<String> typeset){
		
		for(String s:typeset){
			try {
				File file = FileUtil.writeableFile(classifiedCommentPath+"/CLASSIFICATION_"+s+".txt");
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
	private void closeWriter(){
		for(Map.Entry<String, PrintWriter> entry:WRITERMAP.entrySet()){
			entry.getValue().flush();
			entry.getValue().close();
		}
	}
	
	
	
	/**
	 * 基于一个注释的标题，抽取其中的文件名，变量名，行号等信息，进而查出接下来的注释的类型
	 * 
	 * 
	 * @param line  一行的内容类似于这样  /arch/arm/boot/compressed/atags_to_fdt.c/atags_to_fdt(0047)(linux-3.5.4)
	 * @return
	 * @throws SQLException
	 * @throws IOException 
	 */
	public String getType(String line) throws SQLException, IOException{
		File typeFile = new File(commentAndTypes);
		boolean loaded = typeFile.exists();
		if(loaded){
			Map<String, String> commentTypeMap = loadCommentTypeMap();
			return commentTypeMap.get(line);
		}else{
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

			}else{
				type = "exception_android";
			}
			return type;
		}
	}
	
	
	
	/**
	 * 根据传入的文件名，符号名和行号到lxr数据库中查出type信息返回
	 * @param filename
	 * @param symbolname
	 * @param lineNo
	 * @return
	 * @throws SQLException
	 */
	public String getType(String filename,String symbolname,int lineNo) throws SQLException{
		String type = "";
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
		
		return type;
	}

	public static void main(String[] args) throws IOException {
		CommentClassifier cc = new CommentClassifier();
		cc.classifyComment();
		cc.closeDBConnection();
		System.out.println("done");
	}
}
