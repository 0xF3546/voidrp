package de.polo.voidroleplay.database;

import java.sql.ResultSet;
import java.sql.SQLException;

@FunctionalInterface
public interface ResultMapper<T> {
    T map(ResultSet resultSet) throws SQLException;
}
