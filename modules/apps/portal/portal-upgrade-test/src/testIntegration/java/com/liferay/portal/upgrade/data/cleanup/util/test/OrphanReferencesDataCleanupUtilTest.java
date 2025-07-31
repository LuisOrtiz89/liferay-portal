/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.upgrade.data.cleanup.util.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.petra.function.UnsafeConsumer;
import com.liferay.petra.function.UnsafeRunnable;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.dao.db.DB;
import com.liferay.portal.kernel.dao.db.DBInspector;
import com.liferay.portal.kernel.dao.db.DBManagerUtil;
import com.liferay.portal.kernel.dao.jdbc.DataAccess;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.security.auth.CompanyThreadLocal;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.kernel.upgrade.data.cleanup.util.OrphanReferencesDataCleanupUtil;
import com.liferay.portal.kernel.util.PortletKeys;
import com.liferay.portal.test.log.LogCapture;
import com.liferay.portal.test.log.LogEntry;
import com.liferay.portal.test.log.LoggerTestUtil;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;

import java.sql.Connection;

import java.util.List;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Luis Ortiz
 */
@RunWith(Arquillian.class)
public class OrphanReferencesDataCleanupUtilTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new LiferayIntegrationTestRule();

	@BeforeClass
	public static void setUpClass() throws Exception {
		_connection = DataAccess.getConnection();
		_db = DBManagerUtil.getDB();

		_dbInspector = new DBInspector(_connection);
	}

	@Test
	public void testCleanTablesUpExcludedTable() throws Exception {
		long auditEventId = RandomTestUtil.nextLong();
		long companyId = RandomTestUtil.nextLong();

		_testCleanUpTables(
			logCapture -> {
				List<LogEntry> logEntries = logCapture.getLogEntries();

				Assert.assertTrue(logEntries.toString(), logEntries.isEmpty());
			},
			() -> _db.runSQL(
				_connection,
				"delete from Audit_AuditEvent where auditEventId = " +
					auditEventId),
			() -> _db.runSQL(
				_connection,
				StringBundler.concat(
					"insert into Audit_AuditEvent (auditEventId, companyId, ",
					"className) values (", auditEventId, ", ", companyId, ", '",
					OrphanReferencesDataCleanupUtilTest.class.getName(), "')")),
			null, "companyId", "Audit_AuditEvent", "companyId", "Company");
	}

	@Test
	public void testCleanUpTablesWithoutWhereClause() throws Exception {
		long companyId = RandomTestUtil.nextLong();

		_testCleanUpTables(
			logCapture -> {
				List<LogEntry> logEntries = logCapture.getLogEntries();

				Assert.assertEquals(
					logEntries.toString(), 1, logEntries.size());

				LogEntry logEntry = logEntries.get(0);

				Assert.assertEquals(
					_getCleanUpTableExpectedMessage(
						2, _dbInspector.normalizeName("companyId"),
						_dbInspector.normalizeName("Portlet"),
						_dbInspector.normalizeName("companyId"),
						_dbInspector.normalizeName("Company"), companyId),
					logEntry.getMessage());
			},
			() -> _db.runSQL(
				_connection,
				"delete from Portlet where companyId = " + companyId),
			() -> {
				_db.runSQL(
					_connection,
					StringBundler.concat(
						"insert into Portlet (mvccVersion, id_, companyId, ",
						"portletId, active_) values (0, ",
						RandomTestUtil.nextLong(), ", ", companyId, ", '",
						RandomTestUtil.randomString(), "', [$FALSE$])"));
				_db.runSQL(
					_connection,
					StringBundler.concat(
						"insert into Portlet (mvccVersion, id_, companyId, ",
						"portletId, active_) values (0, ",
						RandomTestUtil.nextLong(), ", ", companyId, ", '",
						RandomTestUtil.randomString(), "', [$FALSE$])"));
			},
			null, "companyId", "Portlet", "companyId", "Company");
	}

	@Test
	public void testCleanUpTablesWithWhereClause() throws Exception {
		long companyId = RandomTestUtil.nextLong();
		long ownerType1 = PortletKeys.PREFS_OWNER_TYPE_COMPANY;
		long ownerType2 = PortletKeys.PREFS_OWNER_TYPE_GROUP;

		_testCleanUpTables(
			logCapture -> {
				List<LogEntry> logEntries = logCapture.getLogEntries();

				Assert.assertEquals(
					logEntries.toString(), 1, logEntries.size());

				LogEntry logEntry = logEntries.get(0);

				Assert.assertEquals(
					_getCleanUpTableExpectedMessage(
						2, _dbInspector.normalizeName("ownerId"),
						_dbInspector.normalizeName("PortletPreferences"),
						_dbInspector.normalizeName("companyId"),
						_dbInspector.normalizeName("Company"), companyId),
					logEntry.getMessage());
			},
			() -> _db.runSQL(
				_connection,
				"delete from PortletPreferences where companyId = " +
					companyId),
			() -> {
				_db.runSQL(
					_connection,
					StringBundler.concat(
						"insert into PortletPreferences (mvccVersion, ",
						"ctCollectionId, portletPreferencesId, ownerId, ",
						"ownerType, companyId, portletId) values (0, 0, ",
						RandomTestUtil.nextLong(), ", ", companyId, ", ",
						ownerType1, ", ", companyId, ", '",
						RandomTestUtil.randomString(), "')"));
				_db.runSQL(
					_connection,
					StringBundler.concat(
						"insert into PortletPreferences (mvccVersion, ",
						"ctCollectionId, portletPreferencesId, ownerId, ",
						"ownerType, companyId, portletId) values (0, 0, ",
						RandomTestUtil.nextLong(), ", ", companyId, ", ",
						ownerType1, ", ", companyId, ", '",
						RandomTestUtil.randomString(), "')"));
				_db.runSQL(
					_connection,
					StringBundler.concat(
						"insert into PortletPreferences (mvccVersion, ",
						"ctCollectionId, portletPreferencesId, ownerId, ",
						"ownerType, companyId, portletId) values (0, 0, ",
						RandomTestUtil.nextLong(), ", ", companyId, ", ",
						ownerType2, ", ", companyId, ", '",
						RandomTestUtil.randomString(), "')"));
			},
			"ownerType = " + ownerType1, "ownerId", "PortletPreferences",
			"companyId", "Company");
	}

	@Test
	public void testFixOrphanUsersDeleteAction() throws Exception {
		long userId = RandomTestUtil.nextLong();

		_testFixOrphanUsers(
			logCapture -> {
				List<LogEntry> logEntries = logCapture.getLogEntries();

				Assert.assertEquals(
					logEntries.toString(), 1, logEntries.size());

				LogEntry logEntry = logEntries.get(0);

				Assert.assertEquals(
					_getUserDeletedExpectedMessage(
						_dbInspector.normalizeName("userId"), 1,
						_dbInspector.normalizeName("Users_Roles"),
						_dbInspector.normalizeName("User_"), userId),
					logEntry.getMessage());
			},
			() -> _db.runSQL(
				_connection,
				"delete from Users_Roles where userId = " + userId),
			() -> _db.runSQL(
				_connection,
				StringBundler.concat(
					"insert into Users_Roles (companyId, roleId, userId, ",
					"ctCollectionId) values (",
					CompanyThreadLocal.getCompanyId(), ", ",
					RandomTestUtil.nextLong(), ", ", userId, ", 0)")),
			"userId", "Users_Roles");
	}

	@Test
	public void testFixOrphanUsersExcludedTable() throws Exception {
		long auditEventId = RandomTestUtil.nextLong();
		long userId = RandomTestUtil.nextLong();

		_testFixOrphanUsers(
			logCapture -> {
				List<LogEntry> logEntries = logCapture.getLogEntries();

				Assert.assertTrue(logEntries.toString(), logEntries.isEmpty());
			},
			() -> _db.runSQL(
				_connection,
				"delete from Audit_AuditEvent where auditEventId = " +
					auditEventId),
			() -> _db.runSQL(
				_connection,
				StringBundler.concat(
					"insert into Audit_AuditEvent (auditEventId, userId, ",
					"className) values (", auditEventId, ", ", userId, ", '",
					OrphanReferencesDataCleanupUtilTest.class.getName(), "')")),
			"companyId", "Audit_AuditEvent");
	}

	@Test
	public void testFixOrphanUsersUpdateAction() throws Exception {
		User adminUser = UserTestUtil.getAdminUser(
			CompanyThreadLocal.getCompanyId());
		long layoutId = RandomTestUtil.nextLong();
		long userId = RandomTestUtil.nextLong();

		_testFixOrphanUsers(
			logCapture -> {
				List<LogEntry> logEntries = logCapture.getLogEntries();

				Assert.assertEquals(
					logEntries.toString(), 1, logEntries.size());

				LogEntry logEntry = logEntries.get(0);

				Assert.assertEquals(
					_getUserUpdatedExpectedMessage(
						_dbInspector.normalizeName("userId"), 1,
						adminUser.getUserId(),
						_dbInspector.normalizeName("Layout"),
						_dbInspector.normalizeName("User_"), userId),
					logEntry.getMessage());
			},
			() -> _db.runSQL(
				_connection, "delete from Layout where layoutId = " + layoutId),
			() -> _db.runSQL(
				_connection,
				StringBundler.concat(
					"insert into Layout (mvccVersion, ctCollectionId, plid, ",
					"groupId, companyId, userId, layoutId) values (0, 0, ",
					RandomTestUtil.nextLong(), ", ", RandomTestUtil.nextLong(),
					", ", CompanyThreadLocal.getCompanyId(), ", ", userId, ", ",
					layoutId, ")")),
			"userId", "Layout");
	}

	private String _getCleanUpTableExpectedMessage(
			long count, String sourceColumnName, String sourceTableName,
			String targetColumnName, String targetTableName, long targetValue)
		throws Exception {

		return StringBundler.concat(
			"Table: ", _dbInspector.normalizeName(sourceTableName), ", ", count,
			(count == 1) ? " entry " : " entries ", "deleted. Reason: ",
			_dbInspector.normalizeName(sourceColumnName), StringPool.SPACE,
			targetValue, " was not found in ",
			_dbInspector.normalizeName(targetTableName), StringPool.PERIOD,
			_dbInspector.normalizeName(targetColumnName));
	}

	private String _getUserDeletedExpectedMessage(
			String columnName, long count, String sourceTableName,
			String targetTableName, long targetValue)
		throws Exception {

		return StringBundler.concat(
			"Table: ", _dbInspector.normalizeName(sourceTableName), ", ", count,
			(count == 1) ? " entry " : " entries ", "deleted. Reason: ",
			_dbInspector.normalizeName(columnName), StringPool.SPACE,
			targetValue, " was not found in ",
			_dbInspector.normalizeName(targetTableName), StringPool.PERIOD,
			_dbInspector.normalizeName(columnName));
	}

	private String _getUserUpdatedExpectedMessage(
			String columnName, long count, long newValue,
			String sourceTableName, String targetTableName, long targetValue)
		throws Exception {

		return StringBundler.concat(
			"Table: ", _dbInspector.normalizeName(sourceTableName), ", ", count,
			(count == 1) ? " entry " : " entries ", "updated column ",
			_dbInspector.normalizeName(columnName), " to value ", newValue,
			". Reason: ", _dbInspector.normalizeName(columnName),
			StringPool.SPACE, targetValue, " was not found in ",
			_dbInspector.normalizeName(targetTableName), StringPool.PERIOD,
			_dbInspector.normalizeName(columnName));
	}

	private void _testCleanUpTables(
			UnsafeConsumer<LogCapture, Exception> assertUnsafeConsumer,
			UnsafeRunnable<Exception> cleanUpDataUnsafeRunnable,
			UnsafeRunnable<Exception> initializeDataUnsafeRunnable,
			String sourceAdditionalWhereClause, String sourceColumnName,
			String sourceTableName, String targetColumnName,
			String targetTableName)
		throws Exception {

		initializeDataUnsafeRunnable.run();

		try (LogCapture logCapture = LoggerTestUtil.configureLog4JLogger(
				OrphanReferencesDataCleanupUtil.class.getName(),
				LoggerTestUtil.INFO)) {

			OrphanReferencesDataCleanupUtil.cleanUpTable(
				_connection, sourceAdditionalWhereClause,
				_dbInspector.normalizeName(sourceColumnName),
				_dbInspector.normalizeName(sourceTableName),
				_dbInspector.normalizeName(targetColumnName),
				_dbInspector.normalizeName(targetTableName));

			assertUnsafeConsumer.accept(logCapture);
		}
		finally {
			cleanUpDataUnsafeRunnable.run();
		}
	}

	private void _testFixOrphanUsers(
			UnsafeConsumer<LogCapture, Exception> assertUnsafeConsumer,
			UnsafeRunnable<Exception> cleanUpDataUnsafeRunnable,
			UnsafeRunnable<Exception> initializeDataUnsafeRunnable,
			String sourceColumnName, String sourceTableName)
		throws Exception {

		initializeDataUnsafeRunnable.run();

		try (LogCapture logCapture = LoggerTestUtil.configureLog4JLogger(
				OrphanReferencesDataCleanupUtil.class.getName(),
				LoggerTestUtil.INFO)) {

			OrphanReferencesDataCleanupUtil.fixOrphanUsers(
				_connection, _dbInspector.normalizeName(sourceColumnName),
				_dbInspector.normalizeName(sourceTableName),
				_dbInspector.normalizeName("userId"),
				_dbInspector.normalizeName("User_"));

			assertUnsafeConsumer.accept(logCapture);
		}
		finally {
			cleanUpDataUnsafeRunnable.run();
		}
	}

	private static Connection _connection;
	private static DB _db;
	private static DBInspector _dbInspector;

}