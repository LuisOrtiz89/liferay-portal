/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.batch.engine.internal.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.liferay.batch.engine.BatchEngineTaskContentType;
import com.liferay.batch.engine.exception.BatchEngineImportTaskSensitiveFieldsException;
import com.liferay.petra.io.StreamUtil;
import com.liferay.petra.io.unsync.UnsyncByteArrayInputStream;
import com.liferay.petra.io.unsync.UnsyncByteArrayOutputStream;
import com.liferay.petra.string.CharPool;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.encryptor.Encryptor;
import com.liferay.portal.kernel.encryptor.EncryptorException;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.Base64;
import com.liferay.portal.kernel.util.PropsKeys;
import com.liferay.portal.kernel.util.PropsValues;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;

import java.io.InputStream;

import java.nio.charset.StandardCharsets;

import java.security.Key;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.UnaryOperator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.crypto.spec.SecretKeySpec;

/**
 * @author Luis Ortiz
 */
public class SensitiveFieldsUtil {

	public static void decrypt(
		Map<String, Object> fieldNameValueMap, Encryptor encryptor,
		Set<String> sensitiveFieldNames) {

		if (!isEnabled() || (sensitiveFieldNames == null) ||
			sensitiveFieldNames.isEmpty()) {

			return;
		}

		_process(
			fieldNameValueMap, sensitiveFieldNames,
			value -> _decrypt(encryptor, value));
	}

	public static byte[] encrypt(
			byte[] content, String contentType, Encryptor encryptor,
			Set<String> sensitiveFieldNames)
		throws PortalException {

		if ((sensitiveFieldNames == null) || sensitiveFieldNames.isEmpty()) {
			return content;
		}

		BatchEngineTaskContentType batchEngineTaskContentType =
			_getBatchEngineTaskContentType(contentType);

		if (batchEngineTaskContentType == null) {

			// The payload cannot be inspected, so a sensitive value cannot be
			// ruled out either. Refusing it is the only answer that does not
			// risk storing a secret in the clear.

			throw new BatchEngineImportTaskSensitiveFieldsException(
				StringBundler.concat(
					"Unable to protect ", sensitiveFieldNames, " in \"",
					contentType, "\" content"));
		}

		boolean enabled = isEnabled();

		try {
			ObjectMapper objectMapper = new ObjectMapper();

			List<Map<String, Object>> fieldNameValueMaps = _read(
				batchEngineTaskContentType, objectMapper,
				_readZipEntry(content));

			for (Map<String, Object> fieldNameValueMap : fieldNameValueMaps) {
				if (enabled) {
					_process(
						fieldNameValueMap, sensitiveFieldNames,
						value -> _encrypt(encryptor, value));
				}
				else if (_hasSensitiveValue(
							fieldNameValueMap, sensitiveFieldNames)) {

					throw new BatchEngineImportTaskSensitiveFieldsException(
						StringBundler.concat(
							"Set the portal property \"",
							PropsKeys.BATCH_ENGINE_ENCRYPTION_KEY,
							"\" to submit a payload carrying ",
							sensitiveFieldNames));
				}
			}

			if (!enabled) {
				return content;
			}

			return _writeZipEntry(
				_write(
					batchEngineTaskContentType, objectMapper,
					fieldNameValueMaps));
		}
		catch (BatchEngineImportTaskSensitiveFieldsException
					batchEngineImportTaskSensitiveFieldsException) {

			throw batchEngineImportTaskSensitiveFieldsException;
		}
		catch (Exception exception) {
			throw new BatchEngineImportTaskSensitiveFieldsException(
				"Unable to protect the sensitive fields of the batch engine " +
					"import task content",
				exception);
		}
	}

	public static Key getKey() {
		return new SecretKeySpec(Base64.decode(_KEY), _ALGORITHM);
	}

	public static boolean isEnabled() {
		return Validator.isNotNull(_KEY);
	}

	private static String _decrypt(Encryptor encryptor, String value) {
		try {
			return encryptor.decryptAuthenticated(getKey(), value);
		}
		catch (EncryptorException encryptorException) {

			// Content submitted before a key was configured, or through a
			// content type this utility does not protect, is already plaintext

			if (_log.isWarnEnabled()) {
				_log.warn(
					"Unable to decrypt a sensitive field, using it as is",
					encryptorException);
			}

			return value;
		}
	}

	private static String _encrypt(Encryptor encryptor, String value) {
		try {
			return encryptor.encryptAuthenticated(getKey(), value);
		}
		catch (EncryptorException encryptorException) {
			throw new IllegalStateException(
				"Unable to encrypt a sensitive field", encryptorException);
		}
	}

	private static BatchEngineTaskContentType _getBatchEngineTaskContentType(
		String contentType) {

		if (Validator.isNull(contentType)) {
			return null;
		}

		for (BatchEngineTaskContentType batchEngineTaskContentType :
				new BatchEngineTaskContentType[] {
					BatchEngineTaskContentType.JSON,
					BatchEngineTaskContentType.JSONL,
					BatchEngineTaskContentType.JSONT
				}) {

			if (StringUtil.equalsIgnoreCase(
					contentType, batchEngineTaskContentType.name())) {

				return batchEngineTaskContentType;
			}
		}

		return null;
	}

	private static boolean _hasSensitiveValue(
		Map<String, Object> fieldNameValueMap,
		Set<String> sensitiveFieldNames) {

		boolean[] found = {false};

		_process(
			fieldNameValueMap, sensitiveFieldNames,
			value -> {
				found[0] = true;

				return value;
			});

		return found[0];
	}

	private static void _process(
		Map<String, Object> fieldNameValueMap, Set<String> sensitiveFieldNames,
		UnaryOperator<String> unaryOperator) {

		for (String sensitiveFieldName : sensitiveFieldNames) {
			_process(fieldNameValueMap, sensitiveFieldName, unaryOperator);
		}
	}

	private static void _process(
		Map<String, Object> fieldNameValueMap, String sensitiveFieldName,
		UnaryOperator<String> unaryOperator) {

		int index = sensitiveFieldName.indexOf(CharPool.PERIOD);

		if (index == -1) {
			Object value = fieldNameValueMap.get(sensitiveFieldName);

			if (value instanceof String) {
				fieldNameValueMap.put(
					sensitiveFieldName, unaryOperator.apply((String)value));
			}

			return;
		}

		Object value = fieldNameValueMap.get(
			sensitiveFieldName.substring(0, index));

		if (value instanceof Map) {
			_process(
				(Map<String, Object>)value,
				sensitiveFieldName.substring(index + 1), unaryOperator);
		}
	}

	private static List<Map<String, Object>> _read(
			BatchEngineTaskContentType batchEngineTaskContentType,
			ObjectMapper objectMapper, String json)
		throws Exception {

		if (batchEngineTaskContentType != BatchEngineTaskContentType.JSONL) {
			return objectMapper.readValue(
				json,
				new TypeReference<List<Map<String, Object>>>() {
				});
		}

		List<Map<String, Object>> fieldNameValueMaps = new ArrayList<>();

		for (String line : StringUtil.splitLines(json)) {
			fieldNameValueMaps.add(
				objectMapper.readValue(
					line,
					new TypeReference<Map<String, Object>>() {
					}));
		}

		return fieldNameValueMaps;
	}

	private static String _readZipEntry(byte[] content) throws Exception {
		try (InputStream inputStream = ZipInputStreamUtil.asZipInputStream(
				new UnsyncByteArrayInputStream(content))) {

			return StreamUtil.toString(
				inputStream, StandardCharsets.UTF_8.name());
		}
	}

	private static String _write(
			BatchEngineTaskContentType batchEngineTaskContentType,
			ObjectMapper objectMapper,
			List<Map<String, Object>> fieldNameValueMaps)
		throws Exception {

		if (batchEngineTaskContentType != BatchEngineTaskContentType.JSONL) {
			return objectMapper.writeValueAsString(fieldNameValueMaps);
		}

		List<String> lines = new ArrayList<>();

		for (Map<String, Object> fieldNameValueMap : fieldNameValueMaps) {
			lines.add(objectMapper.writeValueAsString(fieldNameValueMap));
		}

		return StringUtil.merge(lines, StringPool.NEW_LINE);
	}

	private static byte[] _writeZipEntry(String json) throws Exception {
		UnsyncByteArrayOutputStream unsyncByteArrayOutputStream =
			new UnsyncByteArrayOutputStream();

		try (ZipOutputStream zipOutputStream = new ZipOutputStream(
				unsyncByteArrayOutputStream)) {

			zipOutputStream.putNextEntry(new ZipEntry("fileName"));

			StreamUtil.transfer(
				new UnsyncByteArrayInputStream(
					json.getBytes(StandardCharsets.UTF_8)),
				zipOutputStream, false);
		}

		return unsyncByteArrayOutputStream.toByteArray();
	}

	private static final String _ALGORITHM =
		PropsValues.BATCH_ENGINE_ENCRYPTION_ALGORITHM;

	private static final String _KEY = PropsValues.BATCH_ENGINE_ENCRYPTION_KEY;

	private static final Log _log = LogFactoryUtil.getLog(
		SensitiveFieldsUtil.class);

}