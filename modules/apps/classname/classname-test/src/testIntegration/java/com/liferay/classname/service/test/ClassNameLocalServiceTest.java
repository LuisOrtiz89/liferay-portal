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

package com.liferay.classname.service.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.model.ModelHints;
import com.liferay.portal.kernel.model.ModelHintsUtil;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.service.CompanyLocalServiceUtil;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.util.CompanyTestUtil;
import com.liferay.portal.service.impl.ClassNameLocalServiceImpl;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;

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
	}

	@AfterClass
	public static void tearDownClass() throws Exception {
		CompanyLocalServiceUtil.deleteCompany(_company1);
		CompanyLocalServiceUtil.deleteCompany(_company2);
	}

	@Test
	public void testAddClassName() {
		_classNameLocalServiceImpl.addClassName("Test");

		try {
			_companyLocalService.forEachCompanyId(
				companyId -> Assert.assertNotNull(
					_classNameLocalServiceImpl.fetchClassName("Test")));
		}
		finally {
			_classNameLocalServiceImpl.deleteClassName(
				_classNameLocalServiceImpl.getClassName("Test"));
		}
	}

	@Test
	public void testCheckClassNames() {
		ModelHintsUtil modelHintsUtil = new ModelHintsUtil();

		ModelHints originalModelHints = modelHintsUtil.getModelHints();

		try {
			ReflectionTestUtil.setFieldValue(
				modelHintsUtil, "_modelHints", _classNameModelHints);

			_classNameLocalServiceImpl.checkClassNames();

			_companyLocalService.forEachCompanyId(
				companyId -> Assert.assertNotNull(
					_classNameLocalServiceImpl.fetchClassName("Test")));
		}
		finally {
			_classNameLocalServiceImpl.deleteClassName(
				_classNameLocalServiceImpl.getClassName("Test"));

			ReflectionTestUtil.setFieldValue(
				modelHintsUtil, "_modelHints", originalModelHints);
		}
	}

	@Test
	public void testDeleteClassName() {
		_classNameLocalServiceImpl.addClassName("Test");

		_classNameLocalServiceImpl.deleteClassName(
			_classNameLocalServiceImpl.getClassName("Test"));

		_companyLocalService.forEachCompanyId(
			companyId -> Assert.assertNull(
				_classNameLocalServiceImpl.fetchClassName("Test")));
	}

	@Inject
	private static Company _company1;

	@Inject
	private static Company _company2;

	@Inject
	private static CompanyLocalService _companyLocalService;

	@Inject
	private ClassNameLocalServiceImpl _classNameLocalServiceImpl;

	@Inject
	private final ClassNameModelHints _classNameModelHints =
		new ClassNameModelHints();

}