package cho.carbon.fg.eln.algorithm.eln;

import java.math.BigDecimal;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import cho.carbon.complexus.FGRecordComplexus;
import cho.carbon.fg.eln.algorithm.ComputeSign;
import cho.carbon.fg.eln.algorithm.MaterialUnitUtilOld;
import cho.carbon.fg.eln.constant.BaseConstant;
import cho.carbon.fg.eln.constant.EnumKeyValue;
import cho.carbon.fg.eln.constant.RelationType;
import cho.carbon.fg.eln.constant.item.InstrumentCELNE3900Item;
import cho.carbon.fg.eln.constant.item.InstrumentOutInRecordCELNE3894Item;
import cho.carbon.fg.eln.constant.item.MaterialBatchInfoCELNE3571Item;
import cho.carbon.fg.eln.constant.item.MaterialOutInRecordCELNE3558Item;
import cho.carbon.message.Message;
import cho.carbon.message.MessageFactory;
import cho.carbon.ops.builder.RecordRelationOpsBuilder;
import cho.carbon.relation.RecordRelation;
import cho.carbon.rrc.builder.FGRootRecordBuilder;
import cho.carbon.rrc.record.FGRootRecord;

/**
 * 仪器出入库算法
 * @author lhb
 *
 */
public class InstrumentOutInRecordAlgorithm {

	/**
	 * 仪器出入库命令
	 * @param recordComplexus
	 * @param recordCode
	 * @param relatedRecordList
	 * @return
	 */
	public static Message inStorageCommand(FGRecordComplexus recordComplexus, String recordCode, List<FGRootRecord> relatedRecordList, List<RecordRelationOpsBuilder>  relatedRelationOpsBuilderList, RecordRelationOpsBuilder relationOpsBuilder, ComputeSign computeSign) {
		try {
			// 获取当前记录
			FGRootRecord rootRecord = CommonAlgorithm.getRootRecord(recordComplexus, BaseConstant.TYPE_仪器出入库记录, recordCode);
			
			// 获取入库数量
			String inventoryCountStr = CommonAlgorithm.getDataValue(rootRecord, InstrumentOutInRecordCELNE3894Item.基本属性组_出入库数量);
			if (StringUtils.isBlank(inventoryCountStr)) {
				return MessageFactory.buildRefuseMessage("Failed", "入库失败", BaseConstant.TYPE_仪器出入库记录, "入库数量必填");
			}
			// 入库数量
			BigDecimal inventoryCount = new BigDecimal(inventoryCountStr);
			
			// 获取物料库存信息
			List<RecordRelation> stockInfoRelaList = (List)CommonAlgorithm.getAppointRecordRelation(recordComplexus, BaseConstant.TYPE_仪器出入库记录, recordCode, RelationType.RR_仪器出入库记录_仪器库存_仪器库存);
			if (stockInfoRelaList != null && stockInfoRelaList.size() != 1) {
				return MessageFactory.buildRefuseMessage("Failed", "入库失败", BaseConstant.TYPE_仪器出入库记录, "一次只能出入库一个库存");
			}
			// 获取库存的code
			String stockInfoCode = stockInfoRelaList.get(0).getRightCode();
			
			// 获取库存的量
			String stockCountStr = CommonAlgorithm.getDataValue(recordComplexus, BaseConstant.TYPE_仪器库存, stockInfoCode, InstrumentCELNE3900Item.基本属性组_库存量);
			BigDecimal stockCount = new BigDecimal("0");
			if (!StringUtils.isBlank(stockCountStr)) {
				stockCount = new BigDecimal(stockCountStr);
			}
			
			if (ComputeSign.add.equals(computeSign)) {
				stockCount = stockCount.add(inventoryCount);
			} else if (ComputeSign.minus.equals(computeSign)) {
				
				
				if(stockCount.compareTo(inventoryCount) == -1) {
					return MessageFactory.buildRefuseMessage("Failed", "入库操作", BaseConstant.TYPE_仪器出入库记录, "库存太少");
				}
				
				stockCount = stockCount.subtract(inventoryCount);
			}
			// 更改库存量
			
			FGRootRecordBuilder builder =FGRootRecordBuilder.getInstance(BaseConstant.TYPE_仪器库存, stockInfoCode);
			//设置记录属性。第一个参数为模型属性的编码，第二个参数为模型属性的取值
			builder.putAttribute(InstrumentCELNE3900Item.基本属性组_库存量, stockCount);
			builder.putAttribute(InstrumentCELNE3900Item.基本属性组_是否更新阈值, EnumKeyValue.ENUM_是否_是);
			//放入到kie预设的全局变量中
			relatedRecordList.add(builder.getRootRecord());
		} catch (Exception e) {
			e.printStackTrace();
			return MessageFactory.buildRefuseMessage("Failed", "入库操作", BaseConstant.TYPE_仪器出入库记录, "失败");
		}
		
		return MessageFactory.buildInfoMessage("Succeeded", "成功", BaseConstant.TYPE_仪器出入库记录, "入库成功");
	}

}
