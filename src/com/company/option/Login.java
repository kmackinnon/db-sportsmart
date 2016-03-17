package com.company.option;

import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Connection;
import java.sql.PreparedStatement;

import static com.company.util.Connect.openConnection;
import static com.company.util.PasswordUtilities.isPasswordCorrect;

import com.company.util.Util;
import com.company.util.Context;

public class Login extends Option {
	private static final String NAME = "Login";
	private static final String[] SUBOPTION_NAMES = { "username", "password" };
	private static final String QUERY = "SELECT customer_id, name, cart_id, password_hash, password_salt FROM Customer WHERE name = ?;";

	private Context context;

	public Login(Context context) {
		super(NAME, SUBOPTION_NAMES);

		this.context = context;
	}

	public Result execute() throws ExecutionException {
		String username = subOptionValues.get(SUBOPTION_NAMES[0]);
		String password = subOptionValues.get(SUBOPTION_NAMES[1]);

		Result result = new Result();

		try (Connection c = openConnection();
				PreparedStatement stmt = c.prepareStatement(QUERY, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)) {
			stmt.setString(1, username);
			ResultSet rs = stmt.executeQuery();

			if (!rs.isBeforeFirst()) {
				result.message = String.format("No user \"%s\" found", username);
				return result;
			}

			rs.next();

			String hash = rs.getString("password_hash");
			String salt = rs.getString("password_salt");
			this.context.customerId = rs.getInt("customer_id");
			this.context.cartId = rs.getInt("cart_id");

			if (!isPasswordCorrect(salt, hash, password)) {
				result.message = "Incorrect password";
				return result;
			}

			result.message = String.format("Successfully logged in as \"%s\"", username);
			result.results = Util.deepCopyResultSet(rs);
			return result;
		} catch (ClassNotFoundException | SQLException e) {
			throw new ExecutionException("Unexpected error", e);
		}
	}
}
