package cho.carbon.fg.eln.algorithm.eln;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

import cho.carbon.complexus.FGRecordComplexus;
import cho.carbon.fg.eln.common.CommonCalculation;
import cho.carbon.fg.eln.constant.BaseConstant;
import cho.carbon.fg.eln.constant.EnumKeyValue;
import cho.carbon.fg.eln.constant.RelationType;
import cho.carbon.fg.eln.constant.item.ConsultProjectCELNE4320Item;
import cho.carbon.fg.eln.constant.item.ElnprojectCELNE2244Item;
import cho.carbon.fg.eln.constant.item.ExpRecordCELNE2189Item;
import cho.carbon.fuse.improve.attribute.FuseAttributeFactory;
import cho.carbon.fuse.improve.ops.builder.FGRecordOpsBuilder;
import cho.carbon.message.Message;
import cho.carbon.message.MessageFactory;
import cho.carbon.ops.builder.RecordRelationOpsBuilder;
import cho.carbon.relation.RecordRelation;
import cho.carbon.rrc.builder.FGRootRecordBuilder;
import cho.carbon.rrc.record.FGAttribute;
import cho.carbon.rrc.record.FGRootRecord;

/**
 * 查阅项目 算法
 * @author lhb
 *
 */
public class ConsultProjectAlgorithm {

	/**
	 * 执行同意查阅命令
	 * @param recordComplexus
	 * @param recordCode
	 * @param relationOpsBuilder
	 * @param relatedRecordList
	 * @param recordOpsBuilder
	 * @return
	 */
	public static Message agreeConsult(FGRecordComplexus recordComplexus, String recordCode, 
			RecordRelationOpsBuilder relationOpsBuilder, List<FGRootRecord> relatedRecordList, 
			FGRecordOpsBuilder recordOpsBuilder) {
		try {
			// 获取当前查阅项目
			FGRootRecord rootRecord = CommonAlgorithm.getRootRecord(recordComplexus, BaseConstant.TYPE_查阅项目, recordCode);
			// 获取实验项目的所有实验记录
//			List<RecordRelation> expProjectRelaList = (List)CommonAlgorithm.getAppointRecordRelation(recordComplexus, BaseConstant.TYPE_查阅项目, recordCode, RelationType.RR_查阅项目_查阅实验项目_实验项目);
//			if (!expProjectRelaList.isEmpty()) {
//				// 获取实验项目的所有实验记录
//				List<RecordRelation> peopleRelaList = (List)CommonAlgorithm.getAppointRecordRelation(recordComplexus, BaseConstant.TYPE_查阅项目, recordCode, RelationType.RR_查阅项目_查阅人_系统用户);
//				if (peopleRelaList.isEmpty()) {
//					return MessageFactory.buildRefuseMessage("Failed", "失败", BaseConstant.TYPE_查阅项目, "没有找到查阅人");
//				}
//				// 获取查阅人code
//				String consultPeopleCode = peopleRelaList.get(0).getRightCode();
//			}
			
			// 截止日期
			Date endDate = null;
			String endDateStr = CommonAlgorithm.getDataValue(rootRecord, ConsultProjectCELNE4320Item.基本属性组_查阅截止日期);
			if(CommonCalculation.isBasicLawful(endDateStr)) {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				endDate = sdf.parse(endDateStr);
			} else {
				endDate = new Date();
			}
			
			// 获取当前日期
			Date currentDate = new Date();
			
			long currentTime = currentDate.getTime();
			long endTime = endDate.getTime();
			
			if (endTime > currentTime) {
				// 判断相差天数， 大于30天， 按照30天计算
				long  time = endTime	- currentTime;
				int day = (int) (time/(24L*60L*60L*1000L));
				if (day > 30) {
					long aa = 30L*24L*60L*60L*1000L;
					long futureTime = currentTime + aa;
					Date futureTimeDate = new Date(futureTime);
				    
				    FGAttribute attr=FuseAttributeFactory.buildFGAttribute(ConsultProjectCELNE4320Item.基本属性组_查阅截止日期, futureTimeDate);
					recordOpsBuilder.addUpdateAttr(attr);	
				}
			} else {
				long futureTime = currentTime + (15L*24L*60L*60L*1000L);
				Date futureTimeDate = new Date(futureTime);
				// 这里默认15天
				FGAttribute attr=FuseAttributeFactory.buildFGAttribute(ConsultProjectCELNE4320Item.基本属性组_查阅截止日期, futureTimeDate);
				recordOpsBuilder.addUpdateAttr(attr);	
			}
			FGAttribute attr=FuseAttributeFactory.buildFGAttribute(ConsultProjectCELNE4320Item.基本属性组_查阅状态, EnumKeyValue.ENUM_查阅项目状态_查阅中);
			recordOpsBuilder.addUpdateAttr(attr);	
		} catch (Exception e) {
			e.printStackTrace();
			return MessageFactory.buildRefuseMessage("Failed", "失败", BaseConstant.TYPE_查阅项目, "失败");
		}
		return MessageFactory.buildInfoMessage("Succeeded", "成功", BaseConstant.TYPE_查阅项目, "成功");
	}
	
}
