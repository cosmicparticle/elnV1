package cho.carbon.biz.common;

import org.apache.log4j.Logger;

public class OutputDisplay {
	
	private static Logger logger = Logger.getLogger(OutputDisplay.class);
	public static void showText(Object text) {
		logger.debug("【【【【" + text + "】】】】");
	}
}
