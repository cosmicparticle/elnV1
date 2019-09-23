package cho.carbon.biz.bnb;

import java.util.Collection;
import org.springframework.stereotype.Repository;

import cho.carbon.biz.common.KIEHelper;
import cho.carbon.biz.common.SessionFactory;
import cho.carbon.callback.IFusitionCallBack;
import cho.carbon.complexus.FGRecordComplexus;
import cho.carbon.fuse.check.FuseCheckInfo;
import cho.carbon.fuse.fg.FGFusionContext;
import cho.carbon.fuse.fg.FunctionalGroup;
import cho.carbon.fuse.fg.FuseCheck;
import cho.carbon.fuse.fg.IdentityQuery;
import cho.carbon.fuse.fg.ImproveResult;
import cho.carbon.fuse.fg.OneRoundImprovement;
import cho.carbon.fuse.fg.ThreeRoundImprovement;
import cho.carbon.hc.HCFusionContext;
import cho.carbon.meta.criteria.model.ModelCriterion;
import cho.carbon.ops.complexus.OpsComplexus;

@Repository(value = "DXJDE2020")
public class PeopleFG implements FuseCheck,FunctionalGroup, IdentityQuery,OneRoundImprovement, ThreeRoundImprovement, IFusitionCallBack {

	
	@Override
	public Collection<ModelCriterion> getCriterions(String recordCode, FGRecordComplexus recordComplexus) {
		return KIEHelper.getBizCriteriaListFromKIE(recordCode, recordComplexus,
				SessionFactory.findScannerSession("ks-people-idt-query"));
	}

	@Override
	public ImproveResult preImprove(FGFusionContext context, String recordCode, OpsComplexus opsComplexus,
			FGRecordComplexus recordComplexus) {
		return KIEHelper.getImproveResultFromKIE(context, recordCode, opsComplexus, recordComplexus,
				SessionFactory.findScannerSession("ks-people-preipm"));
	}

	@Override
	public ImproveResult improve(FGFusionContext context, String recordCode, FGRecordComplexus recordComplexus) {
		return KIEHelper.getImproveResultFromKIE(context, recordCode, recordComplexus,
				SessionFactory.findScannerSession("ks-people-ipm"));
	} 

	@Override
	public boolean afterFusition(String recordCode, HCFusionContext context) {

		return false;
	}

	@Override
	public ImproveResult postImprove(FGFusionContext context, String recordCode, FGRecordComplexus recordComplexus) {
		return KIEHelper.getImproveResultFromKIE(context, recordCode, recordComplexus,
				SessionFactory.findScannerSession("ks-people-postimp"));
	}

	@Override
	public ImproveResult secondImprove(FGFusionContext context, String recordCode, FGRecordComplexus recordComplexus) {
		return KIEHelper.getImproveResultFromKIE(context, recordCode, recordComplexus,
				SessionFactory.findScannerSession("ks-people-secondipm"));
	}

	@Override
	public ImproveResult thirdImprove(FGFusionContext context, String recordCode, FGRecordComplexus recordComplexus) {
		return KIEHelper.getImproveResultFromKIE(context, recordCode, recordComplexus,
				SessionFactory.findScannerSession("ks-people-thirdIPM"));
	}

	@Override
	public FuseCheckInfo afterCheck(FGFusionContext context, String recordCode, FGRecordComplexus recordComplexus) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FuseCheckInfo beforeCheck(FGFusionContext context, String recordCode, FGRecordComplexus recordComplexus) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
