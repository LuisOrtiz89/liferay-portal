/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.dynamic.data.mapping.internal.upgrade.v8_0_0.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.dynamic.data.mapping.constants.DDMTemplateConstants;
import com.liferay.dynamic.data.mapping.model.DDMTemplate;
import com.liferay.dynamic.data.mapping.service.DDMTemplateService;
import com.liferay.dynamic.data.mapping.service.persistence.DDMTemplatePersistence;
import com.liferay.dynamic.data.mapping.service.persistence.DDMTemplateUtil;
import com.liferay.dynamic.data.mapping.service.test.BaseDDMServiceTestCase;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.security.auth.PrincipalThreadLocal;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.security.permission.PermissionCheckerFactoryUtil;
import com.liferay.portal.kernel.security.permission.PermissionThreadLocal;
import com.liferay.portal.kernel.template.TemplateConstants;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.kernel.upgrade.UpgradeProcess;
import com.liferay.portal.kernel.upgrade.UpgradeStep;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.upgrade.registry.UpgradeStepRegistrator;

import java.util.Collections;
import java.util.Objects;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Luis Ortiz
 */
@RunWith(Arquillian.class)
public class DDMJakartaUpgradeProcessTest extends BaseDDMServiceTestCase {

	@ClassRule
	@Rule
	public static final LiferayIntegrationTestRule liferayIntegrationTestRule =
		new LiferayIntegrationTestRule();

	@Before
	public void setUp() throws Exception {
		super.setUp();

		_originalName = PrincipalThreadLocal.getName();
		_originalPermissionChecker =
			PermissionThreadLocal.getPermissionChecker();

		_siteAdminUser = UserTestUtil.addGroupAdminUser(group);

		PrincipalThreadLocal.setName(_siteAdminUser.getUserId());

		PermissionThreadLocal.setPermissionChecker(
			PermissionCheckerFactoryUtil.create(_siteAdminUser));

		_ddmTemplate = _ddmTemplateService.addTemplate(
			RandomTestUtil.randomString(), group.getGroupId(),
			PortalUtil.getClassNameId(DDL_RECORD_CLASS_NAME), 0,
			PortalUtil.getClassNameId(DDL_RECORD_SET_CLASS_NAME),
			Collections.singletonMap(
				LocaleUtil.getSiteDefault(), RandomTestUtil.randomString()),
			null, DDMTemplateConstants.TEMPLATE_TYPE_DISPLAY,
			DDMTemplateConstants.TEMPLATE_MODE_CREATE,
			TemplateConstants.LANG_TYPE_VM, _JAVAX_SCRIPT,
			ServiceContextTestUtil.getServiceContext());

		_ddmTemplatePersistence = DDMTemplateUtil.getPersistence();

		_upgradeStepRegistrator.register(
			(fromSchemaVersionString, toSchemaVersionString, upgradeSteps) -> {
				for (UpgradeStep upgradeStep : upgradeSteps) {
					Class<?> clazz = upgradeStep.getClass();

					if (Objects.equals(
							clazz.getName(),
							"com.liferay.dynamic.data.mapping.internal." +
								"upgrade.v8_0_0.DDMJakartaUpgradeProcess")) {

						_upgradeProcess = (UpgradeProcess)upgradeStep;
					}
				}
			});
	}

	@After
	public void tearDown() throws Exception {
		_ddmTemplateService.deleteTemplate(_ddmTemplate.getTemplateId());

		PermissionThreadLocal.setPermissionChecker(_originalPermissionChecker);

		PrincipalThreadLocal.setName(_originalName);
	}

	@Test
	public void testUpgrade() throws Exception {
		_upgradeProcess.upgrade();

		_ddmTemplatePersistence.clearCache();

		DDMTemplate ddmTemplate = _ddmTemplateService.getTemplate(
			_ddmTemplate.getTemplateId());

		Assert.assertNotNull(ddmTemplate);

		Assert.assertEquals(_JAKARTA_SCRIPT, ddmTemplate.getScript());
	}

	private static final String _JAKARTA_SCRIPT =
		"import jakarta.servlet.test." +
			"DDMJakartaUpgradeProcessTestConfiguration;";

	private static final String _JAVAX_SCRIPT =
		"import javax.servlet.test.DDMJakartaUpgradeProcessTestConfiguration;";

	private DDMTemplate _ddmTemplate;
	private DDMTemplatePersistence _ddmTemplatePersistence;

	@Inject
	private DDMTemplateService _ddmTemplateService;

	private String _originalName;
	private PermissionChecker _originalPermissionChecker;

	@DeleteAfterTestRun
	private User _siteAdminUser;

	private UpgradeProcess _upgradeProcess;

	@Inject(
		filter = "component.name=com.liferay.dynamic.data.mapping.internal.upgrade.registry.DDMServiceUpgradeStepRegistrator"
	)
	private UpgradeStepRegistrator _upgradeStepRegistrator;

}