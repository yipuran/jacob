package org.jacob;

import javax.servlet.ServletContext;
/**
 * JacobApplication アプリケーション.
 * <pre>Webコンテナ起動時初期化、JSON応答の準備を実行する。
 * 初期化内容、JSON応答実装はサブクラスで定義する。
 * JSON応答は、サブクラスが init() で返す JsonResponder インタフェースで約束する。
 * 本クラスを継承して Webコンテナ起動時、Filter の初期化パラメータとして生成されるように
 * web.xml に記述する。
 * → <a href="package-summary.html">パッケージの説明</a>
 * </pre>
 */
public abstract class JacobApplication{
	private ServletContext servletContext;
	/**
	 * 初期化処理.
	 * @return JsonResponder JSON応答インターフェース
	 */
	public abstract JsonResponder init();

	/** default constructor. */
	public JacobApplication(){
	}

	/**
	 * ServletContext の設定.
	 * @param context ServletContext
	 */
	protected final void setServletContext(ServletContext context){
		servletContext = context;
		requestTranslater = new RequestTranslater(servletContext);
	}

	private static RequestTranslater requestTranslater;
	/**
	 * HTTPリクエストURI振り分けマッピング設定インスタンスの取得.
	 * @return RequestTranslaterインスタンス
	 */
	public final RequestTranslater getRequestTranslater(){
		if (requestTranslater==null){ requestTranslater = new RequestTranslater(servletContext); }
		return requestTranslater;
	}
	/**
	 * ServletContextの取得.
	 * <pre>
	 * JacobApplication 継承クラス以外で ServletContext を取得する場合、Google　guice のインジェクトで取得でき、
	 * ServletContext を取得する為の Module をアプリ側で書く必要はない。既に約束されている。
	 * JacobApplication 継承クラス以外では、
	 *
	 *    &#064;Inject private ServletContext servletContext;
	 *
	 * という記述で取得可能である。
	 * </pre>
	 * @return ServletContext
	 */
	protected final ServletContext getServletContext(){
	   return servletContext;
	}

	/**
	 * アプリケーションコンテキスト破棄時に実行する処理.
	 */
	protected void onDestroy(){
	}
	/**
	 * リクエストのURIが未登録の場合に、HTTP 404 を返す場合のJsonResponderを取得する。.
	 * 本メソッドは null を返すようになっており、オーバライドして HTTP 404 専用のJsonResponderで	 * コンテンツを返すようにするのが目的。
	 * オーバライドしなければ、HTTPレスポンスのコンテンツとして何も返さないで、HTTPステータス 404 を返すだけである
	 * @return JsonResponder
	 */
	protected JsonResponder get404Responder(){
		return null;
	}
}
