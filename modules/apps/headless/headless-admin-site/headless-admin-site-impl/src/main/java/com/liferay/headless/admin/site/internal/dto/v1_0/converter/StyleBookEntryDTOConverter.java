package com.liferay.headless.admin.site.internal.dto.v1_0.converter;

import com.liferay.headless.admin.site.dto.v1_0.StyleBook;
import com.liferay.portal.vulcan.dto.converter.DTOConverter;
import com.liferay.portal.vulcan.dto.converter.DTOConverterContext;
import com.liferay.style.book.model.StyleBookEntry;
import com.liferay.style.book.service.StyleBookEntryLocalService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(
	property = "dto.class.name=com.liferay.style.book.model.StyleBookEntry",
	service = DTOConverter.class
)
public class StyleBookEntryDTOConverter
	implements DTOConverter<StyleBookEntry, StyleBook> {

	public String getContentType() {
		return StyleBook.class.getSimpleName();
	}

	@Override
	public StyleBook toDTO(DTOConverterContext dtoConverterContext)
		throws Exception {

		StyleBookEntry styleBookEntry =
			_styleBookEntryLocalService.getStyleBookEntry(
				(Long)dtoConverterContext.getId());

		return new StyleBook() {
			{
				setKey(styleBookEntry::getStyleBookEntryKey);
				setName(styleBookEntry::getName);
			}
		};
	}

	@Reference
	private StyleBookEntryLocalService _styleBookEntryLocalService;
}
