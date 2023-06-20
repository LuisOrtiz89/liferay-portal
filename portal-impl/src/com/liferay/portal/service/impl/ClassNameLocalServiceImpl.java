/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.portal.service.impl;

import com.liferay.petra.lang.SafeCloseable;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.bean.BeanReference;
import com.liferay.portal.kernel.cache.CacheRegistryItem;
import com.liferay.portal.kernel.change.tracking.CTAware;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.ClassName;
import com.liferay.portal.kernel.model.ModelHintsUtil;
import com.liferay.portal.kernel.security.auth.CompanyThreadLocal;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.transaction.Propagation;
import com.liferay.portal.kernel.transaction.TransactionDefinition;
import com.liferay.portal.kernel.transaction.Transactional;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.PropsUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.model.impl.ClassNameImpl;
import com.liferay.portal.service.base.ClassNameLocalServiceBaseImpl;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Brian Wing Shun Chan
 */
@CTAware
public class ClassNameLocalServiceImpl
	extends ClassNameLocalServiceBaseImpl implements CacheRegistryItem {

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public ClassName addClassName(String value) {
		long currentCompanyId = CompanyThreadLocal.getCompanyId();
		AtomicReference<ClassName> currentClassName = new AtomicReference<>();

		_companyLocalService.forEachCompanyId(
			companyId -> {
				ClassName className = classNamePersistence.fetchByValue(value);

				if (className == null) {
					long classNameId = counterLocalService.increment();

					className = classNamePersistence.create(classNameId);

					className.setValue(value);

					classNamePersistence.update(className);
				}

				if (companyId == currentCompanyId) {
					currentClassName.set(className);
				}
			});

		return currentClassName.get();
	}

	@Override
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public void checkClassNames() {
		_companyLocalService.forEachCompanyId(
			companyId ->
			{
				List<ClassName> classNames = classNamePersistence.findAll();

				for (ClassName className : classNames) {
					_classNames.put(_getCompoundValue(className.getValue()), className);
				}

				List<String> models = ModelHintsUtil.getModels();

				for (String model : models) {
					getClassName(model);
				}
			});
	}

	@Override
	public ClassName deleteClassName(ClassName className) {
		long defaultCompanyId = CompanyThreadLocal.getCompanyId();
		 AtomicReference<ClassName> defaultClassName = new AtomicReference<>();

		_companyLocalService.forEachCompanyId(
			companyId -> {
				_classNames.remove(_getCompoundValue(className.getValue()));

				if (companyId == defaultCompanyId) {
					defaultClassName.set(className);
				}

				classNamePersistence.remove(className); });

		return defaultClassName.get();
	}

	@Override
	public ClassName fetchByClassNameId(long classNameId) {
		return classNamePersistence.fetchByPrimaryKey(classNameId);
	}

	@Override
	public ClassName fetchClassName(String value) {
		if (Validator.isNull(value)) {
			return _nullClassName;
		}

		ClassName className = _classNames.computeIfAbsent(
			_getCompoundValue(value), key -> classNamePersistence.fetchByValue(value));

		if (className == null) {
			return _nullClassName;
		}

		return className;
	}

	@Override
	@Transactional(enabled = false)
	public ClassName getClassName(String value) {
		if (Validator.isNull(value)) {
			return _nullClassName;
		}

		// Always cache the class name. This table exists to improve
		// performance. Create the class name if one does not exist.

		ClassName className = _classNames.computeIfAbsent(
			_getCompoundValue(value),
			key -> {
				try (SafeCloseable safeCloseable = CompanyThreadLocal.lock(
					CompanyThreadLocal.getCompanyId())) {

					return classNameLocalService.addClassName(value);
				}
				catch (Throwable throwable) {
					if (_log.isDebugEnabled()) {
						_log.debug(throwable);
					}

					return null;
				}
			});

		if (className == null) {
			return classNameLocalService.fetchClassName(value);
		}

		return className;
	}

	@Override
	@Transactional(enabled = false)
	public long getClassNameId(Class<?> clazz) {
		return getClassNameId(clazz.getName());
	}

	@Override
	@Transactional(enabled = false)
	public long getClassNameId(String value) {
		ClassName className = getClassName(value);

		return className.getClassNameId();
	}

	public String _getCompoundValue(String value) {
		if (_DATABASE_PARTITION_ENABLED) {
			return StringBundler.concat(value, StringPool.AT,
				CompanyThreadLocal.getCompanyId());
		}
		return value;
	}

	@Override
	public String getRegistryName() {
		return ClassNameLocalServiceImpl.class.getName();
	}

	@Override
	public void invalidate() {
		_classNames.clear();
	}

	private static final Log _log = LogFactoryUtil.getLog(
		ClassNameLocalServiceImpl.class);

	private static final Map<String, ClassName> _classNames =
		new ConcurrentHashMap<>();

	private static final boolean _DATABASE_PARTITION_ENABLED =
		GetterUtil.getBoolean(PropsUtil.get("database.partition.enabled"));
	private static final ClassName _nullClassName = new ClassNameImpl();

	@BeanReference(type = CompanyLocalService.class)
	private CompanyLocalService _companyLocalService;

}