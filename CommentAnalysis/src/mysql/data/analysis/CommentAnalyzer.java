package mysql.data.analysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import tool.nlpir.WordSeg;
import mysql.data.algorithms.AlgorithmsUtil;
import mysql.data.analysisDB.entity.CommentTableInfo;
import mysql.data.analysisDB.entity.TemplateTableInfo;
import mysql.data.filter.CategoryTagFilter;
import mysql.data.filter.DoNothingFilter;
import mysql.data.filter.FilterBase;
import mysql.data.filter.HtmlFilter;
import mysql.data.filter.IscasLinkFilter;
import mysql.data.filter.PunctuationFilter;
import mysql.data.filter.SourceCodeLineByLineCommentFilter;
import mysql.data.filter.TemplateCandidateFilter;
import mysql.data.util.PropertiesUtil;

public class CommentAnalyzer {
	private static Logger logger = Logger.getLogger(CommentAnalyzer.class);
	public static boolean LOADDATATODB = false;
	private FilterBase seg_filter = new CategoryTagFilter(new HtmlFilter(new IscasLinkFilter(new PunctuationFilter(new SourceCodeLineByLineCommentFilter(new  DoNothingFilter())))));
	private FilterBase seg_filter_withPunc = new CategoryTagFilter(new HtmlFilter(new IscasLinkFilter(new SourceCodeLineByLineCommentFilter(new DoNothingFilter()))));
	private Properties props;
	private Connection source_conn;
	private Connection storage_conn;
	private Connection lxr_conn;
	private Map<String, String> allComments = null;
	private Map<String, String> comment_types = null;
	private boolean loadFromFile;

	public CommentAnalyzer(boolean loadFromFile) {
		this.loadFromFile = loadFromFile;
		props = PropertiesUtil.getProperties();
		StringBuilder sourceurl = new StringBuilder();
		sourceurl
				.append("jdbc:mysql://")
				.append(props.getProperty("mysql.data.DataSource.dbserver.ip",
						"192.168.160.131"))
				.append(":")
				.append(props.getProperty(
						"mysql.data.DataSource.dbserver.port", "3306"))
				.append("/")
				.append(props.getProperty("mysql.data.DataSource.commentdb",
						"pku_comment"))
				.append("?user=")
				.append(props.getProperty(
						"mysql.data.DataSource.dbserver.user", "root"))
				.append("&password=")
				.append(props.getProperty(
						"mysql.data.DataSource.dbserver.pass", "123123"));

		StringBuilder storageurl = new StringBuilder();
		storageurl
				.append("jdbc:mysql://")
				.append(props.getProperty("mysql.data.DataSource.dbserver.ip",
						"192.168.160.131"))
				.append(":")
				.append(props.getProperty(
						"mysql.data.DataSource.dbserver.port", "3306"))
				.append("/")
				.append(props
						.getProperty(
								"mysql.data.analysis.CommentAnalyzer.commentAnalysisdb",
								"pkuCommentAnalysis"))
				.append("?user=")
				.append(props.getProperty(
						"mysql.data.DataSource.dbserver.user", "root"))
				.append("&password=")
				.append(props.getProperty(
						"mysql.data.DataSource.dbserver.pass", "123123"));

		StringBuilder lxrurl = new StringBuilder();
		lxrurl.append("jdbc:mysql://")
				.append(props.getProperty("mysql.data.DataSource.dbserver.ip",
						"192.168.160.131"))
				.append(":")
				.append(props.getProperty(
						"mysql.data.DataSource.dbserver.port", "3306"))
				.append("/")
				.append(props.getProperty("mysql.data.CommentClassifier.lxrdb",
						"lxr"))
				.append("?user=")
				.append(props.getProperty(
						"mysql.data.DataSource.dbserver.user", "root"))
				.append("&password=")
				.append(props.getProperty(
						"mysql.data.DataSource.dbserver.pass", "123123"));

		try {
			Class.forName("com.mysql.jdbc.Driver");
			this.source_conn = DriverManager
					.getConnection(sourceurl.toString());
			this.storage_conn = DriverManager.getConnection(storageurl
					.toString());
			this.lxr_conn = DriverManager.getConnection(lxrurl.toString());

		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void closeDBConnection() {
		try {
			if (this.source_conn != null && !this.source_conn.isClosed()) {
				source_conn.close();
			}
			if (this.storage_conn != null && !this.storage_conn.isClosed()) {
				storage_conn.close();
			}
			if (this.lxr_conn != null && !this.lxr_conn.isClosed()) {
				lxr_conn.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws Exception {
		logger.setLevel(Level.INFO);
		boolean loadFromFile = true;
		CommentAnalyzer ca = new CommentAnalyzer(loadFromFile);
//		LOADDATATODB = true;
		if (LOADDATATODB) {
			ca.loadDataToAnalysisDB(false, false, true);
		}
//		ca.extractNewTemplate();
		logger.info("done");
	}
	
	/**
	 * ���˵�ģ���еĴ�Ӣ��
	 * @param input  ԭʼģ��
	 * @return   ���˵�Ӣ��Ƭ�ε�ģ��
	 */
	private List<String> filterTemplate(List<String> input) {
		List<String> result = new ArrayList<String>();
		for(String s: input) {
			if(s.replaceAll("[^\u4e00-\u9fa5]", "").equals("")){
				continue;
			} else {
				result.add(s);
			}
		}
		return result;
	}
	
	public Map<String, List<String>> extractNewTemplate() {
		Map<String, List<String>> result = new TreeMap<String, List<String>>();
		logger.info("extractNewTemplate");
		try {
			WordSeg word_seg = new WordSeg();
			AlgorithmsUtil algo = new AlgorithmsUtil();
			
			PrintWriter writer = new PrintWriter(new File(props.getProperty("mysql.data.DataSource.rootPath") + "/" + props.getProperty("mysql.data.analysis.CommentAnalyzer.newTemplateFile")));
			PrintWriter txtwriter = new PrintWriter(new File(props.getProperty("mysql.data.DataSource.rootPath") + "/" + props.getProperty("mysql.data.analysis.CommentAnalyzer.newTemplateFile") + ".txt"));
			allComments = getAllComments();
			comment_types = loadCommentsTypes();
			
			int counter = 0;
			
			for(Map.Entry<String, String> entry: allComments.entrySet()) {
				String path = entry.getKey();
				++counter;
				logger.info("#" + counter + "\t" + path);
				String comment = entry.getValue();
				
//				path = "/block/deadline-iosched.c/SHOW_FUNCTION(0395)(linux-3.5.4)";
//				comment = allComments.get(path);
				
				if(comment_types.get(path).equals("file")){
					continue;
				}
				
				writer.write(path + "," + comment_types.get(path).replaceAll(",", " ") + ",");
				txtwriter.write(props.getProperty("mysql.data.analysis.TXTCommentAnalyzer.commentSpliter") + "\t" + path + "\r\n");
				
				txtwriter.write(seg_filter_withPunc.getText(comment) + "\r\n" + props.getProperty("mysql.data.analysis.TXTCommentAnalyzer.commentSpliter") + "\r\n");
				
				Map<String, String> fileComments = getFileCommentsWithPath(path, comment_types.get(path));
				
				int longest_template_len = 0;
				List<String> longest_template = null;
				List<String> current_comment_seg = word_seg.segmentation(comment, WordSeg.NO_POS_TAG, seg_filter);
				for(String other_comment : fileComments.values()) {
					List<String> comment_seg = word_seg.segmentation(other_comment, WordSeg.NO_POS_TAG, seg_filter);
					
					//��Ӣ�ĵ�Ƭ�ξͲ�����ģ������
					List<String> temp_template = filterTemplate(algo.longestCommonString(current_comment_seg, comment_seg));
					
					int len = algo.sumLen(temp_template);
					if(len > longest_template_len) {
						longest_template_len = len;
						longest_template = temp_template;
					}
				}
				
				int current = algo.sumLen(current_comment_seg);
				writer.write(current + ",");
				int remain = algo.sumLen(current_comment_seg) - longest_template_len;
				writer.write(remain + ",");
				double ratio = (double) remain / current;
				writer.write(ratio + ",");
				
				if(current_comment_seg != null) {
//					txtwriter.write(current_comment_seg.toString() + "\r\n");
				}else {
					txtwriter.write("current_comment_seg is null" + "\r\n");
					writer.write("\r\n");
					txtwriter.write("\r\n\r\n");
					continue;
				}
				
				if(longest_template != null) {
//					txtwriter.write(longest_template.toString() + "\r\n");
				} else {
					txtwriter.write("longest_template is null" + "\r\n");
					writer.write("\r\n");
					txtwriter.write("\r\n\r\n");
					continue;
				}
				
				
				int nextIndex = 0;
				int nextIndexInTemplate = 0;
				StringBuilder template_format = new StringBuilder();
				current_comment_seg = word_seg.segmentation(comment, WordSeg.NO_POS_TAG, seg_filter_withPunc);   //��ʱҪ����ģ���Ч���������˱�����
				while(nextIndex < current_comment_seg.size() && nextIndexInTemplate < longest_template.size()) {
					if(!seg_filter.getText(current_comment_seg.get(nextIndex)).equals(longest_template.get(nextIndexInTemplate))){
						writer.write(current_comment_seg.get(nextIndex) + ";");
//						template_format.append(" [").append(current_comment_seg.get(nextIndex)).append("] ");
						template_format.append(" ");
						++nextIndex;
					} else {
						template_format.append(current_comment_seg.get(nextIndex));
						++nextIndex;
						++nextIndexInTemplate;
					}
				}
				
				while(nextIndex < current_comment_seg.size()) {
					writer.write(current_comment_seg.get(nextIndex) + ";");
//					template_format.append(" [").append(current_comment_seg.get(nextIndex)).append("] ");
					template_format.append(" ");
					++nextIndex;
				}
				
				txtwriter.write(template_format.toString());
				
				writer.write("\r\n");
				txtwriter.write("\r\n\r\n");
				result.put(path, longest_template);
				logger.info("#finish" + "\t" + path);
			}
			
			writer.flush();
			writer.close();
			txtwriter.flush();
			txtwriter.close();
			
			word_seg.exit();
			
		} catch (SQLException | IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return result;
	}

	public String getSourceCode(String file, int startLine, int lineCount)
			throws IOException {
		File sourceFile = new File(this.props.getProperty(
				"mysql.data.analysis.CommentAnalyzer.sourcecodeDir",
				"linux-3.5.4/")
				+ file);
		BufferedReader reader = new BufferedReader(new FileReader(sourceFile));
		int lineNo = 1;
		while (lineNo < startLine) {
			reader.readLine();
			lineNo++;
		}
		StringBuffer sourcecode = new StringBuffer();
		for (int i = 0; i < lineCount; i++) {
			sourcecode.append(reader.readLine() + "\r\n");
		}
		reader.close();
		return sourcecode.toString();
	}

	public void loadDataToAnalysisDB(boolean load_comment, boolean load_template, boolean load_newtemplate) throws SQLException, IOException {
		Map<String, String> comments = getAllComments(loadFromFile);
		Statement stmt = storage_conn.createStatement();

		// ���comment��
		
		if(load_comment){
			for (Map.Entry<String, String> entry : comments.entrySet()) {
				StringBuilder sql = new StringBuilder();
				String path = entry.getKey().trim();
				String content = entry.getValue().trim();
				sql.append(
						"insert into comment(comment_path,origin_comment,filtered_comment,filetag_count,lxr_type,path_file) values(\"")
						.append(path)
						.append("\",\"")
						// TODO:BUG WITH
						// ##**##/arch/arm/kernel/kprobes-test.h/TESTCASE_END(0179)(linux-3.5.4)
						// �� \ ת˫ \\�� \" ת \\"
						.append(content.replaceAll("\\\\", "\\\\\\\\").replaceAll(
								"\\\"", "\\\\\""))
						.append("\",\"")
						.append(filterComment(content).replaceAll("\\\\",
								"\\\\\\\\").replaceAll("\\\"", "\\\\\""))
						.append("\",").append(countFileTag(content)).append(",\"")
						.append(getType(path, loadFromFile)).append("\",\"")
						.append(getCommentFileName(path)).append("\");");
				stmt.executeUpdate(sql.toString());
				logger.info("inserted " + path);
			}
		}
		

		// ���template
		if(load_template){
			for (String lxrtype : getAllCommentedLxrtypes()) {
				for (String filePath : getAllCommentedFilepath()) {
					List<String> fileComments = getFileComments(filePath, lxrtype);
					Set<String> strictTemplate = extractTemplate(fileComments,
							TXTCommentAnalyzer.EXTRACTPOLICY_STRICT);
					Set<String> middleTemplate = extractTemplate(fileComments,
							TXTCommentAnalyzer.EXTRACTPOLICY_MIDDLE);
					Set<String> looseTemplate = extractTemplate(fileComments,
							TXTCommentAnalyzer.EXTRACTPOLICY_LOOSE);
					StringBuilder sql = new StringBuilder();
					sql.append(
							"insert into template(path_file,lxr_type,strict_template,middle_template,loose_template) values(\"")
							.append(filePath).append("\",\"").append(lxrtype)
							.append("\",\"");
					for (String t : strictTemplate) {
						sql.append(t + ";");
					}
					sql.append("\",\"");
					for (String t : middleTemplate) {
						sql.append(t + ";");
					}
					sql.append("\",\"");
					for (String t : looseTemplate) {
						sql.append(t + ";");
					}
					sql.append("\");");
					logger.info(filePath + "#" + lxrtype);
					stmt.executeUpdate(sql.toString());
				}
			}
		}
		
		
		//��� new_template
		if(load_newtemplate){
			comment_types = loadCommentsTypes();
			for(Map.Entry<String, List<String>> entry : extractNewTemplate().entrySet()) {
				String path = entry.getKey();
				List<String> template = entry.getValue();
				StringBuilder sql = new StringBuilder();
				sql.append("insert into new_template(path_file, lxr_type, new_template) values(\"")
					.append(path)
					.append("\",\"")
					.append(comment_types.get(path))
					.append("\",\"");
				for(String s: template) {
					sql.append(s + ";");
				}
				sql.append("\");");
				stmt.executeUpdate(sql.toString());
				logger.info(path);
			}
		}
		
		
		stmt.close();
		closeDBConnection();
	}

	/**
	 * ��ȡһ��title������ʵ���漰��Դ�����ļ���
	 * 
	 * @param line
	 *            һ��title��������
	 *            /virt/kvm/kvm_main.c/kvm_set_pfn_dirty(1258)(linux-3.5.4)
	 * @return Դ�����ļ��� �� /virt/kvm/kvm_main.c
	 * @throws IOException
	 * @throws SQLException
	 */
	public String getCommentFileName(String line) throws SQLException,
			IOException {
		if (!getType(line, loadFromFile).equals("file")) {
			return line.substring(0, line.lastIndexOf("/"));
		}
		return line.replace("(linux-3.5.4)", "");
	}
	
	
	/**
	 * ���ļ��м���ע�͵�lxr����
	 * @return ����ע�����͵�Map, key��path��value������
	 */
	public Map<String, String> loadCommentsTypes() {
		if(comment_types == null) {
			try {
				comment_types = new HashMap<String, String>();
				BufferedReader reader = new BufferedReader(new FileReader(PropertiesUtil.getProperties().getProperty("mysql.data.CommentClassifier.commentsAndTypes")));
				String oneline = "";
				while((oneline = reader.readLine()) != null) {
					String[] splits = oneline.split(",");
					if(splits.length == 2)
						comment_types.put(splits[0].trim(), splits[1].trim());
					else
						comment_types.put(splits[0].trim(), oneline.substring(splits[0].length() + 1));
				}
				reader.close();
				
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return comment_types;
	}

	/**
	 * ����һ��ע�͵ı��⣬��ȡ���е��ļ��������������кŵ���Ϣ�����������������ע�͵�����
	 * 
	 * 
	 * @param line
	 *            һ�е���������������
	 *            /arch/arm/boot/compressed/atags_to_fdt.c/atags_to_fdt
	 *            (0047)(linux-3.5.4)
	 * @return
	 * @throws SQLException
	 * @throws IOException
	 */
	public String getType(String line, boolean loadFromFile)
			throws SQLException, IOException {
		String comm_filename = "";
		String comm_symname = "";
		int comm_linenoInFile = -1;
		String type = "";

		if (loadFromFile) {
			comment_types = loadCommentsTypes();
			type = comment_types.get(line);
		} else {
			if (line.contains("(linux-3.5.4)")) {
				int pos = line.lastIndexOf("(linux-3.5.4)");
				if (line.charAt(pos - 1) != ')') {
					comm_filename = line.substring(0, pos);
				} else {
					line = line.substring(0, pos);
					int pos_lineNo = line.lastIndexOf("(") + 1;
					comm_linenoInFile = Integer.parseInt(line.substring(
							pos_lineNo, line.length() - 1));
					int pos_slash = line.lastIndexOf("/");
					comm_filename = line.substring(0, pos_slash);
					comm_symname = line
							.substring(pos_slash + 1, pos_lineNo - 1);
				}

				if (!comm_symname.equals("") && comm_linenoInFile != -1) {
					type = getType(comm_filename, comm_symname,
							comm_linenoInFile);
				} else {
					type = "file";
				}

			} else {
				type = "exception_android";
			}
		}
		return type;
	}

	/**
	 * ���ݴ�����ļ��������������кŵ�lxr���ݿ��в��type��Ϣ����
	 * 
	 * @param filename
	 * @param symbolname
	 * @param lineNo
	 * @return
	 * @throws SQLException
	 */
	public String getType(String filename, String symbolname, int lineNo)
			throws SQLException {
		String type = "";
		Statement stmt = lxr_conn.createStatement();

		ResultSet rs = stmt
				.executeQuery("select d.declaration from lxr_declarations AS d WHERE d.declid = (SELECT i.type from lxr_indexes AS i WHERE i.symid = (SELECT s.symid from lxr_symbols AS s WHERE s.symname='"
						+ symbolname
						+ "') and i.fileid = (SELECT f.fileid from lxr_files AS f WHERE f.filename='"
						+ filename + "') and i.line = " + lineNo + ");");
		rs.next();
		type = rs.getString(1);
		rs.close();
		stmt.close();
		return type;
	}

	public String filterComment(String content) {
		// ��ԭע���е�html��ǩ�� �Լ� [[Category:]] [[File:]]�����Ķ��������
//		FilterBase filter = new CategoryTagFilter(new HtmlFilter(
//				new DoNothingFilter()));
		return filterComment(seg_filter, content);
	}

	public String filterComment(FilterBase filter, String content) {
		return filter.getText(content);
	}

	public int countFileTag(String content) {
		int count = 0;
		String regex = "\\[\\[File[:|��][[^\\s]*\\]\\]]*";
		Matcher m = Pattern.compile(regex).matcher(content);
		while (m.find()) {
			count++;
		}
		return count;
	}
	
	/**
	 * Ĭ�ϼ��Ǵ��ļ�����
	 * @return
	 */
	public Map<String, String> getAllComments() {
		try {
			return getAllComments(true);
		} catch (SQLException | IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public Map<String, String> getAllComments(boolean loadFromFile)
			throws SQLException, IOException {
		if(allComments == null) {
			if (loadFromFile) {
				allComments = TXTCommentAnalyzer
						.readContentToMap(
								new File(
										props.getProperty("mysql.data.DataSource.rootPath")
												+ props.getProperty("mysql.data.DataSource.allCommentsFile")),
								props.getProperty("mysql.data.analysis.TXTCommentAnalyzer.commentSpliter"));
			} else {
				allComments = new TreeMap<String, String>();
				Statement stmt = this.source_conn.createStatement();
				for (String pageTitle : getModules()) {
					allComments.put(pageTitle, getComment(stmt, pageTitle));
				}
				stmt.close();
			}
		}
		return allComments;
	}

	/**
	 * ��ȡָ�� Դ����·����ע������
	 * 
	 * @param pageTitle
	 *            Ҫ��ȡע�����ݵ�Դ����·��
	 * @return
	 * @throws SQLException
	 */
	private String getComment(Statement stmt, String pageTitle)
			throws SQLException {
		StringBuilder sb = new StringBuilder();
		try {
			ResultSet rs = stmt
					.executeQuery("select c.old_text as content from "
							+ "(revision as b inner join text as c on b.rev_text_id = c.old_id) "
							+ "inner join page as a on a.page_latest = b.rev_id where a.page_title='"
							+ pageTitle + "'");
			// ע�����������ݿ�������Blob�����ģ�ȡ������ʱ��Ҫ��blob������ת���ı�
			while (rs.next()) {
				Blob blob = rs.getBlob(1);
				int len = (int) blob.length();
				byte[] data = blob.getBytes(1, len);
				sb.append(new String(data, Charset.forName("utf-8"))
						+ "\r\n\r\n\r\n");
			}
			// ResultSet rs =
			// stmt.executeQuery("select count(*) from page where page_title='"+pageTitle+"'");
			// while(rs.next()){
			// int count = rs.getInt(1);
			// assert count==1:"�ж���1�����";
			// }
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return sb.toString();
	}

	/**
	 * �õ���������analystд���ļ��ͱ�ʶ��ע�� �� Դ����·����д�뵽�ļ�path��
	 * �ļ��ͱ�ʶ��ע�����ݵ�page_namespace=0����ģ��ע�͵�page_namespace=14
	 * 
	 * @throws SQLException
	 */
	public ArrayList<String> getModules() throws SQLException {
		ArrayList<String> allModules = new ArrayList<String>();
		Statement stmt = source_conn.createStatement();
		ResultSet rs;
		try {
			rs = stmt
					.executeQuery("select distinct(a.page_title) from page as a inner join revision as b "
							+ "on a.page_id=b.rev_page where a.page_namespace=0 and b.rev_user in "
							+ "(select ug_user from user_groups where ug_group = 'analyst');");

			while (rs.next()) {
				if (rs.getString(1).startsWith("/"))
					allModules.add(rs.getString(1));
			}

			rs.close();
			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return allModules;
	}

	/**
	 * ʶ��ǰ����ע���£������linuxԴ������ļ���������ע���У��Ƿ���ģ����ڣ���ȡ��ģ��
	 * ���ڵ�ģ����ȡ�������ȶ�ÿ��ע�����д������á������ָ���ʶ��ÿ��������ǰ������ĺ���Ϊ��ѡ��
	 * �����ѡ���е��ַ������ֵĴ��������˸�Դ�����ļ���ע��������һ�룬�����
	 * 
	 * @param filename
	 *            �����linuxԴ������ļ���
	 * @param lxrType
	 *            ע�͵�lxr����
	 * @throws SQLException
	 */
	public Set<String> extractTemplate(String filename, String lxrType,
			int extractPolicy) throws SQLException {
		// ���õ����ļ�������ע��
		List<String> fileComments = this.getFileComments(filename, lxrType);
		return extractTemplate(fileComments, extractPolicy);
	}

	public Set<String> extractTemplate(List<String> fileComments,
			int extractPolicy) {
		Map<String, Integer> candidateCount = new HashMap<String, Integer>();
		for (String c : fileComments) {
			// ��ÿ��ע�����д�����ȡ����ѡ��
			Set<String> splits = splitComments(c);
			// ����ͬ��ע��ʱ�����ۼӣ�һ��ע���ڵĸ�Ƶ�ʣ������ظ�ͳ��
			for (String s : splits) {
				if (candidateCount.containsKey(s)) {
					candidateCount.put(s, candidateCount.get(s) + 1);
				} else {
					candidateCount.put(s, 1);
				}
			}
		}
		Set<String> templates = new HashSet<String>();
		int count = fileComments.size();
		for (Map.Entry<String, Integer> entry : candidateCount.entrySet()) {
			if (extractPolicy == TXTCommentAnalyzer.EXTRACTPOLICY_STRICT) {
				if (entry.getValue() >= 2 && entry.getValue() >= count / 2) {
					templates.add(entry.getKey());
				}
			} else if (extractPolicy == TXTCommentAnalyzer.EXTRACTPOLICY_MIDDLE) {
				if (entry.getValue() >= count / 2) {
					templates.add(entry.getKey());
				}
			} else if (extractPolicy == TXTCommentAnalyzer.EXTRACTPOLICY_LOOSE) {
				templates.add(entry.getKey());
			}
		}
		return removeDuplicatedSubTemplate(templates);
	}

	/**
	 * ����һ��linuxԴ������ļ��������ҳ���ǰ����lxrType�����и��ļ���ע��
	 * 
	 * @param filename
	 *            �����linuxԴ������ļ���
	 * @param lxrType
	 *            Ҫ��ѯ��lxr����
	 * @return ע���б�ÿһ����һ��ע��
	 * @throws SQLException
	 */
	public List<String> getFileComments(String filename, String lxrType)
			throws SQLException {
		List<String> c = new ArrayList<String>();
		Statement stmt = this.storage_conn.createStatement();
		StringBuilder sql = new StringBuilder();
		sql.append("select filtered_comment from comment where lxr_type='"
				+ lxrType + "' and path_file='" + filename + "';");
		ResultSet rs = stmt.executeQuery(sql.toString());
		// ע�����������ݿ�������Blob�����ģ�ȡ������ʱ��Ҫ��blob������ת���ı�
		while (rs.next()) {
			Blob blob = rs.getBlob(1);
			int len = (int) blob.length();
			byte[] data = blob.getBytes(1, len);
			c.add(new String(data, Charset.forName("utf-8")));
		}

		rs.close();
		stmt.close();
		return c;
	}
	
	/**
	 * ����һ��ע��·�������ҳ���ǰ����lxrType�����и��ļ���ע��, ������ǰע��
	 * 
	 * @param path
	 *            ע��·����
	 * @param lxrType
	 *            Ҫ��ѯ��lxr����
	 * @return ע���б�ÿһ����һ��ע�ͣ�key��path��value��ע������
	 * @throws IOException 
	 * @throws SQLException 
	 */
	public Map<String,String> getFileCommentsWithPath(String input_path, String lxrType)
			throws IOException, SQLException {
		String filename = getCommentFileName(input_path);
		Map<String, String> c = new TreeMap<String, String>();
		allComments = getAllComments();
		comment_types = loadCommentsTypes();
		for(Map.Entry<String, String> entry: allComments.entrySet()) {
			String path = entry.getKey();
			if(!path.equals(input_path) && path.startsWith(filename) && comment_types.get(path).equals(lxrType)) {
				c.put(path, entry.getValue());
			}
		}
		return c;
	}
	
	
	/**
	 * ����һ��linuxԴ������ļ��������ҳ���ǰ����lxrType�����и��ļ���ע��(��ͬ��getFileComments(String filename, String lxrType)���ǣ��������Ǵ��ļ����ص�)
	 * 
	 * @param filename
	 *            �����linuxԴ������ļ���
	 * @param lxrType
	 *            Ҫ��ѯ��lxr����
	 * @return ע���б�ÿһ����һ��ע��
	 * @throws IOException 
	 * @throws SQLException 
	 */
	public List<String> loadFileComments(String filename, String lxrType) throws SQLException, IOException {
		List<String> c = new ArrayList<String>();
		allComments = getAllComments(true);
		for(Map.Entry<String, String> entry: allComments.entrySet()) {
			if(getCommentFileName(entry.getKey()).equals(filename) && getType(entry.getKey(), true).equals(lxrType)) {
				c.add(entry.getValue());
			}
		}
		return c;
	}

	/**
	 * ��ȡһ��linuxԴ�����µ�����ע�ͣ���lxr������ͳ��ע������
	 * 
	 * @param filename
	 *            �����linuxԴ������ļ���
	 * @return ��lxr������ͳ��ע������
	 * @throws SQLException
	 */
	public Map<String, Integer> getFileComments(String filename)
			throws SQLException {
		Map<String, Integer> type_and_comments_count = new HashMap<String, Integer>();
		Statement stmt = this.storage_conn.createStatement();
		StringBuilder sql = new StringBuilder();
		sql.append("select lxr_type,count(*) from comment where path_file='"
				+ filename + "' group by lxr_type;");
		ResultSet rs = stmt.executeQuery(sql.toString());

		String type = "";
		int count = 0;
		while (rs.next()) {
			type = rs.getString(1);
			count = rs.getInt(2);
			type_and_comments_count.put(type, count);
		}

		rs.close();
		stmt.close();

		return type_and_comments_count;
	}

	public List<String> getAllCommentedLxrtypes() throws SQLException {
		List<String> lxrtypes = new ArrayList<String>();
		Statement stmt = this.storage_conn.createStatement();
		StringBuilder sql = new StringBuilder();
		sql.append("select distinct(lxr_type) from comment;");
		ResultSet rs = stmt.executeQuery(sql.toString());

		while (rs.next()) {
			String type = rs.getString(1);
			lxrtypes.add(type);
		}
		rs.close();
		stmt.close();
		return lxrtypes;
	}

	public List<String> getAllCommentedFilepath() throws SQLException {
		List<String> filePaths = new ArrayList<String>();
		Statement stmt = this.storage_conn.createStatement();
		StringBuilder sql = new StringBuilder();
		sql.append("select distinct(path_file) from comment;");
		ResultSet rs = stmt.executeQuery(sql.toString());

		while (rs.next()) {
			String path = rs.getString(1);
			filePaths.add(path);
		}
		rs.close();
		stmt.close();
		return filePaths;
	}

	/**
	 * ��ÿ��ע�����д�����ȡ����ѡ��
	 * ��Ϊһ��comment���ܰ������У��˺�����comment���д���ÿһ�����ж��ã��ָ��Ժ�Ľ���������set������
	 * ���Ա�֤��һ��ע���ڵĸ�Ƶ�ʣ������ظ�ͳ��
	 * 
	 * @param comment
	 * @return
	 */
	private Set<String> splitComments(String comment) {
		Set<String> splits = new HashSet<String>();
		// ʹ����һ��������������������ᱣ����������������ַ������������ı�㣬Ӣ�ĵ�ȥ��
		// �������������Ѿ��Ǳ����ָ���������ַ����ˣ������������������ַ�������ԭ���н����ţ�������
		TemplateCandidateFilter filter = new TemplateCandidateFilter();
		String[] lines = comment.split("\n");
		for (String line : lines) {
			line = line.trim();
			// ����ð�Ż���Ӣ��ð�Ŷ�Ҫ����
			List<String> cands = filter.getText(line);
			for (String c : cands) {
				if (c.length() > 0) {
					splits.add(c);
				}
			}
		}
		return splits;
	}

	/**
	 * �Ӵ����ע�������й��˵�ģ������ݣ����ع���ģ����Ϣ�Ժ��ע��
	 * 
	 * @param comment
	 *            �����ע��
	 * @param templates
	 *            �����ģ����Ϣ
	 * @return
	 */
	public String removeTemplateFromComment(String comment,
			Set<String> templates) {
		comment = comment.trim();
		for (String template : templates) {
			comment = comment.replaceAll("n?[[\\s|\\pP]&&[^\\n]]*" + template
					+ "(\\s)*[:|��]", "");
		}
		return comment;
	}

	/**
	 * �������ģ������ģ�������ģ������󣬾ͽ��̵Ĵ�ģ�弯����ȥ����
	 * 
	 * @param templates
	 * @return
	 */
	public Set<String> removeDuplicatedSubTemplate(Set<String> templates) {
		List<String> templatesList = new ArrayList<String>();
		templatesList.addAll(templates);
		// ��templatesList�е�ģ�尴�մӳ����̵�����
		Collections.sort(templatesList, new Comparator<String>() {
			@Override
			public int compare(String str1, String str2) {
				return str2.length() - str1.length();
			}
		});
		// �������ģ������ģ�������ģ������󣬾ͽ��̵Ĵ�ģ�弯����ȥ����
		for (int i = 0; i < templatesList.size(); i++) {
			for (int j = i + 1; j < templatesList.size(); j++) {
				if (templatesList.get(i).contains(templatesList.get(j))) {
					templates.remove(templatesList.get(j));
				}
			}
		}
		return templates;
	}

	public CommentTableInfo getCommentFromAnalysisStorage(String comment_path)
			throws SQLException {
		if (LOADDATATODB) {
			try {
				loadDataToAnalysisDB(true, false, false);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		CommentTableInfo cti = new CommentTableInfo();
		Statement stmt = this.storage_conn.createStatement();
		ResultSet rs = stmt
				.executeQuery("select * from comment where comment_path=\""
						+ comment_path + "\";");
		rs.next();
		cti.format(rs);
		rs.close();
		stmt.close();
		return cti;
	}

	public TemplateTableInfo getTemplateFromAnalysisStorage(String path_file,
			String lxr_type) throws SQLException {
		if (LOADDATATODB) {
			try {
				loadDataToAnalysisDB(false, true, false);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		TemplateTableInfo tti = new TemplateTableInfo();
		Statement stmt = this.storage_conn.createStatement();
		ResultSet rs = stmt
				.executeQuery("select * from template where path_file=\""
						+ path_file + "\" and lxr_type=\"" + lxr_type + "\";");
		rs.next();
		tti.format(rs);
		rs.close();
		stmt.close();
		return tti;
	}
	
	public String getColoredInfo(String path) {
		StringBuilder template_format = new StringBuilder();
		allComments = getAllComments();
		String comment = allComments.get(path);
		try {
			WordSeg wordSeg = new WordSeg();
			boolean reserveLineBreak = true;
			List<String> current_comment_seg = wordSeg.segmentation(comment, WordSeg.NO_POS_TAG, seg_filter_withPunc, reserveLineBreak);
			List<String> longest_template = loadNewTemplate(path);
			
			logger.info("#current_comment_seg#:\t" + current_comment_seg.toString());
			logger.info("#longest_template#:\t" + longest_template.toString());
			
			int nextIndex = 0;
			int nextIndexInTemplate = 0;
			
			while(nextIndex < current_comment_seg.size() && nextIndexInTemplate < longest_template.size()) {
				//TODO: ��Ϊ��ȡģ��ʱ���ȹ����˱�㣬���ԻὫע���������ı���������ģ���У�ƥ��ʱ������ȥ��
				if(longest_template.get(nextIndexInTemplate).replaceAll("[^\u4e00-\u9fa5]", "").equals("")){
					++nextIndexInTemplate;
					continue;
				}
				if(!seg_filter.getText(current_comment_seg.get(nextIndex)).equals(longest_template.get(nextIndexInTemplate))){
					template_format.append(current_comment_seg.get(nextIndex));
					logger.info("#APPEND#:\t" + current_comment_seg.get(nextIndex));
					++nextIndex;
				} else {
					template_format.append(getColorString(current_comment_seg.get(nextIndex)));
					logger.info("#APPEND_MATCH#:\t" + getColorString(current_comment_seg.get(nextIndex)));
					++nextIndex;
					++nextIndexInTemplate;
				}
			}
			
			while(nextIndex < current_comment_seg.size()) {
				template_format.append(current_comment_seg.get(nextIndex));
				logger.info("#APPEND#:\t" + current_comment_seg.get(nextIndex));
				++nextIndex;
			}
			
			wordSeg.exit();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return template_format.toString();
	}
	
	private String getColorString(String input) {
		StringBuilder sb = new StringBuilder();
		sb.append("<b><font color = 'red'>").append(input).append("</font></b>");
		return sb.toString();
	}
	
	public List<String> loadNewTemplate(String path) {
		List<String> template = new ArrayList<String>();
		try {
			Statement stmt = this.storage_conn.createStatement();
			ResultSet rs = stmt.executeQuery("select new_template from new_template where path_file = \"" + path + "\"");
			if(!rs.next()){
				return template;
			}
			String[] result = rs.getString(1).split(";");
			for(String s: result) {
				if(s != null && !s.equals("")){
					template.add(s);
				}
			}
			rs.close();
			stmt.close();
			return template;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return template;
		
	}

	public String getChineseComment(String commentContent) {
		return commentContent.replaceAll("[a-zA-Z]+[\\s\\pP<>]*", "");
	}

}
