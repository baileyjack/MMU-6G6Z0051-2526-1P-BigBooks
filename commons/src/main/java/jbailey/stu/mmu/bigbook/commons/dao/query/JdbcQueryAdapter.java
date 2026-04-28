package jbailey.stu.mmu.bigbook.commons.dao.query;

// Internal Imports
import jbailey.stu.mmu.bigbook.commons.config.DatabaseConfig;
import jbailey.stu.mmu.bigbook.commons.dao.DatabaseRowMapper;

// Java Imports
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC adapter for {@link QueryPort}.
 * Owns connection and lifecycle so DAOs contain domain query definitions only.
 * Decouples the data access strategy from the business logic.
 */
public class JdbcQueryAdapter implements QueryPort {

    /**
     * Executes a query and maps the results to a list of domain objects for a given {@link DatabaseRowMapper}
     * Uses {@link Statement} from Java.sql as this is best suited to queries without parameters.
     * queryList(sql, rowMapper) — fixed query
     * queryForInt(sql) — fixed count query
     * @param sql as a fully built SQL string
     * @param rowMapper as the {@link DatabaseRowMapper} object defining the mapping strategy for the database result set
     * @return a {@code List<T>} of objects mapped from the database result set
     * @param <T> as a generic which is mapped to the type of business domain object returned
     * @throws SQLException if there is an error executing the query or mapping results
     */
    @Override
    public <T> List<T> queryList(String sql, DatabaseRowMapper<T> rowMapper) throws SQLException {
        try (Connection connection = DatabaseConfig.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            return mapList(resultSet, rowMapper);
        }
    }

    /**
     * Executes a query and maps the results to a list of domain objects for a given {@link DatabaseRowMapper}
     * Uses {@link PreparedStatement} as this is best suited to queries with parameters.
     * @param sql as a fully built SQL string
     * @param statementParameterBinder as the {@link PreparedStatementParameterBinder} functional interface which binds given parameters to the {@link PreparedStatement}
     * @param rowMapper as the {@link DatabaseRowMapper} object defining the mapping strategy for the database result set
     * @return a {@code List<T>} of business domain objects mapped from the database result set
     * @param <T> as a generic which is mapped to the type of business domain object returned
     * @throws SQLException if there is an error executing the query or mapping results
     */
    @Override
    public <T> List<T> queryList(String sql, PreparedStatementParameterBinder statementParameterBinder, DatabaseRowMapper<T> rowMapper)
            throws SQLException {
        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statementParameterBinder.bind(statement);
            try (ResultSet resultSet = statement.executeQuery()) {
                return mapList(resultSet, rowMapper);
            }
        }
    }

    /**
     * Executes a query and maps the FIRST query result to a domain object
     * Uses {@link PreparedStatement} from Java.sql to allow queries with parameters.
     * @param sql as a fully built SQL string
     * @param statementParameterBinder as the {@link PreparedStatementParameterBinder }functional interface which binds given parameters to the {@link PreparedStatement}
     * @param rowMapper as the {@link DatabaseRowMapper} object defining the mapping strategy for the database result set
     * @return a single {@code <T>} generic business domain object mapped from the database result
     * @param <T> as a generic which is mapped to the type of business domain object returned
     * @throws SQLException if there is an error executing the query or mapping results
     */
    @Override
    public <T> T queryOne(String sql, PreparedStatementParameterBinder statementParameterBinder, DatabaseRowMapper<T> rowMapper)
            throws SQLException {
        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statementParameterBinder.bind(statement);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return rowMapper.mapRow(resultSet);
                }
                return null;
            }
        }
    }

    /**
     * Performs insert, update, delete queries
     * @param sql as an SQL string without parameters
     * @param statementParameterBinder which binds parameters to the SQL Prepared Statement
     * @return an integer showing the number of affected rows
     * @throws SQLException if there is an error executing the query
     */
    @Override
    public int update(String sql, PreparedStatementParameterBinder statementParameterBinder) throws SQLException {
        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statementParameterBinder.bind(statement);
            return statement.executeUpdate();
        }
    }

    /**
     * Explicitly executes a query which is for scalar values instead of a domain object.
     * This is similar to the queryForInt from Spring while demonstrating my own implementation of it.
     * Because not every use case needs a full mapping strategy, more efficient queries can be achieved.
     * @param sql as an SQL string
     * @return a single integer value from the first column of the first row of the result set, or 0 wherever there are no results
     * @throws SQLException if there is an error executing the query
     */
    @Override
    public int queryForInt(String sql) throws SQLException {
        try (Connection connection = DatabaseConfig.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            return resultSet.next() ? resultSet.getInt(1) : 0;
        }
    }

    /**
     * Executes a query which is for scalar values instead of a domain object.
     * but allows parameters in the query for efficiency
     * @param sql as an SQL string
     * @param statementParameterBinder as the {@link PreparedStatementParameterBinder} functional interface which, given parameters, binds parameters to the {@link PreparedStatement}
     * @return a single integer value from the first column of the first row of the result set, or 0 when there are no results
     * @throws SQLException if there is an error executing the query
     */
    @Override
    public int queryForInt(String sql, PreparedStatementParameterBinder statementParameterBinder) throws SQLException {
        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statementParameterBinder.bind(statement);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? resultSet.getInt(1) : 0;
            }
        }
    }

    /**
     * Both queryList overloads use one uterator pattern
     * DRY coding so the loop isn't run twice
     * Reusable in any multi-row method
     * @param resultSet as a ResultSet from a statement
     * @param rowMapper as the RowMapper strategy which maps the database result set
     * @return the list of results mapped from the DB result set
     * @param <T> a generic defining the business domain model to use
     * @throws SQLException if there is an error mapping the result set
     */
    private <T> List<T> mapList(ResultSet resultSet, DatabaseRowMapper<T> rowMapper)
            throws SQLException {
        List<T> results = new ArrayList<>();
        while (resultSet.next()) {
            results.add(rowMapper.mapRow(resultSet));
        }
        return results;
    }
}
