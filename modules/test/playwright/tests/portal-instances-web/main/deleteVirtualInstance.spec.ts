/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {mergeTests} from '@playwright/test';

import {loginTest} from '../../../fixtures/loginTest';
import {notificationPagesTest} from '../../../fixtures/notificationPagesTest';
import {virtualInstancesPagesTest} from '../../../fixtures/virtualInstancesPagesTest';
import {assertVirtualInstanceIsDeletedAndNotified} from '../utils/deleteVirtualInstanceUtil';

const test = mergeTests(
	loginTest(),
	notificationPagesTest,
	virtualInstancesPagesTest
);

test(
	'Acknowledges the start and notifies the completion',
	{tag: '@LPD-93374'},
	async ({page, userPersonalBarPage, virtualInstancesPage}) =>
		assertVirtualInstanceIsDeletedAndNotified(
			page,
			userPersonalBarPage,
			virtualInstancesPage
		)
);
