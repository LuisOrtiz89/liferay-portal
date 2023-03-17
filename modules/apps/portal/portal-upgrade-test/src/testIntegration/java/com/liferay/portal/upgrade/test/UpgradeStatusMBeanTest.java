/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.portal.upgrade.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.upgrade.internal.component.enabler.ComponentEnabler;
import com.liferay.portal.util.PropsValues;

import java.lang.management.ManagementFactory;

import java.util.Map;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.osgi.service.component.ComponentContext;

/**
 * @author Luis Ortiz
 */
@RunWith(Arquillian.class)
public class UpgradeStatusMBeanTest extends ComponentEnabler {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new LiferayIntegrationTestRule();

	@BeforeClass
	public static void setUpClass() {
		_originalUpgradeEnabled = ReflectionTestUtil.getFieldValue(
			PropsValues.class, "UPGRADE_DATABASE_AUTO_RUN");
	}

	@AfterClass
	public static void tearDownClass() {
		ReflectionTestUtil.setFieldValue(
			PropsValues.class, "UPGRADE_DATABASE_AUTO_RUN",
			_originalUpgradeEnabled);
	}

	@Test
	public void testUpgradesEnabled() throws Exception {
		ReflectionTestUtil.setFieldValue(
			PropsValues.class, "UPGRADE_DATABASE_AUTO_RUN", true);
		activate(_componentContext, _properties);

		MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();

		ObjectName objectName = new ObjectName(
			"com.liferay.portal.monitoring:classification=upgrade_status, " +
				"name=UpgradeStatus");

		Assert.assertTrue(mBeanServer.isRegistered(objectName));
	}

	@Test
	public void testUpgradesNotEnabled() throws Exception {
		ReflectionTestUtil.setFieldValue(
			PropsValues.class, "UPGRADE_DATABASE_AUTO_RUN", false);
		activate(_componentContext, _properties);

		MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();

		ObjectName objectName = new ObjectName(
			"com.liferay.portal.monitoring:classification=upgrade_status, " +
				"name=UpgradeStatus");

		Assert.assertFalse(mBeanServer.isRegistered(objectName));
	}

	@Override
	protected void activate(
			ComponentContext componentContext, Map<String, Object> properties)
		throws Exception {

		_componentContext = componentContext;
		_properties = properties;

		super.activate(componentContext, properties);
	}

	private static boolean _originalUpgradeEnabled;

	private ComponentContext _componentContext;
	private Map<String, Object> _properties;

}