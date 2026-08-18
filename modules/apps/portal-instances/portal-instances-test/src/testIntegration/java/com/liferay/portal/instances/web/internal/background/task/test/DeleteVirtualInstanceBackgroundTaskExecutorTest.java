/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.instances.web.internal.background.task.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.portal.kernel.backgroundtask.BackgroundTask;
import com.liferay.portal.kernel.backgroundtask.BackgroundTaskManager;
import com.liferay.portal.kernel.backgroundtask.constants.BackgroundTaskConstants;
import com.liferay.portal.kernel.instance.PortalInstancePool;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.model.UserNotificationDeliveryConstants;
import com.liferay.portal.kernel.model.UserNotificationEvent;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.UserNotificationEventLocalService;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Luis Ortiz
 */
@RunWith(Arquillian.class)
public class DeleteVirtualInstanceBackgroundTaskExecutorTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			PermissionCheckerMethodTestRule.INSTANCE);

	@After
	public void tearDown() throws Exception {
		for (UserNotificationEvent userNotificationEvent :
				_userNotificationEvents) {

			_userNotificationEventLocalService.deleteUserNotificationEvent(
				userNotificationEvent);
		}
	}

	@Test
	public void testExecute() throws Exception {
		String virtualHostname = _WEB_ID + ".com";

		Company company = _companyLocalService.addCompany(
			null, _WEB_ID, virtualHostname, virtualHostname, 0, true, true,
			null, null, null, null, null, null);

		long companyId = company.getCompanyId();

		BackgroundTask backgroundTask = _addBackgroundTask(companyId, _WEB_ID);

		Assert.assertEquals(
			BackgroundTaskConstants.STATUS_SUCCESSFUL,
			backgroundTask.getStatus());

		Assert.assertNull(_companyLocalService.fetchCompany(companyId));

		JSONObject payloadJSONObject = _getPayloadJSONObject(_WEB_ID);

		Assert.assertEquals(companyId, payloadJSONObject.getLong("companyId"));
		Assert.assertEquals(
			BackgroundTaskConstants.LABEL_SUCCESSFUL,
			payloadJSONObject.getString("status"));

		JSONObject statusMessageJSONObject = _jsonFactory.createJSONObject(
			backgroundTask.getStatusMessage());

		Assert.assertEquals(
			companyId, statusMessageJSONObject.getLong("companyId"));
	}

	@Test
	public void testExecuteWhenCompanyIsDefault() throws Exception {
		Company company = _companyLocalService.getCompany(
			PortalInstancePool.getDefaultCompanyId());

		long companyId = company.getCompanyId();
		String webId = company.getWebId();

		BackgroundTask backgroundTask = _addBackgroundTask(companyId, webId);

		Assert.assertEquals(
			BackgroundTaskConstants.STATUS_FAILED, backgroundTask.getStatus());

		Assert.assertNotNull(_companyLocalService.fetchCompany(companyId));

		JSONObject payloadJSONObject = _getPayloadJSONObject(webId);

		Assert.assertEquals(
			"the-default-company-is-required",
			payloadJSONObject.getString("errorMessage"));
		Assert.assertEquals(
			BackgroundTaskConstants.LABEL_FAILED,
			payloadJSONObject.getString("status"));
	}

	private BackgroundTask _addBackgroundTask(long companyId, String webId)
		throws Exception {

		Map<String, Serializable> taskContextMap =
			HashMapBuilder.<String, Serializable>put(
				"companyId", companyId
			).put(
				"webId", webId
			).build();

		BackgroundTask backgroundTask =
			_backgroundTaskManager.addBackgroundTask(
				TestPropsValues.getUserId(),
				BackgroundTaskConstants.GROUP_ID_DEFAULT,
				"DeleteVirtualInstance#" + companyId,
				"com.liferay.portal.instances.web.internal.background.task." +
					"DeleteVirtualInstanceBackgroundTaskExecutor",
				taskContextMap, new ServiceContext());

		return _waitForCompletion(backgroundTask.getBackgroundTaskId());
	}

	private JSONObject _getPayloadJSONObject(String webId) throws Exception {
		List<UserNotificationEvent> userNotificationEvents =
			_userNotificationEventLocalService.getUserNotificationEvents(
				TestPropsValues.getUserId(),
				UserNotificationDeliveryConstants.TYPE_WEBSITE);

		for (UserNotificationEvent userNotificationEvent :
				userNotificationEvents) {

			String type = userNotificationEvent.getType();

			if (!type.equals(
					"com_liferay_portal_instances_web_portlet_" +
						"PortalInstancesPortlet")) {

				continue;
			}

			JSONObject payloadJSONObject = _jsonFactory.createJSONObject(
				userNotificationEvent.getPayload());

			String operationType = payloadJSONObject.getString("operationType");

			if (operationType.equals("delete") &&
				webId.equals(payloadJSONObject.getString("webId"))) {

				_userNotificationEvents.add(userNotificationEvent);

				return payloadJSONObject;
			}
		}

		throw new AssertionError(
			"No user notification event was sent for web ID " + webId);
	}

	private BackgroundTask _waitForCompletion(long backgroundTaskId)
		throws Exception {

		long endTime = System.currentTimeMillis() + 600000;

		while (System.currentTimeMillis() < endTime) {
			BackgroundTask backgroundTask =
				_backgroundTaskManager.fetchBackgroundTask(backgroundTaskId);

			if ((backgroundTask != null) && backgroundTask.isCompleted()) {
				return backgroundTask;
			}

			Thread.sleep(500);
		}

		throw new AssertionError(
			"Background task " + backgroundTaskId + " did not complete");
	}

	private static final String _WEB_ID = StringUtil.toLowerCase(
		RandomTestUtil.randomString());

	@Inject
	private BackgroundTaskManager _backgroundTaskManager;

	@Inject
	private CompanyLocalService _companyLocalService;

	@Inject
	private JSONFactory _jsonFactory;

	@Inject
	private UserNotificationEventLocalService
		_userNotificationEventLocalService;

	private final List<UserNotificationEvent> _userNotificationEvents =
		new ArrayList<>();

}