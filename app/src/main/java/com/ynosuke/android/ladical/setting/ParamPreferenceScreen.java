package com.ynosuke.android.ladical.setting;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ynosuke.android.ladical.R;
import com.ynosuke.android.ladical.util.AplUtil;
import com.ynosuke.android.ladical.util.IGlobalImages;
import com.ynosuke.android.ladical.util.PrefUtil;

//------------------------------------------------------------------------------
/**
* パラメータ用プリファレンススクリーンです。
*
* <p>更新履歴：
* <pre>
*     VerNo.        author              update      comment
*     Ver1.00.00    Yoshinosuke Nagaya  2014/02/03  新規作成
* </pre>
* </p>
* @author Yoshinosuke Nagaya
*/
//------------------------------------------------------------------------------
public class ParamPreferenceScreen extends Preference{
	/** 項目No */
	private int			no ;
	
	/** 設定値管理 */
	private PrefUtil	pref ;
	
	/** イメージ表示ビュー */
	private ImageView 	imageView ;
	
	/** 項目名表示ビュー */
	private TextView	nameView ;
	
	/** 使用未使用ビュー */
	private TextView	enabledView ;
	
	/** 表示非表示ビュー */
	private TextView	displayView ;

	/**-------------------------------------------------------------------------
	 * コンストラクタ
	 *------------------------------------------------------------------------*/
	public ParamPreferenceScreen( Context context, AttributeSet attrs) {
		super( context, attrs);
		setWidgetLayoutResource( R.layout.preference_param) ;
		pref = PrefUtil.getInstance( context) ;
		setOnPreferenceClickListener( new OnPreferenceClickListener() {
			public boolean onPreferenceClick( Preference preference) {
				Intent intent = new Intent( getContext(), ParamSettingPreferenceActivity.class) ;
				intent.putExtra( "no", no) ;
				getContext().startActivity( intent) ;
				return true ;
			}
		}) ;
		
		TypedArray tArray = context.obtainStyledAttributes( attrs, R.styleable.ParamImagePreference) ;
		no = tArray.getInt( R.styleable.ParamImagePreference_no, 0) ;
		tArray.recycle() ;
	}
	
	/**-------------------------------------------------------------------------
	 * 表示内容を更新します。
	 *------------------------------------------------------------------------*/
	public void update(){
		if( imageView == null){
			return ;
		}
		// イメージ表示
		int markNo = pref.getInt( R.string.pref_param_mark, no) ;
		Bitmap bitmap = BitmapFactory.decodeResource( getContext().getResources(), IGlobalImages.MARK_IDS[ markNo]);
		imageView.setImageBitmap( bitmap) ;
		
		// 項目名表示
		nameView.setText( pref.getString( R.string.pref_param_name, no)) ;
		
		// 使用未使用表示
		int enabled = pref.getBoolean( R.string.pref_param_enabled, no) ? R.string.enabled : R.string.disabled ;
		enabledView.setText( getContext().getString( enabled)) ;
		
		// 表示非表示
		int display = pref.getBoolean( R.string.pref_param_display, no) ? R.string.display : R.string.hide ;
		displayView.setText( getContext().getString( display)) ;
	}
	
	/**-------------------------------------------------------------------------
	 * 画面表示時処理を行います。
	 *------------------------------------------------------------------------*/
	@Override
	protected void onBindView( View view){
		super.onBindView( view) ;

		// 各ビュー取得
		TextView titleView = ( TextView)view.findViewById( R.id.title) ;
		imageView = ( ImageView)view.findViewById( R.id.image) ;
		nameView = ( TextView)view.findViewById( R.id.name) ;
		enabledView = ( TextView)view.findViewById( R.id.enabled) ;
		displayView = ( TextView)view.findViewById( R.id.display) ;
		
		// 項目No表示
		titleView.setText( getContext().getString( R.string.param, no)) ;

		// 文字色設定
		nameView.setTextColor( AplUtil.PREF_VALUE_COLOR) ;
		enabledView.setTextColor( AplUtil.PREF_VALUE_COLOR) ;
		displayView.setTextColor( AplUtil.PREF_VALUE_COLOR) ;
		
		// 内容表示
		update() ;
	}
}

