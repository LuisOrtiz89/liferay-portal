/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.delivery.client.dto.v1_0;

import com.liferay.headless.delivery.client.function.UnsafeSupplier;
import com.liferay.headless.delivery.client.serdes.v1_0.RelatedCollectionProviderSerDes;

import jakarta.annotation.Generated;

import java.io.Serializable;

import java.util.Objects;

/**
 * @author Javier Gamarra
 * @generated
 */
@Generated("")
public class RelatedCollectionProvider
	extends ContentSet implements Cloneable, Serializable {

	public static RelatedCollectionProvider toDTO(String json) {
		return RelatedCollectionProviderSerDes.toDTO(json);
	}

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

	public String getSourceItemType() {
		return sourceItemType;
	}

	public void setSourceItemType(String sourceItemType) {
		this.sourceItemType = sourceItemType;
	}

	public void setSourceItemType(
		UnsafeSupplier<String, Exception> sourceItemTypeUnsafeSupplier) {

		try {
			sourceItemType = sourceItemTypeUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String sourceItemType;

	@Override
	public RelatedCollectionProvider clone() throws CloneNotSupportedException {
		return (RelatedCollectionProvider)super.clone();
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}

		if (!(object instanceof RelatedCollectionProvider)) {
			return false;
		}

		RelatedCollectionProvider relatedCollectionProvider =
			(RelatedCollectionProvider)object;

		return Objects.equals(toString(), relatedCollectionProvider.toString());
	}

	@Override
	public int hashCode() {
		String string = toString();

		return string.hashCode();
	}

	public String toString() {
		return RelatedCollectionProviderSerDes.toJSON(this);
	}

}
// LIFERAY-REST-BUILDER-HASH:1032082002