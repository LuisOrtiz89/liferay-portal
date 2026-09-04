/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.portal.instances.internal.resource.v2_0;

import com.liferay.headless.portal.instances.dto.v2_0.Admin;
import com.liferay.headless.portal.instances.dto.v2_0.PortalInstance;
import com.liferay.headless.portal.instances.dto.v2_0.PortalInstanceOperation;
import com.liferay.headless.portal.instances.internal.dto.v2_0.util.PortalInstanceOperationUtil;
import com.liferay.headless.portal.instances.resource.v2_0.PortalInstanceResource;
import com.liferay.portal.instances.background.task.PortalInstanceOperationType;
import com.liferay.portal.kernel.backgroundtask.BackgroundTask;
import com.liferay.portal.kernel.backgroundtask.BackgroundTaskManager;
import com.liferay.portal.kernel.exception.CompanyAlreadyBeingAddedException;
import com.liferay.portal.kernel.exception.UserEmailAddressException;
import com.liferay.portal.kernel.exception.UserScreenNameException;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.security.auth.EmailAddressValidator;
import com.liferay.portal.kernel.security.auth.PrincipalException;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.security.permission.PermissionThreadLocal;
import com.liferay.portal.kernel.service.CompanyService;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.security.auth.EmailAddressValidatorFactory;
import com.liferay.portal.vulcan.status.Status;

import jakarta.ws.rs.ClientErrorException;
import jakarta.ws.rs.core.Response;

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

		_checkPermission();

		Admin admin = portalInstance.getAdmin();

		if (admin != null) {
			_validateAdmin(admin);
		}

		return PortalInstanceOperationUtil.toPortalInstanceOperation(
			_addPortalInstance(admin, portalInstance), _jsonFactory, _language,
			contextAcceptLanguage.getPreferredLocale(),
			PortalInstanceOperationType.ADD);
	}

	private BackgroundTask _addPortalInstance(
			Admin admin, PortalInstance portalInstance)
		throws Exception {

		String defaultAdminEmailAddress = null;
		String defaultAdminFirstName = null;
		String defaultAdminLastName = null;

		if (admin != null) {
			defaultAdminEmailAddress = admin.getEmailAddress();
			defaultAdminFirstName = admin.getGivenName();
			defaultAdminLastName = admin.getFamilyName();
		}

		try {
			return _backgroundTaskManager.getBackgroundTask(
				_companyService.addCompanyInBackground(
					portalInstance.getPortalInstanceId(),
					portalInstance.getVirtualHost(), portalInstance.getDomain(),
					_MAX_USERS, true, null, null, defaultAdminEmailAddress,
					defaultAdminFirstName, null, defaultAdminLastName,
					portalInstance.getSiteInitializerKey()));
		}
		catch (CompanyAlreadyBeingAddedException
					companyAlreadyBeingAddedException) {

			throw new ClientErrorException(
				companyAlreadyBeingAddedException.getMessage(),
				Response.Status.CONFLICT, companyAlreadyBeingAddedException);
		}
	}

	private void _checkPermission() throws Exception {
		PermissionChecker permissionChecker =
			PermissionThreadLocal.getPermissionChecker();

		if (!permissionChecker.isOmniadmin()) {
			throw new PrincipalException.MustBeOmniadmin(permissionChecker);
		}
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
	private CompanyService _companyService;

	@Reference
	private JSONFactory _jsonFactory;

	@Reference
	private Language _language;

}