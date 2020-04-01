package ch.adesso.dbextractor.cli;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.dbcp2.BasicDataSourceFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ch.adesso.dbextractor.core.DatabaseObject;
import ch.adesso.dbextractor.core.DbSupport;
import ch.adesso.dbextractor.core.DbSupportFactory;
import ch.adesso.dbextractor.core.OutputSqlScript;
import ch.adesso.dbextractor.core.ScriptData;
import ch.adesso.dbextractor.core.ScriptDataImpl;
import ch.adesso.dbextractor.core.TableDataFilter;

public class Main {

	private static final Logger LOGGER = LogManager.getLogger(Main.class);

	private static final String JDBC_DRIVER_CLASSNAME = "driverClassName";
	private static final String JDBC_URL = "url";
	private static final String JDBC_USERNAME = "username";
	private static final String JDBC_PASSWORD = "password";

	private static final String OPT_DRIVER = "driver";
	private static final String OPT_URL = "url";
	private static final String OPT_USERNAME = "username";
	private static final String OPT_PASSWORD = "password";
	private static final String OPT_TABLE_FILTER = "F";
	private static final String OPT_TABLE_ORDER = "O";

	private static final Option OPT_HELP = Option.builder("h").longOpt("help").desc("print this message").build();

	public static void main(String[] args) {
		
		Options options = new Options();
		options.addOption(OPT_HELP);
		
		options.addRequiredOption(OPT_DRIVER, null, true, "Database driver classname");
		options.addRequiredOption(OPT_URL, null, true, "Database JDBC URL");
		options.addRequiredOption(OPT_USERNAME, null, true, "Database username");
		options.addOption(OPT_PASSWORD, true, "Database password");
		
		options.addOption(Option.builder(OPT_TABLE_FILTER).numberOfArgs(2).valueSeparator().argName("table=\\\"where-condition\\\"")
				.desc("Table where condition -F<table>=\\\"<where-condition>\\\"").build());
		options.addOption(Option.builder(OPT_TABLE_ORDER).numberOfArgs(2).valueSeparator().argName("table=\\\"order-condition\\\"")
				.desc("Table order by condition -O<table>=\\\"<order-condition>\\\"").build());
		
		try {
			// parse the command line arguments
			DefaultParser parser = new DefaultParser();
			CommandLine line = parser.parse(createOptionsNonRequired(options), args);

			if (args.length == 0 || line.hasOption(OPT_HELP.getOpt())) {
				printHelp(options);
			}
			else {
				line = parser.parse(options, args);

				BasicDataSource dataSource = createDataSource(line);
				DbSupport dbSupport = DbSupportFactory.createInstance(line.getOptionValue(OPT_DRIVER), dataSource);

				ScriptData scriptData = new ScriptDataImpl(dbSupport);
				Collection<TableDataFilter> tableDataFilters = createTableDataFilters(line);
				scriptData.script(tableDataFilters, new OutputSqlScript(dbSupport, System.out));
			}
		} catch (ParseException e) {
			printHelp(options);
		} catch (Exception exp) {
			LOGGER.error("Parsing failed. Reason: {}: {}", exp.getClass().getName(), exp.getMessage(), exp);
		}
	}

	private static void printHelp(Options options) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.setOptionComparator(null);
		String cmdLineSyntax = "dbExtractor -driver org.hsqldb.jdbc.JDBCDriver -url jdbc:hsqldb:mem:memdb -username SA -FItem=\"1=0\" -OItem=\"ID DESC\"";
		formatter.printHelp(cmdLineSyntax, options);
	}

	private static Options createOptionsNonRequired(Options options) {

		Options optionsHelp = new Options();
		for (Option opt : options.getOptions()) {
			Option clone = new Option(opt.getOpt(), opt.getLongOpt(), opt.hasArg(), opt.getDescription());
			clone.setArgs(opt.getArgs());
			clone.setValueSeparator(opt.getValueSeparator());
			clone.setArgName(opt.getArgName());
			optionsHelp.addOption(clone);
		}
		return optionsHelp;
	}

	private static BasicDataSource createDataSource(CommandLine line) throws Exception {
		
		Properties properties = new Properties();
		properties.setProperty(JDBC_DRIVER_CLASSNAME, line.getOptionValue(OPT_DRIVER));
		properties.setProperty(JDBC_URL, line.getOptionValue(OPT_URL));
		properties.setProperty(JDBC_USERNAME, line.getOptionValue(OPT_USERNAME));
		properties.setProperty(JDBC_PASSWORD, line.getOptionValue(OPT_PASSWORD, ""));
		
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
