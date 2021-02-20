package com.zhsq.test.biz;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mvel2.optimizers.impl.refl.nodes.ArrayLength;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import cho.carbon.context.core.FusionContext;
import cho.carbon.context.hc.HCFusionContext;
import cho.carbon.entity.entity.Entity;
import cho.carbon.panel.Discoverer;
import cho.carbon.panel.Integration;
import cho.carbon.panel.IntegrationMsg;
import cho.carbon.panel.PanelFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ContextConfiguration(locations = "classpath*:spring-core.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class StockTest {
	
	 static Logger logger = LoggerFactory.getLogger(StockTest.class);
	protected String mapperName = "仪器库存";
	
	
//	@Test
	public void readData() {
		
			long startTime = System.currentTimeMillis();
			HCFusionContext context=new HCFusionContext();
			context.setSource(FusionContext.SOURCE_COMMON);
//			context.setToEntityRange(BizFusionContext.ENTITY_CONTENT_RANGE_ABCNODE_CONTAIN);
			context.setStrucTitle(mapperName);
			context.setUserCode("e10adc3949ba59abbe56e057f28888d5");
			Integration integration=PanelFactory.getIntegration();
			Entity entity=createEntity(mapperName);
			logger.debug("初始实体： " + entity.toJson());
			IntegrationMsg imsg=integration.integrate(context,entity);
			String code=imsg.getCode();
			Discoverer discoverer=PanelFactory.getDiscoverer(context);
			Entity result=discoverer.discover(code);
			logger.debug("融合后实体： " + code + " : "+ result.toJson());
			
			long endTime = System.currentTimeMillis();// 记录结束时间
			logger.debug(((float) (endTime - startTime) / 1000) + "");
	}
	
	private Entity createEntity(String mappingName) {
		
		Entity entity = new Entity(mappingName);
		//entity.putValue("唯一编码", "d0e2eb99c6c34aeaa9e66c893afe4b89");
		entity.putValue("名称", "测试1"); 
		entity.putValue("材质", "玻璃");
		entity.putValue("库存量", "201");

		entity.putValue("操作类型", "出库操作"); 
		entity.putValue("数量", "100");
		return entity;
	}

	
	@Test
	public void ab() {
		
		List<Integer> list = new ArrayList<Integer>();
//		list.add(8);
		list.add(11);
		Integer scope = 01;
		
		for (int i = 0; i < list.size(); i++) {
			scope = (scope | list.get(i));
		}
		System.out.println("=========scope: " + scope);
		System.out.println( (8 & scope));
	}
	
	
}
