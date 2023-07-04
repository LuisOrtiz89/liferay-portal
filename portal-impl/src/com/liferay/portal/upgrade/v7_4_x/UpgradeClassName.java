package com.liferay.portal.upgrade.v7_4_x;

import com.liferay.portal.db.partition.DBPartitionUtil;
import com.liferay.portal.kernel.upgrade.UpgradeProcess;

public class UpgradeClassName extends UpgradeProcess {

	@Override
	protected void doUpgrade() throws Exception {
		DBPartitionUtil.replaceViewByTable(tableName, connection);
	}

	private static final String tableName = "ClassName_";
}
