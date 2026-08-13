package de.opendailypacks.odpoffice;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class OfficeDb extends SQLiteOpenHelper {

    public OfficeDb(Context context) {
        super(context, "odp_office.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
            "CREATE TABLE products (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "name TEXT NOT NULL," +
            "sku TEXT," +
            "qty INTEGER NOT NULL DEFAULT 0," +
            "ek REAL NOT NULL DEFAULT 0," +
            "vk REAL NOT NULL DEFAULT 0)"
        );

        db.execSQL(
            "CREATE TABLE orders (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "channel TEXT," +
            "customer TEXT," +
            "product TEXT," +
            "qty INTEGER NOT NULL DEFAULT 1," +
            "amount REAL NOT NULL DEFAULT 0," +
            "status TEXT NOT NULL DEFAULT 'Offen'," +
            "created_at DATETIME DEFAULT CURRENT_TIMESTAMP)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}

    public void addProduct(String name, String sku, int qty, double ek, double vk) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(
            "INSERT INTO products(name,sku,qty,ek,vk) VALUES(?,?,?,?,?)",
            new Object[]{name, sku, qty, ek, vk}
        );
    }

    public void addOrder(String channel, String customer, String product, int qty, double amount) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(
            "INSERT INTO orders(channel,customer,product,qty,amount,status) VALUES(?,?,?,?,?,'Offen')",
            new Object[]{channel, customer, product, qty, amount}
        );
    }

    public Cursor products() {
        return getReadableDatabase().rawQuery(
            "SELECT id,name,sku,qty,ek,vk FROM products ORDER BY id DESC",
            null
        );
    }

    public Cursor orders() {
        return getReadableDatabase().rawQuery(
            "SELECT id,channel,customer,product,qty,amount,status FROM orders ORDER BY id DESC",
            null
        );
    }

    public int openOrders() {
        Cursor c = getReadableDatabase().rawQuery(
            "SELECT COUNT(*) FROM orders WHERE status='Offen'",
            null
        );
        int result = c.moveToFirst() ? c.getInt(0) : 0;
        c.close();
        return result;
    }

    public int stockQty() {
        Cursor c = getReadableDatabase().rawQuery(
            "SELECT COALESCE(SUM(qty),0) FROM products",
            null
        );
        int result = c.moveToFirst() ? c.getInt(0) : 0;
        c.close();
        return result;
    }

    public double revenue() {
        Cursor c = getReadableDatabase().rawQuery(
            "SELECT COALESCE(SUM(amount),0) FROM orders",
            null
        );
        double result = c.moveToFirst() ? c.getDouble(0) : 0;
        c.close();
        return result;
    }

    public double stockValue() {
        Cursor c = getReadableDatabase().rawQuery(
            "SELECT COALESCE(SUM(qty*ek),0) FROM products",
            null
        );
        double result = c.moveToFirst() ? c.getDouble(0) : 0;
        c.close();
        return result;
    }
}
