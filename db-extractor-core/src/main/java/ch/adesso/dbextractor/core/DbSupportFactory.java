package ch.adesso.dbextractor.core;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DbSupportFactory {

	private static final Logger LOGGER = LoggerFactory.getLogger(DbSupportFactory.class);

	private DbSupportFactory() {

	}

	public static Collection<String> getDriverClassNames() {
		return LazyHolder.REGISTRY.keySet();
	}

	public static DbSupport createInstance(DataSource dataSource) throws SQLException {

		try (Connection connection = dataSource.getConnection()) {
			String url = connection.getMetaData().getURL();
			Driver driver = DriverManager.getDriver(url);
			return createInstance(driver.getClass().getName(), dataSource);
		}
	}

	public static DbSupport createInstance(String driverClassName, DataSource dataSource) {
		Constructor<? extends DbSupport> constructor = LazyHolder.REGISTRY.get(driverClassName);
		if (constructor == null) {
			throw new IllegalArgumentException("driverClassName: '" + driverClassName + "' is not registered");
		}
		try {
			return constructor.newInstance(dataSource);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new IllegalStateException("can't create new instance of " + constructor.getDeclaringClass().getSimpleName(), e);
		}
	}

	private static class LazyHolder {

		private static final Map<String, Constructor<? extends DbSupport>> REGISTRY = initializeRegistry();

		private static Map<String, Constructor<? extends DbSupport>> initializeRegistry() {

			Map<String, Constructor<? extends DbSupport>> registry = new ConcurrentHashMap<>();
			try {
				ClassLoader classLoader = DbSupportFactory.class.getClassLoader();
				Enumeration<URL> resources = classLoader.getResources("db-extractor.properties");
				while (resources.hasMoreElements()) {
					URL url = resources.nextElement();

					Properties properties = new Properties();
					properties.load(url.openStream());

					for (Entry<Object, Object> entry : properties.entrySet()) {
						Class<?> dbSupportClass = classLoader.loadClass((String) entry.getValue());

						Constructor<? extends DbSupport> constructor = (Constructor) dbSupportClass.getConstructor(DataSource.class);
						if (!Modifier.isPublic(constructor.getModifiers())) {
							LOGGER.warn("failed to initialize DbSupportRegistry");
						} else {
							registry.put((String) entry.getKey(), constructor);
						}
					}
				}
			} catch (IOException | ClassNotFoundException | NoSuchMethodException | SecurityException e) {
				LOGGER.warn("failed to initialize DbSupportRegistry", e);
			}
			return registry;
		}
	}

}
