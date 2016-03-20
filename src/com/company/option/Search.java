package com.company.option;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.Statement;

import static com.company.util.Util.deepCopyResultSet;
import static com.company.util.Connect.openConnection;

public class Search extends Option {
    private static final String NAME = "Search";
    private static final String[] SUBOPTION_NAMES = { "item name", "type", "size", "colour", "min price", "max price", "player name", "team name", "on sale (yes/no)" };
    private static final String COLUMNS = "i.item_id AS id, item_name AS name, type, colour, size, item_price AS regular_price, item_price * (1 - deal_percentage) AS sale_price, CASE WHEN amount_in_stock > 0 THEN TRUE ELSE FALSE END AS in_stock ";

    public Search() {
        super(NAME, SUBOPTION_NAMES);
    }

    public Result execute() throws ExecutionException {
        String playerName = subOptionValues.get(SUBOPTION_NAMES[6]);
        String teamName = subOptionValues.get(SUBOPTION_NAMES[7]);

        StringBuilder queryBuilder = new StringBuilder();
        if (playerName.length() > 0 && teamName.length() > 0) {
            // player and team -> (Player p, Players_Teams pt, Team t WHERE player_name ILIKE ? AND team_name ILIKE ?) JOIN Player_Item JOIN Item
            queryBuilder
                .append("SELECT ")
                .append(COLUMNS)
                .append("FROM Players_Teams AS pt JOIN (SELECT * FROM Team WHERE team_name ILIKE '%")
                .append(teamName)
                .append("%') AS t ON pt.team_id = t.team_id JOIN (SELECT * FROM Player WHERE player_name ILIKE '%")
                .append(playerName)
                .append("%') AS p ON pt.player_id = p.player_id JOIN Player_Item AS pi ON p.player_id = pi.player_id AND t.team_id = pi.team_id JOIN Item AS i ON pi.item_id = i.item_id ");
        } else if (playerName.length() > 0 && teamName.length() == 0) {
            // just player -> (SELECT * FROM Player WHERE player_name ILIKE ?) JOIN Item
            queryBuilder
                .append("SELECT ")
                .append(COLUMNS)
                .append("FROM (SELECT player_id FROM Player WHERE player_name ILIKE '%")
                .append(playerName)
                .append("%') AS p JOIN Player_Item AS pi ON p.player_id = pi.player_id JOIN Item AS i ON pi.item_id = i.item_id ");
        } else if (playerName.length() == 0 && teamName.length() > 0) {
            // just team -> need playeritems and teamitems where team is present
            queryBuilder
                .append("SELECT ")
                .append(COLUMNS)
                .append("FROM (SELECT team_id FROM Team WHERE team_name ILIKE '%")
                .append(teamName)
                .append("%') AS t JOIN (SELECT item_id, team_id FROM Team_Item UNION SELECT item_id, team_id FROM Player_Item) AS tpi ON t.team_id = tpi.team_id JOIN Item AS i ON tpi.item_id = i.item_id ");
        } else {
            // neither -> SELECT * FROM Item
            queryBuilder.append("SELECT ")
                .append(COLUMNS)
                .append("FROM Item AS i ");
        }

        queryBuilder
            .append("LEFT OUTER JOIN (SELECT * FROM Deal WHERE CURRENT_DATE BETWEEN start_date AND end_date) AS d ON i.deal_id = d.deal_id ")
            .append("WHERE ((i.do_not_restock = TRUE AND i.amount_in_stock > 0) OR (i.do_not_restock = FALSE)) ");

        String itemName = subOptionValues.get(SUBOPTION_NAMES[0]);
        if (itemName.length() > 0) {
            buildIlikeCondition(queryBuilder, "item_name", itemName);
        }

        String type = subOptionValues.get(SUBOPTION_NAMES[1]);
        if (type.length() > 0) {
            buildIlikeCondition(queryBuilder, "type", type);
        }

        String size = subOptionValues.get(SUBOPTION_NAMES[2]);
        if (size.length() > 0) {
            buildIlikeCondition(queryBuilder, "size", size);
        }

        String colour = subOptionValues.get(SUBOPTION_NAMES[3]);
        if (colour.length() > 0) {
            buildIlikeCondition(queryBuilder, "colour", colour);
        }

        String minPrice = subOptionValues.get(SUBOPTION_NAMES[4]);
        if (minPrice.length() > 0) {
            queryBuilder
                .append("AND i.item_price::numeric >= ")
                .append(minPrice)
                .append(" ");
        }

        String maxPrice = subOptionValues.get(SUBOPTION_NAMES[5]);
        if (maxPrice.length() > 0) {
            queryBuilder
                .append("AND i.item_price::numeric <= ")
                .append(maxPrice)
                .append(" ");
        }

        boolean onSale = subOptionValues.get(SUBOPTION_NAMES[8]).equals("yes");
        if (onSale) {
            queryBuilder.append("AND i.deal_id IS NOT NULL");
        }

        queryBuilder.append(";");
        String query = queryBuilder.toString();

        try (Connection c = openConnection();
                Statement stmt = c.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)) {
            ResultSet rs = stmt.executeQuery(query);
            Result result = new Result();

            if (!rs.isBeforeFirst()) {
                // No items
                result.message = "No items were found that matched your search terms.";
                return result;
            }

            result.results = deepCopyResultSet(rs);
            return result;
        } catch (SQLException | ClassNotFoundException e) {
            throw new ExecutionException("Unexpected error", e);
        }
    }

    private void buildIlikeCondition(StringBuilder qb, String name, String value) {
        qb.append("AND i.")
            .append(name)
            .append(" ILIKE '%")
            .append(value)
            .append("%' ");
    }
}
