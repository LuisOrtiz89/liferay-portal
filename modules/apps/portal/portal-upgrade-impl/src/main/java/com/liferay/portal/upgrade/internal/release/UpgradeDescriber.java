/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.upgrade.internal.release;

import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.dao.db.DB;
import com.liferay.portal.kernel.dao.db.DBManagerUtil;
import com.liferay.portal.kernel.dao.db.DBType;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.ReleaseConstants;
import com.liferay.portal.kernel.upgrade.UpgradeStep;
import com.liferay.portal.kernel.util.ReleaseInfo;
import com.liferay.portal.upgrade.internal.executor.UpgradeExecutor;
import com.liferay.portal.upgrade.internal.executor.UpgradePlan;
import com.liferay.portal.upgrade.internal.graph.ReleaseGraphManager;
import com.liferay.portal.upgrade.internal.registry.UpgradeInfo;
import com.liferay.portal.upgrade.internal.registry.UpgradeStepRegistry;
import com.liferay.portal.upgrade.internal.release.util.ReleaseManagerUtil;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Luis Ortiz
 */
@Component(service = UpgradeDescriber.class)
public class UpgradeDescriber {

	public JSONObject describe(boolean full, Set<String> bundleSymbolicNames) {
		JSONArray modulesJSONArray = _jsonFactory.createJSONArray();

		for (String bundleSymbolicName :
				new TreeSet<>(_upgradeExecutor.getBundleSymbolicNames())) {

			if (!bundleSymbolicNames.isEmpty() &&
				!bundleSymbolicNames.contains(bundleSymbolicName)) {

				continue;
			}

			try {
				if (full) {
					modulesJSONArray.put(
						_getFullModuleJSONObject(bundleSymbolicName));
				}
				else {
					modulesJSONArray.put(
						_getPendingModuleJSONObject(bundleSymbolicName));
				}
			}
			catch (Throwable throwable) {
				if (_log.isWarnEnabled()) {
					_log.warn(
						ReleaseManagerUtil.getFailedModuleMessage(
							bundleSymbolicName),
						throwable);
				}
			}
		}

		JSONObject jsonObject = JSONUtil.put(
			"description", _DESCRIPTION
		).put(
			"formatVersion", 1
		).put(
			"gaps", _NULL
		).put(
			"modules", modulesJSONArray
		).put(
			"runtime",
			JSONUtil.put(
				"buildNumber", ReleaseInfo.getBuildNumber()
			).put(
				"kind", "portal"
			)
		).put(
			"timestamp", _getTimestamp()
		).put(
			"types", _NULL
		).put(
			"uniqueIndexDuplicates", _NULL
		);

		if (full) {
			return jsonObject.put(
				"database", _NULL
			).put(
				"mode", "full"
			).put(
				"releaseChanges", _NULL
			);
		}

		DB db = DBManagerUtil.getDB();

		DBType dbType = db.getDBType();

		return jsonObject.put(
			"database", JSONUtil.put("dbType", dbType.getName())
		).put(
			"mode", "pending"
		).put(
			"releaseChanges", _jsonFactory.createJSONArray()
		);
	}

	private JSONObject _getEdgeJSONObject(
		String upgradeStepRegistratorClassName, UpgradeInfo upgradeInfo) {

		JSONObject jsonObject = JSONUtil.put(
			"fromSchemaVersion", upgradeInfo.getFromSchemaVersionString());

		if (upgradeInfo.isInitialization()) {
			jsonObject.put("origin", "initialization");
		}
		else {
			jsonObject.put("origin", _NULL);
		}

		return jsonObject.put(
			"registratorClassName", upgradeStepRegistratorClassName
		).put(
			"steps", _getStepsJSONArray(upgradeInfo.getUpgradeSteps())
		).put(
			"toSchemaVersion", upgradeInfo.getToSchemaVersionString()
		);
	}

	private JSONArray _getEdgesJSONArray(
		UpgradeStepRegistry upgradeStepRegistry,
		List<UpgradeInfo> upgradeInfos) {

		JSONArray jsonArray = _jsonFactory.createJSONArray();

		for (UpgradeInfo upgradeInfo : upgradeInfos) {
			jsonArray.put(
				_getEdgeJSONObject(
					upgradeStepRegistry.getUpgradeStepRegistratorClassName(),
					upgradeInfo));
		}

		return jsonArray;
	}

	private JSONObject _getFullModuleJSONObject(String bundleSymbolicName) {
		List<UpgradeInfo> upgradeInfos = _upgradeExecutor.getUpgradeInfos(
			bundleSymbolicName);

		UpgradeStepRegistry upgradeStepRegistry =
			_upgradeExecutor.getUpgradeStepRegistry(bundleSymbolicName);

		JSONObject jsonObject = _getModuleJSONObject(
			bundleSymbolicName,
			_getEdgesJSONArray(upgradeStepRegistry, upgradeInfos),
			_getStepsJSONArray(
				upgradeStepRegistry.getReleaseCreationUpgradeSteps())
		).put(
			"schemaVersion", _NULL
		).put(
			"state", _NULL
		).put(
			"updateIndexes", _NULL
		);

		ReleaseGraphManager releaseGraphManager = new ReleaseGraphManager(
			upgradeInfos);

		List<String> endVertices = releaseGraphManager.getEndVertices();

		if (endVertices.size() == 1) {
			return jsonObject.put("targetSchemaVersion", endVertices.get(0));
		}

		return jsonObject.put("targetSchemaVersion", _NULL);
	}

	private JSONObject _getModuleJSONObject(
		String bundleSymbolicName, JSONArray edgesJSONArray,
		JSONArray releaseCreationStepsJSONArray) {

		return JSONUtil.put(
			"buildNumber", _NULL
		).put(
			"buildNumberSteps", _NULL
		).put(
			"bundleSymbolicName", bundleSymbolicName
		).put(
			"edges", edgesJSONArray
		).put(
			"releaseCreationSteps", releaseCreationStepsJSONArray
		).put(
			"schema", _NULL
		).put(
			"schemaCreation", _NULL
		);
	}

	private JSONObject _getPendingModuleJSONObject(String bundleSymbolicName) {
		UpgradePlan upgradePlan = _upgradeExecutor.getUpgradePlan(
			bundleSymbolicName);

		UpgradeStepRegistry upgradeStepRegistry =
			_upgradeExecutor.getUpgradeStepRegistry(bundleSymbolicName);

		JSONArray releaseCreationStepsJSONArray =
			_jsonFactory.createJSONArray();

		if (upgradePlan.getState() == null) {
			releaseCreationStepsJSONArray = _getStepsJSONArray(
				upgradeStepRegistry.getReleaseCreationUpgradeSteps());
		}

		return _getModuleJSONObject(
			bundleSymbolicName,
			_getEdgesJSONArray(
				upgradeStepRegistry, upgradePlan.getUpgradeInfos()),
			releaseCreationStepsJSONArray
		).put(
			"schemaVersion", _getValue(upgradePlan.getSchemaVersion())
		).put(
			"state", _getValue(_getStateLabel(upgradePlan.getState()))
		).put(
			"targetSchemaVersion",
			_getValue(upgradePlan.getTargetSchemaVersion())
		).put(
			"updateIndexes", upgradePlan.isUpdateIndexes()
		);
	}

	private String _getStateLabel(Integer state) {
		if (state == null) {
			return null;
		}

		if (state == ReleaseConstants.STATE_GOOD) {
			return "good";
		}

		if (state == ReleaseConstants.STATE_IN_PROGRESS) {
			return "inProgress";
		}

		if (state == ReleaseConstants.STATE_UPGRADE_FAILURE) {
			return "upgradeFailure";
		}

		if (state == ReleaseConstants.STATE_VERIFY_FAILURE) {
			return "verifyFailure";
		}

		return null;
	}

	private JSONArray _getStepsJSONArray(Collection<UpgradeStep> upgradeSteps) {
		JSONArray jsonArray = _jsonFactory.createJSONArray();

		int position = 0;

		for (UpgradeStep upgradeStep : upgradeSteps) {
			Class<?> clazz = upgradeStep.getClass();

			jsonArray.put(
				JSONUtil.put(
					"className", clazz.getName()
				).put(
					"operations", _NULL
				).put(
					"position", position++
				).put(
					"sourceClassName", _NULL
				).put(
					"sourceLineNumber", _NULL
				));
		}

		return jsonArray;
	}

	private String _getTimestamp() {
		Instant instant = Instant.now();

		return DateTimeFormatter.ISO_INSTANT.format(
			instant.truncatedTo(ChronoUnit.SECONDS));
	}

	private Object _getValue(Object value) {
		if (value == null) {
			return _NULL;
		}

		return value;
	}

	private static final String _DESCRIPTION = StringBundler.concat(
		"upgrade:describe output: for each module, the Release_ version it ",
		"starts from, the upgrade edges and steps that would run, and its ",
		"schema at the end. Rules: schema versions are opaque strings, never ",
		"compare or sort them. In pending mode 'edges' is the plan in ",
		"execution order; in full mode it is every registered edge, a set and ",
		"not a path. 'steps' is ordered: 'position' is the execution order ",
		"after pre- and post-steps are flattened. 'customChange' means the ",
		"step's effect is unknown, not that it changes nothing; 'noop' means ",
		"it changes nothing. 'unclaimedReleases' is advisory. null means not ",
		"computed in this mode or not applicable; [] means none. All SQL is ",
		"Liferay's generic SQL; 'types' maps its column types to each ",
		"database.");

	private static final Object _NULL = org.json.JSONObject.NULL;

	private static final Log _log = LogFactoryUtil.getLog(
		UpgradeDescriber.class);

	@Reference
	private JSONFactory _jsonFactory;

	@Reference
	private UpgradeExecutor _upgradeExecutor;

}