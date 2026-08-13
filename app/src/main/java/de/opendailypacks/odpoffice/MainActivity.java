package de.opendailypacks.odpoffice;

import android.app.Activity;
import android.app.AlertDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends Activity {

    private OfficeDb db;
    private TextView dashboard;
    private TextView status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = new OfficeDb(this);

        dashboard = findViewById(R.id.dashboardText);
        status = findViewById(R.id.statusText);

        findViewById(R.id.addOrderButton).setOnClickListener(v -> addOrder());
        findViewById(R.id.addStockButton).setOnClickListener(v -> addProduct());

        findViewById(R.id.ordersButton).setOnClickListener(v -> showOrders());
        findViewById(R.id.inventoryButton).setOnClickListener(v -> showProducts());

        findViewById(R.id.shippingButton).setOnClickListener(v ->
            status.setText(
                "VERSAND & LABELS\n\n" +
                db.openOrders() + " offene Sendungen.\n\n" +
                "Nächster Schritt:\n" +
                "eBay-Bestellungen abrufen → DHL Label → Tracking automatisch zurück zu eBay."
            )
        );

        findViewById(R.id.financeButton).setOnClickListener(v ->
            status.setText(
                "FINANZEN\n\n" +
                "Umsatz: " + money(db.revenue()) +
                "\nLagerwert EK: " + money(db.stockValue()) +
                "\n\nFinom / Lexware folgen."
            )
        );

        findViewById(R.id.integrationsButton).setOnClickListener(v ->
            status.setText(
                "SCHNITTSTELLEN\n\n" +
                "eBay\nBillbee\nFinom\nLexware\nDHL\n\n" +
                "API-Anbindungen werden als nächste Stufe eingebaut."
            )
        );

        refreshDashboard();
    }

    private EditText field(String hint) {
        EditText e = new EditText(this);
        e.setHint(hint);
        return e;
    }

    private EditText numberField(String hint) {
        EditText e = field(hint);
        e.setInputType(
            InputType.TYPE_CLASS_NUMBER |
            InputType.TYPE_NUMBER_FLAG_DECIMAL
        );
        return e;
    }

    private LinearLayout form() {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(40, 20, 40, 10);
        return box;
    }

    private void addProduct() {
        LinearLayout box = form();

        EditText name = field("Produktname");
        EditText sku = field("SKU / Artikelnummer");
        EditText qty = numberField("Menge");
        EditText ek = numberField("EK pro Stück €");
        EditText vk = numberField("VK pro Stück €");

        box.addView(name);
        box.addView(sku);
        box.addView(qty);
        box.addView(ek);
        box.addView(vk);

        new AlertDialog.Builder(this)
            .setTitle("Produkt hinzufügen")
            .setView(box)
            .setPositiveButton("Speichern", (d, w) -> {
                if (name.getText().toString().trim().isEmpty()) {
                    status.setText("Produktname fehlt.");
                    return;
                }

                db.addProduct(
                    name.getText().toString().trim(),
                    sku.getText().toString().trim(),
                    intval(qty.getText().toString()),
                    dbl(ek.getText().toString()),
                    dbl(vk.getText().toString())
                );

                status.setText("Produkt gespeichert: " + name.getText());
                refreshDashboard();
            })
            .setNegativeButton("Abbrechen", null)
            .show();
    }

    private void addOrder() {
        LinearLayout box = form();

        EditText channel = field("Kanal, z.B. eBay");
        EditText customer = field("Kunde / Bestellnummer");
        EditText product = field("Produkt");
        EditText qty = numberField("Menge");
        EditText amount = numberField("Gesamtbetrag €");

        box.addView(channel);
        box.addView(customer);
        box.addView(product);
        box.addView(qty);
        box.addView(amount);

        new AlertDialog.Builder(this)
            .setTitle("Bestellung hinzufügen")
            .setView(box)
            .setPositiveButton("Speichern", (d, w) -> {
                db.addOrder(
                    channel.getText().toString().trim(),
                    customer.getText().toString().trim(),
                    product.getText().toString().trim(),
                    Math.max(1, intval(qty.getText().toString())),
                    dbl(amount.getText().toString())
                );

                status.setText("Bestellung gespeichert.");
                refreshDashboard();
            })
            .setNegativeButton("Abbrechen", null)
            .show();
    }

    private void showProducts() {
        Cursor c = db.products();
        ArrayList<String> rows = new ArrayList<>();

        while (c.moveToNext()) {
            String sku = c.getString(2);

            rows.add(
                c.getString(1) +
                "\nSKU: " + (sku == null || sku.isEmpty() ? "—" : sku) +
                "\nBestand: " + c.getInt(3) +
                " · EK " + money(c.getDouble(4)) +
                " · VK " + money(c.getDouble(5))
            );
        }
        c.close();

        if (rows.isEmpty()) rows.add("Noch keine Produkte vorhanden.");

        new AlertDialog.Builder(this)
            .setTitle("Bestand")
            .setItems(rows.toArray(new String[0]), null)
            .setPositiveButton("Schließen", null)
            .show();
    }

    private void showOrders() {
        Cursor c = db.orders();
        ArrayList<String> rows = new ArrayList<>();

        while (c.moveToNext()) {
            rows.add(
                "#" + c.getInt(0) +
                " · " + safe(c.getString(1)) +
                "\n" + safe(c.getString(2)) +
                "\n" + safe(c.getString(3)) +
                " × " + c.getInt(4) +
                "\n" + money(c.getDouble(5)) +
                " · " + c.getString(6)
            );
        }
        c.close();

        if (rows.isEmpty()) rows.add("Noch keine Bestellungen vorhanden.");

        new AlertDialog.Builder(this)
            .setTitle("Bestellungen")
            .setItems(rows.toArray(new String[0]), null)
            .setPositiveButton("Schließen", null)
            .show();
    }

    private void refreshDashboard() {
        dashboard.setText(
            money(db.revenue()) + " Umsatz\n\n" +
            db.openOrders() + " offene Bestellungen  ·  " +
            db.stockQty() + " Artikel\n" +
            money(db.stockValue()) + " Lagerwert EK"
        );

        status.setText(
            "● System online\n" +
            "Lokale Datenbank aktiv\n" +
            "Billbee · eBay · Finom · Lexware werden vorbereitet"
        );
    }

    private double dbl(String s) {
        try {
            return Double.parseDouble(s.replace(",", "."));
        } catch (Exception e) {
            return 0;
        }
    }

    private int intval(String s) {
        try {
            return Integer.parseInt(s);
        } catch (Exception e) {
            return 0;
        }
    }

    private String money(double value) {
        return NumberFormat
            .getCurrencyInstance(Locale.GERMANY)
            .format(value);
    }

    private String safe(String s) {
        return s == null || s.trim().isEmpty() ? "—" : s;
    }
}
