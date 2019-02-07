package org.jacob;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * JacobFilter Webフィルタ.
 * <pre>
 * Webフィルタで JSON応答のアプリケーションが、HTTPレスポンスを返却するようにする。
 * web.xml 記述例 → <a href="package-summary.html">パッケージの説明</a>
 * </pre>
 */
public final class JacobFilter implements Filter{
	/** Logger. */
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	private JsonResponder jsonResponder;
	private JsonResponder notFoundResponder;

	private String accessControlAllowsPath = "*";

	private JacobApplication application;

	/* @see javax.servlet.Filter#init(javax.servlet.FilterConfig) */
	@Override
	public void init(FilterConfig config) throws ServletException{
		String applicationClassName = config.getInitParameter("applicationClassName");
		logger.debug("## JacobApplication init() START  applicationClassName = " + applicationClassName);
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		try{
			application = (JacobApplication)loader.loadClass(applicationClassName).getConstructor(new Class<?>[]{}).newInstance();
			application.setServletContext(config.getServletContext());
			accessControlAllowsPath = application.getAccessControlAllowPath();
			logger.debug("## RequestTranslater created.");
			jsonResponder = application.init();
			notFoundResponder = application.get404Responder();
			logger.debug("## JacobApplication init() end.");
		}catch(Exception e){
			logger.error(e.getMessage(), e);
		}
	}
	/* @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain) */
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,ServletException{
		HttpServletRequest httpServletRequest = (HttpServletRequest)request;
		String method = httpServletRequest.getMethod();
		if (!method.equals("GET") && !method.equals("POST")) return;
		if (jsonResponder==null) return;
		((RequestTranslater)jsonResponder).response = (HttpServletResponse)response;
		String content = jsonResponder.answer(httpServletRequest);
		if (content != null){
			HttpServletResponse httpres = (HttpServletResponse)response;
			httpres.setStatus(jsonResponder.getStatus());
			httpres.addHeader("Content-Type", "application/json; charset=utf-8");
			httpres.addHeader("Access-Control-Allow-Origin", accessControlAllowsPath);
			httpres.addHeader("Access-Control-Allow-Headers", "X-Requested-With");
			byte[] b = content.getBytes();
			httpres.addHeader("Content-Length", Integer.toString(b.length));
			response.setCharacterEncoding("UTF-8");
			response.getOutputStream().write(content.getBytes());
		}else{
			HttpServletResponse httpres = (HttpServletResponse)response;
			httpres.setStatus(404);
			if (notFoundResponder != null){
				String str = notFoundResponder.answer(httpServletRequest);
				if (str != null){
					httpres.addHeader("Content-Type", "application/json; charset=utf-8");
					byte[] b = str.getBytes();
					httpres.addHeader("Content-Length", Integer.toString(b.length));
					response.setCharacterEncoding("UTF-8");
					response.getOutputStream().write(str.getBytes());
				}
			}
		}
	}
	/* @see javax.servlet.Filter#destroy() */
	@Override
	public void destroy(){
		application.onDestroy();
	}
}
