package com.ynosuke.android.ladical.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.os.Environment;
import android.util.Log;

import com.ynosuke.android.ladical.MainActivity;
import com.ynosuke.android.ladical.R;
import com.ynosuke.android.ladical.calendar.CalendarFragment;
import com.ynosuke.android.ladical.data.Holiday;
import com.ynosuke.android.ladical.data.Item;
import com.ynosuke.android.ladical.data.PeriodItem;

//------------------------------------------------------------------------------
/**
* データベース管理クラスです。
*
* <p>更新履歴：
* <pre>
*     VerNo.        author              update      comment
*     Ver1.00.00    Yoshinosuke Nagaya  2013/01/05  新規作成
*     Ver1.00.02						2014/10/08	生理開始日の設定処理の不具合を修正
*     Ver1.00.02						2014/11/04	妊娠週数の更新処理で、現設定の削除処理を修正
*     Ver1.00.02						2014/11/10	Cursorをcloseしない為エラーが発生していた不具合を修正
*     Ver1.00.03						2015/02/24	バックアップにバージョンを記すように対応
*     Ver1.00.03						2015/02/25	バックアップ復元処理でios版のパラメータが復元できない不具合修正、また仕様をiosに合わせた
* </pre>
* </p>
* @author Yoshinosuke Nagaya
*/
//------------------------------------------------------------------------------
@SuppressLint({ "SimpleDateFormat", "DefaultLocale" })
public class DbUtil {
	//	定数定義 ----------------------------------------------------------------
	/** データベース構造バージョン */
	private final static int DB_VERSION = 1;
	
	/** データベース名称 */
	private final static String	DB_NAME = "ladical_db" ;
	
	/** テーブル名称 */
	private final static String TABLE = "calendar" ;
	
	/** テーブル名称（祝日） */
	private final static String TABLE_HOLIDAY = "holiday" ;
	
	/** DB格納用日付フォーマット */
	public final static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat( "yyyy-MM-dd") ;
	
	/** 全パラメータ指定時文字列 */
	private static final String	ALL_PARAMS = "date,temp,weight,ratio,memo,periodStart,willPeriod,willOv,pregWeeks,param1,param2,param3,param4,param5,param6,param7,param8,param9,param10" ;

	/** 生理日・排卵日予想期間 */
	private static final int WILL_SPAN = 180 ; // 180日先まで予測日を設定する
	
	/** 1日のミリ秒 */
	public static final long ONEDAY = 24*60*60*1000 ;
	
	/** プリファレンス型（int）*/
	private static final String INT = "INT" ;
	
	/** プリファレンス型（long）*/
	private static final String LONG = "LONG" ;
	
	/** プリファレンス型（float）*/
	private static final String FLOAT = "FLOAT" ;
	
	/** プリファレンス型（string）*/
	private static final String STRING = "STRING" ;
	
	/** プリファレンス型（boolean）*/
	private static final String BOOLEAN = "BOOLEAN" ;
	
	//	内部定義 ----------------------------------------------------------------
	/** データベースオブジェクト */
	private static SQLiteDatabase 	db ;
	
	/** 設定値管理 */
	private static PrefUtil			pref ;
	
	/** コンテキスト */
	private static Context			context ;
	
	/** 日付計算用カレンダー */
	private static Calendar			cal = Calendar.getInstance() ;
	
	/**-------------------------------------------------------------------------
	 * データベースを開きます。
	 * 
	 * @param context	コンテキスト
	 *------------------------------------------------------------------------*/
	public static void openDatabase( Context context){
		pref = PrefUtil.getInstance( context) ;
		DbUtil.context = context ;
		DatabaseOpenHelper helper = new DatabaseOpenHelper( context) ;
		try{
			db = helper.getWritableDatabase() ;
		} catch( SQLiteException e){
			System.out.println( e);
		}
	}
	
	public static String getPaht(){
		return db.getPath() ;
	}
	
	/**-------------------------------------------------------------------------
	 * 日付を文字列に変換します。
	 * 
	 * @param date	Dateオブジェクト
	 *------------------------------------------------------------------------*/
	public static String formatDate( Date date){
		return DATE_FORMAT.format( date) ;
	}
	
	/**-------------------------------------------------------------------------
	 * 文字列を日付に変換します。
	 * 
	 * @param str	日付文字列
	 *------------------------------------------------------------------------*/
	public static Date parseDate( String str){
		try{
			return DATE_FORMAT.parse( str) ;
		} catch( ParseException e){
			e.printStackTrace();
			return null ;
		}
	}
	
	/**-------------------------------------------------------------------------
	 * 指定日付のItemを取得します。
	 * 
	 * @param date	取得するItemの日付
	 *------------------------------------------------------------------------*/
	public static Item getItem( Date date){
		Item item = null;
		Cursor cursor = null;
		try{
			String sql = String.format( "SELECT %s FROM %s WHERE date = ?", ALL_PARAMS, TABLE) ;
			cursor = db.rawQuery( sql, new String[]{ DbUtil.formatDate( date)}) ;
			cursor.moveToFirst() ;
			if( cursor.getCount() == 0){											// 指定日付のItemがなかった場合
				cursor.close();
				return null ;
			}
			item = new Item( cursor) ;
		} catch( SQLiteException e){
			e.printStackTrace();
		} finally{
			if( cursor != null){
				cursor.close();
			}
		}
		return item ;
	}
	
	/**-------------------------------------------------------------------------
	 * 指定期間のItemリストを取得します。
	 * 
	 * @param start	開始日付
	 * @param end	終了日付
	 *------------------------------------------------------------------------*/
	public static Map<Date,Item> getItemList( Date start, Date end){
		Map<Date,Item> list = new TreeMap<Date,Item>() ;
		Cursor cursor = null ;
		try{
			String sql = String.format( "SELECT %s FROM %s WHERE ( date >= ?) AND ( date <= ?)", ALL_PARAMS, TABLE) ;
			cursor = db.rawQuery( sql, new String[]{ formatDate( start), formatDate( end)}) ;
			cursor.moveToFirst() ;
			int count = cursor.getCount() ;
			for( int i = 0; i < count; i++){
				Item item = new Item( cursor) ;
				list.put( item.date, item) ;
				cursor.moveToNext() ;
			}
		} catch( SQLiteException e){
			e.printStackTrace();
		} finally{
			if( cursor != null){
				cursor.close();
			}
		}
		return list ;
	}
	
	/**-------------------------------------------------------------------------
	 * 指定年の祝日リストを取得します。
	 * 
	 * @param year 指定年
	 *------------------------------------------------------------------------*/
	public static List<Holiday> getHolidayList( int year){
		cal.set( Calendar.YEAR, year) ;
		cal.set( Calendar.MONTH, 0) ;
		cal.set( Calendar.DATE, 1) ;
		Date start = cal.getTime() ;											// 1月1日取得
		cal.add( Calendar.YEAR, 1) ;
		cal.add( Calendar.DATE, -1) ;
		Date end = cal.getTime() ;												// 12月31日取得
		return getHolidayList( start, end) ;
	}
	
	/**-------------------------------------------------------------------------
	 * 指定期間の祝日リストを取得します。
	 * 
	 * @param start	開始日付
	 * @param end	終了日付
	 *------------------------------------------------------------------------*/
	public static List<Holiday> getHolidayList( Date start, Date end){
		List<Holiday> list = new ArrayList<Holiday>() ;
		String sql = String.format( "SELECT * FROM %s WHERE ( date >= ?) AND ( date <= ?)", TABLE_HOLIDAY) ;
		Cursor cursor = null ;
		try{
			cursor = db.rawQuery( sql, new String[]{ formatDate( start), formatDate( end)}) ;
			cursor.moveToFirst() ;
			int count = cursor.getCount() ;
			for( int i = 0; i < count; i++){
				Holiday holiday = new Holiday( cursor) ;
				list.add( holiday) ;
				cursor.moveToNext() ;
			}
		} catch( SQLiteException e){
			
		} finally {
			if( cursor != null){
				cursor.close();
			}
		}
		return list ;
	}
	
	/**-------------------------------------------------------------------------
	 * カレンダーデータをログ出力します。
	 *------------------------------------------------------------------------*/
	public static void logDatas(){
		Log.d( "calendar", "date         pest wilp wilo week prm1 prm2 prm3 prm4 prm5 prm6 prm7 prm8 prm9 prm10 temp weig rati   memo") ;
		Cursor cursor = null ;
		try{
			String sql = String.format( "SELECT %s FROM %s ORDER BY date ASC", ALL_PARAMS, TABLE) ;
			cursor = db.rawQuery( sql, null) ;
			cursor.moveToFirst() ;
			int count = cursor.getCount() ;
			for( int i = 0; i < count; i++){
				Item item = new Item( cursor) ;
				String log = String.format( Locale.getDefault(), "%s %4d %4d %4d %4d %4d %4d %4d %4d %4d %4d %4d %4d %4d %4d    %.2f %.2f %.2f %s", 
						DATE_FORMAT.format( item.date),
						item.periodStart ? 1 : 0,
						item.willPeriod ? 1 : 0,
						item.willOv ? 1 : 0,
						item.pregWeeks,
						item.param[0] ? 1 : 0,
						item.param[1] ? 1 : 0,
						item.param[2] ? 1 : 0,
						item.param[3] ? 1 : 0,
						item.param[4] ? 1 : 0,
						item.param[5] ? 1 : 0,
						item.param[6] ? 1 : 0,
						item.param[7] ? 1 : 0,
						item.param[8] ? 1 : 0,
						item.param[9] ? 1 : 0,
						item.temp,
						item.weight,
						item.ratio,
						item.memo) ;
				Log.d( "calendar", log) ;

				cursor.moveToNext() ;
			}
		} catch( SQLiteException e){
			e.printStackTrace();
		} finally{
			if( cursor != null){
				cursor.close();
			}
		}
	}
	
	/**-------------------------------------------------------------------------
	 * 生理日情報のリストを取得します。
	 *------------------------------------------------------------------------*/
	public static List<PeriodItem> getPeriodList(){
		List<PeriodItem> list = new ArrayList<PeriodItem>() ;
		String sql = String.format( "SELECT date,periodStart from %s WHERE param1 = 1 ORDER BY date ASC", TABLE) ;
		Cursor cursor = null ;
		try{
			cursor = db.rawQuery( sql, null) ;
			cursor.moveToFirst() ;
			PeriodItem periodItem = null ;
			while( !cursor.isAfterLast()){
				Date date = parseDate( cursor.getString( 0)) ;
				if( cursor.getInt( 1) == 1){									// 開始日の場合
					int cycle = 0 ;
					if( periodItem != null){
						cycle = ( int)(( date.getTime() - periodItem.date.getTime()) / ONEDAY) ;
					}
					periodItem = new PeriodItem( date, 0, cycle) ;
					list.add( periodItem) ;
				}
				periodItem.length++ ;
				if( !cursor.moveToNext()){
					break ;
				}
			}
		} catch( SQLiteException e){
			e.printStackTrace();
		} finally{
			if( cursor != null){
				cursor.close();
			}
		}
		return list ;
	}
	
	/**-------------------------------------------------------------------------
	 * Itemをデータベースに格納します。
	 *------------------------------------------------------------------------*/
	public static void setItem( Item item, boolean changePeriod){
		ContentValues values = item.getContentValues() ;
		db.replace( TABLE, null, values) ;										// データ追加または更新する
		
		if( changePeriod){	// 生理中フラグの変更があった場合
			// 生理開始日の設定を更新
			Item periodStartItem = searchPeriodStart( item) ;
			if( periodStartItem != null){
				setPeriodStart( periodStartItem, true) ;						// 生理開始をONに更新
				updatePeriodStart( periodStartItem) ;							// 後の生理開始をOFFに更新
			}
			
			// 生理周期・期間を更新
			updatePeriodCycle() ;
			updatePeriodLength() ;
			
			// 生理・排卵予測を更新
			updateWillPeriodOvulation() ;
		}
	}
	
	/**-------------------------------------------------------------------------
	 * 祝日をデータベースから削除します。
	 *------------------------------------------------------------------------*/
	public static void removeHoliday( Holiday holiday){
		db.delete( TABLE_HOLIDAY, "date=?", new String[]{ holiday.getDate()}) ;
	}
	
	/**-------------------------------------------------------------------------
	 * 祝日をデータベースから削除します。
	 *------------------------------------------------------------------------*/
	public static void removeHoliday( int year){
		db.delete( TABLE_HOLIDAY, String.format( "date LIKE '%d%%'", year), null) ;
	}
	
	/**-------------------------------------------------------------------------
	 * 祝日をデータベースに格納します。
	 *------------------------------------------------------------------------*/
	public static void setHoliday( Date date, String name){
		ContentValues values = new ContentValues() ;
		values.put( "date", DATE_FORMAT.format( date)) ;
		values.put( "name", name) ;
		db.replace( TABLE_HOLIDAY, null, values) ;
	}
	
	/**-------------------------------------------------------------------------
	 * 祝日リストをデータベースに格納します。
	 *------------------------------------------------------------------------*/
	public static void setHolidayList( List<Holiday> list){
		db.beginTransaction() ;
		try{
			SQLiteStatement statement = db.compileStatement( String.format( "INSERT OR REPLACE INTO %s(date,name) values(?,?)", TABLE_HOLIDAY)) ;
			for( Holiday holiday : list){
				statement.bindString( 1, holiday.getDate()) ;
				statement.bindString( 2, holiday.name) ;
				statement.execute() ;
			}
			db.setTransactionSuccessful() ;
		} finally{
			db.endTransaction() ;
		}
	}
	
	/**-------------------------------------------------------------------------
	 * 指定日付から最も近い過去の生理開始日を取得します。
	 *------------------------------------------------------------------------*/
	public static Date getNearPeriodDay( Date date){
		Cursor cursor = null ;
		try{
			String sql = String.format( "SELECT date FROM %s WHERE (periodStart = 1) AND (date < ?) ORDER BY date DESC LIMIT 1 ", TABLE) ;
			cursor = db.rawQuery( sql, new String[]{ formatDate( date)}) ;
			if( cursor.getCount() != 0){
				cursor.moveToFirst() ;
				return parseDate( cursor.getString( 0)) ;
			}
		} catch( SQLiteException e){
			e.printStackTrace();
		} finally{
			if( cursor != null){
				cursor.close() ;
			}
		}
		return null ;
	}
	
	/**-------------------------------------------------------------------------
	 * 出産予定日を取得します。（現在より未来のものに限る）
	 *------------------------------------------------------------------------*/
	public static Date getPregTerm(){
		Cursor cursor = null ;
		try{
			String sql = String.format( "SELECT date FROM %s WHERE (pregWeeks = -1) AND (date > ?) ORDER BY date DESC LIMIT 1 ", TABLE) ;
			cursor = db.rawQuery( sql, new String[]{ formatDate( new Date())}) ;
			if( cursor.getCount() != 0){
				cursor.moveToFirst() ;
				return parseDate( cursor.getString( 0)) ;
			}
		} catch( SQLiteException e){
			e.printStackTrace();
		} finally{
			if( cursor != null){
				cursor.close();
			}
		}
		return null ;
	}
	
	/**-------------------------------------------------------------------------
	 * 妊娠週数と出産予定日を更新します。
	 *------------------------------------------------------------------------*/
	public static void updatePregWeeks( Date newTerm){
		// 現在の週数設定を削除
		Date lastTerm = getPregTerm() ;
		if( lastTerm != null){
			ContentValues values = new ContentValues() ;
			values.put( "pregWeeks", 0) ;
			db.update( TABLE, values, 
					"pregWeeks!=0 AND date > ?", 
					new String[]{ formatDate( new Date())}) ;
		}
		
		// 最終生理開始日から7日おきに妊娠週数を設定
		Date lastPeriod = getNearPeriodDay( new Date()) ;
		
		if( lastPeriod != null){
			if( newTerm == null){
				newTerm = new Date( lastPeriod.getTime() + 280*ONEDAY) ;			// 出産予定日が指定されない場合は生理日から280日後とする
			}
			Date date = ( Date)lastPeriod.clone() ;
			int week = 1 ;
			while( date.before( newTerm)){
				ContentValues values = new ContentValues() ;
				values.put( "pregWeeks", week) ;
				values.put( "date", formatDate( date)) ;
				insertOrUpdate( date, values) ;

				date = new Date( date.getTime() + 7*ONEDAY) ;
				week++ ;
			}
		}
		
		// 出産予定日を設定
		if( newTerm != null){
			ContentValues values = new ContentValues() ;
			values.put( "pregWeeks", Item.TERM) ;
			values.put( "date", formatDate( newTerm)) ;
			insertOrUpdate( newTerm, values) ;
		}
	}
	 
	/**-------------------------------------------------------------------------<br>
	 * バックアップデータをSDカードに保存します。<br>
	 *------------------------------------------------------------------------*/
	public static void saveBackupData(){
		// 保存ディレクトリ作成
		File dir = new File( Environment.getExternalStorageDirectory().getPath() + "/LadiCal") ;
		if( !dir.exists()){
			dir.mkdir() ;
		}
		SimpleDateFormat format = new SimpleDateFormat( "yyyyMMdd_hhmm") ;
		String fileName = String.format( "LadiCal_%s.csv", format.format( new Date())) ;
		File file = new File( dir.getAbsolutePath() + "/" + fileName) ;
		if( file.exists()){
			file.delete() ;
		}
		
		PrintWriter writer = null ;
		String message = null ;
		Cursor cursor = null ;
		try{
			// バックアップファイル作成
			FileOutputStream out = new FileOutputStream( file.getAbsolutePath()) ;
			writer = new PrintWriter( new OutputStreamWriter( out, "UTF-8")) ;
			writer.write( "Ladi Cal backup, 1\n");								// バックアップバージョン書き込み
			
			// カレンダ−データ書き込み
			writer.write( "Data Header,date,periodStart,willPeriod,willOv,pregWeek,prm1,prm2,prm3,prm4,prm5,prm6,prm7,prm8,prm9,prm10,temp,weight,ratio,memo\n") ;
			String sql = String.format( "SELECT %s FROM %s ORDER BY date ASC", ALL_PARAMS, TABLE) ;
			cursor = db.rawQuery( sql, null) ;
			cursor.moveToFirst() ;
			int count = cursor.getCount() ;
			for( int i = 0; i < count; i++){
				Item item = new Item( cursor) ;
				String log = String.format( Locale.getDefault(), "data,%s,%s,%s,%s,%d,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%.2f,%.2f,%.2f,%s\n", 
						DATE_FORMAT.format( item.date),
						item.periodStart ? "1" : "0",
						item.willPeriod ? "1" : "0",
						item.willOv ? "1" : "0",
						item.pregWeeks,
						item.param[0] ? "1" : "0",
						item.param[1] ? "1" : "0",
						item.param[2] ? "1" : "0",
						item.param[3] ? "1" : "0",
						item.param[4] ? "1" : "0",
						item.param[5] ? "1" : "0",
						item.param[6] ? "1" : "0",
						item.param[7] ? "1" : "0",
						item.param[8] ? "1" : "0",
						item.param[9] ? "1" : "0",
						item.temp,
						item.weight,
						item.ratio,
						item.memo.replaceAll( "\n", "<br>")) ;
				writer.write( log) ;

				cursor.moveToNext() ;
			}
			
			// プリファレンス書き込み
			writer.write( "Pref Header, key, type, value\n") ;
			writePrefData( writer, INT, R.string.pref_theme) ;					// テーマ
			writePrefData( writer, FLOAT, R.string.pref_temp_min) ;				// 体温下限
			writePrefData( writer, FLOAT, R.string.pref_temp_max) ;				// 体温上限
			writePrefData( writer, FLOAT, R.string.pref_weight_min) ;			// 体重下限
			writePrefData( writer, FLOAT, R.string.pref_weight_max) ;			// 体重上限
			writePrefData( writer, FLOAT, R.string.pref_ratio_min) ;			// 体脂肪率下限
			writePrefData( writer, FLOAT, R.string.pref_ratio_max) ;			// 体脂肪率上限
//			writePrefData( writer, BOOLEAN, R.string.pref_show_tempborder) ;	// 体温基準ライン表示
//			writePrefData( writer, INT, R.string.pref_tempborder_settype) ;		// 体温基準値設定区分
//			writePrefData( writer, FLOAT, R.string.pref_temp_avg) ;				// 平均体温
//			writePrefData( writer, FLOAT, R.string.pref_temp_fix) ;				// 固定体温
//			writePrefData( writer, INT, R.string.pref_temp_avg_span) ;			// 平均体温の集計期間
			writePrefData( writer, BOOLEAN, R.string.pref_weightratio_enabled) ;// 体重体脂肪率管理有効
			writePrefData( writer, INT, R.string.pref_week_start) ;				// 週開始曜日
			writePrefData( writer, BOOLEAN, R.string.pref_show_blank_line) ;	// 未記入域描画
			writePrefData( writer, STRING, R.string.pref_address) ;				// Eメールアドレス
			writePrefData( writer, INT, R.string.pref_cycle_settype) ;			// 生理周期の設定区分
			writePrefData( writer, INT, R.string.pref_period_cycle) ;			// 生理周期
			writePrefData( writer, INT, R.string.pref_period_length) ;			// 生理期間
			writePrefData( writer, INT, R.string.pref_cycle_avg_times) ;		// 生理周期の集計回数
//			writePrefData( writer, INT, R.string.pref_digit_temp) ;				// 体温入力桁数
//			writePrefData( writer, INT, R.string.pref_digit_weight) ;			// 体重入力桁数
//			writePrefData( writer, INT, R.string.pref_digit_ratio) ;			// 体脂肪率入力桁数
			writePrefData( writer, BOOLEAN, R.string.pref_show_preg) ;			// 妊娠週数表示
			writePrefData( writer, BOOLEAN, R.string.pref_show_all_chart) ;		// 全ライン同時表示
			for( int i = 1; i <= 10; i++){
				writePrefData( writer, STRING, R.string.pref_param_name, i) ;	// パラメータ名称
				writePrefData( writer, INT, R.string.pref_param_mark, i) ;		// パラメータマーク
				writePrefData( writer, BOOLEAN, R.string.pref_param_enabled, i) ;// パラメータ使用
				writePrefData( writer, BOOLEAN, R.string.pref_param_display, i) ;// パラメータ表示
			}
			
			// 祝日データ書き込み
			writer.write( "Holiday Header,date,name\n") ;
			sql = String.format( "SELECT date,name FROM %s ORDER BY date ASC", TABLE_HOLIDAY) ;
			cursor = db.rawQuery( sql, null) ;
			cursor.moveToFirst() ;
			count = cursor.getCount() ;
			for( int i = 0; i < count; i++){
				Holiday item = new Holiday( cursor) ;
				String log = String.format( "holiday,%s,%s\n", 
						DATE_FORMAT.format( item.date),
						item.name) ;
				writer.write( log) ;

				cursor.moveToNext() ;
			}
			
			writer.close() ;
			message = context.getString( R.string.backup_saved) ;
		} catch( IOException e){
			message = context.getString( R.string.backup_save_failed) ;
			e.printStackTrace() ;
		} catch( SQLiteException e){
			e.printStackTrace();
		} finally{
			if( cursor != null){
				cursor.close();
			}
		}
		
		// 完了ダイアログ表示
		AlertDialog.Builder builder = new AlertDialog.Builder( context) ;
		builder.setMessage( message) ;
		builder.setPositiveButton( "OK", null) ;
		builder.create().show() ;
	}
	
	/**-------------------------------------------------------------------------<br>
	 * SDカードのバックアップからデータを復元します。<br>
	 *------------------------------------------------------------------------*/
	public static void restoreDataFromSDCard(){
		// バックアップファイル一覧取得
		final File dir = new File( Environment.getExternalStorageDirectory().getPath() + "/LadiCal") ;
		final List<String> fileNames = new ArrayList<String>() ;
		if( dir != null){
			for( File file : dir.listFiles()){
				if( file.getName().startsWith( "LadiCal") && file.getName().endsWith( ".csv")){
					fileNames.add( file.getName()) ;
				}
			}
		}
		if( fileNames.size() == 0){												// ファイルがなかった場合
			AlertDialog.Builder builder = new AlertDialog.Builder( context) ;
			builder.setMessage( context.getString( R.string.backup_none)) ;
			builder.setPositiveButton( "OK", null) ;
			builder.create().show() ;
			return ;
		}
		
		// ファイル選択ダイアログ表示
		AlertDialog.Builder builder = new AlertDialog.Builder( context) ;
		builder.setTitle( context.getText( R.string.backup_read_title)) ;
		builder.setItems( fileNames.toArray( new CharSequence[ fileNames.size()]), new DialogInterface.OnClickListener(){
			public void onClick( DialogInterface doalog, int witch){
				File file = new File( dir.getAbsolutePath() + "/" + fileNames.get( witch)) ;
				BufferedReader reader = null ;
				try{
					FileReader fileReader = new FileReader( file) ;
					reader = new BufferedReader( fileReader) ;
					readBackupData( reader) ;									// 復元処理
				} catch( IOException e){
					e.printStackTrace() ;
				} finally{
					try{
						reader.close() ;
					} catch( IOException e){
						e.printStackTrace();
					}
				}
			}
		}) ;
		builder.create().show() ;
	}
	
	/**-------------------------------------------------------------------------<br>
	 *  メール添付ファイルからの復元処理を行います。<br>
	 *------------------------------------------------------------------------*/
	public static void restoreDataFromMail( InputStream in){
		BufferedReader reader = null ;
		try{
			reader = new BufferedReader( new InputStreamReader( in)) ;
			if( reader.markSupported()){
				reader.mark( 1024);
			}
			
			int version = 0 ;
			String line ;
			while(( line = reader.readLine()) != null){
				if( line.startsWith( "Ladi Cal backup")){
					version = Integer.parseInt(  line.split( ",")[1].trim());
				}
				if( line.startsWith( "data") || line.startsWith( "setting")){
					break ;
				}
			}
			reader.reset();
			if( version == 0){
				readBackupData_oldBackupFile( reader);
			}
			else{
				readBackupData( reader);
			}
			
		} catch( IOException e){
			e.printStackTrace() ;
		} finally{
			try{
				reader.close() ;
			} catch( IOException e){
				e.printStackTrace();
			}
		}
	}
	
	/**-------------------------------------------------------------------------<br>
	 * バックアップファイルの読み込み処理を行います。<br>
	 *------------------------------------------------------------------------*/
	private static void readBackupData( BufferedReader reader) throws IOException{
		// ファイル読み込み
		final List<Item> itemList = new ArrayList<Item>() ;
		final List<Pref> prefList = new ArrayList<Pref>() ;
		final List<Holiday> holidayList = new ArrayList<Holiday>() ;
		int temp = 0 ;
		int weight = 0 ;
		int ratio = 0 ;
		String line ;
		while(( line = reader.readLine()) != null){
			String[] text = line.split( ",") ;
			if( text.length == 0){
				continue ;
			}
			if( text[0].equals( "data")){
				// データ登録
				try{
					Date date = DATE_FORMAT.parse( text[1]);
					Item item = new Item( date) ;
					item.periodStart = checkBool( text[2]) ;
					item.willPeriod = checkBool( text[3]) ;
					item.willOv = checkBool( text[4]) ;
					item.pregWeeks = Integer.parseInt( text[5]) ;
					for( int i = 0; i < 10; i++){
						item.param[i] = checkBool( text[6 + i]) ;
					}
					item.temp = Float.parseFloat( text[16]) ;
					item.weight = Float.parseFloat( text[17]) ;
					item.ratio = Float.parseFloat( text[18]) ;
					if( text.length == 20){
						item.memo = text[19].replaceAll( "<br>", "\n") ;
					}
					itemList.add( item) ;
					if( item.temp != 0) temp++ ;
					if( item.weight != 0) weight++ ;
					if( item.ratio != 0) ratio++ ;
				} catch( ParseException e){
					e.printStackTrace();
				}
			}
			else if( text[0].equals( "pref")){
				// パラメータ登録
				Pref pref = new Pref( text[1]) ;
				pref.type = text[2] ;
				pref.value = text.length == 4 ? text[3] : "" ;
				prefList.add( pref) ;
			}
			else if( text[0].equals( "holiday")){
				// 祝日データ登録
				try{
					Date date = DATE_FORMAT.parse( text[1]);
					String name = "" ;
					if( text.length == 3){
						name = text[2] ;
					}
					Holiday item = new Holiday( date, name) ;
					holidayList.add( item) ;
				} catch( ParseException e){
					e.printStackTrace();
				}
			}
		}
		
		// 確認ダイアログ表示
		AlertDialog.Builder builder = new AlertDialog.Builder( context) ;
		builder.setMessage( context.getString( R.string.backup_restore_confirm, temp, weight, ratio)) ;
		builder.setPositiveButton( "Yes", new DialogInterface.OnClickListener() {
			public void onClick( DialogInterface dialog, int which) {
				restoreData( itemList, prefList, holidayList) ;				// データ適用処理
			}
		}) ;
		builder.setNegativeButton( "No", null) ;
		builder.create().show() ;
	}
	
	private static boolean checkBool( String text){
		return text.equals( "treu") || text.equals( "1") ;
	}
	
	/**-------------------------------------------------------------------------<br>
	 * バックアップファイルの読み込み処理を行います。（旧バックアップファイル）<br>
	 *------------------------------------------------------------------------*/
	private static void readBackupData_oldBackupFile( BufferedReader reader) throws IOException{
		final List<Item> itemList = new ArrayList<Item>() ;
		final List<Pref> prefList = new ArrayList<Pref>() ;
		int temp = 0 ;
		int weight = 0 ;
		int ratio = 0 ;
		String line ;
		while(( line = reader.readLine()) != null){
			if( line.startsWith( "setting:")){
				// パラメータ登録
				String key = line.substring( 8, line.indexOf( "=")) ;
				String value = line.substring( line.indexOf( "=") + 1) ;
				if( key.equals( "temp_type")) continue ;
				else if( key.equals( "tempborder_avg")){
					key = "tempborder_settype" ;
					value = value.equals( "NO") ? "0" : "1" ;
				}
				else if( key.equals( "average_temp")) key = "temp_avg" ;
				else if( key.equals( "fixed_temp")) key = "temp_fix" ;
				else if( key.equals( "tempAvgSpan")) key = "temp_avg_span" ;
				else if( key.equals( "enable_waitratio")) key = "waightratio_enabled" ;
				else if( key.equals( "show_graph_blank")) key = "show_blank_line" ;
				else if( key.equals( "cycle_mode")) key = "cycle_settype" ;
				else if( key.equals( "cycle")) key = "period_cycle" ;
				else if( key.equals( "periodAvgSpan")) key = "period_avg_times" ;
				else if( key.equals( "enable_preg")) key = "show_preg" ;
				else if( key.equals( "term")) continue ;
				else if( key.equals( "preg_span")) continue ;
				else if( key.equals( "isLbs")) continue ;
				else if( key.equals( "isDrawAllChart")) key = "show_all_chart" ;
				
				Pref prefItem = new Pref( key) ;
				prefItem.type = value.equals( "YES") || value.equals( "NO") ? BOOLEAN : STRING ;
				prefItem.value = value ;
				if( prefItem.key.endsWith( "_mark")){
					changeParamImagePref( prefItem) ;						// マーク名を数値に変換
				}
				if( prefItem.type.equals( BOOLEAN)){
					prefItem.value = prefItem.value.equals( "YES") ? "1" : "0" ;
				}
				prefList.add( prefItem) ;
			}
			else if( line.startsWith( "data:")){
				// データ登録
				line = line.substring( line.indexOf( ":") + 1) ;
				String[] text = line.split( ",") ;
				try{
					Date date = DATE_FORMAT.parse( text[0]);
					Item item = new Item( date) ;
					item.temp = Float.parseFloat( text[1]) ;
					item.weight = Float.parseFloat( text[2]) ;
					item.ratio = Float.parseFloat( text[3]) ;
					item.memo = text[4].replaceAll( "<br>", "\n") ;
					item.periodStart = text[5].equals( "1") ;
					for( int i = 0; i < 10; i++){
						item.param[i] = text[6 + i].equals( "1") ;
					}
					itemList.add( item) ;
					if( item.temp != 0) temp++ ;
					if( item.weight != 0) weight++ ;
					if( item.ratio != 0) ratio++ ;
				} catch( ParseException e){
					e.printStackTrace();
				}
			}
		}
		
		// 確認ダイアログ表示
		AlertDialog.Builder builder = new AlertDialog.Builder( context) ;
		builder.setMessage( context.getString( R.string.backup_restore_confirm, temp, weight, ratio)) ;
		builder.setPositiveButton( "Yes", new DialogInterface.OnClickListener() {
			public void onClick( DialogInterface dialog, int which) {
				restoreData( itemList, prefList, null) ;						// データ適用処理
				MainActivity mainActivity = ( MainActivity)context ;
				CalendarFragment calendar = mainActivity.getCalendarFragment() ;
				calendar.update();												// カレンダー再表示
			}
		}) ;
		builder.setNegativeButton( "No", null) ;
		builder.create().show() ;
	}
	
	/**-------------------------------------------------------------------------<br>
	 * バックアップからデータを復元します。（データ適用）<br>
	 *------------------------------------------------------------------------*/
	private static void restoreData( List<Item> itemList, List<Pref> prefList, List<Holiday> holidayList){
		// データ全削除
		db.delete( TABLE, "", null) ;
		
		// データ登録
		db.beginTransaction() ;
		try{
			SQLiteStatement statement = db.compileStatement( String.format( "INSERT INTO %s(%s) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)", TABLE, ALL_PARAMS)) ;
			for( Item item : itemList){
				int index = 1;
				statement.bindString( index++, DATE_FORMAT.format( item.date)) ;
				statement.bindDouble( index++, item.temp) ;
				statement.bindDouble( index++, item.weight) ;
				statement.bindDouble( index++, item.ratio) ;
				statement.bindString( index++, item.memo) ;
				statement.bindLong( index++, item.periodStart ? 1 : 0) ;
				statement.bindLong( index++, item.willPeriod ? 1 : 0) ;
				statement.bindLong( index++, item.willOv ? 1 : 0) ;
				statement.bindLong( index++, item.pregWeeks) ;
				for( int i = 0; i < 10; i++){
					statement.bindLong( index++, item.param[i] ? 1 : 0) ;
				}
				statement.execute() ;
			}
			db.setTransactionSuccessful() ;
		} finally{
			db.endTransaction() ;
		}
		
		// プリファレンス削除
		pref.getPref().edit().clear().commit() ;
		
		// プリファレンス登録
		for( Pref prefItem : prefList){
			if( prefItem.type.equals( BOOLEAN)){
				boolean ret = prefItem.value.toLowerCase( Locale.getDefault()).equals( "true") ;
				pref.getPref().edit().putBoolean( prefItem.key, ret).commit() ;
			}
			else{
				pref.getPref().edit().putString( prefItem.key, prefItem.value).commit() ;
			}
		}
		
		if( holidayList != null){
			// 祝日データ全削除
			db.delete( TABLE_HOLIDAY, "", null) ;
			
			// データ登録
			db.beginTransaction() ;
			try{
				SQLiteStatement statement = db.compileStatement( String.format( Locale.getDefault(), "INSERT INTO %s(date,name) values(?,?)", TABLE_HOLIDAY)) ;
				for( Holiday item : holidayList){
					int index = 1;
					statement.bindString( index++, DATE_FORMAT.format( item.date)) ;
					statement.bindString( index++, item.name) ;
					statement.execute() ;
				}
				db.setTransactionSuccessful() ;
			} finally{
				db.endTransaction() ;
			}
		}
	}
	
	/**-------------------------------------------------------------------------<br>
	 * パラメータマークの値を変更します。<br>
	 *------------------------------------------------------------------------*/
	private static void changeParamImagePref( Pref item){
		if( item.value.equals( "mark_red.png")) item.value = "0" ;
		else if( item.value.equals( "mark_blue.png")) item.value = "1" ;
		else if( item.value.equals( "mark_green.png")) item.value = "2" ;
		else if( item.value.equals( "mark_orange.png")) item.value = "3" ;
		else if( item.value.equals( "mark_pink.png")) item.value = "4" ;
		else if( item.value.equals( "mark_blown.png")) item.value = "5" ;
		else if( item.value.equals( "mark_cyan.png")) item.value = "6" ;
		else if( item.value.equals( "mark_gray.png")) item.value = "7" ;
		else if( item.value.equals( "mark_drop.png")) item.value = "8" ;
		else if( item.value.equals( "mark_drop_cyan.png")) item.value = "9" ;
		else if( item.value.equals( "mark_drop_yellow.png")) item.value = "10" ;
		else if( item.value.equals( "mark_heart.png")) item.value = "11" ;
		else if( item.value.equals( "mark_pain.png")) item.value = "12" ;
		else if( item.value.equals( "mark_pain_blue.png")) item.value = "13" ;
		else if( item.value.equals( "mark_star.png")) item.value = "14" ;
		else if( item.value.equals( "mark_medicine_red.png")) item.value = "15" ;
		else if( item.value.equals( "mark_medicine_purple.png")) item.value = "16" ;
		else if( item.value.equals( "mark_medicine_blue.png")) item.value = "17" ;
		else if( item.value.equals( "mark_ribon_red.png")) item.value = "18" ;
		else if( item.value.equals( "mark_ribon_pink.png")) item.value = "19" ;
		else if( item.value.equals( "mark_ribon_cyan.png")) item.value = "20" ;
		else if( item.value.equals( "mark_clef.png")) item.value = "21" ;
		else if( item.value.equals( "mark_person.png")) item.value = "22" ;
		else if( item.value.equals( "mark_person2.png")) item.value = "23" ;
		else if( item.value.equals( "mark_hospital.png")) item.value = "24" ;
		else if( item.value.equals( "mark_phone.png")) item.value = "25" ;
		else if( item.value.equals( "mark_mail.png")) item.value = "26" ;
		
	}

	/**-------------------------------------------------------------------------
	 * バックアップデータにプリファレンスの設定値を書き込みます。
	 *------------------------------------------------------------------------*/
	private static void writePrefData( PrintWriter writer, String type, int key){
		writePrefData( writer, type, key, null) ;
	}

	/**-------------------------------------------------------------------------
	 * バックアップデータにプリファレンスの設定値を書き込みます。
	 *------------------------------------------------------------------------*/
	private static void writePrefData( PrintWriter writer, String type, int key, Object obj){
		Object value = "" ;
		if( type.equals( INT)){
			value = pref.getInt( key, obj) ;
		}
		else if( type.equals( LONG)){
			value = pref.getLong( key, obj) ;
		}
		else if( type.equals( FLOAT)){
			value = pref.getFloat( key, obj) ;
		}
		else if( type.equals( STRING)){
			value = pref.getString( key, obj) ;
		}
		else if( type.equals( BOOLEAN)){
			value = pref.getBoolean( key, obj) ;
		}
		String keyString = context.getString( key, obj) ;
		writer.write( String.format( "pref,%s,%s,%s\n", keyString, type, String.valueOf( value))) ;
	}

	/**-------------------------------------------------------------------------
	 * 生理開始日を検索します。
	 *------------------------------------------------------------------------*/
	private static Item searchPeriodStart( Item item){
		if( item.param[0]){														// 生理中の場合
			Item prevItem = getItem( new Date( item.date.getTime() - ONEDAY)) ;
			if( prevItem != null && prevItem.param[0]){							// 前の日も生理中の場合
				return searchPeriodStart( prevItem) ;									// 再起的に検索
			}
			else{																// 前の日が生理中でない場合
				return item ;
			}
		}
		else{																	// 生理中でない場合
			if( item.periodStart){												// 生理開始が設定されている場合
				setPeriodStart( item, false) ;									// 生理開始をOFFに更新
			}
			Item nextItem = getItem( new Date( item.date.getTime() + ONEDAY)) ;
			if( nextItem != null && nextItem.param[0]){ 						// 翌日が生理中の場合
				return nextItem ;
			}
		}
		 return null ;
	}
	
	/**-------------------------------------------------------------------------
	 * 生理開始日をDBに設定します。
	 *------------------------------------------------------------------------*/
	private static void setPeriodStart( Item item, boolean period){
		ContentValues values = new ContentValues() ;
		values.put( "periodStart", period ? 1 : 0) ;
		db.update( TABLE, values, "date=?", new String[]{ formatDate( item.date)}) ;
	}
	
	/**-------------------------------------------------------------------------
	 * 生理周期（平均値）を更新します。
	 *------------------------------------------------------------------------*/
	public static void updatePeriodCycle(){
		int calcTimes = pref.getInt( R.string.pref_cycle_avg_times) ;
		Cursor cursor = null ;
		try{
			String sql = String.format( "SELECT date FROM %s WHERE (periodStart = 1) AND (date < ?) ORDER BY date DESC LIMIT ? ", TABLE) ;
			cursor = db.rawQuery( sql, new String[]{ formatDate( new Date()), String.valueOf( calcTimes)}) ;
			if( cursor.getCount() < 2){
				return ;
			}
			cursor.moveToLast() ;
			Date lastDate = null;
			int sum = 0 ;
			do{
				Date date = parseDate( cursor.getString( 0)) ;
				if( lastDate != null){
					sum += ( int)((date.getTime() - lastDate.getTime()) / ONEDAY) ;
				}
				lastDate = date ;
			} while( cursor.moveToPrevious()) ;
			float cycle = sum / (cursor.getCount() - 1) ;
			int intCycle = Math.round( cycle) ;
			pref.putPref( R.string.pref_period_cycle, intCycle) ;
		} catch( SQLiteException e){
			e.printStackTrace();
		} finally{
			if( cursor != null){
				cursor.close();
			}
		}
	}
	
	/**-------------------------------------------------------------------------
	 * 生理期間（平均値）を更新します。
	 *------------------------------------------------------------------------*/
	private static void updatePeriodLength(){
		int calcTimes = pref.getInt( R.string.pref_cycle_avg_times) ;
		
		String sql = String.format( "SELECT date FROM %s WHERE (periodStart = 1) AND (date < ?) ORDER BY date ASC LIMIT ? ", TABLE) ;
		Cursor cursor = db.rawQuery( sql, new String[]{ formatDate( new Date()), String.valueOf( calcTimes)}) ;
		cursor.moveToFirst() ;
		int count = cursor.getCount() ;
		if( cursor.getCount() == 0){
			pref.putPref( R.string.pref_period_length, 1) ;
			return ;
		}
		
		Date prevDate = null ;
		int sum = 0 ;
		sql = String.format( "SELECT COUNT(date) FROM %s WHERE param1 = 1 AND date >= ? AND date < ?", TABLE) ;
		do{
			Date date = parseDate( cursor.getString( 0)) ;
			if( prevDate != null){
				Cursor cursor1 = db.rawQuery( sql, new String[]{ formatDate( prevDate), formatDate( date)}) ;
				cursor1.moveToFirst() ;
				sum += cursor1.getInt( 0) ;
				cursor1.close();
			}
			prevDate = date ;
		} while( cursor.moveToNext()) ;
		cursor.close();
		Cursor cursor1 = db.rawQuery( sql, new String[]{ formatDate( prevDate), formatDate( new Date())}) ;
		cursor1.moveToFirst() ;
		sum += cursor1.getInt( 0) ;
		cursor1.close();
		pref.putPref( R.string.pref_period_length, Math.round( sum / count)) ;
	}
	
	/**-------------------------------------------------------------------------
	 * 生理開始日の設定を更新します。
	 *------------------------------------------------------------------------*/
	private static void updatePeriodStart( Item item){
		Item nextItem = getItem( new Date( item.date.getTime() + ONEDAY)) ;
		if( nextItem != null && nextItem.param[0]){
			setPeriodStart( nextItem, false) ;	// 生理開始をOFFに更新
			updatePeriodStart( nextItem) ;	// 再起的に処理
		}
	}

	/**-------------------------------------------------------------------------
	 * 指定期間先までの期間の生理日予想、排卵日予想を更新します。
	 *------------------------------------------------------------------------*/
	public static void updateWillPeriodOvulation(){
		Date today = new Date() ;
		// 指定期間の予想を削除する
		Date toDate = new Date( today.getTime() + WILL_SPAN*ONEDAY) ;
		ContentValues values = new ContentValues() ;
		values.put( "willPeriod", 0) ;
		values.put( "willOv", 0) ;
		db.update( TABLE, values, 
				"(willPeriod=1 OR willOv=1) AND date > ? AND date < ?", 
				new String[]{ formatDate( today), formatDate( toDate)}) ;
		
		// 生理・排卵予測日の設定
		Date date = getNearPeriodDay( today) ;									// 最近の生理開始日を取得
		int periodCycle = pref.getInt( R.string.pref_period_cycle) ;
		int periodLength = pref.getInt( R.string.pref_period_length) ;
		if( periodCycle == 0){
			return ;
		}
		while( date.before( toDate)){
			date = new Date( date.getTime() + periodCycle*ONEDAY) ;				// 次の生理開始予測日

			// 生理予測日設定
			values.clear() ;
			values.put( "willPeriod", 1) ;
			for( int i = 0; i < periodLength; i++){
				Date periodDate = new Date( date.getTime() + i*ONEDAY) ;
				if( periodDate.after( today)){
					values.put( "date", formatDate( periodDate)) ;
					insertOrUpdate( periodDate, values) ;
				}
			}
			
			// 排卵予測日設定
			values.clear() ;
			values.put( "willOv", 1) ;
			Date ovDate = new Date( date.getTime() - 14*ONEDAY) ;
			if( ovDate.after( today)){
				values.put( "date", formatDate( ovDate)) ;
				insertOrUpdate( ovDate, values) ;
			}
		}
	}
	
	private static void insertOrUpdate( Date date, ContentValues values){
		String strDate = formatDate( date) ;
		Cursor cursor = null ;
		try{
			String sql = String.format( "SELECT date FROM %s WHERE date=?", TABLE) ;
			cursor = db.rawQuery( sql, new String[]{ strDate}) ;
			if( cursor.getCount() == 0){
				values.put( "memo", "") ;
				db.insert( TABLE, null, values) ;
			}
			else{
				db.update( TABLE, values, "date=?", new String[]{ strDate}) ;
			}
		} catch( SQLiteException e){
			e.printStackTrace();
		} finally{
			if( cursor != null){
				cursor.close();
			}
		}
	}
	
	/**-------------------------------------------------------------------------
	 * データベースオープンヘルパークラス
	 *------------------------------------------------------------------------*/
	private static class DatabaseOpenHelper extends SQLiteOpenHelper {
		/**-------------------------------------------------------------------------
		 * インスタンス作成時の初期化処理を行います。
		 *------------------------------------------------------------------------*/
		public DatabaseOpenHelper( Context context){
			super( context, DB_NAME, null, DB_VERSION) ;
		}

		/**-------------------------------------------------------------------------
		 * テーブル作成処理を行います。
		 *------------------------------------------------------------------------*/
		@Override
		public void onCreate( SQLiteDatabase db) {
			db.execSQL( "CREATE TABLE " + TABLE +
					" (date text primary key," +
					"temp real," +
					"weight real," +
					"ratio real," +
					"memo text," +
					"periodStart int," +
					"willPeriod int," +
					"willOv int," +
					"pregWeeks int," +
					"param1 int," +
					"param2 int," +
					"param3 int," +
					"param4 int," +
					"param5 int," +
					"param6 int," +
					"param7 int," +
					"param8 int," +
					"param9 int," +
					"param10 int)") ;
			
			db.execSQL( "CREATE TABLE " + TABLE_HOLIDAY + 
					" (date text primary key, name text)") ;
		}

		/**-------------------------------------------------------------------------
		 * テーブル構造変更時処理を行います。
		 *------------------------------------------------------------------------*/
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		}
	}
	
	/**-------------------------------------------------------------------------
	 * プリファレンス情報クラス
	 *------------------------------------------------------------------------*/
	public static class Pref {
		/** キー */
		public String key ;
		/** 型 */
		public String type ;
		/** 値 */
		public String value ;
		/** コンストラクタ */
		public Pref( String key){
			this.key = key ;
		}
	}
}