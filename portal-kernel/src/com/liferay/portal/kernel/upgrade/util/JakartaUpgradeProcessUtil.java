/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.kernel.upgrade.util;

import com.liferay.portal.kernel.util.SetUtil;
import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.portal.kernel.util.StringUtil;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Luis Ortiz
 */
public class JakartaUpgradeProcessUtil {

	public static Object[] getSelectResultSetData(
			String columnName, String[] primaryKeyColumnNames,
			ResultSet resultSet)
		throws SQLException {

		Object[] result = new Object[primaryKeyColumnNames.length + 1];

		int i = 0;

		for (String primaryKeyColumnName : primaryKeyColumnNames) {
			result[i] = resultSet.getObject(primaryKeyColumnName);
			i++;
		}

		result[i] = resultSet.getString(columnName);

		return result;
	}

	public static String getSelectSQL(
		String columnName, String[] primaryKeyColumnNames, String tableName) {

		StringBundler sb = new StringBundler();

		sb.append("select ");

		for (String primaryKeyColumnName : primaryKeyColumnNames) {
			sb.append(primaryKeyColumnName);
			sb.append(", ");
		}

		sb.append(columnName);
		sb.append(" from ");
		sb.append(tableName);
		sb.append(" where ");
		sb.append(columnName);
		sb.append(" is not null");

		return sb.toString();
	}

	public static String getUpdateSQL(
		String columnName, String[] primaryKeyColumnNames, String tableName) {

		StringBundler sb = new StringBundler();

		sb.append("update ");
		sb.append(tableName);
		sb.append(" set ");
		sb.append(columnName);
		sb.append(" = ? where ");

		for (String primaryKeyColumnName : primaryKeyColumnNames) {
			sb.append(primaryKeyColumnName);
			sb.append(" = ?");
			sb.append(" and ");
		}

		sb.setIndex(sb.index() - 1);

		return sb.toString();
	}

	public static String replace(String value) {
		return _replace(value, _separators);
	}

	public static String replace(
		String value, Set<Character> customSeparators) {

		Set<Character> separators = new HashSet<>();

		separators.addAll(_separators);
		separators.addAll(customSeparators);

		return _replace(value, separators);
	}

	public static String updateJakartaValue(
			Character[] customSeparators, PreparedStatement preparedStatement,
			String[] primaryKeyColumnNames, Object[] values)
		throws SQLException {

		String javaxValue = (String)values[values.length - 1];

		if (javaxValue == null) {
			return null;
		}

		String jakartaValue;

		if (customSeparators.length > 0) {
			jakartaValue = replace(
				javaxValue, SetUtil.fromArray(customSeparators));
		}
		else {
			jakartaValue = replace(javaxValue);
		}

		if (javaxValue.length() != jakartaValue.length()) {
			preparedStatement.setString(1, jakartaValue);

			for (int i = 0; i < primaryKeyColumnNames.length; i++) {
				preparedStatement.setObject(i + 2, values[i]);
			}

			preparedStatement.addBatch();

			StringBundler sb = new StringBundler("(");

			for (int i = 0; i < primaryKeyColumnNames.length; i++) {
				sb.append(values[i]);
				sb.append(", ");
			}

			sb.setIndex(sb.index() - 1);
			sb.append(")");

			return sb.toString();
		}

		return null;
	}

	private static String _replace(String value, Set<Character> separators) {
		for (String subpackageName : _subpackageNames) {
			String javaxPackage = "javax." + subpackageName;
			String jakartaPackage = "jakarta." + subpackageName;

			value = StringUtil.replace(value, javaxPackage, jakartaPackage);

			for (Character separator : separators) {
				value = StringUtil.replace(
					value, StringUtil.replace(javaxPackage, '.', separator),
					StringUtil.replace(jakartaPackage, '.', separator));
			}
		}

		for (String fixupSubpackageName : _fixupSubpackageNames) {
			String fixupJavaxPackage = "javax." + fixupSubpackageName;
			String fixupJakartaPackage = "jakarta." + fixupSubpackageName;

			value = StringUtil.replace(
				value, fixupJakartaPackage, fixupJavaxPackage);

			for (Character separator : separators) {
				value = StringUtil.replace(
					value,
					StringUtil.replace(fixupJakartaPackage, '.', separator),
					StringUtil.replace(fixupJavaxPackage, '.', separator));
			}
		}

		return StringUtil.replace(
			value, "X-JAVAX-PORTLET-NAMESPACED-RESPONSE",
			"X-JAKARTA-PORTLET-NAMESPACED-RESPONSE");
	}

	private static final Set<String> _fixupSubpackageNames = new HashSet<>(
		Arrays.asList("annotation.processing", "transaction.xa"));
	private static final Set<Character> _separators = new HashSet<>(
		Arrays.asList('.', '-', '/'));
	private static final Set<String> _subpackageNames = new HashSet<>(
		Arrays.asList(
			"activation", "annotation", "batch", "decorator", "ejb", "el",
			"enterprise", "faces", "inject", "interceptor", "jms", "json",
			"jws", "mail", "mvc", "persistence", "portlet", "resource",
			"security.auth.message", "security.enterprise", "security.jacc",
			"servlet", "transaction", "validation", "websocket", "ws.rs",
			"xml.bind", "xml.soap", "xml.ws"));

}