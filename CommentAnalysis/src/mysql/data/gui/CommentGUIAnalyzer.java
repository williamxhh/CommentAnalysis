package mysql.data.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import mysql.data.analysis.CommentStatistics;
import mysql.data.analysis.TXTCommentAnalyzer;

import org.dyno.visual.swing.layouts.Constraints;
import org.dyno.visual.swing.layouts.GroupLayout;
import org.dyno.visual.swing.layouts.Leading;

//VS4E -- DO NOT REMOVE THIS LINE!
public class CommentGUIAnalyzer extends JFrame {
	private CommentStatistics statistics;
	private TXTCommentAnalyzer analyzer;

	private static final long serialVersionUID = 1L;
	private JLabel jLabel0;
	private JTextArea statisticInfoTextArea;
	private JScrollPane jScrollPane0;
	private JTextField searchPathTextField;
	private JButton searchButton;
	public CommentGUIAnalyzer() {
		statistics = new CommentStatistics();
//		analyzer = new TXTCommentAnalyzer();
		
		initComponents();
	}

	private void initComponents() {
		setTitle("源码注释分析工具");
		setFont(new Font("Dialog", Font.PLAIN, 12));
		setForeground(Color.black);
		setLayout(new GroupLayout());
		add(getJLabel0(), new Constraints(new Leading(27, 34, 10, 10), new Leading(21, 16, 10, 450)));
		add(getSearchPathTextField(), new Constraints(new Leading(73, 443, 10, 10), new Leading(18, 12, 12)));
		add(getSearchButton(), new Constraints(new Leading(534, 12, 12), new Leading(15, 12, 12)));
		add(getJScrollPane0(), new Constraints(new Leading(27, 567, 12, 12), new Leading(58, 312, 10, 10)));
		setSize(652, 487);
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
			jScrollPane0.setViewportView(getJTextArea0());
		}
		return jScrollPane0;
	}

	private JTextArea getJTextArea0() {
		if (statisticInfoTextArea == null) {
			statisticInfoTextArea = new JTextArea();
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
		
	}

}
