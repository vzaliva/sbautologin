
package org.crocodile.sbautologin.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * @author Igor Giziy <linsalion@gmail.com>
 */
public class DBCreator extends SQLiteOpenHelper
{
    private static final String DATABASE_NAME    = "sbautologin.db";
    private static final int    DATABASE_VERSION = 1;

    private static final String CREATE_TABLE     = "create table history "
                                                         + " (_id integer primary key autoincrement, "
                                                         + "date integer, " + "success integer, " + "message text);";

    public DBCreator(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase)
    {
        sqLiteDatabase.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1)
    {
    }

}
