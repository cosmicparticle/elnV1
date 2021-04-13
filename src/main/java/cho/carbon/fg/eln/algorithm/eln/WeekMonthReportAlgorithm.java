package cho.carbon.fg.eln.algorithm.eln;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.lowagie.text.Cell;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Table;
import com.lowagie.text.rtf.RtfWriter2;

import cho.carbon.complexus.FGRecordComplexus;
import cho.carbon.fg.eln.algorithm.MyFileUtils;
import cho.carbon.fg.eln.common.CommonCalculation;
import cho.carbon.fg.eln.constant.BaseConstant;
import cho.carbon.fg.eln.constant.EnumKeyValue;
import cho.carbon.fg.eln.constant.RelationType;
import cho.carbon.fg.eln.constant.item.ElnprojectCELNE2244Item;
import cho.carbon.fg.eln.constant.item.ExpRecordCELNE2189Item;
import cho.carbon.fg.eln.constant.item.WeekMonthReportCELNE4037Item;
import cho.carbon.fuse.improve.attribute.FuseAttributeFactory;
import cho.carbon.fuse.improve.ops.builder.FGRecordOpsBuilder;
import cho.carbon.message.Message;
import cho.carbon.message.MessageFactory;
import cho.carbon.meta.enun.operator.BetweenOperator;
import cho.carbon.ops.builder.RecordRelationOpsBuilder;
import cho.carbon.query.model.FGConJunctionFactory;
import cho.carbon.query.model.QueryRecordParmFactory;
import cho.carbon.relation.RecordRelation;
import cho.carbon.rrc.query.SimpleRecordQueryPanel;
import cho.carbon.rrc.record.FGAttribute;
import cho.carbon.rrc.record.FGRootRecord;
import cho.carbon.util.MD5Util;

public class WeekMonthReportAlgorithm {

	

	/**
	 * 	生成周月总结报告
	 * @param recordComplexus
	 * @param recordCode
	 * @param relationOpsBuilder
	 * @param relatedRecordList
	 * @param userCode 当前用户
	 * @return
	 */
	public static Message createWeekMonthReport(FGRecordComplexus recordComplexus, String recordCode, 
			FGRecordOpsBuilder recordOpsBuilder, List<FGRootRecord> relatedRecordList, String userCode) {
		try {
			// 获取周月总结记录
			FGRootRecord rootRecord = CommonAlgorithm.getRootRecord(recordComplexus, BaseConstant.TYPE_周月总结, recordCode);
			
			String startTime = CommonAlgorithm.getDataValue(recordComplexus, BaseConstant.TYPE_周月总结, recordCode, WeekMonthReportCELNE4037Item.基本属性组_开始日期);
			String endTime = CommonAlgorithm.getDataValue(recordComplexus, BaseConstant.TYPE_周月总结, recordCode, WeekMonthReportCELNE4037Item.基本属性组_结束日期);
			
//			String startTime = CommonAlgorithm.getDataValue(rootRecord, WeekMonthReportCELNE4037Item.基本属性组_开始日期);
//			String endTime = CommonAlgorithm.getDataValue(rootRecord, WeekMonthReportCELNE4037Item.基本属性组_结束日期);
			
			
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
			inRelationTypes.add(RelationType.RR_实验记录_实验员_系统用户);
			conJunctionFactory.getRightRelJuncFactory(BaseConstant.TYPE_系统用户)
			.getRelationCriterionFactory().setInRightCodes(userCodes)
			.setInRelationTypes(inRelationTypes);
			
			// 执行查询, 获取到所有相关的实验记录
			List<String> codeList = SimpleRecordQueryPanel.queryCodeList(queryRecordParmFactory.getQueryParameter());
			
		
		// 存放项目code对应的实验记录code
		Map<String, List> projectMap = new HashMap<String, List>();
		// 遍历实验记录， 查询出此实验记录信息， 并按照项目进行分组
		for (String expCode : codeList) {
			List<RecordRelation> projectList = (List)CommonAlgorithm.getAppointRecordRelation(recordComplexus, BaseConstant.TYPE_实验记录, expCode, RelationType.RR_实验记录_关联项目_实验项目);
			if (!projectList.isEmpty()) {
				// 获取项目code
				String projectCode = projectList.get(0).getRightCode();
				boolean containsKey = projectMap.containsKey(projectCode);
				if (containsKey) {
					// map 中存在此项目
					List<String> list = projectMap.get(projectCode);
					list.add(expCode);
				} else {
					List<String> list = new ArrayList<String>();
					list.add(expCode);
					projectMap.put(projectCode, list);
				}
			}
		}
		
		// 获取操作系统桌面路径
		String filePath = CommonAlgorithm.getDesktopPath() + File.separator + "工作总结.doc";
		
		
		if (!projectMap.isEmpty()) {
			// 遍历map， 根据项目和实验记录生成项目日、周、月等报告
			createWord(recordComplexus, projectMap, filePath);
			
			// 上传文件
			CommonAlgorithm.uploadFile(recordOpsBuilder, filePath,WeekMonthReportCELNE4037Item.基本属性组_附件报告, "工作总结.doc", ".doc");
			// 删除文件
			File file = new File(filePath);
			file.delete();
		} else {
			return MessageFactory.buildRefuseMessage("Failed", "失败", BaseConstant.TYPE_周月总结, "没有查询到日期范围的实验记录");
		}
		
		} catch (Exception e) {
			e.printStackTrace();
			return MessageFactory.buildRefuseMessage("computeMaterialGrossFailed", "失败", BaseConstant.TYPE_周月总结, "生成模板报告失败");
		}
		return MessageFactory.buildInfoMessage("computeMaterialGrossSucceeded", "成功", BaseConstant.TYPE_周月总结, "生成模板报告成功");
	}

	/**
	 * 	根据项目code和项目下的实验记录 生成word 文档
	 * @param recordComplexus
	 * @param projectMap
	 * @throws Exception 
	 */
	private static void createWord(FGRecordComplexus recordComplexus, Map<String, List> projectMap, String filePath) throws Exception {
		Iterator<Entry<String, List>> iterator = projectMap.entrySet().iterator();
		
		 //设置纸张大小  
        Document document = new Document(PageSize.A4);  
        //建立一个书写器，与document对象关联  
        RtfWriter2.getInstance(document, new FileOutputStream(filePath));  
        document.open();  
        Paragraph title = new Paragraph("实验总结");  
        //设置标题格式对齐方式  
        title.setAlignment(Element.ALIGN_CENTER);  
        document.add(title);  
        
        Integer count = 0;
    	while (iterator.hasNext()) {
    		count++;
    		
			Entry<String, List> next = iterator.next();
			String projectCode = next.getKey();
			List<String> expCodeList = next.getValue();
			
			// 获取项目信息
			String projectName = CommonAlgorithm.getDataValue(recordComplexus, BaseConstant.TYPE_实验项目, projectCode, ElnprojectCELNE2244Item.基本属性组_名称);
			
			Paragraph context = new Paragraph(count+ ". " + projectName);  
	        context.setAlignment(Element.ALIGN_LEFT);  
	        context.setFirstLineIndent(20);  
	        document.add(context);  
	        
	        //设置Table表格,创建一个三列的表格  
	        Table table = new Table(9);  
	        int width[] = {25,25,25,25,25,25,25,25,25};//设置每列宽度比例  
	        table.setWidths(width);  
	        table.setWidth(90);//占页面宽度比例  
	        table.setAlignment(Element.ALIGN_CENTER);//居中  
	        table.setAlignment(Element.ALIGN_MIDDLE);//垂直居中  
	        table.setAutoFillEmptyCells(true);//自动填满  
	        table.setBorderWidth(1);//边框宽度  
	        //设置表头  
	        table.addCell(new Cell("实验名称"));  
	        table.addCell(new Cell("批号"));  
	      
	        table.addCell(new Cell("含量"));  
	        table.addCell(new Cell("纯度")); 
	        table.addCell(new Cell("单一杂质")); 
	        table.addCell(new Cell("吸光度")); 
	        table.addCell(new Cell("熔点")); 
	        table.addCell(new Cell("重量"));  
	        table.addCell(new Cell("实验总结")); 
			// 获取实验记录的信息
			for (String expCode : expCodeList) {
				String expName = CommonAlgorithm.getDataValue(recordComplexus, BaseConstant.TYPE_实验记录, expCode, ExpRecordCELNE2189Item.基本属性组_名称);
				String piHao = CommonAlgorithm.getDataValue(recordComplexus, BaseConstant.TYPE_实验记录, expCode, ExpRecordCELNE2189Item.基本属性组_批号);
				String zhongliang = CommonAlgorithm.getDataValue(recordComplexus, BaseConstant.TYPE_实验记录, expCode, ExpRecordCELNE2189Item.基本属性组_干品重量);
				// 收量单位
				String shouLiangDanweiStr = CommonAlgorithm.getDataValue(recordComplexus, BaseConstant.TYPE_实验记录, expCode, ExpRecordCELNE2189Item.基本属性组_收量单位);
				String danwei = "";
				if (CommonCalculation.isBasicLawful(shouLiangDanweiStr)) {
					Integer shouLiangDanWei = Integer.parseInt(shouLiangDanweiStr);
					if (EnumKeyValue.ENUM_物料计量单位_克.equals(shouLiangDanWei)) {
						danwei = "g";
					} else if (EnumKeyValue.ENUM_物料计量单位_千克.equals(shouLiangDanWei)) {
						danwei = "kg";
					}  else if (EnumKeyValue.ENUM_物料计量单位_升.equals(shouLiangDanWei)) {
						danwei = "L";
					}  else if (EnumKeyValue.ENUM_物料计量单位_吨.equals(shouLiangDanWei)) {
						danwei = "t";
					} else if (EnumKeyValue.ENUM_物料计量单位_毫升.equals(shouLiangDanWei)) {
						danwei = "ml";
					} 
				}
				
				String zongjie = CommonAlgorithm.getDataValue(recordComplexus, BaseConstant.TYPE_实验记录, expCode, ExpRecordCELNE2189Item.基本属性组_实验总结);
				String hanliang = CommonAlgorithm.getDataValue(recordComplexus, BaseConstant.TYPE_实验记录, expCode, ExpRecordCELNE2189Item.基本属性组_含量);
				String chundu = CommonAlgorithm.getDataValue(recordComplexus, BaseConstant.TYPE_实验记录, expCode, ExpRecordCELNE2189Item.基本属性组_纯度);
				String danyizazhi = CommonAlgorithm.getDataValue(recordComplexus, BaseConstant.TYPE_实验记录, expCode, ExpRecordCELNE2189Item.基本属性组_单一杂质);
				String xiguangdian = CommonAlgorithm.getDataValue(recordComplexus, BaseConstant.TYPE_实验记录, expCode, ExpRecordCELNE2189Item.基本属性组_吸光度);
				String rongdian = CommonAlgorithm.getDataValue(recordComplexus, BaseConstant.TYPE_实验记录, expCode, ExpRecordCELNE2189Item.基本属性组_熔点);
				
				table.addCell(new Cell(expName));  
		        Font fontChinese = new Font(12);  
		        Cell cell = new Cell(new Paragraph(piHao));  
		        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);  
		        table.addCell(cell);  
		        
		        table.addCell(new Cell(hanliang));  
		        table.addCell(new Cell(chundu));  
		        
		        table.addCell(new Cell(danyizazhi));  
		        table.addCell(new Cell(xiguangdian));  
		        table.addCell(new Cell(rongdian));  
		        table.addCell(new Cell(zhongliang+ " " +danwei));  
		        table.addCell(new Cell(zongjie)); 
			}
			
			  // 表格加入文档中
	        document.add(table);
		}
        // 关闭文档
        document.close(); 
	}

	
	       
}
