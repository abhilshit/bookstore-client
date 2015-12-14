# Bookstore-Client

This is an Eclipse Java project which demonstrates how to access secured web services exposed by the BookStore Web App. 
	
		https://github.com/abhilshit/bookstore 
		
To run the client, make sure the bookstore app is up and running and can be accessed on 

		http://localhost:8080/bookstore

Then execute the BookStoreClient.Java by Run As->Java Application in Eclipse

The bookstore-client demonstrates how to access the Book resource of BookStore app secured via 

	1. HTTP BASIC Authentication
	2. Custom Token Based Authentication v 1.0
	3. Custom Token Based Authentication v 2.0
	4. Custom Token Based Authentication v 3.0

For eg. the following method inside Bookstore-Client demonstrates how to access Book resource of BookStore app secured via Custom Token Based Authentication v 3.0

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
		
		httpget.addHeader("-x-signature", HashingUtil.generateSignature(apiKey, httpget.getURI().toURL().toString(), "", 		httpget.getMethod(), headerMap));
		response = httpClient.execute(httpget);
		entity = response.getEntity();

		System.out.println("----------------------------------------");
		System.out.println(response.getStatusLine());
		if (entity != null) {
			System.out.println(EntityUtils.toString(entity, "UTF-8"));
		}
		EntityUtils.consume(entity);
	}