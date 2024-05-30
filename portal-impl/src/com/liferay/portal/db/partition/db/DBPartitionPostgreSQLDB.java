/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.db.partition.db;

import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.util.StringUtil;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author Alberto Chaparro
 */
public class DBPartitionPostgreSQLDB implements DBPartitionDB {

	@Override
	public String getCreatePartitionSQL(
			Connection connection, String partitionName)
		throws SQLException {

		return "create schema if not exists " + partitionName;
	}

	@Override
	public String getCreateTableSQL(
		String fromPartitionName, String toPartitionName, String tableName) {

		return StringBundler.concat(
			"create table if not exists ", toPartitionName, StringPool.PERIOD,
			tableName, " (like ", fromPartitionName, StringPool.PERIOD,
			tableName, " including all)");
	}

	@Override
	public String getDefaultPartitionName(Connection connection)
		throws SQLException {

		return connection.getSchema();
	}

	@Override
	public String getDropPartitionSQL(String partitionName) {
		return "drop schema if exists " + partitionName + " cascade";
	}

	@Override
	public String getRenameTableSQL(
		String fromTableName, String partitionName, String toTableName) {

		return StringBundler.concat(
			"alter table ", partitionName, StringPool.PERIOD, fromTableName,
			" rename to ", toTableName, ";alter table ", partitionName,
			StringPool.PERIOD, toTableName, " rename constraint ",
			fromTableName, "_pkey to ", toTableName, "_pkey;");
	}

	@Override
	public String getSafeAlterTable(String alterTableSQL) {
		if (StringUtil.count(StringUtil.toLowerCase(alterTableSQL), "drop ") >
				0) {

			return alterTableSQL + " cascade";
		}

		return alterTableSQL;
	}

	@Override
	public String getSchema(Connection connection, String partitionName) {
		return partitionName;
	}

	@Override
	public boolean isDDLTransactional() {
		return true;
	}

	@Override
	public void setPartition(Connection connection, String partitionName)
		throws SQLException {

		connection.setSchema(partitionName);
	}

}