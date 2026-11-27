/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.upgrade.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.petra.lang.SafeCloseable;
import com.liferay.portal.kernel.dao.db.DB;
import com.liferay.portal.kernel.dao.db.DBManagerUtil;
import com.liferay.portal.kernel.dao.db.DBType;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.model.Release;
import com.liferay.portal.kernel.service.ReleaseLocalService;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.PropsValuesTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.upgrade.DummyUpgradeStep;
import com.liferay.portal.kernel.upgrade.UpgradeProcess;
import com.liferay.portal.kernel.upgrade.UpgradeStep;
import com.liferay.portal.test.log.LogCapture;
import com.liferay.portal.test.log.LogEntry;
import com.liferay.portal.test.log.LoggerTestUtil;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.upgrade.registry.UpgradeStepRegistrator;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.After;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceRegistration;

/**
 * @author István András Dézsi
 */
@RunWith(Arquillian.class)
public class UpgradeOSGiCommandsTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new LiferayIntegrationTestRule();

	@After
	public void tearDown() {
		if (_serviceRegistration != null) {
			_serviceRegistration.unregister();
		}
	}

	@Test
	public void testDescribe() throws Exception {
		Bundle bundle = FrameworkUtil.getBundle(UpgradeOSGiCommandsTest.class);

		String bundleSymbolicName = bundle.getSymbolicName();

		Release release = _releaseLocalService.addRelease(
			bundleSymbolicName, "1.0.0");

		try {
			TestUpgradeProcess testUpgradeProcess = new TestUpgradeProcess();

			UpgradeStepRegistrator upgradeStepRegistrator =
				_registerDescribedUpgradeStepRegistrator(
					bundle, testUpgradeProcess);

			JSONObject jsonObject = _describe(bundleSymbolicName);

			JSONObject databaseJSONObject = jsonObject.getJSONObject(
				"database");

			Assert.assertEquals(
				_getDBTypeName(), databaseJSONObject.getString("dbType"));

			Assert.assertEquals(1, jsonObject.getInt("formatVersion"));
			Assert.assertTrue(jsonObject.isNull("gaps"));
			Assert.assertEquals("pending", jsonObject.getString("mode"));

			JSONArray releaseChangesJSONArray = jsonObject.getJSONArray(
				"releaseChanges");

			Assert.assertEquals(0, releaseChangesJSONArray.length());

			Assert.assertTrue(jsonObject.isNull("types"));
			Assert.assertTrue(jsonObject.isNull("uniqueIndexDuplicates"));

			JSONObject moduleJSONObject = _getModuleJSONObject(jsonObject);

			Assert.assertEquals(
				bundleSymbolicName,
				moduleJSONObject.getString("bundleSymbolicName"));
			Assert.assertEquals(
				"1.0.0", moduleJSONObject.getString("schemaVersion"));
			Assert.assertEquals("good", moduleJSONObject.getString("state"));
			Assert.assertEquals(
				"2.0.0", moduleJSONObject.getString("targetSchemaVersion"));
			Assert.assertFalse(moduleJSONObject.getBoolean("updateIndexes"));

			JSONArray edgesJSONArray = moduleJSONObject.getJSONArray("edges");

			Assert.assertEquals(
				edgesJSONArray.toString(), 1, edgesJSONArray.length());

			JSONObject edgeJSONObject = edgesJSONArray.getJSONObject(0);

			Assert.assertEquals(
				"1.0.0", edgeJSONObject.getString("fromSchemaVersion"));
			Assert.assertTrue(edgeJSONObject.isNull("origin"));

			Class<?> upgradeStepRegistratorClass =
				upgradeStepRegistrator.getClass();

			Assert.assertEquals(
				upgradeStepRegistratorClass.getName(),
				edgeJSONObject.getString("registratorClassName"));

			Assert.assertEquals(
				"2.0.0", edgeJSONObject.getString("toSchemaVersion"));

			JSONArray stepsJSONArray = edgeJSONObject.getJSONArray("steps");

			Assert.assertEquals(
				stepsJSONArray.toString(), 3, stepsJSONArray.length());

			_assertStepJSONObject(
				TestPreUpgradeStep.class, 0, stepsJSONArray.getJSONObject(0));
			_assertStepJSONObject(
				TestUpgradeProcess.class, 1, stepsJSONArray.getJSONObject(1));
			_assertStepJSONObject(
				TestPostUpgradeStep.class, 2, stepsJSONArray.getJSONObject(2));

			Assert.assertEquals(0, testUpgradeProcess.getUpgradeCount());

			release = _releaseLocalService.fetchRelease(bundleSymbolicName);

			Assert.assertEquals("1.0.0", release.getSchemaVersion());
		}
		finally {
			_releaseLocalService.deleteRelease(release);
		}
	}

	@Test
	public void testDescribeWithFull() throws Exception {
		Bundle bundle = FrameworkUtil.getBundle(UpgradeOSGiCommandsTest.class);

		String bundleSymbolicName = bundle.getSymbolicName();

		Release release = _releaseLocalService.addRelease(
			bundleSymbolicName, "1.0.0");

		try {
			_registerDescribedUpgradeStepRegistrator(
				bundle, new TestUpgradeProcess());

			JSONObject jsonObject = _describe("--full", bundleSymbolicName);

			Assert.assertTrue(jsonObject.isNull("database"));
			Assert.assertEquals("full", jsonObject.getString("mode"));
			Assert.assertTrue(jsonObject.isNull("releaseChanges"));

			JSONObject moduleJSONObject = _getModuleJSONObject(jsonObject);

			Assert.assertTrue(moduleJSONObject.isNull("schemaVersion"));
			Assert.assertTrue(moduleJSONObject.isNull("state"));
			Assert.assertEquals(
				"2.0.0", moduleJSONObject.getString("targetSchemaVersion"));
			Assert.assertTrue(moduleJSONObject.isNull("updateIndexes"));

			JSONArray edgesJSONArray = moduleJSONObject.getJSONArray("edges");

			Assert.assertEquals(
				edgesJSONArray.toString(), 2, edgesJSONArray.length());

			JSONObject edgeJSONObject = edgesJSONArray.getJSONObject(0);

			Assert.assertEquals(
				"0.0.0", edgeJSONObject.getString("fromSchemaVersion"));
			Assert.assertEquals(
				"1.0.0", edgeJSONObject.getString("toSchemaVersion"));

			edgeJSONObject = edgesJSONArray.getJSONObject(1);

			Assert.assertEquals(
				"1.0.0", edgeJSONObject.getString("fromSchemaVersion"));
			Assert.assertEquals(
				"2.0.0", edgeJSONObject.getString("toSchemaVersion"));

			jsonObject = _describe("--full", "com.liferay.account.service");

			moduleJSONObject = _getModuleJSONObject(jsonObject);

			edgeJSONObject = _getEdgeJSONObject(
				moduleJSONObject.getJSONArray("edges"), "1.3.0", "2.0.0");

			JSONArray stepsJSONArray = edgeJSONObject.getJSONArray("steps");

			Assert.assertEquals(
				stepsJSONArray.toString(), 2, stepsJSONArray.length());

			JSONObject stepJSONObject = stepsJSONArray.getJSONObject(0);

			Assert.assertEquals(
				"com.liferay.account.internal.upgrade.v2_0_0." +
					"AccountGroupAccountEntryRelUpgradeProcess",
				stepJSONObject.getString("className"));

			stepJSONObject = stepsJSONArray.getJSONObject(1);

			Assert.assertEquals(1, stepJSONObject.getInt("position"));
		}
		finally {
			_releaseLocalService.deleteRelease(release);
		}
	}

	@Test
	public void testDescribeWithUnregisteredModule() throws Exception {
		JSONObject jsonObject = _describe(RandomTestUtil.randomString());

		JSONArray modulesJSONArray = jsonObject.getJSONArray("modules");

		Assert.assertEquals(
			modulesJSONArray.toString(), 0, modulesJSONArray.length());
	}

	@Test
	public void testExecuteAllWithFailedRegistration() throws Exception {
		Bundle bundle = FrameworkUtil.getBundle(UpgradeOSGiCommandsTest.class);

		String bundleSymbolicName = bundle.getSymbolicName();

		Class<?> clazz = _upgradeExecutor.getClass();

		try (SafeCloseable safeCloseable =
				PropsValuesTestUtil.swapWithSafeCloseable(
					"UPGRADE_DATABASE_AUTO_RUN", false, false);
			LogCapture logCapture = LoggerTestUtil.configureLog4JLogger(
				clazz.getName(), LoggerTestUtil.OFF)) {

			_registerFailingUpgradeStepRegistrator(bundle);

			_assertExecuteAll(
				"The following modules had errors while upgrading:\n\t" +
					bundleSymbolicName);

			_serviceRegistration.unregister();

			AtomicInteger registerCount = new AtomicInteger();

			_registerUpgradeStepRegistrator(
				bundle,
				upgradeStepRegistry -> {
					upgradeStepRegistry.register(
						"0.0.0", "1.0.0", new DummyUpgradeStep());

					if (registerCount.incrementAndGet() == 1) {
						throw new IllegalStateException();
					}
				});

			_assertExecuteAll("All modules were successfully upgraded");

			Set<String> failedBundleSymbolicNames = ReflectionTestUtil.invoke(
				_upgradeExecutor, "getFailedBundleSymbolicNames",
				new Class<?>[0]);

			Assert.assertFalse(
				failedBundleSymbolicNames.toString(),
				failedBundleSymbolicNames.contains(bundleSymbolicName));

			_serviceRegistration.unregister();

			_registerFailingUpgradeStepRegistrator(bundle);

			_assertExecuteAll(
				"The following modules had errors while upgrading:\n\t" +
					bundleSymbolicName);
		}
		finally {
			Release release = _releaseLocalService.fetchRelease(
				bundleSymbolicName);

			if (release != null) {
				_releaseLocalService.deleteRelease(release);
			}
		}
	}

	@Test
	public void testExecuteWithFailedRegistration() throws Exception {
		Bundle bundle = FrameworkUtil.getBundle(UpgradeOSGiCommandsTest.class);

		String bundleSymbolicName = bundle.getSymbolicName();

		try (SafeCloseable safeCloseable =
				PropsValuesTestUtil.swapWithSafeCloseable(
					"UPGRADE_DATABASE_AUTO_RUN", false, false)) {

			_registerRecoveringUpgradeStepRegistrator(bundle);

			ReflectionTestUtil.invoke(
				_upgradeOSGiCommands, "execute",
				new Class<?>[] {String.class, String.class}, bundleSymbolicName,
				"1.0.0");

			_assertRecovered(bundleSymbolicName, "1.0.0");

			_serviceRegistration.unregister();

			_registerRecoveringUpgradeStepRegistrator(bundle);

			Class<?> upgradeExecutorClass = _upgradeExecutor.getClass();

			try (LogCapture logCapture = LoggerTestUtil.configureLog4JLogger(
					upgradeExecutorClass.getName(), LoggerTestUtil.ERROR)) {

				String message = ReflectionTestUtil.invoke(
					_upgradeOSGiCommands, "execute",
					new Class<?>[] {String.class}, bundleSymbolicName);

				Assert.assertEquals(
					"The upgrade of module " + bundleSymbolicName + " failed",
					message);

				List<LogEntry> logEntries = logCapture.getLogEntries();

				Assert.assertEquals(
					logEntries.toString(), 1, logEntries.size());

				LogEntry logEntry = logEntries.get(0);

				Assert.assertEquals(message, logEntry.getMessage());
			}

			Release release = _releaseLocalService.fetchRelease(
				bundleSymbolicName);

			Assert.assertEquals("1.0.0", release.getSchemaVersion());

			ReflectionTestUtil.invoke(
				_upgradeOSGiCommands, "execute", new Class<?>[] {String.class},
				bundleSymbolicName);

			_assertRecovered(bundleSymbolicName, "2.0.0");

			_serviceRegistration.unregister();

			_registerFailingUpgradeStepRegistrator(bundle);

			try (LogCapture logCapture = LoggerTestUtil.configureLog4JLogger(
					upgradeExecutorClass.getName(), LoggerTestUtil.OFF)) {

				String message = ReflectionTestUtil.invoke(
					_upgradeOSGiCommands, "execute",
					new Class<?>[] {String.class, String.class},
					bundleSymbolicName, RandomTestUtil.randomString());

				Assert.assertEquals(
					"The upgrade of module " + bundleSymbolicName + " failed",
					message);
			}
		}
		finally {
			Release release = _releaseLocalService.fetchRelease(
				bundleSymbolicName);

			if (release != null) {
				_releaseLocalService.deleteRelease(release);
			}
		}
	}

	@Test
	public void testExecuteWithUnregisteredModule() {
		String bundleSymbolicName = RandomTestUtil.randomString();

		String expectedMessage =
			"No upgrade processes registered for " + bundleSymbolicName;

		String message = ReflectionTestUtil.invoke(
			_upgradeOSGiCommands, "execute", new Class<?>[] {String.class},
			bundleSymbolicName);

		Assert.assertEquals(expectedMessage, message);

		message = ReflectionTestUtil.invoke(
			_upgradeOSGiCommands, "execute",
			new Class<?>[] {String.class, String.class}, bundleSymbolicName,
			RandomTestUtil.randomString());

		Assert.assertEquals(expectedMessage, message);
	}

	@Test
	public void testListWithFailedRegistration() throws Exception {
		Bundle bundle = FrameworkUtil.getBundle(UpgradeOSGiCommandsTest.class);

		String bundleSymbolicName = bundle.getSymbolicName();

		_registerFailingUpgradeStepRegistrator(bundle);

		_assertList(bundleSymbolicName);

		_serviceRegistration.unregister();

		Release release = _releaseLocalService.addRelease(
			bundleSymbolicName, "1.0.0");

		try (SafeCloseable safeCloseable =
				PropsValuesTestUtil.swapWithSafeCloseable(
					"UPGRADE_DATABASE_AUTO_RUN", false, false)) {

			_registerFailingUpgradeStepRegistrator(bundle);

			Class<?> clazz = _upgradeExecutor.getClass();

			try (LogCapture logCapture = LoggerTestUtil.configureLog4JLogger(
					clazz.getName(), LoggerTestUtil.OFF)) {

				String message = ReflectionTestUtil.invoke(
					_upgradeOSGiCommands, "list", new Class<?>[] {String.class},
					bundleSymbolicName);

				Assert.assertEquals(
					"The upgrade of module " + bundleSymbolicName + " failed",
					message);

				_assertList(bundleSymbolicName);
			}
		}
		finally {
			_releaseLocalService.deleteRelease(release);
		}
	}

	@Test
	public void testListWithUnregisteredModule() {
		String bundleSymbolicName = RandomTestUtil.randomString();

		String message = ReflectionTestUtil.invoke(
			_upgradeOSGiCommands, "list", new Class<?>[] {String.class},
			bundleSymbolicName);

		Assert.assertEquals(
			"No upgrade processes registered for " + bundleSymbolicName,
			message);
	}

	private void _assertExecuteAll(String expectedMessage) {
		String message = ReflectionTestUtil.invoke(
			_upgradeOSGiCommands, "executeAll", new Class<?>[0]);

		Assert.assertTrue(message, message.contains(expectedMessage));
	}

	private void _assertList(String bundleSymbolicName) {
		String message = ReflectionTestUtil.invoke(
			_upgradeOSGiCommands, "list", new Class<?>[0]);

		Assert.assertTrue(
			message,
			message.contains(
				"The upgrade of module " + bundleSymbolicName + " failed"));
	}

	private void _assertRecovered(
		String bundleSymbolicName, String expectedSchemaVersion) {

		Release release = _releaseLocalService.fetchRelease(bundleSymbolicName);

		Assert.assertEquals(expectedSchemaVersion, release.getSchemaVersion());

		Set<String> failedBundleSymbolicNames = ReflectionTestUtil.invoke(
			_upgradeExecutor, "getFailedBundleSymbolicNames", new Class<?>[0]);

		Assert.assertFalse(
			failedBundleSymbolicNames.toString(),
			failedBundleSymbolicNames.contains(bundleSymbolicName));
	}

	private void _assertStepJSONObject(
		Class<?> expectedClass, int expectedPosition,
		JSONObject stepJSONObject) {

		Assert.assertEquals(
			expectedClass.getName(), stepJSONObject.getString("className"));
		Assert.assertTrue(stepJSONObject.isNull("operations"));
		Assert.assertEquals(
			expectedPosition, stepJSONObject.getInt("position"));
		Assert.assertTrue(stepJSONObject.isNull("sourceClassName"));
		Assert.assertTrue(stepJSONObject.isNull("sourceLineNumber"));
	}

	private JSONObject _describe(String... arguments) throws Exception {
		String json = ReflectionTestUtil.invoke(
			_upgradeOSGiCommands, "describe", new Class<?>[] {String[].class},
			new Object[] {arguments});

		return JSONFactoryUtil.createJSONObject(json);
	}

	private String _getDBTypeName() {
		DB db = DBManagerUtil.getDB();

		DBType dbType = db.getDBType();

		return dbType.getName();
	}

	private JSONObject _getEdgeJSONObject(
		JSONArray edgesJSONArray, String fromSchemaVersion,
		String toSchemaVersion) {

		for (int i = 0; i < edgesJSONArray.length(); i++) {
			JSONObject edgeJSONObject = edgesJSONArray.getJSONObject(i);

			if (fromSchemaVersion.equals(
					edgeJSONObject.getString("fromSchemaVersion")) &&
				toSchemaVersion.equals(
					edgeJSONObject.getString("toSchemaVersion"))) {

				return edgeJSONObject;
			}
		}

		throw new AssertionError(edgesJSONArray.toString());
	}

	private JSONObject _getModuleJSONObject(JSONObject jsonObject) {
		JSONArray modulesJSONArray = jsonObject.getJSONArray("modules");

		Assert.assertEquals(
			modulesJSONArray.toString(), 1, modulesJSONArray.length());

		return modulesJSONArray.getJSONObject(0);
	}

	private UpgradeStepRegistrator _registerDescribedUpgradeStepRegistrator(
		Bundle bundle, TestUpgradeProcess testUpgradeProcess) {

		UpgradeStepRegistrator upgradeStepRegistrator = upgradeStepRegistry -> {
			upgradeStepRegistry.register(
				"0.0.0", "1.0.0", new DummyUpgradeStep());
			upgradeStepRegistry.register("1.0.0", "2.0.0", testUpgradeProcess);
		};

		_registerUpgradeStepRegistrator(bundle, upgradeStepRegistrator);

		return upgradeStepRegistrator;
	}

	private void _registerFailingUpgradeStepRegistrator(Bundle bundle) {
		_registerUpgradeStepRegistrator(
			bundle,
			upgradeStepRegistry -> {
				throw new IllegalStateException();
			});
	}

	private void _registerRecoveringUpgradeStepRegistrator(Bundle bundle) {
		AtomicInteger registerCount = new AtomicInteger();

		_registerUpgradeStepRegistrator(
			bundle,
			upgradeStepRegistry -> {
				upgradeStepRegistry.register(
					"0.0.0", "1.0.0", new DummyUpgradeStep());
				upgradeStepRegistry.register(
					"1.0.0", "2.0.0", new DummyUpgradeStep());

				if (registerCount.incrementAndGet() == 1) {
					throw new IllegalStateException();
				}
			});
	}

	private void _registerUpgradeStepRegistrator(
		Bundle bundle, UpgradeStepRegistrator upgradeStepRegistrator) {

		BundleContext bundleContext = bundle.getBundleContext();

		Class<?> clazz = _upgradeExecutor.getClass();

		try (LogCapture logCapture = LoggerTestUtil.configureLog4JLogger(
				clazz.getName(), LoggerTestUtil.OFF)) {

			_serviceRegistration = bundleContext.registerService(
				UpgradeStepRegistrator.class, upgradeStepRegistrator, null);
		}
	}

	@Inject
	private ReleaseLocalService _releaseLocalService;

	private ServiceRegistration<UpgradeStepRegistrator> _serviceRegistration;

	@Inject(
		filter = "component.name=com.liferay.portal.upgrade.internal.executor.UpgradeExecutor",
		type = Inject.NoType.class
	)
	private Object _upgradeExecutor;

	@Inject(
		filter = "component.name=com.liferay.portal.upgrade.internal.release.osgi.commands.UpgradeOSGiCommands",
		type = Inject.NoType.class
	)
	private Object _upgradeOSGiCommands;

	private static class TestPostUpgradeStep implements UpgradeStep {

		@Override
		public void upgrade() {
			_upgradeCount.incrementAndGet();
		}

		private TestPostUpgradeStep(AtomicInteger upgradeCount) {
			_upgradeCount = upgradeCount;
		}

		private final AtomicInteger _upgradeCount;

	}

	private static class TestPreUpgradeStep implements UpgradeStep {

		@Override
		public void upgrade() {
			_upgradeCount.incrementAndGet();
		}

		private TestPreUpgradeStep(AtomicInteger upgradeCount) {
			_upgradeCount = upgradeCount;
		}

		private final AtomicInteger _upgradeCount;

	}

	private static class TestUpgradeProcess extends UpgradeProcess {

		public int getUpgradeCount() {
			return _upgradeCount.get();
		}

		@Override
		protected void doUpgrade() {
			_upgradeCount.incrementAndGet();
		}

		@Override
		protected UpgradeStep[] getPostUpgradeSteps() {
			return new UpgradeStep[] {new TestPostUpgradeStep(_upgradeCount)};
		}

		@Override
		protected UpgradeStep[] getPreUpgradeSteps() {
			return new UpgradeStep[] {new TestPreUpgradeStep(_upgradeCount)};
		}

		private final AtomicInteger _upgradeCount = new AtomicInteger();

	}

}