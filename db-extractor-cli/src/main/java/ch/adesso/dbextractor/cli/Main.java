package ch.adesso.dbextractor.cli;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.dbcp2.BasicDataSourceFactory;

import ch.adesso.dbextractor.core.DatabaseObject;
import ch.adesso.dbextractor.core.DbSupport;
import ch.adesso.dbextractor.core.DbSupportFactory;
import ch.adesso.dbextractor.core.OutputSqlScript;
import ch.adesso.dbextractor.core.ScriptData;
import ch.adesso.dbextractor.core.ScriptDataImpl;
import ch.adesso.dbextractor.core.TableDataFilter;

public class Main {

	private static final String OPT_DRIVER = "driver";
	private static final String OPT_URL = "url";
	private static final String OPT_USERNAME = "username";
	private static final String OPT_PASSWORD = "password";
	private static final String OPT_TABLE_FILTER = "F";
	private static final String OPT_TABLE_ORDER = "O";

	private static final Option OPT_HELP = Option.builder("h").longOpt("help").desc("print this message").build();

	public static void main(String[] args) {

		// create the parser
		CommandLineParser parser = new DefaultParser();
		try {
			Options options = new Options();
			options.addOption(OPT_HELP);
			
			options.addOption(OPT_DRIVER, true, "Database driver classname");
			options.addOption(OPT_URL, true, "Database JDBC URL");
			options.addOption(OPT_USERNAME, true, "Database username");
			options.addOption(OPT_PASSWORD, true, "Database password");
			
			options.addOption(Option.builder(OPT_TABLE_FILTER).numberOfArgs(2).valueSeparator().argName("table=\\\"where-condition\\\"")
					.desc("Table where condition -F<table>=\\\"<where-condition>\\\"").build());
			options.addOption(Option.builder(OPT_TABLE_ORDER).numberOfArgs(2).valueSeparator().argName("table=\\\"order-condition\\\"")
					.desc("Table order by condition -O<table>=\\\"<order-condition>\\\"").build());
			
			// parse the command line arguments
			CommandLine line = parser.parse(options, args);

			if (args.length == 0 || line.hasOption(OPT_HELP.getOpt())) {
				HelpFormatter formatter = new HelpFormatter();
				String cmdLineSyntax = "dbExtractor -driver org.hsqldb.jdbcDriver -url jdbc:hsqldb:mem:memdb -username SA -Fitem=\"1=0\" -Oitem=\"ID DESC\"";
				formatter.printHelp(cmdLineSyntax, options);
			}
			else {
				BasicDataSource dataSource = createDataSource(line);
				DbSupport dbSupport = DbSupportFactory.createInstance(line.getOptionValue(OPT_DRIVER), dataSource);

				ScriptData scriptData = new ScriptDataImpl(dbSupport);
				Collection<TableDataFilter> tableDataFilters = createTableDataFilters(line);
				scriptData.script(tableDataFilters, new OutputSqlScript(dbSupport, System.out));
			}

		} catch (Exception exp) {
			// oops, something went wrong
			System.err.println("Parsing failed.  Reason: " + exp.getClass().getName() +": " + exp.getMessage());
		}
	}

	private static BasicDataSource createDataSource(CommandLine line) throws Exception {
		
		Properties properties = new Properties();
		properties.setProperty("driverClassName", line.getOptionValue(OPT_DRIVER));
		properties.setProperty("url", line.getOptionValue(OPT_URL));
		properties.setProperty("username", line.getOptionValue(OPT_USERNAME));
		properties.setProperty("password", line.getOptionValue(OPT_PASSWORD, ""));
		
		return BasicDataSourceFactory.createDataSource(properties);
	}

	private static Collection<TableDataFilter> createTableDataFilters(CommandLine line) {
		Map<DatabaseObject, TableDataFilter> mapTableFilter = new HashMap<>();

		for (Entry<Object, Object> entry : line.getOptionProperties(OPT_TABLE_FILTER).entrySet()) {
			DatabaseObject key = new DatabaseObject(entry.getKey().toString());
			mapTableFilter.computeIfAbsent(key, TableDataFilter::new)
					.addWhereSql(entry.getValue().toString());
		}
		
		for (Entry<Object, Object> entry : line.getOptionProperties(OPT_TABLE_ORDER).entrySet()) {
			DatabaseObject key = new DatabaseObject(entry.getKey().toString());
			mapTableFilter.computeIfAbsent(key, TableDataFilter::new)
					.setOrderBy(entry.getValue().toString());
		}
		return mapTableFilter.values();
	}
}
