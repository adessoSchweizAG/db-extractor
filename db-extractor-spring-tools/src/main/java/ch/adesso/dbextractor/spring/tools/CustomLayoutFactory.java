package ch.adesso.dbextractor.spring.tools;

import java.io.File;

import org.springframework.boot.loader.tools.Layout;
import org.springframework.boot.loader.tools.LayoutFactory;
import org.springframework.boot.loader.tools.Layouts;
import org.springframework.boot.loader.tools.LibraryScope;

/**
 * Spring Boot Loader Custom Layout Factory. Enforce Launcher
 * PropertiesLauncher.
 * 
 * @author Daniel.Mast
 *
 */
public class CustomLayoutFactory implements LayoutFactory {

	@Override
	public Layout getLayout(File source) {

		return new WrappedLayout(Layouts.forFile(source));
	}

	/**
	 * Wrapped Layout to override {@code Layout#getLauncherClassName()}
	 */
	public static class WrappedLayout implements Layout {

		private final Layout layout;

		public WrappedLayout(Layout layout) {
			this.layout = layout;
		}

		@Override
		public String getLauncherClassName() {
			return "org.springframework.boot.loader.PropertiesLauncher";
		}

		@Override
		public String getLibraryLocation(String libraryName, LibraryScope scope) {
			return layout.getLibraryLocation(libraryName, scope);
		}

		@Override
		public String getClassesLocation() {
			return layout.getClassesLocation();
		}

		@Override
		public String getClasspathIndexFileLocation() {
			return layout.getClasspathIndexFileLocation();
		}

		@Override
		public String getLayersIndexFileLocation() {
			return layout.getLayersIndexFileLocation();
		}

		@Override
		public boolean isExecutable() {
			return layout.isExecutable();
		}

	}
}
