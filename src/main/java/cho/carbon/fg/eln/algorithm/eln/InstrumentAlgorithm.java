package cho.carbon.fg.eln.algorithm.eln;

import java.math.BigDecimal;

import cho.carbon.complexus.FGRecordComplexus;
import cho.carbon.fg.eln.constant.BaseConstant;
import cho.carbon.fg.eln.constant.EnumKeyValue;
import cho.carbon.fg.eln.constant.item.InstrumentCELNE3900Item;
import cho.carbon.fg.eln.constant.item.InstrumentCELNE3900Item;
import cho.carbon.fuse.improve.attribute.FuseAttributeFactory;
import cho.carbon.fuse.improve.ops.builder.FGRecordOpsBuilder;
import cho.carbon.message.Message;
import cho.carbon.message.MessageFactory;
import cho.carbon.rrc.record.FGRootRecord;

/**
 * 仪器库存算法
 * @author lhb
 *
 */
public class InstrumentAlgorithm {
	/**
	 *	计算高低库存
	 * @param recordComplexus
	 * @param recordCode
	 * @param recordOpsBuilder
	 * @return
	 */
	public static Message calculateStockInfo(FGRecordComplexus recordComplexus, String recordCode, FGRecordOpsBuilder recordOpsBuilder) {
		try {
			// 获取当前仪器库存信息
			FGRootRecord rootRecord = CommonAlgorithm.getRootRecord(recordComplexus, BaseConstant.TYPE_仪器库存, recordCode);
			
			// 库存量的值
			String stockCountStr = CommonAlgorithm.getDataValue(rootRecord, InstrumentCELNE3900Item.基本属性组_库存量);
			BigDecimal stockCount = new BigDecimal("0");
			if (stockCountStr != null) {
				stockCount =  new BigDecimal(stockCountStr);
			}
			
			// 高库存的值
			String maxStockCountStr = CommonAlgorithm.getDataValue(rootRecord, InstrumentCELNE3900Item.基本属性组_高库存阈值);
			BigDecimal maxStockCount  = new BigDecimal("0");
			if (maxStockCountStr != null) {
				maxStockCount =  new BigDecimal(maxStockCountStr);
			}
			
			// 低库存的值
			String minStockCountStr = CommonAlgorithm.getDataValue(rootRecord, InstrumentCELNE3900Item.基本属性组_低库存阈值);
			BigDecimal minStockCount = new BigDecimal("0");
			if (minStockCountStr != null) {
				minStockCount =  new BigDecimal(minStockCountStr);
			}
			
			boolean flag = false;
			Integer stock = null;
			if (stockCount != null) {
				
				if (maxStockCount != null && stockCount.compareTo(new BigDecimal("0")) == 0) {
					// 这个就是零库存
					stock = EnumKeyValue.ENUM_库存预警状态_零库存;
					flag = true;
				} else 	if (maxStockCount != null && stockCount.compareTo(maxStockCount) > -1) {
					// 这个就是高库存
					stock = EnumKeyValue.ENUM_库存预警状态_高库存;
					flag = true;
				} else 	if (minStockCount != null && stockCount.compareTo(minStockCount) == -1 ) {
					// 这个就是低库存
					stock = EnumKeyValue.ENUM_库存预警状态_低库存;
					flag = true;
				} else 	if (minStockCount != null && maxStockCount != null) {
					// 这个就是正常
					stock = EnumKeyValue.ENUM_库存预警状态_正常;
					flag = true;
				}
			}
				// 增加高低库存
				recordOpsBuilder.addUpdateAttr(FuseAttributeFactory.buildAttribute(InstrumentCELNE3900Item.基本属性组_库存状态, stock));
		} catch (Exception e) {
			e.printStackTrace();
			return MessageFactory.buildRefuseMessage("Failed", "高低库存", BaseConstant.TYPE_仪器库存, "计算失败");
		}
		return MessageFactory.buildInfoMessage("Succeeded", "高低库存", BaseConstant.TYPE_仪器库存, "成功");
	}
}
