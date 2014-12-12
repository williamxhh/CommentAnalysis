package mysql.data.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
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
import java.net.URL;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

import mysql.data.analysis.CommentAnalyzer;
import mysql.data.analysis.quality.CommentsQualityAnalysis;
import mysql.data.analysisDB.entity.JudgeTableInfo;
import mysql.data.util.PropertiesUtil;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import tool.nlpir.WordSeg;

//VS4E -- DO NOT REMOVE THIS LINE!
public class EvaluationTool extends JFrame {
	private static Logger logger = Logger.getLogger(EvaluationTool.class);

	private static final long serialVersionUID = 1L;

	Map<String, String> allComments = null;

	Map<String, String> filteredComments = null;

	Map<String, String> comment_types = null;

	Map<String, JudgeTableInfo> judgeList = null;

	Map<String, DefaultMutableTreeNode> pathTree = null;

	Map<String, String> tree_comment_info = null;

	String source_code_url = null;

	EvaluationPreparation ep = null;

	CommentAnalyzer ca = null;

	CommentsQualityAnalysis cqa = null;

	// 记录report面板中上面的统计信息
	private String stat_info;

	private Thread backTask = null;

	private int update_counter = 0;

	private boolean judgePanelInitialized = false;

	private JTabbedPane jTabbedPane0;

	private JPanel viewPanel;

	private JScrollPane jScrollPane0;

	private JTextField view_path_text_field;

	private JTextField tree_selected_path_text_field;

	private JButton comment_filter_btn;

	private JLabel jLabel1;

	private JLabel jLabel0;

	private JScrollPane jScrollPane1;

	private JLabel jLabel9;

	private JLabel jLabel11;

	private JEditorPane selected_comment_jeditor_pane;

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

	private JRadioButton judge_type_unjudge_radio_btn;

	private JRadioButton judge_type_judged_radio_btn;

	ButtonGroup template_type_bg = new ButtonGroup();

	private JRadioButton template_type_same_type_radio_btn;

	private JRadioButton template_type_same_editor_radio_btn;

	ButtonGroup color_type_bg = new ButtonGroup();

	private JRadioButton color_type_noun_radio_btn;

	private JRadioButton color_type_verb_radio_btn;

	private JRadioButton color_type_other_radio_btn;

	private JRadioButton color_type_template_radio_btn;

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

	private JTree jTree0;

	private JScrollPane jScrollPane3;

	private JTextArea stat_info_jTextArea;

	private JScrollPane jScrollPane5;

	private JEditorPane comments_info_jEditorPane;

	private JScrollPane jScrollPane4;

	private JComboBox<String> jTree_comment_type_comboBox;

	public EvaluationTool() {
		logger.setLevel(Level.WARN);
		loadComments();
		initComponents();
	}

	private void loadComments() {
		try {
			boolean loadFromFile = true;
			if (allComments == null) {
				ca = new CommentAnalyzer(loadFromFile);
				cqa = new CommentsQualityAnalysis(loadFromFile);
				allComments = ca.getAllComments(loadFromFile);
			}
			filteredComments = new HashMap<String, String>();
			if (comment_types == null) {
				loadCommentsTypes();
			}
		} catch (SQLException | IOException e) {
			e.printStackTrace();
		}
	}

	private void loadCommentsTypes() {
		try {
			comment_types = new HashMap<String, String>();
			BufferedReader reader = new BufferedReader(new FileReader(
					PropertiesUtil.getProperties().getProperty(
							"mysql.data.CommentClassifier.commentsAndTypes")));
			String oneline = "";
			while ((oneline = reader.readLine()) != null) {
				String[] splits = oneline.split(",");
				if (splits.length == 2)
					comment_types.put(splits[0].trim(), splits[1].trim());
				else
					comment_types.put(splits[0].trim(),
							oneline.substring(splits[0].length() + 1));
			}
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void initComponents() {
		setTitle("源代码分析注释分析工具");
		setLayout(new BorderLayout());
		add(getJTabbedPane0());
		setSize(714, 738);
		setLocation(400, 200);
	}

	private JComboBox<String> getJTreeCommentTypeComboBox() {
		if (jTree_comment_type_comboBox == null) {
			jTree_comment_type_comboBox = new JComboBox<String>();
			jTree_comment_type_comboBox.setDoubleBuffered(false);
			jTree_comment_type_comboBox.setBorder(null);
			jTree_comment_type_comboBox.addItemListener(new ItemListener() {

				public void itemStateChanged(ItemEvent event) {
					jTree_comment_type_comboBox_ItemStateChanged(event);
				}
			});
		}
		return jTree_comment_type_comboBox;
	}

	private JScrollPane getJTreeCommentInfoScrollPane() {
		if (jScrollPane4 == null) {
			jScrollPane4 = new JScrollPane();
			jScrollPane4.setViewportView(getCommentInfoEditorPane());
		}
		return jScrollPane4;
	}

	private JEditorPane getCommentInfoEditorPane() {
		if (comments_info_jEditorPane == null) {
			comments_info_jEditorPane = new JEditorPane();
			comments_info_jEditorPane.setContentType("text/html");
			comments_info_jEditorPane.setEditable(false);
			comments_info_jEditorPane
					.addHyperlinkListener(new HyperlinkListener() {

						@Override
						public void hyperlinkUpdate(HyperlinkEvent e) {
							if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
								URL url = e.getURL();
								try {
									Runtime.getRuntime().exec(
											"cmd.exe /c start "
													+ url.toString());
								} catch (IOException e1) {
									e1.printStackTrace();
								}
							}
						}
					});
		}
		return comments_info_jEditorPane;
	}

	private JScrollPane getJTreeStatInfoScrollPane() {
		if (jScrollPane5 == null) {
			jScrollPane5 = new JScrollPane();
			jScrollPane5.setViewportView(getStatInfoTextArea());
		}
		return jScrollPane5;
	}

	private JTextArea getStatInfoTextArea() {
		if (stat_info_jTextArea == null) {
			stat_info_jTextArea = new JTextArea();
		}
		return stat_info_jTextArea;
	}

	private JScrollPane getJTreeScrollPane() {
		if (jScrollPane3 == null) {
			jScrollPane3 = new JScrollPane();
			jScrollPane3.setViewportView(getJTree0());
		}
		return jScrollPane3;
	}

	private JTree getJTree0() {
		if (jTree0 == null) {
			jTree0 = new JTree();
			DefaultTreeModel treeModel = null;

			if (pathTree == null) {
				buildPathTree();
			}

			treeModel = new DefaultTreeModel(pathTree.get("/"));
			jTree0.setModel(treeModel);
			jTree0.addTreeSelectionListener(new TreeSelectionListener() {

				@Override
				public void valueChanged(TreeSelectionEvent e) {
					jTreeValueChanged(e);
				}
			});
		}
		return jTree0;
	}

	private JLabel getSourceCodeUrlLabel() {
		if (source_code_url_label == null) {
			source_code_url_label = new JLabel();
			source_code_url_label
					.setText("<html><a href = ''>"
							+ PropertiesUtil
									.getProperties()
									.getProperty(
											"mysql.data.gui.EvaluationTool.sourcecode_url_prefix")
							+ PropertiesUtil
									.getProperties()
									.getProperty(
											"mysql.data.gui.EvaluationTool.sourcecode_url_version")
							+ "</a></html>");
			source_code_url_label.addMouseListener(new MouseAdapter() {

				public void mouseClicked(MouseEvent event) {
					source_code_url_labelMouseMouseClicked(event);
				}
			});
		}
		return source_code_url_label;
	}

	private JButton getScoreBtn() {
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

	private JList<String> getCommentPathJList() {
		if (comment_path_list == null) {
			comment_path_list = new JList<String>();
			comment_path_list
					.addListSelectionListener(new ListSelectionListener() {

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

	private JLabel getJLabel16() {
		if (jLabel16 == null) {
			jLabel16 = new JLabel();
			jLabel16.setText("一致性：");
		}
		return jLabel16;
	}

	private JLabel getJLabel14() {
		if (jLabel14 == null) {
			jLabel14 = new JLabel();
			jLabel14.setText("完整性：");
		}
		return jLabel14;
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

	private JTextField getJTextField2() {
		if (consistency_score_text_field == null) {
			consistency_score_text_field = new JTextField();
			consistency_score_text_field.setColumns(5);
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

	private JTextField getJTextField1() {
		if (completeness_score_text_field == null) {
			completeness_score_text_field = new JTextField();
			completeness_score_text_field.setColumns(5);
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

	private JTextField getJTextField7() {
		if (relativity_score_text_field == null) {
			relativity_score_text_field = new JTextField();
			relativity_score_text_field.setColumns(5);
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
			verifiability_score_text_field.setColumns(5);
			verifiability_score_text_field.setText("0");
			verifiability_score_text_field
					.addFocusListener(new FocusListener() {

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
			objectivity_score_text_field.setColumns(5);
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
			readability_score_text_field.setColumns(5);
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
			infomation_score_text_field.setColumns(5);
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
			comment_type_combo_box.addItemListener(new ItemListener() {

				@Override
				public void itemStateChanged(ItemEvent e) {
					filter_view_comment();
				}
			});
		}
		return comment_type_combo_box;
	}

	private JTextArea getJTextArea2() {
		if (comment_content_text_area == null) {
			comment_content_text_area = new JTextArea();
		}
		comment_content_text_area.setEditable(false);
		comment_content_text_area.setLineWrap(true);
		return comment_content_text_area;
	}

	private JButton getCommentSearchBtn() {
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

	private JRadioButton getJudgedRadioButton() {
		if (judge_type_judged_radio_btn == null) {
			judge_type_judged_radio_btn = new JRadioButton();
			judge_type_judged_radio_btn.setText("已评阅");
			judge_type_judged_radio_btn.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					jRadioButtonActionActionPerformed(e);
				}
			});
		}
		return judge_type_judged_radio_btn;
	}

	private JRadioButton getUnjudgeRadioButton() {
		if (judge_type_unjudge_radio_btn == null) {
			judge_type_unjudge_radio_btn = new JRadioButton();
			judge_type_unjudge_radio_btn.setText("未评阅");
			judge_type_unjudge_radio_btn.setSelected(true);
			judge_type_unjudge_radio_btn
					.addActionListener(new ActionListener() {

						@Override
						public void actionPerformed(ActionEvent e) {
							jRadioButtonActionActionPerformed(e);
						}
					});
		}
		return judge_type_unjudge_radio_btn;
	}

	private JRadioButton getSameEditorTemplateRadioButton() {
		if (template_type_same_editor_radio_btn == null) {
			template_type_same_editor_radio_btn = new JRadioButton();
			template_type_same_editor_radio_btn.setText("同作者");
			template_type_same_editor_radio_btn
					.addActionListener(new ActionListener() {

						@Override
						public void actionPerformed(ActionEvent e) {
							jRadioButtonActionActionPerformed(e);
						}
					});
		}
		return template_type_same_editor_radio_btn;
	}

	private JRadioButton getSameTypeTemplateRadioButton() {
		if (template_type_same_type_radio_btn == null) {
			template_type_same_type_radio_btn = new JRadioButton();
			template_type_same_type_radio_btn.setText("同类型");
			template_type_same_type_radio_btn.setSelected(true);
			template_type_same_type_radio_btn
					.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							jRadioButtonActionActionPerformed(e);
						}
					});
		}
		return template_type_same_type_radio_btn;
	}

	private JRadioButton getColorTypeTemplateRadioButton() {
		if (color_type_template_radio_btn == null) {
			color_type_template_radio_btn = new JRadioButton();
			color_type_template_radio_btn.setText("模板");
			color_type_template_radio_btn.setSelected(true);
			color_type_template_radio_btn
					.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							jRadioButtonActionActionPerformed(e);
						}
					});
		}
		return color_type_template_radio_btn;
	}

	private JRadioButton getColorTypeNounRadioButton() {
		if (color_type_noun_radio_btn == null) {
			color_type_noun_radio_btn = new JRadioButton();
			color_type_noun_radio_btn.setText("名词");
			color_type_noun_radio_btn.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					jRadioButtonActionActionPerformed(e);
				}
			});
		}
		return color_type_noun_radio_btn;
	}

	private JRadioButton getColorTypeVerbRadioButton() {
		if (color_type_verb_radio_btn == null) {
			color_type_verb_radio_btn = new JRadioButton();
			color_type_verb_radio_btn.setText("动词");
			color_type_verb_radio_btn.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					jRadioButtonActionActionPerformed(e);
				}
			});
		}
		return color_type_verb_radio_btn;
	}

	private JRadioButton getColorTypeOtherRadioButton() {
		if (color_type_other_radio_btn == null) {
			color_type_other_radio_btn = new JRadioButton();
			color_type_other_radio_btn.setText("其他");
			color_type_other_radio_btn.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					jRadioButtonActionActionPerformed(e);
				}
			});
		}
		return color_type_other_radio_btn;
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
			validation_invalid_radio_btn
					.addActionListener(new ActionListener() {

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
			jScrollPane2.setViewportView(getSelectedCommentJEditorPane());
			jScrollPane2
					.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
			jScrollPane2
					.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
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
			jScrollPane1.setViewportView(getCommentPathJList());
		}
		return jScrollPane1;
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

	private JButton getCommentFilterBtn() {
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

	private JTextField getTreeSelectedPathJTextField() {
		if (tree_selected_path_text_field == null) {
			tree_selected_path_text_field = new JTextField();
			tree_selected_path_text_field.setMinimumSize(new Dimension(40, 5));

			tree_selected_path_text_field.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent event) {
					tree_selected_path_text_fieldMouseMouseClicked(event);
				}
			});

			tree_selected_path_text_field.addFocusListener(new FocusListener() {

				@Override
				public void focusLost(FocusEvent e) {
					updateTreeCommentInfo();
				}

				@Override
				public void focusGained(FocusEvent e) {

				}
			});
		}
		return tree_selected_path_text_field;
	}

	private JTextField getJTextField0() {
		if (view_path_text_field == null) {
			view_path_text_field = new JTextField();
			view_path_text_field.setMinimumSize(new Dimension(40, 5));
			view_path_text_field.addMouseListener(new MouseAdapter() {

				public void mouseClicked(MouseEvent event) {
					view_path_text_fieldMouseMouseClicked(event);
				}
			});

			view_path_text_field.addFocusListener(new FocusListener() {

				@Override
				public void focusLost(FocusEvent e) {
					update_view_comment();
				}

				@Override
				public void focusGained(FocusEvent e) {

				}
			});
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

	private JEditorPane getSelectedCommentJEditorPane() {
		if (selected_comment_jeditor_pane == null) {
			selected_comment_jeditor_pane = new JEditorPane();
			selected_comment_jeditor_pane.setEditable(false);
			selected_comment_jeditor_pane.setContentType("text/html");
		}
		return selected_comment_jeditor_pane;
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
					if (tabbedPane.getSelectedComponent().equals(
							getJudgePanel())) {
						loadJudgeInfo();
					}
				}
			});
		}
		return jTabbedPane0;
	}

	private JPanel getViewPanel() {
		if (viewPanel == null) {
			viewPanel = new JPanel();
			viewPanel.setLayout(new BorderLayout());

			JPanel header_panel = new JPanel(new GridLayout(2, 1));
			JPanel comment_path_panel = new JPanel(new BorderLayout());
			JPanel comment_type_panel = new JPanel(new BorderLayout());

			comment_path_panel.add(getJLabel0(), BorderLayout.WEST);
			comment_path_panel.add(getJTextField0(), BorderLayout.CENTER);
			// comment_path_panel.add(getCommentSearchBtn(),BorderLayout.EAST);

			comment_type_panel.add(getJLabel1(), BorderLayout.WEST);
			comment_type_panel.add(getCommentTypeComboBox(),
					BorderLayout.CENTER);
			// comment_type_panel.add(getCommentFilterBtn(),BorderLayout.EAST);

			header_panel.add(comment_path_panel);
			header_panel.add(comment_type_panel);

			viewPanel.add(header_panel, BorderLayout.NORTH);

			viewPanel.add(getJScrollPane0());

		}
		return viewPanel;
	}

	private JPanel getJudgePanel() {
		if (judgePanel == null) {
			judgePanel = new JPanel();
			judgePanel.setLayout(new BorderLayout());

			//最上面的注释类型
			JPanel comment_type_pane = new JPanel(new FlowLayout());
			comment_type_pane.add(getJLabel3());
			comment_type_pane.add(getJudgeCommentTypeComboBox());

			// 共 *条注释，已评*条
			JPanel comment_count_pane = new JPanel(new FlowLayout());
			comment_count_pane.add(getJLabel4());
			comment_count_pane.add(getTotal_comment_label());
			comment_count_pane.add(getJLabel6());
			comment_count_pane.add(getJudged_comment_label());
			comment_count_pane.add(getJLabel8());

			// 评阅状态  未评阅  已评阅
			JPanel comment_judge_state_pane = new JPanel(new FlowLayout());
			comment_judge_state_pane.add(getUnjudgeRadioButton());
			comment_judge_state_pane.add(getJudgedRadioButton());
			judge_type_bg.add(getUnjudgeRadioButton());
			judge_type_bg.add(getJudgedRadioButton());

			JPanel header_pane = new JPanel(new GridLayout(3, 1));
			header_pane.add(comment_type_pane);
			header_pane.add(comment_count_pane);
			header_pane.add(comment_judge_state_pane);

			judgePanel.add(header_pane, BorderLayout.NORTH);

			//右侧主要的显示注释内容的面板
			JPanel comment_info_panel = new JPanel(new BorderLayout());

			JPanel url_panel = new JPanel(new GridLayout(3, 1));
			url_panel.add(getJLabel9());
			url_panel.add(getSourceCodeUrlLabel());
			url_panel.add(getJLabel11());

			comment_info_panel.add(url_panel, BorderLayout.NORTH);
			comment_info_panel.add(getJScrollPane2());

			JPanel score_panel = new JPanel(new BorderLayout());
			JPanel validation_panel = new JPanel(new FlowLayout());
			validation_panel.add(getJLabel12());
			validation_panel.add(getValidRadioButton());
			validation_panel.add(getInvalidRadioButton());
			validation_panel.add(getValidationEmptyRadioButton());
			validation_bg.add(getValidRadioButton());
			validation_bg.add(getValidationEmptyRadioButton());
			validation_bg.add(getInvalidRadioButton());
			score_panel.add(validation_panel, BorderLayout.NORTH);

			JPanel score_grid_panel = new JPanel(new GridLayout(4, 2));
			JPanel completeness_panel = new JPanel(new FlowLayout());
			JPanel consistency_panel = new JPanel(new FlowLayout());
			JPanel information_panel = new JPanel(new FlowLayout());
			JPanel readability_panel = new JPanel(new FlowLayout());
			JPanel objectivity_panel = new JPanel(new FlowLayout());
			JPanel verifiabilit_panel = new JPanel(new FlowLayout());
			JPanel relativity_panel = new JPanel(new FlowLayout());
			completeness_panel.add(getJLabel14());
			completeness_panel.add(getJTextField1());
			completeness_panel.add(getJLabel15());

			consistency_panel.add(getJLabel16());
			consistency_panel.add(getJTextField2());
			consistency_panel.add(getJLabel17());

			information_panel.add(getJLabel18());
			information_panel.add(getJTextField3());
			information_panel.add(getJLabel23());

			readability_panel.add(getJLabel19());
			readability_panel.add(getJTextField4());
			readability_panel.add(getJLabel24());

			objectivity_panel.add(getJLabel20());
			objectivity_panel.add(getJTextField5());
			objectivity_panel.add(getJLabel25());

			verifiabilit_panel.add(getJLabel21());
			verifiabilit_panel.add(getJTextField6());
			verifiabilit_panel.add(getJLabel26());

			relativity_panel.add(getJLabel22());
			relativity_panel.add(getJTextField7());
			relativity_panel.add(getJLabel27());

			score_grid_panel.add(completeness_panel);
			score_grid_panel.add(consistency_panel);
			score_grid_panel.add(information_panel);
			score_grid_panel.add(readability_panel);
			score_grid_panel.add(objectivity_panel);
			score_grid_panel.add(verifiabilit_panel);
			score_grid_panel.add(relativity_panel);
			score_grid_panel.add(getScoreBtn());

			score_panel.add(score_grid_panel);
			comment_info_panel.add(score_panel, BorderLayout.SOUTH);

			JSplitPane hor_split_pane = new JSplitPane(
					JSplitPane.HORIZONTAL_SPLIT, getJScrollPane1(),
					comment_info_panel);
			hor_split_pane.setDividerSize(3);
			hor_split_pane.setDividerLocation(250);
			hor_split_pane.setOneTouchExpandable(true);

			judgePanel.add(hor_split_pane);

		}
		return judgePanel;
	}

	private JPanel getReportPanel() {
		if (reportPanel == null) {
			reportPanel = new JPanel();
			reportPanel.setLayout(new BorderLayout());

			JPanel left_panel = new JPanel();
			left_panel.setLayout(new BorderLayout());
			left_panel.add(getJTreeScrollPane(), BorderLayout.CENTER);

			JPanel left_bottom_panel = new JPanel();
			left_bottom_panel.setLayout(new GridLayout(2, 1));
			left_bottom_panel.add(getTreeSelectedPathJTextField());

			JPanel template_type_panel = new JPanel();
			template_type_panel.setLayout(new GridLayout(1, 2));
			template_type_panel.add(getSameEditorTemplateRadioButton());
			template_type_panel.add(getSameTypeTemplateRadioButton());
			template_type_bg.add(getSameEditorTemplateRadioButton());
			template_type_bg.add(getSameTypeTemplateRadioButton());

			left_bottom_panel.add(template_type_panel);

			left_panel.add(left_bottom_panel, BorderLayout.SOUTH);

			JPanel color_type_panel = new JPanel();
			color_type_panel.setLayout(new GridLayout(1, 4));
			color_type_panel.add(getColorTypeTemplateRadioButton());
			color_type_panel.add(getColorTypeNounRadioButton());
			color_type_panel.add(getColorTypeVerbRadioButton());
			color_type_panel.add(getColorTypeOtherRadioButton());
			color_type_bg.add(getColorTypeTemplateRadioButton());
			color_type_bg.add(getColorTypeNounRadioButton());
			color_type_bg.add(getColorTypeVerbRadioButton());
			color_type_bg.add(getColorTypeOtherRadioButton());

			JPanel comments_info_panel = new JPanel();
			comments_info_panel.setLayout(new BorderLayout());
			comments_info_panel.add(getJTreeCommentTypeComboBox(),
					BorderLayout.NORTH);
			comments_info_panel.add(getJTreeCommentInfoScrollPane(),
					BorderLayout.CENTER);
			comments_info_panel.add(color_type_panel, BorderLayout.SOUTH);

			JSplitPane ver_split_pane = new JSplitPane(
					JSplitPane.VERTICAL_SPLIT, getJTreeStatInfoScrollPane(),
					comments_info_panel);
			ver_split_pane.setDividerSize(3);
			ver_split_pane.setOneTouchExpandable(true);
			ver_split_pane.setDividerLocation(150);
			JSplitPane hor_split_pane = new JSplitPane(
					JSplitPane.HORIZONTAL_SPLIT, left_panel, ver_split_pane);
			hor_split_pane.setDividerSize(3);
			hor_split_pane.setOneTouchExpandable(true);
			hor_split_pane.setDividerLocation(200);

			reportPanel.add(hor_split_pane);
		}
		return reportPanel;
	}

	private void loadJudgeInfo() {
		// 引入一个布尔变量judgePanelInitialized， 是的judge面板只初始化一次
		if (!judgePanelInitialized) {
			judgePanelInitialized = true;

			if (allComments == null) {
				loadComments();
			}
			Set<String> allTypes = new HashSet<String>();
			for (String t : comment_types.values()) {
				allTypes.add(t);
			}

			judge_comment_type_combo_box.addItem("ALL");
			for (String t : allTypes) {
				judge_comment_type_combo_box.addItem(t);
			}

			ep = new EvaluationPreparation();
			try {
				loadJudgeInfoFromDB();
			} catch (HeadlessException | SQLException e) {
				e.printStackTrace();
			}
		}
	}

	private void loadJudgeInfoFromDB() throws HeadlessException, SQLException {
		if (!ep.hasJudgeInfo()) {
			ep.insertAllPathToJudgeDB(allComments.keySet());
		}

		if (judgeList == null) {
			judgeList = ep.loadJudgeInfoFromDB();
		}

		updateCommentPathList(0, "ALL");
	}

	public static void main(String[] args) throws Exception {
		// BeautyEyeLNFHelper.launchBeautyEyeLNF();
		JFrame frame = new EvaluationTool();
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	private void comment_search_btnMouseMouseClicked(MouseEvent event) {
		update_view_comment();
	}

	private void update_view_comment() {
		String path = view_path_text_field.getText();

		if (path.length() != 0) {
			comment_type_combo_box.setEnabled(true);
			comment_type_combo_box.removeAllItems();

			filteredComments.clear();

			Set<String> types = new HashSet<String>();
			StringBuilder content = new StringBuilder();
			for (Map.Entry<String, String> entry : allComments.entrySet()) {
				if (entry.getKey().startsWith(path)) {
					filteredComments.put(entry.getKey(), entry.getValue());
					types.add(comment_types.get(entry.getKey()));
					content.append("###################   " + entry.getKey()
							+ "   ###################\r\n"
							+ CommentAnalyzer.COMMON_FILTER.getText(entry.getValue()) + "\r\n\r\n");
				}
			}

			comment_content_text_area.setText(content.toString());
			for (String t : types) {
				comment_type_combo_box.addItem(t);
			}
		}
	}

	private void comment_filter_btnMouseMouseClicked(MouseEvent event) {
		filter_view_comment();
	}

	private void filter_view_comment() {
		if (comment_type_combo_box.getSelectedItem() != null) {
			String type = comment_type_combo_box.getSelectedItem().toString();
			StringBuilder content = new StringBuilder();

			for (Map.Entry<String, String> entry : filteredComments.entrySet()) {
				if (comment_types.get(entry.getKey()).equals(type)) {
					content.append("###################   " + entry.getKey()
							+ "   ###################\r\n"
							+ CommentAnalyzer.COMMON_FILTER.getText(entry.getValue()) + "\r\n\r\n");
				}
			}

			comment_content_text_area.setText(content.toString());
		}
	}

	private void score_btnMouseMouseClicked(MouseEvent event) {
		String selected_path = comment_path_list.getSelectedValue();
		JudgeTableInfo jti = judgeList.get(selected_path);
		if (validation_empty_radio_btn.isSelected()) {
			jti.setValidation_state(0);
		} else if (validation_invalid_radio_btn.isSelected()) {
			jti.setValidation_state(-1);
		} else if (validation_valid_radio_btn.isSelected()) {
			jti.setValidation_state(1);
		}

		jti.setCompleteness_score(Integer
				.parseInt(completeness_score_text_field.getText()));
		jti.setConsistency_score(Integer.parseInt(consistency_score_text_field
				.getText()));
		jti.setInformation_score(Integer.parseInt(infomation_score_text_field
				.getText()));
		jti.setReadability_score(Integer.parseInt(readability_score_text_field
				.getText()));
		jti.setObjectivity_score(Integer.parseInt(objectivity_score_text_field
				.getText()));
		jti.setVerifiability_score(Integer
				.parseInt(verifiability_score_text_field.getText()));
		jti.setRelativity_score(Integer.parseInt(relativity_score_text_field
				.getText()));

		try {
			ep.updateJudgeInfo(jti);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void jRadioButtonActionActionPerformed(ActionEvent event) {
		Object source = event.getSource();

		if (source == judge_type_judged_radio_btn) {
			String type = judge_comment_type_combo_box.getSelectedItem()
					.toString();
			if (type == null) {
				updateCommentPathList(1, "ALL");
			} else {
				updateCommentPathList(1, type);
			}
		} else if (source == judge_type_unjudge_radio_btn) {
			String type = judge_comment_type_combo_box.getSelectedItem()
					.toString();
			if (type == null) {
				updateCommentPathList(0, "ALL");
			} else {
				updateCommentPathList(0, type);
			}
		} else if (source == validation_empty_radio_btn) {
			// setTitle(((JRadioButton)source).getText());
		} else if (source == validation_valid_radio_btn) {
			// setTitle(((JRadioButton)source).getText());
		} else if (source == validation_invalid_radio_btn) {
			// setTitle(((JRadioButton)source).getText());
		} else if (source == template_type_same_editor_radio_btn
				|| source == template_type_same_type_radio_btn
				|| source == color_type_template_radio_btn) {
			template_type_same_editor_radio_btn.setEnabled(true);
			template_type_same_type_radio_btn.setEnabled(true);
			updateTreeCommentInfo();
		} else if (source == color_type_noun_radio_btn
				|| source == color_type_verb_radio_btn
				|| source == color_type_other_radio_btn) {
			template_type_same_editor_radio_btn.setEnabled(false);
			template_type_same_type_radio_btn.setEnabled(false);
			updateTreeCommentInfo();
		}
	}

	/**
	 * 
	 * @param state
	 *            0表示未评价，1表示已评价
	 * @param comment_type
	 *            ALL表示全部类型，其他的指定特定的类型
	 */
	private void updateCommentPathList(int state, String comment_type) {
		int total_comment_count = 0;
		int judged_comment_count = 0;

		if (judgeList != null) {
			DefaultListModel<String> listModel = new DefaultListModel<String>();
			TreeSet<JudgeTableInfo> list = new TreeSet<JudgeTableInfo>();
			for (JudgeTableInfo jti : judgeList.values()) {
				list.add(jti);
			}
			for (JudgeTableInfo jti : list) {
				if (state == 0) {
					if (comment_types.get(jti.getComment_path()).equals(
							comment_type)
							|| comment_type.equals("ALL")) {
						++total_comment_count;
						if (jti.getValidation_state() == 0) {
							listModel.addElement(jti.getComment_path());
						} else {
							++judged_comment_count;
						}
					}
				} else if (state == 1) {
					if (comment_types.get(jti.getComment_path()).equals(
							comment_type)
							|| comment_type.equals("ALL")) {
						++total_comment_count;
						if (jti.getValidation_state() != 0) {
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
		if (path.indexOf("(") != -1 && path.indexOf(")") != -1)
			return path.substring(path.indexOf("(") + 1, path.indexOf(")"));

		return "";
	}

	private void comment_path_listListSelectionValueChanged(
			ListSelectionEvent event) {
		@SuppressWarnings("unchecked")
		String selected_path = ((JList<String>) event.getSource())
				.getSelectedValue();
		if (selected_path != null) {
			if (allComments == null) {
				loadComments();
			}
			// 更新注释框所显示的注释内容
			if (selected_path != null) {
				selected_comment_jeditor_pane.setText(getColoredInfo(
						selected_path, CommentAnalyzer.TEMPLATE_SAME_TYPE));
			}

			// 更新源码路径的url信息
			if (comment_types.get(selected_path).equals("file")) {
				update_source_code_url_label_text(selected_path, "-1");
			} else {
				update_source_code_url_label_text(
						selected_path.substring(0,
								selected_path.lastIndexOf("/")),
						getLineNo(selected_path));
			}

			JudgeTableInfo selected_judge_info = judgeList.get(selected_path);

			// 0表示未评， -1表示无效，1表示有效
			int validation_state = selected_judge_info.getValidation_state();
			if (validation_state == 0) {
				getValidationEmptyRadioButton().setSelected(true);
			} else if (validation_state == 1) {
				getValidRadioButton().setSelected(true);
			} else if (validation_state == -1) {
				getInvalidRadioButton().setSelected(true);
			}

			completeness_score_text_field.setText(selected_judge_info
					.getCompleteness_score() + "");
			consistency_score_text_field.setText(selected_judge_info
					.getConsistency_score() + "");
			infomation_score_text_field.setText(selected_judge_info
					.getInformation_score() + "");
			readability_score_text_field.setText(selected_judge_info
					.getReadability_score() + "");
			objectivity_score_text_field.setText(selected_judge_info
					.getObjectivity_score() + "");
			verifiability_score_text_field.setText(selected_judge_info
					.getVerifiability_score() + "");
			relativity_score_text_field.setText(selected_judge_info
					.getRelativity_score() + "");
		}
	}

	private void update_source_code_url_label_text(String filename,
			String lineNo) {
		StringBuilder url = new StringBuilder();
		url.append(PropertiesUtil.getProperties().getProperty(
				"mysql.data.gui.EvaluationTool.sourcecode_url_prefix"));
		url.append(filename);
		url.append(PropertiesUtil.getProperties().getProperty(
				"mysql.data.gui.EvaluationTool.sourcecode_url_version"));
		if (!lineNo.equals("-1")) {
			url.append("#").append(lineNo);
		}
		source_code_url = url.toString();

		source_code_url_label.setText("<html><a href = ''>" + source_code_url
				+ "</a></html>");
	}

	private void judge_comment_type_combo_boxItemItemStateChanged(
			ItemEvent event) {
		@SuppressWarnings("unchecked")
		String selected_type = ((JComboBox<String>) event.getSource())
				.getSelectedItem().toString();
		boolean unjudged = judge_type_unjudge_radio_btn.isSelected();
		if (unjudged) {
			updateCommentPathList(0, selected_type);
		} else {
			updateCommentPathList(1, selected_type);
		}
	}

	private void jTree_comment_type_comboBox_ItemStateChanged(ItemEvent event) {
		@SuppressWarnings("unchecked")
		Object selected_item = ((JComboBox<String>) event.getSource())
				.getSelectedItem();

		if (selected_item != null) {
			String selected_type = selected_item.toString();
			StringBuilder stat_content = new StringBuilder();
			StringBuilder content = new StringBuilder();
			for (Map.Entry<String, String> entry : tree_comment_info.entrySet()) {
				if (selected_type.equals("ALL")
						|| comment_types.get(entry.getKey()).equals(
								selected_type)) {
					content.append(entry.getValue() + "<br/><br/>");
				}
			}
			comments_info_jEditorPane.setText(content.toString());
			logger.info("jTree_comment_type_comboBox_ItemStateChanged\r\n"
					+ content.toString());

			if (!selected_type.equals("ALL")) {
				String[] split = stat_info.split("\n");
				for (String s : split) {
					if (s.startsWith(selected_type)) {
						stat_content.append(s + "\n");
					}
				}
				stat_info_jTextArea.setText(stat_content.toString());
			} else {
				stat_info_jTextArea.setText(stat_info);
			}
		}
	}

	private void source_code_url_labelMouseMouseClicked(MouseEvent event) {
		try {
			if (source_code_url != null) {
				Runtime.getRuntime()
						.exec("cmd.exe /c start " + source_code_url);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void textFieldGotFocus(FocusEvent e) {
		JTextField field = (JTextField) e.getSource();
		field.selectAll();
	}

	private void view_path_text_fieldMouseMouseClicked(MouseEvent event) {
		String selected_path = comment_path_list.getSelectedValue();
		if (selected_path != null) {
			if (comment_types == null) {
				loadCommentsTypes();
			}

			if (comment_types.get(selected_path).equals("file")) {
				view_path_text_field.setText(selected_path);
			} else {
				view_path_text_field.setText(selected_path.substring(0,
						selected_path.lastIndexOf("/")));
			}

			view_path_text_field.selectAll();
		}
	}

	private void tree_selected_path_text_fieldMouseMouseClicked(MouseEvent event) {
		String selected_path = comment_path_list.getSelectedValue();
		if (selected_path != null) {
			if (comment_types == null) {
				loadCommentsTypes();
			}

			if (comment_types.get(selected_path).equals("file")) {
				tree_selected_path_text_field.setText(selected_path);
			} else {
				tree_selected_path_text_field.setText(selected_path.substring(
						0, selected_path.lastIndexOf("/")));
			}

			tree_selected_path_text_field.selectAll();
		}
	}

	public String getColoredInfo(String path, int template_type) {
		return ca.getColoredInfo(path, template_type);
	}

	private void buildPathTree() {
		allComments = ca.getAllComments();
		for (String path : allComments.keySet()) {
			addNodeToPathTree(path);
		}
	}

	private DefaultMutableTreeNode addNodeToPathTree(String path) {
		if (pathTree == null) {
			pathTree = new TreeMap<String, DefaultMutableTreeNode>();
			DefaultMutableTreeNode root = new DefaultMutableTreeNode("/");
			pathTree.put("/", root);
		}

		if (path == null || path.equals("")) {
			return pathTree.get("/");
		}

		if (!pathTree.containsKey(path)) {
			DefaultMutableTreeNode parent = addNodeToPathTree(path.substring(0,
					path.lastIndexOf("/")));
			String name = path.substring(path.lastIndexOf("/") + 1);
			DefaultMutableTreeNode node = new DefaultMutableTreeNode(name);
			pathTree.put(path, node);
			parent.add(node);
		}

		return pathTree.get(path);

	}

	private void updateTreeCommentInfo() {
		stat_info_jTextArea.setText("处理中");
		comments_info_jEditorPane.setText("处理中");
		if (tree_comment_info == null) {
			tree_comment_info = new TreeMap<String, String>();
		} else {
			tree_comment_info.clear();
		}

		backTask = new Thread(new BackgroundUpdateTask());
		backTask.start();
	}

	private void jTreeValueChanged(TreeSelectionEvent e) {
		DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) jTree0
				.getLastSelectedPathComponent();
		if (selectedNode != null) {
			StringBuilder sb = new StringBuilder();
			TreeNode[] tn = selectedNode.getPath();
			for (int i = 1; i < tn.length; ++i) {
				sb.append("/" + tn[i].toString());
			}
			tree_selected_path_text_field.setText(sb.toString());

			updateTreeCommentInfo();
		}

	}

	class BackgroundUpdateTask implements Runnable {

		private StringBuilder info;

		private StringBuilder comments;

		WordSeg wordSeg = new WordSeg();

		/**
		 * 将注释按词性着色
		 * 
		 * @param pos_tag
		 *            词性符号
		 * @param color
		 *            颜色
		 * @param content
		 *            原始注释
		 * @return
		 */
		private String getColorString(char pos_tag, String color, String content) {
			StringBuilder result = new StringBuilder();
			List<String> seg_result = wordSeg.segmentation(content,
					WordSeg.POS_TAG, WordSeg.SEG_FILTER_WITHPUNC, true);

			if (pos_tag == WordSeg.PUNCTUATION) {
				for (String w : seg_result) {
					int index = w.lastIndexOf('/');
					if (w.charAt(index + 1) != pos_tag
							&& w.charAt(index + 1) != WordSeg.NOUN
							&& w.charAt(index + 1) != WordSeg.VERB) {
						result.append(ca.getColorString(w.substring(0, index),
								color));
					} else {
						result.append(w.substring(0, index));
					}
				}
			} else {
				for (String w : seg_result) {
					int index = w.lastIndexOf('/');
					if (w.charAt(index + 1) == pos_tag) {
						result.append(ca.getColorString(w.substring(0, index),
								color));
					} else {
						result.append(w.substring(0, index));
					}
				}
			}

			return result.toString();
		}

		@Override
		public void run() {
			jTree_comment_type_comboBox.removeAllItems();
			info = new StringBuilder();
			comments = new StringBuilder();

			// 每次各一个唯一的更新编号，如果当前这个处理还没有处理完，又点了其他路径，update_counter会变大，这样的话当前这次计算的结果就不会覆盖后点击的路径结果。
			int mycouter = ++update_counter;
			String selectedPath = tree_selected_path_text_field.getText();

			if (selectedPath != null && !selectedPath.equals("")) {
				Map<String, String> typeCounter = null;
				try {
					typeCounter = cqa.statCommentedRatio(selectedPath);
				} catch (ClassNotFoundException | SQLException | IOException e) {
					e.printStackTrace();
				}
				Map<String, Map<String, Integer>> template_word_stat = new HashMap<String, Map<String, Integer>>();
				Set<String> templates = new TreeSet<String>();

				// 选择抽取模板的模式
				int template_type = CommentAnalyzer.TEMPLATE_SAME_TYPE;

				if (template_type_same_editor_radio_btn.isSelected()) {
					template_type = CommentAnalyzer.TEMPLATE_SAME_EDITOR;
				} else if (template_type_same_type_radio_btn.isSelected()) {
					template_type = CommentAnalyzer.TEMPLATE_SAME_TYPE;
				}

				// 为了避免多线程时的界面更新异常，先定义一个临时变量temp_tree_comment_info保存当前选中路径的注释着色信息，等确定是本线程更新以后，在更新EvaluationTool里面的tree_comment_info变量
				Map<String, String> temp_tree_comment_info = new HashMap<String, String>();
				for (Map.Entry<String, String> entry : allComments.entrySet()) {
					String path = entry.getKey();
					if (path.startsWith(selectedPath)) {
						String type = comment_types.get(path);

						String filename = "";
						if (!type.equals("file")) {
							filename = path.substring(0, path.lastIndexOf("/"));
						} else {
							filename = path;
						}

						StringBuilder url = new StringBuilder();
						url.append(PropertiesUtil
								.getProperties()
								.getProperty(
										"mysql.data.gui.EvaluationTool.sourcecode_url_prefix"));
						url.append(filename);
						url.append(PropertiesUtil
								.getProperties()
								.getProperty(
										"mysql.data.gui.EvaluationTool.sourcecode_url_version"));

						if (!type.equals("file")) {
							url.append("#" + getLineNo(path));
						}

						String title = "<a href = '" + url.toString() + "'>"
								+ path + "</a>   "
								+ ca.getPathEditors().get(path).toString()
								+ "<br/>";
						comments.append(title);

						List<String> t = ca
								.loadNewTemplate(path, template_type);

						if (!type.equals("file")) {
							templates.add(type + ":" + t.toString());

							Map<String, Integer> wordCounter = null;
							if (template_word_stat.containsKey(type)) {
								wordCounter = template_word_stat.get(type);
							} else {
								wordCounter = new HashMap<String, Integer>();
							}

							for (String s : t) {
								if (wordCounter.containsKey(s)) {
									wordCounter.put(s, wordCounter.get(s) + 1);
								} else {
									wordCounter.put(s, 1);
								}
							}

							template_word_stat.put(type, wordCounter);
						}

						String color_info = "";
						if (color_type_template_radio_btn.isSelected()) {
							color_info = getColoredInfo(path, template_type);
						} else if (color_type_noun_radio_btn.isSelected()) {
							color_info = getColorString(WordSeg.NOUN, "blue",
									entry.getValue());
						} else if (color_type_verb_radio_btn.isSelected()) {
							color_info = getColorString(WordSeg.VERB, "blue",
									entry.getValue());
						} else if (color_type_other_radio_btn.isSelected()) {
							color_info = getColorString(WordSeg.PUNCTUATION,
									"blue", entry.getValue());
						}

						comments.append(color_info);
						comments.append("<br/><br/>");

						temp_tree_comment_info.put(path, title + color_info);
					}
				}

				for (Map.Entry<String, String> entry : typeCounter.entrySet()) {
					info.append(entry.getKey() + ":" + entry.getValue()
							+ "\r\n");
				}

				for (String t : template_word_stat.keySet()) {
					info.append(t + ": " + template_word_stat.get(t).toString()
							+ "\r\n");
				}

				for (String t : templates) {
					if (!t.equals(""))
						info.append(t + "\r\n");
				}

				if (mycouter == update_counter) {
					tree_comment_info = temp_tree_comment_info;
					stat_info = info.toString();
					stat_info_jTextArea.setText(stat_info);
					comments_info_jEditorPane.setText(comments.toString());
					logger.info("thread update\r\n" + comments.toString());
					jTree_comment_type_comboBox.addItem("ALL");
					for (String t : typeCounter.keySet()) {
						jTree_comment_type_comboBox.addItem(t);
					}
				} else {
					System.out.println("cancle " + selectedPath);
				}
			}
		}

	}
}
