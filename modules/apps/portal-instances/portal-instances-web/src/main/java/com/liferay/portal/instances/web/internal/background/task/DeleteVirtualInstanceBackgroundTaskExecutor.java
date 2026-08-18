/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.instances.web.internal.background.task;

import com.liferay.portal.instances.web.internal.constants.PortalInstancesBackgroundTaskConstants;
import com.liferay.portal.instances.web.internal.constants.PortalInstancesPortletKeys;
import com.liferay.portal.instances.web.internal.notifications.PortalInstancesNotificationPayload;
import com.liferay.portal.instances.web.internal.notifications.PortalInstancesOperationType;
import com.liferay.portal.kernel.backgroundtask.BackgroundTask;
import com.liferay.portal.kernel.backgroundtask.BackgroundTaskExecutor;
import com.liferay.portal.kernel.backgroundtask.BackgroundTaskResult;
import com.liferay.portal.kernel.backgroundtask.BaseBackgroundTaskExecutor;
import com.liferay.portal.kernel.backgroundtask.constants.BackgroundTaskConstants;
import com.liferay.portal.kernel.backgroundtask.display.BackgroundTaskDisplay;
import com.liferay.portal.kernel.exception.RequiredCompanyException;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.UserNotificationDeliveryConstants;
import com.liferay.portal.kernel.security.auth.PrincipalException;
import com.liferay.portal.kernel.service.CompanyService;
import com.liferay.portal.kernel.service.UserNotificationEventLocalService;
import com.liferay.portal.kernel.util.GetterUtil;

import java.io.Serializable;

import java.util.Map;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Luis Ortiz
 */
@Component(
	property = "background.task.executor.class.name=com.liferay.portal.instances.web.internal.background.task.DeleteVirtualInstanceBackgroundTaskExecutor",
	service = BackgroundTaskExecutor.class
)
public class DeleteVirtualInstanceBackgroundTaskExecutor
	extends BaseBackgroundTaskExecutor {

	public DeleteVirtualInstanceBackgroundTaskExecutor() {
		setIsolationLevel(BackgroundTaskConstants.ISOLATION_LEVEL_TASK_NAME);
	}

	@Override
	public BackgroundTaskExecutor clone() {
		return this;
	}

	@Override
	public BackgroundTaskResult execute(BackgroundTask backgroundTask)
		throws Exception {

		Map<String, Serializable> taskContextMap =
			backgroundTask.getTaskContextMap();

		long companyId = GetterUtil.getLong(
			taskContextMap.get(
				PortalInstancesBackgroundTaskConstants.COMPANY_ID));

		_companyService.deleteCompany(companyId);

		try {
			_sendUserNotificationEvent(
				backgroundTask.getUserId(),
				PortalInstancesNotificationPayload.build(
					companyId, null, PortalInstancesOperationType.DELETE, null,
					BackgroundTaskConstants.STATUS_SUCCESSFUL,
					GetterUtil.getString(
						taskContextMap.get(
							PortalInstancesBackgroundTaskConstants.WEB_ID))));
		}
		catch (Exception exception) {
			_log.error(
				"Unable to send the virtual instance success notification",
				exception);
		}

		JSONObject statusMessageJSONObject = JSONUtil.put(
			PortalInstancesNotificationPayload.COMPANY_ID, companyId);

		return new BackgroundTaskResult(
			BackgroundTaskConstants.STATUS_SUCCESSFUL,
			statusMessageJSONObject.toString());
	}

	@Override
	public BackgroundTaskDisplay getBackgroundTaskDisplay(
		BackgroundTask backgroundTask) {

		return null;
	}

	@Override
	public String handleException(
		BackgroundTask backgroundTask, Exception exception1) {

		Map<String, Serializable> taskContextMap =
			backgroundTask.getTaskContextMap();

		try {
			_sendUserNotificationEvent(
				backgroundTask.getUserId(),
				PortalInstancesNotificationPayload.build(
					GetterUtil.getLong(
						taskContextMap.get(
							PortalInstancesBackgroundTaskConstants.COMPANY_ID)),
					_getErrorMessageKey(exception1),
					PortalInstancesOperationType.DELETE, null,
					BackgroundTaskConstants.STATUS_FAILED,
					GetterUtil.getString(
						taskContextMap.get(
							PortalInstancesBackgroundTaskConstants.WEB_ID))));
		}
		catch (Exception exception2) {
			_log.error(
				"Unable to send the virtual instance failure notification",
				exception2);
		}

		return super.handleException(backgroundTask, exception1);
	}

	private String _getErrorMessageKey(Exception exception) {
		Throwable throwable = exception;

		while (throwable != null) {
			if (throwable instanceof PrincipalException) {
				return "you-must-be-an-admin-to-complete-this-action";
			}
			else if (throwable instanceof RequiredCompanyException) {
				return "the-default-company-is-required";
			}

			throwable = throwable.getCause();
		}

		return "an-unexpected-error-occurred";
	}

	private void _sendUserNotificationEvent(
			long userId, JSONObject payloadJSONObject)
		throws Exception {

		_userNotificationEventLocalService.sendUserNotificationEvents(
			userId, PortalInstancesPortletKeys.PORTAL_INSTANCES,
			UserNotificationDeliveryConstants.TYPE_WEBSITE, payloadJSONObject);
	}

	private static final Log _log = LogFactoryUtil.getLog(
		DeleteVirtualInstanceBackgroundTaskExecutor.class);

	@Reference
	private CompanyService _companyService;

	@Reference
	private UserNotificationEventLocalService
		_userNotificationEventLocalService;

}