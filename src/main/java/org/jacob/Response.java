package org.jacob;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * HttpServletResponseアノテーション.
 * {@link JsonResponder} 実装クラス内で、@Response を付与したフィールドは、HttpServletResponseがセットされる。<br/>
 * <pre>
 *  （使い方）
 *
 *      ＠Response  private  HttpServletResponse  response;
 *          ：
 *      response.setContentType("application/octet-stream");
 *
 *
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Response{
}
