package com.liferay.style.book.internal.search.spi.model.index.contributor;

import com.liferay.portal.search.spi.model.index.contributor.ModelIndexerWriterContributor;
import com.liferay.style.book.model.StyleBookEntry;
import com.liferay.style.book.service.StyleBookEntryLocalService;

public class StyleBookEntryModelIndexerWriterContributor
	extends ModelIndexerWriterContributor<StyleBookEntry> {

	public StyleBookEntryModelIndexerWriterContributor(
		StyleBookEntryLocalService styleBookEntryLocalService) {

		super(styleBookEntryLocalService::getIndexableActionableDynamicQuery);
	}
}
