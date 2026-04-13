package jbailey.stu.mmu.bigbook.commons.dao.query;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Functional interface used to bind {@link PreparedStatement} parameters to an SQL statement.
 */
@FunctionalInterface
public interface PreparedStatementParameterBinder {
    void bind(PreparedStatement statement) throws SQLException;
}

