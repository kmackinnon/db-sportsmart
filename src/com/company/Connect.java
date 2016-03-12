package com.company;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Connect {

	static final String JDBC_DRIVER = "org.postgresql.Driver";

	public static void main(String args[]) throws ClassNotFoundException {
		Class.forName(JDBC_DRIVER);
		Creds creds = new Creds("creds.txt");

		Connection db;
		try {
			db = DriverManager.getConnection(creds.getDbUrl(), creds.getUser(), creds.getPass());
			testQuery(db);
			db.close();
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("DB connection problems");
		}
	}

	/**
	 * Just for testing purposes
	 * @param db
	 * @throws SQLException
	 */
	private static void testQuery(Connection db) throws SQLException {
		// Test query execution
		Statement stmt = db.createStatement();
		ResultSet rs = stmt.executeQuery("SELECT * FROM Item");

		while (rs.next()) {
			System.out.println(rs.getString("item_name"));
		}

		rs.close();
		stmt.close();
	}

	private static class Creds {
		private String dbUrl;
		private String user;
		private String pass;

		/**
		 * File structure: 
		 * database 
		 * url 
		 * user pass
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
