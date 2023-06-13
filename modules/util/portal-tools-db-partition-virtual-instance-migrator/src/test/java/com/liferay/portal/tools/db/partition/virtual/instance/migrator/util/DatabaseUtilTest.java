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

import com.liferay.portal.kernel.version.Version;
import com.liferay.portal.tools.db.partition.virtual.instance.migrator.Release;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.junit.Assert;
import org.junit.Test;

import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

/**
 * @author Luis Ortiz
 */
public class DatabaseUtilTest {

	@Test
	public void testGetFailedServletContextNames() throws SQLException {
		_testGetFailedServletContextNames(
			failedServletContextNames -> {
				Assert.assertEquals(
					failedServletContextNames.toString(), 2,
					failedServletContextNames.size());

				Assert.assertTrue(
					failedServletContextNames.contains("module1"));
				Assert.assertTrue(
					failedServletContextNames.contains("module2"));
			},
			false);
		_testGetFailedServletContextNames(
			failedServletContextNames -> Assert.assertTrue(
				failedServletContextNames.isEmpty()),
			true);
	}

	@Test
	public void testGetPartitionedTableNames() throws Exception {
		_testGetPartitionedTableNames(true, true,
			(tableNames) -> {
				Assert.assertTrue(tableNames.size() == 4);

				Assert.assertTrue(tableNames.contains("Company"));
				Assert.assertTrue(tableNames.contains("Object_x_25000"));
				Assert.assertTrue(tableNames.contains("Table1"));
				Assert.assertTrue(tableNames.contains("Table2"));
			});
		_testGetPartitionedTableNames(false, false,
			(tableNames) -> {
				Assert.assertTrue(tableNames.size() == 2);

				Assert.assertFalse(tableNames.contains("Company"));
				Assert.assertFalse(tableNames.contains("Object_x_25000"));
				Assert.assertTrue(tableNames.contains("Table1"));
				Assert.assertTrue(tableNames.contains("Table2"));
			});
		_testGetPartitionedTableNames(true, false,
			(tableNames) -> {
				Assert.assertTrue(tableNames.size() == 3);

				Assert.assertFalse(tableNames.contains("Object_x_25000"));
				Assert.assertTrue(tableNames.contains("Company"));
				Assert.assertTrue(tableNames.contains("Table1"));
				Assert.assertTrue(tableNames.contains("Table2"));
			});
		_testGetPartitionedTableNames(false, true,
			(tableNames) -> {
				Assert.assertTrue(tableNames.size() == 3);

				Assert.assertFalse(tableNames.contains("Company"));
				Assert.assertTrue(tableNames.contains("Object_x_25000"));
				Assert.assertTrue(tableNames.contains("Table1"));
				Assert.assertTrue(tableNames.contains("Table2"));
			});
	}

	@Test
	public void testGetReleases() throws SQLException {
		Release module1Release = new Release(
			Version.parseVersion("14.2.4"), "module1", true);
		Release module2Release = new Release(
			Version.parseVersion("2.0.1"), "module2", false);

		Mockito.when(
			_sourceConnection.prepareStatement(
				"select servletContextName, schemaVersion, verified from " +
				"Release_")
		).thenReturn(
			_sourcePreparedStatement
		);

		Mockito.when(
			_sourcePreparedStatement.executeQuery()
		).thenReturn(
			_sourceResultSet
		);

		Mockito.when(
			_sourceResultSet.next()
		).thenReturn(
			true
		).thenReturn(
			true
		).thenReturn(
			false
		);

		Mockito.when(
			_sourceResultSet.getBoolean(3)
		).thenReturn(
			module1Release.getVerified()
		).thenReturn(
			module2Release.getVerified()
		);

		Mockito.when(
			_sourceResultSet.getString(1)
		).thenReturn(
			module1Release.getServletContextName()
		).thenReturn(
			module2Release.getServletContextName()
		);

		Version module1SchemaVersion = module1Release.getSchemaVersion();
		Version module2SchemaVersion = module2Release.getSchemaVersion();

		Mockito.when(
			_sourceResultSet.getString(2)
		).thenReturn(
			module1SchemaVersion.toString()
		).thenReturn(
			module2SchemaVersion.toString()
		);

		List<Release> releases = DatabaseUtil.getReleases(_sourceConnection);

		Assert.assertEquals(releases.toString(), 2, releases.size());
		Assert.assertTrue(module1Release.equals(releases.get(0)));
		Assert.assertTrue(module2Release.equals(releases.get(1)));
	}

	@Test
	public void testGetReleasesMap() throws SQLException {
		_testGetReleasesMap(
			(release, releasesMap) -> {
				Assert.assertNotNull(releasesMap.get("module"));

				Assert.assertTrue(release.equals(releasesMap.get("module")));
			},
			new Release(Version.parseVersion("14.2.4"), "module", true));
		_testGetReleasesMap(
			(release, releasesMap) -> Assert.assertNull(
				releasesMap.get("module")),
			null);
	}

	@Test
	public void testHasSingleCompanyInfo() throws SQLException {
		_testHasSingleCompanyInfo(false);
		_testHasSingleCompanyInfo(true);
	}

	@Test
	public void testHasWebId() throws SQLException {
		_testHasWebId(false);
		_testHasWebId(true);
	}

	@Test
	public void testIsDefaultPartition() throws Exception {
		_testIsDefaultPartition(false);
		_testIsDefaultPartition(true);
	}

	private void _testGetFailedServletContextNames(
		Consumer<List<String>> consumer, boolean state)
		throws SQLException {

		Mockito.when(
			_sourceConnection.prepareStatement(
				"select servletContextName from Release_ where state_ != 0;")
		).thenReturn(
			_sourcePreparedStatement
		);

		Mockito.when(
			_sourcePreparedStatement.executeQuery()
		).thenReturn(
			_sourceResultSet
		);

		if (state) {
			Mockito.when(
				_sourceResultSet.next()
			).thenReturn(
				false
			);
		}
		else {
			Mockito.when(
				_sourceResultSet.getString(1)
			).thenReturn(
				"module1"
			).thenReturn(
				"module2"
			);

			Mockito.when(
				_sourceResultSet.next()
			).thenReturn(
				true
			).thenReturn(
				true
			).thenReturn(
				false
			);
		}

		consumer.accept(DatabaseUtil.getFailedServletContextNames(_sourceConnection));
	}
	
	private void _testGetPartitionedTableNames(boolean controlTables, boolean objectTables, Consumer<List<String>> consumer) throws Exception {

		// Mock _sourceConnection

		Mockito.when(
			_sourceConnection.getMetaData()
		).thenReturn(
			_sourceDatabaseMetaData
		);

		PreparedStatement preparedStatement = Mockito.mock(
			PreparedStatement.class);

		Mockito.when(
			_sourceConnection.prepareStatement("select companyId from Company")
		).thenReturn(
			preparedStatement
		);

		ResultSet resultSet1 = Mockito.mock(ResultSet.class);

		Mockito.when(
			preparedStatement.executeQuery()
		).thenReturn(
			resultSet1
		);

		Mockito.when(
			resultSet1.getLong("companyId")
		).thenReturn(
			25000L
		);

		Mockito.when(
			resultSet1.next()
		).thenReturn(
			true
		).thenReturn(
			false
		);

		// Mock _sourceDatabaseMetaData

		ResultSet resultSet2 = Mockito.mock(ResultSet.class);

		Mockito.when(
			_sourceDatabaseMetaData.getColumns(
				Mockito.nullable(String.class), Mockito.nullable(String.class),
				Mockito.any(), Mockito.nullable(String.class))
		).thenReturn(
			resultSet2
		);

		Mockito.when(
			resultSet2.next()
		).thenReturn(
			false
		);

		ResultSet resultSet3 = Mockito.mock(ResultSet.class);

		Mockito.when(
			_sourceDatabaseMetaData.getColumns(
				Mockito.nullable(String.class), Mockito.nullable(String.class),
				Mockito.eq("company"), Mockito.nullable(String.class))
		).thenReturn(
			resultSet3
		);

		Mockito.when(
			resultSet2.next()
		).thenReturn(
			true
		);

		Mockito.when(
			_sourceDatabaseMetaData.getTables(
				Mockito.nullable(String.class), Mockito.nullable(String.class),
				Mockito.nullable(String.class), Mockito.any(String[].class))
		).thenReturn(
			_sourceResultSet
		);

		// Mock _sourceResultSet

		Mockito.when(
			_sourceResultSet.getString("TABLE_NAME")
		).thenReturn(
			"Table1"
		).thenReturn(
			"Company"
		).thenReturn(
			"Table2"
		).thenReturn(
			"Object_x_25000"
		);

		Mockito.when(
			_sourceResultSet.next()
		).thenReturn(
			true
		).thenReturn(
			true
		).thenReturn(
			true
		).thenReturn(
			true
		).thenReturn(
			false
		);

		consumer.accept(DatabaseUtil.getPartitionedTableNames(
			_sourceConnection, controlTables, objectTables));
	}

	private void _testGetReleasesMap(
		BiConsumer<Release, Map<String, Release>> biConsumer,
		Release release)
		throws SQLException {

		Mockito.when(
			_sourceConnection.prepareStatement(
				"select servletContextName, schemaVersion, verified from " +
				"Release_")
		).thenReturn(
			_sourcePreparedStatement
		);

		Mockito.when(
			_sourcePreparedStatement.executeQuery()
		).thenReturn(
			_sourceResultSet
		);

		if (release != null) {
			Mockito.when(
				_sourceResultSet.getBoolean(3)
			).thenReturn(
				release.getVerified()
			);

			Mockito.when(
				_sourceResultSet.getString(1)
			).thenReturn(
				release.getServletContextName()
			);

			Version releaseVersion = release.getSchemaVersion();

			Mockito.when(
				_sourceResultSet.getString(2)
			).thenReturn(
				releaseVersion.toString()
			);

			Mockito.when(
				_sourceResultSet.next()
			).thenReturn(
				true
			).thenReturn(
				false
			);
		}
		else {
			Mockito.when(
				_sourceResultSet.next()
			).thenReturn(
				false
			);
		}

		biConsumer.accept(release, DatabaseUtil.getReleasesMap(_sourceConnection));
	}

	private void _testHasSingleCompanyInfo(boolean singleCompanyInfo)
		throws SQLException {

		Mockito.when(
			_sourceConnection.prepareStatement(
				"select count(1) from CompanyInfo")
		).thenReturn(
			_sourcePreparedStatement
		);

		Mockito.when(
			_sourcePreparedStatement.executeQuery()
		).thenReturn(
			_sourceResultSet
		);

		Mockito.when(
			_sourceResultSet.getInt(1)
		).thenReturn(
			singleCompanyInfo ? 1 : 4
		);

		Mockito.when(
			_sourceResultSet.next()
		).thenReturn(
			true
		);

		Assert.assertEquals(
			singleCompanyInfo, DatabaseUtil.hasSingleCompanyInfo(_sourceConnection));
	}

	private void _testHasWebId(boolean hasWebId) throws SQLException {
		Mockito.reset(_sourcePreparedStatement);

		Mockito.when(
			_sourceConnection.prepareStatement(
				"select companyId from Company where webId = ?")
		).thenReturn(
			_sourcePreparedStatement
		);

		Mockito.when(
			_sourcePreparedStatement.executeQuery()
		).thenReturn(
			_sourceResultSet
		);

		Mockito.when(
			_sourceResultSet.next()
		).thenReturn(
			hasWebId
		);

		Assert.assertEquals(
			hasWebId, DatabaseUtil.hasWebId(_sourceConnection, "webId"));

		ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(
			String.class);

		Mockito.verify(
			_sourcePreparedStatement
		).setString(
			Mockito.eq(1), argumentCaptor.capture()
		);

		Assert.assertEquals("webId", argumentCaptor.getValue());
	}

	private void _testIsDefaultPartition(boolean defaultPartition)
		throws Exception {

		Mockito.when(
			_sourceConnection.getMetaData()
		).thenReturn(
			_sourceDatabaseMetaData
		);

		Mockito.when(
			_sourceDatabaseMetaData.getTables(
				Mockito.nullable(String.class), Mockito.nullable(String.class),
				Mockito.eq("company"), Mockito.nullable(String[].class))
		).thenReturn(
			_sourceResultSet
		);

		Mockito.when(
			_sourceDatabaseMetaData.storesLowerCaseIdentifiers()
		).thenReturn(
			true
		);

		Mockito.when(
			_sourceResultSet.next()
		).thenReturn(
			defaultPartition
		);

		Assert.assertEquals(
			defaultPartition, DatabaseUtil.isDefaultPartition(_sourceConnection));
	}

	private final Connection _destinationConnection = Mockito.mock(
		Connection.class);
	private final DatabaseMetaData _destinationDatabaseMetaData = Mockito.mock(
		DatabaseMetaData.class);
	private final PreparedStatement _destinationPreparedStatement =
		Mockito.mock(PreparedStatement.class);
	private final ResultSet _destinationResultSet = Mockito.mock(
		ResultSet.class);
	private final Connection _sourceConnection = Mockito.mock(Connection.class);
	private final DatabaseMetaData _sourceDatabaseMetaData = Mockito.mock(
		DatabaseMetaData.class);
	private final PreparedStatement _sourcePreparedStatement = Mockito.mock(
		PreparedStatement.class);
	private final ResultSet _sourceResultSet = Mockito.mock(ResultSet.class);

}