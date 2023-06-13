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

package com.liferay.portal.tools.db.partition.virtual.instance.migrator.internal.util;

import com.liferay.portal.kernel.version.Version;
import com.liferay.portal.tools.db.partition.virtual.instance.migrator.internal.release.Release;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

/**
 * @author Luis Ortiz
 */
public class DatabaseUtilTest {

	@Test
	public void testGetAllTableNames() throws SQLException {
		_mockGetTables();

		List<String> tableNames = DatabaseUtil.getTableNames(
			_sourceConnection, true, true);

		Assert.assertTrue(tableNames.size() == 4);

		Assert.assertTrue(tableNames.contains("Table1"));
		Assert.assertTrue(tableNames.contains("Table2"));
		Assert.assertTrue(tableNames.contains("Company"));
		Assert.assertTrue(tableNames.contains("Object_x_25000"));
	}

	@Test
	public void testGetOnlyRegularTableNames() throws SQLException {
		_mockGetTables();

		List<String> tableNames = DatabaseUtil.getTableNames(
			_sourceConnection, false, false);

		Assert.assertTrue(tableNames.size() == 2);

		Assert.assertTrue(tableNames.contains("Table1"));
		Assert.assertTrue(tableNames.contains("Table2"));
		Assert.assertFalse(tableNames.contains("Company"));
		Assert.assertFalse(tableNames.contains("Object_x_25000"));
	}

	@Test
	public void testGetRegularAndControlTableNames() throws SQLException {
		_mockGetTables();

		List<String> tableNames = DatabaseUtil.getTableNames(
			_sourceConnection, true, false);

		Assert.assertTrue(tableNames.size() == 3);

		Assert.assertTrue(tableNames.contains("Table1"));
		Assert.assertTrue(tableNames.contains("Table2"));
		Assert.assertTrue(tableNames.contains("Company"));
		Assert.assertFalse(tableNames.contains("Object_x_25000"));
	}

	@Test
	public void testGetRegularAndObjectTableNames() throws SQLException {
		_mockGetTables();

		List<String> tableNames = DatabaseUtil.getTableNames(
			_sourceConnection, false, true);

		Assert.assertTrue(tableNames.size() == 3);

		Assert.assertTrue(tableNames.contains("Table1"));
		Assert.assertTrue(tableNames.contains("Table2"));
		Assert.assertFalse(tableNames.contains("Company"));
		Assert.assertTrue(tableNames.contains("Object_x_25000"));
	}

	@Test
	public void testGetReleaseMapEntry() throws SQLException {
		Release release = new Release(
			"module", Version.parseVersion("14.2.4"), true);

		_mockGetReleaseMap(release, true);

		Map<String, Release> releaseMap = DatabaseUtil.getReleaseMap(
			_sourceConnection);

		Assert.assertNotNull(releaseMap.get("module"));

		Assert.assertTrue(release.equals(releaseMap.get("module")));
	}

	@Test
	public void testGetReleaseMapNotFoundEntry() throws SQLException {
		Release release = new Release(
			"module", Version.parseVersion("14.2.4"), true);

		_mockGetReleaseMap(release, false);

		Map<String, Release> releaseMap = DatabaseUtil.getReleaseMap(
			_sourceConnection);

		Assert.assertNull(releaseMap.get("module"));
	}

	@Test
	public void testGetReleases() throws SQLException {
		Release module1Release = new Release(
			"module1", Version.parseVersion("14.2.4"), true);
		Release module2Release = new Release(
			"module2", Version.parseVersion("2.0.1"), false);

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

		Mockito.when(
			_sourceResultSet.getBoolean(3)
		).thenReturn(
			module1Release.getVerified()
		).thenReturn(
			module2Release.getVerified()
		);

		List<Release> releases = DatabaseUtil.getReleases(_sourceConnection);

		Assert.assertTrue(releases.size() == 2);

		Release module1Entry = releases.get(0);

		Assert.assertTrue(module1Entry.equals(module1Release));

		Release module2Entry = releases.get(1);

		Assert.assertTrue(module2Entry.equals(module2Release));
	}

	@Test
	public void testHasNotWebId() throws SQLException {
		_mockWebId(false);

		Assert.assertFalse(DatabaseUtil.hasWebId(_sourceConnection, "webId"));

		ArgumentCaptor<String> valueCapture = ArgumentCaptor.forClass(
			String.class);

		Mockito.verify(
			_sourcePreparedStatement
		).setString(
			Mockito.eq(1), valueCapture.capture()
		);
		Assert.assertEquals("webId", valueCapture.getValue());
	}

	@Test
	public void testHasWebId() throws SQLException {
		_mockWebId(true);

		Assert.assertTrue(DatabaseUtil.hasWebId(_sourceConnection, "webId"));

		ArgumentCaptor<String> valueCapture = ArgumentCaptor.forClass(
			String.class);

		Mockito.verify(
			_sourcePreparedStatement
		).setString(
			Mockito.eq(1), valueCapture.capture()
		);
		Assert.assertEquals("webId", valueCapture.getValue());
	}

	@Test
	public void testInvalidReleaseState() throws SQLException {
		_mockReleaseState(false);

		List<String> failedServletContextNames =
			DatabaseUtil.getFailedServletContextNames(_sourceConnection);

		Assert.assertTrue(failedServletContextNames.size() == 2);

		Assert.assertTrue(failedServletContextNames.contains("module1"));
		Assert.assertTrue(failedServletContextNames.contains("module2"));
	}

	@Test
	public void testIsDefaultPartition() throws SQLException {
		_mockDefaultPartition(true);

		Assert.assertTrue(DatabaseUtil.isDefaultPartition(_sourceConnection));

		ArgumentCaptor<String> valueCapture = ArgumentCaptor.forClass(
			String.class);

		Mockito.verify(
			_sourceDatabaseMetaData, Mockito.times(2)
		).getTables(
			Mockito.nullable(String.class), Mockito.nullable(String.class),
			valueCapture.capture(), Mockito.any(String[].class)
		);

		List<String> capturedValues = valueCapture.getAllValues();

		Assert.assertTrue(capturedValues.size() == 2);
		Assert.assertTrue(capturedValues.contains("company"));
		Assert.assertTrue(capturedValues.contains("virtualhost"));
	}

	@Test
	public void testIsNotDefaultPartition() throws SQLException {
		_mockDefaultPartition(false);

		Assert.assertFalse(DatabaseUtil.isDefaultPartition(_sourceConnection));

		ArgumentCaptor<String> valueCapture = ArgumentCaptor.forClass(
			String.class);

		Mockito.verify(
			_sourceDatabaseMetaData, Mockito.times(1)
		).getTables(
			Mockito.nullable(String.class), Mockito.nullable(String.class),
			valueCapture.capture(), Mockito.any(String[].class)
		);

		List<String> capturedValues = valueCapture.getAllValues();

		Assert.assertTrue(capturedValues.size() == 1);
		Assert.assertTrue(capturedValues.contains("company"));
	}

	@Test
	public void testIsNotSingleVirtualInstance() throws SQLException {
		_mockSingleVirtualInstance(false);

		Assert.assertFalse(
			DatabaseUtil.isSingleVirtualInstance(_sourceConnection));
	}

	@Test
	public void testIsSingleVirtualInstance() throws SQLException {
		_mockSingleVirtualInstance(true);

		Assert.assertTrue(
			DatabaseUtil.isSingleVirtualInstance(_sourceConnection));
	}

	@Test
	public void testValidReleaseState() throws SQLException {
		_mockReleaseState(true);

		List<String> failedServletContextNames =
			DatabaseUtil.getFailedServletContextNames(_sourceConnection);

		Assert.assertTrue(failedServletContextNames.isEmpty());
	}

	private void _mockDefaultPartition(boolean defaultPartition)
		throws SQLException {

		Mockito.when(
			_sourceConnection.getMetaData()
		).thenReturn(
			_sourceDatabaseMetaData
		);

		Mockito.when(
			_sourceDatabaseMetaData.getTables(
				Mockito.nullable(String.class), Mockito.nullable(String.class),
				Mockito.anyString(), Mockito.eq(new String[] {"TABLE"}))
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
	}

	private void _mockGetReleaseMap(Release release, boolean found)
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

		if (found) {
			Mockito.when(
				_sourceResultSet.next()
			).thenReturn(
				true
			).thenReturn(
				false
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
				_sourceResultSet.getBoolean(3)
			).thenReturn(
				release.getVerified()
			);
		}
		else {
			Mockito.when(
				_sourceResultSet.next()
			).thenReturn(
				false
			);
		}
	}

	private void _mockGetTables() throws SQLException {
		Mockito.when(
			_sourceConnection.getMetaData()
		).thenReturn(
			_sourceDatabaseMetaData
		);

		Mockito.when(
			_sourceDatabaseMetaData.getTables(
				Mockito.nullable(String.class), Mockito.nullable(String.class),
				Mockito.nullable(String.class), Mockito.any(String[].class))
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
			true
		).thenReturn(
			true
		).thenReturn(
			false
		);

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

		PreparedStatement preparedStatement1 = Mockito.mock(
			PreparedStatement.class);

		ResultSet resultSet1 = Mockito.mock(ResultSet.class);

		Mockito.when(
			_sourceConnection.prepareStatement(
				"select companyId from CompanyInfo")
		).thenReturn(
			preparedStatement1
		);

		Mockito.when(
			preparedStatement1.executeQuery()
		).thenReturn(
			resultSet1
		);

		Mockito.when(
			resultSet1.next()
		).thenReturn(
			false
		);

		PreparedStatement preparedStatement2 = Mockito.mock(
			PreparedStatement.class);

		ResultSet resultSet2 = Mockito.mock(ResultSet.class);

		Mockito.when(
			_sourceConnection.prepareStatement("select companyId from Company")
		).thenReturn(
			preparedStatement2
		);

		Mockito.when(
			preparedStatement2.executeQuery()
		).thenReturn(
			resultSet2
		);

		Mockito.when(
			resultSet2.next()
		).thenReturn(
			true
		).thenReturn(
			false
		);

		Mockito.when(
			resultSet2.getLong("companyId")
		).thenReturn(
			25000L
		);

		ResultSet resultSet3 = Mockito.mock(ResultSet.class);

		Mockito.when(
			_sourceDatabaseMetaData.getColumns(
				Mockito.nullable(String.class), Mockito.nullable(String.class),
				Mockito.nullable(String.class), Mockito.nullable(String.class))
		).thenReturn(
			resultSet3
		);

		Mockito.when(
			resultSet3.next()
		).thenReturn(
			true
		);
	}

	private void _mockReleaseState(boolean stateGood) throws SQLException {
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

		if (stateGood) {
			Mockito.when(
				_sourceResultSet.next()
			).thenReturn(
				false
			);
		}
		else {
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
				_sourceResultSet.getString(1)
			).thenReturn(
				"module1"
			).thenReturn(
				"module2"
			);
		}
	}

	private void _mockSingleVirtualInstance(boolean singleVirtualInstance)
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
			singleVirtualInstance ? 1 : 4
		);

		Mockito.when(
			_sourceResultSet.next()
		).thenReturn(
			true
		);
	}

	private void _mockWebId(boolean hasWebId) throws SQLException {
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