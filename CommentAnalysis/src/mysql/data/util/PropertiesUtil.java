package mysql.data.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

public class PropertiesUtil {
	static final String PROPERTIESFILE = "/CommentAnalysis.properties";
//	static final String PROPERTIESFILE = "/CommentAnalysis.local.properties";
	
	private static Properties props = null;
	
	public static Properties getProperties(){
		if(props==null){
			loadProperties();
		}
		return props;
	}
	
	private static void loadProperties(){
		BufferedReader reader = new BufferedReader(new InputStreamReader(PropertiesUtil.class.getResourceAsStream(PROPERTIESFILE)));
		try {
			props = new Properties();
			props.load(reader);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
