package cho.carbon.fg.eln.algorithm.pojo;

/**
 * 	投料信息
 * @author lhb
 *
 */
public class PutMaterialRatio {
	public PutMaterialRatio() {}
	
	public  PutMaterialRatio(String putMateriaCode, String materiaCode, String planAmount, String actualAmount, String putMateriaUnit, String putMateriaTime) {
		this.putMateriaCode = putMateriaCode;
		this.materiaCode = materiaCode;
		this.planAmount = planAmount;
		this.actualAmount = actualAmount;
		this.putMateriaUnit = putMateriaUnit;
		this.putMateriaTime = putMateriaTime;
	}
//	
	// 投料唯一编码
	private String putMateriaCode;
	// 物料唯一编码
	private String materiaCode;
	//投料方式
	private String putMateriaWay;
	// 计划投料量
	private String planAmount;
	// 实际投料量
	private String actualAmount;
	//投料单位
	private String putMateriaUnit;
	
	// 投料时间
	private String putMateriaTime;
	
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
	public String getPutMateriaWay() {
		return putMateriaWay;
	}
	public void setPutMateriaWay(String putMateriaWay) {
		this.putMateriaWay = putMateriaWay;
	}
	public String getPlanAmount() {
		return planAmount;
	}
	public void setPlanAmount(String planAmount) {
		this.planAmount = planAmount;
	}
	public String getActualAmount() {
		return actualAmount;
	}
	public void setActualAmount(String actualAmount) {
		this.actualAmount = actualAmount;
	}
	public String getPutMateriaUnit() {
		return putMateriaUnit;
	}
	public void setPutMateriaUnit(String putMateriaUnit) {
		this.putMateriaUnit = putMateriaUnit;
	}
	public String getPutMateriaTime() {
		return putMateriaTime;
	}
	public void setPutMateriaTime(String putMateriaTime) {
		this.putMateriaTime = putMateriaTime;
	}
	
}
