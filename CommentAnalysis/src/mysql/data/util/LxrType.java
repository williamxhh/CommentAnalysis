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
	
	private static List<String> typeNames = new ArrayList<String>();
	private static List<Integer> typeValues = new ArrayList<Integer>();
	
	public static String getTypeName(int type){
		if(typeNames.size()==0){
			fillTypeList();
		}
		return typeNames.get(type);
	}
	
	public static List<String> getTypeList(){
		if(typeNames.size()==0){
			fillTypeList();
		}
		return typeNames;
	}
	
	public static List<Integer> getTypeValues(){
		if(typeValues.size()==0){
			fillTypeList();
		}
		return typeValues;
	}
	
	private static void fillTypeList(){
		typeNames.add("class, struct, or union member");
		typeValues.add(LxrType.class_struct_or_union_member);
		
		typeNames.add("enumeration name");
		typeValues.add(LxrType.enumeration_name);
		
		typeNames.add("enumerator");
		typeValues.add(LxrType.enumerator);
		
		typeNames.add("extern or forward variable declaration");
		typeValues.add(LxrType.extern_or_forward_variable_declaration);
		
		typeNames.add("file");
		typeValues.add(LxrType.file);
		
		typeNames.add("function definition");
		typeValues.add(LxrType.function_definition);
		
		typeNames.add( "function prototype or declaration");
		typeValues.add(LxrType.function_prototype_or_declaration);
		
		typeNames.add("macro (un)definition");
		typeValues.add(LxrType.macro_un_definition);
		
		typeNames.add("structure name");
		typeValues.add(LxrType.structure_name);
		
		typeNames.add("typedef");
		typeValues.add(LxrType.typedef);
		
		typeNames.add("union name");
		typeValues.add(LxrType.union_name);
		
		typeNames.add("variable definition");
		typeValues.add(LxrType.variable_definition);
	}
}
