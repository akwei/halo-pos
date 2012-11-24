package halo.pos;

import halo.pos.util.ISOUtil;

import java.util.HashMap;
import java.util.Map;

public abstract class PosField {

	private final Map<String, Object> context = new HashMap<String, Object>();

	public static int BUILD_TYPE_BCD = 0;

	public static int BUILD_TYPE_ASCII = 1;

	public static int BUILD_TYPE_BINARY = 2;

	/**
	 * 原始内容长度
	 */
	protected int length;

	/**
	 * 组装内容类型 参考 {@link PosField#BUILD_TYPE_ASCII ,PosField#BUILD_TYPE_BCD}
	 */
	protected int buildType;

	/**
	 * 组装前，内容是否左填充
	 */
	protected boolean padLeft;

	/**
	 * 域描述
	 */
	protected String description;

	/**
	 * 域位置，下标从>0开始，可自定义
	 */
	protected int index;

	/**
	 * 内容字符集，默认utf-8
	 */
	protected String charsetName = "utf-8";

	/**
	 * 保存已经编码之后的数据
	 */
	protected byte[] buildedValue;

	/**
	 * 保存解码之后的数据
	 */
	protected Object parsedValue;

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public int getBuildType() {
		return buildType;
	}

	public void setBuildType(int buildType) {
		this.buildType = buildType;
	}

	public boolean isPadLeft() {
		return padLeft;
	}

	public void setPadLeft(boolean padLeft) {
		this.padLeft = padLeft;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public int getIndex() {
		return index;
	}

	public String getCharsetName() {
		return charsetName;
	}

	public void setCharsetName(String charsetName) {
		this.charsetName = charsetName;
	}

	public void putToContext(String key, Object value) {
		context.put(key, value);
	}

	public Object getFromContext(String key) {
		return this.context.get(key);
	}

	public Object getParsedValue() {
		return parsedValue;
	}

	public byte[] getBuildedValue() {
		return buildedValue;
	}

	/**
	 * @param a
	 * @param offset
	 * @param buildedLengthDescr 描述内容长度信息的数据bcd压缩所后的长度
	 * @return
	 */
	public static int parseLength(byte[] a, int offset, int buildedLengthDescr) {
		String v = ISOUtil.bcd2str(a, offset, buildedLengthDescr * 2, true);
		return Integer.parseInt(v);
	}

	/**
	 * 编码
	 * 
	 * @param originValue
	 * @throws Exception
	 */
	public abstract void build(Object originValue) throws Exception;

	/**
	 * 解码
	 * 
	 * @param buildedValue
	 * @throws Exception
	 */
	public abstract void parse(byte[] buildedValue) throws Exception;
}