package hcm.ditagis.com.cholon.qlsc.async;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;


import hcm.ditagis.com.cholon.qlsc.R;
import hcm.ditagis.com.cholon.qlsc.connectDB.ChangePasswordDB;
import hcm.ditagis.com.cholon.qlsc.entities.entitiesDB.User;
import hcm.ditagis.com.cholon.qlsc.utities.Utils;

public class ChangePasswordAsycn extends AsyncTask<String, Void, Integer> {
    private ProgressDialog mDialog;
    @SuppressLint("StaticFieldLeak")
    private Context mContext;
    private AsyncResponse mDelegate;

    public interface AsyncResponse {
        void processFinish(Integer output);
    }

    public ChangePasswordAsycn(Context context, AsyncResponse delegate) {
        this.mContext = context;
        this.mDelegate = delegate;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        this.mDialog = new ProgressDialog(this.mContext, android.R.style.Theme_Material_Dialog_Alert);
        this.mDialog.setMessage(mContext.getString(R.string.change_password_check_old_password_message));
        this.mDialog.setCancelable(false);
        this.mDialog.show();
    }

    @Override
    protected Integer doInBackground(String... params) {
        String danhBo = params[0];
        String pin = params[1];
        String newPin = params[2];
        try {
            ChangePasswordDB changePasswordDB = new ChangePasswordDB(mContext);
            User user = changePasswordDB.find(danhBo, pin);
            if (user != null) {
                publishProgress();
                User user1 = changePasswordDB.change(danhBo, newPin);
                if (user1 != null) {
                    return Utils.CHANGE_PASSWORD_SUCCESS;
                } else return Utils.CHANGE_PASSWORD_FAILURE;
            } else {
                return null;
            }
        } catch (Exception e) {
            Log.e("Lỗi đổi mật khẩu", e.toString());
        }
        return Utils.CHANGE_PASSWORD_FAILURE;
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
        this.mDialog.setMessage(mContext.getString(R.string.change_password_message));


    }

    @Override
    protected void onPostExecute(Integer value) {
//        if (khachHang != null) {
        mDialog.dismiss();
        this.mDelegate.processFinish(value);
//        }
    }
}
