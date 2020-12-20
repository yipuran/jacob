package org.jacob;

import javax.servlet.http.HttpServletRequest;
/**
 * JSONレスポンス応答インターフェース. JsonResponder
 * <br/><pre>JSONデータをHTTPレスポンスの応答として返すメソッドを持つインターフェースです。
 * Webコンテナ起動時、JacobApplication より実装インスタンスが {@link JacobApplication#init()} で
 * 生成返却されなければならない。
 * {@link JacobApplication#init()} が、本インターフェースのインスタンス生成する。
 * Google guice インジェクトで生成するしくみになっている。
 * インスタンス生成のタイミングは、JacobApplicationサブクラスで、
 * {@link RequestTranslater} を {@link JacobApplication#getRequestTranslater()} で取得した後で、{@link RequestTranslater#add(String, Class)} 実行
 * した時である。
 * Google guice の Module の指定は、{@link RequestTranslater#add(String, Class)} 実行する前に
 * {@link RequestTranslater#setModules(com.google.inject.Module...)} を実行しなければならない。
 * インジェクトの予約として、フィールドインジェクションで ServletContext が約束されている。
 * これは、JsonResponder 実装クラスで、
 *       &#064;Inject private ServletContext context;
 * を宣言すると自動的にこの context は ServletContext が格納される。
 * HTTP status は、getStatus() または、getStatus(HttpServletRequest request) をオーバライドすることで
 * 200 以外のコードを返すようにすることが可能
 * </pre>
 */
public interface JsonResponder{

	/**
	 * HttpServletRequest→JSON.
	 * <pre>HTTP Servletリクエストを引数で受け取り、JSON文字列データを返却する。
	 * null を返却すると HTTPレスポンスを返さない。
	 * </pre>
	 * @param request javax.servlet.http.HttpServletRequest
	 * @return JSON文字列データ、null を返却すると HTTPレスポンスを返さない。
	 */
	public String answer(HttpServletRequest request);

	/**
	 * HTTP status code を返す.
	 * @return デフォルトで 200 を返す。
	 */
	public default int getStatus(){
		return 200;
	}

	/**
	 * Override 可能 HTTP status code を返すメソッド.
	 * @return デフォルトは、getStatus() の結果を返す。。
	 */
	public default int getStatus(HttpServletRequest request){
		return getStatus();
	}
}
