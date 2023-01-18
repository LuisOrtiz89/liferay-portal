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

package com.liferay.portal.tools;

import com.liferay.petra.function.UnsafeRunnable;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.dao.orm.common.SQLTransformer;
import com.liferay.portal.kernel.dao.db.DB;
import com.liferay.portal.kernel.dao.db.DBInspector;
import com.liferay.portal.kernel.dao.db.DBManagerUtil;
import com.liferay.portal.kernel.dao.jdbc.DataAccess;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.security.SecureRandomUtil;
import com.liferay.portal.util.PropsValues;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

/**
 * @author Luis Ortiz
 */
public class DatabaseLockRunner {

	public static void runWithLock(
			String lockKey, UnsafeRunnable<Exception> unsafeRunnable)
		throws Exception {

		if (!PropsValues.DATABASE_LOCK_MANAGED_STARTUP) {
			unsafeRunnable.run();

			return;
		}

		while (true) {
			long lockId = _getOrCreateLock(lockKey);

			try (Connection connection = DataAccess.getConnection();
				PreparedStatement preparedStatement =
					_getBlockingSelectLockPreparedStatement(
						connection, lockId, lockKey)) {

				boolean autoCommit = connection.getAutoCommit();

				try {
					connection.setAutoCommit(false);

					try (ResultSet resultSet =
							preparedStatement.executeQuery()) {

						if (!resultSet.next()) {
							continue;
						}
					}

					unsafeRunnable.run();

					try (PreparedStatement preparedStatement1 =
							_getDeleteLockPreparedStatement(
								connection, lockId)) {

						preparedStatement1.executeUpdate();

						connection.commit();
					}
				}
				finally {
					connection.setAutoCommit(autoCommit);
				}

				try (PreparedStatement preparedStatement1 =
						_getDropLockTablePreparedStatement(connection)) {

					preparedStatement1.executeUpdate();
				}
				catch (SQLException sqlException) {
					if (_log.isDebugEnabled()) {
						_log.debug(sqlException);
					}
				}

				return;
			}
			catch (SQLException sqlException) {
				if (_log.isDebugEnabled()) {
					_log.debug(sqlException);
				}

				Thread.sleep(PropsValues.DATABASE_LOCK_REFRESH_TIME);
			}
		}
	}

	private static void _createLockTableIfNotExists() throws Exception {
		while (true) {
			if (_hasLockTable()) {
				return;
			}

			DB db = DBManagerUtil.getDB();

			try (Connection connection = DataAccess.getConnection();
				PreparedStatement preparedStatement1 =
					connection.prepareStatement(
						db.buildSQL(
							StringBundler.concat(
								"create table ", _DATABASE_LOCK_TABLE,
								" (lockId LONG not null primary key, ",
								"createDate DATE default null, className ",
								"VARCHAR(75) default null, key_ VARCHAR(200) ",
								"default null)")));
				PreparedStatement preparedStatement2 =
					connection.prepareStatement(
						db.buildSQL(
							StringBundler.concat(
								"create unique index IX_UPGDLOCK on ",
								_DATABASE_LOCK_TABLE, " (className, key_)")))) {

				preparedStatement1.executeUpdate();
				preparedStatement2.executeUpdate();
			}
			catch (SQLException sqlException) {
				if (_log.isDebugEnabled()) {
					_log.debug(sqlException);
				}
			}
		}
	}

	private static PreparedStatement _getBlockingSelectLockPreparedStatement(
			Connection connection, long lockId, String lockKey)
		throws SQLException {

		PreparedStatement preparedStatement = connection.prepareStatement(
			SQLTransformer.transform(
				StringBundler.concat(
					"select lockId from ", _DATABASE_LOCK_TABLE,
					" where className = ? and key_ = ? and lockId = ? ",
					"FOR_UPDATE")));

		preparedStatement.setString(1, DatabaseLockRunner.class.getName());
		preparedStatement.setString(2, lockKey);
		preparedStatement.setLong(3, lockId);

		return preparedStatement;
	}

	private static PreparedStatement _getDeleteLockPreparedStatement(
			Connection connection, long lockId)
		throws SQLException {

		PreparedStatement preparedStatement = connection.prepareStatement(
			StringBundler.concat(
				"delete from ", _DATABASE_LOCK_TABLE, " where lockId = ?"));

		preparedStatement.setLong(1, lockId);

		return preparedStatement;
	}

	private static PreparedStatement _getDropLockTablePreparedStatement(
			Connection connection)
		throws SQLException {

		return connection.prepareStatement(
			SQLTransformer.transform(
				StringBundler.concat(
					"DROP_TABLE_IF_EXISTS(", _DATABASE_LOCK_TABLE, ")")));
	}

	private static PreparedStatement _getInsertLockPreparedStatement(
			Connection connection, long lockId, String lockKey)
		throws SQLException {

		PreparedStatement preparedStatement = connection.prepareStatement(
			StringBundler.concat(
				"insert into ", _DATABASE_LOCK_TABLE, " (lockId, createDate, ",
				"className, key_) values (?, ?, ?, ?)"));

		Timestamp now = new Timestamp(System.currentTimeMillis());

		preparedStatement.setLong(1, lockId);
		preparedStatement.setTimestamp(2, now);
		preparedStatement.setString(3, DatabaseLockRunner.class.getName());
		preparedStatement.setString(4, lockKey);

		return preparedStatement;
	}

	private static long _getOrCreateLock(String lockKey) throws Exception {
		while (true) {
			_createLockTableIfNotExists();

			try (Connection connection = DataAccess.getConnection();
				PreparedStatement preparedStatement =
					_getSelectLockPreparedStatement(connection, lockKey);
				ResultSet resultSet = preparedStatement.executeQuery()) {

				if (resultSet.next()) {
					return resultSet.getLong(1);
				}

				long lockId = SecureRandomUtil.nextLong();

				try (PreparedStatement preparedStatement2 =
						_getInsertLockPreparedStatement(
							connection, lockId, lockKey)) {

					if (preparedStatement2.executeUpdate() == 1) {
						return lockId;
					}
				}
			}
			catch (SQLException sqlException) {
				if (_log.isDebugEnabled()) {
					_log.debug(sqlException);
				}
			}
		}
	}

	private static PreparedStatement _getSelectLockPreparedStatement(
			Connection connection, String lockKey)
		throws SQLException {

		PreparedStatement preparedStatement = connection.prepareStatement(
			StringBundler.concat(
				"select lockId from ", _DATABASE_LOCK_TABLE,
				" where className = ? and key_ = ?"));

		preparedStatement.setString(1, DatabaseLockRunner.class.getName());
		preparedStatement.setString(2, lockKey);

		return preparedStatement;
	}

	private static boolean _hasLockTable() throws Exception {
		try (Connection connection = DataAccess.getConnection()) {
			DBInspector dbInspector = new DBInspector(connection);

			return dbInspector.hasTable(_DATABASE_LOCK_TABLE);
		}
	}

	private static final String _DATABASE_LOCK_TABLE = "DatabaseLock_";

	private static final Log _log = LogFactoryUtil.getLog(
		DatabaseLockRunner.class);

}