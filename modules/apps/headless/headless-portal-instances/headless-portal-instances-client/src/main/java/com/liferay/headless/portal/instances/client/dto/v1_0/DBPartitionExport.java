/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.portal.instances.client.dto.v1_0;

import com.liferay.headless.portal.instances.client.function.UnsafeSupplier;
import com.liferay.headless.portal.instances.client.serdes.v1_0.DBPartitionExportSerDes;

import jakarta.annotation.Generated;

import java.io.Serializable;

import java.util.Objects;

/**
 * @author Alberto Chaparro
 * @generated
 */
@Generated("")
public class DBPartitionExport implements Cloneable, Serializable {

	public static DBPartitionExport toDTO(String json) {
		return DBPartitionExportSerDes.toDTO(json);
	}

	public String getExportedPartitionName() {
		return exportedPartitionName;
	}

	public void setExportedPartitionName(String exportedPartitionName) {
		this.exportedPartitionName = exportedPartitionName;
	}

	public void setExportedPartitionName(
		UnsafeSupplier<String, Exception> exportedPartitionNameUnsafeSupplier) {

		try {
			exportedPartitionName = exportedPartitionNameUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String exportedPartitionName;

	public String getPortalInstanceId() {
		return portalInstanceId;
	}

	public void setPortalInstanceId(String portalInstanceId) {
		this.portalInstanceId = portalInstanceId;
	}

	public void setPortalInstanceId(
		UnsafeSupplier<String, Exception> portalInstanceIdUnsafeSupplier) {

		try {
			portalInstanceId = portalInstanceIdUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String portalInstanceId;

	@Override
	public DBPartitionExport clone() throws CloneNotSupportedException {
		return (DBPartitionExport)super.clone();
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}

		if (!(object instanceof DBPartitionExport)) {
			return false;
		}

		DBPartitionExport dbPartitionExport = (DBPartitionExport)object;

		return Objects.equals(toString(), dbPartitionExport.toString());
	}

	@Override
	public int hashCode() {
		String string = toString();

		return string.hashCode();
	}

	public String toString() {
		return DBPartitionExportSerDes.toJSON(this);
	}

}
// LIFERAY-REST-BUILDER-HASH:79040086