package jbailey.stu.mmu.bigbook.commons.dao.query;

import jbailey.stu.mmu.bigbook.commons.dao.DatabaseRowMapper;

import java.sql.SQLException;
import java.util.List;

/**
 * Database port for executing generic SQL
 * DAO classes depend on this port rather than on JDBC directly.
 */
public interface QueryPort {

    <T> List<T> queryList(String sql, DatabaseRowMapper<T> rowMapper) throws SQLException;

    <T> List<T> queryList(String sql, PreparedStatementParameterBinder binder, DatabaseRowMapper<T> rowMapper)
            throws SQLException;

    <T> T queryOne(String sql, PreparedStatementParameterBinder binder, DatabaseRowMapper<T> rowMapper)
            throws SQLException;

    int update(String sql, PreparedStatementParameterBinder binder) throws SQLException;

    int queryForInt(String sql) throws SQLException;

    int queryForInt(String sql, PreparedStatementParameterBinder binder) throws SQLException;
}

