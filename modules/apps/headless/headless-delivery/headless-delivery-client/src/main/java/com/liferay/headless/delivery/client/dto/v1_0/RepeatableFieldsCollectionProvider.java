/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.delivery.client.dto.v1_0;

import com.liferay.headless.delivery.client.function.UnsafeSupplier;
import com.liferay.headless.delivery.client.serdes.v1_0.RepeatableFieldsCollectionProviderSerDes;

import jakarta.annotation.Generated;

import java.io.Serializable;

import java.util.Objects;

/**
 * @author Javier Gamarra
 * @generated
 */
@Generated("")
public class RepeatableFieldsCollectionProvider
	extends ContentSet implements Cloneable, Serializable {

	public static RepeatableFieldsCollectionProvider toDTO(String json) {
		return RepeatableFieldsCollectionProviderSerDes.toDTO(json);
	}

	public String getFieldName() {
		return fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	public void setFieldName(
		UnsafeSupplier<String, Exception> fieldNameUnsafeSupplier) {

		try {
			fieldName = fieldNameUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String fieldName;

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public void setKey(UnsafeSupplier<String, Exception> keyUnsafeSupplier) {
		try {
			key = keyUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String key;

	@Override
	public RepeatableFieldsCollectionProvider clone()
		throws CloneNotSupportedException {

		return (RepeatableFieldsCollectionProvider)super.clone();
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}

		if (!(object instanceof RepeatableFieldsCollectionProvider)) {
			return false;
		}

		RepeatableFieldsCollectionProvider repeatableFieldsCollectionProvider =
			(RepeatableFieldsCollectionProvider)object;

		return Objects.equals(
			toString(), repeatableFieldsCollectionProvider.toString());
	}

	@Override
	public int hashCode() {
		String string = toString();

		return string.hashCode();
	}

	public String toString() {
		return RepeatableFieldsCollectionProviderSerDes.toJSON(this);
	}

}
// LIFERAY-REST-BUILDER-HASH:106217754