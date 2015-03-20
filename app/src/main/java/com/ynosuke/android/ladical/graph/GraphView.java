package com.ynosuke.android.ladical.graph;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

import com.ynosuke.android.ladical.R;
import com.ynosuke.android.ladical.data.Item;
import com.ynosuke.android.ladical.util.DbUtil;
import com.ynosuke.android.ladical.util.IGlobalColors;
import com.ynosuke.android.ladical.util.IGlobalImages;
import com.ynosuke.android.ladical.util.IMode;
import com.ynosuke.android.ladical.util.PrefUtil;

//------------------------------------------------------------------------------
/**
* グラフビューです。
*
* <p>更新履歴：
* <pre>
*     VerNo.        author              update      comment
*     Ver1.00.00    Yoshinosuke Nagaya  2013/08/14  新規作成
*     Ver1.00.03						2015/02/27	目盛り線の修正
* </pre>
* </p>
* @author Yoshinosuke Nagaya
*/
//------------------------------------------------------------------------------
public class GraphView extends View implements IGlobalImages, IGlobalColors, IMode {
	//	定数定義 ----------------------------------------------------------------
	/** 日付フォントサイズ */
	private float 	DATE_SIZE = 12.0f ;
	
	/** 周期フォントサイズ */
	private float 	CYCLE_SIZE = 10.0f ;
	
	/** 妊娠週数フォントサイズ */
	private float 	PREGWEEK_SIZE = 10.0f ;
	
	/** 妊娠月数フォントサイズ */
	private float 	PREGMONTH_SIZE = 12.0f ;
	
	/** 一度に表示する日数 */
	public static final int		DAY_LENGTH = 90 ;
	
	/** パラメータ・日付行の高さ */
	private static final int	LINE_HEIGHT = GraphAxisView.LINE_HEIGHT ;
	
	/** 値の○の半径 */
	private static final float	CIRCLE_RADIUS = 3 ;
	
	/** シャドウ位置のオフセット */
	private static final int	SHADOW = 2 ;
	
	//	内部定義 ----------------------------------------------------------------
	/** 表示モード */
	public int			mode = TEMP ;
	
	/** 設定値管理 */
	private PrefUtil	pref ;
	
	/** 年月表示フォーマット */
	private DateFormat	dateFormat ;
	
	/** 区切り線ペイント */
	private Paint		devPaint ;
	
	/** 実線ペイント */
	private Paint		solidLinePaint ;
	
	/** 太線ペイント */
	private Paint		boldLinePaint ;
	
	/** 破線ペイント */
	private Paint		dashLinePaint ;
	
	/** 値ライン（実線）ペイント */
	private Paint		valueLineSolidPaint ;
	
	/** 値ライン（破線）ペイント */
	private Paint		valueLineDashPaint ;
	
	/** 値ラインシャドウペイント */
	private Paint		valueLineShadowPaint ;
	
	/** 値丸塗りつぶし描画ペイント */
	private Paint		valueCircleFillPaint ;
	
	/** 日曜日描画ペイント */
	private Paint		sundayPaint ;
	
	/** 生理日描画ペイント */
	private Paint		periodPaint ;
	
	/** 排卵日描画ペイント */
	private Paint		ovPaint ;
	
	/** パラメータマーク描画用ペイント */
	private Paint		markPaint ;
	
	/** 日付描画ペイント */
	private Paint		dayPaint ;
	
	/** 年月描画ペイント */
	private Paint		datePaint ;
	
	/** 周期描画ペイント */
	private Paint		cyclePaint ;
	
	/** 妊娠月数文字ペイント */
	private Paint		pregMonthPaint ;
	
	/** 妊娠週数文字ペイント */
	private Paint		pregWeekPaint ;
	
	/** 妊娠週数ライン表示ペイント */
	private Paint		pregLinePaint ;
	
	/** 妊娠週数塗りつぶしペイント */
	private Paint		pregFillPaint ;
	
	/** カレンダー */
	private Calendar	cal = Calendar.getInstance() ;
	
	/** 表示開始月日 */
	private Date		start = new Date() ;
	
	/** 表示Itemリスト */
	private Map<Date,Item> list = new HashMap<Date,Item>(0) ;
	
	/** 表示パラメータリスト */
	private List<Integer>	paramNo ;
	
	/** 表示パラメータイメージ */
	private Bitmap[]	paramImage ;
	
	/**-------------------------------------------------------------------------
	 * インスタンス作成時の初期化処理を行います。（findViewByIdを使用する際に必要）
	 * 
	 * @param context	コンテキスト
	 *------------------------------------------------------------------------*/
	public GraphView( Context context) {
		super( context);
		init( context) ;														// 初期化処理
	}
	
	/**-------------------------------------------------------------------------
	 * インスタンス作成時の初期化処理を行います。（xmlにてレイアウトパラメータが指定された場合に使用）
	 * 
	 * @param context	コンテキスト
	 * @param attrs		レイアウトパラメータ
	 *------------------------------------------------------------------------*/
	public GraphView( Context context, AttributeSet attrs) {
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
	public GraphView( Context context, AttributeSet attrs, int defStyle) {
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
		int paramSize = paramNo.size() ;
		int chartHeight = getHeight() - LINE_HEIGHT * ( paramSize + 3) - 2 ;	// チャート高さの設定
		
		// X方向描画処理
		float intervalX = getWidth() / DAY_LENGTH ;								// 1日分の幅
		cal.setTime( start) ;
		for( int i = 0; i < DAY_LENGTH; i++){
			int x = ( int)intervalX * i ;										// X座標
			int day = cal.get( Calendar.DAY_OF_MONTH) ;							// 日付取得
			
			// 日曜日を塗りつぶし
			if( cal.get( Calendar.DAY_OF_WEEK) == Calendar.SUNDAY){
				Rect rect = new Rect( x, 0, x + (int)intervalX, getHeight()-LINE_HEIGHT) ;
				canvas.drawRect( rect, sundayPaint) ;
			}
			
			// 日付描画
			switch( cal.get( Calendar.DAY_OF_WEEK)){
				case Calendar.SUNDAY :	dayPaint.setColor( Color.RED) ; break ;
				default : 				dayPaint.setColor( Color.BLACK) ;
			}
			int offset = day >= 10 ? 3 : 6 ;
			canvas.drawText( Integer.toString( day), 
					x + offset, 
					getHeight() - LINE_HEIGHT - 2, dayPaint) ;
			
			// 年月描画
			if( i == 0 || day == 1){
				String dateString = dateFormat.format( cal.getTime()) ;
				int monthDays = cal.getActualMaximum( Calendar.DATE) ;			// 月末日取得
				if( i == 0){
					monthDays = monthDays - day + 1 ;
				}
				else if( i + monthDays > DAY_LENGTH){							// 最後の月の場合
					monthDays = DAY_LENGTH - i ;
				}
				if( monthDays > 4){
					canvas.drawText( dateString, 
							x + monthDays * intervalX / 2 - 22,
							getHeight() - 2, datePaint) ;
				}
			}
			cal.add( Calendar.DAY_OF_MONTH, 1) ;								// 日付を1日進める
		}
		
		// 各区切り線描画処理
		float x0 = 0 ;
		float x1 = getWidth() ;
		float y = getHeight() - LINE_HEIGHT * 2 ;
		canvas.drawLine( 0, y, getWidth(), y, devPaint) ;						// パラメータと日付の区切り
		
		y = 1 ;
		canvas.drawLine( 0, y, getWidth(), y, devPaint) ;						// グラフ上部
		
		y = chartHeight ;
		canvas.drawLine( x0, y, x1, y, devPaint) ;								// グラフとパラメータの区切り
		
		for( int i = 0; i < paramSize; i++){
			y = chartHeight + LINE_HEIGHT * ( i + 1) ;
			canvas.drawLine( x0, y, x1, y, dashLinePaint) ;						// パラメータ間区切り
		}
		
		// 目盛り線描画処理
		float min = getMin( mode) ;
		float max = getMax( mode) ;
		float intervalY = getInterval( chartHeight, max, min) ;					// 一目盛りの値
		min -= intervalY ;
		max += intervalY ;
		float interval = chartHeight / (max - min) ;							// 1値に対するピクセル量
		int boldInterval = max - min > 10 ? 100 : 10 ;							// 太線の間隔
		float startDef = ( float)( Math.round(  min*10) % Math.round( intervalY*10))/10.0f ;
		for( float def = startDef; def < max - min; def += intervalY){
			float value = min + def ;
			y = chartHeight - interval * def ;
			boolean dash = false ;
			if( max - min < 10){
				dash = value * 10 % 10 != 0 ;
			}
			else{
				dash = value % 10 != 0 ;
			}
			Paint paint = dash ? dashLinePaint : boldLinePaint ;
			canvas.drawLine( 0, y, getWidth(), y, paint) ;
		}
		
		// パラメータ描画処理
		cal.setTime( start) ;
		float x ;
		for( int i = 0; i < DAY_LENGTH; i++){
			x = intervalX * i ;													// X座標
			Item item = list.get( cal.getTime()) ;								// Item取得
			cal.add( Calendar.DAY_OF_MONTH, 1) ;								// 日付を1日進める
			if( item == null){
				continue ;
			}
			for( int j = 0; j < paramNo.size(); j++){
				int no = paramNo.get( j) ;
				if( item.param[no]){
					y = getHeight() - LINE_HEIGHT*( 2 + paramNo.size() - j) ;
					canvas.drawBitmap( paramImage[no], x+2, y, markPaint) ;
				}
			}
		}
		
		// 生理周期描画処理
		Date nearPeriod = DbUtil.getNearPeriodDay( start) ;
		int cycle = 0 ;
		if( nearPeriod != null){
			cycle = ( int)(( start.getTime() - nearPeriod.getTime())/DbUtil.ONEDAY) ;
		}
		cyclePaint.setTextSize( cycle <= 100 ? CYCLE_SIZE : CYCLE_SIZE - 2) ;
		cal.setTime( start) ;
		for( int i = 0; i < DAY_LENGTH; i++){
			if( cycle != 0){
				cycle++ ;
			}
			x = intervalX * i ;													// X座標
			Item item = list.get( cal.getTime()) ;								// Item取得
			cal.add( Calendar.DAY_OF_MONTH, 1) ;								// 日付を1日進める
			if( item != null && item.periodStart){
				cycle = 1 ;
			}
			if( cycle == 100){
				cyclePaint.setTextSize( CYCLE_SIZE - 2) ;
			}
			if( cycle != 0){
				int offset = cycle >= 10 ? 4 : 8 ;
				canvas.drawText( Integer.toString( cycle), 
						x + offset, 
						getHeight() - LINE_HEIGHT*(paramSize+2) - 6, cyclePaint) ;
			}
		}
		
		// 生理日・排卵予測日描画処理
		cal.setTime( start) ;
		for( int i = 0; i < DAY_LENGTH; i++){
			x = intervalX * i ;													// X座標
			Item item = list.get( cal.getTime()) ;								// Item取得
			cal.add( Calendar.DAY_OF_MONTH, 1) ;								// 日付を1日進める
			if( item == null){
				continue ;
			}
			if( item.param[0] || item.willPeriod){
				Rect rect = new Rect(( int)x, 0, ( int)x + (int)intervalX, getHeight()-LINE_HEIGHT*2) ;
				canvas.drawRect( rect, periodPaint) ;							// 生理日または生理予測日の描画
			}
			if( item.willOv){
				Rect rect = new Rect(( int)x, 0, ( int)x + (int)intervalX, getHeight()-LINE_HEIGHT*2) ;
				canvas.drawRect( rect, ovPaint) ;								// 排卵予測日の描画
			}
		}
		
		// 妊娠週数の描画処理
		if( pref.getBoolean( R.string.pref_show_preg)){
			cal.setTime( start) ;
			for( int i = 0; i < DAY_LENGTH; i++){
				x = intervalX * i ;												// X座標
				Item item = list.get( cal.getTime()) ;							// Item取得
				cal.add( Calendar.DAY_OF_MONTH, 1) ;							// 日付を1日進める
				if( item == null){
					continue ;
				}
				if( item.pregWeeks > 0){
					if((item.pregWeeks-1) % 4 == 0){
						int width = ( int)( 28 * intervalX) - 6 ;
						drawPregMonthBar( canvas, item.pregWeeks, x, width) ;	// 1か月分の枠表示
						
						String text = getContext().getString( R.string.pregMonths, (item.pregWeeks-1)/4 + 1) ;
						canvas.drawText( text, x + 4, 20, pregMonthPaint) ;		// 妊娠月数表示
					}
					else{
						String text = getContext().getString( R.string.pregWeeks, item.pregWeeks-1) ;
						canvas.drawText( text, x, 20, pregWeekPaint) ;			// 妊娠週数表示
					}
				}
				else if( item.pregWeeks == Item.TERM){
					Bitmap mark = BitmapFactory.decodeResource( getResources(), R.mipmap.mark_heart) ;
					canvas.drawBitmap( mark, x, 2, markPaint) ;					// マーク表示
					
					String text = getContext().getString( R.string.term) ;
					canvas.drawText( text, x+20, 20, pregMonthPaint) ;			// 出産予定日表示
				}
			}
		}
		
		// 最初の妊娠週数表示
		if( pref.getBoolean( R.string.pref_show_preg)){
			cal.setTime( start) ;
			for( int i = 0; i < 7; i++){
				Item item = list.get( cal.getTime()) ;							// Item取得
				cal.add( Calendar.DAY_OF_MONTH, 1) ;							// 日付を1日進める
				if( item == null){
					continue ;
				}
				if( item.pregWeeks > 0){
					int week = (item.pregWeeks + 1) % 4 ;
					int daySize = week * 7 + i ;
					int width = ( int)( daySize * intervalX) - 6 ;
					drawPregMonthBar( canvas, item.pregWeeks, 0, width) ;		// xか月目の枠表示
				}
			}
		}
		
		// チャート描画
		drawChart( canvas, intervalX, chartHeight, TEMP) ;
		drawChart( canvas, intervalX, chartHeight, WEIGHT) ;
		drawChart( canvas, intervalX, chartHeight, RATIO) ;
	}
	
	/**-------------------------------------------------------------------------
	 *  グラフを描画します。
	 *------------------------------------------------------------------------*/
	private void drawChart( Canvas canvas, float intervalX, int chartHeight, int mode){
		boolean thisMode = mode == this.mode ;
		if( !pref.getBoolean( R.string.pref_show_all_chart) && !thisMode){
			return ;															// グラフ同時表示OFFかつ表示モードと異なるモードは描画しない
		}
		
		// ライン色設定
		int color = 0 ;
		switch( mode){
			case TEMP : 	color = COLOR_TEMP ; break ;
			case WEIGHT : 	color = COLOR_WEIGHT ; break ;
			case RATIO : 	color = COLOR_RATIO ; break ;
		}
		valueLineDashPaint.setColor( color) ;
		valueLineSolidPaint.setColor( color) ;
		
		// ライン透明度設定
		int alpha = thisMode ? 0xFF : 0x30 ;
		valueLineDashPaint.setAlpha( alpha) ;
		valueLineSolidPaint.setAlpha( alpha) ;
		
		// 座標リスト作成
		cal.setTime( start) ;
		float min = getMin( mode) ;
		float max = getMax( mode) ;
		float intervalY = getInterval( chartHeight, max, min) ;					// 一目盛りの値
		min -= intervalY ;
		max += intervalY ;
		float interval = chartHeight / (max - min) ;							// 1値に対するピクセル量
		List<Point> pointList = new ArrayList<Point>() ;
		for( int i = 0; i < DAY_LENGTH; i++){
			Item item = list.get( cal.getTime()) ;
			cal.add( Calendar.DAY_OF_MONTH, 1) ;
			if( item == null){
				continue ;
			}
			float value = 0 ;
			switch( mode){
				case TEMP : 	value = item.temp ; break ;
				case WEIGHT : 	value = item.weight ; break ;
				case RATIO : 	value = item.ratio ; break ;
			}
			if( value != 0){
				float x = intervalX * ( i + 0.5f) ;
				float y = chartHeight - interval * ( value - min) ;
				pointList.add( new Point( x, y)) ;
			}
		}
		
		// ライン描画
		boolean yama = false ;
		boolean showGraphBlank = pref.getBoolean( R.string.pref_show_blank_line) ;
		for( int i = 1; i < pointList.size(); i++){
			Point prev = pointList.get( i - 1) ;
			Point current = pointList.get( i) ;
			Point next = i == pointList.size() - 1 ? current : pointList.get( i + 1) ;
			
			if( current.x - prev.x != intervalX && !showGraphBlank){	// 未記入区間で非表示の設定の場合
				continue ;
			}

			float prevX = yama ? prev.x + CIRCLE_RADIUS-1 : prev.x ;
			Paint paint = current.x - prev.x != intervalX ? valueLineDashPaint : valueLineSolidPaint ;
			if(( prev.y < current.y && next.y < current.y) || ( prev.y > current.y && next.y > current.y)){
																				// カーブが山または谷の場合
				canvas.drawLine( prevX + SHADOW, prev.y, current.x - CIRCLE_RADIUS+1 + SHADOW, current.y, valueLineShadowPaint) ;
				canvas.drawLine( prevX, prev.y, current.x - CIRCLE_RADIUS+1, current.y, paint) ;
				yama = true ;
			}
			else{
				canvas.drawLine( prevX + SHADOW, prev.y, current.x + SHADOW, current.y, valueLineShadowPaint) ;
				canvas.drawLine( prevX, prev.y, current.x, current.y, paint) ;
				yama = false ;
			}
		}
		
		// 値の○描画
		for( Point point : pointList){
			canvas.drawCircle( point.x + SHADOW, point.y, CIRCLE_RADIUS, valueLineShadowPaint) ;
			canvas.drawCircle( point.x, point.y, CIRCLE_RADIUS, valueCircleFillPaint) ;
			canvas.drawCircle( point.x, point.y, CIRCLE_RADIUS, valueLineSolidPaint) ;
		}
	}
	
	/**-------------------------------------------------------------------------
	 *  Xヶ月目表示の枠を描画します。
	 *------------------------------------------------------------------------*/
	private void drawPregMonthBar( Canvas canvas, int pregWeeks, float x, int width){
		int color = (pregWeeks-1)/4 %2 != 0 ? COLOR_PREG1 : COLOR_PREG2 ;
		pregLinePaint.setColor( color) ;
		pregFillPaint.setColor( color) ;
		pregLinePaint.setAlpha( 0x80) ;
		pregFillPaint.setAlpha( 0x40) ;
		int height = ( int)pregMonthPaint.getFontMetrics( null) + 4 ;
		canvas.drawRect( x, 2, x + width, 2 + height, pregFillPaint) ;
		canvas.drawLine( x, 2, x, 2 + height + 2, pregLinePaint) ;
		canvas.drawLine( x, 2, x + width, 2, pregLinePaint) ;
		canvas.drawLine( x, 2 + height, x + width, 2 + height, pregLinePaint) ;
	}

	/**-------------------------------------------------------------------------
	 * 表示最小値を取得します。
	 *------------------------------------------------------------------------*/
	private float getMin( int mode){
		switch( mode){
			case TEMP : 	return pref.getFloat( R.string.pref_temp_min) ;
			case WEIGHT :	return pref.getFloat( R.string.pref_weight_min) ;
			case RATIO :	return pref.getFloat( R.string.pref_ratio_min) ;
			default : 		return 0 ;
		}
	}
	
	/**-------------------------------------------------------------------------
	 * 表示最大値を取得します。
	 *------------------------------------------------------------------------*/
	private float getMax( int mode){
		switch( mode){
			case TEMP : 	return pref.getFloat( R.string.pref_temp_max) ;
			case WEIGHT :	return pref.getFloat( R.string.pref_weight_max) ;
			case RATIO :	return pref.getFloat( R.string.pref_ratio_max) ;
			default : 		return 0 ;
		}
	}
	
	/**-------------------------------------------------------------------------
	 * 表示内容を更新します。
	 *------------------------------------------------------------------------*/
	public void update(){
		// リスト更新
		Date end = new Date( start.getTime() + DAY_LENGTH*DbUtil.ONEDAY) ;		// 終了日時取得
		list = DbUtil.getItemList( start, end) ;
		
		// 表示パラメータ更新
		paramImage = new Bitmap[10] ;
		paramNo = new ArrayList<Integer>() ;
		for( int i = 0; i < paramImage.length; i++){
			boolean display = pref.getBoolean( R.string.pref_param_display, i+1) ;
			if( display){
				int markNo = pref.getInt( R.string.pref_param_mark, i+1) ;
				paramImage[i] = BitmapFactory.decodeResource( getResources(), MARK_IDS[markNo]) ;
				paramNo.add( i) ;
			}
		}
		
		// 再描画
		invalidate() ;
	}
	
	/**-------------------------------------------------------------------------
	 * 表示開始日時を設定します。
	 *------------------------------------------------------------------------*/
	public void setStart( Date date){
		this.start = new Date( date.getTime() - ( DAY_LENGTH - 7)*DbUtil.ONEDAY) ;
	}
	
	/**-------------------------------------------------------------------------
	 * 目盛間隔を取得します。
	 *------------------------------------------------------------------------*/
	private float getInterval( int chartHeight, float max, float min){
		float interval = 0.0f ;
		int	tickMargin = 40 ;													// 確保する目盛間隔
		int	tickSize = chartHeight / tickMargin ;								// ラベル数
		float recommend = ( max - min)/( float)tickSize ;						// 推奨の目盛間隔
		
		// 目盛り間隔を10の塁上倍数の近い値で設定
		int i = 0 ;
		while( true){
			if( Math.pow( 10, i) < recommend && recommend <= Math.pow( 10, i+1)){
				interval = (float)Math.pow( 10, i) ;
				break ;
			}
			if( recommend > 1){													// 1以上の数値の場合
				i++ ;
			}
			else{
				i-- ;
			}
		}
		
		// 推奨値に近づけるよう目盛り間隔を2,5,10倍する
		if(  recommend <= interval * 2){
			interval = interval * 100 ;
			interval = interval * 2 ;
		}
		else if( interval * 2 < recommend && recommend <= interval * 5){
			interval = interval * 100 ;
			interval = interval * 5 ;
		}
		else if( interval * 5 < recommend && recommend <= interval * 10){
			interval = interval * 100 ;
			interval = interval * 10 ;
		}
		interval = interval / 100 ;												// 100を掛けて割るのは丸め誤差対策
		return interval ;
	}

	/**-------------------------------------------------------------------------
	 * 初期化処理を行います。
	 *------------------------------------------------------------------------*/
	private void init( Context context){
		pref = PrefUtil.getInstance( context) ;
		
		// 日付フォーマット初期化
		if( Locale.getDefault().equals( Locale.JAPAN)){
			dateFormat = new SimpleDateFormat( "yyyy年 M月", Locale.JAPAN) ;
		}
		else{
			dateFormat = new SimpleDateFormat( "MMMM yyyy", Locale.US) ;
		}
		
		// フォントサイズ設定
		float density = getResources().getDisplayMetrics().density ;
		DATE_SIZE = DATE_SIZE * density ;
		CYCLE_SIZE = CYCLE_SIZE * density ;
		PREGWEEK_SIZE = PREGWEEK_SIZE * density ;
		PREGMONTH_SIZE = PREGMONTH_SIZE * density ;
		
		// 日曜日描画ペイント
		sundayPaint = new Paint() ;
		sundayPaint.setColor( Color.rgb( 0xFF, 0x10, 0x00)) ;
		sundayPaint.setAlpha( 0x18) ;
		
		// 生理日描画ペイント
		periodPaint = new Paint() ;
		periodPaint.setColor( Color.rgb( 0xFF, 0x10, 0x00)) ;
		periodPaint.setAlpha( 0x28) ;
		
		// 排卵日描画ペイント
		ovPaint = new Paint() ;
		ovPaint.setColor( Color.rgb( 0x10, 0x10, 0xFF)) ;
		ovPaint.setAlpha( 0x20) ;
		
		// マーク描画用ペイント
		markPaint = new Paint() ;
		markPaint.setAntiAlias( true) ;
		
		// 日付表示用ペイント作成
		dayPaint = new Paint() ;
		dayPaint.setTextSize( DATE_SIZE) ;
		dayPaint.setAntiAlias( true) ;
		dayPaint.setColor( Color.BLACK) ;
		
		// 年月表示用ペイント作成
		datePaint = new Paint( dayPaint) ;
		datePaint.setTypeface( Typeface.DEFAULT_BOLD) ;
		
		// 周期表示用ペイント
		cyclePaint = new Paint( dayPaint) ;
		cyclePaint.setTextSize( CYCLE_SIZE) ;
		cyclePaint.setTypeface( Typeface.create( Typeface.DEFAULT, Typeface.ITALIC)) ;
		
		// 妊娠月数文字ペイント
		pregMonthPaint = new Paint( dayPaint) ;
		pregMonthPaint.setTextSize( PREGMONTH_SIZE) ;
		
		// 妊娠週数文字ペイント
		pregWeekPaint = new Paint( dayPaint) ;
		pregWeekPaint.setTextSize( PREGWEEK_SIZE) ;
		
		// 実線ペイント作成
		solidLinePaint = new Paint() ;
		solidLinePaint.setStyle( Paint.Style.STROKE);
		solidLinePaint.setColor( Color.GRAY) ;
		solidLinePaint.setStrokeWidth( 1.0f) ;
		
		// 区切り線ペイント作成
		devPaint = new Paint( solidLinePaint) ;
		devPaint.setStrokeWidth( 3.0f);
		
		// 太線ペイント作成
		boldLinePaint = new Paint( solidLinePaint) ;
		boldLinePaint.setStrokeWidth( 2.0f) ;
		
		// 破線ペイント作成
		dashLinePaint = new Paint( solidLinePaint) ;
		dashLinePaint.setPathEffect( new DashPathEffect( new float[]{ 10.0f, 10.0f}, 0)) ;

		// 値ライン（実線）ペイント作成
		valueLineSolidPaint = new Paint() ;
		valueLineSolidPaint.setStyle( Paint.Style.STROKE) ;
		valueLineSolidPaint.setColor( COLOR_TEMP) ;
		valueLineSolidPaint.setStrokeWidth( 3.0f) ;
		
		// 値ライン（破線）ペイント作成
		valueLineDashPaint = new Paint( valueLineSolidPaint) ;
		valueLineDashPaint.setPathEffect( new DashPathEffect( new float[]{ 8.0f, 4.0f}, 0)) ;
		
		// 値ライン（シャドウ）ペイント作成
		valueLineShadowPaint = new Paint( valueLineSolidPaint) ;
		valueLineShadowPaint.setColor( Color.GRAY) ;
		valueLineShadowPaint.setAlpha( 0x60) ;
		
		// 値丸塗りつぶしペイント
		valueCircleFillPaint = new Paint() ;
		valueCircleFillPaint.setColor( Color.WHITE) ;
		
		// 妊娠週数ライン表示ペイント
		pregLinePaint = new Paint( valueLineSolidPaint) ;
		pregLinePaint.setAlpha( 0x80) ;
		
		// 妊娠週数塗りつぶしペイント
		pregFillPaint = new Paint() ;
		pregFillPaint.setAlpha( 0x40) ;
	}
	
	/**-------------------------------------------------------------------------
	 *　描画ポイントクラスです。
	 *------------------------------------------------------------------------*/
	private class Point{
		public float x ;
		public float y ;
		
		public Point( float x, float y){
			this.x = x ;
			this.y = y ;
		}
	}
}
