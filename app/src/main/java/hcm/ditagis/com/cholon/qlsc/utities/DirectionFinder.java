package hcm.ditagis.com.cholon.qlsc.utities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.text.Html;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import hcm.ditagis.com.cholon.qlsc.R;

public class DirectionFinder {
    //    private static final String DIRECTION_URL_API = "https://maps.googleapis.com/maps/api/directions/json?";
//    private static final String GOOGLE_API_KEY = "AIzaSyDnwLF2-WfK8cVZt9OoDYJ9Y8kspXhEHfI";
    private DirectionFinderListener listener;
    private String origin;
    private String destination;
    private Context mContext;

    public DirectionFinder(Context context, DirectionFinderListener listener, String origin, String destination) {
        this.listener = listener;
        this.origin = origin;
        this.mContext = context;
        this.destination = destination;
    }

    public void execute() throws UnsupportedEncodingException {
        listener.onDirectionFinderStart();
        new DownloadRawData().execute(createUrl());
    }

    private String createUrl() throws UnsupportedEncodingException {
        String urlOrigin = URLEncoder.encode(origin, "utf-8");
        String urlDestination = URLEncoder.encode(destination, "utf-8");

        return mContext.getString(R.string.direction_url_api) + "origin=" + urlOrigin +
                "&destination=" + urlDestination + "&key=" + mContext.getString(R.string.google_api) + "&language=vi";
    }

    @SuppressLint("StaticFieldLeak")
    private class DownloadRawData extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String link = params[0];
            try {
                URL url = new URL(link);
                InputStream is = url.openConnection().getInputStream();
                StringBuilder builder = new StringBuilder();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));

                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line).append("\n");
                }

                return builder.toString();

            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String res) {
            try {
                parseJSon(res);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void parseJSon(String data) throws JSONException {
        if (data == null)
            return;

        List<Route> routes = new ArrayList<>();
        JSONObject jsonData = new JSONObject(data);
        JSONArray jsonRoutes = jsonData.getJSONArray("routes");
        for (int i = 0; i < jsonRoutes.length(); i++) {
            JSONObject jsonRoute = jsonRoutes.getJSONObject(i);
            Route route = new Route();

            JSONObject overview_polylineJson = jsonRoute.getJSONObject("overview_polyline");
            JSONArray jsonLegs = jsonRoute.getJSONArray("legs");
            JSONObject jsonLeg = jsonLegs.getJSONObject(0);
            JSONObject jsonDistance = jsonLeg.getJSONObject("distance");
            JSONObject jsonDuration = jsonLeg.getJSONObject("duration");
            JSONObject jsonEndLocation = jsonLeg.getJSONObject("end_location");
            JSONObject jsonStartLocation = jsonLeg.getJSONObject("start_location");

            JSONArray jsonSteps = jsonLeg.getJSONArray("steps");

            route.distance = new Distance(jsonDistance.getString("text"), jsonDistance.getInt("value"));
            route.duration = new Duration(jsonDuration.getString("text"), jsonDuration.getInt("value"));
            route.endAddress = jsonLeg.getString("end_address");
            route.startAddress = jsonLeg.getString("start_address");
            route.startLocation = new LatLng(jsonStartLocation.getDouble("lat"), jsonStartLocation.getDouble("lng"));
            route.endLocation = new LatLng(jsonEndLocation.getDouble("lat"), jsonEndLocation.getDouble("lng"));
            route.points = decodePolyLine(overview_polylineJson.getString("points"));

            List<Step> steps = new ArrayList<>();
            for (int j = 0; j < jsonSteps.length(); j++) {
                JSONObject jsonStep = jsonSteps.getJSONObject(j);

                JSONObject jsonStepDistance = jsonStep.getJSONObject("distance");
                JSONObject jsonStepDuration = jsonStep.getJSONObject("duration");
                JSONObject jsonStepEndLocation = jsonStep.getJSONObject("end_location");
                JSONObject jsonStepStartLocation = jsonStep.getJSONObject("start_location");

                Step step = new Step();
                step.distance = new Distance(jsonStepDistance.getString("text"), jsonStepDistance.getInt("value"));
                step.duration = new Duration(jsonStepDuration.getString("text"), jsonStepDuration.getInt("value"));
                step.startLocation = new LatLng(jsonStepStartLocation.getDouble("lat"), jsonStepStartLocation.getDouble("lng"));
                step.endLocation = new LatLng(jsonStepEndLocation.getDouble("lat"), jsonStepEndLocation.getDouble("lng"));
                String[] instructions = translateEnglishToVietnamese(stripHtml(jsonStep.getString("html_instructions"))).split("\n\n");
                step.html_instructions = instructions[0];
                if (instructions.length == 2)
                    step.html_sub_instructions = instructions[1];
                step.travel_mode = jsonStep.getString("travel_mode");

                steps.add(step);
            }
            route.steps = steps;
            routes.add(route);

        }

        listener.onDirectionFinderSuccess(routes);
    }

    private String stripHtml(String html) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            return Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY).toString();
        } else {
            return "";
//            return Html.fromHtml(html).toString();
        }
    }

    private String translateEnglishToVietnamese(String eng) {

        return String.valueOf(
                eng
                        .replace("Head", "Đi về")
                        .replace("Pass by", "Băng qua")
                        .replace("Continue", "Tiếp tục")
                        .replace("east", "hướng Đông")
                        .replace("west", "hướng Tây")
                        .replace("straight", "đi thẳng")
                        .replace("Turn left", "Rẽ trái")
                        .replace("Turn right", "Rẽ phải")
                        .replace("pass", "qua")
                        .replace("on the right", "ở bên phải")
                        .replace("on the left", "ở bên trái")
                        .replace("onto", "vào")
                        .replace("on", "trên")
                        .replace("at", "tại")
                        .replace("toward", "về phía"));
    }

    private List<LatLng> decodePolyLine(final String poly) {
        int len = poly.length();
        int index = 0;
        List<LatLng> decoded = new ArrayList<>();
        int lat = 0;
        int lng = 0;

        while (index < len) {
            int b;
            int shift = 0;
            int result = 0;
            do {
                b = poly.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = poly.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            decoded.add(new LatLng(
                    lat / 100000d, lng / 100000d
            ));
        }

        return decoded;
    }
}
