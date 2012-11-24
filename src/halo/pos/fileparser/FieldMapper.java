package halo.pos.fileparser;

import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class FieldMapper {

	private Map<Integer, FieldCnf> map;

	private static final Map<String, FieldMapper> fieldMapperMap = new HashMap<String, FieldMapper>();

	/**
	 * pos配置文件的名称，不需要绝对路径。配置文件需要放在classes的目录下
	 * 
	 * @param fileName
	 * @return
	 */
	public static synchronized FieldMapper getFieldMapper(String fileName) {
		FieldMapper fieldMapper = fieldMapperMap.get(fileName);
		if (fieldMapper == null) {
			fieldMapper = FieldMapper.read(fileName);
			fieldMapperMap.put(fileName, fieldMapper);
		}
		return fieldMapper;
	}

	private static FieldMapper read(String xmlFileName) {
		String path = FieldMapper.class.getClassLoader().getResource("")
		        .getPath() + xmlFileName;
		NodeList nodeList = DomUtil.getRootElement(path, "utf-8")
		        .getChildNodes();
		Map<Integer, FieldCnf> map = new HashMap<Integer, FieldCnf>();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node n = nodeList.item(i);
			if (n.getNodeType() == Node.ELEMENT_NODE) {
				FieldCnf cnf = parseFieldCnfListNode(n);
				map.put(cnf.getIndex(), cnf);
			}
		}
		FieldMapper mapper = new FieldMapper();
		mapper.setMap(map);
		return mapper;
	}

	private static FieldCnf parseFieldCnfListNode(Node node) {
		NodeList nodeList = node.getChildNodes();// <field>的各个节点
		FieldCnf cnf = new FieldCnf();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node n = nodeList.item(i);
			if (n.getNodeType() == Node.ELEMENT_NODE) {
				cnf.add(n.getNodeName(), DomUtil.getString(n));
			}
		}
		String idx = cnf.get("index");
		if (idx != null) {
			cnf.setIndex(Integer.parseInt(idx));
		}
		String fixLen = cnf.get("fixLen");
		if (fixLen != null) {
			cnf.setFixLen(Boolean.parseBoolean(fixLen));
		}
		return cnf;
	}

	public void setMap(Map<Integer, FieldCnf> map) {
		this.map = map;
	}

	public FieldCnf getFieldCnf(int index) {
		if (map == null) {
			return null;
		}
		return map.get(index);
	}

	public static void main(String[] args) throws Exception {
		FieldMapper fieldMapper = FieldMapper.getFieldMapper("halo-pos.xml");
		FieldCnf cnf = fieldMapper.getFieldCnf(2);
		System.out.println(cnf.toString());
	}
}