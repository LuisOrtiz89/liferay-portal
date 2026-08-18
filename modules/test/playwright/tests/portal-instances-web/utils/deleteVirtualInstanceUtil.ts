/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {Page, expect} from '@playwright/test';

import {VirtualInstancesPage} from '../../../pages/portal-instances-web/VirtualInstancesPage';
import {UserPersonalBarPage} from '../../../pages/product-navigation-user-personal-bar-web/UserPersonalBarPage';
import {clickAndExpectToBeVisible} from '../../../utils/clickAndExpectToBeVisible';
import getRandomString from '../../../utils/getRandomString';

export async function assertVirtualInstanceIsDeletedAndNotified(
	page: Page,
	userPersonalBarPage: UserPersonalBarPage,
	virtualInstancesPage: VirtualInstancesPage
) {
	const name = getRandomString();

	await virtualInstancesPage.addNewVirtualInstance(name);

	// The row shows up as soon as the company is committed, but the Add task
	// keeps initializing the instance for a while longer. Deleting before that
	// finishes races the initialization, so wait for the Add notification, which
	// the executor only sends once its task is done.

	await _assertNotification(
		page,
		userPersonalBarPage,
		`The virtual instance ${name} was added successfully.`
	);

	await virtualInstancesPage.deleteVirtualInstance(name);

	// The start message and the completion notification are asserted by
	// DeleteInstanceMVCActionCommandTest and by
	// DeleteVirtualInstanceBackgroundTaskExecutorTest, so only wait for the row
	// to go away once the background task completes

	await virtualInstancesPage.waitForVirtualInstanceDeletion(name);
}

async function _assertNotification(
	page: Page,
	userPersonalBarPage: UserPersonalBarPage,
	name: string
) {
	await expect(async () => {
		await page.reload();

		await expect(userPersonalBarPage.notificationBadge).toBeVisible({
			timeout: 10 * 1000,
		});
	}).toPass({timeout: 120 * 1000});

	// The bell needs its handler retried, since a single click on it often lands
	// before the notifications are wired up and then goes nowhere

	await clickAndExpectToBeVisible({
		target: page.getByRole('link', {name}),
		trigger: userPersonalBarPage.notificationBadge,
	});
}
