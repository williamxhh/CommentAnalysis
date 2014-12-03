package mysql.data.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 代码文件的注释入口总数
 * @author Xiaowei GAO
 * @date 2014年12月1日
 * @description TODO
 * @ClassName CommentEntryCount
 *
 */
public class CommentEntryCount implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String path;
	private Map<String, Integer> type_count;
	
	public CommentEntryCount(String path) {
		this.path = path;
		type_count = new HashMap<String, Integer>();
	}
	
	public void addType(String type, int count) {
		if(type_count == null) {
			type_count = new HashMap<String, Integer>();
		}
		if(type_count.containsKey(type)) {
			type_count.put(type, type_count.get(type) + count);
		} else {
			type_count.put(type, count);
		}
	}
	
	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public Map<String, Integer> getType_count() {
		return type_count;
	}

	public void setType_count(Map<String, Integer> type_count) {
		this.type_count = type_count;
	}


	@Override
	public int hashCode() {
		return this.path.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return path.equals(((CommentEntryCount)obj).getPath());
	}
	
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(path + "\r\n");
		for(String t: type_count.keySet()) {
			sb.append("[" + t + "]\t" + type_count.get(t) + "\r\n");
		}
		return sb.toString();
	}

	public static void main(String[] args) throws FileNotFoundException, IOException, ClassNotFoundException {
		Map<String,CommentEntryCount> map = new HashMap<String,CommentEntryCount>();
		CommentEntryCount t1 = new CommentEntryCount("a1");
		t1.addType("a", 1);
		CommentEntryCount t2 = new CommentEntryCount("b2");
		t2.addType("b", 2);
		CommentEntryCount t3 = new CommentEntryCount("c3");
		t3.addType("c", 3);
		
		map.put("a", t1);
		map.put("b", t2);
		map.put("c", t3);
		
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("test.out"));
		oos.writeObject(map);
		oos.flush();
		oos.close();
		
		ObjectInputStream ois = new ObjectInputStream(new FileInputStream("test.out"));
		Map<String,CommentEntryCount> readlist = (Map<String,CommentEntryCount>)ois.readObject();
		for(CommentEntryCount item: readlist.values()) {
			System.out.println(item.toString());
		}
		ois.close();
	}
}
