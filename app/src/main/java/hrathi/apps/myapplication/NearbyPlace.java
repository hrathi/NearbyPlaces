package hrathi.apps.myapplication;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Concrete object to represent each place. This can be extended to
 * record whatever is needed
 *
 * Created by Harish on 11/16/15.
 */
public class NearbyPlace {
    private String name;
    private String photoReference;

    public String getName() {
        return name;
    }

    public String getPhotoReference() {
        return photoReference;
    }

    // create an instance from json object.
    public static NearbyPlace fromJson(JSONObject jsonObject) {
        NearbyPlace np = new NearbyPlace();
        try {
            np.name = jsonObject.getString("name");
            if (jsonObject.has("photos")) {
                JSONArray photos = jsonObject.getJSONArray("photos");
                JSONObject htmlAttributions = photos.getJSONObject(0);
                np.photoReference = htmlAttributions.getString("photo_reference");
            } else {
                np = null;
            }
        } catch (JSONException je) {
            je.printStackTrace();
            return null;
        }

        return np;
    }
}
