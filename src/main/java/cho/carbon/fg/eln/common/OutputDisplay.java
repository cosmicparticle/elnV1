package cho.carbon.fg.eln.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OutputDisplay {
	
	static Logger logger = LoggerFactory.getLogger(OutputDisplay.class);
	public static void showText(Object text) {
		logger.info("【【【【" + text + "】】】】");
	}
}
