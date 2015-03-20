package com.ynosuke.android.ladical.setting;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.ynosuke.android.ladical.R;
import com.ynosuke.android.ladical.util.PrefUtil;

//------------------------------------------------------------------------------
/**
* パスコード画面アクティビティーです。
*
* <p>更新履歴：
* <pre>
*     VerNo.        author              update      comment
*     Ver1.00.00    Yoshinosuke Nagaya  2014/01/06  新規作成
* </pre>
* </p>
* @author Yoshinosuke Nagaya
*/
//------------------------------------------------------------------------------
public class PasscodeActivity extends Activity{
	//	定数定義 ----------------------------------------------------------------
	/** 表示モード（パスコード入力） */
	public static final int	MODE_INPUT = 0 ;
	
	/** 表示モード（パスコード再入力） */
	public static final int	MODE_INPUT2 = 1 ;
	
	/** 表示モード（設定） */
	public static final int	MODE_SET = 2 ;
	
	/** 表示モード（設定確認） */
	public static final int	MODE_SET2 = 3 ;
	
	/** 表示モード（設定確認間違い） */
	public static final int	MODE_SET3 = 4 ;
	
	//	内部定義 ----------------------------------------------------------------
	/** 設定値管理 */
	private PrefUtil		pref ;
	
	/** 入力文字列 */
	private String			input = "" ;
	
	/** 表示モード */
	private int				mode ;
	
	/** タイトル */
	private TextView		titleText ;
	
	/** 説明文 */
	private TextView		expText ;
	
	/** パスコード入力イメージ */
	private ImageView		image ;
	
	/** パスコード設定一回目の入力文字 */
	private String			inputFirst ;
	
	/**-------------------------------------------------------------------------
	 * アクティビティー作成時処理を行います。
	 * 
	 * @param savedInstanceState
	 *------------------------------------------------------------------------*/
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		pref = PrefUtil.getInstance( this) ;
		setContentView( R.layout.activity_passcode) ;
		
		titleText = ( TextView)findViewById( R.id.passcodeTitle) ;
		expText = ( TextView)findViewById( R.id.passcodeExp) ;
		image = ( ImageView)findViewById( R.id.passcode) ;
		
		mode = getIntent().getIntExtra( "mode", 0) ;
		
		init() ;
	}
	
	/**-------------------------------------------------------------------------
	 * キーボード押下時処理を行います。
	 * 
	 * @param view	押されたキー
	 *------------------------------------------------------------------------*/
	public void pressButton( View view){
		Button button = ( Button)view ;
		if( view.getId() == R.id.backspace){
			if( input.length() != 0){
				input = input.substring( 0, input.length() - 1) ;
				updateImage() ;
			}
		} else{
			input += button.getText() ;
			updateImage() ;
			if( input.length() == 4){
				switch( mode){
					case MODE_INPUT :
					case MODE_INPUT2 :
						if( input.equals( pref.getString( R.string.pref_password))){
							finish() ;
						}
						else{
							mode = MODE_INPUT2 ;
							init() ;
						}
						break ;
					case MODE_SET :
						inputFirst = input ;
						mode = MODE_SET2 ;
						init() ;
						break ;
					case MODE_SET2 :
					case MODE_SET3 :
						if( inputFirst.equals( input)){
							pref.putPref( R.string.pref_password, input) ;
							finish() ;
						}
						else{
							mode = MODE_SET3 ;
							
							init() ;
						}
						break ;
				}
			}
		}
	}
	
	/**-------------------------------------------------------------------------
	 *　パスコード入力状態の表示を更新します。
	 *------------------------------------------------------------------------*/
	private void updateImage(){
		switch( input.length()){
			case 0 : image.setImageResource( R.mipmap.passcode0) ; break ;
			case 1 : image.setImageResource( R.mipmap.passcode1) ; break ;
			case 2 : image.setImageResource( R.mipmap.passcode2) ; break ;
			case 3 : image.setImageResource( R.mipmap.passcode3) ; break ;
			case 4 : image.setImageResource( R.mipmap.passcode4) ; break ;
		}
	}
	
	/**-------------------------------------------------------------------------
	 *　画面状態を初期化します。
	 *------------------------------------------------------------------------*/
	private void init(){
		switch( mode){
			case MODE_INPUT :
				titleText.setText( getString( R.string.passcode_input_title)) ;
				expText.setText( getString( R.string.passcode_input_exp)) ;
				break ;
			case MODE_INPUT2 :
				titleText.setText( getString( R.string.passcode_input_title)) ;
				expText.setText( getString( R.string.passcode_input2_exp)) ;
				break ;
			case MODE_SET :
				titleText.setText( getString( R.string.passcode_set_title)) ;
				expText.setText( getString( R.string.passcode_set_exp)) ;
				break ;
			case MODE_SET2 :
				titleText.setText( getString( R.string.passcode_set_title)) ;
				expText.setText( getString( R.string.passcode_set2_exp)) ;
				break ;
			case MODE_SET3 :
				titleText.setText( getString( R.string.passcode_set_title)) ;
				expText.setText( getString( R.string.passcode_input2_exp)) ;
				break ;
		}
		input = "" ;
		updateImage() ;
	}
}

