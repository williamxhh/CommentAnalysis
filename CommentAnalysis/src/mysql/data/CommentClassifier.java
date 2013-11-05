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
	// ���map�Ƿ����ʱ���ã��ж�����ע�����࣬����type����Ϊ����������ͬ��writer������ͬ���ļ�����д
	static Map<String,PrintWriter> WRITERMAP = new HashMap<String, PrintWriter>(); 
	//���Set���ڲ�֪��Ҫ�����ע���ж�����type��ʱ����ͳ���õģ���Ϊset���ᱣ���ظ�������
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
	 * ����ͬ����writer��ʼ��
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
		
		//ע������������android�Ĳ���ע�ͣ�Ϊ�˳���׳�ԣ��ؼ���������࣬��ʵ���ϲ���һ����ʵ������ķ���
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
	 * �ͷ����е�writer��Դ
	 */
	static void closeWriter(){
		for(Map.Entry<String, PrintWriter> entry:WRITERMAP.entrySet()){
			entry.getValue().flush();
			entry.getValue().close();
		}
	}

	
	/**
	 * �ӹ��˴����Ժ��ע���ļ�filteredComment.txt�ж�ȡ��Ȼ���жϱ�ע�Ͷ������𣬽���ͬ���ע�ͱ��浽��ͬ���ļ���
	 * @throws SQLException
	 */
	@SuppressWarnings("resource")
	static void classifyComment() throws SQLException{
		//����ͬ����writer��ʼ��
		prepareWriter();
		
		//ͨ�����϶�ȡ����һ��ע�����ݣ�����һ����д��
		StringBuilder comment = new StringBuilder();
		try {
			BufferedReader reader = new BufferedReader(
					new FileReader(DataSource.ROOTPATH+"/filteredComment.txt"));
			
			//�Ȱ����ж��������ж�һ����𣬳�ʼ���� currentWriter
			String line = reader.readLine();
			comment.append(line+"\r\n");
			//ȥ��ǰ��� ##**## �ٴ��� ����ȡ��Ӧ��typeֵ���õ���Ӧ��writer
			PrintWriter currentWriter = WRITERMAP.get(getType(line.substring(6)));
			
			while ((line = reader.readLine()) != null) {
				//ÿ��ע�Ͷ���##**##��ͷ������һ�в�����##**##��ͷ�Ļ����Ǿͱ�ʾ����ǰһ��ͬ����һ��ע�ͣ�����ֱ����comment����׷�Ӽ���
				if(!line.startsWith("##**##")){
					if(line.trim().length()!=0)
						comment.append(line+"\r\n");
				}else{
				//�����ǰ����ע���Ѿ���ɣ���ô��������棬Ȼ������comment����ʼ������һ��ע��
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
		
		//�ͷ����е�writer��Դ
		closeWriter();
	}
	
	/**
	 * ����һ��ע�͵ı��⣬��ȡ���е��ļ��������������кŵ���Ϣ�����������������ע�͵�����
	 * 
	 * 
	 * @param line  һ�е���������������  /arch/arm/boot/compressed/atags_to_fdt.c/atags_to_fdt(0047)(linux-3.5.4)
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
	 * ���ݴ�����ļ��������������кŵ�lxr���ݿ��в��type��Ϣ����
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
