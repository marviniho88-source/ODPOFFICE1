package de.opendailypacks.odpoffice;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.content.SharedPreferences;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.text.NumberFormat;
import java.util.Locale;

public class MainActivity extends Activity {

    private SharedPreferences prefs;
    private TextView dashboard;
    private TextView status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = getSharedPreferences("odp_office", MODE_PRIVATE);
        dashboard = findViewById(R.id.dashboardText);
        status = findViewById(R.id.statusText);

        findViewById(R.id.addOrderButton).setOnClickListener(v -> addOrder());
        findViewById(R.id.addStockButton).setOnClickListener(v -> addStock());

        findViewById(R.id.ordersButton).setOnClickListener(v -> showOrders());
        findViewById(R.id.inventoryButton).setOnClickListener(v -> showInventory());
        findViewById(R.id.shippingButton).setOnClickListener(v -> showShipping());
        findViewById(R.id.financeButton).setOnClickListener(v -> showFinance());
        findViewById(R.id.integrationsButton).setOnClickListener(v ->
                status.setText("Schnittstellen: eBay · Billbee · Finom · Lexware\nLive-Anbindungen folgen als nächster Schritt.")
        );

        refreshDashboard();
    }

    private void addOrder() {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        int pad = 32;
        box.setPadding(pad, pad, pad, pad);

        EditText customer = new EditText(this);
        customer.setHint("Kunde / Bestellnummer");

        EditText amount = new EditText(this);
        amount.setHint("Betrag in €");
        amount.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);

        box.addView(customer);
        box.addView(amount);

        new AlertDialog.Builder(this)
                .setTitle("Bestellung hinzufügen")
                .setView(box)
                .setPositiveButton("Speichern", (d, w) -> {
                    double value = parseAmount(amount.getText().toString());
                    int orders = prefs.getInt("orders", 0) + 1;
                    double revenue = Double.longBitsToDouble(
                            prefs.getLong("revenue", Double.doubleToLongBits(0))
                    ) + value;

                    prefs.edit()
                            .putInt("orders", orders)
                            .putLong("revenue", Double.doubleToLongBits(revenue))
                            .putString("lastOrder", customer.getText().toString())
                            .apply();

                    status.setText("Bestellung gespeichert: " + customer.getText());
                    refreshDashboard();
                })
                .setNegativeButton("Abbrechen", null)
                .show();
    }

    private void addStock() {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        int pad = 32;
        box.setPadding(pad, pad, pad, pad);

        EditText product = new EditText(this);
        product.setHint("Produkt");

        EditText qty = new EditText(this);
        qty.setHint("Menge");
        qty.setInputType(InputType.TYPE_CLASS_NUMBER);

        EditText ek = new EditText(this);
        ek.setHint("EK pro Stück in €");
        ek.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);

        box.addView(product);
        box.addView(qty);
        box.addView(ek);

        new AlertDialog.Builder(this)
                .setTitle("Bestand hinzufügen")
                .setView(box)
                .setPositiveButton("Speichern", (d, w) -> {
                    int quantity = parseInt(qty.getText().toString());
                    double purchase = parseAmount(ek.getText().toString());

                    int stock = prefs.getInt("stock", 0) + quantity;
                    double stockValue = Double.longBitsToDouble(
                            prefs.getLong("stockValue", Double.doubleToLongBits(0))
                    ) + (quantity * purchase);

                    prefs.edit()
                            .putInt("stock", stock)
                            .putLong("stockValue", Double.doubleToLongBits(stockValue))
                            .putString("lastProduct", product.getText().toString())
                            .apply();

                    status.setText("Bestand gespeichert: " + quantity + " × " + product.getText());
                    refreshDashboard();
                })
                .setNegativeButton("Abbrechen", null)
                .show();
    }

    private void refreshDashboard() {
        int orders = prefs.getInt("orders", 0);
        int stock = prefs.getInt("stock", 0);
        double revenue = Double.longBitsToDouble(
                prefs.getLong("revenue", Double.doubleToLongBits(0))
        );

        dashboard.setText(
                "HEUTE\n\n" +
                orders + " offene Bestellungen\n" +
                orders + " versandbereit\n" +
                money(revenue) + " Umsatz\n\n" +
                stock + " Artikel im Bestand"
        );
    }

    private void showOrders() {
        int orders = prefs.getInt("orders", 0);
        String last = prefs.getString("lastOrder", "Noch keine Bestellung");
        status.setText(
                "BESTELLUNGEN\n\nOffen: " + orders +
                "\nLetzte Bestellung: " + last
        );
    }

    private void showInventory() {
        int stock = prefs.getInt("stock", 0);
        double value = Double.longBitsToDouble(
                prefs.getLong("stockValue", Double.doubleToLongBits(0))
        );
        String last = prefs.getString("lastProduct", "Noch kein Produkt");

        status.setText(
                "BESTAND\n\nArtikel: " + stock +
                "\nLagerwert EK: " + money(value) +
                "\nLetztes Produkt: " + last
        );
    }

    private void showShipping() {
        int orders = prefs.getInt("orders", 0);
        status.setText(
                "VERSAND & LABELS\n\n" +
                orders + " Sendungen warten auf Bearbeitung.\n\n" +
                "Nächste Ausbaustufe:\nLabel erstellen → Trackingnummer automatisch zu eBay."
        );
    }

    private void showFinance() {
        double revenue = Double.longBitsToDouble(
                prefs.getLong("revenue", Double.doubleToLongBits(0))
        );
        double stockValue = Double.longBitsToDouble(
                prefs.getLong("stockValue", Double.doubleToLongBits(0))
        );

        status.setText(
                "FINANZEN\n\nUmsatz: " + money(revenue) +
                "\nWareneinkaufswert: " + money(stockValue) +
                "\n\nFinom & Lexware Integration folgt."
        );
    }

    private double parseAmount(String s) {
        try {
            return Double.parseDouble(s.replace(",", "."));
        } catch (Exception e) {
            return 0;
        }
    }

    private int parseInt(String s) {
        try {
            return Integer.parseInt(s);
        } catch (Exception e) {
            return 0;
        }
    }

    private String money(double value) {
        return NumberFormat.getCurrencyInstance(Locale.GERMANY).format(value);
    }
}
