package com.example.paygpayment;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

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

import static com.example.paygpayment.AppConstants.REDIRECT_URL;

public class PaymentActivity extends AppCompatActivity {

    private static final String TAG = "PaymentActivity";

    private boolean isNewOrder = false;
    private ApplicationInfo applicationInfo;
    private String paymentURL = "";
    private String orderId = "";
    private ProgressBar progressBar;
    private WebView webView;
    RequestQueue requestQueue;
    String redirectURL;
    int uniqueRequestId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);
        init();

        isNewOrder = getIntent().getBooleanExtra(AppConstants.EXTRAS_IS_NEW_ORDER, false);
        applicationInfo = getIntent().getParcelableExtra(AppConstants.EXTRAS_APPLICATION_INFO);

//        String appName = getAppname();
//        boolean isValidPackName = validatePackageName();

//        Log.e("PaymentActivity", "app name: " + appName);
//        Log.e("PaymentActivity", "is valid package: " + isValidPackName);
//        Toast.makeText(getApplicationContext(),"isValid : "+isValidPackName, Toast.LENGTH_LONG).show();

        if (isNewOrder) {
            createOrder(); // if new order then creating new order on server
        } else {
            paymentURL = getIntent().getStringExtra(AppConstants.EXTRAS_PAYMENT_URL);
            orderId = getIntent().getStringExtra(AppConstants.EXTRAS_ORDER_ID);
            String url = paymentURL + orderId;
            processForPayment(url);
        }

    }

    private boolean validatePackageName() {
        if(applicationInfo!=null) {
            return applicationInfo.packageName.equals("in.payg.androidsdksample");
        }else{
            return false;
        }
    }

    private String getAppname() {
        if(applicationInfo!=null) {
            return applicationInfo.name;
        }else{
            return "Unknown";
        }
    }


/*    private String getApplicationName(Context context, String data, int flag) {

        final PackageManager pckManager = context.getPackageManager();
        ApplicationInfo applicationInformation;
        try {
            applicationInformation = pckManager.getApplicationInfo(data, flag);
        } catch (PackageManager.NameNotFoundException e) {
            applicationInformation = null;
        }
        final String applicationName = (String) (applicationInformation != null ? pckManager.getApplicationLabel(applicationInformation) : "(unknown)");
        return applicationName;

    }*/

    @SuppressLint("SetJavaScriptEnabled")
    private void init() {
        progressBar = findViewById(R.id.progressBar);
        webView = (WebView) findViewById(R.id.webView);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webView.clearCache(true);
        requestQueue = Volley.newRequestQueue(PaymentActivity.this);
    }

    // For new order creation
    private void createOrder() {

        String merchantId = getIntent().getStringExtra(AppConstants.EXTRAS_MERCHANT_ID);
        String authKey = getIntent().getStringExtra(AppConstants.EXTRAS_AUTH_KEY);
        String authToken = getIntent().getStringExtra(AppConstants.EXTRAS_AUTH_TOKEN);
        double orderAmount = getIntent().getDoubleExtra(AppConstants.EXTRAS_ORDER_AMOUNT, 0.0);
        String customerMobileNo = getIntent().getStringExtra(AppConstants.EXTRAS_CUSTOMER_MOBILE_NO);
        redirectURL = getIntent().getStringExtra(AppConstants.EXTRAS_REDIRECT_URL);
        String walletType = getIntent().getStringExtra(AppConstants.EXTRAS_WALLET_TYPE);
        uniqueRequestId = Utility.generateUniqueReqId();
        // Generate request
        JSONObject jsonRequest = Utility.generateJsonRequest(merchantId, uniqueRequestId, orderAmount, customerMobileNo, redirectURL, walletType);
        Logger.logDebug("TAG", "request:" + jsonRequest.toString());
        String hash = Utility.getEncodedBase64Hash(authKey, authToken, merchantId);
        // call api
        String authHeader = "basic " + hash;
        // hit request to server to create order
        createPaymentOrder(authHeader, jsonRequest);
    }

    /**
     * Generate payment process url
     **/
    private void createPaymentOrder(final String authHeader, JSONObject jsonRequest) {
        String url = AppConstants.BASE_URL + AppConstants.CREATE_ORDER;
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST, url, jsonRequest,
                new com.android.volley.Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (response.has("PaymentProcessUrl")) {
                                // take order key id from this url and store somewhere to check status
                                String paymentProcessURL = response.getString("PaymentProcessUrl");
                                String[] orderData = paymentProcessURL.split("=");
                                if (orderData.length >= 1) {
                                    orderId = orderData[1];
                                    Log.d(TAG, "Order Key Id: " + orderId);
                                }
                                processForPayment(paymentProcessURL);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            goBackWithFail();
                        }
                    }
                }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Error:" + error.toString());
                goBackWithFail();
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


    // This url can be payment process url or UPI url
    private void processForPayment(String url) {
        Log.d(TAG, "PayG load order details url :" + url);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {

                if (url.startsWith("upi:")) { // If response start with UPI then can open the UPI intent (android default UPI app will open)
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(url));
                    startActivity(intent);
                    return true;
                }
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                Logger.logDebug(TAG, "Payg page started: " + url);
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                Logger.logDebug(TAG, "Payg page finish: " + url);
                progressBar.setVisibility(View.GONE);
                if (url.equalsIgnoreCase(redirectURL) || url.equalsIgnoreCase(REDIRECT_URL)) {
                    goBackWithSuccess();
                } else if (url.equalsIgnoreCase("https://payg.in/")) {
                    goBackWithSuccess();
                } else {
                    Logger.logDebug(TAG, "Payg page other url finished ");
                }

            }
        });
        webView.loadUrl(url);
    }


    @Override
    public void onBackPressed() {
        //    super.onBackPressed();
        goBackWithFail();
    }

    private void goBackWithFail() {
        Intent output = new Intent();
        output.putExtra("result", "fail");
        setResult(RESULT_OK, output);
        finish();
    }

    private void goBackWithSuccess() {
        Intent output = new Intent();
        output.putExtra("result", "success");
        output.putExtra("orderKeyId", orderId);
        setResult(RESULT_OK, output);
        finish();
    }
}


