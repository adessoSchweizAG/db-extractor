[![Build Status](https://travis-ci.com/adessoSchweizAG/db-extractor.svg?branch=master)](https://travis-ci.com/adessoSchweizAG/db-extractor)
[![sonarcloud.io: Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=ch.adesso%3Adb-extractor&metric=ncloc)](https://sonarcloud.io/dashboard?id=ch.adesso%3Adb-extractor)
[![sonarcloud.io: Coverage](https://sonarcloud.io/api/project_badges/measure?project=ch.adesso%3Adb-extractor&metric=coverage)](https://sonarcloud.io/dashboard?id=ch.adesso%3Adb-extractor)
[![sonarcloud.io: Bugs](https://sonarcloud.io/api/project_badges/measure?project=ch.adesso%3Adb-extractor&metric=bugs)](https://sonarcloud.io/dashboard?id=ch.adesso%3Adb-extractor)
[![sonarcloud.io: Code Smell](https://sonarcloud.io/api/project_badges/measure?project=ch.adesso%3Adb-extractor&metric=code_smells)](https://sonarcloud.io/dashboard?id=ch.adesso%3Adb-extractor)

# db-extractor
Selectivly extraction of data from relational databases into sql scripts

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
