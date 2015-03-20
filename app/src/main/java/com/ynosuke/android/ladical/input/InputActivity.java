package com.ynosuke.android.ladical.input;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.ynosuke.android.ladical.R;
import com.ynosuke.android.ladical.data.Item;
import com.ynosuke.android.ladical.data.ParamItem;
import com.ynosuke.android.ladical.util.AplUtil;
import com.ynosuke.android.ladical.util.DbUtil;
import com.ynosuke.android.ladical.util.IGlobalImages;
import com.ynosuke.android.ladical.util.PrefUtil;


//------------------------------------------------------------------------------
/**
* 入力画面アクティビティーです。
*
* <p>更新履歴：
* <pre>
*     VerNo.        author              update      comment
*     Ver1.00.00    Yoshinosuke Nagaya  2013/01/09  新規作成
* </pre>
* </p>
* @author Yoshinosuke Nagaya
*/
//------------------------------------------------------------------------------
public class InputActivity extends Activity implements OnFocusChangeListener, IGlobalImages, OnClickListener{
	//	内部定義 ----------------------------------------------------------------
	/** 設定値管理 */
	private PrefUtil		pref ;
	
	/** 表示中の情報 */
	private Item			item ;
	
	/** 表示中の日付 */
	private Date			date ;
	
	/** 日付表示フォーマット */
	private DateFormat		dateFormat ;
	
	/** フォーカス中の入力テキスト */
	private TextView		inputText ;
	
	/** 体温入力テキスト */
	private TextView		tempInput ;
	
	/** 体重入力テキスト */
	private TextView		weightInput ;
	
	/** 体脂肪率入力テキスト */
	private TextView		ratioInput ;
	
	/** メモ入力ビュー */
	private EditText		memoInput ;
	
	/** パラメータ設定リストビュー */
	private ListView		paramListView ;
	
	/** キーボードレイアウト */
	private TableLayout		keyboard ;
	
	/** 前日表示ボタン */
	private TextView		prevButton ;
	
	/** 翌日表示ボタン */
	private TextView		nextButton ;
	
	/** パラメータリスト表示アダプター */
	private ParamListAdapter 	adapter ;
	
	/** パラメータ設定リスト */
	private List<ParamItem>		paramList = new ArrayList<ParamItem>() ;
	
	/**-------------------------------------------------------------------------
	 * アクティビティー作成時処理を行います。
	 * 
	 * @param savedInstanceState
	 *------------------------------------------------------------------------*/
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		requestWindowFeature( Window.FEATURE_NO_TITLE);
		pref = PrefUtil.getInstance( this) ;
		
		// 体重・体脂肪率の表示設定に従いレイアウト設定
		boolean wrEnabled = pref.getBoolean( R.string.pref_weightratio_enabled) ;
		setContentView( wrEnabled ? R.layout.activity_input : R.layout.activity_input_simple) ;

		// AdView をリソースとしてルックアップしてリクエストを読み込む
	    AdView adView = ( AdView)this.findViewById( R.id.adView);
	    AdRequest adRequest = new AdRequest.Builder()
	    .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)							// エミュレータ用
	    .build();
	    adView.loadAd(adRequest);

		// 各ウィジェット取得
		tempInput = ( TextView)findViewById( R.id.tempinput) ;
		weightInput = ( TextView)findViewById( R.id.weightinput) ;
		ratioInput = ( TextView)findViewById( R.id.ratioinput) ;
		memoInput = ( EditText)findViewById( R.id.memoinput) ;
		keyboard = ( TableLayout)findViewById( R.id.keyboard) ;
		paramListView = ( ListView)findViewById( R.id.paramlist) ;
		prevButton = ( TextView)findViewById( R.id.prevButton) ;
		nextButton = ( TextView)findViewById( R.id.nextButton) ;
		
		// ボタンにリスナー設定
		prevButton.setOnClickListener( this) ;
		nextButton.setOnClickListener( this) ;

		// 体温、体重、体脂肪率入力テキストにフォーカスリスナー設定
		tempInput.setOnFocusChangeListener( this) ;
		weightInput.setOnFocusChangeListener( this) ;
		ratioInput.setOnFocusChangeListener( this) ;

		// 日付表示
		Intent intent = getIntent() ;
		date = new Date( intent.getLongExtra( "date", 0)) ;						// カレンダー画面より日付取得
		if( Locale.getDefault().equals( Locale.JAPAN)){
			dateFormat = new SimpleDateFormat( "M月 d日 (E)", Locale.JAPAN) ;
		}
		else{
			dateFormat = new SimpleDateFormat( "E, MMM. d", Locale.US) ;
		}
		
		// パラメータ設定リスト初期化
		for( int i = 1; i <= 10; i++){
			boolean enabled = pref.getBoolean( R.string.pref_param_enabled, i) ;
			if( enabled){														// 有効なものだけリストに追加する
				String name = pref.getString( R.string.pref_param_name, i) ;
				int markNo = pref.getInt( R.string.pref_param_mark, i) ;
				Bitmap mark = BitmapFactory.decodeResource( getResources(), MARK_IDS[ markNo]) ;
				paramList.add( new ParamItem( i, name, false, mark)) ;			// ParamItemを作成しリストに追加
			}
		}
		adapter = new ParamListAdapter( this, paramList) ;
		paramListView.setAdapter( adapter) ;
		
		// 背景色設定
		View headerView = findViewById( R.id.headerView) ;
		AplUtil.setHeaderBackground( headerView) ;
		View mainView = findViewById( R.id.mainView) ;
		AplUtil.setViewBackground( mainView) ;
		
		// データ表示
		showData() ;
	}
	
	/**-------------------------------------------------------------------------
	 * キーボード押下時処理を行います。
	 * 
	 * @param view	押されたキー
	 *------------------------------------------------------------------------*/
	public void pressButton( View view){
		Button button = ( Button)view ;
		String value = ( String)inputText.getText() ;
		int id = view.getId();
		if( id == R.id.backspace){
			if( value.length() != 0){
				value = value.substring( 0, value.length() - 1) ;
			}
		} else if( id == R.id.dot){
//			value += "." ;
		} else{
			if( value.length() == 2){
				value += "." ;
			}
			value += button.getText() ;
			if( value.length() == 2){
				value += "." ;
			}
		}
		inputText.setText( value) ;
		if( value.length() == 5){
			keyboard.setVisibility( View.INVISIBLE) ;							// 少数第2位まで入力したらキーボード非表示
			TextView titleText = ( TextView)findViewById( R.id.titletext) ;
			titleText.requestFocus() ;											// タイトル部にフォーカスを移しておく
		}
	}
	
	/**-------------------------------------------------------------------------
	 * リスナーに登録されたViewがクリックされた時の処理を行います。
	 *------------------------------------------------------------------------*/
	@Override
	public void onClick( View view){
		if( view == prevButton){
			applyData() ;
			date = new Date( date.getTime() - DbUtil.ONEDAY) ;
			showData() ;
		}
		else if( view == nextButton){
			applyData() ;
			date = new Date( date.getTime() + DbUtil.ONEDAY) ;
			showData() ;
		}
	}

	/**-------------------------------------------------------------------------
	 * フォーカス変更時処理を行います。
	 *------------------------------------------------------------------------*/
	@Override
	public void onFocusChange( View view, boolean hasFocus) {
		if( view instanceof TextView){
			TextView textView = ( TextView)view ;									// フォーカス変更された入力テキスト取得
			if( hasFocus){
				textView.setBackgroundResource( R.drawable.textinput_gradation_forcus) ;

				keyboard.setVisibility( View.VISIBLE) ;
				inputText = textView ;
			}
			else{
				textView.setBackgroundResource( R.drawable.textinput_gradation) ;
				keyboard.setVisibility( View.INVISIBLE) ;
			}
		}
	}
	
	/**-------------------------------------------------------------------------
	 * アクティビティー一時停止時処理を行います。
	 *------------------------------------------------------------------------*/
	@Override
	public void onPause(){
		applyData() ;																// データ適用
		super.onPause() ;
	}

	/**-------------------------------------------------------------------------
	 * データを適用します。
	 *------------------------------------------------------------------------*/
	private void applyData(){
		// 体温取得
		if( tempInput.getText().length() != 0){
			item.temp = Float.valueOf(( String)tempInput.getText()) ;
		}
		// 体重取得
		if( weightInput.getText().length() != 0){
			item.weight = Float.valueOf(( String)weightInput.getText()) ;
		}
		// 体脂肪率取得
		if( ratioInput.getText().length() != 0){
			item.ratio = Float.valueOf(( String)ratioInput.getText()) ;
		}
		// メモ取得
		if( memoInput.getText() != null){
			item.memo = memoInput.getText().toString() ;
		}
		
		// パラメータのチェック状態取得
		boolean changePeriod = false ;
		if( paramList.size() != 0){
			changePeriod = paramList.get( 0).value != item.param[0] ;
		}
		for( ParamItem paramItem : paramList){
			item.param[paramItem.no-1] = paramItem.value ;
		}
		
		// データベースにItemの内容登録
		DbUtil.setItem( item, changePeriod) ;
	}
	
	/**-------------------------------------------------------------------------
	 * データを表示します。
	 *------------------------------------------------------------------------*/
	private void showData(){
		// 日付表示
		TextView titleText = ( TextView)findViewById( R.id.titletext) ;
		titleText.setText( dateFormat.format( date)) ;							// タイトルに日付表示
		
		// データ表示
		item = DbUtil.getItem( date) ;											// データベースより情報取得
		if( item == null){
			item = new Item( date) ;
		}
		tempInput.setText( item.temp != 0 ? String.format( "%.2f", item.temp) : "") ;
																				// 体温表示
		weightInput.setText( item.weight != 0 ? String.format( "%.2f", item.weight) : "") ;	
																				// 体重表示
		ratioInput.setText( item.ratio != 0 ? String.format( "%.2f", item.ratio) : "") ;
																				// 体脂肪率表示
		memoInput.setText( item.memo) ;											// メモ表示

		for( ParamItem paramItem : paramList){
			paramItem.value = item.param[ paramItem.no - 1] ;					// パラメータ状態変更
		}
		adapter.notifyDataSetChanged() ;										// パラメータリスト更新
	}
	
	/**-------------------------------------------------------------------------
	 * パラメータリスト表示用アダプタークラスです。
	 *------------------------------------------------------------------------*/
	private class ParamListAdapter extends ArrayAdapter<ParamItem>{
		
		/** インフレーター */
		private LayoutInflater	inflater ;

		/**-------------------------------------------------------------------------
		 * パラメータリスト表示用アダプタークラスの初期化を行います。
		 *------------------------------------------------------------------------*/
		public ParamListAdapter( Context context, List<ParamItem> list){
			super( context, R.layout.row_input_paramlist, list) ;
			inflater = ( LayoutInflater)context.getSystemService( Context.LAYOUT_INFLATER_SERVICE) ;
		}
		
		/**-------------------------------------------------------------------------
		 * 行表示用のビューを取得します。
		 *------------------------------------------------------------------------*/
		public View getView( int position, View convertView, ViewGroup parent){
			ImageView markView ;
			TextView nameText ;
			ToggleButton valueButton ;
			if( convertView == null){
				convertView = inflater.inflate( R.layout.row_input_paramlist, null) ;// ビューがnullの場合は作成
			}

			final ParamItem item = getItem( position) ;							// この行のParamItem取得
			if( item != null){
				markView = ( ImageView)convertView.findViewById( R.id.input_paramlist_mark) ;
				markView.setImageBitmap( item.mark) ;							// マーク表示
				nameText = ( TextView)convertView.findViewById( R.id.input_paramlist_name) ;
				nameText.setText( item.name) ;									// 名称表示
				valueButton = ( ToggleButton)convertView.findViewById( R.id.input_paramlist_value) ;
				valueButton.setOnCheckedChangeListener( new OnCheckedChangeListener() {
					public void onCheckedChanged( CompoundButton buttonView, boolean isChecked) {
						item.value = isChecked ;
					}
				}) ;
				valueButton.setChecked( item.value) ;							// 設定値表示
			}
			return convertView ;
		}
	}
}

