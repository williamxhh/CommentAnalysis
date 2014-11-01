package mysql.data.gui;

import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import mysql.data.analysis.CommentAnalyzer;
import mysql.data.analysisDB.entity.JudgeTableInfo;
import mysql.data.filter.CategoryTagFilter;
import mysql.data.filter.DoNothingFilter;
import mysql.data.filter.FilterBase;
import mysql.data.filter.HtmlFilter;
import mysql.data.util.PropertiesUtil;

import org.dyno.visual.swing.layouts.Bilateral;
import org.dyno.visual.swing.layouts.Constraints;
import org.dyno.visual.swing.layouts.GroupLayout;
import org.dyno.visual.swing.layouts.Leading;

//VS4E -- DO NOT REMOVE THIS LINE!
public class EvaluationTool extends JFrame {

	private static final long serialVersionUID = 1L;

	Map<String, String> allComments = null;
	Map<String, String> filteredComments = null;
	Map<String, String> comment_types = null;
	Map<String, JudgeTableInfo> judgeList = null;
	String source_code_url = null;
	FilterBase filter = null;
	EvaluationPreparation ep = null;
	
	private JTabbedPane jTabbedPane0;
	private JPanel viewPanel;
	private JScrollPane jScrollPane0;
	private JTextField view_path_text_field;
	private JButton comment_filter_btn;
	private JLabel jLabel1;
	private JLabel jLabel0;
	private JScrollPane jScrollPane1;
	private JLabel jLabel9;
	private JLabel jLabel11;
	private JTextArea selected_comment_text_area;
	private JScrollPane jScrollPane2;
	private JLabel jLabel12;
	ButtonGroup validation_bg = new ButtonGroup();
	private JRadioButton validation_valid_radio_btn;
	private JRadioButton validation_invalid_radio_btn;
	private JRadioButton validation_empty_radio_btn;
	private JLabel jLabel3;
	private JLabel jLabel4;
	private JLabel total_comment_label;
	private JLabel jLabel6;
	private JLabel judged_comment_label;
	private JLabel jLabel8;
	ButtonGroup judge_type_bg = new ButtonGroup();
	private JRadioButton judge_type_unjudge_ratio_btn;
	private JRadioButton judge_type_judged_ratio_btn;
	private JPanel judgePanel;
	private JLabel source_code_url_label;
	private JPanel reportPanel;
	private JButton comment_search_btn;

	private JTextArea comment_content_text_area;

	private JComboBox<String> comment_type_combo_box;

	private JComboBox<String> judge_comment_type_combo_box;

	private JLabel jLabel18;

	private JLabel jLabel19;

	private JLabel jLabel20;

	private JLabel jLabel21;

	private JLabel jLabel22;

	private JLabel jLabel23;

	private JLabel jLabel24;

	private JLabel jLabel25;

	private JLabel jLabel26;

	private JLabel jLabel27;

	private JTextField infomation_score_text_field;

	private JTextField readability_score_text_field;

	private JTextField objectivity_score_text_field;

	private JTextField verifiability_score_text_field;

	private JTextField relativity_score_text_field;

	private JLabel jLabel14;

	private JTextField completeness_score_text_field;

	private JLabel jLabel16;

	private JTextField consistency_score_text_field;

	private JLabel jLabel15;

	private JLabel jLabel17;

	private JList<String> comment_path_list;

	private JButton score_btn;

	public EvaluationTool() {
		initComponents();

		loadComments();
	}
	
	private void loadComments() {
		try {
			boolean loadFromFile = true;
			if(allComments == null) {
				CommentAnalyzer ca = new CommentAnalyzer(loadFromFile);
				allComments = ca.getAllComments(loadFromFile);
			}
			filteredComments = new HashMap<String, String>();
			filter = new CategoryTagFilter(new HtmlFilter(new DoNothingFilter()));
			
			if(comment_types == null) {
				loadCommentsTypes();
			}
		} catch (SQLException | IOException e) {
			e.printStackTrace();
		}
	}
	
	private void loadCommentsTypes() {
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

	private void initComponents() {
		setTitle("源代码阅读注释分析工具");
		setLayout(new GroupLayout());
		add(getJTabbedPane0(), new Constraints(new Bilateral(0, 0, 5), new Leading(0, 738, 12, 12)));
		setSize(714, 738);
		setLocation(400, 200);
	}


	private JLabel getSource_code_url_label() {
		if (source_code_url_label == null) {
			source_code_url_label = new JLabel();
			source_code_url_label.setText("<html><a href = ''>http://124.16.141.157/lxr-0101/source/?v=linux-3.5.4</a></html>");
			source_code_url_label.addMouseListener(new MouseAdapter() {
	
				public void mouseClicked(MouseEvent event) {
					source_code_url_labelMouseMouseClicked(event);
				}
			});
		}
		return source_code_url_label;
	}

	private JButton getScore_btn() {
		if (score_btn == null) {
			score_btn = new JButton();
			score_btn.setText("打  分");
			score_btn.addMouseListener(new MouseAdapter() {
	
				public void mouseClicked(MouseEvent event) {
					score_btnMouseMouseClicked(event);
				}
			});
		}
		return score_btn;
	}

	private JList<String> getJList0() {
		if (comment_path_list == null) {
			comment_path_list = new JList<String>();
			comment_path_list.addListSelectionListener(new ListSelectionListener() {
	
				public void valueChanged(ListSelectionEvent event) {
					comment_path_listListSelectionValueChanged(event);
				}
			});
		}
		return comment_path_list;
	}

	private JLabel getJLabel17() {
		if (jLabel17 == null) {
			jLabel17 = new JLabel();
			jLabel17.setText("分");
		}
		return jLabel17;
	}

	private JLabel getJLabel15() {
		if (jLabel15 == null) {
			jLabel15 = new JLabel();
			jLabel15.setText("分");
		}
		return jLabel15;
	}

	private JTextField getJTextField2() {
		if (consistency_score_text_field == null) {
			consistency_score_text_field = new JTextField();
			consistency_score_text_field.setText("0");
			consistency_score_text_field.addFocusListener(new FocusListener() {
				
				@Override
				public void focusLost(FocusEvent e) {
				}
				
				@Override
				public void focusGained(FocusEvent e) {
					textFieldGotFocus(e);
				}
			});
		}
		return consistency_score_text_field;
	}

	private JLabel getJLabel16() {
		if (jLabel16 == null) {
			jLabel16 = new JLabel();
			jLabel16.setText("一致性：");
		}
		return jLabel16;
	}

	private JTextField getJTextField1() {
		if (completeness_score_text_field == null) {
			completeness_score_text_field = new JTextField();
			completeness_score_text_field.setText("0");
			completeness_score_text_field.addFocusListener(new FocusListener() {
				
				@Override
				public void focusLost(FocusEvent e) {
				}
				
				@Override
				public void focusGained(FocusEvent e) {
					textFieldGotFocus(e);
				}
			});
		}
		return completeness_score_text_field;
	}
	
	

	private JLabel getJLabel14() {
		if (jLabel14 == null) {
			jLabel14 = new JLabel();
			jLabel14.setText("完整性：");
		}
		return jLabel14;
	}

	private JTextField getJTextField7() {
		if (relativity_score_text_field == null) {
			relativity_score_text_field = new JTextField();
			relativity_score_text_field.setText("0");
			relativity_score_text_field.addFocusListener(new FocusListener() {
				
				@Override
				public void focusLost(FocusEvent e) {
				}
				
				@Override
				public void focusGained(FocusEvent e) {
					textFieldGotFocus(e);
				}
			});
		}
		return relativity_score_text_field;
	}

	private JTextField getJTextField6() {
		if (verifiability_score_text_field == null) {
			verifiability_score_text_field = new JTextField();
			verifiability_score_text_field.setText("0");
			verifiability_score_text_field.addFocusListener(new FocusListener() {
				
				@Override
				public void focusLost(FocusEvent e) {
				}
				
				@Override
				public void focusGained(FocusEvent e) {
					textFieldGotFocus(e);
				}
			});
			
		}
		return verifiability_score_text_field;
	}

	private JTextField getJTextField5() {
		if (objectivity_score_text_field == null) {
			objectivity_score_text_field = new JTextField();
			objectivity_score_text_field.setText("0");
			objectivity_score_text_field.addFocusListener(new FocusListener() {
				
				@Override
				public void focusLost(FocusEvent e) {
				}
				
				@Override
				public void focusGained(FocusEvent e) {
					textFieldGotFocus(e);
				}
			});
		}
		return objectivity_score_text_field;
	}

	private JTextField getJTextField4() {
		if (readability_score_text_field == null) {
			readability_score_text_field = new JTextField();
			readability_score_text_field.setText("0");
			readability_score_text_field.addFocusListener(new FocusListener() {
				
				@Override
				public void focusLost(FocusEvent e) {
				}
				
				@Override
				public void focusGained(FocusEvent e) {
					textFieldGotFocus(e);
				}
			});
			
		}
		return readability_score_text_field;
	}

	private JTextField getJTextField3() {
		if (infomation_score_text_field == null) {
			infomation_score_text_field = new JTextField();
			infomation_score_text_field.setText("0");
			infomation_score_text_field.addFocusListener(new FocusListener() {
				
				@Override
				public void focusLost(FocusEvent e) {
				}
				
				@Override
				public void focusGained(FocusEvent e) {
					textFieldGotFocus(e);
				}
			});
		}
		return infomation_score_text_field;
	}

	private JLabel getJLabel27() {
		if (jLabel27 == null) {
			jLabel27 = new JLabel();
			jLabel27.setText("分");
		}
		return jLabel27;
	}

	private JLabel getJLabel26() {
		if (jLabel26 == null) {
			jLabel26 = new JLabel();
			jLabel26.setText("分");
		}
		return jLabel26;
	}

	private JLabel getJLabel25() {
		if (jLabel25 == null) {
			jLabel25 = new JLabel();
			jLabel25.setText("分");
		}
		return jLabel25;
	}

	private JLabel getJLabel24() {
		if (jLabel24 == null) {
			jLabel24 = new JLabel();
			jLabel24.setText("分");
		}
		return jLabel24;
	}

	private JLabel getJLabel23() {
		if (jLabel23 == null) {
			jLabel23 = new JLabel();
			jLabel23.setText("分");
		}
		return jLabel23;
	}

	private JLabel getJLabel22() {
		if (jLabel22 == null) {
			jLabel22 = new JLabel();
			jLabel22.setText("相关性：");
		}
		return jLabel22;
	}

	private JLabel getJLabel21() {
		if (jLabel21 == null) {
			jLabel21 = new JLabel();
			jLabel21.setText("查证性：");
		}
		return jLabel21;
	}

	private JLabel getJLabel20() {
		if (jLabel20 == null) {
			jLabel20 = new JLabel();
			jLabel20.setText("客观性：");
		}
		return jLabel20;
	}

	private JLabel getJLabel19() {
		if (jLabel19 == null) {
			jLabel19 = new JLabel();
			jLabel19.setText("可读性：");
		}
		return jLabel19;
	}

	private JLabel getJLabel18() {
		if (jLabel18 == null) {
			jLabel18 = new JLabel();
			jLabel18.setText("信息量：");
		}
		return jLabel18;
	}

	private JComboBox<String> getJudgeCommentTypeComboBox() {
		if (judge_comment_type_combo_box == null) {
			judge_comment_type_combo_box = new JComboBox<String>();
			judge_comment_type_combo_box.setDoubleBuffered(false);
			judge_comment_type_combo_box.setBorder(null);
			judge_comment_type_combo_box.addItemListener(new ItemListener() {
	
				public void itemStateChanged(ItemEvent event) {
					judge_comment_type_combo_boxItemItemStateChanged(event);
				}
			});
		}
		return judge_comment_type_combo_box;
	}

	private JComboBox<String> getCommentTypeComboBox() {
		if (comment_type_combo_box == null) {
			comment_type_combo_box = new JComboBox<String>();
			comment_type_combo_box.setDoubleBuffered(false);
			comment_type_combo_box.setEnabled(false);
			comment_type_combo_box.setBorder(null);
		}
		return comment_type_combo_box;
	}
	

	private JTextArea getJTextArea2() {
		if (comment_content_text_area == null) {
			comment_content_text_area = new JTextArea();
		}
		comment_content_text_area.setEditable(false);
		return comment_content_text_area;
	}

	private JButton getJButton2() {
		if (comment_search_btn == null) {
			comment_search_btn = new JButton();
			comment_search_btn.setText("查  询");
			comment_search_btn.addMouseListener(new MouseAdapter() {
	
				public void mouseClicked(MouseEvent event) {
					comment_search_btnMouseMouseClicked(event);
				}
			});
		}
		return comment_search_btn;
	}

	private JPanel getReportPanel() {
		if (reportPanel == null) {
			reportPanel = new JPanel();
			reportPanel.setLayout(new GroupLayout());
		}
		return reportPanel;
	}
	
//	private JPanel getJudgePanel() {
//		if (judgePanel == null) {
//			judgePanel = new JPanel();
//			judgePanel.setLayout(new GroupLayout());
//			judgePanel.add(getJScrollPane1(), new Constraints(new Leading(5, 229, 10, 10), new Leading(137, 528, 10, 10)));
//			judgePanel.add(getJLabel9(), new Constraints(new Leading(246, 12, 12), new Leading(140, 12, 12)));
//			judgePanel.add(getJLabel11(), new Constraints(new Leading(246, 12, 12), new Leading(166, 10, 10)));
//			judgePanel.add(getJLabel3(), new Constraints(new Leading(15, 12, 12), new Leading(22, 12, 12)));
//			judgePanel.add(getJudge_type_filter_btn(), new Constraints(new Leading(471, 12, 12), new Leading(17, 12, 12)));
//			judgePanel.add(getJLabel4(), new Constraints(new Leading(19, 10, 10), new Leading(64, 12, 12)));
//			judgePanel.add(getTotal_comment_label(), new Constraints(new Leading(39, 12, 12), new Leading(64, 12, 12)));
//			judgePanel.add(getJLabel6(), new Constraints(new Leading(86, 12, 12), new Leading(64, 12, 12)));
//			judgePanel.add(getJudged_comment_label(), new Constraints(new Leading(170, 12, 12), new Leading(64, 12, 12)));
//			judgePanel.add(getJLabel8(), new Constraints(new Leading(214, 10, 10), new Leading(64, 12, 12)));
//			judgePanel.add(getUnjudgeRadioButton(), new Constraints(new Leading(39, 12, 12), new Leading(94, 12, 12)));
//			judgePanel.add(getJudgedRadioButton(), new Constraints(new Leading(121, 12, 12), new Leading(94, 12, 12)));
//			judge_type_bg.add(getUnjudgeRadioButton());
//			judge_type_bg.add(getJudgedRadioButton());
//			judgePanel.add(getSource_code_url_label(), new Constraints(new Leading(317, 12, 12), new Leading(140, 12, 12)));
//			judgePanel.add(getJLabel12(), new Constraints(new Leading(246, 12, 12), new Leading(526, 12, 12)));
//			judgePanel.add(getValidRadioButton(), new Constraints(new Leading(300, 10, 10), new Leading(524, 10, 10)));
//			judgePanel.add(getInvalidRadioButton(), new Constraints(new Leading(357, 10, 10), new Leading(524, 10, 10)));
//			judgePanel.add(getValidationEmptyRadioButton(), new Constraints(new Leading(412, 10, 10), new Leading(524, 10, 10)));
//			validation_bg.add(getValidRadioButton());
//			validation_bg.add(getValidationEmptyRadioButton());
//			validation_bg.add(getInvalidRadioButton());
//			judgePanel.add(getJScrollPane2(), new Constraints(new Leading(246, 444, 10, 10), new Leading(192, 322, 10, 10)));
//			judgePanel.add(getJudgeCommentTypeComboBox(), new Constraints(new Leading(82, 371, 12, 12), new Leading(18, 12, 12)));
//			judgePanel.add(getJLabel18(), new Constraints(new Leading(246, 10, 411), new Leading(580, 12, 12)));
//			judgePanel.add(getJLabel19(), new Constraints(new Leading(413, 10, 244), new Leading(580, 10, 110)));
//			judgePanel.add(getJLabel20(), new Constraints(new Leading(246, 10, 411), new Leading(610, 12, 12)));
//			judgePanel.add(getJLabel21(), new Constraints(new Leading(413, 10, 244), new Leading(610, 10, 80)));
//			judgePanel.add(getJLabel22(), new Constraints(new Leading(246, 10, 411), new Leading(640, 12, 12)));
//			judgePanel.add(getJLabel23(), new Constraints(new Leading(334, 10, 363), new Leading(580, 10, 110)));
//			judgePanel.add(getJLabel24(), new Constraints(new Leading(513, 10, 184), new Leading(580, 10, 110)));
//			judgePanel.add(getJLabel25(), new Constraints(new Leading(334, 10, 363), new Leading(610, 10, 80)));
//			judgePanel.add(getJLabel26(), new Constraints(new Leading(513, 10, 184), new Leading(610, 10, 80)));
//			judgePanel.add(getJLabel27(), new Constraints(new Leading(334, 10, 363), new Leading(640, 10, 50)));
//			judgePanel.add(getJTextField3(), new Constraints(new Leading(300, 28, 10, 381), new Leading(580, 18, 10, 110)));
//			judgePanel.add(getJTextField4(), new Constraints(new Leading(477, 28, 10, 204), new Leading(580, 18, 10, 110)));
//			judgePanel.add(getJTextField5(), new Constraints(new Leading(300, 28, 10, 381), new Leading(610, 18, 10, 80)));
//			judgePanel.add(getJTextField6(), new Constraints(new Leading(477, 28, 10, 204), new Leading(610, 18, 10, 80)));
//			judgePanel.add(getJTextField7(), new Constraints(new Leading(300, 28, 10, 381), new Leading(640, 18, 10, 50)));
//			judgePanel.add(getJLabel14(), new Constraints(new Leading(246, 10, 411), new Leading(552, 10, 10)));
//			judgePanel.add(getJTextField1(), new Constraints(new Leading(300, 28, 12, 12), new Leading(552, 18, 10, 138)));
//			judgePanel.add(getJLabel16(), new Constraints(new Leading(413, 12, 12), new Leading(552, 10, 138)));
//			judgePanel.add(getJTextField2(), new Constraints(new Leading(477, 28, 12, 12), new Leading(552, 18, 10, 138)));
//			judgePanel.add(getJLabel15(), new Constraints(new Leading(334, 12, 12), new Leading(552, 10, 138)));
//			judgePanel.add(getJLabel17(), new Constraints(new Leading(513, 12, 12), new Leading(552, 10, 138)));
//			judgePanel.add(getScore_btn(), new Constraints(new Leading(587, 91, 10, 10), new Leading(637, 12, 12)));
//		}
//		return judgePanel;
//	}

	private JPanel getJudgePanel() {
		if (judgePanel == null) {
			judgePanel = new JPanel();
			judgePanel.setLayout(new GroupLayout());
			judgePanel.add(getJScrollPane1(), new Constraints(new Leading(5, 229, 10, 10), new Leading(137, 528, 10, 10)));
			judgePanel.add(getJLabel11(), new Constraints(new Leading(246, 12, 12), new Leading(166, 10, 10)));
			judgePanel.add(getJLabel3(), new Constraints(new Leading(15, 12, 12), new Leading(22, 12, 12)));
			judgePanel.add(getJLabel4(), new Constraints(new Leading(19, 10, 10), new Leading(64, 12, 12)));
			judgePanel.add(getTotal_comment_label(), new Constraints(new Leading(39, 12, 12), new Leading(64, 12, 12)));
			judgePanel.add(getJLabel6(), new Constraints(new Leading(86, 12, 12), new Leading(64, 12, 12)));
			judgePanel.add(getJudged_comment_label(), new Constraints(new Leading(170, 12, 12), new Leading(64, 12, 12)));
			judgePanel.add(getJLabel8(), new Constraints(new Leading(214, 10, 10), new Leading(64, 12, 12)));
			judgePanel.add(getUnjudgeRadioButton(), new Constraints(new Leading(39, 12, 12), new Leading(94, 12, 12)));
			judgePanel.add(getJudgedRadioButton(), new Constraints(new Leading(121, 12, 12), new Leading(94, 12, 12)));
			judge_type_bg.add(getUnjudgeRadioButton());
			judge_type_bg.add(getJudgedRadioButton());
			judgePanel.add(getJLabel12(), new Constraints(new Leading(246, 12, 12), new Leading(526, 12, 12)));
			judgePanel.add(getValidRadioButton(), new Constraints(new Leading(300, 10, 10), new Leading(524, 10, 10)));
			judgePanel.add(getInvalidRadioButton(), new Constraints(new Leading(357, 10, 10), new Leading(524, 10, 10)));
			judgePanel.add(getValidationEmptyRadioButton(), new Constraints(new Leading(412, 10, 10), new Leading(524, 10, 10)));
			validation_bg.add(getValidRadioButton());
			validation_bg.add(getValidationEmptyRadioButton());
			validation_bg.add(getInvalidRadioButton());
			judgePanel.add(getJScrollPane2(), new Constraints(new Leading(246, 444, 10, 10), new Leading(192, 322, 10, 10)));
			judgePanel.add(getJLabel18(), new Constraints(new Leading(246, 10, 411), new Leading(580, 12, 12)));
			judgePanel.add(getJLabel19(), new Constraints(new Leading(413, 10, 244), new Leading(580, 10, 110)));
			judgePanel.add(getJLabel20(), new Constraints(new Leading(246, 10, 411), new Leading(610, 12, 12)));
			judgePanel.add(getJLabel21(), new Constraints(new Leading(413, 10, 244), new Leading(610, 10, 80)));
			judgePanel.add(getJLabel22(), new Constraints(new Leading(246, 10, 411), new Leading(640, 12, 12)));
			judgePanel.add(getJLabel23(), new Constraints(new Leading(334, 10, 363), new Leading(580, 10, 110)));
			judgePanel.add(getJLabel24(), new Constraints(new Leading(513, 10, 184), new Leading(580, 10, 110)));
			judgePanel.add(getJLabel25(), new Constraints(new Leading(334, 10, 363), new Leading(610, 10, 80)));
			judgePanel.add(getJLabel26(), new Constraints(new Leading(513, 10, 184), new Leading(610, 10, 80)));
			judgePanel.add(getJLabel27(), new Constraints(new Leading(334, 10, 363), new Leading(640, 10, 50)));
			judgePanel.add(getJTextField3(), new Constraints(new Leading(300, 28, 10, 381), new Leading(580, 18, 10, 110)));
			judgePanel.add(getJTextField4(), new Constraints(new Leading(477, 28, 10, 204), new Leading(580, 18, 10, 110)));
			judgePanel.add(getJTextField5(), new Constraints(new Leading(300, 28, 10, 381), new Leading(610, 18, 10, 80)));
			judgePanel.add(getJTextField6(), new Constraints(new Leading(477, 28, 10, 204), new Leading(610, 18, 10, 80)));
			judgePanel.add(getJTextField7(), new Constraints(new Leading(300, 28, 10, 381), new Leading(640, 18, 10, 50)));
			judgePanel.add(getJLabel14(), new Constraints(new Leading(246, 10, 411), new Leading(552, 10, 10)));
			judgePanel.add(getJTextField1(), new Constraints(new Leading(300, 28, 12, 12), new Leading(552, 18, 10, 138)));
			judgePanel.add(getJLabel16(), new Constraints(new Leading(413, 12, 12), new Leading(552, 10, 138)));
			judgePanel.add(getJTextField2(), new Constraints(new Leading(477, 28, 12, 12), new Leading(552, 18, 10, 138)));
			judgePanel.add(getJLabel15(), new Constraints(new Leading(334, 12, 12), new Leading(552, 10, 138)));
			judgePanel.add(getJLabel17(), new Constraints(new Leading(513, 12, 12), new Leading(552, 10, 138)));
			judgePanel.add(getScore_btn(), new Constraints(new Leading(587, 91, 10, 10), new Leading(637, 12, 12)));
			judgePanel.add(getJudgeCommentTypeComboBox(), new Constraints(new Leading(82, 604, 10, 10), new Leading(18, 12, 12)));
			judgePanel.add(getJLabel9(), new Constraints(new Leading(246, 12, 12), new Leading(126, 12, 12)));
			judgePanel.add(getSource_code_url_label(), new Constraints(new Leading(246, 12, 12), new Leading(146, 10, 10)));
		}
		return judgePanel;
	}

	private JRadioButton getJudgedRadioButton() {
		if (judge_type_judged_ratio_btn == null) {
			judge_type_judged_ratio_btn = new JRadioButton();
			judge_type_judged_ratio_btn.setText("已评阅");
			judge_type_judged_ratio_btn.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					jRadioButtonActionActionPerformed(e);					
				}
			});
		}
		return judge_type_judged_ratio_btn;
	}

	private JRadioButton getUnjudgeRadioButton() {
		if (judge_type_unjudge_ratio_btn == null) {
			judge_type_unjudge_ratio_btn = new JRadioButton();
			judge_type_unjudge_ratio_btn.setText("未评阅");
			judge_type_unjudge_ratio_btn.setSelected(true);
			judge_type_unjudge_ratio_btn.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					jRadioButtonActionActionPerformed(e);					
				}
			});
		}
		return judge_type_unjudge_ratio_btn;
	}

	private JLabel getJLabel8() {
		if (jLabel8 == null) {
			jLabel8 = new JLabel();
			jLabel8.setText("条");
		}
		return jLabel8;
	}

	private JLabel getJudged_comment_label() {
		if (judged_comment_label == null) {
			judged_comment_label = new JLabel();
			judged_comment_label.setText("12568");
		}
		return judged_comment_label;
	}

	private JLabel getJLabel6() {
		if (jLabel6 == null) {
			jLabel6 = new JLabel();
			jLabel6.setText("条注释，已评");
		}
		return jLabel6;
	}

	private JLabel getTotal_comment_label() {
		if (total_comment_label == null) {
			total_comment_label = new JLabel();
			total_comment_label.setText("32356");
		}
		return total_comment_label;
	}

	private JLabel getJLabel4() {
		if (jLabel4 == null) {
			jLabel4 = new JLabel();
			jLabel4.setText("共");
		}
		return jLabel4;
	}

	private JRadioButton getValidationEmptyRadioButton() {
		if (validation_empty_radio_btn == null) {
			validation_empty_radio_btn = new JRadioButton();
			validation_empty_radio_btn.setText("待评");
			validation_empty_radio_btn.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					jRadioButtonActionActionPerformed(e);					
				}
			});
		}
		return validation_empty_radio_btn;
	}

	private JRadioButton getInvalidRadioButton() {
		if (validation_invalid_radio_btn == null) {
			validation_invalid_radio_btn = new JRadioButton();
			validation_invalid_radio_btn.setText("无效");
			validation_invalid_radio_btn.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					jRadioButtonActionActionPerformed(e);					
				}
			});
		}
		return validation_invalid_radio_btn;
	}

	private JRadioButton getValidRadioButton() {
		if (validation_valid_radio_btn == null) {
			validation_valid_radio_btn = new JRadioButton();
			validation_valid_radio_btn.setText("有效");
			validation_valid_radio_btn.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					jRadioButtonActionActionPerformed(e);					
				}
			});
		}
		return validation_valid_radio_btn;
	}

	private JLabel getJLabel12() {
		if (jLabel12 == null) {
			jLabel12 = new JLabel();
			jLabel12.setText("有效性：");
		}
		return jLabel12;
	}

	private JScrollPane getJScrollPane2() {
		if (jScrollPane2 == null) {
			jScrollPane2 = new JScrollPane();
			jScrollPane2.setViewportView(getJTextArea0());
			jScrollPane2.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
			jScrollPane2.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		}
		return jScrollPane2;
	}

	private JLabel getJLabel11() {
		if (jLabel11 == null) {
			jLabel11 = new JLabel();
			jLabel11.setText("注释内容：");
		}
		return jLabel11;
	}

	private JLabel getJLabel9() {
		if (jLabel9 == null) {
			jLabel9 = new JLabel();
			jLabel9.setText("源码链接：");
		}
		return jLabel9;
	}

	private JScrollPane getJScrollPane1() {
		if (jScrollPane1 == null) {
			jScrollPane1 = new JScrollPane();
			jScrollPane1.setViewportView(getJList0());
		}
		return jScrollPane1;
	}

	private JPanel getViewPanel() {
		if (viewPanel == null) {
			viewPanel = new JPanel();
			viewPanel.setLayout(new GroupLayout());
			viewPanel.add(getJTextField0(), new Constraints(new Leading(118, 378, 10, 10), new Leading(25, 12, 12)));
			viewPanel.add(getJLabel1(), new Constraints(new Leading(39, 12, 12), new Leading(65, 12, 12)));
			viewPanel.add(getJLabel0(), new Constraints(new Leading(39, 12, 12), new Leading(27, 12, 12)));
			viewPanel.add(getJButton1(), new Constraints(new Leading(519, 12, 12), new Leading(65, 12, 12)));
			viewPanel.add(getJButton2(), new Constraints(new Leading(519, 12, 12), new Leading(22, 12, 12)));
			viewPanel.add(getCommentTypeComboBox(), new Constraints(new Leading(116, 380, 12, 12), new Leading(62, 12, 12)));
			viewPanel.add(getJScrollPane0(), new Constraints(new Bilateral(0, 0, 22), new Leading(116, 594, 10, 10)));
		}
		return viewPanel;
	}

	private JLabel getJLabel3() {
		if (jLabel3 == null) {
			jLabel3 = new JLabel();
			jLabel3.setText("注释类型：");
		}
		return jLabel3;
	}

	private JLabel getJLabel0() {
		if (jLabel0 == null) {
			jLabel0 = new JLabel();
			jLabel0.setText("注释路径：");
		}
		return jLabel0;
	}

	private JLabel getJLabel1() {
		if (jLabel1 == null) {
			jLabel1 = new JLabel();
			jLabel1.setText("注释类型：");
		}
		return jLabel1;
	}


	private JButton getJButton1() {
		if (comment_filter_btn == null) {
			comment_filter_btn = new JButton();
			comment_filter_btn.setText("过  滤");
			comment_filter_btn.addMouseListener(new MouseAdapter() {
	
				public void mouseClicked(MouseEvent event) {
					comment_filter_btnMouseMouseClicked(event);
				}
			});
		}
		return comment_filter_btn;
	}

	private JTextField getJTextField0() {
		if (view_path_text_field == null) {
			view_path_text_field = new JTextField();
		}
		return view_path_text_field;
	}

	private JScrollPane getJScrollPane0() {
		if (jScrollPane0 == null) {
			jScrollPane0 = new JScrollPane();
			jScrollPane0.setViewportView(getJTextArea2());
		}
		return jScrollPane0;
	}

	private JTextArea getJTextArea0() {
		if (selected_comment_text_area == null) {
			selected_comment_text_area = new JTextArea();
			selected_comment_text_area.setEditable(false);
			selected_comment_text_area.setLineWrap(true);
		}
		return selected_comment_text_area;
	}

	private JTabbedPane getJTabbedPane0() {
		if (jTabbedPane0 == null) {
			jTabbedPane0 = new JTabbedPane();
			jTabbedPane0.addTab("查看注释", getViewPanel());
			jTabbedPane0.addTab("评阅注释", getJudgePanel());
			jTabbedPane0.addTab("质量报告", getReportPanel());
			jTabbedPane0.addChangeListener(new ChangeListener() {
				
				@Override
				public void stateChanged(ChangeEvent e) {
					JTabbedPane tabbedPane = (JTabbedPane) e.getSource();
					if(tabbedPane.getSelectedComponent().equals(getJudgePanel())){
						 loadJudgeInfo();
					}
				}
			});
		}
		return jTabbedPane0;
	}
	
	private void loadJudgeInfo() {
		if(allComments == null) {
			loadComments();
		}
		Set<String> allTypes = new HashSet<String>();
		for(String t: comment_types.values()){
			allTypes.add(t);
		}
		
		judge_comment_type_combo_box.addItem("ALL");
		for(String t: allTypes) {
			judge_comment_type_combo_box.addItem(t);
		}
		
		ep = new EvaluationPreparation();
		try {
			loadJudgeInfoFromDB();
		} catch (HeadlessException | SQLException e) {
			e.printStackTrace();
		}
	}
	
	private void loadJudgeInfoFromDB() throws HeadlessException, SQLException {
		if(!ep.hasJudgeInfo()){
			ep.insertAllPathToJudgeDB(allComments.keySet());
		}
		
		if(judgeList == null) {
			judgeList = ep.loadJudgeInfoFromDB();
		}
		
		updateCommentPathList(0, "ALL");
	}

	public static void main(String[] args) throws Exception {
//		BeautyEyeLNFHelper.launchBeautyEyeLNF();
		JFrame frame = new EvaluationTool();
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	private void comment_search_btnMouseMouseClicked(MouseEvent event) {
		String path = view_path_text_field.getText();
		
		if(path.length() != 0){
			comment_type_combo_box.setEnabled(true);
			comment_type_combo_box.removeAllItems();
			
			filteredComments.clear();
			
			Set<String> types = new HashSet<String>();
			StringBuilder content = new StringBuilder(); 
			for(Map.Entry<String, String> entry: allComments.entrySet()) {
				if(entry.getKey().startsWith(path)) {
					filteredComments.put(entry.getKey(), entry.getValue());
					types.add(comment_types.get(entry.getKey()));
					content.append("###################   " + entry.getKey() + "   ###################\r\n" + filter.getText(entry.getValue()) + "\r\n\r\n");
				}
			}
			
			comment_content_text_area.setText(content.toString());
			for(String t : types){
				comment_type_combo_box.addItem(t);
			}
		}
	}

	private void comment_filter_btnMouseMouseClicked(MouseEvent event) {
		if(comment_type_combo_box.getSelectedItem() != null){
			String type = comment_type_combo_box.getSelectedItem().toString();
			StringBuilder content = new StringBuilder();
			
			for(Map.Entry<String, String> entry: filteredComments.entrySet()) {
				if(comment_types.get(entry.getKey()).equals(type)) {
					content.append("###################   " + entry.getKey() + "   ###################\r\n" + filter.getText(entry.getValue()) + "\r\n\r\n");
				}
			}
			
			comment_content_text_area.setText(content.toString());
		}
	}

	private void score_btnMouseMouseClicked(MouseEvent event) {
		String selected_path = comment_path_list.getSelectedValue();
		JudgeTableInfo jti = judgeList.get(selected_path);
		if(validation_empty_radio_btn.isSelected()){
			jti.setValidation_state(0);
		}else if(validation_invalid_radio_btn.isSelected()) {
			jti.setValidation_state(-1);
		}else if(validation_valid_radio_btn.isSelected()) {
			jti.setValidation_state(1);
		}
		
		jti.setCompleteness_score(Integer.parseInt(completeness_score_text_field.getText()));
		jti.setConsistency_score(Integer.parseInt(consistency_score_text_field.getText()));
		jti.setInformation_score(Integer.parseInt(infomation_score_text_field.getText()));
		jti.setReadability_score(Integer.parseInt(readability_score_text_field.getText()));
		jti.setObjectivity_score(Integer.parseInt(objectivity_score_text_field.getText()));
		jti.setVerifiability_score(Integer.parseInt(verifiability_score_text_field.getText()));
		jti.setRelativity_score(Integer.parseInt(relativity_score_text_field.getText()));
		
		try {
			ep.updateJudgeInfo(jti);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}


	private void jRadioButtonActionActionPerformed(ActionEvent event) {
		Object source = event.getSource();
		
		
		if(source == judge_type_judged_ratio_btn) {
			String type = judge_comment_type_combo_box.getSelectedItem().toString();
			if(type == null){
				updateCommentPathList(1, "ALL");
			}else {
				updateCommentPathList(1, type);
			}
		} else if(source == judge_type_unjudge_ratio_btn) {
			String type = judge_comment_type_combo_box.getSelectedItem().toString();
			if(type == null){
				updateCommentPathList(0, "ALL");
			}else {
				updateCommentPathList(0, type);
			}
		} else if(source == validation_empty_radio_btn) {
//			setTitle(((JRadioButton)source).getText());
		} else if(source == validation_valid_radio_btn) {
//			setTitle(((JRadioButton)source).getText());
		} else if(source == validation_invalid_radio_btn) {
//			setTitle(((JRadioButton)source).getText());
		}
	}
	
	/**
	 * 
	 * @param state  0表示未评价，1表示已评价
	 * @param comment_type  ALL表示全部类型，其他的指定特定的类型
	 */
	private void updateCommentPathList(int state, String comment_type) {
		int total_comment_count = 0;
		int judged_comment_count = 0;
		
		if(judgeList != null) {
			DefaultListModel<String> listModel = new DefaultListModel<String>();
			TreeSet<JudgeTableInfo> list = new TreeSet<JudgeTableInfo>();
			for(JudgeTableInfo jti: judgeList.values()){
				list.add(jti);
			}
			for(JudgeTableInfo jti: list) {
				if(state == 0) {
					if(comment_types.get(jti.getComment_path()).equals(comment_type) || comment_type.equals("ALL")){
						++total_comment_count;
						if(jti.getValidation_state() == 0) {
							listModel.addElement(jti.getComment_path());
						} else {
							++judged_comment_count;
						}
					}
				} else if(state == 1) {
					if(comment_types.get(jti.getComment_path()).equals(comment_type) || comment_type.equals("ALL")){
						++total_comment_count;
						if(jti.getValidation_state() != 0) {
							++judged_comment_count;
							listModel.addElement(jti.getComment_path());
						}
					}
				}
			}
			
			comment_path_list.setModel(listModel);
			total_comment_label.setText(total_comment_count + "");
			judged_comment_label.setText(judged_comment_count + "");
		}
	}
	
	private String getLineNo(String path) {
		return path.substring(path.indexOf("(") + 1,path.indexOf(")"));
	}

	private void comment_path_listListSelectionValueChanged(ListSelectionEvent event) {
		@SuppressWarnings("unchecked")
		String selected_path = ((JList<String>)event.getSource()).getSelectedValue();
		if(selected_path != null) {
			if(allComments == null) {
				loadComments();
			}
			//更新注释框所显示的注释内容
			if(selected_path != null) {
				selected_comment_text_area.setText(filter.getText(allComments.get(selected_path)));
			}
			
			//更新源码路径的url信息
			if(comment_types.get(selected_path).equals("file")) {
				update_source_code_url_label_text(selected_path,"-1");
			} else {
				update_source_code_url_label_text(selected_path.substring(0,selected_path.lastIndexOf("/")),getLineNo(selected_path));
			}
			
			JudgeTableInfo selected_judge_info = judgeList.get(selected_path);

			// 0表示未评， -1表示无效，1表示有效
			int validation_state = selected_judge_info.getValidation_state();
			if(validation_state == 0) {
				getValidationEmptyRadioButton().setSelected(true);
			} else if(validation_state == 1) {
				getValidRadioButton().setSelected(true);
			} else if(validation_state == -1) {
				getInvalidRadioButton().setSelected(true);
			}
			
			completeness_score_text_field.setText(selected_judge_info.getCompleteness_score() + "");
			consistency_score_text_field.setText(selected_judge_info.getConsistency_score() + "");
			infomation_score_text_field.setText(selected_judge_info.getInformation_score() + "");
			readability_score_text_field.setText(selected_judge_info.getReadability_score() + "");
			objectivity_score_text_field.setText(selected_judge_info.getObjectivity_score() + "");
			verifiability_score_text_field.setText(selected_judge_info.getVerifiability_score() + "");
			relativity_score_text_field.setText(selected_judge_info.getRelativity_score() + "");
		}
	}
	
	private void update_source_code_url_label_text(String filename, String lineNo) {
		StringBuilder url = new StringBuilder();
		url.append(PropertiesUtil.getProperties().getProperty("mysql.data.gui.EvaluationTool.sourcecode_url_prefix"));
		url.append(filename);
		url.append(PropertiesUtil.getProperties().getProperty("mysql.data.gui.EvaluationTool.sourcecode_url_version"));
		if(!lineNo.equals("-1")) {
			url.append("#").append(lineNo);
		}
		source_code_url = url.toString();
		
		source_code_url_label.setText("<html><a href = ''>" + source_code_url + "</a></html>");
	}

	private void judge_comment_type_combo_boxItemItemStateChanged(ItemEvent event) {
		@SuppressWarnings("unchecked")
		String selected_type = ((JComboBox<String>)event.getSource()).getSelectedItem().toString();
		boolean unjudged = judge_type_unjudge_ratio_btn.isSelected();
		if(unjudged) {
			updateCommentPathList(0, selected_type);
		} else {
			updateCommentPathList(1, selected_type);
		}
	}

	private void source_code_url_labelMouseMouseClicked(MouseEvent event) {
		try {
			if(source_code_url != null) {
				Runtime.getRuntime().exec("cmd.exe /c start " + source_code_url);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void textFieldGotFocus(FocusEvent e) {
		JTextField field = (JTextField)e.getSource();
		field.selectAll();
	}

}
