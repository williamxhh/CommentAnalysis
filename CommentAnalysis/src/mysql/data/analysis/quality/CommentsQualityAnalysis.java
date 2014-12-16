package mysql.data.analysis.quality;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import tool.nlpir.WordSeg;

import java.sql.Connection;

import mysql.data.algorithms.AlgorithmsUtil;
import mysql.data.analysis.CommentAnalyzer;
import mysql.data.analysis.TXTCommentAnalyzer;
import mysql.data.analysisDB.entity.FileCommentTypeCount;
import mysql.data.analysisDB.entity.JudgeTableInfo;
import mysql.data.filter.DoNothingFilter;
import mysql.data.filter.FilterBase;
import mysql.data.filter.IscasChineseCommentExtractor;
import mysql.data.filter.IscasLinkFilter;
import mysql.data.gui.EvaluationPreparation;
import mysql.data.util.CommentEntryCount;
import mysql.data.util.ConnectionUtil;
import mysql.data.util.FileUtil;
import mysql.data.util.NLPAnalysisResult;
import mysql.data.util.PropertiesUtil;

public class CommentsQualityAnalysis {
	private static final Logger log = Logger
			.getLogger(CommentsQualityAnalysis.class);
	private static Properties props = PropertiesUtil.getProperties();
	private static String NONE_OF_THIS_TYPE = "None of This Type";

	private CommentAnalyzer ca = null;
	private Map<String, CommentEntryCount> entry_map = null;
	private Map<String, NLPAnalysisResult> nlp_result_map = null;

	// 评估规范性的时候使用
	private EvaluationPreparation ep = null;;
	private AlgorithmsUtil algo = null;
	// 保存从judge数据库中读入的结果，key是path，value是评分结果
	private Map<String, JudgeTableInfo> evaluation_map = null;;

	public static void main(String[] args) throws IOException, SQLException,
			ClassNotFoundException {
		CommentsQualityAnalysis cqa = new CommentsQualityAnalysis(true);

		// cqa.FilterLinkAndImagemap(type);

		// log.info("##validity##");
		// cqa.validityRedundantComment(type);
		// cqa.validityPasteOriginSourceCode(type);
		// cqa.validityChineseTooShort(type, 5);

		// log.info("##usefulness##");
		// cqa.usefulnessLeakRelatedCommentsCount(type);

		// log.info("##completeness##");
		// cqa.completenessCommentedTypeRatio();
		// cqa.completenessFileCommentedRatio(type);

		String path = "/";
		// String type = "function definition";
		// String type = "variable definition";
		// String type = "macro (un)definition";
		String type = "class, struct, or union member";
		// cqa.statCommentTypeDistribution(path);
		// cqa.statCommentedRatio(path);
		// cqa.statNLPAnalysis(path,type);
		// log.info(cqa.getAllEntries().get("/virt/kvm/kvm_main.c"));
		// cqa.outputNLPResultToFile(type);
		// cqa.statWordCounter();
		// cqa.sampleSubsetForEvaluation();
		// cqa.completeness(type);
		// cqa.redundantAnalysis();
		// cqa.consistencyAnalysis();
	}

	public CommentsQualityAnalysis(boolean loadFromFile) {
		ca = new CommentAnalyzer(loadFromFile);
	}

	/**
	 * 获得不同lxr类型注释的分布情况，key是lxr类型，value是注释数量
	 * 
	 * @param prefix
	 *            注释路径前缀
	 * @return
	 */
	public Map<String, Integer> statCommentTypeDistribution(String prefix) {
		Map<String, String> comment_types = ca.loadCommentsTypes();
		Map<String, Integer> type_counter = new TreeMap<String, Integer>();
		int total_count = 0;
		for (Map.Entry<String, String> entry : comment_types.entrySet()) {
			if (entry.getKey().startsWith(prefix)) {
				String type = entry.getValue();
				if (type_counter.containsKey(type)) {
					type_counter.put(type, type_counter.get(type) + 1);
				} else {
					type_counter.put(type, 1);
				}
				++total_count;
			}
		}

		for (Map.Entry<String, Integer> entry : type_counter.entrySet()) {
			log.info(entry.getKey() + "," + entry.getValue());
		}
		log.info("total comments count : " + total_count);
		return type_counter;
	}

	/**
	 * 获取不同lxr类型的注释的已注释数量和注释入口总数，得到注释覆盖度
	 * 
	 * @param prefix
	 *            注释路径前缀
	 * @return
	 * @throws SQLException
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public Map<String, String> statCommentedRatio(String prefix)
			throws SQLException, IOException, ClassNotFoundException {
		Map<String, Integer> commented_count = new TreeMap<String, Integer>();
		Map<String, Integer> total_count = new TreeMap<String, Integer>();
		Set<String> file_set = new TreeSet<String>();
		Map<String, String> comment_types = ca.loadCommentsTypes();

		Map<String, String> all_comments = ca.getAllComments();
		for (Map.Entry<String, String> entry : all_comments.entrySet()) {
			String path = entry.getKey();
			if (path.startsWith(prefix)) {
				file_set.add(ca.getCommentFileName(path));
				String type = comment_types.get(path);
				if (commented_count.containsKey(type)) {
					commented_count.put(type, commented_count.get(type) + 1);
				} else {
					commented_count.put(type, 1);
				}
			}
		}

		log.info("total file: " + file_set.size());
		for (String filename : file_set) {
			log.info(filename);
		}

		for (String file : file_set) {
			Map<String, Integer> file_count = getAllEntries(file);
			for (String type : file_count.keySet()) {
				if (total_count.containsKey(type)) {
					total_count.put(type,
							total_count.get(type) + file_count.get(type));
				} else {
					total_count.put(type, file_count.get(type));
				}
			}
		}

		Map<String, String> result = new TreeMap<String, String>();
		for (String type : total_count.keySet()) {
			if (commented_count.containsKey(type)) {
				result.put(type,
						commented_count.get(type) + "/" + total_count.get(type));
			} else {
				result.put(type, "0/" + total_count.get(type));
			}
		}

		for (Map.Entry<String, String> entry : result.entrySet()) {
			log.info(entry.getKey() + ":\t" + entry.getValue());
		}
		return result;
	}

	private void outputNLPResultToFile(String lxr_type) {
		log.setLevel(Level.INFO);
		try {
			PrintWriter writer = new PrintWriter(
					props.getProperty("mysql.data.DataSource.rootPath")
							+ "/"
							+ lxr_type
							+ "_"
							+ props.getProperty("mysql.data.analysis.quality.CommentsQualityAnalysis.lxrNLP"));

			for (String path : ca.getAllComments().keySet()) {
				log.info(path);
				String info = statNLPAnalysis(path, lxr_type);
				if (!info.equals(NONE_OF_THIS_TYPE)) {
					writer.write(path + "," + info + "\r\n");
				}
			}

			writer.flush();
			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 统计指定路径下，指定lxr类型注释的平均文本长度，平均词组长度，平均名词比率，动词比率，其他词比率等
	 * 
	 * @param prefix
	 * @param lxr_type
	 */
	public String statNLPAnalysis(String prefix, String lxr_type) {
		Set<NLPAnalysisResult> result_set = nlpAnalysis(prefix, lxr_type);

		double sum_content_len = 0;
		double sum_word_count = 0;
		double sum_noun_count = 0;
		double sum_verb_count = 0;
		double sum_other_count = 0;

		for (NLPAnalysisResult r : result_set) {
			sum_content_len += r.getContent_len();
			sum_word_count += r.getWord_list().size();
			sum_noun_count += r.getNoun_list().size();
			sum_verb_count += r.getVerb_list().size();
			sum_other_count += r.getOther_list().size();
		}

		if (result_set.size() == 0 || sum_word_count == 0) {
			return NONE_OF_THIS_TYPE;
		}

		StringBuilder sb = new StringBuilder();

		sb.append(sum_content_len / result_set.size()).append(",")
				.append(sum_word_count / result_set.size()).append(",")
				.append(sum_noun_count / sum_word_count).append(",")
				.append(sum_verb_count / sum_word_count).append(",")
				.append(sum_other_count / sum_word_count).append(",")
				.append(result_set.size());

		log.info("Content length , Word count , Noun ratio , Verb ratio , Other ratio , Number of Comments ");
		log.info(sb.toString());

		return sb.toString();
	}

	public Set<NLPAnalysisResult> nlpAnalysis(String prefix) {
		return nlpAnalysis(prefix, "ALL");
	}

	/**
	 * 获取指定路径，指定lxr类型的全部注释的nlp分析结果
	 * 
	 * @param prefix
	 * @param lxr_type
	 * @return
	 */
	public Set<NLPAnalysisResult> nlpAnalysis(String prefix, String lxr_type) {
		Set<NLPAnalysisResult> result_set = new HashSet<NLPAnalysisResult>();

		if (nlp_result_map == null) {
			File nlp_file = new File(
					props.getProperty("mysql.data.analysis.quality.CommentsQualityAnalysis.nlpResult"));
			if (nlp_file.exists()) {
				log.debug("load from file");
				try {
					ObjectInputStream ois = new ObjectInputStream(
							new FileInputStream(nlp_file));
					nlp_result_map = (Map<String, NLPAnalysisResult>) ois
							.readObject();
					ois.close();
				} catch (IOException | ClassNotFoundException e) {
					e.printStackTrace();
				}

			} else {
				log.debug("compute");
				nlp_result_map = new TreeMap<String, NLPAnalysisResult>();
				WordSeg wordSeg = new WordSeg();
				for (Map.Entry<String, String> entry : ca.getAllComments()
						.entrySet()) {
					String path = entry.getKey();
					String content = CommentAnalyzer.COMMON_FILTER
							.getText(entry.getValue());
					log.info(content);
					NLPAnalysisResult result = new NLPAnalysisResult(path);
					result.setContent_len(content.length());
					result.setWord_list(wordSeg.segmentation(content,
							WordSeg.POS_TAG, WordSeg.SEG_FILTER_WITHPUNC));
					for (String w : result.getWord_list()) {
						int index = w.lastIndexOf('/');
						if (w.charAt(index + 1) == WordSeg.NOUN) {
							result.getNoun_list().add(w.substring(0, index));
						} else if (w.charAt(index + 1) == WordSeg.VERB) {
							result.getVerb_list().add(w.substring(0, index));
						} else if (w.charAt(index + 1) != WordSeg.PUNCTUATION) {
							result.getOther_list().add(w.substring(0, index));
						}
					}
					log.info(result.getNoun_list().toString());
					log.info(result.getVerb_list().toString());
					log.info(result.getOther_list().toString());

					nlp_result_map.put(path, result);
				}

				try {
					ObjectOutputStream oos = new ObjectOutputStream(
							new FileOutputStream(nlp_file));
					oos.writeObject(nlp_result_map);
					oos.flush();
					oos.close();

				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		for (String path : nlp_result_map.keySet()) {
			if (path.startsWith(prefix)
					&& (lxr_type.equals("ALL") || ca.loadCommentsTypes()
							.get(path).equals(lxr_type))) {
				result_set.add(nlp_result_map.get(path));
			}
		}

		return result_set;

	}

	protected void prepareAnalysis() {
		ep = new EvaluationPreparation();
		algo = new AlgorithmsUtil();
		evaluation_map = ep.loadJudgeInfoFromDB();

		// 提前加载必要资源，以免多线程加载的时候出问题
		ca.getAllComments();
		ca.loadCommentsTypes();
	}

	/**
	 * 获取指定路径全部注释的冗余分析结果
	 * 
	 * @param prefix
	 *            注释路径的前缀模式
	 * @return key是lxr类型，value是 冗余数/总数
	 */
	public void redundantAnalysis() {
		prepareAnalysis();

		List<String> all_files = ca.getAllCommentedFilepath();
		for (String file : all_files) {
			Thread t = new Thread(new RedundantEvaluationTask(file));
			t.start();
		}

	}

	/**
	 * 获取指定路径全部注释的规范性分析结果
	 * 
	 * @param prefix
	 *            注释路径的前缀模式
	 * @return key是lxr类型，value是 该类型的规范性平均打分
	 * 
	 *         规范性的打分策略是 对每条注释的模板过滤停用词以后计算同文件中同类型注释模板的编辑距离的平均值 规范性打分为： 平均编辑距离 /
	 *         模板词长度 规范性打分越大，规范性越差 （注意，现在的实现中，当前注释的提取模板为空的时候，规范性打分设为平均编辑距离）
	 * 
	 * 
	 */
	public void consistencyAnalysis() {
		prepareAnalysis();

		List<String> all_files = ca.getAllCommentedFilepath();
		for (String file : all_files) {
			Thread t = new Thread(new ConsistencyEvaluationTask(file));
			t.start();
		}
	}

	/**
	 * 对所有注释做词频统计，为取停用词集合做准备
	 */
	public void statWordCounter() {
		Map<String, Integer> word_counter_map = new HashMap<String, Integer>();
		for (NLPAnalysisResult r : nlpAnalysis("/")) {
			List<String> words = r.getWord_list();
			for (String w : words) {
				if (word_counter_map.containsKey(w)) {
					word_counter_map.put(w, word_counter_map.get(w) + 1);
				} else {
					word_counter_map.put(w, 1);
				}
			}
		}
		log.setLevel(Level.INFO);
		log.info("statWordCounter begin");
		for (Map.Entry<String, Integer> entry : word_counter_map.entrySet()) {
			String content = entry.getKey().replaceAll("[^\u4e00-\u9fa5]", "")
					.trim();
			if (!content.equals("")) {
				log.info(entry.getKey().substring(0,
						entry.getKey().lastIndexOf("/"))
						+ "," + entry.getValue());
			}
		}
		log.info("statWordCounter end");
	}

	protected void sampleSubsetForEvaluation() {
		Map<String, String> all_comments = ca.getAllComments();
		Random r = new Random();
		log.setLevel(Level.DEBUG);
		for (String path : all_comments.keySet()) {
			if (r.nextInt(20) == 1) {
				log.debug(path);
			}
		}
	}

	public int validityRedundantComment(String type) throws IOException {
		Map<String, String> comments = TXTCommentAnalyzer.readContentToMap(
				FileUtil.readableFile(TXTCommentAnalyzer.DEFAULTPATH
						+ "\\validity\\noLinkAndImageMap\\CLASSIFICATION_"
						+ type + ".txt"), TXTCommentAnalyzer.DEFAULTSPLITER);
		List<String> uniqueComments = new ArrayList<String>();
		// 保存每个文件总共的注释数
		int fileCommentsTotal = 0;
		int totalRedundant = 0;
		String currentFile = "";
		for (Map.Entry<String, String> entry : comments.entrySet()) {
			String fileName = entry.getKey().substring(0,
					entry.getKey().lastIndexOf("/"));
			String commentContent = entry.getValue().replaceAll(" ", "");
			if (currentFile.equals("") || currentFile.equals(fileName)) {
				currentFile = fileName;
				fileCommentsTotal++;
				if (!uniqueComments.contains(commentContent)) {
					uniqueComments.add(commentContent);
				}
			} else {
				log.debug(currentFile + "\t fileCommentsTotal:"
						+ fileCommentsTotal + "\t uniqueComments:"
						+ uniqueComments.size());
				if (fileCommentsTotal > uniqueComments.size()) {
					log.debug(currentFile + " 有冗余注释 ######################");
				}
				totalRedundant += fileCommentsTotal - uniqueComments.size();
				currentFile = fileName;
				fileCommentsTotal = 1;

				uniqueComments.clear();
				uniqueComments.add(commentContent);
			}
		}
		log.debug(currentFile + "\t fileCommentsTotal:" + fileCommentsTotal
				+ "\t uniqueComments:" + uniqueComments.size());
		totalRedundant += fileCommentsTotal - uniqueComments.size();
		log.info("总共的冗余注释条数为:" + totalRedundant);
		return totalRedundant;
	}

	/**
	 * 存在粘贴原始代码情况的注释条数即为 *_withSourceCode.txt 中的注释条数 仅粘贴原始代码，未进行任何注释的注释条数即为
	 * *_FilterSourceCode.txt 中字符串 EMPTY!!! 的数目
	 * 
	 * 
	 * 
	 * @param type
	 * @throws IOException
	 */
	public void validityPasteOriginSourceCode(String type) throws IOException {
		String SourceCodePath = props.getProperty("linuxSource.dir");

		Map<String, String> comments = TXTCommentAnalyzer.readContentToMap(
				FileUtil.readableFile(TXTCommentAnalyzer.DEFAULTPATH
						+ "\\validity\\noLinkAndImageMap\\CLASSIFICATION_"
						+ type + ".txt"), TXTCommentAnalyzer.DEFAULTSPLITER);
		// 将包含粘贴源码的注释写入第一个文件 *_withSourceCode
		PrintWriter writer1 = new PrintWriter(
				FileUtil.writeableFile(TXTCommentAnalyzer.DEFAULTPATH
						+ "\\validity\\" + type + "_withSourceCode.txt"));
		// 将不包含粘贴源码的注释写入第二个文件 *_withoutSourceCode
		PrintWriter writer2 = new PrintWriter(
				FileUtil.writeableFile(TXTCommentAnalyzer.DEFAULTPATH
						+ "\\validity\\" + type + "_withoutSourceCode.txt"));
		// 将包含粘贴源码的注释中 对粘贴的源码进行过滤以后的注释内容写入第三个文件 *_FilterSourceCode
		PrintWriter writer3 = new PrintWriter(
				FileUtil.writeableFile(TXTCommentAnalyzer.DEFAULTPATH
						+ "\\validity\\" + type + "_FilterSourceCode.txt"));

		IscasChineseCommentExtractor extractor = new IscasChineseCommentExtractor();

		int count = 0; // 存在粘贴原始代码情况的注释条数
		int emptyCount = 0; // 仅粘贴原始代码，未进行任何注释的注释条数
		for (Map.Entry<String, String> entry : comments.entrySet()) {
			String comment = entry.getValue();

			String path = entry.getKey();
			String file = path.substring(0, path.lastIndexOf("/"));
			int index = path.indexOf("(");
			int lineNo = Integer.parseInt(path.substring(index + 1, index + 5));
			String functionPrototype = FileUtil.readAllLines(
					FileUtil.readableFile(SourceCodePath + file), "UTF-8").get(
					lineNo - 1);
			if ((comment.contains(functionPrototype))
					&& (comment.lastIndexOf(functionPrototype) > 10)) {
				writer1.write(TXTCommentAnalyzer.DEFAULTSPLITER
						+ entry.getKey() + "\r\n");
				writer3.write(TXTCommentAnalyzer.DEFAULTSPLITER
						+ entry.getKey() + "\r\n");
				writer1.write(comment + "\r\n\r\n");
				writer3.write(comment.substring(0,
						comment.lastIndexOf(functionPrototype))
						+ "\r\n");
				String str = comment.substring(comment
						.lastIndexOf(functionPrototype));
				for (String s : extractor.getText(str)) {
					writer3.write(s + "\r\n");
					if (s.equals("EMPTY!!!"))
						emptyCount++;
				}
				writer3.write("\r\n\r\n");

				count++;
			} else if (comment.contains("注释")
					&& (computeEnglishRatio(comment.substring(comment
							.lastIndexOf("注释"))) > 0.7)) {
				writer1.write(TXTCommentAnalyzer.DEFAULTSPLITER
						+ entry.getKey() + "\r\n");
				writer3.write(TXTCommentAnalyzer.DEFAULTSPLITER
						+ entry.getKey() + "\r\n");
				writer1.write(comment + "\r\n\r\n");
				writer3.write(comment.substring(0,
						comment.lastIndexOf("注释") + 2) + "\r\n");
				String str = comment.substring(comment.lastIndexOf("注释") + 2);
				for (String s : extractor.getText(str)) {
					writer3.write(s + "\r\n");
					if (s.equals("EMPTY!!!"))
						emptyCount++;
				}
				writer3.write("\r\n\r\n");

				count++;
			} else {
				writer2.write(TXTCommentAnalyzer.DEFAULTSPLITER
						+ entry.getKey() + "\r\n");
				writer2.write(comment + "\r\n\r\n");
			}

		}
		log.info("存在粘贴原始代码情况的注释条数:" + count);
		log.info("仅粘贴原始代码，未进行任何注释的注释条数:" + emptyCount);
		writer1.flush();
		writer2.flush();
		writer3.flush();
		writer1.close();
		writer2.close();
		writer3.close();

	}

	/**
	 * 此方法验证注释长度的有效性，注释长度太短的视为无效
	 * 
	 * @param type
	 *            要分析的注释类型，如function definition
	 * @param minLength
	 *            最小有效长度
	 * @return
	 * @throws IOException
	 */
	public int validityChineseTooShort(String type, int minLength)
			throws IOException {
		int invalidCount = 0;
		Map<String, String> commentsWithSourceCode = TXTCommentAnalyzer
				.readContentToMap(
						FileUtil.readableFile(TXTCommentAnalyzer.DEFAULTPATH
								+ "\\validity\\" + type
								+ "_FilterSourceCode.txt"),
						TXTCommentAnalyzer.DEFAULTSPLITER);
		Map<String, String> commentsWithoutSourceCode = TXTCommentAnalyzer
				.readContentToMap(
						FileUtil.readableFile(TXTCommentAnalyzer.DEFAULTPATH
								+ "\\validity\\" + type
								+ "_withoutSourceCode.txt"),
						TXTCommentAnalyzer.DEFAULTSPLITER);
		for (Map.Entry<String, String> entry : commentsWithSourceCode
				.entrySet()) {
			String content = entry.getValue();
			String chineseContent = content.replaceAll("[^\u4e00-\u9fa5]", "");
			if (chineseContent.length() < minLength) {
				log.debug(content);
				invalidCount++;
			}
		}
		for (Map.Entry<String, String> entry : commentsWithoutSourceCode
				.entrySet()) {
			String content = entry.getValue();
			String chineseContent = content.replaceAll("[^\u4e00-\u9fa5]", "");
			if (chineseContent.length() < minLength) {
				log.debug(content);
				invalidCount++;
			}
		}
		log.info("注释中中文字符数少于" + minLength + "的注释视为无效，有" + invalidCount + "条");
		return invalidCount;
	}

	public int usefulnessLeakRelatedCommentsCount(String type)
			throws IOException {
		int leakCount = 0;
		Map<String, String> comments = TXTCommentAnalyzer.readContentToMap(
				FileUtil.readableFile(TXTCommentAnalyzer.DEFAULTPATH
						+ "\\validity\\noLinkAndImageMap\\CLASSIFICATION_"
						+ type + ".txt"), TXTCommentAnalyzer.DEFAULTSPLITER);
		for (Map.Entry<String, String> entry : comments.entrySet()) {
			// if(entry.getValue().contains("漏洞")||entry.getValue().contains("缺陷")||entry.getValue().contains("问题")){
			if (entry.getValue().contains("疑似漏洞")) {
				log.debug(entry.getValue());
				leakCount++;
			}
		}
		log.info("漏洞相关的注释条数为：" + leakCount);
		return leakCount;
	}

	/**
	 * 从之前的analysisDB中获取一个单位的所有注释，然后到lxr数据库中去查询所有需要注释的注释入口，得到每一种lxr类型的注释完整度，
	 * 写到CommentsAndTypesRatio_*.txt文件中
	 * 
	 * @throws IOException
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 */
	public void completenessCommentedTypeRatio() throws IOException,
			SQLException, ClassNotFoundException {
		String spliter = props
				.getProperty("mysql.data.analysis.TXTCommentAnalyzer.commentSpliter");
		List<FileCommentTypeCount> commented = loadCommentedEntryFromDB();
		Map<String, Integer> fileAllEntries = null;
		PrintWriter writer = new PrintWriter(
				new FileWriter(
						props.getProperty("mysql.data.analysis.quality.CommentsQualityAnalysis.commentsTypesRatio")));
		String commentedFilePath = "";
		for (FileCommentTypeCount fileCommentType : commented) {
			if (commentedFilePath.equals("")) {
				commentedFilePath = fileCommentType.getFilePath();
				fileAllEntries = getAllEntries(commentedFilePath);
				writer.write(spliter + commentedFilePath + "\r\n");
			}
			// 同一个文件
			if (commentedFilePath.equals(fileCommentType.getFilePath())) {
				writer.write(fileCommentType.getLxrType() + ": "
						+ fileCommentType.getCount() + "/"
						+ fileAllEntries.get(fileCommentType.getLxrType())
						+ "\r\n");
			} else {
				commentedFilePath = fileCommentType.getFilePath();
				fileAllEntries = getAllEntries(commentedFilePath);
				writer.write("\r\n\r\n" + spliter + commentedFilePath + "\r\n");
				writer.write(fileCommentType.getLxrType() + ": "
						+ fileCommentType.getCount() + "/"
						+ fileAllEntries.get(fileCommentType.getLxrType())
						+ "\r\n");
			}
		}
		writer.flush();
		writer.close();
	}

	/**
	 * 查询一个单位的所有注释中一种特定lxr类型的注释在各被注释文件中的注释完整度
	 * 
	 * @param lxrType
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public void completenessFileCommentedRatio(String lxrType)
			throws IOException {
		Map<String, String> typeRatioMap = TXTCommentAnalyzer
				.readContentToMap(
						new File(
								props.getProperty("mysql.data.analysis.quality.CommentsQualityAnalysis.commentsTypesRatio")),
						TXTCommentAnalyzer.DEFAULTSPLITER);
		PrintWriter writer = new PrintWriter(
				FileUtil.writeableFile(TXTCommentAnalyzer.DEFAULTPATH
						+ "//completenesss//fileCommentedRatioBylxrType//"
						+ lxrType + ".csv"));
		for (Map.Entry<String, String> entry : typeRatioMap.entrySet()) {
			if (entry.getValue().contains(lxrType)) {
				StringTokenizer st = new StringTokenizer(entry.getValue(),
						"\r\n");
				while (st.hasMoreTokens()) {
					String line = st.nextToken();
					if (line.contains(lxrType)) {
						String result = entry.getKey()
								+ ","
								+ line.substring(line.indexOf(":") + 2)
										.replaceAll("/", ",");
						log.debug(result);
						writer.write(result + "\r\n");
					}
				}
			}
		}
		writer.flush();
		writer.close();
	}

	/**
	 * 获得指定文件中的注释入口数
	 * 
	 * @param filePath
	 * @return key是lxr类型，value是相应类型的注释入口数
	 * @throws SQLException
	 * @throws FileNotFoundException
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	public Map<String, Integer> getAllEntries(String filePath)
			throws SQLException, FileNotFoundException, ClassNotFoundException,
			IOException {
		if (entry_map == null) {
			entry_map = getAllEntries();
		}

		CommentEntryCount cec = entry_map.get(filePath);
		if (cec != null) {
			return cec.getType_count();
		}

		return null;
	}

	/**
	 * 获取全部代码的注释入口，第一次从lxr数据库读取，并将结果序列化到文件保存，后面直接从文件反序列化得到
	 * 
	 * @return
	 * @throws SQLException
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public Map<String, CommentEntryCount> getAllEntries() throws SQLException,
			FileNotFoundException, IOException, ClassNotFoundException {
		File all_entries_file = new File(
				props.getProperty("mysql.data.analysis.quality.CommentsQualityAnalysis.allEntries"));
		if (all_entries_file.exists()) {
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(
					all_entries_file));
			entry_map = (Map<String, CommentEntryCount>) ois.readObject();
			ois.close();
		} else {
			entry_map = new HashMap<String, CommentEntryCount>();
			Statement stmt = ConnectionUtil.getLxrConnection().createStatement();
			String sql = "select f.filename, d.declaration, count(*) as number from lxr_files f, lxr_declarations d, lxr_indexes i "
					+ "where f.fileid=i.fileid and i.type=d.declid and d.declaration != 'local variable' and f.fileid in (select r.fileid from "
					+ "lxr_releases r where r.releaseid='linux-3.5.4') GROUP BY f.filename, d.declaration;";
			ResultSet rs = stmt.executeQuery(sql);
			while (rs.next()) {
				String path = rs.getString(1);
				String type = rs.getString(2);
				int count = rs.getInt(3);
				CommentEntryCount entry = null;
				if (entry_map.containsKey(path)) {
					entry = entry_map.get(path);
				} else {
					entry = new CommentEntryCount(path);
					entry_map.put(path, entry);
				}
				entry.addType(type, count);
			}
			rs.close();
			stmt.close();

			ObjectOutputStream oos = new ObjectOutputStream(
					new FileOutputStream(all_entries_file));
			oos.writeObject(entry_map);
			oos.flush();
			oos.close();
		}

		return entry_map;

	}

	public List<FileCommentTypeCount> loadCommentedEntryFromDB()
			throws SQLException {
		List<FileCommentTypeCount> commented = new ArrayList<FileCommentTypeCount>();
		Statement stmt = ConnectionUtil.getCommentAnalysisConnection().createStatement();
		ResultSet rs = stmt
				.executeQuery("select path_file,lxr_type,count(*) as number from comment GROUP BY path_file, lxr_type;");
		while (rs.next()) {
			commented.add(new FileCommentTypeCount(rs.getString(1), rs
					.getString(2), rs.getInt(3)));
		}
		rs.close();
		stmt.close();
		return commented;
	}

	public void completeness(String type) throws IOException {
		int yuanxingCount = 0;
		int gongnengCount = 0;
		int canshuCount = 0;
		int fanhuizhiCount = 0;
		int diaoyongguanxiCount = 0;
		int zhushiCount = 0;

		Map<String, String> comments = TXTCommentAnalyzer.readContentToMap(
				FileUtil.readableFile(TXTCommentAnalyzer.DEFAULTPATH
						+ "\\validity\\noLinkAndImageMap\\CLASSIFICATION_"
						+ type + ".txt"), TXTCommentAnalyzer.DEFAULTSPLITER);
		for (Map.Entry<String, String> entry : comments.entrySet()) {
			String content = entry.getValue().replaceAll(" ", "");
			if (content.contains("原型")) {
				yuanxingCount++;
			}
			if (content.contains("功能")) {
				gongnengCount++;
			}
			if (content.contains("参数")) {
				canshuCount++;
			}
			if (content.contains("返回")) {
				fanhuizhiCount++;
			}
			if (content.contains("调用位置")) {
				diaoyongguanxiCount++;
			}
			if (content.contains("注释")) {
				zhushiCount++;
			}
		}
		log.info("原型:" + yuanxingCount);
		log.info("功能描述:" + gongnengCount);
		log.info("参数:" + canshuCount);
		log.info("返回值:" + fanhuizhiCount);
		log.info("调用关系:" + diaoyongguanxiCount);
		log.info("逐行注释:" + zhushiCount);
	}

	/**
	 * 将原始注释中的url和imagemap过滤掉，然后把过滤以后的注释写入到\\validity\\noLinkAndImageMap\\
	 * CLASSIFICATION_function definition.txt
	 * 
	 * @throws IOException
	 */
	public void FilterLinkAndImagemap(String type) throws IOException {
		Map<String, String> comments = TXTCommentAnalyzer.readContentToMap(
				FileUtil.readableFile(TXTCommentAnalyzer.DEFAULTPATH
						+ "\\CLASSIFICATION_" + type + ".txt"),
				TXTCommentAnalyzer.DEFAULTSPLITER);
		PrintWriter writer = new PrintWriter(
				FileUtil.writeableFile(TXTCommentAnalyzer.DEFAULTPATH
						+ "\\validity\\noLinkAndImageMap\\CLASSIFICATION_"
						+ type + ".txt"));
		FilterBase filter = new IscasLinkFilter(new DoNothingFilter());
		for (Map.Entry<String, String> entry : comments.entrySet()) {
			writer.write(TXTCommentAnalyzer.DEFAULTSPLITER + entry.getKey()
					+ "\r\n");
			writer.write(filter.getText(entry.getValue()) + "\r\n\r\n");
		}
		writer.flush();
		writer.close();
	}

	public static double computeEnglishRatio(String inputStr) {
		String str = inputStr.replaceAll("[\u4e00-\u9fa5]", "");
		return (double) str.length() / inputStr.length();
	}

	// 每个task负责一个源码文件，这样多个文件就可以并行的做，提高效率
	class ConsistencyEvaluationTask implements Runnable {

		private String path_file;

		public ConsistencyEvaluationTask(String path_file) {
			this.path_file = path_file;
		}

		@Override
		public void run() {
			for (String path : evaluation_map.keySet()) {
				if (path.startsWith(path_file)) {
					JudgeTableInfo jti = evaluation_map.get(path);
					Set<String> same_file_comments = ca
							.getFileCommentsWithPath(path,
									ca.loadCommentsTypes().get(path)).keySet();
					int total_edit_distance = 0;
					List<String> cur_template = ca.loadNewTemplate(path,
							CommentAnalyzer.TEMPLATE_SAME_TYPE);
					for (String other_path : same_file_comments) {
						List<String> other_template = ca.loadNewTemplate(
								other_path, CommentAnalyzer.TEMPLATE_SAME_TYPE);
						total_edit_distance += algo.editDistance(cur_template,
								other_template);
					}
					double avg_edit_distance = (double) (total_edit_distance)
							/ same_file_comments.size();
					double consistency_score = avg_edit_distance;
					if (cur_template.size() != 0) {
						consistency_score /= cur_template.size();
					}
					// 为了适应consistency_score的整数保存方式，将double型的分数 * 100然后取整
					jti.setConsistency_score((int) (consistency_score * 100));
					ep.updateJudgeInfo(jti);
				}
			}
		}

	}

	class RedundantEvaluationTask implements Runnable {

		private String path_file;

		public RedundantEvaluationTask(String path_file) {
			this.path_file = path_file;
		}

		@Override
		public void run() {
			// 这里为了避免代码冗余，采用了一种偷懒的方式，直接取的模板着色以后的字符串，然后通过正则表达式的
			// 最短匹配(.*?)，去掉着色串，如果剩下的没有中文串了，就认为是冗余的
			for (String path : evaluation_map.keySet()) {
				if (path.startsWith(path_file)) {
					JudgeTableInfo jti = evaluation_map.get(path);
					String color_info = ca.getColoredInfo(path,
							CommentAnalyzer.TEMPLATE_SAME_TYPE);
					color_info = WordSeg.SEG_FILTER
							.getText(color_info.replaceAll(
									"<b><font color = (.*?)</font></b>", ""));
					if (color_info.replaceAll("[^\u4e00-\u9fa5]", "")
							.equals("")) {
						log.info("redundant " + path);
						jti.setIs_redundant(1);
					} else {
						jti.setIs_redundant(0);
					}
					ep.updateJudgeInfo(jti);
				}
			}
		}

	}

}
