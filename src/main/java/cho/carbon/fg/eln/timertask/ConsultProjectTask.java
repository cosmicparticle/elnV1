package cho.carbon.fg.eln.timertask;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import cho.carbon.context.hc.HCFusionContext;
import cho.carbon.fg.eln.constant.BaseConstant;
import cho.carbon.fg.eln.constant.EnumKeyValue;
import cho.carbon.fg.eln.constant.item.ConsultProjectCELNE4320Item;
import cho.carbon.meta.enun.operator.BetweenOperator;
import cho.carbon.meta.enun.operator.IncludeOperator;
import cho.carbon.meta.enun.operator.UnaryOperator;
import cho.carbon.panel.Integration;
import cho.carbon.panel.IntegrationMsg;
import cho.carbon.panel.PanelFactory;
import cho.carbon.query.model.ConJunctionFactory;
import cho.carbon.query.model.QueryRecordParmFactory;
import cho.carbon.record.query.RecordQueryPanel;
import cho.carbon.rrc.builder.FGRootRecordBuilder;

/**
 * 查阅项目的定时任务
 * @author lhb
 *
 */
@Component
@Lazy(value=false)
public class ConsultProjectTask {
	
	/**
	 *	  每天晚上十点执行， 过期查阅项目
	 */
    @Scheduled(cron = "0 0 22 1/1 * ? ") 
    public void updateConsultStatus() {
    	 LocalDate localDate = LocalDate.now();
    	 DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
         String currutTime = localDate.format(fmt);
    	
    	//new 一个QueryRecordParmFactory对象，构造参数为待查询模型的类型名称。
		QueryRecordParmFactory queryRecordParmFactory=new QueryRecordParmFactory(BaseConstant.TYPE_查阅项目);
		ConJunctionFactory groupFactory = queryRecordParmFactory.getConJunctionFactory().getGroupFactory();
		groupFactory.addCommon(ConsultProjectCELNE4320Item.基本属性组_查阅状态, EnumKeyValue.ENUM_查阅项目状态_查阅中, UnaryOperator.EQUAL);
		groupFactory.addCommon(ConsultProjectCELNE4320Item.基本属性组_查阅截止日期, currutTime, UnaryOperator.LESSEROREQUAL);
//		groupFactory.addBetween(ProjectPlanCELNE3976Item.基本属性组_计划开始时间, null, currutTime, BetweenOperator.BETWEEN);
		// 执行查询
		List<String> codes = RecordQueryPanel.queryCodeList(queryRecordParmFactory.getQueryParameter());
		
		for (String code : codes) {
			//首先创建或获得一个record对象。
			FGRootRecordBuilder builder = FGRootRecordBuilder.getInstance(BaseConstant.TYPE_查阅项目, code);
			//设置属性
			builder.putAttribute(ConsultProjectCELNE4320Item.基本属性组_查阅状态, EnumKeyValue.ENUM_查阅项目状态_查阅到期);
			//创建一个融合上下文对象
			HCFusionContext context = new HCFusionContext();
			//获取一个融合器
			Integration integration = PanelFactory.getIntegration();
			//执行融合
			IntegrationMsg msg = integration.integrate(context, builder.getRootRecord());
		}
    }
    
}
