package org.jacob;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
/**
 * HTTPリクエストURI振り分けマッピング. RequestTranslater<br/>
 * <pre>HTTPリクエストURI振り分けの設定は、このクラスのインスタンスを {@link JacobApplication#init()}
 * の返却値とすることで、URI振り分け後の{@link JsonResponder} をHTTPリクエストURIに割り当てることができる。
 * また、{@link RequestTranslater#setModules(Module...)} を実行することで、 Google guice で{@link JsonResponder} を生成することが可能である。
 *
 * 例）
 * public class SampleApplication extends JacobApplication {
 *    &#064;Override
 *    public JsonResponder init(){
 *       RequestTranslater translater = getRequestTranslater();
 *       // URI '/sample/sales/do' に対して、class SampleResponder implements JsonResponder を設定
 *       //     web.xml でフィルタURLが、/sample/sales/* とした場合、
 *       translater.add("/sample/sales/do", SampleResponder.class);
 *       return translater;
 *    }
 * }
 *
 * Google guice で{@link JsonResponder} を生成する場合、
 *
 * public class SampleApplication extends JacobApplication {
 *    &#064;Override
 *    public JsonResponder init(){
 *       RequestTranslater translater = getRequestTranslater();
 *       // 任意の guice の Module で、javax.inject.Named アノテーションを付与した String に初期値を
 *       // インジェクトする場合、RequestTranslater の setModules で指定する。
 *       //   以下は、@Named("output") String tempdir;  に対する設定例である。
 *       translater.setModules(new AbstractModule(){
 *          &#064;Override
 *          protected void configure(){
 *             binder().bind(String.class).annotatedWith(Names.named("output")).toInstance("c:/var/temp/");
 *          }
 *       });
 *       // URI '/sample/sales/do' に対して、class SampleResponder implements JsonResponder を設定
 *       translater.add("/sample/sales/do", SampleResponder.class);
 *       return translater;
 *    }
 * }
 *
 * </pre>
 */
public final class RequestTranslater implements JsonResponder{
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	private Map<String, JsonResponder> map = new HashMap<String, JsonResponder>();
	private Map<String, Field> responseFiledMap = new HashMap<String, Field>();
	private Injector injector;
	private List<Module> moduleList = new ArrayList<Module>();
	private int httpstatus = 200;
	/**
	 * コンストラクタ.
	 * @param servletContext ServletContext
	 */
	protected RequestTranslater(final ServletContext servletContext){
		moduleList.add(new AbstractModule(){
			@Override
			protected void configure(){
				binder().bind(ServletContext.class).toInstance(servletContext);
			}
		});
		injector = Guice.createInjector(moduleList);
	}
	/**
	 * URI振り分け設定登録.
	 * @param uriPath WebコンテキストからのURIパス文字列
	 * @param cls JsonResponder実装クラス名
	 */
	public void add(String uriPath, Class<? extends JsonResponder> cls){
		map.put(uriPath, injector.getInstance(cls));
		for(Field field:cls.getDeclaredFields()){
			if (field.getAnnotation(Response.class) != null){
				field.setAccessible(true);
				responseFiledMap.put(uriPath, field);
			}
		}
	}
	/**
	 * Google guice インジェクトModule設定.
	 * <br/>guice インジェクト用の Moduleを設定する。
	 * {@link RequestTranslater#add(String, Class)} よりも前に実行する必要がある。<br/>
	 * 本メソッドを実行しない場合、guice インジェクトは実行されず、JsonResponder 実装はデフォルトコンストラクタで生成する。
	 * @param modules com.google.inject.Module
	 */
	public void setModules(Module...modules){
		for(Module m:modules){
			moduleList.add(m);
		}
		injector = Guice.createInjector(moduleList);
		logger.debug("### Guice Injector Module set." );
	}
	/**
	 * JsonResponder 呼び出し実行処理.
	 * <pre>HTTP要求に対してURIをチェックして一致する JsonResponder を実行する。
	 * 一致しない場合、null を返す。
	 * </pre>
	 * @param request HttpServletRequest
	 * @return HTTP応答の文字列、URIをチェックして一致しない場合 null を返す。
	 */
	@Override
	public String answer(HttpServletRequest request){
		httpstatus = 404;
		String uriStr = request.getRequestURI();
		if (map.containsKey(uriStr)){
			httpstatus = 200;
			JsonResponder jsonResponder = map.get(uriStr);
			if (responseFiledMap.containsKey(uriStr)){
				try{
					responseFiledMap.get(uriStr).set(jsonResponder, response);
				}catch(SecurityException | IllegalArgumentException | IllegalAccessException e){
					logger.warn(e.getMessage(), e );
				}
			}
			String content = jsonResponder.answer(request);
			httpstatus = jsonResponder.getStatus();
			return content;
		}
		return null;
	}
	/**
	 * HttpServletResponse.
	 */
	protected HttpServletResponse response;

	/* @see org.jacob.JsonResponder#getStatus() */
	@Override
	public int getStatus() {
		return httpstatus;
	}
}
