package cho.carbon.fg.eln.timertask;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import cho.carbon.context.hc.HCFusionContext;
import cho.carbon.fg.eln.constant.BaseConstant;
import cho.carbon.fg.eln.constant.EnumKeyValue;
import cho.carbon.fg.eln.constant.RelationType;
import cho.carbon.fg.eln.constant.item.ElnprojectCELNE2244Item;
import cho.carbon.fg.eln.constant.item.ExpProjectReportCELNE3499Item;
import cho.carbon.fuse.fg.ImproveFGResult;
import cho.carbon.meta.enun.operator.IncludeOperator;
import cho.carbon.model.uid.UidManager;
import cho.carbon.ops.builder.RecordRelationOpsBuilder;
import cho.carbon.panel.Integration;
import cho.carbon.panel.IntegrationMsg;
import cho.carbon.panel.PanelFactory;
import cho.carbon.query.model.ConJunctionFactory;
import cho.carbon.query.model.QueryRecordParmFactory;
import cho.carbon.record.query.RecordQueryPanel;
import cho.carbon.rrc.builder.FGRootRecordBuilder;

@Configuration      //1.主要用于标记配置类，兼备Component的效果。
@EnableScheduling   // 2.开启定时任务
public class ProjectReportTask {
	
	public static void main(String[] args) {
		 LocalDate localDate = LocalDate.now();
		 int year = localDate.getYear();
		 
		 int monthValue = localDate.getMonthValue();
		 
		 DayOfWeek dayOfWeek = localDate.getDayOfWeek();
		 int value = dayOfWeek.getValue();
		 int dayOfMonth = localDate.getDayOfMonth();
		 
		 int weekNo = ((localDate.getDayOfMonth()-1) / 7) +1;
		 
		 System.out.println("年： " + year);
		 System.out.println("月： " + monthValue);
		 System.out.println("周： " + weekNo);
	}
	
	//3.添加定时任务
    @Scheduled(cron = "0 59 2 ? * FRI")
    public void configureTasks() {
    	
//    	 LocalDate localDate = LocalDate.now();
//		 int year = localDate.getYear();
//		 
//		 int monthValue = localDate.getMonthValue();
    	 
    	//new 一个QueryRecordParmFactory对象，构造参数为待查询模型的类型名称。
		QueryRecordParmFactory queryRecordParmFactory=new QueryRecordParmFactory(BaseConstant.TYPE_实验项目);
		List<String> paramList = new ArrayList<String>();
		paramList.add(EnumKeyValue.ENUM_项目状态_进行中+"");
		ConJunctionFactory groupFactory = queryRecordParmFactory.getConJunctionFactory().getGroupFactory();
		groupFactory.addInclude(ElnprojectCELNE2244Item.基本属性组_项目状态, paramList, IncludeOperator.INCLUDES);
//		groupFactory.addCommon(FreightOrderZNCZE2776Item.基本属性组_计划运输日期, yesterday, UnaryOperator.LESSEROREQUAL);
		
		// 执行查询
		List<String> codes=RecordQueryPanel.queryCodeList(queryRecordParmFactory.getQueryParameter());
		
		for (String code : codes) {
			// 修改货运订单状态
			//首先创建或获得一个record对象。
			FGRootRecordBuilder builder = FGRootRecordBuilder.getInstance(BaseConstant.TYPE_实验项目, code);
			//设置属性
			builder.putAttribute(ElnprojectCELNE2244Item.基本属性组_项目命令, EnumKeyValue.ENUM_实验项目命令_生成周报命令);
			//创建一个融合上下文对象
			HCFusionContext context = new HCFusionContext();
			//获取一个融合器
			Integration integration = PanelFactory.getIntegration();
			//执行融合
			IntegrationMsg msg = integration.integrate(context, builder.getRootRecord());
		}
    }
    
}
