package cho.carbon.fg.eln.common;


public class CommonCalculation {

	public static boolean isBasicLawful(Object value){
		if(value==null || "".equals(value)){
			return false;
		}
		return true;
	}

}
