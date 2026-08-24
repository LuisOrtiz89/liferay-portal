/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.portal.instances.resource.v2_0.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.headless.portal.instances.client.dto.v2_0.Admin;
import com.liferay.headless.portal.instances.client.dto.v2_0.PortalInstance;
import com.liferay.headless.portal.instances.client.dto.v2_0.PortalInstanceOperation;
import com.liferay.headless.portal.instances.client.http.HttpInvoker;
import com.liferay.headless.portal.instances.client.problem.Problem;
import com.liferay.headless.portal.instances.client.resource.v2_0.PortalInstanceResource;
import com.liferay.headless.portal.instances.client.serdes.v2_0.PortalInstanceOperationSerDes;
import com.liferay.headless.portal.instances.resource.v2_0.test.util.PortalInstanceOperationTestUtil;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.test.rule.Inject;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Luis Ortiz
 */
@RunWith(Arquillian.class)
public class PortalInstanceResourceTest
	extends BasePortalInstanceResourceTestCase {

	@Override
	@Test
	public void testPostPortalInstance() throws Exception {
		_testPostPortalInstance();
		_testPostPortalInstanceWithDuplicatePortalInstanceId();
		_testPostPortalInstanceWithInvalidAdminEmailAddress();
		_testPostPortalInstanceWithoutOmniadminPermission();
	}

	private PortalInstance _randomPortalInstance() {
		String portalInstanceId = StringUtil.toLowerCase(
			RandomTestUtil.randomString());

		PortalInstance portalInstance = new PortalInstance();

		portalInstance.setDomain(portalInstanceId + ".com");
		portalInstance.setPortalInstanceId(portalInstanceId);
		portalInstance.setVirtualHost(portalInstanceId + ".com");

		return portalInstance;
	}

	private void _testPostPortalInstance() throws Exception {
		PortalInstance portalInstance = _randomPortalInstance();

		HttpInvoker.HttpResponse httpResponse =
			portalInstanceResource.postPortalInstanceHttpResponse(
				portalInstance);

		assertHttpResponseStatusCode(202, httpResponse);

		PortalInstanceOperation portalInstanceOperation =
			PortalInstanceOperationSerDes.toDTO(httpResponse.getContent());

		Assert.assertNotNull(portalInstanceOperation.getBackgroundTaskId());
		Assert.assertEquals(
			PortalInstanceOperation.OperationType.ADD,
			portalInstanceOperation.getOperationType());

		portalInstanceOperation =
			PortalInstanceOperationTestUtil.waitForCompletion(
				portalInstanceOperation.getBackgroundTaskId(),
				PortalInstanceOperationTestUtil.
					getPortalInstanceOperationResource(testCompany));

		Assert.assertEquals(
			PortalInstanceOperation.Status.SUCCESSFUL,
			portalInstanceOperation.getStatus());
		Assert.assertNull(portalInstanceOperation.getErrorMessage());
		Assert.assertNotNull(portalInstanceOperation.getCompletionDate());

		_company = _companyLocalService.getCompanyByWebId(
			portalInstanceOperation.getPortalInstanceId());

		Assert.assertEquals(
			Long.valueOf(_company.getCompanyId()),
			portalInstanceOperation.getCompanyId());
	}

	private void _testPostPortalInstanceWithDuplicatePortalInstanceId()
		throws Exception {

		PortalInstance portalInstance = _randomPortalInstance();

		portalInstance.setPortalInstanceId(testCompany.getWebId());

		try {
			portalInstanceResource.postPortalInstance(portalInstance);

			Assert.fail();
		}
		catch (Problem.ProblemException problemException) {
			Problem problem = problemException.getProblem();

			Assert.assertEquals("BAD_REQUEST", problem.getStatus());
		}
	}

	private void _testPostPortalInstanceWithInvalidAdminEmailAddress()
		throws Exception {

		PortalInstance portalInstance = _randomPortalInstance();

		Admin admin = new Admin();

		admin.setEmailAddress(RandomTestUtil.randomString());
		admin.setFamilyName(RandomTestUtil.randomString());
		admin.setGivenName(RandomTestUtil.randomString());

		portalInstance.setAdmin(admin);

		try {
			portalInstanceResource.postPortalInstance(portalInstance);

			Assert.fail();
		}
		catch (Problem.ProblemException problemException) {
			Problem problem = problemException.getProblem();

			Assert.assertEquals("BAD_REQUEST", problem.getStatus());
		}
	}

	private void _testPostPortalInstanceWithoutOmniadminPermission()
		throws Exception {

		User user = UserTestUtil.addUser(testCompany, "test");

		PortalInstanceResource userPortalInstanceResource =
			PortalInstanceResource.builder(
			).authentication(
				user.getEmailAddress(), "test"
			).endpoint(
				testCompany.getVirtualHostname(),
				PortalUtil.getPortalServerPort(false), "http"
			).locale(
				LocaleUtil.getDefault()
			).build();

		try {
			userPortalInstanceResource.postPortalInstance(
				_randomPortalInstance());

			Assert.fail();
		}
		catch (Problem.ProblemException problemException) {
			Problem problem = problemException.getProblem();

			Assert.assertEquals("FORBIDDEN", problem.getStatus());
		}
	}

	@DeleteAfterTestRun
	private Company _company;

	@Inject
	private CompanyLocalService _companyLocalService;

}