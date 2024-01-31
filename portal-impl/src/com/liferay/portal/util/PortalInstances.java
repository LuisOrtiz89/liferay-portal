/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.util;

import com.liferay.petra.lang.SafeCloseable;
import com.liferay.portal.kernel.exception.NoSuchVirtualHostException;
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
	 * @deprecated As of Cavanaugh (7.4.x), replaced by {@link com.liferay.portal.kernel.instance.PortalInstances#getCompanyIds()}
	 */
	@Deprecated
	public static long getCompanyId(HttpServletRequest httpServletRequest) {
		return com.liferay.portal.kernel.instance.PortalInstances.getCompanyId(
			httpServletRequest);
	}

	/**
	 * @deprecated As of Cavanaugh (7.4.x), replaced by {@link com.liferay.portal.kernel.instance.PortalInstances#getCompanyId(HttpServletRequest, boolean)}
	 */
	@Deprecated
	public static long getCompanyId(
			HttpServletRequest httpServletRequest, boolean strict)
		throws NoSuchVirtualHostException {

		return com.liferay.portal.kernel.instance.PortalInstances.getCompanyId(
			httpServletRequest, strict);
	}

	/**
	 * @deprecated As of Cavanaugh (7.4.x), replaced by {@link com.liferay.portal.kernel.instance.PortalInstances#getCompanyIds()}
	 */
	@Deprecated
	public static long[] getCompanyIds() {
		return com.liferay.portal.kernel.instance.PortalInstances.
			getCompanyIds();
	}

	/**
	 * @deprecated As of Cavanaugh (7.4.x), replaced by {@link com.liferay.portal.kernel.instance.PortalInstances#getCompanyIds()}
	 */
	@Deprecated
	public static long[] getCompanyIdsBySQL() throws SQLException {
		return com.liferay.portal.kernel.instance.PortalInstances.
			getCompanyIds();
	}

	/**
	 * @deprecated As of Cavanaugh (7.4.x), replaced by {@link com.liferay.portal.kernel.instance.PortalInstances#getDefaultCompanyId()}
	 */
	@Deprecated
	public static long getDefaultCompanyId() {
		return com.liferay.portal.kernel.instance.PortalInstances.
			getDefaultCompanyId();
	}

	/**
	 * @deprecated As of Cavanaugh (7.4.x), replaced by {@link com.liferay.portal.kernel.instance.PortalInstances#getDefaultCompanyId()}
	 */
	@Deprecated
	public static long getDefaultCompanyIdBySQL() throws SQLException {
		return com.liferay.portal.kernel.instance.PortalInstances.
			getDefaultCompanyId();
	}

	/**
	 * @deprecated As of Cavanaugh (7.4.x), replaced by {@link com.liferay.portal.kernel.instance.PortalInstances#getWebIds()}
	 */
	@Deprecated
	public static String[] getWebIds() {
		return com.liferay.portal.kernel.instance.PortalInstances.getWebIds();
	}

	/**
	 * @deprecated As of Cavanaugh (7.4.x), replaced by {@link com.liferay.portal.kernel.instance.PortalInstances#initCompany(Company, boolean)}
	 */
	@Deprecated
	public static long initCompany(Company company) {
		return com.liferay.portal.kernel.instance.PortalInstances.initCompany(
			company);
	}

	/**
	 * @deprecated As of Cavanaugh (7.4.x), replaced by {@link com.liferay.portal.kernel.instance.PortalInstances#initCompany(Company, boolean)}
	 */
	@Deprecated
	public static long initCompany(Company company, boolean skipCheck) {
		return com.liferay.portal.kernel.instance.PortalInstances.initCompany(
			company, skipCheck);
	}

	/**
	 * @deprecated As of Cavanaugh (7.4.x), replaced by {@link com.liferay.portal.kernel.instance.PortalInstances#isAutoLoginIgnoreHost(String)}
	 */
	@Deprecated
	public static boolean isAutoLoginIgnoreHost(String host) {
		return com.liferay.portal.kernel.instance.PortalInstances.
			isAutoLoginIgnoreHost(host);
	}

	/**
	 * @deprecated As of Cavanaugh (7.4.x), replaced by {@link com.liferay.portal.kernel.instance.PortalInstances#isAutoLoginIgnorePath(String)}}
	 */
	@Deprecated
	public static boolean isAutoLoginIgnorePath(String path) {
		return com.liferay.portal.kernel.instance.PortalInstances.
			isAutoLoginIgnorePath(path);
	}

	/**
	 * @deprecated As of Cavanaugh (7.4.x), replaced by {@link com.liferay.portal.kernel.instance.PortalInstances#isCompanyActive(long)}
	 */
	@Deprecated
	public static boolean isCompanyActive(long companyId) {
		return com.liferay.portal.kernel.instance.PortalInstances.
			isCompanyActive(companyId);
	}

	/**
	 * @deprecated As of Cavanaugh (7.4.x), replaced by {@link com.liferay.portal.kernel.instance.PortalInstances#isCompanyInDeletionProcess(long)}
	 */
	@Deprecated
	public static boolean isCompanyInDeletionProcess(long companyId) {
		return com.liferay.portal.kernel.instance.PortalInstances.
			isCompanyInDeletionProcess(companyId);
	}

	/**
	 * @deprecated As of Cavanaugh (7.4.x), replaced by {@link com.liferay.portal.kernel.instance.PortalInstances#isCurrentCompanyInDeletionProcess()}
	 */
	@Deprecated
	public static boolean isCurrentCompanyInDeletionProcess() {
		return com.liferay.portal.kernel.instance.PortalInstances.
			isCurrentCompanyInDeletionProcess();
	}

	/**
	 * @deprecated As of Cavanaugh (7.4.x), replaced by {@link com.liferay.portal.kernel.instance.PortalInstances#isVirtualHostsIgnoreHost(String)}
	 */
	@Deprecated
	public static boolean isVirtualHostsIgnoreHost(String host) {
		return com.liferay.portal.kernel.instance.PortalInstances.
			isVirtualHostsIgnoreHost(host);
	}

	/**
	 * @deprecated As of Cavanaugh (7.4.x), replaced by {@link com.liferay.portal.kernel.instance.PortalInstances#isVirtualHostsIgnorePath(String)}
	 */
	@Deprecated
	public static boolean isVirtualHostsIgnorePath(String path) {
		return com.liferay.portal.kernel.instance.PortalInstances.
			isVirtualHostsIgnorePath(path);
	}

	/**
	 * @deprecated As of Cavanaugh (7.4.x), replaced by {@link com.liferay.portal.kernel.instance.PortalInstances#removeCompany(long)}
	 */
	@Deprecated
	public static void removeCompany(long companyId) {
		com.liferay.portal.kernel.instance.PortalInstances.removeCompany(
			companyId);
	}

	/**
	 * @deprecated As of Cavanaugh (7.4.x), replaced by {@link com.liferay.portal.kernel.instance.PortalInstances#setCompanyInDeletionProcess(long)}
	 */
	@Deprecated
	public static SafeCloseable setCompanyInDeletionProcess(long companyId) {
		return com.liferay.portal.kernel.instance.PortalInstances.
			setCompanyInDeletionProcess(companyId);
	}

}