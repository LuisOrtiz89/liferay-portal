/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.delivery.client.serdes.v1_0;

import com.liferay.headless.delivery.client.dto.v1_0.AssetListCollection;
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
public class AssetListCollectionSerDes {

	public static AssetListCollection toDTO(String json) {
		AssetListCollectionJSONParser assetListCollectionJSONParser =
			new AssetListCollectionJSONParser();

		return assetListCollectionJSONParser.parseToDTO(json);
	}

	public static AssetListCollection[] toDTOs(String json) {
		AssetListCollectionJSONParser assetListCollectionJSONParser =
			new AssetListCollectionJSONParser();

		return assetListCollectionJSONParser.parseToDTOs(json);
	}

	public static String toJSON(AssetListCollection assetListCollection) {
		if (assetListCollection == null) {
			return "null";
		}

		StringBuilder sb = new StringBuilder();

		sb.append("{");

		if (assetListCollection.getClassNameId() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"classNameId\": ");

			sb.append(assetListCollection.getClassNameId());
		}

		if (assetListCollection.getClassPK() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"classPK\": ");

			sb.append(assetListCollection.getClassPK());
		}

		if (assetListCollection.getExternalReferenceCode() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"externalReferenceCode\": ");

			sb.append("\"");

			sb.append(_escape(assetListCollection.getExternalReferenceCode()));

			sb.append("\"");
		}

		if (assetListCollection.getCollectionType() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"collectionType\": ");

			sb.append("\"");
			sb.append(assetListCollection.getCollectionType());
			sb.append("\"");
		}

		if (assetListCollection.getItemSubtype() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"itemSubtype\": ");

			sb.append("\"");

			sb.append(_escape(assetListCollection.getItemSubtype()));

			sb.append("\"");
		}

		if (assetListCollection.getItemType() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"itemType\": ");

			sb.append("\"");

			sb.append(_escape(assetListCollection.getItemType()));

			sb.append("\"");
		}

		if (assetListCollection.getTitle() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"title\": ");

			sb.append("\"");

			sb.append(_escape(assetListCollection.getTitle()));

			sb.append("\"");
		}

		sb.append("}");

		return sb.toString();
	}

	public static Map<String, Object> toMap(String json) {
		AssetListCollectionJSONParser assetListCollectionJSONParser =
			new AssetListCollectionJSONParser();

		return assetListCollectionJSONParser.parseToMap(json);
	}

	public static Map<String, String> toMap(
		AssetListCollection assetListCollection) {

		if (assetListCollection == null) {
			return null;
		}

		Map<String, String> map = new TreeMap<>();

		if (assetListCollection.getClassNameId() == null) {
			map.put("classNameId", null);
		}
		else {
			map.put(
				"classNameId",
				String.valueOf(assetListCollection.getClassNameId()));
		}

		if (assetListCollection.getClassPK() == null) {
			map.put("classPK", null);
		}
		else {
			map.put(
				"classPK", String.valueOf(assetListCollection.getClassPK()));
		}

		if (assetListCollection.getExternalReferenceCode() == null) {
			map.put("externalReferenceCode", null);
		}
		else {
			map.put(
				"externalReferenceCode",
				String.valueOf(assetListCollection.getExternalReferenceCode()));
		}

		if (assetListCollection.getCollectionType() == null) {
			map.put("collectionType", null);
		}
		else {
			map.put(
				"collectionType",
				String.valueOf(assetListCollection.getCollectionType()));
		}

		if (assetListCollection.getItemSubtype() == null) {
			map.put("itemSubtype", null);
		}
		else {
			map.put(
				"itemSubtype",
				String.valueOf(assetListCollection.getItemSubtype()));
		}

		if (assetListCollection.getItemType() == null) {
			map.put("itemType", null);
		}
		else {
			map.put(
				"itemType", String.valueOf(assetListCollection.getItemType()));
		}

		if (assetListCollection.getTitle() == null) {
			map.put("title", null);
		}
		else {
			map.put("title", String.valueOf(assetListCollection.getTitle()));
		}

		return map;
	}

	public static class AssetListCollectionJSONParser
		extends BaseJSONParser<AssetListCollection> {

		@Override
		protected AssetListCollection createDTO() {
			return new AssetListCollection();
		}

		@Override
		protected AssetListCollection[] createDTOArray(int size) {
			return new AssetListCollection[size];
		}

		@Override
		protected boolean parseMaps(String jsonParserFieldName) {
			if (Objects.equals(jsonParserFieldName, "classNameId")) {
				return false;
			}
			else if (Objects.equals(jsonParserFieldName, "classPK")) {
				return false;
			}
			else if (Objects.equals(
						jsonParserFieldName, "externalReferenceCode")) {

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
			AssetListCollection assetListCollection, String jsonParserFieldName,
			Object jsonParserFieldValue) {

			if (Objects.equals(jsonParserFieldName, "classNameId")) {
				if (jsonParserFieldValue != null) {
					assetListCollection.setClassNameId(
						Long.valueOf((String)jsonParserFieldValue));
				}
			}
			else if (Objects.equals(jsonParserFieldName, "classPK")) {
				if (jsonParserFieldValue != null) {
					assetListCollection.setClassPK(
						Long.valueOf((String)jsonParserFieldValue));
				}
			}
			else if (Objects.equals(
						jsonParserFieldName, "externalReferenceCode")) {

				if (jsonParserFieldValue != null) {
					assetListCollection.setExternalReferenceCode(
						(String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "collectionType")) {
				if (jsonParserFieldValue != null) {
					assetListCollection.setCollectionType(
						AssetListCollection.CollectionType.create(
							(String)jsonParserFieldValue));
				}
			}
			else if (Objects.equals(jsonParserFieldName, "itemSubtype")) {
				if (jsonParserFieldValue != null) {
					assetListCollection.setItemSubtype(
						(String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "itemType")) {
				if (jsonParserFieldValue != null) {
					assetListCollection.setItemType(
						(String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "title")) {
				if (jsonParserFieldValue != null) {
					assetListCollection.setTitle((String)jsonParserFieldValue);
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
// LIFERAY-REST-BUILDER-HASH:-1596529671