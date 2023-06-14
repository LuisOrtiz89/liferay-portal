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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Luis Ortiz
 */
public class DatabaseUtil {
	public static void copyTablesContent(
			Connection sourceConnection, Connection destinationConnection,
			String newCatalog, List<String> tableNames)
		throws SQLException {

		String currentCatalog = destinationConnection.getCatalog();

		for (String tableName : tableNames) {
			int count = 0;

			try (PreparedStatement preparedStatement =
					sourceConnection.prepareStatement(
						"select * from " + tableName)) {

				preparedStatement.setFetchSize(_FETCH_SIZE);

				boolean autoCommit = destinationConnection.getAutoCommit();

				try (ResultSet resultSet = preparedStatement.executeQuery()) {
					destinationConnection.setCatalog(newCatalog);

					String query = _getInsertRowQuery(tableName, resultSet);

					destinationConnection.setAutoCommit(false);

					PreparedStatement preparedStatement1 =
						destinationConnection.prepareStatement(query);

					int batchCount = 0;

					while (resultSet.next()) {
						_populateParamsDynamically(
							preparedStatement1, resultSet);

						preparedStatement1.addBatch();

						if (++batchCount >= _BATCH_SIZE) {
							batchCount = 0;

							int[] counts = preparedStatement1.executeBatch();

							count += Arrays.stream(
								counts
							).sum();
						}
					}

					int[] counts = preparedStatement1.executeBatch();

					count += Arrays.stream(
						counts
					).sum();
				}
				finally {
					destinationConnection.setCatalog(currentCatalog);
					destinationConnection.setAutoCommit(autoCommit);
				}
			}

			System.out.println(
				StringBundler.concat(
					"[INFO] Copied ", count, " rows for table ", tableName));
		}
	}

	public static List<String> copyTableStructures(
			Connection sourceConnection, Connection destinationConnection,
			String destinationCatalog, List<String> exclusions,
			boolean controlTables, boolean objectTables)
		throws Exception {

		boolean local = _sameHostDatabases(
			sourceConnection, destinationConnection);

		List<String> tableNames = getPartitionedTableNames(
			sourceConnection, controlTables, objectTables);

		List<String> copiedTableNames = new ArrayList<>();

		String defaultCatalog = destinationConnection.getCatalog();

		String sourceDatabaseURL =
			_getHostFromConnection(sourceConnection) + "/" +
			sourceConnection.getCatalog();

		String query = "";

		for (String tableName : tableNames) {
			if (!exclusions.contains(tableName)) {
				if (local) {
					query = _getLocalCreateTableSQL(
						sourceConnection.getCatalog(), destinationCatalog,
						tableName);
				}
				else {
					query = _getRemoteCreateTableSQL(
						sourceConnection, tableName);
				}

				try {
					destinationConnection.setCatalog(destinationCatalog);

					try (PreparedStatement preparedStatement =
							 destinationConnection.prepareStatement(query)) {

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
					destinationConnection.setCatalog(defaultCatalog);
				}
			}
		}

		return copiedTableNames;
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

	public static List<String> getPartitionedTableNames(Connection connection,
			boolean controlTables, boolean objectTables)
		throws Exception {

		List<String> partitionedTableNames = new ArrayList<>();

		List<Long> companyIds = _getCompanyIds(connection);

		DBInspector dbInspector = new DBInspector(connection);

		for (String tableName : dbInspector.getTableNames(null)) {
			if (dbInspector.isObjectTable(companyIds, tableName) && !objectTables) {
				continue;
			}
			else if (dbInspector.isControlTable(companyIds, tableName) &&
					 !controlTables) {

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

	private static String _getHostFromConnection(Connection connection)
		throws SQLException {

		DatabaseMetaData databaseMetaData = connection.getMetaData();

		String databaseURL = databaseMetaData.getURL();

		Matcher matcher = _jdbcHostPattern.matcher(databaseURL);

		if (matcher.matches()) {
			return matcher.group(1);
		}

		return null;
	}

	private static String _getInsertRowQuery(
			String tableName, ResultSet resultSet)
		throws SQLException {

		String query = "insert into " + tableName + " (";

		ResultSetMetaData resultSetMetaData = resultSet.getMetaData();

		int nColumns = resultSetMetaData.getColumnCount();

		for (int count = 1; count <= nColumns; count++) {
			query += resultSetMetaData.getColumnName(count);

			if (count < nColumns) {
				query += ", ";
			}
		}

		query += ") values (";

		for (int count = 1; count <= nColumns; count++) {
			query += "?";

			if (count < nColumns) {
				query += ", ";
			}
		}

		return query + ")";
	}

	private static String _getLocalCreateTableSQL(
		String sourceCatalog, String destinationCatalog, String tableName) {

		return StringBundler.concat(
			"create table if not exists ", destinationCatalog,
			StringPool.PERIOD, tableName, " like ", sourceCatalog,
			StringPool.PERIOD, tableName);
	}

	private static String _getRemoteCreateTableSQL(
			Connection connection, String tableName)
		throws SQLException {

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

	private static void _populateParamsDynamically(
			PreparedStatement preparedStatement, ResultSet resultSet)
		throws SQLException {

		ResultSetMetaData resultSetMetaData = resultSet.getMetaData();

		int nColumns = resultSetMetaData.getColumnCount();

		for (int count = 1; count <= nColumns; count++) {
			preparedStatement.setObject(count, resultSet.getObject(count));
		}
	}

	private static boolean _sameHostDatabases(
			Connection sourceConnection, Connection destinationConnection)
		throws SQLException {

		String sourceURL = _getHostFromConnection(sourceConnection);
		String destinationURL = _getHostFromConnection(destinationConnection);

		if (!sourceURL.equals(destinationURL)) {
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