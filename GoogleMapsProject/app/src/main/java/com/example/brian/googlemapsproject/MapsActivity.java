package com.example.brian.googlemapsproject;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationListener;

import java.util.List;

public class MapsActivity extends AppCompatActivity
        implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    GoogleMap mMap;
    SupportMapFragment mapFrag;
    GoogleApiClient mGoogleApiClient;
    EditText editSearch;
    MarkerOptions markerOptions;
    EditText locationTv;
    int PLACE_PICKER_REQUEST = 1020;
    int numTrack = 1399;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        getSupportActionBar().setTitle("Google Maps Project");

        //placeMarkers = new Marker[MAX_PLACES];

        mapFrag = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFrag.getMapAsync(this);

        editSearch = (EditText)(findViewById(R.id.editSearch));
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.streetbutton:
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                break;
            case R.id.satellitebutton:
                mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                break;
            case R.id.hybridbutton:
                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                break;
            case R.id.terrainbutton:
                mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                break;
            case R.id.Tracker:
                numTrack++;
                if(numTrack%2==0) {
                    Log.d("MyMaps", "Tracking On");
                    Toast.makeText(MapsActivity.this, "Tracking On", Toast.LENGTH_SHORT).show();
                    getLocation();
                }
                else{
                    Log.d("MyMaps", "Tracking Off");
                    Toast.makeText(MapsActivity.this, "Tracking Off", Toast.LENGTH_SHORT).show();
                    locationManager.removeUpdates(locationListenerNetwork);
                }
                break;
            case R.id.Clear:
                mMap.clear();
                break;
            case R.id.Search:
                String search = editSearch.getText().toString();
                Geocoder geocoder = new Geocoder(getBaseContext());
                List<Address> addresses = null;

                try {
                    addresses = geocoder.getFromLocationName(search, 3);
                    if (addresses != null && !addresses.equals(""))
                    search(addresses);
                }
                catch (Exception e) {
                    Log.d("myMaps", "Search function failed");
                }
        }
    }

    protected void search(List<Address> addresses) {
        Address address = addresses.get(0);
        LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());

        String addressText = String.format(
             "%s, %s",
             address.getMaxAddressLineIndex() > 0 ? address
             .getAddressLine(0) : "", address.getCountryName());

        markerOptions = new MarkerOptions();

        markerOptions.position(latLng);
        markerOptions.title(addressText);

        mMap.addMarker(markerOptions);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(18));
        locationTv.setText("Latitude:" + address.getLatitude() + ", Longitude:" +
                            address.getLongitude());
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(this, data);
                String toastMsg = String.format("Place: %s", place.getName());
                Toast.makeText(this, toastMsg, Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        LatLng birthcity = new LatLng(40.8682, -73.4257);
        mMap.addMarker(new MarkerOptions().position(birthcity).title("Born here"));

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);
            } else {
                //checkLocationPermission();
            }
        } else {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }
    }

    public void openPlacePicker(View v) {
        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
        try {
            startActivityForResult(builder.build(this), PLACE_PICKER_REQUEST);
        } catch (GooglePlayServicesRepairableException e) {
            e.printStackTrace();
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
    }

    /*public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                new AlertDialog.Builder(this)
                        .setTitle("Location Permission Needed")
                        .setMessage("This app needs the Location permission, please accept to use location functionality")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityCompat.requestPermissions(MapsActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }

                } else {
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }*/

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onLocationChanged(Location location) {
    }

    @Override
    public void onConnected(Bundle bundle) {
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }

    private LocationManager locationManager;
    private boolean isGPSenabled = false;
    private boolean isNetworkEnabled = false;
    private boolean canGetLocation = false;
    Location mLastLocation;
    private static final long MIN_TIME_BW_UPDATES = 1000 * 15 * 1;
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 5;

    public void getLocation() {
        try {
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

            isGPSenabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if (isGPSenabled) Log.d("MyMaps", "getLocation: GPS is enabled");

            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if (isNetworkEnabled) Log.d("MyMaps", "getLocation: Network is enabled");

            if (!isGPSenabled && !isNetworkEnabled) {
                Log.d("MyMaps", "getLocation: No provider is enabled");
            } else {
                this.canGetLocation = true;

                if (isGPSenabled) {
                    Log.d("MyMaps", "getLocation: GPS enabled, requesting location updates");
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES,
                            locationListenerNetwork);

                    Log.d("MyMaps", "getLocation: GPS update request succcessful");
                    Toast.makeText(MapsActivity.this, "Using Network", Toast.LENGTH_SHORT).show();
                }

                if (isNetworkEnabled) {
                    Log.d("MyMaps", "getLocation: Network enabled, requesting location updates");
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES,
                            locationListenerNetwork);

                    Log.d("MyMaps", "getLocation: Network update request succcessful");
                    Toast.makeText(MapsActivity.this, "Using Network", Toast.LENGTH_SHORT).show();
                }
            }
        }
        catch (Exception e) {
            Log.d("MyMaps", "caught Exception in getLocation");
            e.printStackTrace();
        }
    }

    private Location myLocation;
    private static final int MY_LOC_ZOOM_FACTOR = 18;


    android.location.LocationListener locationListenerGPS = new android.location.LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            Log.d("MyMaps", "GPS Enabled");
            Toast.makeText(MapsActivity.this, "GPS Enabled", Toast.LENGTH_SHORT).show();

            dropMarker(LocationManager.GPS_PROVIDER);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.d("MyMaps", "GPS Enabled");
            Toast.makeText(MapsActivity.this, "GPS Enabled", Toast.LENGTH_SHORT).show();

            switch (status) {
                case LocationProvider.AVAILABLE:
                    Log.d("MyMaps", "Location Provider Available");
                    Toast.makeText(MapsActivity.this, "Location Provider Available", Toast.LENGTH_SHORT).show();
                    break;
                case LocationProvider.OUT_OF_SERVICE:
                    if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerGPS);
                    break;
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerGPS);
                    break;
                default:
                    if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerGPS);
                    break;
            }
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }
    };

    private void dropMarker(String provider) {
        LatLng userLocation = null;

        if (locationManager != null) {
            if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            myLocation = locationManager.getLastKnownLocation(provider);
        }

        if (myLocation == null) {
            Log.d("MyMaps", "dropMarker: myLocation is null");

        } else {
            userLocation = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());

            Toast.makeText(MapsActivity.this, "" + myLocation.getLatitude() + ", " +myLocation.getLongitude(), Toast.LENGTH_SHORT).show();

            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(userLocation, MY_LOC_ZOOM_FACTOR);

            if (provider == LocationManager.GPS_PROVIDER){
                Circle myCircle = mMap.addCircle(new CircleOptions().center(userLocation).radius(1).strokeColor(Color.BLUE).strokeWidth(2).fillColor(Color.RED));
            }

            if (provider == LocationManager.NETWORK_PROVIDER){
                Circle myCircle = mMap.addCircle(new CircleOptions().center(userLocation).radius(1).strokeColor(Color.RED).strokeWidth(2).fillColor(Color.BLUE));
            }
            mMap.animateCamera(update);
        }
    }

    android.location.LocationListener locationListenerNetwork = new android.location.LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            Log.d("MyMaps", "Network Enabled");
            Toast.makeText(MapsActivity.this,"Network Enabled", Toast.LENGTH_SHORT).show();

            dropMarker(LocationManager.NETWORK_PROVIDER);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.d("MyMaps", "Network Enabled");
            Toast.makeText(MapsActivity.this,"Network Enabled", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }
    };

    /*private Marker[] placeMarkers;
    private final int MAX_PLACES = 20;
    private MarkerOptions[] places;

    public void onPostExecute(String result) {
        if(placeMarkers!=null){
            for(int i=0; i<placeMarkers.length; i++){
                if(placeMarkers[i]!=null)
                    placeMarkers[i].remove();
            }
        }

        try {
            JSONObject resultObject = new JSONObject(result);
            JSONArray placesArray = resultObject.getJSONArray("results");
            places = new MarkerOptions[placesArray.length()];
            for (int p=0; p<placesArray.length(); p++) {
                boolean missingValue = false;
                LatLng placeLL=null;
                String placeName="";
                String vicinity="";
                int currIcon = otherIcon;
                try{
                    missingValue = false;
                    JSONObject placeObject = placesArray.getJSONObject(p);
                    JSONObject loc = placeObject.getJSONObject("geometry").getJSONObject("location");
                    placeLL = new LatLng(
                            Double.valueOf(loc.getString("lat")),
                            Double.valueOf(loc.getString("lng")));
                    JSONArray types = placeObject.getJSONArray("types");
                    for(int t=0; t<types.length(); t++){
                        String thisType=types.get(t).toString();
                        if(thisType.contains("food")){
                            currIcon = foodIcon;
                            break;
                        }
                        else if(thisType.contains("bar")){
                            currIcon = drinkIcon;
                            break;
                        }
                        else if(thisType.contains("store")){
                            currIcon = shopIcon;
                            break;
                        }
                    }
                    vicinity = placeObject.getString("vicinity");
                    placeName = placeObject.getString("name");
                }
                catch(JSONException jse){
                    missingValue=true;
                    if(missingValue)    places[p]=null;
                    else
                        places[p]=new MarkerOptions()
                                .position(placeLL)
                                .title(placeName)
                                .icon(BitmapDescriptorFactory.fromResource(currIcon))
                                .snippet(vicinity);
                    jse.printStackTrace();
                }
            }
        }
        catch (Exception e) {
            missingValue = true;
            e.printStackTrace();
        }
        if(places!=null && placeMarkers!=null){
            for(int p=0; p<places.length && p<placeMarkers.length; p++){
                if(places[p]!=null)
                    placeMarkers[p]=mMap.addMarker(places[p]);
            }
        }
        new GetPlaces().execute(placesSearchStr);
    }*/
}
