/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.tools.db.partition.migration.validator;

import com.liferay.petra.function.UnsafeConsumer;
import com.liferay.petra.function.UnsafeRunnable;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.version.Version;
import com.liferay.portal.tools.db.partition.migration.validator.util.BaseTestCase;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import java.net.URL;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import org.skyscreamer.jsonassert.JSONAssert;

/**
 * @author Luis Ortiz
 */
public class DBPartitionMigrationValidatorTest extends BaseTestCase {

	@Before
	public void setUp() {
		_errorTempFile = new File(StringUtil.randomString());
		_outputTempFile = new File(StringUtil.randomString());
	}

	@After
	public void tearDown() {
		_errorTempFile.delete();
		_outputTempFile.delete();
	}

	@Test
	public void testExportDefaultDatabase() throws Exception {
		_testExport(
			Collections.singletonList(RandomTestUtil.randomLong()), true);
	}

	@Test
	public void testExportDefaultDatabaseWithMultipleCompanies()
		throws Exception {

		_testExport(
			Arrays.asList(
				RandomTestUtil.randomLong(), RandomTestUtil.randomLong()),
			true);
	}

	@Test
	public void testExportNondefaultDatabase() throws Exception {
		_testExport(
			Collections.singletonList(RandomTestUtil.randomLong()), false);
	}

	@Test
	public void testExportNondefaultDatabaseWithMultipleCompanies()
		throws Exception {

		_testExport(
			Arrays.asList(
				RandomTestUtil.randomLong(), RandomTestUtil.randomLong()),
			false);
	}

	@Test
	public void testValidateFailure() throws Exception {
		String[] messages = {
			"[ERROR] Company ID 3007447931789165977 already exists in the " +
				"target database",
			"[ERROR] Module com.liferay.address.impl needs to be verified in " +
				"the source database before the migration",
			"[ERROR] Module com.liferay.comment.page.comments.web has a " +
				"failed release state in the source database",
			"[ERROR] Module com.liferay.exportimport.service needs to be " +
				"installed in the source database before the migration",
			"[ERROR] Module com.liferay.knowledge.base.web needs to be " +
				"upgraded in the target database before the migration",
			"[ERROR] Module com.liferay.organizations.service has a failed " +
				"release state in the target database",
			"[ERROR] Module com.liferay.organizations.service needs to be " +
				"verified in the target database before the migration",
			"[ERROR] Module com.liferay.wiki.web needs to be upgraded in the " +
				"source database before the migration",
			"[WARN] Company name Liferay DXP already exists in the target " +
				"database. You must set a different value in " +
					"DBPartitionInsertVirtualInstanceConfiguration.config.",
			"[WARN] Module com.liferay.asset.publisher.web is not present in " +
				"the source database",
			"[WARN] Module com.liferay.license.manager.web is not present in " +
				"the target database",
			"[WARN] Table CommercePriceList is not present in the source " +
				"database",
			"[WARN] Table DDMTemplate is not present in the target database",
			"[WARN] Virtual host localhost already exists in the target " +
				"database. You must set a different value in " +
					"DBPartitionInsertVirtualInstanceConfiguration.config.",
			"[WARN] Web ID liferay.com already exists in the target " +
				"database. You must set a different value in " +
					"DBPartitionInsertVirtualInstanceConfiguration.config."
		};

		_testValidate(
			"source-failure.json", "target-failure.json",
			runtimeException -> {
				Assert.assertEquals("1", runtimeException.getMessage());

				String outputFileContent = new String(
					Files.readAllBytes(_outputTempFile.toPath()), StringPool.UTF8);

				for (String message : messages) {
					Assert.assertTrue(outputFileContent.contains(message));
				}
			},
			() -> {
			});
	}

	@Test
	public void testValidateSuccess() throws Exception {
		_testValidate(
			"source-success.json", "target-success.json",
			runtimeException -> Assert.assertEquals(
				"0", runtimeException.getMessage()),
			() -> {
				String errorFileContent = new String(
					Files.readAllBytes(_errorTempFile.toPath()), StringPool.UTF8);

				String outputFileContent = new String(
					Files.readAllBytes(_outputTempFile.toPath()), StringPool.UTF8);

				Assert.assertTrue(
					errorFileContent.isEmpty());
				Assert.assertTrue(
					outputFileContent.isEmpty());
			});
	}

	@Test
	public void testValidateTargetNondefaultPartition() throws Exception {
		_testValidate(
			"source-success.json", "target-nondefault.json",
			runtimeException -> {
				String errorFileContent = new String(
					Files.readAllBytes(_errorTempFile.toPath()), StringPool.UTF8);

				String outputFileContent = new String(
					Files.readAllBytes(_outputTempFile.toPath()), StringPool.UTF8);

				Assert.assertEquals("1", runtimeException.getMessage());
				Assert.assertTrue(
					errorFileContent.contains(
						"Target is not the default partition"
					));
				Assert.assertTrue(
					outputFileContent.isEmpty());
			},
			() -> {
			});
	}

	@Rule
	public TemporaryFolder temporaryFolder = new TemporaryFolder();

	private String _getPathString(String fileName) throws Exception {
		URL url = DBPartitionMigrationValidatorTest.class.getResource(
			"dependencies/" + fileName);

		Path path = Paths.get(url.toURI());

		return path.toString();
	}

	private static void _mockDatabase(
			List<Company> companies, List<Long> companyIds,
			List<Long> companyInfoIds, boolean defaultPartition,
			String password, List<Release> releases, String schemaName,
			List<String> tableNames, String url, String user)
		throws Exception {

		mockGetColumns(tableNames);
		mockGetCompanies(companies);
		mockGetCompanyIds(companyIds);
		mockGetCompanyInfos(companyInfoIds);
		mockGetConnection(
			password, StringUtil.replace(url, "lportal", schemaName), user);
		mockGetReleases(releases);
		mockGetTables(defaultPartition);
	}

	private String _read(File file) throws Exception {
		StringBuilder sb = new StringBuilder();

		try (BufferedReader bufferedReader = new BufferedReader(
				new FileReader(file))) {

			String line = null;

			while ((line = bufferedReader.readLine()) != null) {
				sb.append(line);
			}
		}

		return sb.toString();
	}

	private void _testExport(List<Long> companyIds, boolean defaultPartition)
		throws Exception {

		File outputDirectory = temporaryFolder.newFolder();

		try {
			List<String> args = new ArrayList<>();
			args.add("export");
			args.add("--jdbc-url");
			args.add(_url);
			args.add("--output-dir");
			args.add(outputDirectory.getAbsolutePath());
			args.add("--password");
			args.add(_password);
			args.add("--schema-name");
			args.add(_schemaName);
			args.add("--user");
			args.add(_user);

			List<String> jvmArgs = new ArrayList<>();
			jvmArgs.add("-D" + _COMPANY_IDS_PROPERTY_NAME + "=" + companyIds.toString());
			jvmArgs.add("-D" + _DEFAULT_PARTITION_PROPERTY_NAME + "=" + defaultPartition);

			_callDBPartitionMigrationValidatorTool(jvmArgs, args);
		}
		catch (RuntimeException runtimeException) {
			String errorFileContent = new String(
				Files.readAllBytes(_errorTempFile.toPath()), StringPool.UTF8);

			if (companyIds.size() > 1) {
				Assert.assertTrue(
					errorFileContent.contains(
						"Database schema has to have a single company or " +
							"database partitioning must be enabled"
					));
				Assert.assertEquals("1", runtimeException.getMessage());

				File[] files = outputDirectory.listFiles();

				Assert.assertEquals(Arrays.toString(files), 0, files.length);

				return;
			}

			Assert.assertEquals("0", runtimeException.getMessage());
		}
		finally {
			System.clearProperty(_COMPANY_IDS_PROPERTY_NAME);
			System.clearProperty(_DEFAULT_PARTITION_PROPERTY_NAME);
		}

		File[] files = outputDirectory.listFiles();

		Assert.assertEquals(Arrays.toString(files), 1, files.length);

		String content = _read(files[0]);

		JSONAssert.assertEquals(
			new JSONObject(
			).put(
				"companies", new JSONArray(_companies)
			).toString(),
			content, false);
		JSONAssert.assertEquals(
			new JSONObject(
			).put(
				"exportedCompanyDefault", defaultPartition
			).toString(),
			content, false);

		Long exportedCompanyId = null;

		if (companyIds.size() == 1) {
			exportedCompanyId = companyIds.get(0);
		}

		JSONAssert.assertEquals(
			new JSONObject(
			).put(
				"exportedCompanyId", exportedCompanyId
			).toString(),
			content, false);

		JSONAssert.assertEquals(
			new JSONObject(
			).put(
				"releases", new JSONArray(_releases)
			).toString(),
			content, false);
		JSONAssert.assertEquals(
			new JSONObject(
			).put(
				"tableNames", new JSONArray(Arrays.asList("Table1", "Table2"))
			).toString(),
			content, false);
	}

	private void _testValidate(
			String sourceFileName, String targetFileName,
			UnsafeConsumer<RuntimeException, Exception> unsafeConsumer,
			UnsafeRunnable<Exception> unsafeRunnable)
		throws Exception {

		try {
			List<String> args = new ArrayList<>();
			args.add("validate");
			args.add("--source-file");
			args.add(_getPathString(sourceFileName));
			args.add("--target-file");
			args.add( _getPathString(targetFileName));

			List<String> jvmArgs = new ArrayList<>();

			_callDBPartitionMigrationValidatorTool(jvmArgs, args);
		}
		catch (RuntimeException runtimeException) {
			unsafeConsumer.accept(runtimeException);
		}

		unsafeRunnable.run();
	}

	private void _callDBPartitionMigrationValidatorTool(List<String> jvmArgs, List<String> args)
		throws Exception {
		String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
		String classPath = System.getProperty("java.class.path");
		String className = MockedDBPartitionMigrationValidator.class.getName();

		List<String> command = new ArrayList<>();
		command.add(javaBin);
		command.addAll(jvmArgs);
		command.add("-cp");
		command.add(classPath);
		command.add(className);
		command.addAll(args);

		ProcessBuilder builder = new ProcessBuilder(command)
			.redirectOutput(_outputTempFile)
			.redirectError(_errorTempFile);

		Process process = builder.start();
		process.waitFor();

		throw new RuntimeException(String.valueOf(process.exitValue()));
	}

	private File _errorTempFile;
	private File _outputTempFile;

	private static final List<Company> _companies = Arrays.asList(
		new Company(
			12345L, "test.com",
			"test.com", "test.com"),
		new Company(
			54321L, "test.net",
			"test.net", "test.net"));
	private static final String _password = "secretPassword";
	private static final List<Release> _releases = Arrays.asList(
		new Release(Version.parseVersion("14.2.4"), "module1", 0, true),
		new Release(Version.parseVersion("2.0.1"), "module2", 1, false));
	private static final String _schemaName = "schemaNameTest";
	private static final String _url = "jdbc:mysql://localhost:3306/lportal?useUnicode=true";
	private static final String _user = "secretUser";

	private static final String _COMPANY_IDS_PROPERTY_NAME = "dbpartitionmigrationvalidatortest.companies";
	private static final String _DEFAULT_PARTITION_PROPERTY_NAME = "dbpartitionmigrationvalidatortest.defaultPartition";

	public static class MockedDBPartitionMigrationValidator {

		public static void main(String[] args) throws Exception {
			String companyIdsString = System.getProperty(_COMPANY_IDS_PROPERTY_NAME);

			if (companyIdsString != null) {
				String[] companyIdsArray = companyIdsString.substring(1,
					companyIdsString.length() - 1).split(", ");

				boolean defaultPartition = Boolean.valueOf(
					System.getProperty(_DEFAULT_PARTITION_PROPERTY_NAME));

				List<Long> companyIds = new ArrayList<>();
				for (String companyId : companyIdsArray) {
					companyIds.add(Long.valueOf(companyId));
				}

				_mockDatabase(
					_companies, companyIds, companyIds, defaultPartition,
					_password,
					_releases, _schemaName,
					Arrays.asList(
						"Company", "Object_x_" + companyIds.get(0), "Table1", "Table2"),
					_url, _user);
			}

			DBPartitionMigrationValidator.main(args);
		}
	}
	
}