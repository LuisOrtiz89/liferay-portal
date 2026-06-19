/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.delivery.client.serdes.v1_0;

import com.liferay.headless.delivery.client.dto.v1_0.RepeatableFieldsCollectionProvider;
import com.liferay.headless.delivery.client.json.BaseJSONParser;

import jakarta.annotation.Generated;

import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

/**
 * @author Javier Gamarra
 * @generated
 */
@Generated("")
public class RepeatableFieldsCollectionProviderSerDes {

	public static RepeatableFieldsCollectionProvider toDTO(String json) {
		RepeatableFieldsCollectionProviderJSONParser
			repeatableFieldsCollectionProviderJSONParser =
				new RepeatableFieldsCollectionProviderJSONParser();

		return repeatableFieldsCollectionProviderJSONParser.parseToDTO(json);
	}

	public static RepeatableFieldsCollectionProvider[] toDTOs(String json) {
		RepeatableFieldsCollectionProviderJSONParser
			repeatableFieldsCollectionProviderJSONParser =
				new RepeatableFieldsCollectionProviderJSONParser();

		return repeatableFieldsCollectionProviderJSONParser.parseToDTOs(json);
	}

	public static String toJSON(
		RepeatableFieldsCollectionProvider repeatableFieldsCollectionProvider) {

		if (repeatableFieldsCollectionProvider == null) {
			return "null";
		}

		StringBuilder sb = new StringBuilder();

		sb.append("{");

		if (repeatableFieldsCollectionProvider.getFieldName() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"fieldName\": ");

			sb.append("\"");

			sb.append(
				_escape(repeatableFieldsCollectionProvider.getFieldName()));

			sb.append("\"");
		}

		if (repeatableFieldsCollectionProvider.getKey() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"key\": ");

			sb.append("\"");

			sb.append(_escape(repeatableFieldsCollectionProvider.getKey()));

			sb.append("\"");
		}

		if (repeatableFieldsCollectionProvider.getCollectionType() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"collectionType\": ");

			sb.append("\"");
			sb.append(repeatableFieldsCollectionProvider.getCollectionType());
			sb.append("\"");
		}

		if (repeatableFieldsCollectionProvider.getItemSubtype() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"itemSubtype\": ");

			sb.append("\"");

			sb.append(
				_escape(repeatableFieldsCollectionProvider.getItemSubtype()));

			sb.append("\"");
		}

		if (repeatableFieldsCollectionProvider.getItemType() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"itemType\": ");

			sb.append("\"");

			sb.append(
				_escape(repeatableFieldsCollectionProvider.getItemType()));

			sb.append("\"");
		}

		if (repeatableFieldsCollectionProvider.getTitle() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"title\": ");

			sb.append("\"");

			sb.append(_escape(repeatableFieldsCollectionProvider.getTitle()));

			sb.append("\"");
		}

		sb.append("}");

		return sb.toString();
	}

	public static Map<String, Object> toMap(String json) {
		RepeatableFieldsCollectionProviderJSONParser
			repeatableFieldsCollectionProviderJSONParser =
				new RepeatableFieldsCollectionProviderJSONParser();

		return repeatableFieldsCollectionProviderJSONParser.parseToMap(json);
	}

	public static Map<String, String> toMap(
		RepeatableFieldsCollectionProvider repeatableFieldsCollectionProvider) {

		if (repeatableFieldsCollectionProvider == null) {
			return null;
		}

		Map<String, String> map = new TreeMap<>();

		if (repeatableFieldsCollectionProvider.getFieldName() == null) {
			map.put("fieldName", null);
		}
		else {
			map.put(
				"fieldName",
				String.valueOf(
					repeatableFieldsCollectionProvider.getFieldName()));
		}

		if (repeatableFieldsCollectionProvider.getKey() == null) {
			map.put("key", null);
		}
		else {
			map.put(
				"key",
				String.valueOf(repeatableFieldsCollectionProvider.getKey()));
		}

		if (repeatableFieldsCollectionProvider.getCollectionType() == null) {
			map.put("collectionType", null);
		}
		else {
			map.put(
				"collectionType",
				String.valueOf(
					repeatableFieldsCollectionProvider.getCollectionType()));
		}

		if (repeatableFieldsCollectionProvider.getItemSubtype() == null) {
			map.put("itemSubtype", null);
		}
		else {
			map.put(
				"itemSubtype",
				String.valueOf(
					repeatableFieldsCollectionProvider.getItemSubtype()));
		}

		if (repeatableFieldsCollectionProvider.getItemType() == null) {
			map.put("itemType", null);
		}
		else {
			map.put(
				"itemType",
				String.valueOf(
					repeatableFieldsCollectionProvider.getItemType()));
		}

		if (repeatableFieldsCollectionProvider.getTitle() == null) {
			map.put("title", null);
		}
		else {
			map.put(
				"title",
				String.valueOf(repeatableFieldsCollectionProvider.getTitle()));
		}

		return map;
	}

	public static class RepeatableFieldsCollectionProviderJSONParser
		extends BaseJSONParser<RepeatableFieldsCollectionProvider> {

		@Override
		protected RepeatableFieldsCollectionProvider createDTO() {
			return new RepeatableFieldsCollectionProvider();
		}

		@Override
		protected RepeatableFieldsCollectionProvider[] createDTOArray(
			int size) {

			return new RepeatableFieldsCollectionProvider[size];
		}

		@Override
		protected boolean parseMaps(String jsonParserFieldName) {
			if (Objects.equals(jsonParserFieldName, "fieldName")) {
				return false;
			}
			else if (Objects.equals(jsonParserFieldName, "key")) {
				return false;
			}
			else if (Objects.equals(jsonParserFieldName, "collectionType")) {
				return false;
			}
			else if (Objects.equals(jsonParserFieldName, "itemSubtype")) {
				return false;
			}
			else if (Objects.equals(jsonParserFieldName, "itemType")) {
				return false;
			}
			else if (Objects.equals(jsonParserFieldName, "title")) {
				return false;
			}

			return false;
		}

		@Override
		protected void setField(
			RepeatableFieldsCollectionProvider
				repeatableFieldsCollectionProvider,
			String jsonParserFieldName, Object jsonParserFieldValue) {

			if (Objects.equals(jsonParserFieldName, "fieldName")) {
				if (jsonParserFieldValue != null) {
					repeatableFieldsCollectionProvider.setFieldName(
						(String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "key")) {
				if (jsonParserFieldValue != null) {
					repeatableFieldsCollectionProvider.setKey(
						(String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "collectionType")) {
				if (jsonParserFieldValue != null) {
					repeatableFieldsCollectionProvider.setCollectionType(
						RepeatableFieldsCollectionProvider.CollectionType.
							create((String)jsonParserFieldValue));
				}
			}
			else if (Objects.equals(jsonParserFieldName, "itemSubtype")) {
				if (jsonParserFieldValue != null) {
					repeatableFieldsCollectionProvider.setItemSubtype(
						(String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "itemType")) {
				if (jsonParserFieldValue != null) {
					repeatableFieldsCollectionProvider.setItemType(
						(String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "title")) {
				if (jsonParserFieldValue != null) {
					repeatableFieldsCollectionProvider.setTitle(
						(String)jsonParserFieldValue);
				}
			}
		}

	}

	private static String _escape(Object object) {
		String string = String.valueOf(object);

		for (String[] strings : BaseJSONParser.JSON_ESCAPE_STRINGS) {
			string = string.replace(strings[0], strings[1]);
		}

		return string;
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
			sb.append(entry.getKey());
			sb.append("\": ");

			Object value = entry.getValue();

			sb.append(_toJSON(value));

			if (iterator.hasNext()) {
				sb.append(", ");
			}
		}

		sb.append("}");

		return sb.toString();
	}

	private static String _toJSON(Object value) {
		if (value == null) {
			return "null";
		}

		if (value instanceof Map) {
			return _toJSON((Map)value);
		}

		Class<?> clazz = value.getClass();

		if (clazz.isArray()) {
			StringBuilder sb = new StringBuilder("[");

			Object[] values = (Object[])value;

			for (int i = 0; i < values.length; i++) {
				sb.append(_toJSON(values[i]));

				if ((i + 1) < values.length) {
					sb.append(", ");
				}
			}

			sb.append("]");

			return sb.toString();
		}

		if (value instanceof String) {
			return "\"" + _escape(value) + "\"";
		}

		return String.valueOf(value);
	}

}
// LIFERAY-REST-BUILDER-HASH:1292687896