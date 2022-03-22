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
import com.liferay.petra.function.UnsafeRunnable;
import com.liferay.portal.dao.orm.common.SQLTransformer;
import com.liferay.portal.kernel.dao.db.DBInspector;
import com.liferay.portal.kernel.dao.jdbc.DataAccess;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.portal.kernel.util.Time;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.tools.DatabaseLockRunner;
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

/**
 * @author Luis Ortiz
 */
@RunWith(Arquillian.class)
public class ManagedStartupTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new LiferayIntegrationTestRule();

	@BeforeClass
	public static void setUpClass() throws Exception {
		_bundle = FrameworkUtil.getBundle(ManagedStartupTest.class);
	}

	@Before
	public void setUp() {
		ReflectionTestUtil.setFieldValue(
			PropsValues.class, "DATABASE_LOCK_MANAGED_STARTUP", true);

		ReflectionTestUtil.setFieldValue(
			PropsValues.class, "DATABASE_LOCK_REFRESH_TIME", _REFRESH_TIME);
	}

	@After
	public void tearDown() throws IllegalAccessException, SQLException {
		ReflectionTestUtil.setFieldValue(
			PropsValues.class, "DATABASE_LOCK_MANAGED_STARTUP",
			_ORIGINAL_DATABASE_LOCK_MANAGED_STARTUP);

		ReflectionTestUtil.setFieldValue(
			PropsValues.class, "DATABASE_LOCK_REFRESH_TIME",
			_ORIGINAL_DATABASE_LOCK_REFRESH_TIME);
	}

	@Test(timeout = 60000)
	public void testAcquireLocks() throws Exception {
		ParallelRunnable parallelRunnable1 = new ParallelRunnable();

		CountDownLatch executingThread1 = parallelRunnable1.getExecuting();
		CountDownLatch executedThread1 = parallelRunnable1.getExecuted();

		ParallelRunnable parallelRunnable2 = new ParallelRunnable();

		CountDownLatch executingThread2 = parallelRunnable2.getExecuting();
		CountDownLatch executedThread2 = parallelRunnable2.getExecuted();

		Thread thread1 = new Thread(parallelRunnable1);
		Thread thread2 = new Thread(parallelRunnable2);

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

			parallelRunnable1.stopThread();

			executedThread1.await(10, TimeUnit.SECONDS);

			Assert.assertEquals(0, executedThread1.getCount());

			thread1.join(5000);

			Assert.assertFalse(thread1.isAlive());

			Assert.assertTrue(thread2.isAlive());

			executingThread2.await(10, TimeUnit.SECONDS);

			Assert.assertEquals(0, executingThread2.getCount());

			Assert.assertEquals(1, executedThread2.getCount());

			parallelRunnable2.stopThread();

			executedThread2.await(10, TimeUnit.SECONDS);

			Assert.assertEquals(0, executedThread2.getCount());

			thread2.join(5000);

			Assert.assertFalse(thread2.isAlive());

			Assert.assertFalse(_hasLockTable());
		}
		finally {
			parallelRunnable1.stopThread();
			parallelRunnable2.stopThread();

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
		Class<?> clazz = _bundle.loadClass(DatabaseLockRunner.class.getName());

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
			DatabaseLockRunner.class, "_DATABASE_LOCK_TABLE");

	private static final boolean _ORIGINAL_DATABASE_LOCK_MANAGED_STARTUP =
		ReflectionTestUtil.getFieldValue(
			PropsValues.class, "DATABASE_LOCK_MANAGED_STARTUP");

	private static final long _ORIGINAL_DATABASE_LOCK_REFRESH_TIME =
		ReflectionTestUtil.getFieldValue(
			PropsValues.class, "DATABASE_LOCK_REFRESH_TIME");

	private static final long _REFRESH_TIME = 2 * Time.SECOND;

	private static final String _TEST_LOCK_KEY = "TestProcess";

	private static volatile Bundle _bundle;

	private class ParallelRunnable implements Runnable {

		public ParallelRunnable() {
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
			try {
				DatabaseLockRunner.runWithLock(_TEST_LOCK_KEY, _unsafeRunnable);
			}
			catch (Exception exception) {
				throw new RuntimeException(exception);
			}
		}

		public void stopThread() {
			_continue = false;
		}

		private volatile boolean _continue = true;
		private volatile CountDownLatch _executedCountDownLatch;
		private volatile CountDownLatch _executingCountDownLatch;

		private UnsafeRunnable<Exception> _unsafeRunnable = () -> {
			_executingCountDownLatch.countDown();

			while (_continue) {
			}

			_executedCountDownLatch.countDown();
		};

	}

}