package halo.pos;

import halo.pos.util.ISOUtil;

/**
 * 定长类型域
 * 
 * @author akwei
 */
public class PosFixedLenField extends PosField {

	/**
	 * bcd,ascii编码之后的内容长度
	 */
	private int buildedLength;

	public int getBuildedLength() {
		return buildedLength;
	}

	public void setBuildedLength(int buildedLength) {
		this.buildedLength = buildedLength;
	}

	public void build(Object originValue) throws Exception {
		if (this.buildType == BUILD_TYPE_ASCII) {
			String v = ISOUtil.padright((String) originValue, this.length, ' ');
			this.buildedValue = v.getBytes(this.charsetName);
			return;
		}
		if (this.buildType == BUILD_TYPE_BCD) {
			this.buildedValue = ISOUtil.str2bcd((String) originValue,
			        this.padLeft);
			return;
		}
		if (this.buildType == BUILD_TYPE_BINARY) {
			this.buildedValue = (byte[]) originValue;
			return;
		}
		throw new RuntimeException(this.buildType + " not  be supported");
	}

	public void parse(byte[] buildedValue) throws Exception {
		if (this.buildType == BUILD_TYPE_ASCII) {
			this.parsedValue = new String(buildedValue, this.charsetName);
			return;
		}
		if (this.buildType == BUILD_TYPE_BCD) {
			String value = ISOUtil.bcd2str(buildedValue, 0,
			        buildedValue.length * 2, false);
			byte[] valueBytes = value.getBytes(this.charsetName);
			if (this.length != valueBytes.length) {
				byte[] resultByte = new byte[valueBytes.length - 1];
				if (this.padLeft) {
					System.arraycopy(valueBytes, 1, resultByte, 0,
					        resultByte.length);
				}
				else {
					System.arraycopy(valueBytes, 0, resultByte, 0,
					        resultByte.length);
				}
				this.parsedValue = new String(resultByte, this.charsetName);
			}
			else {
				this.parsedValue = value;
			}
			return;
		}
		if (this.buildType == BUILD_TYPE_BINARY) {
			this.parsedValue = new String(buildedValue, this.charsetName);
			return;
		}
		throw new RuntimeException(this.buildType + " not  be supported");
	}
}