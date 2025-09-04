/*
 * Copyright (C) 2025 たんらる
 */

package jp.fourthline.mabiicco.midi;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.DoubleConsumer;
import java.util.stream.Collectors;


public final class DLSLoader implements Comparator<File> {
	private final List<File> fileList;

	private static final List<String> priorityList = List.of("MSXspirit01.dls", "MSXspirit02.dls", "MSXspirit03.dls", "MSXspirit04.dls");
	public static boolean noParallel = false;
	public static final int DLS_LOAD_LIMIT = 64;

	public DLSLoader(List<File> list) {
		var fileList = new ArrayList<File>();
		for (var f : list) {
			var file = fixFile(f);
			if (!fileList.contains(file)) {
				if (file.exists()) {
					if (fileList.size() < DLS_LOAD_LIMIT) {
						fileList.add(file);
					} else {
						break;
					}
				}
			}
		}

		Collections.sort(fileList, this);
		this.fileList = Collections.unmodifiableList(fileList);
	}

	public List<File> getFileList() {
		return fileList;
	}

	@Override
	public int compare(File o1, File o2) {
		String s1 = o1.getName();
		String s2 = o2.getName();
		if (priorityList.contains(s1) && priorityList.contains(s2)) {
			return Integer.compare(priorityList.indexOf(s1), priorityList.indexOf(s2));
		} else if (priorityList.contains(s1)) {
			return -1;
		} else if (priorityList.contains(s2)) {
			return 1;
		} else {
			return s1.compareTo(s2);
		}
	}

	private File fixFile(File file) {
		if (!file.exists()) {
			// 各Rootディレクトリを探索します.
			for (Path path : FileSystems.getDefault().getRootDirectories()) {
				File aFile = new File(path.toString() + file.getPath());
				if (aFile.exists()) {
					file = aFile;
					break;
				}
			}
		}
		return file;
	}

	private List<List<InstClass>> fileLoad(DoubleConsumer progress) {
		List<PInputStream> ccList = new ArrayList<>();
		fileList.forEach(f -> ccList.add(startLoad(f))); // 数は一致させる必要があるので、ccListにはnullが入っている場合もある。
		var cfArray = ccList.stream().filter(t -> t != null).map(t -> t.cf).toArray(CompletableFuture[]::new);
		var allOfFuture = CompletableFuture.allOf( cfArray );
		int total = PInputStream.sumTotal(ccList);

		// 完了待ちと進捗更新
		while (!allOfFuture.isDone()) {
			updateProgress(PInputStream.sumCurrent(ccList), total, progress);
			try {
				TimeUnit.MILLISECONDS.sleep(100);
			} catch (InterruptedException e) {}
		}
		updateProgress(PInputStream.sumCurrent(ccList), total, progress);
		return ccList.stream().filter(t -> t != null).map(t -> t.cf.join()).collect(Collectors.toList());
	}

	private PInputStream startLoad(File f) {
		try {
			var stream = new PInputStream(f);
			if (noParallel) stream.cf.join();
			return stream;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private void updateProgress(int current, int total, DoubleConsumer progress) {
		if (progress != null) {
			if (total > 0) {
				double p = (double)current/total;
				progress.accept(p);
			}
		}
	}

	public List<File> load(DoubleConsumer progress, List<InstClass> insts, Map<File, List<InstClass>> instsMap) throws IOException {
		var ccList = fileLoad(progress);

		// 集計
		insts.clear();
		instsMap.clear();
		int size = fileList.size();
		for (int i = 0; i < size; i++) {
			var loadList = ccList.get(i);
			if (loadList != null) {
				List<InstClass> addList = new ArrayList<>();
				for (InstClass inst : loadList) {
					if (!insts.contains(inst)) {
						insts.add(inst);
						addList.add(inst);
					}
				}
				instsMap.put(fileList.get(i), addList);
			} else {
				throw new IOException("load failed: " + fileList.get(i).getName());
			}
		}

		return fileList;
	}

	private static class PInputStream extends BufferedInputStream {
		private final int total;
		private int current = 0;
		private final File f;
		private CompletableFuture<List<InstClass>> cf;

		private static int sumTotal(List<PInputStream> list) {
			return list.stream().filter(t -> t != null).mapToInt(t -> t.total).sum();
		}
		private static int sumCurrent(List<PInputStream> list) {
			return list.stream().filter(t -> t != null).mapToInt(t -> t.current).sum();
		}

		public PInputStream(File file) throws IOException {
			super(new FileInputStream(file), 65536);
			this.total = (available() >> 10);
			this.f = file;
			this.cf = CompletableFuture.supplyAsync(() -> {
				try {
					var r = InstClass.loadDLS(this, f.getName());
					close();
					current = total;
					return r;
				} catch (IOException e) {
					e.printStackTrace();
				}
				return null;
			});
			System.out.println("[ " + f.getName() + " ]");
		}

		@Override
		public int read() throws IOException {
			int r = super.read();
			int pos = total - (available() >> 10);
			current = Math.max(current, pos);
			return r;
		}
	}
}
