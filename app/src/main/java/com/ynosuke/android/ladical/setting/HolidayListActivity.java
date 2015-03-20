package com.ynosuke.android.ladical.setting;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.ynosuke.android.ladical.R;
import com.ynosuke.android.ladical.data.Holiday;
import com.ynosuke.android.ladical.util.AplUtil;
import com.ynosuke.android.ladical.util.DbUtil;

//------------------------------------------------------------------------------
/**
* 祝日設定画面アクティビティーです。
*
* <p>更新履歴：
* <pre>
*     VerNo.        author              update      comment
*     Ver1.00.00    Yoshinosuke Nagaya  2014/02/28  新規作成
* </pre>
* </p>
* @author Yoshinosuke Nagaya
*/
@SuppressLint("NewApi")
//------------------------------------------------------------------------------
public class HolidayListActivity extends Activity implements OnClickListener{
	//	内部定義 ----------------------------------------------------------------
	/** 表示リスト */
	private List<Holiday> 	list ;
	
	/** カレンダーインスタンス */
	private Calendar		cal = Calendar.getInstance() ;
	
	/** リスト表示ビュー */
	private ListView 		listView ;
	
	/** 前ボタン */
	private TextView		prevButton ;
	
	/** 次ボタン */
	private TextView		nextButton ;
	
	/** 選択中の祝日 */
	private Holiday			selection ;
	
	/**-------------------------------------------------------------------------
	 * アクティビティー作成時処理を行います。
	 *------------------------------------------------------------------------*/
	@Override
	public void onCreate( Bundle savedInstanceState){
		super.onCreate( savedInstanceState) ;
		setTitle( getString( R.string.settings_holiday));
		setContentView( R.layout.activity_holidaylist) ;
		
		cal.setTime( new Date()) ;
		list = DbUtil.getHolidayList( cal.get( Calendar.YEAR)) ;				// 今年の分の祝日リストを取得
		listView = ( ListView)findViewById( android.R.id.list) ;
		listView.setOnItemClickListener( new OnItemClickListener() {
			public void onItemClick( AdapterView<?> arg0, View view, int position, long id) {
				selection = ( Holiday)listView.getItemAtPosition( position) ;
			}
		}) ;
		listView.setAdapter( new HolidayListAdapter()) ;
		
		prevButton = ( TextView)findViewById( R.id.prevButton) ;
		nextButton = ( TextView)findViewById( R.id.nextButton) ;
		prevButton.setOnClickListener( this) ;
		nextButton.setOnClickListener( this) ;									// 前後ボタンにリスナー設定
		
		updateTitle() ;															// タイトル更新
		
		View headerView = findViewById( R.id.headerView) ;
		AplUtil.setHeaderBackground( headerView) ;								// ヘッダー背景設定
	}
	
	/**-------------------------------------------------------------------------
	* メニュー作成処理を行います。
	*-------------------------------------------------------------------------*/
	@Override
	public boolean onCreateOptionsMenu( Menu menu){
		getMenuInflater().inflate( R.menu.holiday_menu, menu) ;
		return super.onCreateOptionsMenu( menu) ;
	}
	
	/**-------------------------------------------------------------------------
	 * リスナーに登録されたViewがクリックされた時の処理を行います。
	 *------------------------------------------------------------------------*/
	@Override
	public void onClick( View view){
		if( view == prevButton){
			cal.add( Calendar.YEAR, -1) ;
		}
		else if( view == nextButton){
			cal.add( Calendar.YEAR, 1) ;
		}
		updateList() ;															// リスト更新
		updateTitle() ;															// タイトル更新
	}
	
	/**-------------------------------------------------------------------------
	 * リビュー表示前処理を行います。
	 *------------------------------------------------------------------------*/
	@Override
	public void onResume(){
		super.onResume() ;
		
		updateList() ;															// リスト更新
	}
	
	/**-------------------------------------------------------------------------
	* メニュー選択時処理を行います。
	*-------------------------------------------------------------------------*/
	@Override
  public boolean onOptionsItemSelected( MenuItem item){
		int itemId = item.getItemId();
		if( itemId == R.id.menu_holiday_add){
			Intent intent = new Intent( this, HolidayAddActivity.class) ;
			intent.putExtra( "year", cal.get( Calendar.YEAR)) ;
			startActivity( intent) ;
		} else if( itemId == R.id.menu_holiday_delete){
			DbUtil.removeHoliday( selection) ;
			updateList() ;
		} else if( itemId == R.id.menu_holiday_deleteall){
			Builder builder = new Builder( this) ;
			builder.setTitle( getString( R.string.confirm)) ;
			builder.setMessage( getString( R.string.holiday_removeall_confirm, cal.get( Calendar.YEAR))) ;
			builder.setPositiveButton( "OK", new DialogInterface.OnClickListener(){
				public void onClick( DialogInterface dialog, int which) {
					DbUtil.removeHoliday( cal.get( Calendar.YEAR)) ;
					updateList() ;
				}
			}) ;
			builder.setNegativeButton( "Cancel", null) ;
			builder.show() ;
		} else if( itemId == R.id.menu_holiday_sunday){
			Builder builder = new Builder( this) ;
			builder.setTitle( getString( R.string.confirm)) ;
			builder.setMessage( getString( R.string.holiday_addsunday_confirm, cal.get( Calendar.YEAR))) ;
			builder.setPositiveButton( "OK", new DialogInterface.OnClickListener(){
				public void onClick( DialogInterface dialog, int which) {
					addSunday() ;
				}
			}) ;
			builder.setNegativeButton( "Cancel", null) ;
			builder.show() ;
		}
		return true ;
	}
	
	/**-------------------------------------------------------------------------
	* メニュー表示前処理を行います。
	*-------------------------------------------------------------------------*/
	@Override
	public boolean onPrepareOptionsMenu( Menu menu){
		MenuItem deleteItem = menu.findItem( R.id.menu_holiday_delete) ;
		deleteItem.setEnabled( selection != null) ;								// 選択の有無で削除メニュー状態設定
		
		MenuItem deleteAllItem = menu.findItem( R.id.menu_holiday_deleteall) ;
		deleteAllItem.setTitle( getString( R.string.menu_holiday_deleteall, cal.get( Calendar.YEAR))) ;
																				// 全削除メニューのタイトル設定
		return super.onPrepareOptionsMenu( menu) ;
	}
	
	/**-------------------------------------------------------------------------
	* 日曜日を祝日に登録します。
	*-------------------------------------------------------------------------*/
	private void addSunday(){
		cal.set( Calendar.MONTH, 0) ;
		cal.set( Calendar.DATE, 1) ;
		List<Holiday> list = new ArrayList<Holiday>() ;
		int year = cal.get( Calendar.YEAR) ;
		while( cal.get( Calendar.YEAR) == year){
			if( cal.get( Calendar.DAY_OF_WEEK) == Calendar.SUNDAY){
				list.add( new Holiday( cal.getTime(), "")) ;
			}
			cal.add( Calendar.DATE, 1) ;
		}
		cal.set( Calendar.YEAR, year) ;
		DbUtil.setHolidayList( list) ;
		updateList() ;
	}

	/**-------------------------------------------------------------------------
	 * リストを更新します。
	 *------------------------------------------------------------------------*/
	private void updateList(){
		listView.clearChoices() ;												// 選択解除
		selection = null ;
		list = DbUtil.getHolidayList( cal.get( Calendar.YEAR)) ;				// リスト取得												
		HolidayListAdapter adapter = ( HolidayListAdapter)listView.getAdapter() ;
		adapter.notifyDataSetChanged() ;										// 表示更新
	}
	
	/**-------------------------------------------------------------------------
	 * タイトルを更新します。
	 *------------------------------------------------------------------------*/
	private void updateTitle(){
		TextView title = ( TextView)findViewById( R.id.titleView) ;
		title.setText( getString( R.string.holiday_title, cal.get( Calendar.YEAR))) ;
	}
	
	/**-------------------------------------------------------------------------
	 * リスト表示用アダプタークラスです。
	 *------------------------------------------------------------------------*/
	private class HolidayListAdapter extends BaseAdapter{
		/**-------------------------------------------------------------------------
		 * リスト数を取得します。
		 *------------------------------------------------------------------------*/
		@Override
		public int getCount() {
			return list.size() ;
		}

		/**-------------------------------------------------------------------------
		 * 指定位置のオブジェクトを取得します。
		 *------------------------------------------------------------------------*/
		@Override
		public Holiday getItem( int pos) {
			return list.get( pos) ;
		}

		/**-------------------------------------------------------------------------
		 * 指定位置のIDを取得します。
		 *------------------------------------------------------------------------*/
		@Override
		public long getItemId( int pos) {
			return pos ;
		}
		
		/**-------------------------------------------------------------------------
		 * 行単位のビューを取得します。
		 *------------------------------------------------------------------------*/
		@Override
		public View getView( int pos, View view, ViewGroup parent) {
			if( view == null){
				LayoutInflater inflater = ( LayoutInflater)getSystemService( Context.LAYOUT_INFLATER_SERVICE) ;
				view = inflater.inflate( R.layout.row_holidaylist, null) ;
			}
			Holiday item = ( Holiday)getItem( pos) ;
			// 日付表示
			TextView textView = ( TextView)view.findViewById( R.id.date) ;
			textView.setText( item.getDate()) ;
			
			// 名称表示
			textView = ( TextView)view.findViewById( R.id.name) ;
			textView.setText( item.name) ;
			
			return view ;
		}
	}
}

