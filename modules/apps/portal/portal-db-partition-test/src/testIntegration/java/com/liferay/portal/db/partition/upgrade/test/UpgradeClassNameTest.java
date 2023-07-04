package com.liferay.portal.db.partition.upgrade.test;

import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.db.partition.test.util.BaseDBPartitionTestCase;
import com.liferay.portal.kernel.upgrade.UpgradeProcess;
import com.liferay.portal.upgrade.v7_4_x.UpgradeClassName;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.Statement;

public class UpgradeClassNameTest extends BaseDBPartitionTestCase {

	@BeforeClass
	public static void setUpClass() throws Exception {
		enableDBPartition();

		createTable(TEST_TABLE_NAME);

		addDBPartitions();

		insertPartitionRequiredData();
	}

	@AfterClass
	public static void tearDownClass() throws Exception {
		deletePartitionRequiredData();

		removeDBPartitions(false);

		dropTable(TEST_TABLE_NAME);

		disableDBPartition();
	}

	@Test
	public void testUpgrade() throws Exception {
		createAndPopulateTable(TEST_TABLE_NAME);

		_createViewSQL(TEST_TABLE_NAME);

		UpgradeProcess upgradeProcess = new UpgradeClassName();

		upgradeProcess.upgrade();

		boolean exists = true;

		try (Statement statement = connection.createStatement()) {
			_defaultSchemaName = connection.getCatalog();

			exists = statement.execute(StringBundler.concat("select * from ",
				getSchemaName(COMPANY_IDS[0]), StringPool.PERIOD,
				TEST_TABLE_NAME));
		}

		Assert.assertFalse(exists);
	}

	private void _createViewSQL (String viewName) throws Exception {
		try (Statement statement = connection.createStatement()) {
			_defaultSchemaName = connection.getCatalog();

			statement.execute(StringBundler.concat("create or replace view ",
				getSchemaName(COMPANY_IDS[0]), StringPool.PERIOD,
				viewName, " as select * from ", _defaultSchemaName,
				StringPool.PERIOD, viewName));
		}
	}

	private static final String tableName = "ClassName_";
	private static String _defaultSchemaName;
}