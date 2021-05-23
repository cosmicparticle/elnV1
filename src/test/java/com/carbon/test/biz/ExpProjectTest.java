package com.carbon.test.biz;
import java.math.BigDecimal;

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
public class ExpProjectTest {
	
	Logger logger = LoggerFactory.getLogger(ExpProjectTest.class);
	protected String mapperName = "默认实验项目";// 结构体的名称
	
	
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
				
				logger.info("融合拒绝情况： " + imsg.getRefuse());
			}
			
			String code=imsg.getCode();
			Discoverer discoverer=PanelFactory.getDiscoverer(context);
			Entity result=discoverer.discover(code);
			logger.debug("融合后实体： " + code + " : "+ result.toJson());
			
			long endTime = System.currentTimeMillis();// 记录结束时间
			logger.debug(( (endTime - startTime) / 1000 ) + "");
			
			// 删除实体
			
			
	}
	
	
	// 客户下单
	private Entity createEntity(String mappingName) {
		
		Entity entity = new Entity(mappingName);
		entity.putValue("唯一编码", "157895685135179782");
		entity.putValue("方案描述", "描述11"); 
		entity.putValue("项目命令", EnumKeyValue.ENUM_实验项目命令_存档命令); 
//		
		return entity;
	}
	
	
	@Test
	public void fff() {
		
		BigDecimal aa = new BigDecimal("0");
		BigDecimal bb = new BigDecimal("1");
		
		BigDecimal divide = aa.divide(bb);
		
	}
	
}
