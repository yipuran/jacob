/**
 * JSON 応答アプリケーション.<br/>
 * <pre>Webコンテナ上、Filterで実行するJSON応答アプリケーション
 * web.xml で Filter を定義、Servlet-API より HttpServletRequest を取得して
 * JSON文字列を応答として ServletResponse に返却する。
 * <br>
 * Filter は、org.jacob.JacobFilter を使用し初期化パラメータに、
 * 属性名＝"applicationClassName" に、<b>org.jacob.JacobApplication</b> の継承クラスを
 * 指定しなければならない。
 *
 * <h4>レスポンスのHTTPヘッダ</h4>
 * JSON 応答のアプリである為に、次の Content-Type が自動的に付与される。
 *           Content-Type: application/json; charset=utf-8
 * web.xml で、
 *       Access-Control-Allow-Origin
 *       Access-Control-Allow-Headers
 *       Access-Control-Allow-Methods
 *       Access-Control-Allow-Credentials
 *       Access-Control-Expose-Headers
 * に付与について、フィルタ設定の init-param で指定する。
 * web.xml 記述例
 * jp.uran.sample.SampleApplication は、JacobApplication を継承している。
 * &lt;filter&gt;
 *     &lt;filter-name&gt;JacobFilter&lt;/filter-name&gt;
 *     &lt;filter-class&gt;org.jacob.JacobFilter&lt;/filter-class&gt;
 *     &lt;init-param&gt;
 *         &lt;param-name&gt;applicationClassName&lt;/param-name&gt;
 *         &lt;param-value&gt;jp.uran.sample.SampleApplication&lt;/param-value&gt;
 *     &lt;/init-param&gt;
 *     &lt;init-param&gt;
 *         &lt;param-name&gt;accessPath&lt;/param-name&gt;
 *         &lt;param-value&gt;*&lt;/param-value&gt;
 *     &lt;/init-param&gt;
 *     &lt;init-param&gt;
 *         &lt;param-name&gt;customHeaders&lt;/param-name&gt;
 *         &lt;param-value&gt;X-Requested-With,X-some,X-foo&lt;/param-value&gt;
 *     &lt;/init-param&gt;
 *     &lt;init-param&gt;
 *         &lt;param-name&gt;allowMethods&lt;/param-name&gt;
 *         &lt;param-value&gt;GET,POST&lt;/param-value&gt;
 *     &lt;/init-param&gt;
 *     &lt;init-param&gt;
 *         &lt;param-name&gt;allowCredentials&lt;/param-name&gt;
 *         &lt;param-value&gt;true&lt;/param-value&gt;
 *     &lt;/init-param&gt;
 *     &lt;init-param&gt;
 *         &lt;param-name&gt;exposeHeaders&lt;/param-name&gt;
 *         &lt;param-value&gt;X-some,X-foo&lt;/param-value&gt;
 *     &lt;/init-param&gt;
 * &lt;/filter&gt;
 *
 * 上の記述例で、応答レスポンスHTTPヘッダは、以下が付与される。
 *    Content-Type: application/json; charset=utf-8
 *    Access-Control-Allow-Origin: *
 *    Access-Control-Allow-Headers: X-Requested-With,X-some,X-foo
 *    Access-Control-Allow-Methods: GET,POST
 *    Access-Control-Allow-Credentials: true
 *    Access-Control-Expose-Headers: X-some,X-foo
 *
 *    allowCredentials と exposeHeaders を記述しない場合、
 *    Access-Control-Allow-Credentials と Access-Control-Expose-Headers は付与されない。
 *    accessPath, customHeaders, allowMethods を記述しない場合、
 *    デフォルトで
 *    Access-Control-Allow-Origin: *
 *    Access-Control-Allow-Headers: Content-Type
 *    Access-Control-Allow-Methods: GET,POST,OPTIONS
 *    が付与される。
 *    OPTIONS は、JsonResponder でハンドリングされることはなくそのままレスポンス応答する
 * </pre>
 * <hr/>
 * <h4>Install</h4>
 * <pre>
 * mvn install:install-file -Dfile=path/jacob-1.0.jar -DgroupId=org.jacob -DartifactId=jacob -Dversion=1.0 -Dpackaging=jar -DgeneratePom=true
 *
 * Maven pom.xml への記述
 *
 * &lt;dependency&gt;
 *    &lt;groupId&gt;org.jacob&lt;/groupId&gt;
 *    &lt;artifactId&gt;jacob&lt;/artifactId&gt;
 *    &lt;version&gt;1.0&lt;/version&gt;
 * &lt;/dependency&gt;
 * </pre>
 * <hr/>
 * <h4>Google guice Inject をサポート</h4>
 * <pre>
 * JSON 応答のレスポンス生成は、JacobApplication の init() によるものであるが、init()の実行過程において、
 * Google guice による Inject を実行可能としている。
 * 本パッケージでの guice の実行は、{@link org.jacob.RequestTranslater} を参照
 * </pre>
 * <hr/>
 * <h4>URIによる応答レスポンス実装の振り分け</h4>
 * <pre>
 * JacobApplication の init() で、単一の JsonResponder を返せばURIによる振り分けは、web.xml の filter の記述だけに委ねるが、
 * 以下のように、RequestTranslater を返すことで、URI による JsonResponder 処理の振り分けが可能になる。
 * （例） web.xml でフィルタURLが、/sample/json/* とした場合、、
 *    public class SampleApplication extends JacobApplication {
 *       &#064;Override
 *       public JsonResponder init(){
 *          RequestTranslater translater = getRequestTranslater();
 *
 *          // class SalesResponder implements JsonResponder を呼び出す設定
 *          translater.add("/sample/json/sales", SalesResponder.class);
 *
 *          // class ContactResponder implements JsonResponder を呼び出す設定
 *          translater.add("/sample/json/contact", ContactResponder.class);
 *
 *          // class EditResponder implements JsonResponder を呼び出す設定
 *          translater.add("/sample/json/edit", EditResponder.class);
 *       }
 *    }
 *
 * </pre>
 * <hr/>
 * <h4>ServletContext の取得</h4>
 * <pre>
 * JacobApplication 継承クラスは、ServletContext を取得する場合、JacobApplication から getServletContext()が提供されているが、
 * JacobApplication 継承クラス以外で ServletContext を取得する場合、Google　guice のインジェクトで取得できるようになっている。
 * 改めて、ServletContext を取得する為の Module をアプリ側で書く必要はない。既に約束されている。
 *
 *    &#064;Inject private ServletContext servletContext;
 *
 * という記述で取得可能である。
 * </pre>
 */
package org.jacob;
