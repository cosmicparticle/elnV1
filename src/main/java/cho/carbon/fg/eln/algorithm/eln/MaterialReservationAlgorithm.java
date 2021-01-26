package cho.carbon.fg.eln.algorithm.eln;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import cho.carbon.complexus.FGRecordComplexus;
import cho.carbon.fg.eln.algorithm.ComputeSign;
import cho.carbon.fg.eln.algorithm.MaterialUnitUtil;
import cho.carbon.fg.eln.constant.BaseConstant;
import cho.carbon.fg.eln.constant.EnumKeyValue;
import cho.carbon.fg.eln.constant.RelationType;
import cho.carbon.fg.eln.constant.item.MaterialRatioCELNE3466Item;
import cho.carbon.fg.eln.constant.item.MaterialStockInfoCELNE3551Item;
import cho.carbon.fg.eln.constant.item.ReserveInventoryCELNE3881Item;
import cho.carbon.fuse.improve.ops.builder.FGRecordOpsBuilder;
import cho.carbon.message.Message;
import cho.carbon.message.MessageFactory;
import cho.carbon.model.uid.UidManager;
import cho.carbon.ops.builder.RecordRelationOpsBuilder;
import cho.carbon.query.model.FGConJunctionFactory;
import cho.carbon.query.model.QueryRecordParmFactory;
import cho.carbon.record.query.RecordQueryPanel;
import cho.carbon.relation.RecordRelation;
import cho.carbon.rrc.builder.FGRootRecordBuilder;
import cho.carbon.rrc.record.FGRootRecord;

/**
 *  	物料预定算法
 * @author lhb
 */
public class MaterialReservationAlgorithm {

	/**
	 * 	执行预定命令
	 * @param recordComplexus
	 * @param recordCode
	 * @param recordOpsBuilder
	 * @return
	 */
	public static Message reservationcommand(FGRecordComplexus recordComplexus, String recordCode, FGRecordOpsBuilder recordOpsBuilder, List<FGRootRecord> relatedRecordList, List<RecordRelationOpsBuilder>  relatedRelationOpsBuilderList, ComputeSign computeSign) {
		try {
			// 获取物料预定实体
			FGRootRecord rootRecord = CommonAlgorithm.getRootRecord(recordComplexus, BaseConstant.TYPE_物料预定, recordCode);
			
			// 预定项目必填
			List<RecordRelation> projectRlea = (List)CommonAlgorithm.getAppointRecordRelation(recordComplexus, BaseConstant.TYPE_物料预定, recordCode, RelationType.RR_物料预定_所属项目_实验项目);
			if (projectRlea == null || projectRlea.size() != 1) {
				return MessageFactory.buildRefuseMessage("Failed", "预定物料", BaseConstant.TYPE_物料预定, "所属项目必填");
			}
			// 获取项目code
			String projectCode = projectRlea.get(0).getRightCode();
			
			// 获取物料预定实体对应的投料信息 （可以是多个
			List<RecordRelation> mreservationRela = (List)CommonAlgorithm.getAppointRecordRelation(recordComplexus, BaseConstant.TYPE_物料预定, recordCode, RelationType.RR_物料预定_预定物料_投料信息);
			for (RecordRelation recordRelation : mreservationRela) {
				// 循环所有的预定投料信息
				// 获取投料唯一编码
				String rightCode = recordRelation.getRightCode();
				
//				投料信息中预定物料量和单位必填
				String materialuUitStr = CommonAlgorithm.getDataValue(recordComplexus, BaseConstant.TYPE_投料信息, rightCode, MaterialRatioCELNE3466Item.基本属性组_投料量单位);
				if (StringUtils.isBlank(materialuUitStr)) {
					// 预定物料单位必填
					return MessageFactory.buildRefuseMessage("Failed", "预定物料", BaseConstant.TYPE_物料预定, "单位必填");
				}
				Integer materialuUit = Integer.parseInt(materialuUitStr);
				
				String  materialCountStr = CommonAlgorithm.getDataValue(recordComplexus, BaseConstant.TYPE_投料信息, rightCode, MaterialRatioCELNE3466Item.基本属性组_预定物料量);
				if (StringUtils.isBlank(materialCountStr)) {
					// 
					return MessageFactory.buildRefuseMessage("Failed", "预定物料", BaseConstant.TYPE_物料预定, "数量必填");
				}
				// 预订的量
				BigDecimal materialCount = new BigDecimal(materialCountStr);
				
				// 获取投料信息对应的物料基础信息
				List<RecordRelation> materialBaseRela = (List)CommonAlgorithm.getAppointRecordRelation(recordComplexus, BaseConstant.TYPE_投料信息, rightCode, RelationType.RR_投料信息_物料信息_物料基础信息);
				if(materialBaseRela == null || materialBaseRela.size() !=1) {
					return MessageFactory.buildRefuseMessage("Failed", "预定物料", BaseConstant.TYPE_物料预定, "对应的物料基础信息不唯一");
				}
				// 获取物料基础信息的唯一code
				String materialBaseCode = materialBaseRela.get(0).getRightCode();
				
				
				materialCount = MaterialUnitUtil.convertBaseUnit(recordComplexus, materialuUit, materialCount, materialBaseCode);
				
				// 获取物料库存信息
				List<RecordRelation> materialStockRela = (List)CommonAlgorithm.getAppointRecordRelation(recordComplexus, BaseConstant.TYPE_物料基础信息, materialBaseCode, RelationType.RR_物料基础信息_库存信息_物料库存信息);
				if(materialStockRela == null || materialStockRela.size() !=1) {
					return MessageFactory.buildRefuseMessage("Failed", "预定物料", BaseConstant.TYPE_物料预定, "库存信息不唯一");
				}
				// 获取物料库存的唯一code
				String materialStockCode = materialStockRela.get(0).getRightCode();
				
				// 获取库存已预订信息
				String reservationCountStr = CommonAlgorithm.getDataValue(recordComplexus, BaseConstant.TYPE_物料库存信息, materialStockCode, MaterialStockInfoCELNE3551Item.基本属性组_已预订量);
				BigDecimal reservationCount = new BigDecimal("0");
				if (!StringUtils.isBlank(reservationCountStr)) {
					reservationCount =  new BigDecimal(reservationCountStr);
				}
				
				if (ComputeSign.add.equals(computeSign)) {
					reservationCount = reservationCount.add(materialCount);
				} else {
					if (reservationCount.compareTo(materialCount) == -1) {
						return MessageFactory.buildRefuseMessage("Failed", "预定物料", BaseConstant.TYPE_物料预定, "不能撤销，库存中预定量较少");
					}
					reservationCount = reservationCount.subtract(materialCount);
				}
				
				
				// 构造库存信息
				FGRootRecordBuilder msbuilder =FGRootRecordBuilder.getInstance(BaseConstant.TYPE_物料库存信息, materialStockCode);
				//设置记录属性。第一个参数为模型属性的编码，第二个参数为模型属性的取值
				msbuilder.putAttribute(MaterialStockInfoCELNE3551Item.基本属性组_已预订量, reservationCount);
				msbuilder.putAttribute(MaterialStockInfoCELNE3551Item.基本属性组_是否更新阈值, EnumKeyValue.ENUM_是否_是);
				//放入到kie预设的全局变量中
				relatedRecordList.add(msbuilder.getRootRecord());
				
				// 构造【项目预定库存信息】
				
//				materialBaseCode 物料基础code   projectCode  项目code
				
				   // 直接查项目物料中间表  【项目预定库存信息】
				QueryRecordParmFactory queryRecordParmFactory=new QueryRecordParmFactory(BaseConstant.TYPE_项目预定库存);
				
				FGConJunctionFactory conJunctionFactory = queryRecordParmFactory.getConJunctionFactory();
				List<String> projectCodes  = new ArrayList<String>();
				projectCodes.add(projectCode);
				conJunctionFactory.getRightRelJuncFactory(BaseConstant.TYPE_实验项目).getRelationCriterionFactory().setInRightCodes(projectCodes);
				List<String> materialBaseCodes  = new ArrayList<String>();
				materialBaseCodes.add(materialBaseCode);
				conJunctionFactory.getRightRelJuncFactory(BaseConstant.TYPE_物料基础信息).getRelationCriterionFactory().setInRightCodes(materialBaseCodes);
				
				List<String> queryCodeList = RecordQueryPanel.queryCodeList(queryRecordParmFactory.getQueryParameter());
				
				
				if (queryCodeList == null || queryCodeList.isEmpty()) {
					
					BigDecimal pmCount = new BigDecimal("0");
					if (ComputeSign.add.equals(computeSign)) {
						pmCount = materialCount;
					} 				
					
					Integer materialBaseUnit = MaterialUnitUtil.getMaterialBaseUnit(recordComplexus, materialBaseCode);
					
					// 新建一个 【项目预定库存信息】, 并和项目、物料基础信息， 进行关联关系
					String pMCode = UidManager.getLongUID() + "";
					
					FGRootRecordBuilder pmbuilder =FGRootRecordBuilder.getInstance(BaseConstant.TYPE_项目预定库存, pMCode);
					//设置记录属性。第一个参数为模型属性的编码，第二个参数为模型属性的取值
					pmbuilder.putAttribute(ReserveInventoryCELNE3881Item.基本属性组_预定物料量, pmCount);
					pmbuilder.putAttribute(ReserveInventoryCELNE3881Item.基本属性组_物料计量单位, materialBaseUnit);
					//放入到kie预设的全局变量中
					relatedRecordList.add(pmbuilder.getRootRecord());
					
					RecordRelationOpsBuilder relationOpsBuilder = RecordRelationOpsBuilder.getInstance(BaseConstant.TYPE_项目预定库存, pMCode);
					relationOpsBuilder.putRelation(RelationType.RR_项目预定库存_实验项目_实验项目, projectCode);
					relationOpsBuilder.putRelation(RelationType.RR_项目预定库存_物料基础信息_物料基础信息, materialBaseCode);
					relationOpsBuilder.putRelation(RelationType.RR_项目预定库存_物料库存_物料库存信息, materialStockCode);
					relatedRelationOpsBuilderList.add(relationOpsBuilder);
				} else {
					// 更新一个  【项目预定库存信息】
					String projectMCode = queryCodeList.get(0);
					
					String projectMReservationCountStr = CommonAlgorithm.getDataValue(recordComplexus, BaseConstant.TYPE_项目预定库存, projectMCode, ReserveInventoryCELNE3881Item.基本属性组_预定物料量);
//					String projectMReservationCountUnit = CommonAlgorithm.getDataValue(recordComplexus, BaseConstant.TYPE_项目预定库存, projectMCode, ReserveInventoryCELNE3881Item.基本属性组_预定物料量);
					BigDecimal projectMReservationCount = new BigDecimal("0");
					if (!StringUtils.isBlank(projectMReservationCountStr)) {
						 projectMReservationCount = new BigDecimal(projectMReservationCountStr);
					}
					
					if (ComputeSign.add.equals(computeSign)) {
						projectMReservationCount = projectMReservationCount.add(materialCount);
					} else {
						if (projectMReservationCount.compareTo(materialCount) == -1) {
							return MessageFactory.buildRefuseMessage("Failed", "预定物料", BaseConstant.TYPE_物料预定, "不能撤销，库存中预定量较少");
						}
						projectMReservationCount = projectMReservationCount.subtract(materialCount);
					}
					
					
					// 构造库存信息
					FGRootRecordBuilder pMbuilder =FGRootRecordBuilder.getInstance(BaseConstant.TYPE_项目预定库存, projectMCode);
					//设置记录属性。第一个参数为模型属性的编码，第二个参数为模型属性的取值
					pMbuilder.putAttribute(ReserveInventoryCELNE3881Item.基本属性组_预定物料量, projectMReservationCount);
					//放入到kie预设的全局变量中
					relatedRecordList.add(pMbuilder.getRootRecord());
				}
				
			// for 循环结束	
			}
		} catch (Exception e) {
			e.printStackTrace();
			return MessageFactory.buildRefuseMessage("Failed", "预订物料", BaseConstant.TYPE_物料库存信息, "失败");
		}
		return MessageFactory.buildInfoMessage("Succeeded", "预订物料", BaseConstant.TYPE_物料库存信息, "成功");
	}
	
	
	/**
	 * 暂时废弃
	 *  	项目对应的物料预定只能对应一个物料基础信息
	 * @param recordComplexus
	 * @param recordCode
	 * @param recordOpsBuilder
	 * @param relatedRecordList
	 * @param computeSign
	 * @return
	 */
	public static Message checkMaterialBase(FGRecordComplexus recordComplexus, String recordCode, FGRecordOpsBuilder recordOpsBuilder, List<FGRootRecord> relatedRecordList) {
		try {
			
			List<String> materialList = new ArrayList<String>();
			
			// 获取物料预定实体
			FGRootRecord rootRecord = CommonAlgorithm.getRootRecord(recordComplexus, BaseConstant.TYPE_物料预定, recordCode);
			
			// 预定项目必填
			List<RecordRelation> projectRleaList = (List)CommonAlgorithm.getAppointRecordRelation(recordComplexus, BaseConstant.TYPE_物料预定, recordCode, RelationType.RR_物料预定_所属项目_实验项目);
			if (projectRleaList == null || projectRleaList.size() != 1) {
				return MessageFactory.buildRefuseMessage("Failed", "预定物料", BaseConstant.TYPE_物料预定, "所属项目必填");
			}
			// 获取项目code
			String projectCode = projectRleaList.get(0).getRightCode();
			//  获取项目的所有对应的 物料预定
			List<RecordRelation> reservaRelaList = (List)CommonAlgorithm.getAppointRecordRelation(recordComplexus, BaseConstant.TYPE_实验项目, projectCode, RelationType.RR_实验项目_物料预定_物料预定);
			for (RecordRelation recordRelation : reservaRelaList) {
				// 获取物料预定的code
				String reservaCode = recordRelation.getRightCode();
				// 获取所有投料信息
				List<RecordRelation> putMaterialRelaList = (List)CommonAlgorithm.getAppointRecordRelation(recordComplexus, BaseConstant.TYPE_物料预定,reservaCode, RelationType.RR_物料预定_预定物料_投料信息);
				
				for (RecordRelation putMaterialRela : putMaterialRelaList) {
					// 获取投料code
					String putMCode = putMaterialRela.getRightCode();
					// 获取投料对应的物料基础信息
					List<RecordRelation> materialRelaList = (List)CommonAlgorithm.getAppointRecordRelation(recordComplexus, BaseConstant.TYPE_投料信息,putMCode, RelationType.RR_投料信息_物料信息_物料基础信息);
					
					if (materialRelaList == null || materialRelaList.isEmpty()) {
						return MessageFactory.buildRefuseMessage("Failed", "预订物料", BaseConstant.TYPE_物料库存信息, "请检查项目对应的预定物料， 投料信息中缺少物料基础信息");
					}
					// 物料基础信息code
					String materialCode = materialRelaList.get(0).getRightCode();
					
					boolean contains = materialList.contains(materialCode);
					
					if (contains) {
						return MessageFactory.buildRefuseMessage("Failed", "预订物料", BaseConstant.TYPE_物料库存信息, "请检查项目对应的预定物料， 投料信息中存在相同的物料基础信息");
					} else {
						materialList.add(materialCode);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return MessageFactory.buildRefuseMessage("Failed", "预订物料", BaseConstant.TYPE_物料库存信息, "失败");
		}
		return MessageFactory.buildInfoMessage("Succeeded", "预订物料", BaseConstant.TYPE_物料库存信息, "成功");
	}
	
	
	
	
}
