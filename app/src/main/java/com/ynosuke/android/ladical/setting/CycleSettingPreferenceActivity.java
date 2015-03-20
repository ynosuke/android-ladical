package com.ynosuke.android.ladical.setting;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;

import com.ynosuke.android.ladical.R;
import com.ynosuke.android.ladical.util.DbUtil;
import com.ynosuke.android.ladical.util.IGlobalPreferences;
import com.ynosuke.android.ladical.util.PrefUtil;

//------------------------------------------------------------------------------
/**
* 生理周期設定画面のアクティビティーです。
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
public class CycleSettingPreferenceActivity extends PreferenceActivity{
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle( getString( R.string.settings_cycle));
		getFragmentManager().beginTransaction().replace( android.R.id.content, new PreferenceScreenFragment()).commit() ;
	}

	public static class PreferenceScreenFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener{
		/** 設定情報 */
		private PrefUtil pref = PrefUtil.getInstance( getActivity()) ;
		
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource( R.xml.pref_period) ;
			
			setParamEnabled() ;
		}
		
		/**-------------------------------------------------------------------------
		 * 設定値変更時処理を行います。
		 *------------------------------------------------------------------------*/
		@Override
		public void onSharedPreferenceChanged( SharedPreferences sharedPreferences, String key) {
			ListPreferenceEx list = ( ListPreferenceEx)findPreference( key) ;
	        list.update() ;
			if( key.equals( getString( R.string.pref_cycle_settype))){			// 生理周期の設定区分が変更された場合
				setParamEnabled() ;
				if( pref.getInt( R.string.pref_cycle_settype) == IGlobalPreferences.CYCLE_SETTYPE_AVG){
					DbUtil.updatePeriodCycle() ;								// 生理周期更新
				}
			}
			DbUtil.updateWillPeriodOvulation() ;								// 生理予測日更新
		}
		
		/**-------------------------------------------------------------------------
		 * アクティビティー再表示時時処理を行います。
		 *------------------------------------------------------------------------*/
		@Override
		public void onResume() {
		    super.onResume();
		    getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
		}
		
		/**-------------------------------------------------------------------------
		 * アクティビティー一時停止時処理を行います。
		 *------------------------------------------------------------------------*/
		@Override
		public void onPause() {
		    super.onPause();
		    getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
		}
		
		private void setParamEnabled(){
			boolean enabled = pref.getInt( R.string.pref_cycle_settype) == IGlobalPreferences.CYCLE_SETTYPE_FIX ;
			(( ListPreferenceEx)findPreference( getString( R.string.pref_period_cycle))).setEnabled( enabled) ;
			(( ListPreferenceEx)findPreference( getString( R.string.pref_cycle_avg_times))).setEnabled( !enabled) ;
		}
	}
}

