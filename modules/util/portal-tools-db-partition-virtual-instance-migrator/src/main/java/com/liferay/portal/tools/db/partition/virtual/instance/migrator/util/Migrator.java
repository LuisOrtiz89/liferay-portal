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

import java.sql.Connection;

import java.util.Collections;
import java.util.List;

/**
 * @author Luis Ortiz
 */
public class Migrator {

	public static void migrateDatabases(
			Connection sourceConnection, Connection targetConnection)
		throws Exception {

		long sourceCompanyId = DatabaseUtil.getCompanyId(sourceConnection);

		if ((sourceCompanyId == 0) ||
			!DatabaseUtil.checkCompanyIdEligible(
				sourceCompanyId, targetConnection)) {

			throw new Exception(
				StringBundler.concat(
					"CompanyId ", sourceCompanyId,
					" already exists in the target database. Migration is not ",
					"possible"));
		}

		_migrateControlTables(sourceConnection, targetConnection);

		String newCatalog = DatabaseUtil.createCatalog(
			sourceCompanyId, targetConnection);

		List<String> copiedTableNames = _copyTableStructures(
			sourceConnection, newCatalog, targetConnection);

		DatabaseUtil.copyTablesContent(
			sourceConnection, copiedTableNames, newCatalog, targetConnection);
	}

	private static List<String> _copyNoncontrolTableStructures(
			List<String> excludedTableNames, Connection sourceConnection,
			String targetCatalog, Connection targetConnection)
		throws Exception {

		return DatabaseUtil.copyTableStructures(
			false, excludedTableNames, false, sourceConnection, targetCatalog,
			targetConnection);
	}

	private static List<String> _copyObjectTableStructures(
			List<String> excludedTableNames, Connection sourceConnection,
			String targetCatalog, Connection targetConnection)
		throws Exception {

		return DatabaseUtil.copyTableStructures(
			false, excludedTableNames, true, sourceConnection, targetCatalog,
			targetConnection);
	}

	private static List<String> _copyTableStructures(
			Connection sourceConnection, String targetCatalog,
			Connection targetConnection)
		throws Exception {

		List<String> copiedTableNames = _copyNoncontrolTableStructures(
			Collections.emptyList(), targetConnection, targetCatalog,
			targetConnection);

		copiedTableNames.addAll(
			_copyObjectTableStructures(
				copiedTableNames, sourceConnection, targetCatalog,
				targetConnection));

		return copiedTableNames;
	}

	private static void _migrateCompanyTable(
			Connection sourceConnection, Connection targetConnection)
		throws Exception {

		DatabaseUtil.copyTableRecordsByCompanyId(
			DatabaseUtil.getCompanyId(sourceConnection), sourceConnection,
			"Company", targetConnection);
	}

	private static void _migrateControlTables(
			Connection sourceConnection, Connection targetConnection)
		throws Exception {

		_migrateCompanyTable(sourceConnection, targetConnection);
	}

}