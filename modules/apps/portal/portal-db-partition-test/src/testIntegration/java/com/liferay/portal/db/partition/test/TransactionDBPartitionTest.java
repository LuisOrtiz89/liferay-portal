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

package com.liferay.portal.db.partition.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.journal.constants.JournalFolderConstants;
import com.liferay.journal.model.JournalArticle;
import com.liferay.journal.service.JournalArticleLocalService;
import com.liferay.journal.test.util.JournalTestUtil;
import com.liferay.portal.db.partition.DBPartitionUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.GroupConstants;
import com.liferay.portal.kernel.security.auth.CompanyThreadLocal;
import com.liferay.portal.kernel.service.CompanyLocalServiceUtil;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.kernel.util.Time;
import com.liferay.portal.test.rule.Inject;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Luis Ortiz
 */
@RunWith(Arquillian.class)
public class TransactionDBPartitionTest extends BaseDBPartitionTestCase {

	@BeforeClass
	public static void setUpClass() throws Exception {
		enableDBPartition();

		for (long companyId : COMPANY_IDS) {
			CompanyLocalServiceUtil.addCompany(
				companyId,
				String.valueOf(companyId),
				RandomTestUtil.randomString(),
				companyId + ".com",
				0,
				true);
		}

	}

	@AfterClass
	public static void tearDownClass() throws Exception {

		for (long companyId : COMPANY_IDS) {
			CompanyLocalServiceUtil.deleteCompany(companyId);
		}

		disableDBPartition();
	}

	@AfterClass
	public void tearDown() {
		_groupIds = new ConcurrentHashMap<>();
	}


	@Test
	public void testApplyServiceForEachCompany() throws Exception {

		DBPartitionUtil.forEachCompanyId(
			companyId -> {

				Group group = null;

				group = GroupTestUtil.addGroup(
					CompanyThreadLocal.getCompanyId(),
					UserTestUtil.getAdminUser(CompanyThreadLocal.getCompanyId()).getUserId(),
					JournalFolderConstants.DEFAULT_PARENT_FOLDER_ID);

				_groupIds.put(companyId, group);

				ServiceContext serviceContext =
					ServiceContextTestUtil.getServiceContext(group.getGroupId());

				Date expirationDate = new Date(System.currentTimeMillis() + 1 * Time.MINUTE);

				JournalTestUtil.addArticle(
					group.getGroupId(),
					"1234",
					"1234",
					expirationDate,
					serviceContext
				);
			});

		DBPartitionUtil.forEachCompanyId(
			companyId -> {
				Group group = _groupLocalService.getGroup(companyId, GroupConstants.GUEST);
				JournalArticle journalArticle =
					_journalArticleLocalService.getArticleByUrlTitle(_groupIds.get(companyId).getGroupId(), "1234");

				Assert.assertNotNull(journalArticle);
			}
		);
	}

	@Inject
	private GroupLocalService _groupLocalService;

	@Inject
	private JournalArticleLocalService _journalArticleLocalService;

	private ConcurrentHashMap<Long, Group> _groupIds = new ConcurrentHashMap<>();
}
