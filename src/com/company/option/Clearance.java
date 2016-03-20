package com.company.option;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.company.util.Connect;
import com.company.util.Util;

/**
 * Generate list of items that will not be restocked 
 * ordered from fewest items to most remaining
 *
 */
public class Clearance extends Option {
	
	private static final String NAME = "Clearance";
	private static final String QUERY = "SELECT item_name,item_price,amount_in_stock FROM item WHERE (do_not_restock = true AND amount_in_stock > 0) ORDER BY amount_in_stock ASC";
	
	public Clearance() {
		super(NAME);
	}

	@Override
	public Result execute() throws ExecutionException {
		
		Result result = new Result();
		
		try (Connection c = Connect.openConnection();
				Statement stmt = Connect.createStatementFromConnection(c)) {
			ResultSet rs = stmt.executeQuery(QUERY);
			
			// checks if there are no results
			if (!rs.isBeforeFirst()) {
				result.message = "There are no items which will not be restocked";
			}
			
			result.message = "Here are the items that will not be restocked";
			result.results = Util.deepCopyResultSet(rs);
			return result;
		} catch (ClassNotFoundException | SQLException e) {
			throw new ExecutionException("Unexpected error", e);
		}
	}

}
