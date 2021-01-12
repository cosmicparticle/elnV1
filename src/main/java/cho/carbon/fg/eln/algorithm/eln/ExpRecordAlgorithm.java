package cho.carbon.fg.eln.algorithm.eln;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import cho.carbon.complexus.FGRecordComplexus;
import cho.carbon.fg.eln.algorithm.pojo.PutMaterialRatio;
import cho.carbon.fg.eln.constant.BaseConstant;
import cho.carbon.fg.eln.constant.RelationType;
import cho.carbon.fg.eln.constant.item.ExpProcessCELNE3433Item;
import cho.carbon.fg.eln.constant.item.MateriaInfoCELNE3393Item;
import cho.carbon.fg.eln.constant.item.MaterialRatioCELNE3466Item;
import cho.carbon.fuse.improve.ops.builder.FGRecordOpsBuilder;
import cho.carbon.message.Message;
import cho.carbon.message.MessageFactory;
import cho.carbon.model.uid.UidManager;
import cho.carbon.ops.builder.RecordRelationOpsBuilder;
import cho.carbon.relation.RecordRelation;
import cho.carbon.rrc.builder.FGRootRecordBuilder;
import cho.carbon.rrc.record.FGRootRecord;

/**
 * 	实验记录规则
 * @author lhb
 *
 */
public class ExpRecordAlgorithm {

	/**
	 *	 计算投料总量
	 * @param recordComplexus
	 * @param recordCode
	 * @param recordOpsBuilder
	 * @return
	 */
	public static Message computeMaterialGross(FGRecordComplexus recordComplexus, String recordCode, FGRecordOpsBuilder recordOpsBuilder, List<FGRootRecord> relatedRecordList, List<RecordRelationOpsBuilder>  relatedRelationOpsBuilderList) {
		try {
			// 获取当前实验记录
			FGRootRecord rootRecord = CommonAlgorithm.getRootRecord(recordComplexus, BaseConstant.TYPE_实验记录, recordCode);
			// 获取实验记录的操作过程
			List<RecordRelation> expProcessRelaList = (List)CommonAlgorithm.getAppointRecordRelation(recordComplexus, BaseConstant.TYPE_实验记录, recordCode, RelationType.RR_实验记录_实验操作过程_实验操作过程);
			if (expProcessRelaList.isEmpty()) {
				return MessageFactory.buildInfoMessage("computeMaterialGrossSucceeded", "没有计算投料总量", BaseConstant.TYPE_实验记录, "没有计算投料总量");
			}
			// 获取当前实验记录对应的所有投料信息
			List<PutMaterialRatio> putMaterialRatioList = new ArrayList<PutMaterialRatio>();
			
			for (RecordRelation expProcessRela : expProcessRelaList) {
				// 获取实验操作过程的唯一code
				String expProcessCode = expProcessRela.getRightCode();
				// 获取实验过程操作时间
				String expProcessTime = CommonAlgorithm.getDataValue(recordComplexus,  BaseConstant.TYPE_实验操作过程, expProcessCode, ExpProcessCELNE3433Item.基本属性组_时间);
				
				// 根据操作过程唯一code， 获取操作过程对应的投料信息 关系
				List<RecordRelation> putMaterialRatioRelaList = (List)CommonAlgorithm.getAppointRecordRelation(recordComplexus, BaseConstant.TYPE_实验操作过程, expProcessCode, RelationType.RR_实验操作过程_投料信息_投料信息);
				if (putMaterialRatioRelaList.isEmpty()) {
					// 没有投料结束当前循环
					continue;
				}
				
				for (RecordRelation putMaterialRatioRela : putMaterialRatioRelaList) {
					// 获取投料信息唯一编码
					String putMaterialCode = putMaterialRatioRela.getRightCode();
					
					// 获取投料信息对应的物料信息
					List<RecordRelation> materialRelaList = (List)CommonAlgorithm.getAppointRecordRelation(recordComplexus, BaseConstant.TYPE_投料信息, putMaterialCode, RelationType.RR_投料信息_物料信息_物料基础信息);
					if (materialRelaList.isEmpty()) {
						// 没有物料结束当前循环
						continue;
					}
					// 获取物料code
					String materialCode = materialRelaList.get(0).getRightCode();
					String materialName = CommonAlgorithm.getDataValue(recordComplexus, BaseConstant.TYPE_物料基础信息, materialCode, MateriaInfoCELNE3393Item.基本属性组_物料名称);
					
					//  获取投料实体
					FGRootRecord putMaterial = CommonAlgorithm.getRootRecord(recordComplexus, BaseConstant.TYPE_投料信息, putMaterialCode);
					
					String planAmount = CommonAlgorithm.getDataValue(putMaterial, MaterialRatioCELNE3466Item.基本属性组_计划投料量);
					String actualAmount = CommonAlgorithm.getDataValue(putMaterial, MaterialRatioCELNE3466Item.基本属性组_实际投料量);
					String putMateriaUnit = CommonAlgorithm.getDataValue(putMaterial, MaterialRatioCELNE3466Item.基本属性组_投料量单位);

					PutMaterialRatio putMaterialRatio = new PutMaterialRatio(putMaterialCode, materialCode, planAmount, actualAmount, putMateriaUnit, expProcessTime);
					putMaterialRatio.setMaterialName(materialName);
					
					putMaterialRatioList.add(putMaterialRatio);
				}
			}
			
			// 汇总物料实际投入量
			Map<String, PutMaterialRatio> map = new HashMap<String, PutMaterialRatio>();
			// 循环所有的投料信息， 根据物资唯一Code进行分类
			for (PutMaterialRatio putMaterialRatio : putMaterialRatioList) {
				String materiaCode = putMaterialRatio.getMateriaCode();
				
				PutMaterialRatio pm = map.get(materiaCode);
				if (pm == null) {
					pm = putMaterialRatio;
				} else {
					// 没有 计算实际投料量总和
					String actualAmount = putMaterialRatio.getActualAmount();
					String putMateriaUnit = putMaterialRatio.getPutMateriaUnit();
					
					String actualAmountSum = pm.getActualAmount();
					String putMateriaUnit2 = pm.getPutMateriaUnit();
					
					if (!putMateriaUnit.equals(putMateriaUnit2)) {
						return MessageFactory.buildRefuseMessage("computeMaterialGrossFailed", "计算投料总量失败-", BaseConstant.TYPE_实验记录, "实验记录对应操作过程中投料信息相同物料【"+pm.getMaterialName()+"】单位必须一致");
					}
					
					Double sum = Double.parseDouble(actualAmountSum) + Double.parseDouble(actualAmount);
					pm.setActualAmount(sum+"");
				}
				map.put(materiaCode, pm);
			}
			
			// 根据实验记录， 获取投料方案
			List<RecordRelation> materialRatioRelaList = (List)CommonAlgorithm.getAppointRecordRelation(recordComplexus, BaseConstant.TYPE_实验记录, recordCode, RelationType.RR_实验记录_物料配比_投料信息);
//			if (materialRatioRelaList.isEmpty()) {
//				// 生成所有方案
//			}
			
			// 有方案的补充方案， 没有方案的新增方案
			for (RecordRelation putMaterialRatioRela : materialRatioRelaList) {
				// 获取投料信息Code
				String putMaterialCode = putMaterialRatioRela.getRightCode();
				
				// 获取投料信息对应的物料信息
				List<RecordRelation> materialRelaList = (List)CommonAlgorithm.getAppointRecordRelation(recordComplexus, BaseConstant.TYPE_投料信息, putMaterialCode, RelationType.RR_投料信息_物料信息_物料基础信息);
				if (materialRelaList.isEmpty()) {
					// 没有物料结束当前循环
					return MessageFactory.buildInfoMessage("computeMaterialGrossSucceeded", "投料方案", BaseConstant.TYPE_实验记录, "没有物料基础信息");
				}
				// 获取物料唯一编码
				String materialCode = materialRelaList.get(0).getRightCode();
				
				// 获取实际投料信息
				PutMaterialRatio putMaterialRatio = map.get(materialCode);
				if (putMaterialRatio != null) {
					map.remove(materialCode);
					
					// 实际投料量
					String actualAmount = putMaterialRatio.getActualAmount();
					FGRootRecordBuilder builder =FGRootRecordBuilder.getInstance(BaseConstant.TYPE_投料信息,putMaterialCode);
					builder.putAttribute(MaterialRatioCELNE3466Item.基本属性组_实际投料量, actualAmount);
					
					relatedRecordList.add(builder.getRootRecord());
				} else {
					// 实际投料量 设置为0
					FGRootRecordBuilder builder =FGRootRecordBuilder.getInstance(BaseConstant.TYPE_投料信息,putMaterialCode);
					builder.putAttribute(MaterialRatioCELNE3466Item.基本属性组_实际投料量, 0);
					
					relatedRecordList.add(builder.getRootRecord());
				}
			}
			
			// 遍历map， 生成新的投料方案
	        Iterator<Entry<String, PutMaterialRatio>> it = map.entrySet().iterator();
	        while (it.hasNext()) {
	            Entry<String, PutMaterialRatio> entry = it.next();
	            PutMaterialRatio pm = entry.getValue();
	            
	            // 生成新的实体， 并和当前实验记录 关联关系 - 投料方案关系
	            
				String longUID = UidManager.getLongUID() + "";
				//先得到一个FGRootRecordBuilder对象，用于辅助构建记录，第一个参数是模型类型名称，第二个参数是待构建记录的编码
				FGRootRecordBuilder materialRatioBuilder =FGRootRecordBuilder.getInstance(BaseConstant.TYPE_投料信息,longUID);
				//设置记录属性，第一个参数为模型属性的编码，第二个参数为模型属性的取值
				materialRatioBuilder.putAttribute(MaterialRatioCELNE3466Item.基本属性组_实际投料量, pm.getActualAmount());
				materialRatioBuilder.putAttribute(MaterialRatioCELNE3466Item.基本属性组_投料量单位, pm.getPutMateriaUnit());
				//得到记录对象
				FGRootRecord materialRatioRecord = materialRatioBuilder.getRootRecord();
				relatedRecordList.add(materialRatioRecord);
				
				RecordRelationOpsBuilder putMaterialRelaOpsBuilder = RecordRelationOpsBuilder.getInstance(BaseConstant.TYPE_投料信息, longUID);
				// 
				putMaterialRelaOpsBuilder.putRelation(RelationType.RR_投料信息_实验记录_实验记录, recordCode);
				putMaterialRelaOpsBuilder.putRelation(RelationType.RR_投料信息_物料信息_物料基础信息, pm.getMateriaCode());
	            
				relatedRelationOpsBuilderList.add(putMaterialRelaOpsBuilder);
	        }
		} catch (Exception e) {
			e.printStackTrace();
			return MessageFactory.buildRefuseMessage("computeMaterialGrossFailed", "计算投料总量失败", BaseConstant.TYPE_实验记录, "计算投料总量失败");
		}
		return MessageFactory.buildInfoMessage("computeMaterialGrossSucceeded", "计算投料总量成功", BaseConstant.TYPE_实验记录, "计算投料总量成功");
	}
	
	
	/**
	 *	 实验记录增加审核人
	 * @param recordComplexus
	 * @param recordCode
	 * @param recordOpsBuilder
	 * @return
	 */
	public static Message addVerifier(FGRecordComplexus recordComplexus, String recordCode, RecordRelationOpsBuilder relationOpsBuilder) {
		try {
			// 获取当前实验记录
			FGRootRecord rootRecord = CommonAlgorithm.getRootRecord(recordComplexus, BaseConstant.TYPE_实验记录, recordCode);
			// 获取实验记录的操作过程
			List<RecordRelation> expProcessRelaList = (List)CommonAlgorithm.getAppointRecordRelation(recordComplexus, BaseConstant.TYPE_实验记录, recordCode, RelationType.RR_实验记录_关联项目_实验项目);
			if (expProcessRelaList.isEmpty() || expProcessRelaList.size() != 1) {
				return MessageFactory.buildRefuseMessage("Failed", "实验记录", BaseConstant.TYPE_实验记录, "关联实验项目不唯一");
			}
			// 获取实验项目的code
			String expProjectCode = expProcessRelaList.get(0).getRightCode();
			List<RecordRelation> groupLeaderRela = (List)CommonAlgorithm.getAppointRecordRelation(recordComplexus, BaseConstant.TYPE_实验项目, expProjectCode, RelationType.RR_实验项目_组长_系统用户);
			if (groupLeaderRela.isEmpty() || groupLeaderRela.size() != 1) {
				return MessageFactory.buildRefuseMessage("Failed", "实验记录", BaseConstant.TYPE_实验记录, "实验项目的组长不唯一");
			}
			// 获取组长唯一编码
			String groupLeaderCode = groupLeaderRela.get(0).getRightCode();
			
			// 获取当前当前实验记录的审核员
			List<RecordRelation> verifierRela = (List)CommonAlgorithm.getAppointRecordRelation(recordComplexus, BaseConstant.TYPE_实验记录, recordCode, RelationType.RR_实验记录_审核员_系统用户);
			if (verifierRela == null || verifierRela.isEmpty() || verifierRela.size()==0) {
				// 直接增加审核员
				relationOpsBuilder.putRelation(RelationType.RR_实验记录_审核员_系统用户, groupLeaderCode);
			} else {
				// 有一个值
				String verifierCode = verifierRela.get(0).getRightCode();
				if (!verifierCode.equals(groupLeaderCode)) {
					// 更换新的审核员
					relationOpsBuilder.putRelation(RelationType.RR_实验记录_审核员_系统用户, groupLeaderCode);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return MessageFactory.buildRefuseMessage("computeMaterialGrossFailed", "计算投料总量失败", BaseConstant.TYPE_实验记录, "计算投料总量失败");
		}
		return MessageFactory.buildInfoMessage("computeMaterialGrossSucceeded", "计算投料总量成功", BaseConstant.TYPE_实验记录, "计算投料总量成功");
	}
}
