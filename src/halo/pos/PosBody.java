package halo.pos;

import halo.pos.fileparser.FieldCnf;
import halo.pos.fileparser.FieldMapper;
import halo.pos.util.ISOUtil;
import halo.pos.util.PosUtil;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class PosBody {

	private static final Log log = LogFactory.getLog(PosBody.class);

	private final Map<Integer, PosField> map = new HashMap<Integer, PosField>();

	/**
	 * 位图长度
	 */
	private static final int LEN_POS_MSG_BIGMAP = 8;

	private static final int LEN_POS_MSG_BIGMAP_MAXBITS = 64;

	/**
	 * 消息类型长度
	 */
	private static final int LEN_POS_MSG_MIT_IN_BCD = 2;

	private String msgTypeId;

	private BitSet bitSet;

	private FieldMapper fieldMapper;

	private PosBody() {
	}

	public PosBody(int nbits, FieldMapper fieldMapper) {
		this.bitSet = new BitSet(nbits);
		this.fieldMapper = fieldMapper;
	}

	public void setFieldMapper(FieldMapper fieldMapper) {
		this.fieldMapper = fieldMapper;
	}

	public FieldMapper getFieldMapper() {
		return fieldMapper;
	}

	public void setMsgTypeId(String msgTypeId) {
		this.msgTypeId = msgTypeId;
	}

	public String getMsgTypeId() {
		return msgTypeId;
	}

	public void setBitSet(BitSet bitSet) {
		this.bitSet = bitSet;
	}

	public BitSet getBitSet() {
		return bitSet;
	}

	/**
	 * 添加域数据,不需要添加最后一个校验位，校验位自动添加
	 * 
	 * @param posField
	 */
	public void addField(PosField posField) {
		this.bitSet.set(posField.getIndex());
		map.put(posField.getIndex(), posField);
		if (PosMsg.isLogInfo()) {
			log.info(PosMsg.LOG_HEAD + "field " + posField.getIndex()
			        + " build hex :["
			        + ISOUtil.hexString(posField.getBuildedValue()) + "]");
		}
	}

	/**
	 * 添加域数据，添加的域信息需要与配置文件对应
	 * 
	 * @param index 域下标
	 * @param originValue
	 * @throws Exception
	 */
	public void addField(int index, Object originValue) throws Exception {
		FieldCnf fieldCnf = this.fieldMapper.getFieldCnf(index);
		if (fieldCnf == null) {
			throw new PosRuntimeException("no field " + index + " config");
		}
		if (fieldCnf.isFixLen()) {
			PosFixedLenField field = this.createFixLenField(fieldCnf);
			field.build(originValue);
			this.addField(field);
		}
		else {
			PosVarLenField field = this.createVarLenField(fieldCnf);
			field.build(originValue);
			this.addField(field);
		}
	}

	public byte[] build() throws Exception {
		if (this.msgTypeId == null) {
			throw new PosRuntimeException("body must set msgTypeId");
		}
		byte[] mitb = ISOUtil.str2bcd(this.msgTypeId, true);
		List<byte[]> list = new ArrayList<byte[]>();
		byte[] bitsetb = ISOUtil.bitSet2byte(this.bitSet);
		int len = 0;
		for (int i = 0; i < bitSet.length(); i++) {
			if (bitSet.get(i)) {
				int num = i;// 域位
				PosField field = this.map.get(num);
				len = len + field.getBuildedValue().length;
				list.add(field.getBuildedValue());
			}
		}
		int begin = 0;
		byte[] b = new byte[mitb.length + bitsetb.length + len];
		System.arraycopy(mitb, 0, b, begin, mitb.length);
		begin = begin + mitb.length;
		System.arraycopy(bitsetb, 0, b, begin, bitsetb.length);
		begin = begin + bitsetb.length;
		for (byte[] by : list) {
			System.arraycopy(by, 0, b, begin, by.length);
			begin = begin + by.length;
		}
		return b;
	}

	/**
	 * 默认的数据mac签名算法
	 * 
	 * @param posMacEnc
	 * @return
	 * @throws Exception
	 */
	public PosMacField createMacField(PosMacEnc posMacEnc) throws Exception {
		byte[] b = this.build();
		PosMacField macField = new PosMacField();
		macField.setPosMacEnc(posMacEnc);
		macField.build(b);
		return macField;
	}

	public static PosBody parse(byte[] data, FieldMapper fieldMapper)
	        throws Exception {
		PosBody body = new PosBody();
		body.setFieldMapper(fieldMapper);
		int mitPos = 0;
		byte[] mitBytes = new byte[LEN_POS_MSG_MIT_IN_BCD];
		System.arraycopy(data, mitPos, mitBytes, 0, LEN_POS_MSG_MIT_IN_BCD);
		if (PosMsg.isLogInfo()) {
			log.info(PosMsg.LOG_HEAD + "mit: [" + ISOUtil.hexString(mitBytes)
			        + "]");
		}
		body.setMsgTypeId(ISOUtil.bcd2str(mitBytes, 0,
		        LEN_POS_MSG_MIT_IN_BCD * 2,
		        false));
		int bitMapPos = mitPos + LEN_POS_MSG_MIT_IN_BCD;
		byte[] bitMapBytes = new byte[LEN_POS_MSG_BIGMAP];
		System.arraycopy(data, bitMapPos, bitMapBytes, 0, LEN_POS_MSG_BIGMAP);
		BitSet bitSet = ISOUtil.byte2BitSet(bitMapBytes, 0,
		        LEN_POS_MSG_BIGMAP_MAXBITS);
		body.setBitSet(bitSet);
		int curIndex = bitMapPos + LEN_POS_MSG_BIGMAP;
		if (PosMsg.isLogInfo()) {
			log.info(PosMsg.LOG_HEAD + "bitmap: ["
			        + ISOUtil.hexString(bitMapBytes) + "]");
		}
		for (int i = 0; i < bitSet.length(); i++) {
			if (bitSet.get(i)) {
				int num = i;// 域位
				FieldCnf fieldCnf = fieldMapper.getFieldCnf(num);
				if (fieldCnf == null) {
					throw new PosMissFieldException("miss field " + num);
				}
				curIndex = body.parseField(data, curIndex, fieldCnf);
			}
		}
		return body;
	}

	private int getFieldBuildType(String str) {
		if (str.equals("bcd")) {
			return PosField.BUILD_TYPE_BCD;
		}
		if (str.equals("ascii")) {
			return PosField.BUILD_TYPE_ASCII;
		}
		if (str.equals("binary")) {
			return PosField.BUILD_TYPE_BINARY;
		}
		throw new RuntimeException("unknown buildType [" + str + "]");
	}

	private PosFixedLenField createFixLenField(FieldCnf fieldCnf) {
		PosFixedLenField field = new PosFixedLenField();
		field.setIndex(fieldCnf.getIndex());
		field.setPadLeft(fieldCnf.getBoolean("padLeft"));
		field.setBuildType(getFieldBuildType(fieldCnf.get("buildType")));
		field.setDescription(fieldCnf.get("description"));
		field.setBuildedLength(fieldCnf.getInt("buildedLength"));
		field.setLength(fieldCnf.getInt("length"));
		return field;
	}

	private PosVarLenField createVarLenField(FieldCnf fieldCnf) {
		PosVarLenField field = new PosVarLenField();
		field.setIndex(fieldCnf.getIndex());
		field.setPadLeft(fieldCnf.getBoolean("padLeft"));
		field.setDescription(fieldCnf.get("description"));
		field.setBuildType(getFieldBuildType(fieldCnf.get("buildType")));
		field.setBuildedLengthDescr(fieldCnf.getInt("buildedLengthDescr"));
		field.setLengthDescr(fieldCnf.getInt("lengthDescr"));
		return field;
	}

	public int parseField(byte[] data, int curIdx, FieldCnf fieldCnf)
	        throws Exception {
		byte[] sub = null;
		int idx = 0;
		if (fieldCnf.isFixLen()) {
			PosFixedLenField field = this.createFixLenField(fieldCnf);
			sub = PosUtil.subBytes(data, curIdx, field.getBuildedLength());
			if (PosMsg.isLogInfo()) {
				log.info(PosMsg.LOG_HEAD + "field " + field.getIndex() + ": ["
				        + ISOUtil.hexString(sub) + "]");
			}
			idx = curIdx + field.getBuildedLength();
			field.parse(sub);
			map.put(field.getIndex(), field);
			if (PosMsg.isLogInfo()) {
				log.info(PosMsg.LOG_HEAD + "field " + field.getIndex()
				        + " value: [" + field.getParsedValue() + "]");
			}
		}
		else {
			PosVarLenField field = this.createVarLenField(fieldCnf);
			field.setLength(PosField.parseLength(data, curIdx,
			        field.getBuildedLengthDescr()));
			int datalen = 0;
			if (field.getBuildType() == PosField.BUILD_TYPE_BCD) {
				datalen = (field.getLength() + 1) / 2;
			}
			else {
				datalen = field.getLength();
			}
			sub = PosUtil.subBytes(data, curIdx,
			        datalen + field.getBuildedLengthDescr());
			if (PosMsg.isLogInfo()) {
				log.info(PosMsg.LOG_HEAD + "field " + field.getIndex() + ": ["
				        + ISOUtil.hexString(sub) + "]");
			}
			field.parse(sub);
			idx = curIdx + datalen + field.getBuildedLengthDescr();
			map.put(field.getIndex(), field);
			if (PosMsg.isLogInfo()) {
				log.info(PosMsg.LOG_HEAD + "field " + field.getIndex()
				        + " value: [" + field.getParsedValue() + "]");
			}
		}
		return idx;
	}
}
