package ch.adesso.dbextractor.core;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;
import java.util.regex.Pattern;

public class TestHelper {

	private TestHelper() {

	}

	public static int runSqlScript(Connection con, InputStream stream) throws SQLException {

		Pattern pattern = Pattern.compile("(?:;(?:\\r|\\n)+)|(?:--.*(?:\\r|\\n)+)");

		int affectedRowCount = 0;
		try (Statement stmt = con.createStatement();
				Scanner scanner = new Scanner(stream).useDelimiter(pattern)) {

			while (scanner.hasNext()) {

				String sql = scanner.next().trim();
				if (sql.length() > 0) {
					affectedRowCount += stmt.executeUpdate(sql);
				}
			}
		}
		return affectedRowCount;
	}
}
