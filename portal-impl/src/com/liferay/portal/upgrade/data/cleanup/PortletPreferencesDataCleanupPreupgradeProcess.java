/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.upgrade.data.cleanup;

import com.liferay.petra.string.StringBundler;
import com.liferay.portal.dao.orm.common.SQLTransformer;
import com.liferay.portal.kernel.upgrade.data.cleanup.DataCleanupPreupgradeProcess;
import com.liferay.portal.kernel.upgrade.data.cleanup.TableOrphanReferencesDataCleanupPreupgradeProcess;
import com.liferay.portal.kernel.util.PortletKeys;

/**
 * @author Luis Ortiz
 */
public class PortletPreferencesDataCleanupPreupgradeProcess
	extends DataCleanupPreupgradeProcess {

	@Override
	protected void doUpgrade() throws Exception {
		upgrade(
			new TableOrphanReferencesDataCleanupPreupgradeProcess(
				null,
				"not exists (select 1 from LayoutRevision where " +
					"layoutRevisionId = [$SOURCE_TABLE_ALIAS$].plid)",
				"plid", "PortletPreferences", "plid", "Layout"));
		upgrade(
			new TableOrphanReferencesDataCleanupPreupgradeProcess(
				SQLTransformer.transform(
					StringBundler.concat(
						"CASE WHEN INSTR([$SOURCE_TABLE_ALIAS$].portletId, ",
						"'_INSTANCE_') > 0 THEN SUBSTR([$SOURCE_TABLE_ALIAS$].",
						"portletId, 1, INSTR([$SOURCE_TABLE_ALIAS$].",
						"portletId, '_INSTANCE_') - 1) ELSE ",
						"[$SOURCE_TABLE_ALIAS$].portletId END or ",
						"[$TARGET_TABLE_ALIAS$].portletId = CASE WHEN ",
						"INSTR([$SOURCE_TABLE_ALIAS$].portletId, '_USER_') > ",
						"0 THEN SUBSTR([$SOURCE_TABLE_ALIAS$].portletId, 1, ",
						"INSTR([$SOURCE_TABLE_ALIAS$].portletId, '_USER_') - ",
						"1) ELSE [$SOURCE_TABLE_ALIAS$].portletId END")),
				"[$SOURCE_TABLE_ALIAS$].ownerType = " +
					PortletKeys.PREFS_OWNER_TYPE_LAYOUT,
				"portletId", "PortletPreferences", "portletId", "Portlet"));

		upgrade(
			new TableOrphanReferencesDataCleanupPreupgradeProcess(
				null, null, "portletPreferencesId", "PortletPreferenceValue",
				"portletPreferencesId", "PortletPreferences"));
	}

}