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

package com.liferay.portal.upgrade.util;

import com.liferay.portal.kernel.dao.jdbc.DataAccess;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Luis Ortiz
 */
public class DBUpgradeStatus {

	public static void addErrorMessage(String loggerName, String message) {
		Map<String, Integer> errorMessages = _errorMessages.computeIfAbsent(
			loggerName, key -> new ConcurrentHashMap<>());

		int occurrences = errorMessages.computeIfAbsent(message, key -> 0);

		occurrences++;

		errorMessages.put(message, occurrences);
	}

	public static void addEventMessage(String loggerName, String message) {
		List<String> eventMessages = _eventMessages.computeIfAbsent(
			loggerName, key -> new ArrayList<>());

		eventMessages.add(message);
	}

	public static void addWarningMessage(String loggerName, String message) {
		Map<String, Integer> warningMessages = _warningMessages.computeIfAbsent(
			loggerName, key -> new ConcurrentHashMap<>());

		int count = warningMessages.computeIfAbsent(message, key -> 0);

		count++;

		warningMessages.put(message, count);
	}

	public static Map<String, Map<String, Integer>> getErrorMessages() {
		_filterMessages();

		return _errorMessages;
	}

	public static Map<String, ArrayList<String>> getEventMessages() {
		return _eventMessages;
	}

	public static String getExpectedSchemaVersion(String bundleSymbolicName) {
		ModuleSchemaVersions moduleSchemaVersions =
			_moduleSchemaVersionsMap.get(bundleSymbolicName);

		return moduleSchemaVersions.getExpectedSchemaVersion();
	}

	public static String getFinalSchemaVersion(String bundleSymbolicName) {
		ModuleSchemaVersions moduleSchemaVersions =
			_moduleSchemaVersionsMap.get(bundleSymbolicName);

		return moduleSchemaVersions.getFinalSchemaVersion();
	}

	public static String getInitialSchemaVersion(String bundleSymbolicName) {
		ModuleSchemaVersions moduleSchemaVersions =
			_moduleSchemaVersionsMap.get(bundleSymbolicName);

		return moduleSchemaVersions.getInitialSchemaVersion();
	}

	public static Map<String, Map<String, Integer>> getWarningMessages() {
		_filterMessages();

		return _warningMessages;
	}

	public static void setInitialSchemaVersion(
		String bundleSymbolicName, String schemaVersion) {

		_moduleSchemaVersionsMap.putIfAbsent(
			bundleSymbolicName, new ModuleSchemaVersions(schemaVersion));
	}

	public static void upgradeFinished() {
		_setFinalSchemaVersion();
	}

	private static void _filterMessages() {
		if (!_filtered) {
			for (String filteredClassName : _FILTERED_CLASS_NAMES) {
				_errorMessages.remove(filteredClassName);
				_warningMessages.remove(filteredClassName);
			}

			_filtered = true;
		}
	}

	private static void _setFinalSchemaVersion() {
		try (Connection connection = DataAccess.getConnection();
			PreparedStatement preparedStatement = connection.prepareStatement(
				"select servletContextName, schemaVersion from Release_")) {

			ResultSet resultSet = preparedStatement.executeQuery();

			while (resultSet.next()) {
				String servletContextName = resultSet.getString(
					"servletContextName");
				String schemaVersion = resultSet.getString("schemaVersion");

				ModuleSchemaVersions moduleSchemaVersions =
					_moduleSchemaVersionsMap.get(servletContextName);

				if (moduleSchemaVersions == null) {
					moduleSchemaVersions = new ModuleSchemaVersions(null);

					_moduleSchemaVersionsMap.put(
						servletContextName, moduleSchemaVersions);
				}

				moduleSchemaVersions.setFinalSchemaVersion(schemaVersion);
			}
		}
		catch (SQLException sqlException) {
			_log.error("Unable to get schema version", sqlException);
		}
	}

	private static void _setInitialSchemaVersion() {
		try (Connection connection = DataAccess.getConnection();
			PreparedStatement preparedStatement = connection.prepareStatement(
				"select servletContextName, schemaVersion from Release_")) {

			ResultSet resultSet = preparedStatement.executeQuery();

			while (resultSet.next()) {
				String servletContextName = resultSet.getString(
					"servletContextName");
				String schemaVersion = resultSet.getString("schemaVersion");

				_moduleSchemaVersionsMap.putIfAbsent(
					servletContextName,
					new ModuleSchemaVersions(schemaVersion));
			}
		}
		catch (SQLException sqlException) {
			_log.error("Unable to get schema version", sqlException);
		}
	}

	private static final String[] _FILTERED_CLASS_NAMES = {
		"com.liferay.portal.search.elasticsearch7.internal.sidecar." +
			"SidecarManager"
	};

	private static final Log _log = LogFactoryUtil.getLog(
		DBUpgradeStatus.class);

	private static final Map<String, Map<String, Integer>> _errorMessages =
		new ConcurrentHashMap<>();
	private static final Map<String, ArrayList<String>> _eventMessages =
		new ConcurrentHashMap<>();
	private static boolean _filtered;
	private static final Map<String, ModuleSchemaVersions>
		_moduleSchemaVersionsMap = new HashMap<>();
	private static final String _upgradeStatus = "Pending";
	private static String _upgradeType;
	private static final Map<String, Map<String, Integer>> _warningMessages =
		new ConcurrentHashMap<>();

	static {
		_setInitialSchemaVersion();
	}

	private static class ModuleSchemaVersions {

		public ModuleSchemaVersions(String initialSchemaVersion) {
			_initialSchemaVersion = initialSchemaVersion;
		}

		public String getExpectedSchemaVersion() {
			return _expectedSchemaVersion;
		}

		public String getFinalSchemaVersion() {
			return _finalSchemaVersion;
		}

		public String getInitialSchemaVersion() {
			return _initialSchemaVersion;
		}

		public void setExpectedSchemaVersion(String expectedSchemaVersion) {
			_expectedSchemaVersion = expectedSchemaVersion;
		}

		public void setFinalSchemaVersion(String finalSchemaVersion) {
			_finalSchemaVersion = finalSchemaVersion;
		}

		private String _expectedSchemaVersion;
		private String _finalSchemaVersion;
		private final String _initialSchemaVersion;

	}

}