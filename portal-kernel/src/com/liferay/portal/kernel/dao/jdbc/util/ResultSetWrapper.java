/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.kernel.dao.jdbc.util;

import java.io.InputStream;
import java.io.Reader;

import java.math.BigDecimal;

import java.net.URL;

import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;

import java.util.Calendar;
import java.util.Map;

/**
 * @author Luis Ortiz
 */
public class ResultSetWrapper implements ResultSet {

	public ResultSetWrapper(ResultSet resultSet) {
		_resultSet = resultSet;
	}

	@Override
	public boolean absolute(int i) throws SQLException {
		return _resultSet.absolute(i);
	}

	@Override
	public void afterLast() throws SQLException {
		_resultSet.afterLast();
	}

	@Override
	public void beforeFirst() throws SQLException {
		_resultSet.beforeFirst();
	}

	@Override
	public void cancelRowUpdates() throws SQLException {
		_resultSet.cancelRowUpdates();
	}

	@Override
	public void clearWarnings() throws SQLException {
		_resultSet.clearWarnings();
	}

	@Override
	public void close() throws SQLException {
		_resultSet.close();
	}

	@Override
	public void deleteRow() throws SQLException {
		_resultSet.deleteRow();
	}

	@Override
	public int findColumn(String s) throws SQLException {
		return _resultSet.findColumn(s);
	}

	@Override
	public boolean first() throws SQLException {
		return _resultSet.first();
	}

	@Override
	public Array getArray(int i) throws SQLException {
		return _resultSet.getArray(i);
	}

	@Override
	public Array getArray(String s) throws SQLException {
		return _resultSet.getArray(s);
	}

	@Override
	public InputStream getAsciiStream(int i) throws SQLException {
		return _resultSet.getAsciiStream(i);
	}

	@Override
	public InputStream getAsciiStream(String s) throws SQLException {
		return _resultSet.getAsciiStream(s);
	}

	@Override
	public BigDecimal getBigDecimal(int i) throws SQLException {
		return _resultSet.getBigDecimal(i);
	}

	@Override
	public BigDecimal getBigDecimal(int i, int i1) throws SQLException {
		return _resultSet.getBigDecimal(i, i1);
	}

	@Override
	public BigDecimal getBigDecimal(String s) throws SQLException {
		return _resultSet.getBigDecimal(s);
	}

	@Override
	public BigDecimal getBigDecimal(String s, int i) throws SQLException {
		return _resultSet.getBigDecimal(s, i);
	}

	@Override
	public InputStream getBinaryStream(int i) throws SQLException {
		return _resultSet.getBinaryStream(i);
	}

	@Override
	public InputStream getBinaryStream(String s) throws SQLException {
		return _resultSet.getBinaryStream(s);
	}

	@Override
	public Blob getBlob(int i) throws SQLException {
		return _resultSet.getBlob(i);
	}

	@Override
	public Blob getBlob(String s) throws SQLException {
		return _resultSet.getBlob(s);
	}

	@Override
	public boolean getBoolean(int i) throws SQLException {
		return _resultSet.getBoolean(i);
	}

	@Override
	public boolean getBoolean(String s) throws SQLException {
		return _resultSet.getBoolean(s);
	}

	@Override
	public byte getByte(int i) throws SQLException {
		return _resultSet.getByte(i);
	}

	@Override
	public byte getByte(String s) throws SQLException {
		return _resultSet.getByte(s);
	}

	@Override
	public byte[] getBytes(int i) throws SQLException {
		return _resultSet.getBytes(i);
	}

	@Override
	public byte[] getBytes(String s) throws SQLException {
		return _resultSet.getBytes(s);
	}

	@Override
	public Reader getCharacterStream(int i) throws SQLException {
		return _resultSet.getCharacterStream(i);
	}

	@Override
	public Reader getCharacterStream(String s) throws SQLException {
		return _resultSet.getCharacterStream(s);
	}

	@Override
	public Clob getClob(int i) throws SQLException {
		return _resultSet.getClob(i);
	}

	@Override
	public Clob getClob(String s) throws SQLException {
		return _resultSet.getClob(s);
	}

	@Override
	public int getConcurrency() throws SQLException {
		return _resultSet.getConcurrency();
	}

	@Override
	public String getCursorName() throws SQLException {
		return _resultSet.getCursorName();
	}

	@Override
	public Date getDate(int i) throws SQLException {
		return _resultSet.getDate(i);
	}

	@Override
	public Date getDate(int i, Calendar calendar) throws SQLException {
		return _resultSet.getDate(i, calendar);
	}

	@Override
	public Date getDate(String s) throws SQLException {
		return _resultSet.getDate(s);
	}

	@Override
	public Date getDate(String s, Calendar calendar) throws SQLException {
		return _resultSet.getDate(s, calendar);
	}

	@Override
	public double getDouble(int i) throws SQLException {
		return _resultSet.getDouble(i);
	}

	@Override
	public double getDouble(String s) throws SQLException {
		return _resultSet.getDouble(s);
	}

	@Override
	public int getFetchDirection() throws SQLException {
		return _resultSet.getFetchDirection();
	}

	@Override
	public int getFetchSize() throws SQLException {
		return _resultSet.getFetchSize();
	}

	@Override
	public float getFloat(int i) throws SQLException {
		return _resultSet.getFloat(i);
	}

	@Override
	public float getFloat(String s) throws SQLException {
		return _resultSet.getFloat(s);
	}

	@Override
	public int getHoldability() throws SQLException {
		return _resultSet.getHoldability();
	}

	@Override
	public int getInt(int i) throws SQLException {
		return _resultSet.getInt(i);
	}

	@Override
	public int getInt(String s) throws SQLException {
		return _resultSet.getInt(s);
	}

	@Override
	public long getLong(int i) throws SQLException {
		return _resultSet.getLong(i);
	}

	@Override
	public long getLong(String s) throws SQLException {
		return _resultSet.getLong(s);
	}

	@Override
	public ResultSetMetaData getMetaData() throws SQLException {
		return _resultSet.getMetaData();
	}

	@Override
	public Reader getNCharacterStream(int i) throws SQLException {
		return _resultSet.getNCharacterStream(i);
	}

	@Override
	public Reader getNCharacterStream(String s) throws SQLException {
		return _resultSet.getNCharacterStream(s);
	}

	@Override
	public NClob getNClob(int i) throws SQLException {
		return _resultSet.getNClob(i);
	}

	@Override
	public NClob getNClob(String s) throws SQLException {
		return _resultSet.getNClob(s);
	}

	@Override
	public String getNString(int i) throws SQLException {
		return _resultSet.getNString(i);
	}

	@Override
	public String getNString(String s) throws SQLException {
		return _resultSet.getNString(s);
	}

	@Override
	public Object getObject(int i) throws SQLException {
		return _resultSet.getObject(i);
	}

	@Override
	public <T> T getObject(int i, Class<T> clazz) throws SQLException {
		return _resultSet.getObject(i, clazz);
	}

	@Override
	public Object getObject(int i, Map<String, Class<?>> map)
		throws SQLException {

		return _resultSet.getObject(i, map);
	}

	@Override
	public Object getObject(String s) throws SQLException {
		return _resultSet.getObject(s);
	}

	@Override
	public <T> T getObject(String s, Class<T> clazz) throws SQLException {
		return _resultSet.getObject(s, clazz);
	}

	@Override
	public Object getObject(String s, Map<String, Class<?>> map)
		throws SQLException {

		return _resultSet.getObject(s, map);
	}

	@Override
	public Ref getRef(int i) throws SQLException {
		return _resultSet.getRef(i);
	}

	@Override
	public Ref getRef(String s) throws SQLException {
		return _resultSet.getRef(s);
	}

	@Override
	public int getRow() throws SQLException {
		return _resultSet.getRow();
	}

	@Override
	public RowId getRowId(int i) throws SQLException {
		return _resultSet.getRowId(i);
	}

	@Override
	public RowId getRowId(String s) throws SQLException {
		return _resultSet.getRowId(s);
	}

	@Override
	public short getShort(int i) throws SQLException {
		return _resultSet.getShort(i);
	}

	@Override
	public short getShort(String s) throws SQLException {
		return _resultSet.getShort(s);
	}

	@Override
	public SQLXML getSQLXML(int i) throws SQLException {
		return _resultSet.getSQLXML(i);
	}

	@Override
	public SQLXML getSQLXML(String s) throws SQLException {
		return _resultSet.getSQLXML(s);
	}

	@Override
	public Statement getStatement() throws SQLException {
		return _resultSet.getStatement();
	}

	@Override
	public String getString(int i) throws SQLException {
		return _resultSet.getString(i);
	}

	@Override
	public String getString(String s) throws SQLException {
		return _resultSet.getString(s);
	}

	@Override
	public Time getTime(int i) throws SQLException {
		return _resultSet.getTime(i);
	}

	@Override
	public Time getTime(int i, Calendar calendar) throws SQLException {
		return _resultSet.getTime(i, calendar);
	}

	@Override
	public Time getTime(String s) throws SQLException {
		return _resultSet.getTime(s);
	}

	@Override
	public Time getTime(String s, Calendar calendar) throws SQLException {
		return _resultSet.getTime(s, calendar);
	}

	@Override
	public Timestamp getTimestamp(int i) throws SQLException {
		return _resultSet.getTimestamp(i);
	}

	@Override
	public Timestamp getTimestamp(int i, Calendar calendar)
		throws SQLException {

		return _resultSet.getTimestamp(i, calendar);
	}

	@Override
	public Timestamp getTimestamp(String s) throws SQLException {
		return _resultSet.getTimestamp(s);
	}

	@Override
	public Timestamp getTimestamp(String s, Calendar calendar)
		throws SQLException {

		return _resultSet.getTimestamp(s, calendar);
	}

	@Override
	public int getType() throws SQLException {
		return _resultSet.getType();
	}

	@Override
	public InputStream getUnicodeStream(int i) throws SQLException {
		return _resultSet.getUnicodeStream(i);
	}

	@Override
	public InputStream getUnicodeStream(String s) throws SQLException {
		return _resultSet.getUnicodeStream(s);
	}

	@Override
	public URL getURL(int i) throws SQLException {
		return _resultSet.getURL(i);
	}

	@Override
	public URL getURL(String s) throws SQLException {
		return _resultSet.getURL(s);
	}

	@Override
	public SQLWarning getWarnings() throws SQLException {
		return _resultSet.getWarnings();
	}

	@Override
	public void insertRow() throws SQLException {
		_resultSet.insertRow();
	}

	@Override
	public boolean isAfterLast() throws SQLException {
		return _resultSet.isAfterLast();
	}

	@Override
	public boolean isBeforeFirst() throws SQLException {
		return _resultSet.isBeforeFirst();
	}

	@Override
	public boolean isClosed() throws SQLException {
		return _resultSet.isClosed();
	}

	@Override
	public boolean isFirst() throws SQLException {
		return _resultSet.isFirst();
	}

	@Override
	public boolean isLast() throws SQLException {
		return _resultSet.isLast();
	}

	@Override
	public boolean isWrapperFor(Class<?> clazz) throws SQLException {
		return ResultSet.class.equals(clazz);
	}

	@Override
	public boolean last() throws SQLException {
		return _resultSet.last();
	}

	@Override
	public void moveToCurrentRow() throws SQLException {
		_resultSet.moveToCurrentRow();
	}

	@Override
	public void moveToInsertRow() throws SQLException {
		_resultSet.moveToInsertRow();
	}

	@Override
	public boolean next() throws SQLException {
		return _resultSet.next();
	}

	@Override
	public boolean previous() throws SQLException {
		return _resultSet.previous();
	}

	@Override
	public void refreshRow() throws SQLException {
		_resultSet.refreshRow();
	}

	@Override
	public boolean relative(int i) throws SQLException {
		return _resultSet.relative(i);
	}

	@Override
	public boolean rowDeleted() throws SQLException {
		return _resultSet.rowDeleted();
	}

	@Override
	public boolean rowInserted() throws SQLException {
		return _resultSet.rowInserted();
	}

	@Override
	public boolean rowUpdated() throws SQLException {
		return _resultSet.rowUpdated();
	}

	@Override
	public void setFetchDirection(int i) throws SQLException {
		_resultSet.setFetchDirection(i);
	}

	@Override
	public void setFetchSize(int i) throws SQLException {
		_resultSet.setFetchSize(i);
	}

	@Override
	public <T> T unwrap(Class<T> clazz) throws SQLException {
		if (!ResultSet.class.equals(clazz)) {
			throw new SQLException("Invalid class " + clazz);
		}

		return (T)this;
	}

	@Override
	public void updateArray(int i, Array array) throws SQLException {
		_resultSet.updateArray(i, array);
	}

	@Override
	public void updateArray(String s, Array array) throws SQLException {
		_resultSet.updateArray(s, array);
	}

	@Override
	public void updateAsciiStream(int i, InputStream inputStream)
		throws SQLException {

		_resultSet.updateAsciiStream(i, inputStream);
	}

	@Override
	public void updateAsciiStream(int i, InputStream inputStream, int i1)
		throws SQLException {

		_resultSet.updateAsciiStream(i, inputStream, i1);
	}

	@Override
	public void updateAsciiStream(int i, InputStream inputStream, long l)
		throws SQLException {

		_resultSet.updateAsciiStream(i, inputStream, l);
	}

	@Override
	public void updateAsciiStream(String s, InputStream inputStream)
		throws SQLException {

		_resultSet.updateAsciiStream(s, inputStream);
	}

	@Override
	public void updateAsciiStream(String s, InputStream inputStream, int i)
		throws SQLException {

		_resultSet.updateAsciiStream(s, inputStream, i);
	}

	@Override
	public void updateAsciiStream(String s, InputStream inputStream, long l)
		throws SQLException {

		_resultSet.updateAsciiStream(s, inputStream, l);
	}

	@Override
	public void updateBigDecimal(int i, BigDecimal bigDecimal)
		throws SQLException {

		_resultSet.updateBigDecimal(i, bigDecimal);
	}

	@Override
	public void updateBigDecimal(String s, BigDecimal bigDecimal)
		throws SQLException {

		_resultSet.updateBigDecimal(s, bigDecimal);
	}

	@Override
	public void updateBinaryStream(int i, InputStream inputStream)
		throws SQLException {

		_resultSet.updateBinaryStream(i, inputStream);
	}

	@Override
	public void updateBinaryStream(int i, InputStream inputStream, int i1)
		throws SQLException {

		_resultSet.updateBinaryStream(i, inputStream, i1);
	}

	@Override
	public void updateBinaryStream(int i, InputStream inputStream, long l)
		throws SQLException {

		_resultSet.updateBinaryStream(i, inputStream, l);
	}

	@Override
	public void updateBinaryStream(String s, InputStream inputStream)
		throws SQLException {

		_resultSet.updateBinaryStream(s, inputStream);
	}

	@Override
	public void updateBinaryStream(String s, InputStream inputStream, int i)
		throws SQLException {

		_resultSet.updateBinaryStream(s, inputStream, i);
	}

	@Override
	public void updateBinaryStream(String s, InputStream inputStream, long l)
		throws SQLException {

		_resultSet.updateBinaryStream(s, inputStream, l);
	}

	@Override
	public void updateBlob(int i, Blob blob) throws SQLException {
		_resultSet.updateBlob(i, blob);
	}

	@Override
	public void updateBlob(int i, InputStream inputStream) throws SQLException {
		_resultSet.updateBlob(i, inputStream);
	}

	@Override
	public void updateBlob(int i, InputStream inputStream, long l)
		throws SQLException {

		_resultSet.updateBlob(i, inputStream, l);
	}

	@Override
	public void updateBlob(String s, Blob blob) throws SQLException {
		_resultSet.updateBlob(s, blob);
	}

	@Override
	public void updateBlob(String s, InputStream inputStream)
		throws SQLException {

		_resultSet.updateBlob(s, inputStream);
	}

	@Override
	public void updateBlob(String s, InputStream inputStream, long l)
		throws SQLException {

		_resultSet.updateBlob(s, inputStream, l);
	}

	@Override
	public void updateBoolean(int i, boolean b) throws SQLException {
		_resultSet.updateBoolean(i, b);
	}

	@Override
	public void updateBoolean(String s, boolean b) throws SQLException {
		_resultSet.updateBoolean(s, b);
	}

	@Override
	public void updateByte(int i, byte b) throws SQLException {
		_resultSet.updateByte(i, b);
	}

	@Override
	public void updateByte(String s, byte b) throws SQLException {
		_resultSet.updateByte(s, b);
	}

	@Override
	public void updateBytes(int i, byte[] bytes) throws SQLException {
		_resultSet.updateBytes(i, bytes);
	}

	@Override
	public void updateBytes(String s, byte[] bytes) throws SQLException {
		_resultSet.updateBytes(s, bytes);
	}

	@Override
	public void updateCharacterStream(int i, Reader reader)
		throws SQLException {

		_resultSet.updateCharacterStream(i, reader);
	}

	@Override
	public void updateCharacterStream(int i, Reader reader, int i1)
		throws SQLException {

		_resultSet.updateCharacterStream(i, reader, i1);
	}

	@Override
	public void updateCharacterStream(int i, Reader reader, long l)
		throws SQLException {

		_resultSet.updateCharacterStream(i, reader, l);
	}

	@Override
	public void updateCharacterStream(String s, Reader reader)
		throws SQLException {

		_resultSet.updateCharacterStream(s, reader);
	}

	@Override
	public void updateCharacterStream(String s, Reader reader, int i)
		throws SQLException {

		_resultSet.updateCharacterStream(s, reader, i);
	}

	@Override
	public void updateCharacterStream(String s, Reader reader, long l)
		throws SQLException {

		_resultSet.updateCharacterStream(s, reader, l);
	}

	@Override
	public void updateClob(int i, Clob clob) throws SQLException {
		_resultSet.updateClob(i, clob);
	}

	@Override
	public void updateClob(int i, Reader reader) throws SQLException {
		_resultSet.updateClob(i, reader);
	}

	@Override
	public void updateClob(int i, Reader reader, long l) throws SQLException {
		_resultSet.updateClob(i, reader, l);
	}

	@Override
	public void updateClob(String s, Clob clob) throws SQLException {
		_resultSet.updateClob(s, clob);
	}

	@Override
	public void updateClob(String s, Reader reader) throws SQLException {
		_resultSet.updateClob(s, reader);
	}

	@Override
	public void updateClob(String s, Reader reader, long l)
		throws SQLException {

		_resultSet.updateClob(s, reader, l);
	}

	@Override
	public void updateDate(int i, Date date) throws SQLException {
		_resultSet.updateDate(i, date);
	}

	@Override
	public void updateDate(String s, Date date) throws SQLException {
		_resultSet.updateDate(s, date);
	}

	@Override
	public void updateDouble(int i, double v) throws SQLException {
		_resultSet.updateDouble(i, v);
	}

	@Override
	public void updateDouble(String s, double v) throws SQLException {
		_resultSet.updateDouble(s, v);
	}

	@Override
	public void updateFloat(int i, float v) throws SQLException {
		_resultSet.updateFloat(i, v);
	}

	@Override
	public void updateFloat(String s, float v) throws SQLException {
		_resultSet.updateFloat(s, v);
	}

	@Override
	public void updateInt(int i, int i1) throws SQLException {
		_resultSet.updateInt(i, i1);
	}

	@Override
	public void updateInt(String s, int i) throws SQLException {
		_resultSet.updateInt(s, i);
	}

	@Override
	public void updateLong(int i, long l) throws SQLException {
		_resultSet.updateLong(i, l);
	}

	@Override
	public void updateLong(String s, long l) throws SQLException {
		_resultSet.updateLong(s, l);
	}

	@Override
	public void updateNCharacterStream(int i, Reader reader)
		throws SQLException {

		_resultSet.updateNCharacterStream(i, reader);
	}

	@Override
	public void updateNCharacterStream(int i, Reader reader, long l)
		throws SQLException {

		_resultSet.updateNCharacterStream(i, reader, l);
	}

	@Override
	public void updateNCharacterStream(String s, Reader reader)
		throws SQLException {

		_resultSet.updateNCharacterStream(s, reader);
	}

	@Override
	public void updateNCharacterStream(String s, Reader reader, long l)
		throws SQLException {

		_resultSet.updateNCharacterStream(s, reader, l);
	}

	@Override
	public void updateNClob(int i, NClob nClob) throws SQLException {
		_resultSet.updateNClob(i, nClob);
	}

	@Override
	public void updateNClob(int i, Reader reader) throws SQLException {
		_resultSet.updateNClob(i, reader);
	}

	@Override
	public void updateNClob(int i, Reader reader, long l) throws SQLException {
		_resultSet.updateNClob(i, reader, l);
	}

	@Override
	public void updateNClob(String s, NClob nClob) throws SQLException {
		_resultSet.updateNClob(s, nClob);
	}

	@Override
	public void updateNClob(String s, Reader reader) throws SQLException {
		_resultSet.updateNClob(s, reader);
	}

	@Override
	public void updateNClob(String s, Reader reader, long l)
		throws SQLException {

		_resultSet.updateNClob(s, reader, l);
	}

	@Override
	public void updateNString(int i, String s) throws SQLException {
		_resultSet.updateNString(i, s);
	}

	@Override
	public void updateNString(String s, String s1) throws SQLException {
		_resultSet.updateNString(s, s1);
	}

	@Override
	public void updateNull(int i) throws SQLException {
		_resultSet.updateNull(i);
	}

	@Override
	public void updateNull(String s) throws SQLException {
		_resultSet.updateNull(s);
	}

	@Override
	public void updateObject(int i, Object object) throws SQLException {
		_resultSet.updateObject(i, object);
	}

	@Override
	public void updateObject(int i, Object object, int i1) throws SQLException {
		_resultSet.updateObject(i, object, i1);
	}

	@Override
	public void updateObject(String s, Object object) throws SQLException {
		_resultSet.updateObject(s, object);
	}

	@Override
	public void updateObject(String s, Object object, int i)
		throws SQLException {

		_resultSet.updateObject(s, object, i);
	}

	@Override
	public void updateRef(int i, Ref ref) throws SQLException {
		_resultSet.updateRef(i, ref);
	}

	@Override
	public void updateRef(String s, Ref ref) throws SQLException {
		_resultSet.updateRef(s, ref);
	}

	@Override
	public void updateRow() throws SQLException {
		_resultSet.updateRow();
	}

	@Override
	public void updateRowId(int i, RowId rowId) throws SQLException {
		_resultSet.updateRowId(i, rowId);
	}

	@Override
	public void updateRowId(String s, RowId rowId) throws SQLException {
		_resultSet.updateRowId(s, rowId);
	}

	@Override
	public void updateShort(int i, short i1) throws SQLException {
		_resultSet.updateShort(i, i1);
	}

	@Override
	public void updateShort(String s, short i) throws SQLException {
		_resultSet.updateShort(s, i);
	}

	@Override
	public void updateSQLXML(int i, SQLXML sqlxml) throws SQLException {
		_resultSet.updateSQLXML(i, sqlxml);
	}

	@Override
	public void updateSQLXML(String s, SQLXML sqlxml) throws SQLException {
		_resultSet.updateSQLXML(s, sqlxml);
	}

	@Override
	public void updateString(int i, String s) throws SQLException {
		_resultSet.updateString(i, s);
	}

	@Override
	public void updateString(String s, String s1) throws SQLException {
		_resultSet.updateString(s, s1);
	}

	@Override
	public void updateTime(int i, Time time) throws SQLException {
		_resultSet.updateTime(i, time);
	}

	@Override
	public void updateTime(String s, Time time) throws SQLException {
		_resultSet.updateTime(s, time);
	}

	@Override
	public void updateTimestamp(int i, Timestamp timestamp)
		throws SQLException {

		_resultSet.updateTimestamp(i, timestamp);
	}

	@Override
	public void updateTimestamp(String s, Timestamp timestamp)
		throws SQLException {

		_resultSet.updateTimestamp(s, timestamp);
	}

	@Override
	public boolean wasNull() throws SQLException {
		return _resultSet.wasNull();
	}

	private volatile ResultSet _resultSet;

}