package halo.pos;

import halo.pos.fileparser.FieldMapper;
import halo.pos.util.ISOUtil;
import halo.pos.util.PosUtil;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 负责报文的解析与组装
 * 
 * @author akwei
 */
public class PosMsg {

	public static final String LOG_HEAD = "[POS MSG] ";

	private static final Log log = LogFactory.getLog(PosBody.class);

	/**
	 * tpdu长度
	 */
	private static final int LEN_POS_MSG_TPDU_IN_BCD = 5;

	/**
	 * 报文头长度
	 */
	private static final int LEN_POS_MSG_HEADER_IN_BCD = 6;

	private static boolean logInfo;

	private String tpdu;

	private PosHeader posHeader;

	private PosBody posBody;

	public static void setLogInfo(boolean logInfo) {
		PosMsg.logInfo = logInfo;
	}

	public static boolean isLogInfo() {
		return logInfo;
	}

	/**
	 * 解析报文内容
	 * 
	 * @param hex 字节数组的16进制表示
	 * @param hasMsgLength
	 * @param fieldMapper
	 * @return
	 * @throws Exception
	 */
	public static PosMsg parseHex(String hex, boolean hasMsgLength,
	        FieldMapper fieldMapper) throws Exception {
		return parse(ISOUtil.hex2byte(hex), hasMsgLength, fieldMapper);
	}

	/**
	 * 解析报文内容<br>
	 * 
	 * @param data
	 * @param hasMsgLength 是否有消息长度的字节,消息长度=报文头+消息体,2个字节表示
	 * @param fieldMapper
	 * @throws Exception
	 */
	public static PosMsg parse(byte[] data, boolean hasMsgLength,
	        FieldMapper fieldMapper) throws Exception {
		if (hasMsgLength) {
			int lenDescr = 2;
			byte[] lenByte = new byte[lenDescr];
			System.arraycopy(data, 0, lenByte, 0, lenDescr);
			int msgLen = PosUtil.getMsgLen(lenByte);
			if (logInfo) {
				log.info(LOG_HEAD + "msgLen: " + ISOUtil.hexString(lenByte));
			}
			if (msgLen != data.length - lenDescr) {
				throw new PosRuntimeException(
				        "pos msg length error msglen must=" + msgLen
				                + ",but now " + (data.length - lenDescr));
			}
		}
		int msgPos = 0;
		if (hasMsgLength) {
			msgPos += 2;
		}
		if (data.length <= msgPos + LEN_POS_MSG_TPDU_IN_BCD
		        + LEN_POS_MSG_HEADER_IN_BCD) {
			throw new PosRuntimeException(
			        "pos_iso_8583 msg length error [" + data.length + "]");
		}
		int tpduPos = msgPos;
		PosMsg msg = new PosMsg();
		// tpdu bytes
		byte[] tpduBytes = new byte[LEN_POS_MSG_TPDU_IN_BCD];
		System.arraycopy(data, tpduPos, tpduBytes, 0, LEN_POS_MSG_TPDU_IN_BCD);
		// build tpdu
		msg.setTpdu(ISOUtil.bcd2str(tpduBytes, 0, tpduBytes.length * 2, false));
		if (logInfo) {
			log.info(LOG_HEAD + "tpdu: " + ISOUtil.hexString(tpduBytes));
		}
		// header bytes
		int headerPos = tpduPos + LEN_POS_MSG_TPDU_IN_BCD;
		byte[] headerBytes = new byte[LEN_POS_MSG_HEADER_IN_BCD];
		System.arraycopy(data, headerPos, headerBytes, 0,
		        LEN_POS_MSG_HEADER_IN_BCD);
		if (logInfo) {
			log.info(LOG_HEAD + "header: " + ISOUtil.hexString(headerBytes));
		}
		PosHeader header = PosHeader.parse(headerBytes);
		int bodyPos = headerPos + LEN_POS_MSG_HEADER_IN_BCD;
		int bodyLen = data.length - headerPos - LEN_POS_MSG_HEADER_IN_BCD;
		byte[] bodyBytes = new byte[bodyLen];
		System.arraycopy(data, bodyPos, bodyBytes, 0, bodyLen);
		PosBody body = PosBody.parse(bodyBytes, fieldMapper);
		msg.setPosHeader(header);
		msg.setPosBody(body);
		return msg;
	}

	public byte[] build(boolean hasLength) throws Exception {
		byte[] tpdub = ISOUtil.str2bcd(this.tpdu, true);
		byte[] headerb = this.posHeader.build();
		byte[] bodyb = this.posBody.build();
		byte[] b = new byte[tpdub.length + headerb.length +
		        +bodyb.length];
		int begin = 0;
		System.arraycopy(tpdub, 0, b, begin, tpdub.length);
		begin = begin + tpdub.length;
		System.arraycopy(headerb, 0, b, begin, headerb.length);
		begin = begin + headerb.length;
		System.arraycopy(bodyb, 0, b, begin, bodyb.length);
		if (hasLength) {
			int len = b.length;
			byte[] lenbytes = PosUtil.getByteMsgLen(len);
			byte[] result = new byte[lenbytes.length + b.length];
			System.arraycopy(lenbytes, 0, result, 0, lenbytes.length);
			System.arraycopy(b, 0, result, lenbytes.length, b.length);
			return result;
		}
		return b;
	}

	public String buildToHex(boolean hasLength) throws Exception {
		byte[] b = this.build(hasLength);
		return ISOUtil.hexString(b);
	}

	public void setPosBody(PosBody posBody) {
		this.posBody = posBody;
	}

	public void setPosHeader(PosHeader posHeader) {
		this.posHeader = posHeader;
	}

	public PosBody getPosBody() {
		return posBody;
	}

	public PosHeader getPosHeader() {
		return posHeader;
	}

	public void setTpdu(String tpdu) {
		this.tpdu = tpdu;
	}

	public String getTpdu() {
		return tpdu;
	}
}
