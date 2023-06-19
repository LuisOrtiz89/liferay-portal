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

import java.sql.Connection;
import java.sql.SQLException;

import java.util.Collections;
import java.util.List;

/**
 * @author Luis Ortiz
 */
public class Migrator {

	public static void migrateDatabases(
			Connection sourceConnection, Connection destinationConnection)
		throws SQLException {

		String newCatalog = "lportal_123456";

		List<String> copiedTableNames = _copyTableStructures(
			sourceConnection, destinationConnection, newCatalog);

		DatabaseUtil.copyTablesContent(
			sourceConnection, destinationConnection, newCatalog,
			copiedTableNames);
	}

	private static List<String> _copyNoncontrolTableStructures(
			Connection sourceConnection, Connection destinationConnection,
			String destinationCatalog, List<String> excludedTableNames)
		throws SQLException {

		return DatabaseUtil.copyTableStructures(
			sourceConnection, destinationConnection, destinationCatalog,
			excludedTableNames, false, false);
	}

	private static List<String> _copyObjectTableStructures(
			Connection sourceConnection, Connection destinationConnection,
			String destinationCatalog, List<String> excludedTableNames)
		throws SQLException {

		return DatabaseUtil.copyTableStructures(
			sourceConnection, destinationConnection, destinationCatalog,
			excludedTableNames, false, true);
	}

	private static List<String> _copyTableStructures(
			Connection sourceConnection, Connection destinationConnection,
			String destinationCatalog)
		throws SQLException {

		List<String> copiedTableNames = _copyNoncontrolTableStructures(
			destinationConnection, destinationConnection, destinationCatalog,
			Collections.emptyList());

		copiedTableNames.addAll(
			_copyObjectTableStructures(
				sourceConnection, destinationConnection, destinationCatalog,
				copiedTableNames));

		return copiedTableNames;
	}

}