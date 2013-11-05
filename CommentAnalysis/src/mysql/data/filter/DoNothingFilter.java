package mysql.data.filter;

public class DoNothingFilter implements FilterBase {

	@Override
	public String getText(String inputStr) {
		return inputStr.trim();
	}

}
