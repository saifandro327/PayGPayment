package com.example.paygpayment;

public class AppConstants {

    public static String BASE_URL = "https://uatapi.payg.in/payment/"; // This is UAT base URL
//    public static String BASE_URL = " https://paygapi.payg.in/payment/";
    public static final String CREATE_ORDER = "api/Order/Create";
    public static final String ORDER_DETAIL = "api/Order/Detail";
    public static final String REDIRECT_URL = "https://payg.in"; // TODO(Important): // replace with customer URL to compare the response after payment successfull


    public static final boolean IS_DEBUG_MODE = true;

    public static final int MY_SOCKET_TIMEOUT_MS = 10 * 1000;
    // Parameter to check if new order
    public static final String EXTRAS_IS_NEW_ORDER = "isNewOrder";

    //==== For pay with payment url ====//
    public static final String EXTRAS_PAYMENT_URL = "paymentURL";
    public static final String EXTRAS_ORDER_ID = "orderId";

    //===== For Create new order extras ====//
    // Mandatory parameters
    public static final String EXTRAS_MERCHANT_ID = "merchantId";
    public static final String EXTRAS_AUTH_KEY = "authKey";
    public static final String EXTRAS_AUTH_TOKEN = "authToken";
    public static final String EXTRAS_ORDER_AMOUNT = "orderAmount";
    public static final String EXTRAS_CUSTOMER_MOBILE_NO = "customerMobileNo";  // if we dont pass mobile number then payment page will ask customer info
    public static final String EXTRAS_REDIRECT_URL = "redirectURL";
    public static final String EXTRAS_WALLET_TYPE = "walletType"; // wallet type
    public static final String EXTRAS_WITH_UPI_INTENT = "withUpiIntent";
    public static final String EXTRAS_APPLICATION_INFO = "applicationInfo";
}

