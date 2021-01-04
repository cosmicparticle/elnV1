package cho.carbon.fg.eln.algorithm.eln;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

import javax.swing.filechooser.FileSystemView;

import cho.carbon.complexus.FGRecordComplexus;
import cho.carbon.complexus.RecordComplexus;
import cho.carbon.context.core.PersistentContext;
import cho.carbon.context.hc.HCFusionContext;
import cho.carbon.meta.enun.operator.UnaryOperator;
import cho.carbon.panel.Integration;
import cho.carbon.panel.IntegrationMsg;
import cho.carbon.panel.PanelFactory;
import cho.carbon.query.model.QueryRecordParmFactory;
import cho.carbon.relation.RecordRelation;
import cho.carbon.relation.RelationCorrelation;
import cho.carbon.rrc.Updatable;
import cho.carbon.rrc.query.SimpleRecordQueryPanel;
import cho.carbon.rrc.record.FGAttribute;
import cho.carbon.rrc.record.FGRootRecord;
import cho.carbon.rrc.record.LeafRecord;

/**
 * carbon通用算法
 * @author lhb
 *
 */
public class CommonAlgorithm {
	
	 /**
     * 	获取操作系统的桌面路径
     * @return
     */
    public static String getDesktopPath() {
		File desktopDir = FileSystemView.getFileSystemView().getHomeDirectory();
		return desktopDir.getAbsolutePath();
	}
	
	/**
	 * 	获取 FGRootRecord
	 * @param recordComplexus
	 * @param modelCode      BaseConstant.TYPE_系统用户
	 * @param recordCode
	 * @return
	 */
	public static FGRootRecord getRootRecord(FGRecordComplexus recordComplexus, String recordName, String recordCode) {
		return  recordComplexus.getRecord(recordName,recordCode);
	}
	
	/**
	 * 	通过左实体的唯一关系， 获取此关系的右实体
	 * @param recordComplexus
	 * @param recordName   左实体类型
	 * @param recordCode   左实体中记录的具体code
	 * @param relationType  关系类型
	 * @param rightRecordName  右实体类型
	 * @return  右实体  中记录的   FGRootRecord
	 */
	public static FGRootRecord getRightRootRecord(FGRecordComplexus recordComplexus,String recordName,  String recordCode, Long relationType, String rightRecordName) {
		// 此关系必须唯一
		List<RecordRelation> appointRecordRelation = (List<RecordRelation>)getAppointRecordRelation(recordComplexus,recordName, recordCode, relationType);
		if (appointRecordRelation.isEmpty() || appointRecordRelation.size() !=1) {
			return null;
		}
		
		return  getRootRecord(recordComplexus, rightRecordName, appointRecordRelation.get(0).getRight());
	}
	
	/**
	 *  	获取指定关系的数量   
	 * @param recordComplexus
	 * @param recordName
	 * @param recordCode
	 * @param relationType   指定的关系类型       type==RelationType.RR_人口信息_走访记录_走访记录
	 * @return 
	 */
	public static Integer getAppointRecordRelationCount(FGRecordComplexus recordComplexus,String recordName,  String recordCode, Long relationType) {
		Integer count = 0;
		Collection<RecordRelation> appointRecordRelation = getAppointRecordRelation(recordComplexus,recordName, recordCode, relationType);
		
		if (!appointRecordRelation.isEmpty()) {
			count = appointRecordRelation.size();
		}
		
		return count;
	}
	
	/**
	 * 	获取指定关系类型的  所有 关系 List
	 * @param recordComplexus
	 * @param recordName
	 * @param recordCode
	 * @param relationType   指定的关系类型       type==RelationType.RR_人口信息_走访记录_走访记录
	 * @return
	 */
	public static Collection<RecordRelation> getAppointRecordRelation(FGRecordComplexus recordComplexus,String recordName,  String recordCode, Long relationType) {
		
		Collection<RecordRelation> appointRelation = new ArrayList<RecordRelation>();
		
		RelationCorrelation	relationCorrelation = CommonAlgorithm.getRelationCorrelation(recordComplexus,recordName, recordCode);
		
		if (relationCorrelation != null) {
			Collection<RecordRelation> recordRelation = relationCorrelation.getRecordRelation();
			if (!recordRelation.isEmpty()) {
				for (RecordRelation recordRelation2 : recordRelation) {
					if (relationType.equals(recordRelation2.getType())) {
						appointRelation.add(recordRelation2);
					}
				}
			}
		}
		
		return appointRelation;
	}
	
	/**
	 * 根据 recordCode 获取本实例的所有关系
	 * @param recordComplexus
	 * @param recordName    
	 * @param recordCode
	 * @return
	 */
	public static RelationCorrelation getRelationCorrelation(FGRecordComplexus recordComplexus,String recordName,  String recordCode) {
		RelationCorrelation relationCorrelation = recordComplexus.getRelationCorrelation(recordName, recordCode);
		return relationCorrelation;
	}
	/**
	 * 根据  recordCode 获取本实例  指定属性的值
	 * @param recordComplexus
	 * @param recordCode
	 * @param itemValue
	 * @return
	 */
	public static String getDataValue(FGRecordComplexus recordComplexus, String recordName,  String recordCode, String itemValue) {
		
		FGRootRecord rootRecord = recordComplexus.getRecord(recordName, recordCode);
		
		String name = null;
		if (rootRecord != null) {
			FGAttribute findAttribute = rootRecord.findAttribute(itemValue);
			if (findAttribute != null) {
				name = findAttribute.getValueStr();
			}
		}
		return name;
	}
	
	/**
	 * 
	 * @param rootRecord
	 * @param itemCode  
	 * @return
	 */
	public static String getDataValue(FGRootRecord rootRecord , String itemCode) {
		String name = null;
		if (rootRecord != null) {
			FGAttribute findAttribute = rootRecord.findAttribute(itemCode);
			if (findAttribute != null) {
				name = findAttribute.getValueStr();
			}
		}
		return name;
	}
	
	/**
	 * 	可以根据这个数量判断多值属性是否有值
	 * 获取多值属性值的数量       
	 * @param recordComplexus
	 * @param recordCode
	 * @param leaf
	 * @return
	 */
	public static Integer getLeafCount(RecordComplexus recordComplexus, String recordCode, String leaf) {
		Collection<LeafRecord> findLeafs = CommonAlgorithm.getLeaFecords(recordComplexus, recordCode, leaf);
		if (findLeafs == null) {
			return 0;
		}
		return findLeafs.size();
	}
	
	/**
	 * 	获取指定的多值属性
	 * @param recordComplexus
	 * @param recordCode
	 * @param leaf
	 * @return
	 */
	private static Collection<LeafRecord> getLeaFecords(RecordComplexus recordComplexus, String recordCode, String leaf) {
		return recordComplexus.getRecord(recordCode).findLeafs(leaf);
	}
	
	 /**
     * 	融合一个POJO, 并返回一个IntegrationMsg
     * @param bean
     */
    public static IntegrationMsg saveUpdatable(Updatable bean) {
    	Integration integration = PanelFactory.getIntegration();
		HCFusionContext context = new HCFusionContext();
		context.setSource(PersistentContext.SOURCE_COMMON);
		IntegrationMsg msg = null;
    	if (bean != null) {
			msg = integration.integrate(context, bean);
		}
    	return msg;
	}
	
	/**
	 * 验证用户名
	 * @param username
	 * @return
	 */
	
	public static boolean isLawfulUserName(Object userName) {
		if (userName != null && userName instanceof String) {
			String userNameStr = (String) userName;
			String regex = "^[A-Za-z]+[A-Za-z0-9]*$";
			return Pattern.matches(regex, userNameStr);
		} else {
			return false;
		}
	}
	
	public static boolean existingUserName(Object userName) {
		if (userName != null && userName instanceof String) {
			String userNameStr = (String) userName;
			QueryRecordParmFactory queryRecordParmFactory = new QueryRecordParmFactory("ABCBE002");
			queryRecordParmFactory.getConJunctionFactory().getGroupFactory().addCommon("ABCB006", userNameStr,
					UnaryOperator.EQUAL);
			List<String> codeList = SimpleRecordQueryPanel.queryCodeList(queryRecordParmFactory.getQueryParameter());
			return codeList != null && !codeList.isEmpty();
		} else {
			return false;
		}
	}
	
	public static String encryptMD5(Object obj) {
		String temp = obj.toString();
		return "{MD5}"+md5(temp);
	}
	
	/**
	   * 对字符串进行MD5摘要加密，返回结果与MySQL的MD5函数一致
     * 
     * @param input
     * @return 返回值中的字母为小写
     */
    private static String md5(String input) {
        if (null == input) {
            input = "";
        }
        String result = "";
        try {
            // MessageDigest类用于为应用程序提供信息摘要算法的功能，如MD5或SHA算法
            MessageDigest md = MessageDigest.getInstance("MD5");
            // 获取输入
            md.update(input.getBytes());
            // 获得产出（有符号的哈希值字节数组，包含16个元素）
            byte output[] = md.digest();

            // 32位的加密字符串
            StringBuilder builder = new StringBuilder(32);
            // 下面进行十六进制的转换
            for (int offset = 0; offset < output.length; offset++) {
                // 转变成对应的ASSIC值
                int value = output[offset];
                // 将负数转为正数（最终返回结果是无符号的）
                if (value < 0) {
                    value += 256;
                }
                // 小于16，转为十六进制后只有一个字节，左边追加0来补足2个字节
                if (value < 16) {
                    builder.append("0");
                }
                // 将16位byte[]转换为32位无符号String
                builder.append(Integer.toHexString(value));
            }
            result = builder.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return result;
    }

    
}
