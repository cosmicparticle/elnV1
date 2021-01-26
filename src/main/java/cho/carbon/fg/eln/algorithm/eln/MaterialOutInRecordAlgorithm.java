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
import cho.carbon.fg.eln.constant.item.MaterialBatchInfoCELNE3571Item;
import cho.carbon.fg.eln.constant.item.MaterialOutInRecordCELNE3558Item;
import cho.carbon.fg.eln.constant.item.MaterialRatioCELNE3466Item;
import cho.carbon.fg.eln.constant.item.MaterialReservationCELNE3756Item;
import cho.carbon.fg.eln.constant.item.MaterialStockInfoCELNE3551Item;
import cho.carbon.fg.eln.constant.item.ReserveInventoryCELNE3881Item;
import cho.carbon.fuse.improve.attribute.FuseAttributeFactory;
import cho.carbon.fuse.improve.ops.builder.FGRecordOpsBuilder;
import cho.carbon.message.Message;
import cho.carbon.message.MessageFactory;
import cho.carbon.ops.builder.RecordRelationOpsBuilder;
import cho.carbon.query.model.FGConJunctionFactory;
import cho.carbon.query.model.QueryRecordParmFactory;
import cho.carbon.record.query.RecordQueryPanel;
import cho.carbon.relation.RecordRelation;
import cho.carbon.rrc.builder.FGRootRecordBuilder;
import cho.carbon.rrc.record.FGAttribute;
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
			// 获取入库对应的批次信息code
			String batchCode = null;
			try {
				batchCode = getBatchCode(recordComplexus, recordCode);
			} catch (Exception e1) {
				return MessageFactory.buildRefuseMessage("Failed", "入库失败", BaseConstant.TYPE_物料出入库记录, "出库对应的批次不唯一");
			}
			
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
			BigDecimal inventoryCount = new BigDecimal(inventoryCountStr);
			
			// 获取入库单位
			String inventoryUnitStr = CommonAlgorithm.getDataValue(rootRecord, MaterialOutInRecordCELNE3558Item.基本属性组_单位);
			if (StringUtils.isBlank(inventoryUnitStr)) {
				return MessageFactory.buildRefuseMessage("Failed", "入库失败", BaseConstant.TYPE_物料出入库记录, "入库单位必填");
			}
			Integer inventoryUnit = Integer.parseInt(inventoryUnitStr);
			
			// 获取入库对应的批次信息code
				String batchCode = null;
				try {
					batchCode = getBatchCode(recordComplexus, recordCode);
				} catch (Exception e1) {
					return MessageFactory.buildRefuseMessage("Failed", "入库失败", BaseConstant.TYPE_物料出入库记录, "出库对应的批次不唯一");
				}
			// 批次总量
			String batchGrossStr = CommonAlgorithm.getDataValue(recordComplexus, BaseConstant.TYPE_物料批次信息, batchCode, MaterialBatchInfoCELNE3571Item.基本属性组_总量);
			BigDecimal batchGross = new BigDecimal("0");
			if (!StringUtils.isBlank(batchGrossStr)) {
				batchGross = new BigDecimal(batchGrossStr);
			}
			// 批次存量
			String batchStockStr = CommonAlgorithm.getDataValue(recordComplexus, BaseConstant.TYPE_物料批次信息, batchCode, MaterialBatchInfoCELNE3571Item.基本属性组_存量);
			BigDecimal batchStock = new BigDecimal("0");
			if (!StringUtils.isBlank(batchStockStr)) {
				batchStock = new BigDecimal(batchStockStr);
			}
			
			String materiaCode = getMateriaCode(recordComplexus, batchCode);
			try {
				// 判断入库的单位是否可以转换为物料基础计量单位, 若可以， 则进行转换
				inventoryCount = MaterialUnitUtil.convertBaseUnit(recordComplexus, inventoryUnit,  inventoryCount, materiaCode); 
			} catch (Exception e) {
				return MessageFactory.buildRefuseMessage("Failed", "失败", BaseConstant.TYPE_物料出入库记录, "物料基础单位和所选单位不符合");
			}
			// 开始计算批次总量和存量
			batchGross = batchGross.add(inventoryCount);
			batchStock = batchStock.add(inventoryCount);
			
			FGRootRecordBuilder builder =FGRootRecordBuilder.getInstance(BaseConstant.TYPE_物料批次信息, batchCode);
			//设置记录属性。第一个参数为模型属性的编码，第二个参数为模型属性的取值
			builder.putAttribute(MaterialBatchInfoCELNE3571Item.基本属性组_总量, batchGross);
			builder.putAttribute(MaterialBatchInfoCELNE3571Item.基本属性组_存量, batchStock);
			//放入到kie预设的全局变量中
			relatedRecordList.add(builder.getRootRecord());
			
			// 把批次和物料对应的库存信息关联
			
			// 获取物料库存信息
			List<RecordRelation> materialStockInfoRela = (List)CommonAlgorithm.getAppointRecordRelation(recordComplexus, BaseConstant.TYPE_物料基础信息, materiaCode, RelationType.RR_物料基础信息_库存信息_物料库存信息);
			if (materialStockInfoRela != null && materialStockInfoRela.size() != 1) {
				return MessageFactory.buildRefuseMessage("Failed", "入库失败", BaseConstant.TYPE_物料出入库记录, "物料没有对应的库存");
			}
			
			// 获取库存的唯一code
			String materialStockInfoCode = materialStockInfoRela.get(0).getRightCode();
			// 构件批次和库存的关系
//			RecordRelationOpsBuilder relatedRelationOpsBuilder = RecordRelationOpsBuilder.getInstance(BaseConstant.TYPE_物料批次信息, batchCode);
//			relatedRelationOpsBuilder.putRelation(RelationType.RR_物料批次信息_关联物料库存_物料库存信息, materialStockInfoCode);
//			relatedRelationOpsBuilderList.add(relatedRelationOpsBuilder);
		
			// 更改库存量
			updateMaterialCount(recordComplexus, relatedRecordList, inventoryCount, materialStockInfoCode, ComputeSign.add);
		} catch (Exception e) {
			e.printStackTrace();
			return MessageFactory.buildRefuseMessage("Failed", "入库操作", BaseConstant.TYPE_物料出入库记录, "失败");
		}
		
		return MessageFactory.buildInfoMessage("Succeeded", "成功", BaseConstant.TYPE_物料出入库记录, "入库成功");
	}

	/**
	 * 根据批次code， 获取物料基础信息的code
	 * @param recordComplexus
	 * @param batchCode
	 * @return
	 */
	private static String getMateriaCode(FGRecordComplexus recordComplexus, String batchCode) {
		// 获取 彼此对应的物料信息
		List<RecordRelation> materiaInfoRela = (List)CommonAlgorithm.getAppointRecordRelation(recordComplexus, BaseConstant.TYPE_物料批次信息, batchCode, RelationType.RR_物料批次信息_物料信息_物料基础信息);
		if (materiaInfoRela != null && materiaInfoRela.size() != 1) {
			throw new RuntimeException("批次对应的物料不唯一");}
		// 物料code
		return materiaInfoRela.get(0).getRightCode();
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
			BigDecimal inventoryCount = new BigDecimal(inventoryCountStr);
			// 获取入库单位
			String inventoryUnitStr = CommonAlgorithm.getDataValue(rootRecord, MaterialOutInRecordCELNE3558Item.基本属性组_单位);
			if (StringUtils.isBlank(inventoryUnitStr)) {
				return MessageFactory.buildRefuseMessage("Failed", "入库失败", BaseConstant.TYPE_物料出入库记录, "入库单位必填");
			}
			Integer inventoryUnit = Integer.parseInt(inventoryUnitStr);
			
			// 获取入库对应的批次信息
			// 获取入库对应的批次信息code
			String batchCode = null;
			try {
				batchCode = getBatchCode(recordComplexus, recordCode);
			} catch (Exception e1) {
				return MessageFactory.buildRefuseMessage("Failed", "入库失败", BaseConstant.TYPE_物料出入库记录, "出库对应的批次不唯一");
			}
			// 批次总量
			String batchGrossStr = CommonAlgorithm.getDataValue(recordComplexus, BaseConstant.TYPE_物料批次信息, batchCode, MaterialBatchInfoCELNE3571Item.基本属性组_总量);
			BigDecimal batchGross = new BigDecimal("0");
			if (!StringUtils.isBlank(batchGrossStr)) {
				batchGross = new BigDecimal(batchGrossStr);
			}
			// 批次存量
			String batchStockStr = CommonAlgorithm.getDataValue(recordComplexus, BaseConstant.TYPE_物料批次信息, batchCode, MaterialBatchInfoCELNE3571Item.基本属性组_存量);
			BigDecimal batchStock = new BigDecimal("0");
			if (!StringUtils.isBlank(batchStockStr)) {
				batchStock = new BigDecimal(batchStockStr);
			}
			//根据批次code， 获取物料基础信息的code
			String materiaCode = getMateriaCode(recordComplexus, batchCode);
			try {
				// 判断入库的单位是否可以转换为物料基础计量单位, 若可以， 则进行转换
				inventoryCount = MaterialUnitUtil.convertBaseUnit(recordComplexus, inventoryUnit,  inventoryCount, materiaCode); 
			} catch (Exception e) {
				return MessageFactory.buildRefuseMessage("Failed", "失败", BaseConstant.TYPE_物料出入库记录, "物料基础单位和所选单位不符合");
			}
			
			if ((batchGross.compareTo(inventoryCount) ==-1 ) || (batchStock.compareTo(inventoryCount) ==-1 )) {
				return MessageFactory.buildRefuseMessage("Failed", "撤销失败", BaseConstant.TYPE_物料出入库记录, "撤销批次总量or存量不足");
			}
			
			// 开始计算批次总量和存量
			batchGross = batchGross.subtract(inventoryCount);
			batchStock = batchStock.subtract(inventoryCount);
			
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
			BigDecimal inventoryCount, String materialStockInfoCode, ComputeSign computeSign) {
		String kucunCountStr = CommonAlgorithm.getDataValue(recordComplexus,  BaseConstant.TYPE_物料库存信息, materialStockInfoCode, MaterialStockInfoCELNE3551Item.基本属性组_库存总量);
		BigDecimal kucunCount = new BigDecimal("0");
		if (!StringUtils.isBlank(kucunCountStr)) {
			kucunCount = new BigDecimal(kucunCountStr); 
		}
		
		if (ComputeSign.minus.equals(computeSign)) {
			if (kucunCount.compareTo(inventoryCount) == -1 ) {
				throw new RuntimeException("物料库存不足");
			}
			kucunCount = kucunCount.subtract(inventoryCount);
		} else {
			kucunCount = kucunCount.add(inventoryCount);
		}
		
		FGRootRecordBuilder kuCunBuilder =FGRootRecordBuilder.getInstance(BaseConstant.TYPE_物料库存信息, materialStockInfoCode);
		//设置记录属性。第一个参数为模型属性的编码，第二个参数为模型属性的取值
		kuCunBuilder.putAttribute(MaterialStockInfoCELNE3551Item.基本属性组_库存总量, kucunCount);
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
			BigDecimal inventoryCount = new BigDecimal(inventoryCountStr);
			// 获取入库单位
			String inventoryUnitStr = CommonAlgorithm.getDataValue(rootRecord, MaterialOutInRecordCELNE3558Item.基本属性组_单位);
			if (StringUtils.isBlank(inventoryUnitStr)) {
				return MessageFactory.buildRefuseMessage("Failed", "出库失败", BaseConstant.TYPE_物料出入库记录, "出库单位必填");
			}
			Integer inventoryUnit = Integer.parseInt(inventoryUnitStr);
			
			// 获取入库对应的批次信息code
			String batchCode = null;
			try {
				batchCode = getBatchCode(recordComplexus, recordCode);
			} catch (Exception e1) {
				return MessageFactory.buildRefuseMessage("Failed", "入库失败", BaseConstant.TYPE_物料出入库记录, "出库对应的批次不唯一");
			}
			// 批次总量
//			String batchGrossStr = CommonAlgorithm.getDataValue(recordComplexus, BaseConstant.TYPE_物料批次信息, batchCode, MaterialBatchInfoCELNE3571Item.基本属性组_总量);
//			Double batchGross = 0.0;
//			if (!StringUtils.isBlank(batchGrossStr)) {
//				batchGross = Double.parseDouble(batchGrossStr);
//			}
			// 批次存量
			String batchStockStr = CommonAlgorithm.getDataValue(recordComplexus, BaseConstant.TYPE_物料批次信息, batchCode, MaterialBatchInfoCELNE3571Item.基本属性组_存量);
			BigDecimal batchStock = new BigDecimal("0");
			if (!StringUtils.isBlank(batchStockStr)) {
				batchStock = new BigDecimal(batchStockStr);
			}
			//
			String materiaCode = getMateriaCode(recordComplexus, batchCode);
			try {
				// 判断入库的单位是否可以转换为物料基础计量单位, 若可以， 则进行转换
				inventoryCount = MaterialUnitUtil.convertBaseUnit(recordComplexus, inventoryUnit,  inventoryCount, materiaCode); 
			} catch (Exception e) {
				return MessageFactory.buildRefuseMessage("Failed", "失败", BaseConstant.TYPE_物料出入库记录, "物料基础单位和所选单位不符合");
			}
			
			if ((batchStock.compareTo(inventoryCount) == -1 )) {
				return MessageFactory.buildRefuseMessage("Failed", "出库失败", BaseConstant.TYPE_物料出入库记录, "批次总量or存量不足");
			}
			
			// 开始计算批次总量和存量
//			batchGross = batchGross - inventoryCount;
			batchStock = batchStock.subtract(inventoryCount);
			
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
			BigDecimal inventoryCount = new BigDecimal(inventoryCountStr);
			// 获取入库单位
			String inventoryUnitStr = CommonAlgorithm.getDataValue(rootRecord, MaterialOutInRecordCELNE3558Item.基本属性组_单位);
			if (StringUtils.isBlank(inventoryUnitStr)) {
				return MessageFactory.buildRefuseMessage("Failed", "出库失败", BaseConstant.TYPE_物料出入库记录, "出库单位必填");
			}
			Integer inventoryUnit = Integer.parseInt(inventoryUnitStr);
			
			// 获取入库对应的批次信息code
			String batchCode = null;
			try {
				batchCode = getBatchCode(recordComplexus, recordCode);
			} catch (Exception e1) {
				return MessageFactory.buildRefuseMessage("Failed", "入库失败", BaseConstant.TYPE_物料出入库记录, "出库对应的批次不唯一");
			}
			// 批次总量
//			String batchGrossStr = CommonAlgorithm.getDataValue(recordComplexus, BaseConstant.TYPE_物料批次信息, batchCode, MaterialBatchInfoCELNE3571Item.基本属性组_总量);
//			Double batchGross = 0.0;
//			if (!StringUtils.isBlank(batchGrossStr)) {
//				batchGross = Double.parseDouble(batchGrossStr);
//			}
			// 批次存量
			String batchStockStr = CommonAlgorithm.getDataValue(recordComplexus, BaseConstant.TYPE_物料批次信息, batchCode, MaterialBatchInfoCELNE3571Item.基本属性组_存量);
			BigDecimal batchStock = new BigDecimal("0");
			if (!StringUtils.isBlank(batchStockStr)) {
				batchStock = new BigDecimal(batchStockStr);
			}
			
//			if ((batchGross < inventoryCount) || (batchStock < inventoryCount)) {
//				return MessageFactory.buildRefuseMessage("Failed", "撤销出库失败", BaseConstant.TYPE_物料出入库记录, "批次总量or存量不足");
//			}
			
			String materiaCode = getMateriaCode(recordComplexus, batchCode);
			try {
				// 判断入库的单位是否可以转换为物料基础计量单位, 若可以， 则进行转换
				inventoryCount = MaterialUnitUtil.convertBaseUnit(recordComplexus, inventoryUnit,  inventoryCount, materiaCode); 
			} catch (Exception e) {
				return MessageFactory.buildRefuseMessage("Failed", "失败", BaseConstant.TYPE_物料出入库记录, "物料基础单位和所选单位不符合");
			}
			
			// 开始计算批次总量和存量
//			batchGross = batchGross + inventoryCount;
			batchStock = batchStock.add(inventoryCount);
			
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
	public static Message projecdtOutStorageCommand(FGRecordComplexus recordComplexus, String recordCode, List<FGRootRecord> relatedRecordList, List<RecordRelationOpsBuilder>  relatedRelationOpsBuilderList, FGRecordOpsBuilder recordOpsBuilder) {
		try {
			// 获取当前物料库存信息
			FGRootRecord rootRecord = CommonAlgorithm.getRootRecord(recordComplexus, BaseConstant.TYPE_物料出入库记录, recordCode);
	// 获取领用出库数量
	String inventoryCountStr = CommonAlgorithm.getDataValue(rootRecord, MaterialOutInRecordCELNE3558Item.基本属性组_数量);
			if (StringUtils.isBlank(inventoryCountStr)) {
				return MessageFactory.buildRefuseMessage("Failed", "出库失败", BaseConstant.TYPE_物料出入库记录, "出库数量必填");
			}
			BigDecimal inventoryCount = new BigDecimal(inventoryCountStr);
	// 获取出库单位
	String inventoryUnitStr = CommonAlgorithm.getDataValue(rootRecord, MaterialOutInRecordCELNE3558Item.基本属性组_单位);
			if (StringUtils.isBlank(inventoryUnitStr)) {
				return MessageFactory.buildRefuseMessage("Failed", "出库失败", BaseConstant.TYPE_物料出入库记录, "出库单位必填");
			}
			Integer inventoryUnit = Integer.parseInt(inventoryUnitStr);
			
			// 获取入库对应的批次信息code
			String batchCode = null;
			try {
				batchCode = getBatchCode(recordComplexus, recordCode);
			} catch (Exception e1) {
				return MessageFactory.buildRefuseMessage("Failed", "入库失败", BaseConstant.TYPE_物料出入库记录, "出库对应的批次不唯一");
			}
			// 批次存量
			String batchStockStr = CommonAlgorithm.getDataValue(recordComplexus, BaseConstant.TYPE_物料批次信息, batchCode, MaterialBatchInfoCELNE3571Item.基本属性组_存量);
			BigDecimal batchStock = new BigDecimal("0");
			if (!StringUtils.isBlank(batchStockStr)) {
				batchStock = new BigDecimal(batchStockStr);
			}
	// 物料基础的code
	String materiaBaseCode = getMateriaCode(recordComplexus, batchCode);
			try {
				// 判断入库的单位是否可以转换为物料基础计量单位, 若可以， 则进行转换
				inventoryCount = MaterialUnitUtil.convertBaseUnit(recordComplexus, inventoryUnit,  inventoryCount, materiaBaseCode); 
			} catch (Exception e) {
				return MessageFactory.buildRefuseMessage("Failed", "失败", BaseConstant.TYPE_物料出入库记录, "物料基础单位和所选单位不符合");
			}
			
			if (batchStock.compareTo(inventoryCount) == -1 ) {
				return MessageFactory.buildRefuseMessage("Failed", "出库失败", BaseConstant.TYPE_物料出入库记录, "批次总量or存量不足");
			}
			
			// 开始计算批次存量
			batchStock = batchStock.subtract(inventoryCount);
			
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
			
			 // 领用的项目如果有已经预定的量， 需要减去预定量
			
			// 领用出库的项目必填
			List<RecordRelation> projectRela = (List)CommonAlgorithm.getAppointRecordRelation(recordComplexus, BaseConstant.TYPE_物料出入库记录, recordCode, RelationType.RR_物料出入库记录_关联实验项目_实验项目);
			if (projectRela == null || projectRela.size() != 1) {
				return MessageFactory.buildRefuseMessage("Failed", "出库失败", BaseConstant.TYPE_物料出入库记录, "项目必填");
			}
	// 获取出入库对应的项目code
	String projectCode = projectRela.get(0).getRightCode();
				// 更新预定
				updateReserve(recordComplexus, relatedRecordList, recordCode, materiaBaseCode, projectCode, inventoryCount, materialStockInfoCode, ComputeSign.minus, recordOpsBuilder);
		} catch (Exception e) {
			e.printStackTrace();
			return MessageFactory.buildRefuseMessage("Failed", "出库操作", BaseConstant.TYPE_物料出入库记录, "失败");
		}
		return MessageFactory.buildInfoMessage("Succeeded", "成功", BaseConstant.TYPE_物料出入库记录, "出库成功");
	}

	/**
	 * 	 更新预定
	 * @param recordComplexus
	 * @param relatedRecordList
	 * @param materiaBaseCode   出入库的物料code
	 * @param projectCode   项目code
	 * @param inventoryCount   出入库量
	 * @param materialStockInfoCode   库存唯一code
	 * @throws Exception 
	 */
	private static void updateReserve(FGRecordComplexus recordComplexus, 
			List<FGRootRecord> relatedRecordList,
			String recordCode, 
			String materiaBaseCode, 
			String projectCode, 
			BigDecimal inventoryCount, 
			String materialStockInfoCode, ComputeSign computeSign, FGRecordOpsBuilder recordOpsBuilder) throws Exception {
		
		// 计算【加减预定量】
		BigDecimal addMinusCount = new BigDecimal("0");
		String addMinusCountStr = CommonAlgorithm.getDataValue(recordComplexus, BaseConstant.TYPE_物料出入库记录, recordCode, MaterialOutInRecordCELNE3558Item.基本属性组_加减预定量);
		if (!StringUtils.isBlank(addMinusCountStr)) {
			addMinusCount = new BigDecimal(addMinusCountStr);
		}
		
		// 查询此项目和物料对应的【项目预定库存
		   // 直接查项目物料中间表  【项目预定库存信息】
		QueryRecordParmFactory queryRecordParmFactory=new QueryRecordParmFactory(BaseConstant.TYPE_项目预定库存);
		
		FGConJunctionFactory conJunctionFactory = queryRecordParmFactory.getConJunctionFactory();
		List<String> projectCodes  = new ArrayList<String>();
		projectCodes.add(projectCode);
		conJunctionFactory.getRightRelJuncFactory(BaseConstant.TYPE_实验项目).getRelationCriterionFactory().setInRightCodes(projectCodes);
		List<String> materialBaseCodes  = new ArrayList<String>();
		materialBaseCodes.add(materiaBaseCode);
		conJunctionFactory.getRightRelJuncFactory(BaseConstant.TYPE_物料基础信息).getRelationCriterionFactory().setInRightCodes(materialBaseCodes);
		
		List<String> queryCodeList = RecordQueryPanel.queryCodeList(queryRecordParmFactory.getQueryParameter());
		
		if (queryCodeList != null && !queryCodeList.isEmpty()) {
			String pmCode = queryCodeList.get(0);
			
			String projectMReservationCountStr = CommonAlgorithm.getDataValue(recordComplexus, BaseConstant.TYPE_项目预定库存, pmCode, ReserveInventoryCELNE3881Item.基本属性组_预定物料量);
			BigDecimal projectMReservationCount = new BigDecimal("0");
			if (!StringUtils.isBlank(projectMReservationCountStr)) {
				 projectMReservationCount = new BigDecimal(projectMReservationCountStr);
			}
			
			if (ComputeSign.add.equals(computeSign)) {
				projectMReservationCount = projectMReservationCount.add(addMinusCount);
			} else {
				if (projectMReservationCount.compareTo(inventoryCount) == -1) {
					addMinusCount = projectMReservationCount;
					projectMReservationCount = new BigDecimal("0");
				} else {
					projectMReservationCount = projectMReservationCount.subtract(inventoryCount);
					addMinusCount = inventoryCount;
				}
			}
			
			// 构造项目预定库存信息
			FGRootRecordBuilder pMbuilder =FGRootRecordBuilder.getInstance(BaseConstant.TYPE_项目预定库存, pmCode);
			//设置记录属性。第一个参数为模型属性的编码，第二个参数为模型属性的取值
			pMbuilder.putAttribute(ReserveInventoryCELNE3881Item.基本属性组_预定物料量, projectMReservationCount);
			//放入到kie预设的全局变量中
			relatedRecordList.add(pMbuilder.getRootRecord());
			
			String materialStockCountStr = CommonAlgorithm.getDataValue(recordComplexus, BaseConstant.TYPE_物料库存信息, materialStockInfoCode, MaterialStockInfoCELNE3551Item.基本属性组_已预订量);
			BigDecimal materialStockCount = new BigDecimal("0");
			if (!StringUtils.isBlank(materialStockCountStr)) {
				 materialStockCount = new BigDecimal(materialStockCountStr);
			}
			
			if (ComputeSign.add.equals(computeSign)) {
				materialStockCount = materialStockCount.add(addMinusCount);
			} else {
				if (materialStockCount.compareTo(inventoryCount) == -1) {
					addMinusCount = materialStockCount;
					materialStockCount = new BigDecimal("0");
				} else {
					materialStockCount = materialStockCount.subtract(inventoryCount);
					addMinusCount = inventoryCount;
				}
			}
			
			
			// 构造项目预定库存信息
			FGRootRecordBuilder kucunbuilder =FGRootRecordBuilder.getInstance(BaseConstant.TYPE_物料库存信息, materialStockInfoCode);
			//设置记录属性。第一个参数为模型属性的编码，第二个参数为模型属性的取值
			kucunbuilder.putAttribute(MaterialStockInfoCELNE3551Item.基本属性组_已预订量, materialStockCount);
			//放入到kie预设的全局变量中
			relatedRecordList.add(kucunbuilder.getRootRecord());
			
			// 构造出入库预定量，增加减少的
			
			
			FGAttribute attr=FuseAttributeFactory.buildAttribute(MaterialOutInRecordCELNE3558Item.基本属性组_加减预定量, addMinusCount);
			recordOpsBuilder.addUpdateAttr(attr);
		}
		
	}

	/**
	 * 获取入库对应的批次信息
	 * @param recordComplexus
	 * @param recordCode   出入库的唯一编码
	 * @return
	 */
	private static String getBatchCode(FGRecordComplexus recordComplexus, String recordCode) throws Exception{
		List<RecordRelation> inOutStorageBatchRela = (List)CommonAlgorithm.getAppointRecordRelation(recordComplexus, BaseConstant.TYPE_物料出入库记录, recordCode, RelationType.RR_物料出入库记录_物料批次_物料批次信息);
		if (inOutStorageBatchRela != null && inOutStorageBatchRela.size() != 1) {
			throw new RuntimeException("出库对应的批次不唯一");
		}
		// 获取批次编码
		String batchCode = inOutStorageBatchRela.get(0).getRightCode();
		return batchCode;
	}
	
	/**
	 * 项目撤销领用出库命令
	 * @param recordComplexus
	 * @param recordCode
	 * @param relatedRecordList
	 * @return
	 */
	public static Message projecdtRevocationOutStorageCommand(FGRecordComplexus recordComplexus, String recordCode, List<FGRootRecord> relatedRecordList, List<RecordRelationOpsBuilder>  relatedRelationOpsBuilderList, FGRecordOpsBuilder recordOpsBuilder) {
		try {
			// 获取当前物料库存信息
			FGRootRecord rootRecord = CommonAlgorithm.getRootRecord(recordComplexus, BaseConstant.TYPE_物料出入库记录, recordCode);
			
			// 获取撤销入库数量
			String inventoryCountStr = CommonAlgorithm.getDataValue(rootRecord, MaterialOutInRecordCELNE3558Item.基本属性组_数量);
			if (StringUtils.isBlank(inventoryCountStr)) {
				return MessageFactory.buildRefuseMessage("Failed", "出库失败", BaseConstant.TYPE_物料出入库记录, "出库数量必填");
			}
			BigDecimal inventoryCount = new BigDecimal(inventoryCountStr);
			// 获取入库单位
			String inventoryUnitStr = CommonAlgorithm.getDataValue(rootRecord, MaterialOutInRecordCELNE3558Item.基本属性组_单位);
			if (StringUtils.isBlank(inventoryUnitStr)) {
				return MessageFactory.buildRefuseMessage("Failed", "出库失败", BaseConstant.TYPE_物料出入库记录, "出库单位必填");
			}
			Integer inventoryUnit = Integer.parseInt(inventoryUnitStr);
			
			// 获取入库对应的批次信息code
			String batchCode = null;
			try {
				batchCode = getBatchCode(recordComplexus, recordCode);
			} catch (Exception e1) {
				return MessageFactory.buildRefuseMessage("Failed", "入库失败", BaseConstant.TYPE_物料出入库记录, "出库对应的批次不唯一");
			}
			// 批次总量
//			String batchGrossStr = CommonAlgorithm.getDataValue(recordComplexus, BaseConstant.TYPE_物料批次信息, batchCode, MaterialBatchInfoCELNE3571Item.基本属性组_总量);
//			Double batchGross = 0.0;
//			if (!StringUtils.isBlank(batchGrossStr)) {
//				batchGross = Double.parseDouble(batchGrossStr);
//			}
			// 批次存量
			String batchStockStr = CommonAlgorithm.getDataValue(recordComplexus, BaseConstant.TYPE_物料批次信息, batchCode, MaterialBatchInfoCELNE3571Item.基本属性组_存量);
			BigDecimal batchStock = new BigDecimal("0");
			if (!StringUtils.isBlank(batchStockStr)) {
				batchStock = new BigDecimal(batchStockStr);
			}
			
//			if ((batchGross < inventoryCount) || (batchStock < inventoryCount)) {
//				return MessageFactory.buildRefuseMessage("Failed", "撤销出库失败", BaseConstant.TYPE_物料出入库记录, "批次总量or存量不足");
//			}
			
			String materiaBaseCode = getMateriaCode(recordComplexus, batchCode);
			try {
				// 判断入库的单位是否可以转换为物料基础计量单位, 若可以， 则进行转换
				inventoryCount = MaterialUnitUtil.convertBaseUnit(recordComplexus, inventoryUnit,  inventoryCount, materiaBaseCode); 
			} catch (Exception e) {
				return MessageFactory.buildRefuseMessage("Failed", "失败", BaseConstant.TYPE_物料出入库记录, "物料基础单位和所选单位不符合");
			}
			
			// 开始计算批次总量和存量
//			batchGross = batchGross + inventoryCount;
			batchStock = batchStock.add(inventoryCount);
			
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
			
			// 领用的项目如果有已经预定的量， 需要减去预定量
			
			// 领用出库的项目必填
			List<RecordRelation> projectRela = (List)CommonAlgorithm.getAppointRecordRelation(recordComplexus, BaseConstant.TYPE_物料出入库记录, recordCode, RelationType.RR_物料出入库记录_关联实验项目_实验项目);
			if (projectRela == null || projectRela.size() != 1) {
				return MessageFactory.buildRefuseMessage("Failed", "出库失败", BaseConstant.TYPE_物料出入库记录, "项目必填");
			}
			// 获取出入库对应的项目code
			String projectCode = projectRela.get(0).getRightCode();
			// 获取项目对应物料预定信息
				// 更新预定
//				updateReserve(recordComplexus, relatedRecordList, reserveRela, materiaCode, inventoryCount, materialStockInfoCode, ComputeSign.add);
			updateReserve(recordComplexus, relatedRecordList,recordCode, materiaBaseCode, projectCode, inventoryCount, materialStockInfoCode, ComputeSign.add, recordOpsBuilder);
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
			BigDecimal inventoryCount = new BigDecimal(inventoryCountStr);
			// 获取入库单位
			String inventoryUnitStr = CommonAlgorithm.getDataValue(rootRecord, MaterialOutInRecordCELNE3558Item.基本属性组_单位);
			if (StringUtils.isBlank(inventoryUnitStr)) {
				return MessageFactory.buildRefuseMessage("Failed", "出库失败", BaseConstant.TYPE_物料出入库记录, "出库单位必填");
			}
			Integer inventoryUnit = Integer.parseInt(inventoryUnitStr);
			
			// 获取入库对应的批次信息code
			String batchCode = null;
			try {
				batchCode = getBatchCode(recordComplexus, recordCode);
			} catch (Exception e1) {
				return MessageFactory.buildRefuseMessage("Failed", "入库失败", BaseConstant.TYPE_物料出入库记录, "出库对应的批次不唯一");
			}
			// 批次总量
//			String batchGrossStr = CommonAlgorithm.getDataValue(recordComplexus, BaseConstant.TYPE_物料批次信息, batchCode, MaterialBatchInfoCELNE3571Item.基本属性组_总量);
//			Double batchGross = 0.0;
//			if (!StringUtils.isBlank(batchGrossStr)) {
//				batchGross = Double.parseDouble(batchGrossStr);
//			}
			// 批次存量
			String batchStockStr = CommonAlgorithm.getDataValue(recordComplexus, BaseConstant.TYPE_物料批次信息, batchCode, MaterialBatchInfoCELNE3571Item.基本属性组_存量);
			BigDecimal batchStock = new BigDecimal("0");
			if (!StringUtils.isBlank(batchStockStr)) {
				batchStock = new BigDecimal(batchStockStr);
			}
			
			String materiaCode = getMateriaCode(recordComplexus, batchCode);
			try {
				// 判断入库的单位是否可以转换为物料基础计量单位, 若可以， 则进行转换
				inventoryCount = MaterialUnitUtil.convertBaseUnit(recordComplexus, inventoryUnit,  inventoryCount, materiaCode); 
			} catch (Exception e) {
				return MessageFactory.buildRefuseMessage("Failed", "失败", BaseConstant.TYPE_物料出入库记录, "物料基础单位和所选单位不符合");
			}
			
			// 开始计算批次总量和存量
//			batchGross = batchGross + inventoryCount;
			batchStock = batchStock.add(inventoryCount);
			
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
			BigDecimal inventoryCount = new BigDecimal(inventoryCountStr);
			// 获取入库单位
			String inventoryUnitStr = CommonAlgorithm.getDataValue(rootRecord, MaterialOutInRecordCELNE3558Item.基本属性组_单位);
			if (StringUtils.isBlank(inventoryUnitStr)) {
				return MessageFactory.buildRefuseMessage("Failed", "撤销失败", BaseConstant.TYPE_物料出入库记录, "出库单位必填");
			}
			Integer inventoryUnit = Integer.parseInt(inventoryUnitStr);
			
			// 获取入库对应的批次信息code
			String batchCode = null;
			try {
				batchCode = getBatchCode(recordComplexus, recordCode);
			} catch (Exception e1) {
				return MessageFactory.buildRefuseMessage("Failed", "入库失败", BaseConstant.TYPE_物料出入库记录, "出库对应的批次不唯一");
			}
			// 批次总量
//			String batchGrossStr = CommonAlgorithm.getDataValue(recordComplexus, BaseConstant.TYPE_物料批次信息, batchCode, MaterialBatchInfoCELNE3571Item.基本属性组_总量);
//			Double batchGross = 0.0;
//			if (!StringUtils.isBlank(batchGrossStr)) {
//				batchGross = Double.parseDouble(batchGrossStr);
//			}
			// 批次存量
			String batchStockStr = CommonAlgorithm.getDataValue(recordComplexus, BaseConstant.TYPE_物料批次信息, batchCode, MaterialBatchInfoCELNE3571Item.基本属性组_存量);
			BigDecimal batchStock = new BigDecimal("0");
			if (!StringUtils.isBlank(batchStockStr)) {
				batchStock = new BigDecimal(batchStockStr);
			}
			String materiaCode = getMateriaCode(recordComplexus, batchCode);
			try {
				// 判断入库的单位是否可以转换为物料基础计量单位, 若可以， 则进行转换
				inventoryCount = MaterialUnitUtil.convertBaseUnit(recordComplexus, inventoryUnit,  inventoryCount, materiaCode); 
			} catch (Exception e) {
				return MessageFactory.buildRefuseMessage("Failed", "失败", BaseConstant.TYPE_物料出入库记录, "物料基础单位和所选单位不符合");
			}
			if (batchStock.compareTo(inventoryCount) == -1 ) {
				return MessageFactory.buildRefuseMessage("Failed", "撤销归还入库失败", BaseConstant.TYPE_物料出入库记录, "批次总量or存量不足");
			}
			
			// 开始计算批次总量和存量
//			batchGross = batchGross - inventoryCount;
			batchStock = batchStock.subtract(inventoryCount);
			
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
