package org.jacob;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
	private String accessControlAllowsPath;
	private String customheaders;
	private String allowMethods;
	private List<String> allowMethodList;
	private Optional<String> allowCredentials;
	private Optional<String> exposeheaders;
	private JacobApplication application;

	/* @see javax.servlet.Filter#init(javax.servlet.FilterConfig) */
	@Override
	public void init(FilterConfig config) throws ServletException{
		String applicationClassName = config.getInitParameter("applicationClassName");
		accessControlAllowsPath = Optional.ofNullable(config.getInitParameter("accessPath")).orElse("*");
		customheaders = Optional.ofNullable(config.getInitParameter("customHeaders")).orElse("Content-Type");
		allowMethods = Optional.ofNullable(config.getInitParameter("allowMethods")).orElse("GET,POST,OPTIONS");
		allowMethodList = Arrays.stream(allowMethods.split(","))	.map(e->e.replaceAll(" ", "")).filter(e->e.length() > 0)
				.map(e->e.toUpperCase()).collect(Collectors.toList());
		allowMethods = allowMethodList.stream().collect(Collectors.joining(","));
		allowCredentials = Optional.ofNullable(config.getInitParameter("allowCredentials"))
				.map(e->e.toLowerCase()).filter(e->"true".equals(e)||"false".equals(e));
		exposeheaders = Optional.ofNullable(config.getInitParameter("exposeHeaders"));
		logger.debug("## JacobApplication init() START  applicationClassName = " + applicationClassName);
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		try{
			application = (JacobApplication)loader.loadClass(applicationClassName).getConstructor(new Class<?>[]{}).newInstance();
			application.setServletContext(config.getServletContext());
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
		HttpServletResponse httpres = (HttpServletResponse)response;
		httpres.addHeader("Content-Type", "application/json; charset=utf-8");
		httpres.addHeader("Access-Control-Allow-Origin", accessControlAllowsPath);
		httpres.addHeader("Access-Control-Allow-Headers", customheaders);
		httpres.addHeader("Access-Control-Allow-Methods", allowMethods);
		allowCredentials.ifPresent(b->{
			httpres.addHeader("Access-Control-Allow-Credentials", b);
		});
		exposeheaders.ifPresent(e->{
			httpres.addHeader("Access-Control-Expose-Headers", e);
		});

		HttpServletRequest httpServletRequest = (HttpServletRequest)request;
		String method = httpServletRequest.getMethod().toUpperCase();
		logger.debug("## request Method = " + method);
		if ("OPTIONS".equals(method)){
			httpres.setStatus(200);
			return;
		}
		if (!allowMethodList.stream().anyMatch(e->e.equals(method))){
			httpres.setStatus(400);
			return;
		}
		logger.debug("## jsonResponder = " + jsonResponder);
		if (jsonResponder==null){
			httpres.setStatus(404);
			return;
		}
		((RequestTranslater)jsonResponder).response = (HttpServletResponse)response;
		String content = jsonResponder.answer(httpServletRequest);
		if (content != null){
			httpres.setStatus(jsonResponder.getStatus());
			byte[] b = content.getBytes();
			httpres.addHeader("Content-Length", Integer.toString(b.length));
			response.setCharacterEncoding("UTF-8");
			response.getOutputStream().write(content.getBytes());
		}else{
			httpres.setStatus(404);
			if (notFoundResponder != null){
				String str = notFoundResponder.answer(httpServletRequest);
				if (str != null){
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
