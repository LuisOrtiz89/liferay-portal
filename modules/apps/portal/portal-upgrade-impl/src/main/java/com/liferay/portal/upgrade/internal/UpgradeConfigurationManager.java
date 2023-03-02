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

package com.liferay.portal.upgrade.internal;

import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.upgrade.internal.configuration.UpgradeMonitoringConfiguration;
import com.liferay.portal.upgrade.internal.jmx.UpgradeStatus;

import java.util.Map;

import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;

/**
 * @author Luis Ortiz
 */
@Component(
	configurationPid = "com.liferay.portal.upgrade.internal.configuration.UpgradeMonitoringConfiguration",
	service = {}
)
public class UpgradeConfigurationManager {

	@Activate
	@Modified
	protected void activate(
			ComponentContext componentContext, Map<String, Object> properties)
		throws Exception {

		UpgradeMonitoringConfiguration monitoringConfiguration =
			ConfigurableUtil.createConfigurable(
				UpgradeMonitoringConfiguration.class, properties);

		if (monitoringConfiguration.enableUpgradeStatusMbean()) {
			componentContext.enableComponent(UpgradeStatus.class.getName());
		}
		else {
			componentContext.disableComponent(UpgradeStatus.class.getName());
		}
	}

}