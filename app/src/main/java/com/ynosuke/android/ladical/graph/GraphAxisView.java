package com.ynosuke.android.ladical.graph;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

import com.ynosuke.android.ladical.R;
import com.ynosuke.android.ladical.data.ParamItem;
import com.ynosuke.android.ladical.util.IMode;
import com.ynosuke.android.ladical.util.PrefUtil;

//------------------------------------------------------------------------------
/**
* グラフ軸ビューです。
*
* <p>更新履歴：
* <pre>
*     VerNo.        author              update      comment
*     Ver1.00.00    Yoshinosuke Nagaya  2013/08/15  新規作成
*     Ver1.00.03						2015/02/27	目盛り線の修正
* </pre>
* </p>
* @author Yoshinosuke Nagaya
*/
//------------------------------------------------------------------------------
public class GraphAxisView extends View implements IMode {
	//	定数定義 ----------------------------------------------------------------	
	/** パラメータ・日付行の高さ */
	public static final int	LINE_HEIGHT = 25 ;
	
	/** 目盛り文字大きさ */
	private float REGEND_SIZE = 14.0f ;
	
	/** 項目名文字大きさ */
	private float PARAM_SIZE = 11.0f ;
	
	//	内部定義 ----------------------------------------------------------------	
	/** 表示モード */
	public int				mode = TEMP ;
	
	/** 設定値管理 */
	private PrefUtil		pref ;

	/** 表示パラメータリスト */
	private List<ParamItem>	paramList ;
	
	/** 区切り線ペイント */
	private Paint			devPaint ;
	
	/** パラメータ名表示ペイント */
	private Paint			paramFontPaint ;
	
	/** 目盛り値表示ペイント */
	private Paint			scaleFontPaint ;
	
	/** 日付表示ペイント */
	private Paint			datePaint ;
	
	/**-------------------------------------------------------------------------
	 * インスタンス作成時の初期化処理を行います。（findViewByIdを使用する際に必要）
	 * 
	 * @param context	コンテキスト
	 *------------------------------------------------------------------------*/
	public GraphAxisView( Context context) {
		super( context);
		init( context) ;														// 初期化処理
	}
	
	/**-------------------------------------------------------------------------
	 * インスタンス作成時の初期化処理を行います。（xmlにてレイアウトパラメータが指定された場合に使用）
	 * 
	 * @param context	コンテキスト
	 * @param attrs		レイアウトパラメータ
	 *------------------------------------------------------------------------*/
	public GraphAxisView( Context context, AttributeSet attrs) {
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
	public GraphAxisView( Context context, AttributeSet attrs, int defStyle) {
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
		int paramSize = paramList.size() ;
		int chartHeight = getHeight() - LINE_HEIGHT * ( paramSize + 3) - 2 ;		// チャート高さの設定
		
		// 区切り線
		float y = getHeight() - LINE_HEIGHT * 2 ;
		canvas.drawLine( 0, 1, getWidth(), 1, devPaint) ;
		canvas.drawLine( 0, y, getWidth(), y, devPaint) ;
		canvas.drawLine( getWidth()-1.5f, 0, getWidth()-1.5f, getHeight(), devPaint) ;
		
		// 単位
		String text = "℃" ;
		switch( mode){
			case WEIGHT : 	text = "kg" ; break ;
			case RATIO :	text = "%" ; break ;
		}
		float textWidth = scaleFontPaint.measureText( text) ;
		float x = getWidth() - 8 - textWidth ;
		y = 2 + scaleFontPaint.getTextSize() ;
		canvas.drawText( text,  x, y, scaleFontPaint) ;
		
		// 生理周期
		text = getContext().getString( R.string.graph_cycle) ;
		textWidth = paramFontPaint.measureText( text) ;
		x = getWidth() - 5 - textWidth ;
		y = chartHeight + LINE_HEIGHT - 3 ;
		canvas.drawText( text, x, y, paramFontPaint) ;
		
		// パラメータ名称
		for( int i = 0; i < paramSize; i++){
			text = paramList.get( i).name ;
			textWidth = paramFontPaint.measureText( text) ;
			x = getWidth() - 8 - textWidth ;
			y = chartHeight + LINE_HEIGHT * (i + 2) - 3 ;
			canvas.drawText( text, x, y, paramFontPaint) ;
		}
		
		// 日付
		text = getContext().getString( R.string.graph_date) ;
		textWidth = datePaint.measureText( text) ;
		x = getWidth() - 8 - textWidth ;
		y = getHeight() - 14  ;
		canvas.drawText( text, x, y, datePaint) ;
		
		// 目盛り
		float min = 0 ;
		float max = 0 ;
		switch( mode){
			case TEMP : 	
				min = pref.getFloat( R.string.pref_temp_min) ;
				max = pref.getFloat( R.string.pref_temp_max) ;
				break ;
			case WEIGHT :
				min = pref.getFloat( R.string.pref_weight_min) ;
				max = pref.getFloat( R.string.pref_weight_max) ;
				break ;
			case RATIO :
				min = pref.getFloat( R.string.pref_ratio_min) ;
				max = pref.getFloat( R.string.pref_ratio_max) ;
				break ;
		}
		float intervalY = getInterval( chartHeight, max, min) ;
		min -= intervalY ;
		max += intervalY ;
		float interval = chartHeight / (max - min) ;
		float start = ( float)( Math.round(  min*10) % Math.round( intervalY*10))/10.0f ;
		for( float def = start; def < max - min; def += intervalY){
			float value = min + def ;
			text = String.format( "%.1f", value) ;
			textWidth = scaleFontPaint.measureText( text) ;
			x = getWidth() - 8 - textWidth ;
			y = chartHeight - interval * def + scaleFontPaint.getFontMetrics( null) / 2 - 5;
			if( y < 80 || chartHeight - 10 < y){
				continue ;
			}
			canvas.drawText( text, x, y, scaleFontPaint) ;
		}
	}
	
	/**-------------------------------------------------------------------------
	 * 表示内容を更新します。
	 *------------------------------------------------------------------------*/
	public void update(){
		paramList = new ArrayList<ParamItem>(10) ;
		for( int i = 1; i <= 10; i++){
			boolean enabled = pref.getBoolean( R.string.pref_param_enabled, i) ;
			if( enabled){
				String name = pref.getString( R.string.pref_param_name, i) ;
				paramList.add( new ParamItem( i, name)) ;
			}
		}
		
		// 再描画
		invalidate() ;
	}
	
	/**-------------------------------------------------------------------------
	 * 初期化処理を行います。
	 *------------------------------------------------------------------------*/
	private void init( Context context){
		pref = PrefUtil.getInstance( context) ;
		
		// フォントサイズ設定
		float density = getResources().getDisplayMetrics().density ;
		REGEND_SIZE = REGEND_SIZE * density ;
		PARAM_SIZE = PARAM_SIZE * density ;
		
		// 区切り線ペイント作成
		devPaint = new Paint() ;
		devPaint.setColor( Color.GRAY) ;
		devPaint.setStrokeWidth( 3.0f) ;
		
		// パラメータ名表示ペイント作成
		paramFontPaint = new Paint() ;
		paramFontPaint.setTextSize( PARAM_SIZE) ;
		paramFontPaint.setAntiAlias( true) ;
		
		// 目盛り表示ペイント作成
		scaleFontPaint = new Paint( paramFontPaint) ;
		scaleFontPaint.setTextSize( REGEND_SIZE) ;
		
		// 日付表示ペイント作成
		datePaint = new Paint( scaleFontPaint) ;
		datePaint.setTypeface( Typeface.DEFAULT_BOLD) ;
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
}

