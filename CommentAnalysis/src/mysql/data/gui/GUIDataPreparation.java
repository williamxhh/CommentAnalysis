package mysql.data.gui;

import java.io.IOException;
import java.sql.SQLException;

import mysql.data.CommentClassifier;
import mysql.data.DataClean;
import mysql.data.DataSource;
import mysql.data.analysis.CommentAnalyzer;

public class GUIDataPreparation {
	public static void main(String[] args) throws IOException, SQLException {
		DataSource.main(args);
		DataClean.main(args);
		CommentClassifier.main(args);
		
		CommentAnalyzer.LOADDATATODB = true;
		CommentAnalyzer.main(args);
	}
	
}
