/*
 * Copyright (C) 2015-2017 たんらる
 */

package fourthline.mmlTools.core;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class MMLTickTable {

	private static final int COMBN = 3;
	public static final int TPQN = 96;

	/**
	 * For MML text -> tick
	 */
	private final LinkedHashMap<String, Integer> tickTable = new LinkedHashMap<>();

	/**
	 * For tick -> MML text
	 */
	private final LinkedHashMap<Integer, List<String>> tickInvTable = new LinkedHashMap<>();

	public static MMLTickTable createTickTable() {
		InputStream preTable = MMLTickTable.class.getResourceAsStream("ticktable.txt");

		MMLTickTable tickTable;
		if (preTable == null) {
			tickTable = new MMLTickTable();
		} else {
			tickTable = new MMLTickTable(preTable);
		}
		return tickTable;
	}

	MMLTickTable() {
		NanoTime time = NanoTime.start();
		generateTickTable();
		generateInvTable();
		System.out.println("MMLTickTable " + time.ms() + "ms");
	}

	MMLTickTable(InputStream inputStream) {
		long startTime = System.currentTimeMillis();
		generateTickTable();
		readFromInputStreamInvTable(inputStream);
		long endTime = System.currentTimeMillis();
		System.out.println("MMLTickTable(load) " + (endTime - startTime) + "ms");
	}

	public Map<Integer, List<String>> getInvTable() {
		return this.tickInvTable;
	}

	public Map<String, Integer> getTable() {
		return this.tickTable;
	}

	private void add(int l, boolean dot) {
		int tick = (int)( TPQN*4 / l ); 
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

	private void generateInvTable() {
		String keys[] = tickTable.keySet().toArray(new String[tickTable.size()]);
		int mTick = tickTable.values().stream().max(Integer::compare).get();
		for (int i = 1; i <= COMBN; i++) {
			List<List<String>> pattern = new Combination<>(keys, i).getArray();
			for (List<String> list : pattern) {
				int tick = list.stream().mapToInt(tickTable::get).sum();
				if (tick > mTick) {
					continue;
				}
				if (!tickInvTable.containsKey(tick)) {
					tickInvTable.put(tick, list);
				} else {
					List<String> currentList = tickInvTable.get(tick);
					if ( (patternLength(list) <= patternLength(currentList)) ) {
						tickInvTable.put(tick, list);
					}
				}
			}
		}
	}

	void writeToOutputStreamInvTable(OutputStream outputStream) {
		try {
			PrintStream stream = new PrintStream(outputStream, false, "UTF-8");
			stream.println("# Generated Text --- ");
			stream.println("# registered key: " + tickInvTable.size());

			int max = tickInvTable.keySet().stream().max(Integer::compare).get();
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
		} catch (UnsupportedEncodingException e) {}
	}

	private void readFromInputStreamInvTable(InputStream inputStream) {
		try {
			InputStreamReader reader = new InputStreamReader(inputStream, "UTF-8");
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
					tickInvTable.put(Integer.parseInt(key), valueList);
				}
			});
		} catch (UnsupportedEncodingException e) {}
	}

	private void printTickList() {
		writeToOutputStreamInvTable(System.out);
	}

	public static void main(String args[]) {
		MMLTickTable tickTable = new MMLTickTable();
		tickTable.printTickList();
	}
}
