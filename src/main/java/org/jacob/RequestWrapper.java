package org.jacob;

import java.io.File;
import java.io.UnsupportedEncodingException;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
/**
 * HttpServletRequest Wrapper.
 * <PRE>
 * HttpServletRequest のラッパー、HTTPリクエストパラメータ値の取得で、２バイト文字が文字化けするのを回避する為にラッピングして使用する。
 * <h3>Usage.</h3>
 * JsonResponder 実装の中で RequestWrapper を取得して使用する。
 * 例）
 *    public class SampleResponder implements JsonResponder{
 *      &#064;Override
 *       public String answer(HttpServletRequest request){
 *          RequestWrapper requestWrapper = RequestWrapper.get(request);
 *          String p1 = requestWrapper.getParameterValue("param1");
 *          String[] p2 = requestWrapper.getParameterValues("param2");
 *          //
 *          return "{ 'status': 0 }";
 *       }
 *    }
 *
 * </PRE>
 */
public final class RequestWrapper{
	private HttpServletRequest request;
	/**
	 * private constructor.
	 * @param request HttpServletRequest
	 */
	private RequestWrapper(HttpServletRequest request){
		this.request = request;
	}
	/**
	 * RequestWrapper取得.
	 * @param request HttpServletRequest
	 * @return RequestWrapper
	 */
	public static RequestWrapper get(HttpServletRequest request){
		return new RequestWrapper(request);
	}
	/**
	 * HttpServletRequest参照.
	 * @return HttpServletRequest
	 */
	public HttpServletRequest getRequest(){
	   return request;
	}
	/**
	 * パラメータ取得.
	 * @param parameterName パラメータ名
	 * @return String
	 */
	public String getParameterValue(String parameterName){
		String value = request.getParameter(parameterName);
		if (isRequireChangeFromUnicode(value)){
			try{
				return new String(value.getBytes("ISO-8859-1"), "utf-8");
			}catch(UnsupportedEncodingException e){
			}
		}
		return value;
	}
	/**
	 * int型パラメータ取得.
	 * @param parameterName パラメータ名
	 * @return int 注意：数値に変換できない場合（空文字列含む）、Exception が発生する。全角数字は半角数字に置き換えられて解釈される。
	 */
	public int getParameterValueInt(String parameterName){
		String s = request.getParameter(parameterName);
		if (isRequireChangeFromUnicode(s)){
			try{
				s = new String(s.getBytes("ISO-8859-1"), "utf-8");
			}catch(UnsupportedEncodingException e){
			}
		}
		StringBuffer sb = new StringBuffer(s);
		for(int i=0;i < sb.length();i++){
			char c = sb.charAt(i);
			if (c >= '０' && c <= '９'){
				sb.setCharAt(i, (char)(c - '０' + '0'));
			}else if(c == '－'){
				sb.setCharAt(i, '-');
			}
		}
		s = sb.toString();
		return Integer.parseInt(s);
	}
	/**
	 * long型パラメータ取得.
	 * @param parameterName パラメータ名
	 * @return long 注意：数値に変換できない場合（空文字列含む）、Exception が発生する。全角数字は半角数字に置き換えられて解釈される。
	 */
	public long getParameterValueLong(String parameterName){
		String s = request.getParameter(parameterName);
		if (isRequireChangeFromUnicode(s)){
			try{
				s = new String(s.getBytes("ISO-8859-1"), "utf-8");
			}catch(UnsupportedEncodingException e){
			}
		}
		StringBuffer sb = new StringBuffer(s);
		for(int i=0;i < sb.length();i++){
			char c = sb.charAt(i);
			if (c >= '０' && c <= '９'){
				sb.setCharAt(i, (char)(c - '０' + '0'));
			}else if(c == '－'){
				sb.setCharAt(i, '-');
			}
		}
		s = sb.toString();
		return Long.parseLong(s);
	}
	/**
	 * パラメータ取得配列.
	 * @param parameterName パラメータ名
	 * @return String[]
	 */
	public String[] getParameterValues(String parameterName){
		String[] array = request.getParameterValues(parameterName);
		for(int i = 0; i < array.length; i++){
			if (isRequireChangeFromUnicode(array[i])){
				try{
					array[i] = new String(array[i].getBytes("ISO-8859-1"), "utf-8");
				}catch(UnsupportedEncodingException e){
				}
			}
		}
		return array;
	}
	/**
	 * ２byte 文字判定.
	 * @param str 対象文字列
	 * @return true : ISO-8859-1 から変換が必要 → new String(str.getBytes("ISO-8859-1"), "utf-8");
	 */
	public static boolean isRequireChangeFromUnicode(String str){
		if (str==null) return false;
		for(int i = 0; i < str.length(); i++){
			char ch = str.charAt(i);
			Character.UnicodeBlock unicodeBlock = Character.UnicodeBlock.of(ch);
			if (Character.UnicodeBlock.HIRAGANA.equals(unicodeBlock))
				return false;
			if (Character.UnicodeBlock.KATAKANA.equals(unicodeBlock))
				return false;
			if (Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS.equals(unicodeBlock))
				return false;
			if (Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS.equals(unicodeBlock))
				return false;
			if (Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION.equals(unicodeBlock))
				return false;
		}
		return true;
	}
	/**
	 * WEB-INF上のClassのサーブレットコンテキストパスのFileを取得.
	 * @param context ServletContext
	 * @param cls WEB-INF上のClass
	 * @return java.io.File
	 */
	public static File packageFile(ServletContext context, Class<?> cls){
		return new File(
			context.getRealPath("") + "/WEB-INF/classes/" + cls.getPackage().getName().replaceAll("\\.", "/")
		);
	}
}
