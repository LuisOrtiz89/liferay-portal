/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.upgrade.internal.registry;

import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.upgrade.UpgradeStep;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Manuel de la Peña
 */
public class UpgradeInfoTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Test
	public void testEqualsReturnsFalseComparingDifferentClass() {
		UpgradeStep upgradeStep = new TestUpgradeStep();

		UpgradeInfo upgradeInfo = new UpgradeInfo(
			"1.0.0", "2.0.0", upgradeStep);

		Assert.assertNotEquals(upgradeInfo, "");
	}

	@Test
	public void testEqualsReturnsFalseComparingDifferentInitialization() {
		UpgradeStep upgradeStep = new TestUpgradeStep();

		UpgradeInfo upgradeInfo1 = new UpgradeInfo(
			"0.0.0", "1.0.0", true, upgradeStep);
		UpgradeInfo upgradeInfo2 = new UpgradeInfo(
			"0.0.0", "1.0.0", false, upgradeStep);

		Assert.assertNotEquals(upgradeInfo1, upgradeInfo2);
	}

	@Test
	public void testEqualsReturnsFalseComparingDifferentSchemaVersion() {
		UpgradeInfo upgradeInfo1 = new UpgradeInfo(
			"1.0.0", "2.0.0", new TestUpgradeStep());
		UpgradeInfo upgradeInfo2 = new UpgradeInfo(
			"2.0.0", "3.0.0", new TestUpgradeStep());

		Assert.assertNotEquals(upgradeInfo1, upgradeInfo2);
	}

	@Test
	public void testEqualsReturnsFalseComparingDifferentUpgradeStepOrder() {
		UpgradeStep upgradeStep1 = new TestUpgradeStep();
		UpgradeStep upgradeStep2 = new TestUpgradeStep();

		UpgradeInfo upgradeInfo1 = new UpgradeInfo(
			"1.0.0", "2.0.0", upgradeStep1, upgradeStep2);
		UpgradeInfo upgradeInfo2 = new UpgradeInfo(
			"1.0.0", "2.0.0", upgradeStep2, upgradeStep1);

		Assert.assertNotEquals(upgradeInfo1, upgradeInfo2);
	}

	@Test
	public void testEqualsReturnsFalseComparingSameSchemaVersion() {
		UpgradeInfo upgradeInfo1 = new UpgradeInfo(
			"1.0.0", "2.0.0", new TestUpgradeStep());
		UpgradeInfo upgradeInfo2 = new UpgradeInfo(
			"1.0.0", "2.0.0", new TestUpgradeStep());

		Assert.assertNotEquals(upgradeInfo1, upgradeInfo2);
	}

	@Test
	public void testEqualsReturnsTrueComparingSameInstance() {
		UpgradeInfo upgradeInfo = new UpgradeInfo(
			"1.0.0", "2.0.0", new TestUpgradeStep());

		Assert.assertEquals(upgradeInfo, upgradeInfo);
	}

	@Test
	public void testEqualsReturnsTrueComparingSameSchemaVersion() {
		UpgradeStep upgradeStep = new TestUpgradeStep();

		UpgradeInfo upgradeInfo1 = new UpgradeInfo(
			"1.0.0", "2.0.0", upgradeStep);
		UpgradeInfo upgradeInfo2 = new UpgradeInfo(
			"1.0.0", "2.0.0", upgradeStep);

		Assert.assertEquals(upgradeInfo1, upgradeInfo2);
	}

	@Test
	public void testGetUpgradeSteps() {
		UpgradeStep upgradeStep1 = new TestUpgradeStep();
		UpgradeStep upgradeStep2 = new TestUpgradeStep();

		UpgradeInfo upgradeInfo = new UpgradeInfo(
			"1.0.0", "2.0.0", upgradeStep1, upgradeStep2);

		Assert.assertEquals(
			Arrays.asList(upgradeStep1, upgradeStep2),
			upgradeInfo.getUpgradeSteps());
		Assert.assertFalse(upgradeInfo.isInitialization());
	}

	@Test
	public void testToString() {
		UpgradeStep upgradeStep1 = new TestUpgradeStep();

		UpgradeInfo upgradeInfo = new UpgradeInfo(
			"1.0.0", "2.0.0", upgradeStep1);

		Assert.assertEquals(
			StringBundler.concat(
				"{fromSchemaVersionString=1.0.0, toSchemaVersionString=2.0.0, ",
				"upgradeStep=", upgradeStep1, "}"),
			upgradeInfo.toString());

		UpgradeStep upgradeStep2 = new TestUpgradeStep();

		upgradeInfo = new UpgradeInfo(
			"1.0.0", "2.0.0", upgradeStep1, upgradeStep2);

		Assert.assertEquals(
			StringBundler.concat(
				"{fromSchemaVersionString=1.0.0, toSchemaVersionString=2.0.0, ",
				"upgradeSteps=[", upgradeStep1, ", ", upgradeStep2, "]}"),
			upgradeInfo.toString());
	}

	private static class TestUpgradeStep implements UpgradeStep {

		@Override
		public void upgrade() {
		}

	}

}