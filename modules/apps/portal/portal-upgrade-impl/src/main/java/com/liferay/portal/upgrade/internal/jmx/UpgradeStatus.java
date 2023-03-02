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

import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.upgrade.util.DBUpgradeStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
	public List<String> getErrors() {
		return _getEventsList(DBUpgradeStatus.getErrorMessages());
	}

	@Override
	public String getStatus() {
		return DBUpgradeStatus.getStatus();
	}

	@Override
	public String getType() {
		return DBUpgradeStatus.getType();
	}

	@Override
	public List<String> getWarnings() {
		return _getEventsList(DBUpgradeStatus.getWarningMessages());
	}

	private List<String> _getEventsList(
		Map<String, Map<String, Integer>> eventsMap) {

		List<String> events = new ArrayList<>();

		for (Map.Entry<String, Map<String, Integer>> classEventEntry :
				eventsMap.entrySet()) {

			String clazz = classEventEntry.getKey();
			Map<String, Integer> classEventMap = classEventEntry.getValue();

			for (Map.Entry<String, Integer> event : classEventMap.entrySet()) {
				String eventMessage = event.getKey();

				events.add(
					StringBundler.concat(
						clazz, StringPool.COLON, StringPool.SPACE,
						eventMessage));
			}
		}

		return events;
	}

}