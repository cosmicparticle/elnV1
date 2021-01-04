package cho.carbon.fg.eln.common;

import org.apache.commons.lang.StringUtils;

public class CommonCalculation {

	/**
	 * 	判断是合法的
	 * @param value
	 * @return
	 */
	public static boolean isBasicLawful(Object value){
		
		if (value == null) {
			return false;
		}
		
		if (value instanceof String) {
			boolean blank = StringUtils.isBlank((String)value);
			return !blank;
		}
		
		return true;
	}
	
	/**
	 * 判断是非法的
	 * @param value
	 * @return
	 */
	public static boolean isNotBasicLawful(Object value){
		if (value == null) {
			return true;
		}
		
		if (value instanceof String) {
			boolean blank = StringUtils.isBlank((String)value);
			return blank;
		}
		
		return true;
	}

}
