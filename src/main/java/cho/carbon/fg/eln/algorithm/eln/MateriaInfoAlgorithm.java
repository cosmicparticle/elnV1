package cho.carbon.fg.eln.algorithm.eln;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

import cho.carbon.complexus.FGRecordComplexus;
import cho.carbon.fg.eln.algorithm.MaterialUnitUtil;
import cho.carbon.fg.eln.constant.BaseConstant;
import cho.carbon.fg.eln.constant.EnumKeyValue;
import cho.carbon.fg.eln.constant.RelationType;
import cho.carbon.fg.eln.constant.item.MaterialStockInfoCELNE3551Item;
import cho.carbon.message.Message;
import cho.carbon.message.MessageFactory;
import cho.carbon.model.uid.UidManager;
import cho.carbon.ops.builder.RecordRelationOpsBuilder;
import cho.carbon.relation.RecordRelation;
import cho.carbon.rrc.builder.FGRootRecordBuilder;
import cho.carbon.rrc.record.FGRootRecord;

/**
 * 物料基础信息算法
 * @author lhb
 *
 */
public class MateriaInfoAlgorithm {

	/**
	 *	物料基础信息添加物料库存
	 * @param recordComplexus
	 * @param recordCode
	 * @param recordOpsBuilder
	 * @return
	 */
	public static Message addMaterialStockInfo(FGRecordComplexus recordComplexus, String recordCode, List<FGRootRecord> relatedRecordList, List<RecordRelationOpsBuilder>  relatedRelationOpsBuilderList, RecordRelationOpsBuilder relationOpsBuilder) {
		try {
			// 获取当前物料基础信息
			FGRootRecord rootRecord = CommonAlgorithm.getRootRecord(recordComplexus, BaseConstant.TYPE_物料基础信息, recordCode);
		
			// 新增物料库存信息
			String materialStockCode = UidManager.getLongUID() + "";
			FGRootRecordBuilder msbuilder =FGRootRecordBuilder.getInstance(BaseConstant.TYPE_物料库存信息, materialStockCode);
			//设置记录属性。第一个参数为模型属性的编码，第二个参数为模型属性的取值
			msbuilder.putAttribute(MaterialStockInfoCELNE3551Item.基本属性组_库存总量, 0);
			msbuilder.putAttribute(MaterialStockInfoCELNE3551Item.基本属性组_已预订量, 0);
			msbuilder.putAttribute(MaterialStockInfoCELNE3551Item.基本属性组_低库存阈值, 0);
			msbuilder.putAttribute(MaterialStockInfoCELNE3551Item.基本属性组_高库存阈值, 0);
			msbuilder.putAttribute(MaterialStockInfoCELNE3551Item.基本属性组_库存量状态, EnumKeyValue.ENUM_库存预警状态_零库存);
			//得到记录对象
			FGRootRecord newRecord = msbuilder.getRootRecord();
			//放入到kie预设的全局变量中
			relatedRecordList.add(newRecord);
			
			relationOpsBuilder.putRelation(RelationType.RR_物料基础信息_库存信息_物料库存信息, materialStockCode);
		} catch (Exception e) {
			e.printStackTrace();
			return MessageFactory.buildRefuseMessage("Failed", "物料基础信息添加物料库存失败", BaseConstant.TYPE_物料基础信息, "物料基础信息添加物料库存失败");
		}
		return MessageFactory.buildInfoMessage("Succeeded", "物料基础信息添加物料库存成功", BaseConstant.TYPE_物料基础信息, "物料基础信息添加物料库存成功");
	}
	
	
	
	/**
	 * 	物料基础计量单位更改的时候
	 * @param recordComplexus
	 * @param recordCode
	 * @param relatedRelationOpsBuilderList
	 * @param opsValue   现在的基础计量单
	 * @param fuseValue  未来的计量单位
	 * @return
	 */
	public static Message updateBaseUnit(FGRecordComplexus recordComplexus, String recordCode,
			List<RecordRelationOpsBuilder>  relatedRelationOpsBuilderList, 
			Object opsValue, Object fuseValue) {
		try {
			Integer opsUnit = (Integer)opsValue;
			Integer fuseUnit = (Integer)fuseValue;
			
			
			// 判断前后两个单位是否能转换
			BigDecimal convertCount = MaterialUnitUtil.getConvertCount(fuseUnit, new BigDecimal("0"), opsUnit);
			
			// 获取物料对应的库存信息
			List<RecordRelation> materialStockRela = (List)CommonAlgorithm.getAppointRecordRelation(recordComplexus, BaseConstant.TYPE_物料基础信息, recordCode, RelationType.RR_物料基础信息_库存信息_物料库存信息);
			
			
			
			
			
			
			
			
			
			
			
			// 获取当前物料基础信息
			FGRootRecord rootRecord = CommonAlgorithm.getRootRecord(recordComplexus, BaseConstant.TYPE_物料基础信息, recordCode);
		
			
			
		
		} catch (Exception e) {
			e.printStackTrace();
			return MessageFactory.buildRefuseMessage("Failed", "物料基础信息添加物料库存失败", BaseConstant.TYPE_物料基础信息, "物料基础信息添加物料库存失败");
		}
		return MessageFactory.buildInfoMessage("Succeeded", "物料基础信息添加物料库存成功", BaseConstant.TYPE_物料基础信息, "物料基础信息添加物料库存成功");
	}
}
