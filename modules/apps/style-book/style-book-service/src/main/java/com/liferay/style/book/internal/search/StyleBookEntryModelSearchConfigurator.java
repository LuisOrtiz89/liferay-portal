package com.liferay.style.book.internal.search;

import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.search.spi.model.index.contributor.ModelIndexerWriterContributor;
import com.liferay.portal.search.spi.model.registrar.ModelSearchConfigurator;
import com.liferay.portal.search.spi.model.result.contributor.ModelSummaryContributor;
import com.liferay.style.book.internal.search.spi.model.index.contributor.StyleBookEntryModelIndexerWriterContributor;
import com.liferay.style.book.internal.search.spi.model.result.contributor.StyleBookEntryModelSummaryContributor;
import com.liferay.style.book.model.StyleBookEntry;
import com.liferay.style.book.service.StyleBookEntryLocalService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(service = ModelSearchConfigurator.class)
public class StyleBookEntryModelSearchConfigurator implements ModelSearchConfigurator<StyleBookEntry> {
	@Override
	public String getClassName() {
		return StyleBookEntry.class.getName();
	}

	@Override
	public ModelIndexerWriterContributor<StyleBookEntry>
	getModelIndexerWriterContributor() {

		return _modelIndexerWriterContributor;
	}

	@Override
	public String[] getDefaultSelectedFieldNames() {
		return new String[] {
			Field.COMPANY_ID, Field.ENTRY_CLASS_NAME, Field.ENTRY_CLASS_PK,
			Field.GROUP_ID, Field.CREATE_DATE, Field.TITLE
		};
	}

	@Override
	public ModelSummaryContributor getModelSummaryContributor() {
		return _modelSummaryContributor;
	}

	@Activate
	protected void activate() {
		_modelIndexerWriterContributor =
			new StyleBookEntryModelIndexerWriterContributor(
				_styleBookEntryLocalService);
		_modelSummaryContributor = new StyleBookEntryModelSummaryContributor();
	}

	private ModelIndexerWriterContributor<StyleBookEntry>
		_modelIndexerWriterContributor;

	@Reference
	private StyleBookEntryLocalService _styleBookEntryLocalService;

	private ModelSummaryContributor _modelSummaryContributor;
}
