package cho.carbon.fg.eln.bnb;

import java.util.Collection;

import org.springframework.stereotype.Repository;

import cho.carbon.complexus.FGRecordComplexus;
import cho.carbon.context.fg.FuncGroupContext;
import cho.carbon.fg.eln.common.KIEHelper;
import cho.carbon.fg.eln.common.SessionFactory;
import cho.carbon.fuse.fg.ConJunctionFGResult;
import cho.carbon.fuse.fg.FunctionGroup;
import cho.carbon.fuse.fg.FuseCallBackFuncGroup;
import cho.carbon.fuse.fg.IdentityQueryFuncGroup;
import cho.carbon.fuse.fg.ImproveFGResult;
import cho.carbon.fuse.fg.QueryJunctionFuncGroup;
import cho.carbon.fuse.fg.ThirdRoundImproveFuncGroup;
import cho.carbon.fuse.fg.ValidatorFGResult;
import cho.carbon.fuse.fg.ValidatorFuncGroup;
import cho.carbon.meta.criteria.model.ModelConJunction;
import cho.carbon.meta.criteria.model.ModelCriterion;
import cho.carbon.ops.complexus.OpsComplexus;

/**
 * 	实验记录
 * @author lhb
 *
 */
@Repository(value = "celne2189")
public class ExpRecordCELNE2189BNB implements FunctionGroup, IdentityQueryFuncGroup, ValidatorFuncGroup, ThirdRoundImproveFuncGroup,
	FuseCallBackFuncGroup, /* FetchFuncGroup, */QueryJunctionFuncGroup {

	@Override
	public  ImproveFGResult preImprove(FuncGroupContext context, String recordCode, OpsComplexus opsComplexus,
			FGRecordComplexus recordComplexus) {
		
		return KIEHelper.getImproveFGResultFromKIE(context, recordCode, opsComplexus, recordComplexus,
				SessionFactory.findScannerSession("ks-celne2189-f1-pre"));
	}

	@Override
	public ImproveFGResult improve(FuncGroupContext context, String recordCode, FGRecordComplexus recordComplexus) {
		return KIEHelper.getImproveFGResultFromKIE(context, recordCode, recordComplexus,
				SessionFactory.findScannerSession("ks-celne2189-f2-imp"));

	}

	@Override
	public boolean afterFusition(FuncGroupContext context,String recordCode) {

		return false;
	}

	@Override
	public ImproveFGResult postImprove(FuncGroupContext context, String recordCode, FGRecordComplexus recordComplexus) {
		return KIEHelper.getImproveFGResultFromKIE(context, recordCode, recordComplexus,
				SessionFactory.findScannerSession("ks-celne2189-f3-post"));
	}

	@Override
	public ImproveFGResult secondImprove(FuncGroupContext context, String recordCode, FGRecordComplexus recordComplexus) {
		return KIEHelper.getImproveFGResultFromKIE(context, recordCode, recordComplexus,
				SessionFactory.findScannerSession("ks-celne2189-imp-second"));
	}

	@Override
	public ImproveFGResult thirdImprove(FuncGroupContext context, String recordCode, FGRecordComplexus recordComplexus) {
		return KIEHelper.getImproveFGResultFromKIE(context, recordCode, recordComplexus,
				SessionFactory.findScannerSession("ks-celne2189-imp-third"));
	}

	@Override
	public ValidatorFGResult afterValidate(FuncGroupContext context, String recordCode, FGRecordComplexus recordComplexus) {
		return KIEHelper.getValidatorInfoFromKIE(context, recordCode, recordComplexus, 
				SessionFactory.findScannerSession("ks-celne2189-validator-after"));
	}

	@Override
	public ValidatorFGResult beforeValidate(FuncGroupContext context, String recordCode, FGRecordComplexus recordComplexus) {
		return KIEHelper.getValidatorInfoFromKIE(context, recordCode, recordComplexus, 
				SessionFactory.findScannerSession("ks-celne2189-validator-before"));
	}

	@Override
	public Collection<ModelCriterion> getCriterions(FuncGroupContext context,String recordCode, FGRecordComplexus recordComplexus) {
		return KIEHelper.getBizCriteriaListFromKIE(recordCode, recordComplexus,
				SessionFactory.findScannerSession("ks-celne2189-identity-query"));
	}

//	@Override
//	public FetchFGResult fetchImprove(FuncGroupContext context, FGRootRecord record) {
//		return KIEHelper.getFetchImproveResultFromKIE(context, record,
//				SessionFactory.findScannerSession("ks-celne2189-imp-fetch"));
//	}

	@Override
	public ConJunctionFGResult junctionImprove(FuncGroupContext context, ModelConJunction modelConJunction) {
		return KIEHelper.getConJunctionImproveResultFromKIE(context, modelConJunction,
				SessionFactory.findScannerSession("ks-celne2189-query-conjunction"));
	}

}
