package com.ynosuke.android.ladical.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

import com.ynosuke.android.ladical.R;

//------------------------------------------------------------------------------
/**
* 設定値管理クラスです。
*
* <p>更新履歴：
* <pre>
*     VerNo.        author              update      comment
*     Ver1.00.00    Yoshinosuke Nagaya  2013/12/16  新規作成
*     Ver1.00.01						2014/12/24	表示モードのパラメータ初期化処理追加
*     Ver1.00.02						2015/02/02	生理中設定を無効、非表示にしないよう起動時に処理
* </pre>
* </p>
* @author Yoshinosuke Nagaya
*/
//------------------------------------------------------------------------------
public class PrefUtil implements IGlobalPreferences, IMode{
	/** 設定値管理 */
	private static SharedPreferences	pref ;
	
	/** このクラスのインスタンス */
	private static PrefUtil instance ;
	
	/** コンテキスト */
	private static Context context ;
	
	/**-------------------------------------------------------------------------
	 * インスタンスを取得します。
	 *------------------------------------------------------------------------*/
	public static PrefUtil getInstance( Context context){
		if( instance == null){
			instance = new PrefUtil() ;
			PrefUtil.context = context ;
			pref = PreferenceManager.getDefaultSharedPreferences( context) ;
			if( !instance.getBoolean( R.string.pref_init)){
				instance.initPreferences() ;									// パラメータ初期化
			}
			
			// Ver.1.00.02パラメータ初期化
			int mode = instance.getInt( R.string.pref_displaymode) ;
			if( mode != TEMP && mode != WEIGHT && mode != RATIO){
				instance.putPref( R.string.pref_displaymode, mode);
			}
			if( !instance.getBoolean( R.string.pref_param_enabled, 1)){
				instance.putPref( R.string.pref_param_enabled, 1, true);
				instance.putPref( R.string.pref_param_display, 1, true);
			}
		}
		return instance ;
	}
	
	public SharedPreferences getPref(){
		return pref ;
	}
	
	/**-------------------------------------------------------------------------
	 * String設定値を取得します。
	 * 
	 * @param key 	R.string
	 *------------------------------------------------------------------------*/
	public String getString( int key){
		return getString( key, null) ;
	}
	
	/**-------------------------------------------------------------------------
	 * String設定値を取得します。
	 * 
	 * @param key 	R.string
	 * @param obj	補助値
	 *------------------------------------------------------------------------*/
	public String getString( int key, Object obj){
		return pref.getString( context.getString( key, obj), "") ;
	}
	
	/**-------------------------------------------------------------------------
	 * int設定値を取得します。
	 * 
	 * @param key	キー文字列
	 *------------------------------------------------------------------------*/
	public int getInt( int key){
		return getInt( key, null) ;
	}
	
	/**-------------------------------------------------------------------------
	 * int設定値を取得します。
	 * 
	 * @param key	キー文字列
	 * @param obj	補助値
	 *------------------------------------------------------------------------*/
	public int getInt( int key, Object obj){
		return Integer.parseInt( pref.getString( context.getString( key, obj), "0")) ;
	}
	
	/**-------------------------------------------------------------------------
	 * long設定値を取得します。
	 * 
	 * @param key	キー文字列
	 *------------------------------------------------------------------------*/
	public long getLong( int key){
		return getLong( key, null) ;
	}
	
	/**-------------------------------------------------------------------------
	 * long設定値を取得します。
	 * 
	 * @param key	キー文字列
	 * @param obj	補助値
	 *------------------------------------------------------------------------*/
	public long getLong( int key, Object obj){
		return Long.parseLong( pref.getString( context.getString( key, obj), "0")) ;
	}
	
	/**-------------------------------------------------------------------------
	 * float設定値を取得します。
	 * 
	 * @param key	キー文字列
	 *------------------------------------------------------------------------*/
	public float getFloat( int key){
		return getFloat( key, null) ;
	}
	
	/**-------------------------------------------------------------------------
	 * float設定値を取得します。
	 * 
	 * @param key	キー文字列
	 * @param obj	補助値
	 *------------------------------------------------------------------------*/
	public float getFloat( int key, Object obj){
		return Float.parseFloat( pref.getString( context.getString( key, obj), "0")) ;
	}

	/**-------------------------------------------------------------------------
	 * boolean設定値を取得します。
	 * 
	 * @param key	R.string.キー
	 *------------------------------------------------------------------------*/
	public boolean getBoolean( int key){
		return getBoolean( key, null) ;
	}
	
	/**-------------------------------------------------------------------------
	 * boolean設定値を取得します。
	 * 
	 * @param key	R.string.キー
	 * @param obj	補助値
	 *------------------------------------------------------------------------*/
	public boolean getBoolean( int key, Object obj){
		return pref.getBoolean( context.getString( key, obj), false) ;
	}

	/**-------------------------------------------------------------------------
	 * 設定値を保存します。
	 * 
	 * @param key	R.string
	 * @param value	設定値
	 *------------------------------------------------------------------------*/
	public void putPref( int intkey, Object value){
		putPref( intkey, null, value) ;
	}
	
	/**-------------------------------------------------------------------------
	 * 設定値を保存します。
	 * 
	 * @param key	R.string
	 * @param obj	R.string補助値
	 * @param value	設定値
	 *------------------------------------------------------------------------*/
	public void putPref( int intkey, Object obj, Object value){
		Editor editor = pref.edit() ;
		String key = context.getString( intkey, obj) ;
		if( value instanceof Boolean){
			editor.putBoolean( key, ( Boolean)value) ;
		}
		else{
			editor.putString( key, value.toString()) ;
		}
		editor.commit() ;
	}

	/**-------------------------------------------------------------------------
	 * 設定値を初期化します。
	 *------------------------------------------------------------------------*/
	private void initPreferences(){
		putPref( R.string.pref_theme, PINK_DOT) ;								// テーマをピンク・ドットに設定
		putPref( R.string.pref_temp_min, 35.5f) ;								// 体温下限を35.5度に設定
		putPref( R.string.pref_temp_max, 37.5f) ;								// 体温上限を37.5度に設定
		putPref( R.string.pref_weight_min, 40) ;								// 体重下限を40kgに設定
		putPref( R.string.pref_weight_max, 80) ;								// 体重上限を80kgに設定
		putPref( R.string.pref_ratio_min, 6) ;									// 体脂肪率下限を6%に設定
		putPref( R.string.pref_ratio_max, 30) ;									// 体脂肪率上限を30%に設定
//		putPref( R.string.pref_digit_temp, 2) ;									// 体温入力桁数を2桁に設定
//		putPref( R.string.pref_digit_weight, 2) ;								// 体重入力桁数を2桁に設定
//		putPref( R.string.pref_digit_ratio, 2) ;								// 体脂肪率入力桁数を2桁に設定
		putPref( R.string.pref_period_cycle, 28) ;								// 生理周期を28日に設定
		putPref( R.string.pref_cycle_settype, CYCLE_SETTYPE_AVG) ;				// 生理周期設定モードを平均に設定
		putPref( R.string.pref_cycle_avg_times, 6) ;							// 生理周期平均集計回数を6回に設定
		putPref( R.string.pref_week_start, 0) ;									// 週開始曜日を日曜に設定
		putPref( R.string.pref_show_blank_line, true) ;							// 未記入域描画を描画ありに設定
//		putPref( R.string.pref_show_tempborder, false) ;						// 体温基準ラインなしに設定
//		putPref( R.string.pref_temp_fix, 36.5f) ;								// 固定体温を36.5度に設定
//		putPref( R.string.pref_tempborder_settype, TEMPBORDER_SETTYPE_FIX) ;	// 体温基準ラインを固定値に設定
//		putPref( R.string.pref_temp_avg_span, 6) ;								// 体温平均集計期間を過去6回に設定
		
		for( int i = 1; i <= 10; i++){
			putPref( R.string.pref_param_enabled, i, i<=5) ;					// 項目5までパラメータ使用可
			putPref( R.string.pref_param_display, i, i<=5) ;					// 項目5までパラメータ表示
		}
		
		
		putPref( R.string.pref_param_name, 1, context.getString( R.string.param1)) ;
		putPref( R.string.pref_param_name, 2, context.getString( R.string.param2)) ;
		putPref( R.string.pref_param_name, 3, context.getString( R.string.param3)) ;
		putPref( R.string.pref_param_name, 4, context.getString( R.string.param4)) ;
		putPref( R.string.pref_param_name, 5, context.getString( R.string.param5)) ;
		putPref( R.string.pref_param_name, 6, context.getString( R.string.param6)) ;
		putPref( R.string.pref_param_name, 7, context.getString( R.string.param7)) ;
		putPref( R.string.pref_param_name, 8, context.getString( R.string.param8)) ;
		putPref( R.string.pref_param_name, 9, context.getString( R.string.param9)) ;
		putPref( R.string.pref_param_name, 10, context.getString( R.string.param10)) ;	// 各項目名称を設定
		putPref( R.string.pref_param_mark, 1, 0) ;
		putPref( R.string.pref_param_mark, 2, 1) ;
		putPref( R.string.pref_param_mark, 3, 2) ;
		putPref( R.string.pref_param_mark, 4, 3) ;
		putPref( R.string.pref_param_mark, 5, 4) ;
		putPref( R.string.pref_param_mark, 6, 5) ;
		putPref( R.string.pref_param_mark, 7, 6) ;
		putPref( R.string.pref_param_mark, 8, 7) ;
		putPref( R.string.pref_param_mark, 9, 7) ;
		putPref( R.string.pref_param_mark, 10, 7) ;								// 各項目マーク設定
		
		putPref( R.string.pref_init, true) ;									// パラメータ初期化済みとする
	}
}

