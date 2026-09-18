/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.batch.engine.internal.util;

import com.liferay.batch.engine.BatchEngineTaskContentType;
import com.liferay.batch.engine.exception.BatchEngineImportTaskSensitiveFieldsException;
import com.liferay.petra.io.StreamUtil;
import com.liferay.petra.io.unsync.UnsyncByteArrayInputStream;
import com.liferay.petra.io.unsync.UnsyncByteArrayOutputStream;
import com.liferay.portal.kernel.encryptor.Encryptor;
import com.liferay.portal.kernel.encryptor.EncryptorException;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.nio.charset.StandardCharsets;

import java.security.Key;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import org.mockito.Mockito;

/**
 * @author Luis Ortiz
 */
public class SensitiveFieldsUtilTest {

	@ClassRule
	public static LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Before
	public void setUp() throws Exception {
		_encryptor = Mockito.mock(Encryptor.class);

		Mockito.when(
			_encryptor.decryptAuthenticated(
				Mockito.any(Key.class), Mockito.anyString())
		).thenAnswer(
			invocationOnMock -> _toPlaintext(invocationOnMock.getArgument(1))
		);

		Mockito.when(
			_encryptor.encryptAuthenticated(
				Mockito.any(Key.class), Mockito.anyString())
		).thenAnswer(
			invocationOnMock -> _toCiphertext(invocationOnMock.getArgument(1))
		);

		_setKey(_KEY);
	}

	@Test
	public void testDecryptLeavesAValueItCannotDecryptAsItIs()
		throws Exception {

		Map<String, Object> fieldNameValueMap = _toFieldNameValueMap(_PASSWORD);

		SensitiveFieldsUtil.decrypt(
			fieldNameValueMap, _encryptor, _sensitiveFieldNames);

		Map<String, Object> adminFieldNameValueMap =
			(Map<String, Object>)fieldNameValueMap.get("admin");

		Assert.assertEquals(_PASSWORD, adminFieldNameValueMap.get("password"));
	}

	@Test
	public void testDecryptRestoresTheValueEncryptOnTheWayIn()
		throws Exception {

		byte[] content = SensitiveFieldsUtil.encrypt(
			_toContent(_PASSWORD), _CONTENT_TYPE, _encryptor,
			_sensitiveFieldNames);

		Assert.assertFalse(
			_toJSON(
				content
			).contains(
				_PASSWORD
			));

		Map<String, Object> fieldNameValueMap = _toFieldNameValueMap(
			_toCiphertext(_PASSWORD));

		SensitiveFieldsUtil.decrypt(
			fieldNameValueMap, _encryptor, _sensitiveFieldNames);

		Map<String, Object> adminFieldNameValueMap =
			(Map<String, Object>)fieldNameValueMap.get("admin");

		Assert.assertEquals(_PASSWORD, adminFieldNameValueMap.get("password"));
	}

	@Test
	public void testEncryptLeavesTheRestOfThePayloadReadable()
		throws Exception {

		String json = _toJSON(
			SensitiveFieldsUtil.encrypt(
				_toContent(_PASSWORD), _CONTENT_TYPE, _encryptor,
				_sensitiveFieldNames));

		Assert.assertTrue(json.contains(_VIRTUAL_HOST));
		Assert.assertTrue(json.contains("admin@" + _VIRTUAL_HOST));
	}

	@Test
	public void testEncryptReplacesTheDeclaredFieldOfEveryJSONLLine()
		throws Exception {

		String json = _toJSON(
			SensitiveFieldsUtil.encrypt(
				_toContent(
					_PASSWORD,
					String.valueOf(BatchEngineTaskContentType.JSONL)),
				String.valueOf(BatchEngineTaskContentType.JSONL), _encryptor,
				_sensitiveFieldNames));

		Assert.assertFalse(json.contains(_PASSWORD));
		Assert.assertTrue(json.contains(_toCiphertext(_PASSWORD)));
	}

	@Test
	public void testEncryptReplacesTheDeclaredNestedField() throws Exception {
		String json = _toJSON(
			SensitiveFieldsUtil.encrypt(
				_toContent(_PASSWORD), _CONTENT_TYPE, _encryptor,
				_sensitiveFieldNames));

		Assert.assertFalse(json.contains(_PASSWORD));
		Assert.assertTrue(json.contains(_toCiphertext(_PASSWORD)));
	}

	@Test
	public void testEncryptWhenContentTypeIsNotInspectable() throws Exception {
		try {
			SensitiveFieldsUtil.encrypt(
				_toContent(_PASSWORD),
				String.valueOf(BatchEngineTaskContentType.CSV), _encryptor,
				_sensitiveFieldNames);

			Assert.fail();
		}
		catch (BatchEngineImportTaskSensitiveFieldsException
					batchEngineImportTaskSensitiveFieldsException) {

			Assert.assertTrue(
				batchEngineImportTaskSensitiveFieldsException.getMessage(
				).contains(
					"CSV"
				));
		}
	}

	@Test
	public void testEncryptWhenKeyIsNullAndPayloadCarriesNoSensitiveValue()
		throws Exception {

		_setKey(null);

		byte[] content = _toContent(null);

		Assert.assertArrayEquals(
			content,
			SensitiveFieldsUtil.encrypt(
				content, _CONTENT_TYPE, _encryptor, _sensitiveFieldNames));
	}

	@Test
	public void testEncryptWhenKeyIsNullAndPayloadCarriesSensitiveValue()
		throws Exception {

		_setKey(null);

		try {
			SensitiveFieldsUtil.encrypt(
				_toContent(_PASSWORD), _CONTENT_TYPE, _encryptor,
				_sensitiveFieldNames);

			Assert.fail();
		}
		catch (BatchEngineImportTaskSensitiveFieldsException
					batchEngineImportTaskSensitiveFieldsException) {

			Assert.assertTrue(
				batchEngineImportTaskSensitiveFieldsException.getMessage(
				).contains(
					"batch.engine.encryption.key"
				));
		}
	}

	@Test
	public void testEncryptWhenNoSensitiveFieldNamesAreDeclared()
		throws Exception {

		byte[] content = _toContent(_PASSWORD);

		Assert.assertArrayEquals(
			content,
			SensitiveFieldsUtil.encrypt(
				content, _CONTENT_TYPE, _encryptor, Collections.emptySet()));
	}

	private void _setKey(String key) {
		ReflectionTestUtil.setFieldValue(
			SensitiveFieldsUtil.class, "_ALGORITHM", "AES");
		ReflectionTestUtil.setFieldValue(
			SensitiveFieldsUtil.class, "_KEY", key);
	}

	private String _toCiphertext(String plaintext) {
		StringBuilder sb = new StringBuilder(plaintext);

		return _CIPHERTEXT_PREFIX + sb.reverse();
	}

	private byte[] _toContent(String password) throws Exception {
		return _toContent(password, _CONTENT_TYPE);
	}

	private byte[] _toContent(String password, String contentType)
		throws Exception {

		boolean jsonl = contentType.equals(
			String.valueOf(BatchEngineTaskContentType.JSONL));

		StringBuilder sb = new StringBuilder();

		if (!jsonl) {
			sb.append("[");
		}

		sb.append("{\"portalInstanceId\":\"");
		sb.append(_VIRTUAL_HOST);
		sb.append("\",\"admin\":{");
		sb.append("\"emailAddress\":\"admin@");
		sb.append(_VIRTUAL_HOST);
		sb.append("\"");

		if (password != null) {
			sb.append(",\"password\":\"");
			sb.append(password);
			sb.append("\"");
		}

		sb.append("}}");

		if (!jsonl) {
			sb.append("]");
		}

		UnsyncByteArrayOutputStream unsyncByteArrayOutputStream =
			new UnsyncByteArrayOutputStream();

		try (ZipOutputStream zipOutputStream = new ZipOutputStream(
				unsyncByteArrayOutputStream)) {

			zipOutputStream.putNextEntry(new ZipEntry("fileName"));

			StreamUtil.transfer(
				new UnsyncByteArrayInputStream(
					sb.toString(
					).getBytes(
						StandardCharsets.UTF_8
					)),
				zipOutputStream, false);
		}

		return unsyncByteArrayOutputStream.toByteArray();
	}

	private Map<String, Object> _toFieldNameValueMap(String password) {
		return HashMapBuilder.<String, Object>put(
			"admin",
			HashMapBuilder.<String, Object>put(
				"password", password
			).build()
		).build();
	}

	private String _toJSON(byte[] content) throws Exception {
		return StreamUtil.toString(
			ZipInputStreamUtil.asZipInputStream(
				new UnsyncByteArrayInputStream(content)),
			StandardCharsets.UTF_8.name());
	}

	private String _toPlaintext(String ciphertext) throws EncryptorException {
		if (!ciphertext.startsWith(_CIPHERTEXT_PREFIX)) {
			throw new EncryptorException("Unable to decrypt " + ciphertext);
		}

		StringBuilder sb = new StringBuilder(
			ciphertext.substring(_CIPHERTEXT_PREFIX.length()));

		return sb.reverse(
		).toString();
	}

	private static final String _CIPHERTEXT_PREFIX = "ENCRYPTED:";

	private static final String _CONTENT_TYPE = String.valueOf(
		BatchEngineTaskContentType.JSON);

	private static final String _KEY = RandomTestUtil.randomString();

	private static final String _PASSWORD = RandomTestUtil.randomString();

	private static final String _VIRTUAL_HOST =
		RandomTestUtil.randomString() + ".com";

	private static final Set<String> _sensitiveFieldNames =
		Collections.singleton("admin.password");

	private Encryptor _encryptor;

}