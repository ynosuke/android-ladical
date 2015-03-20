package com.ynosuke.android.ladical.setting;

import java.util.Calendar;
import java.util.Date;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.ynosuke.android.ladical.R;
import com.ynosuke.android.ladical.util.DbUtil;
import com.ynosuke.android.ladical.util.PrefUtil;

//------------------------------------------------------------------------------
/**
* 祝日追加画面アクティビティーです。
*
* <p>更新履歴：
* <pre>
*     VerNo.        author              update      comment
*     Ver1.00.00    Yoshinosuke Nagaya  2014/03/02  新規作成
* </pre>
* </p>
* @author Yoshinosuke Nagaya
*/
@SuppressLint("NewApi")
//------------------------------------------------------------------------------
public class HolidayAddActivity extends Activity{
	
	/** 月選択スピナー */
	private Spinner 	monthSpinner ;
	
	/** 日選択スピナー */
	private Spinner		daySpinner ;
	
	/** 設定値管理 */
	private PrefUtil	pref ;
	
	/** カレンダー */
	private Calendar	cal = Calendar.getInstance() ;

	/**-------------------------------------------------------------------------
	 * アクティビティー作成時処理を行います。
	 *------------------------------------------------------------------------*/
	@Override
	public void onCreate( Bundle savedInstanceState){
		super.onCreate( savedInstanceState) ;
		setTitle( getString( R.string.holiday_add_title)) ;
		setContentView( R.layout.activity_holidayadd) ;
		pref = PrefUtil.getInstance( this) ;

		// カレンダーに年設定
		int year = getIntent().getIntExtra( "year", 0) ;
		cal.set( Calendar.YEAR, year) ;
		
		// 月選択スピナー設定
		monthSpinner = ( Spinner)findViewById( R.id.month_spinner) ;
		ArrayAdapter<String> adapter = new ArrayAdapter<String>( this, R.layout.row_spinner) ;
		adapter.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item) ;
		for( int i = 0; i < 12; i++){
			cal.set( Calendar.MONTH, i);
			adapter.add( DateFormat.format( "MM", cal).toString()) ;
		}
		monthSpinner.setAdapter( adapter) ;
		monthSpinner.setOnItemSelectedListener( new OnItemSelectedListener() {
			public void onItemSelected( AdapterView<?> parent, View view, int pos, long id){
				setDaySpinner() ;											// 日付スピナー更新
			}
			public void onNothingSelected( AdapterView<?> arg0) {
			}
		}) ;
		int month = pref.getInt( R.string.pref_holiday_month) ;
		monthSpinner.setSelection( month) ;
		
		// 日選択スピナー設定
		daySpinner = ( Spinner)findViewById( R.id.day_spinner) ;
		setDaySpinner() ;
		int day = pref.getInt( R.string.pref_holiday_day) ;
		monthSpinner.setSelection( month) ;
	}
	
	/**-------------------------------------------------------------------------
	 * データを登録します。
	 *------------------------------------------------------------------------*/
	public void submit( View view){
		Spinner monthSpinner = ( Spinner)findViewById( R.id.month_spinner) ;
		Spinner daySpinner = ( Spinner)findViewById( R.id.day_spinner) ;
		int year = cal.get( Calendar.YEAR) ;
		int month = monthSpinner.getSelectedItemPosition() ;
		int day = daySpinner.getSelectedItemPosition() + 1 ;
		Calendar cal = Calendar.getInstance() ;
		cal.set( Calendar.YEAR, year) ;
		cal.set( Calendar.MONTH, month) ;
		cal.set( Calendar.DATE, day) ;
		Date date = cal.getTime() ;												// 日付取得
		
		TextView nameView = ( TextView)findViewById( R.id.name) ;
		String name = nameView.getText().toString() ;							// 名称取得
		
		DbUtil.setHoliday( date, name) ;										// 登録
		
		pref.putPref( R.string.pref_holiday_month, month) ;
		pref.putPref( R.string.pref_holiday_day, day) ;							// 登録日を次回表示するよう保存
		
		finish() ;																// 画面閉じる
	}
	
	/**-------------------------------------------------------------------------
	 * 日付選択スピナーを設定します。
	 *------------------------------------------------------------------------*/
	private void setDaySpinner(){
		int month = monthSpinner.getSelectedItemPosition() ;
		cal.set( Calendar.MONTH, month) ;
		int dayMax = cal.getActualMaximum( Calendar.DATE) ;
		
		ArrayAdapter<String> adapter = new ArrayAdapter<String>( this, R.layout.row_spinner) ;
		adapter.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item) ;
		for( int i =1; i <= dayMax; i++){
			cal.set( Calendar.DATE, i);
			adapter.add( DateFormat.format( "dd", cal).toString()) ;
		}
		daySpinner.setAdapter( adapter) ;
	}
}

