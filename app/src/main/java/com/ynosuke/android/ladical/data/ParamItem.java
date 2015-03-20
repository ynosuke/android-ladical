package com.ynosuke.android.ladical.data;

import android.graphics.Bitmap;

//------------------------------------------------------------------------------
/**
* パラメータ設定項目アイテムです。
*
* <p>更新履歴：
* <pre>
*     VerNo.        author              update      comment
*     Ver1.00.00    Yoshinosuke Nagaya  2013/01/12  新規作成
* </pre>
* </p>
* @author Yoshinosuke Nagaya
*/
//------------------------------------------------------------------------------
public class ParamItem {
	//	変数定義 ----------------------------------------------------------------
	/** 項目No */
	public int		no ;
	
	/** 名称 */
	public String	name ;
	
	/** 設定値 */
	public boolean	value ;

	/** マーク */
	public Bitmap	mark ;
	
	public ParamItem( int no , String name){
		this.no = no ;
		this.name = name ;
	}
	
	public ParamItem( int no, String name, boolean value, Bitmap mark){
		this.no = no ;
		this.name = name ;
		this.value = value ;
		this.mark = mark ;
	}
}

