package org.jacob;

import java.util.Arrays;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;


/**
 * HTTP Method enum
 */
public enum HTTPMethod{

	GET,POST,PUT,PATCH,DELETE,OPTIONS;

	public static HTTPMethod getHTTPMethod(HttpServletRequest request) {
		String method = Optional.ofNullable(request.getMethod()).map(e->e.toUpperCase()).orElse(null);
		return Arrays.stream(HTTPMethod.values()).filter(e->e.name().equals(method)).findAny().orElse(null);
	}
}
