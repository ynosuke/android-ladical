package com.ynosuke.android.ladical.calendar;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.AlertDialog.Builder;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ynosuke.android.ladical.R;
import com.ynosuke.android.ladical.data.Holiday;
import com.ynosuke.android.ladical.data.Item;
import com.ynosuke.android.ladical.input.InputActivity;
import com.ynosuke.android.ladical.util.AplUtil;
import com.ynosuke.android.ladical.util.IGlobalImages;
import com.ynosuke.android.ladical.util.IMode;
import com.ynosuke.android.ladical.util.PrefUtil;

//------------------------------------------------------------------------------
/**
* カレンダー画面です。
*
* <p>更新履歴：
* <pre>
*     VerNo.        author              update      comment
*     Ver1.00.00    Yoshinosuke Nagaya  2013/01/05  新規作成
*     Ver1.00.02						2014/12/24	表示モードを記憶するよう対応
* </pre>
* </p>
* @author Yoshinosuke Nagaya
*/
//------------------------------------------------------------------------------
@SuppressLint("NewApi")
public class CalendarFragment extends Fragment implements IGlobalImages, IMode, OnClickListener{
	//	定数定義 ----------------------------------------------------------------	
	/** 詳細パラメータ表示数 */
	private static final int	PARAM_SIZE = 6 ;
	
	//	内部定義 ----------------------------------------------------------------
	/** 設定値管理 */
	public PrefUtil				pref = PrefUtil.getInstance( getActivity()) ;;
	
	/** タイトルテキスト（年月表示用） */
	private TextView			titleText ;
	
	/** タイトル用日付フォーマット */
	private DateFormat			dateFormat ;
	
	/** 詳細部の日付表示フォーマット */
	private DateFormat			detailFormat ;

	/** カレンダー表示ビュー */
	private CalendarView		calendarView ;
	
	/** 日付計算用カレンダーインスタンス */
	private Calendar			cal ;
	
	/** 詳細ビューパラメータマーク表示 */
	private ImageView[]			paramMark = new ImageView[PARAM_SIZE] ;
	
	/** 詳細ビューパラメータ名称表示 */
	private TextView[]			paramName = new TextView[PARAM_SIZE] ;
	
	/** 詳細ビュー日付表示 */
	private TextView			dateView ;
	
	/** 詳細ビューメモ表示 */
	private TextView			memoView ;
	
	/** 先月表示ボタン */
	private TextView			prevButton ;
	
	/** 翌月表示ボタン */
	private TextView			nextButton ;
	
	/** ヘッダービュー */
	private View 				headerView ;
	
	/** メインビュー */
	private View				mainView ;
	
	/**-------------------------------------------------------------------------
	 * フラグメント作成時処理を行います。
	 *------------------------------------------------------------------------*/
	@Override
	public void onCreate( Bundle savedInstanceState){
		super.onCreate( savedInstanceState) ;
		setHasOptionsMenu( true) ;
		setRetainInstance( true);
		
		// 日付類初期化
		cal = Calendar.getInstance() ;
		cal.setTime( new Date()) ;
		cal.set( Calendar.HOUR_OF_DAY, 0) ;
		cal.set( Calendar.MINUTE, 0) ;
		cal.set( Calendar.SECOND, 0) ;
		cal.set( Calendar.MILLISECOND, 0) ;
		
		// 日付フォーマット初期化
		if( Locale.getDefault().equals( Locale.JAPAN)){
			dateFormat = new SimpleDateFormat( "yyyy年 M月", Locale.JAPAN) ;
			detailFormat = new SimpleDateFormat( "M月d日（E）", Locale.JAPAN) ;
		}
		else{
			dateFormat = new SimpleDateFormat( "MMMM yyyy", Locale.US) ;
			detailFormat = new SimpleDateFormat( "E, MMM. d", Locale.US) ;
		}
	}

	/**-------------------------------------------------------------------------
	 * アクティビティー作成時処理を行います。
	 * 
	 * @param savedInstanceState
	 *------------------------------------------------------------------------*/
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate( R.layout.fragment_calendar, container, false) ;

		// 各ビュー取得
		titleText = ( TextView)view.findViewById( R.id.titleView) ;
		calendarView = ( CalendarView)view.findViewById( R.id.calendarView) ;
		calendarView.mode = pref.getInt( R.string.pref_displaymode) ;
		
		paramMark[0] = ( ImageView)view.findViewById( R.id.detailParamMark0) ;
		paramMark[1] = ( ImageView)view.findViewById( R.id.detailParamMark1) ;
		paramMark[2] = ( ImageView)view.findViewById( R.id.detailParamMark2) ;
		paramMark[3] = ( ImageView)view.findViewById( R.id.detailParamMark3) ;
		paramMark[4] = ( ImageView)view.findViewById( R.id.detailParamMark4) ;
		paramMark[5] = ( ImageView)view.findViewById( R.id.detailParamMark5) ;
		paramName[0] = ( TextView)view.findViewById( R.id.detailParamName0) ;
		paramName[1] = ( TextView)view.findViewById( R.id.detailParamName1) ;
		paramName[2] = ( TextView)view.findViewById( R.id.detailParamName2) ;
		paramName[3] = ( TextView)view.findViewById( R.id.detailParamName3) ;
		paramName[4] = ( TextView)view.findViewById( R.id.detailParamName4) ;
		paramName[5] = ( TextView)view.findViewById( R.id.detailParamName5) ;
		dateView = ( TextView)view.findViewById( R.id.detailDate) ;
		memoView = ( TextView)view.findViewById( R.id.detailMemo) ;
		prevButton = ( TextView)view.findViewById( R.id.prevButton) ;
		nextButton = ( TextView)view.findViewById( R.id.nextButton) ;
		
		// ボタンにリスナー設定
		prevButton.setOnClickListener( this) ;
		nextButton.setOnClickListener( this) ;
		

		// 曜日表示
		int weekStart = pref.getInt( R.string.pref_week_start) ;
		TextView[] week = new TextView[7] ;
		week[0] = ( TextView)view.findViewById( R.id.week1) ;
		week[1] = ( TextView)view.findViewById( R.id.week2) ;
		week[2] = ( TextView)view.findViewById( R.id.week3) ;
		week[3] = ( TextView)view.findViewById( R.id.week4) ;
		week[4] = ( TextView)view.findViewById( R.id.week5) ;
		week[5] = ( TextView)view.findViewById( R.id.week6) ;
		week[6] = ( TextView)view.findViewById( R.id.week7) ;
		int[] weekString = new int[]{ R.string.sun, R.string.mon, R.string.tue, R.string.wed, R.string.thu, R.string.fri, R.string.sat} ;
		for( int i = 0; i < 7; i++){
			int index = i + weekStart ;
			if( index >= 7){
				index -= 7 ;
			}
			week[i].setText( getString( weekString[ index])) ;
		}
		
		calendarView.setDate( cal.getTime()) ;
		updateTitle() ;
		
		// 背景色設定
		headerView = view.findViewById( R.id.headerView) ;
		AplUtil.setHeaderBackground( headerView) ;
		mainView = view.findViewById( R.id.mainView) ;
		AplUtil.setViewBackground( mainView) ;

		return view ;
	}
	
	/**-------------------------------------------------------------------------
	* メニュー作成処理を行います。
	*-------------------------------------------------------------------------*/
	@Override
	public void onCreateOptionsMenu( Menu menu, MenuInflater inflater){
		menu.clear() ;
		boolean wrEnabled = pref.getBoolean( R.string.pref_weightratio_enabled) ;
		boolean pregEnabled = pref.getBoolean( R.string.pref_show_preg) ;
		if( wrEnabled | pregEnabled){
			inflater.inflate( wrEnabled ? R.menu.calendar_menu : R.menu.calendar_menu_simple, menu) ;
		}
		else{
			inflater.inflate( R.menu.calendar_menu_none, menu);
		}
	}
	
	/**-------------------------------------------------------------------------
	* 再表示時時処理を行います。
	*-------------------------------------------------------------------------*/
	@Override
	public void onResume(){
		super.onResume() ;
		calendarView.update() ;													// 表示更新
	}
	
	/**-------------------------------------------------------------------------
	* メニュー選択時処理を行います。
	*-------------------------------------------------------------------------*/
	@Override
	public boolean onOptionsItemSelected( MenuItem item){
		// 表示モード設定
		int itemId = item.getItemId();
		if( itemId == R.id.menu_temp){
			calendarView.mode = TEMP ;
		} 
		else if( itemId == R.id.menu_weight){
			calendarView.mode = WEIGHT ;
		} 
		else if( itemId == R.id.menu_ratio){
			calendarView.mode = RATIO ;
		} 
		else if( itemId == R.id.menu_weeks){
			calendarView.mode = WEEKS ;
		}
		else if( itemId == R.id.menu_help){
			Builder builder = new Builder( getActivity()) ;
			builder.setTitle( getString( R.string.help_calendar_title)) ;
			builder.setMessage( getString( R.string.help_calendar)) ;
			builder.setNegativeButton( getString( R.string.close), null) ;
			builder.show() ;
			return true ;
		}
		pref.putPref( R.string.pref_displaymode, calendarView.mode);			// 表示モード保存
		calendarView.invalidate() ;												// カレンダー再描画
		return true ;
	}
	
	/**-------------------------------------------------------------------------
	* メニュー表示前処理を行います。
	*-------------------------------------------------------------------------*/
	@Override
	public void onPrepareOptionsMenu( Menu menu){
		MenuItem tempItem = menu.findItem( R.id.menu_temp) ;
		if( tempItem != null) tempItem.setEnabled( calendarView.mode != TEMP) ;
		
		MenuItem weightItem = menu.findItem( R.id.menu_weight) ;
		if( weightItem != null) weightItem.setEnabled( calendarView.mode != WEIGHT) ;
		
		MenuItem ratioItem = menu.findItem( R.id.menu_ratio) ;
		if( ratioItem != null) ratioItem.setEnabled( calendarView.mode != RATIO) ;
		
		MenuItem weeksItem = menu.findItem( R.id.menu_weeks) ;
		if( weeksItem != null){
			weeksItem.setVisible( pref.getBoolean( R.string.pref_show_preg)) ;
			weeksItem.setEnabled( calendarView.mode != WEEKS) ;
		}
	}
	
	/**-------------------------------------------------------------------------
	 * 入力画面を表示します。
	 *------------------------------------------------------------------------*/
	public void showInputActivity( Date date){
		Intent intent = new Intent( getActivity(), InputActivity.class) ;
		intent.putExtra( "date", date.getTime()) ;
		startActivity( intent) ;
	}
	
	/**-------------------------------------------------------------------------
	 * 詳細ビューに選択内容を表示します。
	 *------------------------------------------------------------------------*/
	public void showDetail( Date date, Item item, Holiday holiday){
		// ONパラメータ表示
		int index = 0 ;
		if( item != null){
			for( int i = 0; i < item.param.length; i++){
				if( item.param[i] && index < PARAM_SIZE){
					// マーク表示
					int markNo = pref.getInt( R.string.pref_param_mark, i+1) ;
					Bitmap mark = BitmapFactory.decodeResource( getResources(), MARK_IDS[markNo]) ;
					paramMark[index].setImageBitmap( mark) ;
					
					// パラメータ名称表示
					String name = pref.getString( R.string.pref_param_name, i+1) ;
					paramName[index].setText( name) ;
					index++ ;
				}
			}
		}
		for( int i = index; i < PARAM_SIZE; i++){
			paramMark[i].setImageBitmap( null) ;
			paramName[i].setText( "") ;
		}
		
		// 日付表示
		String dateText = detailFormat.format( date) ;
		if( holiday != null && !holiday.name.equals( "")){
			dateText += holiday.name ;
		}
		dateView.setText( dateText) ;
		
		// メモ表示
		if( item != null && item.memo != null && item.memo.length() != 0){
			memoView.setText( item.memo) ;
			memoView.setBackgroundResource( R.drawable.detail_memo_background);
		}
		else{
			memoView.setText( "") ;
			memoView.setBackgroundResource( 0);
		}
	}
	
	/**-------------------------------------------------------------------------
	 * 翌月を表示します。
	 *------------------------------------------------------------------------*/
	public void showNextMonth(){
		cal.add( Calendar.MONTH, 1) ;
		calendarView.setDate( cal.getTime()) ;
		updateTitle() ;
	}
	
	/**-------------------------------------------------------------------------
	 * 前月を表示します。
	 *------------------------------------------------------------------------*/
	public void showPrevMonth(){
		cal.add( Calendar.MONTH, -1) ;
		calendarView.setDate( cal.getTime()) ;
		updateTitle() ;
	}
	
	/**-------------------------------------------------------------------------
	 * リスナーに登録されたViewがクリックされた時の処理を行います。
	 *------------------------------------------------------------------------*/
	@Override
	public void onClick( View view){
		if( view == prevButton){
			showPrevMonth() ;
		}
		else if( view == nextButton){
			showNextMonth() ;
		}
	}
	
	public void update(){
		// 背景色設定
		AplUtil.setHeaderBackground( headerView) ;
		AplUtil.setViewBackground( mainView) ;
		
		// 再描画
		calendarView.update() ;
	}
	
	/**-------------------------------------------------------------------------
	 * タイトルを更新します。
	 *------------------------------------------------------------------------*/
	private void updateTitle(){
		titleText.setText( dateFormat.format( cal.getTime())) ;
	}
}

