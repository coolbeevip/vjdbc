// VJDBC - Virtual JDBC
// Written by Michael Link
// Website: http://vjdbc.sourceforge.net

package de.simplicit.vjdbc.test.junit.hsqldb;

import javax.sql.DataSource;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class HSqlDataSource implements DataSource {
    public HSqlDataSource() throws SQLException {
        try {
            Class.forName("org.hsqldb.jdbcDriver");
        } catch (ClassNotFoundException e) {
            throw new SQLException(e.getMessage());
        }
    }
    
    public int getLoginTimeout() throws SQLException {
        return 0;
    }

    public void setLoginTimeout(int seconds) throws SQLException {
    }

    public PrintWriter getLogWriter() throws SQLException {
        return null;
    }

    public void setLogWriter(PrintWriter out) throws SQLException {
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:hsqldb:.");
    }

    public Connection getConnection(String username, String password) throws SQLException {
        return DriverManager.getConnection("jdbc:hsqldb:.", username, password);
    }

	@Override
	public <T> T unwrap(Class<T> paramClass) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isWrapperFor(Class<?> paramClass) throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}
}
