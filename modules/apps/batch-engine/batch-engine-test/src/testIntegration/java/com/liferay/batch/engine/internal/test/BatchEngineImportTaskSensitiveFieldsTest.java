/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.batch.engine.internal.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.batch.engine.BaseBatchEngineTaskItemDelegate;
import com.liferay.batch.engine.BatchEngineImportTaskExecutor;
import com.liferay.batch.engine.BatchEngineTaskExecuteStatus;
import com.liferay.batch.engine.BatchEngineTaskItemDelegate;
import com.liferay.batch.engine.BatchEngineTaskOperation;
import com.liferay.batch.engine.constants.BatchEngineImportTaskConstants;
import com.liferay.batch.engine.model.BatchEngineImportTask;
import com.liferay.batch.engine.pagination.Page;
import com.liferay.batch.engine.pagination.Pagination;
import com.liferay.batch.engine.service.BatchEngineImportTaskLocalService;
import com.liferay.petra.io.StreamUtil;
import com.liferay.portal.kernel.search.Sort;
import com.liferay.portal.kernel.search.filter.Filter;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.util.HashMapDictionary;
import com.liferay.portal.kernel.util.PropsValues;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.odata.entity.EntityModel;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.Serializable;

import java.nio.charset.StandardCharsets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceRegistration;

/**
 * @author Luis Ortiz
 */
@RunWith(Arquillian.class)
public class BatchEngineImportTaskSensitiveFieldsTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new LiferayIntegrationTestRule();

	@Before
	public void setUp() throws Exception {
		Bundle bundle = FrameworkUtil.getBundle(
			BatchEngineImportTaskSensitiveFieldsTest.class);

		BundleContext bundleContext = bundle.getBundleContext();

		_serviceRegistration = bundleContext.registerService(
			BatchEngineTaskItemDelegate.class.getName(),
			new SensitiveFieldBatchEngineTaskItemDelegate(),
			new HashMapDictionary<String, String>());
	}

	@After
	public void tearDown() throws Exception {
		_serviceRegistration.unregister();
	}

	@Test
	public void testArticleBodyIsStoredEncryptedAndReadBackDecrypted()
		throws Exception {

		Assume.assumeTrue(
			Validator.isNotNull(PropsValues.BATCH_ENGINE_ENCRYPTION_KEY));

		String articleBody = RandomTestUtil.randomString();

		BatchEngineImportTask batchEngineImportTask = _addBatchEngineImportTask(
			articleBody);

		Assert.assertFalse(
			_readContent(
				batchEngineImportTask
			).contains(
				articleBody
			));

		_batchEngineImportTaskExecutor.execute(batchEngineImportTask);

		Assert.assertEquals(
			Collections.singletonList(articleBody), _articleBodies);
	}

	@Test
	public void testTheRestOfThePayloadIsStoredReadable() throws Exception {
		Assume.assumeTrue(
			Validator.isNotNull(PropsValues.BATCH_ENGINE_ENCRYPTION_KEY));

		String headline = RandomTestUtil.randomString();

		BatchEngineImportTask batchEngineImportTask = _addBatchEngineImportTask(
			RandomTestUtil.randomString(), headline);

		Assert.assertTrue(
			_readContent(
				batchEngineImportTask
			).contains(
				headline
			));
	}

	public class SensitiveFieldBatchEngineTaskItemDelegate
		extends BaseBatchEngineTaskItemDelegate<BlogPosting> {

		@Override
		public void create(
				Collection<BlogPosting> blogPostings,
				Map<String, Serializable> parameters)
			throws Exception {

			for (BlogPosting blogPosting : blogPostings) {
				_articleBodies.add(blogPosting.getArticleBody());
			}
		}

		@Override
		public EntityModel getEntityModel(
				Map<String, List<String>> multivaluedMap)
			throws Exception {

			return null;
		}

		@Override
		public Set<String> getSensitiveFieldNames() {
			return Collections.singleton("articleBody");
		}

		@Override
		public Page<BlogPosting> read(
				Filter filter, Pagination pagination, Sort[] sorts,
				Map<String, Serializable> parameters, String search)
			throws Exception {

			return Page.of(Collections.emptyList());
		}

	}

	private BatchEngineImportTask _addBatchEngineImportTask(String articleBody)
		throws Exception {

		return _addBatchEngineImportTask(
			articleBody, RandomTestUtil.randomString());
	}

	private BatchEngineImportTask _addBatchEngineImportTask(
			String articleBody, String headline)
		throws Exception {

		String json = StringUtil.replace(
			"[{\"articleBody\": \"[$ARTICLE_BODY$]\", \"headline\": " +
				"\"[$HEADLINE$]\"}]",
			new String[] {"[$ARTICLE_BODY$]", "[$HEADLINE$]"},
			new String[] {articleBody, headline});

		return _batchEngineImportTaskLocalService.addBatchEngineImportTask(
			null, TestPropsValues.getCompanyId(), TestPropsValues.getUserId(),
			10, null, BlogPosting.class.getName(),
			_compressContent(json.getBytes(StandardCharsets.UTF_8)), "JSON",
			BatchEngineTaskExecuteStatus.INITIAL.name(), null,
			BatchEngineImportTaskConstants.IMPORT_STRATEGY_ON_ERROR_FAIL,
			BatchEngineTaskOperation.CREATE.toString(), new HashMap<>(), null);
	}

	private byte[] _compressContent(byte[] content) throws Exception {
		try (ByteArrayOutputStream byteArrayOutputStream =
				new ByteArrayOutputStream()) {

			try (ZipOutputStream zipOutputStream = new ZipOutputStream(
					byteArrayOutputStream)) {

				zipOutputStream.putNextEntry(new ZipEntry("import.json"));

				zipOutputStream.write(content, 0, content.length);
			}

			return byteArrayOutputStream.toByteArray();
		}
	}

	private String _readContent(BatchEngineImportTask batchEngineImportTask)
		throws Exception {

		try (InputStream inputStream =
				_batchEngineImportTaskLocalService.openContentInputStream(
					batchEngineImportTask.getBatchEngineImportTaskId());

			ZipInputStream zipInputStream = new ZipInputStream(inputStream)) {

			zipInputStream.getNextEntry();

			return StreamUtil.toString(
				zipInputStream, StandardCharsets.UTF_8.name());
		}
	}

	private final List<String> _articleBodies = new ArrayList<>();

	@Inject
	private BatchEngineImportTaskExecutor _batchEngineImportTaskExecutor;

	@Inject
	private BatchEngineImportTaskLocalService
		_batchEngineImportTaskLocalService;

	private ServiceRegistration<?> _serviceRegistration;

}