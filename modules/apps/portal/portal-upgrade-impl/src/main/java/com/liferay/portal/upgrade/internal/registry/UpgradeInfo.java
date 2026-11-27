/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.upgrade.internal.registry;

import com.liferay.petra.lang.HashUtil;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.upgrade.UpgradeStep;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author Miguel Pastor
 * @author Carlos Sierra Andrés
 */
public class UpgradeInfo {

	public UpgradeInfo(
		String fromSchemaVersionString, String toSchemaVersionString,
		boolean initialization, UpgradeStep... upgradeSteps) {

		_fromSchemaVersionString = fromSchemaVersionString;
		_toSchemaVersionString = toSchemaVersionString;
		_initialization = initialization;
		_upgradeSteps = Collections.unmodifiableList(
			Arrays.asList(upgradeSteps.clone()));
	}

	public UpgradeInfo(
		String fromSchemaVersionString, String toSchemaVersionString,
		UpgradeStep... upgradeSteps) {

		this(
			fromSchemaVersionString, toSchemaVersionString, false,
			upgradeSteps);
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}

		if (!(object instanceof UpgradeInfo)) {
			return false;
		}

		UpgradeInfo upgradeInfo = (UpgradeInfo)object;

		if (Objects.equals(
				_fromSchemaVersionString,
				upgradeInfo._fromSchemaVersionString) &&
			Objects.equals(
				_toSchemaVersionString, upgradeInfo._toSchemaVersionString) &&
			(_initialization == upgradeInfo._initialization) &&
			Objects.equals(_upgradeSteps, upgradeInfo._upgradeSteps)) {

			return true;
		}

		return false;
	}

	public String getFromSchemaVersionString() {
		return _fromSchemaVersionString;
	}

	public String getToSchemaVersionString() {
		return _toSchemaVersionString;
	}

	public List<UpgradeStep> getUpgradeSteps() {
		return _upgradeSteps;
	}

	@Override
	public int hashCode() {
		int hash = HashUtil.hash(0, _fromSchemaVersionString);

		hash = HashUtil.hash(hash, _toSchemaVersionString);
		hash = HashUtil.hash(hash, _initialization);
		hash = HashUtil.hash(hash, _upgradeSteps);

		return hash;
	}

	public boolean isInitialization() {
		return _initialization;
	}

	@Override
	public String toString() {
		if (_upgradeSteps.size() == 1) {
			return StringBundler.concat(
				"{fromSchemaVersionString=", _fromSchemaVersionString,
				", toSchemaVersionString=", _toSchemaVersionString,
				", upgradeStep=", _upgradeSteps.get(0), "}");
		}

		return StringBundler.concat(
			"{fromSchemaVersionString=", _fromSchemaVersionString,
			", toSchemaVersionString=", _toSchemaVersionString,
			", upgradeSteps=", _upgradeSteps, "}");
	}

	private final String _fromSchemaVersionString;
	private final boolean _initialization;
	private final String _toSchemaVersionString;
	private final List<UpgradeStep> _upgradeSteps;

}