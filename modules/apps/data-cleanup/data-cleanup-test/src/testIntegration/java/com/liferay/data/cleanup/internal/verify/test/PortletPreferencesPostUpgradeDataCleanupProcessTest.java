/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.data.cleanup.internal.verify.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.portal.kernel.module.util.SystemBundleUtil;
import com.liferay.portal.kernel.service.PortletLocalService;
import com.liferay.portal.test.rule.Inject;

import java.sql.Connection;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.osgi.framework.Bundle;

/**
 * @author Luis Ortiz
 */
@RunWith(Arquillian.class)
public class PortletPreferencesPostUpgradeDataCleanupProcessTest
	extends BasePostUpgradeDataCleanupProcessTestCase {

	@Test
	public void testNonliferayPortletIsNotDeleted() {
	}

	// PortletPreferenceValue linked to not found PortletPreference

	// PortletPreference linked to plid (Layout) not found and LayoutRevisionId (LayoutRevision) neither

	// PortletPreference linked to plid (Layout) not found and LayoutRevisionId (LayoutRevision) yes

	// PortletPreference linked to Portlet not existent, ownerType 3

	// PortletPreference linked to Portlet existent, ownerType 3

	// PortletPreference linked to Portlet existent with _USER_, ownerType 3

	// PortletPreference linked to Portlet existent with _INSTANCE_, ownerType 3

	public void testNotFoundLiferayPortletIsDeleted() {
	}

	@Test
	public void testVerifyDoesNotRunIfModulesNotStarted() throws Exception {
		AtomicReference<Bundle> bundleAtomicReference = new AtomicReference<>();

		test(
			logCapture -> {
				List<String> messages = logCapture.getMessages();

				Assert.assertTrue(
					messages.toString(),
					messages.contains(
						"PortletPreferencesPostUpgradeDataCleanupProcess " +
							"cannot be executed because there are modules with " +
								"unsatisfied references"));
			},
			() -> {
				Bundle bundle = bundleAtomicReference.get();

				if (bundle != null) {
					installBundle(bundle, SystemBundleUtil.getBundleContext());
				}
			},
			() -> bundleAtomicReference.set(
				uninstallBundle(
					SystemBundleUtil.getBundleContext(),
					"com.liferay.dynamic.data.mapping.service")));
	}

	@Override
	protected Object[] getPostUpgradeDataCleanupProcessArguments() {
		return new Object[] {connection, _portletLocalService};
	}

	@Override
	protected Class<?>[] getPostUpgradeDataCleanupProcessArgumentTypes() {
		return new Class<?>[] {Connection.class, PortletLocalService.class};
	}

	@Override
	protected String getPostUpgradeDataCleanupProcessClassName() {
		return "com.liferay.data.cleanup.internal.verify." +
			"PortletPreferencesPostUpgradeDataCleanupProcess";
	}

	@Inject
	private PortletLocalService _portletLocalService;

}