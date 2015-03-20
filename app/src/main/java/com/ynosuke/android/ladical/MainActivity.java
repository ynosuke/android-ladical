package com.ynosuke.android.ladical;

import java.io.FileNotFoundException;
import java.io.InputStream;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;
import android.widget.Button;
import android.widget.TabHost;

import com.ynosuke.android.ladical.calendar.CalendarFragment;
import com.ynosuke.android.ladical.graph.GraphFragment;
import com.ynosuke.android.ladical.setting.SettingFragment;
import com.ynosuke.android.ladical.util.DbUtil;

//------------------------------------------------------------------------------
/**
* メイン画面アクティビティーです。
*
* <p>更新履歴：
* <pre>
*     VerNo.        author              update      comment
*     Ver1.00.00    Yoshinosuke Nagaya  2013/12/14  新規作成
*     Ver1.00.03                        2015/03/11  タブの方式をandroid5にも対応できるよう変更
* </pre>
* </p>
* @author Yoshinosuke Nagaya
*/
//------------------------------------------------------------------------------
public class MainActivity extends FragmentActivity {
	//	定数定義 ----------------------------------------------------------------	
	/** フラグメントタグ（カレンダー） */
	private static final String TAG_CALENDAR = "tag_calendar" ;
	
	/** フラグメントタグ（グラフ） */
	private static final String TAG_GRAPH = "tag_graph" ;
	
	/** フラグメントタグ（設定） */
	private static final String TAG_SETTING = "tag_setting" ;
	
	//	内部定義 ----------------------------------------------------------------
	
	/**-------------------------------------------------------------------------
	 * 作成時処理を行います。
	 *------------------------------------------------------------------------*/
	@Override
	protected void onCreate( Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView( R.layout.activity_main) ;
	
		DbUtil.openDatabase( this) ;											// データベースオープン
	      
		// タブ設定
        FragmentTabHost host = ( FragmentTabHost)findViewById( android.R.id.tabhost) ;
        host.setup( this, getSupportFragmentManager(), R.id.content) ;

        TabHost.TabSpec tab1 = host.newTabSpec( TAG_CALENDAR) ;
        tab1.setIndicator( getString(R.string.calendar), getResources().getDrawable( R.mipmap.tab_calendar)) ;
        host.addTab( tab1, CalendarFragment.class, null);

        TabHost.TabSpec tab2 = host.newTabSpec( TAG_GRAPH) ;
        tab2.setIndicator( getString(R.string.graph), getResources().getDrawable( R.mipmap.tab_graph)) ;
        host.addTab( tab2, GraphFragment.class, null);

        TabHost.TabSpec tab3 = host.newTabSpec( TAG_SETTING) ;
        tab3.setIndicator( getString(R.string.setting), getResources().getDrawable( R.mipmap.tab_setting)) ;
        host.addTab( tab3, SettingFragment.class, null);

//		// パスコード入力画面表示
//		pref = PrefUtil.getInstance( this) ;
//		if( pref.getBoolean( R.string.pref_password_enabled)){
//	      	Intent intent = new Intent( this, PasscodeActivity.class) ;
//	      	startActivity( intent) ;
//		}
	      
		// バックアップファイルから起動された場合の復元処理
		Intent intent = getIntent() ;
		if( intent.getAction().equals( Intent.ACTION_VIEW)){
			try{
				InputStream inputStream = getContentResolver().openInputStream( getIntent().getData() );
				DbUtil.restoreDataFromMail( inputStream); ;
			} catch( FileNotFoundException e){
				e.printStackTrace();
			}
		}
	}

	/**-------------------------------------------------------------------------
	 * カレンダー画面フラグメントを取得します。
	 *------------------------------------------------------------------------*/
	public CalendarFragment getCalendarFragment(){
		return ( CalendarFragment)getSupportFragmentManager().findFragmentByTag( TAG_CALENDAR) ;
	}
}