/*
 * Copyright (C) 2015-2022 たんらる
 */

package jp.fourthline.mmlTools.core;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public final class MMLTickTable {

	private static final int COMBN = 3;
	public static final int TPQN = 96;

	/**
	 * For MML text -> tick
	 */
	private final Map<String, Integer> tickTable = new LinkedHashMap<>(1024);

	/**
	 * For tick -> MML text
	 */
	private final IntMap<List<String>> tickInvTable;

	public static MMLTickTable createTickTable() {
		InputStream preTable = null;
		String preLoadFile = System.getProperty("mabiicco.ticktable");
		if (preLoadFile != null) {
			preTable = MMLTickTable.class.getResourceAsStream(preLoadFile);
		}
		return new MMLTickTable(preTable);
	}

	MMLTickTable(InputStream inputStream) {
		NanoTime time = NanoTime.start();
		generateTickTable();
		tickInvTable = (inputStream == null) ? generateInvTable() : readFromInputStreamInvTable(inputStream);
		System.out.println("MMLTickTable " + time.ms() + "ms");
	}

	public IntMap<List<String>> getInvTable() {
		return this.tickInvTable;
	}

	public Map<String, Integer> getTable() {
		return this.tickTable;
	}

	private void add(int l, boolean dot) {
		int tick = TPQN*4 / l;
		if (dot) {
			tick += tick / 2;
		}
		String s = l+(dot?".":"");
		tickTable.put(s, tick);
	}

	private void generateTickTable() {
		for (int i = 1; i <= 64; i++) {
			add(i, true);
			add(i, false);
		}
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

	private IntMap<List<String>> generateInvTable() {
		var table = new HashMap<Integer, List<String>>(1024);
		String[] keys = tickTable.keySet().toArray(new String[0]);
		int mTick = tickTable.get("1") * 2 - 1;
		for (int i = 1; i <= COMBN; i++) {
			List<List<String>> pattern = new Combination<>(keys, i).getArray();
			for (List<String> list : pattern) {
				int tick = list.stream().mapToInt(tickTable::get).sum();
				if (tick <= mTick) {
					List<String> currentList = table.get(tick);
					if ( (currentList == null) || (patternLength(list) <= patternLength(currentList)) ) {
						table.put(tick, list);
					}
				}
			}
		}
		return new IntMap<>(table);
	}

	void writeToOutputStreamInvTable(OutputStream outputStream) {
		PrintStream stream = new PrintStream(outputStream, false, StandardCharsets.UTF_8);
		stream.println("# Generated Text --- ");
		stream.println("# registered key: " + tickInvTable.validCount());

		int max = tickInvTable.max();
		for (int i = 1; i <= max; i++) {
			if (tickInvTable.containsKey(i)) {
				stream.print(i+"=");
				tickInvTable.get(i).forEach( s -> {
					stream.print("[" + s + "]");
				});
				stream.println();
			} else {
				stream.println("# "+i+"=<< not supported >>");
			}
		}
	}

	private IntMap<List<String>> readFromInputStreamInvTable(InputStream inputStream) {
		var table = new HashMap<Integer, List<String>>(1024);
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
				table.put(Integer.parseInt(key), valueList);
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
		writeToOutputStreamInvTable(System.out);
	}

	public static void main(String[] args) {
		MMLTickTable tickTable = new MMLTickTable(null);
		tickTable.printTickList();
	}
}
