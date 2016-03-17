package com.company.option;

import com.company.util.Context;

import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Connection;
import java.util.List;

import static com.company.util.Connect.*;
import static com.company.util.Util.deepCopyResultSet;

public class AddToCart extends Option {
	private static final String NAME = "Add to cart";
	private static final String[] SUBOPTION_NAMES = { "item id", "quantity" };

	private static final String CREATE_CART_QUERY = "INSERT INTO Shopping_Cart DEFAULT VALUES RETURNING cart_id;";
	private static final String TOUCH_CART_QUERY = "UPDATE Shopping_Cart SET last_changed = now() WHERE cart_id = ?;";
	private static final String UPDATE_CUSTOMER_QUERY = "UPDATE Customer SET cart_id = ? WHERE customer_id = ?;";
	private static final String AMOUNT_IN_STOCK = "SELECT amount_in_stock FROM Item WHERE item_id = ?;";
	private static final String INSERT_CART_ITEM = "INSERT INTO Cart_to_Item (cart_id, item_id, quantity) VALUES (?, ?, ?);";
	private static final String QUANTITY_IN_CART = "SELECT quantity FROM Cart_to_Item WHERE cart_id = ? AND item_id = ?;";
	private static final String GET_CART_CONTENTS = "SELECT i.item_id, item_name, quantity, item_price FROM Item i, Cart_to_Item c WHERE c.item_id = i.item_id AND cart_id = ?;";

	private Context context;

	public AddToCart(Context context) {
		super(NAME, SUBOPTION_NAMES);

		this.context = context;
	}

	public Result execute() throws ExecutionException {
		int itemId;
		int quantity;

		Result result = new Result();

		try {
			itemId = Integer.parseInt(subOptionValues.get(SUBOPTION_NAMES[0]));
			quantity = Integer.parseInt(subOptionValues.get(SUBOPTION_NAMES[1]));
		} catch (NumberFormatException e) {
			result.message = "Invalid input - ids must be integers";
			return result;
		}

		if (context.customerId == -1) {
			result.message = "Customer not logged in!";
			return result;
		}

		List<List<String>> results;

		try (Connection conn = openConnection()) {
			setupCart(conn, context);
			addItemToCart(conn, context, itemId, quantity);
			results = getAllCartItems(conn, context);
		} catch (SQLException | ClassNotFoundException e) {
			throw new ExecutionException("Couldn't open connection!", e);
		}

		result.message = "Item successfully added to cart";
		result.results = results;
		return result;
		// if quantity requested is greater than amount in stock, take all stock
	}

	private static void setupCart(Connection conn, Context context) throws ExecutionException {
		if (context.cartId != -1) {
			touchCart(conn, context.cartId);
		} else {
			createCartForCustomer(conn, context);
		}
	}

	private static void touchCart(Connection conn, int cartId) throws ExecutionException {
		try (PreparedStatement stmt = prepareStatementFromConnection(conn, TOUCH_CART_QUERY)) {
			stmt.setInt(1, cartId);
			stmt.execute();
		} catch (SQLException e) {
			throw new ExecutionException("Error while marking cart as changed", e);
		}
	}

	private static void createCartForCustomer(Connection conn, Context context) throws ExecutionException {
		try (Statement createCartStmt = createStatementFromConnection(conn);
				PreparedStatement customerUpdateStmt = prepareStatementFromConnection(conn, UPDATE_CUSTOMER_QUERY)) {
			// Run as a transaction
			conn.setAutoCommit(false);

			ResultSet rs = createCartStmt.executeQuery(CREATE_CART_QUERY);

			if (!rs.isBeforeFirst()) {
				throw new ExecutionException("Could not create cart for customer");
			}

			rs.next();
			int newCartId = rs.getInt("cart_id");

			customerUpdateStmt.setInt(1, newCartId);
			customerUpdateStmt.setInt(2, context.customerId);

			customerUpdateStmt.execute();

			conn.commit();

			// Only set if the full transaction worked.
			context.cartId = newCartId;

			// Reset autocommit for other jobs in this task.
			conn.setAutoCommit(true);
		} catch (SQLException e) {
			if (conn != null) {
				try {
					conn.rollback();
				} catch (SQLException e2) {
					throw new ExecutionException("Rollback failed. Java I love you, but you're bringing me down.", e2);
				}
			}

			throw new ExecutionException("Unexpected error while creating cart for customer", e);
		}
	}

	private static void addItemToCart(Connection conn, Context context, int itemId, int quantity) throws ExecutionException {
		// Want to allocate the min of quantity and amount_in_stock to record
		try (PreparedStatement quantityInStock = prepareStatementFromConnection(conn, AMOUNT_IN_STOCK);
				PreparedStatement quantityInCart = prepareStatementFromConnection(conn, QUANTITY_IN_CART);
				PreparedStatement addToCart = prepareStatementFromConnection(conn, INSERT_CART_ITEM)) {
			quantityInStock.setInt(1, itemId);
			ResultSet rs = quantityInStock.executeQuery();

			if (!rs.isBeforeFirst()) {
				throw new ExecutionException("Item could not be found!");
			}

			rs.next();
			int inStock = rs.getInt("amount_in_stock");

			quantityInCart.setInt(1, context.cartId);
			quantityInCart.setInt(2, itemId);

			rs = quantityInCart.executeQuery();

			int inCart = 0;
			if (rs.isBeforeFirst()) {
				rs.next();
				inCart = rs.getInt(1);
			}

			if (inCart + quantity > inStock) {
				quantity = inStock;
			}

			addToCart.setInt(1, context.cartId);
			addToCart.setInt(2, itemId);
			addToCart.setInt(3, quantity);

			addToCart.execute();
		} catch (SQLException e) {
			throw new ExecutionException("Unexpected error while adding item to cart", e);
		}
	}

	private static List<List<String>> getAllCartItems(Connection conn, Context context) throws ExecutionException {
		try (PreparedStatement fetchAll = prepareStatementFromConnection(conn, GET_CART_CONTENTS)) {
			fetchAll.setInt(1, context.cartId);

			ResultSet rs = fetchAll.executeQuery();

			return deepCopyResultSet(rs);
		} catch (SQLException e) {
			throw new ExecutionException("Unexpected error while fetching all cart items", e);
		}
	}
}
