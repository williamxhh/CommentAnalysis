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
	 * 过滤掉模板中的纯英文
	 * @param input  原始模板
	 * @return   过滤掉英文片段的模板
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
					
					//纯英文的片段就不放入模板中了
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
				current_comment_seg = word_seg.segmentation(comment, WordSeg.NO_POS_TAG, seg_filter_withPunc);   //此时要复现模板的效果，不过滤标点符号
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

		// 填充comment表
		
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
						// 单 \ 转双 \\， \" 转 \\"
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
		

		// 填充template
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
		
		
		//填充 new_template
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
	 * 获取一个title描述所实际涉及的源代码文件名
	 * 
	 * @param line
	 *            一个title描述，如
	 *            /virt/kvm/kvm_main.c/kvm_set_pfn_dirty(1258)(linux-3.5.4)
	 * @return 源代码文件名 如 /virt/kvm/kvm_main.c
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
	 * 从文件中加载注释的lxr类型
	 * @return 反馈注释类型的Map, key是path，value是类型
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
	 * 基于一个注释的标题，抽取其中的文件名，变量名，行号等信息，进而查出接下来的注释的类型
	 * 
	 * 
	 * @param line
	 *            一行的内容类似于这样
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
	 * 根据传入的文件名，符号名和行号到lxr数据库中查出type信息返回
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
		// 将原注释中的html标签， 以及 [[Category:]] [[File:]]这样的东西处理掉
//		FilterBase filter = new CategoryTagFilter(new HtmlFilter(
//				new DoNothingFilter()));
		return filterComment(seg_filter, content);
	}

	public String filterComment(FilterBase filter, String content) {
		return filter.getText(content);
	}

	public int countFileTag(String content) {
		int count = 0;
		String regex = "\\[\\[File[:|：][[^\\s]*\\]\\]]*";
		Matcher m = Pattern.compile(regex).matcher(content);
		while (m.find()) {
			count++;
		}
		return count;
	}
	
	/**
	 * 默认即是从文件加载
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
	 * 获取指定 源代码路径的注释内容
	 * 
	 * @param pageTitle
	 *            要获取注释内容的源代码路径
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
			// 注释内容在数据库中是以Blob对象存的，取出来的时候，要把blob对象再转成文本
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
			// assert count==1:"有多于1的情况";
			// }
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return sb.toString();
	}

	/**
	 * 得到所有已有analyst写的文件和标识符注释 的 源代码路径，写入到文件path中
	 * 文件和标识符注释内容的page_namespace=0。而模块注释的page_namespace=14
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
	 * 识别当前类别的注释下，传入的linux源代码的文件名的所有注释中，是否有模板存在，提取出模板
	 * 现在的模板提取策略是先对每条注释逐行处理，先用“：”分隔，识别每个“：”前面的中文汉字为候选集
	 * 如果候选集中的字符串出现的次数超过了该源代码文件总注释条数的一半，则输出
	 * 
	 * @param filename
	 *            传入的linux源代码的文件名
	 * @param lxrType
	 *            注释的lxr类型
	 * @throws SQLException
	 */
	public Set<String> extractTemplate(String filename, String lxrType,
			int extractPolicy) throws SQLException {
		// 先拿到该文件的所有注释
		List<String> fileComments = this.getFileComments(filename, lxrType);
		return extractTemplate(fileComments, extractPolicy);
	}

	public Set<String> extractTemplate(List<String> fileComments,
			int extractPolicy) {
		Map<String, Integer> candidateCount = new HashMap<String, Integer>();
		for (String c : fileComments) {
			// 对每条注释逐行处理，抽取出候选集
			Set<String> splits = splitComments(c);
			// 处理不同的注释时，才累加，一条注释内的高频词，不会重复统计
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
	 * 输入一个linux源代码的文件名，查找出当前分类lxrType下所有该文件的注释
	 * 
	 * @param filename
	 *            传入的linux源代码的文件名
	 * @param lxrType
	 *            要查询的lxr类型
	 * @return 注释列表，每一条是一条注释
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
		// 注释内容在数据库中是以Blob对象存的，取出来的时候，要把blob对象再转成文本
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
	 * 输入一个注释路径，查找出当前分类lxrType下所有该文件的注释, 除掉当前注释
	 * 
	 * @param path
	 *            注释路径名
	 * @param lxrType
	 *            要查询的lxr类型
	 * @return 注释列表，每一条是一条注释，key是path，value是注释内容
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
	 * 输入一个linux源代码的文件名，查找出当前分类lxrType下所有该文件的注释(不同与getFileComments(String filename, String lxrType)的是，本方法是从文件加载的)
	 * 
	 * @param filename
	 *            传入的linux源代码的文件名
	 * @param lxrType
	 *            要查询的lxr类型
	 * @return 注释列表，每一条是一条注释
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
	 * 获取一个linux源代码下的所有注释，按lxr类别归类统计注释条数
	 * 
	 * @param filename
	 *            传入的linux源代码的文件名
	 * @return 按lxr类别归类统计注释条数
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
	 * 对每条注释逐行处理，抽取出候选集
	 * 因为一条comment可能包含多行，此函数将comment逐行处理，每一行再判断用：分隔以后的结果，最后用set保存结果
	 * ，以保证在一条注释内的高频词，不会重复统计
	 * 
	 * @param comment
	 * @return
	 */
	private Set<String> splitComments(String comment) {
		Set<String> splits = new HashSet<String>();
		// 使用了一个过滤器，这个过滤器会保留最后连续的中文字符串，将其他的标点，英文等去掉
		// 传给过滤器的已经是被：分隔处理过了字符串了，所以最后的连续中文字符串就是原文中紧挨着：的描述
		TemplateCandidateFilter filter = new TemplateCandidateFilter();
		String[] lines = comment.split("\n");
		for (String line : lines) {
			line = line.trim();
			// 中文冒号或者英文冒号都要采用
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
	 * 从传入的注释内容中过滤掉模板的内容，返回过滤模板信息以后的注释
	 * 
	 * @param comment
	 *            传入的注释
	 * @param templates
	 *            传入的模板信息
	 * @return
	 */
	public String removeTemplateFromComment(String comment,
			Set<String> templates) {
		comment = comment.trim();
		for (String template : templates) {
			comment = comment.replaceAll("n?[[\\s|\\pP]&&[^\\n]]*" + template
					+ "(\\s)*[:|：]", "");
		}
		return comment;
	}

	/**
	 * 如果发现模板中有模板包含短模板的现象，就将短的从模板集合中去掉。
	 * 
	 * @param templates
	 * @return
	 */
	public Set<String> removeDuplicatedSubTemplate(Set<String> templates) {
		List<String> templatesList = new ArrayList<String>();
		templatesList.addAll(templates);
		// 将templatesList中的模板按照从长到短的排列
		Collections.sort(templatesList, new Comparator<String>() {
			@Override
			public int compare(String str1, String str2) {
				return str2.length() - str1.length();
			}
		});
		// 如果发现模板中有模板包含短模板的现象，就将短的从模板集合中去掉。
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
				//TODO: 因为提取模板时，先过滤了标点，所以会将注释中连续的变量名放入模板中，匹配时，现在去掉
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
