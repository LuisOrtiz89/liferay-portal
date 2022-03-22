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
import com.liferay.petra.function.UnsafeConsumer;
import com.liferay.portal.dao.orm.common.SQLTransformer;
import com.liferay.portal.kernel.dao.db.DBInspector;
import com.liferay.portal.kernel.dao.jdbc.DataAccess;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.portal.kernel.util.Time;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.tools.DBUpgrader;
import com.liferay.portal.util.PropsValues;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

import org.springframework.context.ApplicationContext;

/**
 * @author Luis Ortiz
 */
@RunWith(Arquillian.class)
public class UpgradeManagedStartupTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new LiferayIntegrationTestRule();

	@BeforeClass
	public static void setUpClass() throws Exception {
		_bundle = FrameworkUtil.getBundle(UpgradeManagedStartupTest.class);
	}

	@Before
	public void setUp() {
		ReflectionTestUtil.setFieldValue(
			PropsValues.class, "UPGRADE_DATABASE_LOCK_REFRESH_TIME",
			_REFRESH_TIME);
	}

	@After
	public void tearDown() throws IllegalAccessException, SQLException {
		ReflectionTestUtil.setFieldValue(
			PropsValues.class, "UPGRADE_DATABASE_MANAGED_STARTUP",
			_ORIGINAL_UPGRADE_DATABASE_MANAGED_STARTUP);

		ReflectionTestUtil.setFieldValue(
			PropsValues.class, "UPGRADE_DATABASE_LOCK_REFRESH_TIME",
			_ORIGINAL_UPGRADE_DATABASE_LOCK_REFRESH_TIME);
	}

	@Test(timeout = 60000)
	public void testAcquireLocks() throws Exception {
		UpgradeThread upgradeThread1 = new UpgradeThread();

		CountDownLatch executingThread1 = upgradeThread1.getExecuting();
		CountDownLatch executedThread1 = upgradeThread1.getExecuted();

		UpgradeThread upgradeThread2 = new UpgradeThread();

		CountDownLatch executingThread2 = upgradeThread2.getExecuting();
		CountDownLatch executedThread2 = upgradeThread2.getExecuted();

		Thread thread1 = new Thread(upgradeThread1);
		Thread thread2 = new Thread(upgradeThread2);

		try {
			Assert.assertFalse(_hasLockTable());

			thread1.start();

			executingThread1.await(10, TimeUnit.SECONDS);

			Assert.assertTrue(thread1.isAlive());

			Assert.assertTrue(_hasLockTable());

			Assert.assertEquals(0, executingThread1.getCount());

			Assert.assertEquals(1, executedThread1.getCount());

			thread2.start();

			Assert.assertEquals(1, executingThread2.getCount());

			upgradeThread1.stopThread();

			executedThread1.await(10, TimeUnit.SECONDS);

			Assert.assertEquals(0, executedThread1.getCount());

			thread1.join(5000);

			Assert.assertFalse(thread1.isAlive());

			Assert.assertTrue(thread2.isAlive());

			executingThread2.await(10, TimeUnit.SECONDS);

			Assert.assertEquals(0, executingThread2.getCount());

			Assert.assertEquals(1, executedThread2.getCount());

			upgradeThread2.stopThread();

			executedThread2.await(10, TimeUnit.SECONDS);

			Assert.assertEquals(0, executedThread2.getCount());

			thread2.join(5000);

			Assert.assertFalse(thread2.isAlive());

			Assert.assertFalse(_hasLockTable());
		}
		finally {
			upgradeThread1.stopThread();
			upgradeThread2.stopThread();

			if (thread1.isAlive()) {
				thread1.interrupt();
			}

			if (thread2.isAlive()) {
				thread2.interrupt();
			}

			if (_hasLockTable()) {
				_dropLockTable();
			}
		}
	}

	@Test(timeout = 10000)
	public void testCreateLockTable() throws Exception {
		Class<?> clazz = _bundle.loadClass(DBUpgrader.class.getName());

		ReflectionTestUtil.invoke(clazz, "_createLockTableIfNotExists", null);

		Assert.assertTrue(_hasLockTable());

		try (Connection connection = DataAccess.getConnection()) {
			DBInspector dbInspector = new DBInspector(connection);

			Assert.assertTrue(
				dbInspector.hasColumnType(
					_LOCK_TABLE_NAME, "lockId", "LONG NOT NULL"));

			Assert.assertTrue(
				dbInspector.hasColumnType(
					_LOCK_TABLE_NAME, "createDate", "DATE DEFAULT NULL"));

			Assert.assertTrue(
				dbInspector.hasColumnType(
					_LOCK_TABLE_NAME, "className", "VARCHAR(75) DEFAULT NULL"));

			Assert.assertTrue(
				dbInspector.hasColumnType(
					_LOCK_TABLE_NAME, "key_", "VARCHAR(200) DEFAULT NULL"));

			Assert.assertTrue(
				dbInspector.hasIndex(_LOCK_TABLE_NAME, "PRIMARY"));

			Assert.assertTrue(
				dbInspector.hasIndex(_LOCK_TABLE_NAME, "IX_UPGDLOCK"));
		}
		finally {
			_dropLockTable();
		}
	}

	private void _dropLockTable() throws SQLException {
		try (Connection connection = DataAccess.getConnection();
			PreparedStatement preparedStatement = connection.prepareStatement(
				SQLTransformer.transform(
					StringBundler.concat(
						"DROP_TABLE_IF_EXISTS(", _LOCK_TABLE_NAME, ")")))) {

			preparedStatement.executeUpdate();
		}
	}

	private boolean _hasLockTable() throws Exception {
		try (Connection connection = DataAccess.getConnection()) {
			DBInspector dbInspector = new DBInspector(connection);

			return dbInspector.hasTable(_LOCK_TABLE_NAME);
		}
	}

	private static final String _LOCK_TABLE_NAME =
		ReflectionTestUtil.getFieldValue(
			DBUpgrader.class, "_UPGRADES_LOCK_TABLE");

	private static final long _ORIGINAL_UPGRADE_DATABASE_LOCK_REFRESH_TIME =
		ReflectionTestUtil.getFieldValue(
			PropsValues.class, "UPGRADE_DATABASE_LOCK_REFRESH_TIME");

	private static final boolean _ORIGINAL_UPGRADE_DATABASE_MANAGED_STARTUP =
		ReflectionTestUtil.getFieldValue(
			PropsValues.class, "UPGRADE_DATABASE_MANAGED_STARTUP");

	private static final long _REFRESH_TIME = 2 * Time.SECOND;

	private static volatile Bundle _bundle;

	private class UpgradeThread implements Runnable {

		public UpgradeThread() {
			_executedCountDownLatch = new CountDownLatch(1);
			_executingCountDownLatch = new CountDownLatch(1);
		}

		public CountDownLatch getExecuted() {
			return _executedCountDownLatch;
		}

		public CountDownLatch getExecuting() {
			return _executingCountDownLatch;
		}

		@Override
		public void run() {
			Class<?> clazz = null;

			try {
				clazz = _bundle.loadClass(DBUpgrader.class.getName());

				ReflectionTestUtil.invoke(
					clazz, "_upgradeWithLocking",
					new Class<?>[] {
						ApplicationContext.class, UnsafeConsumer.class
					},
					null, _unsafeConsumer);
			}
			catch (ClassNotFoundException classNotFoundException) {
				throw new RuntimeException(classNotFoundException);
			}
		}

		public void stopThread() {
			_continue = false;
		}

		private volatile boolean _continue = true;
		private volatile CountDownLatch _executedCountDownLatch;
		private volatile CountDownLatch _executingCountDownLatch;

		private UnsafeConsumer<ApplicationContext, Exception> _unsafeConsumer =
			applicationContext -> {
				_executingCountDownLatch.countDown();

				while (_continue) {
				}

				_executedCountDownLatch.countDown();
			};

	}

}