package com.company.option;

import com.company.util.Context;

import java.sql.SQLException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.PreparedStatement;

import java.util.List;

import static com.company.util.Connect.*;
import static com.company.util.Util.*;

public class ConvertToPurchase extends Option {
	private static final String NAME = "Buy all items in shopping cart";
	private static final String[] SUBOPTION_NAMES = { "are you sure? (yes/no)" };

	private static final String CREATE_PURCHASE = "INSERT INTO Purchase (customer_id) VALUES (?) RETURNING purchase_id;";
	private static final String CREATE_ITEM_TO_PURCHASE = "INSERT INTO Item_to_Purchase (purchase_id, deal_id, item_id, quantity, purchase_price) SELECT ?, d.deal_id, i.item_id, quantity, coalesce(item_price * (1 - deal_percentage), item_price) FROM Cart_to_Item AS ci JOIN Item AS i ON (ci.item_id = i.item_id) LEFT OUTER JOIN Deal AS d ON (i.deal_id = d.deal_id) WHERE ci.cart_id = ?;";
	private static final String DECREASE_STOCK_FOR_ALL_PURCHASED_ITEMS = "UPDATE Item AS i SET amount_in_stock = i.amount_in_stock - quantity FROM (SELECT item_id, quantity FROM Item_to_Purchase WHERE purchase_id = ?) AS ip WHERE ip.item_id = i.item_id;";
	private static final String DISSOCIATE_CART_FROM_CUSTOMER = "UPDATE Customer SET cart_id = NULL WHERE customer_id = ?;";
	private static final String DELETE_CART = "DELETE FROM Shopping_Cart WHERE cart_id = ?;";
	private static final String FETCH_TOTAL_PURCHASE_COST = "SELECT sum(purchase_price * quantity) as total FROM Item_to_Purchase WHERE purchase_id = ?;";

	private Context context;

	public ConvertToPurchase(Context userContext) {
		super(NAME, SUBOPTION_NAMES);

		this.context = userContext;
	}

	public Result execute() throws ExecutionException {
		String confirmation = subOptionValues.get(SUBOPTION_NAMES[0]);
		boolean confirmed = confirmation.equals("yes");

		Result result = new Result();

		if (this.context.customerId == -1) {
			result.message = "No customer logged in, aborting purchase";
			return result;
		}

		if (this.context.cartId == -1) {
			result.message = "Customer has no cart, aborting purchase";
			return result;
		}

		if (!confirmed) {
			result.message = "Purchase aborted - confirmation must be provided";
			return result;
		}

		try (Connection c = openConnection()) {
			c.setAutoCommit(false);

			int purchaseId = createPurchase(c);
			createItemToPurchase(c, purchaseId);
			decreaseStockForAllPurchasedItems(c, purchaseId);
			dissociateCartFromCustomer(c);
			deleteCart(c);

			result.message = "Your purchase has been completed - your bill is below.";
			result.results = fetchTotalPrice(c, purchaseId);

			c.commit();
			c.setAutoCommit(true);

			return result;
		} catch (SQLException | ClassNotFoundException e) {
			throw new ExecutionException("Unexpected error occurred while making purchase", e);
		}
	}

	private int createPurchase(Connection c) throws SQLException, ExecutionException {
		int customerId = this.context.customerId;

		try (PreparedStatement create = prepareStatementFromConnection(c, CREATE_PURCHASE)) {
			create.setInt(1, customerId);

			ResultSet rs = create.executeQuery();

			if (!rs.isBeforeFirst()) {
				throw new ExecutionException("Couldn't create purchase");
			}

			rs.next();
			return rs.getInt("purchase_id");
		}
	}

	private void createItemToPurchase(Connection c, int purchaseId) throws SQLException {
		// need purchaseId, cartId in that order
		int cartId = this.context.cartId;

		try (PreparedStatement create = prepareStatementFromConnection(c, CREATE_ITEM_TO_PURCHASE)) {
			create.setInt(1, purchaseId);
			create.setInt(2, cartId);

			create.execute();
		}
	}

	private void decreaseStockForAllPurchasedItems(Connection c, int purchaseId) throws SQLException {
		try (PreparedStatement update = prepareStatementFromConnection(c, DECREASE_STOCK_FOR_ALL_PURCHASED_ITEMS)) {
			update.setInt(1, purchaseId);
			update.execute();
		}
	}

	private void dissociateCartFromCustomer(Connection c) throws SQLException {
		int customerId = this.context.customerId;

		try (PreparedStatement dissociate = prepareStatementFromConnection(c, DISSOCIATE_CART_FROM_CUSTOMER)) {
			dissociate.setInt(1, customerId);
			dissociate.execute();
		}
	}

	private void deleteCart(Connection c) throws SQLException {
		int cartId = this.context.cartId;

		try (PreparedStatement delete = prepareStatementFromConnection(c, DELETE_CART)) {
			delete.setInt(1, cartId);
			delete.execute();

			// Remove the cartId from the current context
			this.context.cartId = -1;
		}
	}

	private List<List<String>> fetchTotalPrice(Connection c, int purchaseId) throws SQLException {
		try (PreparedStatement fetch = prepareStatementFromConnection(c, FETCH_TOTAL_PURCHASE_COST)) {
			fetch.setInt(1, purchaseId);
			ResultSet rs = fetch.executeQuery();
			return deepCopyResultSet(rs);
		}
	}
}
