package cho.carbon.biz.timertask;

import java.util.Collection;

import cho.carbon.auth.constant.AuthConstant;
import cho.carbon.fuse.fg.FunctionalGroup;
import cho.carbon.hc.FusionContext;
import cho.carbon.hc.HCFusionContext;
import cho.carbon.panel.Integration;
import cho.carbon.panel.PanelFactory;


public class LoadEntityToWorkMemory {
	public static void loadEntity(String entityType, Collection<String>  codes, FunctionalGroup functionalGroup) {
		Integration integration=PanelFactory.getIntegration();
		HCFusionContext context = new HCFusionContext();
		context.putFunctionalGroup(entityType, functionalGroup);
		context.setUserCode(AuthConstant.SUPERCODE);
		context.setSource(FusionContext.SOURCE_COMMON);
		if(codes!=null) {
			for(String code :codes) {
				integration.integrate(context,code);
			}
		}
	}
	
}
