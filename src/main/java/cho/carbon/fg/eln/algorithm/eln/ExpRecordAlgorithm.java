package cho.carbon.fg.eln.algorithm.eln;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.activemq.command.Command;
import org.apache.commons.lang.StringUtils;

import cho.carbon.complexus.FGRecordComplexus;
import cho.carbon.fg.eln.algorithm.MaterialUnitUtil;
import cho.carbon.fg.eln.algorithm.pojo.MaterialEQR;
import cho.carbon.fg.eln.algorithm.pojo.PutMaterialRatio;
import cho.carbon.fg.eln.common.CommonCalculation;
import cho.carbon.fg.eln.constant.BaseConstant;
import cho.carbon.fg.eln.constant.EnumKeyValue;
import cho.carbon.fg.eln.constant.RelationType;
import cho.carbon.fg.eln.constant.item.ABCBE002Item;
import cho.carbon.fg.eln.constant.item.ExpProcessCELNE3433Item;
import cho.carbon.fg.eln.constant.item.ExpRecordCELNE2189Item;
import cho.carbon.fg.eln.constant.item.ExpSafetyAnalysisCELNE3447Item;
import cho.carbon.fg.eln.constant.item.MateriaInfoCELNE3393Item;
import cho.carbon.fg.eln.constant.item.MaterialRatioCELNE3466Item;
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
import javassist.bytecode.SignatureAttribute.BaseType;

/**
 * 	实验记录规则
 * @author lhb
 *
 */
public class ExpRecordAlgorithm {
	
	
	
	/**
	 * 	计算实体投料的当量比
	 * @param recordComplexus
	 * @param recordCode
	 * @param relationOpsBuilder
	 * @return
	 */
	public static Message computeRealityEQR(FGRecordComplexus recordComplexus, String recordCode, RecordRelationOpsBuilder relationOpsBuilder, List<FGRootRecord> relatedRecordList) {
		try {
			// 获取当前实验记录
			FGRootRecord rootRecord = CommonAlgorithm.getRootRecord(recordComplexus, BaseConstant.TYPE_实验记录, recordCode);
			// 获取实验记录的投料方案
			List<RecordRelation> putMaterialRelaList = (List)CommonAlgorithm.getAppointRecordRelation(recordComplexus, BaseConstant.TYPE_实验记录, recordCode, RelationType.RR_实验记录_物料配比_投料信息);
			if (putMaterialRelaList.isEmpty()) {
				return MessageFactory.buildInfoMessage("Failed", "实验记录", BaseConstant.TYPE_实验记录, "无投料方案");
			}
			
			// 存放主投料方案的code， 只能有一个主物料
			String hostPutMaterialCode = null;
			// 存放次投料方案的code
			List<String> nextPutMaterialCodeList = new ArrayList<String>();
			
			// 主物料个数
			int count = 0;
			// 处理主物料
			for (RecordRelation recordRelation : putMaterialRelaList) {
				// 获取投料方案的code
				String putMaterialCode = recordRelation.getRightCode();
				// 获取投料方案实体
				FGRootRecord putMaterialFG = CommonAlgorithm.getRootRecord(recordComplexus, BaseConstant.TYPE_投料信息, putMaterialCode);
				
				// 获取是否主物料
				String hostMaterialStr = CommonAlgorithm.getDataValue(putMaterialFG, MaterialRatioCELNE3466Item.基本属性组_是否主物料);
				if (CommonCalculation.isBasicLawful(hostMaterialStr)) {
					// count 
					Integer hostMaterial = Integer.parseInt(hostMaterialStr);
					if (EnumKeyValue.ENUM_是否_是.equals(hostMaterial)) {
						count++;
						if (count >1) {
							return MessageFactory.buildWarnMessage("Failed", "实验记录", BaseConstant.TYPE_实验记录, "主物料只能有一个");
						}
						// 把主投料方案的code赋值
						hostPutMaterialCode = putMaterialCode;
					} else {
						nextPutMaterialCodeList.add(putMaterialCode);
					}
				} else {
					nextPutMaterialCodeList.add(putMaterialCode);
				}
			}	
			
			if (CommonCalculation.isNotBasicLawful(hostPutMaterialCode)) {
				return MessageFactory.buildWarnMessage("Failed", "实验记录", BaseConstant.TYPE_实验记录, "缺少主物料");
			}
			
			// 主物料当量比对象
			MaterialEQR hostMaterialEQR = getMaterialEQR(recordComplexus, hostPutMaterialCode);
			
			// 物料计划投料量
			String planQuality = hostMaterialEQR.getPlanQuality();
			BigDecimal planQualityBig = new BigDecimal("0");
			if (CommonCalculation.isBasicLawful(planQuality)) {
				planQualityBig = new BigDecimal(planQuality);
			}
			
			// 物料实际投料量
			String realityQuality = hostMaterialEQR.getRealityQuality();
			BigDecimal realityqualityBig = new BigDecimal("0");
			if (CommonCalculation.isBasicLawful(realityQuality)) {
				realityqualityBig = new BigDecimal(realityQuality);
			}
			
			// 主物料的计划摩尔量
			BigDecimal hostplanMolqQality = null;
			// 主物料的实际摩尔量
			BigDecimal hostRealityMolqQality = null;
			// 物料的摩尔质量
			String molqQality = hostMaterialEQR.getMolqQality();
			BigDecimal molqQalityBig = new BigDecimal("0");
			if (CommonCalculation.isBasicLawful(molqQality)) {
				molqQalityBig = new BigDecimal(molqQality);
				hostplanMolqQality = planQualityBig.divide(molqQalityBig, 4).setScale(4, BigDecimal.ROUND_HALF_UP);
				hostRealityMolqQality = realityqualityBig.divide(molqQalityBig, 4).setScale(4, BigDecimal.ROUND_HALF_UP);
			} else {
				return MessageFactory.buildWarnMessage("Failed", "警告", BaseConstant.TYPE_实验记录, "请确认物料的摩尔质量or密度已填入");
			}
			hostMaterialEQR.setPlanMolarWeight(hostplanMolqQality);
			hostMaterialEQR.setRealityMolarWeight(hostRealityMolqQality);
			
			// 更新主物料 的摩尔比、质量比、体积比
			FGRootRecordBuilder builder = FGRootRecordBuilder.getInstance(BaseConstant.TYPE_投料信息, hostPutMaterialCode);
			//设置记录属性。第一个参数为模型属性的编码，第二个参数为模型属性的取值
			builder.putAttribute(MaterialRatioCELNE3466Item.基本属性组_实际摩尔比, 1);
			builder.putAttribute(MaterialRatioCELNE3466Item.基本属性组_实际当量比, 1);
			builder.putAttribute(MaterialRatioCELNE3466Item.基本属性组_实际摩尔量, hostRealityMolqQality);
			
			builder.putAttribute(MaterialRatioCELNE3466Item.基本属性组_计划摩尔比, 1);
			builder.putAttribute(MaterialRatioCELNE3466Item.基本属性组_计划当量比, 1);
			builder.putAttribute(MaterialRatioCELNE3466Item.基本属性组_计划摩尔量, hostplanMolqQality);
			//放入到kie预设的全局变量中
			relatedRecordList.add(builder.getRootRecord());	
			try {
//				计算次物料的  当量比、 摩尔量、摩尔比
				for (String nextPutMaterialCode : nextPutMaterialCodeList) {
					computeEQR(recordComplexus, hostMaterialEQR, nextPutMaterialCode, relatedRecordList);
				}
			} catch (Exception e) {
				return MessageFactory.buildWarnMessage("Failed", "警告", BaseConstant.TYPE_实验记录, "请确认物料的摩尔质量or密度已填入");
			}

			
		} catch (Exception e) {
			e.printStackTrace();
			return MessageFactory.buildWarnMessage("Failed", "警告", BaseConstant.TYPE_实验记录, "请确认物料的摩尔质量or密度已填入");
		}
		return MessageFactory.buildInfoMessage("Succeeded", "成功", BaseConstant.TYPE_实验记录, "计算当量比成功");
	} 
	
	
	/**
	 * 根据投料方案code， 封装当量比对象
	 * @param recordComplexus
	 * @param putMaterialCode   投料code
	 * @return
	 */
	public static MaterialEQR getMaterialEQR(FGRecordComplexus recordComplexus, String putMaterialCode) {
		// 摩尔质量
		String molqQality = null;
		// 密度
		String density = null;
		
		// 获取投料方案实体
		FGRootRecord putMaterialFG = CommonAlgorithm.getRootRecord(recordComplexus, BaseConstant.TYPE_投料信息, putMaterialCode);
		
		// 获取是否-计算当量比
		String calculateEQRStr = CommonAlgorithm.getDataValue(putMaterialFG, MaterialRatioCELNE3466Item.基本属性组_计算当量比);
		// 计算当量比的枚举
		Integer calculateEQRENUM = null;
		if (CommonCalculation.isBasicLawful(calculateEQRStr)) {
			calculateEQRENUM = Integer.parseInt(calculateEQRStr);
		}
		
		
		// 获取物料计划投料量
		String planQualityStr = CommonAlgorithm.getDataValue(putMaterialFG, MaterialRatioCELNE3466Item.基本属性组_计划投料量);
		// 获取物料实际投料量
		String realityQualityStr = CommonAlgorithm.getDataValue(putMaterialFG, MaterialRatioCELNE3466Item.基本属性组_实际投料量);
		// 获取物料的code
		String materiaCode = null;
		List<RecordRelation> materialRelaList = (List)CommonAlgorithm.getAppointRecordRelation(recordComplexus, BaseConstant.TYPE_投料信息, putMaterialCode, RelationType.RR_投料信息_物料信息_物料基础信息);
		if (!materialRelaList.isEmpty()) {
			materiaCode = materialRelaList.get(0).getRightCode();
		}
		if (CommonCalculation.isBasicLawful(materiaCode)) {
			molqQality = CommonAlgorithm.getDataValue(recordComplexus, BaseConstant.TYPE_物料基础信息, materiaCode, MateriaInfoCELNE3393Item.基本属性组_摩尔质量_g);
			density = CommonAlgorithm.getDataValue(recordComplexus, BaseConstant.TYPE_物料基础信息, materiaCode, MateriaInfoCELNE3393Item.基本属性组_密度_g);
		}
		MaterialEQR materialEQR = 	new MaterialEQR(putMaterialCode, materiaCode , planQualityStr,realityQualityStr, molqQality, density, calculateEQRENUM);
		return materialEQR;
	}
	
	
	/**
	 * 	计算实际当量比
	 * @param recordComplexus
	 * @param hostPutMaterialCode
	 * @param nextPutMaterialCode
	 * @return
	 */
	public static Message computeEQR(FGRecordComplexus recordComplexus, MaterialEQR hostMaterialEQR, String nextPutMaterialCode, List<FGRootRecord> relatedRecordList) {
		try {
			
			// 主物料的计划摩尔量
			BigDecimal hostPlanMolarWeight = hostMaterialEQR.getPlanMolarWeight();
			// 主物料的实际摩尔量
			BigDecimal hostRealityMolarWeight = hostMaterialEQR.getRealityMolarWeight();
			
			// 主物料计划投料量
			String hostPlanQualityStr = hostMaterialEQR.getPlanQuality();
			BigDecimal hostPlanQualityBig = new BigDecimal("0");
			if (CommonCalculation.isBasicLawful(hostPlanQualityStr)) {
				hostPlanQualityBig = new BigDecimal(hostPlanQualityStr);
			}
			
			// 主物料实际投料量
			String hostRealityQualityStr = hostMaterialEQR.getRealityQuality();
			BigDecimal hostRealityQualityBig = new BigDecimal("0");
			if (CommonCalculation.isBasicLawful(hostRealityQualityStr)) {
				hostRealityQualityBig = new BigDecimal(hostRealityQualityStr);
			}
			
			// 获取次物料的当量比数据
			MaterialEQR nextMaterialEQR = getMaterialEQR(recordComplexus, nextPutMaterialCode);

			Integer calculateEQRENUM = nextMaterialEQR.getCalculateEQRENUM();
			
			//	计算此物料的  当量比、 摩尔量、摩尔比
			
			// 次物料计划投料量
			String nextPlanQualityStr = nextMaterialEQR.getRealityQuality();
			BigDecimal nextPlanQualityBig = new BigDecimal("0");
			if (CommonCalculation.isBasicLawful(nextPlanQualityStr)) {
				nextPlanQualityBig = new BigDecimal(nextPlanQualityStr);
			}
			// 次物料实际投料量
			String nextRealityQualityStr = nextMaterialEQR.getRealityQuality();
			BigDecimal nextRealityQualityBig = new BigDecimal("0");
			if (CommonCalculation.isBasicLawful(nextRealityQualityStr)) {
				nextRealityQualityBig = new BigDecimal(nextRealityQualityStr);
			}
			
			// 次物料实际摩尔量
			BigDecimal nextPlanMolqQality = null;
			// 次物料实际摩尔量
			BigDecimal nextRealityMolqQality = null;
			// 物料的摩尔质量
			String nextMolqQalityStr = nextMaterialEQR.getMolqQality();
			BigDecimal nextMolqQalityBig = new BigDecimal("0");
			if (CommonCalculation.isBasicLawful(nextMolqQalityStr)) {
				nextMolqQalityBig = new BigDecimal(nextMolqQalityStr);
				nextPlanMolqQality = nextPlanQualityBig.divide(nextMolqQalityBig, 4).setScale(4, BigDecimal.ROUND_HALF_UP);
				nextRealityMolqQality = nextRealityQualityBig.divide(nextMolqQalityBig, 4).setScale(4, BigDecimal.ROUND_HALF_UP);
			} 
			
			nextMaterialEQR.setPlanMolarWeight(nextPlanMolqQality);
			nextMaterialEQR.setRealityMolarWeight(nextRealityMolqQality);
			// 计算摩尔比  摩尔比： 物料的摩尔量/主物料的摩尔量     
			BigDecimal nextPlanMolarRatio = nextPlanMolqQality.divide(hostPlanMolarWeight,4);
			BigDecimal nextRealityMolarRatio = nextRealityMolqQality.divide(hostRealityMolarWeight,4);
			
			// 计划当量比
			BigDecimal planEQR = new BigDecimal("0");
			// 实际当量比
			BigDecimal realityEQR = new BigDecimal("0");
			// 计算当量比
			if (EnumKeyValue.ENUM_实验记录当量比_质量比.equals(calculateEQRENUM)) {
				if (new BigDecimal("0").compareTo(hostRealityQualityBig) == 0) {
					
				} else {
					planEQR = nextPlanQualityBig.divide(hostPlanQualityBig,4).setScale(4, BigDecimal.ROUND_HALF_UP);
					realityEQR = nextRealityQualityBig.divide(hostRealityQualityBig,4).setScale(4, BigDecimal.ROUND_HALF_UP);
				}
			} else if (EnumKeyValue.ENUM_实验记录当量比_体积比.equals(calculateEQRENUM)) {
				// 计算体积比
				
//				计算主物料体积
				String hostDensityStr = hostMaterialEQR.getDensity();
				BigDecimal hostDensity =  new BigDecimal("0");
				if (CommonCalculation.isBasicLawful(hostDensityStr)) {
					hostDensity = new BigDecimal(hostDensityStr);
				}
				
				BigDecimal planHostV = hostPlanQualityBig.divide(hostDensity,4).setScale(4, BigDecimal.ROUND_HALF_UP);
				BigDecimal realityHostV = hostRealityQualityBig.divide(hostDensity,4).setScale(4, BigDecimal.ROUND_HALF_UP);
				
				// 计算此物料体积
				String nextdDnsityStr = nextMaterialEQR.getDensity();
				BigDecimal nextdDnsity =  new BigDecimal("0");
				if (CommonCalculation.isBasicLawful(nextdDnsityStr)) {
					nextdDnsity = new BigDecimal(nextdDnsityStr);
				}
				BigDecimal planNextV = nextPlanQualityBig.divide(nextdDnsity,4).setScale(4, BigDecimal.ROUND_HALF_UP);
				BigDecimal realityNextV = nextRealityQualityBig.divide(nextdDnsity,4).setScale(4, BigDecimal.ROUND_HALF_UP);
				
				planEQR = planNextV.divide(planHostV,4).setScale(4, BigDecimal.ROUND_HALF_UP);
				realityEQR = realityNextV.divide(realityHostV,4).setScale(4, BigDecimal.ROUND_HALF_UP);
			} else {
				nextRealityMolarRatio = null;
				realityEQR = null;
				nextRealityMolqQality = null;
				
				nextPlanMolarRatio = null;
				planEQR = null;
				nextPlanMolqQality = null;
			}
			
			
			// 更新主物料 的摩尔比、质量比、体积比
			FGRootRecordBuilder builder = FGRootRecordBuilder.getInstance(BaseConstant.TYPE_投料信息, nextPutMaterialCode);
			//设置记录属性。第一个参数为模型属性的编码，第二个参数为模型属性的取值
			builder.putAttribute(MaterialRatioCELNE3466Item.基本属性组_实际摩尔比, nextRealityMolarRatio);
			builder.putAttribute(MaterialRatioCELNE3466Item.基本属性组_实际当量比, realityEQR);
			builder.putAttribute(MaterialRatioCELNE3466Item.基本属性组_实际摩尔量, nextRealityMolqQality);
			
			builder.putAttribute(MaterialRatioCELNE3466Item.基本属性组_计划摩尔比, nextPlanMolarRatio);
			builder.putAttribute(MaterialRatioCELNE3466Item.基本属性组_计划当量比, planEQR);
			builder.putAttribute(MaterialRatioCELNE3466Item.基本属性组_计划摩尔量, nextPlanMolqQality);
			
			//放入到kie预设的全局变量中
			relatedRecordList.add(builder.getRootRecord());	
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("请确认物料的摩尔质量or密度已填入");
		}
		return MessageFactory.buildInfoMessage("Succeeded", "计成功", BaseConstant.TYPE_实验记录, "成功");
	}
	

	/**
	 *	 计算投料总量
	 * @param recordComplexus
	 * @param recordCode
	 * @param recordOpsBuilder
	 * @return
	 */
	public static Message computeMaterialGross(FGRecordComplexus recordComplexus, String recordCode, FGRecordOpsBuilder recordOpsBuilder, List<FGRootRecord> relatedRecordList, List<RecordRelationOpsBuilder>  relatedRelationOpsBuilderList) {
		try {
			// 获取当前实验记录
			FGRootRecord rootRecord = CommonAlgorithm.getRootRecord(recordComplexus, BaseConstant.TYPE_实验记录, recordCode);
			// 获取实验记录的操作过程
			List<RecordRelation> expProcessRelaList = (List)CommonAlgorithm.getAppointRecordRelation(recordComplexus, BaseConstant.TYPE_实验记录, recordCode, RelationType.RR_实验记录_实验操作过程_实验操作过程);
			if (expProcessRelaList.isEmpty()) {
				return MessageFactory.buildInfoMessage("computeMaterialGrossSucceeded", "没有计算投料总量", BaseConstant.TYPE_实验记录, "没有计算投料总量");
			}
			// 获取当前实验记录对应的操作过程中  所有投料信息
			List<PutMaterialRatio> putMaterialRatioList = new ArrayList<PutMaterialRatio>();
			
			for (RecordRelation expProcessRela : expProcessRelaList) {
				// 获取实验操作过程的唯一code
				String expProcessCode = expProcessRela.getRightCode();
				// 获取实验过程操作时间
				String expProcessTime = CommonAlgorithm.getDataValue(recordComplexus,  BaseConstant.TYPE_实验操作过程, expProcessCode, ExpProcessCELNE3433Item.基本属性组_时间);
				
				// 根据操作过程唯一code， 获取操作过程对应的投料信息 关系
				List<RecordRelation> putMaterialRatioRelaList = (List)CommonAlgorithm.getAppointRecordRelation(recordComplexus, BaseConstant.TYPE_实验操作过程, expProcessCode, RelationType.RR_实验操作过程_投料信息_投料信息);
				if (putMaterialRatioRelaList.isEmpty()) {
					// 没有投料结束当前循环
					continue;
				}
				
				for (RecordRelation putMaterialRatioRela : putMaterialRatioRelaList) {
					// 获取投料信息唯一编码
					String putMaterialCode = putMaterialRatioRela.getRightCode();
					
					// 获取投料信息对应的物料信息
					List<RecordRelation> materialRelaList = (List)CommonAlgorithm.getAppointRecordRelation(recordComplexus, BaseConstant.TYPE_投料信息, putMaterialCode, RelationType.RR_投料信息_物料信息_物料基础信息);
					if (materialRelaList.isEmpty()) {
						// 没有物料结束当前循环
						continue;
					}
					// 获取物料code
					String materialCode = materialRelaList.get(0).getRightCode();
					String materialName = CommonAlgorithm.getDataValue(recordComplexus, BaseConstant.TYPE_物料基础信息, materialCode, MateriaInfoCELNE3393Item.基本属性组_物料名称);
					
					//  获取投料实体
					FGRootRecord putMaterial = CommonAlgorithm.getRootRecord(recordComplexus, BaseConstant.TYPE_投料信息, putMaterialCode);
					
//					String planAmount = CommonAlgorithm.getDataValue(putMaterial, MaterialRatioCELNE3466Item.基本属性组_计划投料量);
					String actualAmountStr = CommonAlgorithm.getDataValue(putMaterial, MaterialRatioCELNE3466Item.基本属性组_实际投料量);
					BigDecimal actualAmount = new BigDecimal("0");
					if (CommonCalculation.isBasicLawful(actualAmountStr)) {
						actualAmount = new BigDecimal(actualAmountStr);
					}
					
					String putMateriaUnitStr = CommonAlgorithm.getDataValue(putMaterial, MaterialRatioCELNE3466Item.基本属性组_投料量单位);
					Integer putMateriaUnit = null;
					if (CommonCalculation.isBasicLawful(putMateriaUnitStr)) {
						putMateriaUnit = Integer.parseInt(putMateriaUnitStr);
					}
					// 投料量 ， g为单位
					BigDecimal convertUnitg = null;
					try {
						// 把实际投料量 转换为  g， 来进行保存
						convertUnitg = MaterialUnitUtil.convertUnitg(recordComplexus, putMateriaUnit, actualAmount, materialCode);
					} catch (Exception e) {
						return MessageFactory.buildRefuseMessage("Failed", "计失败", BaseConstant.TYPE_实验记录, "物料【"+materialName+"】密度没有填写");
					}
					
					PutMaterialRatio putMaterialRatio = new PutMaterialRatio(putMaterialCode, materialCode,  convertUnitg, EnumKeyValue.ENUM_物料计量单位_克, expProcessTime);
					putMaterialRatio.setMaterialName(materialName);
					
					putMaterialRatioList.add(putMaterialRatio);
				}
			}
			
			// 汇总物料实际投入量
			Map<String, PutMaterialRatio> map = new HashMap<String, PutMaterialRatio>();
			// 循环所有的投料信息， 根据物资唯一Code进行分类
			for (PutMaterialRatio putMaterialRatio : putMaterialRatioList) {
				String materiaCode = putMaterialRatio.getMateriaCode();
				
				PutMaterialRatio pm = map.get(materiaCode);
				if (pm == null) {
					pm = putMaterialRatio;
				} else {
					// 没有 计算实际投料量总和
					BigDecimal actualAmount = putMaterialRatio.getActualAmount();
//					String putMateriaUnit = putMaterialRatio.getPutMateriaUnit();
					BigDecimal actualAmountSum = pm.getActualAmount();
//					String putMateriaUnit2 = pm.getPutMateriaUnit();
					
//					if (!putMateriaUnit.equals(putMateriaUnit2)) {
//						return MessageFactory.buildRefuseMessage("computeMaterialGrossFailed", "计算投料总量失败-", BaseConstant.TYPE_实验记录, "实验记录对应操作过程中投料信息相同物料【"+pm.getMaterialName()+"】单位必须一致");
//					}
					
					BigDecimal sum = actualAmountSum.add(actualAmount);
					pm.setActualAmount(sum);
				}
				map.put(materiaCode, pm);
			}
			
			// 根据实验记录， 获取投料方案
			List<RecordRelation> materialRatioRelaList = (List)CommonAlgorithm.getAppointRecordRelation(recordComplexus, BaseConstant.TYPE_实验记录, recordCode, RelationType.RR_实验记录_物料配比_投料信息);
//			if (materialRatioRelaList.isEmpty()) {
//				// 生成所有方案
//			}
			
			// 有方案的补充方案， 没有方案的新增方案
			for (RecordRelation putMaterialRatioRela : materialRatioRelaList) {
				// 获取投料信息Code
				String putMaterialCode = putMaterialRatioRela.getRightCode();
				
				// 获取投料信息对应的物料信息
				List<RecordRelation> materialRelaList = (List)CommonAlgorithm.getAppointRecordRelation(recordComplexus, BaseConstant.TYPE_投料信息, putMaterialCode, RelationType.RR_投料信息_物料信息_物料基础信息);
				if (materialRelaList.isEmpty()) {
					// 没有物料结束当前循环
					return MessageFactory.buildInfoMessage("computeMaterialGrossSucceeded", "投料方案", BaseConstant.TYPE_实验记录, "没有物料基础信息");
				}
				// 获取物料唯一编码
				String materialCode = materialRelaList.get(0).getRightCode();
				
				// 获取实际投料信息
				PutMaterialRatio putMaterialRatio = map.get(materialCode);
				if (putMaterialRatio != null) {
					map.remove(materialCode);
					
					// 实际投料量
					BigDecimal actualAmount = putMaterialRatio.getActualAmount().setScale(4, BigDecimal.ROUND_HALF_UP);
					FGRootRecordBuilder builder =FGRootRecordBuilder.getInstance(BaseConstant.TYPE_投料信息,putMaterialCode);
					builder.putAttribute(MaterialRatioCELNE3466Item.基本属性组_实际投料量, actualAmount);
					
					relatedRecordList.add(builder.getRootRecord());
				} else {
					// 实际投料量 设置为0
					FGRootRecordBuilder builder =FGRootRecordBuilder.getInstance(BaseConstant.TYPE_投料信息,putMaterialCode);
					builder.putAttribute(MaterialRatioCELNE3466Item.基本属性组_实际投料量, 0);
					
					relatedRecordList.add(builder.getRootRecord());
				}
			}
			
			// 遍历map， 生成新的投料方案
	        Iterator<Entry<String, PutMaterialRatio>> it = map.entrySet().iterator();
	        while (it.hasNext()) {
	            Entry<String, PutMaterialRatio> entry = it.next();
	            PutMaterialRatio pm = entry.getValue();
	            
	            // 生成新的实体， 并和当前实验记录 关联关系 - 投料方案关系
	            
				String longUID = UidManager.getLongUID() + "";
				//先得到一个FGRootRecordBuilder对象，用于辅助构建记录，第一个参数是模型类型名称，第二个参数是待构建记录的编码
				FGRootRecordBuilder materialRatioBuilder =FGRootRecordBuilder.getInstance(BaseConstant.TYPE_投料信息,longUID);
				//设置记录属性，第一个参数为模型属性的编码，第二个参数为模型属性的取值
				materialRatioBuilder.putAttribute(MaterialRatioCELNE3466Item.基本属性组_实际投料量, pm.getActualAmount());
				materialRatioBuilder.putAttribute(MaterialRatioCELNE3466Item.基本属性组_投料量单位, pm.getPutMateriaUnit());
				//得到记录对象
				FGRootRecord materialRatioRecord = materialRatioBuilder.getRootRecord();
				relatedRecordList.add(materialRatioRecord);
				
				RecordRelationOpsBuilder putMaterialRelaOpsBuilder = RecordRelationOpsBuilder.getInstance(BaseConstant.TYPE_投料信息, longUID);
				// 
				putMaterialRelaOpsBuilder.putRelation(RelationType.RR_投料信息_实验记录_实验记录, recordCode);
				putMaterialRelaOpsBuilder.putRelation(RelationType.RR_投料信息_物料信息_物料基础信息, pm.getMateriaCode());
	            
				relatedRelationOpsBuilderList.add(putMaterialRelaOpsBuilder);
	        }
		} catch (Exception e) {
			e.printStackTrace();
			return MessageFactory.buildRefuseMessage("computeMaterialGrossFailed", "计算投料总量失败", BaseConstant.TYPE_实验记录, "计算投料总量失败");
		}
		return MessageFactory.buildInfoMessage("computeMaterialGrossSucceeded", "计算投料总量成功", BaseConstant.TYPE_实验记录, "计算投料总量成功");
	}
	
	
	/**
	 *	 实验记录增加审核人
	 * @param recordComplexus
	 * @param recordCode
	 * @param recordOpsBuilder
	 * @return
	 */
	public static Message addVerifier(FGRecordComplexus recordComplexus, String recordCode, RecordRelationOpsBuilder relationOpsBuilder) {
		try {
			// 获取当前实验记录
			FGRootRecord rootRecord = CommonAlgorithm.getRootRecord(recordComplexus, BaseConstant.TYPE_实验记录, recordCode);
			// 获取实验记录的实验项目
			List<RecordRelation> expProcessRelaList = (List)CommonAlgorithm.getAppointRecordRelation(recordComplexus, BaseConstant.TYPE_实验记录, recordCode, RelationType.RR_实验记录_关联项目_实验项目);
			if (expProcessRelaList.isEmpty() || expProcessRelaList.size() != 1) {
				return MessageFactory.buildRefuseMessage("Failed", "实验记录", BaseConstant.TYPE_实验记录, "关联实验项目不唯一");
			}
			// 获取实验项目的code
			String expProjectCode = expProcessRelaList.get(0).getRightCode();
			List<RecordRelation> groupLeaderRela = (List)CommonAlgorithm.getAppointRecordRelation(recordComplexus, BaseConstant.TYPE_实验项目, expProjectCode, RelationType.RR_实验项目_组长_系统用户);
			if (groupLeaderRela.isEmpty() || groupLeaderRela.size() != 1) {
				return MessageFactory.buildRefuseMessage("Failed", "实验记录", BaseConstant.TYPE_实验记录, "实验项目的组长不唯一");
			}
			// 获取组长唯一编码
			String groupLeaderCode = groupLeaderRela.get(0).getRightCode();
			
			// 获取当前当前实验记录的审核员
			List<RecordRelation> verifierRela = (List)CommonAlgorithm.getAppointRecordRelation(recordComplexus, BaseConstant.TYPE_实验记录, recordCode, RelationType.RR_实验记录_审核员_系统用户);
			if (verifierRela == null || verifierRela.isEmpty() || verifierRela.size()==0) {
				// 直接增加审核员
				relationOpsBuilder.putRelation(RelationType.RR_实验记录_审核员_系统用户, groupLeaderCode);
			} else {
				// 有一个值
				String verifierCode = verifierRela.get(0).getRightCode();
				if (!verifierCode.equals(groupLeaderCode)) {
					// 更换新的审核员
					relationOpsBuilder.putRelation(RelationType.RR_实验记录_审核员_系统用户, groupLeaderCode);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return MessageFactory.buildRefuseMessage("computeMaterialGrossFailed", "计算投料总量失败", BaseConstant.TYPE_实验记录, "计算投料总量失败");
		}
		return MessageFactory.buildInfoMessage("computeMaterialGrossSucceeded", "计算投料总量成功", BaseConstant.TYPE_实验记录, "计算投料总量成功");
	}
	
	/**
	 *	 设置实验记录中的实验员名称
	 * @param recordComplexus
	 * @param recordCode
	 * @param relationOpsBuilder
	 * @return
	 */
	public static Message setLaboratoryName(FGRecordComplexus recordComplexus, String recordCode, FGRecordOpsBuilder recordOpsBuilder) {
		try {
			// 获取当前实验记录
			FGRootRecord rootRecord = CommonAlgorithm.getRootRecord(recordComplexus, BaseConstant.TYPE_实验记录, recordCode);
			// 获取实验记录的实验员
			List<RecordRelation> laboratoryList = (List)CommonAlgorithm.getAppointRecordRelation(recordComplexus, BaseConstant.TYPE_实验记录, recordCode, RelationType.RR_实验记录_实验员_系统用户);
			String laboratoryNameStr = "";
			for (RecordRelation recordRelation : laboratoryList) {
				
				String laboratoryCode = recordRelation.getRightCode();
				
				String name = CommonAlgorithm.getDataValue(recordComplexus, BaseConstant.TYPE_系统用户, laboratoryCode, ABCBE002Item.基本信息_实名);
				
				if (StringUtils.isBlank(name)) {
					name = CommonAlgorithm.getDataValue(recordComplexus, BaseConstant.TYPE_系统用户, laboratoryCode, ABCBE002Item.基本信息_用户名);
				}
				
				laboratoryNameStr = laboratoryNameStr + name;
			}
			
			FGAttribute attr2=FuseAttributeFactory.buildAttribute(ExpRecordCELNE2189Item.基本属性组_实验员名称, laboratoryNameStr);
			recordOpsBuilder.addUpdateAttr(attr2); 
		} catch (Exception e) {
			e.printStackTrace();
			return MessageFactory.buildRefuseMessage("Failed", "设置实验记录实验员名称失败", BaseConstant.TYPE_实验记录, "");
		}
		return MessageFactory.buildInfoMessage("Succeeded", "设置实验记录实验员名称成功", BaseConstant.TYPE_实验记录, "");
	}
	
	/**
	 * 复制实验记录
	 * @param recordComplexus
	 * @param recordCode
	 * @param recordOpsBuilder
	 * @return
	 */
	public static Message copyExpRecord(FGRecordComplexus recordComplexus, String recordCode, FGRecordOpsBuilder recordOpsBuilder, List<FGRootRecord> relatedRecordList, List<RecordRelationOpsBuilder>  relatedRelationOpsBuilderList) {
		try {
			// 获取当前实验记录
			FGRootRecord rootRecord = CommonAlgorithm.getRootRecord(recordComplexus, BaseConstant.TYPE_实验记录, recordCode);
			// 对实验记录进行复制
			
			String expRecordNameOld = CommonAlgorithm.getDataValue(rootRecord, ExpRecordCELNE2189Item.基本属性组_名称);
			String expRecordTarget = CommonAlgorithm.getDataValue(rootRecord, ExpRecordCELNE2189Item.基本属性组_实验目的);
			
			String fileFN = CommonAlgorithm.getDataValue(rootRecord, ExpRecordCELNE2189Item.基本属性组_化学结构式+"_fn");
			String fileFK = CommonAlgorithm.getDataValue(rootRecord, ExpRecordCELNE2189Item.基本属性组_化学结构式+"_fk");
			String fileFSF = CommonAlgorithm.getDataValue(rootRecord, ExpRecordCELNE2189Item.基本属性组_化学结构式+"_fsf");
			
			// 生成一条新的实验记录
			String expRecordCode = UidManager.getLongUID() + "";
			FGRootRecordBuilder builder =FGRootRecordBuilder.getInstance(BaseConstant.TYPE_实验记录,expRecordCode);
			//设置记录属性，第一个参数为模型属性的编码，第二个参数为模型属性的取值
			builder.putAttribute(ExpRecordCELNE2189Item.基本属性组_名称, "复制自【" + expRecordNameOld+"】");
			builder.putAttribute(ExpRecordCELNE2189Item.基本属性组_实验目的, expRecordTarget);
			builder.putAttribute(ExpRecordCELNE2189Item.基本属性组_化学结构式+"_fn", fileFN);
			builder.putAttribute(ExpRecordCELNE2189Item.基本属性组_化学结构式+"_fk", fileFK);
			builder.putAttribute(ExpRecordCELNE2189Item.基本属性组_化学结构式+"_fsf", fileFSF);
//			builder.putAttribute("实验日期", new Date());
			//融合实验记录对象
			relatedRecordList.add(builder.getRootRecord());
			
			// 构件新实验记录的关系
			RecordRelationOpsBuilder expRecordOpsBuilder = RecordRelationOpsBuilder.getInstance(BaseConstant.TYPE_实验记录, expRecordCode);
			
			// 查询出实验记录关联的项目， 并和新的实验记录进行关联
			List<RecordRelation> projectRelaList = (List)CommonAlgorithm.getAppointRecordRelation(recordComplexus, BaseConstant.TYPE_实验记录, recordCode, RelationType.RR_实验记录_关联项目_实验项目);
			
			if (!projectRelaList.isEmpty()) {
				// 获取到项目code
				String projectCode = projectRelaList.get(0).getRightCode();
				// 实验记录和项目进行关系关联
				expRecordOpsBuilder.putRelation(RelationType.RR_实验记录_关联项目_实验项目, projectCode);
			}
			// 给新的实验记录增加安全分析
			// 查询出当前实验记录的所有实验分析数据
			List<RecordRelation> expSafetyAnalysisList = (List<RecordRelation>)CommonAlgorithm.getAppointRecordRelation(recordComplexus, BaseConstant.TYPE_实验记录, recordCode, RelationType.RR_实验记录_实验安全分析_实验安全分析);
			for (RecordRelation expSafetyAnalyRela : expSafetyAnalysisList) {
				// 获取到实验安全分析的code
				String expSafetyAnalyCodeOld = expSafetyAnalyRela.getRightCode();
				
				String hazardAnaylsisOld = CommonAlgorithm.getDataValue(recordComplexus, BaseConstant.TYPE_实验安全分析, expSafetyAnalyCodeOld, ExpSafetyAnalysisCELNE3447Item.基本属性组_危险性分析);
				String harmOld = CommonAlgorithm.getDataValue(recordComplexus, BaseConstant.TYPE_实验安全分析, expSafetyAnalyCodeOld, ExpSafetyAnalysisCELNE3447Item.基本属性组_生产的危害);
				String measureOld = CommonAlgorithm.getDataValue(recordComplexus, BaseConstant.TYPE_实验安全分析, expSafetyAnalyCodeOld, ExpSafetyAnalysisCELNE3447Item.基本属性组_采取的措施);
				
				String expSafetyAnalyCode = UidManager.getLongUID() + "";
				FGRootRecordBuilder expSafetyAnalyBuilder =FGRootRecordBuilder.getInstance(BaseConstant.TYPE_实验安全分析,expSafetyAnalyCode);
				//设置记录属性，第一个参数为模型属性的编码，第二个参数为模型属性的取值
				expSafetyAnalyBuilder.putAttribute(ExpSafetyAnalysisCELNE3447Item.基本属性组_采取的措施, measureOld);
				expSafetyAnalyBuilder.putAttribute(ExpSafetyAnalysisCELNE3447Item.基本属性组_生产的危害, harmOld);
				expSafetyAnalyBuilder.putAttribute(ExpSafetyAnalysisCELNE3447Item.基本属性组_危险性分析, hazardAnaylsisOld);
				//融合实验记录对象
				relatedRecordList.add(expSafetyAnalyBuilder.getRootRecord());
				// 新的安全分析， 需要和新的实验记录建立关系
				expRecordOpsBuilder.putRelation(RelationType.RR_实验记录_实验安全分析_实验安全分析, expSafetyAnalyCode);
			}
			
			// 查询出实验记录所有的投料方案
//			List<RecordRelation> putMateriaSchemeList = (List<RecordRelation>)CommonAlgorithm.getAppointRecordRelation(recordComplexus, BaseConstant.TYPE_实验记录, recordCode, RelationType.RR_实验记录_物料配比_投料信息);
//			for (RecordRelation putMateriaSchemeRela : putMateriaSchemeList) {
//				// 获取投料方案code
//				String putMateriaSchemeCodeOld = putMateriaSchemeRela.getRightCode();
//				// 获取摩尔比
//				String molarRatio = CommonAlgorithm.getDataValue(recordComplexus, BaseConstant.TYPE_投料信息, putMateriaSchemeCodeOld, MaterialRatioCELNE3466Item.基本属性组_摩尔比);
//				// 获取投料单位
//				String putMateriaSchemeUnit = CommonAlgorithm.getDataValue(recordComplexus, BaseConstant.TYPE_投料信息, putMateriaSchemeCodeOld, MaterialRatioCELNE3466Item.基本属性组_投料量单位);
//				// 获取计划投料量
//				String putMateriaSchemePlan = CommonAlgorithm.getDataValue(recordComplexus, BaseConstant.TYPE_投料信息, putMateriaSchemeCodeOld, MaterialRatioCELNE3466Item.基本属性组_计划投料量);
//				// 获取投料实体类型
//				String putMateriaSchemeEntityType = CommonAlgorithm.getDataValue(recordComplexus, BaseConstant.TYPE_投料信息, putMateriaSchemeCodeOld, MaterialRatioCELNE3466Item.基本属性组_投料实体类型);
//				
//				// 获取投料信息的物料
//				List<RecordRelation> materiaSchemeList = (List<RecordRelation>)CommonAlgorithm.getAppointRecordRelation(recordComplexus, BaseConstant.TYPE_投料信息, putMateriaSchemeCodeOld, RelationType.RR_投料信息_物料信息_物料基础信息);
//				// 物料的code， 
//				String materiaInfoSchemeCode = materiaSchemeList.get(0).getRightCode();
//				
//				// 构件新的投料方案， 并和实验记录关联
//				String putMateriaSchemeCode = UidManager.getLongUID() + "";
//				FGRootRecordBuilder putMateriaSchemeBuilder =FGRootRecordBuilder.getInstance(BaseConstant.TYPE_投料信息,putMateriaSchemeCode);
//				//设置记录属性，第一个参数为模型属性的编码，第二个参数为模型属性的取值
//				putMateriaSchemeBuilder.putAttribute(MaterialRatioCELNE3466Item.基本属性组_摩尔比, molarRatio);
//				putMateriaSchemeBuilder.putAttribute(MaterialRatioCELNE3466Item.基本属性组_投料量单位, putMateriaSchemeUnit);
//				putMateriaSchemeBuilder.putAttribute(MaterialRatioCELNE3466Item.基本属性组_计划投料量, putMateriaSchemePlan);
//				putMateriaSchemeBuilder.putAttribute(MaterialRatioCELNE3466Item.基本属性组_投料实体类型, putMateriaSchemeEntityType);
//				
//				//融合投料方案信息
//				relatedRecordList.add(putMateriaSchemeBuilder.getRootRecord());
//				// 构件新投料方案的关系
//				RecordRelationOpsBuilder putMateriaRelaSchemeBuilder = RecordRelationOpsBuilder.getInstance(BaseConstant.TYPE_投料信息, putMateriaSchemeCode);
//				// 建立投料信息和物料基础信息的关系
//				putMateriaRelaSchemeBuilder.putRelation(RelationType.RR_投料信息_物料信息_物料基础信息, materiaInfoSchemeCode);
//				// 融合投料方案关系信息
//				relatedRelationOpsBuilderList.add(putMateriaRelaSchemeBuilder);
//				
//				expRecordOpsBuilder.putRelation(RelationType.RR_实验记录_物料配比_投料信息, putMateriaSchemeCode);
//			}
			
			// 查询当前实验记录的实验过程
			List<RecordRelation> expProcessList = (List<RecordRelation>)CommonAlgorithm.getAppointRecordRelation(recordComplexus, BaseConstant.TYPE_实验记录, recordCode, RelationType.RR_实验记录_实验操作过程_实验操作过程);
			for (RecordRelation expProcessRela : expProcessList) {
				// 旧的 实验过程 code  
				String expProcessCodeOld = expProcessRela.getRightCode();
				// 实验过程描述
				String expProcessDescription = CommonAlgorithm.getDataValue(recordComplexus, BaseConstant.TYPE_实验操作过程, expProcessCodeOld, ExpProcessCELNE3433Item.基本属性组_过程描述);
				// 实验过程备注
				String expProcessNote = CommonAlgorithm.getDataValue(recordComplexus, BaseConstant.TYPE_实验操作过程, expProcessCodeOld, ExpProcessCELNE3433Item.基本属性组_备注);
				// 构件新的实验过程
				
			// 构件新的实验过程code
			String expProcessCode = UidManager.getLongUID() + "";
				FGRootRecordBuilder expProcessBuilder =FGRootRecordBuilder.getInstance(BaseConstant.TYPE_实验操作过程,expProcessCode);
				//设置记录属性，第一个参数为模型属性的编码，第二个参数为模型属性的取值
				expProcessBuilder.putAttribute(ExpProcessCELNE3433Item.基本属性组_过程描述, expProcessDescription);
				expProcessBuilder.putAttribute(ExpProcessCELNE3433Item.基本属性组_备注, expProcessNote);
				//融合实验过程
				relatedRecordList.add(expProcessBuilder.getRootRecord());
				
				// 构件新实验过程的关系的关系
				RecordRelationOpsBuilder expProcessRelaBuilder = RecordRelationOpsBuilder.getInstance(BaseConstant.TYPE_实验操作过程, expProcessCode);
				
				// 查询出 实验过程 的所有投料配比信息
				List<RecordRelation> putMateriaInfoList = (List<RecordRelation>)CommonAlgorithm.getAppointRecordRelation(recordComplexus, BaseConstant.TYPE_实验操作过程, expProcessCodeOld, RelationType.RR_实验操作过程_投料信息_投料信息);
				
				for (RecordRelation putMateriaInRela : putMateriaInfoList) {
				// 获取投料信息的code
				String putMateriaCodeOld = putMateriaInRela.getRightCode();
					// 获取投料方式
					String putMateriaType = CommonAlgorithm.getDataValue(recordComplexus, BaseConstant.TYPE_投料信息, putMateriaCodeOld, MaterialRatioCELNE3466Item.基本属性组_投料方式);
					// 获取投料单位
					String putMateriaUnit = CommonAlgorithm.getDataValue(recordComplexus, BaseConstant.TYPE_投料信息, putMateriaCodeOld, MaterialRatioCELNE3466Item.基本属性组_投料量单位);
					// 获取实际投料量
					String putMateriaReal = CommonAlgorithm.getDataValue(recordComplexus, BaseConstant.TYPE_投料信息, putMateriaCodeOld, MaterialRatioCELNE3466Item.基本属性组_实际投料量);
					// 获取投料实体类型
					String putMateriaEntityType = CommonAlgorithm.getDataValue(recordComplexus, BaseConstant.TYPE_投料信息, putMateriaCodeOld, MaterialRatioCELNE3466Item.基本属性组_投料实体类型);
					
					
					// 获取投料信息的物料
					List<RecordRelation> materiaInfoList = (List<RecordRelation>)CommonAlgorithm.getAppointRecordRelation(recordComplexus, BaseConstant.TYPE_投料信息, putMateriaCodeOld, RelationType.RR_投料信息_物料信息_物料基础信息);
					// 物料的code， 
					String materiaInfoCode = materiaInfoList.get(0).getRightCode();
					
					// 构件新的投料信息， 并和实验过程关联
					String putMateriaCode = UidManager.getLongUID() + "";
					FGRootRecordBuilder putMateriaBuilder =FGRootRecordBuilder.getInstance(BaseConstant.TYPE_投料信息,putMateriaCode);
					//设置记录属性，第一个参数为模型属性的编码，第二个参数为模型属性的取值
					putMateriaBuilder.putAttribute(MaterialRatioCELNE3466Item.基本属性组_投料方式, putMateriaType);
					putMateriaBuilder.putAttribute(MaterialRatioCELNE3466Item.基本属性组_投料量单位, putMateriaUnit);
					putMateriaBuilder.putAttribute(MaterialRatioCELNE3466Item.基本属性组_实际投料量, putMateriaReal);
					putMateriaBuilder.putAttribute(MaterialRatioCELNE3466Item.基本属性组_投料实体类型, putMateriaEntityType);
					
					//融合投料信息
					relatedRecordList.add(putMateriaBuilder.getRootRecord());
					
					// 构件新投料信息的关系
					RecordRelationOpsBuilder putMateriaRelaBuilder = RecordRelationOpsBuilder.getInstance(BaseConstant.TYPE_投料信息, putMateriaCode);
					// 建立投料信息和物料基础信息的关系
					putMateriaRelaBuilder.putRelation(RelationType.RR_投料信息_物料信息_物料基础信息, materiaInfoCode);
					// 建立投料信息和实验过程的关系
//					putMateriaRelaBuilder.putRelation(RelationType.RR_投料信息_关联实验过程_实验操作过程, expProcessCode);
					
					// 融合投料关系信息
					relatedRelationOpsBuilderList.add(putMateriaRelaBuilder);
					
					expProcessRelaBuilder.putRelation(RelationType.RR_实验操作过程_投料信息_投料信息, putMateriaCode);
				}
				// 融合实验过程的关系
				relatedRelationOpsBuilderList.add(expProcessRelaBuilder);
				// 实验记录和实验过程进行关联
				expRecordOpsBuilder.putRelation(RelationType.RR_实验记录_实验操作过程_实验操作过程, expProcessCode);
			}
			
			// 融合实验记录的关系
			relatedRelationOpsBuilderList.add(expRecordOpsBuilder);
		} catch (Exception e) {
			e.printStackTrace();
			return MessageFactory.buildRefuseMessage("Failed", "设置实验记录实验员名称失败", BaseConstant.TYPE_实验记录, "");
		}
		return MessageFactory.buildInfoMessage("Succeeded", "设置实验记录实验员名称成功", BaseConstant.TYPE_实验记录, "");
	}
	
}
