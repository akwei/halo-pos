package halo.pos;

import halo.pos.util.ISOUtil;
import halo.pos.util.PosUtil;

import java.util.List;

public class PosMacField extends PosFixedLenField {

	protected PosMacEnc posMacEnc;

	public void setPosMacEnc(PosMacEnc posMacEnc) {
		this.posMacEnc = posMacEnc;
	}

	public PosMacField() {
		this.setBuildType(BUILD_TYPE_ASCII);
		this.setLength(8);
		this.setPadLeft(false);
		this.setBuildedLength(8);
	}

	@Override
	public void build(Object originValue) throws Exception {
		this.buildedValue = this.createMac((byte[]) originValue);
	}

	public byte[] createMac(byte[] by) throws Exception {
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
}