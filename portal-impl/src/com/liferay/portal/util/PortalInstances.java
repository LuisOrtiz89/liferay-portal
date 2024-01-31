/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.util;

import com.liferay.petra.lang.SafeCloseable;
import com.liferay.portal.kernel.exception.NoSuchVirtualHostException;
import com.liferay.portal.kernel.instance.PortalInstancePool;
import com.liferay.portal.kernel.model.Company;

import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Brian Wing Shun Chan
 * @author Jose Oliver
 * @author Atul Patel
 * @author Mika Koivisto
 */
public class PortalInstances {

	/**
	 * @deprecated As of Cavanaugh (7.4.x), replaced by {@link PortalInstancePool#getCompanyIds()}
	 */
	@Deprecated
	public static long getCompanyId(HttpServletRequest httpServletRequest) {
		return PortalInstancePool.getCompanyId(httpServletRequest);
	}

	/**
	 * @deprecated As of Cavanaugh (7.4.x), replaced by {@link PortalInstancePool#getCompanyId(HttpServletRequest, boolean)}
	 */
	@Deprecated
	public static long getCompanyId(
			HttpServletRequest httpServletRequest, boolean strict)
		throws NoSuchVirtualHostException {

		return PortalInstancePool.getCompanyId(httpServletRequest, strict);
	}

	/**
	 * @deprecated As of Cavanaugh (7.4.x), replaced by {@link PortalInstancePool#getCompanyIds()}
	 */
	@Deprecated
	public static long[] getCompanyIds() {
		return PortalInstancePool.getCompanyIds();
	}

	/**
	 * @deprecated As of Cavanaugh (7.4.x), replaced by {@link PortalInstancePool#getCompanyIds()}
	 */
	@Deprecated
	public static long[] getCompanyIdsBySQL() throws SQLException {
		return PortalInstancePool.getCompanyIds();
	}

	/**
	 * @deprecated As of Cavanaugh (7.4.x), replaced by {@link PortalInstancePool#getDefaultCompanyId()}
	 */
	@Deprecated
	public static long getDefaultCompanyId() {
		return PortalInstancePool.getDefaultCompanyId();
	}

	/**
	 * @deprecated As of Cavanaugh (7.4.x), replaced by {@link PortalInstancePool#getDefaultCompanyId()}
	 */
	@Deprecated
	public static long getDefaultCompanyIdBySQL() throws SQLException {
		return PortalInstancePool.getDefaultCompanyId();
	}

	/**
	 * @deprecated As of Cavanaugh (7.4.x), replaced by {@link PortalInstancePool#getWebIds()}
	 */
	@Deprecated
	public static String[] getWebIds() {
		return PortalInstancePool.getWebIds();
	}

	/**
	 * @deprecated As of Cavanaugh (7.4.x), replaced by {@link PortalInstancePool#initCompany(Company, boolean)}
	 */
	@Deprecated
	public static long initCompany(Company company) {
		return PortalInstancePool.initCompany(company);
	}

	/**
	 * @deprecated As of Cavanaugh (7.4.x), replaced by {@link PortalInstancePool#initCompany(Company, boolean)}
	 */
	@Deprecated
	public static long initCompany(Company company, boolean skipCheck) {
		return PortalInstancePool.initCompany(company, skipCheck);
	}

	/**
	 * @deprecated As of Cavanaugh (7.4.x), replaced by {@link PortalInstancePool#isAutoLoginIgnoreHost(String)}
	 */
	@Deprecated
	public static boolean isAutoLoginIgnoreHost(String host) {
		return PortalInstancePool.isAutoLoginIgnoreHost(host);
	}

	/**
	 * @deprecated As of Cavanaugh (7.4.x), replaced by {@link PortalInstancePool#isAutoLoginIgnorePath(String)}}
	 */
	@Deprecated
	public static boolean isAutoLoginIgnorePath(String path) {
		return PortalInstancePool.isAutoLoginIgnorePath(path);
	}

	/**
	 * @deprecated As of Cavanaugh (7.4.x), replaced by {@link PortalInstancePool#isCompanyActive(long)}
	 */
	@Deprecated
	public static boolean isCompanyActive(long companyId) {
		return PortalInstancePool.isCompanyActive(companyId);
	}

	/**
	 * @deprecated As of Cavanaugh (7.4.x), replaced by {@link PortalInstancePool#isCompanyInDeletionProcess(long)}
	 */
	@Deprecated
	public static boolean isCompanyInDeletionProcess(long companyId) {
		return PortalInstancePool.isCompanyInDeletionProcess(companyId);
	}

	/**
	 * @deprecated As of Cavanaugh (7.4.x), replaced by {@link PortalInstancePool#isCurrentCompanyInDeletionProcess()}
	 */
	@Deprecated
	public static boolean isCurrentCompanyInDeletionProcess() {
		return PortalInstancePool.isCurrentCompanyInDeletionProcess();
	}

	/**
	 * @deprecated As of Cavanaugh (7.4.x), replaced by {@link PortalInstancePool#isVirtualHostsIgnoreHost(String)}
	 */
	@Deprecated
	public static boolean isVirtualHostsIgnoreHost(String host) {
		return PortalInstancePool.isVirtualHostsIgnoreHost(host);
	}

	/**
	 * @deprecated As of Cavanaugh (7.4.x), replaced by {@link PortalInstancePool#isVirtualHostsIgnorePath(String)}
	 */
	@Deprecated
	public static boolean isVirtualHostsIgnorePath(String path) {
		return PortalInstancePool.isVirtualHostsIgnorePath(path);
	}

	/**
	 * @deprecated As of Cavanaugh (7.4.x), replaced by {@link PortalInstancePool#removeCompany(long)}
	 */
	@Deprecated
	public static void removeCompany(long companyId) {
		PortalInstancePool.removeCompany(companyId);
	}

	/**
	 * @deprecated As of Cavanaugh (7.4.x), replaced by {@link PortalInstancePool#setCompanyInDeletionProcess(long)}
	 */
	@Deprecated
	public static SafeCloseable setCompanyInDeletionProcess(long companyId) {
		return PortalInstancePool.setCompanyInDeletionProcess(companyId);
	}

}