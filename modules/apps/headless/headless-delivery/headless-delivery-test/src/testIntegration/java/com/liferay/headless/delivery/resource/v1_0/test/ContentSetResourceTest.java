/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.delivery.resource.v1_0.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.asset.list.constants.AssetListEntryTypeConstants;
import com.liferay.asset.list.model.AssetListEntry;
import com.liferay.asset.list.service.AssetListEntryLocalService;
import com.liferay.headless.delivery.client.dto.v1_0.AssetListCollection;
import com.liferay.headless.delivery.client.dto.v1_0.CollectionProvider;
import com.liferay.headless.delivery.client.dto.v1_0.ContentSet;
import com.liferay.headless.delivery.client.dto.v1_0.RelatedCollectionProvider;
import com.liferay.headless.delivery.client.dto.v1_0.RepeatableFieldsCollectionProvider;
import com.liferay.headless.delivery.client.pagination.Page;
import com.liferay.headless.delivery.client.pagination.Pagination;
import com.liferay.info.collection.provider.CollectionQuery;
import com.liferay.info.collection.provider.InfoCollectionProvider;
import com.liferay.info.collection.provider.RelatedInfoItemCollectionProvider;
import com.liferay.info.collection.provider.RepeatableFieldInfoItemCollectionProvider;
import com.liferay.info.collection.provider.SingleFormVariationInfoCollectionProvider;
import com.liferay.info.field.InfoField;
import com.liferay.info.field.InfoFieldSet;
import com.liferay.info.field.type.TextInfoFieldType;
import com.liferay.info.form.InfoForm;
import com.liferay.info.item.provider.RepeatableFieldsInfoItemFormProvider;
import com.liferay.info.localized.InfoLocalizedValue;
import com.liferay.info.pagination.InfoPage;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.test.rule.FeatureFlag;
import com.liferay.portal.test.rule.Inject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceRegistration;

/**
 * @author Luis Ortiz
 */
@FeatureFlag("LPD-88505")
@RunWith(Arquillian.class)
public class ContentSetResourceTest extends BaseContentSetResourceTestCase {

	@Before
	@Override
	public void setUp() throws Exception {
		super.setUp();

		Bundle bundle = FrameworkUtil.getBundle(ContentSetResourceTest.class);

		_bundleContext = bundle.getBundleContext();

		_serviceRegistrations = new ArrayList<>();
	}

	@After
	@Override
	public void tearDown() throws Exception {
		for (ServiceRegistration<?> serviceRegistration :
				_serviceRegistrations) {

			serviceRegistration.unregister();
		}

		super.tearDown();
	}

	@Override
	@Test
	public void testGetAssetLibraryContentSetsPage() throws Exception {
		String title = RandomTestUtil.randomString();

		_addAssetListEntry(testDepotEntryGroup.getGroupId(), title);

		Page<ContentSet> page =
			contentSetResource.getAssetLibraryContentSetsPage(
				testDepotEntryGroup.getGroupId(), null, null, null,
				Pagination.of(1, 500));

		ContentSet contentSet = _findByTitle(
			(List<ContentSet>)page.getItems(), title);

		Assert.assertNotNull(contentSet);
		Assert.assertEquals(
			ContentSet.CollectionType.ASSET_LIST_COLLECTION,
			contentSet.getCollectionType());
	}

	@Override
	@Test
	public void testGetSiteContentSetsPage() throws Exception {
		_testGetSiteContentSetsPageExcludesUnavailableProviders();
		_testGetSiteContentSetsPageFiltersCollectionsByItemSubtype();
		_testGetSiteContentSetsPageWithCollection();
		_testGetSiteContentSetsPageWithCollectionProvider();
		_testGetSiteContentSetsPageWithKeywords();
		_testGetSiteContentSetsPageWithRelatedCollectionProvider();
		_testGetSiteContentSetsPageWithRepeatableField();
	}

	@Override
	protected String[] getAdditionalAssertFieldNames() {
		return new String[] {"itemSubtype", "itemType", "title"};
	}

	@Override
	protected ContentSet testGetAssetLibraryContentSetsPage_addContentSet(
			Long assetLibraryId, ContentSet contentSet)
		throws Exception {

		return _addContentSet(assetLibraryId, contentSet);
	}

	@Override
	protected Long testGetAssetLibraryContentSetsPage_getAssetLibraryId()
		throws Exception {

		return testDepotEntryGroup.getGroupId();
	}

	@Override
	protected ContentSet testGetSiteContentSetsPage_addContentSet(
			Long siteId, ContentSet contentSet)
		throws Exception {

		return _addContentSet(siteId, contentSet);
	}

	private AssetListEntry _addAssetListEntry(long groupId, String title)
		throws Exception {

		return _assetListEntryLocalService.addAssetListEntry(
			RandomTestUtil.randomString(), TestPropsValues.getUserId(), groupId,
			title, AssetListEntryTypeConstants.TYPE_MANUAL,
			ServiceContextTestUtil.getServiceContext(
				groupId, TestPropsValues.getUserId()));
	}

	private ContentSet _addContentSet(Long groupId, ContentSet contentSet)
		throws Exception {

		AssetListEntry assetListEntry = _addAssetListEntry(
			groupId, contentSet.getTitle());

		if (Validator.isNotNull(contentSet.getItemType())) {
			assetListEntry.setAssetEntrySubtype(contentSet.getItemSubtype());
			assetListEntry.setAssetEntryType(contentSet.getItemType());

			assetListEntry = _assetListEntryLocalService.updateAssetListEntry(
				assetListEntry);
		}

		return _toClientDTO(assetListEntry);
	}

	private ContentSet _findByTitle(
		List<ContentSet> contentSets, String title) {

		for (ContentSet contentSet : contentSets) {
			if (title.equals(contentSet.getTitle())) {
				return contentSet;
			}
		}

		return null;
	}

	private List<ContentSet> _getSiteContentSets(
			String itemSubtype, String itemType, String keywords)
		throws Exception {

		Page<ContentSet> page = contentSetResource.getSiteContentSetsPage(
			testGroup.getGroupId(), itemSubtype, itemType, keywords,
			Pagination.of(1, 500));

		return (List<ContentSet>)page.getItems();
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	private void _registerInfoCollectionProvider(
		InfoCollectionProvider<?> infoCollectionProvider) {

		Dictionary<String, Object> properties = new Hashtable<>();

		properties.put(
			"item.class.name",
			infoCollectionProvider.getCollectionItemClassName());

		_serviceRegistrations.add(
			_bundleContext.registerService(
				(Class<InfoCollectionProvider<?>>)
					(Class)InfoCollectionProvider.class,
				infoCollectionProvider, properties));
	}

	@SuppressWarnings("rawtypes")
	private void _registerRelatedInfoItemCollectionProvider(
		RelatedInfoItemCollectionProvider relatedInfoItemCollectionProvider) {

		_serviceRegistrations.add(
			_bundleContext.registerService(
				RelatedInfoItemCollectionProvider.class,
				relatedInfoItemCollectionProvider, null));
	}

	@SuppressWarnings("rawtypes")
	private void _registerRepeatableFieldsInfoItemFormProvider(
		RepeatableFieldsInfoItemFormProvider
			repeatableFieldsInfoItemFormProvider) {

		_serviceRegistrations.add(
			_bundleContext.registerService(
				RepeatableFieldsInfoItemFormProvider.class,
				repeatableFieldsInfoItemFormProvider, null));
	}

	private void _testGetSiteContentSetsPageExcludesUnavailableProviders()
		throws Exception {

		String title1 = RandomTestUtil.randomString();
		String title2 = RandomTestUtil.randomString();

		_registerInfoCollectionProvider(
			new TestInfoCollectionProvider(
				true, null, Object.class.getName(),
				RandomTestUtil.randomString(), title1));
		_registerInfoCollectionProvider(
			new TestInfoCollectionProvider(
				false, null, Object.class.getName(),
				RandomTestUtil.randomString(), title2));

		List<ContentSet> contentSets = _getSiteContentSets(null, null, null);

		Assert.assertNotNull(_findByTitle(contentSets, title1));
		Assert.assertNull(_findByTitle(contentSets, title2));
	}

	private void _testGetSiteContentSetsPageFiltersCollectionsByItemSubtype()
		throws Exception {

		String itemSubtype = RandomTestUtil.randomString();
		String itemType = RandomTestUtil.randomString();
		String matchingTitle = RandomTestUtil.randomString();
		String nonmatchingTitle = RandomTestUtil.randomString();

		AssetListEntry matchingAssetListEntry = _addAssetListEntry(
			testGroup.getGroupId(), matchingTitle);

		matchingAssetListEntry.setAssetEntrySubtype(itemSubtype);
		matchingAssetListEntry.setAssetEntryType(itemType);

		_assetListEntryLocalService.updateAssetListEntry(
			matchingAssetListEntry);

		AssetListEntry nonmatchingAssetListEntry = _addAssetListEntry(
			testGroup.getGroupId(), nonmatchingTitle);

		nonmatchingAssetListEntry.setAssetEntrySubtype(
			RandomTestUtil.randomString());
		nonmatchingAssetListEntry.setAssetEntryType(itemType);

		_assetListEntryLocalService.updateAssetListEntry(
			nonmatchingAssetListEntry);

		List<ContentSet> contentSets = _getSiteContentSets(
			itemSubtype, itemType, null);

		Assert.assertNotNull(_findByTitle(contentSets, matchingTitle));
		Assert.assertNull(_findByTitle(contentSets, nonmatchingTitle));
	}

	private void _testGetSiteContentSetsPageWithCollection() throws Exception {
		String title = RandomTestUtil.randomString();

		_addAssetListEntry(testGroup.getGroupId(), title);

		ContentSet contentSet = _findByTitle(
			_getSiteContentSets(null, null, null), title);

		Assert.assertNotNull(contentSet);
		Assert.assertEquals(
			ContentSet.CollectionType.ASSET_LIST_COLLECTION,
			contentSet.getCollectionType());

		AssetListCollection assetListCollection =
			(AssetListCollection)contentSet;

		Assert.assertNotNull(assetListCollection.getClassPK());
		Assert.assertNotNull(assetListCollection.getExternalReferenceCode());
	}

	private void _testGetSiteContentSetsPageWithCollectionProvider()
		throws Exception {

		String itemSubtype = RandomTestUtil.randomString();
		String key = RandomTestUtil.randomString();
		String title = RandomTestUtil.randomString();

		_registerInfoCollectionProvider(
			new TestInfoCollectionProvider(
				true, itemSubtype, Object.class.getName(), key, title));

		ContentSet contentSet = _findByTitle(
			_getSiteContentSets(null, null, null), title);

		Assert.assertNotNull(contentSet);
		Assert.assertEquals(
			ContentSet.CollectionType.COLLECTION_PROVIDER,
			contentSet.getCollectionType());

		CollectionProvider collectionProvider = (CollectionProvider)contentSet;

		Assert.assertEquals(itemSubtype, collectionProvider.getItemSubtype());
		Assert.assertEquals(
			Object.class.getName(), collectionProvider.getItemType());
		Assert.assertEquals(key, collectionProvider.getKey());
	}

	private void _testGetSiteContentSetsPageWithKeywords() throws Exception {
		String title1 = RandomTestUtil.randomString();
		String title2 = RandomTestUtil.randomString();

		_registerInfoCollectionProvider(
			new TestInfoCollectionProvider(
				true, null, Object.class.getName(),
				RandomTestUtil.randomString(), title1));
		_registerInfoCollectionProvider(
			new TestInfoCollectionProvider(
				true, null, Object.class.getName(),
				RandomTestUtil.randomString(), title2));

		List<ContentSet> contentSets = _getSiteContentSets(null, null, title1);

		Assert.assertNotNull(_findByTitle(contentSets, title1));
		Assert.assertNull(_findByTitle(contentSets, title2));
	}

	private void _testGetSiteContentSetsPageWithRelatedCollectionProvider()
		throws Exception {

		String key = RandomTestUtil.randomString();
		String title = RandomTestUtil.randomString();

		_registerRelatedInfoItemCollectionProvider(
			new TestRelatedInfoItemCollectionProvider(true, key, title));

		ContentSet contentSet = _findByTitle(
			_getSiteContentSets(null, TestSourceItem.class.getName(), null),
			title);

		Assert.assertNotNull(contentSet);
		Assert.assertEquals(
			ContentSet.CollectionType.RELATED_COLLECTION_PROVIDER,
			contentSet.getCollectionType());

		RelatedCollectionProvider relatedCollectionProvider =
			(RelatedCollectionProvider)contentSet;

		Assert.assertEquals(key, relatedCollectionProvider.getKey());
		Assert.assertEquals(
			TestSourceItem.class.getName(),
			relatedCollectionProvider.getSourceItemType());
	}

	private void _testGetSiteContentSetsPageWithRepeatableField()
		throws Exception {

		String fieldSetName = RandomTestUtil.randomString();
		String repeatableFieldName = RandomTestUtil.randomString();
		String unrepeatableFieldName = RandomTestUtil.randomString();

		_registerRepeatableFieldsInfoItemFormProvider(
			new TestRepeatableFieldsInfoItemFormProvider(
				fieldSetName, repeatableFieldName, unrepeatableFieldName));

		String itemSubtype = RandomTestUtil.randomString();

		List<ContentSet> contentSets = _getSiteContentSets(
			itemSubtype, TestRepeatableItem.class.getName(), null);

		ContentSet contentSet = _findByTitle(contentSets, repeatableFieldName);

		Assert.assertNotNull(contentSet);
		Assert.assertEquals(
			ContentSet.CollectionType.REPEATABLE_FIELDS_COLLECTION_PROVIDER,
			contentSet.getCollectionType());

		RepeatableFieldsCollectionProvider repeatableFieldsCollectionProvider =
			(RepeatableFieldsCollectionProvider)contentSet;

		Assert.assertNotNull(repeatableFieldsCollectionProvider.getFieldName());
		Assert.assertEquals(
			itemSubtype, repeatableFieldsCollectionProvider.getItemSubtype());
		Assert.assertEquals(
			TestRepeatableItem.class.getName(),
			repeatableFieldsCollectionProvider.getItemType());
		Assert.assertEquals(
			RepeatableFieldInfoItemCollectionProvider.class.getName(),
			repeatableFieldsCollectionProvider.getKey());

		Assert.assertNull(_findByTitle(contentSets, unrepeatableFieldName));

		ContentSet fieldSetContentSet = _findByTitle(contentSets, fieldSetName);

		Assert.assertNotNull(fieldSetContentSet);
		Assert.assertEquals(
			ContentSet.CollectionType.REPEATABLE_FIELDS_COLLECTION_PROVIDER,
			fieldSetContentSet.getCollectionType());
	}

	private ContentSet _toClientDTO(AssetListEntry assetListEntry) {
		AssetListCollection assetListCollection = new AssetListCollection();

		assetListCollection.setCollectionType(
			ContentSet.CollectionType.ASSET_LIST_COLLECTION);
		assetListCollection.setItemSubtype(
			assetListEntry.getAssetEntrySubtype());
		assetListCollection.setItemType(assetListEntry.getAssetEntryType());
		assetListCollection.setTitle(assetListEntry.getTitle());

		return assetListCollection;
	}

	@Inject
	private AssetListEntryLocalService _assetListEntryLocalService;

	private BundleContext _bundleContext;
	private List<ServiceRegistration<?>> _serviceRegistrations;

	private static class TestCollectionItem {
	}

	private static class TestInfoCollectionProvider
		implements SingleFormVariationInfoCollectionProvider<Object> {

		public TestInfoCollectionProvider(
			boolean available, String itemSubtype, String itemType, String key,
			String title) {

			_available = available;
			_itemSubtype = itemSubtype;
			_itemType = itemType;
			_key = key;
			_title = title;
		}

		@Override
		public InfoPage<Object> getCollectionInfoPage(
			CollectionQuery collectionQuery) {

			return InfoPage.of(Collections.emptyList());
		}

		@Override
		public String getCollectionItemClassName() {
			return _itemType;
		}

		@Override
		public String getFormVariationKey() {
			return _itemSubtype;
		}

		@Override
		public String getKey() {
			return _key;
		}

		@Override
		public String getLabel(Locale locale) {
			return _title;
		}

		@Override
		public boolean isAvailable() {
			return _available;
		}

		private final boolean _available;
		private final String _itemSubtype;
		private final String _itemType;
		private final String _key;
		private final String _title;

	}

	private static class TestRelatedInfoItemCollectionProvider
		implements RelatedInfoItemCollectionProvider
			<TestSourceItem, TestCollectionItem> {

		public TestRelatedInfoItemCollectionProvider(
			boolean available, String key, String title) {

			_available = available;
			_key = key;
			_title = title;
		}

		@Override
		public InfoPage<TestCollectionItem> getCollectionInfoPage(
			CollectionQuery collectionQuery) {

			return InfoPage.of(Collections.emptyList());
		}

		@Override
		public String getKey() {
			return _key;
		}

		@Override
		public String getLabel(Locale locale) {
			return _title;
		}

		@Override
		public boolean isAvailable() {
			return _available;
		}

		private final boolean _available;
		private final String _key;
		private final String _title;

	}

	private static class TestRepeatableFieldsInfoItemFormProvider
		implements RepeatableFieldsInfoItemFormProvider<TestRepeatableItem> {

		public TestRepeatableFieldsInfoItemFormProvider(
			String fieldSetName, String repeatableFieldName,
			String unrepeatableFieldName) {

			_fieldSetName = fieldSetName;
			_repeatableFieldName = repeatableFieldName;
			_unrepeatableFieldName = unrepeatableFieldName;
		}

		@Override
		public InfoForm getInfoForm() {
			return null;
		}

		@Override
		public InfoForm getRepeatableFieldsInfoForm(String formVariationKey) {
			return InfoForm.builder(
			).infoFieldSetEntry(
				infoFieldSetEntryUnsafeConsumer -> {
					infoFieldSetEntryUnsafeConsumer.accept(
						InfoField.builder(
						).infoFieldType(
							TextInfoFieldType.INSTANCE
						).namespace(
							TestRepeatableItem.class.getName()
						).name(
							_repeatableFieldName
						).labelInfoLocalizedValue(
							(InfoLocalizedValue<String>)
								InfoLocalizedValue.function(
									locale -> _repeatableFieldName)
						).repeatable(
							true
						).build());

					infoFieldSetEntryUnsafeConsumer.accept(
						InfoField.builder(
						).infoFieldType(
							TextInfoFieldType.INSTANCE
						).namespace(
							TestRepeatableItem.class.getName()
						).name(
							_unrepeatableFieldName
						).labelInfoLocalizedValue(
							(InfoLocalizedValue<String>)
								InfoLocalizedValue.function(
									locale -> _unrepeatableFieldName)
						).build());

					infoFieldSetEntryUnsafeConsumer.accept(
						InfoFieldSet.builder(
						).infoFieldSetEntry(
							InfoField.builder(
							).infoFieldType(
								TextInfoFieldType.INSTANCE
							).namespace(
								TestRepeatableItem.class.getName()
							).name(
								_fieldSetName + "Field"
							).labelInfoLocalizedValue(
								(InfoLocalizedValue<String>)
									InfoLocalizedValue.function(
										locale -> _fieldSetName + "Field")
							).build()
						).labelInfoLocalizedValue(
							(InfoLocalizedValue<String>)
								InfoLocalizedValue.function(
									locale -> _fieldSetName)
						).name(
							_fieldSetName
						).build());
				}
			).build();
		}

		private final String _fieldSetName;
		private final String _repeatableFieldName;
		private final String _unrepeatableFieldName;

	}

	private static class TestRepeatableItem {
	}

	private static class TestSourceItem {
	}

}