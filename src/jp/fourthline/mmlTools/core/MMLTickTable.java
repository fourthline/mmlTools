/*
 * Copyright (C) 2015-2025 たんらる
 */

package jp.fourthline.mmlTools.core;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.IntFunction;

public final class MMLTickTable {

	private static final int COMBN = 3;
	public static final int TPQN = 96;

	/**
	 * For MML text -> tick
	 */
	private final Map<String, Integer> tickTable;

	/**
	 * For tick -> MML text
	 */
	private final IntMap<MMLPatern> tickInvTable;

	private IntMap<MMLPatern> tickInvTableForMb;

	private static MMLTickTable obj = null;

	public enum Switch {
		FULL, MB;
	}

	private Switch tableInvSwitch = null;
	public void tableInvSwitch(Switch sw) {
		tableInvSwitch = sw;
	}

	public static synchronized MMLTickTable getInstance() {
		if (obj == null) {
			InputStream preTable = null;
			String preLoadFile = System.getProperty("mabiicco.ticktable");
			if (preLoadFile != null) {
				preTable = MMLTickTable.class.getResourceAsStream(preLoadFile);
			}
			obj = new MMLTickTable(preTable);
		}
		return obj;
	}

	MMLTickTable(InputStream inputStream) {
		tickTable = generateTickTable(TPQN, true);
		tickInvTable = (inputStream == null) ? generateInvTable() : readFromInputStreamInvTable(inputStream);
		tickInvTableForMb = generateInvTable(new String[] { "1", "1.", "2", "2.", "4", "4.", "8", "8.", "16", "16.", "32", "32.", "64", "64.", "6", "12", "24", "48" });
	}

	IntMap<MMLPatern> getInvTable() {
		if (tableInvSwitch != null) {
			switch (tableInvSwitch) {
			case FULL: return tickInvTable;
			case MB:   return tickInvTableForMb;
			}
		}
		return tickInvTable;
	}

	public Map<String, Integer> getTable() {
		return tickTable;
	}

	private static void add(Map<String, Integer> tickTable, int tpqn, int l, boolean dot) {
		int tick = tpqn * 4 / l;
		if (dot) {
			tick += tick / 2;
		}
		String s = l+(dot?".":"");
		tickTable.put(s, tick);
	}

	private static void add2(Map<String, Integer> tickTable, int tpqn, int l, boolean dot) {
		double tick = tpqn * 4.0 / l;
		if (dot) {
			tick += tick / 2;
		}
		String s = l+(dot?".":"");
		tickTable.put(s, (int)tick);
	}

	private static Map<String, Integer> generateTickTable(int tpqn, boolean mabi) {
		var table = new LinkedHashMap<String, Integer>();
		for (int i = 1; i <= 64; i++) {
			if (mabi) {
				add(table, tpqn, i, true);
				add(table, tpqn, i, false);
			} else {
				add2(table, tpqn, i, true);
				add2(table, tpqn, i, false);
			}
		}
		return table;
	}

	static class MMLPatern {
		List<String> primary = null;
		List<List<String>> alt = new ArrayList<>();

		private MMLPatern(List<String> list) {
			this.primary = list;
		}

		private int patternLength(List<String> pattern) {
			int len = 0;
			List<String> spll = Arrays.asList("1", "2", "4", "8", "16");
			for (String s : pattern) {
				if (spll.stream().anyMatch(s::equals)) {
					len += s.length();
				} else if (spll.stream().map(t -> t+".").anyMatch(s::equals)) {
					len += s.length()*2;
				} else {
					len += s.length()*3;
				}
			}
			return len + pattern.size()*10;
		}

		private void put(List<String> list) {
			if (patternLength(list) <= patternLength(primary)) {
				if (list.size() == 2) alt.add(primary);
				primary = list;
			} else {
				if (list.size() == 2) alt.add(list);
			}
		}
	}

	private IntMap<MMLPatern> generateInvTable() {
		var keys = tickTable.keySet().toArray(new String[0]);
		return generateInvTable(keys);
	}

	private IntMap<MMLPatern> generateInvTable(String keys[]) {
		return generateInvTable(keys, tickTable, t->t);
	}

	private static IntMap<MMLPatern> generateInvTable(String keys[], Map<String, Integer> tickTable, IntFunction<Integer> func) {
		var time = NanoTime.start();
		var table = new HashMap<Integer, MMLPatern>(1024);
		int mTick = tickTable.get("1") * 2 - 1;
		for (int i = 1; i <= COMBN; i++) {
			List<List<String>> pattern = new Combination<>(keys, i).getArray();
			for (List<String> list : pattern) {
				int tick = list.stream().mapToInt(tickTable::get).sum();
				if (tick <= mTick) {
					int key = func.apply(tick);
					if (key > 0) {
						var currentList = table.get(key);
						if (currentList == null) {
							table.put(key, new MMLPatern(list));
						} else {
							currentList.put(list);
						}
					}
				}
			}
		}
		System.out.println("MMLTickTable " + time.ms() + "ms");
		return new IntMap<>(table);
	}

	void writeToOutputStreamInvTable(OutputStream outputStream) {
		writeToOutputStreamInvTable(outputStream, false);
	}

	void writeToOutputStreamInvTable(OutputStream outputStream, boolean alt) {
		PrintStream stream = new PrintStream(outputStream, false, StandardCharsets.UTF_8);
		var currentTable = getInvTable();
		stream.println("# Generated Text --- ");
		stream.println("# registered key: " + currentTable.validCount());

		int max = currentTable.max();
		for (int i = 1; i <= max; i++) {
			if (currentTable.containsKey(i)) {
				stream.print(i+"=");
				currentTable.get(i).primary.forEach( s -> stream.print("[" + s + "]"));
				if (alt) {
					stream.print("\t");
					currentTable.get(i).alt.forEach( s -> stream.print("[" + s + "]"));
				}
				stream.println();
			} else {
				stream.println("# "+i+"=<< not supported >>");
			}
		}
	}

	private IntMap<MMLPatern> readFromInputStreamInvTable(InputStream inputStream) {
		var table = new HashMap<Integer, MMLPatern>(1024);
		InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
		new BufferedReader(reader).lines().forEach(s -> {
			if (!s.startsWith("#")) {
				int keySep = s.indexOf('=');
				String key = s.substring(0, keySep);
				String itemList = s.substring(keySep+1);
				ArrayList<String> valueList = new ArrayList<>();
				while (itemList.length() > 0) {
					int itemIndex = itemList.indexOf(']');
					String item = itemList.substring(1, itemIndex);
					valueList.add(item);
					itemList = itemList.substring(itemIndex+1);
				}
				table.put(Integer.parseInt(key), new MMLPatern(valueList));
			}
		});
		return new IntMap<>(table);
	}

	public final static class IntMap<T> {
		private final T[] array;

		@SuppressWarnings("unchecked")
		public IntMap(Map<Integer, T> map) {
			var list = new ArrayList<T>();
			int max = map.keySet().stream().max(Integer::compare).get();
			for (int i = 0; i <= max; i++) {
				list.add(map.get(i));
			}
			array = (T[]) list.toArray();
		}

		public int max() {
			return array.length;
		}

		public int validCount() {
			int count = 0;
			for (var a : array) {
				if (a != null) {
					count++;
				}
			}
			return count;
		}

		public boolean containsKey(int key) {
			return (key >= 0) && (key < array.length) ? array[key] != null : false;
		}

		public T get(int key) {
			return (key >= 0) && (key < array.length) ? array[key] : null;
		}
	}

	private void printTickList() {
		writeToOutputStreamInvTable(System.out, true);
	}

	public static void main(String[] args) {
		MMLTickTable tickTable = new MMLTickTable(null);
		tickTable.printTickList();
	}
}
