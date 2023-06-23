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
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.dao.db.DBInspector;
import com.liferay.portal.kernel.version.Version;
import com.liferay.portal.tools.db.partition.virtual.instance.migrator.Release;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Luis Ortiz
 */
public class DatabaseUtil {

	public static boolean checkCompanyIdEligible(
			long companyId, Connection connection)
		throws SQLException {

		try (PreparedStatement preparedStatement = connection.prepareStatement(
				"select companyId from Company where companyId = ?")) {

			preparedStatement.setLong(1, companyId);

			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				if (resultSet.next()) {
					return false;
				}
			}
		}

		return true;
	}

	public static void copyTableRecordsByCompanyId(
			long companyId, Connection sourceConnection, String tableName,
			Connection targetConnection)
		throws SQLException {

		int rowCount = _copyTableRecords(
			StringBundler.concat(
				"select * from ", tableName, " where companyId=", companyId),
			sourceConnection, tableName, null, targetConnection);

		System.out.println(
			StringBundler.concat(
				"[INFO] Copied ", rowCount, " rows for table ", tableName,
				" with companyId ", companyId));
	}

	public static void copyTablesContent(
			Connection sourceConnection, List<String> tableNames,
			String targetCatalog, Connection targetConnection)
		throws SQLException {

		for (String tableName : tableNames) {
			int rowCount = _copyTableRecords(
				"select * from " + tableName, sourceConnection, tableName,
				targetCatalog, targetConnection);

			System.out.println(
				StringBundler.concat(
					"[INFO] Copied ", rowCount, " rows for table ", tableName));
		}
	}

	public static List<String> copyTableStructures(
			boolean controlTables, List<String> excludedTableNames,
			boolean objectTables, Connection sourceConnection,
			String targetCatalog, Connection targetConnection)
		throws Exception {

		List<String> copiedTableNames = new ArrayList<>();

		String defaultCatalog = targetConnection.getCatalog();

		boolean local = _sameHostDatabases(sourceConnection, targetConnection);

		String sourceDatabaseURL =
			_getHostFromConnection(sourceConnection) + "/" +
				sourceConnection.getCatalog();

		List<String> tableNames = getPartitionedTableNames(
			sourceConnection, controlTables, objectTables);

		for (String tableName : tableNames) {
			if (!excludedTableNames.contains(tableName)) {
				String query = "";

				if (local) {
					query = _getLocalCreateTableSQL(
						sourceConnection.getCatalog(), tableName,
						targetCatalog);
				}
				else {
					query = _getRemoteCreateTableSQL(
						sourceConnection, tableName);
				}

				try {
					targetConnection.setCatalog(targetCatalog);

					try (PreparedStatement preparedStatement =
							targetConnection.prepareStatement(query)) {

						preparedStatement.executeUpdate();

						copiedTableNames.add(tableName);

						System.out.println(
							StringBundler.concat(
								"[INFO] Copied table structure for table ",
								tableName, " from ", sourceDatabaseURL,
								" by using the script \"", query, "\""));
					}
				}
				finally {
					targetConnection.setCatalog(defaultCatalog);
				}
			}
		}

		return copiedTableNames;
	}

	public static String createCatalog(long companyId, Connection connection)
		throws SQLException {

		try (PreparedStatement preparedStatement = connection.prepareStatement(
				_getCreateSchemaSQL(companyId, connection))) {

			preparedStatement.executeUpdate();
		}

		return _getSchemaName(companyId);
	}

	public static long getCompanyId(Connection connection) throws SQLException {
		try (PreparedStatement preparedStatement = connection.prepareStatement(
				"select companyId from CompanyInfo");
			ResultSet resultSet = preparedStatement.executeQuery()) {

			if (resultSet.next()) {
				return resultSet.getLong(1);
			}
		}

		return 0;
	}

	public static List<String> getFailedServletContextNames(
			Connection connection)
		throws SQLException {

		List<String> failedServletContextNames = new ArrayList<>();

		try (PreparedStatement preparedStatement = connection.prepareStatement(
				"select servletContextName from Release_ where state_ != 0;");
			ResultSet resultSet = preparedStatement.executeQuery()) {

			while (resultSet.next()) {
				failedServletContextNames.add(resultSet.getString(1));
			}
		}

		return failedServletContextNames;
	}

	public static List<String> getPartitionedTableNames(
			Connection connection, boolean controlTables, boolean objectTables)
		throws Exception {

		List<String> partitionedTableNames = new ArrayList<>();

		List<Long> companyIds = _getCompanyIds(connection);

		DBInspector dbInspector = new DBInspector(connection);

		for (String tableName : dbInspector.getTableNames(null)) {
			if (dbInspector.isControlTable(companyIds, tableName) &&
				!controlTables) {

				continue;
			}
			else if (dbInspector.isObjectTable(companyIds, tableName) &&
					 !objectTables) {

				continue;
			}

			partitionedTableNames.add(tableName);
		}

		return partitionedTableNames;
	}

	public static List<Release> getReleases(Connection connection)
		throws SQLException {

		List<Release> releases = new ArrayList<>();

		Map<String, Release> releasesMap = getReleasesMap(connection);

		for (Release release : releasesMap.values()) {
			releases.add(release);
		}

		return releases;
	}

	public static Map<String, Release> getReleasesMap(Connection connection)
		throws SQLException {

		Map<String, Release> releasesMap = new HashMap<>();

		try (PreparedStatement preparedStatement = connection.prepareStatement(
				"select servletContextName, schemaVersion, verified from " +
					"Release_");
			ResultSet resultSet = preparedStatement.executeQuery()) {

			while (resultSet.next()) {
				String servletContextName = resultSet.getString(1);

				releasesMap.put(
					servletContextName,
					new Release(
						Version.parseVersion(resultSet.getString(2)),
						servletContextName, resultSet.getBoolean(3)));
			}
		}

		return releasesMap;
	}

	public static String getWebId(Connection connection) throws SQLException {
		try (PreparedStatement preparedStatement = connection.prepareStatement(
				"select webId from Company, CompanyInfo where " +
					"CompanyInfo.companyId = Company.companyId");
			ResultSet resultSet = preparedStatement.executeQuery()) {

			if (resultSet.next()) {
				return resultSet.getString(1);
			}

			return null;
		}
	}

	public static boolean hasSingleCompanyInfo(Connection connection)
		throws SQLException {

		try (PreparedStatement preparedStatement = connection.prepareStatement(
				"select count(1) from CompanyInfo");
			ResultSet resultSet = preparedStatement.executeQuery()) {

			if (resultSet.next() && (resultSet.getInt(1) > 1)) {
				return false;
			}
		}

		return true;
	}

	public static boolean hasWebId(Connection connection, String webId)
		throws SQLException {

		try (PreparedStatement preparedStatement = connection.prepareStatement(
				"select companyId from Company where webId = ?")) {

			preparedStatement.setString(1, webId);

			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				if (resultSet.next()) {
					return true;
				}
			}
		}

		return false;
	}

	public static boolean isDefaultPartition(Connection connection)
		throws Exception {

		DBInspector dbInspector = new DBInspector(connection);

		return dbInspector.hasTable("Company");
	}

	public static void setSchemaPrefix(String schemaPrefix) {
		_schemaPrefix = schemaPrefix;
	}

	private static int _copyTableRecords(
			String selectQuery, Connection sourceConnection, String tableName,
			String targetCatalog, Connection targetConnection)
		throws SQLException {

		int rowCount = 0;

		String currentCatalog = targetConnection.getCatalog();

		try (PreparedStatement preparedStatement1 =
				sourceConnection.prepareStatement(selectQuery)) {

			preparedStatement1.setFetchSize(_FETCH_SIZE);

			boolean autoCommit = targetConnection.getAutoCommit();

			try (ResultSet resultSet = preparedStatement1.executeQuery()) {
				String insertQuery = _getInsertRowQuery(resultSet, tableName);

				targetConnection.setAutoCommit(false);

				if (targetCatalog != null) {
					targetConnection.setCatalog(targetCatalog);
				}

				PreparedStatement preparedStatement2 =
					targetConnection.prepareStatement(insertQuery);

				int batchCount = 0;

				while (resultSet.next()) {
					_populateParamsDynamically(preparedStatement2, resultSet);

					preparedStatement2.addBatch();

					if (++batchCount >= _BATCH_SIZE) {
						batchCount = 0;

						int[] rowCounts = preparedStatement2.executeBatch();

						for (int rows : rowCounts) {
							rowCount += rows;
						}
					}
				}

				int[] rowCounts = preparedStatement2.executeBatch();

				for (int rows : rowCounts) {
					rowCount += rows;
				}
			}
			finally {
				targetConnection.setAutoCommit(autoCommit);

				if (targetCatalog != null) {
					targetConnection.setCatalog(currentCatalog);
				}
			}
		}

		return rowCount;
	}

	private static List<Long> _getCompanyIds(Connection connection)
		throws Exception {

		List<Long> companyIds = new ArrayList<>();

		try (PreparedStatement preparedStatement = connection.prepareStatement(
				"select companyId from Company");
			ResultSet resultSet = preparedStatement.executeQuery()) {

			while (resultSet.next()) {
				companyIds.add(resultSet.getLong("companyId"));
			}
		}

		return companyIds;
	}

	private static String _getCreateSchemaSQL(
			long companyId, Connection connection)
		throws SQLException {

		return StringBundler.concat(
			"create schema if not exists ", _getSchemaName(companyId),
			" character set ", _getSessionCharsetEncoding(connection));
	}

	private static String _getHostFromConnection(Connection connection)
		throws Exception {

		DatabaseMetaData databaseMetaData = connection.getMetaData();

		String databaseURL = databaseMetaData.getURL();

		Matcher matcher = _jdbcHostPattern.matcher(databaseURL);

		if (matcher.matches()) {
			return matcher.group(1);
		}

		return null;
	}

	private static String _getInsertRowQuery(
			ResultSet resultSet, String tableName)
		throws SQLException {

		String query = "insert into " + tableName + " (";

		ResultSetMetaData resultSetMetaData = resultSet.getMetaData();

		int columns = resultSetMetaData.getColumnCount();

		for (int count = 1; count <= columns; count++) {
			query += resultSetMetaData.getColumnName(count);

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

		return query + ")";
	}

	private static String _getLocalCreateTableSQL(
		String sourceCatalog, String tableName, String targetCatalog) {

		return StringBundler.concat(
			"create table if not exists ", targetCatalog, StringPool.PERIOD,
			tableName, " like ", sourceCatalog, StringPool.PERIOD, tableName);
	}

	private static String _getRemoteCreateTableSQL(
			Connection connection, String tableName)
		throws Exception {

		try (PreparedStatement preparedStatement = connection.prepareStatement(
				"show create table " + tableName)) {

			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				if (resultSet.next()) {
					return resultSet.getString(2);
				}
			}
		}

		return null;
	}

	private static String _getSchemaName(long companyId) {
		return _schemaPrefix + companyId;
	}

	private static String _getSessionCharsetEncoding(Connection connection)
		throws SQLException {

		try (PreparedStatement preparedStatement = connection.prepareStatement(
				"select variable_value from " +
					"performance_schema.session_variables where " +
						"variable_name = 'character_set_client'");
			ResultSet resultSet = preparedStatement.executeQuery()) {

			if (resultSet.next()) {
				return resultSet.getString("variable_value");
			}

			return "utf8";
		}
	}

	private static void _populateParamsDynamically(
			PreparedStatement preparedStatement, ResultSet resultSet)
		throws SQLException {

		ResultSetMetaData resultSetMetaData = resultSet.getMetaData();

		int columns = resultSetMetaData.getColumnCount();

		for (int count = 1; count <= columns; count++) {
			preparedStatement.setObject(count, resultSet.getObject(count));
		}
	}

	private static boolean _sameHostDatabases(
			Connection sourceConnection, Connection targetConnection)
		throws Exception {

		String sourceURL = _getHostFromConnection(sourceConnection);
		String targetURL = _getHostFromConnection(targetConnection);

		if (!sourceURL.equals(targetURL)) {
			return false;
		}

		return true;
	}

	private static final int _BATCH_SIZE = 100;

	private static final int _FETCH_SIZE = 100;

	private static final Pattern _jdbcHostPattern = Pattern.compile(
		"jdbc:mysql://([^\\?]*)/([^\\?]*)(\\?([^\\?]*))?");
	private static String _schemaPrefix = "lpartition_";

}