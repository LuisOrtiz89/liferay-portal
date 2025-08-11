/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.upgrade.data.cleanup;

import com.liferay.portal.kernel.dao.db.DB;
import com.liferay.portal.kernel.dao.db.DBInspector;
import com.liferay.portal.kernel.dao.db.DBManagerUtil;
import com.liferay.portal.kernel.dao.db.DBType;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.upgrade.data.cleanup.DataCleanupPreupgradeProcess;

import java.sql.PreparedStatement;

/**
 * @author Luis Ortiz
 */
public class AnalyticsMessageDataCleanupPreupgradeProcess
	extends DataCleanupPreupgradeProcess {

	@Override
	protected void doUpgrade() throws Exception {
		DBInspector dbInspector = new DBInspector(connection);

		if (!dbInspector.hasTable("AnalyticsMessage")) {
			return;
		}

		String sql = "truncate table AnalyticsMessage";

		DB db = DBManagerUtil.getDB();

		if (db.getDBType() == DBType.DB2) {
			sql += " immediate";
		}

		try (PreparedStatement preparedStatement = connection.prepareStatement(
				sql)) {

			int rowCount = preparedStatement.executeUpdate();

			if (_log.isInfoEnabled() && (rowCount > 0)) {
				_log.info(
					"Deleted content of table " +
						dbInspector.normalizeName("AnalyticsMessage"));
			}
		}
	}

	private static final Log _log = LogFactoryUtil.getLog(
		AnalyticsMessageDataCleanupPreupgradeProcess.class);

}