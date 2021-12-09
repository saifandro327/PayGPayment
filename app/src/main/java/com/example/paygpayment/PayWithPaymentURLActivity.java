package com.example.paygpayment;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class PayWithPaymentURLActivity extends AppCompatActivity {

    private static final String TAG = PayWithPaymentURLActivity.class.getName();
    private static final int REQUEST_CODE = 1001;
    private EditText editTextPaymentURL;
    private EditText editTextOrderId;
    private Button btnProceed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay_with_payment_urlactivity);
        init();
    }

    private void init() {
        editTextPaymentURL = (EditText) findViewById(R.id.editTextPaymentURL);
        editTextOrderId = (EditText) findViewById(R.id.editTextOrderId);
        btnProceed = (Button) findViewById(R.id.btnProceed);

        btnProceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String paymentURL = editTextPaymentURL.getText().toString();
                String orderId = editTextOrderId.getText().toString();
                if (!orderId.isEmpty() && !paymentURL.isEmpty()) {
                    pay(paymentURL, orderId);
                } else {
                    Toast.makeText(getApplicationContext(), "Please enter valid order details.", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void pay(String paymentURL, String orderId) {
        // For Normal Payment URL
        Intent intent = new Intent(getApplicationContext(), PaymentActivity.class);
        intent.putExtra(AppConstants.EXTRAS_IS_NEW_ORDER, false);
        intent.putExtra(AppConstants.EXTRAS_PAYMENT_URL, paymentURL);
        intent.putExtra(AppConstants.EXTRAS_ORDER_ID, orderId);
        startActivityForResult(intent, REQUEST_CODE);

/*        // FOR UPI Intent (This will open the android UPI intent)
//        String url ="upi://pay?pa=xsilica.payu@indus&pn=XSILICA SOFTWARE SOLUTIONS PVT.LTD&tr=13281204513&tid=TID547254PRU210616M11130U2021061621&am=1.00&cu=INR&tn=UPI Transaction for TID547254PRU210616M11130U2021061621";
        String url ="upi://pay?pa=xsilica.payu@indus&pn=XSILICA SOFTWARE SOLUTIONS PVT.LTD&tr=13802126914&tid=TID661456PRU210811M11209U8c77e9f671&am=2.00&cu=INR&tn=UPI Transaction for TID661456PRU210811M11209U8c77e9f671";
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        startActivity(intent);*/
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE) {
            String result = data.getStringExtra("result");
            if (result.equalsIgnoreCase("success")) {
                Toast.makeText(getApplicationContext(), "Order payment completed successfully", Toast.LENGTH_SHORT).show();

            } else {
                Toast.makeText(getApplicationContext(), "Order payment failed. Please try again.", Toast.LENGTH_SHORT).show();
            }
            Log.d(TAG, "Payment processing result code: " + resultCode);
        }
    }
}
