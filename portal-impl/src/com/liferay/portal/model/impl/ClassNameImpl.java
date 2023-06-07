/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.model.impl;

import com.liferay.petra.string.StringPool;
import com.liferay.portal.db.partition.DBPartitionUtil;
import com.liferay.portal.kernel.model.ShardedModel;
import com.liferay.portal.kernel.security.auth.CompanyThreadLocal;

/**
 * @author Brian Wing Shun Chan
 */
public class ClassNameImpl extends ClassNameBaseImpl implements ShardedModel {

	public ClassNameImpl() {
		setValue(StringPool.BLANK);
	}

	@Override
	public long getCompanyId() {
		if (DBPartitionUtil.isPartitionEnabled()) {
			return CompanyThreadLocal.getCompanyId();
		}

		return 0;
	}

	@Override
	public void setCompanyId(long companyId) {
	}

}