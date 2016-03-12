package com.app;

import java.sql.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Connect {
	
	public static void main(String args[]) throws SQLException, ClassNotFoundException {
		Class.forName("org.postgresql.Driver");
		
		// TODO remove the creds from the repo (currently private)
		Connection db = DriverManager.getConnection(
				"jdbc:postgresql://comp421.cs.mcgill.ca/cs421",
				"cs421g42", "cs421_42_sportsmart");
		
		// Test query execution
		Statement stmt = db.createStatement();
		ResultSet rs = stmt.executeQuery("SELECT * FROM Item");
		
		while(rs.next()) {
			System.out.println(rs.getString("item_name"));
		}
		
		stmt.close();
		
		db.close();
	}

}
