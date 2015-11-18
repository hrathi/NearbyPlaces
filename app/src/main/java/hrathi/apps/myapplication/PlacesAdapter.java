package hrathi.apps.myapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.media.Image;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Harish on 11/16/15.
 */
public class PlacesAdapter extends RecyclerView.Adapter<PlacesAdapter.ViewHolder> {

    // limit search results with which radius distance from current location
    private int mRadius;

    private Context mContext;

    // a library to fetch json and images from google web services.
    private RequestQueue mRequestQueue;

    // places metadata returned from web service. This data is used to display the list
    private List<NearbyPlace> mNearbyPlaces;

    public PlacesAdapter(Context context) {
        mContext = context;
        mRadius = 1000; // choose higher values for remote locations
        mNearbyPlaces = new ArrayList<>();

        // a singleton request queue for all network I/O requests
        mRequestQueue = Volley.newRequestQueue(mContext);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View placeItemView = inflater.inflate(R.layout.places_item, parent, false);

        // Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(placeItemView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // user has seen end of the list, so fetch more by increasing the radius
        if (position == mNearbyPlaces.size() - 1) {
            fetchNearbyPlaces(mRadius += 500);
        }

        // get the data at this position
        NearbyPlace np = mNearbyPlaces.get(position);

        // populate the place name
        holder.placeTextView.setText(np.getName());

        // go fetch the image and set it when downloaded
        loadImage(holder.placeImageView, np.getPhotoReference());
    }

    @Override
    public int getItemCount() {
        return mNearbyPlaces.size();
    }

    /**
     * add a place at the end of the list. Ideally we should look for dups
     *
     * @param nearbyPlace
     */
    public void addPlace(NearbyPlace nearbyPlace) {
        mNearbyPlaces.add(nearbyPlace);
    }

    /**
     * Use view holder to reduce inflating resources
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView placeTextView;
        ImageView placeImageView;

        public ViewHolder(View itemView) {
            super(itemView);

            placeTextView = (TextView) itemView.findViewById(R.id.placeName);
            placeImageView = (ImageView) itemView.findViewById(R.id.placeIcon);
        }
    }

    void fetchNearbyPlaces() {
        fetchNearbyPlaces(mRadius);
    }

    /**
     * give a radius to search within, this method calls google places web service api
     * to find all the places within the boundry. Currently, we are restricting to
     * places that have images (others are simply discared).
     * @param radius
     */
    void fetchNearbyPlaces(int radius) {
        // get the current location
        Location location = getMostRecentLocation();

        String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location="
                + location.getLatitude() + ","
                + location.getLongitude() + "&"
                + "radius=" + radius + "&key=AIzaSyAoHGV0v0ZnAd6nWCPQkcT5ad--_KCpOYU";


        // cannot use Google Places API for Android key: AIzaSyCfmWsaHA1JaCgO-dMSu4nvLciuL07beDI"

        // the request is automatically happening on a worker thread.
        JsonObjectRequest jreq = new JsonObjectRequest(url,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray array = response.getJSONArray("results");

                            for (int i = 0; i < array.length(); i++) {

                                JSONObject jo = array.getJSONObject(i);

                                NearbyPlace place = NearbyPlace.fromJson(jo);
                                if (place != null) {
                                    addPlace(place);
                                }
                                notifyDataSetChanged();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("ERROR",
                                "Error occurred while fetching places: " + error.getMessage());
                    }
                }
        );

        mRequestQueue.add(jreq);
    }

    public Location getMostRecentLocation() {
        LocationManager locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        String locationProvider = LocationManager.NETWORK_PROVIDER;
        return locationManager.getLastKnownLocation(locationProvider);
    }

    /**
     * Load images once the user is trying to view that item. We could potentially
     * prefetch here so user doesn't see any jitter
     * @param iv
     * @param photoReference
     */
    void loadImage(final ImageView iv, final String photoReference) {
        // if api didn't give any references to a picture, use a stock image
        if (photoReference == null) {
            iv.setImageResource(R.drawable.unknown_icon);
            iv.setMaxHeight(300);
        }

        String url = "https://maps.googleapis.com/maps/api/place/photo?maxheight=400&"
                        + "photoreference=" + photoReference
                        + "&key=AIzaSyAoHGV0v0ZnAd6nWCPQkcT5ad--_KCpOYU";

        // fetch the image
        ImageRequest request = new ImageRequest(url, new Response.Listener<Bitmap>() {
                @Override
                public void onResponse(Bitmap response) {
                    iv.setImageBitmap(response);
                }
            }, 0, 0, ImageView.ScaleType.CENTER, null, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                }
            }
        );

        mRequestQueue.add(request);
    }
}
