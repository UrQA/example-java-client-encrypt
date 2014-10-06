import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class test {
	

	public static void main(String[] args)  {
			
		UrQACryptor cryptor = new UrQACryptor();
		
		String encrypted_msg = cryptor.encrypt( "test message".getBytes() );
		String decrypted_msg = new String( cryptor.decrypt( encrypted_msg ) );
		
		
		System.out.println("Encrypt : " + encrypted_msg );
		System.out.println("Decrypt : " + decrypted_msg );
		
		System.out.println("ttt : " + new String( cryptor.decrypt("WgtOEDgHLWPao0CfW3wsNw==" ) ) ) ;

		// send exception
		try {
			test.sendPost( true );
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	// HTTP POST request
	private static void sendPost( boolean encrypt ) throws Exception {
		
		UrQACryptor cryptor = new UrQACryptor();
		
		String data = "{ \"exception\": { \"sdkversion\": \"0.95\", \"locale\": \"en-US\", \"tag\": \"ttttt\", \"rank\": 2, \"callstack\": \"\\tat test_sendexception(file:///private/source/urqa/sample/simpleweb/index.html:35:5)\\n\\tat onclick(file:///private/source/urqa/sample/simpleweb/index.html:1:1)\\n\", \"apikey\": \"10Eb2BF7\", \"datetime\": \"2014-10-06 15:48:50\", \"device\": \"Firefox 32.0\", \"country\": \"US\", \"errorname\": \"hhh test error\", \"errorclassname\": \"hhh test error\", \"linenum\": 0, \"appversion\": \"1.1\",  \"osversion\": \"32.0\", \"gpson\": 0, \"wifion\": 1, \"mobileon\": 0, \"scrwidth\": 1218, \"scrheight\": 751, \"batterylevel\": 50, \"availsdcard\": 0, \"rooted\": 0, \"appmemtotal\": 0, \"appmemfree\": 0, \"appmemmax\": 0, \"kernelversion\": \"32.0\", \"xdpi\": 1218, \"ydpi\": 751, \"scrorientation\": 0, \"sysmemlow\": 0, \"lastactivity\": \"it.is.browser\", \"eventpaths\": [] },\"console_log\" : { \"data\" : \"\" }, \"instance\": { \"id\": 1412545730820 },\"version\": \"0.95\" }";
		
		if( encrypt ){
			data = "{ \"encdata\" : \"" + cryptor.encrypt( data.getBytes() ) + "\", \"src_len\":"+ data.getBytes().length +" }";
		}
		
		//String url = "http://localhost:55555/urqa/client/send/exception";
		//String url = "http://ur-qa.com/urqa/client/send/exception";
		String url = "http://localhost:55555/test_enc";

		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
 
		//add reuqest header
		con.setRequestMethod("POST");
		con.setRequestProperty("User-Agent", "Urqa Client");
		con.setRequestProperty("Content-type", "application/json");
		con.setRequestProperty("Charset", "utf8");
		
		if( encrypt ){
			con.setRequestProperty("Urqa-Encrypt-Opt", "aes-256-cbc-pkcs5padding+base64");
		}
		
		//con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
 
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
 
		//print result
		System.out.println(response.toString());
 
	}

}
