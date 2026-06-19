/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.delivery.client.serdes.v1_0;

import com.liferay.headless.delivery.client.dto.v1_0.CollectionProvider;
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
public class CollectionProviderSerDes {

	public static CollectionProvider toDTO(String json) {
		CollectionProviderJSONParser collectionProviderJSONParser =
			new CollectionProviderJSONParser();

		return collectionProviderJSONParser.parseToDTO(json);
	}

	public static CollectionProvider[] toDTOs(String json) {
		CollectionProviderJSONParser collectionProviderJSONParser =
			new CollectionProviderJSONParser();

		return collectionProviderJSONParser.parseToDTOs(json);
	}

	public static String toJSON(CollectionProvider collectionProvider) {
		if (collectionProvider == null) {
			return "null";
		}

		StringBuilder sb = new StringBuilder();

		sb.append("{");

		if (collectionProvider.getKey() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"key\": ");

			sb.append("\"");

			sb.append(_escape(collectionProvider.getKey()));

			sb.append("\"");
		}

		if (collectionProvider.getCollectionType() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"collectionType\": ");

			sb.append("\"");
			sb.append(collectionProvider.getCollectionType());
			sb.append("\"");
		}

		if (collectionProvider.getItemSubtype() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"itemSubtype\": ");

			sb.append("\"");

			sb.append(_escape(collectionProvider.getItemSubtype()));

			sb.append("\"");
		}

		if (collectionProvider.getItemType() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"itemType\": ");

			sb.append("\"");

			sb.append(_escape(collectionProvider.getItemType()));

			sb.append("\"");
		}

		if (collectionProvider.getTitle() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"title\": ");

			sb.append("\"");

			sb.append(_escape(collectionProvider.getTitle()));

			sb.append("\"");
		}

		sb.append("}");

		return sb.toString();
	}

	public static Map<String, Object> toMap(String json) {
		CollectionProviderJSONParser collectionProviderJSONParser =
			new CollectionProviderJSONParser();

		return collectionProviderJSONParser.parseToMap(json);
	}

	public static Map<String, String> toMap(
		CollectionProvider collectionProvider) {

		if (collectionProvider == null) {
			return null;
		}

		Map<String, String> map = new TreeMap<>();

		if (collectionProvider.getKey() == null) {
			map.put("key", null);
		}
		else {
			map.put("key", String.valueOf(collectionProvider.getKey()));
		}

		if (collectionProvider.getCollectionType() == null) {
			map.put("collectionType", null);
		}
		else {
			map.put(
				"collectionType",
				String.valueOf(collectionProvider.getCollectionType()));
		}

		if (collectionProvider.getItemSubtype() == null) {
			map.put("itemSubtype", null);
		}
		else {
			map.put(
				"itemSubtype",
				String.valueOf(collectionProvider.getItemSubtype()));
		}

		if (collectionProvider.getItemType() == null) {
			map.put("itemType", null);
		}
		else {
			map.put(
				"itemType", String.valueOf(collectionProvider.getItemType()));
		}

		if (collectionProvider.getTitle() == null) {
			map.put("title", null);
		}
		else {
			map.put("title", String.valueOf(collectionProvider.getTitle()));
		}

		return map;
	}

	public static class CollectionProviderJSONParser
		extends BaseJSONParser<CollectionProvider> {

		@Override
		protected CollectionProvider createDTO() {
			return new CollectionProvider();
		}

		@Override
		protected CollectionProvider[] createDTOArray(int size) {
			return new CollectionProvider[size];
		}

		@Override
		protected boolean parseMaps(String jsonParserFieldName) {
			if (Objects.equals(jsonParserFieldName, "key")) {
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
			CollectionProvider collectionProvider, String jsonParserFieldName,
			Object jsonParserFieldValue) {

			if (Objects.equals(jsonParserFieldName, "key")) {
				if (jsonParserFieldValue != null) {
					collectionProvider.setKey((String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "collectionType")) {
				if (jsonParserFieldValue != null) {
					collectionProvider.setCollectionType(
						CollectionProvider.CollectionType.create(
							(String)jsonParserFieldValue));
				}
			}
			else if (Objects.equals(jsonParserFieldName, "itemSubtype")) {
				if (jsonParserFieldValue != null) {
					collectionProvider.setItemSubtype(
						(String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "itemType")) {
				if (jsonParserFieldValue != null) {
					collectionProvider.setItemType(
						(String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "title")) {
				if (jsonParserFieldValue != null) {
					collectionProvider.setTitle((String)jsonParserFieldValue);
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
// LIFERAY-REST-BUILDER-HASH:-326331967