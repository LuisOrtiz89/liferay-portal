/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.style.book.web.internal.product.navigation.control.menu;

import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.theme.PortletDisplay;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.product.navigation.control.menu.BaseJSPProductNavigationControlMenuEntry;
import com.liferay.product.navigation.control.menu.ProductNavigationControlMenuEntry;
import com.liferay.product.navigation.control.menu.constants.ProductNavigationControlMenuCategoryKeys;
import com.liferay.style.book.constants.StyleBookPortletKeys;
import com.liferay.style.book.model.StyleBookEntry;
import com.liferay.style.book.service.StyleBookEntryLocalService;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Luis Ortiz
 */
@Component(
	property = {
		"product.navigation.control.menu.category.key=" + ProductNavigationControlMenuCategoryKeys.SITES,
		"product.navigation.control.menu.entry.order:Integer=250"
	},
	service = ProductNavigationControlMenuEntry.class
)
public class StyleBookEditorBreadcrumbProductNavigationControlMenuEntry
	extends BaseJSPProductNavigationControlMenuEntry {

	public static final String ATTRIBUTE_LIBRARY_NAME =
		"STYLE_BOOK_EDITOR_BREADCRUMB_LIBRARY_NAME";

	public static final String ATTRIBUTE_LIBRARY_URL =
		"STYLE_BOOK_EDITOR_BREADCRUMB_LIBRARY_URL";

	public static final String ATTRIBUTE_STYLE_BOOK_NAME =
		"STYLE_BOOK_EDITOR_BREADCRUMB_STYLE_BOOK_NAME";

	@Override
	public String getIconJspPath() {
		return _ICON_JSP_PATH;
	}

	@Override
	public boolean includeIcon(
			HttpServletRequest httpServletRequest,
			HttpServletResponse httpServletResponse)
		throws IOException {

		StyleBookEntry styleBookEntry = _getDepotStyleBookEntry(
			httpServletRequest);

		if (styleBookEntry == null) {
			return false;
		}

		Group depotGroup = _groupLocalService.fetchGroup(
			styleBookEntry.getGroupId());

		ThemeDisplay themeDisplay =
			(ThemeDisplay)httpServletRequest.getAttribute(
				WebKeys.THEME_DISPLAY);

		PortletDisplay portletDisplay = themeDisplay.getPortletDisplay();

		portletDisplay.setTitle(StringPool.BLANK);

		try {
			httpServletRequest.setAttribute(
				ATTRIBUTE_LIBRARY_NAME,
				depotGroup.getDescriptiveName(themeDisplay.getLocale()));
		}
		catch (PortalException portalException) {
			_log.error(portalException);

			return false;
		}

		httpServletRequest.setAttribute(
			ATTRIBUTE_LIBRARY_URL, portletDisplay.getURLBack());
		httpServletRequest.setAttribute(
			ATTRIBUTE_STYLE_BOOK_NAME, styleBookEntry.getName());

		return super.includeIcon(httpServletRequest, httpServletResponse);
	}

	@Override
	public boolean isShow(HttpServletRequest httpServletRequest) {
		if (_getDepotStyleBookEntry(httpServletRequest) != null) {
			return true;
		}

		return false;
	}

	@Override
	protected ServletContext getServletContext() {
		return _servletContext;
	}

	private StyleBookEntry _getDepotStyleBookEntry(
		HttpServletRequest httpServletRequest) {

		String portletId = ParamUtil.getString(httpServletRequest, "p_p_id");

		if (!StyleBookPortletKeys.STYLE_BOOK.equals(portletId)) {
			return null;
		}

		long styleBookEntryId = ParamUtil.getLong(
			httpServletRequest,
			_portal.getPortletNamespace(portletId) + "styleBookEntryId");

		if (styleBookEntryId <= 0) {
			return null;
		}

		StyleBookEntry styleBookEntry =
			_styleBookEntryLocalService.fetchStyleBookEntry(styleBookEntryId);

		if (styleBookEntry == null) {
			return null;
		}

		Group group = _groupLocalService.fetchGroup(
			styleBookEntry.getGroupId());

		if ((group == null) || !group.isDepot()) {
			return null;
		}

		return styleBookEntry;
	}

	private static final String _ICON_JSP_PATH =
		"/breadcrumb/style_book_editor_breadcrumb.jsp";

	private static final Log _log = LogFactoryUtil.getLog(
		StyleBookEditorBreadcrumbProductNavigationControlMenuEntry.class);

	@Reference
	private GroupLocalService _groupLocalService;

	@Reference
	private Portal _portal;

	@Reference(target = "(osgi.web.symbolicname=com.liferay.style.book.web)")
	private ServletContext _servletContext;

	@Reference
	private StyleBookEntryLocalService _styleBookEntryLocalService;

}