package cho.carbon.fg.eln.algorithm.eln;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import cho.carbon.complexus.FGRecordComplexus;
import cho.carbon.fg.eln.constant.BaseConstant;
import cho.carbon.fg.eln.constant.RelationType;
import cho.carbon.fg.eln.constant.item.ExpRecordCELNE2189Item;
import cho.carbon.fg.eln.constant.item.WeekMonthReportCELNE4037Item;
import cho.carbon.message.Message;
import cho.carbon.message.MessageFactory;
import cho.carbon.meta.enun.operator.BetweenOperator;
import cho.carbon.ops.builder.RecordRelationOpsBuilder;
import cho.carbon.query.model.ConJunctionFactory;
import cho.carbon.query.model.FGConJunctionFactory;
import cho.carbon.query.model.QueryRecordParmFactory;
import cho.carbon.rrc.query.SimpleRecordQueryPanel;
import cho.carbon.rrc.record.FGRootRecord;

public class WeekMonthReportAlgorithm {

	

	/**
	 * 生成周月总结报告
	 * @param recordComplexus
	 * @param recordCode
	 * @param relationOpsBuilder
	 * @param relatedRecordList
	 * @param userCode 当前用户
	 * @return
	 */
	public static Message createWeekMonthReport(FGRecordComplexus recordComplexus, String recordCode, 
			RecordRelationOpsBuilder relationOpsBuilder, List<FGRootRecord> relatedRecordList, String userCode) {
		try {
			// 获取周月总结记录
			FGRootRecord rootRecord = CommonAlgorithm.getRootRecord(recordComplexus, BaseConstant.TYPE_周月总结, recordCode);
			
			String startTime = CommonAlgorithm.getDataValue(rootRecord, WeekMonthReportCELNE4037Item.基本属性组_开始日期);
			String endTime = CommonAlgorithm.getDataValue(rootRecord, WeekMonthReportCELNE4037Item.基本属性组_结束日期);
			
			
			// 查询实验记录， 按照用户、开始时间、结束时间 进行查询
			
			QueryRecordParmFactory queryRecordParmFactory= new QueryRecordParmFactory(BaseConstant.TYPE_实验记录);
			//设置结构化查询过滤条件，可选项，默认不设任何过滤条件。
			FGConJunctionFactory conJunctionFactory = queryRecordParmFactory.getConJunctionFactory();
			conJunctionFactory.getGroupFactory().addBetween(ExpRecordCELNE2189Item.基本属性组_实验日期, startTime, endTime, BetweenOperator.BETWEEN);
			// 并且实验记录的实验员为userCode
			List<String> userCodes  = new ArrayList<String>();
			userCodes.add(userCode);
			// 存在实验员的关系， 并且实验员为 uerCode
			List<Long> inRelationTypes =  new ArrayList<Long>();
			inRelationTypes.add(123L);
			conJunctionFactory.getRightRelJuncFactory(BaseConstant.TYPE_系统用户)
			.getRelationCriterionFactory().setInRightCodes(userCodes);
//			.setInRelationTypes(inRelationTypes);
//			
			// 执行查询
			List<String> codeList = SimpleRecordQueryPanel.queryCodeList(queryRecordParmFactory.getQueryParameter());
			
		System.out.println();
			
		} catch (Exception e) {
			e.printStackTrace();
			return MessageFactory.buildRefuseMessage("computeMaterialGrossFailed", "计算投料总量失败", BaseConstant.TYPE_实验记录, "计算投料总量失败");
		}
		return MessageFactory.buildInfoMessage("computeMaterialGrossSucceeded", "计算投料总量成功", BaseConstant.TYPE_实验记录, "计算投料总量成功");
	}
	
	
}
