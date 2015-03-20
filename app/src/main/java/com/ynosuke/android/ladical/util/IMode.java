package com.ynosuke.android.ladical.util;

//------------------------------------------------------------------------------
/**
* 表示モード定義インターフェースです。
*
* <p>更新履歴：
* <pre>
*     VerNo.        author              update      comment
*     Ver1.00.00    Yoshinosuke Nagaya  2013/03/05  新規作成
* </pre>
* </p>
* @author Yoshinosuke Nagaya
*/
//------------------------------------------------------------------------------
public interface IMode {
	/** 体温 */
	public static final int		TEMP = 0 ;
	
	/** 体重 */
	public static final int		WEIGHT = 1 ;
	
	/** 体脂肪率 */
	public static final int		RATIO = 2 ;
	
	/** 妊娠週数 */
	public static final int		WEEKS = 3 ;
}