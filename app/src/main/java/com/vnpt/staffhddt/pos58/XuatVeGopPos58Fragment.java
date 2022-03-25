package com.vnpt.staffhddt.pos58;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.awesomedialog.blennersilva.awesomedialoglibrary.AwesomeProgressDialog;
import com.izettle.html2bitmap.Html2Bitmap;
import com.izettle.html2bitmap.content.WebViewContent;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.vnpt.common.Common;
import com.vnpt.common.ModelEvent;
import com.vnpt.dto.BienLai;
import com.vnpt.listener.OnEventControlListener;
import com.vnpt.printproject.pos58bus.command.sdk.Command;
import com.vnpt.printproject.pos58bus.command.sdk.PrintPicture;
import com.vnpt.printproject.pos58bus.command.sdk.PrinterCommand;
import com.vnpt.room.LoaiPhi;
import com.vnpt.staffhddt.MainPos58Activity;
import com.vnpt.staffhddt.R;
import com.vnpt.staffhddt.fragment.BaseFragment;
import com.vnpt.utils.DialogUtils;
import com.vnpt.utils.StoreSharePreferences;
import com.vnpt.utils.StringBienLai;
import com.vnpt.webservice.AppServices;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.vnpt.staffhddt.MainPos58Activity.mPOSPrinter;
import static com.vnpt.utils.Helper.hideSoftKeyboard;

public class XuatVeGopPos58Fragment extends BaseFragment implements View.OnClickListener, OnEventControlListener {

    private static final int REQUEST_BLUE_ADMIN = 888;
    Spinner spMenhGia;
    MaterialEditText edtSoLuong;
    Button btnXuatBL, btnInThu, btnCheckTB;
    TextView txtCompanyInfo;
    private AwesomeProgressDialog dg;
    StoreSharePreferences preferences = null;

    List<LoaiPhi> loaiPhiList;
    LoaiPhi loaiPhi = null;

    private XuatVeGopPos58Fragment.GetInvTask getInvTask = null;

    public static String TAG = XuatVeGopPos58Fragment.class.getName();

    private static final String[] PRICES = new String[]{
            "10.000"
    };

    private int type;

    public XuatVeGopPos58Fragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_xuat_ve_gop, container, false);

        setupUI(layout.findViewById(R.id.layout_frament_xuatvegop));
        init(layout);

        setEventForMembers();
        setValueForMembers();
        ((MainPos58Activity) getActivity()).showProccessbar(false);

        preferences = StoreSharePreferences.getInstance(getContext());

        return layout;
    }

    @Override
    protected void init(View layout) {
        spMenhGia = layout.findViewById(R.id.spMenhGia);
        edtSoLuong = layout.findViewById(R.id.edtSoLuong);
        btnXuatBL = layout.findViewById(R.id.btnXuatBienLai);
        btnInThu = layout.findViewById(R.id.btnInThu);
        btnCheckTB = layout.findViewById(R.id.btnCheck);
        txtCompanyInfo = layout.findViewById(R.id.txtCompanyInfo);
    }

    @Override
    protected void setValueForMembers() {
        String name = StoreSharePreferences.getInstance(getContext()).loadStringSavedPreferences(Common.KEY_COMPANY_NAME);
        String mst = StoreSharePreferences.getInstance(getContext()).loadStringSavedPreferences(Common.KEY_MST);
        txtCompanyInfo.setText(String.format("%s - MST: %s", name, mst));

        type = getArguments().getInt(Common.KEY_COMPANY_TYPE);

        if (type == Common.TYPE_VE) {
            btnXuatBL.setText(R.string.xuat_ve);
        } else {
            btnXuatBL.setText(R.string.xuat_bien_lai);
        }
    }

    @Override
    protected void setEventForMembers() {
        btnXuatBL.setOnClickListener(this);
        btnInThu.setOnClickListener(this);
        btnCheckTB.setOnClickListener(this);

        spMenhGia.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                loaiPhi = loaiPhiList.get(i);
//                Toast.makeText(getContext(), loaiPhi.getPATTERN(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                Toast.makeText(getContext(), "Vui lòng chọn ít nhất một loại phí", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void handleModelViewEvent(ModelEvent modelEvent) {
    }

    @Override
    public void handleErrorModelViewEvent(ModelEvent modelEvent) {

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Intent intent = getActivity().getIntent();
        if (intent != null) {
            loaiPhiList = (List<LoaiPhi>) intent.getSerializableExtra("KEY_LIST_FEE");
            if (loaiPhiList != null && loaiPhiList.size() > 0) {
                ArrayAdapter<LoaiPhi> adapter = new ArrayAdapter<LoaiPhi>(getContext(),
                        android.R.layout.simple_dropdown_item_1line, loaiPhiList);
                spMenhGia.setAdapter(adapter);
            } else {
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(),
                        android.R.layout.simple_dropdown_item_1line, PRICES);
                spMenhGia.setAdapter(adapter);
            }
            spMenhGia.setSelection(0);
        }
    }

    private void attemptGetInv() {
        if (getInvTask != null) {
            return;
        }

        String num = edtSoLuong.getText().toString().trim();

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(num)) {
            edtSoLuong.setError(getString(R.string.error_empty_input));
            focusView = edtSoLuong;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            showProgress(true);
            int numOfInv = Integer.parseInt(num);
            StringBuilder sb = new StringBuilder();

            //lap lai so ve
            for (int i = 1; i <= numOfInv; i++) {
                try {
                    TimeUnit.MILLISECONDS.sleep(1);
                    long currentTime = System.currentTimeMillis();
                    String keyData = i + "_INVE" + currentTime;
                    // mau XML o day
                    String xmlChildData = "";
                    if (type == Common.TYPE_VE) {
                        xmlChildData = "<Inv><key>" + keyData + "</key><Invoice><CusCode>" + StoreSharePreferences.getInstance(getContext()).loadStringSavedPreferences(Common.KEY_USER_NAME) + "</CusCode><Buyer></Buyer><CusName></CusName><CusAddress></CusAddress><CusPhone></CusPhone><CusTaxCode></CusTaxCode><PaymentMethod>TM, CK </PaymentMethod><KindOfService></KindOfService><Products><Product><ProdName>" + loaiPhi.getNAME() + "</ProdName><ProdUnit></ProdUnit><ProdQuantity></ProdQuantity><ProdPrice>" + loaiPhi.getTOTAL() + "</ProdPrice><Amount>" + loaiPhi.getTOTAL() + "</Amount><Extra1></Extra1><Extra2></Extra2></Product></Products><Total>" + loaiPhi.getTOTAL() + "</Total><VATRate>"+
                                loaiPhi.getVAT_RATE() + "</VATRate><VATAmount>" + loaiPhi.getVAT_AMOUNT() + "</VATAmount><Amount>" + loaiPhi.getAMOUNT() + "</Amount><AmountInWords>"+ StringBienLai.docSo(loaiPhi.getAMOUNT()) +"</AmountInWords><Extra></Extra></Invoice></Inv>";
                    } else {
                        xmlChildData = "<Inv><key>" + keyData + "</key><Invoice><CusCode><![CDATA[" + keyData + "]]></CusCode><CusName><![CDATA[" + StoreSharePreferences.getInstance(getContext()).loadStringSavedPreferences(Common.KEY_USER_NAME) + "]]></CusName><CusAddress><![CDATA[]]></CusAddress><CusPhone></CusPhone><CusTaxCode></CusTaxCode><PaymentMethod><![CDATA[]]></PaymentMethod><Products><Product><Code><![CDATA[]]></Code><ProdName>"+loaiPhi.getNAME()+"</ProdName><ProdUnit>Lần</ProdUnit><ProdQuantity>1</ProdQuantity><ProdPrice>"+loaiPhi.getAMOUNT()+"</ProdPrice><Amount>1</Amount></Product></Products><KindOfService><![CDATA[]]></KindOfService><Total>"+loaiPhi.getTOTAL()+"</Total><Amount>"+loaiPhi.getAMOUNT()+"</Amount><AmountInWords>"+StringBienLai.docSo(loaiPhi.getAMOUNT())+"</AmountInWords></Invoice></Inv>";
                    }
                    /////////////////////////
                    sb.append(xmlChildData);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            String xmlData = "<Invoices>" + sb.toString() + "</Invoices>";

            getInvTask = new XuatVeGopPos58Fragment.GetInvTask(
                    StoreSharePreferences.getInstance(getContext()).loadStringSavedPreferences(Common.KEY_USER_NAME),
                    StoreSharePreferences.getInstance(getContext()).loadStringSavedPreferences(Common.KEY_USER_PASS),
                    loaiPhi.getPATTERN(),
                    loaiPhi.getSERIAL(),
                    0,
                    xmlData);
            getInvTask.execute((Void) null);
        }
    }

    private void setupUI(View view) {
        // Set up touch listener for non-text box views to hide keyboard.
        if (!(view instanceof EditText)) {
            view.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    hideSoftKeyboard(getActivity());
                    return false;
                }
            });
        }

        //If a layout container, iterate over children and seed recursion.
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View innerView = ((ViewGroup) view).getChildAt(i);
                setupUI(innerView);
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnXuatBienLai: {
                if (mPOSPrinter.isBTActivated()) {
                    attemptGetInv();
                } else {
                    Toast.makeText(getContext(), "Vui lòng bật bluetooth và kết nối máy in", Toast.LENGTH_SHORT).show();
                }
                break;
            }
            case R.id.btnInThu: {
                if (mPOSPrinter.isBTActivated()) {
                    Print_Test();
                } else {
                    Toast.makeText(getContext(), "Vui lòng bật bluetooth và kết nối máy in", Toast.LENGTH_SHORT).show();
                }

                break;
            }
            case R.id.btnCheck: {
                checkPrinter();
                break;
            }
        }
    }

    @Override
    public void onEvent(int eventType, View control, Object data) {
        ((MainPos58Activity) getActivity()).showProccessbar(false);
    }

    //gui request len webservices de lay inv data
    public class GetInvTask extends AsyncTask<Void, Void, String> {

        //String username, String password, String strPattern, String strSerial, int convert, String strXmlInvData
        private String userName;
        private String password;
        private String strPattern;
        private String strSerial;
        private int convert;
        private String strXmlInvData;

        public GetInvTask(String userName, String password, String strPattern,
                          String strSerial, int convert, String strXmlInvData) {
            this.userName = userName;
            this.password = password;
            this.strPattern = strPattern;
            this.strSerial = strSerial;
            this.convert = convert;
            this.strXmlInvData = strXmlInvData;
        }

        @Override
        protected String doInBackground(Void... voids) {
            AppServices appServices = new AppServices(getActivity());
            String result = appServices.importAndPublishInv(userName, password, strPattern, strSerial, convert, strXmlInvData);
            return result;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            System.out.println(s);

            if (!s.split(":")[0].equals("ERR")) {

                String[] listResults = s.substring(s.lastIndexOf("-") + 1).split(",");

                printInvoice(listResults);
            } else {
                Toast.makeText(getContext(), s, Toast.LENGTH_SHORT).show();
            }
            getInvTask = null;
            showProgress(false);
            if (dg != null) {
                //if (dg.isShowing())
                dg.hide();
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            showProgress(false);
            if (dg != null) {
                //if (dg.isShowing())
                dg.hide();
            }
        }
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (show) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
                dg = DialogUtils.showLoadingDialog(getString(R.string.text_proccessing), getContext(), null);
            } else {
                dg = DialogUtils.showLoadingDialog(getString(R.string.text_proccessing), getContext(), null);
            }
        } else {
            if (dg != null) {
                //if (dg.isShowing())
                dg.hide();
            }
        }

    }

    public void printInvoice(String[] list) {

        float price = loaiPhi.getAMOUNT() * (list.length);
        String firstIdx = list[0].split("_")[2];
        String lastIndx = list[list.length - 1].split("_")[2];

        int soBL = list.length;
        BienLai bienLai = new BienLai();
        bienLai.setMoTa(loaiPhi.getNAME());
        bienLai.setGiaTien(price);
        bienLai.setMau(loaiPhi.getPATTERN());
        bienLai.setSo(firstIdx + " -> " + lastIndx);
        bienLai.setKyHieu(loaiPhi.getSERIAL());
        bienLai.setPortal(StoreSharePreferences.getInstance(getContext()).loadStringSavedPreferences(Common.KEY_COMPANY_PORTAL));

        byte reset[] = {0x1b, 0x40};
        byte lineFeed[] = {0x00, 0x0A};
        // Command -- Font Size, Alignment
        byte normalSize[] = {0x1D, 0x21, 0x00};
        byte dWidthSize[] = {0x1D, 0x21, 0x10};
        byte dHeightSize[] = {0x1D, 0x21, 0x01};
        byte rightAlign[] = {0x1B, 0x61, 0x02};
        byte centerAlign[] = {0x1B, 0x61, 0x01};
        byte leftAlign[] = {0x1B, 0x61, 0x00};

        String date = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date());

        //Get current locale information
//        Locale currentLocale = Locale.getDefault();
//
//        //Get currency instance from locale; This will have all currency related information
//        Currency currentCurrency = Currency.getInstance(currentLocale);
//
//        //Currency Formatter specific to locale
//        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(currentLocale);

        String name = (type == Common.TYPE_VE) ? "VÉ ĐIỆN TỬ" :  "BIÊN LAI ĐIỆN TỬ";

        String html = "<html>\n" +
                "                <body>\n" +
                "                <style>\n" +
                "                 </style>\n" +
                "<div style=\"text-align:center;\">"+
                "                   <h2 style=\\\"text-align: center;font-size: 40px\\\">"+StoreSharePreferences.getInstance(getContext()).loadStringSavedPreferences(Common.KEY_COMPANY_NAME)+"</h2>\n" +
                "                       <h2 style=\\\"text-align: center;font-size: 40px\\\">MST: "+ StoreSharePreferences.getInstance(getContext()).loadStringSavedPreferences(Common.KEY_MST) +
                "               <h1 style=\\\"text-align: center;font-size: 40px\\\">"+name+"</h1>\n" +
                "<h3 style='text-align:center;font-size: 30px'>("+loaiPhi.getNAME()+")</h3>" +
               // "<h3 style='text-align:right;font-size: 20px'>(BLĐT này không thay thế cho BL thu phí, lệ phí)</h3>" +
                "</div>"+
                "                \n" +"<h2 style=\\\"text-align: center;font-size: 40px\\\">Ngày: "+date+"</h2>"+
                "               <h3 style=\\\"text-align:right;font-size: 30px\\\">Mẫu: "+ bienLai.getMau() +"</h3>\n" +
                "                   <h3 style=\\\"text-align:right;font-size: 30px\\\">Ký hiệu: "+bienLai.getKyHieu()+"</h3>\n" +
                "                    <h2 style=\\\"text-align:right;font-size: 30px\\\">Số vé: "+bienLai.getSo()+"</h2>\n" +
                "                    <h2 style=\\\"text-align: right;font-size: 30px\\\">SL: "+soBL+"</h2>\n" +

                "                 <h2>Giá: "+ Math.round(bienLai.getGiaTien()) +" VND</h2>\n" +
                "<h2>("+ StringBienLai.docSo(price) +")</h2>" +

                "               <h1 style=\\\"text-align: center\\\">-------------------------- </h1>" +
                "</body></html>";

//        StringBuilder endContent = new StringBuilder();

//        for (String inv: list) {
//            endContent.append("<h3>").append(inv).append("</h3>");
//        }
//
//        html += endContent.toString() + "</body></html>";

        new converHTMLTask().execute(html);

    }

    private class converHTMLTask extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... str) {
            String html = str[0];
            //Toast.makeText(MainActivity.this, "Converting...", Toast.LENGTH_SHORT).show();
            return new Html2Bitmap.Builder()
                    .setContext(getContext())
                    .setContent(WebViewContent.html(html))
                    .setBitmapWidth(384)
                    .build()
                    .getBitmap();
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (bitmap != null) {
                Print_BMP(bitmap);
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            Toast.makeText(getContext(), "Quá trình in bị huỷ", Toast.LENGTH_SHORT).show();
        }
    }

    private void Print_BMP(Bitmap mBitmap){
        //	byte[] buffer = PrinterCommand.POS_Set_PrtInit();
        int nMode = 0;
        int nPaperWidth = 384;

        if(mBitmap != null)
        {
            byte[] data = PrintPicture.POS_PrintBMP(mBitmap, nPaperWidth, nMode);
            //	SendDataByte(buffer);
            SendDataByte(Command.ESC_Init);
            SendDataByte(Command.LF);
            SendDataByte(data);
            SendDataByte(PrinterCommand.POS_Set_PrtAndFeedPaper(50));
            SendDataByte(PrinterCommand.POS_Set_Cut(1));
            SendDataByte(PrinterCommand.POS_Set_PrtInit());
        }
    }


    /*
     *SendDataByte
     */
    private void SendDataByte(byte[] data) {

        if (mPOSPrinter.getState() != com.vnpt.printproject.pos58bus.BluetoothService.STATE_CONNECTED) {
            Toast.makeText(getContext(), R.string.not_connected, Toast.LENGTH_SHORT)
                    .show();
            return;
        }
        mPOSPrinter.write(data);
    }

    private void Print_Test() {
        String msg = "<html>\n" +
                "<body>\n" +
                "<style>\n" +
                "  </style>\n" +
                "<h1 style=\"text-align: center;font-size: 40px\">BIÊN LAI ĐIỆN TỬ</h1>\n" +
                "<h1 style=\"text-align: left;font-size: 40px\">STT: 1</h1>\n" +
                "<table style=\"text-align: center;width:100%;\">\n" +
                "  <tr>\n" +
                "    <th style=\"font-size:30px\">Phí:  Biên lai in thử</th>\n" +
                "  </tr>\n" +
                "</table>  <h1 style=\"text-align: center\">\n" +
                "    -----------------------------\n" +
                "  </h1>" +
                "\n" +
                "</body>\n" +
                "</html>";
        new converHTMLTask().execute(msg);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_BLUE_ADMIN) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getContext(), "Cấp quyền thành công", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Vui lòng cấp quyền cho ứng dụng", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void checkPrinter() {
        if (mPOSPrinter.getState() != com.vnpt.printproject.pos58bus.BluetoothService.STATE_CONNECTED) {
            Toast.makeText(getContext(), R.string.not_connected, Toast.LENGTH_SHORT)
                    .show();
        } else {
            Toast.makeText(getContext(), R.string.connected, Toast.LENGTH_SHORT)
                    .show();
        }
    }

}