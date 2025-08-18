/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.upgrade.data.cleanup;

import com.liferay.document.library.kernel.model.DLFileEntry;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.upgrade.data.cleanup.DataCleanupPreupgradeProcess;
import com.liferay.portal.kernel.upgrade.data.cleanup.DefaultAllTablesOrphanReferencesDataCleanupPreupgradeProcess;
import com.liferay.portal.kernel.upgrade.data.cleanup.FilterableAllTablesOrphanReferencesDataCleanupPreupgradeProcess;
import com.liferay.portal.kernel.upgrade.data.cleanup.TableOrphanReferencesDataCleanupPreupgradeProcess;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * @author István András Dézsi
 */
public class DLFileEntryDataCleanupPreupgradeProcess
	extends DataCleanupPreupgradeProcess {

	@Override
	protected void doUpgrade() throws Exception {
		upgrade(
			new DataCleanupPreupgradeProcess() {

				@Override
				protected void doUpgrade() throws Exception {
					try (PreparedStatement preparedStatement1 =
							connection.prepareStatement(
								"select fileEntryId, name from DLFileEntry " +
									"where name is null or name = ''");
						PreparedStatement preparedStatement2 =
							connection.prepareStatement(
								"delete from DLFileEntry where name is null " +
									"or name = ''");
						ResultSet resultSet =
							preparedStatement1.executeQuery()) {

						preparedStatement2.execute();

						if (!_log.isInfoEnabled()) {
							return;
						}

						while (resultSet.next()) {
							long fileEntryId = resultSet.getLong("fileEntryId");
							String name = resultSet.getString("name");

							_log.info(
								StringBundler.concat(
									"Deleted document library file entry ",
									fileEntryId, " because its name was ",
									(name == null) ? "null" : "empty"));
						}
					}
				}

			});

		upgrade(
			new FilterableAllTablesOrphanReferencesDataCleanupPreupgradeProcess(
				StringBundler.concat(
					"classNameId = (select classNameId from ClassName_ where ",
					"value = '", FileEntry.class.getName(), "')"),
				new String[] {"classNameId"}, "classPK",
				new String[] {"fileEntryId"}, "DLFileEntry"));

		upgrade(
			new DefaultAllTablesOrphanReferencesDataCleanupPreupgradeProcess(
				"fileEntryId", "DLFileEntry"));

		upgrade(
			new TableOrphanReferencesDataCleanupPreupgradeProcess(
				"name = '" + DLFileEntry.class.getName() + "'", "primKeyId",
				"ResourcePermission", "fileEntryId", "DLFileEntry"));
	}

	private static final Log _log = LogFactoryUtil.getLog(
		DLFileEntryDataCleanupPreupgradeProcess.class);

}