package halo.pos.fileparser;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class FieldCnf {

	private Map<String, String> map = new LinkedHashMap<String, String>();

	private int index;

	private boolean fixLen;

	public void setFixLen(boolean fixLen) {
		this.fixLen = fixLen;
	}

	public boolean isFixLen() {
		return fixLen;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public int getIndex() {
		return index;
	}

	public void add(String name, String value) {
		map.put(name, value);
	}

	public String get(String name) {
		return map.get(name);
	}

	public int getInt(String name) {
		String v = get(name);
		return Integer.parseInt(v);
	}

	public boolean getBoolean(String name) {
		String v = get(name);
		return Boolean.parseBoolean(v);
	}

	@Override
	public String toString() {
		Set<Entry<String, String>> set = map.entrySet();
		StringBuilder sb = new StringBuilder();
		for (Entry<String, String> e : set) {
			sb.append(e.getKey()).append("=").append(e.getValue());
			sb.append("\n");
		}
		return sb.toString();
	}
}
