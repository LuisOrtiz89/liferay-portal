/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.upgrade.data.cleanup.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.dynamic.data.mapping.service.DDMStructureLocalService;
import com.liferay.expando.kernel.model.ExpandoColumnConstants;
import com.liferay.expando.kernel.service.ExpandoColumnLocalService;
import com.liferay.expando.kernel.service.ExpandoTableLocalService;
import com.liferay.journal.constants.JournalFolderConstants;
import com.liferay.journal.model.JournalArticle;
import com.liferay.journal.model.JournalFeed;
import com.liferay.journal.service.JournalArticleLocalService;
import com.liferay.journal.test.util.JournalTestUtil;
import com.liferay.journal.test.util.search.JournalArticleBlueprintBuilder;
import com.liferay.journal.test.util.search.JournalArticleContent;
import com.liferay.journal.test.util.search.JournalArticleSearchFixture;
import com.liferay.journal.test.util.search.JournalArticleTitle;
import com.liferay.layout.test.util.LayoutTestUtil;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.model.ClassName;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.model.ResourcePermission;
import com.liferay.portal.kernel.service.ClassNameLocalService;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.service.ResourcePermissionLocalService;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.DataGuard;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.search.test.util.ExpandoTableSearchFixture;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.upgrade.data.cleanup.JournalDataCleanupPreupgradeProcess;

import java.util.Collections;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Luis Ortiz
 */
@DataGuard(autoDelete = false, scope = DataGuard.Scope.METHOD)
@RunWith(Arquillian.class)
public class JournalDataCleanupPreupgradeProcessTest
	extends JournalDataCleanupPreupgradeProcess {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new LiferayIntegrationTestRule();

	@Before
	public void setUp() throws Exception {
		_classNames = _classNameLocalService.getClassNames(
			QueryUtil.ALL_POS, QueryUtil.ALL_POS);
		_resourcePermissions =
			_resourcePermissionLocalService.getResourcePermissions(
				QueryUtil.ALL_POS, QueryUtil.ALL_POS);

		_expandoTableSearchFixture = new ExpandoTableSearchFixture(
			_classNameLocalService, _expandoColumnLocalService,
			_expandoTableLocalService);
		_journalArticleSearchFixture = new JournalArticleSearchFixture(
			_ddmStructureLocalService, _journalArticleLocalService, _portal);
	}

	@After
	public void tearDown() throws Exception {
		List<ClassName> classNames = ListUtil.remove(
			_classNameLocalService.getClassNames(
				QueryUtil.ALL_POS, QueryUtil.ALL_POS),
			_classNames);

		for (ClassName className : classNames) {
			_classNameLocalService.deleteClassName(className);
		}

		List<ResourcePermission> resourcePermissions = ListUtil.remove(
			_resourcePermissionLocalService.getResourcePermissions(
				QueryUtil.ALL_POS, QueryUtil.ALL_POS),
			_resourcePermissions);

		for (ResourcePermission resourcePermission : resourcePermissions) {
			_resourcePermissionLocalService.deleteResourcePermission(
				resourcePermission);
		}
	}

	@Test
	public void testUpgrade() throws Exception {
		Group group = GroupTestUtil.addGroup();

		JournalArticle journalArticle1 = JournalTestUtil.addArticle(
			group.getGroupId(), JournalFolderConstants.DEFAULT_PARENT_FOLDER_ID,
			Collections.emptyMap());

		Layout layout = LayoutTestUtil.addTypeContentLayout(group);

		JournalFeed journalFeed = JournalTestUtil.addFeed(
			group.getGroupId(), layout.getPlid(), RandomTestUtil.randomString(),
			journalArticle1.getDDMStructureId(),
			journalArticle1.getDDMTemplateKey(),
			journalArticle1.getDDMTemplateKey());

		JournalArticle journalArticle2 = addJournalArticleWithExpandos(
			RandomTestUtil.randomString(), group);

		runSQL(
			"delete from JournalArticle where articleId = '" +
				journalArticle1.getArticleId() + "'");
		runSQL(
			"delete from JournalArticle where articleId = '" +
				journalArticle2.getArticleId() + "'");
		runSQL(
			"delete from JournalFeed where feedId = '" +
				journalFeed.getFeedId() + "'");
		runSQL("delete from Layout where plid = " + layout.getPlid());

		upgrade();

		_groupLocalService.deleteGroup(group);
	}

	private JournalArticle addJournalArticleWithExpandos(
			String expandoValue, Group group)
		throws Exception {

		UserTestUtil.setUser(TestPropsValues.getUser());

		_expandoTableSearchFixture.addExpandoColumn(
			JournalArticle.class, ExpandoColumnConstants.INDEX_TYPE_KEYWORD,
			_EXPANDO_COLUMN);

		return _journalArticleSearchFixture.addArticle(
			JournalArticleBlueprintBuilder.builder(
			).expandoBridgeAttributes(
				Collections.singletonMap(_EXPANDO_COLUMN, expandoValue)
			).groupId(
				group.getGroupId()
			).journalArticleContent(
				new JournalArticleContent() {
					{
						put(LocaleUtil.US, "alpha");

						setDefaultLocale(LocaleUtil.US);
						setName("content");
					}
				}
			).journalArticleTitle(
				new JournalArticleTitle() {
					{
						put(LocaleUtil.US, "gamma");
					}
				}
			).build());
	}

	private static final String _EXPANDO_COLUMN = RandomTestUtil.randomString();

	private static List<ClassName> _classNames;

	@Inject
	private static DDMStructureLocalService _ddmStructureLocalService;

	@Inject
	private static Portal _portal;

	private static List<ResourcePermission> _resourcePermissions;

	@Inject
	private ClassNameLocalService _classNameLocalService;

	@Inject
	private ExpandoColumnLocalService _expandoColumnLocalService;

	@Inject
	private ExpandoTableLocalService _expandoTableLocalService;

	private ExpandoTableSearchFixture _expandoTableSearchFixture;

	@Inject
	private GroupLocalService _groupLocalService;

	@Inject
	private JournalArticleLocalService _journalArticleLocalService;

	private JournalArticleSearchFixture _journalArticleSearchFixture;

	@Inject
	private ResourcePermissionLocalService _resourcePermissionLocalService;

}