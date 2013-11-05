package mysql.data.util;

import java.util.ArrayList;
import java.util.List;

public class LxrType {
	public static final int class_struct_or_union_member = 0;
	public static final int enumeration_name = 1;
	public static final int enumerator = 2;
	public static final int extern_or_forward_variable_declaration = 3;
	public static final int file = 4;
	public static final int function_definition = 5;
	public static final int function_prototype_or_declaration = 6;
	public static final int macro_un_definition = 7;
	public static final int structure_name = 8;
	public static final int typedef = 9;
	public static final int union_name = 10;
	public static final int variable_definition = 11;	
	
	public static String getTypeName(int type){
		List<String> typenames = new ArrayList<String>();
		typenames.add("class, struct, or union member");
		typenames.add("enumeration name");
		typenames.add("enumerator");
		typenames.add("extern or forward variable declaration");
		typenames.add("file");
		typenames.add("function definition");
		typenames.add( "function prototype or declaration");
		typenames.add("macro (un)definition");
		typenames.add("structure name");
		typenames.add("typedef");
		typenames.add("union name");
		typenames.add("variable definition");
		return typenames.get(type);
	}
}
