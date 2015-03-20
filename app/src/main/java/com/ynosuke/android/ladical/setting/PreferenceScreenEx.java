package com.ynosuke.android.ladical.setting;

import android.content.Context;
import android.content.Intent;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.ynosuke.android.ladical.R;
import com.ynosuke.android.ladical.util.AplUtil;

//------------------------------------------------------------------------------
/**
* 拡張プリファレンススクリーンです。
*
* <p>更新履歴：
* <pre>
*     VerNo.        author              update      comment
*     Ver1.00.00    Yoshinosuke Nagaya  2014/02/01  新規作成
* </pre>
* </p>
* @author Yoshinosuke Nagaya
*/
//------------------------------------------------------------------------------
public class PreferenceScreenEx extends Preference{
	private String 		prefValue ;

	/**-------------------------------------------------------------------------
	 * コンストラクタ
	 *------------------------------------------------------------------------*/
	public PreferenceScreenEx( Context context, AttributeSet attrs) {
		super( context, attrs);
		setWidgetLayoutResource( R.layout.preference_valueright) ;
		setOnPreferenceClickListener( new OnPreferenceClickListener() {
			public boolean onPreferenceClick( Preference preference) {
			Context context = getContext() ;
			if( getKey().equals( context.getString( R.string.settings_cyclesetting))){
				// 生理周期のみ次画面表示
				Intent intent = new Intent( context, CycleSettingPreferenceActivity.class) ;
				getContext().startActivity( intent) ;
			}
			return true ;
			}
		}) ;
	}
	
	public void setPrefValue( String prefValue){
		this.prefValue = prefValue ;
	}
	
	/**-------------------------------------------------------------------------
	 * 画面表示時処理を行います。
	 *------------------------------------------------------------------------*/
	@Override
	protected void onBindView( View view){
		super.onBindView( view) ;
		
		TextView textView = ( TextView)view.findViewById( R.id.value) ;
		textView.setText( prefValue) ;
		textView.setTextColor( AplUtil.PREF_VALUE_COLOR) ;
	}

}

