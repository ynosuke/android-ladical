package com.ynosuke.android.ladical.data;

import java.util.Date;

import android.content.ContentValues;
import android.database.Cursor;

import com.ynosuke.android.ladical.util.DbUtil;

//------------------------------------------------------------------------------
/**
* 1日分の情報アイテムです。
*
* <p>更新履歴：
* <pre>
*     VerNo.        author              update      comment
*     Ver1.00.00    Yoshinosuke Nagaya  2013/01/11  新規作成
* </pre>
* </p>
* @author Yoshinosuke Nagaya
*/
//------------------------------------------------------------------------------
public class Item {
	//	定数定義 ----------------------------------------------------------------
	/** 出産予定日 */
	public static final int	TERM = -1 ;
	
	//	変数定義 ----------------------------------------------------------------
	/** 日付 */
	public Date		date ;
	
	/** 体温 */
	public float	temp ;
	
	/** 体重 */
	public float	weight ;
	
	/** 体脂肪率 */
	public float	ratio ;
	
	/** メモ */
	public String	memo ;
	
	/** 生理開始 */
	public boolean	periodStart ;
	
	/** 生理日予想 */
	public boolean	willPeriod ;
	
	/** 排卵日予想 */
	public boolean	willOv ;
	
	/** 妊娠週数
	 * 		TERM 	: 出産予定日
	 *		0		: 週数表記なし
	 *		1〜		: 0週目から
	 *  */
	public int		pregWeeks ;
	
	/** パラメータ */
	public boolean[] param = new boolean[10] ;
	
	/**-------------------------------------------------------------------------
	 * Itemクラスのインスタンスを作成します。
	 *------------------------------------------------------------------------*/
	public Item( Date date){
		this.date = date ;
		this.memo = "" ;
	} ;
	
	/**-------------------------------------------------------------------------
	 * Itemクラスのインスタンスを作成します。
	 *------------------------------------------------------------------------*/
	public Item( Date date, float temp, float weight, float ratio, String memo, boolean period, boolean[] param){
		this.date = date ;
		this.temp = temp ;
		this.weight = weight ;
		this.ratio = ratio ;
		this.memo = memo ;
		this.periodStart = period ;
		for( int i = 0; i < param.length; i++){
			this.param[i] = param[i] ;
		}
	}
	
	/**-------------------------------------------------------------------------
	 * Itemクラスのインスタンスを作成します。（データベースから取得する場合）
	 *------------------------------------------------------------------------*/
	public Item( Cursor cursor){
		int index = 0 ;
		date = DbUtil.parseDate( cursor.getString( index++)) ;
		temp = cursor.getFloat( index++) ;
		weight = cursor.getFloat( index++) ;
		ratio = cursor.getFloat( index++) ;
		memo = cursor.getString( index++) ;
		periodStart = cursor.getInt( index++) == 1 ;
		willPeriod = cursor.getInt( index++) == 1 ;
		willOv = cursor.getInt( index++) == 1 ;
		pregWeeks = cursor.getInt( index++) ;
		for( int i = 0; i < param.length; i++){
			param[i] = cursor.getInt( index++) == 1 ;
		}
	}
	
	/**-------------------------------------------------------------------------
	 * データベース格納用のパラメータを取得します。
	 *------------------------------------------------------------------------*/
	public ContentValues getContentValues(){
		ContentValues values = new ContentValues() ;
		values.put( "date", DbUtil.formatDate( date)) ;
		values.put( "temp", temp) ;
		values.put( "weight", weight) ;
		values.put( "ratio", ratio) ;
		values.put( "memo", memo) ;
		values.put( "periodStart", periodStart ? 1 : 0) ;
		values.put( "willPeriod", willPeriod ? 1 : 0) ;
		values.put( "willOv", willOv ? 1 : 0) ;
		values.put( "pregWeeks", pregWeeks) ;
		for( int i = 0; i < param.length; i++){
			values.put( "param" + ( i + 1), param[i] ? 1 : 0) ;
		}
		return values ;
	}
}

