package cho.carbon.fg.eln.algorithm.eln;

import java.util.List;

import cho.carbon.fg.eln.constant.item.CELNE2006Item;
import cho.carbon.fuse.improve.attribute.FuseAttributeFactory;
import cho.carbon.fuse.improve.attribute.mline.FuseMLineAttr;
import cho.carbon.fuse.improve.ops.builder.FGRecordOpsBuilder;
import cho.carbon.model.uid.UidManager;
import cho.carbon.rrc.record.FGAttribute;
import cho.carbon.rrc.record.FGRootRecord;

public class Stock {

	public static String buildAddLeaf(FGRootRecord fgRootRecord,FGRecordOpsBuilder recordOpsBuilder) {
		
		String leafCode = UidManager.getLongUID()+"";
		String recordCode = fgRootRecord.getCode();
		
//		//更改库存总量
//		FGAttribute allCountAttr=FuseAttributeFactory.buildAttribute(CELNE2006Item.基本信息_库存量, allCountAttr.getValue(AttributeValueType.INT)+addCountAttr.getValue(AttributeValueType.INT));
//		attributeList.add(birthday);
//		FGAttribute countAttr = fgRootRecord.findAttribute(CELNE2006Item.入库表单_数量);
//		
//		FGAttribute opsAttr = fgRootRecord.findAttribute(CELNE2006Item.入库表单_操作类型);
//		if(opsAttr==null || opsAttr.getValue(AttributeValueType.INT) == null ||  ) {
//			return null;
//		}else {
//			Integer count=(Integer) opsAttr.getValue(AttributeValueType.INT);
//			
//		}
		
		FuseMLineAttr leafAttr;
		
		FGAttribute attr = fgRootRecord.findAttribute(CELNE2006Item.出入库表单_数量);
		if (attr != null) {
			leafAttr = FuseAttributeFactory.buildFuseMlineAttr(recordCode, CELNE2006Item.仪器库存_出入库记录, leafCode,
					CELNE2006Item.出入库记录_数量, attr.getInherentTypeValue());
			recordOpsBuilder.addMLineUpdateAttr(leafAttr);
			recordOpsBuilder.addUpdateAttr(FuseAttributeFactory.buildAttribute(CELNE2006Item.出入库表单_数量, null));
		}
		attr = fgRootRecord.findAttribute(CELNE2006Item.出入库表单_操作类型);
		if (attr != null) {
			leafAttr = FuseAttributeFactory.buildFuseMlineAttr(recordCode, CELNE2006Item.仪器库存_出入库记录, leafCode,
					CELNE2006Item.出入库记录_操作类型, attr.getInherentTypeValue());
			recordOpsBuilder.addMLineUpdateAttr(leafAttr);
			recordOpsBuilder.addUpdateAttr(FuseAttributeFactory.buildAttribute(CELNE2006Item.出入库表单_操作类型, null));
		}
		attr = fgRootRecord.findAttribute(CELNE2006Item.出入库表单_入库时间);
		if (attr != null) {
			leafAttr = FuseAttributeFactory.buildFuseMlineAttr(recordCode, CELNE2006Item.仪器库存_出入库记录, leafCode,
					CELNE2006Item.出入库记录_入库时间, attr.getInherentTypeValue());
			recordOpsBuilder.addMLineUpdateAttr(leafAttr);
			recordOpsBuilder.addUpdateAttr(FuseAttributeFactory.buildAttribute(CELNE2006Item.出入库表单_入库时间, null));
		}
		attr = fgRootRecord.findAttribute(CELNE2006Item.出入库表单_来源);
		if (attr != null) {
			leafAttr = FuseAttributeFactory.buildFuseMlineAttr(recordCode, CELNE2006Item.仪器库存_出入库记录, leafCode,
					CELNE2006Item.出入库记录_来源, attr.getInherentTypeValue());
			recordOpsBuilder.addMLineUpdateAttr(leafAttr);
			recordOpsBuilder.addUpdateAttr(FuseAttributeFactory.buildAttribute(CELNE2006Item.出入库表单_来源, null));
		}
		
		attr = fgRootRecord.findAttribute(CELNE2006Item.出入库表单_备注);
		if (attr != null) {
			leafAttr = FuseAttributeFactory.buildFuseMlineAttr(recordCode, CELNE2006Item.仪器库存_出入库记录, leafCode,
					CELNE2006Item.出入库记录_备注, attr.getInherentTypeValue());
			recordOpsBuilder.addMLineUpdateAttr(leafAttr);
			recordOpsBuilder.addUpdateAttr(FuseAttributeFactory.buildAttribute(CELNE2006Item.出入库表单_备注, null));
		}

		return leafCode;
	}

}
