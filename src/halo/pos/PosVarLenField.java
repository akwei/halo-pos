package halo.pos;

import halo.pos.util.ISOUtil;

public class PosVarLenField extends PosField {

	/**
	 * 编码后的长度描述部分的字节长度
	 */
	private int buildedLengthDescr;

	private int lengthDescr;

	public void setLengthDescr(int lengthDescr) {
		this.lengthDescr = lengthDescr;
	}

	public int getLengthDescr() {
		return lengthDescr;
	}

	public int getBuildedLengthDescr() {
		return buildedLengthDescr;
	}

	public void setBuildedLengthDescr(int buildedLengthDescr) {
		this.buildedLengthDescr = buildedLengthDescr;
	}

	@Override
	public void build(Object originValue) throws Exception {
		byte[] originValueBytes = ((String) originValue)
		        .getBytes(this.charsetName);
		int len = originValueBytes.length;
		String len_str = ISOUtil.padleft(len + "", this.lengthDescr, '0');
		byte[] a = ISOUtil.str2bcd(len_str, true);
		if (this.buildType == BUILD_TYPE_ASCII) {
			byte[] c = new byte[a.length + originValueBytes.length];
			System.arraycopy(a, 0, c, 0, a.length);
			System.arraycopy(originValueBytes, 0, c, a.length,
			        originValueBytes.length);
			this.buildedValue = c;
			return;
		}
		if (this.buildType == BUILD_TYPE_BCD) {
			byte[] b = ISOUtil.str2bcd((String) originValue, this.padLeft);
			byte[] c = new byte[a.length + b.length];
			System.arraycopy(a, 0, c, 0, a.length);
			System.arraycopy(b, 0, c, a.length, b.length);
			this.buildedValue = c;
			return;
		}
		throw new RuntimeException(this.buildType + " not  be supported");
	}

	@Override
	public void parse(byte[] buildedValue) throws Exception {
		if (this.length <= 0) {
			this.length = parseLength(buildedValue, 0, this.buildedLengthDescr);
		}
		if (this.buildType == BUILD_TYPE_ASCII) {
			byte[] a = new byte[this.length];
			System.arraycopy(buildedValue, this.buildedLengthDescr, a, 0,
			        buildedValue.length - this.buildedLengthDescr);
			this.parsedValue = new String(a, this.charsetName);
			return;
		}
		if (this.buildType == BUILD_TYPE_BCD) {
			String s = ISOUtil.bcd2str(buildedValue, this.buildedLengthDescr,
			        (buildedValue.length - this.buildedLengthDescr) * 2,
			        this.padLeft);
			if (this.length != s.length()) {
				if (this.padLeft) {
					this.parsedValue = s.substring(1);
				}
				else {
					this.parsedValue = s.substring(0, s.length() - 1);
				}
			}
			else {
				this.parsedValue = s;
			}
			return;
		}
		throw new RuntimeException(this.buildType + " not  be supported");
	}
}
