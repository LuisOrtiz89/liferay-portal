/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.upgrade.internal.executor;

import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.model.Release;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.upgrade.internal.graph.ReleaseGraphManager;
import com.liferay.portal.upgrade.internal.registry.UpgradeInfo;
import com.liferay.portal.upgrade.internal.release.util.ReleaseManagerUtil;

import java.util.Collections;
import java.util.List;

/**
 * @author Luis Ortiz
 */
public class UpgradePlan {

	public static UpgradePlan create(
		String bundleSymbolicName, boolean schemaBundle, Release release,
		List<UpgradeInfo> upgradeInfos) {

		ReleaseGraphManager releaseGraphManager = new ReleaseGraphManager(
			upgradeInfos);

		String schemaVersionString = ReleaseManagerUtil.getSchemaVersionString(
			release);

		List<List<UpgradeInfo>> upgradeInfosList =
			releaseGraphManager.getUpgradeInfosList(schemaVersionString);

		int size = upgradeInfosList.size();

		if (size > 1) {
			throw new IllegalStateException(
				StringBundler.concat(
					"There are ", size, " possible end nodes for ",
					schemaVersionString));
		}

		if (size == 0) {
			return new UpgradePlan(
				bundleSymbolicName, schemaBundle, release,
				Collections.emptyList());
		}

		return new UpgradePlan(
			bundleSymbolicName, schemaBundle, release, upgradeInfosList.get(0));
	}

	public UpgradePlan(
		String bundleSymbolicName, boolean schemaBundle, Release release,
		List<UpgradeInfo> upgradeInfos) {

		_bundleSymbolicName = bundleSymbolicName;
		_schemaBundle = schemaBundle;

		if (release != null) {
			if (Validator.isNotNull(release.getSchemaVersion())) {
				_schemaVersion = release.getSchemaVersion();
			}
			else {
				_schemaVersion = null;
			}

			_state = release.getState();
		}
		else {
			_schemaVersion = null;
			_state = null;
		}

		_upgradeInfos = Collections.unmodifiableList(upgradeInfos);
	}

	public String getBundleSymbolicName() {
		return _bundleSymbolicName;
	}

	public String getSchemaVersion() {
		return _schemaVersion;
	}

	public Integer getState() {
		return _state;
	}

	public String getTargetSchemaVersion() {
		if (_upgradeInfos.isEmpty()) {
			return _schemaVersion;
		}

		UpgradeInfo upgradeInfo = _upgradeInfos.get(_upgradeInfos.size() - 1);

		return upgradeInfo.getToSchemaVersionString();
	}

	public List<UpgradeInfo> getUpgradeInfos() {
		return _upgradeInfos;
	}

	public boolean isInitialRelease() {
		if (_upgradeInfos.isEmpty()) {
			return false;
		}

		UpgradeInfo upgradeInfo = _upgradeInfos.get(0);

		String fromSchemaVersion = upgradeInfo.getFromSchemaVersionString();

		return fromSchemaVersion.equals("0.0.0");
	}

	public boolean isUpdateIndexes() {
		if (!_schemaBundle || _upgradeInfos.isEmpty()) {
			return false;
		}

		if (_upgradeInfos.size() != 1) {
			return true;
		}

		return !isInitialRelease();
	}

	private final String _bundleSymbolicName;
	private final boolean _schemaBundle;
	private final String _schemaVersion;
	private final Integer _state;
	private final List<UpgradeInfo> _upgradeInfos;

}