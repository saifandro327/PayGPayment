package com.example.paygpayment;

import android.util.Base64;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

public class Utility {

//    / <MerchantAuthenticationKey>:<MerchantAuthenticationToken>:M:<MerchantKeyId>

    /**
     * @param authKey
     * @param authToken
     * @param merchantId
     * @return
     */
    public static String getEncodedBase64Hash(String authKey, String authToken, String merchantId) {
        String userName = authKey + ":" + authToken + ":" + "M" + ":" + "" + merchantId;
        byte[] byteEncoded = Base64.encode(userName.getBytes(), Base64.NO_WRAP);
        return new String(byteEncoded);
    }

    /**
     * @param merchantId
     * @param uniqueRequestId
     * @param orderAmount
     * @param customerMobileNo
     * @param redirectURL
     * @param walletType
     * @return
     */
    public static JSONObject generateJsonRequest(String merchantId, int uniqueRequestId, double orderAmount, String customerMobileNo, String redirectURL, String walletType) {
        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("OrderKeyId", null);
            jsonObject.put("MerchantKeyId", merchantId); // required field
            jsonObject.put("ApiKey", null);
            jsonObject.put("UniqueRequestId", uniqueRequestId); // required field
            jsonObject.put("OrderAmount", orderAmount); // required field
            jsonObject.put("OrderType", "MOBILE");
            jsonObject.put("OrderId", null);
            jsonObject.put("OrderStatus", "Initiating");
            jsonObject.put("OrderAmountData", null);
            jsonObject.put("ProductData", null);
            jsonObject.put("NextStepFlowData", null);
//            jsonObject.put("TransactionData", null); // old one

            // wallet type (Seamless integration)
            JSONObject jsonObjTransactionData = new JSONObject();
            jsonObjTransactionData.put("PAYMENTTYPE", "Upiintent"); // Wallet/UPI/CreditCard/DebitCard (This is seamless integration it will navigate to payment option directly without going to payment gateway page)
//            JSONObject jsonObjWalletType = new JSONObject();
//            jsonObjWalletType.put("USERNAME", null);

//            jsonObjWalletType.put("WALLETTYPE", walletType);
//            jsonObjWalletType.put("FIRSTNAME", null);
//            jsonObjWalletType.put("LASTNAME", null);
//            jsonObjTransactionData.put("WALLET", jsonObjWalletType);
            jsonObject.put("TransactionData", jsonObjTransactionData); // new one

            // sample transaction data with wallet
//            "TransactionData":
//            {
//                "PAYMENTTYPE": "Wallet",
//                    "WALLET": {
//                "USERNAME": "SAMPLE STRING",
//                        "WALLETTYPE": "AirtelMoney/ AmazonPay/ FreeCharge/ ITZ/ JIOMONEY/ OLAMoney/ Oxigen/ PayTm/ PayCash/ PayG/ PhonePe/ Payzapp/ YesWallet",
//                        "FIRSTNAME": "SAMPLE STRING",
//                        "LASTNAME": "SAMPLE STRING"
//            }
//
//            },


            // add customer info
            JSONObject jsonObjCustomer = new JSONObject();
            jsonObjCustomer.put("CustomerId", "");
            jsonObjCustomer.put("CustomerNotes", "");
            jsonObjCustomer.put("FirstName", "");
            jsonObjCustomer.put("LastName", "");
            jsonObjCustomer.put("MobileNo", customerMobileNo); // its required (new change)
            jsonObjCustomer.put("Email", "");
            jsonObjCustomer.put("EmailReceipt", false);
            jsonObjCustomer.put("BillingAddress", "");
            jsonObjCustomer.put("BillingCity", "");
            jsonObjCustomer.put("BillingState", "");
            jsonObjCustomer.put("BillingCountry", "");
            jsonObjCustomer.put("BillingZipCode", "");
            jsonObjCustomer.put("ShippingFirstName", "");
            jsonObjCustomer.put("ShippingLastName", "");
            jsonObjCustomer.put("ShippingAddress", "");
            jsonObjCustomer.put("ShippingCity", "");
            jsonObjCustomer.put("ShippingState", "");
            jsonObjCustomer.put("ShippingCountry", "");
            jsonObjCustomer.put("ShippingZipCode", "");
            jsonObjCustomer.put("ShippingMobileNo", "");

            jsonObject.put("CustomerData", jsonObjCustomer);

            // add user defined data
            JSONObject jsonObjUserDefined = new JSONObject();
            for (int i = 1; i <= 20; i++) {
                jsonObjUserDefined.put("UserDefined" + i, "");
            }
            jsonObject.put("UserDefinedData", jsonObjUserDefined);

            // add integration data
            JSONObject jsonObjIntegrationData = new JSONObject();
            jsonObjIntegrationData.put("UserName", "");
            jsonObjIntegrationData.put("Source", "MobileSDK");
            jsonObjIntegrationData.put("IntegrationType", "11");
            jsonObjIntegrationData.put("HashData", "");
            jsonObjIntegrationData.put("PlatformId", "");
            jsonObject.put("IntegrationData", jsonObjIntegrationData);

            // add recurring billing data
            jsonObject.put("RecurringBillingData", "");
            jsonObject.put("CouponData", "");
            jsonObject.put("ShipmentData", "");
            jsonObject.put("RequestDateTime", "");
            jsonObject.put("RedirectUrl", redirectURL);
            jsonObject.put("Source", "");

        } catch (JSONException jsonException) {
            jsonException.printStackTrace();
        }
        return jsonObject;

    }

    public static JSONObject generateOrderStatusRequest(String orderKeyId, String merchantId) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("OrderKeyId", orderKeyId);
            jsonObject.put("MerchantKeyId", merchantId);
            jsonObject.put("PaymentTransactionId", null);
            jsonObject.put("PaymentType", null);

            Log.d(Utility.class.getName(), "hello orderkeyid " + orderKeyId  );

        } catch (JSONException jsonException) {
            jsonException.printStackTrace();
        }
        return jsonObject;
    }

    /**
     * Generate unique order id
     *
     * @return
     */
    public static int generateUniqueReqId() {
        UUID uuid = UUID.randomUUID();
        String str = "" + uuid;
        int uid = str.hashCode();
        String filterStr = "" + uid;
        str = filterStr.replaceAll("-", "");
        return Integer.parseInt(str);
    }

}
