package com.ynosuke.android.ladical.calendar;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;

import com.ynosuke.android.ladical.MainActivity;
import com.ynosuke.android.ladical.R;
import com.ynosuke.android.ladical.data.Holiday;
import com.ynosuke.android.ladical.data.Item;
import com.ynosuke.android.ladical.input.InputActivity;
import com.ynosuke.android.ladical.util.DbUtil;
import com.ynosuke.android.ladical.util.IGlobalColors;
import com.ynosuke.android.ladical.util.IGlobalImages;
import com.ynosuke.android.ladical.util.IMode;
import com.ynosuke.android.ladical.util.PrefUtil;

//------------------------------------------------------------------------------
/**
* カレンダービューです。
*
* <p>更新履歴：
* <pre>
*     VerNo.        author              update      comment
*     Ver1.00.00    Yoshinosuke Nagaya  2013/01/06  新規作成
* </pre>
* </p>
* @author Yoshinosuke Nagaya
*/
//------------------------------------------------------------------------------
public class CalendarView extends View implements OnGestureListener, OnDoubleTapListener, IGlobalImages, IGlobalColors, IMode {
	//	内部定義 ----------------------------------------------------------------
	/** 日付フォントサイズ */
	private  float 	DATE_SIZE = 24.0f ;
	
	/** 値フォントサイズ */
	private  float 	VALUE_SIZE = 14.0f ;
	
	/** メモフォントサイズ */
	private  float 	MEMO_SIZE = 11.0f ;
	
	/** 妊娠週数フォントサイズ */
	private  float 	PREGWEEK_SIZE = 9.0f ;
	
	/** 妊娠月数フォントサイズ */
	private  float 	PREGMONTH_SIZE = 12.0f ;
	
	/** 表示モード */
	public int			mode = TEMP ;
	
	/** 設定値管理 */
	private PrefUtil	pref ;

	/** 罫線用ペイント */
	private Paint		linePaint ;
	
	/** 日付表示用ペイント */
	private Paint		datePaint ;
	
	/** 日付表示用ペイント（前後月） */
	private Paint		otherDatePaint ;
	
	/** 値表示用ペイント */
	private Paint		valuePaint ;
	
	/** パラメータマーク描画用ペイント */
	private Paint		markPaint ;
	
	/** 選択日表示用ペイント */
	private Paint		selectDayPaint ;
	
	/** 本日表示用ペイント */
	private Paint		todayPaint ;
	
	/** メモ表示用ペイント */
	private Paint		memoPaint ;
	
	/** 生理日予測ペイント */
	private Paint		willPeriodPaint ;
	
	/** 排卵日予測ペイント */
	private Paint		willOvPaint ;
	
	/** 妊娠週数文字ペイント */
	private Paint		pregWeeksPaint ;
	
	/** 表示月のDate */
	private Date		date = new Date() ;
	
	/** 日付計算用カレンダーインスタンス */
	private Calendar	cal = Calendar.getInstance() ;
	
	/** 表示月の月末日 */
	private int			lastDay ;
	
	/** 表示月の開始曜日（0が日曜） */
	private int			weekDay ;
	
	/** 選択日 */
	private Date		selectDate ;
	
	/** 表示Itemリスト */
	private Map<Date,Item> list = new HashMap<Date,Item>(0) ;
	
	/** 表示パラメータイメージ */
	private Bitmap[]	paramImage ;
	
	/** タッチ処理用 */
	private GestureDetector gestureDetector ;
	
	/** メインアクティビティー */
	private MainActivity	mainActivity ;
	
	/** 祝日リスト */
	private Map<Date,Holiday> holidayMap = new HashMap<Date,Holiday>() ;
	
	/**-------------------------------------------------------------------------
	 * インスタンス作成時の初期化処理を行います。（findViewByIdを使用する際に必要）
	 * 
	 * @param context	コンテキスト
	 *------------------------------------------------------------------------*/
	public CalendarView( Context context) {
		super( context);
		init( context) ;														// 初期化処理
	}
	
	/**-------------------------------------------------------------------------
	 * インスタンス作成時の初期化処理を行います。（xmlにてレイアウトパラメータが指定された場合に使用）
	 * 
	 * @param context	コンテキスト
	 * @param attrs		レイアウトパラメータ
	 *------------------------------------------------------------------------*/
	public CalendarView( Context context, AttributeSet attrs) {
		super( context, attrs);
		init( context) ;														// 初期化処理
	}
	
	/**-------------------------------------------------------------------------
	 * インスタンス作成時の初期化処理を行います。（xmlにてスタイルが指定された場合に使用）
	 * 
	 * @param context	コンテキスト
	 * @param attrs		レイアウトパラメータ
	 * @param defStyle	スタイル
	 *------------------------------------------------------------------------*/
	public CalendarView( Context context, AttributeSet attrs, int defStyle) {
		super( context, attrs, defStyle);
		init( context) ;														// 初期化処理
	}

	/**-------------------------------------------------------------------------
	 * 描画処理を行います。
	 * 
	 * @param canvas
	 *------------------------------------------------------------------------*/
	@SuppressLint("DefaultLocale")
	public void onDraw( Canvas canvas){
		boolean showPreg = pref.getBoolean( R.string.pref_show_preg) ;
		int rowSize = ( weekDay + lastDay - 1) / 7 + 1 ;						// 行数取得
		if( rowSize == 4) rowSize = 5;
		
		// 罫線描画
		float intervalX = (float)getWidth() / 7 ;
		for( int i = 0; i <= 7; i++){
			canvas.drawLine( intervalX*i, 0, intervalX*i, getHeight()-2, linePaint) ;
																				// 縦線描画
		}
		float intervalY = (float)( getHeight() - 2) / rowSize ;
		for( int i = 0; i <= rowSize; i++){
			canvas.drawLine( 0, intervalY*i + 1, getWidth(), intervalY*i + 1, linePaint) ;
																				// 横線描画
		}

		int thisMonth = cal.get( Calendar.MONTH) ;								// 表示月取得（0〜11）
		cal.set( Calendar.DAY_OF_MONTH, 1) ;
		cal.set( Calendar.DAY_OF_MONTH, -weekDay + 1) ;							// 1日のある週初めまで移動
		switch( mode){
			case WEIGHT : 	valuePaint.setColor( COLOR_WEIGHT) ; break ;
			case RATIO : 	valuePaint.setColor( COLOR_RATIO) ; break ;			// 値表示色設定（体温は可変色）
		}
		Date today = new Date() ;
		float memoOffset = intervalY - paramImage[0].getHeight() - 4 ;
		float valueOffset = memoOffset + memoPaint.ascent() - 2 ;
		float dateOffset = ( valueOffset + valuePaint.ascent()) / 2 + -datePaint.ascent()/2 ;
		for( int row = 0; row < rowSize; row++){
			for( int col = 0; col < 7; col++){
				// 日付描画
				int day = cal.get( Calendar.DAY_OF_MONTH) ;
				String dayText = Integer.toString( day) ;
				float x = col*intervalX + intervalX/2 - datePaint.measureText( dayText)/2 ;
				float y = row*intervalY + dateOffset ;
				datePaint.setColor( holidayMap.containsKey( cal.getTime()) ? Color.RED : Color.BLACK) ;
				datePaint.setAlpha( cal.get( Calendar.MONTH) == thisMonth ? 0xFF : 0x60) ;
				canvas.drawText( dayText, x, y, datePaint) ;
				
				Item item = list.get( cal.getTime()) ;							// この日付のItemを取得
				if( item != null){
					// 値描画
					float value = 0 ;
					switch( mode){
						case TEMP : 	value = item.temp ; break ;
						case WEIGHT : 	value = item.weight ; break ;
						case RATIO : 	value = item.ratio ; break ;
					}
					if( value != 0){
						if( mode == TEMP){
							setTempColor( value) ;
						}
						String valueText = String.format( "%.2f", value) ;
						x = col*intervalX + intervalX/2 - valuePaint.measureText( valueText)/2 ;
						y = row*intervalY + valueOffset ;
						canvas.drawText( valueText, x, y, valuePaint) ;
					}
					// メモ描画
					boolean memoExist = item.memo != null && item.memo.length() != 0 ;
					y = row*intervalY + memoOffset ;
					if( memoExist){
						int displayLength = memoPaint.breakText( item.memo, true, intervalX-4, null) ;
						String displayText = item.memo.substring( 0, displayLength) ;
																				// 1行で表示できるテキストを取得
						x = col*intervalX + 2 ;
						memoPaint.setColor( Color.DKGRAY) ;
						canvas.drawText( displayText, x, y, memoPaint) ;
					}
					// 生理予測日描画
					if( item.willPeriod){
						Rect rect = new Rect(
								(int)( col*intervalX + 1),
								(int)(( row + 1)*intervalY - 25),
								(int)(( col + 1)*intervalX),
								(int)(( row + 1)*intervalY) - 2) ;
						canvas.drawRect( rect, willPeriodPaint) ;
						if( !memoExist){
							Date prevDate = new Date( cal.getTimeInMillis() - DbUtil.ONEDAY) ;
							Item prevItem = list.get( prevDate) ;
							if( prevItem == null || !prevItem.willPeriod){
								String text = getContext().getString( R.string.willPeriod) ;
								x = col == 6 ? getWidth() - memoPaint.measureText( text) - 2 : col*intervalX + 2;
								memoPaint.setColor( COLOR_WILLPERIOD) ;
								canvas.drawText( text, x, y, memoPaint) ;
							}
						}
					}
					// 排卵予測日描画
					if( item.willOv){
						Rect rect = new Rect(
								(int)( col*intervalX + 1),
								(int)(( row + 1)*intervalY - 25),
								(int)(( col + 1)*intervalX),
								(int)(( row + 1)*intervalY) - 2) ;
						canvas.drawRect( rect, willOvPaint) ;
						if( !memoExist){
							String text = getContext().getString( R.string.willOv) ;
							x = col == 6 ? getWidth() - memoPaint.measureText( text) - 2 : col*intervalX + 2;
							memoPaint.setColor( COLOR_WILLOV) ;
							canvas.drawText( text, x, y, memoPaint) ;
						}
					}
					
					// パラメータ描画
					int pos = 0 ;
					for( int i = 0; i < 10; i++){
						Bitmap image = paramImage[i] ;
						if( item.param[i] == true && image != null){
							x = col*intervalX + 2 + pos*image.getWidth() ;
							y = row*intervalY + intervalY - 2 - image.getHeight() ;
							canvas.drawBitmap( image, x, y, markPaint) ;
							pos++ ;
						}
					}
					
					if( showPreg){	// 妊娠モードの場合
						if( item.pregWeeks == Item.TERM){
							// 出産予定日描画
							Bitmap mark = BitmapFactory.decodeResource( getResources(), R.mipmap.mark_heart) ;
							x = col*intervalX + 4 + 20 ;
							y = row*intervalY + valueOffset ;
							canvas.drawBitmap( mark, x, y, markPaint) ;
							
							pregWeeksPaint.setColor( Color.BLACK) ;
							pregWeeksPaint.setTextSize( PREGMONTH_SIZE) ;
							String text = getContext().getString( R.string.term) ;
							canvas.drawText( text, x, y, pregWeeksPaint) ;
						}
						else if( item.pregWeeks > 0 && value == 0){
							// 妊娠週数表示
							x = col*intervalX + 4 ;
							y = row*intervalY + valueOffset ;
							pregWeeksPaint.setColor((item.pregWeeks-1)/4 %2 != 0 ? COLOR_PREG1 : COLOR_PREG2) ;
							if((item.pregWeeks-1) % 4 == 0){
								String text = getContext().getString( R.string.pregMonths, (item.pregWeeks-1)/4 + 1) ;
								pregWeeksPaint.setTextSize( PREGMONTH_SIZE) ;
								canvas.drawText( text, x, y, pregWeeksPaint) ;	// 月数表示
							}
							else{
								String text = getContext().getString( R.string.pregWeeks, item.pregWeeks-1) ;
								pregWeeksPaint.setTextSize( PREGWEEK_SIZE) ;
								canvas.drawText( text, x, y, pregWeeksPaint) ;	// 週数表示
							}
						}
					}
				}
				
				// 本日の表示
				if( today.after( cal.getTime()) && today.getTime() - cal.getTimeInMillis() < DbUtil.ONEDAY){
					Rect rect = new Rect(
							(int)( col*intervalX) + 1,
							(int)( row*intervalY) + 1,
							(int)(( col + 1)*intervalX),
							(int)(( row + 1)*intervalY) - 1) ;
					canvas.drawRect( rect, todayPaint) ;
				}

				// 選択日表示
				if( selectDate != null && cal.getTime().equals( selectDate)){
					Rect rect = new Rect(
							(int)( col*intervalX) + 1,
							(int)( row*intervalY) + 1,
							(int)(( col + 1)*intervalX) - 2,
							(int)(( row + 1)*intervalY) - 2) ;
					canvas.drawRect( rect, selectDayPaint) ;
				}

				cal.add( Calendar.DAY_OF_MONTH, 1) ;							// カレンダーを1日進める
			}
		}
		// カレンダーを月始めに戻す
		cal.setTime( date) ;
		cal.set( Calendar.DAY_OF_MONTH, 1) ;
	}
	
	/**-------------------------------------------------------------------------
	 * タッチ時イベント処理を行います。
	 * 
	 * @param event	タッチイベント
	 *------------------------------------------------------------------------*/
	@Override
	public boolean onTouchEvent( MotionEvent event){
//		if( gestureDetector.onTouchEvent( event)){		
//			return true;
//		}
//      return super.onTouchEvent(event);
		gestureDetector.onTouchEvent( event) ;
		return true ;
	}
	
	/**------------------------------------------------------------------------
	 * カレンダーに表示する日付を設定します。
	 * 
	 * @param date	日付
	 *------------------------------------------------------------------------*/
	public void setDate( Date date){
		this.date = date ;
		cal.setTime( date) ;

		lastDay = cal.getActualMaximum( Calendar.DATE) ;						// 月末日取得
		cal.set( Calendar.DAY_OF_MONTH, 1) ;									// カレンダーを月初めに設定
		weekDay = cal.get( Calendar.DAY_OF_WEEK) - 1 ;							// 1日の曜日取得(日曜が1なので-1して0とする)
		weekDay -= pref.getInt( R.string.pref_week_start) ;
		if( weekDay < 0){
			weekDay += 7 ;
		}
		cal.setTime( date) ;
		selectDate = null ;
		update() ;																// 表示更新
	}
	
	/**-------------------------------------------------------------------------
	 * 表示内容を更新します。
	 *------------------------------------------------------------------------*/
	public void update(){
		// リスト更新（表示月より前後6日分余分に取得している）
		cal.set( Calendar.DATE, 1) ;
		cal.add( Calendar.DATE, -6) ;
		Date start = cal.getTime() ;											// 開始日時取得
		cal.add( Calendar.DATE, 12) ;
		cal.add( Calendar.DATE, cal.getActualMaximum( Calendar.DATE) - 1) ;
		Date end = cal.getTime() ;												// 終了日時取得
		list = DbUtil.getItemList( start, end) ;								// リスト更新
		cal.setTime( date) ;
		
		// 表示パラメータ更新
		paramImage = new Bitmap[10] ;
		for( int i = 0; i < paramImage.length; i++){
			boolean display = pref.getBoolean( R.string.pref_param_display, i+1) ;
			if( display){
				int markNo = pref.getInt( R.string.pref_param_mark, i+1) ;
				paramImage[i] = BitmapFactory.decodeResource( getResources(), MARK_IDS[markNo]) ;
			}
		}
		
		// 祝日リスト更新
		holidayMap.clear() ;
		List<Holiday> holidayList = DbUtil.getHolidayList( start, end) ;
		for( Holiday holiday : holidayList){
			holidayMap.put( holiday.date, holiday) ;
		}
		
		invalidate() ;															// 再描画
	}

	/**-------------------------------------------------------------------------
	 * タッチダウン時処理を行います。
	 * 
	 * @param event	タッチイベント
	 *------------------------------------------------------------------------*/
	@Override
	public boolean onDown( MotionEvent event) {
		// 選択された日付を取得
		float xInterval = getWidth() / 7 ;
		int rowSize = ( weekDay + lastDay - 1) / 7 + 1 ;
		if( rowSize == 4) rowSize = 5;
		float yInterval = getHeight()/rowSize ;
					
		int col = ( int)Math.floor( event.getX() / xInterval) ;
		int row = ( int)Math.floor( event.getY() / yInterval) ;
		int selectDayOffset = col - weekDay + 1 + row * 7 - 1 ;
		cal.add( Calendar.DATE, selectDayOffset) ;
		selectDate = cal.getTime() ;
		cal.add( Calendar.DATE, -selectDayOffset) ;
		invalidate() ;															// 再描画
		
		// 詳細ビューに選択日の内容を表示
		Item item = list.get( selectDate) ;										// 選択日のItem取得
		Holiday holiday = holidayMap.get( selectDate) ;							// 選択日の祝日取得
		mainActivity.getCalendarFragment().showDetail( selectDate, item, holiday) ;// 詳細ビューに表示
		return false ;
	}

	/**-------------------------------------------------------------------------
	 * フリック時処理を行います。
	 *------------------------------------------------------------------------*/
	@Override
	public boolean onFling( MotionEvent event1, MotionEvent event2, float velocityX, float velocityY) {
//		int moveY = ( int)( event1.getY() - event2.getY()) ;
//		if( Math.abs( moveY) > 50){
//			if( moveY > 0){
//				mainActivity.showNextMonth() ;
//			}
//			else{
//				mainActivity.showPrevMonth() ;
//			}
//		}
		return false;
	}

	/**-------------------------------------------------------------------------
	 * 長押し時処理を行います。
	 *------------------------------------------------------------------------*/
	@Override
	public void onLongPress( MotionEvent arg0) {
		showInputActivity() ;													// 入力画面アクティビティー表示
	}

	/**-------------------------------------------------------------------------
	 * スクロール時処理を行います。
	 *------------------------------------------------------------------------*/
	@Override
	public boolean onScroll( MotionEvent arg0, MotionEvent arg1, float arg2, float arg3) {
		return false;
	}

	@Override
	public void onShowPress( MotionEvent arg0) {
	}

	@Override
	public boolean onSingleTapUp( MotionEvent arg0) {
		return false;
	}

	/**-------------------------------------------------------------------------
	 * ダブルタップ時処理を行います。
	 *------------------------------------------------------------------------*/
	@Override
	public boolean onDoubleTap( MotionEvent arg0) {
		showInputActivity() ;													// 入力画面アクティビティー表示
		return false;
	}

	/**-------------------------------------------------------------------------
	 * ダブルタップイベント時処理を行います。
	 *------------------------------------------------------------------------*/
	@Override
	public boolean onDoubleTapEvent( MotionEvent arg0) {
		return false;
	}

	/**-------------------------------------------------------------------------
	 * タップ完了時処理を行います。
	 *------------------------------------------------------------------------*/
	@Override
	public boolean onSingleTapConfirmed( MotionEvent arg0) {
		return false;
	}
	
	/**-------------------------------------------------------------------------
	 * 初期化処理を行います。
	 *------------------------------------------------------------------------*/
	private void init( Context context){
		mainActivity = ( MainActivity)context ;
		pref = PrefUtil.getInstance( context) ;
		gestureDetector = new GestureDetector( context, this) ;
		
		// フォントサイズ設定
		float density = getResources().getDisplayMetrics().density ;
		DATE_SIZE = DATE_SIZE * density ;
		VALUE_SIZE = VALUE_SIZE * density ;
		MEMO_SIZE = MEMO_SIZE * density ;
		PREGWEEK_SIZE = PREGWEEK_SIZE * density ;
		PREGMONTH_SIZE = PREGMONTH_SIZE * density ;
		
		// 罫線ペイント作成
		linePaint = new Paint() ;
		linePaint.setColor( Color.GRAY) ;
		linePaint.setStrokeWidth( 2.0f) ;

		// 選択日表示用ペイント作成
		selectDayPaint = new Paint() ;
		selectDayPaint.setColor( Color.rgb( 0x30, 0x8B, 0xF0)) ;
		selectDayPaint.setStrokeWidth( 3.0f) ;
		selectDayPaint.setStyle( Paint.Style.STROKE) ;
		
		// 本日表示用ペイント作成
		todayPaint = new Paint( selectDayPaint) ;
		todayPaint.setColor( Color.rgb( 0xFF, 0x8C, 0x00)) ;
				
		// 日付表示用ペイント作成
		datePaint = new Paint() ;
		datePaint.setTextSize( DATE_SIZE) ;
		datePaint.setAntiAlias( true) ;
		datePaint.setColor( Color.BLACK) ;
		datePaint.setTypeface( Typeface.DEFAULT) ;
//		Typeface typeface = Typeface.createFromAsset( getContext().getAssets(), "Helvetica.ttf") ;
//		datePaint.setTypeface( typeface) ;
		
		// 日付表示用ペイント作成
		otherDatePaint = new Paint( datePaint) ;
		otherDatePaint.setColor( Color.GRAY) ;
		
		// 値表示用ペイント作成
		valuePaint = new Paint( datePaint) ;
		valuePaint.setTextSize( VALUE_SIZE) ;
		valuePaint.setColor( Color.DKGRAY) ;
		
		// メモ表示用ペイント作成
		memoPaint = new Paint() ;
		memoPaint.setAntiAlias( true) ;
		memoPaint.setColor( Color.DKGRAY) ;
		memoPaint.setTextSize( MEMO_SIZE) ;
		
		// 生理日予測ペイント作成
		willPeriodPaint = new Paint() ;
		willPeriodPaint.setStyle( Paint.Style.FILL) ;
		willPeriodPaint.setColor( COLOR_WILLPERIOD) ;
		willPeriodPaint.setAlpha( 0x30) ;
		
		// 排卵日予測ペイント作成
		willOvPaint = new Paint( willPeriodPaint) ;
		willOvPaint.setColor( COLOR_WILLOV) ;
		willOvPaint.setAlpha( 0x30) ;
		
		// マーク描画用ペイント
		markPaint = new Paint() ;
		markPaint.setAntiAlias( true) ;
		
		// 妊娠週数文字ペイント作成
		pregWeeksPaint = new Paint( memoPaint) ;
	}
	
	/**-------------------------------------------------------------------------
	 * 入力画面アクティビティーを表示します。
	 *------------------------------------------------------------------------*/
	private void showInputActivity(){
		Intent intent = new Intent( mainActivity, InputActivity.class) ;
		intent.putExtra( "date", selectDate.getTime()) ;
		mainActivity.startActivity( intent) ;
	}
	
	/**-------------------------------------------------------------------------
	 * 体温値の表示色を設定します。
	 *------------------------------------------------------------------------*/
	private void setTempColor( float temp){
		float min = pref.getFloat( R.string.pref_temp_min) ;
		float max = pref.getFloat( R.string.pref_temp_max) ;
		// Hは240〜360度
		int h = 240 ;	// 色相
		int s = 0xA0 ;	// 彩度
		int v = 0xC0 ;	// 明度
		if( temp <= min){
			h = 240 ;
		}
		else if( temp >= max){
			h = 360 ;
		}
		else{
			float ratio = 120 / (max - min) ;
			h = 240 + ( int)( ratio * ( temp - min)) ;
		}
		int[] rgb = HSVtoRGB( h, s, v) ;
		valuePaint.setColor( Color.rgb( rgb[0], rgb[1], rgb[2])) ;
	}
	
	/**-------------------------------------------------------------------------
	 * HSV値をRGB値に変換します。
	 *------------------------------------------------------------------------*/
	private int[] HSVtoRGB(int h, int s, int v){
      float f;
      int i, p, q, t;
      int[] rgb = new int[3];
      
      i = (int)Math.floor(h / 60.0f) % 6;
      f = (float)(h / 60.0f) - (float)Math.floor(h / 60.0f);
      p = (int)Math.round(v * (1.0f - (s / 255.0f)));
      q = (int)Math.round(v * (1.0f - (s / 255.0f) * f));
      t = (int)Math.round(v * (1.0f - (s / 255.0f) * (1.0f - f)));
      
      switch(i){
          case 0 : rgb[0] = v; rgb[1] = t; rgb[2] = p; break;
          case 1 : rgb[0] = q; rgb[1] = v; rgb[2] = p; break;
          case 2 : rgb[0] = p; rgb[1] = v; rgb[2] = t; break;
          case 3 : rgb[0] = p; rgb[1] = q; rgb[2] = v; break;
          case 4 : rgb[0] = t; rgb[1] = p; rgb[2] = v; break;
          case 5 : rgb[0] = v; rgb[1] = p; rgb[2] = q; break;
      }

      return rgb;
  }
}

