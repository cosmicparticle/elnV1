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
	 * 
	 * @param convertUnit  转换单位
	 * @param convertCount 转换量
	 * @param materiaCode    物料基础信息唯一编码
	 */
	public static BigDecimal convertBaseUnit(FGRecordComplexus recordComplexus, Integer convertUnit, BigDecimal convertCount, String materiaCode) throws Exception {
		Integer baseUnit = getMaterialBaseUnit(recordComplexus, materiaCode);
		// 进行转换
		BigDecimal convert = getConvertCount(convertUnit, convertCount, baseUnit);
		return convert;
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
	 * 获取转换后的量
	 * @param convertUnit  待转换的单位
	 * @param convertCount 待转换的量
	 * @param baseUnit       基础计量单位
	 */
	public static BigDecimal getConvertCount(Integer convertUnit, BigDecimal convertCount, Integer baseUnit) throws Exception{

		switch (convertUnit) {
		case 30732:
//			EnumKeyValue.ENUM_物料计量单位_克
			// 获取转换利率
			if (EnumKeyValue.ENUM_物料计量单位_克.equals(baseUnit)) {
				return convertCount;
			} else if (EnumKeyValue.ENUM_物料计量单位_千克.equals(baseUnit)) {
				return convertCount.divide(new BigDecimal("1000"));
			} else if (EnumKeyValue.ENUM_物料计量单位_吨.equals(baseUnit)) {
				return convertCount.divide(new BigDecimal("1000000"));
			}
			break;
		case 31091:
			// EnumKeyValue.ENUM_物料计量单位_千克
			// 获取转换利率
			if (EnumKeyValue.ENUM_物料计量单位_克.equals(baseUnit)) {
				return convertCount.multiply(new BigDecimal("1000"));
			} else if (EnumKeyValue.ENUM_物料计量单位_千克.equals(baseUnit)) {
				return convertCount;
			} else if (EnumKeyValue.ENUM_物料计量单位_吨.equals(baseUnit)) {
				return convertCount.divide(new BigDecimal("1000"));
			}
			break;
		case 31093:
//			EnumKeyValue.ENUM_物料计量单位_吨
			if (EnumKeyValue.ENUM_物料计量单位_克.equals(baseUnit)) {
				return convertCount.multiply(new BigDecimal("1000000"));
			} else if (EnumKeyValue.ENUM_物料计量单位_千克.equals(baseUnit)) {
				return convertCount.multiply(new BigDecimal("1000"));
			} else if (EnumKeyValue.ENUM_物料计量单位_吨.equals(baseUnit)) {
				return convertCount;
			}
			break;
		case 30733:
//			EnumKeyValue.ENUM_物料计量单位_毫升
			if (EnumKeyValue.ENUM_物料计量单位_毫升.equals(baseUnit)) {
				return convertCount;
			} else if (EnumKeyValue.ENUM_物料计量单位_升.equals(baseUnit)) {
				return convertCount.divide(new BigDecimal("1000"));
			}
			break;
		case 31092:
//			EnumKeyValue.ENUM_物料计量单位_升
			if (EnumKeyValue.ENUM_物料计量单位_毫升.equals(baseUnit)) {
				return convertCount.multiply(new BigDecimal("1000"));
			} else if (EnumKeyValue.ENUM_物料计量单位_升.equals(baseUnit)) {
				return convertCount;
			}
		}
		if(true) {
			throw new RuntimeException("无法转换物料量");
		}
		return null;
	}
	

}
