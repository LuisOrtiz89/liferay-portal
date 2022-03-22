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
import com.liferay.portal.kernel.dao.db.DB;
import com.liferay.portal.kernel.dao.db.DBInspector;
import com.liferay.portal.kernel.dao.db.DBManagerUtil;
import com.liferay.portal.kernel.dao.jdbc.DataAccess;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.portal.kernel.util.Time;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.tools.DBUpgrader;
import com.liferay.portal.util.PropsValues;

import java.io.IOException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

import java.util.concurrent.atomic.AtomicReference;

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

	@Test(timeout = 8 * _REFRESH_TIME)
	public void testAcquireLocks() throws Exception {
		Thread thread1 = null;
		Thread thread2 = null;

		_stopConsumer = false;

		UnsafeConsumer<ApplicationContext, Exception> unsafeConsumer =
			applicationContext -> {
				while (!_stopConsumer) {
				}
			};

		try {
			Assert.assertFalse(_hasLockTable());

			AtomicReference<Boolean> hasUpdated1 = new AtomicReference<>();

			thread1 = new Thread(
				() -> {
					Class<?> clazz = null;

					try {
						clazz = _bundle.loadClass(DBUpgrader.class.getName());

						hasUpdated1.set(
							ReflectionTestUtil.invoke(
								clazz, "_upgradeWithLocking",
								new Class<?>[] {
									ApplicationContext.class,
									UnsafeConsumer.class
								},
								null, unsafeConsumer));
					}
					catch (ClassNotFoundException classNotFoundException) {
						throw new RuntimeException(classNotFoundException);
					}
				});

			thread1.start();

			Thread.sleep(_REFRESH_TIME);

			Assert.assertTrue(thread1.isAlive());

			Assert.assertTrue(_hasLockTable());

			AtomicReference<Boolean> hasUpdated2 = new AtomicReference<>();

			thread2 = new Thread(
				() -> {
					Class<?> clazz = null;

					try {
						clazz = _bundle.loadClass(DBUpgrader.class.getName());

						hasUpdated2.set(
							ReflectionTestUtil.invoke(
								clazz, "_upgradeWithLocking",
								new Class<?>[] {
									ApplicationContext.class,
									UnsafeConsumer.class
								},
								null, unsafeConsumer));
					}
					catch (ClassNotFoundException classNotFoundException) {
						throw new RuntimeException(classNotFoundException);
					}
				});

			thread2.start();

			Thread.sleep(_REFRESH_TIME);

			Assert.assertTrue(thread1.isAlive());

			Assert.assertTrue(thread2.isAlive());

			_stopConsumer = true;

			thread1.join(2 * _REFRESH_TIME);

			thread2.join(2 * _REFRESH_TIME);

			Assert.assertFalse(thread1.isAlive());

			Assert.assertFalse(thread2.isAlive());

			Assert.assertTrue(hasUpdated1.get());

			Assert.assertFalse(hasUpdated2.get());

			Assert.assertFalse(_hasLockTable());
		}
		finally {
			if ((thread1 != null) && thread1.isAlive()) {
				thread1.interrupt();
			}

			if ((thread2 != null) && thread2.isAlive()) {
				thread2.interrupt();
			}

			if (_hasLockTable()) {
				_dropLockTable();
			}
		}
	}

	@Test
	public void testCreateLockTable() throws Exception {
		Class<?> clazz = _bundle.loadClass(DBUpgrader.class.getName());

		ReflectionTestUtil.invoke(clazz, "_createLockTable", null);

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

	@Test(timeout = 4 * _REFRESH_TIME)
	public void testWaitForLocksWhenManagedStartupDisabled() throws Exception {
		ReflectionTestUtil.setFieldValue(
			PropsValues.class, "UPGRADE_DATABASE_MANAGED_STARTUP", false);

		_createLockTable();

		_insertLock();

		Thread thread = null;

		try {
			thread = new Thread(
				() -> {
					try {
						DBUpgrader.waitForLocks();
					}
					catch (Exception exception) {
					}
				});

			thread.start();

			thread.join(_REFRESH_TIME);

			Assert.assertFalse(thread.isAlive());

			_deleteLock();
		}
		finally {
			if ((thread != null) && thread.isAlive()) {
				thread.interrupt();
			}

			_dropLockTable();
		}
	}

	@Test(timeout = 6 * _REFRESH_TIME)
	public void testWaitForLocksWhenManagedStartupEnabled()
		throws InterruptedException, IOException, SQLException {

		ReflectionTestUtil.setFieldValue(
			PropsValues.class, "UPGRADE_DATABASE_MANAGED_STARTUP", true);

		_createLockTable();

		_insertLock();

		Thread thread = null;

		try {
			thread = new Thread(
				() -> {
					try {
						DBUpgrader.waitForLocks();
					}
					catch (Exception exception) {
					}
				});

			thread.start();

			Thread.sleep(_REFRESH_TIME);

			Assert.assertTrue(thread.isAlive());

			Thread.sleep(2 * _REFRESH_TIME);

			Assert.assertTrue(thread.isAlive());

			_deleteLock();

			thread.join(2 * _REFRESH_TIME);

			Assert.assertFalse(thread.isAlive());
		}
		finally {
			if ((thread != null) && thread.isAlive()) {
				thread.interrupt();
			}

			_dropLockTable();
		}
	}

	private void _createLockTable() throws IOException, SQLException {
		DB db = DBManagerUtil.getDB();

		try (Connection connection = DataAccess.getConnection();
			PreparedStatement preparedStatement1 = connection.prepareStatement(
				db.buildSQL(
					com.liferay.petra.string.StringBundler.concat(
						"create table ", _LOCK_TABLE_NAME,
						" (lockId LONG not null primary key, ",
						"createDate DATE default null, className ",
						"VARCHAR(75) default null, key_ VARCHAR(200) ",
						"default null)")));
			PreparedStatement preparedStatement2 = connection.prepareStatement(
				db.buildSQL(
					com.liferay.petra.string.StringBundler.concat(
						"create unique index IX_UPGDLOCK on ", _LOCK_TABLE_NAME,
						" (className, key_)")))) {

			preparedStatement1.executeUpdate();
			preparedStatement2.executeUpdate();
		}
	}

	private void _deleteLock() throws SQLException {
		try (Connection connection = DataAccess.getConnection();
			PreparedStatement preparedStatement = connection.prepareStatement(
				com.liferay.petra.string.StringBundler.concat(
					"delete from ", _LOCK_TABLE_NAME, " where lockId = ?"))) {

			preparedStatement.setLong(1, _LOCK_ID);

			preparedStatement.executeUpdate();
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

	private void _insertLock() throws SQLException {
		try (Connection connection = DataAccess.getConnection();
			PreparedStatement preparedStatement = connection.prepareStatement(
				com.liferay.petra.string.StringBundler.concat(
					"insert into ", _LOCK_TABLE_NAME, " (lockId, createDate, ",
					"className, key_) values (?, ?, ?, ?)"))) {

			Timestamp now = new Timestamp(System.currentTimeMillis());
			String lockKey = ReflectionTestUtil.getFieldValue(
				DBUpgrader.class, "_LOCK_KEY");

			preparedStatement.setLong(1, _LOCK_ID);
			preparedStatement.setTimestamp(2, now);
			preparedStatement.setString(3, DBUpgrader.class.getName());
			preparedStatement.setString(4, lockKey);

			preparedStatement.executeUpdate();
		}
	}

	private static final long _LOCK_ID = 123456789;

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

	private static Bundle _bundle;
	private static volatile boolean _stopConsumer;

}