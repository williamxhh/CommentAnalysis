package mysql.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import mysql.data.util.ConnectionUtil;
import mysql.data.util.FileUtil;
import mysql.data.util.PropertiesUtil;

public class CommentClassifier {
	private static final Logger log = Logger.getLogger(CommentClassifier.class);
	private String rootPath;
	private String classifiedCommentPath;
	private String commentAndTypes;
	
	// ���map�Ƿ����ʱ���ã��ж�����ע�����࣬����type����Ϊ����������ͬ��writer������ͬ���ļ�����д
	static Map<String,PrintWriter> WRITERMAP = new HashMap<String, PrintWriter>(); 

	
	public CommentClassifier(){
		this.rootPath = PropertiesUtil.getProperty("mysql.data.DataSource.rootPath","commentData/");
		this.classifiedCommentPath = rootPath+PropertiesUtil.getProperty("mysql.data.analysis.TXTCommentAnalyzer.classifiedCommentPath","CLASSIFIED");
		this.commentAndTypes = PropertiesUtil.getProperty("mysql.data.CommentClassifier.commentsAndTypes");
	}
	
	public Connection getConn() {
		return ConnectionUtil.getLxrConnection();
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
				BufferedReader reader = new BufferedReader(new FileReader(PropertiesUtil.getProperty("mysql.data.DataSource.pathFile")));
				PrintWriter writer = new PrintWriter(new FileWriter(commentAndTypes));
				String line ="";
				while((line=reader.readLine())!=null){
					log.info("call getType() "+ line); 
					writer.write(line+","+getType(line)+"\r\n");
				}
				log.info("getAllCommentedTypes() COMPLETE");
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

		//ͨ�����϶�ȡ����һ��ע�����ݣ�����һ����д��
		StringBuilder comment = new StringBuilder();
		BufferedReader reader = new BufferedReader(
				new FileReader(rootPath+"/"+PropertiesUtil.getProperty("mysql.data.DataClean.filteredCommentsFile")));

		//�Ȱ����ж��������ж�һ����𣬳�ʼ���� currentWriter
		String line = reader.readLine();
		comment.append(line+"\r\n");
		//ȥ��ǰ��� ##**## �ٴ��� ����ȡ��Ӧ��typeֵ���õ���Ӧ��writer
		PrintWriter currentWriter = WRITERMAP.get(commentTypeMap.get(line.substring(6)));

		while ((line = reader.readLine()) != null) {
			//ÿ��ע�Ͷ���##**##��ͷ������һ�в�����##**##��ͷ�Ļ����Ǿͱ�ʾ����ǰһ��ͬ����һ��ע�ͣ�����ֱ����comment����׷�Ӽ���
			if(!line.startsWith("##**##")){
				if(line.trim().length()!=0)
					comment.append(line+"\r\n");
			}else{
				//�����ǰ����ע���Ѿ���ɣ���ô��������棬Ȼ������comment����ʼ������һ��ע��
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

		//�ͷ����е�writer��Դ
		closeWriter();
	}
	
	
	/**
	 * ����ͬ����writer��ʼ��
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
	 * �ͷ����е�writer��Դ
	 */
	private void closeWriter(){
		for(Map.Entry<String, PrintWriter> entry:WRITERMAP.entrySet()){
			entry.getValue().flush();
			entry.getValue().close();
		}
	}
	
	
	
	/**
	 * ����һ��ע�͵ı��⣬��ȡ���е��ļ��������������кŵ���Ϣ�����������������ע�͵�����
	 * 
	 * 
	 * @param line  һ�е���������������  /arch/arm/boot/compressed/atags_to_fdt.c/atags_to_fdt(0047)(linux-3.5.4)
	 * @return
	 * @throws SQLException
	 * @throws IOException 
	 */
	public String getType(String line) throws SQLException, IOException{
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
			} else {
				type = "file";
			}

		} else {
			type = "exception_android";
		}
		return type;
	}
	
	
	
	/**
	 * ���ݴ�����ļ��������������кŵ�lxr���ݿ��в��type��Ϣ����
	 * @param filename
	 * @param symbolname
	 * @param lineNo
	 * @return
	 * @throws SQLException
	 */
	public String getType(String filename,String symbolname,int lineNo) throws SQLException{
		String type = "";
		Statement stmt = ConnectionUtil.getLxrConnection().createStatement();
		
		ResultSet rs = stmt
				.executeQuery("select d.declaration from lxr_declarations AS d WHERE d.declid = (SELECT i.type from lxr_indexes AS i WHERE i.symid = (SELECT s.symid from lxr_symbols AS s WHERE s.symname='"
						+ symbolname
						+ "') and i.fileid = (SELECT f.fileid from lxr_files AS f WHERE f.filename='"
						+ filename
						+ "') and i.line = "
						+ lineNo + ");");
		if(rs.next()){
			type = rs.getString(1);
		}else{
			type = "UNKNOWN";
		}
		
		rs.close();
		
		
		stmt.close();
		
		return type;
	}

	public static void main(String[] args) throws IOException {
		CommentClassifier cc = new CommentClassifier();
		cc.classifyComment();
		System.out.println("CommentClassifier done");
	}
}
