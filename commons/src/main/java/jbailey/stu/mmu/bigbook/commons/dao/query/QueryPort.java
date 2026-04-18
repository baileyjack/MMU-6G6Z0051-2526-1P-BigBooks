package jbailey.stu.mmu.bigbook.commons.dao.query;

import jbailey.stu.mmu.bigbook.commons.dao.DatabaseRowMapper;

import java.sql.SQLException;
import java.util.List;

/**
 * Database port for executing generic SQL.
 * DAO classes depend on this port rather than on JDBC directly.
 */
public interface QueryPort {

    /**
     * Executes a query and maps results to a {@code List} of domain objects.
     *
     * @param sql the SQL query {@code String}
     * @param rowMapper the {@link DatabaseRowMapper} for mapping rows
     * @param <T> the type of objects in the result {@code List}
     * @return a {@code List<T>} of mapped objects
     * @throws SQLException if a database access error occurs
     */
    <T> List<T> queryList(String sql, DatabaseRowMapper<T> rowMapper) throws SQLException;

    /**
     * Executes a parameterized query and maps results to a {@code List} of domain objects.
     *
     * @param sql the SQL query {@code String}
     * @param binder the {@link PreparedStatementParameterBinder} for binding parameters
     * @param rowMapper the {@link DatabaseRowMapper} for mapping rows
     * @param <T> the type of objects in the result {@code List}
     * @return a {@code List<T>} of mapped objects
     * @throws SQLException if a database access error occurs
     */
    <T> List<T> queryList(String sql, PreparedStatementParameterBinder binder, DatabaseRowMapper<T> rowMapper)
            throws SQLException;

    /**
     * Executes a parameterized query and maps the first result to a domain object.
     *
     * @param sql the SQL query {@code String}
     * @param binder the {@link PreparedStatementParameterBinder} for binding parameters
     * @param rowMapper the {@link DatabaseRowMapper} for mapping rows
     * @param <T> the type of the returned object
     * @return a single object of type {@code T}, or {@code null} if no result found
     * @throws SQLException if a database access error occurs
     */
    <T> T queryOne(String sql, PreparedStatementParameterBinder binder, DatabaseRowMapper<T> rowMapper)
            throws SQLException;

    /**
     * Executes an INSERT, UPDATE, or DELETE statement.
     *
     * @param sql the SQL statement {@code String}
     * @param binder the {@link PreparedStatementParameterBinder} for binding parameters
     * @return the number of rows affected
     * @throws SQLException if a database access error occurs
     */
    int update(String sql, PreparedStatementParameterBinder binder) throws SQLException;

    /**
     * Executes a scalar query and returns an {@code int} result.
     *
     * @param sql the SQL query {@code String}
     * @return an {@code int} value from the query result
     * @throws SQLException if a database access error occurs
     */
    int queryForInt(String sql) throws SQLException;

    /**
     * Executes a parameterized scalar query and returns an {@code int} result.
     *
     * @param sql the SQL query {@code String}
     * @param binder the {@link PreparedStatementParameterBinder} for binding parameters
     * @return an {@code int} value from the query result
     * @throws SQLException if a database access error occurs
     */
    int queryForInt(String sql, PreparedStatementParameterBinder binder) throws SQLException;
}
