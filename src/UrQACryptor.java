import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;


public class UrQACryptor {
	
	private String HASH_UPDATE_DATA = "urqa_service_NumberONE!!!";
	private byte[] key;
	private Cipher encryptor;
	private Cipher decryptor;
	
	public UrQACryptor(){
		
		// 참고 
		// http://dukeom.wordpress.com/2013/01/03/aes256-%EC%95%94%ED%98%B8%ED%99%94%EC%8B%9C-java-security-invalidkeyexception-illegal-key-size-%ED%95%B4%EA%B2%B0-%EB%B0%A9%EC%95%88/
		//		policy 파일을 덮어써야 java 에서 aes256을 사용 할수 있습니다.
		/*
		var INITIALIZE_VECTOR = '0000000000000000';	--> It's 16byte array ( 0 set )
		var CHIPER_TYPE = 'aes-256-cbc';
		var ENCODE_TYPE = 'base64';
		 */
		String IV = "";
		
		byte[] key = SHA256( HASH_UPDATE_DATA );
		
		try{
			SecretKey secureKey = new SecretKeySpec(key, "AES");
			encryptor = Cipher.getInstance("AES/CBC/PKCS5Padding");
			encryptor.init(Cipher.ENCRYPT_MODE, secureKey, new IvParameterSpec( new byte[16] ) );
			
			decryptor = Cipher.getInstance("AES/CBC/PKCS5Padding");
			decryptor.init(Cipher.DECRYPT_MODE, secureKey, new IvParameterSpec( new byte[16] ) );
			
		}catch(Exception ex ){
			ex.printStackTrace();
		}
		
	}

	/**
	 * 
	 * @param src
	 * @return AEC-256-cbc-pkcs5padding + BASE64
	 */
	public String encrypt( byte[] src ){
		
		byte[] encrypted = null;
		try {
			encrypted = encryptor.doFinal( src );
		} catch (IllegalBlockSizeException | BadPaddingException e) {
			e.printStackTrace();
		}
		return Base64.encode( encrypted );
	}
	
	/**
	 * 
	 * @param src
	 * @return source data 
	 */
	public byte[] decrypt( String src ){
		byte[] dec = Base64.decode(src);
		
		byte[] ret = null;
		
		try {
			ret = decryptor.doFinal( dec );
		} catch (IllegalBlockSizeException | BadPaddingException e) {
			e.printStackTrace();
		}
		
		return ret;
	}
	
	private byte[] SHA256(String str) {
		try {

			MessageDigest sh = MessageDigest.getInstance("SHA-256");
			sh.update(str.getBytes());
			byte byteData[] = sh.digest();
			
			return byteData;
			
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return null;
	}
	
}

