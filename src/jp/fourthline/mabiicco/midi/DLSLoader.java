/*
 * Copyright (C) 2025 たんらる
 */

package jp.fourthline.mabiicco.midi;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import javax.sound.midi.InvalidMidiDataException;

public final class DLSLoader implements Comparator<File> {
	private final List<File> fileList;
	private final List<CompletableFuture<List<InstClass>>> ccList = new ArrayList<>();

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

	private void fileLoad(Runnable progress) {
		for (var f : fileList) {
			System.out.println("[ " + f.getName() + " ]");
			var cc = CompletableFuture.supplyAsync(() -> {
				try {
					var r = InstClass.loadDLS(f);
					if (progress != null) progress.run();
					return r;
				} catch (InvalidMidiDataException | IOException e) {
					e.printStackTrace();
				}
				return null;
			});
			ccList.add(cc);
			if (noParallel) {
				cc.join();
			}
		}
	}

	public List<File> load(Runnable progress, List<InstClass> insts, Map<File, List<InstClass>> instsMap) {
		fileLoad(progress);

		// 集計
		insts.clear();
		instsMap.clear();
		int size = fileList.size();
		for (int i = 0; i < size; i++) {
			var loadList = ccList.get(i).join();
			if (loadList != null) {
				List<InstClass> addList = new ArrayList<>();
				for (InstClass inst : loadList) {
					if (!insts.contains(inst)) {
						insts.add(inst);
						addList.add(inst);
					}
				}
				instsMap.put(fileList.get(i), addList);
			}
		}

		return fileList;
	}
}
