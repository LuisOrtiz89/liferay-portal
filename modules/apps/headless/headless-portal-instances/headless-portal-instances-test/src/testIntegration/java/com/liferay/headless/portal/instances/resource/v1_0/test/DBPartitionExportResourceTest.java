/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.portal.instances.resource.v1_0.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.headless.portal.instances.client.dto.v1_0.DBPartitionExport;
import com.liferay.headless.portal.instances.client.problem.Problem;
import com.liferay.headless.portal.instances.client.resource.v1_0.DBPartitionExportResource;
import com.liferay.petra.lang.SafeCloseable;
import com.liferay.portal.db.partition.util.DBPartitionUtil;
import com.liferay.portal.kernel.dao.db.DB;
import com.liferay.portal.kernel.dao.db.DBManagerUtil;
import com.liferay.portal.kernel.dao.db.DBType;
import com.liferay.portal.kernel.dao.jdbc.DataAccess;
import com.liferay.portal.kernel.instance.PortalInstancePool;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.security.auth.CompanyThreadLocal;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.test.util.CompanyTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.PropsValues;
import com.liferay.portal.test.log.LogCapture;
import com.liferay.portal.test.log.LoggerTestUtil;
import com.liferay.portal.test.rule.Inject;

import java.sql.Connection;
import java.sql.PreparedStatement;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Alberto Chaparro
 */
@RunWith(Arquillian.class)
public class DBPartitionExportResourceTest
	extends BaseDBPartitionExportResourceTestCase {

	@BeforeClass
	public static void setUpClass() throws Exception {
		_company = CompanyTestUtil.addCompany();
	}

	@AfterClass
	public static void tearDownClass() throws Exception {
		_companyLocalService.deleteCompany(_company.getCompanyId());
	}

	@Before
	@Override
	public void setUp() throws Exception {
		super.setUp();

		try (SafeCloseable safeCloseable =
				CompanyThreadLocal.setCompanyIdWithSafeCloseable(
					PortalInstancePool.getDefaultCompanyId())) {

			Company company = _companyLocalService.getCompany(
				PortalInstancePool.getDefaultCompanyId());

			User user = UserTestUtil.getAdminUser(company.getCompanyId());

			dbPartitionExportResource = DBPartitionExportResource.builder(
			).authentication(
				user.getEmailAddress(), PropsValues.DEFAULT_ADMIN_PASSWORD
			).endpoint(
				company.getVirtualHostname(),
				PortalUtil.getPortalServerPort(false), "http"
			).locale(
				LocaleUtil.getDefault()
			).build();
		}
	}

	@Override
	@Test
	public void testPostDBPartitionExport() throws Exception {
		DB db = DBManagerUtil.getDB();

		Assume.assumeTrue(db.isSupportsDBPartition());

		_testPostDBPartitionExportMissingRequiredFields();
		_testPostDBPartitionExportSuccess();
		_testPostDBPartitionExportWithNonexistentPortalInstance();
		_testPostDBPartitionExportWithoutOmniadminPermission();
	}

	private DBPartitionExportResource _createUserDBPartitionExportResource()
		throws Exception {

		User user = UserTestUtil.addUser(testCompany, "test");

		return DBPartitionExportResource.builder(
		).authentication(
			user.getEmailAddress(), "test"
		).endpoint(
			testCompany.getVirtualHostname(),
			PortalUtil.getPortalServerPort(false), "http"
		).locale(
			LocaleUtil.getDefault()
		).build();
	}

	private void _dropExportedSchema(long companyId) throws Exception {
		DB db = DBManagerUtil.getDB();

		String sql =
			"drop schema if exists " +
				DBPartitionUtil.getExportedPartitionName(companyId);

		if (db.getDBType() == DBType.POSTGRESQL) {
			sql = sql + " cascade";
		}

		try (Connection connection = DataAccess.getConnection();

			PreparedStatement preparedStatement = connection.prepareStatement(
				sql)) {

			preparedStatement.executeUpdate();
		}
	}

	private void _testPostDBPartitionExportMissingRequiredFields()
		throws Exception {

		try {
			dbPartitionExportResource.postDBPartitionExport(
				new DBPartitionExport());

			Assert.fail();
		}
		catch (Problem.ProblemException problemException) {
			Problem problem = problemException.getProblem();

			Assert.assertEquals("BAD_REQUEST", problem.getStatus());
		}
	}

	private void _testPostDBPartitionExportSuccess() throws Exception {
		long companyId = _company.getCompanyId();

		try {
			DBPartitionExport dbPartitionExport =
				dbPartitionExportResource.postDBPartitionExport(
					_toDBPartitionExport(_company.getWebId()));

			Assert.assertEquals(
				DBPartitionUtil.getExportedPartitionName(companyId),
				dbPartitionExport.getExportedPartitionName());
			Assert.assertEquals(
				_company.getWebId(), dbPartitionExport.getPortalInstanceId());
		}
		finally {
			_dropExportedSchema(companyId);
		}
	}

	private void _testPostDBPartitionExportWithNonexistentPortalInstance()
		throws Exception {

		try (LogCapture logCapture = LoggerTestUtil.configureLog4JLogger(
				"com.liferay.portal.vulcan.internal.jaxrs.exception.mapper." +
					"WebApplicationExceptionMapper",
				LoggerTestUtil.ERROR)) {

			dbPartitionExportResource.postDBPartitionExport(
				_toDBPartitionExport(RandomTestUtil.randomString()));

			Assert.fail();
		}
		catch (Problem.ProblemException problemException) {
			Problem problem = problemException.getProblem();

			Assert.assertEquals("NOT_FOUND", problem.getStatus());
			Assert.assertNull(problem.getTitle());
		}
	}

	private void _testPostDBPartitionExportWithoutOmniadminPermission()
		throws Exception {

		DBPartitionExportResource userDBPartitionExportResource =
			_createUserDBPartitionExportResource();

		try {
			userDBPartitionExportResource.postDBPartitionExport(
				_toDBPartitionExport(_company.getWebId()));

			Assert.fail();
		}
		catch (Problem.ProblemException problemException) {
			Problem problem = problemException.getProblem();

			Assert.assertEquals("FORBIDDEN", problem.getStatus());
		}
	}

	private DBPartitionExport _toDBPartitionExport(String portalInstanceId) {
		DBPartitionExport dbPartitionExport = new DBPartitionExport();

		dbPartitionExport.setPortalInstanceId(portalInstanceId);

		return dbPartitionExport;
	}

	private static Company _company;

	@Inject
	private static CompanyLocalService _companyLocalService;

}