/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.classname.service.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.portal.kernel.model.ClassName;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.model.ModelHints;
import com.liferay.portal.kernel.model.ModelHintsUtil;
import com.liferay.portal.kernel.service.ClassNameLocalService;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.service.CompanyLocalServiceUtil;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.util.CompanyTestUtil;
import com.liferay.portal.model.DefaultModelHintsImpl;
import com.liferay.portal.service.impl.ClassNameLocalServiceImpl;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;

import java.util.Arrays;
import java.util.List;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Sofía Mendoza Gutiérrez
 */
@RunWith(Arquillian.class)
public class ClassNameLocalServiceTest {

	@ClassRule
	@Rule
	public static final LiferayIntegrationTestRule liferayIntegrationTestRule =
		new LiferayIntegrationTestRule();

	@BeforeClass
	public static void setUpClass() throws Exception {
		_company1 = CompanyTestUtil.addCompany();
		_company2 = CompanyTestUtil.addCompany();

		_emptyClassName = ReflectionTestUtil.getFieldValue(
			ClassNameLocalServiceImpl.class, "_nullClassName");
	}

	@AfterClass
	public static void tearDownClass() throws Exception {
		CompanyLocalServiceUtil.deleteCompany(_company1);
		CompanyLocalServiceUtil.deleteCompany(_company2);
	}

	@Test
	public void testAddClassName() {
		_classNameLocalService.addClassName(_CLASS_NAME_VALUE);

		try {
			_companyLocalService.forEachCompanyId(
				companyId -> Assert.assertNotEquals(
					_emptyClassName,
					_classNameLocalService.fetchClassName(_CLASS_NAME_VALUE)));
		}
		finally {
			_classNameLocalService.deleteClassName(
				_classNameLocalService.getClassName(_CLASS_NAME_VALUE));
		}
	}

	@Test
	public void testCheckClassNames() {
		ModelHintsUtil modelHintsUtil = new ModelHintsUtil();

		ModelHints originalModelHints = modelHintsUtil.getModelHints();

		try {
			modelHintsUtil.setModelHints(new ClassNameModelHints());

			_classNameLocalService.checkClassNames();

			_companyLocalService.forEachCompanyId(
				companyId -> Assert.assertNotEquals(
					_emptyClassName,
					_classNameLocalService.fetchClassName(_CLASS_NAME_VALUE)));
		}
		finally {
			_classNameLocalService.deleteClassName(
				_classNameLocalService.getClassName(_CLASS_NAME_VALUE));

			modelHintsUtil.setModelHints(originalModelHints);
		}
	}

	@Test
	public void testDeleteClassName() {
		_classNameLocalService.addClassName(_CLASS_NAME_VALUE);

		_classNameLocalService.checkClassNames();

		_classNameLocalService.deleteClassName(
			_classNameLocalService.getClassName(_CLASS_NAME_VALUE));

		_companyLocalService.forEachCompanyId(
			companyId -> Assert.assertEquals(
				_emptyClassName,
				_classNameLocalService.fetchClassName(_CLASS_NAME_VALUE)));
	}

	private static final String _CLASS_NAME_VALUE = "class.name.test";

	@Inject
	private static ClassNameLocalService _classNameLocalService;

	private static Company _company1;
	private static Company _company2;

	@Inject
	private static CompanyLocalService _companyLocalService;

	private static ClassName _emptyClassName;

	private class ClassNameModelHints extends DefaultModelHintsImpl {

		@Override
		public List<String> getModels() {
			return Arrays.asList(_CLASS_NAME_VALUE);
		}

	}

}