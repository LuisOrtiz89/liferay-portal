/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.delivery.dto.v1_0;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import com.liferay.petra.function.UnsafeSupplier;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.vulcan.graphql.annotation.GraphQLField;
import com.liferay.portal.vulcan.graphql.annotation.GraphQLName;
import com.liferay.portal.vulcan.util.ObjectMapperUtil;

import jakarta.annotation.Generated;

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
	description = "A content set backed by a persisted asset list.",
	value = "AssetListCollection"
)
@io.swagger.v3.oas.annotations.media.Schema(
	description = "A content set backed by a persisted asset list."
)
@JsonFilter("Liferay.Vulcan")
@XmlRootElement(name = "AssetListCollection")
public class AssetListCollection extends ContentSet implements Serializable {

	public static AssetListCollection toDTO(String json) {
		return ObjectMapperUtil.readValue(AssetListCollection.class, json);
	}

	public static AssetListCollection unsafeToDTO(String json) {
		return ObjectMapperUtil.unsafeReadValue(
			AssetListCollection.class, json);
	}

	@io.swagger.v3.oas.annotations.media.Schema
	public Long getClassNameId() {
		if (_classNameIdSupplier != null) {
			classNameId = _classNameIdSupplier.get();

			_classNameIdSupplier = null;
		}

		return classNameId;
	}

	public void setClassNameId(Long classNameId) {
		this.classNameId = classNameId;

		_classNameIdSupplier = null;
	}

	@JsonIgnore
	public void setClassNameId(
		UnsafeSupplier<Long, Exception> classNameIdUnsafeSupplier) {

		_classNameIdSupplier = () -> {
			try {
				return classNameIdUnsafeSupplier.get();
			}
			catch (RuntimeException runtimeException) {
				throw runtimeException;
			}
			catch (Exception exception) {
				throw new RuntimeException(exception);
			}
		};
	}

	@GraphQLField
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	protected Long classNameId;

	@JsonIgnore
	private Supplier<Long> _classNameIdSupplier;

	@io.swagger.v3.oas.annotations.media.Schema
	public Long getClassPK() {
		if (_classPKSupplier != null) {
			classPK = _classPKSupplier.get();

			_classPKSupplier = null;
		}

		return classPK;
	}

	public void setClassPK(Long classPK) {
		this.classPK = classPK;

		_classPKSupplier = null;
	}

	@JsonIgnore
	public void setClassPK(
		UnsafeSupplier<Long, Exception> classPKUnsafeSupplier) {

		_classPKSupplier = () -> {
			try {
				return classPKUnsafeSupplier.get();
			}
			catch (RuntimeException runtimeException) {
				throw runtimeException;
			}
			catch (Exception exception) {
				throw new RuntimeException(exception);
			}
		};
	}

	@GraphQLField
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	protected Long classPK;

	@JsonIgnore
	private Supplier<Long> _classPKSupplier;

	@io.swagger.v3.oas.annotations.media.Schema
	public String getExternalReferenceCode() {
		if (_externalReferenceCodeSupplier != null) {
			externalReferenceCode = _externalReferenceCodeSupplier.get();

			_externalReferenceCodeSupplier = null;
		}

		return externalReferenceCode;
	}

	public void setExternalReferenceCode(String externalReferenceCode) {
		this.externalReferenceCode = externalReferenceCode;

		_externalReferenceCodeSupplier = null;
	}

	@JsonIgnore
	public void setExternalReferenceCode(
		UnsafeSupplier<String, Exception> externalReferenceCodeUnsafeSupplier) {

		_externalReferenceCodeSupplier = () -> {
			try {
				return externalReferenceCodeUnsafeSupplier.get();
			}
			catch (RuntimeException runtimeException) {
				throw runtimeException;
			}
			catch (Exception exception) {
				throw new RuntimeException(exception);
			}
		};
	}

	@GraphQLField
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	protected String externalReferenceCode;

	@JsonIgnore
	private Supplier<String> _externalReferenceCodeSupplier;

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}

		if (!(object instanceof AssetListCollection)) {
			return false;
		}

		AssetListCollection assetListCollection = (AssetListCollection)object;

		return Objects.equals(toString(), assetListCollection.toString());
	}

	@Override
	public int hashCode() {
		String string = toString();

		return string.hashCode();
	}

	public String toString() {
		StringBundler sb = new StringBundler();

		sb.append("{");

		Long classNameId = getClassNameId();

		if (classNameId != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"classNameId\": ");

			sb.append(classNameId);
		}

		Long classPK = getClassPK();

		if (classPK != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"classPK\": ");

			sb.append(classPK);
		}

		String externalReferenceCode = getExternalReferenceCode();

		if (externalReferenceCode != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"externalReferenceCode\": ");

			sb.append("\"");

			sb.append(_escape(externalReferenceCode));

			sb.append("\"");
		}

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
		defaultValue = "com.liferay.headless.delivery.dto.v1_0.AssetListCollection",
		name = "x-class-name"
	)
	public String xClassName;

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
// LIFERAY-REST-BUILDER-HASH:-1806395726