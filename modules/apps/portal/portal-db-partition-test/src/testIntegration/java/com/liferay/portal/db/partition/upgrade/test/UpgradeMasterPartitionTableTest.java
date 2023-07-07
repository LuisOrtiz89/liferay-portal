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

package com.liferay.portal.db.partition.upgrade.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.db.partition.test.util.BaseDBPartitionTestCase;
import com.liferay.portal.kernel.upgrade.UpgradeProcess;
import com.liferay.portal.upgrade.v7_4_x.UpgradeMasterPartitionTable;

import java.sql.Statement;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Sofía Mendoza Gutiérrez
 */
@RunWith(Arquillian.class)
public class UpgradeMasterPartitionTableTest extends BaseDBPartitionTestCase {

	@BeforeClass
	public static void setUpClass() throws Exception {
		enableDBPartition();

		addDBPartitions();

		insertPartitionRequiredData();
	}

	@AfterClass
	public static void tearDownClass() throws Exception {
		deletePartitionRequiredData();

		removeDBPartitions(false);

		disableDBPartition();
	}

	@Test
	public void testUpgrade() throws Exception {
		createAndPopulateTable(TEST_TABLE_NAME);

		_createViewSQL(TEST_TABLE_NAME);

		UpgradeProcess upgradeProcess = new UpgradeMasterPartitionTable(
			TEST_TABLE_NAME);

		upgradeProcess.upgrade();

		Assert.assertTrue(
			dbInspector.hasTable(
				StringBundler.concat(
					getSchemaName(COMPANY_IDS[0]), StringPool.PERIOD,
					TEST_TABLE_NAME)));

		dropTable(TEST_TABLE_NAME);
	}

	private void _createViewSQL(String viewName) throws Exception {
		try (Statement statement = connection.createStatement()) {
			_defaultSchemaName = connection.getCatalog();

			statement.execute(
				StringBundler.concat(
					"create or replace view ", getSchemaName(COMPANY_IDS[0]),
					StringPool.PERIOD, viewName, " as select * from ",
					_defaultSchemaName, StringPool.PERIOD, viewName));
		}
	}

	private static String _defaultSchemaName;

}