/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.portal.tools.db.partition.virtual.instance.migrator.util;

import java.sql.Connection;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

/**
 * @author Luis Ortiz
 */
public class MigratorTest {

	@Before
	public void setUp() {
		_databaseMockedStatic = Mockito.mockStatic(DatabaseUtil.class);
	}

	@After
	public void tearDown() {
		_databaseMockedStatic.close();
	}

	@Test
	public void testMigrateDatabase() throws Exception {
		Connection sourceConnection = Mockito.mock(Connection.class);

		Connection destinationConnection = Mockito.mock(Connection.class);

		_databaseMockedStatic.when(
			() -> DatabaseUtil.copyTableStructures(
				Mockito.eq(destinationConnection),
				Mockito.eq(destinationConnection),
				Mockito.eq(_DESTINATION_CATALOG_NAME),
				Mockito.eq(Collections.emptyList()), Mockito.eq(false),
				Mockito.eq(false))
		).thenReturn(
			new ArrayList<>(Arrays.asList("Table1", "Table2"))
		);

		_databaseMockedStatic.when(
			() -> DatabaseUtil.copyTableStructures(
				Mockito.eq(sourceConnection), Mockito.eq(destinationConnection),
				Mockito.eq(_DESTINATION_CATALOG_NAME),
				Mockito.eq(Arrays.asList("Table1", "Table2")),
				Mockito.eq(false), Mockito.eq(true))
		).thenReturn(
			new ArrayList<>(Arrays.asList("Company", "Object_x_25000"))
		);

		Migrator.migrateDatabases(sourceConnection, destinationConnection);

		ArgumentCaptor<List<String>> valueCapture = ArgumentCaptor.forClass(
			List.class);
		ArgumentCaptor<Connection> srcCaptureCaptor = ArgumentCaptor.forClass(
			Connection.class);
		ArgumentCaptor<Connection> dstCaptureCaptor = ArgumentCaptor.forClass(
			Connection.class);
		ArgumentCaptor<String> catalogCaptor = ArgumentCaptor.forClass(
			String.class);

		_databaseMockedStatic.verify(
			() -> DatabaseUtil.copyTablesContent(
				srcCaptureCaptor.capture(), dstCaptureCaptor.capture(),
				catalogCaptor.capture(), valueCapture.capture()),
			Mockito.times(1));

		Assert.assertEquals(sourceConnection, srcCaptureCaptor.getValue());
		Assert.assertEquals(destinationConnection, dstCaptureCaptor.getValue());
		Assert.assertEquals(
			_DESTINATION_CATALOG_NAME, catalogCaptor.getValue());

		Assert.assertEquals(
			Arrays.asList("Table1", "Table2", "Company", "Object_x_25000"),
			valueCapture.getValue());
	}

	private static final String _DESTINATION_CATALOG_NAME = "lportal_123456";

	private MockedStatic<DatabaseUtil> _databaseMockedStatic;

}