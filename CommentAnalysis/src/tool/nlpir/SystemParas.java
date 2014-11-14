package tool.nlpir;

/**
 * 系统配置参数加载类
 * 
 * @author kernal
 * 
 */
public class SystemParas {
	public static String dll_or_so_lib_path = ReadConfigUtil.getValue("dll_or_so_path");
	public static String user_dict = ReadConfigUtil.getValue("user_dict");
}
