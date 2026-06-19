/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.delivery.client.serdes.v1_0;

import com.liferay.headless.delivery.client.dto.v1_0.RelatedCollectionProvider;
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
public class RelatedCollectionProviderSerDes {

	public static RelatedCollectionProvider toDTO(String json) {
		RelatedCollectionProviderJSONParser
			relatedCollectionProviderJSONParser =
				new RelatedCollectionProviderJSONParser();

		return relatedCollectionProviderJSONParser.parseToDTO(json);
	}

	public static RelatedCollectionProvider[] toDTOs(String json) {
		RelatedCollectionProviderJSONParser
			relatedCollectionProviderJSONParser =
				new RelatedCollectionProviderJSONParser();

		return relatedCollectionProviderJSONParser.parseToDTOs(json);
	}

	public static String toJSON(
		RelatedCollectionProvider relatedCollectionProvider) {

		if (relatedCollectionProvider == null) {
			return "null";
		}

		StringBuilder sb = new StringBuilder();

		sb.append("{");

		if (relatedCollectionProvider.getKey() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"key\": ");

			sb.append("\"");

			sb.append(_escape(relatedCollectionProvider.getKey()));

			sb.append("\"");
		}

		if (relatedCollectionProvider.getSourceItemType() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"sourceItemType\": ");

			sb.append("\"");

			sb.append(_escape(relatedCollectionProvider.getSourceItemType()));

			sb.append("\"");
		}

		if (relatedCollectionProvider.getCollectionType() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"collectionType\": ");

			sb.append("\"");
			sb.append(relatedCollectionProvider.getCollectionType());
			sb.append("\"");
		}

		if (relatedCollectionProvider.getItemSubtype() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"itemSubtype\": ");

			sb.append("\"");

			sb.append(_escape(relatedCollectionProvider.getItemSubtype()));

			sb.append("\"");
		}

		if (relatedCollectionProvider.getItemType() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"itemType\": ");

			sb.append("\"");

			sb.append(_escape(relatedCollectionProvider.getItemType()));

			sb.append("\"");
		}

		if (relatedCollectionProvider.getTitle() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"title\": ");

			sb.append("\"");

			sb.append(_escape(relatedCollectionProvider.getTitle()));

			sb.append("\"");
		}

		sb.append("}");

		return sb.toString();
	}

	public static Map<String, Object> toMap(String json) {
		RelatedCollectionProviderJSONParser
			relatedCollectionProviderJSONParser =
				new RelatedCollectionProviderJSONParser();

		return relatedCollectionProviderJSONParser.parseToMap(json);
	}

	public static Map<String, String> toMap(
		RelatedCollectionProvider relatedCollectionProvider) {

		if (relatedCollectionProvider == null) {
			return null;
		}

		Map<String, String> map = new TreeMap<>();

		if (relatedCollectionProvider.getKey() == null) {
			map.put("key", null);
		}
		else {
			map.put("key", String.valueOf(relatedCollectionProvider.getKey()));
		}

		if (relatedCollectionProvider.getSourceItemType() == null) {
			map.put("sourceItemType", null);
		}
		else {
			map.put(
				"sourceItemType",
				String.valueOf(relatedCollectionProvider.getSourceItemType()));
		}

		if (relatedCollectionProvider.getCollectionType() == null) {
			map.put("collectionType", null);
		}
		else {
			map.put(
				"collectionType",
				String.valueOf(relatedCollectionProvider.getCollectionType()));
		}

		if (relatedCollectionProvider.getItemSubtype() == null) {
			map.put("itemSubtype", null);
		}
		else {
			map.put(
				"itemSubtype",
				String.valueOf(relatedCollectionProvider.getItemSubtype()));
		}

		if (relatedCollectionProvider.getItemType() == null) {
			map.put("itemType", null);
		}
		else {
			map.put(
				"itemType",
				String.valueOf(relatedCollectionProvider.getItemType()));
		}

		if (relatedCollectionProvider.getTitle() == null) {
			map.put("title", null);
		}
		else {
			map.put(
				"title", String.valueOf(relatedCollectionProvider.getTitle()));
		}

		return map;
	}

	public static class RelatedCollectionProviderJSONParser
		extends BaseJSONParser<RelatedCollectionProvider> {

		@Override
		protected RelatedCollectionProvider createDTO() {
			return new RelatedCollectionProvider();
		}

		@Override
		protected RelatedCollectionProvider[] createDTOArray(int size) {
			return new RelatedCollectionProvider[size];
		}

		@Override
		protected boolean parseMaps(String jsonParserFieldName) {
			if (Objects.equals(jsonParserFieldName, "key")) {
				return false;
			}
			else if (Objects.equals(jsonParserFieldName, "sourceItemType")) {
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
			RelatedCollectionProvider relatedCollectionProvider,
			String jsonParserFieldName, Object jsonParserFieldValue) {

			if (Objects.equals(jsonParserFieldName, "key")) {
				if (jsonParserFieldValue != null) {
					relatedCollectionProvider.setKey(
						(String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "sourceItemType")) {
				if (jsonParserFieldValue != null) {
					relatedCollectionProvider.setSourceItemType(
						(String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "collectionType")) {
				if (jsonParserFieldValue != null) {
					relatedCollectionProvider.setCollectionType(
						RelatedCollectionProvider.CollectionType.create(
							(String)jsonParserFieldValue));
				}
			}
			else if (Objects.equals(jsonParserFieldName, "itemSubtype")) {
				if (jsonParserFieldValue != null) {
					relatedCollectionProvider.setItemSubtype(
						(String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "itemType")) {
				if (jsonParserFieldValue != null) {
					relatedCollectionProvider.setItemType(
						(String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "title")) {
				if (jsonParserFieldValue != null) {
					relatedCollectionProvider.setTitle(
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
// LIFERAY-REST-BUILDER-HASH:-359168065