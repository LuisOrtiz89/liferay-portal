/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.batch.engine.exception;

import com.liferay.portal.kernel.exception.PortalException;

/**
 * @author Luis Ortiz
 */
public class BatchEngineImportTaskSensitiveFieldsException
	extends PortalException {

	public BatchEngineImportTaskSensitiveFieldsException() {
	}

	public BatchEngineImportTaskSensitiveFieldsException(String msg) {
		super(msg);
	}

	public BatchEngineImportTaskSensitiveFieldsException(
		String msg, Throwable throwable) {

		super(msg, throwable);
	}

	public BatchEngineImportTaskSensitiveFieldsException(Throwable throwable) {
		super(throwable);
	}

}