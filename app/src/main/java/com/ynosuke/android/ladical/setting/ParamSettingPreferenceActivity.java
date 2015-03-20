package com.ynosuke.android.ladical.setting;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;

import com.ynosuke.android.ladical.R;

//------------------------------------------------------------------------------
/**
* パラメータ設定画面です。
*
* <p>更新履歴：
* <pre>
*     VerNo.        author              update      comment
*     Ver1.00.00    Yoshinosuke Nagaya  2013/03/04  新規作成
*     Ver1.00.02						2014/11/11	使用・表示設定の連動処理
* </pre>
* </p>
* @author Yoshinosuke Nagaya
*/
@SuppressLint("NewApi")
//------------------------------------------------------------------------------
public class ParamSettingPreferenceActivity extends PreferenceActivity{
	/** 項目No */
	private static int		no ;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		no = getIntent().getIntExtra( "no", 0) ;
		getFragmentManager().beginTransaction().replace( android.R.id.content, new ParamPreferenceScreenFragment()).commit() ;
	}
	
	public static class ParamPreferenceScreenFragment extends PreferenceFragment{
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			int resId = 0 ;
			switch( no){
				case 1 : resId = R.xml.pref_param01 ; break ;
				case 2 : resId = R.xml.pref_param02 ; break ;
				case 3 : resId = R.xml.pref_param03 ; break ;
				case 4 : resId = R.xml.pref_param04 ; break ;
				case 5 : resId = R.xml.pref_param05 ; break ;
				case 6 : resId = R.xml.pref_param06 ; break ;
				case 7 : resId = R.xml.pref_param07 ; break ;
				case 8 : resId = R.xml.pref_param08 ; break ;
				case 9 : resId = R.xml.pref_param09 ; break ;
				case 10 : resId = R.xml.pref_param10 ; break ;
			}
			addPreferencesFromResource( resId) ;
			
			// 使用設定による表示スイッチの連動処理
			final CheckBoxPreference displayCheck = ( CheckBoxPreference)findPreference( String.format( "param%d_display", no)) ;
			final CheckBoxPreference enabledCheck = ( CheckBoxPreference)findPreference( String.format( "param%d_enabled", no)) ;
			enabledCheck.setOnPreferenceChangeListener( new OnPreferenceChangeListener(){
				@Override
				public boolean onPreferenceChange( Preference preference, Object newValue) {
					if(!( Boolean)newValue ){
						displayCheck.setChecked( false);
						displayCheck.setEnabled( false);
					}
					else{
						displayCheck.setEnabled( true);
					}
					return true;
				}
			});
			
			// 表示スイッチの初期化
			if( !enabledCheck.isChecked()){
				displayCheck.setEnabled( false);
			}
		}
	}
}

