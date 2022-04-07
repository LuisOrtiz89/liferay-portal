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

package com.liferay.portal.kernel.dao.jdbc;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import java.sql.Connection;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Luis Ortiz
 */
public class ConnectionThreadProxyHandler implements InvocationHandler {

	@Override
	public Object invoke(Object proxy, Method method, Object[] args)
		throws Throwable {

		String methodName = method.getName();

		if (methodName.equals("close")) {
			for (long threadId : _currentThreads) {
				Connection connection = _connectionMap.remove(threadId);

				if (connection != null) {
					method.invoke(connection, args);
				}
			}

			_currentThreads.clear();

			return null;
		}

		return method.invoke(_getConnection(), args);
	}

	private Connection _getConnection() {
		Thread thread = Thread.currentThread();

		long threadId = thread.getId();

		Connection connection = _connectionMap.get(threadId);

		if (connection == null) {
			try {
				connection = DataAccess.getConnection();

				Connection prevConnection = _connectionMap.putIfAbsent(
					threadId, connection);

				if (prevConnection != null) {
					connection.close();

					connection = prevConnection;
				}
				else {
					_currentThreads.add(threadId);
				}
			}
			catch (SQLException sqlException) {
				_log.error(
					"Unable to obtain a database connection ", sqlException);
			}
		}

		return connection;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		ConnectionThreadProxyHandler.class);

	private static final Map<Long, Connection> _connectionMap =
		new ConcurrentHashMap<>();

	private volatile List<Long> _currentThreads = new ArrayList<>();

}