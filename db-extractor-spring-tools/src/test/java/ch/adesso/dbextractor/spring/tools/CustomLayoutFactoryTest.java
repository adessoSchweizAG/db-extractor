package ch.adesso.dbextractor.spring.tools;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.junit.Test;
import org.springframework.boot.loader.tools.Layout;
import org.springframework.boot.loader.tools.LayoutFactory;
import org.springframework.boot.loader.tools.LibraryScope;

public class CustomLayoutFactoryTest {

	private LayoutFactory layoutFactory = new CustomLayoutFactory();

	@Test
	public void testWar() {
		File source = new File("test.war");
		Layout layout = layoutFactory.getLayout(source);

		assertEquals("org.springframework.boot.loader.PropertiesLauncher", layout.getLauncherClassName());
		assertEquals("WEB-INF/classes/", layout.getClassesLocation());
		assertEquals("WEB-INF/lib/", layout.getLibraryDestination(null, LibraryScope.COMPILE));
		assertEquals("WEB-INF/lib-provided/", layout.getLibraryDestination(null, LibraryScope.PROVIDED));
		assertEquals("WEB-INF/lib/", layout.getLibraryDestination(null, LibraryScope.RUNTIME));
	}

	@Test
	public void testJar() {
		File source = new File("test.jar");
		Layout layout = layoutFactory.getLayout(source);

		assertEquals("org.springframework.boot.loader.PropertiesLauncher", layout.getLauncherClassName());
		assertEquals("", layout.getClassesLocation());
		assertEquals("BOOT-INF/lib/", layout.getLibraryDestination(null, null));
	}

}
