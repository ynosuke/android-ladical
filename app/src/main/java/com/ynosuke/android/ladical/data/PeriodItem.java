package com.ynosuke.android.ladical.data;

import java.util.Date;

//------------------------------------------------------------------------------
/**
* 生理期間・周期情報です。
*
* <p>更新履歴：
* <pre>
*     VerNo.        author              update      comment
*     Ver1.00.00    Yoshinosuke Nagaya  2013/12/18  新規作成
* </pre>
* </p>
* @author Yoshinosuke Nagaya
*/
//------------------------------------------------------------------------------
public class PeriodItem {
	/** 開始日 */
	public Date	date ;
	
	/** 期間 */
	public int	length ;
	
	/** 周期 */
	public int	cycle ;
	
	/**-------------------------------------------------------------------------
	 * コンストラクタ
	 *------------------------------------------------------------------------*/
	public PeriodItem( Date date, int length, int cycle){
		this.date = date ;
		this.length = length ;
		this.cycle = cycle ;
	}
}

