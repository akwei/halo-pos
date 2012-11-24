package halo.pos.util;

import halo.pos.PosMacEnc;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PosUtil {

	/**
	 * 金额 以分为单位
	 * 
	 * @param value
	 * @return
	 */
	public static String getRMBValue(int value) {
		DecimalFormat format = new DecimalFormat("000000000000");
		return format.format(value);
	}

	/**
	 * 金额 外币，如果没有小数位就直接，垓值就为金额。如果有2位小数，方式同RMB.如果有3位小数，最后小数位为0
	 * 
	 * @param d
	 * @return
	 */
	public static String getForeignCurrencyValue(double d) {
		DecimalFormat format = new DecimalFormat("000000000000");
		long v = (long) d;
		if (v == d) {
			return format.format(v);
		}
		String s = String.valueOf(d);
		int idx = s.indexOf(".");
		int len = s.length() - idx - 1;
		// 小数位2位
		if (len == 2) {
			long n = (long) (d * 100);
			return format.format(n);
		}
		// 小数3位
		else if (len == 3) {
			BigDecimal bd = new BigDecimal(d);
			BigDecimal res = bd.divide(new BigDecimal(1), 2,
			        RoundingMode.DOWN);
			long n = (long) (res.doubleValue() * 1000);
			return format.format(n);
		}
		else {
			throw new RuntimeException("decimal length " + len
			        + "is not supported");
		}
	}

	public static String formatDate(String format, Date date) {
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		return sdf.format(date);
	}

	public static byte[] subBytes(byte[] data, int begin, int length) {
		byte[] b = new byte[length];
		System.arraycopy(data, begin, b, 0, length);
		return b;
	}

	public static List<byte[]> parseByteArray(byte[] by, int count) {
		int byteLen = 8;
		List<byte[]> list = new ArrayList<byte[]>();
		int begin = 0;
		for (int i = 0; i < count; i++) {
			begin = byteLen * i;
			byte[] a = new byte[byteLen];
			System.arraycopy(by, begin, a, 0, byteLen);
			list.add(a);
		}
		return list;
	}

	public static byte[] paddEnd(byte[] b, byte add) {
		int pad = b.length % 8;
		if (pad == 0) {
			return b;
		}
		int padlen = 8 - pad;
		byte[] a = new byte[b.length + padlen];
		System.arraycopy(b, 0, a, 0, b.length);
		int begin = b.length;
		for (int i = 0; i < padlen; i++) {
			a[begin] = add;
			begin++;
		}
		return a;
	}

	/**
	 * 根据标准mac算法进行mac计算
	 * 
	 * @param by
	 * @return
	 * @throws Exception
	 */
	public static byte[] createMac(byte[] by, PosMacEnc posMacEnc)
	        throws Exception {
		// 1 不足8的倍数，填充
		byte[] nby = PosUtil.paddEnd(by, (byte) 0x00);
		int count = nby.length / 8;
		// 2 分组异或
		List<byte[]> list = PosUtil.parseByteArray(nby, count);
		if (list.isEmpty()) {
			throw new RuntimeException("must more than 8 bytes");
		}
		if (list.size() == 1) {
			throw new RuntimeException("must more than 8 bytes");
		}
		byte[] tmp = list.get(0);
		for (int i = 1; i < list.size(); i++) {
			tmp = ISOUtil.xor(tmp, list.get(i));
		}
		int lastLen = 8;
		byte[] last8Bytes = new byte[lastLen];
		System.arraycopy(tmp, tmp.length - lastLen - 1, last8Bytes, 0, lastLen);
		// 3 将异或运算后的最后8个字节（RESULT BLOCK）转换成16 个HEXDECIMAL
		String s = ISOUtil.hexString(last8Bytes);
		// 4 取前8 个字节用MAK加密
		// byte[] before8Bytes = new byte[8];
		// System.arraycopy(s.getBytes(), 0, before8Bytes, 0, 8);
		String before8Str = s.substring(0, 8);
		String enc_block_str = posMacEnc.encode(before8Str);
		// 5 将加密后的结果与后8 个字节异或
		byte[] xorResult = ISOUtil.xor(enc_block_str.getBytes(),
		        s.substring(8)
		                .getBytes());
		// 6 用异或的结果TEMP BLOCK 再进行一次单倍长密钥算法运算
		String enc_block_str2 = posMacEnc.encode(new String(xorResult));
		// 7 将运算后的结果（ENC BLOCK2）转换成16 个HEXDECIMAL
		String hex_block_str = ISOUtil.hexString(enc_block_str2.getBytes());
		// 8 取前8个字节作为MAC值
		return hex_block_str.substring(0, 8).getBytes();
	}

	public static int getMsgLen(byte[] blen) {
		String hexlen = Integer.toHexString(blen[0])
		        + Integer.toHexString(blen[1] & 0xff);
		return Integer.parseInt(hexlen, 16);
	}

	public static byte[] getByteMsgLen(int len) {
		byte[] buf = new byte[2];
		// 取高8位
		buf[0] = (byte) (len >> 8);
		// 取低8
		buf[1] = (byte) (len & 0xff);
		return buf;
	}
}
