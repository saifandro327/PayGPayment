package com.example.paygpayment;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getName();
    private Button btnExistingOrder;
    private Button btnCreateNewOrderWithUPI;
    private Button btnCreateNewOrderNormal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init() {
        btnExistingOrder = (Button) findViewById(R.id.btnPayWithPaymentURL);
        btnCreateNewOrderWithUPI = (Button) findViewById(R.id.btnCreateNewOrderWithUPI);
        btnCreateNewOrderNormal = (Button) findViewById(R.id.btnCreateNewOrderNormal);

        btnExistingOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoExistingOrderActivity();
            }
        });

        btnCreateNewOrderWithUPI.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoCreateNewOrderActivity(true);
            }
        });

        btnCreateNewOrderNormal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoCreateNewOrderActivity(false);
            }
        });
    }

    private void gotoExistingOrderActivity() {
        Intent intent = new Intent(getApplicationContext(), PayWithPaymentURLActivity.class);
        startActivity(intent);
    }

    private void gotoCreateNewOrderActivity(boolean withUpiIntent) {
        Intent intent = new Intent(getApplicationContext(), NewOrderActivity.class);
        intent.putExtra("withUpiIntent", withUpiIntent);
        startActivity(intent);
    }
}