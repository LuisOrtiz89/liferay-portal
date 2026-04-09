package com.liferay.style.book.internal.search.spi.model.result.contributor;

import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.search.Summary;
import com.liferay.portal.search.spi.model.result.contributor.ModelSummaryContributor;

import java.util.Locale;

public class StyleBookEntryModelSummaryContributor implements
	ModelSummaryContributor {

	@Override
	public Summary getSummary(
		Document document, Locale locale, String snippet) {

		return new Summary(
			locale,
			document.get(locale, Field.TITLE),
			document.get(locale, Field.TITLE)
		);
	}
}
