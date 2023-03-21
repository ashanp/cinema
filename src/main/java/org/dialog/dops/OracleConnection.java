package org.dialog.dops;

import java.sql.*;

public class OracleConnection {
	private static Connection _con;

	public OracleConnection() {
		try {

			DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());

			Connection con = DriverManager
					.getConnection(
							"jdbc:oracle:thin:@(description=(address=(host=172.26.1.7)(protocol=tcp)(port=1521))(connect_data=(service_name=cam)))",
							"ccbs2", "10sthope");
			System.out.println("CCBS2 Connection ok");
			_con = con;

		} catch (SQLException e) {
			System.out.println("Connection Failed");
			System.err.println(e);
			System.err.println(e.getErrorCode());
			System.err.println(e.getSQLState());
		}
	}

	public Connection getConnection() {
		return _con;
	}
}