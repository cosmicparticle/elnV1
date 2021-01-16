package cho.carbon.fg.eln.algorithm.eln;

import java.util.List;

import com.google.protobuf.Descriptors.EnumValueDescriptor;

import cho.carbon.complexus.FGRecordComplexus;
import cho.carbon.fg.eln.constant.BaseConstant;
import cho.carbon.fg.eln.constant.EnumKeyValue;
import cho.carbon.fg.eln.constant.RelationType;
import cho.carbon.fg.eln.constant.item.MaterialStockInfoCELNE3551Item;
import cho.carbon.fuse.improve.attribute.FuseAttributeFactory;
import cho.carbon.message.Message;
import cho.carbon.message.MessageFactory;
import cho.carbon.model.uid.UidManager;
import cho.carbon.ops.builder.RecordRelationOpsBuilder;
import cho.carbon.rrc.builder.FGRootRecordBuilder;
import cho.carbon.rrc.record.FGAttribute;
import cho.carbon.rrc.record.FGRootRecord;

/**
 * 物料库存信息
 * @author lhb
 *
 */
public class MaterialStockAlgorithm {

	/**
	 *	计算高低库存
	 * @param recordComplexus
	 * @param recordCode
	 * @param recordOpsBuilder
	 * @return
	 */
	public static Message calculateMaterialStockInfo(FGRecordComplexus recordComplexus, String recordCode, List<FGRootRecord> relatedRecordList) {
		try {
			// 获取当前物料库存信息
			FGRootRecord rootRecord = CommonAlgorithm.getRootRecord(recordComplexus, BaseConstant.TYPE_物料库存信息, recordCode);
			
			// 库存量的值
			String stockCountStr = CommonAlgorithm.getDataValue(rootRecord, MaterialStockInfoCELNE3551Item.基本属性组_库存量);
			Double stockCount = null;
			if (stockCountStr != null) {
				stockCount = Double.parseDouble(stockCountStr);
			}
			// 高库存的值
			String maxStockCountStr = CommonAlgorithm.getDataValue(rootRecord, MaterialStockInfoCELNE3551Item.基本属性组_高库存阈值);
			Double maxStockCount = null;
			if (maxStockCountStr != null) {
				maxStockCount = Double.parseDouble(maxStockCountStr);
			}
			
			// 低库存的值
			String minStockCountStr = CommonAlgorithm.getDataValue(rootRecord, MaterialStockInfoCELNE3551Item.基本属性组_低库存阈值);
			Double minStockCount = null;
			if (minStockCountStr != null) {
				minStockCount = Double.parseDouble(minStockCountStr);
			}
			
			boolean flag = false;
			Integer stock = null;
			if (stockCount != null) {
				if (maxStockCount != null && stockCount >= maxStockCount) {
					// 这个就是高库存
					stock = EnumKeyValue.ENUM_库存预警状态_高库存;
					flag = true;
				}
				if (minStockCount != null && stockCount < minStockCount) {
					// 这个就是低库存
					stock = EnumKeyValue.ENUM_库存预警状态_低库存;
					flag = true;
				}
			}
			if (flag && stock != null) {
				// 增加高低库存
				relatedRecordList.add((FGRootRecord) FuseAttributeFactory.buildFGAttribute(MaterialStockInfoCELNE3551Item.基本属性组_库存量状态, stock));
			}
		} catch (Exception e) {
			e.printStackTrace();
			return MessageFactory.buildRefuseMessage("Failed", "高低库存", BaseConstant.TYPE_物料基础信息, "计算失败");
		}
		return MessageFactory.buildInfoMessage("Succeeded", "高低库存", BaseConstant.TYPE_物料基础信息, "成功");
	}
}
