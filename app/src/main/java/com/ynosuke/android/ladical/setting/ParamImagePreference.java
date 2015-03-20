package com.ynosuke.android.ladical.setting;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.AdapterView.OnItemClickListener;

import com.ynosuke.android.ladical.R;
import com.ynosuke.android.ladical.util.IGlobalImages;
import com.ynosuke.android.ladical.util.PrefUtil;

//------------------------------------------------------------------------------
/**
* 項目イメージ選択プリファレンスです。
*
* <p>更新履歴：
* <pre>
*     VerNo.        author              update      comment
*     Ver1.00.00    Yoshinosuke Nagaya  2013/03/16  新規作成
* </pre>
* </p>
* @author Yoshinosuke Nagaya
*/
//------------------------------------------------------------------------------
public class ParamImagePreference extends DialogPreference implements IGlobalImages {

	/** 表示マーク */
	private Bitmap 	bitmap ;
	
	/** 設定項目No */
	public int		no ;
	
	/** 設定管理 */
	private PrefUtil pref ;
	
	/**-------------------------------------------------------------------------
	 * インスタンス作成時の初期化処理を行います。（xmlにてレイアウトパラメータが指定された場合に使用）
	 * 
	 * @param context	コンテキスト
	 * @param attrs		レイアウトパラメータ
	 *------------------------------------------------------------------------*/
	public ParamImagePreference( Context context, AttributeSet attrs) {
		super( context, attrs);
		init( context, attrs) ;
	}
	
	/**-------------------------------------------------------------------------
	 * インスタンス作成時の初期化処理を行います。（xmlにてスタイルが指定された場合に使用）
	 * 
	 * @param context	コンテキスト
	 * @param attrs		レイアウトパラメータ
	 * @param defStyle	スタイル
	 *------------------------------------------------------------------------*/
	public ParamImagePreference( Context context, AttributeSet attrs, int defStyle) {
		super( context, attrs, defStyle);
		init( context, attrs) ;
	}
	
	/**-------------------------------------------------------------------------
	 * 表示イメージを更新します。
	 *------------------------------------------------------------------------*/
	public void updateImage(){
		int markNo = pref.getInt( R.string.pref_param_mark, no) ;
      bitmap = BitmapFactory.decodeResource( getContext().getResources(), MARK_IDS[ markNo]);
	}
	
	/**-------------------------------------------------------------------------
	 * ダイアログ作成処理を行います。
	 *------------------------------------------------------------------------*/
	@Override
	protected View onCreateDialogView(){
		
		LayoutInflater inflater = (LayoutInflater)getContext().getSystemService( Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate( R.layout.preference_imageselect_grid, null);

		GridView gridview = ( GridView)view.findViewById( R.id.gridview);
	    gridview.setAdapter( new ImageAdapter( getContext()));

	    gridview.setOnItemClickListener( new OnItemClickListener() {
	        public void onItemClick( AdapterView<?> parent, View v, int position, long id) {
	        	// アイテムクリック時処理
	        	pref.putPref( R.string.pref_param_mark, no, position) ;			// 設定値適用
	        	updateImage() ;													// 表示イメージ更新
	        	notifyChanged() ;												// 画面更新処理
	        	ParamImagePreference.this.getDialog().dismiss() ;				// ダイアログを閉じる
	        }
	    });
		
		return view ;
	}

	/**-------------------------------------------------------------------------
	 * 画面表示時処理を行います。
	 *------------------------------------------------------------------------*/
	@Override
	protected void onBindView( View view){
		super.onBindView( view) ;
		
		ImageView imageView = ( ImageView)view.findViewById( R.id.image) ;
		imageView.setImageBitmap( bitmap) ;
	}
	
	/**-------------------------------------------------------------------------
	 * 初期化処理処理を行います。
	 *------------------------------------------------------------------------*/
	private void init( Context context, AttributeSet attrs){
		TypedArray tArray = context.obtainStyledAttributes(attrs, R.styleable.ParamImagePreference);
      no = tArray.getInt( 0, 0) ;
      tArray.recycle() ;
      
      pref = PrefUtil.getInstance( context) ;									// 設定値管理取得
      setTitle( getContext().getString( R.string.settings_paramimage));
      updateImage() ;
      setWidgetLayoutResource( R.layout.preference_imageselect) ;
      
	}
	
	/**-------------------------------------------------------------------------
	 * イメージ表示クラス
	 *------------------------------------------------------------------------*/
	private class ImageAdapter extends BaseAdapter {
	    private Context mContext;

	    public ImageAdapter(Context c) {
	        mContext = c;
	    }

	    public int getCount() {
	        return MARK_IDS.length;
	    }

	    public Object getItem(int position) {
	        return null;
	    }

	    public long getItemId(int position) {
	        return 0;
	    }

	    public View getView( int position, View convertView, ViewGroup parent) {
	    	LinearLayout view ;													// grid上に並ぶView
	        ImageView imageView;												// 上記Viewの中のイメージ表示用
	        if( convertView == null){
	        	view = new LinearLayout( mContext) ;
	        	view.setGravity( Gravity.CENTER) ;
	        	view.setPadding( 50, 50, 50, 50) ;
	        	
	            imageView = new ImageView( mContext);
	            view.addView( imageView) ;
	        } 
	        else {
	        	view = ( LinearLayout)convertView ;
	        	imageView = ( ImageView)view.getChildAt( 0) ;
	        }

	        imageView.setImageResource( MARK_IDS[ position]);					// 表示イメージ設定
	        
	        int markNo = pref.getInt( R.string.pref_param_mark, no) ;
	        if( position == markNo){					
	        	view.setBackgroundColor( Color.LTGRAY) ;						// 設定中の項目なら背景変更
	        }
	        
	        return view;
	    }
	}
}

