package com.ynosuke.android.ladical.setting;

import java.util.Date;
import java.util.Locale;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.support.v4.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ynosuke.android.ladical.R;
import com.ynosuke.android.ladical.util.AplUtil;
import com.ynosuke.android.ladical.util.DbUtil;
import com.ynosuke.android.ladical.util.PrefUtil;

//------------------------------------------------------------------------------
/**
* 設定画面フラグメントです。
*
* <p>更新履歴：
* <pre>
*     VerNo.        author              update      comment
*     Ver1.00.00    Yoshinosuke Nagaya  2013/03/04  新規作成
* </pre>
* </p>
* @author Yoshinosuke Nagaya
*/
//------------------------------------------------------------------------------
public class SettingFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener {
	/** 設定値管理 */
	private PrefUtil	pref ;
	
	/**-------------------------------------------------------------------------
	 * アクティビティー作成時処理を行います。
	 * 
	 * @param savedInstanceState
	 *------------------------------------------------------------------------*/
	public void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref);
        pref = PrefUtil.getInstance(getActivity());

        boolean wrEnabled = pref.getBoolean(R.string.pref_weightratio_enabled);

        // 体重上限
        ListPreference weightMax = (ListPreference) findPreference(getString(R.string.pref_weight_max));
        weightMax.setEnabled(wrEnabled);

        // 体重下限
        ListPreference weightMin = (ListPreference) findPreference(getString(R.string.pref_weight_min));
        weightMin.setEnabled(wrEnabled);

        // 体脂肪率上限
        ListPreference ratioMax = (ListPreference) findPreference(getString(R.string.pref_ratio_max));
        ratioMax.setEnabled(wrEnabled);

        // 体脂肪率下限
        ListPreference ratioMin = (ListPreference) findPreference(getString(R.string.pref_ratio_min));
        ratioMin.setEnabled(wrEnabled);

        // 全ライン同時表示
        findPreference(getString(R.string.pref_show_all_chart)).setEnabled(wrEnabled);

        // 出産予定日
        final DatePickerPreference datePicker = (DatePickerPreference) findPreference("pref_term");
        datePicker.setEnabled(pref.getBoolean(R.string.pref_show_preg));

        // 妊娠週数表示
        CheckBoxPreference pregEnabledPref = (CheckBoxPreference) findPreference(getString(R.string.pref_show_preg));
        pregEnabledPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object value) {
                if ((Boolean) value) {
                    DbUtil.updatePregWeeks(null);
                    Date term = DbUtil.getPregTerm();
                    if (term == null) {
                        return true;
                    }
                    datePicker.updateSummary();

                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setMessage(getString(R.string.preg_set_comp, DatePickerPreference.DATEFORMAT.format(term)));
                    builder.create().show();
                }
                return true;
            }
        });

//      // パスワード入力の有効無効設定
//      final CheckBoxPreference passEnabledPreference = ( CheckBoxPreference)findPreference( getString( R.string.pref_password_enabled)) ;
//      passEnabledPreference.setOnPreferenceChangeListener( new OnPreferenceChangeListener(){
//			public boolean onPreferenceChange( Preference preference, Object value) {
//				if(( Boolean)value){
//					Intent intent = new Intent( getActivity(), PasscodeActivity.class) ;
//	        		intent.putExtra( "mode", PasscodeActivity.MODE_SET) ;
//	        		startActivity( intent) ;
//				}
//				return true;
//			}
//      }) ;

        // データ表示
        PreferenceScreen printLogScreen = (PreferenceScreen) findPreference(getString(R.string.settings_printdata));
        if (printLogScreen != null) {
            printLogScreen.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    DbUtil.logDatas();
                    return true;
                }
            });
        }

        // データバックアップ
        PreferenceScreen backupScreen = (PreferenceScreen) findPreference(getString(R.string.settings_savedata));
        backupScreen.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                DbUtil.saveBackupData();
                return true;
            }
        });

        // データ復元
        PreferenceScreen restoreScreen = (PreferenceScreen) findPreference(getString(R.string.settings_readdata));
        restoreScreen.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                DbUtil.restoreDataFromSDCard();
                return true;
            }
        });

        // 祝日設定
        PreferenceScreen holidayScreen = (PreferenceScreen) findPreference(getString(R.string.settings_holiday));
        holidayScreen.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(getActivity(), HolidayListActivity.class);
                startActivity(intent);
                return true;
            }
        });

        // 開発者へメール
        PreferenceScreen contactScreen = (PreferenceScreen) findPreference(getString(R.string.settings_contact));
        contactScreen.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                AplUtil.contactUs(getActivity());
                return true;
            }
        });

        // バージョン
        PreferenceScreenEx versionScreen = (PreferenceScreenEx) findPreference(getString(R.string.version));
        try {
            PackageInfo packageInfo = getActivity().getPackageManager().getPackageInfo("com.ynosuke.android.ladical", PackageManager.GET_META_DATA);
            versionScreen.setPrefValue(packageInfo.versionName);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
    }

	/**-------------------------------------------------------------------------
	 * ビュー作成時処理を行います。
	 *------------------------------------------------------------------------*/
	@Override
	public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		View view = super.onCreateView( inflater, container, savedInstanceState) ;
//		view.setBackgroundColor( getResources().getColor( android.R.color.background_light)) ;
//        view.setBackgroundColor( getResources().getColor( android.R.color.background_dark)) ;
        return view ;
	}

	/**-------------------------------------------------------------------------
	 * アクティビティー再表示時時処理を行います。
	 *------------------------------------------------------------------------*/
	@Override
	public void onResume() {
	    super.onResume();
	    getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
	    
	    // 体調情報を更新
	    // 今日の周期
	    PreferenceScreenEx todayCycle = ( PreferenceScreenEx)findPreference( getString( R.string.settings_todaycycle)) ;
	    int cycle = pref.getInt( R.string.pref_period_cycle) ;
	    Date nearPeriod = DbUtil.getNearPeriodDay( new Date()) ;
	    if( nearPeriod == null){
	    	todayCycle.setPrefValue( "") ;
	    	todayCycle.setSummary(null) ;
	    }
	    else{
	    	int passDays = ( int)(( new Date().getTime() - nearPeriod.getTime())/(24*60*60*1000)) + 1 ;
	    	todayCycle.setPrefValue( getString( R.string.days, passDays)) ;
	    	int reachDays = cycle - passDays + 1 ;
	    	todayCycle.setSummary( getString( R.string.settings_todaycyclesummary, reachDays)) ;
	    }
	    
	    // 生理周期
	    PreferenceScreenEx cycleScreen = ( PreferenceScreenEx)findPreference( getString( R.string.settings_cyclesetting)) ;
	    cycleScreen.setPrefValue( getString( R.string.day, cycle)) ;
	    int calcTimes = pref.getInt( R.string.pref_cycle_avg_times) ;
	    cycleScreen.setSummary( getString( R.string.settings_cyclesettingsummary, calcTimes)) ;
	
	    // 生理期間
	    PreferenceScreenEx lengthScreen = ( PreferenceScreenEx)findPreference( getString( R.string.settings_periodlength)) ;
	    int length = pref.getInt( R.string.pref_period_length) ;
	    lengthScreen.setPrefValue( getString( R.string.day, length)) ;
	    lengthScreen.setSummary( getString( R.string.settings_periodlengthsummary, calcTimes)) ;
	    
	    // 入力項目の表示更新
	    for( int i = 1; i <= 10; i++){
	    	String tag = String.format( Locale.getDefault(), "param%d", i) ;
	    	ParamPreferenceScreen paramScreen = ( ParamPreferenceScreen)findPreference( tag) ;
		    paramScreen.update() ;
	    }
	    
	}
	
	/**-------------------------------------------------------------------------
	 * アクティビティー一時停止時処理を行います。
	 *------------------------------------------------------------------------*/
	@Override
	public void onPause() {
	    super.onPause();
	    getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
	}

	/**-------------------------------------------------------------------------
	 * 設定値変更時処理を行います。
	 *------------------------------------------------------------------------*/
	@Override
	public void onSharedPreferenceChanged( SharedPreferences sharedPreferences, String key) {
		if( key.equals( getString( R.string.pref_theme)) ||						// テーマが変更された場合
				key.equals( getString( R.string.pref_week_start)) ||			// または週開始用日が変更された場合
				key.equals( getString( R.string.pref_temp_max)) ||
				key.equals( getString( R.string.pref_temp_min)) ||
				key.equals( getString( R.string.pref_weight_max)) ||
				key.equals( getString( R.string.pref_weight_min)) ||
				key.equals( getString( R.string.pref_ratio_max)) ||
				key.equals( getString( R.string.pref_ratio_min))
				){	
	        ListPreferenceEx list = ( ListPreferenceEx)findPreference( key) ;
	        list.update() ;
		}
		else if( key.equals( getString( R.string.pref_weightratio_enabled))){	// 体重・体脂肪率の入力表示有効が変更された場合
			boolean enabled = pref.getPref().getBoolean( key, false) ;
			findPreference( getString( R.string.pref_weight_max)).setEnabled( enabled) ;
			findPreference( getString( R.string.pref_weight_min)).setEnabled( enabled) ;
			findPreference( getString( R.string.pref_ratio_max)).setEnabled( enabled) ;
			findPreference( getString( R.string.pref_ratio_min)).setEnabled( enabled) ;
			findPreference( getString( R.string.pref_show_all_chart)).setEnabled( enabled) ;
		}
		else if( key.equals( getString( R.string.pref_show_preg))){				// 妊娠週数表示が変更された場合
			DatePickerPreference datePicker = ( DatePickerPreference)findPreference( "pref_term") ;
			datePicker.setEnabled( pref.getPref().getBoolean( key, false)) ;
		}
	}
}

