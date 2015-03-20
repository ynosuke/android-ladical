package com.ynosuke.android.ladical.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.view.View;

import com.ynosuke.android.ladical.R;


//------------------------------------------------------------------------------
/**
* ユーティリティークラスです。
*
* <p>更新履歴：
* <pre>
*     VerNo.        author              update      comment
*     Ver1.00.00    Yoshinosuke Nagaya  2013/03/04  新規作成
*     Ver1.00.02						2014/11/24	開発者に問い合わせで宛先が入っていなかったのを修正
*     Ver1.00.03						2015/03/03	iOS版のテーマ追加（花柄、ハート柄）
* </pre>
* </p>
* @author Yoshinosuke Nagaya
*/
//------------------------------------------------------------------------------
public class AplUtil implements IGlobalPreferences{
	/** 設定画面の設定値文字色 */
	public static final int PREF_VALUE_COLOR = Color.rgb( 0x39, 0x4F, 0x72) ;
	
	/**-------------------------------------------------------------------------
	 * テーマ別の背景色を設定します。
	 *------------------------------------------------------------------------*/
    @SuppressLint("NewApi")
    @SuppressWarnings("deprecation")
	public static void setViewBackground( View view){
		// 背景を取得
		int tag ;
		Context context = view.getContext() ;
		switch( getPref( context).getInt( R.string.pref_theme)){
			case PINK_DOT :		tag = R.drawable.background_pink_dot ; break ;
			case BLUE_DOT :		tag = R.drawable.background_blue_dot ; break ;
			case ORANGE_DOT :	tag = R.drawable.background_orange_dot ; break ;
			case RED_DOT :		tag = R.drawable.background_red_dot ; break ;
			case GREEN_DOT :	tag = R.drawable.background_green_dot ; break ;
			case PINK_CHECK :	tag = R.drawable.background_pink_check ; break ;
			case BLUE_CHECK :	tag = R.drawable.background_blue_check ; break ;
			case ORANGE_CHECK :	tag = R.drawable.background_orange_check ; break ;
			case RED_CHECK :	tag = R.drawable.background_red_check ; break ;
			case GREEN_CHECK :	tag = R.drawable.background_green_check ; break ;
			case WA_SAKURA :	tag = R.drawable.background_wa_sakura ; break ;
			case WA_SEKICHIKU :	tag = R.drawable.background_wa_sekitiku ; break ;
			case WA_TORI :		tag = R.drawable.background_wa_tori ; break ;
			case WA_TAKWENOHANA :	tag = R.drawable.background_wa_hanamaru ; break ;
			case FLOWER_1 :	tag = R.drawable.background_flower_1 ; break ;
			case FLOWER_2 :	tag = R.drawable.background_flower_2 ; break ;
			case FLOWER_3 :	tag = R.drawable.background_flower_3 ; break ;
			case HEART_1 :	tag = R.drawable.background_heart_1 ; break ;
			case HEART_2 :	tag = R.drawable.background_heart_2 ; break ;
			case HEART_3 :	tag = R.drawable.background_heart_3 ; break ;
			default :			tag = R.drawable.background_monotone ;
		}
		Drawable background = context.getResources().getDrawable( tag) ;
		// ビューに設定
		int sdk = android.os.Build.VERSION.SDK_INT ;
		if( sdk < android.os.Build.VERSION_CODES.JELLY_BEAN){
			view.setBackgroundDrawable( background) ;
		}
		else {
            view.setBackground(background);
        }
	}
	
	/**-------------------------------------------------------------------------
	 * テーマ別のヘッダー背景色を設定します。
	 *------------------------------------------------------------------------*/
    @SuppressLint("NewApi")
    @SuppressWarnings("deprecation")
	public static void setHeaderBackground( View view){
		// 背景を取得
		int tag ;
		Context context = view.getContext() ;
		switch( getPref( context).getInt( R.string.pref_theme)){
			case PINK_DOT :
			case PINK_CHECK :	tag = R.drawable.background_pink_header ; break ;
			case BLUE_DOT :	
			case BLUE_CHECK :	tag = R.drawable.background_blue_header ; break ;
			case ORANGE_DOT :
			case ORANGE_CHECK :	tag = R.drawable.background_orange_header ; break ;
			case RED_DOT :
			case RED_CHECK :	tag = R.drawable.background_red_header ; break ;
			case GREEN_DOT :
			case GREEN_CHECK :	tag = R.drawable.background_green_header ; break ;
			case WA_SAKURA :	tag = R.drawable.background_wa_sakura_header ; break ;
			case WA_SEKICHIKU :	tag = R.drawable.background_wa_sekitiku_header ; break ;
			case WA_TORI :		tag = R.drawable.background_wa_tori_header ; break ;
			case WA_TAKWENOHANA :	tag = R.drawable.background_wa_hanamaru_header ; break ;
			case FLOWER_1 :		tag = R.drawable.background_flower_1_header ; break ;
			case FLOWER_2 :		tag = R.drawable.background_flower_2_header ; break ;
			case FLOWER_3 :		tag = R.drawable.background_flower_3_header ; break ;
			case HEART_1 :		tag = R.drawable.background_heart_1_header ; break ;
			case HEART_2 :		tag = R.drawable.background_heart_2_header ; break ;
			case HEART_3 :		tag = R.drawable.background_heart_3_header ; break ;
			default :			tag = R.drawable.background_monotone_header ;
		}
		Drawable background = context.getResources().getDrawable( tag) ;
		// ビューに設定
		int sdk = android.os.Build.VERSION.SDK_INT ;
		if( sdk < android.os.Build.VERSION_CODES.JELLY_BEAN){
			view.setBackgroundDrawable( background) ;
		}
		else{
			view.setBackground(background) ;
		}
	}

	/**-------------------------------------------------------------------------<br>
	 * 開発者にメールを送付します。<br>
	 *------------------------------------------------------------------------*/
	public static void contactUs( Context context){
		// 添付ファイル作成
		File destFile = new File( Environment.getExternalStorageDirectory(), "ladical.zip");
		if( destFile.exists()){
			destFile.delete() ;													// 既に存在する場合は削除しておく
		}
		File srcFile = new File( DbUtil.getPaht()).getParentFile().getParentFile() ;
		File[] list = srcFile.listFiles() ;
		archiveZip( srcFile, destFile) ;
		
		// メール送付
		Uri uri = Uri.parse ("mailto:ynosuke@gmail.com");  
        Intent intent = new Intent( Intent.ACTION_SEND, uri);
        intent.putExtra( Intent.EXTRA_EMAIL, new String[]{"ynosuke@gmail.com"});
        intent.putExtra( Intent.EXTRA_SUBJECT, "Ladi Cal 問い合わせ(Android)");  
        intent.putExtra( Intent.EXTRA_TEXT, "不具合解析用に入力されたデータのファイルが添付されています。\n送付したくない場合はladical.zipを削除して下さい。");
        Uri attach = Uri.parse( "file://" + destFile.getPath()) ;
        intent.setType( "application/zip") ;
        intent.putExtra( Intent.EXTRA_STREAM, attach) ;
        
        context.startActivity( intent);
	}
	
	/**-------------------------------------------------------------------------
	 * 設定値管理を取得します。
	 *------------------------------------------------------------------------*/
	private static PrefUtil	pref ;
	private static PrefUtil getPref( Context context){
		if( pref == null){
			pref = PrefUtil.getInstance( context) ;
		}
		return pref ;
	}
	
	/**-------------------------------------------------------------------------
	 * ZIPファイル圧縮処理を行います。
	 *------------------------------------------------------------------------*/
	public static void archiveZip( File srcDir, File dest){
		ZipOutputStream zos = null ;
		try{
			zos = new ZipOutputStream( new FileOutputStream( dest)) ;
			addZipEntry( zos, srcDir.listFiles(), srcDir.getAbsolutePath()) ;
		} catch( FileNotFoundException e){
			e.printStackTrace();
		} finally{
			if( zos != null){
				try{
					zos.close() ;
				} catch( IOException e){
					e.printStackTrace();
				}
			}	
		}
	}
	
	private static void addZipEntry( ZipOutputStream zos, File[] files, String rootPath){
		for( File file : files){
			if( file.isDirectory()){
				if( file.listFiles() != null){
					addZipEntry( zos, file.listFiles(), rootPath) ;					// ディレクトリの場合再帰処理
				}
			}
			else{
				BufferedInputStream input = null ;
				try{
					input = new BufferedInputStream( new FileInputStream( file)) ;
					
					// Entry設定
					String entryName = file.getAbsolutePath().replace( rootPath,  "").substring( 1) ;
					zos.putNextEntry( new ZipEntry( entryName));
					
					// 書き込み
					byte[] buf = new byte[ 1024] ;
					for(;;){
						int len = input.read( buf) ;
						if( len < 0) break ;
						zos.write( buf, 0, len);
					}
					zos.closeEntry();
					
				} catch( FileNotFoundException e){
					e.printStackTrace();
				} catch( IOException e){
					e.printStackTrace();
				} finally{
					if( input != null){
						try{
							input.close() ;
						} catch( IOException e){
							
						}
					}
				}
			}
		}
	}
}

