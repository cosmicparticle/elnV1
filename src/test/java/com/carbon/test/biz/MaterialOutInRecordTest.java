package com.carbon.test.biz;
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
import cho.carbon.panel.Discoverer;
import cho.carbon.panel.Integration;
import cho.carbon.panel.IntegrationMsg;
import cho.carbon.panel.PanelFactory;

/**
 * 物料出入库测试
 * @author lhb
 */
@ContextConfiguration(locations = "classpath*:spring-core.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class MaterialOutInRecordTest {
	
	Logger logger = LoggerFactory.getLogger(MaterialOutInRecordTest.class);
	protected String mapperName = "默认物料出入库记录";// 结构体的名称
	
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
				throw new RuntimeException("融合拒绝情况");
			}
			
			String code=imsg.getCode();
			Discoverer discoverer=PanelFactory.getDiscoverer(context);
			Entity result=discoverer.discover(code);
			logger.debug("融合后实体： " + code + " : "+ result.toJson());
			
			long endTime = System.currentTimeMillis();// 记录结束时间
			logger.debug(( (endTime - startTime) / 1000 ) + "");
			
			// 删除实体
			
			
	}
	private Entity createEntity(String mappingName) {
		
		Entity entity = new Entity(mappingName);
		entity.putValue("唯一编码", "140922573529325572");
		entity.putValue("物料出入库命令", EnumKeyValue.ENUM_物料出入库命令_销售出库命令);
		
		return entity;
	}
	
	
}
