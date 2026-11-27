/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.upgrade.internal.executor;

import com.liferay.portal.kernel.model.Release;
import com.liferay.portal.kernel.model.ReleaseConstants;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.upgrade.DummyUpgradeStep;
import com.liferay.portal.test.rule.LiferayUnitTestRule;
import com.liferay.portal.upgrade.internal.registry.UpgradeInfo;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.Mockito;

/**
 * @author Luis Ortiz
 */
public class UpgradePlanTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Test
	public void testCreate() {
		UpgradeInfo upgradeInfo1 = _createUpgradeInfo("0.0.0", "1.0.0");
		UpgradeInfo upgradeInfo2 = _createUpgradeInfo("1.0.0", "2.0.0");
		UpgradeInfo upgradeInfo3 = _createUpgradeInfo("2.0.0", "3.0.0");

		String bundleSymbolicName = RandomTestUtil.randomString();

		UpgradePlan upgradePlan = UpgradePlan.create(
			bundleSymbolicName, true,
			_createRelease("1.0.0", ReleaseConstants.STATE_UPGRADE_FAILURE),
			Arrays.asList(upgradeInfo1, upgradeInfo2, upgradeInfo3));

		Assert.assertEquals(
			bundleSymbolicName, upgradePlan.getBundleSymbolicName());
		Assert.assertEquals("1.0.0", upgradePlan.getSchemaVersion());
		Assert.assertEquals(
			Integer.valueOf(ReleaseConstants.STATE_UPGRADE_FAILURE),
			upgradePlan.getState());
		Assert.assertEquals("3.0.0", upgradePlan.getTargetSchemaVersion());
		Assert.assertEquals(
			Arrays.asList(upgradeInfo2, upgradeInfo3),
			upgradePlan.getUpgradeInfos());
		Assert.assertFalse(upgradePlan.isInitialRelease());
		Assert.assertTrue(upgradePlan.isUpdateIndexes());
	}

	@Test
	public void testCreateWithMultipleEndNodes() {
		try {
			UpgradePlan.create(
				RandomTestUtil.randomString(), true, _createRelease("1.0.0"),
				Arrays.asList(
					_createUpgradeInfo("1.0.0", "2.0.0"),
					_createUpgradeInfo("1.0.0", "3.0.0")));

			Assert.fail();
		}
		catch (IllegalStateException illegalStateException) {
			Assert.assertEquals(
				"There are 2 possible end nodes for 1.0.0",
				illegalStateException.getMessage());
		}
	}

	@Test
	public void testCreateWithUpToDateRelease() {
		UpgradePlan upgradePlan = UpgradePlan.create(
			RandomTestUtil.randomString(), true,
			_createRelease("2.0.0", ReleaseConstants.STATE_GOOD),
			Arrays.asList(
				_createUpgradeInfo("0.0.0", "1.0.0"),
				_createUpgradeInfo("1.0.0", "2.0.0")));

		Assert.assertEquals("2.0.0", upgradePlan.getSchemaVersion());
		Assert.assertEquals(
			Integer.valueOf(ReleaseConstants.STATE_GOOD),
			upgradePlan.getState());
		Assert.assertEquals("2.0.0", upgradePlan.getTargetSchemaVersion());

		List<UpgradeInfo> upgradeInfos = upgradePlan.getUpgradeInfos();

		Assert.assertTrue(upgradeInfos.toString(), upgradeInfos.isEmpty());

		Assert.assertFalse(upgradePlan.isInitialRelease());
		Assert.assertFalse(upgradePlan.isUpdateIndexes());
	}

	@Test
	public void testCreateWithoutRelease() {
		UpgradeInfo upgradeInfo1 = new UpgradeInfo(
			"0.0.0", "2.0.0", true, new DummyUpgradeStep());

		UpgradePlan upgradePlan = UpgradePlan.create(
			RandomTestUtil.randomString(), true, null,
			Arrays.asList(upgradeInfo1, _createUpgradeInfo("1.0.0", "2.0.0")));

		Assert.assertNull(upgradePlan.getSchemaVersion());
		Assert.assertNull(upgradePlan.getState());
		Assert.assertEquals("2.0.0", upgradePlan.getTargetSchemaVersion());
		Assert.assertEquals(
			Collections.singletonList(upgradeInfo1),
			upgradePlan.getUpgradeInfos());
		Assert.assertTrue(upgradePlan.isInitialRelease());
		Assert.assertFalse(upgradePlan.isUpdateIndexes());
	}

	@Test
	public void testCreateWithoutUpgradeInfos() {
		UpgradePlan upgradePlan = UpgradePlan.create(
			RandomTestUtil.randomString(), true, null, null);

		Assert.assertNull(upgradePlan.getSchemaVersion());
		Assert.assertNull(upgradePlan.getTargetSchemaVersion());

		List<UpgradeInfo> upgradeInfos = upgradePlan.getUpgradeInfos();

		Assert.assertTrue(upgradeInfos.toString(), upgradeInfos.isEmpty());
	}

	@Test
	public void testIsUpdateIndexes() {
		Assert.assertFalse(
			_isUpdateIndexes(false, _createUpgradeInfo("1.0.0", "2.0.0")));
		Assert.assertFalse(
			_isUpdateIndexes(true, _createUpgradeInfo("0.0.0", "1.0.0")));
		Assert.assertTrue(
			_isUpdateIndexes(
				true, _createUpgradeInfo("0.0.0", "1.0.0"),
				_createUpgradeInfo("1.0.0", "2.0.0")));
		Assert.assertTrue(
			_isUpdateIndexes(true, _createUpgradeInfo("1.0.0", "2.0.0")));
	}

	private Release _createRelease(String schemaVersion) {
		Release release = Mockito.mock(Release.class);

		Mockito.when(
			release.getSchemaVersion()
		).thenReturn(
			schemaVersion
		);

		return release;
	}

	private Release _createRelease(String schemaVersion, int state) {
		Release release = _createRelease(schemaVersion);

		Mockito.when(
			release.getState()
		).thenReturn(
			state
		);

		return release;
	}

	private UpgradeInfo _createUpgradeInfo(
		String fromSchemaVersionString, String toSchemaVersionString) {

		return new UpgradeInfo(
			fromSchemaVersionString, toSchemaVersionString,
			new DummyUpgradeStep());
	}

	private boolean _isUpdateIndexes(
		boolean schemaBundle, UpgradeInfo... upgradeInfos) {

		UpgradePlan upgradePlan = new UpgradePlan(
			RandomTestUtil.randomString(), schemaBundle, null,
			Arrays.asList(upgradeInfos));

		return upgradePlan.isUpdateIndexes();
	}

}