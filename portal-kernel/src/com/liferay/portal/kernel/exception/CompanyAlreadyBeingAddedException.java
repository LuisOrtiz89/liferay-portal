/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.kernel.exception;

/**
 * @author Luis Ortiz
 */
public class CompanyAlreadyBeingAddedException extends PortalException {

	public CompanyAlreadyBeingAddedException() {
	}

	public CompanyAlreadyBeingAddedException(String msg) {
		super(msg);
	}

	public CompanyAlreadyBeingAddedException(String msg, Throwable throwable) {
		super(msg, throwable);
	}

	public CompanyAlreadyBeingAddedException(Throwable throwable) {
		super(throwable);
	}

}