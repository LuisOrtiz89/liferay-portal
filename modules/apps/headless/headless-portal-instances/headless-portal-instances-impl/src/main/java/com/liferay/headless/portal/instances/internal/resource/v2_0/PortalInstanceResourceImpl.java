/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.portal.instances.internal.resource.v2_0;

import com.liferay.headless.portal.instances.dto.v2_0.Admin;
import com.liferay.headless.portal.instances.dto.v2_0.PortalInstance;
import com.liferay.headless.portal.instances.dto.v2_0.PortalInstanceOperation;
import com.liferay.headless.portal.instances.internal.dto.v2_0.util.PortalInstanceOperationUtil;
import com.liferay.headless.portal.instances.internal.util.PermissionUtil;
import com.liferay.headless.portal.instances.resource.v2_0.PortalInstanceResource;
import com.liferay.portal.instances.background.task.PortalInstancesOperationType;
import com.liferay.portal.instances.background.task.constants.PortalInstancesBackgroundTaskConstants;
import com.liferay.portal.kernel.backgroundtask.BackgroundTask;
import com.liferay.portal.kernel.backgroundtask.BackgroundTaskManager;
import com.liferay.portal.kernel.backgroundtask.constants.BackgroundTaskConstants;
import com.liferay.portal.kernel.exception.UserEmailAddressException;
import com.liferay.portal.kernel.exception.UserScreenNameException;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.security.auth.EmailAddressValidator;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.security.auth.EmailAddressValidatorFactory;
import com.liferay.portal.vulcan.status.Status;

import jakarta.ws.rs.ClientErrorException;
import jakarta.ws.rs.core.Response;

import java.io.Serializable;

import java.util.Map;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * @author Luis Ortiz
 */
@Component(
	properties = "OSGI-INF/liferay/rest/v2_0/portal-instance.properties",
	scope = ServiceScope.PROTOTYPE, service = PortalInstanceResource.class
)
public class PortalInstanceResourceImpl extends BasePortalInstanceResourceImpl {

	@Override
	@Status(Response.Status.ACCEPTED)
	public PortalInstanceOperation postPortalInstance(
			PortalInstance portalInstance)
		throws Exception {

		PermissionUtil.checkOmniadminPermission();

		Admin admin = portalInstance.getAdmin();

		if (admin != null) {
			_validateAdmin(admin);
		}

		String webId = portalInstance.getPortalInstanceId();

		_companyLocalService.validateCompany(
			webId, portalInstance.getVirtualHost(), portalInstance.getDomain(),
			_MAX_USERS);

		String name = PortalInstancesOperationType.ADD.getBackgroundTaskName(
			webId);

		String taskExecutorClassName =
			PortalInstancesOperationType.ADD.
				getBackgroundTaskExecutorClassName();

		int count = _backgroundTaskManager.getBackgroundTasksCount(
			BackgroundTaskConstants.GROUP_ID_DEFAULT, name,
			taskExecutorClassName, false);

		if (count > 0) {
			throw new ClientErrorException(
				"Portal instance " + webId + " is already being added",
				Response.Status.CONFLICT);
		}

		BackgroundTask backgroundTask =
			_backgroundTaskManager.addBackgroundTask(
				contextUser.getUserId(),
				BackgroundTaskConstants.GROUP_ID_DEFAULT, name,
				taskExecutorClassName, _getTaskContextMap(portalInstance),
				new ServiceContext());

		return PortalInstanceOperationUtil.toPortalInstanceOperation(
			backgroundTask, _jsonFactory, PortalInstancesOperationType.ADD);
	}

	private Map<String, Serializable> _getTaskContextMap(
		PortalInstance portalInstance) {

		String defaultAdminEmailAddress = null;
		String defaultAdminFirstName = null;
		String defaultAdminLastName = null;

		Admin admin = portalInstance.getAdmin();

		if (admin != null) {
			defaultAdminEmailAddress = admin.getEmailAddress();
			defaultAdminFirstName = admin.getGivenName();
			defaultAdminLastName = admin.getFamilyName();
		}

		return HashMapBuilder.<String, Serializable>put(
			PortalInstancesBackgroundTaskConstants.ACTIVE, true
		).put(
			PortalInstancesBackgroundTaskConstants.COMPANY_ID,
			portalInstance.getCompanyId()
		).put(
			PortalInstancesBackgroundTaskConstants.DEFAULT_ADMIN_EMAIL_ADDRESS,
			defaultAdminEmailAddress
		).put(
			PortalInstancesBackgroundTaskConstants.DEFAULT_ADMIN_FIRST_NAME,
			defaultAdminFirstName
		).put(
			PortalInstancesBackgroundTaskConstants.DEFAULT_ADMIN_LAST_NAME,
			defaultAdminLastName
		).put(
			PortalInstancesBackgroundTaskConstants.MAX_USERS, _MAX_USERS
		).put(
			PortalInstancesBackgroundTaskConstants.MX,
			portalInstance.getDomain()
		).put(
			PortalInstancesBackgroundTaskConstants.SITE_INITIALIZER_KEY,
			portalInstance.getSiteInitializerKey()
		).put(
			PortalInstancesBackgroundTaskConstants.VIRTUAL_HOSTNAME,
			portalInstance.getVirtualHost()
		).put(
			PortalInstancesBackgroundTaskConstants.WEB_ID,
			portalInstance.getPortalInstanceId()
		).build();
	}

	private void _validateAdmin(Admin admin) throws Exception {
		if (Validator.isNull(admin.getEmailAddress()) ||
			Validator.isNull(admin.getFamilyName()) ||
			Validator.isNull(admin.getGivenName())) {

			throw new UserScreenNameException.MustNotBeNull();
		}

		EmailAddressValidator emailAddressValidator =
			EmailAddressValidatorFactory.getInstance();

		if (!emailAddressValidator.validate(0, admin.getEmailAddress())) {
			throw new UserEmailAddressException.MustValidate(
				admin.getEmailAddress(), emailAddressValidator);
		}
	}

	private static final int _MAX_USERS = 0;

	@Reference
	private BackgroundTaskManager _backgroundTaskManager;

	@Reference
	private CompanyLocalService _companyLocalService;

	@Reference
	private JSONFactory _jsonFactory;

}