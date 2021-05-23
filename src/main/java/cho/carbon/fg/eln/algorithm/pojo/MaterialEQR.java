package cho.carbon.fg.eln.algorithm.pojo;

import java.math.BigDecimal;

/**
 * 	物料当量比对象
 * @author lhb
 *
 */
public class MaterialEQR {

	
	//方案code
	private String putMateriaCode;
	// 物料的code
	private String materiaCode;
	
	
	// 物料实际投料质量
	private String realityQuality;
	// 物料计划投料质量
	private String planQuality;
	
	//  摩尔质量
	private String molqQality;
	// 实际摩尔量
	private BigDecimal realityMolarWeight;
	// 计划摩尔量
	private BigDecimal planMolarWeight;
	// 摩尔比
//	private String molarRatio;
	
	// 密度
	private String density;

	// 计算当量比 枚举
	private Integer calculateEQRENUM;
	
	public MaterialEQR() {}
	
	/**
	 * 
	 * @param putMateriaCode  方案code
	 * @param materiaCode     物料的code
	 * @param planQuality     计划投料量
	 * @param realityQuality         实际投料质量
	 * @param molqQality      摩尔质量
	 * @param density         密度
	 * @param calculateEQRENUM    计算当量比枚举值
	 */
	public MaterialEQR(String putMateriaCode, String materiaCode,String planQuality,  String realityQuality, String molqQality,
		String density, Integer calculateEQRENUM) {
		this.putMateriaCode = putMateriaCode;
		this.materiaCode = materiaCode;
		this.planQuality = planQuality;
		this.realityQuality = realityQuality;
		this.molqQality = molqQality;
		
		this.density = density;
		this.calculateEQRENUM = calculateEQRENUM;
	}


	public String getPutMateriaCode() {
		return putMateriaCode;
	}

	public void setPutMateriaCode(String putMateriaCode) {
		this.putMateriaCode = putMateriaCode;
	}

	public String getMateriaCode() {
		return materiaCode;
	}

	public void setMateriaCode(String materiaCode) {
		this.materiaCode = materiaCode;
	}

	
	public String getMolqQality() {
		return molqQality;
	}

	public void setMolqQality(String molqQality) {
		this.molqQality = molqQality;
	}


	public String getDensity() {
		return density;
	}

	public void setDensity(String density) {
		this.density = density;
	}

	public String getRealityQuality() {
		return realityQuality;
	}

	public void setRealityQuality(String realityQuality) {
		this.realityQuality = realityQuality;
	}

	public String getPlanQuality() {
		return planQuality;
	}

	public void setPlanQuality(String planQuality) {
		this.planQuality = planQuality;
	}

	public BigDecimal getRealityMolarWeight() {
		return realityMolarWeight;
	}

	public void setRealityMolarWeight(BigDecimal realityMolarWeight) {
		this.realityMolarWeight = realityMolarWeight;
	}

	public BigDecimal getPlanMolarWeight() {
		return planMolarWeight;
	}

	public void setPlanMolarWeight(BigDecimal planMolarWeight) {
		this.planMolarWeight = planMolarWeight;
	}

	public Integer getCalculateEQRENUM() {
		return calculateEQRENUM;
	}

	public void setCalculateEQRENUM(Integer calculateEQRENUM) {
		this.calculateEQRENUM = calculateEQRENUM;
	}
	
}
