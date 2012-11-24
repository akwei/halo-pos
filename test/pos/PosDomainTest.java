package pos;

import halo.pos.PosBody;
import halo.pos.PosException;
import halo.pos.PosField;
import halo.pos.PosFixedLenField;
import halo.pos.PosHeader;
import halo.pos.PosMsg;
import halo.pos.PosVarLenField;
import halo.pos.fileparser.FieldMapper;
import halo.pos.util.ISOUtil;
import halo.pos.util.PosUtil;

import java.util.Date;

import org.junit.Assert;
import org.junit.Test;

public class PosDomainTest {

	@Test
	public void parseCz() throws Exception {
		String s = "0053600004000060220000000004003020048002C080130000000000010020003002030210003938313034333132353838373434343033343531313235313031353600082200070500033031203342463735343838";
		PosMsg.setLogInfo(true);
		FieldMapper fieldMapper = FieldMapper
		        .getFieldMapper("halo-cz-pos-request.xml");
		PosMsg.parseHex(s, true, fieldMapper);
	}

	@Test
	public void buildCz() throws Exception {
		PosMsg.setLogInfo(true);
		PosMsg posMsg = new PosMsg();
		posMsg.setTpdu("6000040000");
		PosHeader header = new PosHeader();
		header.setUseType("60");
		header.setVersion("22");
		header.setTerminalStatus("0");
		header.setProcessDemand("0");
		posMsg.setPosHeader(header);
		PosBody body = new PosBody(64,
		        FieldMapper.getFieldMapper("halo-cz-pos-request.xml"));
		body.setMsgTypeId("0400");
		body.addField(3, "000000");
		body.addField(4, PosUtil.getRMBValue(1002000));
		body.addField(11, "300203");
		body.addField(22, "021");
		body.addField(25, "00");
		body.addField(39, "98");
		body.addField(41, "10431258");
		body.addField(42, "874440345112510");
		body.addField(49, "156");
		body.addField(60, "22000705");
		body.addField(63, "01 ");
		body.addField(64, "3BF75488".getBytes());
		posMsg.setPosBody(body);
		String result = posMsg.buildToHex(true);
		String expected = "0053600004000060220000000004003020048002C080130000000000010020003002030210003938313034333132353838373434343033343531313235313031353600082200070500033031203342463735343838";
		Assert.assertEquals(expected, result);
	}

	@Test
	public void parsePay() throws Exception {
		String s = "007060000400006022000000000200302004C020C0981100000000000008880010017502100012356221550999400728D15111018546801584403130343231363935383734333530333537323232343731313536F421C8F4FFE584DC20000000000000000008220000013246394433453536";
		byte[] data = ISOUtil.hex2byte(s);
		PosMsg.setLogInfo(true);
		FieldMapper fieldMapper = FieldMapper
		        .getFieldMapper("halo-xiaofei-pos-request.xml");
		PosMsg.parse(data, true, fieldMapper);
	}

	@Test
	public void buildPay() throws Exception {
		PosMsg.setLogInfo(true);
		PosMsg posMsg = new PosMsg();
		posMsg.setTpdu("6000040000");
		PosHeader header = new PosHeader();
		header.setUseType("60");
		header.setVersion("22");
		header.setTerminalStatus("0");
		header.setProcessDemand("0");
		posMsg.setPosHeader(header);
		PosBody body = new PosBody(64,
		        FieldMapper.getFieldMapper("halo-xiaofei-pos-request.xml"));
		body.setMsgTypeId("0200");
		body.addField(3, "000000");
		body.addField(4, PosUtil.getRMBValue(88800));
		body.addField(11, "100175");
		body.addField(22, "021");
		body.addField(25, "00");
		body.addField(26, "12");
		body.addField(35, "6221550999400728=151110185468015844");
		body.addField(41, "10421695");
		body.addField(42, "874350357222471");
		body.addField(49, "156");
		body.addField(52, ISOUtil.hex2byte("F421C8F4FFE584DC"));
		body.addField(53, "2000000000000000");
		body.addField(60, "22000001");
		body.addField(64, "2F9D3E56".getBytes());
		posMsg.setPosBody(body);
		String result = posMsg.buildToHex(true);
		String expected = "007060000400006022000000000200302004C020C0981100000000000008880010017502100012356221550999400728D15111018546801584403130343231363935383734333530333537323232343731313536F421C8F4FFE584DC20000000000000000008220000013246394433453536";
		Assert.assertEquals(expected, result);
	}

	@Test
	public void header() throws PosException {
		String hex = "602200000001";
		byte[] bcd = ISOUtil.hex2byte(hex);
		PosHeader header = PosHeader.parse(bcd);
		Assert.assertEquals("60", header.getUseType());
		Assert.assertEquals("22", header.getVersion());
		Assert.assertEquals("0", header.getTerminalStatus());
		Assert.assertEquals("0", header.getProcessDemand());
		Assert.assertEquals("000001", header.getObligate());
		byte[] b = header.build();
		Assert.assertEquals(hex, ISOUtil.hexString(b));
	}

	@Test
	public void d52_parse() throws Exception {
		PosFixedLenField field = new PosFixedLenField();
		field.setIndex(52);
		field.setBuildType(PosField.BUILD_TYPE_BINARY);
		field.setBuildedLength(8);
		field.setLength(8);
		String hex = "F421C8F4FFE584DC";
		byte[] b = ISOUtil.hex2byte(hex);
		field.parse(b);
	}

	@Test
	public void d52_build() throws Exception {
		PosFixedLenField field = new PosFixedLenField();
		field.setIndex(52);
		field.setBuildType(PosField.BUILD_TYPE_BINARY);
		field.setBuildedLength(8);
		field.setLength(8);
		String hex = "F421C8F4FFE584DC";
		field.build(hex);
	}

	@Test
	public void d60_build() throws Exception {
		PosVarLenField field = new PosVarLenField();
		field.setBuildedLengthDescr(2);
		field.setLengthDescr(3);
		field.setBuildType(PosField.BUILD_TYPE_BCD);
		field.setIndex(60);
		field.setPadLeft(false);
		String s = "22000001";
		field.build(s);
		Assert.assertEquals("000822000001",
		        ISOUtil.hexString(field.getBuildedValue()));
	}

	public static void main(String[] args) {
		String s = "8";
		byte[] b = ISOUtil.str2bcd(s, true);
		System.out.println(b.length);
	}

	@Test
	public void d2_parse() throws Exception {
		PosVarLenField field = new PosVarLenField();
		field.setBuildedLengthDescr(1);
		field.setLengthDescr(2);
		field.setBuildType(PosField.BUILD_TYPE_BCD);
		field.setIndex(2);
		field.setPadLeft(false);
		String hex = "165477666265921222";
		byte[] bcd = ISOUtil.hex2byte(hex);
		field.parse(bcd);
		Assert.assertEquals("5477666265921222", field.getParsedValue());
	}

	@Test
	public void d2_build() throws Exception {
		String s = "5477666265921222";
		PosVarLenField field = new PosVarLenField();
		field.setBuildedLengthDescr(1);
		field.setLengthDescr(2);
		field.setBuildType(PosField.BUILD_TYPE_BCD);
		field.setIndex(2);
		field.setPadLeft(false);
		field.build(s);
		String msgStr = ISOUtil.hexString(field.getBuildedValue());
		Assert.assertEquals("165477666265921222", msgStr);
	}

	@Test
	public void d3_parse() throws Exception {
		String hex = "004000";
		byte[] bcd = ISOUtil.hex2byte(hex);
		PosFixedLenField field = new PosFixedLenField();
		field.setBuildedLength(3);
		field.setBuildType(PosField.BUILD_TYPE_BCD);
		field.setIndex(3);
		field.setLength(6);
		field.parse(bcd);
		Assert.assertEquals("004000", field.getParsedValue());
	}

	@Test
	public void d3_build() throws Exception {
		String s = "004000";
		PosFixedLenField field = new PosFixedLenField();
		field.setBuildedLength(3);
		field.setBuildType(PosField.BUILD_TYPE_BCD);
		field.setIndex(3);
		field.setLength(6);
		field.build(s);
		String msgStr = ISOUtil.hexString(field.getBuildedValue());
		Assert.assertEquals("004000", msgStr);
	}

	@Test
	public void d4_parse() throws Exception {
		String hex = "000000100002";
		byte[] bcd = ISOUtil.hex2byte(hex);
		PosFixedLenField field = new PosFixedLenField();
		field.setLength(12);
		field.setBuildedLength(6);
		field.setPadLeft(false);
		field.setIndex(4);
		field.setBuildType(PosField.BUILD_TYPE_BCD);
		field.parse(bcd);
		int money = Integer.parseInt((String) field.getParsedValue());
		Assert.assertEquals(100002, money);
	}

	@Test
	public void d4_build() throws Exception {
		{
			int d = 100002;
			PosFixedLenField field = new PosFixedLenField();
			field.setLength(12);
			field.setBuildedLength(6);
			field.setPadLeft(false);
			field.setIndex(4);
			field.setBuildType(PosField.BUILD_TYPE_BCD);
			String v = PosUtil.getRMBValue(d);
			field.build(v);
			String msgStr = ISOUtil.hexString(field.getBuildedValue());
			Assert.assertEquals("000000100002", msgStr);
		}
		{
			double d = 1000.112;
			PosFixedLenField field = new PosFixedLenField();
			field.setLength(12);
			field.setBuildedLength(6);
			field.setPadLeft(false);
			field.setIndex(4);
			field.setBuildType(PosField.BUILD_TYPE_BCD);
			String v = PosUtil.getForeignCurrencyValue(d);
			field.build(v);
			String msgStr = ISOUtil.hexString(field.getBuildedValue());
			Assert.assertEquals("000001000110", msgStr);
		}
	}

	@Test
	public void d11_parse() throws Exception {
		String hex = "000073";
		PosFixedLenField field = new PosFixedLenField();
		field.setBuildedLength(3);
		field.setLength(6);
		field.setBuildType(PosField.BUILD_TYPE_BCD);
		field.setIndex(11);
		byte[] bcd = ISOUtil.hex2byte(hex);
		field.parse(bcd);
		Assert.assertEquals("000073", field.getParsedValue());
	}

	@Test
	public void d11_build() throws Exception {
		String s = "555556";
		PosFixedLenField field = new PosFixedLenField();
		field.setBuildedLength(3);
		field.setLength(6);
		field.setBuildType(PosField.BUILD_TYPE_BCD);
		field.setIndex(11);
		field.build(s);
		String msgStr = ISOUtil.hexString(field.getBuildedValue());
		Assert.assertEquals(s, msgStr);
	}

	@Test
	public void d12_parse() throws Exception {
		String hex = "195929";
		byte[] bcd = ISOUtil.hex2byte(hex);
		PosFixedLenField field = new PosFixedLenField();
		field.setBuildedLength(3);
		field.setLength(6);
		field.setBuildType(PosField.BUILD_TYPE_BCD);
		field.setIndex(12);
		field.parse(bcd);
		Assert.assertEquals("195929", field.getParsedValue());
	}

	@Test
	public void d12_build() throws Exception {
		Date date = new Date();
		PosFixedLenField field = new PosFixedLenField();
		field.setBuildedLength(3);
		field.setLength(6);
		field.setBuildType(PosField.BUILD_TYPE_BCD);
		field.setIndex(12);
		field.build(PosUtil.formatDate("HHmmss", date));
		String msgStr = ISOUtil.hexString(field.getBuildedValue());
		Assert.assertEquals(field.getLength(), msgStr.length());
	}

	@Test
	public void d22_parse() throws Exception {
		String hex = "0220";
		byte[] bcd = ISOUtil.hex2byte(hex);
		PosFixedLenField field = new PosFixedLenField();
		field.setBuildedLength(2);
		field.setLength(3);
		field.setBuildType(PosField.BUILD_TYPE_BCD);
		field.setIndex(22);
		field.setPadLeft(false);
		field.parse(bcd);
		Assert.assertEquals("022", field.getParsedValue());
	}

	@Test
	public void d22_build() throws Exception {
		PosFixedLenField field = new PosFixedLenField();
		field.setBuildedLength(2);
		field.setLength(3);
		field.setBuildType(PosField.BUILD_TYPE_BCD);
		field.setIndex(22);
		field.setPadLeft(false);
		field.build("021");
		String msgStr = ISOUtil.hexString(field.getBuildedValue());
		Assert.assertEquals("0210", msgStr);
	}

	@Test
	public void d23_parse() throws Exception {
		String hex = "0123";
		byte[] bcd = ISOUtil.hex2byte(hex);
		PosFixedLenField field = new PosFixedLenField();
		field.setBuildedLength(2);
		field.setLength(3);
		field.setBuildType(PosField.BUILD_TYPE_BCD);
		field.setIndex(23);
		field.setPadLeft(true);
		field.parse(bcd);
		Assert.assertEquals("123", field.getParsedValue());
	}

	@Test
	public void d23_build() throws Exception {
		PosFixedLenField field = new PosFixedLenField();
		field.setBuildedLength(2);
		field.setLength(3);
		field.setBuildType(PosField.BUILD_TYPE_BCD);
		field.setIndex(23);
		field.setPadLeft(true);
		field.build("123");
		String msgStr = ISOUtil.hexString(field.getBuildedValue());
		Assert.assertEquals("0123", msgStr);
	}
	// @Test
	// public void d25_parse()throws Exception {
	// String hex = "00";
	// byte[] bcd = ISOUtil.hex2byte(hex);
	// POSMsgDomain25 o = new POSMsgDomain25();
	// o.setMsgValue(bcd);
	// try {
	// o.parse();
	// Assert.assertEquals("00", o.getValue());
	// }
	// catch (PosException e) {
	// Assert.fail(e.getMessage());
	// }
	// }
	//
	// @Test
	// public void d25_build()throws Exception {
	// POSMsgDomain25 o = new POSMsgDomain25();
	// o.setValue("06");
	// o.build();
	// String msgStr = ISOUtil.hexString(o.getMsgValue());
	// Assert.assertEquals("06", msgStr);
	// }
	//
	// @Test
	// public void d26_parse()throws Exception {
	// String hex = "12";
	// byte[] bcd = ISOUtil.hex2byte(hex);
	// POSMsgDomain26 o = new POSMsgDomain26();
	// o.setMsgValue(bcd);
	// try {
	// o.parse();
	// Assert.assertEquals("12", o.getValue());
	// }
	// catch (PosException e) {
	// Assert.fail(e.getMessage());
	// }
	// }
	//
	// @Test
	// public void d26_build() throws Exception{
	// POSMsgDomain26 o = new POSMsgDomain26();
	// o.setValue("12");
	// o.build();
	// String msgStr = ISOUtil.hexString(o.getMsgValue());
	// Assert.assertEquals("12", msgStr);
	// }
	//
	// @Test
	// public void d42_parse() throws Exception{
	// String hex = "303433202020202020202020202020";
	// byte[] by = ISOUtil.hex2byte(hex);
	// POSMsgDomain42 o = new POSMsgDomain42();
	// o.setMsgValue(by);
	// try {
	// o.parse();
	// Assert.assertEquals("043", o.getValue());
	// }
	// catch (PosException e) {
	// Assert.fail(e.getMessage());
	// }
	// }
	//
	// @Test
	// public void d42_build() throws Exception {
	// POSMsgDomain42 o = new POSMsgDomain42();
	// o.setValue("043");
	// o.build();
	// String msgStr = ISOUtil.hexString(o.getMsgValue());
	// Assert.assertEquals("303433202020202020202020202020", msgStr);
	// }
	//
	// @Test
	// public void d44_parse()throws Exception{
	// String hex = "0761626364656667";
	// byte[] by = ISOUtil.hex2byte(hex);
	// POSMsgDomain44 o = new POSMsgDomain44();
	// o.setMsgValue(by);
	// try {
	// o.parse();
	// Assert.assertEquals("abcdefg", o.getValue());
	// }
	// catch (PosException e) {
	// Assert.fail(e.getMessage());
	// }
	// }
	//
	// @Test
	// public void d44_build() throws Exception{
	// POSMsgDomain44 o = new POSMsgDomain44();
	// o.setValue("abcdefg");
	// o.build();
	// String msgStr = ISOUtil.hexString(o.getMsgValue());
	// Assert.assertEquals("0761626364656667", msgStr);
	// }
	//
	// @Test
	// public void d49_parse() {
	// String hex = ISOUtil.hexString("156".getBytes());
	// byte[] by = ISOUtil.hex2byte(hex);
	// POSMsgDomain49 o = new POSMsgDomain49();
	// o.setMsgValue(by);
	// try {
	// o.parse();
	// Assert.assertEquals("156", o.getValue());
	// }
	// catch (PosException e) {
	// Assert.fail(e.getMessage());
	// }
	// }
	//
	// @Test
	// public void d49_build() throws Exception {
	// POSMsgDomain49 o = new POSMsgDomain49();
	// o.setValue("156");
	// o.build();
	// Assert.assertEquals("156", new String(o.getMsgValue()));
	// }
}