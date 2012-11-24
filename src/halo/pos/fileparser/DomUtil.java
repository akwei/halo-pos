package halo.pos.fileparser;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class DomUtil {

	private static String readXML(String xmlFileName, String charsetName) {
		InputStreamReader reader = null;
		StringBuilder sb = new StringBuilder();
		try {
			// 一次读一个字符
			reader = new InputStreamReader(new FileInputStream(new File(
			        xmlFileName)), charsetName);
			int tempchar;
			while ((tempchar = reader.read()) != -1) {
				sb.append((char) tempchar);
			}
			return sb.toString();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		finally {
			if (reader != null) {
				try {
					reader.close();
				}
				catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}
	}

	public static Element getRootElement(String xmlFileName, String charsetName) {
		String xml = readXML(xmlFileName, charsetName);
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		ByteArrayInputStream bis = null;
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			bis = new ByteArrayInputStream(xml.getBytes(charsetName));
			Document document = builder.parse(bis);
			return document.getDocumentElement();// <resp>
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		finally {
			if (bis != null) {
				try {
					bis.close();
				}
				catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}
	}

	public static String getString(Node node) {
		NodeList list = node.getChildNodes();
		for (int i = 0; i < list.getLength(); i++) {
			Node n = list.item(0);
			if (n.getNodeType() == Node.TEXT_NODE) {
				return n.getNodeValue().trim();
			}
			if (n.getNodeType() == Node.CDATA_SECTION_NODE) {
				CharacterData cd = (CharacterData) n;
				return cd.getData();
			}
		}
		return null;
	}

	public static long getLong(Node node) {
		String v = getString(node);
		if (v == null) {
			return 0;
		}
		return Long.valueOf(v);
	}

	public static int getInt(Node node) {
		String v = getString(node);
		if (v == null) {
			return 0;
		}
		return Integer.valueOf(v);
	}

	public static double getDouble(Node node) {
		String v = getString(node);
		if (v == null) {
			return 0;
		}
		return Double.valueOf(v);
	}
}
