package de.polo.core.database.utility;

import java.sql.ResultSet;
import java.sql.Statement;

public class Result {
    private final ResultSet resultSet;
    private final Statement statement;

    public Result(ResultSet resultSet, Statement statement) {
        this.resultSet = resultSet;
        this.statement = statement;
    }

    public static Result of(ResultSet resultSet, Statement statement) {
        return new Result(resultSet, statement);
    }

    public ResultSet resultSet() {
        return resultSet;
    }

    public void close() {
        try {
            statement.close();
            resultSet.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean next() {
        try {
            return resultSet.next();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public Statement statement() {
        return statement;
    }
}
