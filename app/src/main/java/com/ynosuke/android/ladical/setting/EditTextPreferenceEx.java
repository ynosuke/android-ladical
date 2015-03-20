package com.ynosuke.android.ladical.setting;

import android.content.Context;
import android.graphics.Color;
import android.preference.EditTextPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.ynosuke.android.ladical.R;
import com.ynosuke.android.ladical.util.AplUtil;

//------------------------------------------------------------------------------
/**
* 文字入力プリファレンスの拡張クラスです。
*
* <p>更新履歴：
* <pre>
*     VerNo.        author              update      comment
*     Ver1.00.00    Yoshinosuke Nagaya  2014/02/04  新規作成
* </pre>
* </p>
* @author Yoshinosuke Nagaya
*/
//------------------------------------------------------------------------------
public class EditTextPreferenceEx extends EditTextPreference{
	/**-------------------------------------------------------------------------
	 * コンストラクタ
	 *------------------------------------------------------------------------*/
	public EditTextPreferenceEx( Context context, AttributeSet attrs) {
		super( context, attrs);
		setWidgetLayoutResource( R.layout.preference_valueright) ;
	}
	
	/**-------------------------------------------------------------------------
	 * 画面表示時処理を行います。
	 *------------------------------------------------------------------------*/
	@Override
	protected void onBindView( View view){
		super.onBindView( view) ;
		
		// 設定値表示
		TextView prefValue = ( TextView)view.findViewById( R.id.value) ;
		prefValue.setText( getText()) ;
		
		// 有効無効で表示色を設定
		int color = isEnabled() ? AplUtil.PREF_VALUE_COLOR : Color.LTGRAY ;
		prefValue.setTextColor( color) ;
	}
}

