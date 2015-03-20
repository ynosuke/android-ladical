package com.ynosuke.android.ladical.graph;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.AlertDialog.Builder;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.HorizontalScrollView;
import android.widget.TextView;

import com.ynosuke.android.ladical.R;
import com.ynosuke.android.ladical.util.AplUtil;
import com.ynosuke.android.ladical.util.IMode;
import com.ynosuke.android.ladical.util.PrefUtil;

//------------------------------------------------------------------------------
/**
* グラフ画面アクティビティーです。
*
* <p>更新履歴：
* <pre>
*     VerNo.        author              update      comment
*     Ver1.00.00    Yoshinosuke Nagaya  2013/08/14  新規作成
*     Ver1.00.02						2014/12/24	表示モードを記憶するよう対応
* </pre>
* </p>
* @author Yoshinosuke Nagaya
*/
//------------------------------------------------------------------------------
@SuppressLint("NewApi")
public class GraphFragment extends Fragment implements OnClickListener, IMode{
	//	内部定義 ----------------------------------------------------------------
	/** 設定値管理 */
	public PrefUtil				pref = PrefUtil.getInstance( getActivity()) ;
	
	/** タイトルビュー */
	private TextView			titleView ;
	
	/** 期間表示ビュー */
	private TextView			subTitleView ;
	
	/** グラフ軸表示ビュー */
	private GraphAxisView		axisView ;
	
	/** グラフ表示ビュー */
	private GraphView			graphView ;
	
	/** スクロールビュー */
	private HorizontalScrollView	scrollView ;
	
	/** 次表示ボタン */
	private TextView			prevButton ;
	
	/** 前表示ボタン */
	private TextView			nextButton ;
	
	/** 日付計算用カレンダーインスタンス */
	private Calendar			cal ;
	
	/** 期間表示用日付フォーマット */
	private DateFormat			dateFormat ;
	
	/**-------------------------------------------------------------------------
	 * フラグメント作成時処理を行います。
	 *------------------------------------------------------------------------*/
	@Override
	public void onCreate( Bundle savedInstanceState){
		super.onCreate( savedInstanceState) ;
		setHasOptionsMenu( true) ;
		setRetainInstance( true);

		// 日付初期化
		cal = Calendar.getInstance() ;
		cal.setTime( new Date()) ;
		cal.set( Calendar.HOUR_OF_DAY, 0) ;
		cal.set( Calendar.MINUTE, 0) ;
		cal.set( Calendar.SECOND, 0) ;
		cal.set( Calendar.MILLISECOND, 0) ;
		
		cal.add( Calendar.DAY_OF_MONTH, -7) ;
		
		// 日付フォーマット初期化
		if( Locale.getDefault().equals( Locale.JAPAN)){
			dateFormat = new SimpleDateFormat( "yyyy年M月", Locale.JAPAN) ;
		}
		else{
			dateFormat = new SimpleDateFormat( "MMM yyyy", Locale.US) ;
		}
	}
	
	/**-------------------------------------------------------------------------
	 * アクティビティー作成時処理を行います。
	 *------------------------------------------------------------------------*/
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate( R.layout.fragment_graph, container, false) ;

		// 各ビュー取得
		titleView = ( TextView)view.findViewById( R.id.titleView) ;
		subTitleView = ( TextView)view.findViewById( R.id.subTitleView) ;
		scrollView = ( HorizontalScrollView)view.findViewById( R.id.scrollView) ;
		
		axisView = ( GraphAxisView)view.findViewById( R.id.axisView) ;
		axisView.mode = pref.getInt( R.string.pref_displaymode) ;
		graphView = ( GraphView)view.findViewById( R.id.graphView) ;
		graphView.setStart( cal.getTime()) ;
		graphView.mode = pref.getInt( R.string.pref_displaymode) ;
		prevButton = ( TextView)view.findViewById( R.id.prevButton) ;
		nextButton = ( TextView)view.findViewById( R.id.nextButton) ;
		
		// ボタンにリスナー設定
		prevButton.setOnClickListener( this) ;
		nextButton.setOnClickListener( this) ;
		
		// タイトル更新
		updateTitle() ;
		
		// 背景色設定
		View headerView = view.findViewById( R.id.headerView) ;
		AplUtil.setHeaderBackground( headerView) ;
		AplUtil.setViewBackground( axisView) ;
		AplUtil.setViewBackground( graphView) ;
		
		return view ;
	}
	
	/**-------------------------------------------------------------------------
	* メニュー作成処理を行います。
	*-------------------------------------------------------------------------*/
	@Override
	public void onCreateOptionsMenu( Menu menu, MenuInflater inflater){
		menu.clear() ;
		
		if( pref.getBoolean( R.string.pref_weightratio_enabled)){
			inflater.inflate( R.menu.graph_menu, menu) ;
		}
	}
	
	/**-------------------------------------------------------------------------
	 * アクティビティー開始時処理を行います。
	 *------------------------------------------------------------------------*/
	@Override
	public void onStart(){
		super.onStart() ;
		scrollView.post( new Runnable(){
			public void run(){
				scrollView.scrollTo( graphView.getWidth(), scrollView.getScrollY()) ;
																				// 右端にスクロールするように
			}
		}) ;
	}
	
	/**-------------------------------------------------------------------------
	 * アクティビティー再表示時時処理を行います。
	 *------------------------------------------------------------------------*/
	@Override
	public void onResume(){
		super.onResume() ;
		
		axisView.update() ;
		graphView.update() ;
	}
	
	/**-------------------------------------------------------------------------
	* メニュー選択時処理を行います。
	*-------------------------------------------------------------------------*/
	@Override
	public boolean onOptionsItemSelected( MenuItem item){
		// 表示モード設定
		int mode = 0 ;
		int itemId = item.getItemId();
		if( itemId == R.id.menu_temp){
			mode = TEMP ;
		} 
		else if( itemId == R.id.menu_weight){
			mode = WEIGHT ;
		} 
		else if( itemId == R.id.menu_ratio){
			mode = RATIO ;
		}
		else if( itemId == R.id.menu_help){
			Builder builder = new Builder( getActivity()) ;
			builder.setTitle( getString( R.string.help_graph_title)) ;
			builder.setMessage( getString( R.string.help_graph)) ;
			builder.setNegativeButton( getString( R.string.close), null) ;
			builder.show() ;
			return true ;
		}
		graphView.mode = mode ;
		axisView.mode = mode ;
		graphView.invalidate() ;
		axisView.invalidate() ;													// 各ビュー再描画
		updateTitle() ;															// タイトル更新
		pref.putPref( R.string.pref_displaymode, mode);							// 表示モード保存
		return true ;
	}
	
	/**-------------------------------------------------------------------------
	* メニュー表示前処理を行います。
	*-------------------------------------------------------------------------*/
	@Override
	public void onPrepareOptionsMenu( Menu menu){
		if( pref.getBoolean( R.string.pref_weightratio_enabled)){
			if( graphView == null){
				return ;
			}
			MenuItem tempItem = menu.findItem( R.id.menu_temp) ;
			tempItem.setEnabled( graphView.mode != TEMP) ;
			
			MenuItem weightItem = menu.findItem( R.id.menu_weight) ;
			weightItem.setEnabled( graphView.mode != WEIGHT) ;
			
			MenuItem ratioItem = menu.findItem( R.id.menu_ratio) ;
			ratioItem.setEnabled( graphView.mode != RATIO) ;
		}
	}
	
	/**-------------------------------------------------------------------------
	 * リスナーに登録されたViewがクリックされた時の処理を行います。
	 *------------------------------------------------------------------------*/
	@Override
	public void onClick( View view){
		if( view == prevButton){
			cal.add( Calendar.DATE, -GraphView.DAY_LENGTH) ;
			graphView.setStart( cal.getTime()) ;
			graphView.update() ;
			scrollView.post( new Runnable(){
				public void run(){
					scrollView.scrollTo( graphView.getWidth(), scrollView.getScrollY()) ;
																				// 右端にスクロールするように
				}
			}) ;
			updateTitle() ;
		}
		else if( view == nextButton){
			cal.add( Calendar.DATE, GraphView.DAY_LENGTH) ;
			graphView.setStart( cal.getTime()) ;
			graphView.update() ;
			scrollView.post( new Runnable(){
				public void run(){
					scrollView.scrollTo( 0, scrollView.getScrollY()) ;			// 左端にスクロールするように
				}
			}) ;
			updateTitle() ;
		}
	}
	
	/**-------------------------------------------------------------------------
	 * 表示を更新します。
	 *------------------------------------------------------------------------*/
	public void redraw(){
		updateTitle() ;
		graphView.invalidate() ;
		axisView.invalidate() ;
	}
	
	/**-------------------------------------------------------------------------
	 * タイトルを更新します。
	 *------------------------------------------------------------------------*/
	private void updateTitle(){
		// 表示期間更新
		cal.add( Calendar.DATE, -GraphView.DAY_LENGTH) ;
		String start = dateFormat.format( cal.getTime()) ;
		cal.add( Calendar.DATE, GraphView.DAY_LENGTH) ;
		titleView.setText( String.format( "%s ~", start)) ;
				
		// タイトル名更新
		switch( graphView.mode){
			case TEMP : subTitleView.setText( getString( R.string.menu_temp)) ; break ;
			case WEIGHT : subTitleView.setText( getString( R.string.menu_weight)) ; break ;
			case RATIO : subTitleView.setText( getString( R.string.menu_ratio)) ; break ;
		}
	}
}

