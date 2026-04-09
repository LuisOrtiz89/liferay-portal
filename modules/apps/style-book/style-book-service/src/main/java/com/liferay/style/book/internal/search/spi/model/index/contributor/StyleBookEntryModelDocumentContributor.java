package com.liferay.style.book.internal.search.spi.model.index.contributor;

import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.search.spi.model.index.contributor.ModelDocumentContributor;
import com.liferay.style.book.model.StyleBookEntry;
import org.osgi.service.component.annotations.Component;

@Component(
	property = "indexer.class.name=com.liferay.style.book.model.StyleBookEntry",
	service = ModelDocumentContributor.class
)
public class StyleBookEntryModelDocumentContributor implements
	ModelDocumentContributor<StyleBookEntry> {

	@Override
	public void contribute(Document document, StyleBookEntry styleBookEntry) {

		document.addText(Field.TITLE, styleBookEntry.getName());
		document.addKeyword("styleBookEntryId", styleBookEntry.getStyleBookEntryId());
	}
}
