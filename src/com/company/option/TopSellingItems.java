package com.company.option;

import com.company.util.Context;
import com.company.util.Util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static com.company.util.Connect.openConnection;
import static com.company.util.Connect.prepareStatementFromConnection;

public class TopSellingItems extends Option {
    private static final String NAME = "Find top selling items";
    private static final String[] SUBOPTION_NAMES = {"How many items would you like to see?"};
    private static final String QUERY = "SELECT Item.item_id, item_name FROM Item " +
            "JOIN (" +
            "SELECT item_id, sum(quantity) AS purchase_sum " +
            "FROM Item_to_Purchase " +
            "GROUP BY item_id " +
            "ORDER BY purchase_sum DESC LIMIT ?" +
            ") " +
            "AS Best_Items ON Item.item_id = Best_Items.item_id " +
            "ORDER BY purchase_sum DESC;";

    public TopSellingItems() {
        super(NAME, SUBOPTION_NAMES);
    }

    public Result execute() throws ExecutionException {
        Result result = new Result();
        int limit;

        try {
            limit = Integer.parseInt(subOptionValues.get(SUBOPTION_NAMES[0]));
        } catch (NumberFormatException e) {
            result.message = "Invalid input - you must give an integer";
            return result;
        }

        try (Connection conn = openConnection();
             PreparedStatement stmt = prepareStatementFromConnection(conn, QUERY)) {
            stmt.setInt(1, limit);
            stmt.execute();
            ResultSet rs = stmt.executeQuery();

            if (!rs.isBeforeFirst()) {
                result.message = "No items found!";
                return result;
            }

            result.message = "Here are our " + limit + " best selling items:";
            result.results = Util.deepCopyResultSet(rs);
            return result;

        } catch (SQLException | ClassNotFoundException e) {
            throw new ExecutionException("Couldn't open connection!", e);
        }
    }
}
