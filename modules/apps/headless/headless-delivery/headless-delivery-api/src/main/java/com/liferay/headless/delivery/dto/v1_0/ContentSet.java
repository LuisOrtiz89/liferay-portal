/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.delivery.dto.v1_0;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonValue;

import com.liferay.petra.function.UnsafeSupplier;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.vulcan.graphql.annotation.GraphQLField;
import com.liferay.portal.vulcan.graphql.annotation.GraphQLName;
import com.liferay.portal.vulcan.util.ObjectMapperUtil;

import jakarta.annotation.Generated;

import jakarta.validation.Valid;

import jakarta.xml.bind.annotation.XmlRootElement;

import java.io.Serializable;

import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

/**
 * @author Javier Gamarra
 * @generated
 */
@Generated("")
@GraphQLName(
	description = "Represents a content set available in the Item Selector, covering persisted collections, collection providers, related providers, and repeatable field providers.",
	value = "ContentSet"
)
@io.swagger.v3.oas.annotations.media.Schema(
	description = "Represents a content set available in the Item Selector, covering persisted collections, collection providers, related providers, and repeatable field providers."
)
@JsonFilter("Liferay.Vulcan")
@JsonSubTypes(
	{
		@JsonSubTypes.Type(
			name = "AssetListCollection", value = AssetListCollection.class
		),
		@JsonSubTypes.Type(
			name = "CollectionProvider", value = CollectionProvider.class
		),
		@JsonSubTypes.Type(
			name = "RelatedCollectionProvider",
			value = RelatedCollectionProvider.class
		),
		@JsonSubTypes.Type(
			name = "RepeatableFieldsCollectionProvider",
			value = RepeatableFieldsCollectionProvider.class
		)
	}
)
@JsonTypeInfo(
	include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "collectionType",
	use = JsonTypeInfo.Id.NAME, visible = true
)
@XmlRootElement(name = "ContentSet")
public abstract class ContentSet implements Serializable {

	public static ContentSet toDTO(String json) {
		return ObjectMapperUtil.readValue(ContentSet.class, json);
	}

	public static ContentSet unsafeToDTO(String json) {
		return ObjectMapperUtil.unsafeReadValue(ContentSet.class, json);
	}

	@io.swagger.v3.oas.annotations.media.Schema(
		description = "The content set's type (AssetListCollection, CollectionProvider, RelatedCollectionProvider, RepeatableFieldsCollectionProvider)."
	)
	@JsonGetter("collectionType")
	@Valid
	public CollectionType getCollectionType() {
		if (_collectionTypeSupplier != null) {
			collectionType = _collectionTypeSupplier.get();

			_collectionTypeSupplier = null;
		}

		return collectionType;
	}

	@JsonIgnore
	public String getCollectionTypeAsString() {
		CollectionType collectionType = getCollectionType();

		if (collectionType == null) {
			return null;
		}

		return collectionType.toString();
	}

	public void setCollectionType(CollectionType collectionType) {
		this.collectionType = collectionType;

		_collectionTypeSupplier = null;
	}

	@JsonIgnore
	public void setCollectionType(
		UnsafeSupplier<CollectionType, Exception>
			collectionTypeUnsafeSupplier) {

		_collectionTypeSupplier = () -> {
			try {
				return collectionTypeUnsafeSupplier.get();
			}
			catch (RuntimeException runtimeException) {
				throw runtimeException;
			}
			catch (Exception exception) {
				throw new RuntimeException(exception);
			}
		};
	}

	@GraphQLField(
		description = "The content set's type (AssetListCollection, CollectionProvider, RelatedCollectionProvider, RepeatableFieldsCollectionProvider)."
	)
	@JsonProperty(access = JsonProperty.Access.READ_WRITE)
	protected CollectionType collectionType;

	@JsonIgnore
	private Supplier<CollectionType> _collectionTypeSupplier;

	@io.swagger.v3.oas.annotations.media.Schema(
		description = "The content set's item subtype."
	)
	public String getItemSubtype() {
		if (_itemSubtypeSupplier != null) {
			itemSubtype = _itemSubtypeSupplier.get();

			_itemSubtypeSupplier = null;
		}

		return itemSubtype;
	}

	public void setItemSubtype(String itemSubtype) {
		this.itemSubtype = itemSubtype;

		_itemSubtypeSupplier = null;
	}

	@JsonIgnore
	public void setItemSubtype(
		UnsafeSupplier<String, Exception> itemSubtypeUnsafeSupplier) {

		_itemSubtypeSupplier = () -> {
			try {
				return itemSubtypeUnsafeSupplier.get();
			}
			catch (RuntimeException runtimeException) {
				throw runtimeException;
			}
			catch (Exception exception) {
				throw new RuntimeException(exception);
			}
		};
	}

	@GraphQLField(description = "The content set's item subtype.")
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	protected String itemSubtype;

	@JsonIgnore
	private Supplier<String> _itemSubtypeSupplier;

	@io.swagger.v3.oas.annotations.media.Schema(
		description = "The fully qualified class name of the content set's items."
	)
	public String getItemType() {
		if (_itemTypeSupplier != null) {
			itemType = _itemTypeSupplier.get();

			_itemTypeSupplier = null;
		}

		return itemType;
	}

	public void setItemType(String itemType) {
		this.itemType = itemType;

		_itemTypeSupplier = null;
	}

	@JsonIgnore
	public void setItemType(
		UnsafeSupplier<String, Exception> itemTypeUnsafeSupplier) {

		_itemTypeSupplier = () -> {
			try {
				return itemTypeUnsafeSupplier.get();
			}
			catch (RuntimeException runtimeException) {
				throw runtimeException;
			}
			catch (Exception exception) {
				throw new RuntimeException(exception);
			}
		};
	}

	@GraphQLField(
		description = "The fully qualified class name of the content set's items."
	)
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	protected String itemType;

	@JsonIgnore
	private Supplier<String> _itemTypeSupplier;

	@io.swagger.v3.oas.annotations.media.Schema(
		description = "The content set's localized title."
	)
	public String getTitle() {
		if (_titleSupplier != null) {
			title = _titleSupplier.get();

			_titleSupplier = null;
		}

		return title;
	}

	public void setTitle(String title) {
		this.title = title;

		_titleSupplier = null;
	}

	@JsonIgnore
	public void setTitle(
		UnsafeSupplier<String, Exception> titleUnsafeSupplier) {

		_titleSupplier = () -> {
			try {
				return titleUnsafeSupplier.get();
			}
			catch (RuntimeException runtimeException) {
				throw runtimeException;
			}
			catch (Exception exception) {
				throw new RuntimeException(exception);
			}
		};
	}

	@GraphQLField(description = "The content set's localized title.")
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	protected String title;

	@JsonIgnore
	private Supplier<String> _titleSupplier;

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
		StringBundler sb = new StringBundler();

		sb.append("{");

		CollectionType collectionType = getCollectionType();

		if (collectionType != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"collectionType\": ");

			sb.append("\"");
			sb.append(collectionType);
			sb.append("\"");
		}

		String itemSubtype = getItemSubtype();

		if (itemSubtype != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"itemSubtype\": ");

			sb.append("\"");

			sb.append(_escape(itemSubtype));

			sb.append("\"");
		}

		String itemType = getItemType();

		if (itemType != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"itemType\": ");

			sb.append("\"");

			sb.append(_escape(itemType));

			sb.append("\"");
		}

		String title = getTitle();

		if (title != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"title\": ");

			sb.append("\"");

			sb.append(_escape(title));

			sb.append("\"");
		}

		sb.append("}");

		return sb.toString();
	}

	@io.swagger.v3.oas.annotations.media.Schema(
		accessMode = io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_ONLY,
		defaultValue = "com.liferay.headless.delivery.dto.v1_0.ContentSet",
		name = "x-class-name"
	)
	public String xClassName;

	@GraphQLName("CollectionType")
	public static enum CollectionType {

		ASSET_LIST_COLLECTION("AssetListCollection"),
		COLLECTION_PROVIDER("CollectionProvider"),
		RELATED_COLLECTION_PROVIDER("RelatedCollectionProvider"),
		REPEATABLE_FIELDS_COLLECTION_PROVIDER(
			"RepeatableFieldsCollectionProvider");

		@JsonCreator
		public static CollectionType create(String value) {
			if ((value == null) || value.equals("")) {
				return null;
			}

			for (CollectionType collectionType : values()) {
				if (Objects.equals(collectionType.getValue(), value)) {
					return collectionType;
				}
			}

			throw new IllegalArgumentException("Invalid enum value: " + value);
		}

		@JsonValue
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

	private static String _escape(Object object) {
		return StringUtil.replace(
			String.valueOf(object), _JSON_ESCAPE_STRINGS[0],
			_JSON_ESCAPE_STRINGS[1]);
	}

	private static boolean _isArray(Object value) {
		if (value == null) {
			return false;
		}

		Class<?> clazz = value.getClass();

		return clazz.isArray();
	}

	private static String _toJSON(Map<String, ?> map) {
		StringBuilder sb = new StringBuilder("{");

		@SuppressWarnings("unchecked")
		Set set = map.entrySet();

		@SuppressWarnings("unchecked")
		Iterator<Map.Entry<String, ?>> iterator = set.iterator();

		while (iterator.hasNext()) {
			Map.Entry<String, ?> entry = iterator.next();

			sb.append("\"");
			sb.append(_escape(entry.getKey()));
			sb.append("\": ");

			Object value = entry.getValue();

			if (_isArray(value)) {
				sb.append("[");

				Object[] valueArray = (Object[])value;

				for (int i = 0; i < valueArray.length; i++) {
					if (valueArray[i] instanceof Map) {
						sb.append(_toJSON((Map<String, ?>)valueArray[i]));
					}
					else if (valueArray[i] instanceof String) {
						sb.append("\"");
						sb.append(valueArray[i]);
						sb.append("\"");
					}
					else {
						sb.append(valueArray[i]);
					}

					if ((i + 1) < valueArray.length) {
						sb.append(", ");
					}
				}

				sb.append("]");
			}
			else if (value instanceof Map) {
				sb.append(_toJSON((Map<String, ?>)value));
			}
			else if (value instanceof String) {
				sb.append("\"");
				sb.append(_escape(value));
				sb.append("\"");
			}
			else {
				sb.append(value);
			}

			if (iterator.hasNext()) {
				sb.append(", ");
			}
		}

		sb.append("}");

		return sb.toString();
	}

	private static final String[][] _JSON_ESCAPE_STRINGS = {
		{"\\", "\"", "\b", "\f", "\n", "\r", "\t"},
		{"\\\\", "\\\"", "\\b", "\\f", "\\n", "\\r", "\\t"}
	};

	private Map<String, Serializable> _extendedProperties;

}
// LIFERAY-REST-BUILDER-HASH:-2031862308