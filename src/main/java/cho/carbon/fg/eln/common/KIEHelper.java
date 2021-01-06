package cho.carbon.fg.eln.common;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.kie.api.runtime.KieSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cho.carbon.complexus.FGRecordComplexus;
import cho.carbon.context.core.RemoveRecordInfo;
import cho.carbon.context.fg.FuncGroupContext;
import cho.carbon.fuse.fg.ConJunctionFGResult;
import cho.carbon.fuse.fg.FetchFGResult;
import cho.carbon.fuse.fg.ImproveFGResult;
import cho.carbon.fuse.fg.ValidatorFGResult;
import cho.carbon.fuse.improve.ops.builder.FGRecordOpsBuilder;
import cho.carbon.fuse.improve.transfer.BizzAttributeTransfer;
import cho.carbon.message.Message;
import cho.carbon.meta.criteria.model.ModelConJunction;
import cho.carbon.meta.criteria.model.ModelCriterion;
import cho.carbon.ops.builder.ConJunctionOpsBuilder;
import cho.carbon.ops.builder.RecordRelationOpsBuilder;
import cho.carbon.ops.complexus.OpsComplexus;
import cho.carbon.query.model.FGConJunctionFactory;
import cho.carbon.record.RecordBeanOpsBuilder;
import cho.carbon.relation.FGRelationCorrelation;
import cho.carbon.rrc.Updatable;
import cho.carbon.rrc.record.FGRootRecord;

public class KIEHelper {

	static Logger logger = LoggerFactory.getLogger(KIEHelper.class);

	public static Collection<ModelCriterion> getBizCriteriaListFromKIE(String recordCode, FGRecordComplexus complexus,
			KieSession kSession) {
		FGRootRecord record = complexus.getRecord(recordCode);
		String recordName = record.getName();

		BizzAttributeTransfer.transfer(record).forEach(fuseAttribute -> kSession.insert(fuseAttribute));

		FGConJunctionFactory conJunctionFactory = null;
		try {
			conJunctionFactory = new FGConJunctionFactory(recordName);
			kSession.setGlobal("conJunctionFactory", conJunctionFactory);
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			kSession.setGlobal("recordName", recordName);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// 触发规则
		logger.debug("开始执行规则===================== ");
		int fireAllRules = kSession.fireAllRules();
		logger.debug("本次触发规则数量 =  " + fireAllRules);
		logger.debug("规则执行完毕===================== ");

		kSession.destroy();

		ModelConJunction junction = conJunctionFactory.getJunction();
		Collection<ModelCriterion> criterions = null;
		if (junction != null) {
			criterions = junction.getCriterions();
		}

		return criterions;
	}

//	public static ImproveFGResult getImproveFGResultFromKIE1(FuncGroupContext fgFusionContext, String recordCode,
//			OpsComplexus opsComplexus, FGRecordComplexus recordComplexus, KieSession kSession) {
//
//		String userCode = fgFusionContext.getUserCode();
//		FGRootRecord currentRecord = recordComplexus.getRootRecord(recordCode);
//
//		String currentRecordName = currentRecord.getName();
//		String hostCode = recordComplexus.getHostCode();
//		String hostName = recordComplexus.getHostType();
//		// 定义 全局变量
//
//		List<FGRootRecord> relatedRecordList = new ArrayList<FGRootRecord>();
//		List<FGAttribute> updateAttrList = new ArrayList<FGAttribute>();
//		List<FGAttribute> meAddList = new ArrayList<FGAttribute>();
//		List<FGAttribute> meRemoveList = new ArrayList<FGAttribute>();
//		List<FuseMLineAttr> mlineUpdateAttrList = new ArrayList<FuseMLineAttr>();
//		Map<String, String> mlineRemoveMap = new HashMap<String, String>();
//
//		// 存放新建
//		List<RecordRelationOpsBuilder> relatedRecordRelationOpsBuilderList = new ArrayList<RecordRelationOpsBuilder>();
//
//		RecordRelationOpsBuilder currentRecordRelationOpsBuilder = RecordRelationOpsBuilder
//				.getInstance(currentRecordName, recordCode);
//		List<Message> messageList = new ArrayList<Message>();
//		try {
//			kSession.setGlobal("relatedRecordRelationOpsBuilderList", relatedRecordRelationOpsBuilderList);
//		} catch (Exception e) {
//			logger.debug("全局变量未设置： relatedRecordRelationOpsBuilderList");
//		}
//
//		try {
//			kSession.setGlobal("currentRecordRelationOpsBuilder", currentRecordRelationOpsBuilder);
//		} catch (Exception e) {
//			logger.debug("全局变量未设置： currentRecordRelationOpsBuilder");
//		}
//
//		try {
//			kSession.setGlobal("recordCode", recordCode);
//		} catch (Exception e) {
//			logger.debug("全局变量未设置： recordCode");
//		}
//
//		try {
//			kSession.setGlobal("userCode", userCode);
//		} catch (Exception e) {
//			logger.debug("全局变量未设置： userCode");
//		}
//
//		try {
//			kSession.setGlobal("currentRecordName", currentRecordName);
//		} catch (Exception e) {
//			logger.debug("全局变量未设置： currentRecordName");
//		}
//
//		try {
//			kSession.setGlobal("relatedRecordList", relatedRecordList);
//		} catch (Exception e) {
//			logger.debug("全局变量未设置： relatedRecordList");
//		}
//		try {
//			kSession.setGlobal("hostCode", hostCode);
//		} catch (Exception e) {
//			logger.debug("全局变量未设置： hostCode");
//		}
//		try {
//			kSession.setGlobal("hostName", hostName);
//		} catch (Exception e) {
//			logger.debug("全局变量未设置： hostName");
//		}
//		try {
//			kSession.setGlobal("updateAttrList", updateAttrList);
//		} catch (Exception e) {
//			logger.debug("全局变量未设置： updateAttrList");
//		}
//		try {
//			kSession.setGlobal("meAddList", meAddList);
//		} catch (Exception e) {
//			logger.debug("全局变量未设置： meAddList");
//		}
//		try {
//			kSession.setGlobal("meRemoveList", meRemoveList);
//		} catch (Exception e) {
//			logger.debug("全局变量未设置： meRemoveList");
//		}
//		try {
//			kSession.setGlobal("mlineUpdateAttrList", mlineUpdateAttrList);
//		} catch (Exception e) {
//			logger.debug("全局变量未设置： mlineUpdateAttrList");
//		}
//		try {
//			kSession.setGlobal("mlineRemoveMap", mlineRemoveMap);
//		} catch (Exception e) {
//			logger.debug("全局变量未设置： mlineRemoveMap");
//		}
//		try {
//			kSession.setGlobal("currentRecord", currentRecord);
//		} catch (Exception e) {
//			logger.debug("全局变量未设置： currentRecord");
//		}
//		try {
//			kSession.setGlobal("recordComplexus", recordComplexus);
//		} catch (Exception e) {
//			logger.debug("全局变量未设置： recordComplexus");
//		}
//		try {
//			kSession.setGlobal("messageList", messageList);
//		} catch (Exception e) {
//			logger.debug("全局变量未设置： messageList");
//		}
//		// insert object
//		BizzAttributeTransfer.transfer(currentRecord).forEach(fuseAttribute -> kSession.insert(fuseAttribute));
//
//		// 这里需要改
//		FGRelationCorrelation relationCorrelation = recordComplexus.getRelationCorrelation(recordName, recordCode);
//
//		if (relationCorrelation != null) {
//
//			relationCorrelation.getRecordRelation().forEach(recordRelation -> kSession.insert(recordRelation));
//		}
//
//		if (opsComplexus != null) {
//			if (opsComplexus.getRootRecordOps(recordCode) != null) {
//				BizzAttributeTransfer.transfer(opsComplexus.getRootRecordOps(recordCode))
//						.forEach(opsAttr -> kSession.insert(opsAttr));
//			}
//
//			if (opsComplexus.getRecordRelationOps(recordCode) != null) {
//				BizzAttributeTransfer.transfer(opsComplexus.getRecordRelationOps(recordCode))
//						.forEach(opsRelation -> kSession.insert(opsRelation));
//			}
//
//		}
//
//		// 触发规则
//		logger.debug("开始执行规则===================== ");
//		int fireAllRules = kSession.fireAllRules();
//		logger.debug("本次触发规则数量 =  " + fireAllRules);
//		logger.debug("规则执行完毕===================== ");
//		kSession.destroy();
//
//		// 组装结果
//		FGRecordOpsBuilder recordOpsBuilder = FGRecordOpsBuilder.getInstance(currentRecordName, recordCode);
//		recordOpsBuilder.addUpdateAttr(updateAttrList);
//
//		meAddList.forEach(attr -> {
//			recordOpsBuilder.addMeAddAttr(attr);
//		});
//
//		meRemoveList.forEach(attr -> {
//			recordOpsBuilder.addMeRemoveAttr(attr);
//		});
//
//		recordOpsBuilder.addMLineUpdateAttr(mlineUpdateAttrList);
//		// 删除的多值属性
//		for (String key : mlineRemoveMap.keySet()) {
//			recordOpsBuilder.addRemoveMLine(mlineRemoveMap.get(key), key);
//		}
//
//		ImproveFGResult imprveResult = new ImproveFGResult();
//		imprveResult.setHostRecordOps(recordOpsBuilder.getRootRecordOps());
//		imprveResult.setRecordRelationOps(currentRecordRelationOpsBuilder.getRecordRelationOps());
//		imprveResult.setRelatedRecords(relatedRecordList);
//		imprveResult.setMessages(messageList);
//
//		for (RecordRelationOpsBuilder builder : relatedRecordRelationOpsBuilderList) {
//			imprveResult.putRelatedRecordRelationOps(builder.getRecordRelationOps());
//		}
//
//		return imprveResult;
//	}

	public static ImproveFGResult getImproveFGResultFromKIE(FuncGroupContext fgFusionContext, String recordCode,
			OpsComplexus opsComplexus, FGRecordComplexus recordComplexus, KieSession kSession) {
		// 定义 全局变量
		String userCode = fgFusionContext.getUserCode();
		FGRootRecord record = recordComplexus.getRecord(recordCode);
		String recordName = record.getName();
		String hostCode = recordComplexus.getHostCode();
		String hostName = recordComplexus.getHostName();
		FGRecordOpsBuilder recordOpsBuilder = FGRecordOpsBuilder.getInstance(recordName, recordCode);
		List<Updatable> updateBeans = new ArrayList<>();
		List<FGRootRecord> relatedRecordList = new ArrayList<FGRootRecord>();
		List<RemoveRecordInfo> removeRecordInfoList=new ArrayList<>();

		// 存放新建
		List<RecordRelationOpsBuilder> relatedRelationOpsBuilderList = new ArrayList<RecordRelationOpsBuilder>();
		
		RecordRelationOpsBuilder relationOpsBuilder = RecordRelationOpsBuilder.getInstance(recordName, recordCode);
		List<Message> messageList = new ArrayList<Message>();

		try {
			kSession.setGlobal("recordCode", recordCode);
		} catch (Exception e) {
			logger.debug("全局变量未设置： recordCode");
		}

		try {
			kSession.setGlobal("userCode", userCode);
		} catch (Exception e) {
			logger.debug("全局变量未设置： userCode");
		}
		try {
			kSession.setGlobal("recordName", recordName);
		} catch (Exception e) {
			logger.debug("全局变量未设置： recordName");
		}
		try {
			kSession.setGlobal("hostCode", hostCode);
		} catch (Exception e) {
			logger.debug("全局变量未设置： hostCode");
		}
		try {
			kSession.setGlobal("hostName", hostName);
		} catch (Exception e) {
			logger.debug("全局变量未设置： hostName");
		}
		try {
			kSession.setGlobal("record", record);
		} catch (Exception e) {
			logger.debug("全局变量未设置： record");
		}
		try {
			kSession.setGlobal("recordComplexus", recordComplexus);
		} catch (Exception e) {
			logger.debug("全局变量未设置： recordComplexus");
		}
		try {
			kSession.setGlobal("recordOpsBuilder", recordOpsBuilder);
		} catch (Exception e) {
			logger.debug("全局变量未设置： recordOpsBuilder");
		}
		
		try {
			kSession.setGlobal("relatedRecordList", relatedRecordList);
		} catch (Exception e) {
			logger.debug("全局变量未设置： relatedRecordList");
		}
		try {
			kSession.setGlobal("updateBeans", updateBeans);
		} catch (Exception e) {
			logger.debug("全局变量未设置： updateBeans");
		}
		try {
			kSession.setGlobal("removeRecordInfoList", removeRecordInfoList);
		} catch (Exception e) {
			logger.debug("全局变量未设置： removeRecordInfoList");
		}
		try {
			kSession.setGlobal("relatedRelationOpsBuilderList", relatedRelationOpsBuilderList);
		} catch (Exception e) {
			logger.debug("全局变量未设置： relatedRelationOpsBuilderList");
		}

		try {
			kSession.setGlobal("relationOpsBuilder", relationOpsBuilder);
		} catch (Exception e) {
			logger.debug("全局变量未设置： relationOpsBuilder");
		}

		try {
			kSession.setGlobal("messageList", messageList);
		} catch (Exception e) {
			logger.debug("全局变量未设置： messageList");
		}
		// insert object
		BizzAttributeTransfer.transfer(record).forEach(fuseAttribute -> kSession.insert(fuseAttribute));

		// 这里需要改
		FGRelationCorrelation relationCorrelation = recordComplexus.getRelationCorrelation(recordName, recordCode);

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
		logger.debug("开始执行规则===================== ");
		int fireAllRules = kSession.fireAllRules();
		logger.debug("本次触发规则数量 =  " + fireAllRules);
		logger.debug("规则执行完毕===================== ");
		kSession.destroy();

		RecordBeanOpsBuilder builder1 = RecordBeanOpsBuilder.getInstance(recordComplexus, recordCode);
		updateBeans.forEach(updated -> {
			builder1.putRecordBean(updated);
		});

		ImproveFGResult imprveResult = new ImproveFGResult();
		imprveResult.setUpdateRecord(builder1.getHostRecord());
		List<FGRootRecord> relatedRecords = builder1.getRelatedRecords();
		if (relatedRecords != null) {
			relatedRecordList.addAll(relatedRecords);
		}
		imprveResult.setRecordOps(recordOpsBuilder.getRecordOps());
		imprveResult.setRelationOps(relationOpsBuilder.getRecordRelationOps());
		imprveResult.setRelatedRecords(relatedRecordList);
		imprveResult.setMessages(messageList);
	
		for (RecordRelationOpsBuilder builder2 : relatedRelationOpsBuilderList) {
			imprveResult.putRelatedRelationOps(builder2.getRecordRelationOps());
		}

		return imprveResult;
	}

	public static ImproveFGResult getImproveFGResultFromKIE(FuncGroupContext context, String recordCode,
			FGRecordComplexus recordComplexus, KieSession kSession) {
		return getImproveFGResultFromKIE(context, recordCode, null, recordComplexus, kSession);
	}

	public static ValidatorFGResult getValidatorInfoFromKIE(FuncGroupContext context, String recordCode,
			FGRecordComplexus recordComplexus, KieSession kSession) {

		String userCode = context.getUserCode();
		FGRootRecord record = recordComplexus.getRecord(recordCode);

		String recordName = record.getName();
		String hostCode = recordComplexus.getHostCode();
		String hostName = recordComplexus.getHostName();
		// 定义 全局变量

		List<Message> messageList = new ArrayList<Message>();
		try {
			kSession.setGlobal("recordCode", recordCode);
		} catch (Exception e) {
			logger.debug("全局变量未设置： recordCode");
		}
		try {
			kSession.setGlobal("recordName", recordName);
		} catch (Exception e) {
			logger.debug("全局变量未设置： recordName");
		}
		
		try {
			kSession.setGlobal("userCode", userCode);
		} catch (Exception e) {
			logger.debug("全局变量未设置： userCode");
		}

		
		try {
			kSession.setGlobal("hostCode", hostCode);
		} catch (Exception e) {
			logger.debug("全局变量未设置： hostCode");
		}
		try {
			kSession.setGlobal("hostName", hostName);
		} catch (Exception e) {
			logger.debug("全局变量未设置： hostName");
		}

		try {
			kSession.setGlobal("record", record);
		} catch (Exception e) {
			logger.debug("全局变量未设置： record");
		}
		try {
			kSession.setGlobal("recordComplexus", recordComplexus);
		} catch (Exception e) {
			logger.debug("全局变量未设置： recordComplexus");
		}
		try {
			kSession.setGlobal("messageList", messageList);
		} catch (Exception e) {
			logger.debug("全局变量未设置： messageList");
		}

		// insert object
		BizzAttributeTransfer.transfer(record).forEach(fuseAttribute -> kSession.insert(fuseAttribute));

		// 这里需要改
		FGRelationCorrelation relationCorrelation = recordComplexus.getRelationCorrelation(recordName,recordCode);

		if (relationCorrelation != null) {
			relationCorrelation.getRecordRelation().forEach(recordRelation -> kSession.insert(recordRelation));
		}

		// 触发规则
		logger.debug("开始执行规则===================== ");
		int fireAllRules = kSession.fireAllRules();
		logger.debug("本次触发规则数量 =  " + fireAllRules);
		logger.debug("规则执行完毕===================== ");
		kSession.destroy();

		// 组装结果
		ValidatorFGResult fuseCheckInfo = new ValidatorFGResult(recordCode);
		fuseCheckInfo.setMessages(messageList);
		return fuseCheckInfo;
	}

	public static FetchFGResult getFetchImproveResultFromKIE(FuncGroupContext context, FGRootRecord record,
			KieSession kSession) {

		String userCode = context.getUserCode();
		// 定义 全局变量
		String recordName = record.getName();
		String recordCode = record.getCode();
		FGRecordOpsBuilder recordOpsBuilder = FGRecordOpsBuilder.getInstance(recordName, recordCode);
		
		
		try {
			kSession.setGlobal("recordCode", recordCode);
		} catch (Exception e) {
			logger.debug("全局变量未设置： recordCode");
		}

		try {
			kSession.setGlobal("userCode", userCode);
		} catch (Exception e) {
			logger.debug("全局变量未设置： userCode");
		}

		try {
			kSession.setGlobal("recordName", recordName);
		} catch (Exception e) {
			logger.debug("全局变量未设置： recordName");
		}

		try {
			kSession.setGlobal("record", record);
		} catch (Exception e) {
			logger.debug("全局变量未设置： record");
		}
		
		try {
			kSession.setGlobal("recordOpsBuilder", recordOpsBuilder);
		} catch (Exception e) {
			logger.debug("全局变量未设置： recordOpsBuilder");
		}

		// insert object
		BizzAttributeTransfer.transfer(record).forEach(fuseAttribute -> kSession.insert(fuseAttribute));

		// 触发规则
		logger.debug("开始执行规则===================== ");
		int fireAllRules = kSession.fireAllRules();
		logger.debug("本次触发规则数量 =  " + fireAllRules);
		logger.debug("规则执行完毕===================== ");
		kSession.destroy();

		// 组装结果
		FetchFGResult fetchImproveResult = new FetchFGResult();

		fetchImproveResult.setRecordOps(recordOpsBuilder.getRecordOps());
		return fetchImproveResult;
	}

	public static ConJunctionFGResult getConJunctionImproveResultFromKIE(FuncGroupContext context,
			ModelConJunction modelConJunction, KieSession kSession) {

		String userCode = context.getUserCode();

		String recordName = modelConJunction == null ? context.getModelCode() : modelConJunction.getRecordName();

		// 定义 全局变量
//		Collection<ModelCriterion> addedCriterions = new ArrayList<>();
//		Collection<String> removedCriterions = new HashSet<>();
//		Collection<RightRelationJunction> addedRRJunctions = new ArrayList<>();
//		Collection<String> removedRRJunctions = new HashSet<>();
		ConJunctionOpsBuilder conJunctionOpsBuilder = ConJunctionOpsBuilder.getInstance(recordName);

		try {
			kSession.setGlobal("userCode", userCode);
		} catch (Exception e) {
			logger.debug("全局变量未设置： userCode");
		}

		try {
			kSession.setGlobal("recordName", recordName);
		} catch (Exception e) {
			logger.debug("全局变量未设置： recordName");
		}

		try {
			kSession.setGlobal("modelConJunction", modelConJunction);
		} catch (Exception e) {
			logger.debug("全局变量未设置： rootRecord");
		}
		try {
			kSession.setGlobal("conJunctionOpsBuilder", conJunctionOpsBuilder);
		} catch (Exception e) {
			logger.debug("全局变量未设置： conJunctionOpsBuilder");
		}
		
		if (modelConJunction != null) {
			// insert object
			modelConJunction.getCriterions().forEach(criterion -> kSession.insert(criterion));
		}

		// 触发规则
		logger.debug("开始执行规则===================== ");
		int fireAllRules = kSession.fireAllRules();
		logger.debug("本次触发规则数量 =  " + fireAllRules);
		logger.debug("规则执行完毕===================== ");
		kSession.destroy();

		// 组装结果
		ConJunctionFGResult conJunctionFGResult = new ConJunctionFGResult();
		
		conJunctionFGResult.setConJunctionOps(conJunctionOpsBuilder.getRootRecordOps());
		return conJunctionFGResult;
	}

}
