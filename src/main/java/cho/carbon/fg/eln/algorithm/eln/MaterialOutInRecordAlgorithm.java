package cho.carbon.fg.eln.algorithm.eln;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import cho.carbon.complexus.FGRecordComplexus;
import cho.carbon.fg.eln.algorithm.ComputeSign;
import cho.carbon.fg.eln.constant.BaseConstant;
import cho.carbon.fg.eln.constant.EnumKeyValue;
import cho.carbon.fg.eln.constant.RelationType;
import cho.carbon.fg.eln.constant.item.MaterialBatchInfoCELNE3571Item;
import cho.carbon.fg.eln.constant.item.MaterialOutInRecordCELNE3558Item;
import cho.carbon.fg.eln.constant.item.MaterialStockInfoCELNE3551Item;
import cho.carbon.message.Message;
import cho.carbon.message.MessageFactory;
import cho.carbon.ops.builder.RecordRelationOpsBuilder;
import cho.carbon.relation.RecordRelation;
import cho.carbon.rrc.builder.FGRootRecordBuilder;
import cho.carbon.rrc.record.FGRootRecord;
/**
 * 物料出入库记录算法
 * @author lhb
 */
public class MaterialOutInRecordAlgorithm {
	
	/**
	 * 出入库记录添加物料基础关系
	 * @param recordComplexus
	 * @param recordCode
	 * @param relatedRecordList
	 * @return
	 */
	public static Message addMaterialRela(FGRecordComplexus recordComplexus, String recordCode, RecordRelationOpsBuilder relationOpsBuilder) {
		try {
			// 获取当前物料库存信息
			FGRootRecord rootRecord = CommonAlgorithm.getRootRecord(recordComplexus, BaseConstant.TYPE_物料出入库记录, recordCode);
			// 获取入库对应的批次信息
			List<RecordRelation> inOutStorageBatchRela = (List)CommonAlgorithm.getAppointRecordRelation(recordComplexus, BaseConstant.TYPE_物料出入库记录, recordCode, RelationType.RR_物料出入库记录_物料批次_物料批次信息);
			if (inOutStorageBatchRela != null && inOutStorageBatchRela.size() != 1) {
				return MessageFactory.buildRefuseMessage("Failed", "失败", BaseConstant.TYPE_物料出入库记录, "入库对应的批次不唯一");
			}
			// 获取批次编码
			String batchCode = inOutStorageBatchRela.get(0).getRightCode();
			
			// 获取 彼此对应的物料信息
			List<RecordRelation> materiaInfoRela = (List)CommonAlgorithm.getAppointRecordRelation(recordComplexus, BaseConstant.TYPE_物料批次信息, batchCode, RelationType.RR_物料批次信息_物料信息_物料基础信息);
			if (materiaInfoRela != null && materiaInfoRela.size() != 1) {
				return MessageFactory.buildRefuseMessage("Failed", "失败", BaseConstant.TYPE_物料出入库记录, "批次没有物料");
			}
			// 物料code
			String  materiaCode = materiaInfoRela.get(0).getRightCode();
			
			// 构件出入库记录和物料的关系
			relationOpsBuilder.putRelation(RelationType.RR_物料出入库记录_关联物料_物料基础信息, materiaCode);
		} catch (Exception e) {
			e.printStackTrace();
			return MessageFactory.buildRefuseMessage("Failed", "操作", BaseConstant.TYPE_物料出入库记录, "关联物料基础信息失败");
		}
		return MessageFactory.buildInfoMessage("Succeeded", "成功", BaseConstant.TYPE_物料出入库记录, "关联物料基础信息成功");
	}
	
	/**
	 * 采购入库命令
	 * @param recordComplexus
	 * @param recordCode
	 * @param relatedRecordList
	 * @return
	 */
	public static Message procurementInStorageCommand(FGRecordComplexus recordComplexus, String recordCode, List<FGRootRecord> relatedRecordList, List<RecordRelationOpsBuilder>  relatedRelationOpsBuilderList, RecordRelationOpsBuilder relationOpsBuilder) {
		try {
			// 获取当前物料库存信息
			FGRootRecord rootRecord = CommonAlgorithm.getRootRecord(recordComplexus, BaseConstant.TYPE_物料出入库记录, recordCode);
			
			// 获取入库数量
			String inventoryCountStr = CommonAlgorithm.getDataValue(rootRecord, MaterialOutInRecordCELNE3558Item.基本属性组_数量);
			if (StringUtils.isBlank(inventoryCountStr)) {
				return MessageFactory.buildRefuseMessage("Failed", "入库失败", BaseConstant.TYPE_物料出入库记录, "入库数量必填");
			}
			Double inventoryCount = Double.parseDouble(inventoryCountStr);
			// 获取入库单位
			String inventoryUnitStr = CommonAlgorithm.getDataValue(rootRecord, MaterialOutInRecordCELNE3558Item.基本属性组_单位);
			if (StringUtils.isBlank(inventoryUnitStr)) {
				return MessageFactory.buildRefuseMessage("Failed", "入库失败", BaseConstant.TYPE_物料出入库记录, "入库单位必填");
			}
			Integer inventoryUnit = Integer.parseInt(inventoryUnitStr);
			
			// 获取入库对应的批次信息
			List<RecordRelation> inOutStorageBatchRela = (List)CommonAlgorithm.getAppointRecordRelation(recordComplexus, BaseConstant.TYPE_物料出入库记录, recordCode, RelationType.RR_物料出入库记录_物料批次_物料批次信息);
			if (inOutStorageBatchRela != null && inOutStorageBatchRela.size() != 1) {
				return MessageFactory.buildRefuseMessage("Failed", "入库失败", BaseConstant.TYPE_物料出入库记录, "入库对应的批次不唯一");
			}
			// 获取批次编码
			String batchCode = inOutStorageBatchRela.get(0).getRightCode();
			// 批次总量
			String batchGrossStr = CommonAlgorithm.getDataValue(recordComplexus, BaseConstant.TYPE_物料批次信息, batchCode, MaterialBatchInfoCELNE3571Item.基本属性组_总量);
			Double batchGross = 0.0;
			if (!StringUtils.isBlank(batchGrossStr)) {
				batchGross = Double.parseDouble(batchGrossStr);
			}
			// 批次存量
			String batchStockStr = CommonAlgorithm.getDataValue(recordComplexus, BaseConstant.TYPE_物料批次信息, batchCode, MaterialBatchInfoCELNE3571Item.基本属性组_存量);
			Double batchStock = 0.0;
			if (!StringUtils.isBlank(batchStockStr)) {
				batchStock = Double.parseDouble(batchStockStr);
			}
			
			// 开始计算批次总量和存量
			batchGross = batchGross + inventoryCount;
			batchStock = batchStock + inventoryCount;
			
			FGRootRecordBuilder builder =FGRootRecordBuilder.getInstance(BaseConstant.TYPE_物料批次信息, batchCode);
			//设置记录属性。第一个参数为模型属性的编码，第二个参数为模型属性的取值
			builder.putAttribute(MaterialBatchInfoCELNE3571Item.基本属性组_总量, batchGross);
			builder.putAttribute(MaterialBatchInfoCELNE3571Item.基本属性组_存量, batchStock);
			//放入到kie预设的全局变量中
			relatedRecordList.add(builder.getRootRecord());
			
			// 把批次和物料对应的库存信息关联
			// 获取 彼此对应的物料信息
			List<RecordRelation> materiaInfoRela = (List)CommonAlgorithm.getAppointRecordRelation(recordComplexus, BaseConstant.TYPE_物料批次信息, batchCode, RelationType.RR_物料批次信息_物料信息_物料基础信息);
			if (materiaInfoRela != null && materiaInfoRela.size() != 1) {
				return MessageFactory.buildRefuseMessage("Failed", "入库失败", BaseConstant.TYPE_物料出入库记录, "批次没有物料");
			}
			// 物料code
			String  materiaCode = materiaInfoRela.get(0).getRightCode();
			// 获取物料库存信息
			List<RecordRelation> materialStockInfoRela = (List)CommonAlgorithm.getAppointRecordRelation(recordComplexus, BaseConstant.TYPE_物料基础信息, materiaCode, RelationType.RR_物料基础信息_库存信息_物料库存信息);
			if (materialStockInfoRela != null && materialStockInfoRela.size() != 1) {
				return MessageFactory.buildRefuseMessage("Failed", "入库失败", BaseConstant.TYPE_物料出入库记录, "物料没有对应的库存");
			}
			// 获取库存的唯一code
			String materialStockInfoCode = materialStockInfoRela.get(0).getRightCode();
			// 构件批次和库存的关系
			RecordRelationOpsBuilder relatedRelationOpsBuilder = RecordRelationOpsBuilder.getInstance(BaseConstant.TYPE_物料批次信息, batchCode);
			relatedRelationOpsBuilder.putRelation( RelationType.RR_物料批次信息_关联物料库存_物料库存信息, materialStockInfoCode);
			relatedRelationOpsBuilderList.add(relatedRelationOpsBuilder);
		
			// 更改库存量
			updateMaterialCount(recordComplexus, relatedRecordList, inventoryCount, materialStockInfoCode, ComputeSign.add);
		} catch (Exception e) {
			e.printStackTrace();
			return MessageFactory.buildRefuseMessage("Failed", "入库操作", BaseConstant.TYPE_物料出入库记录, "失败");
		}
		
		return MessageFactory.buildInfoMessage("Succeeded", "成功", BaseConstant.TYPE_物料出入库记录, "入库成功");
	}
	
	/**
	 * 采购撤销入库命令
	 * @param recordComplexus
	 * @param recordCode
	 * @param relatedRecordList
	 * @return
	 */
	public static Message procurementRevocationInStorageCommand(FGRecordComplexus recordComplexus, String recordCode, List<FGRootRecord> relatedRecordList, List<RecordRelationOpsBuilder>  relatedRelationOpsBuilderList) {
		try {
			// 获取当前物料库存信息
			FGRootRecord rootRecord = CommonAlgorithm.getRootRecord(recordComplexus, BaseConstant.TYPE_物料出入库记录, recordCode);
			
			// 获取撤销入库数量
			String inventoryCountStr = CommonAlgorithm.getDataValue(rootRecord, MaterialOutInRecordCELNE3558Item.基本属性组_数量);
			if (StringUtils.isBlank(inventoryCountStr)) {
				return MessageFactory.buildRefuseMessage("Failed", "入库失败", BaseConstant.TYPE_物料出入库记录, "入库数量必填");
			}
			Double inventoryCount = Double.parseDouble(inventoryCountStr);
			// 获取入库单位
			String inventoryUnitStr = CommonAlgorithm.getDataValue(rootRecord, MaterialOutInRecordCELNE3558Item.基本属性组_单位);
			if (StringUtils.isBlank(inventoryUnitStr)) {
				return MessageFactory.buildRefuseMessage("Failed", "入库失败", BaseConstant.TYPE_物料出入库记录, "入库单位必填");
			}
			Integer inventoryUnit = Integer.parseInt(inventoryUnitStr);
			
			// 获取入库对应的批次信息
			List<RecordRelation> inOutStorageBatchRela = (List)CommonAlgorithm.getAppointRecordRelation(recordComplexus, BaseConstant.TYPE_物料出入库记录, recordCode, RelationType.RR_物料出入库记录_物料批次_物料批次信息);
			if (inOutStorageBatchRela != null && inOutStorageBatchRela.size() != 1) {
				return MessageFactory.buildRefuseMessage("Failed", "入库失败", BaseConstant.TYPE_物料出入库记录, "入库对应的批次不唯一");
			}
			// 获取批次编码
			String batchCode = inOutStorageBatchRela.get(0).getRightCode();
			// 批次总量
			String batchGrossStr = CommonAlgorithm.getDataValue(recordComplexus, BaseConstant.TYPE_物料批次信息, batchCode, MaterialBatchInfoCELNE3571Item.基本属性组_总量);
			Double batchGross = 0.0;
			if (!StringUtils.isBlank(batchGrossStr)) {
				batchGross = Double.parseDouble(batchGrossStr);
			}
			// 批次存量
			String batchStockStr = CommonAlgorithm.getDataValue(recordComplexus, BaseConstant.TYPE_物料批次信息, batchCode, MaterialBatchInfoCELNE3571Item.基本属性组_存量);
			Double batchStock = 0.0;
			if (!StringUtils.isBlank(batchStockStr)) {
				batchStock = Double.parseDouble(batchStockStr);
			}
			
			if ((batchGross < inventoryCount) || (batchStock < inventoryCount)) {
				return MessageFactory.buildRefuseMessage("Failed", "撤销失败", BaseConstant.TYPE_物料出入库记录, "撤销批次总量or存量不足");
			}
			
			// 开始计算批次总量和存量
			batchGross = batchGross - inventoryCount;
			batchStock = batchStock - inventoryCount;
			
			FGRootRecordBuilder builder =FGRootRecordBuilder.getInstance(BaseConstant.TYPE_物料批次信息, batchCode);
			//设置记录属性。第一个参数为模型属性的编码，第二个参数为模型属性的取值
			builder.putAttribute(MaterialBatchInfoCELNE3571Item.基本属性组_总量, batchGross);
			builder.putAttribute(MaterialBatchInfoCELNE3571Item.基本属性组_存量, batchStock);
			//放入到kie预设的全局变量中
			relatedRecordList.add(builder.getRootRecord());
			
			String materialStockInfoCode = getMaterialStockCode(recordComplexus, batchCode);
			// 更改库存量
			updateMaterialCount(recordComplexus, relatedRecordList, inventoryCount, materialStockInfoCode, ComputeSign.minus);
		} catch (Exception e) {
			e.printStackTrace();
			return MessageFactory.buildRefuseMessage("Failed", "入库操作", BaseConstant.TYPE_物料出入库记录, "失败");
		}
		return MessageFactory.buildInfoMessage("Succeeded", "成功", BaseConstant.TYPE_物料出入库记录, "入库成功");
	}

	/**
	 * 根据批次code， 获取物料库存code
	 * @param recordComplexus
	 * @param batchCode   批次唯一code
	 * @return
	 */
	private static String getMaterialStockCode(FGRecordComplexus recordComplexus, String batchCode) throws Exception {
		// 获取物料库存信息
		List<RecordRelation> materialStockInfoRela = (List)CommonAlgorithm.getAppointRecordRelation(recordComplexus, BaseConstant.TYPE_物料批次信息, batchCode, RelationType.RR_物料批次信息_关联物料库存_物料库存信息);
		if (materialStockInfoRela != null && materialStockInfoRela.size() != 1) {
			throw new RuntimeException("批次对应的物料库存不唯一");
		}
		// 获取库存的唯一code
		String materialStockInfoCode = materialStockInfoRela.get(0).getRightCode();
		return materialStockInfoCode;
	}

	/**
	 * 更改库存量
	 * @param recordComplexus
	 * @param relatedRecordList
	 * @param inventoryCount   增加或减少的库存量
	 * @param materialStockInfoCode  库存唯一编码
	 * * @param computeSign  加还是减
	 */
	private static void updateMaterialCount(FGRecordComplexus recordComplexus, List<FGRootRecord> relatedRecordList,
			Double inventoryCount, String materialStockInfoCode, ComputeSign computeSign) {
		String kucunCountStr = CommonAlgorithm.getDataValue(recordComplexus,  BaseConstant.TYPE_物料库存信息, materialStockInfoCode, MaterialStockInfoCELNE3551Item.基本属性组_库存量);
		Double kucunCount = 0.0;
		if (!StringUtils.isBlank(kucunCountStr)) {
			kucunCount = Double.parseDouble(kucunCountStr);
		}
		
		if (ComputeSign.minus.equals(computeSign)) {
			if (kucunCount < inventoryCount) {
				throw new RuntimeException("物料库存不足");
			}
			kucunCount = kucunCount - inventoryCount;
		} else {
			kucunCount = kucunCount + inventoryCount;
		}
		
		FGRootRecordBuilder kuCunBuilder =FGRootRecordBuilder.getInstance(BaseConstant.TYPE_物料库存信息, materialStockInfoCode);
		//设置记录属性。第一个参数为模型属性的编码，第二个参数为模型属性的取值
		kuCunBuilder.putAttribute(MaterialStockInfoCELNE3551Item.基本属性组_库存量, kucunCount);
		kuCunBuilder.putAttribute(MaterialStockInfoCELNE3551Item.基本属性组_是否更新阈值, EnumKeyValue.ENUM_是否_是);
		//放入到kie预设的全局变量中
		relatedRecordList.add(kuCunBuilder.getRootRecord());
	}
	
	/**
	 * 销售出库命令
	 * @param recordComplexus
	 * @param recordCode
	 * @param relatedRecordList
	 * @return
	 */
	public static Message sellOutStorageCommand(FGRecordComplexus recordComplexus, String recordCode, List<FGRootRecord> relatedRecordList, List<RecordRelationOpsBuilder>  relatedRelationOpsBuilderList) {
		try {
			// 获取当前物料库存信息
			FGRootRecord rootRecord = CommonAlgorithm.getRootRecord(recordComplexus, BaseConstant.TYPE_物料出入库记录, recordCode);
			
			// 获取撤销入库数量
			String inventoryCountStr = CommonAlgorithm.getDataValue(rootRecord, MaterialOutInRecordCELNE3558Item.基本属性组_数量);
			if (StringUtils.isBlank(inventoryCountStr)) {
				return MessageFactory.buildRefuseMessage("Failed", "出库失败", BaseConstant.TYPE_物料出入库记录, "出库数量必填");
			}
			Double inventoryCount = Double.parseDouble(inventoryCountStr);
			// 获取入库单位
			String inventoryUnitStr = CommonAlgorithm.getDataValue(rootRecord, MaterialOutInRecordCELNE3558Item.基本属性组_单位);
			if (StringUtils.isBlank(inventoryUnitStr)) {
				return MessageFactory.buildRefuseMessage("Failed", "出库失败", BaseConstant.TYPE_物料出入库记录, "出库单位必填");
			}
			Integer inventoryUnit = Integer.parseInt(inventoryUnitStr);
			
			// 获取入库对应的批次信息
			List<RecordRelation> inOutStorageBatchRela = (List)CommonAlgorithm.getAppointRecordRelation(recordComplexus, BaseConstant.TYPE_物料出入库记录, recordCode, RelationType.RR_物料出入库记录_物料批次_物料批次信息);
			if (inOutStorageBatchRela != null && inOutStorageBatchRela.size() != 1) {
				return MessageFactory.buildRefuseMessage("Failed", "入库失败", BaseConstant.TYPE_物料出入库记录, "出库对应的批次不唯一");
			}
			// 获取批次编码
			String batchCode = inOutStorageBatchRela.get(0).getRightCode();
			// 批次总量
//			String batchGrossStr = CommonAlgorithm.getDataValue(recordComplexus, BaseConstant.TYPE_物料批次信息, batchCode, MaterialBatchInfoCELNE3571Item.基本属性组_总量);
//			Double batchGross = 0.0;
//			if (!StringUtils.isBlank(batchGrossStr)) {
//				batchGross = Double.parseDouble(batchGrossStr);
//			}
			// 批次存量
			String batchStockStr = CommonAlgorithm.getDataValue(recordComplexus, BaseConstant.TYPE_物料批次信息, batchCode, MaterialBatchInfoCELNE3571Item.基本属性组_存量);
			Double batchStock = 0.0;
			if (!StringUtils.isBlank(batchStockStr)) {
				batchStock = Double.parseDouble(batchStockStr);
			}
			
			if ((batchStock < inventoryCount)) {
				return MessageFactory.buildRefuseMessage("Failed", "出库失败", BaseConstant.TYPE_物料出入库记录, "批次总量or存量不足");
			}
			
			// 开始计算批次总量和存量
//			batchGross = batchGross - inventoryCount;
			batchStock = batchStock - inventoryCount;
			
			FGRootRecordBuilder builder = FGRootRecordBuilder.getInstance(BaseConstant.TYPE_物料批次信息, batchCode);
			//设置记录属性。第一个参数为模型属性的编码，第二个参数为模型属性的取值
//			builder.putAttribute(MaterialBatchInfoCELNE3571Item.基本属性组_总量, batchGross);
			builder.putAttribute(MaterialBatchInfoCELNE3571Item.基本属性组_存量, batchStock);
			//放入到kie预设的全局变量中
			relatedRecordList.add(builder.getRootRecord());
			// 获取库存唯一code
			String materialStockInfoCode = getMaterialStockCode(recordComplexus, batchCode);
			
			// 更改库存量
			updateMaterialCount(recordComplexus, relatedRecordList, inventoryCount, materialStockInfoCode, ComputeSign.minus);
		} catch (Exception e) {
			e.printStackTrace();
			return MessageFactory.buildRefuseMessage("Failed", "出库操作", BaseConstant.TYPE_物料出入库记录, "失败");
		}
		return MessageFactory.buildInfoMessage("Succeeded", "成功", BaseConstant.TYPE_物料出入库记录, "出库成功");
	}
	
	
	/**
	 * 销售撤销出库命令
	 * @param recordComplexus
	 * @param recordCode
	 * @param relatedRecordList
	 * @return
	 */
	public static Message sellRevocationOutStorageCommand(FGRecordComplexus recordComplexus, String recordCode, List<FGRootRecord> relatedRecordList, List<RecordRelationOpsBuilder>  relatedRelationOpsBuilderList) {
		try {
			// 获取当前物料库存信息
			FGRootRecord rootRecord = CommonAlgorithm.getRootRecord(recordComplexus, BaseConstant.TYPE_物料出入库记录, recordCode);
			
			// 获取撤销入库数量
			String inventoryCountStr = CommonAlgorithm.getDataValue(rootRecord, MaterialOutInRecordCELNE3558Item.基本属性组_数量);
			if (StringUtils.isBlank(inventoryCountStr)) {
				return MessageFactory.buildRefuseMessage("Failed", "出库失败", BaseConstant.TYPE_物料出入库记录, "出库数量必填");
			}
			Double inventoryCount = Double.parseDouble(inventoryCountStr);
			// 获取入库单位
			String inventoryUnitStr = CommonAlgorithm.getDataValue(rootRecord, MaterialOutInRecordCELNE3558Item.基本属性组_单位);
			if (StringUtils.isBlank(inventoryUnitStr)) {
				return MessageFactory.buildRefuseMessage("Failed", "出库失败", BaseConstant.TYPE_物料出入库记录, "出库单位必填");
			}
			Integer inventoryUnit = Integer.parseInt(inventoryUnitStr);
			
			// 获取入库对应的批次信息
			List<RecordRelation> inOutStorageBatchRela = (List)CommonAlgorithm.getAppointRecordRelation(recordComplexus, BaseConstant.TYPE_物料出入库记录, recordCode, RelationType.RR_物料出入库记录_物料批次_物料批次信息);
			if (inOutStorageBatchRela != null && inOutStorageBatchRela.size() != 1) {
				return MessageFactory.buildRefuseMessage("Failed", "入库失败", BaseConstant.TYPE_物料出入库记录, "出库对应的批次不唯一");
			}
			// 获取批次编码
			String batchCode = inOutStorageBatchRela.get(0).getRightCode();
			// 批次总量
//			String batchGrossStr = CommonAlgorithm.getDataValue(recordComplexus, BaseConstant.TYPE_物料批次信息, batchCode, MaterialBatchInfoCELNE3571Item.基本属性组_总量);
//			Double batchGross = 0.0;
//			if (!StringUtils.isBlank(batchGrossStr)) {
//				batchGross = Double.parseDouble(batchGrossStr);
//			}
			// 批次存量
			String batchStockStr = CommonAlgorithm.getDataValue(recordComplexus, BaseConstant.TYPE_物料批次信息, batchCode, MaterialBatchInfoCELNE3571Item.基本属性组_存量);
			Double batchStock = 0.0;
			if (!StringUtils.isBlank(batchStockStr)) {
				batchStock = Double.parseDouble(batchStockStr);
			}
			
//			if ((batchGross < inventoryCount) || (batchStock < inventoryCount)) {
//				return MessageFactory.buildRefuseMessage("Failed", "撤销出库失败", BaseConstant.TYPE_物料出入库记录, "批次总量or存量不足");
//			}
			
			// 开始计算批次总量和存量
//			batchGross = batchGross + inventoryCount;
			batchStock = batchStock + inventoryCount;
			
			FGRootRecordBuilder builder = FGRootRecordBuilder.getInstance(BaseConstant.TYPE_物料批次信息, batchCode);
			//设置记录属性。第一个参数为模型属性的编码，第二个参数为模型属性的取值
//			builder.putAttribute(MaterialBatchInfoCELNE3571Item.基本属性组_总量, batchGross);
			builder.putAttribute(MaterialBatchInfoCELNE3571Item.基本属性组_存量, batchStock);
			//放入到kie预设的全局变量中
			relatedRecordList.add(builder.getRootRecord());
			
			// 获取库存唯一code
			String materialStockInfoCode = getMaterialStockCode(recordComplexus, batchCode);
			// 更改库存量
			updateMaterialCount(recordComplexus, relatedRecordList, inventoryCount, materialStockInfoCode, ComputeSign.add);
		} catch (Exception e) {
			e.printStackTrace();
			return MessageFactory.buildRefuseMessage("Failed", "出库操作", BaseConstant.TYPE_物料出入库记录, "失败");
		}
		return MessageFactory.buildInfoMessage("Succeeded", "成功", BaseConstant.TYPE_物料出入库记录, "出库成功");
	}
	
	/**
	 * 项目领用出库
	 * @param recordComplexus
	 * @param recordCode
	 * @param relatedRecordList
	 * @return
	 */
	public static Message projecdtOutStorageCommand(FGRecordComplexus recordComplexus, String recordCode, List<FGRootRecord> relatedRecordList, List<RecordRelationOpsBuilder>  relatedRelationOpsBuilderList) {
		try {
			// 获取当前物料库存信息
			FGRootRecord rootRecord = CommonAlgorithm.getRootRecord(recordComplexus, BaseConstant.TYPE_物料出入库记录, recordCode);
			
			// 获取撤销入库数量
			String inventoryCountStr = CommonAlgorithm.getDataValue(rootRecord, MaterialOutInRecordCELNE3558Item.基本属性组_数量);
			if (StringUtils.isBlank(inventoryCountStr)) {
				return MessageFactory.buildRefuseMessage("Failed", "出库失败", BaseConstant.TYPE_物料出入库记录, "出库数量必填");
			}
			Double inventoryCount = Double.parseDouble(inventoryCountStr);
			// 获取入库单位
			String inventoryUnitStr = CommonAlgorithm.getDataValue(rootRecord, MaterialOutInRecordCELNE3558Item.基本属性组_单位);
			if (StringUtils.isBlank(inventoryUnitStr)) {
				return MessageFactory.buildRefuseMessage("Failed", "出库失败", BaseConstant.TYPE_物料出入库记录, "出库单位必填");
			}
			Integer inventoryUnit = Integer.parseInt(inventoryUnitStr);
			
			// 获取入库对应的批次信息
			List<RecordRelation> inOutStorageBatchRela = (List)CommonAlgorithm.getAppointRecordRelation(recordComplexus, BaseConstant.TYPE_物料出入库记录, recordCode, RelationType.RR_物料出入库记录_物料批次_物料批次信息);
			if (inOutStorageBatchRela != null && inOutStorageBatchRela.size() != 1) {
				return MessageFactory.buildRefuseMessage("Failed", "入库失败", BaseConstant.TYPE_物料出入库记录, "出库对应的批次不唯一");
			}
			// 获取批次编码
			String batchCode = inOutStorageBatchRela.get(0).getRightCode();
			// 批次总量
//			String batchGrossStr = CommonAlgorithm.getDataValue(recordComplexus, BaseConstant.TYPE_物料批次信息, batchCode, MaterialBatchInfoCELNE3571Item.基本属性组_总量);
//			Double batchGross = 0.0;
//			if (!StringUtils.isBlank(batchGrossStr)) {
//				batchGross = Double.parseDouble(batchGrossStr);
//			}
			// 批次存量
			String batchStockStr = CommonAlgorithm.getDataValue(recordComplexus, BaseConstant.TYPE_物料批次信息, batchCode, MaterialBatchInfoCELNE3571Item.基本属性组_存量);
			Double batchStock = 0.0;
			if (!StringUtils.isBlank(batchStockStr)) {
				batchStock = Double.parseDouble(batchStockStr);
			}
			
			if (batchStock < inventoryCount) {
				return MessageFactory.buildRefuseMessage("Failed", "出库失败", BaseConstant.TYPE_物料出入库记录, "批次总量or存量不足");
			}
			
			// 开始计算批次总量和存量
//			batchGross = batchGross - inventoryCount;
			batchStock = batchStock - inventoryCount;
			
			FGRootRecordBuilder builder = FGRootRecordBuilder.getInstance(BaseConstant.TYPE_物料批次信息, batchCode);
			//设置记录属性。第一个参数为模型属性的编码，第二个参数为模型属性的取值
//			builder.putAttribute(MaterialBatchInfoCELNE3571Item.基本属性组_总量, batchGross);
			builder.putAttribute(MaterialBatchInfoCELNE3571Item.基本属性组_存量, batchStock);
			//放入到kie预设的全局变量中
			relatedRecordList.add(builder.getRootRecord());
			// 获取库存唯一code
			String materialStockInfoCode = getMaterialStockCode(recordComplexus, batchCode);
			// 更改库存量
			updateMaterialCount(recordComplexus, relatedRecordList, inventoryCount, materialStockInfoCode, ComputeSign.minus);
		} catch (Exception e) {
			e.printStackTrace();
			return MessageFactory.buildRefuseMessage("Failed", "出库操作", BaseConstant.TYPE_物料出入库记录, "失败");
		}
		return MessageFactory.buildInfoMessage("Succeeded", "成功", BaseConstant.TYPE_物料出入库记录, "出库成功");
	}

	
	/**
	 * 项目撤销领用出库命令
	 * @param recordComplexus
	 * @param recordCode
	 * @param relatedRecordList
	 * @return
	 */
	public static Message projecdtRevocationOutStorageCommand(FGRecordComplexus recordComplexus, String recordCode, List<FGRootRecord> relatedRecordList, List<RecordRelationOpsBuilder>  relatedRelationOpsBuilderList) {
		try {
			// 获取当前物料库存信息
			FGRootRecord rootRecord = CommonAlgorithm.getRootRecord(recordComplexus, BaseConstant.TYPE_物料出入库记录, recordCode);
			
			// 获取撤销入库数量
			String inventoryCountStr = CommonAlgorithm.getDataValue(rootRecord, MaterialOutInRecordCELNE3558Item.基本属性组_数量);
			if (StringUtils.isBlank(inventoryCountStr)) {
				return MessageFactory.buildRefuseMessage("Failed", "出库失败", BaseConstant.TYPE_物料出入库记录, "出库数量必填");
			}
			Double inventoryCount = Double.parseDouble(inventoryCountStr);
			// 获取入库单位
			String inventoryUnitStr = CommonAlgorithm.getDataValue(rootRecord, MaterialOutInRecordCELNE3558Item.基本属性组_单位);
			if (StringUtils.isBlank(inventoryUnitStr)) {
				return MessageFactory.buildRefuseMessage("Failed", "出库失败", BaseConstant.TYPE_物料出入库记录, "出库单位必填");
			}
			Integer inventoryUnit = Integer.parseInt(inventoryUnitStr);
			
			// 获取入库对应的批次信息
			List<RecordRelation> inOutStorageBatchRela = (List)CommonAlgorithm.getAppointRecordRelation(recordComplexus, BaseConstant.TYPE_物料出入库记录, recordCode, RelationType.RR_物料出入库记录_物料批次_物料批次信息);
			if (inOutStorageBatchRela != null && inOutStorageBatchRela.size() != 1) {
				return MessageFactory.buildRefuseMessage("Failed", "入库失败", BaseConstant.TYPE_物料出入库记录, "出库对应的批次不唯一");
			}
			// 获取批次编码
			String batchCode = inOutStorageBatchRela.get(0).getRightCode();
			// 批次总量
//			String batchGrossStr = CommonAlgorithm.getDataValue(recordComplexus, BaseConstant.TYPE_物料批次信息, batchCode, MaterialBatchInfoCELNE3571Item.基本属性组_总量);
//			Double batchGross = 0.0;
//			if (!StringUtils.isBlank(batchGrossStr)) {
//				batchGross = Double.parseDouble(batchGrossStr);
//			}
			// 批次存量
			String batchStockStr = CommonAlgorithm.getDataValue(recordComplexus, BaseConstant.TYPE_物料批次信息, batchCode, MaterialBatchInfoCELNE3571Item.基本属性组_存量);
			Double batchStock = 0.0;
			if (!StringUtils.isBlank(batchStockStr)) {
				batchStock = Double.parseDouble(batchStockStr);
			}
			
//			if ((batchGross < inventoryCount) || (batchStock < inventoryCount)) {
//				return MessageFactory.buildRefuseMessage("Failed", "撤销出库失败", BaseConstant.TYPE_物料出入库记录, "批次总量or存量不足");
//			}
			
			// 开始计算批次总量和存量
//			batchGross = batchGross + inventoryCount;
			batchStock = batchStock + inventoryCount;
			
			FGRootRecordBuilder builder = FGRootRecordBuilder.getInstance(BaseConstant.TYPE_物料批次信息, batchCode);
			//设置记录属性。第一个参数为模型属性的编码，第二个参数为模型属性的取值
//			builder.putAttribute(MaterialBatchInfoCELNE3571Item.基本属性组_总量, batchGross);
			builder.putAttribute(MaterialBatchInfoCELNE3571Item.基本属性组_存量, batchStock);
			//放入到kie预设的全局变量中
			relatedRecordList.add(builder.getRootRecord());
			
			// 获取库存唯一code
			String materialStockInfoCode = getMaterialStockCode(recordComplexus, batchCode);
			// 更改库存量
			updateMaterialCount(recordComplexus, relatedRecordList, inventoryCount, materialStockInfoCode, ComputeSign.add);
					
		} catch (Exception e) {
			e.printStackTrace();
			return MessageFactory.buildRefuseMessage("Failed", "出库操作", BaseConstant.TYPE_物料出入库记录, "失败");
		}
		return MessageFactory.buildInfoMessage("Succeeded", "成功", BaseConstant.TYPE_物料出入库记录, "出库成功");
	}
	
	/**
	 * 项目归还入库命令
	 * @param recordComplexus
	 * @param recordCode
	 * @param relatedRecordList
	 * @return
	 */
	public static Message projectGiveBackStorageCommand(FGRecordComplexus recordComplexus, String recordCode, List<FGRootRecord> relatedRecordList, List<RecordRelationOpsBuilder>  relatedRelationOpsBuilderList) {
		try {
			// 获取当前物料库存信息
			FGRootRecord rootRecord = CommonAlgorithm.getRootRecord(recordComplexus, BaseConstant.TYPE_物料出入库记录, recordCode);
			
			// 获取撤销入库数量
			String inventoryCountStr = CommonAlgorithm.getDataValue(rootRecord, MaterialOutInRecordCELNE3558Item.基本属性组_数量);
			if (StringUtils.isBlank(inventoryCountStr)) {
				return MessageFactory.buildRefuseMessage("Failed", "出库失败", BaseConstant.TYPE_物料出入库记录, "出库数量必填");
			}
			Double inventoryCount = Double.parseDouble(inventoryCountStr);
			// 获取入库单位
			String inventoryUnitStr = CommonAlgorithm.getDataValue(rootRecord, MaterialOutInRecordCELNE3558Item.基本属性组_单位);
			if (StringUtils.isBlank(inventoryUnitStr)) {
				return MessageFactory.buildRefuseMessage("Failed", "出库失败", BaseConstant.TYPE_物料出入库记录, "出库单位必填");
			}
			Integer inventoryUnit = Integer.parseInt(inventoryUnitStr);
			
			// 获取入库对应的批次信息
			List<RecordRelation> inOutStorageBatchRela = (List)CommonAlgorithm.getAppointRecordRelation(recordComplexus, BaseConstant.TYPE_物料出入库记录, recordCode, RelationType.RR_物料出入库记录_物料批次_物料批次信息);
			if (inOutStorageBatchRela != null && inOutStorageBatchRela.size() != 1) {
				return MessageFactory.buildRefuseMessage("Failed", "入库失败", BaseConstant.TYPE_物料出入库记录, "出库对应的批次不唯一");
			}
			// 获取批次编码
			String batchCode = inOutStorageBatchRela.get(0).getRightCode();
			// 批次总量
//			String batchGrossStr = CommonAlgorithm.getDataValue(recordComplexus, BaseConstant.TYPE_物料批次信息, batchCode, MaterialBatchInfoCELNE3571Item.基本属性组_总量);
//			Double batchGross = 0.0;
//			if (!StringUtils.isBlank(batchGrossStr)) {
//				batchGross = Double.parseDouble(batchGrossStr);
//			}
			// 批次存量
			String batchStockStr = CommonAlgorithm.getDataValue(recordComplexus, BaseConstant.TYPE_物料批次信息, batchCode, MaterialBatchInfoCELNE3571Item.基本属性组_存量);
			Double batchStock = 0.0;
			if (!StringUtils.isBlank(batchStockStr)) {
				batchStock = Double.parseDouble(batchStockStr);
			}
			
			// 开始计算批次总量和存量
//			batchGross = batchGross + inventoryCount;
			batchStock = batchStock + inventoryCount;
			
			FGRootRecordBuilder builder = FGRootRecordBuilder.getInstance(BaseConstant.TYPE_物料批次信息, batchCode);
			//设置记录属性。第一个参数为模型属性的编码，第二个参数为模型属性的取值
//			builder.putAttribute(MaterialBatchInfoCELNE3571Item.基本属性组_总量, batchGross);
			builder.putAttribute(MaterialBatchInfoCELNE3571Item.基本属性组_存量, batchStock);
			//放入到kie预设的全局变量中
			relatedRecordList.add(builder.getRootRecord());
			
			// 获取库存唯一code
			String materialStockInfoCode = getMaterialStockCode(recordComplexus, batchCode);
			// 更改库存量
			updateMaterialCount(recordComplexus, relatedRecordList, inventoryCount, materialStockInfoCode, ComputeSign.add);
					
		} catch (Exception e) {
			e.printStackTrace();
			return MessageFactory.buildRefuseMessage("Failed", "入库操作", BaseConstant.TYPE_物料出入库记录, "失败");
		}
		return MessageFactory.buildInfoMessage("Succeeded", "成功", BaseConstant.TYPE_物料出入库记录, "出库成功");
	}
	
	/**
	 * 项目撤销归还入库命令
	 * @param recordComplexus
	 * @param recordCode
	 * @param relatedRecordList
	 * @return
	 */
	public static Message projecdtRevocationGiveBackStorageCommand(FGRecordComplexus recordComplexus, String recordCode, List<FGRootRecord> relatedRecordList, List<RecordRelationOpsBuilder>  relatedRelationOpsBuilderList) {
		try {
			// 获取当前物料库存信息
			FGRootRecord rootRecord = CommonAlgorithm.getRootRecord(recordComplexus, BaseConstant.TYPE_物料出入库记录, recordCode);
			
			// 获取撤销入库数量
			String inventoryCountStr = CommonAlgorithm.getDataValue(rootRecord, MaterialOutInRecordCELNE3558Item.基本属性组_数量);
			if (StringUtils.isBlank(inventoryCountStr)) {
				return MessageFactory.buildRefuseMessage("Failed", "撤销失败", BaseConstant.TYPE_物料出入库记录, "出库数量必填");
			}
			Double inventoryCount = Double.parseDouble(inventoryCountStr);
			// 获取入库单位
			String inventoryUnitStr = CommonAlgorithm.getDataValue(rootRecord, MaterialOutInRecordCELNE3558Item.基本属性组_单位);
			if (StringUtils.isBlank(inventoryUnitStr)) {
				return MessageFactory.buildRefuseMessage("Failed", "撤销失败", BaseConstant.TYPE_物料出入库记录, "出库单位必填");
			}
			Integer inventoryUnit = Integer.parseInt(inventoryUnitStr);
			
			// 获取入库对应的批次信息
			List<RecordRelation> inOutStorageBatchRela = (List)CommonAlgorithm.getAppointRecordRelation(recordComplexus, BaseConstant.TYPE_物料出入库记录, recordCode, RelationType.RR_物料出入库记录_物料批次_物料批次信息);
			if (inOutStorageBatchRela != null && inOutStorageBatchRela.size() != 1) {
				return MessageFactory.buildRefuseMessage("Failed", "撤销失败", BaseConstant.TYPE_物料出入库记录, "出库对应的批次不唯一");
			}
			// 获取批次编码
			String batchCode = inOutStorageBatchRela.get(0).getRightCode();
			// 批次总量
//			String batchGrossStr = CommonAlgorithm.getDataValue(recordComplexus, BaseConstant.TYPE_物料批次信息, batchCode, MaterialBatchInfoCELNE3571Item.基本属性组_总量);
//			Double batchGross = 0.0;
//			if (!StringUtils.isBlank(batchGrossStr)) {
//				batchGross = Double.parseDouble(batchGrossStr);
//			}
			// 批次存量
			String batchStockStr = CommonAlgorithm.getDataValue(recordComplexus, BaseConstant.TYPE_物料批次信息, batchCode, MaterialBatchInfoCELNE3571Item.基本属性组_存量);
			Double batchStock = 0.0;
			if (!StringUtils.isBlank(batchStockStr)) {
				batchStock = Double.parseDouble(batchStockStr);
			}
			
			if (batchStock < inventoryCount) {
				return MessageFactory.buildRefuseMessage("Failed", "撤销归还入库失败", BaseConstant.TYPE_物料出入库记录, "批次总量or存量不足");
			}
			
			// 开始计算批次总量和存量
//			batchGross = batchGross - inventoryCount;
			batchStock = batchStock - inventoryCount;
			
			FGRootRecordBuilder builder = FGRootRecordBuilder.getInstance(BaseConstant.TYPE_物料批次信息, batchCode);
			//设置记录属性。第一个参数为模型属性的编码，第二个参数为模型属性的取值
//			builder.putAttribute(MaterialBatchInfoCELNE3571Item.基本属性组_总量, batchGross);
			builder.putAttribute(MaterialBatchInfoCELNE3571Item.基本属性组_存量, batchStock);
			//放入到kie预设的全局变量中
			relatedRecordList.add(builder.getRootRecord());
			
			// 获取库存唯一code
			String materialStockInfoCode = getMaterialStockCode(recordComplexus, batchCode);
			// 更改库存量
			updateMaterialCount(recordComplexus, relatedRecordList, inventoryCount, materialStockInfoCode, ComputeSign.minus);
		} catch (Exception e) {
			e.printStackTrace();
			return MessageFactory.buildRefuseMessage("Failed", "出库操作", BaseConstant.TYPE_物料出入库记录, "失败");
		}
		return MessageFactory.buildInfoMessage("Succeeded", "成功", BaseConstant.TYPE_物料出入库记录, "出库成功");
	}
}
