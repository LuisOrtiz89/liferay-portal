/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.portal.instances.client.serdes.v1_0;

import com.liferay.headless.portal.instances.client.dto.v1_0.DBPartitionExport;
import com.liferay.headless.portal.instances.client.json.BaseJSONParser;

import jakarta.annotation.Generated;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

/**
 * @author Alberto Chaparro
 * @generated
 */
@Generated("")
public class DBPartitionExportSerDes {

	public static DBPartitionExport toDTO(String json) {
		DBPartitionExportJSONParser dbPartitionExportJSONParser =
			new DBPartitionExportJSONParser();

		return dbPartitionExportJSONParser.parseToDTO(json);
	}

	public static DBPartitionExport[] toDTOs(String json) {
		DBPartitionExportJSONParser dbPartitionExportJSONParser =
			new DBPartitionExportJSONParser();

		return dbPartitionExportJSONParser.parseToDTOs(json);
	}

	public static String toJSON(DBPartitionExport dbPartitionExport) {
		if (dbPartitionExport == null) {
			return "null";
		}

		StringBuilder sb = new StringBuilder();

		sb.append("{");

		if (dbPartitionExport.getExportedPartitionName() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"exportedPartitionName\": ");

			sb.append("\"");

			sb.append(_escape(dbPartitionExport.getExportedPartitionName()));

			sb.append("\"");
		}

		if (dbPartitionExport.getPortalInstanceId() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"portalInstanceId\": ");

			sb.append("\"");

			sb.append(_escape(dbPartitionExport.getPortalInstanceId()));

			sb.append("\"");
		}

		sb.append("}");

		return sb.toString();
	}

	public static Map<String, Object> toMap(String json) {
		DBPartitionExportJSONParser dbPartitionExportJSONParser =
			new DBPartitionExportJSONParser();

		return dbPartitionExportJSONParser.parseToMap(json);
	}

	public static Map<String, String> toMap(
		DBPartitionExport dbPartitionExport) {

		if (dbPartitionExport == null) {
			return null;
		}

		Map<String, String> map = new TreeMap<>();

		if (dbPartitionExport.getExportedPartitionName() == null) {
			map.put("exportedPartitionName", null);
		}
		else {
			map.put(
				"exportedPartitionName",
				String.valueOf(dbPartitionExport.getExportedPartitionName()));
		}

		if (dbPartitionExport.getPortalInstanceId() == null) {
			map.put("portalInstanceId", null);
		}
		else {
			map.put(
				"portalInstanceId",
				String.valueOf(dbPartitionExport.getPortalInstanceId()));
		}

		return map;
	}

	public static class DBPartitionExportJSONParser
		extends BaseJSONParser<DBPartitionExport> {

		@Override
		protected DBPartitionExport createDTO() {
			return new DBPartitionExport();
		}

		@Override
		protected DBPartitionExport[] createDTOArray(int size) {
			return new DBPartitionExport[size];
		}

		@Override
		protected boolean parseMaps(String jsonParserFieldName) {
			if (Objects.equals(jsonParserFieldName, "exportedPartitionName")) {
				return false;
			}
			else if (Objects.equals(jsonParserFieldName, "portalInstanceId")) {
				return false;
			}

			return false;
		}

		@Override
		protected void setField(
			DBPartitionExport dbPartitionExport, String jsonParserFieldName,
			Object jsonParserFieldValue) {

			if (Objects.equals(jsonParserFieldName, "exportedPartitionName")) {
				if (jsonParserFieldValue != null) {
					dbPartitionExport.setExportedPartitionName(
						(String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "portalInstanceId")) {
				if (jsonParserFieldValue != null) {
					dbPartitionExport.setPortalInstanceId(
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

		if (value instanceof Collection) {
			Collection<?> collection = (Collection<?>)value;

			return _toJSON(collection.toArray());
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
// LIFERAY-REST-BUILDER-HASH:-2063624943