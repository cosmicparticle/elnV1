package cho.carbon.fg.eln.algorithm;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.management.RuntimeErrorException;
import javax.persistence.criteria.CriteriaBuilder.In;

import org.apache.commons.lang.StringUtils;
import org.hibernate.mapping.Array;

import cho.carbon.complexus.FGRecordComplexus;
import cho.carbon.fg.eln.algorithm.eln.CommonAlgorithm;
import cho.carbon.fg.eln.constant.BaseConstant;
import cho.carbon.fg.eln.constant.EnumKeyValue;
import cho.carbon.fg.eln.constant.item.MateriaInfoCELNE3393Item;

/**
 * 物料单位转换
 * 
 * @author lhb
 *
 */
public class MaterialUnitUtil {
	
	
	/**
	 * 	出入进来的单位转为 g
	 * @param convertUnit  转换单位
	 * @param convertCount 转换量
	 * @param materiaCode    物料基础信息唯一编码
	 */
	public static BigDecimal convertUnitg(FGRecordComplexus recordComplexus, Integer convertUnit, BigDecimal convertCount, String materiaCode) throws Exception {
		BigDecimal convert = null;
		try {
			// 进行转换
			convert = getConvertCount(recordComplexus, convertUnit, convertCount, EnumKeyValue.ENUM_物料计量单位_克, materiaCode);
		} catch (Exception e) {
			throw new RuntimeException("物料转g错误, 请查看物料是否有密度");
		}
		return convert;
	}
	
	
	/**
	 * 	出入进来的单位转为kg
	 * @param convertUnit  转换单位
	 * @param convertCount 转换量
	 * @param materiaCode    物料基础信息唯一编码
	 */
	public static BigDecimal convertUnitKG(FGRecordComplexus recordComplexus, Integer convertUnit, BigDecimal convertCount, String materiaCode) throws Exception {
		BigDecimal convert = null;
		try {
			// 进行转换
			convert = getConvertCount(recordComplexus, convertUnit, convertCount, EnumKeyValue.ENUM_物料计量单位_千克, materiaCode);
		} catch (Exception e) {
			throw new RuntimeException("物料转kg错误, 请查看物料是否有密度");
		}
		return convert;
	}
	
	/**
	 * 	获取物料密度    单位为g/ml
	 * @param recordComplexus
	 * @param materiaCode
	 * @return
	 */
	public static BigDecimal getMaterialDensity(FGRecordComplexus recordComplexus, String materiaCode) {
		String densityStr = CommonAlgorithm.getDataValue(recordComplexus, BaseConstant.TYPE_物料基础信息, materiaCode,
				MateriaInfoCELNE3393Item.基本属性组_密度_g);
		if (StringUtils.isBlank(densityStr)) {
			throw new RuntimeException("物料密度不能为空");
		}
		// 获取物料密度
		BigDecimal bigDecimal = new BigDecimal(densityStr);
		return bigDecimal;
	}

	/**
	 * 获取物料基础计量单位
	 * @param recordComplexus
	 * @param materiaCode
	 * @return
	 */
	public static Integer getMaterialBaseUnit(FGRecordComplexus recordComplexus, String materiaCode) {
		String baseUnitStr = CommonAlgorithm.getDataValue(recordComplexus, BaseConstant.TYPE_物料基础信息, materiaCode,
				MateriaInfoCELNE3393Item.基本属性组_基础计量单位);
		if (StringUtils.isBlank(baseUnitStr)) {
			throw new RuntimeException("物料基础单位不能为空");
		}
		// 物料基础单位
		Integer baseUnit = Integer.parseInt(baseUnitStr);
		return baseUnit;
	}

	/**
	 * 	获取转换后的量
	 * @param convertUnit  待转换的单位
	 * @param convertCount 待转换的量
	 * @param baseUnit       基础计量单位
	 *  @param materiaCode   物料基础信息 code 
	 */
	public static BigDecimal getConvertCount(FGRecordComplexus recordComplexus,Integer convertUnit, BigDecimal convertCount, 
			Integer baseUnit, String materiaCode) throws Exception{
		BigDecimal density = null;
		switch (convertUnit) {
		case 30732:
//			EnumKeyValue.ENUM_物料计量单位_克
			// 获取转换利率
			if (EnumKeyValue.ENUM_物料计量单位_克.equals(baseUnit)) {
				return convertCount.setScale(4, BigDecimal.ROUND_HALF_UP);
			} else if (EnumKeyValue.ENUM_物料计量单位_千克.equals(baseUnit)) {
				return convertCount.divide(new BigDecimal("1000")).setScale(4, BigDecimal.ROUND_HALF_UP);
			} else if (EnumKeyValue.ENUM_物料计量单位_吨.equals(baseUnit)) {
				return convertCount.divide(new BigDecimal("1000000")).setScale(4, BigDecimal.ROUND_HALF_UP);
			}
			break;
		case 31091:
			// EnumKeyValue.ENUM_物料计量单位_千克
			// 获取转换利率
			if (EnumKeyValue.ENUM_物料计量单位_克.equals(baseUnit)) {
				return convertCount.multiply(new BigDecimal("1000")).setScale(4, BigDecimal.ROUND_HALF_UP);
			} else if (EnumKeyValue.ENUM_物料计量单位_千克.equals(baseUnit)) {
				return convertCount.setScale(4, BigDecimal.ROUND_HALF_UP);
			} else if (EnumKeyValue.ENUM_物料计量单位_吨.equals(baseUnit)) {
				return convertCount.divide(new BigDecimal("1000")).setScale(4, BigDecimal.ROUND_HALF_UP);
			}
			break;
		case 31093:
//			EnumKeyValue.ENUM_物料计量单位_吨
			if (EnumKeyValue.ENUM_物料计量单位_克.equals(baseUnit)) {
				return convertCount.multiply(new BigDecimal("1000000")).setScale(4, BigDecimal.ROUND_HALF_UP);
			} else if (EnumKeyValue.ENUM_物料计量单位_千克.equals(baseUnit)) {
				return convertCount.multiply(new BigDecimal("1000")).setScale(4, BigDecimal.ROUND_HALF_UP);
			} else if (EnumKeyValue.ENUM_物料计量单位_吨.equals(baseUnit)) {
				return convertCount.setScale(4, BigDecimal.ROUND_HALF_UP);
			}
			break;
		case 30733:
//			EnumKeyValue.ENUM_物料计量单位_毫升
			// 获取物料密度
			density = getMaterialDensity(recordComplexus, materiaCode);
				// 毫升 * g/ml  = g
				BigDecimal multiply = convertCount.multiply(density).setScale(8, BigDecimal.ROUND_UP);
				return multiply;
		case 31092:
//			EnumKeyValue.ENUM_物料计量单位_升
//			把升转为kg
			// 获取物料密度
			density = getMaterialDensity(recordComplexus, materiaCode);
			// 把什转为毫升， 在乘以密度
			BigDecimal ml = convertCount.multiply(new BigDecimal("1000")).setScale(4, BigDecimal.ROUND_HALF_UP);
			// 毫升乘以密度得到g
			multiply = ml.multiply(density).setScale(4, BigDecimal.ROUND_HALF_UP);
			return multiply;
		}
		if(true) {
			throw new RuntimeException("无法转换物料量");
		}
		return null;
	}

}
