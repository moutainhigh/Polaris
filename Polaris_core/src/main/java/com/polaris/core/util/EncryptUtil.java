package com.polaris.core.util;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.crypto.Cipher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.polaris.core.Constant;

public class EncryptUtil {
	private static final Logger logger = LoggerFactory.getLogger(EncryptUtil.class);

	private static String strDefaultKey = "Tom.Yu";
	private Cipher encryptCipher = null;

	public static final String START_WITH = "{DES}";
	private Cipher decryptCipher = null;
	
	/**
	 * 单例
	 */
    private static volatile Map<String, EncryptUtil> instanceMap = new ConcurrentHashMap<>();

    public static String getDefaultKey() {
    	return strDefaultKey;
    }
    
    /**
     * 获取唯一进程限制的实例
     * @Title: getInstance
     * @return  唯一进程实例
     */
    public static EncryptUtil getInstance(){
        return getInstance(strDefaultKey);
    }
    public static EncryptUtil getInstance(String strKey){
    	return getInstance(strKey, Type.DES);
    }
    public static EncryptUtil getInstance(String strKey, Type type){
    	if (StringUtil.isEmpty(strKey)) {
    		strKey = strDefaultKey;
    	}
    	String key = type.name() + strKey;
    	EncryptUtil instance = instanceMap.get(key);
        if(instance == null){
        	synchronized(strKey.intern()){
        		instance = instanceMap.get(key);
        		if(instance == null){
        			try {
        				instance = new EncryptUtil(strKey, type);
                        instanceMap.put(key, instance);
                	} catch (Exception ex) {
                		logger.error("异常", ex);
                	}
        		}
        	}
        }
        return instance;
    }
    
	public enum Algorithm {
		SHA,//SHA算法
		MD5;//MD5算法
		
		public String getName() {
			return "{" + name() + "}";
		}
	}
	
	public enum Type {
		DES,
		AES;
	}
	
	/**
	 * 将byte数组转换为表示16进制值的字符串， 如：byte[]{8,18}转换为：0813， 和public static byte[]
	 * hexStr2ByteArr(String strIn) 互为可逆的转换过程
	 * 
	 * @param arrB
	 *            需要转换的byte数组
	 * @return 转换后的字符串
	 * @throws Exception
	 *             本方法不处理任何异常，所有异常全部抛出
	 */
	public static String byteArr2HexStr(byte[] arrB)  {
		int iLen = arrB.length;
		// 每个byte用两个字符才能表示，所以字符串的长度是数组长度的两倍
		StringBuilder sb = new StringBuilder(iLen * 2);
		for (int i = 0; i < iLen; i++) {
			int intTmp = arrB[i];
			// 把负数转换为正数
			while (intTmp < 0) {
				intTmp = intTmp + 256;
			}
			// 小于0F的数需要在前面补0
			if (intTmp < 16) {
				sb.append("0");
			}
			sb.append(Integer.toString(intTmp, 16));
		}
		return sb.toString();
	}

	/**
	 * 将表示16进制值的字符串转换为byte数组， 和public static String byteArr2HexStr(byte[] arrB)
	 * 互为可逆的转换过程
	 * 
	 * @param strIn
	 *            需要转换的字符串
	 * @return 转换后的byte数组
	 * @throws UnsupportedEncodingException 
	 * @throws Exception
	 *             本方法不处理任何异常，所有异常全部抛出
	 * 
	 */
	public static byte[] hexStr2ByteArr(String strIn) throws UnsupportedEncodingException  {
		byte[] arrB = strIn.getBytes(Constant.UTF_CODE);
		int iLen = arrB.length;

		// 两个字符表示一个字节，所以字节数组长度是字符串长度除以2
		byte[] arrOut = new byte[iLen / 2];
		for (int i = 0; i < iLen; i = i + 2) {
			String strTmp = new String(arrB, i, 2);
			arrOut[i / 2] = (byte) Integer.parseInt(strTmp, 16);
		}
		return arrOut;
	}

	/**
	 * 默认构造方法，使用默认密钥
	 * 
	 * @throws Exception
	 */
	public EncryptUtil() throws NoSuchAlgorithmException, InvalidKeyException, Exception {
		this(strDefaultKey,Type.DES);
	}

	/**
	 * 指定密钥构造方法
	 * 
	 * @param strKey
	 *            指定的密钥
	 * @throws Exception
	 */
	public EncryptUtil(String strKey, Type type) 
			throws NoSuchAlgorithmException, InvalidKeyException, Exception{
		if (type == Type.DES) {
			String transformation = "DES";
			String transformationKey = "DES";
			int len = 8;
			Key key = getKey(strKey.getBytes(Constant.UTF_CODE),transformationKey, len);

			encryptCipher = Cipher.getInstance(transformation);
			encryptCipher.init(Cipher.ENCRYPT_MODE, key);

			decryptCipher = Cipher.getInstance(transformation);
			decryptCipher.init(Cipher.DECRYPT_MODE, key);
		} else {
			String transformation = "AES/ECB/PKCS5Padding";
			String transformationKey = "AES";
			int len = 16;
			Key key = getKey(strKey.getBytes(Constant.UTF_CODE),transformationKey, len);
			encryptCipher = Cipher.getInstance(transformation);
			encryptCipher.init(Cipher.ENCRYPT_MODE, key);
			decryptCipher = Cipher.getInstance(transformation);
			decryptCipher.init(Cipher.DECRYPT_MODE, key);
		}
		
	}

	/**
	 * 加密字节数组
	 * 
	 * @param arrB
	 *            需加密的字节数组
	 * @return 加密后的字节数组
	 * @throws Exception
	 */
	public byte[] encrypt(byte[] arrB) throws Exception {
		return encryptCipher.doFinal(arrB);
	}

	/**
	 * 加密字符串
	 * 
	 * @param strIn
	 *            需加密的字符串
	 * @return 加密后的字符串
	 * @throws Exception
	 */
	public String encrypt(String strIn) throws Exception {
		return byteArr2HexStr(encrypt(strIn.getBytes(Constant.UTF_CODE)));
	}
	
	/**
	 * 加密字符串
	 * 
	 * @param startsWith 
	 *            加密具有特殊前缀的字符串
	 * @param strIn
	 *            需加密的字符串
	 * @return 加密后的字符串
	 * @throws Exception
	 */
	public String encrypt(String startsWith, String strIn) throws Exception {
		return startsWith + byteArr2HexStr(encrypt(strIn.getBytes(Constant.UTF_CODE)));
	}

	/**
	 * 解密字节数组
	 * 
	 * @param arrB
	 *            需解密的字节数组
	 * @return 解密后的字节数组
	 * @throws Exception
	 */
	public byte[] decrypt(byte[] arrB) throws Exception {
		return decryptCipher.doFinal(arrB);
	}

	/**
	 * 解密字符串
	 * 
	 * @param strIn
	 *            需解密的字符串
	 * @return 解密后的字符串
	 * @throws Exception
	 */
	public String decrypt(String strIn) throws Exception {
		return new String(decrypt(hexStr2ByteArr(strIn)),Constant.UTF_CODE);
	}
	
	/**
	 * 解密字符串
	 * 
	 * @param startsWith 
	 *            解密具有特殊前缀的字符串
	 * @param strIn
	 *            需解密的字符串
	 * @return 解密后的字符串
	 * @throws Exception
	 */
	public String decrypt(String startsWith, String strIn) throws Exception {
		if(strIn.startsWith(startsWith)){
			return new String(decrypt(hexStr2ByteArr(strIn.substring(startsWith.length()))),Constant.UTF_CODE);
		} else {
			return strIn;
		}
	}

	/**
	 * 从指定字符串生成密钥，密钥所需的字节数组长度为8位 不足8位时后面补0，超出8位只取前8位
	 * 
	 * @param arrBTmp
	 *            构成该字符串的字节数组
	 * @return 生成的密钥
	 * @throws java.lang.Exception
	 */
	private Key getKey(byte[] arrBTmp, String transformationKey, int len) {
		// 创建一个空的8位字节数组（默认值为0）
		byte[] arrB = new byte[len];

		// 将原始字节数组转换为8位
		for (int i = 0; i < arrBTmp.length && i < arrB.length; i++) {
			arrB[i] = arrBTmp[i];
		}

		// 生成密钥
		Key key = new javax.crypto.spec.SecretKeySpec(arrB, transformationKey);

		return key;
	}
	
	/**
	 * 使用SHA加密算法加密字符串
	 * @param s
	 * @param algorithm 加密算法
	 * @return
	 * @throws Exception
	 */
	public static String encrypt(String s, Enum<Algorithm> algorithm) {
		MessageDigest sha;
		try {
			sha = MessageDigest.getInstance(algorithm.name());
			byte[] obj = s.getBytes(Constant.UTF_CODE);
			sha.update(obj);

			return "{" + algorithm + "}" + byte2hex(sha.digest());
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return null;
	}

	/**
	 * 二进制转换成十六进制的字符串
	 * 
	 * @param b
	 * @return
	 */
	public static String byte2hex(byte[] b) { // 二进制转字符串
		StringBuilder sb = new StringBuilder();
		for (int n = 0; n < b.length; n++) {
			String stmp = (java.lang.Integer.toHexString(b[n] & 0XFF));
			if (stmp.length() == 1) {
				sb.append("0").append(stmp);
			} else {
				sb.append(stmp);
			}
		}
		return sb.toString().toUpperCase();
	}

	/**
	 * 将16进制的String转换成byte[]
	 * 
	 * @param hex
	 * @return
	 */
	public static byte[] hex2byte(String hex) {
		int length = hex.length();
		byte[] result = new byte[length / 2];
		for (int i = 0; i < length; i += 2) {
			result[i / 2] = Integer.valueOf(hex.substring(i, i + 2), 16)
					.byteValue();
		}
		return result;
	}
	
	public static String getEncryptValue(String propVal, EncryptUtil... enstance) {
		//解密操作
		try {
			return getEncryptValue(EncryptUtil.START_WITH, propVal, enstance);
		} catch (Exception ex) {
			return propVal;
		}
	}
	public static String getEncryptValue(String startWith, String propVal, EncryptUtil... enstance) {
		//解密操作
		try {
			if (enstance != null && enstance.length > 0) {
				return enstance[0].encrypt(startWith, propVal);
			}
			return EncryptUtil.getInstance().encrypt(startWith, propVal);
		} catch (Exception ex) {
			return propVal;
		}
	}
	public static String getDecryptValue(String propVal, EncryptUtil... enstance) {
		//解密操作
		try {
			return getDecryptValue(EncryptUtil.START_WITH, propVal, enstance);
		} catch (Exception ex) {
			return propVal;
		}
	}
	public static String getDecryptValue(String startWith, String propVal, EncryptUtil... enstance) {
		//解密操作
		try {
			if (enstance != null && enstance.length > 0) {
				return enstance[0].decrypt(startWith, propVal);
			}
			return EncryptUtil.getInstance().decrypt(startWith, propVal);
		} catch (Exception ex) {
			return propVal;
		}
	}
	
	/**
	 * Main主函数输出加密结果
	 * 
	 * @param args
	 * @return
	 */
	public static void main(String[] args) throws Exception { 
		
		EncryptUtil en = EncryptUtil.getInstance("gaoprd0201abcdaa",Type.AES);
		String result = en.encrypt(START_WITH, "polaris-adf-asystem-call-001");
		System.out.println(result);
		result = en.decrypt(START_WITH, result);
		System.out.println(result);
		en = EncryptUtil.getInstance("gaoprd0201abcdbb",Type.AES);
		result = en.encrypt(START_WITH, "polaris-adf-asystem-call-001");
		System.out.println(result);
		result = en.decrypt(START_WITH, result);
		System.out.println(result);
		en = EncryptUtil.getInstance("gaoprd0201abcdcc",Type.AES);
		result = en.encrypt(START_WITH, "polaris-adf-asystem-call-001");
		System.out.println(result);
		result = en.decrypt(START_WITH, result);
		System.out.println(result);
    }
}


