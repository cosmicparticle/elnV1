package cho.carbon.fg.eln.algorithm.eln;

import cho.carbon.complexus.FGRecordComplexus;
import cho.carbon.fg.eln.constant.BaseConstant;
import cho.carbon.fuse.improve.ops.builder.FGRecordOpsBuilder;
import cho.carbon.message.Message;
import cho.carbon.message.MessageFactory;
import cho.carbon.rrc.record.FGRootRecord;

/**
 * 投料信息
 * @author lhb
 *
 */
public class MaterialRatioAlgorithm {

	/**
	 *	
	 * @param recordComplexus
	 * @param recordCode
	 * @param recordOpsBuilder
	 * @return
	 */
	public static Message xxxxx(FGRecordComplexus recordComplexus, String recordCode, FGRecordOpsBuilder recordOpsBuilder) {
		try {
			// 获取当前物料投料信息
			FGRootRecord rootRecord = CommonAlgorithm.getRootRecord(recordComplexus, BaseConstant.TYPE_投料信息, recordCode);
			
			
			
			
		} catch (Exception e) {
			e.printStackTrace();
			return MessageFactory.buildRefuseMessage("Failed", "失败", BaseConstant.TYPE_投料信息, "失败");
		}
		return MessageFactory.buildInfoMessage("Succeeded", "成功", BaseConstant.TYPE_投料信息, "成功");
	}
}
