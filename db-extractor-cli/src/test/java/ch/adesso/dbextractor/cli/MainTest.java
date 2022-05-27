package ch.adesso.dbextractor.cli;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.Scanner;
import java.util.regex.Pattern;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.dbcp2.BasicDataSourceFactory;
import org.junit.Before;
import org.junit.Test;

import ch.adesso.dbextractor.core.DbSupportHsqlDb;

public class MainTest {

	private static final String JDBC_URL = "jdbc:hsqldb:mem:memdb";
	private static final String JDBC_USERNAME = "SA";

	private ByteArrayOutputStream out = new ByteArrayOutputStream();

	@Before
	public void setupSystemOut() {
		System.setOut(new PrintStream(out));
	}

	@Test
	public void help() throws Exception {

		Main.main(new String[] { "-h" });
		
		String generateScript = out.toString();
		assertThat(generateScript, containsString("usage: dbExtractor"));
	}

	@Test
	public void script() throws Exception {

		Properties properties = new Properties();
		properties.setProperty("driverClassName", DbSupportHsqlDb.DRIVER_CLASS_NAME);
		properties.setProperty("url", JDBC_URL);
		properties.setProperty("username", JDBC_USERNAME);

		try (BasicDataSource dataSource = BasicDataSourceFactory.createDataSource(properties)) {

			try (Connection con = dataSource.getConnection()) {
				runSqlScript(con, MainTest.class.getResourceAsStream("MainTest.sql"));
			}

			Main.main(new String[] {
					"-driver", DbSupportHsqlDb.DRIVER_CLASS_NAME,
					"-url", JDBC_URL,
					"-username", JDBC_USERNAME,
					"-FItem=InvoiceID = 0",
					"-OItem=InvoiceID DESC" });

			String generateScript = out.toString();
			assertThat(generateScript, containsString("-- SELECT * FROM Item WHERE InvoiceID = 0 ORDER BY InvoiceID DESC"));

			try (Connection con = dataSource.getConnection();
					Statement stmt = con.createStatement()) {

				stmt.executeUpdate("SHUTDOWN");
			}
		}
	}

	private static void runSqlScript(Connection con, InputStream stream) throws SQLException {

		Pattern pattern = Pattern.compile("(?:;(?:\\r|\\n)+)|(?:--.*(?:\\r|\\n)+)");
		try (Statement stmt = con.createStatement();
				Scanner scanner = new Scanner(stream).useDelimiter(pattern)) {

			while (scanner.hasNext()) {

				String sql = scanner.next().trim();
				if (sql.length() > 0) {
					stmt.executeUpdate(sql);
				}
			}
		}
	}
}
