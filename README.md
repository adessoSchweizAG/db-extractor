[![Build Status](https://travis-ci.org/adessoSchweizAG/db-extractor.svg?branch=master)](https://travis-ci.org/adessoSchweizAG/db-extractor)
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
			<id>github-db-executor</id>
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