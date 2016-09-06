package team8.codepath.sightseeingapp.activities;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Places;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import team8.codepath.sightseeingapp.R;
import team8.codepath.sightseeingapp.adapters.SearchRecyclerAdapter;
import team8.codepath.sightseeingapp.models.TripModel;

public class SearchActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {
    RecyclerView rvSearchTrips;
    private static DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("trips");
    private DatabaseReference geoDatabase = FirebaseDatabase.getInstance().getReference("geofire");
    protected GoogleApiClient mGoogleApiClient;
    ArrayList<TripModel> trips = new ArrayList<TripModel>();
    SearchRecyclerAdapter adapter;
    Integer totalLength;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, 0, this)
                .addApi(Places.GEO_DATA_API)
                .build();

        adapter = new SearchRecyclerAdapter(getApplicationContext(), trips, mGoogleApiClient);
        rvSearchTrips = (RecyclerView) findViewById(R.id.rvSearchTrips);
        rvSearchTrips.setAdapter(adapter);
        rvSearchTrips.setLayoutManager(new LinearLayoutManager(this));



        Double latitude = getIntent().getDoubleExtra("latitude", 00);
        Log.d("latitude in search", latitude.toString());
        Double longitude = getIntent().getDoubleExtra("longitude", 00);
        Integer distance = getIntent().getIntExtra("distance", 00);

        findNearbyPlaces( latitude, longitude, distance);
    }

    private void findNearbyPlaces(Double lat, Double longit, Integer distance){
        GeoFire geoFire = new GeoFire(geoDatabase);
        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(lat, longit), distance);
        final ArrayList<String> placeKeys = new ArrayList<>();
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                System.out.println(String.format("Key %s entered the search area at [%f,%f]", key, location.latitude, location.longitude));
                placeKeys.add(key);
                Log.d("Found result", key);
            }

            @Override
            public void onKeyExited(String key) {
                System.out.println(String.format("Key %s is no longer in the search area", key));
            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {
                System.out.println(String.format("Key %s moved within the search area to [%f,%f]", key, location.latitude, location.longitude));
            }

            @Override
            public void onGeoQueryReady() {
                System.out.println("All initial data has been loaded and events have been fired!");
                findMatchingTrips(placeKeys);
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {
                System.err.println("There was an error with this query: " + error);
            }
        });



    }

    public void findMatchingTrips(ArrayList<String> placeKeys){
        Query queryRef;

        for(int i=0; i<placeKeys.size() - 1; i++){
            placeKeys.get(i);

            //Insert query here to find a trip with placeId that matches
            queryRef = mDatabase.orderByChild("placeId").equalTo(placeKeys.get(i)).limitToFirst(1);

            queryRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot messageSnapshot: dataSnapshot.getChildren()) {
                        TripModel trip = messageSnapshot.getValue(TripModel.class);
                        trips.add(trip);
                        adapter.notifyDataSetChanged();
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });


        }



    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
