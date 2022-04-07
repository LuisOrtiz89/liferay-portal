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

import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

/**
 * @author Luis Ortiz
 */
public class ConnectionThreadProxy implements Connection {

	@Override
	public void abort(Executor executor) throws SQLException {
		_getConnection().abort(executor);
	}

	@Override
	public void clearWarnings() throws SQLException {
		_getConnection().clearWarnings();
	}

	@Override
	public void close() throws SQLException {
		for (long threadId : _currentThreads) {
			Connection connection = _connectionMap.remove(threadId);

			if (connection != null) {
				connection.close();
			}
		}

		_currentThreads.clear();
	}

	@Override
	public void commit() throws SQLException {
		_getConnection().commit();
	}

	@Override
	public Array createArrayOf(String typeName, Object[] elements)
		throws SQLException {

		return _getConnection().createArrayOf(typeName, elements);
	}

	@Override
	public Blob createBlob() throws SQLException {
		return _getConnection().createBlob();
	}

	@Override
	public Clob createClob() throws SQLException {
		return _getConnection().createClob();
	}

	@Override
	public NClob createNClob() throws SQLException {
		return _getConnection().createNClob();
	}

	@Override
	public SQLXML createSQLXML() throws SQLException {
		return _getConnection().createSQLXML();
	}

	@Override
	public Statement createStatement() throws SQLException {
		return _getConnection().createStatement();
	}

	@Override
	public Statement createStatement(
			int resultSetType, int resultSetConcurrency)
		throws SQLException {

		return _getConnection().createStatement(
			resultSetType, resultSetConcurrency);
	}

	@Override
	public Statement createStatement(
			int resultSetType, int resultSetConcurrency,
			int resultSetHoldability)
		throws SQLException {

		return _getConnection().createStatement(
			resultSetType, resultSetConcurrency, resultSetHoldability);
	}

	@Override
	public Struct createStruct(String typeName, Object[] attributes)
		throws SQLException {

		return _getConnection().createStruct(typeName, attributes);
	}

	@Override
	public boolean getAutoCommit() throws SQLException {
		return _getConnection().getAutoCommit();
	}

	@Override
	public String getCatalog() throws SQLException {
		return _getConnection().getCatalog();
	}

	@Override
	public Properties getClientInfo() throws SQLException {
		return _getConnection().getClientInfo();
	}

	@Override
	public String getClientInfo(String name) throws SQLException {
		return _getConnection().getClientInfo(name);
	}

	@Override
	public int getHoldability() throws SQLException {
		return _getConnection().getHoldability();
	}

	@Override
	public DatabaseMetaData getMetaData() throws SQLException {
		return _getConnection().getMetaData();
	}

	@Override
	public int getNetworkTimeout() throws SQLException {
		return _getConnection().getNetworkTimeout();
	}

	@Override
	public String getSchema() throws SQLException {
		return _getConnection().getSchema();
	}

	@Override
	public int getTransactionIsolation() throws SQLException {
		return _getConnection().getTransactionIsolation();
	}

	@Override
	public Map<String, Class<?>> getTypeMap() throws SQLException {
		return _getConnection().getTypeMap();
	}

	@Override
	public SQLWarning getWarnings() throws SQLException {
		return _getConnection().getWarnings();
	}

	@Override
	public boolean isClosed() throws SQLException {
		return _getConnection().isClosed();
	}

	@Override
	public boolean isReadOnly() throws SQLException {
		return _getConnection().isReadOnly();
	}

	@Override
	public boolean isValid(int timeout) throws SQLException {
		return _getConnection().isValid(timeout);
	}

	@Override
	public boolean isWrapperFor(Class<?> clazz) throws SQLException {

		// JDK 6

		return Connection.class.equals(clazz);
	}

	@Override
	public String nativeSQL(String sql) throws SQLException {
		return _getConnection().nativeSQL(sql);
	}

	@Override
	public CallableStatement prepareCall(String sql) throws SQLException {
		return _getConnection().prepareCall(sql);
	}

	@Override
	public CallableStatement prepareCall(
			String sql, int resultSetType, int resultSetConcurrency)
		throws SQLException {

		return _getConnection().prepareCall(
			sql, resultSetType, resultSetConcurrency);
	}

	@Override
	public CallableStatement prepareCall(
			String sql, int resultSetType, int resultSetConcurrency,
			int resultSetHoldability)
		throws SQLException {

		return _getConnection().prepareCall(
			sql, resultSetType, resultSetConcurrency, resultSetHoldability);
	}

	@Override
	public PreparedStatement prepareStatement(String sql) throws SQLException {
		return _getConnection().prepareStatement(sql);
	}

	@Override
	public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys)
		throws SQLException {

		return _getConnection().prepareStatement(sql, autoGeneratedKeys);
	}

	@Override
	public PreparedStatement prepareStatement(
			String sql, int resultSetType, int resultSetConcurrency)
		throws SQLException {

		return _getConnection().prepareStatement(
			sql, resultSetType, resultSetConcurrency);
	}

	@Override
	public PreparedStatement prepareStatement(
			String sql, int resultSetType, int resultSetConcurrency,
			int resultSetHoldability)
		throws SQLException {

		return _getConnection().prepareStatement(
			sql, resultSetType, resultSetConcurrency, resultSetHoldability);
	}

	@Override
	public PreparedStatement prepareStatement(String sql, int[] columnIndexes)
		throws SQLException {

		return _getConnection().prepareStatement(sql, columnIndexes);
	}

	@Override
	public PreparedStatement prepareStatement(String sql, String[] columnNames)
		throws SQLException {

		return _getConnection().prepareStatement(sql, columnNames);
	}

	@Override
	public void releaseSavepoint(Savepoint savepoint) throws SQLException {
		_getConnection().releaseSavepoint(savepoint);
	}

	@Override
	public void rollback() throws SQLException {
		_getConnection().rollback();
	}

	@Override
	public void rollback(Savepoint savepoint) throws SQLException {
		_getConnection().rollback(savepoint);
	}

	@Override
	public void setAutoCommit(boolean autoCommit) throws SQLException {
		_getConnection().setAutoCommit(autoCommit);
	}

	@Override
	public void setCatalog(String catalog) throws SQLException {
		_getConnection().setCatalog(catalog);
	}

	@Override
	public void setClientInfo(Properties properties)
		throws SQLClientInfoException {

		_getConnection().setClientInfo(properties);
	}

	@Override
	public void setClientInfo(String name, String value)
		throws SQLClientInfoException {

		_getConnection().setClientInfo(name, value);
	}

	@Override
	public void setHoldability(int holdability) throws SQLException {
		_getConnection().setHoldability(holdability);
	}

	@Override
	public void setNetworkTimeout(Executor executor, int milliseconds)
		throws SQLException {

		_getConnection().setNetworkTimeout(executor, milliseconds);
	}

	@Override
	public void setReadOnly(boolean readOnly) throws SQLException {
		_getConnection().setReadOnly(readOnly);
	}

	@Override
	public Savepoint setSavepoint() throws SQLException {
		return _getConnection().setSavepoint();
	}

	@Override
	public Savepoint setSavepoint(String name) throws SQLException {
		return _getConnection().setSavepoint(name);
	}

	@Override
	public void setSchema(String schema) throws SQLException {
		_getConnection().setSchema(schema);
	}

	@Override
	public void setTransactionIsolation(int level) throws SQLException {
		_getConnection().setTransactionIsolation(level);
	}

	@Override
	public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
		_getConnection().setTypeMap(map);
	}

	@Override
	public <T> T unwrap(Class<T> clazz) throws SQLException {

		// JDK 6

		if (!Connection.class.equals(clazz)) {
			throw new SQLException("Invalid class " + clazz);
		}

		return (T)this;
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
		ConnectionThreadProxy.class);

	private static final Map<Long, Connection> _connectionMap =
		new ConcurrentHashMap<>();

	private volatile List<Long> _currentThreads = new ArrayList<>();

}