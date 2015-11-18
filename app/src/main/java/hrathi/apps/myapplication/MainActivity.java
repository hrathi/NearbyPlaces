package hrathi.apps.myapplication;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

public class MainActivity extends AppCompatActivity {
    PlacesAdapter mPlacesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // setup recycler view with default layout manager
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.nearbyPlacesListView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // hookup the recycler view data/views from adapter
        mPlacesAdapter = new PlacesAdapter(this);
        recyclerView.setAdapter(mPlacesAdapter);

        // start fetching initial data to show in the list
        mPlacesAdapter.fetchNearbyPlaces();
    }


}
