<%--
/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */
--%>

<%@ include file="/init.jsp" %>

<%
String libraryName = (String)request.getAttribute(StyleBookEditorBreadcrumbProductNavigationControlMenuEntry.ATTRIBUTE_LIBRARY_NAME);
String libraryURL = (String)request.getAttribute(StyleBookEditorBreadcrumbProductNavigationControlMenuEntry.ATTRIBUTE_LIBRARY_URL);
String styleBookName = (String)request.getAttribute(StyleBookEditorBreadcrumbProductNavigationControlMenuEntry.ATTRIBUTE_STYLE_BOOK_NAME);
%>

<li class="control-menu-nav-item">
	<react:component
		module="{StyleBookEditorBreadcrumb} from style-book-web"
		props='<%=
			HashMapBuilder.<String, Object>put(
				"items",
				ListUtil.fromArray(
					HashMapBuilder.<String, Object>put(
						"href", libraryURL
					).put(
						"label", libraryName
					).build(),
					HashMapBuilder.<String, Object>put(
						"active", true
					).put(
						"label", styleBookName
					).build())
			).build()
		%>'
	/>
</li>