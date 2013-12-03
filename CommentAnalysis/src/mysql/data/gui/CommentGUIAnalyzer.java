package mysql.data.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.sql.SQLException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import mysql.data.CommentClassifier;
import mysql.data.analysis.CommentAnalyzer;
import mysql.data.analysis.CommentStatistics;
import mysql.data.analysisDB.entity.CommentTableInfo;
import mysql.data.analysisDB.entity.TemplateTableInfo;

import org.dyno.visual.swing.layouts.Constraints;
import org.dyno.visual.swing.layouts.GroupLayout;
import org.dyno.visual.swing.layouts.Leading;

//VS4E -- DO NOT REMOVE THIS LINE!
public class CommentGUIAnalyzer extends JFrame {

	private static final long serialVersionUID = 1L;
	private JLabel jLabel0;
	private JTextArea statisticInfoTextArea;
	private JScrollPane jScrollPane0;
	private JTextField searchPathTextField;
	private JButton searchButton;
	private JTextArea templateInfoTextArea;
	private JScrollPane jScrollPane1;
	private JTextArea originCommentTextArea;
	private JScrollPane jScrollPane2;
	private JTextArea noTemplateCommentTextArea;
	private JScrollPane jScrollPane3;
	public CommentGUIAnalyzer() {
		
		initComponents();
	}

	private void initComponents() {
		setTitle("源码注释分析工具");
		setFont(new Font("Dialog", Font.PLAIN, 12));
		setForeground(Color.black);
		setLayout(new GroupLayout());
		add(getJLabel0(), new Constraints(new Leading(27, 34, 10, 10), new Leading(21, 16, 10, 450)));
		add(getSearchPathTextField(), new Constraints(new Leading(73, 1051, 10, 10), new Leading(18, 12, 12)));
		add(getSearchButton(), new Constraints(new Leading(1145, 10, 10), new Leading(12, 12, 12)));
		add(getJScrollPane1(), new Constraints(new Leading(617, 507, 10, 10), new Leading(48, 238, 12, 12)));
		add(getJScrollPane3(), new Constraints(new Leading(616, 508, 12, 12), new Leading(312, 473, 12, 12)));
		add(getJScrollPane0(), new Constraints(new Leading(73, 512, 10, 10), new Leading(50, 236, 10, 10)));
		add(getJScrollPane2(), new Constraints(new Leading(72, 514, 12, 12), new Leading(313, 472, 10, 10)));
		setSize(1315, 812);
	}

	private JTextArea getOriginCommentTextArea() {
		if (originCommentTextArea == null) {
			originCommentTextArea = new JTextArea();
			originCommentTextArea.setEditable(false);
		}
		return originCommentTextArea;
	}

	private JTextArea getTemplateInfoTextArea() {
		if (templateInfoTextArea == null) {
			templateInfoTextArea = new JTextArea();
			templateInfoTextArea.setEditable(false);
		}
		return templateInfoTextArea;
	}

	private JScrollPane getJScrollPane3() {
		if (jScrollPane3 == null) {
			jScrollPane3 = new JScrollPane();
			jScrollPane3.setViewportView(getNoTemplateCommentTextArea());
		}
		return jScrollPane3;
	}

	private JScrollPane getJScrollPane2() {
		if (jScrollPane2 == null) {
			jScrollPane2 = new JScrollPane();
			jScrollPane2.setViewportView(getOriginCommentTextArea());
		}
		return jScrollPane2;
	}

	private JScrollPane getJScrollPane1() {
		if (jScrollPane1 == null) {
			jScrollPane1 = new JScrollPane();
			jScrollPane1.setViewportView(getTemplateInfoTextArea());
		}
		return jScrollPane1;
	}

	private JTextArea getNoTemplateCommentTextArea() {
		if (noTemplateCommentTextArea == null) {
			noTemplateCommentTextArea = new JTextArea();
			noTemplateCommentTextArea.setEditable(false);
		}
		return noTemplateCommentTextArea;
	}

	private JButton getSearchButton() {
		if (searchButton == null) {
			searchButton = new JButton();
			searchButton.setText("查询");
			searchButton.addMouseListener(new MouseAdapter() {
	
				public void mouseClicked(MouseEvent event) {
					searchButtonMouseMouseClicked(event);
				}
			});
		}
		return searchButton;
	}

	private JTextField getSearchPathTextField() {
		if (searchPathTextField == null) {
			searchPathTextField = new JTextField();
		}
		return searchPathTextField;
	}

	private JScrollPane getJScrollPane0() {
		if (jScrollPane0 == null) {
			jScrollPane0 = new JScrollPane();
			jScrollPane0.setViewportView(getStatisticInfoTextArea());
		}
		return jScrollPane0;
	}

	private JTextArea getStatisticInfoTextArea() {
		if (statisticInfoTextArea == null) {
			statisticInfoTextArea = new JTextArea();
			statisticInfoTextArea.setEditable(false);
		}
		return statisticInfoTextArea;
	}

	private JLabel getJLabel0() {
		if (jLabel0 == null) {
			jLabel0 = new JLabel();
			jLabel0.setText("Path:");
		}
		return jLabel0;
	}

	public static void main(String[] args) throws Exception {
//		BeautyEyeLNFHelper.launchBeautyEyeLNF();
		
		JFrame frame = new CommentGUIAnalyzer();
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	private void searchButtonMouseMouseClicked(MouseEvent event) {
		String path = this.searchPathTextField.getText();
		try {
			boolean loadFromFile = true;
			CommentAnalyzer ca = new CommentAnalyzer(loadFromFile);
			CommentTableInfo cti = ca.getCommentFromAnalysisStorage(path);
			TemplateTableInfo tti = ca.getTemplateFromAnalysisStorage(cti.getPath_file(), cti.getLxr_type());
			String commentRemoveStrictTemplate = ca.removeTemplateFromComment(cti.getFiltered_comment(), tti.getStrict_template());
			String commentRemoveMiddleTemplate = ca.removeTemplateFromComment(cti.getFiltered_comment(), tti.getMiddle_template());
			String commentRemoveLooseTemplate = ca.removeTemplateFromComment(cti.getFiltered_comment(), tti.getLoose_template());
			
			StringBuilder statisticInfo = new StringBuilder();
			statisticInfo.append("源代码路径："+path+"\r\n");
			statisticInfo.append("lxr类型："+cti.getLxr_type()+"\r\n");
			statisticInfo.append("原始注释长度："+cti.getFiltered_comment().length()+"\r\n");
			statisticInfo.append("中文字符数："+ca.getChineseComment(cti.getFiltered_comment()).length()+"\r\n");
			statisticInfo.append("包含[[File:]]标记数："+cti.getFiletag_count()+"\r\n");
			statisticInfo.append("\r\n");
			
			statisticInfo.append("strict模板包含词数："+tti.getStrict_template().size()+"\r\n");
			statisticInfo.append("过滤strict模板后注释长度："+commentRemoveStrictTemplate.length()+"\r\n");
			statisticInfo.append("\r\n");
			statisticInfo.append("middle模板包含词数："+tti.getMiddle_template().size()+"\r\n");
			statisticInfo.append("过滤middle模板后注释长度："+commentRemoveMiddleTemplate.length()+"\r\n");
			statisticInfo.append("\r\n");
			statisticInfo.append("loose模板包含词数："+tti.getLoose_template().size()+"\r\n");
			statisticInfo.append("过滤loose模板后注释长度："+commentRemoveLooseTemplate.length()+"\r\n");
			
			StringBuilder templateInfo = new StringBuilder();
			templateInfo.append("strict型模板：\r\n");
			for(String s:tti.getStrict_template()){
				templateInfo.append("\t"+s+"\r\n");
			}
			templateInfo.append("middle型模板：\r\n");
			for(String m:tti.getMiddle_template()){
				templateInfo.append("\t"+m+"\r\n");
			}
			templateInfo.append("loose型模板：\r\n");
			for(String l:tti.getLoose_template()){
				templateInfo.append("\t"+l+"\r\n");
			}
			
			
			
			this.statisticInfoTextArea.setText(statisticInfo.toString());
			this.templateInfoTextArea.setText(templateInfo.toString());
			this.originCommentTextArea.setText(cti.getFiltered_comment());
			this.noTemplateCommentTextArea.setText(commentRemoveStrictTemplate);
		} catch (SQLException e) {
			this.statisticInfoTextArea.setText("读取数据库出错！\r\n"+e.getMessage());
			e.printStackTrace();
		}
	}

}
