package cho.carbon.biz.timertask;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
@Lazy(value=false)
public class TimeTaskDemo {
	//@Scheduled(cron = "0 0 1 * * ?")//每天凌晨1点整
    //@Scheduled(cron = "0 30 0 * * ?")//每天凌晨0点30分
    //@Scheduled(cron = "0 */60 * * * ?")//1小时处理一次
	
	/*@Scheduled(cron = "0/40 * * * * ?")
	public void doSomething() {
	}*/
	 

}
