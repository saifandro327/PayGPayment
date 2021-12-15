package com.example.paygpayment;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class NewOrderActivity extends AppCompatActivity {

    private static final String TAG = NewOrderActivity.class.getName();
    private static final int REQUEST_CODE = 1001;

    private boolean withUpiIntent;
    private EditText editTextMerchantId, editTextAuthKey, editTextAuthToken, editTextOrderAmount, editTextCustomerMobileNo, editTextRedirectURL;
    private Spinner spinnerWalletTypes;
    private String merchantId;
    private String authKey;
    private String authToken;
    RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_order);

        if(getIntent().hasExtra("withUpiIntent")){
            withUpiIntent = getIntent().getBooleanExtra("withUpiIntent", false);
        }
        init();
    }

    private void init() {
        editTextMerchantId = findViewById(R.id.editTextMerchantId);
        editTextAuthKey = findViewById(R.id.editTextAuthKey);
        editTextAuthToken = findViewById(R.id.editTextAuthToken);
        editTextOrderAmount = findViewById(R.id.editTextOrderAmount);
        editTextCustomerMobileNo = findViewById(R.id.editTextCustomerMobileNo);
        editTextRedirectURL = findViewById(R.id.editTextRedirectURL);
        spinnerWalletTypes = (Spinner) findViewById(R.id.spinnerWalletTypes);
        setSpinnerWalletTypesAdapter();

        Button btnProceed = findViewById(R.id.btnProceed);
        btnProceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String walletType = (String) spinnerWalletTypes.getSelectedItem();
                Log.d(TAG, "wallet type: " + walletType);
                createOder();
            }
        });

        requestQueue = Volley.newRequestQueue(NewOrderActivity.this);
    }

    private void setSpinnerWalletTypesAdapter() {
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.array_wallet_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinnerWalletTypes.setAdapter(adapter);
    }

    private void createOder() {

        merchantId = editTextMerchantId.getText().toString();
        authKey = editTextAuthKey.getText().toString();
        authToken = editTextAuthToken.getText().toString();
        String orderAmount = editTextOrderAmount.getText().toString();
        String redirectURL = editTextRedirectURL.getText().toString();
        String customerMobileNo = editTextCustomerMobileNo.getText().toString();
        String walletType = (String) spinnerWalletTypes.getSelectedItem();
        if (merchantId.isEmpty()) {
            editTextMerchantId.setError("Merchant Id can't be empty. Please enter Merchant Id.");
            editTextMerchantId.requestFocus();
            return;
        }
        if (authKey.isEmpty()) {
            editTextAuthKey.setError("Authentication Key can't be empty. Please enter Authentication Key.");
            editTextAuthKey.requestFocus();
            return;
        }
        if (authToken.isEmpty()) {
            editTextAuthToken.setError("Authentication Token can't be empty. Please enter Authentication Token.");
            editTextAuthToken.requestFocus();
            return;
        }
        if (orderAmount.isEmpty()) {
            editTextOrderAmount.setError("Order Amount can't be empty. Please enter Order Amount.");
            editTextOrderAmount.requestFocus();
            return;
        }
        if (customerMobileNo.isEmpty()) {
            editTextCustomerMobileNo.setError("Customer mobile number can't be empty. Please enter mobile number.");
            editTextCustomerMobileNo.requestFocus();
            return;
        }

        if (redirectURL.isEmpty()) {
            editTextRedirectURL.setError("Redirect URL can't be empty. Please enter Redirect URL.");
            editTextRedirectURL.requestFocus();
            return;
        }

        final PackageManager pckManager = getApplicationContext().getPackageManager();
        ApplicationInfo applicationInformation = null;
        try{
            applicationInformation = pckManager.getApplicationInfo("com.example.paygpayment", PackageManager.GET_META_DATA);
            String packageName = applicationInformation.packageName;
            Log.d("NewOrderActivity", "package name: "+packageName);
        }catch (Exception ex ){
            Log.d("NewOrderActivity", "ex: "+ex.toString());
        }

        Intent intent = new Intent(getApplicationContext(), PaymentActivity.class);
        intent.putExtra(AppConstants.EXTRAS_IS_NEW_ORDER, true); // passing true due to new order
        intent.putExtra(AppConstants.EXTRAS_MERCHANT_ID, merchantId);
        intent.putExtra(AppConstants.EXTRAS_AUTH_KEY, authKey);
        intent.putExtra(AppConstants.EXTRAS_AUTH_TOKEN, authToken);
        intent.putExtra(AppConstants.EXTRAS_ORDER_AMOUNT, Double.parseDouble(orderAmount));
        intent.putExtra(AppConstants.EXTRAS_CUSTOMER_MOBILE_NO, customerMobileNo);
        intent.putExtra(AppConstants.EXTRAS_REDIRECT_URL, redirectURL); // when payment finish will redirect to specific url
        intent.putExtra(AppConstants.EXTRAS_WALLET_TYPE, walletType);
        intent.putExtra(AppConstants.EXTRAS_WITH_UPI_INTENT, withUpiIntent);
        intent.putExtra(AppConstants.EXTRAS_APPLICATION_INFO, applicationInformation);
        startActivityForResult(intent, REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE) {
            String result = data.getStringExtra("result");
            if (result.equalsIgnoreCase("success")) {
                if (data.hasExtra("orderKeyId")) {
                    String orderKeyId = data.getStringExtra("orderKeyId");
                    checkOrderStatus(orderKeyId, merchantId); // Now, we can update our UI accordingly
                }

                Toast.makeText(getApplicationContext(), "Order payment completed successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), "Order payment failed. Please try again.", Toast.LENGTH_SHORT).show();
            }
            Log.d(TAG, "Payment processing result code: " + resultCode);
        }
    }

    private void checkOrderStatus(String orderKeyId, String merchantId){
        JSONObject jsonRequest = Utility.generateOrderStatusRequest(orderKeyId, merchantId);
        Logger.logDebug("TAG", "request:" + jsonRequest.toString());
        String hash = Utility.getEncodedBase64Hash(authKey, authToken, merchantId);
        // call api
        String authHeader = "basic " + hash;
        Log.d("AuthHeader", authHeader);
        // hit request to server to check status
        getOrderDetail(authHeader, jsonRequest);
    }

    /**
     * get order detail to update UI
     **/
    private void getOrderDetail(final String authHeader, JSONObject jsonRequest) {
        String url = AppConstants.BASE_URL + AppConstants.ORDER_DETAIL;

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST, url, jsonRequest,
                new com.android.volley.Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Log.d(TAG, "Order status server response: " + response.toString());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Error:" + error.toString());

            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json; charset=utf-8");
                headers.put("Authorization", authHeader);
                return headers;
            }
        };
        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(
                AppConstants.MY_SOCKET_TIMEOUT_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(jsonObjReq);
    }

}