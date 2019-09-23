package cho.carbon.biz.common;

import java.util.List;

import cho.carbon.fuse.fg.IdentityQuery;


public abstract class AbstractIdentityQuery  implements IdentityQuery{

	/*
	 * public List<Criteria> getCriteriaList(String recordCode,RecordComplexus
	 * complexus) {
	 * 
	 * RecordCodeIdentityQuery recordCodeOnlyIQ = new RecordCodeIdentityQuery();
	 * List<Criteria> result =
	 * recordCodeOnlyIQ.getCriteriaList(recordCode,complexus); if (result.size() >
	 * 0) { return result; }
	 * 
	 * List<Criteria> criteriaList = bizCriteriaList(recordCode,complexus);
	 * 
	 * if (criteriaList==null || criteriaList.size() == 0) { DeniedIdentityQuery
	 * noElementForIQ = new DeniedIdentityQuery();
	 * criteriaList=noElementForIQ.getCriteriaList(recordCode,complexus); } return
	 * criteriaList; }
	 * 
	 * protected abstract List<Criteria> bizCriteriaList(String
	 * recordCode,RecordComplexus complexus);
	 */
}
