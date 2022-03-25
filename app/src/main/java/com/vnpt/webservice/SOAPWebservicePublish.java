package com.vnpt.webservice;

import android.content.Context;
import android.util.Log;

import com.vnpt.common.Common;
import com.vnpt.common.ConstantsApp;
import com.vnpt.utils.StoreSharePreferences;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;



public  class SOAPWebservicePublish {
    public SOAPWebservicePublish(String server, String user, String pass)
    {
       this.URL_SERVER_SOAP = server;
       this.URL_PUBLISHSERVICE = URL_SERVER_SOAP+"/publishservice.asmx?WSDL";
       this.WS_USER = user;
       this.WS_PASS = pass;
    }
    Context context;
    private static SOAPWebservicePublish instance;
    /**
     * Get current instance of class
     *
     * @return
     */
    public static synchronized SOAPWebservicePublish getInstance(Context context) {
        if (instance == null) {
            String wsUser = StoreSharePreferences.getInstance(context).loadStringSavedPreferences(Common.KEY_COMPANY_USER);
            String wsPass = StoreSharePreferences.getInstance(context).loadStringSavedPreferences(Common.KEY_COMPANY_PASS);
            String strServer = StoreSharePreferences.getInstance(context).loadStringSavedPreferences(Common.KEY_COMPANY_URL);
            instance = new SOAPWebservicePublish(strServer, wsUser, wsPass);
        }
        return instance;
    }
    private static final String TAG = "SOAPWebservicePublish";
    private static final String NAMESPACE = "http://tempuri.org/";
    private String URL_SERVER_SOAP = "";
    private String WS_USER = "";
    private String WS_PASS = "";
    private String URL_PUBLISHSERVICE = URL_SERVER_SOAP+"/publishservice.asmx?WSDL";



    // các hàm ws xử lý
    private static final String SOAP_ACTION_PUBLISHINV = "http://tempuri.org/publishInv";
    private static final String METHOD_PUBLISHINV = "publishInv";

    private static final String SOAP_ACTION_PUBLISHINVWITHTOKEN = "http://tempuri.org/publishInvWithToken";
    private static final String METHOD_PUBLISHINVWITHTOKEN = "publishInvWithToken";

    private static final String SOAP_ACTION_IMPORTANDPUBLISHINV = "http://tempuri.org/ImportAndPublishInv";
    private static final String METHOD_IMPORTANDPUBLISHINV = "ImportAndPublishInv";

    public String getWS_ImportAndPublishInv(String strUserName, String strUserPass, String strPattern, String strSerial,int convert, String strXmlInvData) {
        String ret = null;
        try {
            SoapObject request = new SoapObject(NAMESPACE, METHOD_IMPORTANDPUBLISHINV);
            // Set the property info for the to currency
            //Tham số default
            PropertyInfo userName = new PropertyInfo();
            userName.setName("Account");
            userName.setValue(strUserName);
            userName.setType(String.class);
            request.addProperty(userName);

            PropertyInfo userPass = new PropertyInfo();
            userPass.setName("ACpass");
            userPass.setValue(strUserPass);
            userPass.setType(String.class);
            request.addProperty(userPass);

            PropertyInfo pxmlInvData = new PropertyInfo();
            pxmlInvData.setName("xmlInvData");
            pxmlInvData.setValue(strXmlInvData);
            pxmlInvData.setType(String.class);
            request.addProperty(pxmlInvData);

            PropertyInfo userCName = new PropertyInfo();
            userCName.setName("username");
            userCName.setValue(WS_USER);
            userCName.setType(String.class);
            request.addProperty(userCName);

            PropertyInfo userCPass = new PropertyInfo();
            userCPass.setName("password");
            userCPass.setValue(WS_PASS);
            userCPass.setType(String.class);
            request.addProperty(userCPass);

            PropertyInfo pPattern = new PropertyInfo();
            pPattern.setName("pattern");
            pPattern.setValue(strPattern);
            pPattern.setType(String.class);
            request.addProperty(pPattern);

            PropertyInfo pSerial = new PropertyInfo();
            pSerial.setName("serial");
            pSerial.setValue(strSerial);
            pSerial.setType(String.class);
            request.addProperty(pSerial);

            PropertyInfo pConvert = new PropertyInfo();
            pConvert.setName("serial");
            pConvert.setValue(convert);
            pConvert.setType(Integer.class);
            request.addProperty(pConvert);


            //String url = "https://congtrinhdothigliadmindemo.vnpt-invoice.com.vn/publishservice.asmx?WSDL"
            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            envelope.dotNet = true;
            envelope.setOutputSoapObject(request);
            HttpTransportSE androidHttpTransport = new HttpTransportSE(URL_PUBLISHSERVICE, 30000);
            try {
                androidHttpTransport.call(SOAP_ACTION_IMPORTANDPUBLISHINV, envelope);
                //SoapPrimitive  resultsRequestSOAP = (SoapPrimitive) envelope.getResponse();
                SoapPrimitive resultsRequestSOAP = (SoapPrimitive) envelope.getResponse();
//            SoapObject resultsRequestSOAP = (SoapObject) envelope.bodyIn;
                System.out.println("Response:"+resultsRequestSOAP.toString());
                ret = resultsRequestSOAP.toString();
            } catch (Exception e) {
                Log.e(TAG, "getWS_ImportAndPublishInv"+e);
                e.printStackTrace();
            }
        } catch (Exception ex) {
            Log.e(TAG, "getWS_ImportAndPublishInv");
            ex.printStackTrace();
        }
        return ret;
    }


    public String getWS_publishInv(String strPattern, String strSerial, String invIDs) {
        //URL_SERVER_SOAP = strServer;
        //URL_PUBLISHSERVICE =URL_SERVER_SOAP+"/publishservice.asmx?wsdl";
        String ret = null;
        try {
            SoapObject request = new SoapObject(NAMESPACE, METHOD_PUBLISHINV);
            // Set the property info for the to currency
            //Tham số default
            PropertyInfo userCName = new PropertyInfo();
            userCName.setName("username");
            userCName.setValue(ConstantsApp.AUTHORIZE_WS_Account);
            userCName.setType(String.class);
            request.addProperty(userCName);

            PropertyInfo userCPass = new PropertyInfo();
            userCPass.setName("password");
            userCPass.setValue(ConstantsApp.AUTHORIZE_WS_Password);
            userCPass.setType(String.class);
            request.addProperty(userCPass);

            PropertyInfo pPattern = new PropertyInfo();
            pPattern.setName("pattern");
            pPattern.setValue(strPattern);
            pPattern.setType(String.class);
            request.addProperty(pPattern);

            PropertyInfo pSerial = new PropertyInfo();
            pSerial.setName("serial");
            pSerial.setValue(strSerial);
            pSerial.setType(String.class);
            request.addProperty(pSerial);

            PropertyInfo pinvIDs = new PropertyInfo();
            pinvIDs.setName("invIDs");
            pinvIDs.setValue(invIDs);
            pinvIDs.setType(String.class);
            request.addProperty(pinvIDs);

            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            envelope.dotNet = true;
            envelope.setOutputSoapObject(request);
            HttpTransportSE androidHttpTransport = new HttpTransportSE(URL_PUBLISHSERVICE, 30000);
            try {
                androidHttpTransport.call(SOAP_ACTION_PUBLISHINV, envelope);
                //SoapPrimitive  resultsRequestSOAP = (SoapPrimitive) envelope.getResponse();
                SoapPrimitive resultsRequestSOAP = (SoapPrimitive) envelope.getResponse();
//            SoapObject resultsRequestSOAP = (SoapObject) envelope.bodyIn;
                System.out.println("Response::"+resultsRequestSOAP.toString());
                ret = resultsRequestSOAP.toString();
            } catch (Exception e) {
                System.out.println("Error:"+e);
            }
        } catch (Exception ex) {
            Log.e(TAG, "getWS_publishInv");
            ex.printStackTrace();
        }
        return ret;
    }
    public String getWS_publishInvWithToken(String strPattern, String strSerial, String strUserName, String strUserPass) {
//        URL_SERVER_SOAP = strServer;
//        URL_PUBLISHSERVICE =URL_SERVER_SOAP+"/publishservice.asmx?wsdl";
        String ret = null;
        try {
            SoapObject request = new SoapObject(NAMESPACE, METHOD_PUBLISHINVWITHTOKEN);
            // Set the property info for the to currency
            //Tham số default
            PropertyInfo userName = new PropertyInfo();
            userName.setName("Account");
            userName.setValue(strUserName);
            userName.setType(String.class);
            request.addProperty(userName);

            PropertyInfo userPass = new PropertyInfo();
            userPass.setName("ACpass");
            userPass.setValue(strUserPass);
            userPass.setType(String.class);
            request.addProperty(userPass);

            PropertyInfo userCName = new PropertyInfo();
            userCName.setName("username");
            userCName.setValue(ConstantsApp.AUTHORIZE_WS_Account);
            userCName.setType(String.class);
            request.addProperty(userCName);

            PropertyInfo userCPass = new PropertyInfo();
            userCPass.setName("password");
            userCPass.setValue(ConstantsApp.AUTHORIZE_WS_Password);
            userCPass.setType(String.class);
            request.addProperty(userCPass);

            PropertyInfo pPattern = new PropertyInfo();
            pPattern.setName("pattern");
            pPattern.setValue(strPattern);
            pPattern.setType(String.class);
            request.addProperty(pPattern);

            PropertyInfo pSerial = new PropertyInfo();
            pSerial.setName("serial");
            pSerial.setValue(strSerial);
            pSerial.setType(String.class);
            request.addProperty(pSerial);



            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            envelope.dotNet = true;
            envelope.setOutputSoapObject(request);
            HttpTransportSE androidHttpTransport = new HttpTransportSE(URL_PUBLISHSERVICE, 30000);
            try {
                androidHttpTransport.call(SOAP_ACTION_PUBLISHINVWITHTOKEN, envelope);
                //SoapPrimitive  resultsRequestSOAP = (SoapPrimitive) envelope.getResponse();
                SoapPrimitive resultsRequestSOAP = (SoapPrimitive) envelope.getResponse();
//            SoapObject resultsRequestSOAP = (SoapObject) envelope.bodyIn;
                System.out.println("Response::"+resultsRequestSOAP.toString());
                ret = resultsRequestSOAP.toString();
            } catch (Exception e) {
                System.out.println("Error:"+e);
            }
        } catch (Exception ex) {
            Log.e(TAG, "getWS_publishInvWithToken");
            ex.printStackTrace();
        }
        return ret;
    }
}
