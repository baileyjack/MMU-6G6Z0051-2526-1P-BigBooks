package jbailey.stu.mmu.bigbook.commons.dao.query;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Functional interface used to bind {@link PreparedStatement} parameters to an SQL statement.
 */
@FunctionalInterface
public interface PreparedStatementParameterBinder {
    /**
     * Binds parameters to a {@link PreparedStatement}.
     *
     * @param statement the {@link PreparedStatement} to bind parameters to
     * @throws SQLException if a database access error occurs
     */
    void bind(PreparedStatement statement) throws SQLException;
}
