package net.tg.webinar.demo.bookstore.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Comparator;
import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;


public class HashingUtil {
	public static String hmacSha1Encode(String key, String message) throws Exception{

		SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(), "HmacSHA1");

		Mac mac = Mac.getInstance("HmacSHA1");
		mac.init(keySpec);
		byte[] rawHmac = mac.doFinal(message.getBytes());

		Base64 encoder = new Base64();
		String encodedHMac = encoder.encodeAsString(rawHmac);
		System.out.println("Signature Generated: "+ encodedHMac);

		return encodedHMac;
	}

	
	public static String generateSignature(String apikey, String url, String messageBody, String method,
			HashMap<String, String> headers) throws Exception {

		StringBuffer stringToSign = new StringBuffer();
		stringToSign.append(method);
		stringToSign.append(url);
		stringToSign.append(messageBody);
		
		SortedSet<String> headerKeys = new TreeSet<String>(new Comparator<String>() {

			@Override
			public int compare(String o1, String o2) {
				return o1.toLowerCase().compareTo(o2.toLowerCase());
			}
		});
		
		headerKeys.addAll(headers.keySet());
		
		for(String headerKey: headerKeys){
			if(!headerKey.contains("-x-signature")&&headerKey.startsWith("-x")){
				stringToSign.append(headerKey+""+headers.get(headerKey));
			}
		}
		
		return hmacSha1Encode(apikey,stringToSign.toString());
	}

}
