package com.ynosuke.android.ladical.setting;

import java.util.List;

import android.content.Context;
import android.preference.DialogPreference;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.ynosuke.android.ladical.R;
import com.ynosuke.android.ladical.data.PeriodItem;
import com.ynosuke.android.ladical.util.DbUtil;

//------------------------------------------------------------------------------
/**
* 生理日一覧表示ダイアログです。
*
* <p>更新履歴：
* <pre>
*     VerNo.        author              update      comment
*     Ver1.00.00    Yoshinosuke Nagaya  2013/12/18  新規作成
* </pre>
* </p>
* @author Yoshinosuke Nagaya
*/
//------------------------------------------------------------------------------
public class PeriodListPreference extends DialogPreference{
	//	内部定義 ----------------------------------------------------------------
	/** 生理日情報リスト */
	private List<PeriodItem> 	list ;
	
	/**-------------------------------------------------------------------------
	 * コンストラクタ
	 *------------------------------------------------------------------------*/
	public PeriodListPreference( Context context, AttributeSet attrs) {
		super( context, attrs);
		setNegativeButtonText( null);  // キャンセルボタンを非表示にする
	}

	/**-------------------------------------------------------------------------
	 * ダイアログビューを作成します。
	 *------------------------------------------------------------------------*/
	@Override
	protected View onCreateDialogView(){
		PeriodListAdapter adapter = new PeriodListAdapter() ;
		list = DbUtil.getPeriodList() ;
		
		ListView listView = new ListView( getContext()) ;
		listView.setAdapter( adapter) ;
		
		return listView ;
	}
	
	/**-------------------------------------------------------------------------
	 * リスト表示用アダプタークラスです。
	 *------------------------------------------------------------------------*/
	private class PeriodListAdapter extends BaseAdapter{
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
		public Object getItem( int pos) {
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
				LayoutInflater inflater = ( LayoutInflater)getContext().getSystemService( Context.LAYOUT_INFLATER_SERVICE) ;
				view = inflater.inflate( R.layout.row_periodlist, null) ;
			}
			PeriodItem item = ( PeriodItem)getItem( pos) ;
			// 開始日表示
			TextView textView = ( TextView)view.findViewById( R.id.periodlist_row_date) ;
			textView.setText( DateFormat.format( "yyyy-MM-dd", item.date)) ;
			// 周期表示
			textView = ( TextView)view.findViewById( R.id.periodlist_row_cycle) ;
			textView.setText( String.valueOf( item.cycle)) ;
			// 期間表示
			textView = ( TextView)view.findViewById( R.id.periodlist_row_length) ;
			textView.setText( String.valueOf( item.length)) ;
			return view;
		}
	}
}

