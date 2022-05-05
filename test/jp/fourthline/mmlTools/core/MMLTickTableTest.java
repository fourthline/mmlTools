/*
 * Copyright (C) 2015 たんらる
 */

package jp.fourthline.mmlTools.core;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.Test;

import jp.fourthline.FileSelect;

public final class MMLTickTableTest extends FileSelect {

	@Test
	public void test_writeAndReadInvTable() {
		MMLTickTable tickTable1 = new MMLTickTable();
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		tickTable1.writeToOutputStreamInvTable(outputStream);

		ByteArrayInputStream inputStream = new ByteArrayInputStream( outputStream.toByteArray() );
		MMLTickTable tickTable2 = new MMLTickTable(inputStream);

		ByteArrayOutputStream outputStream2 = new ByteArrayOutputStream();
		tickTable2.writeToOutputStreamInvTable(outputStream2);

		assertEquals(outputStream.toString(), outputStream2.toString());
	}

	@Test
	public void test_invTable() throws IOException {
		MMLTickTable tickTable1 = new MMLTickTable();
		MMLTickTable tickTable2 = new MMLTickTable( fileSelect("tickInvTable.txt") );
		ByteArrayOutputStream outputStream1 = new ByteArrayOutputStream();
		ByteArrayOutputStream outputStream2 = new ByteArrayOutputStream();
		tickTable1.writeToOutputStreamInvTable(outputStream1);
		tickTable2.writeToOutputStreamInvTable(outputStream2);
		assertEquals(outputStream2.toString(), outputStream1.toString());
	}

	@Test
	public void test_create() {
		MMLTickTable tickTable = MMLTickTable.createTickTable();
		assertNotNull(tickTable);
		assertEquals(567, tickTable.getInvTable().size());
	}
}
