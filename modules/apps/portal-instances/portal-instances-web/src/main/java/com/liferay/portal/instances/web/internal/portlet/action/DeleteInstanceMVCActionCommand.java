/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.instances.web.internal.portlet.action;

import com.liferay.portal.instances.web.internal.background.task.DeleteVirtualInstanceBackgroundTaskExecutor;
import com.liferay.portal.instances.web.internal.constants.PortalInstancesBackgroundTaskConstants;
import com.liferay.portal.instances.web.internal.constants.PortalInstancesPortletKeys;
import com.liferay.portal.instances.web.internal.exception.VirtualInstanceAlreadyBeingDeletedException;
import com.liferay.portal.instances.web.internal.notifications.PortalInstancesOperationType;
import com.liferay.portal.kernel.backgroundtask.BackgroundTaskManager;
import com.liferay.portal.kernel.backgroundtask.constants.BackgroundTaskConstants;
import com.liferay.portal.kernel.exception.RequiredCompanyException;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.portlet.JSONPortletResponseUtil;
import com.liferay.portal.kernel.portlet.bridges.mvc.BaseMVCActionCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCActionCommand;
import com.liferay.portal.kernel.security.auth.PrincipalException;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.HtmlUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.WebKeys;

import jakarta.portlet.ActionRequest;
import jakarta.portlet.ActionResponse;

import java.io.Serializable;

import java.util.Locale;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Víctor Galán Grande
 */
@Component(
	property = {
		"jakarta.portlet.name=" + PortalInstancesPortletKeys.PORTAL_INSTANCES,
		"mvc.command.name=/portal_instances/delete_instance"
	},
	service = MVCActionCommand.class
)
public class DeleteInstanceMVCActionCommand extends BaseMVCActionCommand {

	@Override
	protected void doProcessAction(
			ActionRequest actionRequest, ActionResponse actionResponse)
		throws Exception {

		JSONObject jsonObject = _jsonFactory.createJSONObject();

		Locale locale = actionRequest.getLocale();

		hideDefaultSuccessMessage(actionRequest);

		try {
			String webId = _addBackgroundTask(actionRequest);

			jsonObject.put(
				"successMessage",
				_language.format(
					locale, "the-virtual-instance-x-is-being-deleted",
					HtmlUtil.escape(webId), false));
		}
		catch (Exception exception) {
			if (_log.isDebugEnabled()) {
				_log.debug(exception);
			}

			jsonObject.put(
				"error", _language.get(locale, _getErrorMessageKey(exception)));
		}

		JSONPortletResponseUtil.writeJSON(
			actionRequest, actionResponse, jsonObject);
	}

	private String _addBackgroundTask(ActionRequest actionRequest)
		throws Exception {

		ThemeDisplay themeDisplay = (ThemeDisplay)actionRequest.getAttribute(
			WebKeys.THEME_DISPLAY);

		PermissionChecker permissionChecker =
			themeDisplay.getPermissionChecker();

		if (!permissionChecker.isOmniadmin()) {
			throw new PrincipalException.MustBeOmniadmin(permissionChecker);
		}

		long companyId = ParamUtil.getLong(actionRequest, "companyId");

		Company company = _companyLocalService.getCompany(companyId);

		String name = PortalInstancesOperationType.DELETE.getBackgroundTaskName(
			String.valueOf(companyId));

		String taskExecutorClassName =
			DeleteVirtualInstanceBackgroundTaskExecutor.class.getName();

		int count = _backgroundTaskManager.getBackgroundTasksCount(
			BackgroundTaskConstants.GROUP_ID_DEFAULT, name,
			taskExecutorClassName, false);

		if (count > 0) {
			throw new VirtualInstanceAlreadyBeingDeletedException(
				"Virtual instance " + companyId + " is already being deleted");
		}

		String webId = company.getWebId();

		_backgroundTaskManager.addBackgroundTask(
			themeDisplay.getUserId(), BackgroundTaskConstants.GROUP_ID_DEFAULT,
			name, taskExecutorClassName,
			HashMapBuilder.<String, Serializable>put(
				PortalInstancesBackgroundTaskConstants.COMPANY_ID, companyId
			).put(
				PortalInstancesBackgroundTaskConstants.WEB_ID, webId
			).build(),
			new ServiceContext());

		return webId;
	}

	private String _getErrorMessageKey(Exception exception) {
		if (exception instanceof PrincipalException.MustBeOmniadmin) {
			return "you-must-be-an-admin-to-complete-this-action";
		}
		else if (exception instanceof RequiredCompanyException) {
			return "the-default-company-is-required";
		}
		else if (exception instanceof
					VirtualInstanceAlreadyBeingDeletedException) {

			return "this-virtual-instance-is-already-being-deleted";
		}

		return "an-unexpected-error-occurred";
	}

	private static final Log _log = LogFactoryUtil.getLog(
		DeleteInstanceMVCActionCommand.class);

	@Reference
	private BackgroundTaskManager _backgroundTaskManager;

	@Reference
	private CompanyLocalService _companyLocalService;

	@Reference
	private JSONFactory _jsonFactory;

	@Reference
	private Language _language;

}