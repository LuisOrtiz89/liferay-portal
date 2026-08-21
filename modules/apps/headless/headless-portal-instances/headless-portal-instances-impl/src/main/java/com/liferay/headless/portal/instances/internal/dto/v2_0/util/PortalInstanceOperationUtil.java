/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.portal.instances.internal.dto.v2_0.util;

import com.liferay.headless.portal.instances.dto.v2_0.PortalInstanceOperation;
import com.liferay.portal.instances.background.task.PortalInstancesOperationType;
import com.liferay.portal.instances.background.task.constants.PortalInstancesBackgroundTaskConstants;
import com.liferay.portal.kernel.backgroundtask.BackgroundTask;
import com.liferay.portal.kernel.backgroundtask.constants.BackgroundTaskConstants;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.StringUtil;

import java.io.Serializable;

import java.util.Map;

/**
 * @author Luis Ortiz
 */
public class PortalInstanceOperationUtil {

	public static PortalInstanceOperation toPortalInstanceOperation(
		BackgroundTask backgroundTask, JSONFactory jsonFactory,
		PortalInstancesOperationType portalInstancesOperationType) {

		Map<String, Serializable> taskContextMap =
			backgroundTask.getTaskContextMap();

		JSONObject statusMessageJSONObject = jsonFactory.safeCreateJSONObject(
			backgroundTask.getStatusMessage(), true);

		PortalInstanceOperation portalInstanceOperation =
			new PortalInstanceOperation();

		portalInstanceOperation.setBackgroundTaskId(
			backgroundTask::getBackgroundTaskId);
		portalInstanceOperation.setCompanyId(
			() -> _getCompanyId(statusMessageJSONObject, taskContextMap));
		portalInstanceOperation.setCompletionDate(
			backgroundTask::getCompletionDate);
		portalInstanceOperation.setCreateDate(backgroundTask::getCreateDate);
		portalInstanceOperation.setErrorMessage(
			() -> GetterUtil.getString(
				taskContextMap.get(
					PortalInstancesBackgroundTaskConstants.ERROR_MESSAGE),
				null));
		portalInstanceOperation.setOperationType(
			() -> PortalInstanceOperation.OperationType.create(
				StringUtil.toUpperCase(
					portalInstancesOperationType.getValue())));
		portalInstanceOperation.setPortalInstanceId(
			() -> GetterUtil.getString(
				taskContextMap.get(
					PortalInstancesBackgroundTaskConstants.WEB_ID),
				null));
		portalInstanceOperation.setSchemaName(
			() -> _getSchemaName(statusMessageJSONObject, taskContextMap));
		portalInstanceOperation.setStatus(
			() -> _getStatus(backgroundTask.getStatus()));

		return portalInstanceOperation;
	}

	private static Long _getCompanyId(
		JSONObject statusMessageJSONObject,
		Map<String, Serializable> taskContextMap) {

		long companyId = 0;

		if (statusMessageJSONObject != null) {
			companyId = statusMessageJSONObject.getLong(
				PortalInstancesBackgroundTaskConstants.COMPANY_ID);
		}

		if (companyId <= 0) {
			companyId = GetterUtil.getLong(
				taskContextMap.get(
					PortalInstancesBackgroundTaskConstants.COMPANY_ID));
		}

		if (companyId <= 0) {
			return null;
		}

		return companyId;
	}

	private static String _getSchemaName(
		JSONObject statusMessageJSONObject,
		Map<String, Serializable> taskContextMap) {

		if (statusMessageJSONObject != null) {
			String schemaName = statusMessageJSONObject.getString(
				PortalInstancesBackgroundTaskConstants.SCHEMA_NAME, null);

			if (schemaName != null) {
				return schemaName;
			}
		}

		return GetterUtil.getString(
			taskContextMap.get(
				PortalInstancesBackgroundTaskConstants.SCHEMA_NAME),
			null);
	}

	private static PortalInstanceOperation.Status _getStatus(int status) {
		if (status == BackgroundTaskConstants.STATUS_CANCELLED) {
			return PortalInstanceOperation.Status.CANCELLED;
		}
		else if (status ==
					BackgroundTaskConstants.STATUS_COMPLETED_WITH_ERRORS) {

			return PortalInstanceOperation.Status.COMPLETED_WITH_ERRORS;
		}
		else if (status == BackgroundTaskConstants.STATUS_FAILED) {
			return PortalInstanceOperation.Status.FAILED;
		}
		else if (status == BackgroundTaskConstants.STATUS_IN_PROGRESS) {
			return PortalInstanceOperation.Status.IN_PROGRESS;
		}
		else if (status == BackgroundTaskConstants.STATUS_NEW) {
			return PortalInstanceOperation.Status.NEW;
		}
		else if (status == BackgroundTaskConstants.STATUS_QUEUED) {
			return PortalInstanceOperation.Status.QUEUED;
		}
		else if (status == BackgroundTaskConstants.STATUS_SUCCESSFUL) {
			return PortalInstanceOperation.Status.SUCCESSFUL;
		}

		return null;
	}

}