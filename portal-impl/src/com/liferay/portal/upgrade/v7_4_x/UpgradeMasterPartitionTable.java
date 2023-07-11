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

package com.liferay.portal.upgrade.v7_4_x;

import com.liferay.portal.db.partition.DBPartitionUtil;
import com.liferay.portal.kernel.upgrade.UpgradeProcess;

/**
 * @author Sofía Mendoza Gutiérrez
 */
public class UpgradeMasterPartitionTable extends UpgradeProcess {

	public UpgradeMasterPartitionTable(String tableName) {
		_TABLE_NAME = tableName;
	}

	@Override
	protected void doUpgrade() throws Exception {
		DBPartitionUtil.replaceViewByTable(connection, _TABLE_NAME);
	}

	private static String _TABLE_NAME;
}