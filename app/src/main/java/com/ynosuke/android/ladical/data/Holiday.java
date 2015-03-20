package com.ynosuke.android.ladical.data;

import java.util.Date;

import android.database.Cursor;

import com.ynosuke.android.ladical.util.DbUtil;

//------------------------------------------------------------------------------
/**
* 祝日情報アイテムです。
*
* <p>更新履歴：
* <pre>
*     VerNo.        author              update      comment
*     Ver1.00.00    Yoshinosuke Nagaya  2014/02/28  新規作成
* </pre>
* </p>
* @author Yoshinosuke Nagaya
*/
//------------------------------------------------------------------------------
public class Holiday {
	//	変数定義 ----------------------------------------------------------------
	/** 日付 */
	public Date		date ;
	
	/** 名称 */
	public String	name ;
	
	/**-------------------------------------------------------------------------
	 * Holidayクラスのインスタンスを作成します。
	 *------------------------------------------------------------------------*/
	public Holiday( Date date, String name){
		this.date = date ;
		this.name = name ;
	}
	
	/**-------------------------------------------------------------------------
	 * Holidayクラスのインスタンスを作成します。
	 *------------------------------------------------------------------------*/
	public Holiday( Cursor cursor){
		int index = 0 ;
		date = DbUtil.parseDate( cursor.getString( index++)) ;
		name = cursor.getString( index++) ;
	}
	
	/**-------------------------------------------------------------------------
	 * DB格納用の日付文字列を取得します。
	 *------------------------------------------------------------------------*/
	public String getDate(){
		return DbUtil.DATE_FORMAT.format( date) ;
	}
}

