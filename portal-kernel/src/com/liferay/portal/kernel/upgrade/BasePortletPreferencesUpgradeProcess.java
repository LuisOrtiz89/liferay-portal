/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.portal.kernel.upgrade;

import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.dao.jdbc.AutoBatchPreparedStatementUtil;
import com.liferay.portal.kernel.model.ModelHintsUtil;
import com.liferay.portal.kernel.model.PortletConstants;
import com.liferay.portal.kernel.model.PortletPreferenceValue;
import com.liferay.portal.kernel.portlet.PortletPreferencesFactoryUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.LoggingTimer;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.xml.simple.Element;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.portlet.PortletPreferences;
import javax.portlet.ReadOnlyException;

/**
 * @author Jorge Ferrer
 * @author Brian Wing Shun Chan
 */
public abstract class BasePortletPreferencesUpgradeProcess
	extends UpgradeProcess {

	@Override
	protected void doUpgrade() throws Exception {
		updatePortletPreferences();
	}

	protected long getCompanyId(String sql, long primaryKey) throws Exception {
		long companyId = 0;

		try (LoggingTimer loggingTimer = new LoggingTimer("getCompanyId");
			Connection connection = getConnection();
			 PreparedStatement preparedStatement = connection.prepareStatement(
				sql)) {

			preparedStatement.setLong(1, primaryKey);

			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				while (resultSet.next()) {
					companyId = resultSet.getLong("companyId");
				}
			}
		}

		return companyId;
	}

	protected Object[] getGroup(long groupId) throws Exception {
		Object[] group = null;

		try (LoggingTimer loggingTimer = new LoggingTimer("getGroup");
			Connection connection = getConnection();
			 PreparedStatement preparedStatement = connection.prepareStatement(
				"select companyId from Group_ where groupId = ?")) {

			preparedStatement.setLong(1, groupId);

			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				while (resultSet.next()) {
					long companyId = resultSet.getLong("companyId");

					group = new Object[] {groupId, companyId};
				}
			}
		}

		return group;
	}

	protected Object[] getLayout(long plid) throws Exception {
		Object[] layout = null;

		try (LoggingTimer loggingTimer = new LoggingTimer("getLayout");
			Connection connection = getConnection();
			 PreparedStatement preparedStatement = connection.prepareStatement(
				"select groupId, companyId, privateLayout, layoutId from " +
					"Layout where plid = ?")) {

			preparedStatement.setLong(1, plid);

			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				while (resultSet.next()) {
					long groupId = resultSet.getLong("groupId");
					long companyId = resultSet.getLong("companyId");
					boolean privateLayout = resultSet.getBoolean(
						"privateLayout");
					long layoutId = resultSet.getLong("layoutId");

					layout = new Object[] {
						groupId, companyId, privateLayout, layoutId
					};
				}
			}
		}

		if (layout == null) {
			layout = getLayoutRevision(plid);
		}

		return layout;
	}

	protected Object[] getLayoutRevision(long layoutRevisionId)
		throws Exception {

		Object[] layoutRevision = null;

		try (LoggingTimer loggingTimer = new LoggingTimer("getLayoutRevision");
			Connection connection = getConnection();
			 PreparedStatement preparedStatement = connection.prepareStatement(
				"select groupId, companyId, privateLayout, layoutRevisionId " +
					"from LayoutRevision where layoutRevisionId = ?")) {

			preparedStatement.setLong(1, layoutRevisionId);

			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				while (resultSet.next()) {
					long groupId = resultSet.getLong("groupId");
					long companyId = resultSet.getLong("companyId");
					boolean privateLayout = resultSet.getBoolean(
						"privateLayout");
					long layoutId = resultSet.getLong("layoutRevisionId");

					layoutRevision = new Object[] {
						groupId, companyId, privateLayout, layoutId
					};
				}
			}
		}

		return layoutRevision;
	}

	protected String getLayoutUuid(long plid, long layoutId) throws Exception {
		Object[] layout = getLayout(plid);

		if (layout == null) {
			return null;
		}

		String uuid = null;

		try (LoggingTimer loggingTimer = new LoggingTimer("getLayoutUuid");
			Connection connection = getConnection();
			 PreparedStatement preparedStatement = connection.prepareStatement(
				"select uuid_ from Layout where groupId = ? and " +
					"privateLayout = ? and layoutId = ?")) {

			long groupId = (Long)layout[0];
			boolean privateLayout = (Boolean)layout[2];

			preparedStatement.setLong(1, groupId);
			preparedStatement.setBoolean(2, privateLayout);
			preparedStatement.setLong(3, layoutId);

			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				if (resultSet.next()) {
					uuid = resultSet.getString("uuid_");
				}
			}
		}

		return uuid;
	}

	protected String[] getPortletIds() {
		return new String[0];
	}

	protected String getUpdatePortletPreferencesWhereClause() {
		String[] portletIds = getPortletIds();

		if (portletIds.length == 0) {
			throw new IllegalArgumentException(
				"Subclasses must override getPortletIds or " +
					"getUpdatePortletPreferencesWhereClause");
		}

		StringBundler sb = new StringBundler((portletIds.length * 5) - 1);

		for (int i = 0; i < portletIds.length; i++) {
			String portletId = portletIds[i];

			sb.append("PortletPreferences.portletId ");

			if (portletId.contains(StringPool.PERCENT)) {
				sb.append(" like '");
				sb.append(portletId);
				sb.append("'");
			}
			else {
				sb.append(" = '");
				sb.append(portletId);
				sb.append("'");
			}

			if ((i + 1) < portletIds.length) {
				sb.append(" or ");
			}
		}

		return sb.toString();
	}

	protected void updatePortletPreferences() throws Exception {
		try (LoggingTimer loggingTimer = new LoggingTimer()) {
			if (hasColumn("PortletPreferences", "preferences")) {
				_updatePortletPreferences();
			}
			else {
				_updatePortletPreferenceValues();
			}
		}
	}

	protected void upgradeMultiValuePreference(
			PortletPreferences portletPreferences, String key)
		throws ReadOnlyException {

		try (LoggingTimer loggingTimer = new LoggingTimer("upgradeMultiValuePreference")) {
			String value = portletPreferences.getValue(key, StringPool.BLANK);

			if (Validator.isNotNull(value)) {
				portletPreferences.setValues(key, StringUtil.split(value));
			}
		}
	}

	protected abstract String upgradePreferences(
			long companyId, long ownerId, int ownerType, long plid,
			String portletId, String xml)
		throws Exception;

	private Map<String, PreferenceValues> _getPreferenceValuesMap(
			PreparedStatement selectPreparedStatement)
		throws Exception {

		Map<String, PreferenceValues> preferenceValuesMap = new HashMap<>();

		try (LoggingTimer loggingTimer = new LoggingTimer("_getPreferenceValuesMap");
			ResultSet resultSet = selectPreparedStatement.executeQuery()) {
			while (resultSet.next()) {
				long portletPreferenceValueId = resultSet.getLong(
					"portletPreferenceValueId");
				String name = resultSet.getString("name");

				String value = resultSet.getString("smallValue");

				if (Validator.isBlank(value)) {
					value = resultSet.getString("largeValue");
				}

				boolean readOnly = resultSet.getBoolean("readOnly");

				PreferenceValues preferenceValues =
					preferenceValuesMap.computeIfAbsent(
						name, key -> new PreferenceValues());

				preferenceValues._portletPreferenceValueIds.add(
					portletPreferenceValueId);
				preferenceValues._readOnly |= readOnly;
				preferenceValues._values.add(value);
			}
		}

		return preferenceValuesMap;
	}

	private String _toXMLString(Map<String, PreferenceValues> preferenceMap) {
		if (preferenceMap.isEmpty()) {
			return PortletConstants.DEFAULT_PREFERENCES;
		}

		try (LoggingTimer loggingTimer = new LoggingTimer("_toXMLString")) {
			Element portletPreferencesElement = new Element(
				"portlet-preferences", false);

			for (Map.Entry<String, PreferenceValues> entry :
				preferenceMap.entrySet()) {

				Element preferenceElement =
					portletPreferencesElement.addElement(
						"preference");

				preferenceElement.addElement("name", entry.getKey());

				PreferenceValues preferenceValues = entry.getValue();

				for (String value : preferenceValues._values) {
					preferenceElement.addElement("value", value);
				}

				if (preferenceValues._readOnly) {
					preferenceElement.addElement("read-only", Boolean.TRUE);
				}
			}

			return portletPreferencesElement.toXMLString();
		}
	}

	private void _updatePortletPreferences() throws Exception {
		try (LoggingTimer loggingTimer = new LoggingTimer("_updatePortletPreferences")) {
			StringBundler sb = new StringBundler(5);

			sb.append("select portletPreferencesId, companyId, ownerId, ");
			sb.append("ownerType, plid, portletId, preferences from ");
			sb.append("PortletPreferences");

			String whereClause = getUpdatePortletPreferencesWhereClause();

			if (Validator.isNotNull(whereClause)) {
				sb.append(" where ");
				sb.append(whereClause);
			}
			
			try (LoggingTimer loggingTimer2 = new LoggingTimer("_updatePortletPreferences:ExecuteQueries");
				Connection connection = getConnection();
				 PreparedStatement preparedStatement1 = connection.prepareStatement(
					 sb.toString());
				 PreparedStatement preparedStatement2 =
					 AutoBatchPreparedStatementUtil.concurrentAutoBatch(
						 connection,
						 "update PortletPreferences set preferences = ? where " +
						 "portletPreferencesId = ?");
				 PreparedStatement preparedStatement3 =
					 AutoBatchPreparedStatementUtil.concurrentAutoBatch(
						 connection,
						 "delete from PortletPreferences where " +
						 "portletPreferencesId = ?");
				 ResultSet resultSet = preparedStatement1.executeQuery()) {

				while (resultSet.next()) {
					long portletPreferencesId = resultSet.getLong(
						"portletPreferencesId");
					long companyId = resultSet.getLong("companyId");

					if (companyId > 0) {
						int ownerType = resultSet.getInt("ownerType");
						long plid = resultSet.getLong("plid");
						long ownerId = resultSet.getLong("ownerId");
						String portletId = resultSet.getString("portletId");
						String preferences = GetterUtil.getString(
							resultSet.getString("preferences"));

						String newPreferences = upgradePreferences(
							companyId, ownerId, ownerType, plid, portletId,
							preferences);

						if (!preferences.equals(newPreferences)) {
							preparedStatement2.setString(1, newPreferences);
							preparedStatement2.setLong(2, portletPreferencesId);

							preparedStatement2.addBatch();
						}
					}
					else {
						preparedStatement3.setLong(1, portletPreferencesId);

						preparedStatement3.addBatch();
					}
				}

				try (LoggingTimer loggingTimer3 = new LoggingTimer("_updatePortletPreferences:Statement2")) {
					preparedStatement2.executeBatch();
				}

				try (LoggingTimer loggingTimer3 = new LoggingTimer("_updatePortletPreferences:Statement3")) {
					preparedStatement3.executeBatch();
				}
			}
		}
	}

	private void _updatePortletPreferenceValues() throws Exception {
		try (LoggingTimer loggingTimer = new LoggingTimer("_updatePortletPreferenceValues")) {
			StringBundler sb = new StringBundler(5);

			sb.append(
				"select ctCollectionId, portletPreferencesId, companyId, ");
			sb.append("ownerId, ownerType, plid, portletId from ");
			sb.append("PortletPreferences");

			String whereClause = getUpdatePortletPreferencesWhereClause();

			if (Validator.isNotNull(whereClause)) {
				sb.append(" where ");
				sb.append(whereClause);
			}

			try (LoggingTimer loggingTimer2 = new LoggingTimer(
				"_updatePortletPreferenceValues:ExecuteQueries")) {

				processConcurrently(
					sb.toString(),
					resultSet -> {
						long portletPreferencesId = resultSet.getLong(
							"portletPreferencesId");
						long companyId = resultSet.getLong("companyId");
						int ownerType = resultSet.getInt("ownerType");
						long plid = resultSet.getLong("plid");
						long ownerId = resultSet.getLong("ownerId");
						String portletId = resultSet.getString("portletId");
						long ctCollectionId = resultSet.getLong("ctCollectionId");

						return new Object[] {
							portletPreferencesId, companyId, ownerType, plid,
							ownerId, portletId, ctCollectionId
						};
					},
					values -> {
						long portletPreferencesId = (Long)values[0];
						long companyId = (Long)values[1];
						int ownerType = (Integer)values[2];
						long plid = (Long)values[3];
						long ownerId = (Long)values[4];
						String portletId = (String)values[5];
						long ctCollectionId = (Long)values[6];

						if (companyId > 0) {
							try (Connection connection = getConnection();
								 PreparedStatement preparedStatement2 = connection.prepareStatement(
									 "select portletPreferenceValueId, largeValue, name, " +
									 "readOnly, smallValue from PortletPreferenceValue where " +
									 "portletPreferencesId = ? order by index_ asc");
								 PreparedStatement preparedStatement3 =
									 AutoBatchPreparedStatementUtil.concurrentAutoBatch(
										 connection,
										 StringBundler.concat(
											 "insert into PortletPreferenceValue (mvccVersion, ",
											 "ctCollectionId, portletPreferenceValueId, companyId, ",
											 "portletPreferencesId, index_, largeValue, name, ",
											 "readOnly, smallValue) values (0, ?, ?, ?, ?, ?, ?, ",
											 "?, ?, ?)"));
								 PreparedStatement preparedStatement4 =
									 AutoBatchPreparedStatementUtil.concurrentAutoBatch(
										 connection,
										 "update PortletPreferenceValue set largeValue = ?, " +
										 "readOnly = ?, smallValue = ? where " +
										 "portletPreferenceValueId = ?");
								 PreparedStatement preparedStatement5 =
									 AutoBatchPreparedStatementUtil.concurrentAutoBatch(
										 connection,
										 "delete from PortletPreferenceValue where " +
										 "portletPreferenceValueId = ?")) {
								preparedStatement2.setLong(
									1, portletPreferencesId);

								Map<String, PreferenceValues>
									preferenceValuesMap =
									_getPreferenceValuesMap(preparedStatement2);

								String preferences =
									_toXMLString(preferenceValuesMap);

								String newPreferences = upgradePreferences(
									companyId, ownerId, ownerType, plid,
									portletId,
									preferences);

								if (preferences.equals(newPreferences)) {
									return;
								}

								_upgradePortletPreferenceValues(
									preferenceValuesMap, ctCollectionId,
									portletPreferencesId, companyId,
									newPreferences, preparedStatement3,
									preparedStatement4, preparedStatement5);

								try (LoggingTimer loggingTimer3 = new LoggingTimer(
									"_updatePortletPreferenceValues:Statement3")) {
									preparedStatement3.executeBatch();
								}

								try (LoggingTimer loggingTimer3 = new LoggingTimer(
									"_updatePortletPreferenceValues:Statement4")) {
									preparedStatement4.executeBatch();
								}

								try (LoggingTimer loggingTimer3 = new LoggingTimer(
									"_updatePortletPreferenceValues:Statement5")) {
									preparedStatement5.executeBatch();
								}
							}
						}
						else {
							try (Connection connection = getConnection();
								 PreparedStatement preparedStatement6 =
									 connection.prepareStatement(
										 "delete from PortletPreferences where " +
										 "portletPreferencesId = ?");
								 PreparedStatement preparedStatement7 =
										 connection.prepareStatement(
										 "delete from PortletPreferenceValue where " +
										 "portletPreferencesId = ?")) {
								preparedStatement6.setLong(
									1, portletPreferencesId);

								try (LoggingTimer loggingTimer3 = new LoggingTimer(
									"_updatePortletPreferenceValues:Statement6")) {
									preparedStatement6.executeUpdate();
								}

								preparedStatement7.setLong(
									1, portletPreferencesId);

								try (LoggingTimer loggingTimer3 = new LoggingTimer(
									"_updatePortletPreferenceValues:Statement7")) {
									preparedStatement7.executeUpdate();
								}
							}
						}
					},
					"ERROR processing upgrade of PortletPreferenceValues");
			}
		}
	}

	private void _upgradePortletPreferenceValues(
			Map<String, PreferenceValues> preferenceMap, long ctCollectionId,
			long portletPreferencesId, long companyId, String newPreferences,
			PreparedStatement insertPreparedStatement,
			PreparedStatement updatePreparedStatement,
			PreparedStatement deletePreparedStatement)
		throws Exception {

		try (LoggingTimer loggingTimer3 = new LoggingTimer("_upgradePortletPreferenceValues")) {

			PortletPreferences portletPreferences = null;

			try (LoggingTimer loggingTimer4 = new LoggingTimer("_upgradePortletPreferenceValues:fromDefaultXML")) {
				portletPreferences =
					PortletPreferencesFactoryUtil.fromDefaultXML(
						newPreferences);
			}

			Map<String, Map.Entry<PreferenceValues, PreferenceValues>>
				preferenceEntries = new HashMap<>(preferenceMap.size());

			int newCount = 0;

			Map<String, String[]> newPreferenceMap =
				portletPreferences.getMap();

			try (LoggingTimer loggingTimer4 = new LoggingTimer("_upgradePortletPreferenceValues:Browse newPreferenceMap")) {
				for (Map.Entry<String, String[]> entry : newPreferenceMap.entrySet()) {
					String[] values = entry.getValue();

					if (values == null) {
						continue;
					}

					int size = 0;

					String name = entry.getKey();

					PreferenceValues preferenceValues =
						preferenceMap.remove(name);

					if (preferenceValues != null) {
						size = preferenceValues._values.size();
					}

					if (values.length > size) {
						newCount += values.length - size;
					}

					PreferenceValues newPreferenceValues =
						new PreferenceValues();

					newPreferenceValues._readOnly =
						portletPreferences.isReadOnly(
							entry.getKey());

					Collections.addAll(newPreferenceValues._values, values);

					preferenceEntries.put(
						name,
						new AbstractMap.SimpleImmutableEntry<>(
							preferenceValues, newPreferenceValues));
				}
			}

			try (LoggingTimer loggingTimer4 = new LoggingTimer("_upgradePortletPreferenceValues:browsePortletPreferenceValues")) {
				for (PreferenceValues portletPreferenceValues :
					preferenceMap.values()) {

					for (long portletPreferenceValueId :
						portletPreferenceValues._portletPreferenceValueIds) {

						deletePreparedStatement.setLong(
							1, portletPreferenceValueId);

						deletePreparedStatement.addBatch();
					}
				}
			}

			long batchCounter = 0;

			if (newCount > 0) {
				batchCounter = increment(
					PortletPreferenceValue.class.getName(), newCount);

				batchCounter -= newCount;
			}

			int smallValueMaxLength = ModelHintsUtil.getMaxLength(
				PortletPreferenceValue.class.getName(), "smallValue");

			try (LoggingTimer loggingTimer4 = new LoggingTimer("_upgradePortletPreferenceValues:browsePreferenceEntries")) {
				for (Map.Entry<String, Map.Entry<PreferenceValues, PreferenceValues>>
					entry : preferenceEntries.entrySet()) {

					Map.Entry<PreferenceValues, PreferenceValues>
						preferenceValuesEntry = entry.getValue();

					PreferenceValues oldPreferenceValues =
						preferenceValuesEntry.getKey();

					PreferenceValues newPreferenceValues =
						preferenceValuesEntry.getValue();

					List<String> newValues = newPreferenceValues._values;

					int oldSize = 0;

					if (oldPreferenceValues != null) {
						oldSize = oldPreferenceValues._values.size();
					}

					try (LoggingTimer loggingTimer5 = new LoggingTimer("_upgradePortletPreferenceValues:browseInsertUpdateBatch")) {
						for (int i = 0; i < newValues.size(); i++) {
							String value = newValues.get(i);

							if (oldSize > i) {
								try (LoggingTimer loggingTimer6 = new LoggingTimer("_upgradePortletPreferenceValues:updateBatch")) {
									if (!Objects.equals(
										value,
										oldPreferenceValues._values.get(i)) ||
										(newPreferenceValues._readOnly !=
										 oldPreferenceValues._readOnly)) {

										String largeValue = null;
										String smallValue = null;

										if (value.length() >
											smallValueMaxLength) {
											largeValue = value;
										}
										else {
											smallValue = value;
										}

										updatePreparedStatement.setString(
											1, largeValue);
										updatePreparedStatement.setBoolean(
											2, newPreferenceValues._readOnly);
										updatePreparedStatement.setString(
											3, smallValue);

										updatePreparedStatement.setLong(
											4,
											oldPreferenceValues._portletPreferenceValueIds.get(
												i));

										updatePreparedStatement.addBatch();
									}
								}
							}
							else {
								try (LoggingTimer loggingTimer6 = new LoggingTimer(
									"_upgradePortletPreferenceValues:insertBatch")) {
									String largeValue = null;
									String smallValue = null;

									if (value.length() > smallValueMaxLength) {
										largeValue = value;
									}
									else {
										smallValue = value;
									}

									insertPreparedStatement.setLong(
										1, ctCollectionId);
									insertPreparedStatement.setLong(
										2, ++batchCounter);
									insertPreparedStatement.setLong(
										3, companyId);
									insertPreparedStatement.setLong(
										4, portletPreferencesId);
									insertPreparedStatement.setInt(5, i);
									insertPreparedStatement.setString(
										6, largeValue);
									insertPreparedStatement.setString(
										7, entry.getKey());
									insertPreparedStatement.setBoolean(
										8, newPreferenceValues._readOnly);
									insertPreparedStatement.setString(
										9, smallValue);

									insertPreparedStatement.addBatch();
								}
							}
						}
					}

					try (LoggingTimer loggingTimer5 = new LoggingTimer("_upgradePortletPreferenceValues:browseDeleteBatch")) {
						for (int i = newValues.size(); i < oldSize; i++) {
							deletePreparedStatement.setLong(
								1,
								oldPreferenceValues._portletPreferenceValueIds.get(
									i));

							deletePreparedStatement.addBatch();
						}
					}
				}
			}
		}
	}

	private static class PreferenceValues {

		private final List<Long> _portletPreferenceValueIds = new ArrayList<>();
		private boolean _readOnly;
		private final List<String> _values = new ArrayList<>();

	}

}