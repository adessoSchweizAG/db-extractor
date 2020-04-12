[![Build Status](https://app.travis-ci.com/adessoSchweizAG/db-extractor.svg?branch=master)](https://app.travis-ci.com/github/adessoSchweizAG/db-extractor)
[![sonarcloud.io: Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=ch.adesso%3Adb-extractor&metric=ncloc)](https://sonarcloud.io/dashboard?id=ch.adesso%3Adb-extractor)
[![sonarcloud.io: Coverage](https://sonarcloud.io/api/project_badges/measure?project=ch.adesso%3Adb-extractor&metric=coverage)](https://sonarcloud.io/dashboard?id=ch.adesso%3Adb-extractor)
[![sonarcloud.io: Bugs](https://sonarcloud.io/api/project_badges/measure?project=ch.adesso%3Adb-extractor&metric=bugs)](https://sonarcloud.io/dashboard?id=ch.adesso%3Adb-extractor)
[![sonarcloud.io: Code Smell](https://sonarcloud.io/api/project_badges/measure?project=ch.adesso%3Adb-extractor&metric=code_smells)](https://sonarcloud.io/dashboard?id=ch.adesso%3Adb-extractor)

# db-extractor
Selectively extraction of data from relational databases into sql scripts


## Web Based Interface (DBExtractor - UI)

### Standalone spring boot application
DbExtractor - UI can be directly started as spring boot application. All settings are stored in a file based hsqldb.
Additional JDBC drivers can be placed in the `lib` directory.

#### Startup
 1. Open Command Line Prompt
 1. Run `java -jar db-extractor-ui.jar`
 1. Navigate to http://localhost:8080 with your preferred web browser.


### Web application in tomcat
DbExtractor - UI can be deployed as web app in tomcat. The database for the settings must be provided as JNDI Datasource.

#### Installation
 1. Add JDBC driver to `$CATALINA_HOME/lib` directory
 1. Configure JNDI Resource in `context.xml`
```xml
<Context>
	<Resource name="jdbc/DbExtractorDataSource" auth="Container" type="javax.sql.DataSource"
		maxTotal="100" maxIdle="30" maxWaitMillis="10000"
		username="sa" password="sa" driverClassName="org.hsqldb.jdbc.JDBCDriver"
		url="jdbc:hsqldb:mem:DbExtractor" />
</Context>
```
 3. put db-extractor-ui.war to webapps directory
 1. start tomcat
 1. Navigate to http://localhost:8080/db-extractor-ui with your preferred web browser.

# Example

The selection condition for dbextractor in this example only selects all rows in table `item` with `InvocationID = 0`.

```
+----------+    +----------+    +----------+
| Customer |---<| Invoice  |---<| Item     |
+----------+    +----------+    +----------+
                                      |
                                     /|\
                                +----------+
                                | Product  |
                                +----------+
```

DbExtractor follow automatically the foreign key constraints defined in the database and generates a sql script containing all data required to create a consistent export. Look like this:

```sql
-- Input:
-- SELECT * FROM ITEM WHERE InvoiceID = 0

-- SELECT * FROM CUSTOMER WHERE ID IN (0) ORDER BY ID;
INSERT INTO CUSTOMER (ID, FIRSTNAME, LASTNAME, STREET, CITY) VALUES (0, 'Laura', 'Steel', '429 Seventh Av.', 'Dallas');

-- SELECT * FROM PRODUCT WHERE ID IN (7, 14, 47) ORDER BY ID;
INSERT INTO PRODUCT (ID, NAME, PRICE) VALUES (7, 'Telephone Shoe', 84);
INSERT INTO PRODUCT (ID, NAME, PRICE) VALUES (14, 'Telephone Iron', 124);
INSERT INTO PRODUCT (ID, NAME, PRICE) VALUES (47, 'Ice Tea Iron', 178);

-- SELECT * FROM INVOICE WHERE ID IN (0) ORDER BY ID;
INSERT INTO INVOICE (ID, CUSTOMERID, TOTAL) VALUES (0, 0, 3898);

-- SELECT * FROM ITEM WHERE InvoiceID = 0 ORDER BY ID;
INSERT INTO ITEM (ID, INVOICEID, ITEM, PRODUCTID, QUANTITY, COST) VALUES (0, 0, 2, 47, 3, 178);
INSERT INTO ITEM (ID, INVOICEID, ITEM, PRODUCTID, QUANTITY, COST) VALUES (1, 0, 1, 14, 19, 124);
INSERT INTO ITEM (ID, INVOICEID, ITEM, PRODUCTID, QUANTITY, COST) VALUES (2, 0, 0, 7, 12, 84);
```

# Command Line Interface

The command line interface requires an installed java runtime 1.8 or newer.

```sh
bin/dbExtractor -driver org.hsqldb.jdbc.JDBCDriver
 -url jdbc:hsqldb:hsql//localhost/example -username SA \
 -FItem="InvocationID = 0" -OItem="ID"
```

Paramter | Description
------ | -----------
`-help` | Help text of dbextractor
`-driver <jdbc.driver.ClassName>` | Database driver class name
`-url <value>` | Database JDBC URL
`-username <value>` | Database username
`-password <value>` | Database password
`-F<TableName>="<whereCondition>"` | Table and where condition to select the row to start data extraction.
`-O<TableName>="<orderByCondition>"` | Optional order by condition for the specified table. Default sort order is by primary key.

# Maven Repository
To use db-extractor-core in your own project you can include the following maven settings.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
	
	<repositories>
		<repository>
			<id>github-db-extractor</id>
			<url>https://maven.pkg.github.com/adessoSchweizAG/db-extractor</url>
		</repository>
	</repositories>
	
	<dependencies>
		<dependency>
			<groupId>ch.adesso</groupId>
			<artifactId>db-extractor-core</artifactId>
			<version>0.0.1-SNAPSHOT</version>
		</dependency>
	<dependencies>
</project>
```

GitHub enforce authentication with access token to access GitHub Packages.
 1. Create your own personal access token on GitHub [https://github.com/settings/tokens](https://github.com/settings/tokens)
 1. Extend maven `settings.xml` located under `~/.m2/settings.xml`
```xml
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
		xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
		xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 https://maven.apache.org/xsd/settings-1.0.0.xsd">

	<servers>
		<server>
			<id>github-db-extractor</id> <!-- must match id of repository in pom.xml -->
			<username><!-- GitHub Username --></username>
			<password><!-- GitHub Personal Access Token --></password>
		</server>
	</servers>

</settings>
```
