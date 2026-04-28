package jbailey.stu.mmu.bigbook.commons.dao;

// Java Imports
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Interface for mapping a single row of a {@link ResultSet} object to a domain object.
 * Each implementation encapsulates different mapping behind this interface.
 * The DAO can accept any {@link DatabaseRowMapper} without needing to know the concrete mapping logic.
 * New mappers can be introduced without modifying existing code.
 * @param <T> as a generic object which can be any type of domain object produced by the mapping.
 */
@FunctionalInterface
public interface DatabaseRowMapper<T> {

    /**
     * Maps the current DB row to the given domain object.
     *
     * @param currentResultSet as the {@link ResultSet} positioned on the current row.
     * @return A fully constructed domain object.
     * @throws SQLException if a database access error occurs.
     * {@link ResultSet} is a Java SQL type which represents the result of a database query. It provides methods to access the data in the current row, such as {@code getInt()}, {@code getString()}, etc.
     */
    T mapRow(ResultSet currentResultSet) throws SQLException;
}
