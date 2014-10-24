import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyPair;
import java.security.KeyPairGenerator;

import javax.crypto.Cipher;

public class test {
	
	/*
	 * 암호화 스탭 정리
	 * 
	 * 1) /urqa/client/get_key 를 이용하여 암호화 키 받아온다.
	 * 		req : { public: 'RSA public key' }
	 * 		res : { result: 'success or fail', 
	 * 				enc_data: 'RSA public key로 암호화된 암호화 정보' }
	 * 
	 * 		enc_data 는 RSA로 암호화된 데이터 private key 로 복호화 하면 아래와 같은 데이터 형식을 가진다.
	 * 		enc_data : { token:'토큰 ID, 암호화 데이터를 던질때 같이 암호화 되지 않은상태로 전달필요',
	 * 					 basekey:'암호화용 비밀키를 생성하기 위한 해시될 값' }
	 * 
	 * 2) 실제 데이터 전송은 아래 둘중에 하나 선택 해서 하면 된다. ( 추후 성능 테스트 하고 나서, 취사 선택 될 예정임 )
	 * 
	 * 2.1) url 방식
	 * 
	 * 		/urqa/client/req_enc 에 아래의 형식으로 요청 하기 
	 * 		req : { target_uri: '대상 uri ',
	 * 				token: 'get_key에서 받아온 token 값',
	 * 				enc_data: '암호화된 데이터',
	 * 				src_len: '암호화전 데이터의 길이' }
	 * 		res : 원래 요청하려던 대상 uri 에서의 리턴과 동일
	 * 
	 * 
	 * 2.2) header 방식
	 * 		: 원래 전송 하려는 uri 그대로 사용 하고, header에 다음 정보를 추가 한다. 그리고 원래 보내려는 body 값을 아래의 body 값으로 변경 하여 보낸다.
	 * 		req header
	 * 			"Urqa-Encrypt-Opt" : "aes-256-cbc-pkcs5padding+base64" 
	 * 
	 * 		req : { token: 'get_key에서 받아온 token 값',
	 * 				enc_data: '암호화된 데이터',
	 * 				src_len: '암호화전 데이터의 길이'
	 * 			}
	 * 
	 */
	

	public static void main(String[] args) throws Exception  {
		
		// default test
		testEncrypt();
		
		// Test Step - getToken
		System.out.println("--------------------------------------------");
		System.out.println("Get Token");
		String[] token_basekey = test.getToken();
		
		// test Step - non encrypt
		System.out.println("--------------------------------------------");
		System.out.println("None Encrypt Test");
		test.sendSampleEncryptRequest( "AAAAAAAAAA", token_basekey[0], token_basekey[1], false, "" );
		
		// Test Step - mode url
		System.out.println("--------------------------------------------");
		System.out.println("url Type Encrypt Test");
		test.sendSampleEncryptRequest( "AAAAAAAAAA", token_basekey[0], token_basekey[1], true, "url" );
		
		// Test Step - mode header
		System.out.println("--------------------------------------------");
		System.out.println("Header Type Encrypt Test");
		test.sendSampleEncryptRequest( "AAAAAAAAAA", token_basekey[0], token_basekey[1], true, "header" );
		
	}
	
	/**
	 * 토큰을 가져오는 절차
	 * @return
	 * @throws Exception
	 */
	private static String[] getToken() throws Exception {
		
		String public_key = "";
		
		// Generate RSA key pairs
		KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(1024);
        KeyPair keypair = keyGen.genKeyPair();
        byte[] publicKey = keypair.getPublic().getEncoded();
        
        public_key = "-----BEGIN PUBLIC KEY-----\\n" + 
        			 Base64.encode( publicKey ) + 
        			 "\\n-----END PUBLIC KEY-----\\n";
        
		// request Key
		String url = "http://localhost:55555/urqa/client/get_key";
		String data = "{\"public\":\"" + public_key + "\"}";
		String[] results = new String[2];
		
		String response = sendPost( url, data, false, "" );
		
		// token parse
		String enc_data = JsonUtil.getJsonToken( response, "enc_data" );
		System.out.println("enc_data " + enc_data );
		
		// rsa decode
		
	    try {
	        Cipher rsa;
	        rsa = Cipher.getInstance("RSA");
	        rsa.init(Cipher.DECRYPT_MODE, keypair.getPrivate() );
	        byte[] utf8 = rsa.doFinal( Base64.decode( enc_data ) );
	        String enc_data2 = new String(utf8, "UTF8");
	        
	        results[0] = JsonUtil.getJsonToken( enc_data2, "token");
	        results[1] = JsonUtil.getJsonToken( enc_data2, "basekey" );
	        
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    
	    System.out.println("token " + results[0] );
	    System.out.println("basekey " + results[1] );
		
		return results;
	}
	
	/**
	 * 
	 * @param apikey
	 * @param token
	 * @param basekey
	 * @param encrypt
	 * @param mode		url = URL 타입으로 암호요청 하기, header = Header 타입으로 암호화 요청 하기
	 * @throws Exception
	 */
	private static void sendSampleEncryptRequest( String apikey, String token, String basekey, boolean encrypt, String mode ) throws Exception {
		
		UrQACryptor cryptor = new UrQACryptor(basekey);
		
		String data = "{ \"exception\": { \"sdkversion\": \"0.95\", \"locale\": \"en-US\", \"tag\": \"ttttt\", \"rank\": 2, \"callstack\": \"\\tat test_sendexception(file:///private/source/urqa/sample/simpleweb/index.html:35:5)\\n\\tat onclick(file:///private/source/urqa/sample/simpleweb/index.html:1:1)\\n\", \"apikey\": \"10Eb2BF7\", \"datetime\": \"2014-10-06 15:48:50\", \"device\": \"Firefox 32.0\", \"country\": \"US\", \"errorname\": \"hhh test error\", \"errorclassname\": \"hhh test error\", \"linenum\": 0, \"appversion\": \"1.1\",  \"osversion\": \"32.0\", \"gpson\": 0, \"wifion\": 1, \"mobileon\": 0, \"scrwidth\": 1218, \"scrheight\": 751, \"batterylevel\": 50, \"availsdcard\": 0, \"rooted\": 0, \"appmemtotal\": 0, \"appmemfree\": 0, \"appmemmax\": 0, \"kernelversion\": \"32.0\", \"xdpi\": 1218, \"ydpi\": 751, \"scrorientation\": 0, \"sysmemlow\": 0, \"lastactivity\": \"it.is.browser\", \"eventpaths\": [] },\"console_log\" : { \"data\" : \"\" }, \"instance\": { \"id\": 1412545730820 },\"version\": \"0.95\" }";

		//String baseurl = "http://localhost:55555";
		//String baseurl = "http://ur-qa.com";
		String baseurl = "http://localhost:55555";
		String uri = "/test_enc";
		
		if( encrypt ){
			if( "url".equals( mode ) ){
				// url type
				data = "{  \"target_uri\":\"" + uri + "\", \"token\":\"" + token + "\", \"enc_data\" : \"" + cryptor.encrypt( data.getBytes() ) + "\", \"src_len\":"+ data.getBytes().length +" }";
				uri = "/urqa/client/req_enc";
			}else{
				// header type
				data = "{  \"token\":\"" + token + "\", \"enc_data\" : \"" + cryptor.encrypt( data.getBytes() ) + "\", \"src_len\":"+ data.getBytes().length +" }";
			}
		}
				
		System.out.println( "result "  + sendPost( baseurl + uri , data, encrypt, mode )  );

	}
	
	
	// post request
	private static String sendPost( String url, String data, boolean isEncrypt, String mode ) throws Exception {
	
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
 
		//add reuqest header
		con.setRequestMethod("POST");
		con.setRequestProperty("User-Agent", "Urqa Client");
		con.setRequestProperty("Content-type", "application/json");
		con.setRequestProperty("Charset", "utf8");
		
		// add Version info ( 암호화에는 영향 없지만, 이번 부터 버전 해더를 넣는 것을 기본 정책으로 가져갈 예정 )
		con.setRequestProperty("version", "1.0.0");
		
		//  header 타입 암호화 요청일 경우에 아래의 형식으로 요청 한다.
		if( isEncrypt && "header".equals( mode ) ){
			// header 모드 일 때만 보낸다.
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
	
	
	/**
	 * 메시지 암복호화 테스트용 함수
	 */
	private static void testEncrypt(){
		UrQACryptor cryptor = new UrQACryptor(null);
		
		String encrypted_msg = cryptor.encrypt( "test message".getBytes() );
		String decrypted_msg = new String( cryptor.decrypt( encrypted_msg ) );
		
		System.out.println("Encrypt : " + encrypted_msg );
		System.out.println("Decrypt : " + decrypted_msg );
	}

}
