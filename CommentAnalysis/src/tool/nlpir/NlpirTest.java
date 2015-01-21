package tool.nlpir;

import java.util.Map;

import mysql.data.analysis.CommentAnalyzer;
import mysql.data.filter.CategoryTagFilter;
import mysql.data.filter.DoNothingFilter;
import mysql.data.filter.FilterBase;
import mysql.data.filter.HtmlFilter;

public class NlpirTest {


	public static void main(String[] args) throws Exception {
		CommentAnalyzer ca = new CommentAnalyzer(true);
		Map<String, String> allComments = ca.getAllComments();
		
		String path = "/security/selinux/hooks.c/selinux_task_setscheduler(3465)(linux-3.5.4)";
		
		String sInput = allComments.get(path);
		WordSeg wordSeg = new WordSeg();
		for(String s : wordSeg.segmentation(sInput, WordSeg.NO_POS_TAG, WordSeg.SEG_FILTER_WITHPUNC)){
			System.out.print(s + "#");
		}

	}
}
