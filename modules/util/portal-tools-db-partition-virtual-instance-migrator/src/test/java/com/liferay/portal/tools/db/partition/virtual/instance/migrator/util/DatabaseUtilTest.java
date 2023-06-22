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

import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.version.Version;
import com.liferay.portal.tools.db.partition.virtual.instance.migrator.Release;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * @author Luis Ortiz
 */
public class DatabaseUtilTest {

	@Before
	public void setUp() {
		System.setOut(new PrintStream(_testOutByteArrayOutputStream));
	}

	@After
	public void tearDown() {
		System.setOut(_originalOut);
	}

	@Test
	public void testCopyTableContent() throws SQLException {
		List<Integer> numberOfColumns = Arrays.asList(4, 2, 10, 5);
		List<Integer> numberOfRows = Arrays.asList(8, 12, 2, 9);
		List<String> tableNames = Arrays.asList(
			"Table1", "Table2", "Company", "Object_x_25000");

		Connection targetConnection = Mockito.mock(Connection.class);

		List<PreparedStatement> targetPreparedStatements = new ArrayList<>();

		for (int count = 0; count < tableNames.size(); count++) {
			int columns = numberOfColumns.get(count);
			int rows = numberOfRows.get(count);
			String tableName = tableNames.get(count);

			_mockBrowseSourceTable(columns, _sourceConnection, rows, tableName);
			targetPreparedStatements.add(
				_mockInsertTargetData(
					columns, targetConnection, rows, tableName));
		}

		DatabaseUtil.copyTablesContent(
			_sourceConnection, tableNames, _TARGET_CATALOG_NAME,
			targetConnection);

		String string = _testOutByteArrayOutputStream.toString();

		for (int count = 0; count < tableNames.size(); count++) {
			int columns = numberOfColumns.get(count);
			int rows = numberOfRows.get(count);
			String tableName = tableNames.get(count);

			PreparedStatement preparedStatement = targetPreparedStatements.get(
				count);

			Assert.assertTrue(
				string.contains(
					StringBundler.concat(
						"Copied ", rows, " rows for table ", tableName)));

			Mockito.verify(
				preparedStatement, Mockito.times(rows)
			).addBatch();

			Mockito.verify(
				preparedStatement, Mockito.times(rows * columns)
			).setObject(
				Mockito.anyInt(), Mockito.any()
			);
		}
	}

	@Test
	public void testCopyTableStructures() throws Exception {
		_testCopyTableStructures(false, Arrays.asList("Table2"), false, false);
		_testCopyTableStructures(false, Arrays.asList("Table2"), false, true);
		_testCopyTableStructures(false, Arrays.asList("Table2"), true, false);
		_testCopyTableStructures(false, Arrays.asList("Table2"), true, true);
		_testCopyTableStructures(true, Collections.emptyList(), false, true);
		_testCopyTableStructures(true, Collections.emptyList(), true, true);
		_testCopyTableStructures(true, Arrays.asList("Table1"), false, false);
		_testCopyTableStructures(true, Arrays.asList("Table1"), true, false);
	}

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
		_testGetPartitionedTableNames(
			true, true,
			tableNames -> {
				Assert.assertTrue(tableNames.size() == 4);

				Assert.assertTrue(tableNames.contains("Company"));
				Assert.assertTrue(tableNames.contains("Object_x_25000"));
				Assert.assertTrue(tableNames.contains("Table1"));
				Assert.assertTrue(tableNames.contains("Table2"));
			});
		_testGetPartitionedTableNames(
			false, false,
			tableNames -> {
				Assert.assertTrue(tableNames.size() == 2);

				Assert.assertFalse(tableNames.contains("Company"));
				Assert.assertFalse(tableNames.contains("Object_x_25000"));
				Assert.assertTrue(tableNames.contains("Table1"));
				Assert.assertTrue(tableNames.contains("Table2"));
			});
		_testGetPartitionedTableNames(
			true, false,
			tableNames -> {
				Assert.assertTrue(tableNames.size() == 3);

				Assert.assertFalse(tableNames.contains("Object_x_25000"));
				Assert.assertTrue(tableNames.contains("Company"));
				Assert.assertTrue(tableNames.contains("Table1"));
				Assert.assertTrue(tableNames.contains("Table2"));
			});
		_testGetPartitionedTableNames(
			false, true,
			tableNames -> {
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

	private void _assertCopiedTable(
		List<String> expectedTables, boolean local, String tableName) {

		String string = _testOutByteArrayOutputStream.toString();

		if (expectedTables.contains(tableName)) {
			if (local) {
				Assert.assertTrue(
					string.contains(
						StringBundler.concat(
							"create table if not exists ", _TARGET_CATALOG_NAME,
							".", tableName, " like ",
							_DEFAULT_SOURCE_CATALOG_NAME, ".", tableName)));
			}
			else {
				Assert.assertTrue(
					string.contains(
						StringBundler.concat(
							"Copied table structure for table ", tableName,
							" from localhost:8000/",
							_DEFAULT_SOURCE_CATALOG_NAME,
							" by using the script \"create table ", tableName,
							"\"")));
			}
		}
		else {
			Assert.assertFalse(string.contains(tableName));
		}
	}

	private void _mockBrowseSourceTable(
			int columns, Connection connection, int rows, String tableName)
		throws SQLException {

		PreparedStatement preparedStatement = Mockito.mock(
			PreparedStatement.class);

		Mockito.when(
			connection.prepareStatement("select * from " + tableName)
		).thenReturn(
			preparedStatement
		);

		ResultSet resultSet = Mockito.mock(ResultSet.class);

		Mockito.when(
			preparedStatement.executeQuery()
		).thenReturn(
			resultSet
		);

		ResultSetMetaData resultSetMetaData = Mockito.mock(
			ResultSetMetaData.class);

		Mockito.when(
			resultSet.getMetaData()
		).thenReturn(
			resultSetMetaData
		);

		Mockito.when(
			resultSet.getObject(Mockito.anyInt())
		).thenReturn(
			new Object()
		);

		Mockito.when(
			resultSet.next()
		).thenAnswer(
			new Answer() {

				public Object answer(InvocationOnMock invocation) {
					if (_count++ < rows) {
						return true;
					}

					return false;
				}

				private int _count;

			}
		);

		Mockito.when(
			resultSetMetaData.getColumnCount()
		).thenReturn(
			columns
		);

		for (int count = 1; count <= columns; count++) {
			Mockito.when(
				resultSetMetaData.getColumnName(count)
			).thenReturn(
				"Column" + count
			);
		}
	}

	private void _mockCatalog(String catalog, Connection connection)
		throws Exception {

		Mockito.when(
			connection.getCatalog()
		).thenReturn(
			catalog
		);
	}

	private void _mockConnectionURL(
			Connection connection, DatabaseMetaData databaseMetaData,
			boolean local)
		throws Exception {

		Mockito.when(
			connection.getMetaData()
		).thenReturn(
			databaseMetaData
		);

		String url =
			"jdbc:mysql://localhost:8000/" + _DEFAULT_TARGET_CATALOG_NAME;

		if (!local) {
			url =
				"jdbc:mysql://remotehost:8000/" + _DEFAULT_SOURCE_CATALOG_NAME;
		}

		Mockito.when(
			databaseMetaData.getURL()
		).thenReturn(
			url
		);
	}

	private void _mockGetPartitionedTableNames() throws Exception {

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
	}

	private PreparedStatement _mockInsertTargetData(
			int columns, Connection connection, int rows, String tableName)
		throws SQLException {

		String query = "insert into " + tableName + " (";

		for (int count = 1; count <= columns; count++) {
			query += "Column" + count;

			if (count < columns) {
				query += ", ";
			}
		}

		query += ") values (";

		for (int count = 1; count <= columns; count++) {
			query += "?";

			if (count < columns) {
				query += ", ";
			}
		}

		query += ")";

		PreparedStatement preparedStatement = Mockito.mock(
			PreparedStatement.class);

		Mockito.when(
			connection.prepareStatement(query)
		).thenReturn(
			preparedStatement
		);

		int[] response = new int[rows];

		for (int count = 1; count <= rows; count++) {
			response[count - 1] = 1;
		}

		Mockito.when(
			preparedStatement.executeBatch()
		).thenReturn(
			response
		);

		return preparedStatement;
	}

	private void _mockRemotePreparedStatement(
			Connection connection, String tableName)
		throws Exception {

		PreparedStatement preparedStatement = Mockito.mock(
			PreparedStatement.class);

		Mockito.when(
			connection.prepareStatement("show create table " + tableName)
		).thenReturn(
			preparedStatement
		);

		ResultSet resultSet = Mockito.mock(ResultSet.class);

		Mockito.when(
			preparedStatement.executeQuery()
		).thenReturn(
			resultSet
		);

		Mockito.when(
			resultSet.getString(2)
		).thenReturn(
			"create table " + tableName
		);

		Mockito.when(
			resultSet.next()
		).thenReturn(
			true
		).thenReturn(
			false
		);
	}

	private void _testCopyTableStructures(
			boolean controlTables, List<String> excludedTableNames,
			boolean local, boolean objectTables)
		throws Exception {

		_testOutByteArrayOutputStream.reset();

		_mockConnectionURL(_sourceConnection, _sourceDatabaseMetaData, true);

		Connection targetConnection = Mockito.mock(Connection.class);
		DatabaseMetaData targetDatabaseMetaData = Mockito.mock(
			DatabaseMetaData.class);

		_mockConnectionURL(targetConnection, targetDatabaseMetaData, local);

		_mockGetPartitionedTableNames();

		_mockCatalog(_DEFAULT_SOURCE_CATALOG_NAME, _sourceConnection);
		_mockCatalog(_DEFAULT_TARGET_CATALOG_NAME, targetConnection);

		_mockRemotePreparedStatement(_sourceConnection, "Company");
		_mockRemotePreparedStatement(_sourceConnection, "Object_x_25000");
		_mockRemotePreparedStatement(_sourceConnection, "Table1");
		_mockRemotePreparedStatement(_sourceConnection, "Table2");

		PreparedStatement preparedStatement = Mockito.mock(
			PreparedStatement.class);

		Mockito.when(
			targetConnection.prepareStatement(Mockito.anyString())
		).thenReturn(
			preparedStatement
		);

		List<String> copiedTables = DatabaseUtil.copyTableStructures(
			controlTables, excludedTableNames, objectTables, _sourceConnection,
			_TARGET_CATALOG_NAME, targetConnection);

		List<String> expectedTables = new ArrayList<>();

		if (!excludedTableNames.contains("Company") && controlTables) {
			expectedTables.add("Company");
		}

		if (!excludedTableNames.contains("Object_x_25000") && objectTables) {
			expectedTables.add("Object_x_25000");
		}

		if (!excludedTableNames.contains("Table1")) {
			expectedTables.add("Table1");
		}

		if (!excludedTableNames.contains("Table2")) {
			expectedTables.add("Table2");
		}

		Assert.assertEquals(
			copiedTables.toString(), expectedTables.size(),
			copiedTables.size());

		_assertCopiedTable(expectedTables, local, "Company");
		_assertCopiedTable(expectedTables, local, "Object_x_25000");
		_assertCopiedTable(expectedTables, local, "Table1");
		_assertCopiedTable(expectedTables, local, "Table2");
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

		consumer.accept(
			DatabaseUtil.getFailedServletContextNames(_sourceConnection));
	}

	private void _testGetPartitionedTableNames(
			boolean controlTables, boolean objectTables,
			Consumer<List<String>> consumer)
		throws Exception {

		_mockGetPartitionedTableNames();

		consumer.accept(
			DatabaseUtil.getPartitionedTableNames(
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

		biConsumer.accept(
			release, DatabaseUtil.getReleasesMap(_sourceConnection));
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
			singleCompanyInfo,
			DatabaseUtil.hasSingleCompanyInfo(_sourceConnection));
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
			defaultPartition,
			DatabaseUtil.isDefaultPartition(_sourceConnection));
	}

	private static final String _DEFAULT_SOURCE_CATALOG_NAME =
		"lpartition_11111";

	private static final String _DEFAULT_TARGET_CATALOG_NAME = "lportal";

	private static final String _TARGET_CATALOG_NAME = "lpartition_123456";

	private final PrintStream _originalOut = System.out;
	private final Connection _sourceConnection = Mockito.mock(Connection.class);
	private final DatabaseMetaData _sourceDatabaseMetaData = Mockito.mock(
		DatabaseMetaData.class);
	private final PreparedStatement _sourcePreparedStatement = Mockito.mock(
		PreparedStatement.class);
	private final ResultSet _sourceResultSet = Mockito.mock(ResultSet.class);
	private final ByteArrayOutputStream _testOutByteArrayOutputStream =
		new ByteArrayOutputStream();

}