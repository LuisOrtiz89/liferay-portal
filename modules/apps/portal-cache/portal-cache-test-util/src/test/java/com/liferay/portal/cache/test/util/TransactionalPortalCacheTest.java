/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.cache.test.util;

import com.liferay.petra.lang.SafeCloseable;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.cache.MVCCPortalCache;
import com.liferay.portal.cache.TransactionalPortalCache;
import com.liferay.portal.kernel.cache.PortalCache;
import com.liferay.portal.kernel.cache.PortalCacheHelperUtil;
import com.liferay.portal.kernel.cache.transactional.TransactionalPortalCacheUtil;
import com.liferay.portal.kernel.model.MVCCModel;
import com.liferay.portal.kernel.security.auth.CompanyThreadLocal;
import com.liferay.portal.kernel.service.persistence.impl.BasePersistenceImpl;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.CodeCoverageAssertor;
import com.liferay.portal.kernel.test.util.PropsTestUtil;
import com.liferay.portal.kernel.transaction.Propagation;
import com.liferay.portal.kernel.transaction.TransactionAttribute;
import com.liferay.portal.kernel.transaction.TransactionLifecycleListener;
import com.liferay.portal.kernel.transaction.TransactionStatus;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.lang.reflect.InvocationTargetException;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.MockedStatic;
import org.mockito.Mockito;

/**
 * @author Shuyang Zhou
 */
public class TransactionalPortalCacheTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new CodeCoverageAssertor() {

				@Override
				public void appendAssertClasses(List<Class<?>> assertClasses) {
					assertClasses.add(TransactionalPortalCache.class);

					Class<TransactionalPortalCacheUtil> clazz =
						TransactionalPortalCacheUtil.class;

					assertClasses.add(clazz);

					Collections.addAll(
						assertClasses, clazz.getDeclaredClasses());

					TransactionLifecycleListener transactionLifecycleListener =
						TransactionalPortalCacheUtil.
							TRANSACTION_LIFECYCLE_LISTENER;

					assertClasses.add(transactionLifecycleListener.getClass());
				}

			},
			LiferayUnitTestRule.INSTANCE);

	@Before
	public void setUp() {
		_portalCache = new TestPortalCache<>("Test Portal Cache");
		_shardedPortalCache = new TestShardedPortalCache<>(
			"Test Sharded Portal Cache");

		_testCacheListener = new TestPortalCacheListener<>();
		_testCacheReplicator = new TestPortalCacheReplicator<>();

		_portalCache.registerPortalCacheListener(_testCacheListener);
		_portalCache.registerPortalCacheListener(_testCacheReplicator);

		_testShardedCacheListener = new TestPortalCacheListener<>();
		_testShardedCacheReplicator = new TestPortalCacheReplicator<>();

		_shardedPortalCache.registerPortalCacheListener(
			_testShardedCacheListener);
		_shardedPortalCache.registerPortalCacheListener(
			_testShardedCacheReplicator);

		_companyThreadLocalMockedStatic.when(
			CompanyThreadLocal::getCompanyId
		).thenReturn(
			_TEST_COMPANY_ID
		);
		_companyThreadLocalMockedStatic.when(
			() -> CompanyThreadLocal.lock(_TEST_COMPANY_ID)
		).thenReturn(
			Mockito.mock(SafeCloseable.class)
		);
	}

	@Test
	public void testConcurrentTransactionForMVCCPortalCache() throws Exception {
		_setupAndTestConcurrentTransactionForMVCCPortalCache(
			_portalCache, _testCacheListener, _testCacheReplicator);
		_setupAndTestConcurrentTransactionForMVCCPortalCache(
			_shardedPortalCache, _testShardedCacheListener,
			_testShardedCacheReplicator);
	}

	@Test
	public void testConcurrentTransactionForNonmvccPortalCache()
		throws Exception {

		_setupAndTestConcurrentTransactionForNonmvccPortalCache(
			_portalCache, _testCacheListener, _testCacheReplicator);
		_setupAndTestConcurrentTransactionForNonmvccPortalCache(
			_shardedPortalCache, _testShardedCacheListener,
			_testShardedCacheReplicator);
	}

	@Test
	public void testMisc() {
		_setupAndTestMisc(_portalCache);
		_setupAndTestMisc(_shardedPortalCache);
	}

	@Test
	public void testNoneTransactionalCache() {
		_setupAndTestNoneTransactionalCache(
			_portalCache, _testCacheListener, _testCacheReplicator);
		_setupAndTestNoneTransactionalCache(
			_shardedPortalCache, _testShardedCacheListener,
			_testShardedCacheReplicator);
	}

	@Test
	public void testTransactionalCache()
		throws ClassNotFoundException, IllegalAccessException,
			   InvocationTargetException, NoSuchMethodException {

		_setupAndTestTransactionalCache(
			_portalCache, _testCacheListener, _testCacheReplicator);
		_setupAndTestTransactionalCache(
			_shardedPortalCache, _testShardedCacheListener,
			_testShardedCacheReplicator);
	}

	@Test
	public void testTransactionalCacheWithParameterValidation() {
		_setupAndTestTransactionalCacheWithParameterValidation(_portalCache);
		_setupAndTestTransactionalCacheWithParameterValidation(
			_shardedPortalCache);
	}

	@Test
	public void testTransactionalPortalCacheUtilEnabled() {
		_setEnableTransactionalCache(false);

		Assert.assertFalse(
			"TransactionalPortalCacheUtil should be disabled",
			TransactionalPortalCacheUtil.isEnabled());

		_setEnableTransactionalCache(true);

		Assert.assertFalse(
			"TransactionalPortalCacheUtil should be disabled",
			TransactionalPortalCacheUtil.isEnabled());

		TransactionalPortalCacheUtil.begin();

		Assert.assertTrue(
			"TransactionalPortalCacheUtil should be enabled",
			TransactionalPortalCacheUtil.isEnabled());

		TransactionalPortalCacheUtil.commit(false);

		ReflectionTestUtil.setFieldValue(
			TransactionalPortalCacheUtil.class, "_transactionalCacheEnabled",
			null);

		PropsTestUtil.setProps(Collections.emptyMap());

		Assert.assertFalse(
			"TransactionalPortalCacheUtil should be disabled",
			TransactionalPortalCacheUtil.isEnabled());
	}

	@Test
	public void testTransactionalPortalCacheWithRealMVCCPortalCache() {
		_setEnableTransactionalCache(true);

		TransactionalPortalCache<String, MVCCModel> transactionalPortalCache =
			new TransactionalPortalCache<>(
				new MVCCPortalCache<>(
					new TestPortalCache<>("Test MVCC Portal Cache")),
				true);

		// Put real value and commit

		TransactionalPortalCacheUtil.begin();

		MockMVCCModel mockMVCCModel = new MockMVCCModel(0);

		transactionalPortalCache.put(_KEY_1, mockMVCCModel);

		TransactionalPortalCacheUtil.commit(false);

		Assert.assertSame(mockMVCCModel, transactionalPortalCache.get(_KEY_1));

		// Remove, put NullModel and commit

		TransactionalPortalCacheUtil.begin();

		transactionalPortalCache.remove(_KEY_1);

		MVCCModel nullMVCCModel = ReflectionTestUtil.getFieldValue(
			BasePersistenceImpl.class, "nullModel");

		transactionalPortalCache.put(_KEY_1, nullMVCCModel);

		TransactionalPortalCacheUtil.commit(false);

		Assert.assertSame(nullMVCCModel, transactionalPortalCache.get(_KEY_1));
	}

	@Test
	public void testTransactionLifecycleListenerEnabledWithBarrier() {
		_setEnableTransactionalCache(true);

		_testTransactionLifecycleListenerEnabledWithBarrier(
			Propagation.NOT_SUPPORTED);
		_testTransactionLifecycleListenerEnabledWithBarrier(Propagation.NEVER);
		_testTransactionLifecycleListenerEnabledWithBarrier(Propagation.NESTED);
	}

	@Test
	public void testTransactionLifecycleListenerEnabledWithExistTransaction() {
		_setEnableTransactionalCache(true);

		Assert.assertEquals(0, _getTransactionStackSize());

		TransactionLifecycleListener transactionLifecycleListener =
			TransactionalPortalCacheUtil.TRANSACTION_LIFECYCLE_LISTENER;

		TransactionAttribute.Builder builder =
			new TransactionAttribute.Builder();

		TransactionAttribute transactionAttribute = builder.build();

		TransactionStatus transactionStatus = new TestTransactionStatus(
			false, false, false);

		transactionLifecycleListener.created(
			transactionAttribute, transactionStatus);

		Assert.assertEquals(0, _getTransactionStackSize());

		transactionLifecycleListener.committed(
			transactionAttribute, transactionStatus);

		Assert.assertEquals(0, _getTransactionStackSize());

		transactionLifecycleListener.created(
			transactionAttribute, transactionStatus);

		Assert.assertEquals(0, _getTransactionStackSize());

		transactionLifecycleListener.rollbacked(
			transactionAttribute, transactionStatus, null);

		Assert.assertEquals(0, _getTransactionStackSize());
	}

	@Test
	public void testTransactionLifecycleListenerEnabledWithoutBarrier() {
		_setEnableTransactionalCache(true);

		_testTransactionLifecycleListenerEnabledWithoutBarrier(
			Propagation.REQUIRED);
		_testTransactionLifecycleListenerEnabledWithoutBarrier(
			Propagation.SUPPORTS);
		_testTransactionLifecycleListenerEnabledWithoutBarrier(
			Propagation.MANDATORY);
		_testTransactionLifecycleListenerEnabledWithoutBarrier(
			Propagation.REQUIRES_NEW);
	}

	private int _getTransactionStackSize() {
		ThreadLocal<List<?>> portalCacheMapsThreadLocal =
			ReflectionTestUtil.getFieldValue(
				TransactionalPortalCacheUtil.class,
				"_portalCacheMapsThreadLocal");

		List<?> portalCacheMaps = portalCacheMapsThreadLocal.get();

		return portalCacheMaps.size();
	}

	private void _invokeTransactionalPortalCacheConcurrently(
			TransactionalPortalCache<String, String> transactionalPortalCache,
			String key1, String value1, boolean readOnly1, String key2,
			String value2, boolean readOnly2, boolean skipReplicator)
		throws Exception {

		Thread currentThread = Thread.currentThread();

		StackTraceElement[] stackTraceElements = currentThread.getStackTrace();

		StackTraceElement stackTraceElement = stackTraceElements[2];

		String threadNamePrefix = StringBundler.concat(
			stackTraceElement.getClassName(), StringPool.UNDERLINE,
			stackTraceElement.getMethodName(), "_LineNumber_",
			stackTraceElement.getLineNumber());

		TestCallable testCallable1 = new TestCallable(
			transactionalPortalCache, key1, value1, readOnly1, skipReplicator);
		TestCallable testCallable2 = new TestCallable(
			transactionalPortalCache, key2, value2, readOnly2, skipReplicator);

		FutureTask<Void> futureTask1 = new FutureTask<>(testCallable1);
		FutureTask<Void> futureTask2 = new FutureTask<>(testCallable2);

		Thread thread1 = new Thread(
			futureTask1, threadNamePrefix + "_Thread_1");
		Thread thread2 = new Thread(
			futureTask2, threadNamePrefix + "_Thread_2");

		thread1.start();
		thread2.start();

		testCallable1.waitUntilBlock();
		testCallable2.waitUntilBlock();

		testCallable1.unblock();
		futureTask1.get();

		testCallable2.unblock();
		futureTask2.get();
	}

	private void _setEnableTransactionalCache(boolean enabled) {
		ReflectionTestUtil.setFieldValue(
			TransactionalPortalCacheUtil.class, "_transactionalCacheEnabled",
			enabled);
	}

	private void _setupAndTestConcurrentTransactionForMVCCPortalCache(
			PortalCache<String, String> portalCache,
			TestPortalCacheListener<String, String> testPortalCacheListener,
			TestPortalCacheReplicator<String, String> testPortalCacheReplicator)
		throws Exception {

		_setEnableTransactionalCache(true);

		TransactionalPortalCache<String, String> transactionalPortalCache =
			new TransactionalPortalCache<>(portalCache, true);

		// Two read only transactions do put

		_invokeTransactionalPortalCacheConcurrently(
			transactionalPortalCache, _KEY_1, _VALUE_1, true, _KEY_2, _VALUE_2,
			true, false);

		testPortalCacheListener.assertPut(_KEY_1, _VALUE_1);
		testPortalCacheListener.assertPut(_KEY_2, _VALUE_2);
		testPortalCacheListener.assertActionsCount(2);

		testPortalCacheReplicator.assertPut(_KEY_1, _VALUE_1);
		testPortalCacheReplicator.assertPut(_KEY_2, _VALUE_2);
		testPortalCacheReplicator.assertActionsCount(2);

		Assert.assertEquals(_VALUE_1, portalCache.get(_KEY_1));
		Assert.assertEquals(_VALUE_2, portalCache.get(_KEY_2));

		testPortalCacheListener.reset();
		testPortalCacheReplicator.reset();

		// Two read only transactions do remove

		_invokeTransactionalPortalCacheConcurrently(
			transactionalPortalCache, _KEY_1, null, true, _KEY_2, null, true,
			false);

		testPortalCacheListener.assertActionsCount(0);
		testPortalCacheReplicator.assertActionsCount(0);

		Assert.assertEquals(_VALUE_1, portalCache.get(_KEY_1));
		Assert.assertEquals(_VALUE_2, portalCache.get(_KEY_2));

		// One read only transaction and one write transaction do put

		_invokeTransactionalPortalCacheConcurrently(
			transactionalPortalCache, _KEY_1, _VALUE_2, true, _KEY_2, _VALUE_1,
			false, false);

		testPortalCacheListener.assertUpdated(_KEY_1, _VALUE_2);
		testPortalCacheListener.assertUpdated(_KEY_2, _VALUE_1);
		testPortalCacheListener.assertActionsCount(2);

		testPortalCacheReplicator.assertUpdated(_KEY_1, _VALUE_2);
		testPortalCacheReplicator.assertUpdated(_KEY_2, _VALUE_1);
		testPortalCacheReplicator.assertActionsCount(2);

		Assert.assertEquals(_VALUE_2, portalCache.get(_KEY_1));
		Assert.assertEquals(_VALUE_1, portalCache.get(_KEY_2));

		testPortalCacheListener.reset();
		testPortalCacheReplicator.reset();

		// One write transaction and one read only transaction do put

		_invokeTransactionalPortalCacheConcurrently(
			transactionalPortalCache, _KEY_1, _VALUE_1, false, _KEY_2, _VALUE_2,
			true, false);

		testPortalCacheListener.assertUpdated(_KEY_1, _VALUE_1);
		testPortalCacheListener.assertUpdated(_KEY_2, _VALUE_2);
		testPortalCacheListener.assertActionsCount(2);

		testPortalCacheReplicator.assertUpdated(_KEY_1, _VALUE_1);
		testPortalCacheListener.assertUpdated(_KEY_2, _VALUE_2);
		testPortalCacheReplicator.assertActionsCount(2);

		Assert.assertEquals(_VALUE_1, portalCache.get(_KEY_1));
		Assert.assertEquals(_VALUE_2, portalCache.get(_KEY_2));

		testPortalCacheListener.reset();
		testPortalCacheReplicator.reset();

		// Two write transactions do put

		_invokeTransactionalPortalCacheConcurrently(
			transactionalPortalCache, _KEY_1, _VALUE_2, false, _KEY_2, _VALUE_1,
			false, false);

		testPortalCacheListener.assertUpdated(_KEY_1, _VALUE_2);
		testPortalCacheListener.assertUpdated(_KEY_2, _VALUE_1);
		testPortalCacheListener.assertActionsCount(2);

		testPortalCacheReplicator.assertUpdated(_KEY_1, _VALUE_2);
		testPortalCacheReplicator.assertUpdated(_KEY_2, _VALUE_1);
		testPortalCacheReplicator.assertActionsCount(2);

		Assert.assertEquals(_VALUE_2, portalCache.get(_KEY_1));
		Assert.assertEquals(_VALUE_1, portalCache.get(_KEY_2));

		testPortalCacheListener.reset();
		testPortalCacheReplicator.reset();

		// Two write transactions do remove without replicator

		_invokeTransactionalPortalCacheConcurrently(
			transactionalPortalCache, _KEY_1, null, false, _KEY_2, null, false,
			true);

		testPortalCacheListener.assertRemoved(_KEY_1, _VALUE_2);
		testPortalCacheListener.assertRemoved(_KEY_2, _VALUE_1);
		testPortalCacheListener.assertActionsCount(2);

		testPortalCacheReplicator.assertActionsCount(0);

		Assert.assertNull(portalCache.get(_KEY_1));
		Assert.assertNull(portalCache.get(_KEY_2));
	}

	private void _setupAndTestConcurrentTransactionForNonmvccPortalCache(
			PortalCache<String, String> portalCache,
			TestPortalCacheListener<String, String> testPortalCacheListener,
			TestPortalCacheReplicator<String, String> testPortalCacheReplicator)
		throws Exception {

		_setEnableTransactionalCache(true);

		TransactionalPortalCache<String, String> transactionalPortalCache =
			new TransactionalPortalCache<>(portalCache, false);

		// Two read only transactions do put

		_invokeTransactionalPortalCacheConcurrently(
			transactionalPortalCache, _KEY_1, _VALUE_1, true, _KEY_2, _VALUE_2,
			true, false);

		testPortalCacheListener.assertPut(_KEY_1, _VALUE_1);
		testPortalCacheListener.assertPut(_KEY_2, _VALUE_2);
		testPortalCacheListener.assertActionsCount(2);

		testPortalCacheReplicator.assertPut(_KEY_1, _VALUE_1);
		testPortalCacheReplicator.assertPut(_KEY_2, _VALUE_2);
		testPortalCacheReplicator.assertActionsCount(2);

		Assert.assertEquals(_VALUE_1, portalCache.get(_KEY_1));
		Assert.assertEquals(_VALUE_2, portalCache.get(_KEY_2));

		testPortalCacheListener.reset();
		testPortalCacheReplicator.reset();

		// Two read only transactions do remove

		_invokeTransactionalPortalCacheConcurrently(
			transactionalPortalCache, _KEY_1, null, true, _KEY_2, null, true,
			false);

		testPortalCacheListener.assertActionsCount(0);
		testPortalCacheReplicator.assertActionsCount(0);

		Assert.assertEquals(_VALUE_1, portalCache.get(_KEY_1));
		Assert.assertEquals(_VALUE_2, portalCache.get(_KEY_2));

		// One read only transaction and one write transaction do put

		_invokeTransactionalPortalCacheConcurrently(
			transactionalPortalCache, _KEY_1, _VALUE_2, true, _KEY_2, _VALUE_1,
			false, false);

		testPortalCacheListener.assertUpdated(_KEY_1, _VALUE_2);
		testPortalCacheListener.assertUpdated(_KEY_2, _VALUE_1);
		testPortalCacheListener.assertActionsCount(2);

		testPortalCacheReplicator.assertUpdated(_KEY_1, _VALUE_2);
		testPortalCacheReplicator.assertUpdated(_KEY_2, _VALUE_1);
		testPortalCacheReplicator.assertActionsCount(2);

		Assert.assertEquals(_VALUE_2, portalCache.get(_KEY_1));
		Assert.assertEquals(_VALUE_1, portalCache.get(_KEY_2));

		testPortalCacheListener.reset();
		testPortalCacheReplicator.reset();

		// One write transaction and one read only transaction do put

		_invokeTransactionalPortalCacheConcurrently(
			transactionalPortalCache, _KEY_1, _VALUE_1, false, _KEY_2, _VALUE_2,
			true, false);

		testPortalCacheListener.assertUpdated(_KEY_1, _VALUE_1);
		testPortalCacheListener.assertActionsCount(1);

		testPortalCacheReplicator.assertUpdated(_KEY_1, _VALUE_1);
		testPortalCacheReplicator.assertActionsCount(1);

		Assert.assertEquals(_VALUE_1, portalCache.get(_KEY_1));
		Assert.assertEquals(_VALUE_1, portalCache.get(_KEY_2));

		testPortalCacheListener.reset();
		testPortalCacheReplicator.reset();

		// Two write transactions do put

		_invokeTransactionalPortalCacheConcurrently(
			transactionalPortalCache, _KEY_1, _VALUE_2, false, _KEY_2, _VALUE_2,
			false, false);

		testPortalCacheListener.assertUpdated(_KEY_1, _VALUE_2);
		testPortalCacheListener.assertRemoved(_KEY_2, _VALUE_1);
		testPortalCacheListener.assertActionsCount(2);

		testPortalCacheReplicator.assertUpdated(_KEY_1, _VALUE_2);
		testPortalCacheReplicator.assertRemoved(_KEY_2, _VALUE_1);
		testPortalCacheReplicator.assertActionsCount(2);

		Assert.assertEquals(_VALUE_2, portalCache.get(_KEY_1));
		Assert.assertNull(portalCache.get(_KEY_2));

		testPortalCacheListener.reset();
		testPortalCacheReplicator.reset();

		// Two write transactions do remove without replicator

		_invokeTransactionalPortalCacheConcurrently(
			transactionalPortalCache, _KEY_1, null, false, _KEY_2, null, false,
			true);

		testPortalCacheListener.assertRemoved(_KEY_1, _VALUE_2);
		testPortalCacheListener.assertRemoved(_KEY_2, null);
		testPortalCacheListener.assertActionsCount(2);

		testPortalCacheReplicator.assertActionsCount(0);

		Assert.assertNull(portalCache.get(_KEY_1));
		Assert.assertNull(portalCache.get(_KEY_2));
	}

	private void _setupAndTestMisc(PortalCache<String, String> portalCache) {

		// For code coverage

		new TransactionalPortalCacheUtil();

		_setEnableTransactionalCache(true);

		TransactionalPortalCacheUtil.begin();

		TransactionalPortalCache<String, String> transactionalPortalCache =
			new TransactionalPortalCache(portalCache, false);

		TransactionalPortalCacheUtil.put(
			transactionalPortalCache, _KEY_1, _VALUE_1, 0, false);

		TransactionalPortalCacheUtil.removeAll(transactionalPortalCache, false);

		TransactionalPortalCacheUtil.commit(false);

		TransactionLifecycleListener transactionLifecycleListener =
			TransactionalPortalCacheUtil.TRANSACTION_LIFECYCLE_LISTENER;

		_setEnableTransactionalCache(false);

		transactionLifecycleListener.created(null, null);

		transactionLifecycleListener.committed(null, null);

		transactionLifecycleListener.rollbacked(null, null, null);
	}

	private void _setupAndTestNoneTransactionalCache(
		PortalCache<String, String> portalCache,
		TestPortalCacheListener<String, String> testPortalCacheListener,
		TestPortalCacheReplicator<String, String> testPortalCacheReplicator) {

		_setEnableTransactionalCache(false);

		Assert.assertFalse(
			"TransactionalPortalCacheUtil should be disabled",
			TransactionalPortalCacheUtil.isEnabled());

		// MVCC portal cache when transactional cache is disabled

		_testNoneTransactionalPortalCache(
			portalCache, testPortalCacheListener, testPortalCacheReplicator,
			new TransactionalPortalCache<>(portalCache, true));

		// Non MVCC portal cache when transactional cache is disabled

		_testNoneTransactionalPortalCache(
			portalCache, testPortalCacheListener, testPortalCacheReplicator,
			new TransactionalPortalCache<>(portalCache, false));

		// MVCC portal cache when not used in transaction

		_setEnableTransactionalCache(true);

		Assert.assertFalse(
			"TransactionalPortalCacheUtil should be disabled",
			TransactionalPortalCacheUtil.isEnabled());

		_testNoneTransactionalPortalCache(
			portalCache, testPortalCacheListener, testPortalCacheReplicator,
			new TransactionalPortalCache<>(portalCache, true));

		// Non MVCC portal cache when not used in transaction

		_testNoneTransactionalPortalCache(
			portalCache, testPortalCacheListener, testPortalCacheReplicator,
			new TransactionalPortalCache<>(portalCache, false));
	}

	private void _setupAndTestTransactionalCache(
			PortalCache<String, String> portalCache,
			TestPortalCacheListener<String, String> testPortalCacheListener,
			TestPortalCacheReplicator<String, String> testPortalCacheReplicator)
		throws ClassNotFoundException, IllegalAccessException,
			   InvocationTargetException, NoSuchMethodException {

		_setEnableTransactionalCache(true);

		// MVCC portal cache without ttl

		_testTransactionalPortalCache(
			portalCache, testPortalCacheListener, testPortalCacheReplicator,
			new TransactionalPortalCache<>(portalCache, true), false, true);

		// Non MVCC portal cache without ttl

		_testTransactionalPortalCache(
			portalCache, testPortalCacheListener, testPortalCacheReplicator,
			new TransactionalPortalCache<>(portalCache, false), false, false);

		// MVCC portal cache with ttl

		_testTransactionalPortalCache(
			portalCache, testPortalCacheListener, testPortalCacheReplicator,
			new TransactionalPortalCache<>(portalCache, true), true, true);

		// Non MVCC portal cache with ttl

		_testTransactionalPortalCache(
			portalCache, testPortalCacheListener, testPortalCacheReplicator,
			new TransactionalPortalCache<>(portalCache, false), true, false);
	}

	private void _setupAndTestTransactionalCacheWithParameterValidation(
		PortalCache<String, String> portalCache) {

		_setEnableTransactionalCache(true);

		TransactionalPortalCache<String, String> transactionalPortalCache =
			new TransactionalPortalCache<>(portalCache, true);

		portalCache.put(_KEY_1, _VALUE_1);

		TransactionalPortalCacheUtil.begin();

		// Get

		Assert.assertEquals(_VALUE_1, transactionalPortalCache.get(_KEY_1));
		Assert.assertEquals(_VALUE_1, portalCache.get(_KEY_1));

		// Get with null key

		try {
			transactionalPortalCache.get(null);

			Assert.fail("Should throw NullPointerException");
		}
		catch (NullPointerException nullPointerException) {
			Assert.assertEquals(
				"Key is null", nullPointerException.getMessage());
		}

		// Put

		transactionalPortalCache.put(_KEY_1, _VALUE_2);

		Assert.assertEquals(_VALUE_2, transactionalPortalCache.get(_KEY_1));
		Assert.assertEquals(_VALUE_1, portalCache.get(_KEY_1));

		// Put with null key

		try {
			transactionalPortalCache.put(null, _VALUE_1);

			Assert.fail("Should throw NullPointerException");
		}
		catch (NullPointerException nullPointerException) {
			Assert.assertEquals(
				"Key is null", nullPointerException.getMessage());
		}

		// Put with null value

		try {
			transactionalPortalCache.put(_KEY_1, null);

			Assert.fail("Should throw NullPointerException");
		}
		catch (NullPointerException nullPointerException) {
			Assert.assertEquals(
				"Value is null", nullPointerException.getMessage());
		}

		// Put with negative ttl

		try {
			transactionalPortalCache.put(_KEY_1, _VALUE_1, -1);

			Assert.fail("Should throw IllegalArgumentException");
		}
		catch (IllegalArgumentException illegalArgumentException) {
			Assert.assertEquals(
				"Time to live is negative",
				illegalArgumentException.getMessage());
		}

		// Remove

		transactionalPortalCache.remove(_KEY_1);

		Assert.assertNull(transactionalPortalCache.get(_KEY_1));
		Assert.assertEquals(_VALUE_1, portalCache.get(_KEY_1));

		// Remove with null key

		try {
			transactionalPortalCache.remove(null);

			Assert.fail("Should throw NullPointerException");
		}
		catch (NullPointerException nullPointerException) {
			Assert.assertEquals(
				"Key is null", nullPointerException.getMessage());
		}

		TransactionalPortalCacheUtil.commit(false);
	}

	private void _testNoneTransactionalPortalCache(
		PortalCache<String, String> portalCache,
		TestPortalCacheListener<String, String> testPortalCacheListener,
		TestPortalCacheReplicator<String, String> testPortalCacheReplicator,
		TransactionalPortalCache<String, String> transactionalPortalCache) {

		// Put 1

		transactionalPortalCache.put(_KEY_1, _VALUE_1);

		testPortalCacheListener.assertPut(_KEY_1, _VALUE_1);
		testPortalCacheListener.assertActionsCount(1);

		testPortalCacheReplicator.assertPut(_KEY_1, _VALUE_1);
		testPortalCacheReplicator.assertActionsCount(1);

		Assert.assertEquals(_VALUE_1, transactionalPortalCache.get(_KEY_1));
		Assert.assertEquals(_VALUE_1, portalCache.get(_KEY_1));

		testPortalCacheListener.reset();
		testPortalCacheReplicator.reset();

		// Put 2

		transactionalPortalCache.put(_KEY_1, _VALUE_2, 10);

		testPortalCacheListener.assertUpdated(_KEY_1, _VALUE_2, 10);
		testPortalCacheListener.assertActionsCount(1);

		testPortalCacheReplicator.assertUpdated(_KEY_1, _VALUE_2, 10);
		testPortalCacheReplicator.assertActionsCount(1);

		Assert.assertEquals(_VALUE_2, transactionalPortalCache.get(_KEY_1));
		Assert.assertEquals(_VALUE_2, portalCache.get(_KEY_1));

		testPortalCacheListener.reset();
		testPortalCacheReplicator.reset();

		// Put 3

		try {
			transactionalPortalCache.put(_KEY_1, _VALUE_2, -1);

			Assert.fail("Should throw IllegalArgumentException");
		}
		catch (IllegalArgumentException illegalArgumentException) {
			Assert.assertEquals(
				"Time to live is negative",
				illegalArgumentException.getMessage());
		}

		// Put 4

		PortalCacheHelperUtil.putWithoutReplicator(
			transactionalPortalCache, _KEY_1, _VALUE_1);

		testPortalCacheListener.assertUpdated(_KEY_1, _VALUE_1);
		testPortalCacheListener.assertActionsCount(1);

		testPortalCacheReplicator.assertActionsCount(0);

		Assert.assertEquals(_VALUE_1, transactionalPortalCache.get(_KEY_1));
		Assert.assertEquals(_VALUE_1, portalCache.get(_KEY_1));

		testPortalCacheListener.reset();

		// Put 5

		PortalCacheHelperUtil.putWithoutReplicator(
			transactionalPortalCache, _KEY_1, _VALUE_2, 10);

		testPortalCacheListener.assertUpdated(_KEY_1, _VALUE_2, 10);
		testPortalCacheListener.assertActionsCount(1);

		testPortalCacheReplicator.assertActionsCount(0);

		Assert.assertEquals(_VALUE_2, transactionalPortalCache.get(_KEY_1));
		Assert.assertEquals(_VALUE_2, portalCache.get(_KEY_1));

		testPortalCacheListener.reset();

		// Remove 1

		transactionalPortalCache.remove(_KEY_1);

		testPortalCacheListener.assertRemoved(_KEY_1, _VALUE_2);
		testPortalCacheListener.assertActionsCount(1);

		testPortalCacheReplicator.assertRemoved(_KEY_1, _VALUE_2);
		testPortalCacheReplicator.assertActionsCount(1);

		Assert.assertNull(transactionalPortalCache.get(_KEY_1));
		Assert.assertNull(portalCache.get(_KEY_1));

		testPortalCacheListener.reset();
		testPortalCacheReplicator.reset();

		// Remove 2

		PortalCacheHelperUtil.putWithoutReplicator(
			transactionalPortalCache, _KEY_1, _VALUE_1);
		PortalCacheHelperUtil.removeWithoutReplicator(
			transactionalPortalCache, _KEY_1);

		testPortalCacheListener.assertPut(_KEY_1, _VALUE_1);
		testPortalCacheListener.assertRemoved(_KEY_1, _VALUE_1);
		testPortalCacheListener.assertActionsCount(2);

		testPortalCacheReplicator.assertActionsCount(0);

		Assert.assertNull(transactionalPortalCache.get(_KEY_1));
		Assert.assertNull(portalCache.get(_KEY_1));

		testPortalCacheListener.reset();

		// Remove all 1

		transactionalPortalCache.put(_KEY_1, _VALUE_1);
		transactionalPortalCache.put(_KEY_2, _VALUE_2);

		transactionalPortalCache.removeAll();

		testPortalCacheListener.assertPut(_KEY_1, _VALUE_1);
		testPortalCacheListener.assertPut(_KEY_2, _VALUE_2);
		testPortalCacheListener.assertRemoveAll();
		testPortalCacheListener.assertActionsCount(3);

		testPortalCacheReplicator.assertPut(_KEY_1, _VALUE_1);
		testPortalCacheReplicator.assertPut(_KEY_2, _VALUE_2);
		testPortalCacheReplicator.assertRemoveAll();
		testPortalCacheReplicator.assertActionsCount(3);

		Assert.assertNull(transactionalPortalCache.get(_KEY_1));
		Assert.assertNull(transactionalPortalCache.get(_KEY_2));
		Assert.assertNull(portalCache.get(_KEY_1));
		Assert.assertNull(portalCache.get(_KEY_2));

		testPortalCacheListener.reset();
		testPortalCacheReplicator.reset();

		// Remove all 2

		transactionalPortalCache.put(_KEY_1, _VALUE_1);
		transactionalPortalCache.put(_KEY_2, _VALUE_2);

		PortalCacheHelperUtil.removeAllWithoutReplicator(
			transactionalPortalCache);

		testPortalCacheListener.assertPut(_KEY_1, _VALUE_1);
		testPortalCacheListener.assertPut(_KEY_2, _VALUE_2);
		testPortalCacheListener.assertRemoveAll();
		testPortalCacheListener.assertActionsCount(3);

		testPortalCacheReplicator.assertPut(_KEY_1, _VALUE_1);
		testPortalCacheReplicator.assertPut(_KEY_2, _VALUE_2);
		testPortalCacheReplicator.assertActionsCount(2);

		Assert.assertNull(transactionalPortalCache.get(_KEY_1));
		Assert.assertNull(transactionalPortalCache.get(_KEY_2));
		Assert.assertNull(portalCache.get(_KEY_1));
		Assert.assertNull(portalCache.get(_KEY_2));

		testPortalCacheListener.reset();
		testPortalCacheReplicator.reset();
	}

	private void _testTransactionalPortalCache(
			PortalCache<String, String> portalCache,
			TestPortalCacheListener<String, String> testPortalCacheListener,
			TestPortalCacheReplicator<String, String> testPortalCacheReplicator,
			TransactionalPortalCache<String, String> transactionalPortalCache,
			boolean ttl, boolean mvcc)
		throws ClassNotFoundException, IllegalAccessException,
			   InvocationTargetException, NoSuchMethodException {

		// Rollback

		TransactionalPortalCacheUtil.begin();

		if (ttl) {
			transactionalPortalCache.put(_KEY_1, _VALUE_1, 10);
		}
		else {
			transactionalPortalCache.put(_KEY_1, _VALUE_1);
		}

		Assert.assertEquals(_VALUE_1, transactionalPortalCache.get(_KEY_1));
		Assert.assertNull(transactionalPortalCache.get(_KEY_2));
		Assert.assertNull(portalCache.get(_KEY_1));

		TransactionalPortalCacheUtil.rollback();

		testPortalCacheListener.assertActionsCount(0);
		testPortalCacheReplicator.assertActionsCount(0);

		Assert.assertNull(transactionalPortalCache.get(_KEY_1));
		Assert.assertNull(portalCache.get(_KEY_1));

		// Commit 1

		TransactionalPortalCacheUtil.begin();

		if (ttl) {
			transactionalPortalCache.put(_KEY_1, _VALUE_1, 10);

			PortalCacheHelperUtil.putWithoutReplicator(
				transactionalPortalCache, _KEY_1, _VALUE_2, 10);
		}
		else {
			transactionalPortalCache.put(_KEY_1, _VALUE_1);

			PortalCacheHelperUtil.putWithoutReplicator(
				transactionalPortalCache, _KEY_1, _VALUE_2);
		}

		Assert.assertEquals(_VALUE_2, transactionalPortalCache.get(_KEY_1));
		Assert.assertNull(portalCache.get(_KEY_1));

		TransactionalPortalCacheUtil.commit(false);

		if (ttl) {
			testPortalCacheListener.assertPut(_KEY_1, _VALUE_2, 10);
		}
		else {
			testPortalCacheListener.assertPut(_KEY_1, _VALUE_2);
		}

		testPortalCacheListener.assertActionsCount(1);

		if (ttl) {
			testPortalCacheReplicator.assertPut(_KEY_1, _VALUE_2, 10);
		}
		else {
			testPortalCacheReplicator.assertPut(_KEY_1, _VALUE_2);
		}

		testPortalCacheReplicator.assertActionsCount(1);

		Assert.assertEquals(_VALUE_2, transactionalPortalCache.get(_KEY_1));
		Assert.assertEquals(_VALUE_2, portalCache.get(_KEY_1));

		testPortalCacheListener.reset();
		testPortalCacheReplicator.reset();

		// Commit 2

		TransactionalPortalCacheUtil.begin();

		if (ttl) {
			PortalCacheHelperUtil.putWithoutReplicator(
				transactionalPortalCache, _KEY_1, _VALUE_2, 10);

			transactionalPortalCache.put(_KEY_1, _VALUE_1, 10);
		}
		else {
			PortalCacheHelperUtil.putWithoutReplicator(
				transactionalPortalCache, _KEY_1, _VALUE_2);

			transactionalPortalCache.put(_KEY_1, _VALUE_1);
		}

		Assert.assertEquals(_VALUE_1, transactionalPortalCache.get(_KEY_1));
		Assert.assertEquals(_VALUE_2, portalCache.get(_KEY_1));

		TransactionalPortalCacheUtil.commit(false);

		if (ttl) {
			testPortalCacheListener.assertUpdated(_KEY_1, _VALUE_1, 10);
		}
		else {
			testPortalCacheListener.assertUpdated(_KEY_1, _VALUE_1);
		}

		testPortalCacheListener.assertActionsCount(1);

		if (ttl) {
			testPortalCacheReplicator.assertUpdated(_KEY_1, _VALUE_1, 10);
		}
		else {
			testPortalCacheReplicator.assertUpdated(_KEY_1, _VALUE_1);
		}

		testPortalCacheReplicator.assertActionsCount(1);

		Assert.assertEquals(_VALUE_1, transactionalPortalCache.get(_KEY_1));
		Assert.assertEquals(_VALUE_1, portalCache.get(_KEY_1));

		testPortalCacheListener.reset();
		testPortalCacheReplicator.reset();

		// Commit 3

		TransactionalPortalCacheUtil.begin();

		PortalCacheHelperUtil.removeAllWithoutReplicator(
			transactionalPortalCache);

		if (ttl) {
			PortalCacheHelperUtil.putWithoutReplicator(
				transactionalPortalCache, _KEY_1, _VALUE_2, 10);
		}
		else {
			PortalCacheHelperUtil.putWithoutReplicator(
				transactionalPortalCache, _KEY_1, _VALUE_2);
		}

		Assert.assertEquals(_VALUE_2, transactionalPortalCache.get(_KEY_1));
		Assert.assertEquals(_VALUE_1, portalCache.get(_KEY_1));

		TransactionalPortalCacheUtil.commit(false);

		testPortalCacheListener.assertRemoveAll();

		if (ttl) {
			testPortalCacheListener.assertPut(_KEY_1, _VALUE_2, 10);
		}
		else {
			testPortalCacheListener.assertPut(_KEY_1, _VALUE_2);
		}

		testPortalCacheListener.assertActionsCount(2);

		testPortalCacheReplicator.assertActionsCount(0);

		Assert.assertEquals(_VALUE_2, transactionalPortalCache.get(_KEY_1));
		Assert.assertEquals(_VALUE_2, portalCache.get(_KEY_1));

		testPortalCacheListener.reset();

		// Commit 4

		TransactionalPortalCacheUtil.begin();

		transactionalPortalCache.remove(_KEY_1);

		if (ttl) {
			PortalCacheHelperUtil.putWithoutReplicator(
				transactionalPortalCache, _KEY_1, _VALUE_1, 10);
		}
		else {
			PortalCacheHelperUtil.putWithoutReplicator(
				transactionalPortalCache, _KEY_1, _VALUE_1);
		}

		Assert.assertEquals(_VALUE_1, transactionalPortalCache.get(_KEY_1));
		Assert.assertEquals(_VALUE_2, portalCache.get(_KEY_1));

		TransactionalPortalCacheUtil.commit(false);

		if (mvcc) {
			testPortalCacheListener.assertRemoved(_KEY_1, _VALUE_2);

			if (ttl) {
				testPortalCacheListener.assertPut(_KEY_1, _VALUE_1, 10);
			}
			else {
				testPortalCacheListener.assertPut(_KEY_1, _VALUE_1);
			}

			testPortalCacheListener.assertActionsCount(2);

			testPortalCacheReplicator.assertRemoved(_KEY_1, _VALUE_2);

			if (ttl) {
				testPortalCacheReplicator.assertPut(_KEY_1, _VALUE_1, 10);
			}
			else {
				testPortalCacheReplicator.assertPut(_KEY_1, _VALUE_1);
			}

			testPortalCacheReplicator.assertActionsCount(2);
		}
		else {
			if (ttl) {
				testPortalCacheListener.assertUpdated(_KEY_1, _VALUE_1, 10);
			}
			else {
				testPortalCacheListener.assertUpdated(_KEY_1, _VALUE_1);
			}

			testPortalCacheListener.assertActionsCount(1);

			if (ttl) {
				testPortalCacheReplicator.assertUpdated(_KEY_1, _VALUE_1, 10);
			}
			else {
				testPortalCacheReplicator.assertUpdated(_KEY_1, _VALUE_1);
			}

			testPortalCacheReplicator.assertActionsCount(1);
		}

		Assert.assertEquals(_VALUE_1, transactionalPortalCache.get(_KEY_1));
		Assert.assertEquals(_VALUE_1, portalCache.get(_KEY_1));

		testPortalCacheListener.reset();
		testPortalCacheReplicator.reset();

		// Commit 5

		TransactionalPortalCacheUtil.begin();

		PortalCacheHelperUtil.removeWithoutReplicator(
			transactionalPortalCache, _KEY_1);

		if (ttl) {
			PortalCacheHelperUtil.putWithoutReplicator(
				transactionalPortalCache, _KEY_1, _VALUE_2, 10);
		}
		else {
			PortalCacheHelperUtil.putWithoutReplicator(
				transactionalPortalCache, _KEY_1, _VALUE_2);
		}

		Assert.assertEquals(_VALUE_2, transactionalPortalCache.get(_KEY_1));
		Assert.assertEquals(_VALUE_1, portalCache.get(_KEY_1));

		TransactionalPortalCacheUtil.commit(false);

		if (mvcc) {
			testPortalCacheListener.assertRemoved(_KEY_1, _VALUE_1);

			if (ttl) {
				testPortalCacheListener.assertPut(_KEY_1, _VALUE_2, 10);
			}
			else {
				testPortalCacheListener.assertPut(_KEY_1, _VALUE_2);
			}

			testPortalCacheListener.assertActionsCount(2);
			testPortalCacheReplicator.assertActionsCount(0);
		}
		else {
			if (ttl) {
				testPortalCacheListener.assertUpdated(_KEY_1, _VALUE_2, 10);
			}
			else {
				testPortalCacheListener.assertUpdated(_KEY_1, _VALUE_2);
			}

			testPortalCacheListener.assertActionsCount(1);
			testPortalCacheReplicator.assertActionsCount(0);
		}

		Assert.assertEquals(_VALUE_2, transactionalPortalCache.get(_KEY_1));
		Assert.assertEquals(_VALUE_2, portalCache.get(_KEY_1));

		testPortalCacheListener.reset();
		testPortalCacheReplicator.reset();

		// Commit 6

		TransactionalPortalCacheUtil.begin();

		transactionalPortalCache.removeAll();

		if (ttl) {
			transactionalPortalCache.put(_KEY_1, _VALUE_1, 10);
		}
		else {
			transactionalPortalCache.put(_KEY_1, _VALUE_1);
		}

		PortalCacheHelperUtil.removeAllWithoutReplicator(
			transactionalPortalCache);

		Assert.assertNull(transactionalPortalCache.get(_KEY_1));
		Assert.assertEquals(_VALUE_2, portalCache.get(_KEY_1));

		TransactionalPortalCacheUtil.commit(false);

		testPortalCacheListener.assertRemoveAll();
		testPortalCacheListener.assertActionsCount(1);

		testPortalCacheReplicator.assertRemoveAll();
		testPortalCacheReplicator.assertActionsCount(1);

		Assert.assertNull(transactionalPortalCache.get(_KEY_1));
		Assert.assertNull(portalCache.get(_KEY_1));

		testPortalCacheListener.reset();
		testPortalCacheReplicator.reset();
	}

	private void _testTransactionLifecycleListenerEnabledWithBarrier(
		Propagation propagation) {

		Assert.assertEquals(0, _getTransactionStackSize());

		TransactionLifecycleListener transactionLifecycleListener =
			TransactionalPortalCacheUtil.TRANSACTION_LIFECYCLE_LISTENER;

		// Start parent transaction

		TransactionAttribute.Builder parentBuilder =
			new TransactionAttribute.Builder();

		TransactionAttribute parentTransactionAttribute = parentBuilder.build();

		TransactionStatus parentTransactionStatus = new TestTransactionStatus(
			true, false, false);

		transactionLifecycleListener.created(
			parentTransactionAttribute, parentTransactionStatus);

		Assert.assertEquals(1, _getTransactionStackSize());

		// Start child transaction with barrier

		TransactionAttribute.Builder childBuilder =
			new TransactionAttribute.Builder();

		childBuilder.setPropagation(propagation);

		TransactionAttribute childTransactionAttribute = childBuilder.build();

		TransactionStatus childTransactionStatus = new TestTransactionStatus(
			true, false, false);

		transactionLifecycleListener.created(
			childTransactionAttribute, childTransactionStatus);

		Assert.assertEquals(0, _getTransactionStackSize());

		// Start grandchild transaction

		TransactionAttribute.Builder grandchildBuilder =
			new TransactionAttribute.Builder();

		TransactionAttribute grandchildTransactionAttribute =
			grandchildBuilder.build();

		TransactionStatus grandchildTransactionStatus =
			new TestTransactionStatus(true, false, false);

		transactionLifecycleListener.created(
			grandchildTransactionAttribute, grandchildTransactionStatus);

		Assert.assertEquals(1, _getTransactionStackSize());

		// Commit grandchild transaction

		transactionLifecycleListener.committed(
			grandchildTransactionAttribute, grandchildTransactionStatus);

		Assert.assertEquals(0, _getTransactionStackSize());

		// Start grandchild transaction again

		transactionLifecycleListener.created(
			grandchildTransactionAttribute, grandchildTransactionStatus);

		Assert.assertEquals(1, _getTransactionStackSize());

		// Rollback grandchild transaction

		transactionLifecycleListener.rollbacked(
			grandchildTransactionAttribute, grandchildTransactionStatus, null);

		Assert.assertEquals(0, _getTransactionStackSize());

		// Commit child transaction

		transactionLifecycleListener.committed(
			childTransactionAttribute, childTransactionStatus);

		Assert.assertEquals(1, _getTransactionStackSize());

		// Start child transaction with barrier with barrier again

		transactionLifecycleListener.created(
			childTransactionAttribute, childTransactionStatus);

		Assert.assertEquals(0, _getTransactionStackSize());

		// Rollback child transaction

		transactionLifecycleListener.rollbacked(
			childTransactionAttribute, childTransactionStatus, null);

		Assert.assertEquals(1, _getTransactionStackSize());

		// Commit parent transaction

		transactionLifecycleListener.committed(
			parentTransactionAttribute, parentTransactionStatus);

		Assert.assertEquals(0, _getTransactionStackSize());
	}

	private void _testTransactionLifecycleListenerEnabledWithoutBarrier(
		Propagation propagation) {

		Assert.assertEquals(0, _getTransactionStackSize());

		TransactionLifecycleListener transactionLifecycleListener =
			TransactionalPortalCacheUtil.TRANSACTION_LIFECYCLE_LISTENER;

		// Start parent transaction

		TransactionAttribute.Builder parentBuilder =
			new TransactionAttribute.Builder();

		TransactionAttribute parentTransactionAttribute = parentBuilder.build();

		TransactionStatus parentTransactionStatus = new TestTransactionStatus(
			true, false, false);

		transactionLifecycleListener.created(
			parentTransactionAttribute, parentTransactionStatus);

		Assert.assertEquals(1, _getTransactionStackSize());

		// Start child transaction

		TransactionAttribute.Builder childBuilder =
			new TransactionAttribute.Builder();

		childBuilder.setPropagation(propagation);

		TransactionAttribute childTransactionAttribute = parentBuilder.build();

		TransactionStatus childTransactionStatus = new TestTransactionStatus(
			true, false, false);

		transactionLifecycleListener.created(
			childTransactionAttribute, childTransactionStatus);

		Assert.assertEquals(2, _getTransactionStackSize());

		// Commit child transaction

		transactionLifecycleListener.committed(
			childTransactionAttribute, childTransactionStatus);

		Assert.assertEquals(1, _getTransactionStackSize());

		// Start child transaction again

		transactionLifecycleListener.created(
			childTransactionAttribute, childTransactionStatus);

		Assert.assertEquals(2, _getTransactionStackSize());

		// Rollback child transaction

		transactionLifecycleListener.rollbacked(
			childTransactionAttribute, childTransactionStatus, null);

		Assert.assertEquals(1, _getTransactionStackSize());

		// Commit parent transaction

		transactionLifecycleListener.committed(
			parentTransactionAttribute, parentTransactionStatus);

		Assert.assertEquals(0, _getTransactionStackSize());

		// Start parent transaction again

		transactionLifecycleListener.created(
			parentTransactionAttribute, parentTransactionStatus);

		Assert.assertEquals(1, _getTransactionStackSize());

		// Rollback parent transaction

		transactionLifecycleListener.rollbacked(
			parentTransactionAttribute, parentTransactionStatus, null);

		Assert.assertEquals(0, _getTransactionStackSize());
	}

	private static final String _KEY_1 = "KEY_1";

	private static final String _KEY_2 = "KEY_2";

	private static final long _TEST_COMPANY_ID = 15000;

	private static final String _VALUE_1 = "VALUE_1";

	private static final String _VALUE_2 = "VALUE_2";

	private static final MockedStatic<CompanyThreadLocal>
		_companyThreadLocalMockedStatic = Mockito.mockStatic(
			CompanyThreadLocal.class);

	private PortalCache<String, String> _portalCache;
	private PortalCache<String, String> _shardedPortalCache;
	private TestPortalCacheListener<String, String> _testCacheListener;
	private TestPortalCacheReplicator<String, String> _testCacheReplicator;
	private TestPortalCacheListener<String, String> _testShardedCacheListener;
	private TestPortalCacheReplicator<String, String>
		_testShardedCacheReplicator;

	private static class TestCallable implements Callable<Void> {

		@Override
		public Void call() throws Exception {
			TransactionalPortalCacheUtil.begin();

			if (_skipReplicator) {
				if (_value == null) {
					PortalCacheHelperUtil.removeWithoutReplicator(
						_transactionalPortalCache, _key);
				}
				else {
					PortalCacheHelperUtil.putWithoutReplicator(
						_transactionalPortalCache, _key, _value);
				}
			}
			else {
				if (_value == null) {
					_transactionalPortalCache.remove(_key);
				}
				else {
					_transactionalPortalCache.put(_key, _value);
				}
			}

			_waitCountDownLatch.countDown();

			_blockCountDownLatch.await();

			TransactionalPortalCacheUtil.commit(_readOnly);

			return null;
		}

		public void unblock() {
			_blockCountDownLatch.countDown();
		}

		public void waitUntilBlock() throws InterruptedException {
			_waitCountDownLatch.await();
		}

		private TestCallable(
			TransactionalPortalCache<String, String> transactionalPortalCache,
			String key, String value, boolean readOnly,
			boolean skipReplicator) {

			_transactionalPortalCache = transactionalPortalCache;
			_key = key;
			_value = value;
			_readOnly = readOnly;
			_skipReplicator = skipReplicator;
		}

		private final CountDownLatch _blockCountDownLatch = new CountDownLatch(
			1);
		private final String _key;
		private final boolean _readOnly;
		private final boolean _skipReplicator;
		private final TransactionalPortalCache<String, String>
			_transactionalPortalCache;
		private final String _value;
		private final CountDownLatch _waitCountDownLatch = new CountDownLatch(
			1);

	}

	private static class TestTransactionStatus implements TransactionStatus {

		@Override
		public boolean isCompleted() {
			return _completed;
		}

		@Override
		public boolean isNewTransaction() {
			return _newTransaction;
		}

		@Override
		public boolean isRollbackOnly() {
			return _rollbackOnly;
		}

		@Override
		public void suppressLifecycleListenerThrowable(Throwable throwable) {
		}

		private TestTransactionStatus(
			boolean newTransaction, boolean rollbackOnly, boolean completed) {

			_newTransaction = newTransaction;
			_rollbackOnly = rollbackOnly;
			_completed = completed;
		}

		private final boolean _completed;
		private final boolean _newTransaction;
		private final boolean _rollbackOnly;

	}

}