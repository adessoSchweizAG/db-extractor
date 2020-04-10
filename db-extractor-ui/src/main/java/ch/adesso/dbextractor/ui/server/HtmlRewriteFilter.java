package ch.adesso.dbextractor.ui.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

@WebFilter({ "/", "/index.html" })
public class HtmlRewriteFilter extends HttpFilter {

	private static final long serialVersionUID = 963513117581835703L;

	@Override
	protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {

		String contextPath = request.getServletContext().getContextPath();
		if (contextPath.length() == 0) {
			chain.doFilter(request, response);
		} else {

			ContentRewriteResponseWrapper wrapper = new ContentRewriteResponseWrapper(response);
			chain.doFilter(request, wrapper);
			StringBuffer content = wrapper.getBuffer();

			if (response.getStatus() == HttpServletResponse.SC_OK
					&& wrapper.getContentType().contains("text/html")) {

				replaceFirst(content, "<script>baseUrl=new URL(\"/\"", "<script>baseUrl=new URL(\"" + contextPath + "/\"");
				replaceAll(content, " href=\"/static/css/", " href=\"" + contextPath + "/static/css/");
				replaceAll(content, " src=\"/static/js/", " src=\"" + contextPath + "/static/js/");

				response.setContentLength(content.length());
			}

			if (content.length() > 0) {
				response.getWriter().write(content.toString());
			}
		}
	}

	private void replaceFirst(StringBuffer buffer, String from, String to) {
		int index = buffer.indexOf(from);
		if (index != -1) {
			buffer.replace(index, index + from.length(), to);
		}
	}

	private void replaceAll(StringBuffer buffer, String from, String to) {
		int index = buffer.indexOf(from);
		while (index != -1) {
			buffer.replace(index, index + from.length(), to);
			index += to.length(); // Move to the end of the replacement
			index = buffer.indexOf(from, index);
		}
	}

	private static class ContentRewriteResponseWrapper extends HttpServletResponseWrapper {

		private final StringWriter writer = new StringWriter();

		public ContentRewriteResponseWrapper(HttpServletResponse response) {
			super(response);
		}

		@Override
		public PrintWriter getWriter() {
			return new PrintWriter(writer);
		}

		@Override
		public ServletOutputStream getOutputStream() throws IOException {
			return new StringWriterServletOutputStream(writer);
		}

		public StringBuffer getBuffer() {
			return writer.getBuffer();
		}
	}

	private static class StringWriterServletOutputStream extends ServletOutputStream {

		private final StringWriter writer;

		public StringWriterServletOutputStream(StringWriter writer) {
			this.writer = writer;
		}

		@Override
		public void write(int b) throws IOException {
			writer.write(b);
		}

		@Override
		public void setWriteListener(WriteListener listener) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean isReady() {
			return true;
		}
	}
}
