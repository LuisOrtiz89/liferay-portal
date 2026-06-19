/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.delivery.client.serdes.v1_0;

import com.liferay.headless.delivery.client.dto.v1_0.AssetListCollection;
import com.liferay.headless.delivery.client.dto.v1_0.CollectionProvider;
import com.liferay.headless.delivery.client.dto.v1_0.ContentSet;
import com.liferay.headless.delivery.client.dto.v1_0.RelatedCollectionProvider;
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
public class ContentSetSerDes {

	public static ContentSet toDTO(String json) {
		ContentSetJSONParser contentSetJSONParser = new ContentSetJSONParser();

		return contentSetJSONParser.parseToDTO(json);
	}

	public static ContentSet[] toDTOs(String json) {
		ContentSetJSONParser contentSetJSONParser = new ContentSetJSONParser();

		return contentSetJSONParser.parseToDTOs(json);
	}

	public static String toJSON(ContentSet contentSet) {
		if (contentSet == null) {
			return "null";
		}

		ContentSet.CollectionType collectionType =
			contentSet.getCollectionType();

		if (collectionType != null) {
			String collectionTypeString = collectionType.toString();

			if (collectionTypeString.equals("AssetListCollection")) {
				return AssetListCollectionSerDes.toJSON(
					(AssetListCollection)contentSet);
			}

			if (collectionTypeString.equals("CollectionProvider")) {
				return CollectionProviderSerDes.toJSON(
					(CollectionProvider)contentSet);
			}

			if (collectionTypeString.equals("RelatedCollectionProvider")) {
				return RelatedCollectionProviderSerDes.toJSON(
					(RelatedCollectionProvider)contentSet);
			}

			if (collectionTypeString.equals(
					"RepeatableFieldsCollectionProvider")) {

				return RepeatableFieldsCollectionProviderSerDes.toJSON(
					(RepeatableFieldsCollectionProvider)contentSet);
			}

			throw new IllegalArgumentException(
				"Unknown collectionType " + collectionTypeString);
		}
		else {
			throw new IllegalArgumentException(
				"Missing collectionType parameter");
		}
	}

	public static Map<String, Object> toMap(String json) {
		ContentSetJSONParser contentSetJSONParser = new ContentSetJSONParser();

		return contentSetJSONParser.parseToMap(json);
	}

	public static Map<String, String> toMap(ContentSet contentSet) {
		if (contentSet == null) {
			return null;
		}

		Map<String, String> map = new TreeMap<>();

		if (contentSet.getCollectionType() == null) {
			map.put("collectionType", null);
		}
		else {
			map.put(
				"collectionType",
				String.valueOf(contentSet.getCollectionType()));
		}

		if (contentSet.getItemSubtype() == null) {
			map.put("itemSubtype", null);
		}
		else {
			map.put("itemSubtype", String.valueOf(contentSet.getItemSubtype()));
		}

		if (contentSet.getItemType() == null) {
			map.put("itemType", null);
		}
		else {
			map.put("itemType", String.valueOf(contentSet.getItemType()));
		}

		if (contentSet.getTitle() == null) {
			map.put("title", null);
		}
		else {
			map.put("title", String.valueOf(contentSet.getTitle()));
		}

		return map;
	}

	public static class ContentSetJSONParser
		extends BaseJSONParser<ContentSet> {

		@Override
		protected ContentSet createDTO() {
			return null;
		}

		@Override
		protected ContentSet[] createDTOArray(int size) {
			return new ContentSet[size];
		}

		@Override
		protected boolean parseMaps(String jsonParserFieldName) {
			if (Objects.equals(jsonParserFieldName, "collectionType")) {
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
		public ContentSet parseToDTO(String json) {
			Map<String, Object> jsonMap = parseToMap(json);

			Object collectionType = jsonMap.get("collectionType");

			if (collectionType != null) {
				String collectionTypeString = collectionType.toString();

				if (collectionTypeString.equals("AssetListCollection")) {
					return AssetListCollection.toDTO(json);
				}

				if (collectionTypeString.equals("CollectionProvider")) {
					return CollectionProvider.toDTO(json);
				}

				if (collectionTypeString.equals("RelatedCollectionProvider")) {
					return RelatedCollectionProvider.toDTO(json);
				}

				if (collectionTypeString.equals(
						"RepeatableFieldsCollectionProvider")) {

					return RepeatableFieldsCollectionProvider.toDTO(json);
				}

				throw new IllegalArgumentException(
					"Unknown collectionType " + collectionTypeString);
			}
			else {
				throw new IllegalArgumentException(
					"Missing collectionType parameter");
			}
		}

		@Override
		protected void setField(
			ContentSet contentSet, String jsonParserFieldName,
			Object jsonParserFieldValue) {

			if (Objects.equals(jsonParserFieldName, "collectionType")) {
				if (jsonParserFieldValue != null) {
					contentSet.setCollectionType(
						ContentSet.CollectionType.create(
							(String)jsonParserFieldValue));
				}
			}
			else if (Objects.equals(jsonParserFieldName, "itemSubtype")) {
				if (jsonParserFieldValue != null) {
					contentSet.setItemSubtype((String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "itemType")) {
				if (jsonParserFieldValue != null) {
					contentSet.setItemType((String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "title")) {
				if (jsonParserFieldValue != null) {
					contentSet.setTitle((String)jsonParserFieldValue);
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
// LIFERAY-REST-BUILDER-HASH:1410577080