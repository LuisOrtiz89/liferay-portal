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

package com.liferay.portal.model.impl;

import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.model.ShardedModel;
import com.liferay.portal.kernel.security.auth.CompanyThreadLocal;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.PropsUtil;

/**
 * @author Brian Wing Shun Chan
 */
public class ClassNameImpl extends ClassNameBaseImpl implements ShardedModel {

	public ClassNameImpl() {
		setValue(StringPool.BLANK);
	}

	@Override
	public long getCompanyId() {
		if (_DATABASE_PARTITION_ENABLED) {
			return CompanyThreadLocal.getCompanyId();
		}

		return 0;
	}

	@Override
	public void setCompanyId(long companyId) {
	}

	private static final boolean _DATABASE_PARTITION_ENABLED =
		GetterUtil.getBoolean(PropsUtil.get("database.partition.enabled"));
}