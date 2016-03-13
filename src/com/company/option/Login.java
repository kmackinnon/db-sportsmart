package com.company.option;

import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Connection;
import java.sql.PreparedStatement;

import static com.company.util.Connect.openConnection;
import static com.company.util.PasswordUtilities.isPasswordCorrect;
import com.company.util.Util;

public class Login extends Option {
	private static final String NAME = "Login";
	private static final String[] SUBOPTION_NAMES = { "username", "password" };
	private static final String QUERY = "SELECT customer_id, name, password_hash, password_salt FROM Customer WHERE name = ?;";

	public Login() {
		super(NAME, SUBOPTION_NAMES);
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
