package mysql.data.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

public class PropertiesUtil {
	static final String PROPERTIESFILE = "/CommentAnalysis.properties";
//	static final String PROPERTIESFILE = "/CommentAnalysis.local.properties";
	
	private static Properties props = null;
	
//	public static Properties getProperties(){
//		if(props==null){
//			loadProperties();
//		}
//		return props;
//	}
	
	public static String getProperty(String key) {
		return getProperty(key,"");
	}
	
	public static String getProperty(String key, String default_value) {
		if(props == null) {
			loadProperties();
		}
		int index = Integer.parseInt(props.getProperty("mysql.data.util.PropertiesUtil.confIndex","1"));
		
		String[] valueList = props.getProperty(key).split(";");
		if(valueList.length == 1) {
			return valueList[0];
		}
		
		if(index > valueList.length) {
			return default_value;
		}
		return valueList[index - 1];
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
