package com.ynosuke.android.ladical.setting;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.DatePicker.OnDateChangedListener;

import com.ynosuke.android.ladical.R;
import com.ynosuke.android.ladical.util.AplUtil;
import com.ynosuke.android.ladical.util.DbUtil;

//------------------------------------------------------------------------------
/**
* 日付設定プリファレンスです。
*
* <p>更新履歴：
* <pre>
*     VerNo.        author              update      comment
*     Ver1.00.00    Yoshinosuke Nagaya  2013/03/16  新規作成
* </pre>
* </p>
* @author Yoshinosuke Nagaya
*/
//------------------------------------------------------------------------------
public class DatePickerPreference extends DialogPreference {
	/** 日付表示用フォーマット */
	@SuppressLint("SimpleDateFormat")
	public static final SimpleDateFormat DATEFORMAT = new SimpleDateFormat( "yyyy-MM-dd") ;
	
	/** 日付選択ピッカー */
	private DatePicker 	picker ;
	
	/** 設定値表示テキスト */
	private TextView 	valueText ;
	
	/** 日付計算用カレンダー */
	private Calendar	calendar = Calendar.getInstance() ;

	/**-------------------------------------------------------------------------
	 * インスタンス作成時の初期化処理を行います。（xmlにてレイアウトパラメータが指定された場合に使用）
	 * 
	 * @param context	コンテキスト
	 * @param attrs		レイアウトパラメータ
	 *------------------------------------------------------------------------*/
	public DatePickerPreference( Context context, AttributeSet attrs) {
		super( context, attrs);
		setWidgetLayoutResource( R.layout.preference_valueright) ;
	}

	/**-------------------------------------------------------------------------
	 * 設定値表示を更新します。
	 *------------------------------------------------------------------------*/
	public void updateSummary(){
		Date date = getDate() ;
		if( date == null || date.before( new Date())){
			valueText.setText( "") ;
		}
		else{
			valueText.setText( DATEFORMAT.format( date)) ;
		}
	}
	
	/**-------------------------------------------------------------------------
	 * 画面表示時処理を行います。
	 *------------------------------------------------------------------------*/
	@Override
	protected void onBindView( View view){
		super.onBindView( view) ;
		
		// 設定値表示
		valueText = ( TextView)view.findViewById( R.id.value) ;
		updateSummary() ;
		
		// 有効無効で表示色を設定
		int color = isEnabled() ? AplUtil.PREF_VALUE_COLOR : Color.LTGRAY ;
		valueText.setTextColor( color) ;
	}
	
	/**-------------------------------------------------------------------------
	 * ダイアログ作成処理を行います。
	 *------------------------------------------------------------------------*/
	@Override
	protected View onCreateDialogView(){
		picker = new DatePicker( this.getContext()) ;
		Date date = getDate();
		if( date == null){
			date = new Date();
		}
		calendar.setTime( date) ;
		picker.init( calendar.get( Calendar.YEAR), 
				calendar.get( Calendar.MONTH), 
				calendar.get( Calendar.DAY_OF_MONTH),
				new OnDateChangedListener(){
			public void onDateChanged( DatePicker view, int year, int monthOfYear, int dayOfMonth){
				
			}
		}) ;
		return picker ;
	}

	/**-------------------------------------------------------------------------
	 * ダイアログ閉時処理を行います。
	 *------------------------------------------------------------------------*/
	@Override
	protected void onDialogClosed( boolean positiveResult){
		if( positiveResult){
			calendar.set( Calendar.YEAR, picker.getYear()) ;
			calendar.set( Calendar.MONTH, picker.getMonth()) ;
			calendar.set( Calendar.DAY_OF_MONTH, picker.getDayOfMonth()) ;
			persistString( String.valueOf( calendar.getTime().getTime())) ;
			updateSummary() ;
			DbUtil.updatePregWeeks( calendar.getTime()) ;						// 妊娠週数情報を更新
		}
		super.onDialogClosed( positiveResult) ;
	}
	
	/**-------------------------------------------------------------------------<br>
	 * 設定された日付を取得します。<br>
	 *-------------------------------------------------------------------------*/
	private Date getDate(){
//		return new Date( Long.valueOf( this.getPersistedString( "0"))) ;
		return DbUtil.getPregTerm() ;
	}
}

