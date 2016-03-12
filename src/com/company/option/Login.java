package com.company.option;

import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Connection;
import java.sql.PreparedStatement;

import static com.company.util.Connect.openConnection;
import static com.company.util.PasswordUtilities.isPasswordCorrect;

public class Login extends Option {
	private static final String NAME = "Login";
	private static final String[] SUBOPTION_NAMES = { "username", "password" };
	private static final String QUERY = "SELECT name, password_hash, password_salt FROM Customer WHERE name=?;";

	public Login() {
		super(NAME, SUBOPTION_NAMES);
	}

	public ResultSet execute() throws SQLException {
		String username = subOptionValues.get(SUBOPTION_NAMES[0]);
		String password = subOptionValues.get(SUBOPTION_NAMES[1]);

		try (Connection c = openConnection();
				PreparedStatement stmt = c.prepareStatement(QUERY)) {
			stmt.setString(1, username);
			ResultSet rs = stmt.executeQuery();

			String hash = rs.getString("password_hash");
			String salt = rs.getString("password_salt");

			if (!isPasswordCorrect(salt, hash, password)) {
				return null;
			}

			return rs;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}
}
