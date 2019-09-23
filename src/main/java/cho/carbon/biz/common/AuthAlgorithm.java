package cho.carbon.biz.common;

import java.util.HashMap;
import java.util.Map;

public class AuthAlgorithm {
	
	public static String getAreaAuthCode(Integer areaCode) {
		if(areaCode==null){
			return noAreaAuth;
		}
		String authcode= areaMappingAuth.get(areaCode);
		if(authcode==null) {
			return noAreaAuth;
		}else {
			return authcode;
		}
	}
	
	public static String getNoAreaAuthCode( ) {
		return noAreaAuth;
	}
	
	public static Map<Integer,String> areaMappingAuth=new HashMap<Integer,String>();
	public static String noAreaAuth="91ff74535cc741f6ae1a39363c643f8f";//没有正确社区的数据访问权限
	
	static{
	/*	areaMappingAuth.put(EnumKeyValue.ENUM_祥符街道社区_勤丰社区, "f23b9f4eb14a409fb78498dc4457c190");
		areaMappingAuth.put(EnumKeyValue.ENUM_祥符街道社区_祥符桥社区, "27e14645b42341aea6fc2727e34855b4");
		areaMappingAuth.put(EnumKeyValue.ENUM_祥符街道社区_新文社区, "209080a4f6b6403eb4f735e4e5287512");
		areaMappingAuth.put(EnumKeyValue.ENUM_祥符街道社区_庆隆社区, "f84d746d7f1e46caa340790cb3886f25");
		areaMappingAuth.put(EnumKeyValue.ENUM_祥符街道社区_吉如社区, "e5e447e084be4eef974880f90b90bf97");
		areaMappingAuth.put(EnumKeyValue.ENUM_祥符街道社区_阮家桥社区, "a32f439d170a44ab90e724106e772409");
		areaMappingAuth.put(EnumKeyValue.ENUM_祥符街道社区_方家埭社区, "20033fde900142d2adb0975edcd8f6d5");
		areaMappingAuth.put(EnumKeyValue.ENUM_祥符街道社区_花园岗社区, "d12731b3b8354baf95ea454b8aa94fb5");
		areaMappingAuth.put(EnumKeyValue.ENUM_祥符街道社区_孔家埭社区, "893e5b71d7ea4fa9b6510390d92f3049");
		areaMappingAuth.put(EnumKeyValue.ENUM_祥符街道社区_星桥社区, "8791e9828d59432d80729463cc36f022");
		areaMappingAuth.put(EnumKeyValue.ENUM_祥符街道社区_映月社区, "0535bf145c29443f81055e8821f7149b");
		areaMappingAuth.put(EnumKeyValue.ENUM_祥符街道社区_北星社区, "e2a1bdaf7ef74bfdb6a333da91b2988b");
		areaMappingAuth.put(EnumKeyValue.ENUM_祥符街道社区_秀水社区, "5dddd405881a410e87b91787b0b0ba7c");
		areaMappingAuth.put(EnumKeyValue.ENUM_祥符街道社区_总管堂社区, "febb3fe7d90140bf886618affacd108e");
		areaMappingAuth.put(EnumKeyValue.ENUM_祥符街道社区_申悦社区, "51152f1b94e549ba8362475ea89a069a");
		areaMappingAuth.put(EnumKeyValue.ENUM_祥符街道社区_申慧社区, "ddf805022b9b48fcb23c0c9f89f182e9");
		areaMappingAuth.put(EnumKeyValue.ENUM_祥符街道社区_蓝孔雀社区, "a40d9d1696814d96a02c27313fd0d734");*/
	}

}
