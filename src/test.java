import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class test {
	
	/*
	 * 암호화 스탭 정리
	 * 
	 * 1) /urqa/client/getenctoken  를 이용하여 암호화 키 받아온다.
	 * 		: apikey를 보내야 함
	 * 		: { apikey: 'KEY' }
	 * 
	 * 2) enc_test url을 이용하여 암호화가 잘 되는지 확인 한다.
	 * 		: 해더 정보 및 패키지 정보는 모르겟음 ~~!
	 * 		: 바뀐 부분은 여기에 기록 하면서 진행 하자 ~!
	 * 
	 * 변경된 부분
	 * 
	 *  - routes/index.js 
	 *  	: /urqa/client/getenctoken  url 추가
	 * 	- controller/url_control.js
	 * 		: url_getenctoken() 함수 추
	 *  - util/enckey_manager.js 추가
	 *  	: getEncToken( apikey, isCreate ) 추가 ==> 현종님이 해주실 부분 ~!
	 *  
	 *  - middleware/encrypt.js
	 *  	: 호출 형식에 apikey 추가 -> 이것도 좀 문제가 있내... apikey가 있으므로... 
	 *  	  apikey를 보내면 key가 고정 되니까 ... 흠... 이걸 어쩐다? 주기적으로 갱신 하는건 어떻게 해야 할까?
	 *  	: 일단 만들어 놓고 고민 하자 ~!!
	 *  
	 *  	: encrypt token 발급이 실피하면 406 코드와 함께 에러 메시지를 보낸다. 
	 *  
	 */
	

	public static void main(String[] args)  {
			
		UrQACryptor cryptor = new UrQACryptor(null);
		
		String encrypted_msg = cryptor.encrypt( "test message".getBytes() );
		String decrypted_msg = new String( cryptor.decrypt( encrypted_msg ) );
		
		System.out.println("Encrypt : " + encrypted_msg );
		System.out.println("Decrypt : " + decrypted_msg );

		// send exception
		try {
			String token = test.getToken("AAAAAAAAAA");
			test.sendException( "AAAAAAAAAA", token, true );
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	// Connection and get Token
	private static String getToken(String apikey) throws Exception {
		
		String url = "http://localhost:55555/urqa/client/getenctoken";
		String data = "{\"apikey\":\"" + apikey + "\"}";
		String token = "";
		
		String response = sendPost( url, data, false );
		
		System.out.println( "result "  + response );
		
		// token parse
		int start_idx = response.indexOf("\"enctoken\"") + "\"enctoken\"".length();
		start_idx = response.indexOf('"', start_idx) + 1;
		int end_idx = response.indexOf('"', start_idx );
		token = response.substring( start_idx, end_idx );
		
		return token;
	}
	
	// exceptin request
	private static void sendException( String apikey, String token, boolean encrypt ) throws Exception {
		
		UrQACryptor cryptor = new UrQACryptor(token);
		
		String data = "{ \"exception\": { \"sdkversion\": \"0.95\", \"locale\": \"en-US\", \"tag\": \"ttttt\", \"rank\": 2, \"callstack\": \"\\tat test_sendexception(file:///private/source/urqa/sample/simpleweb/index.html:35:5)\\n\\tat onclick(file:///private/source/urqa/sample/simpleweb/index.html:1:1)\\n\", \"apikey\": \"10Eb2BF7\", \"datetime\": \"2014-10-06 15:48:50\", \"device\": \"Firefox 32.0\", \"country\": \"US\", \"errorname\": \"hhh test error\", \"errorclassname\": \"hhh test error\", \"linenum\": 0, \"appversion\": \"1.1\",  \"osversion\": \"32.0\", \"gpson\": 0, \"wifion\": 1, \"mobileon\": 0, \"scrwidth\": 1218, \"scrheight\": 751, \"batterylevel\": 50, \"availsdcard\": 0, \"rooted\": 0, \"appmemtotal\": 0, \"appmemfree\": 0, \"appmemmax\": 0, \"kernelversion\": \"32.0\", \"xdpi\": 1218, \"ydpi\": 751, \"scrorientation\": 0, \"sysmemlow\": 0, \"lastactivity\": \"it.is.browser\", \"eventpaths\": [] },\"console_log\" : { \"data\" : \"\" }, \"instance\": { \"id\": 1412545730820 },\"version\": \"0.95\" }";
		
		if( encrypt ){
			data = "{ \"apikey\":\"" + apikey + "\", \"encdata\" : \"" + cryptor.encrypt( data.getBytes() ) + "\", \"src_len\":"+ data.getBytes().length +" }";
		}
		
		//String url = "http://localhost:55555/urqa/client/send/exception";
		//String url = "http://ur-qa.com/urqa/client/send/exception";
		String url = "http://localhost:55555/test_enc";
		
		System.out.println( "result "  + sendPost( url, data, encrypt )  );

	}
	
	
	// post request
	private static String sendPost( String url, String data, boolean isEncrypt ) throws Exception {
	
		
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
 
		//add reuqest header
		con.setRequestMethod("POST");
		con.setRequestProperty("User-Agent", "Urqa Client");
		con.setRequestProperty("Content-type", "application/json");
		con.setRequestProperty("Charset", "utf8");
		
		if( isEncrypt ){
			con.setRequestProperty("Urqa-Encrypt-Opt", "aes-256-cbc-pkcs5padding+base64");
		}
		
		// Send post request
		con.setDoOutput(true);
		DataOutputStream wr = new DataOutputStream(con.getOutputStream());
		wr.writeBytes( data );
		wr.flush();
		wr.close();
		
		int responseCode = con.getResponseCode();
		System.out.println("\nSending 'POST' request to URL : " + url);
		System.out.println("Response Code : " + responseCode);
 
		BufferedReader in = new BufferedReader(
		        new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();
 
		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
		
		return response.toString();
	}

}
