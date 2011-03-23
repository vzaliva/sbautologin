package org.crocodile.sbautologin.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import org.crocodile.sbautologin.model.HistoryItem;

import java.util.ArrayList;
import java.util.Date;

/**
 * @author Igor Giziy <linsalion@gmail.com>
 */
public class DBAccesser {
    private SQLiteDatabase db;
    private DBCreator dbCreator;


    public DBAccesser(Context context) {
        dbCreator = new DBCreator(context);
    }


    public void addHistoryItem(HistoryItem historyItem) {
        db = dbCreator.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("date", historyItem.getDate().getTime());
        contentValues.put("success", historyItem.isSuccess() ? 1 : 0);
        contentValues.put("message", historyItem.getMessage());
        db.insert("history", null, contentValues);
        db.close();
    }

    public void addHistoryItems(ArrayList<HistoryItem> historyItems) {
        for (HistoryItem historyItem : historyItems) {
            addHistoryItem(historyItem);
        }
    }

    public HistoryItem getHistoryItem(int id) {
        HistoryItem historyItem = new HistoryItem();
        db = dbCreator.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from history where _id = " + id, null);
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            historyItem.setId(cursor.getInt(0));
            historyItem.setDate(new Date(cursor.getInt(1)));
            historyItem.setSuccess(cursor.getInt(2) != 0);
            historyItem.setMessage(cursor.getString(3));
        }
        cursor.close();
        db.close();
        return historyItem;
    }

    public ArrayList<HistoryItem> getHistoryItems() {
        ArrayList<HistoryItem> historyItems = new ArrayList<HistoryItem>();
        db = dbCreator.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from history;", null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            historyItems.add(getHistoryItem(cursor.getInt(0)));
            cursor.moveToNext();
        }
        cursor.close();
        db.close();
        return historyItems;
    }

    public void removeHistoryItem(int id) {
        db = dbCreator.getWritableDatabase();
        db.delete("history", "_id = " + id, null);
        db.close();
    }

    public void removeHistoryItems() {
        db = dbCreator.getWritableDatabase();
        db.delete("history", null, null);
        db.close();
    }

}
