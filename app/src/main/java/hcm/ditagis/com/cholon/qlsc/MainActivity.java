package hcm.ditagis.com.cholon.qlsc;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.CompoundButtonCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.esri.arcgisruntime.ArcGISRuntimeEnvironment;
import com.esri.arcgisruntime.ArcGISRuntimeException;
import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.data.CodedValue;
import com.esri.arcgisruntime.data.CodedValueDomain;
import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.data.Field;
import com.esri.arcgisruntime.data.QueryParameters;
import com.esri.arcgisruntime.data.ServiceFeatureTable;
import com.esri.arcgisruntime.geometry.Geometry;
import com.esri.arcgisruntime.geometry.GeometryEngine;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.layers.ArcGISMapImageLayer;
import com.esri.arcgisruntime.layers.ArcGISMapImageSublayer;
import com.esri.arcgisruntime.layers.ArcGISSublayer;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.Callout;
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.LocationDisplay;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.symbology.PictureMarkerSymbol;
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol;
import com.esri.arcgisruntime.symbology.UniqueValueRenderer;
import com.esri.arcgisruntime.util.ListenableList;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import hcm.ditagis.com.cholon.qlsc.async.FindLocationAsycn;
import hcm.ditagis.com.cholon.qlsc.async.PreparingAsycn;
import hcm.ditagis.com.cholon.qlsc.async.QueryServiceFeatureTableGetListAsync;
import hcm.ditagis.com.cholon.qlsc.entities.DAddress;
import hcm.ditagis.com.cholon.qlsc.entities.DApplication;
import hcm.ditagis.com.cholon.qlsc.entities.DLayerInfo;
import hcm.ditagis.com.cholon.qlsc.entities.entitiesDB.DFeatureLayer;
import hcm.ditagis.com.cholon.qlsc.fragment.task.HandlingSearchHasDone;
import hcm.ditagis.com.cholon.qlsc.utities.CheckConnectInternet;
import hcm.ditagis.com.cholon.qlsc.utities.Constant;
import hcm.ditagis.com.cholon.qlsc.utities.MapViewHandler;
import hcm.ditagis.com.cholon.qlsc.utities.MySnackBar;
import hcm.ditagis.com.cholon.qlsc.utities.Popup;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {
    public static DFeatureLayer DFeatureLayerDiemSuCo;

    private Popup mPopUp;
    private MapView mMapView;
    private DFeatureLayer mDFeatureLayer;

    public MapViewHandler getMapViewHandler() {
        return mMapViewHandler;
    }

    private MapViewHandler mMapViewHandler;
    private LocationDisplay mLocationDisplay;
    private int requestCode = 2;
    private GraphicsOverlay mGraphicsOverlay;
    private boolean mIsSearchingFeature = false;
    private LinearLayout mLayoutTimSuCo;
    private LinearLayout mLayoutTimDiaChi;
    private LinearLayout mLayoutTimKiem;
    private FloatingActionButton mFloatButtonLayer;
    private FloatingActionButton mFloatButtonLocation;
    private List<DFeatureLayer> mDFeatureLayers;
    private Point mPointFindLocation;
    private Geocoder mGeocoder;
    private ImageView mImageOpenStreetMap, mImageStreetMap, mImageImageWithLabel;
    private TextView mTxtOpenStreetMap, mTxtStreetMap, mTxtImageWithLabel;
    private SearchView mTxtSearchView;
    private int states[][];
    private int colors[];
    private ArcGISMapImageLayer hanhChinhImageLayers, taiSanImageLayers;
    private LinearLayout mLayoutDisplayLayerThematic, mLayoutDisplayLayerAdministration;
    private SeekBar mSeekBarAdministrator, mSeekBarThematic;
    private List<String> mListLayerID;
    private DApplication mApplication;
    private boolean mIsFirstLocating = true;
    private boolean isChangingGeometry = false;
    private FeatureLayer mFeatureLayer;
    private LinearLayout mLLayoutSearch;

    public void setChangingGeometry(boolean changingGeometry) {
        isChangingGeometry = changingGeometry;
    }

    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quan_ly_su_co);
        mListLayerID = new ArrayList<>();
        mApplication = (DApplication) getApplication();
        startSignIn();
    }

    private void startSignIn() {
        Intent intent = new Intent(MainActivity.this, LogInActivity.class);
        MainActivity.this.startActivityForResult(intent, Constant.RequestCode.LOGIN);
    }

    public void requestPermisson() {
        boolean permissionCheck1 = ContextCompat.checkSelfPermission(this,
                Constant.REQUEST_PERMISSIONS[0]) == PackageManager.PERMISSION_GRANTED;
        boolean permissionCheck2 = ContextCompat.checkSelfPermission(this,
                Constant.REQUEST_PERMISSIONS[1]) == PackageManager.PERMISSION_GRANTED;
        boolean permissionCheck3 = ContextCompat.checkSelfPermission(this,
                Constant.REQUEST_PERMISSIONS[2]) == PackageManager.PERMISSION_GRANTED;
        boolean permissionCheck4 = ContextCompat.checkSelfPermission(this,
                Constant.REQUEST_PERMISSIONS[3]) == PackageManager.PERMISSION_GRANTED;

        if (!(permissionCheck1 && permissionCheck2 && permissionCheck3 && permissionCheck4)) {
            // If permissions are not already granted, request permission from the user.
            ActivityCompat.requestPermissions(this, Constant.REQUEST_PERMISSIONS, Constant.RequestCode.PERMISSION);
        }  // Report other unknown failure types to the user - for example, location services may not // be enabled on the device. //                    String message = String.format("Error in DataSourceStatusChangedListener: %s", dataSourceStatusChangedEvent //                            .getSource().getLocationDataSource().getError().getMessage()); //                    Toast.makeText(QuanLySuCo.this, message, Toast.LENGTH_LONG).show();
        else {
            PreparingAsycn preparingAsycn = new PreparingAsycn(this, mApplication, output -> {
                if (output != null && output.size() > 0) {
                    mApplication.setLayerInfos(output);
                    startMain();
                } else {
                    Toast.makeText(MainActivity.this, "Có lỗi xảy ra", Toast.LENGTH_SHORT).show();
                    startSignIn();
                }
            });
            if (CheckConnectInternet.isOnline(this))
                preparingAsycn.execute();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        int expected = grantResults.length;
        int sum = 0;
        for (int i : grantResults)
            if (i == PackageManager.PERMISSION_GRANTED)
                sum++;
        if (sum == expected) {
            PreparingAsycn preparingAsycn = new PreparingAsycn(this, mApplication, output -> {
                if (output != null && output.size() > 0) {
                    mApplication.setLayerInfos(output);
                    startMain();
                } else {
                    Toast.makeText(MainActivity.this, "Có lỗi xảy ra", Toast.LENGTH_SHORT).show();
                    startSignIn();
                }
            });
            if (CheckConnectInternet.isOnline(this))
                preparingAsycn.execute();
        } else {
//            Toast.makeText(MainActivity.this, "Vui lòng cho phép ứng dụng truy cập các quyền trên", Toast.LENGTH_LONG).show();
            requestPermisson();
        }
    }

    @SuppressLint({"ClickableViewAccessibility", "SetTextI18n"})
    private void startMain() {
        // create an empty map instance
        mListLayerID.clear();
        hanhChinhImageLayers = taiSanImageLayers = null;
        states = new int[][]{{android.R.attr.state_checked}, {}};
        colors = new int[]{R.color.colorTextColor_1, R.color.colorTextColor_1};
        findViewById(R.id.layout_layer).setVisibility(View.INVISIBLE);
        setLicense();
        mGeocoder = new Geocoder(this.getApplicationContext(), Locale.getDefault());
        mLayoutDisplayLayerThematic = findViewById(R.id.linearDisplayLayerFeature);
        mLayoutDisplayLayerAdministration = findViewById(R.id.linearDisplayLayerAdministration);
        mLayoutDisplayLayerThematic.removeAllViews();
        mLayoutDisplayLayerAdministration.removeAllViews();
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        //for camera begin
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        //for camera end
        mLLayoutSearch = findViewById(R.id.llayout_main_search);
        //đưa listview search ra phía sau
        mLLayoutSearch.invalidate();
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(MainActivity.this,
                drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(MainActivity.this);
        mMapView = findViewById(R.id.mapView);
        mMapView.setMap(new ArcGISMap(Basemap.Type.OPEN_STREET_MAP, 10.7554041, 106.6546293, 12));

        mMapView.getMap().addDoneLoadingListener(this::handlingMapViewDoneLoading);
        final EditText edit_latitude_vido = findViewById(R.id.edit_latitude_vido);
        final EditText edit_longtitude_kinhdo = findViewById(R.id.edit_longtitude_kinhdo);
        mMapView.setOnTouchListener(new DefaultMapViewOnTouchListener(this, mMapView) {
            @Override
            public void onLongPress(MotionEvent e) {
                addGraphicsAddFeature(e);
                super.onLongPress(e);
            }

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                try {
                    if (mMapViewHandler != null)
                        mMapViewHandler.onSingleTapMapView(e);
                } catch (ArcGISRuntimeException ex) {
                    Log.d("", ex.toString());
                }
                return super.onSingleTapConfirmed(e);
            }

            @SuppressLint("SetTextI18n")
            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                mGraphicsOverlay.getGraphics().clear();
                return super.onScroll(e1, e2, distanceX, distanceY);
            }

            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                return super.onScale(detector);
            }
        });
        mGraphicsOverlay = new GraphicsOverlay();
        mMapView.getGraphicsOverlays().add(mGraphicsOverlay);

        mSeekBarAdministrator = findViewById(R.id.skbr_hanhchinh_app_bar_quan_ly_su_co);
        mSeekBarThematic = findViewById(R.id.skbr_chuyende_app_bar_quan_ly_su_co);
        mSeekBarAdministrator.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                hanhChinhImageLayers.setOpacity((float) i / 100);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        mSeekBarThematic.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                taiSanImageLayers.setOpacity((float) i / 100);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        findViewById(R.id.layout_layer_open_street_map).setOnClickListener(this);
        findViewById(R.id.layout_layer_street_map).setOnClickListener(this);
        findViewById(R.id.layout_layer_image).setOnClickListener(this);


        mTxtOpenStreetMap = findViewById(R.id.txt_layer_open_street_map);
        mTxtStreetMap = findViewById(R.id.txt_layer_street_map);
        mTxtImageWithLabel = findViewById(R.id.txt_layer_image);
        mImageOpenStreetMap = findViewById(R.id.img_layer_open_street_map);
        mImageStreetMap = findViewById(R.id.img_layer_street_map);
        mImageImageWithLabel = findViewById(R.id.img_layer_image);

        mFloatButtonLayer = findViewById(R.id.floatBtnLayer);
        mFloatButtonLayer.setOnClickListener(this);
        findViewById(R.id.btn_add_feature_close).setOnClickListener(this);
        findViewById(R.id.btn_layer_close).setOnClickListener(this);
        findViewById(R.id.img_chonvitri_themdiemsuco).setOnClickListener(this);
        mFloatButtonLocation = findViewById(R.id.floatBtnLocation);
        mFloatButtonLocation.setOnClickListener(this);
        mLayoutTimSuCo = findViewById(R.id.layout_tim_su_co);
        mLayoutTimSuCo.setOnClickListener(this);
        mLayoutTimDiaChi = findViewById(R.id.layout_tim_dia_chi);
        mLayoutTimDiaChi.setOnClickListener(this);
        mLayoutTimKiem = findViewById(R.id.layout_tim_kiem);
        ((TextView) findViewById(R.id.txt_nav_header_tenNV)).setText(mApplication.getUserDangNhap().getUserName());
        ((TextView) findViewById(R.id.txt_nav_header_displayname)).setText(mApplication.getUserDangNhap().getDisplayName());
        optionSearchFeature();

    }

    public void addFeature() {
        Intent intentAdd = new Intent(MainActivity.this, AddFeatureActivity.class);
        startActivityForResult(intentAdd, Constant.RequestCode.ADD);
    }

    public void handlingAddFeatureSuccess() {
        handlingCancelAdd();
        mMapViewHandler.query(String.format(Constant.QUERY_BY_OBJECTID, mApplication.getDiemSuCo().getObjectID()));

        mApplication.getDiemSuCo().clear();
    }

    public void handlingCancelAdd() {
        if (mPopUp.getCallout() != null && mPopUp.getCallout().isShowing()) {
            mPopUp.getCallout().dismiss();
        }
        mGraphicsOverlay.getGraphics().clear();
    }


    private void addGraphicsAddFeature(MotionEvent... e) {
        Point center;
        if (e.length == 0)
            center = mMapView.getCurrentViewpoint(Viewpoint.Type.CENTER_AND_SCALE).getTargetGeometry().getExtent().getCenter();
        else {
            center = mMapView.screenToLocation(new android.graphics.Point(Math.round(e[0].getX()), Math.round(e[0].getY())));
            mMapView.setViewpointCenterAsync(center);
        }
        SimpleMarkerSymbol symbol = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CROSS, Color.YELLOW, 20);
        Graphic graphic = new Graphic(center, symbol);
        mGraphicsOverlay.getGraphics().clear();
        mGraphicsOverlay.getGraphics().add(graphic);
        mPopUp.showPopupAdd(center, isChangingGeometry);
        mPointFindLocation = center;
    }

    public void findRoute() {
        String uri = String.format("google.navigation:q=%s", Uri.encode(mApplication.getSelectedArcGISFeature().getAttributes().get(
                Constant.FieldSuCo.DIA_CHI
        ).toString()));
        Uri gmmIntentUri = Uri.parse(uri);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        startActivity(mapIntent);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void handlingMapViewDoneLoading() {
        mLocationDisplay = mMapView.getLocationDisplay();
        mLocationDisplay.startAsync();

//        loginWithPortal1();
        setServices();
    }

    private void setServices() {
        try {
            // config feature layer service
            mDFeatureLayers = new ArrayList<>();
            for (final DLayerInfo dLayerInfo : mApplication.getLayerInfos()) {
                if (dLayerInfo.getId().substring(dLayerInfo.getId().length() - 3).equals("TBL") || !dLayerInfo.isView())
                    continue;
                String url = dLayerInfo.getUrl();
                if (!dLayerInfo.getUrl().startsWith("http"))
                    url = "http:" + dLayerInfo.getUrl();
                if (url == null)
                    continue;
                if (dLayerInfo.getId().equals(getString(R.string.IDLayer_Basemap)) && hanhChinhImageLayers == null) {
                    hanhChinhImageLayers = new ArcGISMapImageLayer(url);
                    hanhChinhImageLayers.setId(dLayerInfo.getId());
                    mMapView.getMap().getOperationalLayers().add(hanhChinhImageLayers);
                    hanhChinhImageLayers.addDoneLoadingListener(() -> {
                        if (hanhChinhImageLayers.getLoadStatus() == LoadStatus.LOADED) {
                            ListenableList<ArcGISSublayer> sublayerList = hanhChinhImageLayers.getSublayers();
                            for (ArcGISSublayer sublayer : sublayerList) {
                                addCheckBox((ArcGISMapImageSublayer) sublayer, states, colors, true);

                            }

                        }
                    });
                    hanhChinhImageLayers.loadAsync();
                } else if (dLayerInfo.getId().equals(getString(R.string.IDLayer_DiemSuCo))) {
                    final ServiceFeatureTable serviceFeatureTable = new ServiceFeatureTable(url);
                    mFeatureLayer = new FeatureLayer(serviceFeatureTable);
                    if (dLayerInfo.getDefinition().toLowerCase().equals("null")) {
                        mFeatureLayer.setDefinitionExpression(Constant.DEFINITION_HIDE_COMPLETE);
                    } else
                        mFeatureLayer.setDefinitionExpression(dLayerInfo.getDefinition().concat(" and ").concat(Constant.DEFINITION_HIDE_COMPLETE));
                    mFeatureLayer.setId(dLayerInfo.getId());
                    mFeatureLayer.setName(dLayerInfo.getTitleLayer());
                    mFeatureLayer.setId(dLayerInfo.getId());
                    mFeatureLayer.setPopupEnabled(true);
                    mFeatureLayer.setMinScale(0);
                    mFeatureLayer.addDoneLoadingListener(() -> {
                        setRendererSuCoFeatureLayer(mFeatureLayer);
                        mDFeatureLayer = new DFeatureLayer(serviceFeatureTable, mFeatureLayer, dLayerInfo);
                        mApplication.setDFeatureLayer(mDFeatureLayer);
                        mDFeatureLayers.add(mDFeatureLayer);
                        Callout callout = mMapView.getCallout();
                        mPopUp = new Popup(MainActivity.this, mMapView, serviceFeatureTable, callout, mGeocoder);


                        DFeatureLayerDiemSuCo = mDFeatureLayer;

                        mMapViewHandler = new MapViewHandler(this, mDFeatureLayer, callout, mMapView, mPopUp,
                                MainActivity.this, mGeocoder);
                        mMapViewHandler.setFeatureLayerDTGs(mDFeatureLayers);

                    });
                    mMapView.getMap().getOperationalLayers().add(mFeatureLayer);

                } else if (!dLayerInfo.getId().equals("diemdanhgiaLYR") && taiSanImageLayers == null) {

                    taiSanImageLayers = new ArcGISMapImageLayer(url.replaceFirst("FeatureServer(.*)", "MapServer"));
                    taiSanImageLayers.setName(dLayerInfo.getTitleLayer());
                    taiSanImageLayers.setId(dLayerInfo.getId());
                    mMapView.getMap().getOperationalLayers().add(taiSanImageLayers);
                    taiSanImageLayers.addDoneLoadingListener(() -> {
                        if (taiSanImageLayers.getLoadStatus() == LoadStatus.LOADED) {

                            ListenableList<ArcGISSublayer> sublayerList = taiSanImageLayers.getSublayers();
                            for (ArcGISSublayer sublayer : sublayerList) {
                                if (sublayer.getId() == 13) {
                                    sublayer.setVisible(false);
                                } else
                                    addCheckBox((ArcGISMapImageSublayer) sublayer, states, colors, false);
                            }

                        }
                    });
                    taiSanImageLayers.loadAsync();
                }

            }
        } catch (Exception e) {
            Log.e("error", e.toString());
        }
//        mMapViewHandler.setFeatureLayerDTGs(mDFeatureLayers);
    }

    private void addCheckBox(final ArcGISMapImageSublayer layer, int[][] states, int[] colors, boolean isAdministrator) {
        @SuppressLint("InflateParams") LinearLayout layoutFeature = (LinearLayout) getLayoutInflater().inflate(R.layout.layout_feature, null);
        final CheckBox checkBox = layoutFeature.findViewById(R.id.ckb_layout_feature);
        final TextView textView = layoutFeature.findViewById(R.id.txt_layout_feature);
        textView.setTextColor(getResources().getColor(android.R.color.black));
        textView.setText(layer.getName());
        checkBox.setChecked(false);
        layer.setVisible(false);
        CompoundButtonCompat.setButtonTintList(checkBox, new ColorStateList(states, colors));
        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {

            if (buttonView.isChecked()) {
                if (textView.getText().equals(layer.getName()))
                    layer.setVisible(true);


            } else {
                if (textView.getText().equals(layer.getName()))
                    layer.setVisible(false);
            }
        });
        if (!mListLayerID.contains(layer.getName())) {
            if (isAdministrator) mLayoutDisplayLayerAdministration.addView(layoutFeature);
            else mLayoutDisplayLayerThematic.addView(layoutFeature);
            mListLayerID.add(layer.getName());
        }
    }


    private void setLicense() {
        //way 1
        ArcGISRuntimeEnvironment.setLicense(getString(R.string.license));
        //way 2
//        UserCredential credential = new UserCredential("thanle95", "Gemini111");
//
//// replace the URL with either the ArcGIS Online URL or your portal URL
//        final Portal portal = new Portal("https://than-le.maps.arcgis.com");
//        portal.setCredential(credential);
//
//// load portal and listen to done loading event
//        portal.loadAsync();
//        portal.addDoneLoadingListener(new Runnable() {
//            @Override
//            public void run() {
//                LicenseInfo licenseInfo = portal.getPortalInfo().getLicenseInfo();
//                // Apply the license at Standard level
//                ArcGISRuntimeEnvironment.setLicense(licenseInfo);
//            }
//        });
    }

    private void setRendererSuCoFeatureLayer(FeatureLayer mSuCoTanHoaLayer) {
        UniqueValueRenderer uniqueValueRenderer = new UniqueValueRenderer();
        uniqueValueRenderer.getFieldNames().add(Constant.FieldSuCo.TRANG_THAI);
        uniqueValueRenderer.getFieldNames().add(Constant.FieldSuCo.THONG_TIN_PHAN_ANH);
        PictureMarkerSymbol chuaXuLyDacBiet = new PictureMarkerSymbol(Constant.URLImage.CHUA_SUA_CHUA_BAT_THUONG);
        chuaXuLyDacBiet.setHeight(getResources().getInteger(R.integer.size_feature_renderer));
        chuaXuLyDacBiet.setWidth(getResources().getInteger(R.integer.size_feature_renderer));
        PictureMarkerSymbol chuaXuLyBinhThuong = new PictureMarkerSymbol(Constant.URLImage.CHUA_SUA_CHUA);
        chuaXuLyBinhThuong.setHeight(getResources().getInteger(R.integer.size_feature_renderer));
        chuaXuLyBinhThuong.setWidth(getResources().getInteger(R.integer.size_feature_renderer));
        PictureMarkerSymbol hoanThanh = new PictureMarkerSymbol(Constant.URLImage.HOAN_THANH);
        chuaXuLyBinhThuong.setHeight(getResources().getInteger(R.integer.size_feature_renderer));
        chuaXuLyBinhThuong.setWidth(getResources().getInteger(R.integer.size_feature_renderer));

        uniqueValueRenderer.setDefaultSymbol(chuaXuLyBinhThuong);
        uniqueValueRenderer.setDefaultLabel("Chưa xác định");

        List<Object> dacBietValue1 = new ArrayList<>();
        dacBietValue1.add(Constant.TrangThaiSuCo.CHUA_XU_LY);
        dacBietValue1.add(Constant.ThongTinPhanAnh.KHONG_NUOC);
        List<Object> dacBietValue2 = new ArrayList<>();
        dacBietValue2.add(Constant.TrangThaiSuCo.CHUA_XU_LY);
        dacBietValue2.add(Constant.ThongTinPhanAnh.XI_DHN);
        List<Object> dacBietValue3 = new ArrayList<>();
        dacBietValue3.add(Constant.TrangThaiSuCo.CHUA_XU_LY);
        dacBietValue3.add(Constant.ThongTinPhanAnh.ONG_BE);

        List<Object> binhThuongValue = new ArrayList<>();
        binhThuongValue.add(Constant.TrangThaiSuCo.CHUA_XU_LY);

        List<Object> hoanThanhValueNull = new ArrayList<>();
        hoanThanhValueNull.add(Constant.TrangThaiSuCo.HOAN_THANH);
        hoanThanhValueNull.add(null);
        List<Object> hoanThanhValue0 = new ArrayList<>();
        hoanThanhValue0.add(Constant.TrangThaiSuCo.HOAN_THANH);
        hoanThanhValue0.add(Constant.ThongTinPhanAnh.KHAC);
        List<Object> hoanThanhValue1 = new ArrayList<>();
        hoanThanhValue1.add(Constant.TrangThaiSuCo.HOAN_THANH);
        hoanThanhValue1.add(Constant.ThongTinPhanAnh.KHONG_NUOC);
        List<Object> hoanThanhValue2 = new ArrayList<>();
        hoanThanhValue2.add(Constant.TrangThaiSuCo.HOAN_THANH);
        hoanThanhValue2.add(Constant.ThongTinPhanAnh.NUOC_DUC);
        List<Object> hoanThanhValue3 = new ArrayList<>();
        hoanThanhValue3.add(Constant.TrangThaiSuCo.HOAN_THANH);
        hoanThanhValue3.add(Constant.ThongTinPhanAnh.NUOC_YEU);
        List<Object> hoanThanhValue4 = new ArrayList<>();
        hoanThanhValue4.add(Constant.TrangThaiSuCo.HOAN_THANH);
        hoanThanhValue4.add(Constant.ThongTinPhanAnh.XI_DHN);
        List<Object> hoanThanhValue5 = new ArrayList<>();
        hoanThanhValue5.add(Constant.TrangThaiSuCo.HOAN_THANH);
        hoanThanhValue5.add(Constant.ThongTinPhanAnh.HU_VAN);
        List<Object> hoanThanhValue6 = new ArrayList<>();
        hoanThanhValue6.add(Constant.TrangThaiSuCo.HOAN_THANH);
        hoanThanhValue6.add(Constant.ThongTinPhanAnh.ONG_BE);

        uniqueValueRenderer.getUniqueValues().add(new UniqueValueRenderer.UniqueValue(
                "Chưa xử lý", "Chưa xử lý", chuaXuLyDacBiet, dacBietValue1));
        uniqueValueRenderer.getUniqueValues().add(new UniqueValueRenderer.UniqueValue(
                "Chưa xử lý", "Chưa xử lý", chuaXuLyDacBiet, dacBietValue2));
        uniqueValueRenderer.getUniqueValues().add(new UniqueValueRenderer.UniqueValue(
                "Chưa xử lý", "Chưa xử lý", chuaXuLyDacBiet, dacBietValue3));

        uniqueValueRenderer.getUniqueValues().add(new UniqueValueRenderer.UniqueValue(
                "Chưa xử lý", "Chưa xử lý", chuaXuLyBinhThuong, binhThuongValue));

        uniqueValueRenderer.getUniqueValues().add(new UniqueValueRenderer.UniqueValue(
                "Hoàn thành", "Hoàn thành", hoanThanh, hoanThanhValueNull));
        uniqueValueRenderer.getUniqueValues().add(new UniqueValueRenderer.UniqueValue(
                "Hoàn thành", "Hoàn thành", hoanThanh, hoanThanhValue0));
        uniqueValueRenderer.getUniqueValues().add(new UniqueValueRenderer.UniqueValue(
                "Hoàn thành", "Hoàn thành", hoanThanh, hoanThanhValue1));
        uniqueValueRenderer.getUniqueValues().add(new UniqueValueRenderer.UniqueValue(
                "Hoàn thành", "Hoàn thành", hoanThanh, hoanThanhValue2));
        uniqueValueRenderer.getUniqueValues().add(new UniqueValueRenderer.UniqueValue(
                "Hoàn thành", "Hoàn thành", hoanThanh, hoanThanhValue3));
        uniqueValueRenderer.getUniqueValues().add(new UniqueValueRenderer.UniqueValue(
                "Hoàn thành", "Hoàn thành", hoanThanh, hoanThanhValue4));
        uniqueValueRenderer.getUniqueValues().add(new UniqueValueRenderer.UniqueValue(
                "Hoàn thành", "Hoàn thành", hoanThanh, hoanThanhValue5));
        uniqueValueRenderer.getUniqueValues().add(new UniqueValueRenderer.UniqueValue(
                "Hoàn thành", "Hoàn thành", hoanThanh, hoanThanhValue6));
        mSuCoTanHoaLayer.setRenderer(uniqueValueRenderer);
        mSuCoTanHoaLayer.loadAsync();
    }

    private void setViewPointCenter(final Point position) {
        if (mPopUp == null) {
            MySnackBar.make(mMapView, getString(R.string.message_unloaded_map), true);
        } else {
            final Geometry geometry = GeometryEngine.project(position, SpatialReferences.getWebMercator());
            final ListenableFuture<Boolean> booleanListenableFuture = mMapView.setViewpointCenterAsync(geometry.getExtent().getCenter());
            booleanListenableFuture.addDoneListener(() -> {
                try {
                    if (booleanListenableFuture.get()) {
                        MainActivity.this.mPointFindLocation = position;
                    }
//                    mPopUp.showPopupFindLocation(position);
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }

            });
        }

    }

    private void setViewPointCenterLongLat(Point position, String location) {
        if (mPopUp == null) {
            MySnackBar.make(mMapView, getString(R.string.message_unloaded_map), true);
        } else {
            Geometry geometry = GeometryEngine.project(position, SpatialReferences.getWgs84());
            Geometry geometry1 = GeometryEngine.project(geometry, SpatialReferences.getWebMercator());
            Point point = geometry1.getExtent().getCenter();

            SimpleMarkerSymbol symbol = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CROSS, Color.RED, 20);
            Graphic graphic = new Graphic(point, symbol);
            mGraphicsOverlay.getGraphics().add(graphic);

            mMapView.setViewpointCenterAsync(point, getResources().getInteger(R.integer.SCALE_IMAGE_WITH_LABLES));
//            mPopUp.showPopupFindLocation(point, location);
            this.mPointFindLocation = point;
        }

    }


    private void optionSearchFeature() {
        this.mIsSearchingFeature = true;
        mLayoutTimSuCo.setBackgroundResource(R.drawable.layout_border_bottom);
        mLayoutTimDiaChi.setBackgroundResource(R.drawable.layout_shape_basemap_none);
    }

    private void optionFindRoute() {
        this.mIsSearchingFeature = false;
        mLayoutTimDiaChi.setBackgroundResource(R.drawable.layout_border_bottom);
        mLayoutTimSuCo.setBackgroundResource(R.drawable.layout_shape_basemap_none);
    }

    private void deleteSearching() {
        mGraphicsOverlay.getGraphics().clear();
        mLLayoutSearch.removeAllViews();
    }


    private void visibleFloatActionButton() {
        if (mFloatButtonLayer.getVisibility() == View.VISIBLE) {
            mFloatButtonLayer.setVisibility(View.INVISIBLE);
            mFloatButtonLocation.setVisibility(View.INVISIBLE);
        } else {
            mFloatButtonLayer.setVisibility(View.VISIBLE);
            mFloatButtonLocation.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }  //            super.onBackPressed();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.quan_ly_su_co, menu);
        mTxtSearchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        mTxtSearchView.setQueryHint(getString(R.string.title_search));
        mTxtSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public boolean onQueryTextSubmit(String query) {
                try {
                    mLLayoutSearch.removeAllViews();
                    if (mIsSearchingFeature && mMapViewHandler != null) {
                        mPopUp.getCallout().dismiss();

                        mFeatureLayer.clearSelection();
                        QueryParameters queryParameters = new QueryParameters();
                        StringBuilder builder = new StringBuilder();
                        for (Field field : mFeatureLayer.getFeatureTable().getFields()) {
                            switch (field.getFieldType()) {
                                case OID:
                                case INTEGER:
                                case SHORT:
                                    try {
                                        int search = Integer.parseInt(query);
                                        builder.append(String.format("%s = %s", field.getName(), search));
                                        builder.append(" or ");
                                    } catch (Exception ignored) {

                                    }
                                    break;
                                case FLOAT:
                                case DOUBLE:
                                    try {
                                        double search = Double.parseDouble(query);
                                        builder.append(String.format("%s = %s", field.getName(), search));
                                        builder.append(" or ");
                                    } catch (Exception ignored) {

                                    }
                                    break;
                                case TEXT:
                                    builder.append(field.getName()).append(" like N'%").append(query).append("%'");
                                    builder.append(" or ");
                                    break;
                            }
                        }
                        builder.append(" 1 = 2 ");
                        queryParameters.setWhereClause(builder.toString());

                        new QueryServiceFeatureTableGetListAsync(MainActivity.this, output -> {
                            if (output != null && output.size() > 0) {
                                List<HandlingSearchHasDone.Item> items = new ArrayList<>();
                                for (Feature feature : output) {
                                    Map<String, Object> attributes = feature.getAttributes();
                                    Object idSuCo = attributes.get(Constant.FieldSuCo.ID_SUCO);
                                    Object ngayXayRa = attributes.get(Constant.FieldSuCo.TG_PHAN_ANH);
                                    Object thongTinPhanAnhCode = attributes.get(Constant.FieldSuCo.THONG_TIN_PHAN_ANH);
                                    List<CodedValue> codedValues = ((CodedValueDomain) feature.getFeatureTable().getField(Constant.FieldSuCo.THONG_TIN_PHAN_ANH).getDomain()).getCodedValues();
                                    Object thongTinPhanAnhValue = thongTinPhanAnhCode == null ? null : HandlingSearchHasDone.getValueDomain(codedValues, thongTinPhanAnhCode);
                                    items.add(new HandlingSearchHasDone.Item(Integer.parseInt(attributes.get(Constant.Field.OBJECTID).toString()),
                                            idSuCo != null ? idSuCo.toString() : "",
                                            ngayXayRa != null ? Constant.DateFormat.DATE_FORMAT_VIEW.format(((Calendar) ngayXayRa).getTime()) : "",
                                            attributes.get(Constant.FieldSuCo.DIA_CHI) != null ? attributes.get(Constant.FieldSuCo.DIA_CHI).toString() : "",
                                            thongTinPhanAnhCode != null ? Short.parseShort(thongTinPhanAnhCode.toString()) : Constant.ThongTinPhanAnh.KHAC,
                                            thongTinPhanAnhValue != null ? thongTinPhanAnhValue.toString() : ""));
                                }
                                List<View> views = HandlingSearchHasDone.handleFromItems(MainActivity.this, MainActivity.this, items);
                                for (View view : views) {
                                    TextView txtID = view.findViewById(R.id.txt_top);
                                    view.setOnClickListener(v -> {
                                        if (mMapViewHandler != null) {
                                            mMapViewHandler.query(String.format(Constant.QUERY_BY_SUCOID, txtID.getText().toString()));
                                            mLLayoutSearch.removeAllViews();
                                        }
                                    });
                                    mLLayoutSearch.addView(view);
                                }
                            } else {

                            }
                        }).execute(queryParameters);
                    } else if (query.length() > 0) {
                        deleteSearching();
                        FindLocationAsycn findLocationAsycn = new FindLocationAsycn(MainActivity.this,
                                true, output -> {
                            if (output != null) {
                                if (output.size() > 0) {
                                    for (DAddress address : output) {
                                        HandlingSearchHasDone.Item item = new HandlingSearchHasDone.Item(-1, "", "", address.getLocation(), Constant.ThongTinPhanAnh.KHAC, null);
                                        item.setLatitude(address.getLatitude());
                                        item.setLongtitude(address.getLongtitude());

                                        LinearLayout layout = (LinearLayout) MainActivity.this.getLayoutInflater().inflate(R.layout.item_tracuu, null);
                                        TextView txtThongTinPhanAnh = layout.findViewById(R.id.txt_bottom);
                                        TextView txtDiaChi = layout.findViewById(R.id.txt_bottom1);
                                        TextView txtID = layout.findViewById(R.id.txt_top);
                                        TextView txtNgayCapNhat = layout.findViewById(R.id.txt_right);


                                        txtID.setVisibility(View.GONE);
                                        txtDiaChi.setText(item.getDiaChi());

                                        txtNgayCapNhat.setVisibility(View.GONE);
                                        txtThongTinPhanAnh.setVisibility(View.GONE);
                                        layout.setOnClickListener(v -> {

                                            setViewPointCenterLongLat(new Point(item.getLongtitude(), item.getLatitude()), item.getDiaChi());
                                            Log.d("Tọa độ tìm kiếm", String.format("[% ,.9f;% ,.9f]", item.getLongtitude(), item.getLatitude()));
                                        });
                                        mLLayoutSearch.addView(layout);
                                    }

                                    //                                    }
                                }
                            }

                        });
                        findLocationAsycn.execute(query);

                    }
                } catch (Exception e) {
                    Log.e("Lỗi tìm kiếm", e.toString());
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.trim().length() > 0 && !mIsSearchingFeature) {
                } else {
                    mLLayoutSearch.removeAllViews();
                    mGraphicsOverlay.getGraphics().clear();
                }
                return false;
            }
        });
        menu.findItem(R.id.action_search).

                setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
                    @Override
                    public boolean onMenuItemActionExpand(MenuItem item) {
                        visibleFloatActionButton();
                        mLayoutTimKiem.setVisibility(View.VISIBLE);
                        return true;
                    }

                    @Override
                    public boolean onMenuItemActionCollapse(MenuItem item) {
                        mLayoutTimKiem.setVisibility(View.INVISIBLE);
                        visibleFloatActionButton();
                        return true;
                    }
                });
        return true;
    }

    private void showHideComplete() {
        if (mApplication.getDFeatureLayer().getdLayerInfo().getDefinition().toLowerCase().equals("null")) {
            if (mApplication.getDFeatureLayer().getLayer().getDefinitionExpression().contains(Constant.DEFINITION_HIDE_COMPLETE)) {
                mApplication.getDFeatureLayer().getLayer().setDefinitionExpression(null);
            } else {
                mApplication.getDFeatureLayer().getLayer().setDefinitionExpression(mApplication.getDFeatureLayer().getdLayerInfo().getDefinition()
                        .concat(" and ").concat(Constant.DEFINITION_HIDE_COMPLETE));
            }
        } else {
            if (mApplication.getDFeatureLayer().getLayer().getDefinitionExpression().contains(Constant.DEFINITION_HIDE_COMPLETE)) {
                mApplication.getDFeatureLayer().getLayer().setDefinitionExpression(mApplication.getDFeatureLayer().getdLayerInfo().getDefinition());
            } else {
                mApplication.getDFeatureLayer().getLayer().setDefinitionExpression(mApplication.getDFeatureLayer().getdLayerInfo().getDefinition()
                        .concat(Constant.DEFINITION_HIDE_COMPLETE));
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handleFromFeatures clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_search:
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        switch (item.getItemId()) {
            case R.id.nav_thongke:
                Intent intent = new Intent(this, ListTaskActivity.class);
                this.startActivityForResult(intent, Constant.RequestCode.LIST_TASK);
                break;
//            case R.id.nav_tracuu:
//                intent = new Intent(this, TraCuuActivity.class);
//                this.startActivityForResult(intent, 1);
//                break;
            case R.id.nav_change_password:
                Intent intentChangePassword = new Intent(this, DoiMatKhauActivity.class);
                startActivity(intentChangePassword);
                break;
            case R.id.nav_reload:
                if (CheckConnectInternet.isOnline(this))
                    startMain();
                break;
            case R.id.nav_reload_layer:
                if (CheckConnectInternet.isOnline(this)) {
                    if (mPopUp != null && mPopUp.getCallout() != null && mPopUp.getCallout().isShowing())
                        mPopUp.getCallout().dismiss();
                    mFeatureLayer.loadAsync();
                    if (mApplication.getDFeatureLayer().getdLayerInfo().getDefinition().toLowerCase().equals("null")) {
                        mFeatureLayer.setDefinitionExpression(Constant.DEFINITION_HIDE_COMPLETE);
                    } else
                        mFeatureLayer.setDefinitionExpression(mApplication.getDFeatureLayer().getdLayerInfo().getDefinition().concat(" and ").concat(Constant.DEFINITION_HIDE_COMPLETE));
                }
                break;
            case R.id.nav_show_hide_complete:
                showHideComplete();
                break;
            case R.id.nav_logOut:
                startSignIn();
                break;
            case R.id.nav_delete_searching:
                deleteSearching();
                break;
            case R.id.nav_visible_float_button:
                visibleFloatActionButton();
                break;
            default:
                break;
        }


        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void onClickTextView(View v) {
        switch (v.getId()) {
            case R.id.txt_quanlysuco_hanhchinh:

                if (mLayoutDisplayLayerAdministration.getVisibility() == View.VISIBLE) {
                    mSeekBarAdministrator.setVisibility(View.GONE);
                    mLayoutDisplayLayerAdministration.setVisibility(View.GONE);
                } else {
                    mSeekBarAdministrator.setVisibility(View.VISIBLE);
                    mLayoutDisplayLayerAdministration.setVisibility(View.VISIBLE);
                }
                break;
            case R.id.txt_quanlysuco_dulieu:
                if (mLayoutDisplayLayerThematic.getVisibility() == View.VISIBLE) {
                    mLayoutDisplayLayerThematic.setVisibility(View.GONE);
                    mSeekBarThematic.setVisibility(View.GONE);
                } else {
                    mLayoutDisplayLayerThematic.setVisibility(View.VISIBLE);
                    mSeekBarThematic.setVisibility(View.VISIBLE);
                }
                break;
        }
    }

    public void onClickCheckBox(View v) {
        if (v instanceof CheckBox) {
            CheckBox checkBox = (CheckBox) v;
            switch (v.getId()) {
                case R.id.ckb_quanlysuco_hanhchinh:

                    for (int i = 0; i < mLayoutDisplayLayerAdministration.getChildCount(); i++) {
                        View view = mLayoutDisplayLayerAdministration.getChildAt(i);
                        if (view instanceof LinearLayout) {
                            LinearLayout layoutFeature = (LinearLayout) view;
                            for (int j = 0; j < layoutFeature.getChildCount(); j++) {
                                View view1 = layoutFeature.getChildAt(j);
                                if (view1 instanceof LinearLayout) {
                                    LinearLayout layoutCheckBox = (LinearLayout) view1;
                                    for (int k = 0; k < layoutCheckBox.getChildCount(); k++) {
                                        View view2 = layoutCheckBox.getChildAt(k);
                                        if (view2 instanceof CheckBox) {
                                            CheckBox checkBoxK = (CheckBox) view2;
                                            if (checkBox.isChecked())
                                                checkBoxK.setChecked(true);
                                            else checkBoxK.setChecked(false);
                                        }
                                    }
                                }
                            }
                        }
                    }
                    break;
                case R.id.ckb_quanlysuco_dulieu:
                    for (int i = 0; i < mLayoutDisplayLayerThematic.getChildCount(); i++) {
                        View view = mLayoutDisplayLayerThematic.getChildAt(i);
                        if (view instanceof LinearLayout) {
                            LinearLayout layoutFeature = (LinearLayout) view;
                            for (int j = 0; j < layoutFeature.getChildCount(); j++) {
                                View view1 = layoutFeature.getChildAt(j);
                                if (view1 instanceof LinearLayout) {
                                    LinearLayout layoutCheckBox = (LinearLayout) view1;
                                    for (int k = 0; k < layoutCheckBox.getChildCount(); k++) {
                                        View view2 = layoutCheckBox.getChildAt(k);
                                        if (view2 instanceof CheckBox) {
                                            CheckBox checkBoxK = (CheckBox) view2;
                                            if (checkBox.isChecked())
                                                checkBoxK.setChecked(true);
                                            else checkBoxK.setChecked(false);
                                        }
                                    }
                                }
                            }
                        }
                    }
                    break;
            }
        }
    }

    private void handlingLocation() {
        if (mIsFirstLocating) {
            mIsFirstLocating = false;
            mLocationDisplay.stop();
            enableLocation();
        } else {
            if (mLocationDisplay.isStarted()) {
                disableLocation();
            } else if (!mLocationDisplay.isStarted()) {
                enableLocation();
            }
        }
    }

    private void disableLocation() {
        mLocationDisplay.stop();
    }

    private void enableLocation() {
        mLocationDisplay.setAutoPanMode(LocationDisplay.AutoPanMode.RECENTER);
        mLocationDisplay.startAsync();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.layout_tim_su_co:
                optionSearchFeature();
                break;
            case R.id.layout_tim_dia_chi:
                optionFindRoute();
                break;
            case R.id.floatBtnLayer:
                v.setVisibility(View.INVISIBLE);
                findViewById(R.id.layout_layer).setVisibility(View.VISIBLE);
//                mCurrentPoint = mMapView.getCurrentViewpoint(Viewpoint.Type.CENTER_AND_SCALE).getTargetGeometry().getExtent().getCenter();
                break;
            case R.id.layout_layer_open_street_map:
                mMapView.getMap().setMaxScale(1128.497175);
                mMapView.getMap().setBasemap(Basemap.createOpenStreetMap());
                handlingColorBackgroundLayerSelected(R.id.layout_layer_open_street_map);
                mMapView.getCurrentViewpoint(Viewpoint.Type.CENTER_AND_SCALE);

                break;
            case R.id.layout_layer_street_map:
                mMapView.getMap().setMaxScale(1128.497176);
                mMapView.getMap().setBasemap(Basemap.createStreets());
                handlingColorBackgroundLayerSelected(R.id.layout_layer_street_map);

                break;
            case R.id.layout_layer_image:
                mMapView.getMap().setMaxScale(getResources().getInteger(R.integer.MAX_SCALE_IMAGE_WITH_LABLES));
                mMapView.getMap().setBasemap(Basemap.createImageryWithLabels());
                handlingColorBackgroundLayerSelected(R.id.layout_layer_image);

                break;
            case R.id.btn_layer_close:
                findViewById(R.id.layout_layer).setVisibility(View.INVISIBLE);
                findViewById(R.id.floatBtnLayer).setVisibility(View.VISIBLE);
                break;
            case R.id.img_chonvitri_themdiemsuco:
//                themDiemSuCo();
                break;
            case R.id.btn_add_feature_close:
                if (mMapViewHandler != null) {
                    findViewById(R.id.linear_addfeature).setVisibility(View.GONE);
                    findViewById(R.id.img_map_pin).setVisibility(View.GONE);
                    mMapViewHandler.setClickBtnAdd(false);
                }
                break;
            case R.id.floatBtnLocation:
                handlingLocation();
                break;
            case R.id.imgBtn_timkiemdiachi_themdiemsuco:
//                themDiemSuCo();
                break;

        }
    }

    @Nullable
    private Bitmap getBitmap(String path) {

        Uri uri = Uri.fromFile(new File(path));
        InputStream in;
        try {
            final int IMAGE_MAX_SIZE = 1200000; // 1.2MP
            in = getContentResolver().openInputStream(uri);

            // Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(in, null, o);
            assert in != null;
            in.close();


            int scale = 1;
            while ((o.outWidth * o.outHeight) * (1 / Math.pow(scale, 2)) > IMAGE_MAX_SIZE) {
                scale++;
            }
            Log.d("", "scale = " + scale + ", orig-width: " + o.outWidth + ", orig-height: " + o.outHeight);

            Bitmap b;
            in = getContentResolver().openInputStream(uri);
            if (scale > 1) {
                scale--;
                // scale to max possible inSampleSize that still yields an image
                // larger than target
                o = new BitmapFactory.Options();
                o.inSampleSize = scale;
                b = BitmapFactory.decodeStream(in, null, o);

                // resize to desired dimensions
                int height = b.getHeight();
                int width = b.getWidth();
                Log.d("", "1th scale operation dimenions - width: " + width + ", height: " + height);

                double y = Math.sqrt(IMAGE_MAX_SIZE / (((double) width) / height));
                double x = (y / height) * width;

                Bitmap scaledBitmap = Bitmap.createScaledBitmap(b, (int) x, (int) y, true);
                b.recycle();
                b = scaledBitmap;

                System.gc();
            } else {
                b = BitmapFactory.decodeStream(in);
            }
            assert in != null;
            in.close();

            Log.d("", "bitmap size - width: " + b.getWidth() + ", height: " + b.getHeight());
            return b;
        } catch (IOException e) {
            Log.e("", e.getMessage(), e);
            return null;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void handlingListTaskActivityResult() {
        //query sự cố theo idsuco, lấy objectid
        String selectedIDSuCo = mApplication.getDiemSuCo().getIdSuCo();
        mMapViewHandler.query(String.format("%s = '%s'", Constant.FieldSuCo.ID_SUCO, selectedIDSuCo));
    }

    @SuppressLint("ResourceAsColor")
    private void handlingColorBackgroundLayerSelected(int id) {
        switch (id) {
            case R.id.layout_layer_open_street_map:
                mImageOpenStreetMap.setBackgroundResource(R.drawable.layout_shape_basemap);
                mTxtOpenStreetMap.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));
                mImageStreetMap.setBackgroundResource(R.drawable.layout_shape_basemap_none);
                mTxtStreetMap.setTextColor(ContextCompat.getColor(this, R.color.colorTextColor_1));
                mImageImageWithLabel.setBackgroundResource(R.drawable.layout_shape_basemap_none);
                mTxtImageWithLabel.setTextColor(ContextCompat.getColor(this, R.color.colorTextColor_1));
                break;
            case R.id.layout_layer_street_map:
                mImageStreetMap.setBackgroundResource(R.drawable.layout_shape_basemap);
                mTxtStreetMap.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));
                mImageOpenStreetMap.setBackgroundResource(R.drawable.layout_shape_basemap_none);
                mTxtOpenStreetMap.setTextColor(ContextCompat.getColor(this, R.color.colorTextColor_1));
                mImageImageWithLabel.setBackgroundResource(R.drawable.layout_shape_basemap_none);
                mTxtImageWithLabel.setTextColor(ContextCompat.getColor(this, R.color.colorTextColor_1));
                break;
            case R.id.layout_layer_image:
                mImageImageWithLabel.setBackgroundResource(R.drawable.layout_shape_basemap);
                mTxtImageWithLabel.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));
                mImageOpenStreetMap.setBackgroundResource(R.drawable.layout_shape_basemap_none);
                mTxtOpenStreetMap.setTextColor(ContextCompat.getColor(this, R.color.colorTextColor_1));
                mImageStreetMap.setBackgroundResource(R.drawable.layout_shape_basemap_none);
                mTxtStreetMap.setTextColor(ContextCompat.getColor(this, R.color.colorTextColor_1));
                break;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            final int objectid = data.getIntExtra(getString(R.string.ket_qua_objectid), 1);
            switch (requestCode) {
                case 1:
                    if (resultCode == Activity.RESULT_OK && mMapViewHandler != null) {
                        mMapViewHandler.query(String.format(Constant.QUERY_BY_OBJECTID, objectid));
                    }
                    break;
                case Constant.RequestCode.LOGIN:
                    if (resultCode == RESULT_OK)
                        requestPermisson();
                    break;
                case Constant.RequestCode.LIST_TASK:
                    if (resultCode == Activity.RESULT_OK)
                        handlingListTaskActivityResult();
                    break;
                case Constant.RequestCode.ADD:
                    if (resultCode == RESULT_OK) {
                        handlingAddFeatureSuccess();
                    } else {
                        handlingCancelAdd();
                    }
                    break;
                case Constant.RequestCode.UPDATE:
                    mPopUp.refreshPopup(mApplication.getSelectedArcGISFeature());
                    break;
                default:
                    break;
            }
        } catch (Exception ignored) {
        }
    }
}