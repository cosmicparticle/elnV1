package cho.carbon.biz.common;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.kie.api.runtime.KieSession;

import cho.carbon.complexus.FGRecordComplexus;
import cho.carbon.fuse.fg.FGFusionContext;
import cho.carbon.fuse.fg.ImproveResult;
import cho.carbon.fuse.improve.attribute.FuseAttribute;
import cho.carbon.fuse.improve.attribute.leaf.FuseLeafAttribute;
import cho.carbon.fuse.improve.ops.builder.FuseFGRecordOpsBuilder;
import cho.carbon.fuse.improve.transfer.BizzAttributeTransfer;
import cho.carbon.meta.criteria.model.ModelCriterion;
import cho.carbon.ops.builder.RecordRelationOpsBuilder;
import cho.carbon.ops.complexus.OpsComplexus;
import cho.carbon.query.model.FGConJunctionFactory;
import cho.carbon.relation.FGRelationCorrelation;
import cho.carbon.rrc.builder.FGRootRecordBuilder;
import cho.carbon.rrc.record.FGAttribute;
import cho.carbon.rrc.record.FGRootRecord;
import cho.carbon.rrc.record.RootRecord;


public class KIEHelper {
	
	
	private static Logger logger = Logger.getLogger(KIEHelper.class);
	
	public static Collection<ModelCriterion> getBizCriteriaListFromKIE(String recordCode, FGRecordComplexus complexus,
			KieSession kSession) {
		FGRootRecord record = complexus.getRootRecord(recordCode);
		String recordName = record.getName();
		
		List<FuseAttribute> transfer = BizzAttributeTransfer.transfer(record);
		
		BizzAttributeTransfer.transfer(record).forEach(fuseAttribute -> kSession.insert(fuseAttribute));
		kSession.setGlobal("recordName", recordName);

		FGConJunctionFactory conJunctionFactory = null;
		try {
			
			conJunctionFactory= new FGConJunctionFactory(recordName);
			
			//conJunctionFactory.getGroupFactory().addCommon(itemCode, value, UnaryOperator)
			//conJunctionFactory.getGroupFactory().addBetween(itemCode, left, right, operator)
			//conJunctionFactory.getGroup2DFactory("").addBetween(itemCode, left, right, operator)
			//conJunctionFactory.getGroupFactory().addCommon($nameFB.getAttribute().getName(), $nameFB.getAttribute().getValueStr(), UnaryOperator.EQUAL);
			//conJunctionFactory.getGroupFactory().addCommon($IdFB.getAttribute().getName(), $IdFB.getAttribute().getValueStr(), UnaryOperator.EQUAL);
			
			kSession.setGlobal("conJunctionFactory", conJunctionFactory);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// 触发规则
		logger.debug("开始执行规则===================== ");
		int fireAllRules = kSession.fireAllRules();
		logger.debug("本次触发规则数量 =  " + fireAllRules);
		logger.debug("规则执行完毕===================== ");
		Collection<ModelCriterion> criterions;
		if(conJunctionFactory.getJunction()==null) {
			criterions=null;
		}else {
			criterions = conJunctionFactory.getJunction().getCriterions();
		}
		 
		
		kSession.destroy();
		return criterions;
	}

	public static ImproveResult getImproveResultFromKIE(FGFusionContext fgFusionContext, String recordCode,
			OpsComplexus opsComplexus, FGRecordComplexus recordComplexus, KieSession kSession) {
		
		String userCode = fgFusionContext.getUserCode();
		FGRootRecord fgRootRecord = recordComplexus.getRootRecord(recordCode);

		String recordName = fgRootRecord.getName();
		String hostCode = recordComplexus.getHostCode();
		String hostType = recordComplexus.getHostType();
		// 定义 全局变量
		
		List<FGRootRecord> rootRecordList = new ArrayList<FGRootRecord>();
		List<FGAttribute> attributeList = new ArrayList<FGAttribute>();
		
		List<FuseLeafAttribute> addedLeafAttrList = new ArrayList<FuseLeafAttribute>();
		Map<String, String> removedLeafAttrMap = new HashMap<String, String>();
		
		//存放新建
		List<RecordRelationOpsBuilder> recordRelationOpsBuilderNew = new ArrayList<RecordRelationOpsBuilder>();
		
		RecordRelationOpsBuilder recordRelationOpsBuilder = RecordRelationOpsBuilder.getInstance(recordName,
				recordCode);

		try {
			kSession.setGlobal("userCode", userCode);
			kSession.setGlobal("recordCode", recordCode);
			kSession.setGlobal("recordName", recordName);
			kSession.setGlobal("recordRelationOpsBuilder", recordRelationOpsBuilder);
			
			kSession.setGlobal("recordRelationOpsBuilderNew", recordRelationOpsBuilderNew);
		} catch (Exception e) {
			logger.debug("全局变量未设置： recordRelationOpsBuilderNew");
		}
		try {
			kSession.setGlobal("rootRecordList", rootRecordList);
		} catch (Exception e) {
			logger.debug("全局变量未设置： rootRecordList");
		}
		try {
			kSession.setGlobal("hostCode", hostCode);
		} catch (Exception e) {
			logger.debug("全局变量未设置： hostCode");
		}
		try {
			kSession.setGlobal("hostType", hostType);
		} catch (Exception e) {
			logger.debug("全局变量未设置： hostType");
		}
		try {
			kSession.setGlobal("attributeList", attributeList);
		} catch (Exception e) {
			logger.debug("全局变量未设置： attributeList");
		}
		try {
			kSession.setGlobal("addedLeafAttrList", addedLeafAttrList);
		} catch (Exception e) {
			logger.debug("全局变量未设置： addedLeafAttrList");
		}
		try {
			kSession.setGlobal("removedLeafAttrMap", removedLeafAttrMap);
		} catch (Exception e) {
			logger.debug("全局变量未设置： removedLeafAttrMap");
		}
		try {
			kSession.setGlobal("fgRootRecord", fgRootRecord);
		} catch (Exception e) {
			logger.debug("全局变量未设置： rootRecord");
		}
		try {
			kSession.setGlobal("recordComplexus", recordComplexus);
		} catch (Exception e) {
			logger.debug("全局变量未设置： recordComplexus");
		}
		
		// insert object
		BizzAttributeTransfer.transfer(fgRootRecord).forEach(fuseAttribute -> kSession.insert(fuseAttribute));
		
		//这里需要改
		 FGRelationCorrelation relationCorrelation = recordComplexus.getRelationCorrelation(recordCode);
	
		if (relationCorrelation != null) {
			
			relationCorrelation.getRecordRelation().forEach(recordRelation -> kSession.insert(recordRelation));
		}
		
		if (opsComplexus != null) {
			if (opsComplexus.getRootRecordOps(recordCode) != null) {
				BizzAttributeTransfer.transfer(opsComplexus.getRootRecordOps(recordCode))
						.forEach(opsAttr -> kSession.insert(opsAttr));
			}

			if (opsComplexus.getRecordRelationOps(recordCode) != null) {
				BizzAttributeTransfer.transfer(opsComplexus.getRecordRelationOps(recordCode))
						.forEach(opsRelation -> kSession.insert(opsRelation));
			}

		}

		// 触发规则
		logger.debug(  "开始执行规则===================== ");
		int fireAllRules = kSession.fireAllRules();
		logger.debug("本次触发规则数量 =  " + fireAllRules);
		logger.debug("规则执行完毕===================== ");
		kSession.destroy();

		// 组装结果
		FuseFGRecordOpsBuilder recordOpsBuilder = FuseFGRecordOpsBuilder.getInstance(recordName, recordCode);
		recordOpsBuilder.addAttribute(attributeList);
		
		recordOpsBuilder.addLeafAttribute(addedLeafAttrList);
		// 删除的多值属性
		for (String key : removedLeafAttrMap.keySet()) {
			recordOpsBuilder.removeLeaf(removedLeafAttrMap.get(key), key);
		}
		
		ImproveResult imprveResult = new ImproveResult();
		imprveResult.setRootRecordOps(recordOpsBuilder.getRootRecordOps());
		imprveResult.setRecordRelationOps(recordRelationOpsBuilder.getRecordRelationOps());
		imprveResult.setGeneratedRecords(rootRecordList);
		
		for (RecordRelationOpsBuilder builder : recordRelationOpsBuilderNew) {
			imprveResult.putDerivedRecordRelationOps(builder.getRecordRelationOps());
		}
		
		return imprveResult;
	}

	public static ImproveResult getImproveResultFromKIE(FGFusionContext context, String recordCode,
			FGRecordComplexus recordComplexus, KieSession kSession) {
		return getImproveResultFromKIE(context, recordCode, null, recordComplexus, kSession);
	}

}
