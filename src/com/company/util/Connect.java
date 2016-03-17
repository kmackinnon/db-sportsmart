package com.company.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class Connect {
	static final String JDBC_DRIVER = "org.postgresql.Driver";

	/**
	 * Use try with resources in order to ensure connection gets closed each time
	 * 
	 * @return
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public static Connection openConnection() throws ClassNotFoundException, SQLException {
		Class.forName(JDBC_DRIVER);
		Creds creds = new Creds("creds.txt");
		return DriverManager.getConnection(creds.getDbUrl(), creds.getUser(), creds.getPass());
	}

	/**
	 * Creates a Statement with the appropriate flags for ResultSet traversal.
	 *
	 * @param conn the Connection from which the Statement will be created
	 * @returns the Statement
	 * @throws SQLException for all sorts of reasons, I'm sure
	 */
	public static Statement createStatementFromConnection(Connection conn) throws SQLException {
		return conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
	}

	/**
	 * Creates a PreparedStatement with the appropriate flags for ResultSet traversal.
	 *
	 * @param conn the Connection from which the PreparedStatement will be created
	 * @param query the query template to build this PreparedStatement from
	 * @returns the PreparedStatement
	 * @throws SQLException for all sorts of reasons, I'm sure
	 */
	public static PreparedStatement prepareStatementFromConnection(Connection conn, String query) throws SQLException {
		return conn.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
	}

	private static class Creds {
		private String dbUrl;
		private String user;
		private String pass;

		/**
		 * File structure: database url user pass
		 * 
		 * @param fileName
		 */
		public Creds(String fileName) {
			Path filePath = Paths.get(fileName);
			try {
				setDbUrl(Files.readAllLines(filePath).get(0));
				setUser(Files.readAllLines(filePath).get(1));
				setPass(Files.readAllLines(filePath).get(2));
			} catch (IOException e) {
				System.out.println("Check credentials file");
			}
		}

		public String getUser() {
			return user;
		}

		public void setUser(String user) {
			this.user = user;
		}

		public String getPass() {
			return pass;
		}

		public void setPass(String pass) {
			this.pass = pass;
		}

		public String getDbUrl() {
			return dbUrl;
		}

		public void setDbUrl(String dbUrl) {
			this.dbUrl = dbUrl;
		}

	}

}
