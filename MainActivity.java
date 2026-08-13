package de.opendailypacks.odpoffice;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView status = findViewById(R.id.statusText);
        bind(R.id.ordersButton, status, "Bestellungen: eBay, Shopify und weitere Kanäle werden hier gebündelt.");
        bind(R.id.inventoryButton, status, "Bestand: Lagerbestand, EK, VK und Reservierungen.");
        bind(R.id.shippingButton, status, "Versand: Label drucken und Tracking automatisch zurückmelden.");
        bind(R.id.financeButton, status, "Finanzen: Umsatz, Kosten, Gebühren, Wareneinsatz und Liquidität.");
        bind(R.id.integrationsButton, status, "Schnittstellen: eBay, Billbee, Finom und Lexware.");
    }

    private void bind(int buttonId, TextView status, String message) {
        Button button = findViewById(buttonId);
        button.setOnClickListener(v -> status.setText(message));
    }
}
