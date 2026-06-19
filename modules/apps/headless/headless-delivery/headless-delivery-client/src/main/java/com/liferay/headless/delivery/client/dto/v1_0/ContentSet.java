/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.delivery.client.dto.v1_0;

import com.liferay.headless.delivery.client.function.UnsafeSupplier;
import com.liferay.headless.delivery.client.serdes.v1_0.ContentSetSerDes;

import jakarta.annotation.Generated;

import java.io.Serializable;

import java.util.Objects;

/**
 * @author Javier Gamarra
 * @generated
 */
@Generated("")
public abstract class ContentSet implements Cloneable, Serializable {

	public static ContentSet toDTO(String json) {
		return ContentSetSerDes.toDTO(json);
	}

	public CollectionType getCollectionType() {
		return collectionType;
	}

	public String getCollectionTypeAsString() {
		if (collectionType == null) {
			return null;
		}

		return collectionType.toString();
	}

	public void setCollectionType(CollectionType collectionType) {
		this.collectionType = collectionType;
	}

	public void setCollectionType(
		UnsafeSupplier<CollectionType, Exception>
			collectionTypeUnsafeSupplier) {

		try {
			collectionType = collectionTypeUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected CollectionType collectionType;

	public String getItemSubtype() {
		return itemSubtype;
	}

	public void setItemSubtype(String itemSubtype) {
		this.itemSubtype = itemSubtype;
	}

	public void setItemSubtype(
		UnsafeSupplier<String, Exception> itemSubtypeUnsafeSupplier) {

		try {
			itemSubtype = itemSubtypeUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String itemSubtype;

	public String getItemType() {
		return itemType;
	}

	public void setItemType(String itemType) {
		this.itemType = itemType;
	}

	public void setItemType(
		UnsafeSupplier<String, Exception> itemTypeUnsafeSupplier) {

		try {
			itemType = itemTypeUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String itemType;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setTitle(
		UnsafeSupplier<String, Exception> titleUnsafeSupplier) {

		try {
			title = titleUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String title;

	@Override
	public ContentSet clone() throws CloneNotSupportedException {
		return (ContentSet)super.clone();
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}

		if (!(object instanceof ContentSet)) {
			return false;
		}

		ContentSet contentSet = (ContentSet)object;

		return Objects.equals(toString(), contentSet.toString());
	}

	@Override
	public int hashCode() {
		String string = toString();

		return string.hashCode();
	}

	public String toString() {
		return ContentSetSerDes.toJSON(this);
	}

	public static enum CollectionType {

		ASSET_LIST_COLLECTION("AssetListCollection"),
		COLLECTION_PROVIDER("CollectionProvider"),
		RELATED_COLLECTION_PROVIDER("RelatedCollectionProvider"),
		REPEATABLE_FIELDS_COLLECTION_PROVIDER(
			"RepeatableFieldsCollectionProvider");

		public static CollectionType create(String value) {
			for (CollectionType collectionType : values()) {
				if (Objects.equals(collectionType.getValue(), value) ||
					Objects.equals(collectionType.name(), value)) {

					return collectionType;
				}
			}

			return null;
		}

		public String getValue() {
			return _value;
		}

		@Override
		public String toString() {
			return _value;
		}

		private CollectionType(String value) {
			_value = value;
		}

		private final String _value;

	}

}
// LIFERAY-REST-BUILDER-HASH:-354925821