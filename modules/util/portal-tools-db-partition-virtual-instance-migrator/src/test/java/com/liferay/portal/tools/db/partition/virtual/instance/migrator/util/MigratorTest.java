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
		Connection targetConnection = Mockito.mock(Connection.class);

		_databaseMockedStatic.when(
			() -> DatabaseUtil.checkCompanyIdEligible(
				_SOURCE_DATABASE_COMPANY_ID, targetConnection)
		).thenReturn(
			true
		);

		Connection sourceConnection = Mockito.mock(Connection.class);

		_databaseMockedStatic.when(
			() -> DatabaseUtil.copyTableStructures(
				Mockito.eq(false),
				Mockito.eq(Arrays.asList("Table1", "Table2")), Mockito.eq(true),
				Mockito.eq(false), Mockito.eq(sourceConnection),
				Mockito.eq(
					_TARGET_CATALOG_PREFIX + _SOURCE_DATABASE_COMPANY_ID),
				Mockito.eq(targetConnection))
		).thenReturn(
			new ArrayList<>(Arrays.asList("Company", "Object_x_25000"))
		);

		_databaseMockedStatic.when(
			() -> DatabaseUtil.copyTableStructures(
				Mockito.eq(false), Mockito.eq(Collections.emptyList()),
				Mockito.eq(false), Mockito.eq(true),
				Mockito.eq(targetConnection),
				Mockito.eq(
					_TARGET_CATALOG_PREFIX + _SOURCE_DATABASE_COMPANY_ID),
				Mockito.eq(targetConnection))
		).thenReturn(
			new ArrayList<>(Arrays.asList("Table1", "Table2"))
		);

		_databaseMockedStatic.when(
			() -> DatabaseUtil.createCatalog(
				_SOURCE_DATABASE_COMPANY_ID, targetConnection)
		).thenReturn(
			_TARGET_CATALOG_PREFIX + _SOURCE_DATABASE_COMPANY_ID
		);

		_databaseMockedStatic.when(
			() -> DatabaseUtil.getCompanyId(sourceConnection)
		).thenReturn(
			_SOURCE_DATABASE_COMPANY_ID
		);

		Migrator.migrateDatabases(sourceConnection, targetConnection);

		ArgumentCaptor<String> catalogCaptor = ArgumentCaptor.forClass(
			String.class);
		ArgumentCaptor<Connection> sourceConnectionCaptor =
			ArgumentCaptor.forClass(Connection.class);
		ArgumentCaptor<List<String>> tableNamesCaptor = ArgumentCaptor.forClass(
			List.class);
		ArgumentCaptor<Connection> targetConnectionCaptor =
			ArgumentCaptor.forClass(Connection.class);

		_databaseMockedStatic.verify(
			() -> DatabaseUtil.copyTablesContent(
				sourceConnectionCaptor.capture(), tableNamesCaptor.capture(),
				catalogCaptor.capture(), targetConnectionCaptor.capture()),
			Mockito.times(1));

		Assert.assertEquals(
			_TARGET_CATALOG_PREFIX + _SOURCE_DATABASE_COMPANY_ID,
			catalogCaptor.getValue());
		Assert.assertEquals(
			sourceConnection, sourceConnectionCaptor.getValue());
		Assert.assertEquals(
			Arrays.asList("Table1", "Table2", "Company", "Object_x_25000"),
			tableNamesCaptor.getValue());
		Assert.assertEquals(
			targetConnection, targetConnectionCaptor.getValue());
	}

	private static final long _SOURCE_DATABASE_COMPANY_ID = 123456;

	private static final String _TARGET_CATALOG_PREFIX = "lportal_";

	private MockedStatic<DatabaseUtil> _databaseMockedStatic;

}