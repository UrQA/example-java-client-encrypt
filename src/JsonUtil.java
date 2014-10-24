
public class JsonUtil {
	/**
	 * Json 구조에서 특정 토큰의 값을 빼오기 ( 중복이나 배열은 못빼옵니다. )
	 * @param json
	 * @param key
	 * @return
	 */
	public static String getJsonToken( String json, String key ){
		String wrapped_key = "\""+ key + "\"";
		int start_idx = json.indexOf(wrapped_key) + wrapped_key.length();
		start_idx = json.indexOf('"', start_idx) + 1;
		int end_idx = json.indexOf('"', start_idx );
		return json.substring( start_idx, end_idx );
	}
}
