/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.instances.web.internal.notifications;

import com.liferay.portal.instances.background.task.PortalInstancesOperationType;
import com.liferay.portal.instances.background.task.constants.PortalInstancesBackgroundTaskConstants;
import com.liferay.portal.kernel.backgroundtask.constants.BackgroundTaskConstants;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;

/**
 * @author Luis Ortiz
 */
public class PortalInstancesNotificationPayload {

	public static JSONObject build(
		long companyId, String errorMessage,
		PortalInstancesOperationType portalInstancesOperationType,
		String schemaName, int status, String webId) {

		return JSONUtil.put(
			PortalInstancesBackgroundTaskConstants.COMPANY_ID, companyId
		).put(
			PortalInstancesBackgroundTaskConstants.ERROR_MESSAGE, errorMessage
		).put(
			PortalInstancesBackgroundTaskConstants.OPERATION_TYPE,
			portalInstancesOperationType.getValue()
		).put(
			PortalInstancesBackgroundTaskConstants.SCHEMA_NAME, schemaName
		).put(
			PortalInstancesBackgroundTaskConstants.STATUS,
			BackgroundTaskConstants.getStatusLabel(status)
		).put(
			PortalInstancesBackgroundTaskConstants.WEB_ID, webId
		);
	}

}