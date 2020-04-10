package ch.adesso.dbextractor.ui.server;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class HtmlRewriteFilterTest {

	private Filter filer = new HtmlRewriteFilter();

	@Mock
	private HttpServletRequest request;

	@Mock
	private ServletContext servletContext;

	@Mock
	private HttpServletResponse response;

	@Mock
	private PrintWriter writer;

	@Mock
	private ServletOutputStream servletOutputStream;

	@Before
	public void setUpMock() throws IOException {

		doReturn("/db-extractor-ui").when(servletContext).getContextPath();

		doReturn(servletContext).when(request).getServletContext();

		doReturn(HttpServletResponse.SC_OK).when(response).getStatus();
		doReturn("text/html").when(response).getContentType();

		doReturn(writer).when(response).getWriter();
		doReturn(servletOutputStream).when(response).getOutputStream();
	}

	@Test
	public void testRoot() throws IOException, ServletException {

		doReturn("").when(servletContext).getContextPath();

		filer.doFilter(request, response, chain("<script>baseUrl=new URL(\"/\",window.localtion).toString()</script>"));

		verify(servletOutputStream).print("<script>baseUrl=new URL(\"/\",window.localtion).toString()</script>");
	}

	@Test
	public void testBaseUrl() throws IOException, ServletException {

		filer.doFilter(request, response, chain("<script>baseUrl=new URL(\"/\",window.localtion).toString()</script>"));

		verify(writer).write("<script>baseUrl=new URL(\"/db-extractor-ui/\",window.localtion).toString()</script>");
	}

	@Test
	public void testBaseCss() throws IOException, ServletException {

		filer.doFilter(request, response, chain("<link href=\"/static/css/main.f2577515.chunk.css\" rel=\"stylesheet\">"));

		verify(writer).write("<link href=\"/db-extractor-ui/static/css/main.f2577515.chunk.css\" rel=\"stylesheet\">");
	}

	@Test
	public void testBaseJavaScript() throws IOException, ServletException {

		filer.doFilter(request, response, chain("<script src=\"/static/js/main.f8824103.chunk.js\"></script>"));

		verify(writer).write("<script src=\"/db-extractor-ui/static/js/main.f8824103.chunk.js\"></script>");
	}

	@Test
	public void testNotModified() throws IOException, ServletException {

		doReturn(HttpServletResponse.SC_NOT_MODIFIED).when(response).getStatus();

		filer.doFilter(request, response, mock(FilterChain.class));

		verify(writer, never()).write(anyString());
	}

	private FilterChain chain(String content) {
		return (request, response) -> {
			response.getOutputStream().print(content);
		};
	}
}
