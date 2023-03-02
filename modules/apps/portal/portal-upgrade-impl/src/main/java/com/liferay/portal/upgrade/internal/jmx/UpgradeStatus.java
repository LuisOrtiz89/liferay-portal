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

package com.liferay.portal.upgrade.internal.jmx;

import java.util.ArrayList;
import java.util.List;

import javax.management.DynamicMBean;
import javax.management.NotCompliantMBeanException;
import javax.management.StandardMBean;

import org.osgi.service.component.annotations.Component;

/**
 * @author Luis Ortiz
 */
@Component(
	enabled = false,
	property = {
		"jmx.objectname=com.liferay.portal.monitoring:classification=upgrade_status,name=UpgradeStatus",
		"jmx.objectname.cache.key=UpgradeStatus"
	},
	service = DynamicMBean.class
)
public class UpgradeStatus extends StandardMBean implements UpgradeStatusMBean {

	public UpgradeStatus() throws NotCompliantMBeanException {
		super(UpgradeStatusMBean.class);
	}

	@Override
	public List<String> getUpgradeErrors() {
		return new ArrayList<>();
	}

	@Override
	public String getUpgradeStatus() {
		return "Running";
	}

	@Override
	public String getUpgradeType() {
		return "Minor";
	}

}