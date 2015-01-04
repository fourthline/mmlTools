/*
 * Copyright (C) 2015 たんらる
 */

package fourthline.mabiicco;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.TransferHandler;

public class FileTransferHandler extends TransferHandler {
	private static final long serialVersionUID = 7618330769836269887L;

	private final ActionDispatcher dispacher;

	public FileTransferHandler(ActionDispatcher dispatcher) {
		super();
		this.dispacher = dispatcher;
	}

	@Override
	public boolean canImport(TransferSupport support) {
		return support.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
	}

	@Override
	public boolean importData(TransferSupport support) {
		if (!canImport(support)) {
			return false;
		}

		try {
			for (Object obj: (List<?>) support.getTransferable().getTransferData(DataFlavor.javaFileListFlavor)) {
				if (obj instanceof File) {
					File file = (File) obj;
					if (file.isFile()) {
						dispacher.checkAndOpenMMLFile(file);
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