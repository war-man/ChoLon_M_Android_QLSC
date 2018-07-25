package hcm.ditagis.com.cholon.qlsc.services;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;

import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.data.FeatureQueryResult;
import com.esri.arcgisruntime.data.QueryParameters;
import com.esri.arcgisruntime.data.ServiceFeatureTable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;

import hcm.ditagis.com.cholon.qlsc.R;
import hcm.ditagis.com.cholon.qlsc.entities.VatTu;
import hcm.ditagis.com.cholon.qlsc.entities.entitiesDB.LayerInfoDTG;
import hcm.ditagis.com.cholon.qlsc.entities.entitiesDB.ListObjectDB;

public class GetVatTuOngNganh  {
    private Context mContext;


    public GetVatTuOngNganh(Context context) {
        this.mContext = context;
    }



    public void getVatTuFromService() {

        String layerInfoVatTu = mContext.getString(R.string.LayerInfo_vatTuOngNganh);

        for (LayerInfoDTG layerInfoDTG : ListObjectDB.getInstance().getLstFeatureLayerDTG()) {
            if (layerInfoDTG.getId().equals(layerInfoVatTu)) {
                QueryParameters queryParameters = new QueryParameters();
                queryParameters.setWhereClause("1=1");
                String url = layerInfoDTG.getUrl();
                if (!url.startsWith("http"))
                    url = "http:" + layerInfoDTG.getUrl();
                ServiceFeatureTable serviceFeatureTable = new ServiceFeatureTable(url);
                final ListenableFuture<FeatureQueryResult> feature = serviceFeatureTable.queryFeaturesAsync(queryParameters, ServiceFeatureTable.QueryFeatureFields.LOAD_ALL);
                feature.addDoneListener(new Runnable() {
                    @Override
                    public void run() {
                        final List<VatTu> vatTuList = new ArrayList<>();
                        try {
                            FeatureQueryResult result = feature.get();
                            Iterator<Feature> iterator = result.iterator();
                            Feature item;
                            while (iterator.hasNext()) {
                                item = iterator.next();
                                String maVatTu = (String) item.getAttributes().get(mContext.getString(R.string.field_VatTu_maVatTu));
                                String tenVatTu = (String) item.getAttributes().get(mContext.getString(R.string.field_VatTu_tenVatTu));
                                String donViTinh = (String) item.getAttributes().get(mContext.getString(R.string.field_VatTu_donViTinh));
                                VatTu vatTu = new VatTu(maVatTu, tenVatTu, donViTinh);
                                vatTuList.add(vatTu);
                            }

                        } catch (InterruptedException | ExecutionException e) {
                            e.printStackTrace();
                        }
                        finally {
                            ListObjectDB.getInstance().setVatTuOngNganhs(vatTuList);
                        }


                    }
                });
                break;
            }
        }

    }


}
