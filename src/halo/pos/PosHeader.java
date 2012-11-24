package halo.pos;

import halo.pos.util.ISOUtil;

public class PosHeader {

	/**
	 * 应用类别
	 */
	private String useType;

	/**
	 * 软件版本号
	 */
	private String version;

	/**
	 * 终端状态
	 */
	private String terminalStatus;

	/**
	 * 处理要求
	 */
	private String processDemand;

	/**
	 * 保留使用，默认为0
	 */
	private String obligate = "000000";

	public static PosHeader parse(byte[] data) {
		PosHeader header = new PosHeader();
		String s = ISOUtil.bcd2str(data, 0, data.length * 2, false);
		if (s.length() != 12) {
			throw new PosRuntimeException("header format error [" + s + "]");
		}
		int beginIndex = 0;
		header.setUseType(s.substring(beginIndex,
		        beginIndex + 2));
		beginIndex = beginIndex + 2;
		header.setVersion(s.substring(beginIndex,
		        beginIndex + 2));
		beginIndex = beginIndex + 2;
		header.setTerminalStatus(s.substring(beginIndex,
		        beginIndex + 1));
		beginIndex = beginIndex + 1;
		header.setProcessDemand(s.substring(beginIndex,
		        beginIndex + 1));
		beginIndex = beginIndex + 1;
		header.setObligate(s.substring(beginIndex,
		        beginIndex + 6));
		return header;
	}

	public String getUseType() {
		return useType;
	}

	public void setUseType(String useType) {
		this.useType = useType;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getVersion() {
		return version;
	}

	public String getTerminalStatus() {
		return terminalStatus;
	}

	public void setTerminalStatus(String terminalStatus) {
		this.terminalStatus = terminalStatus;
	}

	public String getProcessDemand() {
		return processDemand;
	}

	public void setProcessDemand(String processDemand) {
		this.processDemand = processDemand;
	}

	public String getObligate() {
		return obligate;
	}

	public void setObligate(String obligate) {
		this.obligate = obligate;
	}

	@Override
	public String toString() {
		return this.useType + "|" + this.version + "|"
		        + this.terminalStatus + "|" + this.processDemand + "|"
		        + this.obligate;
	}

	public byte[] build() {
		return ISOUtil.str2bcd(this.useType + this.version
		        + this.terminalStatus
		        + this.processDemand + this.obligate, true);
	}
}
