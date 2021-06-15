package cho.carbon.fg.eln.algorithm.eln;

import java.time.LocalDate;
import java.util.List;

import cho.carbon.complexus.FGRecordComplexus;
import cho.carbon.fg.eln.constant.BaseConstant;
import cho.carbon.fg.eln.constant.EnumKeyValue;
import cho.carbon.fg.eln.constant.RelationType;
import cho.carbon.fg.eln.constant.item.ElnprojectCELNE2244Item;
import cho.carbon.fg.eln.constant.item.ExpProjectReportCELNE3499Item;
import cho.carbon.fg.eln.constant.item.ExpRecordCELNE2189Item;
import cho.carbon.fuse.improve.attribute.FuseAttributeFactory;
import cho.carbon.fuse.improve.ops.builder.FGRecordOpsBuilder;
import cho.carbon.message.Message;
import cho.carbon.message.MessageFactory;
import cho.carbon.model.uid.UidManager;
import cho.carbon.ops.builder.RecordRelationOpsBuilder;
import cho.carbon.relation.RecordRelation;
import cho.carbon.rrc.builder.FGRootRecordBuilder;
import cho.carbon.rrc.record.FGAttribute;
import cho.carbon.rrc.record.FGRootRecord;

/**
 * 实验项目
 * @author lhb
 */
public class ElnprojectAlgorithm {
	
	
	/**
	 * 对项目进行存档
	 * @param recordComplexus
	 * @param recordCode
	 * @param relationOpsBuilder
	 * @param relatedRecordList
	 * @return
	 */
	public static Message archivedProject(FGRecordComplexus recordComplexus, String recordCode, 
			RecordRelationOpsBuilder relationOpsBuilder, List<FGRootRecord> relatedRecordList, FGRecordOpsBuilder recordOpsBuilder) {
		try {
			// 获取当前实验项目
			FGRootRecord rootRecord = CommonAlgorithm.getRootRecord(recordComplexus, BaseConstant.TYPE_实验项目, recordCode);
			// 实验项目存档
			FGAttribute attr=FuseAttributeFactory.buildFGAttribute(ElnprojectCELNE2244Item.基本属性组_存档状态, EnumKeyValue.ENUM_实验存档状态_已存档);
			recordOpsBuilder.addUpdateAttr(attr);	
			// 获取实验项目的所有实验记录
			List<RecordRelation> expRecordRelaList = (List)CommonAlgorithm.getAppointRecordRelation(recordComplexus, BaseConstant.TYPE_实验项目, recordCode, RelationType.RR_实验项目_组成实验_实验记录);
			for (RecordRelation recordRelation : expRecordRelaList) {
				// 实验记录code
				String expRecordCode = recordRelation.getRightCode();
				
				// 把此实验记录记性存档
				FGRootRecordBuilder builder = FGRootRecordBuilder.getInstance(BaseConstant.TYPE_实验记录, expRecordCode);
				//设置记录属性。第一个参数为模型属性的编码，第二个参数为模型属性的取值
				builder.putAttribute(ExpRecordCELNE2189Item.基本属性组_存档状态, EnumKeyValue.ENUM_实验存档状态_已存档);
				//放入到kie预设的全局变量中
				relatedRecordList.add(builder.getRootRecord());	
			}
			
			// 获取项目的所有项目报告
			List<RecordRelation> projectReportRelaList = (List)CommonAlgorithm.getAppointRecordRelation(recordComplexus, BaseConstant.TYPE_实验项目, recordCode, RelationType.RR_实验项目_项目报告_项目报告);
			for (RecordRelation recordRelation : projectReportRelaList) {
				// 项目报告 code
				String projectReportCode = recordRelation.getRightCode();
				
				// 把此实验记录记性存档
				FGRootRecordBuilder builder = FGRootRecordBuilder.getInstance(BaseConstant.TYPE_项目报告, projectReportCode);
				//设置记录属性。第一个参数为模型属性的编码，第二个参数为模型属性的取值
				builder.putAttribute(ExpProjectReportCELNE3499Item.基本属性组_存档状态, EnumKeyValue.ENUM_实验存档状态_已存档);
				//放入到kie预设的全局变量中
				relatedRecordList.add(builder.getRootRecord());	
			}
		} catch (Exception e) {
			e.printStackTrace();
			return MessageFactory.buildRefuseMessage("computeMaterialGrossFailed", "计算投料总量失败", BaseConstant.TYPE_实验记录, "计算投料总量失败");
		}
		return MessageFactory.buildInfoMessage("computeMaterialGrossSucceeded", "计算投料总量成功", BaseConstant.TYPE_实验记录, "计算投料总量成功");
	}
	

	/**
	 *	 生成实验项目报告
	 * @param recordComplexus
	 * @param recordCode
	 * @param recordOpsBuilder
	 * @return
	 */
	public static Message createProjectReport(FGRecordComplexus recordComplexus, String recordCode, 
			RecordRelationOpsBuilder relationOpsBuilder, List<FGRootRecord> relatedRecordList) {
		try {
			
	    	 LocalDate localDate = LocalDate.now();
			 int year = localDate.getYear();
			 int monthValue = localDate.getMonthValue();
			 
			 int dayOfMonth = localDate.getDayOfMonth()-1;
			 int weekNo = (dayOfMonth / 7) +1;
			 
			
			// 获取当前实验项目
			FGRootRecord rootRecord = CommonAlgorithm.getRootRecord(recordComplexus, BaseConstant.TYPE_实验项目, recordCode);
			String reportCode = UidManager.getLongUID() + "";
			FGRootRecordBuilder builder =FGRootRecordBuilder.getInstance(BaseConstant.TYPE_项目报告, reportCode);
			//设置记录属性。第一个参数为模型属性的编码，第二个参数为模型属性的取值
			builder.putAttribute(ExpProjectReportCELNE3499Item.基本属性组_年, getYear(year));
			builder.putAttribute(ExpProjectReportCELNE3499Item.基本属性组_月, getMonth(monthValue));
			builder.putAttribute(ExpProjectReportCELNE3499Item.基本属性组_月周, getWeek(weekNo));
			builder.putAttribute(ExpProjectReportCELNE3499Item.基本属性组_周期类别, EnumKeyValue.ENUM_报告周期类别_周报);
			builder.putAttribute(ExpProjectReportCELNE3499Item.基本属性组_状态, EnumKeyValue.ENUM_项目报告状态_编辑中);
			
			//放入到kie预设的全局变量中
			relatedRecordList.add(builder.getRootRecord());
			
			relationOpsBuilder.putRelation(RelationType.RR_实验项目_项目报告_项目报告, reportCode);
		} catch (Exception e) {
			e.printStackTrace();
			return MessageFactory.buildRefuseMessage("computeMaterialGrossFailed", "计算投料总量失败", BaseConstant.TYPE_实验记录, "计算投料总量失败");
		}
		return MessageFactory.buildInfoMessage("computeMaterialGrossSucceeded", "计算投料总量成功", BaseConstant.TYPE_实验记录, "计算投料总量成功");
	}
	
	/**
	 * 获取年
	 * @return
	 */
	private static Integer getYear(Integer year) {
		
		switch (year) {
		case 2020:
			return EnumKeyValue.ENUM_年_2020年;
		case 2021:
			return EnumKeyValue.ENUM_年_2021年;
		case 2022:
			return EnumKeyValue.ENUM_年_2022年;
		case 2023:
			return EnumKeyValue.ENUM_年_2023年;
		case 2024:
			return EnumKeyValue.ENUM_年_2024年;
		case 2025:
			return EnumKeyValue.ENUM_年_2025年;
//		case 2026:
//			return EnumKeyValue.ENUM_年_2026年;
//		case 8:
//			return EnumKeyValue.ENUM_年_2020年;
//		case 9:
//			return EnumKeyValue.ENUM_年_2020年;
//		case 10:
//			return EnumKeyValue.ENUM_年_2020年;
//		case 11:
//			return EnumKeyValue.ENUM_年_2020年;
//		case 12:
//			return EnumKeyValue.ENUM_年_2020年;
}
		return null;
	}
	
	
	/**
	 * 获取月
	 * @return
	 */
	private static Integer getMonth(Integer month) {
		switch (month) {
			case 1:
				return EnumKeyValue.ENUM_月_1月;
			case 2:
				return EnumKeyValue.ENUM_月_2月;
			case 3:
				return EnumKeyValue.ENUM_月_3月;
			case 4:
				return EnumKeyValue.ENUM_月_4月;
			case 5:
				return EnumKeyValue.ENUM_月_5月;
			case 6:
				return EnumKeyValue.ENUM_月_6月;
			case 7:
				return EnumKeyValue.ENUM_月_7月;
			case 8:
				return EnumKeyValue.ENUM_月_8月;
			case 9:
				return EnumKeyValue.ENUM_月_9月;
			case 10:
				return EnumKeyValue.ENUM_月_10月;
			case 11:
				return EnumKeyValue.ENUM_月_11月;
			case 12:
				return EnumKeyValue.ENUM_月_12月;
	}
	
		return null;
	}
	
	/**
	 * 获取周
	 * @return
	 */
	private static Integer getWeek(Integer week) {
		
		switch (week) {
			case 1:
				return EnumKeyValue.ENUM_月周_第一周;
			case 2:
				return EnumKeyValue.ENUM_月周_第二周;
			case 3:
				return EnumKeyValue.ENUM_月周_第三周;
			case 4:
				return EnumKeyValue.ENUM_月周_第四周;
			case 5:
				return EnumKeyValue.ENUM_月周_第五周;
		}
		
		return null;
	}
	
}
