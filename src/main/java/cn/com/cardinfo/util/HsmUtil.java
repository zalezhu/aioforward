package cn.com.cardinfo.util;


/**
 * 基础功能
 * 
 * @author JueYue
 * @date 2015年5月18日 上午11:41:09
 */
public final class HsmUtil {

	private HsmUtil() {
	}

	public static byte[] hexStringToByte(String hex) {
		int len = (hex.length() / 2);
		byte[] result = new byte[len];
		char[] achar = hex.toCharArray();
		for (int i = 0; i < len; i++) {
			int pos = i * 2;
			result[i] = (byte) (toByte(achar[pos]) << 4 | toByte(achar[pos + 1]));
		}
		return result;
	}

	private static byte toByte(char c) {
		byte b = (byte) "0123456789ABCDEF".indexOf(c);
		return b;
	}

	public static final String bytesToHexString(byte[] bArray) {
		StringBuffer sb = new StringBuffer(bArray.length);
		String sTemp;
		for (int i = 0; i < bArray.length; i++) {
			sTemp = Integer.toHexString(0xFF & bArray[i]);
			if (sTemp.length() < 2)
				sb.append(0);
			sb.append(sTemp.toUpperCase());
		}
		return sb.toString();
	}

	/**
	 * 合并
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public static byte[] byteAdd(byte[] a, byte[] b) {
		byte[] ret = new byte[a.length + b.length];
		System.arraycopy(a, 0, ret, 0, a.length);
		System.arraycopy(b, 0, ret, a.length, b.length);
		return ret;
	}

	/**
	 * 合并
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public static byte[] byteAdd(byte[] a, Byte... b) {
		byte[] ret = new byte[a.length + b.length];
		System.arraycopy(a, 0, ret, 0, a.length);
		for (int i = 0, le = b.length; i < le; i++) {
			ret[a.length + i] = b[i];
		}
		//System.arraycopy(b, 0, ret, a.length, b.length);
		return ret;
	}

	/**
	 * 包长度计算
	 * 
	 * @param cmdLen
	 * @param lengthBytes
	 * @return
	 */
	public static byte[] getPackageLenByte(int cmdLen, int lengthBytes) {
		int l = cmdLen;
		byte[] buf = new byte[lengthBytes];
		int pos = 0;
		if (lengthBytes == 4) {
			buf[0] = (byte) ((l & 0xff000000) >> 24);
			pos++;
		}
		if (lengthBytes > 2) {
			buf[pos] = (byte) ((l & 0xff0000) >> 16);
			pos++;
		}
		if (lengthBytes > 1) {
			buf[pos] = (byte) ((l & 0xff00) >> 8);
			pos++;
		}
		buf[pos] = (byte) (l & 0xff);
		return buf;
	}

	/**
	 * 16进制字符串异或
	 * 
	 * @param first
	 * @param second
	 * @return
	 */
	public static String hexXor(String first, String second) {
		int le = first.length() / 2;
		StringBuilder tempStr = new StringBuilder();
		for (int i = 0; i < le; i++) {
			Integer temp = Integer.parseInt(
					first.substring(i * 2, (i + 1) * 2), 16)
					^ Integer
							.parseInt(second.substring(i * 2, (i + 1) * 2), 16);
			String hex = Integer.toHexString(temp);
			if (hex.length() == 1) {
				tempStr.append("0");
			}
			tempStr.append(hex.toUpperCase());
		}
		return tempStr.toString();
	}
	
	  /** 
     * @功能: 10进制串转为BCD码 
     * @参数: 10进制串 
     * @结果: BCD码 
     */  
    public static byte[] str2Bcd(String asc) {  
        int len = asc.length();  
        int mod = len % 2;  
        if (mod != 0) {  
            asc = "0" + asc;  
            len = asc.length();  
        }  
        byte abt[] = new byte[len];  
        if (len >= 2) {  
            len = len / 2;  
        }  
        byte bbt[] = new byte[len];  
        abt = asc.getBytes();  
        int j, k;  
        for (int p = 0; p < asc.length() / 2; p++) {  
            if ((abt[2 * p] >= '0') && (abt[2 * p] <= '9')) {  
                j = abt[2 * p] - '0';  
            } else if ((abt[2 * p] >= 'a') && (abt[2 * p] <= 'z')) {  
                j = abt[2 * p] - 'a' + 0x0a;  
            } else {  
                j = abt[2 * p] - 'A' + 0x0a;  
            }  
            if ((abt[2 * p + 1] >= '0') && (abt[2 * p + 1] <= '9')) {  
                k = abt[2 * p + 1] - '0';  
            } else if ((abt[2 * p + 1] >= 'a') && (abt[2 * p + 1] <= 'z')) {  
                k = abt[2 * p + 1] - 'a' + 0x0a;  
            } else {  
                k = abt[2 * p + 1] - 'A' + 0x0a;  
            }  
            int a = (j << 4) + k;  
            byte b = (byte) a;  
            bbt[p] = b;  
        }  
        return bbt;  
    }



}
