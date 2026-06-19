/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.delivery.internal.resource.v1_0;

import com.liferay.asset.kernel.AssetRendererFactoryRegistryUtil;
import com.liferay.asset.kernel.model.AssetEntry;
import com.liferay.asset.kernel.model.AssetRendererFactory;
import com.liferay.asset.list.model.AssetListEntry;
import com.liferay.asset.list.service.AssetListEntryService;
import com.liferay.headless.delivery.dto.v1_0.AssetListCollection;
import com.liferay.headless.delivery.dto.v1_0.CollectionProvider;
import com.liferay.headless.delivery.dto.v1_0.ContentSet;
import com.liferay.headless.delivery.dto.v1_0.RelatedCollectionProvider;
import com.liferay.headless.delivery.dto.v1_0.RepeatableFieldsCollectionProvider;
import com.liferay.headless.delivery.resource.v1_0.ContentSetResource;
import com.liferay.info.collection.provider.InfoCollectionProvider;
import com.liferay.info.collection.provider.RelatedInfoItemCollectionProvider;
import com.liferay.info.collection.provider.RepeatableFieldInfoItemCollectionProvider;
import com.liferay.info.collection.provider.SingleFormVariationInfoCollectionProvider;
import com.liferay.info.exception.NoSuchFormVariationException;
import com.liferay.info.field.InfoField;
import com.liferay.info.field.InfoFieldSetEntry;
import com.liferay.info.form.InfoForm;
import com.liferay.info.item.InfoItemServiceRegistry;
import com.liferay.info.item.provider.RepeatableFieldsInfoItemFormProvider;
import com.liferay.info.search.InfoSearchClassMapperRegistry;
import com.liferay.portal.events.ServicePreAction;
import com.liferay.portal.events.ThemeServicePreAction;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextThreadLocal;
import com.liferay.portal.kernel.servlet.DummyHttpServletResponse;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.vulcan.pagination.Page;
import com.liferay.portal.vulcan.pagination.Pagination;

import jakarta.servlet.http.HttpServletResponse;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * @author Luis Ortiz
 */
@Component(
	properties = "OSGI-INF/liferay/rest/v1_0/content-set.properties",
	scope = ServiceScope.PROTOTYPE, service = ContentSetResource.class
)
public class ContentSetResourceImpl extends BaseContentSetResourceImpl {

	@Override
	public Page<ContentSet> getAssetLibraryContentSetsPage(
			Long assetLibraryId, String itemSubtype, String itemType,
			String keywords, Pagination pagination)
		throws Exception {

		return _getContentSetsPage(
			assetLibraryId, itemSubtype, itemType, keywords, pagination);
	}

	@Override
	public Page<ContentSet> getSiteContentSetsPage(
			Long siteId, String itemSubtype, String itemType, String keywords,
			Pagination pagination)
		throws Exception {

		return _getContentSetsPage(
			siteId, itemSubtype, itemType, keywords, pagination);
	}

	private List<AssetListEntry> _getAssetListEntries(
		long groupId, String itemSubtype, String itemType) {

		long[] groupIds = {groupId};

		if (Validator.isNotNull(itemSubtype) && Validator.isNotNull(itemType)) {
			return _assetListEntryService.getAssetListEntries(
				groupIds, itemSubtype, itemType, QueryUtil.ALL_POS,
				QueryUtil.ALL_POS, null);
		}

		if (Validator.isNotNull(itemType)) {
			return _assetListEntryService.getAssetListEntries(
				groupIds, new String[] {itemType}, QueryUtil.ALL_POS,
				QueryUtil.ALL_POS, null);
		}

		return _assetListEntryService.getAssetListEntries(
			groupIds, QueryUtil.ALL_POS, QueryUtil.ALL_POS, null);
	}

	private List<ContentSet> _getCollections(
			long groupId, String itemSubtype, String itemType,
			String lowerKeywords)
		throws Exception {

		List<ContentSet> contentSets = new ArrayList<>();

		for (AssetListEntry assetListEntry :
				_getAssetListEntries(groupId, itemSubtype, itemType)) {

			if (!_matchesKeywords(assetListEntry.getTitle(), lowerKeywords)) {
				continue;
			}

			contentSets.add(_toAssetListCollection(assetListEntry));
		}

		return contentSets;
	}

	private Page<ContentSet> _getContentSetsPage(
			Long groupId, String itemSubtype, String itemType, String keywords,
			Pagination pagination)
		throws Exception {

		ServiceContextThreadLocal.pushServiceContext(
			_getServiceContext(groupId));

		try {
			Locale locale = contextAcceptLanguage.getPreferredLocale();

			String lowerKeywords =
				Validator.isNotNull(keywords) ?
					StringUtil.toLowerCase(keywords) : null;

			List<ContentSet> contentSets = new ArrayList<>();

			contentSets.addAll(
				_getInfoCollectionProviders(locale, lowerKeywords));
			contentSets.addAll(
				_getCollections(groupId, itemSubtype, itemType, lowerKeywords));
			contentSets.addAll(
				_getRelatedInfoItemCollectionProviders(
					itemType, locale, lowerKeywords));
			contentSets.addAll(
				_getRepeatableFieldsInfoItemCollectionProviders(
					itemSubtype, itemType, locale, lowerKeywords));

			contentSets = ListUtil.sort(
				contentSets,
				Comparator.comparing(
					ContentSet::getTitle,
					Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)));

			return Page.of(
				ListUtil.subList(
					contentSets, pagination.getStartPosition(),
					pagination.getEndPosition()),
				pagination, contentSets.size());
		}
		finally {
			ServiceContextThreadLocal.popServiceContext();
		}
	}

	private String _getFormVariationKey(
		InfoCollectionProvider<?> infoCollectionProvider) {

		if (!(infoCollectionProvider instanceof
				SingleFormVariationInfoCollectionProvider)) {

			return null;
		}

		SingleFormVariationInfoCollectionProvider<?>
			singleFormVariationInfoCollectionProvider =
				(SingleFormVariationInfoCollectionProvider<?>)
					infoCollectionProvider;

		return singleFormVariationInfoCollectionProvider.getFormVariationKey();
	}

	@SuppressWarnings("unchecked")
	private List<ContentSet> _getInfoCollectionProviders(
		Locale locale, String lowerKeywords) {

		List<ContentSet> contentSets = new ArrayList<>();

		List<InfoCollectionProvider<?>> infoCollectionProviders =
			_infoItemServiceRegistry.getAllInfoItemServices(
				(Class<InfoCollectionProvider<?>>)
					(Class<?>)InfoCollectionProvider.class);

		for (InfoCollectionProvider<?> infoCollectionProvider :
				infoCollectionProviders) {

			if (!infoCollectionProvider.isAvailable() ||
				!_matchesKeywords(
					infoCollectionProvider.getLabel(locale), lowerKeywords)) {

				continue;
			}

			contentSets.add(
				_toCollectionProvider(infoCollectionProvider, locale));
		}

		return contentSets;
	}

	@SuppressWarnings("unchecked")
	private List<ContentSet> _getRelatedInfoItemCollectionProviders(
		String itemType, Locale locale, String lowerKeywords) {

		List<ContentSet> contentSets = new ArrayList<>();

		for (String sourceItemType : _getSourceItemTypes(itemType)) {
			List<RelatedInfoItemCollectionProvider<?, ?>>
				relatedInfoItemCollectionProviders =
					_infoItemServiceRegistry.getAllInfoItemServices(
						(Class<RelatedInfoItemCollectionProvider<?, ?>>)
							(Class<?>)RelatedInfoItemCollectionProvider.class,
						sourceItemType);

			for (RelatedInfoItemCollectionProvider<?, ?>
					relatedInfoItemCollectionProvider :
						relatedInfoItemCollectionProviders) {

				if (!relatedInfoItemCollectionProvider.isAvailable() ||
					!_matchesKeywords(
						relatedInfoItemCollectionProvider.getLabel(locale),
						lowerKeywords)) {

					continue;
				}

				contentSets.add(
					_toRelatedCollectionProvider(
						relatedInfoItemCollectionProvider, locale));
			}
		}

		return contentSets;
	}

	private List<ContentSet> _getRepeatableFieldsInfoItemCollectionProviders(
			String itemSubtype, String itemType, Locale locale,
			String lowerKeywords)
		throws Exception {

		List<ContentSet> contentSets = new ArrayList<>();

		if (Validator.isNull(itemType)) {
			return contentSets;
		}

		RepeatableFieldsInfoItemFormProvider<?>
			repeatableFieldsInfoItemFormProvider =
				_infoItemServiceRegistry.getFirstInfoItemService(
					RepeatableFieldsInfoItemFormProvider.class, itemType);

		if (repeatableFieldsInfoItemFormProvider == null) {
			return contentSets;
		}

		InfoForm infoForm;

		try {
			infoForm =
				repeatableFieldsInfoItemFormProvider.
					getRepeatableFieldsInfoForm(itemSubtype);
		}
		catch (NoSuchFormVariationException noSuchFormVariationException) {
			if (_log.isDebugEnabled()) {
				_log.debug(noSuchFormVariationException);
			}

			return contentSets;
		}

		for (InfoFieldSetEntry infoFieldSetEntry :
				infoForm.getInfoFieldSetEntries()) {

			if (infoFieldSetEntry instanceof InfoField) {
				InfoField<?> infoField = (InfoField<?>)infoFieldSetEntry;

				if (!infoField.isRepeatable()) {
					continue;
				}
			}

			if (!_matchesKeywords(
					infoFieldSetEntry.getLabel(locale), lowerKeywords)) {

				continue;
			}

			contentSets.add(
				_toRepeatableFieldsCollectionProvider(
					infoFieldSetEntry, itemSubtype, itemType, locale));
		}

		return contentSets;
	}

	private ServiceContext _getServiceContext(Long groupId) throws Exception {
		ServiceContext serviceContext = new ServiceContext();

		serviceContext.setCompanyId(contextCompany.getCompanyId());
		serviceContext.setRequest(contextHttpServletRequest);
		serviceContext.setScopeGroupId(groupId);
		serviceContext.setUserId(contextUser.getUserId());

		_initThemeDisplay(groupId);

		return serviceContext;
	}

	private List<String> _getSourceItemTypes(String itemType) {
		List<String> sourceItemTypes = new ArrayList<>();

		if (Validator.isNull(itemType)) {
			return sourceItemTypes;
		}

		sourceItemTypes.add(itemType);

		AssetRendererFactory<?> assetRendererFactory =
			AssetRendererFactoryRegistryUtil.getAssetRendererFactoryByClassName(
				_infoSearchClassMapperRegistry.getSearchClassName(itemType));

		if (assetRendererFactory != null) {
			sourceItemTypes.add(AssetEntry.class.getName());
		}

		return sourceItemTypes;
	}

	private void _initThemeDisplay(Long groupId) throws Exception {
		ThemeDisplay themeDisplay =
			(ThemeDisplay)contextHttpServletRequest.getAttribute(
				WebKeys.THEME_DISPLAY);

		if (themeDisplay == null) {
			ServicePreAction servicePreAction = new ServicePreAction();

			HttpServletResponse httpServletResponse =
				new DummyHttpServletResponse();

			servicePreAction.servicePre(
				contextHttpServletRequest, httpServletResponse, false);

			ThemeServicePreAction themeServicePreAction =
				new ThemeServicePreAction();

			themeServicePreAction.run(
				contextHttpServletRequest, httpServletResponse);

			themeDisplay = (ThemeDisplay)contextHttpServletRequest.getAttribute(
				WebKeys.THEME_DISPLAY);
		}

		themeDisplay.setScopeGroupId(groupId);
		themeDisplay.setSiteGroupId(groupId);
	}

	private boolean _matchesKeywords(String label, String lowerKeywords) {
		if (lowerKeywords == null) {
			return true;
		}

		if (label == null) {
			return false;
		}

		String lowerLabel = StringUtil.toLowerCase(label);

		return lowerLabel.contains(lowerKeywords);
	}

	private ContentSet _toAssetListCollection(AssetListEntry assetListEntry) {
		AssetListCollection assetListCollection = new AssetListCollection();

		assetListCollection.setClassNameId(
			() -> _portal.getClassNameId(AssetListEntry.class));
		assetListCollection.setClassPK(assetListEntry::getAssetListEntryId);
		assetListCollection.setCollectionType(
			() -> ContentSet.CollectionType.ASSET_LIST_COLLECTION);
		assetListCollection.setExternalReferenceCode(
			assetListEntry::getExternalReferenceCode);
		assetListCollection.setItemSubtype(
			assetListEntry::getAssetEntrySubtype);
		assetListCollection.setItemType(assetListEntry::getAssetEntryType);
		assetListCollection.setTitle(assetListEntry::getTitle);

		return assetListCollection;
	}

	private ContentSet _toCollectionProvider(
		InfoCollectionProvider<?> infoCollectionProvider, Locale locale) {

		CollectionProvider collectionProvider = new CollectionProvider();

		collectionProvider.setCollectionType(
			() -> ContentSet.CollectionType.COLLECTION_PROVIDER);
		collectionProvider.setItemSubtype(
			() -> _getFormVariationKey(infoCollectionProvider));
		collectionProvider.setItemType(
			infoCollectionProvider::getCollectionItemClassName);
		collectionProvider.setKey(infoCollectionProvider::getKey);
		collectionProvider.setTitle(
			() -> infoCollectionProvider.getLabel(locale));

		return collectionProvider;
	}

	private ContentSet _toRelatedCollectionProvider(
		RelatedInfoItemCollectionProvider<?, ?>
			relatedInfoItemCollectionProvider,
		Locale locale) {

		RelatedCollectionProvider relatedCollectionProvider =
			new RelatedCollectionProvider();

		relatedCollectionProvider.setCollectionType(
			() -> ContentSet.CollectionType.RELATED_COLLECTION_PROVIDER);
		relatedCollectionProvider.setItemSubtype(
			() -> _getFormVariationKey(relatedInfoItemCollectionProvider));
		relatedCollectionProvider.setItemType(
			relatedInfoItemCollectionProvider::getCollectionItemClassName);
		relatedCollectionProvider.setKey(
			relatedInfoItemCollectionProvider::getKey);
		relatedCollectionProvider.setSourceItemType(
			relatedInfoItemCollectionProvider::getSourceItemClassName);
		relatedCollectionProvider.setTitle(
			() -> relatedInfoItemCollectionProvider.getLabel(locale));

		return relatedCollectionProvider;
	}

	private ContentSet _toRepeatableFieldsCollectionProvider(
		InfoFieldSetEntry infoFieldSetEntry, String itemSubtype,
		String itemType, Locale locale) {

		RepeatableFieldsCollectionProvider repeatableFieldsCollectionProvider =
			new RepeatableFieldsCollectionProvider();

		repeatableFieldsCollectionProvider.setCollectionType(
			() ->
				ContentSet.CollectionType.
					REPEATABLE_FIELDS_COLLECTION_PROVIDER);
		repeatableFieldsCollectionProvider.setFieldName(
			infoFieldSetEntry::getUniqueId);
		repeatableFieldsCollectionProvider.setItemSubtype(() -> itemSubtype);
		repeatableFieldsCollectionProvider.setItemType(() -> itemType);
		repeatableFieldsCollectionProvider.setKey(
			() -> RepeatableFieldInfoItemCollectionProvider.class.getName());
		repeatableFieldsCollectionProvider.setTitle(
			() -> infoFieldSetEntry.getLabel(locale));

		return repeatableFieldsCollectionProvider;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		ContentSetResourceImpl.class);

	@Reference
	private AssetListEntryService _assetListEntryService;

	@Reference
	private InfoItemServiceRegistry _infoItemServiceRegistry;

	@Reference
	private InfoSearchClassMapperRegistry _infoSearchClassMapperRegistry;

	@Reference
	private Portal _portal;

}