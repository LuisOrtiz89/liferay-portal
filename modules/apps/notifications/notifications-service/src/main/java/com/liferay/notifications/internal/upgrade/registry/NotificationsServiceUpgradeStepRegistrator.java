/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.notifications.internal.upgrade.registry;

import com.liferay.portal.kernel.upgrade.DummyUpgradeStep;
import com.liferay.portal.upgrade.registry.UpgradeStepRegistrator;

import org.osgi.service.component.annotations.Component;

/**
 * @author Luis Ortiz
 */
@Component(service = UpgradeStepRegistrator.class)
public class NotificationsServiceUpgradeStepRegistrator
	implements UpgradeStepRegistrator {

	@Override
	public void register(Registry registry) {
		registry.registerInitialization();

		registry.register("1.0.0", "1.0.1", new DummyUpgradeStep());

		registry.register("1.0.1", "1.0.2", new DummyUpgradeStep());

		registry.register("1.0.2", "1.0.3", new DummyUpgradeStep());

		registry.register("1.0.3", "1.0.4", new DummyUpgradeStep());

		registry.register("1.0.4", "1.0.5", new DummyUpgradeStep());

		registry.register("1.0.5", "1.0.6", new DummyUpgradeStep());

		registry.register("1.0.6", "1.0.7", new DummyUpgradeStep());

		registry.register("1.0.7", "1.0.8", new DummyUpgradeStep());

		registry.register("1.0.8", "1.0.9", new DummyUpgradeStep());

		registry.register("1.0.9", "1.0.10", new DummyUpgradeStep());

		registry.register("1.0.10", "1.0.11", new DummyUpgradeStep());
	}

}