package com.sap.test.httpparser;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.hamcrest.core.StringContains.containsString;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.junit.Test;

public class HttpParserUnitTest {
	
	private String httpRequestAsString;
	private HttpParser httpRequest;
	
	@Test
	public void testEmptyStatusLine() throws IOException{
		httpRequestAsString = "";
		httpRequest = new HttpParser(new ByteArrayInputStream(httpRequestAsString.getBytes()));
		assertThat(httpRequest.parseRequest(), is(0));
	}
	
	@Test 
	public void testStatusLineStartingWithWhiteSpace() throws IOException{
		httpRequestAsString = " GET / HTTP/1.1\r\n";
		httpRequest = new HttpParser(new ByteArrayInputStream(httpRequestAsString.getBytes()));
		assertThat(httpRequest.parseRequest(), is(Status.BAD_REQUEST));
	}

	@Test
	public void testIncompleteStatusLine() throws IOException{
		httpRequestAsString = "GET /\r\n";
		httpRequest = new HttpParser(new ByteArrayInputStream(httpRequestAsString.getBytes()));
		assertThat(httpRequest.parseRequest(), is(Status.BAD_REQUEST));
	}
	
	@Test
	public void testInvalidHTTPProtocolString() throws IOException{
		httpRequestAsString = "GET / HTTP1.1\r\n";
		httpRequest = new HttpParser(new ByteArrayInputStream(httpRequestAsString.getBytes()));
		assertThat(httpRequest.parseRequest(), is(Status.BAD_REQUEST));
	}
	
	@Test
	public void testInvalidHTTPProtocolNumber() throws IOException{
		httpRequestAsString = "GET / HTTP/S.1\r\n";
		httpRequest = new HttpParser(new ByteArrayInputStream(httpRequestAsString.getBytes()));
		assertThat(httpRequest.parseRequest(), is(Status.BAD_REQUEST));
	}
	

	
	@Test
	public void testRequestWithoutHeaders() throws IOException{
		httpRequestAsString = "GET / HTTP/1.1\r\n";
		httpRequest = new HttpParser(new ByteArrayInputStream(httpRequestAsString.getBytes()));
		assertThat(httpRequest.parseRequest(), is(Status.BAD_REQUEST));
	}
	
	@Test
	public void testValidHostHeader() throws IOException{
		httpRequestAsString = "GET / HTTP/1.1\r\n" +
													"Host: localhost:50000\r\n";
		httpRequest = new HttpParser(new ByteArrayInputStream(httpRequestAsString.getBytes()));
		assertThat(httpRequest.parseRequest(), is(Status.OK));
	}
	
	@Test
	public void testOneRequestParameters() throws IOException{
		httpRequestAsString = "GET /?param1=value1 HTTP/1.1\r\n" +
				"Host: localhost:50000\r\n";
		httpRequest = new HttpParser(new ByteArrayInputStream(httpRequestAsString.getBytes()));
		httpRequest.parseRequest();
		assertTrue(httpRequest.getParams().size() == 1);
		assertEquals(httpRequest.getParam("param1"), "value1");
	}
	
	@Test
	public void testTwoRequestParameters() throws IOException{
		httpRequestAsString = "GET /?param1=value1&param2=value2 HTTP/1.1\r\n" +
				"Host: localhost:50000\r\n";
		httpRequest = new HttpParser(new ByteArrayInputStream(httpRequestAsString.getBytes()));
		httpRequest.parseRequest();
		assertTrue(httpRequest.getParams().size() == 2);
		assertEquals(httpRequest.getParam("param1"), "value1");
		assertEquals(httpRequest.getParam("param2"), "value2");
	}
	
	@Test
	public void testRequestParameterWithEmptyValue() throws IOException{
		httpRequestAsString = "GET /?param1= HTTP/1.1\r\n" +
				"Host: localhost:50000\r\n";
		httpRequest = new HttpParser(new ByteArrayInputStream(httpRequestAsString.getBytes()));
		httpRequest.parseRequest();
		assertTrue(httpRequest.getParams().size() == 1);
		assertEquals(httpRequest.getParam("param1"), "");
	}

	@Test
	public void testRequestHeaders() throws IOException{
		httpRequestAsString = "GET / HTTP/1.1\r\n" +
				"Host: localhost:50000\r\n" +
				"Cookie: JSESSIONID=123456";
		httpRequest = new HttpParser(new ByteArrayInputStream(httpRequestAsString.getBytes()));
		httpRequest.parseRequest();
		assertTrue(httpRequest.getHeaders().size() == 2);
		assertEquals(httpRequest.getHeader("Cookie"), "JSESSIONID=123456");
		assertEquals(httpRequest.getHeader("Host"), "localhost:50000");
	}
	
	@Test
	public void testNonExistingRequestHeader() throws IOException{
		httpRequestAsString = "GET / HTTP/1.1\r\n" +
				"Host: localhost:50000\r\n" +
				"Cookie: JSESSIONID=123456";
		httpRequest = new HttpParser(new ByteArrayInputStream(httpRequestAsString.getBytes()));
		httpRequest.parseRequest();
		assertTrue(httpRequest.getHeaders().size() == 2);
		assertEquals(httpRequest.getHeader("Cookies"), null);
	}
	
	@Test
	public void testNoRequestHeaders() throws IOException{
		httpRequestAsString = "GET / HTTP/1.0\r\n";
		httpRequest = new HttpParser(new ByteArrayInputStream(httpRequestAsString.getBytes()));
		httpRequest.parseRequest();
		assertEquals(httpRequest.getHeader("Cookies"), null);
	}
	
	
	@Test 
	public void testPostRequest() throws IOException{
		httpRequestAsString = "POST / HTTP/1.1\r\n"	+
				"Host: localhost:50000\r\n";
		httpRequest = new HttpParser(new ByteArrayInputStream(httpRequestAsString.getBytes()));
		assertThat(httpRequest.parseRequest(), is(Status.NOT_IMPLEMENTED));
	}
	
	@Test
	public void testBadMethodName() throws IOException{
		httpRequestAsString = "GUEST / HTTP/1.1\r\n";
		httpRequest = new HttpParser(new ByteArrayInputStream(httpRequestAsString.getBytes()));
		assertThat(httpRequest.parseRequest(), is(Status.BAD_REQUEST));
	}
	
	@Test
	public void testCompareVersion() throws IOException{
		httpRequestAsString = "GET / HTTP/1.1\r\n"	+
				"Host: localhost:50000\r\n";
		httpRequest = new HttpParser(new ByteArrayInputStream(httpRequestAsString.getBytes()));
		httpRequest.parseRequest();
		assertThat(httpRequest.compareVersion(0, 0), is(-1));
		assertThat(httpRequest.compareVersion(0, 1), is(-1));
		assertThat(httpRequest.compareVersion(0, 2), is(-1));
		assertThat(httpRequest.compareVersion(1, 0), is(-1));
		assertThat(httpRequest.compareVersion(2, 0), is(1));
		assertThat(httpRequest.compareVersion(2, 1), is(1));
		assertThat(httpRequest.compareVersion(1, 2), is(1));
		assertThat(httpRequest.compareVersion(1, 1), is(0));
	}
	
	@Test
	public void testDateHeader() throws IOException {
		assertThat(HttpParser.getDateHeader(), allOf(containsString("Date:"), containsString("GMT")));
	}
}