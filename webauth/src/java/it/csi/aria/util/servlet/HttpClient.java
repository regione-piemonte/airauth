/*
 *Copyright Regione Piemonte - 2022
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */
package it.csi.aria.util.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;

/**
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public class HttpClient {

	private static final int DEFAULT_CONNECT_TIMEOUT_ms = 60000;

	private static final int DEFAULT_READ_TIMEOUT_ms = 60000;

	private String host;

	private int port;

	private String servicePath;

	private String authProperty;

	private int connectTimeout_ms = DEFAULT_CONNECT_TIMEOUT_ms;

	private int readTimeout_ms = DEFAULT_READ_TIMEOUT_ms;

	public HttpClient(String host, int port, String servicePath) {
		this(host, port, servicePath, null, null);
	}

	public HttpClient(ConnectionParams connectionParams) {
		this(connectionParams.getHost(), connectionParams.getPort(),
				connectionParams.getServicePath(), connectionParams.getUser(),
				connectionParams.getPassword());
	}

	public HttpClient(String host, int port, String servicePath, String user,
			String password) {
		if (host == null)
			throw new IllegalArgumentException("Host name or IP should be "
					+ "specified");
		if (port <= 0 || port >= 65536)
			throw new IllegalArgumentException("Port number should be positive"
					+ " and less than 65536");
		if (servicePath == null)
			throw new IllegalArgumentException("Service path should be "
					+ "specified");
		if (user == null && password != null)
			throw new IllegalArgumentException("User should not be null when"
					+ " password is given");
		if (user != null && password == null)
			throw new IllegalArgumentException("Password should not be null "
					+ "when user is given");
		this.host = host;
		this.port = port;
		this.servicePath = servicePath;
		if (user != null) {
			String authString = user + ":" + password;
			byte[] authEncBytes = Base64.encodeBase64(authString.getBytes());
			authProperty = "Basic " + new String(authEncBytes);
		} else {
			authProperty = null;
		}
	}

	public int getConnectTimeout_ms() {
		return connectTimeout_ms;
	}

	public void setConnectTimeout_ms(int connectTimeoutMs) {
		connectTimeout_ms = connectTimeoutMs;
	}

	public int getReadTimeout_ms() {
		return readTimeout_ms;
	}

	public void setReadTimeout_ms(int readTimeoutMs) {
		readTimeout_ms = readTimeoutMs;
	}

	public BufferedReader execHttpGet(Map<String, String[]> requestParams)
			throws IOException, ApplicationException {
		String strUrl = "http://" + host + ":" + port + servicePath
				+ makeUrlParams(requestParams);
		URL acqSrv;
		try {
			acqSrv = new URL(strUrl);
		} catch (MalformedURLException ex) {
			throw new ApplicationException("URL non valida: " + strUrl, ex);
		}
		URLConnection srvConn = acqSrv.openConnection();
		if (authProperty != null)
			srvConn.setRequestProperty("Authorization", authProperty);
		srvConn.setDoInput(true);
		srvConn.setDoOutput(false);
		srvConn.setUseCaches(false);
		srvConn.setConnectTimeout(connectTimeout_ms);
		srvConn.setReadTimeout(readTimeout_ms);
		BufferedReader br = new BufferedReader(new InputStreamReader(srvConn
				.getInputStream(), "UTF-8"));
		return br;
	}

	public BufferedReader execHttpPost(Map<String, String[]> requestParams,
			PostDataSender postDataSender) throws IOException,
			ApplicationException {
		String strUrl = "http://" + host + ":" + port + servicePath
				+ makeUrlParams(requestParams);
		URL acqSrv;
		try {
			acqSrv = new URL(strUrl);
		} catch (MalformedURLException ex) {
			throw new ApplicationException("URL non valida: " + strUrl, ex);
		}
		URLConnection srvConn = acqSrv.openConnection();
		if (authProperty != null)
			srvConn.setRequestProperty("Authorization", authProperty);
		srvConn.setDoInput(true);
		srvConn.setDoOutput(true);
		srvConn.setUseCaches(false);
		srvConn.setConnectTimeout(connectTimeout_ms);
		srvConn.setReadTimeout(readTimeout_ms);
		PrintWriter pw = new PrintWriter(new OutputStreamWriter(srvConn
				.getOutputStream(), "UTF-8"));
		postDataSender.send(pw);
		BufferedReader br = new BufferedReader(new InputStreamReader(srvConn
				.getInputStream(), "UTF-8"));
		return br;
	}

	private String makeUrlParams(Map<String, String[]> mapParams)
			throws UnsupportedEncodingException {
		if (mapParams == null || mapParams.isEmpty())
			return "";
		Collection<String> keys = mapParams.keySet();
		StringBuilder urlParams = new StringBuilder();
		char separator = '?';
		for (String key : keys) {
			urlParams.append(separator);
			separator = '&';
			String[] values = mapParams.get(key);
			if (values == null || values.length == 0) {
				urlParams.append(URLEncoder.encode(key, "UTF-8"));
			} else {
				for (String value : values) {
					urlParams.append(URLEncoder.encode(key, "UTF-8"));
					if (value != null)
						urlParams.append('=').append(
								URLEncoder.encode(value, "UTF-8"));
				}
			}
		}
		return urlParams.toString();
	}

}
