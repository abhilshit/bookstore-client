package net.tg.webinar.demo.bookstore.client;

import java.io.IOException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.UUID;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import net.tg.webinar.demo.bookstore.util.HashingUtil;

public class BookStoreClient {

	public static void basicAuthClient() throws ClientProtocolException, IOException {

		/**
		 * use following commented code for non -preemptive BASIC auth
		 */
		// CredentialsProvider credentialsProvider = new
		// BasicCredentialsProvider();
		// AuthScope scope = new AuthScope("localhost", 8080);
		// credentialsProvider.setCredentials(scope, new
		// UsernamePasswordCredentials("user1", "password1"));

		CloseableHttpClient httpClient = HttpClientBuilder.create().build();

		HttpGet httpget = new HttpGet("http://localhost:8080/bookstore/api/books/2");
		httpget.addHeader("Authorization", "Basic " + Base64.encodeBase64String("user1:password1".getBytes()));
		CloseableHttpResponse response = httpClient.execute(httpget);
		HttpEntity entity = response.getEntity();

		System.out.println("----------------------------------------");
		System.out.println(response.getStatusLine());
		if (entity != null) {
			System.out.println(EntityUtils.toString(entity, "UTF-8"));
		}
		EntityUtils.consume(entity);

	}

	public static void customeAuthV1Client() throws ParseException, IOException {

		CloseableHttpClient httpClient = HttpClientBuilder.create().build();

		HttpPost httpPostAuth = new HttpPost("http://localhost:8080/bookstore/api/auth/v1");

		HttpGet httpget = new HttpGet("http://localhost:8080/bookstore/api/v1/books/1");

		httpPostAuth.addHeader("Authorization", "Basic " + Base64.encodeBase64String("user1:password1".getBytes()));
		CloseableHttpResponse response = httpClient.execute(httpPostAuth);
		HttpEntity entity = response.getEntity();

		System.out.println("----------------------------------------");
		System.out.println(response.getStatusLine());
		if (entity != null) {
			System.out.println(EntityUtils.toString(entity, "UTF-8"));
		}
		String access_token = response.getFirstHeader("x-access-token").getValue();
		System.out.println("access_token obtained: " + access_token);
		// use the access_token for next calls till it expires

		EntityUtils.consume(entity);

		httpget.addHeader("-x-access-token", access_token);
		response = httpClient.execute(httpget);
		entity = response.getEntity();

		System.out.println("----------------------------------------");
		System.out.println(response.getStatusLine());
		if (entity != null) {
			System.out.println(EntityUtils.toString(entity, "UTF-8"));
		}
		EntityUtils.consume(entity);

	}

	public static void customeAuthV2Client() throws ClientProtocolException, IOException {

		CloseableHttpClient httpClient = HttpClientBuilder.create().build();

		HttpPost httpPostAuth = new HttpPost("http://localhost:8080/bookstore/api/auth/v2");

		HttpGet httpget = new HttpGet("http://localhost:8080/bookstore/api/v2/books/1");

		httpPostAuth.addHeader("-x-api-key", "egEmfIwA6dyFmHoKjjjsQw==");
		httpPostAuth.addHeader("-x-api-user", "user1");

		CloseableHttpResponse response = httpClient.execute(httpPostAuth);
		HttpEntity entity = response.getEntity();

		System.out.println("----------------------------------------");
		System.out.println(response.getStatusLine());
		if (entity != null) {
			System.out.println(EntityUtils.toString(entity, "UTF-8"));
		}
		String access_token = response.getFirstHeader("x-access-token").getValue();
		System.out.println("access_token obtained: " + access_token);

		// use the access_token for next calls till it expires

		EntityUtils.consume(entity);

		httpget.addHeader("-x-access-token", access_token);
		response = httpClient.execute(httpget);
		entity = response.getEntity();

		System.out.println("----------------------------------------");
		System.out.println(response.getStatusLine());
		if (entity != null) {
			System.out.println(EntityUtils.toString(entity, "UTF-8"));
		}
		EntityUtils.consume(entity);
	}

	public static void customeAuthV3Client() throws Exception {
		CloseableHttpClient httpClient = HttpClientBuilder.create().build();

		HttpPost httpPostAuth = new HttpPost("http://localhost:8080/bookstore/api/auth/v3");
		HttpPost httpPostToken = new HttpPost("http://localhost:8080/bookstore/api/auth/v3/token");

		HttpGet httpget = new HttpGet("http://localhost:8080/bookstore/api/v3/books/1");
		String apiKey = "egEmfIwA6dyFmHoKjjjsQw==";

		httpPostAuth.addHeader("-x-api-user", "user1");

		CloseableHttpResponse response = httpClient.execute(httpPostAuth);
		HttpEntity entity = response.getEntity();

		System.out.println("----------------------------------------");
		System.out.println(response.getStatusLine());
		if (entity != null) {
			System.out.println(EntityUtils.toString(entity, "UTF-8"));
		}
		byte[] challenge = Base64.decodeBase64(response.getFirstHeader("-x-challenge").getValue());
		System.out.println("challenge obtained: " + response.getFirstHeader("-x-challenge").getValue());

		Cipher c = Cipher.getInstance("AES");
		SecretKeySpec k = new SecretKeySpec(Base64.decodeBase64(apiKey.getBytes()), "AES");

		c.init(Cipher.DECRYPT_MODE, k);
		byte[] decryptedData = c.doFinal(challenge);
		String answer = new String(decryptedData);

		httpPostToken.addHeader("-x-challenge-answer", answer);
		httpPostToken.addHeader("-x-api-user", "user1");

		response = httpClient.execute(httpPostToken);
		entity = response.getEntity();

		System.out.println("----------------------------------------");
		System.out.println(response.getStatusLine());
		if (entity != null) {
			System.out.println(EntityUtils.toString(entity, "UTF-8"));
		}
		String access_token = response.getFirstHeader("-x-access-token").getValue();
		System.out.println("access token obtained : " + access_token);

		// use the access_token for next calls till it expires

		EntityUtils.consume(entity);

		httpget.addHeader("-x-access-token", access_token);
		ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
		long currentTime = now.toInstant().toEpochMilli();
		httpget.addHeader("-x-timestamp", currentTime+"");
		httpget.addHeader("-x-nonce", UUID.randomUUID().toString());
		
		HashMap<String, String> headerMap = new HashMap<>();
		
		for(Header header:httpget.getAllHeaders()){
			headerMap.put(header.getName(),header.getValue());
		}
		
		httpget.addHeader("-x-signature", HashingUtil.generateSignature(apiKey, httpget.getURI().toURL().toString(), "", httpget.getMethod(), headerMap));
		response = httpClient.execute(httpget);
		entity = response.getEntity();

		System.out.println("----------------------------------------");
		System.out.println(response.getStatusLine());
		if (entity != null) {
			System.out.println(EntityUtils.toString(entity, "UTF-8"));
		}
		EntityUtils.consume(entity);
	}

	public static void main(String args[]) throws Exception {

		 BookStoreClient.basicAuthClient();
		 BookStoreClient.customeAuthV1Client();
		 BookStoreClient.customeAuthV2Client();

		BookStoreClient.customeAuthV3Client();
	}
}
