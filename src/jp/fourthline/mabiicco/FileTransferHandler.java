/*
 * Copyright (C) 2015 たんらる
 */

package jp.fourthline.mabiicco;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.TransferHandler;

/**
 * ファイルドロップ: 開く
 * Ctrl+ファイルドロップ: インポート
 */
public final class FileTransferHandler extends TransferHandler {
	private static final long serialVersionUID = 7618330769836269887L;

	private final ActionDispatcher dispacher;

	public FileTransferHandler(ActionDispatcher dispatcher) {
		super();
		this.dispacher = dispatcher;
	}

	@Override
	public boolean canImport(TransferSupport support) {
		int action = support.getDropAction();
		if ( (action == MOVE) || (action == COPY) ) {
			return support.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
		}
		return false;
	}

	@Override
	public boolean importData(TransferSupport support) {
		if (!canImport(support)) {
			return false;
		}
		int action = support.getDropAction();

		try {
			for (Object obj : (List<?>) support.getTransferable().getTransferData(DataFlavor.javaFileListFlavor)) {
				if (obj instanceof File) {
					File file = (File) obj;
					if (file.isFile()) {
						if (action == MOVE) {
							dispacher.checkAndOpenMMLFile(file);
						} else {
							dispacher.fileImport(file);
						}
						return true;
					}
				}
			}
		} catch (UnsupportedFlavorException | IOException e) {
			e.printStackTrace();
		}
		return false;
	}
}
