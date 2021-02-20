package cho.carbon.fg.eln.algorithm.eln;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

import cho.carbon.complexus.FGRecordComplexus;
import cho.carbon.fg.eln.constant.BaseConstant;
import cho.carbon.fg.eln.constant.EnumKeyValue;
import cho.carbon.fg.eln.constant.RelationType;
import cho.carbon.fg.eln.constant.item.ElnprojectCELNE2244Item;
import cho.carbon.fg.eln.constant.item.InstrumentCELNE3900Item;
import cho.carbon.fg.eln.constant.item.ProjectExecutionPlanCELNE3705Item;
import cho.carbon.fuse.improve.attribute.FuseAttributeFactory;
import cho.carbon.fuse.improve.ops.builder.FGRecordOpsBuilder;
import cho.carbon.message.Message;
import cho.carbon.message.MessageFactory;
import cho.carbon.relation.RecordRelation;
import cho.carbon.rrc.builder.FGRootRecordBuilder;
import cho.carbon.rrc.record.FGRootRecord;

public class ProjectExecutionPlanAlgorithm {

	
	/**
	 * 当计划困难是， 项目也会困难
	 * @param recordComplexus
	 * @param recordCode
	 * @param recordOpsBuilder
	 * @return
	 */
	public static Message planProjectDifficulty(FGRecordComplexus recordComplexus, String recordCode, FGRecordOpsBuilder recordOpsBuilder, List<FGRootRecord> relatedRecordList) {
		try {
			// 获取当前仪器库存信息
			FGRootRecord rootRecord = CommonAlgorithm.getRootRecord(recordComplexus, BaseConstant.TYPE_项目执行计划, recordCode);
			
			String label = CommonAlgorithm.getDataValue(rootRecord, ProjectExecutionPlanCELNE3705Item.基本属性组_标签);
			
			boolean contains = label.contains(EnumKeyValue.ENUM_项目执行计划标签_困难+"");
			
			if (contains) {
				// 那么增加项目困难标签
				
				List<RecordRelation> projectRelaList = (List<RecordRelation>) CommonAlgorithm.getAppointRecordRelation(recordComplexus, BaseConstant.TYPE_项目执行计划, recordCode, RelationType.RR_项目执行计划_实验项目_实验项目);
				if (projectRelaList != null && projectRelaList.size() != 1) {
					return MessageFactory.buildRefuseMessage("Failed", "失败", BaseConstant.TYPE_项目执行计划, "此项目计划对应的实验项目不唯一");
				}
				// 获取到项目code
				String projectCode = projectRelaList.get(0).getRightCode();
				
				String projectLabel = CommonAlgorithm.getDataValue(recordComplexus, BaseConstant.TYPE_实验项目, projectCode, ElnprojectCELNE2244Item.基本属性组_项目标签);
				
				boolean contains2 = projectLabel.contains(EnumKeyValue.ENUM_项目标签_困难 +"");
				if (!contains2) {
					projectLabel = projectLabel + "," + EnumKeyValue.ENUM_项目标签_困难 +"";
					
					FGRootRecordBuilder builder =FGRootRecordBuilder.getInstance(BaseConstant.TYPE_实验项目, projectCode);
					//设置记录属性。第一个参数为模型属性的编码，第二个参数为模型属性的取值
					builder.putAttribute(ElnprojectCELNE2244Item.基本属性组_项目标签, projectLabel);
					//放入到kie预设的全局变量中
					relatedRecordList.add(builder.getRootRecord());
				}
				
			}
			
			
			// 增加高低库存
//			recordOpsBuilder.addUpdateAttr(FuseAttributeFactory.buildAttribute(InstrumentCELNE3900Item.基本属性组_库存状态, stock));
		} catch (Exception e) {
			e.printStackTrace();
			return MessageFactory.buildRefuseMessage("Failed", "高低库存", BaseConstant.TYPE_仪器库存, "计算失败");
		}
		return MessageFactory.buildInfoMessage("Succeeded", "高低库存", BaseConstant.TYPE_仪器库存, "成功");
	}
}
