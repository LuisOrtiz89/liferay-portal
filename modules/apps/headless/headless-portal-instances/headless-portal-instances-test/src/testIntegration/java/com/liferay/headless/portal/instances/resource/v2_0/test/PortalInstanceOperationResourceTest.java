/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.portal.instances.resource.v2_0.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.headless.portal.instances.client.dto.v2_0.PortalInstanceOperation;
import com.liferay.headless.portal.instances.client.problem.Problem;
import com.liferay.headless.portal.instances.client.resource.v2_0.PortalInstanceOperationResource;
import com.liferay.headless.portal.instances.resource.v2_0.test.util.PortalInstanceOperationTestUtil;
import com.liferay.portal.instances.background.task.PortalInstancesOperationType;
import com.liferay.portal.instances.background.task.constants.PortalInstancesBackgroundTaskConstants;
import com.liferay.portal.kernel.backgroundtask.BackgroundTask;
import com.liferay.portal.kernel.backgroundtask.BackgroundTaskManager;
import com.liferay.portal.kernel.backgroundtask.constants.BackgroundTaskConstants;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;

import java.io.Serializable;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Luis Ortiz
 */
@RunWith(Arquillian.class)
public class PortalInstanceOperationResourceTest
	extends BasePortalInstanceOperationResourceTestCase {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			PermissionCheckerMethodTestRule.INSTANCE);

	@Override
	@Test
	public void testGetPortalInstanceOperation() throws Exception {
		long backgroundTaskId = _testGetPortalInstanceOperationWhenFailed();

		_testGetPortalInstanceOperationWithUnrelatedBackgroundTask();
		_testGetPortalInstanceOperationWithNonexistentBackgroundTask();
		_testGetPortalInstanceOperationWithoutOmniadminPermission(
			backgroundTaskId);
	}

	private BackgroundTask _addBackgroundTask(
			String defaultAdminEmailAddress, String webId)
		throws Exception {

		Map<String, Serializable> taskContextMap =
			HashMapBuilder.<String, Serializable>put(
				PortalInstancesBackgroundTaskConstants.ACTIVE, true
			).put(
				PortalInstancesBackgroundTaskConstants.
					DEFAULT_ADMIN_EMAIL_ADDRESS,
				defaultAdminEmailAddress
			).put(
				PortalInstancesBackgroundTaskConstants.MAX_USERS, 0
			).put(
				PortalInstancesBackgroundTaskConstants.MX, webId + ".com"
			).put(
				PortalInstancesBackgroundTaskConstants.VIRTUAL_HOSTNAME,
				webId + ".com"
			).put(
				PortalInstancesBackgroundTaskConstants.WEB_ID, webId
			).build();

		return _backgroundTaskManager.addBackgroundTask(
			TestPropsValues.getUserId(),
			BackgroundTaskConstants.GROUP_ID_DEFAULT,
			PortalInstancesOperationType.ADD.getBackgroundTaskName(webId),
			PortalInstancesOperationType.ADD.
				getBackgroundTaskExecutorClassName(),
			taskContextMap, new ServiceContext());
	}

	private long _testGetPortalInstanceOperationWhenFailed() throws Exception {
		String defaultAdminEmailAddress = StringUtil.toLowerCase(
			RandomTestUtil.randomString());

		String webId = StringUtil.toLowerCase(RandomTestUtil.randomString());

		BackgroundTask backgroundTask = _addBackgroundTask(
			defaultAdminEmailAddress, webId);

		PortalInstanceOperation portalInstanceOperation =
			PortalInstanceOperationTestUtil.waitForCompletion(
				backgroundTask.getBackgroundTaskId(),
				portalInstanceOperationResource);

		Assert.assertEquals(
			PortalInstanceOperation.Status.FAILED,
			portalInstanceOperation.getStatus());
		Assert.assertEquals(
			PortalInstanceOperation.OperationType.ADD,
			portalInstanceOperation.getOperationType());
		Assert.assertEquals(
			webId, portalInstanceOperation.getPortalInstanceId());

		String errorMessage = portalInstanceOperation.getErrorMessage();

		Assert.assertTrue(
			"Unexpected error message " + errorMessage,
			errorMessage.contains(defaultAdminEmailAddress));

		_company = _companyLocalService.getCompanyByWebId(webId);

		return backgroundTask.getBackgroundTaskId();
	}

	private void _testGetPortalInstanceOperationWithNonexistentBackgroundTask()
		throws Exception {

		try {
			portalInstanceOperationResource.getPortalInstanceOperation(
				RandomTestUtil.randomLong());

			Assert.fail();
		}
		catch (Problem.ProblemException problemException) {
			Problem problem = problemException.getProblem();

			Assert.assertEquals("NOT_FOUND", problem.getStatus());
		}
	}

	private void _testGetPortalInstanceOperationWithoutOmniadminPermission(
			long backgroundTaskId)
		throws Exception {

		User user = UserTestUtil.addUser(testCompany, "test");

		PortalInstanceOperationResource userPortalInstanceOperationResource =
			PortalInstanceOperationResource.builder(
			).authentication(
				user.getEmailAddress(), "test"
			).endpoint(
				testCompany.getVirtualHostname(),
				PortalUtil.getPortalServerPort(false), "http"
			).locale(
				LocaleUtil.getDefault()
			).build();

		assertHttpResponseStatusCode(
			404,
			userPortalInstanceOperationResource.
				getPortalInstanceOperationHttpResponse(backgroundTaskId));
	}

	private void _testGetPortalInstanceOperationWithUnrelatedBackgroundTask()
		throws Exception {

		BackgroundTask backgroundTask =
			_backgroundTaskManager.addBackgroundTask(
				TestPropsValues.getUserId(),
				BackgroundTaskConstants.GROUP_ID_DEFAULT,
				RandomTestUtil.randomString(), RandomTestUtil.randomString(),
				new HashMap<String, Serializable>(), new ServiceContext());

		try {
			portalInstanceOperationResource.getPortalInstanceOperation(
				backgroundTask.getBackgroundTaskId());

			Assert.fail();
		}
		catch (Problem.ProblemException problemException) {
			Problem problem = problemException.getProblem();

			Assert.assertEquals("NOT_FOUND", problem.getStatus());
		}
	}

	@Inject
	private BackgroundTaskManager _backgroundTaskManager;

	@DeleteAfterTestRun
	private Company _company;

	@Inject
	private CompanyLocalService _companyLocalService;

}