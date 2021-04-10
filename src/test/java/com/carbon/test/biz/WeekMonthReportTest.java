package com.carbon.test.biz;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.codehaus.plexus.util.dag.DAG;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import cho.carbon.context.core.FusionContext;
import cho.carbon.context.hc.HCFusionContext;
import cho.carbon.entity.entity.Entity;
import cho.carbon.fg.eln.constant.EnumKeyValue;
import cho.carbon.fg.eln.timertask.ProjectReportTask;
import cho.carbon.panel.Discoverer;
import cho.carbon.panel.Integration;
import cho.carbon.panel.IntegrationMsg;
import cho.carbon.panel.PanelFactory;


@ContextConfiguration(locations = "classpath*:spring-core.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class WeekMonthReportTest {
	
	Logger logger = LoggerFactory.getLogger(WeekMonthReportTest.class);
	protected String mapperName = "默认周月总结";// 结构体的名称
	
	
	@Test
	public void readData() {
			long startTime = System.currentTimeMillis();
			HCFusionContext context=new HCFusionContext();
			context.setSource(FusionContext.SOURCE_COMMON);
//			context.setToEntityRange(BizFusionContext.ENTITY_CONTENT_RANGE_ABCNODE_CONTAIN);
			context.setStrucTitle(mapperName);
			context.setUserCode("69328017186241720");
			Integration integration=PanelFactory.getIntegration();
			Entity entity=createEntity(mapperName);
			logger.debug("初始实体： " + entity.toJson());
			IntegrationMsg imsg=integration.integrate(context,entity);
			
			boolean success = imsg.success();
			logger.debug("融合情况： " + success);
			if (!success) {
				
				System.out.println("融合拒绝情况： " + imsg.getRefuse());
			} else {
				String code=imsg.getCode();
				Discoverer discoverer=PanelFactory.getDiscoverer(context);
				Entity result=discoverer.discover(code);
				logger.debug("融合后实体： " + code + " : "+ result.toJson());
				
				long endTime = System.currentTimeMillis();// 记录结束时间
				logger.debug(( (endTime - startTime) / 1000 ) + "");
				
				// 删除实体
			}
	}
	
	
	private Entity createEntity(String mappingName) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		java.util.Date startTime = null;
		try {
			startTime = sdf.parse("2021-03-028");
		} catch (ParseException e) {
			e.printStackTrace();
		}
		java.util.Date endTime = new java.util.Date();
		
		Entity entity = new Entity(mappingName);
		entity.putValue("唯一编码", "156920710379118594");
		entity.putValue("开始日期", startTime); 
//		entity.putValue("结束日期", endTime);

		entity.putValue("周月总结命令", EnumKeyValue.ENUM_周月总结命令_生成报告); 
//		
		return entity;
	}
	
	
}
