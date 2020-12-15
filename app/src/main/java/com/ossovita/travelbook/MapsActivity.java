package com.ossovita.travelbook;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    private GoogleMap mMap;
    LocationManager lm;
    LocationListener ll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapLongClickListener(this);
        Intent i = getIntent();
        //Bu sayafaya nereden geldiğini öğreniyoruz
        String info = i.getStringExtra("infı");

        if(info.matches("new")){//yeni konum eklemeye girdiyse mevcut konumunun oraya zoomla
            lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            ll = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    SharedPreferences sp = MapsActivity.this.getSharedPreferences("com.ossovita.travelbook",MODE_PRIVATE);
                    boolean trackBoolean = sp.getBoolean("trackBoolean",false);

                    if(!trackBoolean){
                        LatLng userLocation = new LatLng(location.getLatitude(),location.getLongitude());
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation,15));
                        sp.edit().putBoolean("trackBoolean",true).apply();
                    }







                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                @Override
                public void onProviderEnabled(String provider) {

                }

                @Override
                public void onProviderDisabled(String provider) {

                }
            };
            //onMapReady'nin altındayız
            //izin verilmediyse izin iste
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this,new String[] {Manifest.permission.ACCESS_FINE_LOCATION},1);
            }else{//izin verildiyse
                lm.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,ll);
                //son bilinen  konum varsa
                //GPS provider'dan gelen son konumu al lastLocation'a ata
                Location lastLocation = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if(lastLocation!=null){
                    LatLng lastUserLocation = new LatLng(lastLocation.getLatitude(),lastLocation.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation,15));

                }
            }
        }

        else{
            //Sqlite'da kaydedilen veriyi gösterecek
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //Eğer gelen bir cevap varsa
        if(grantResults.length>0){
            if(requestCode==1){//Konum isteği 1 olana izin verildiyse
                if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED){
                    lm.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,ll);

                    Intent i = getIntent();
                    String info = i.getStringExtra("new");
                    if(info.matches("new")){//yeni konum eklemeye girdiyse mevcut konumunun oraya zoomla
                        Location lastLocation = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        if(lastLocation!=null){
                            LatLng lastUserLocation = new LatLng(lastLocation.getLatitude(),lastLocation.getLongitude());
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation,15));
                        }
                    }else{
                        //SQLite'dan kayıtlı konum gelecek
                    }
                }
            }
        }
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        Geocoder gc = new Geocoder(getApplicationContext(), Locale.getDefault());
        String address = "";

        //Belirtilen konumun gc karşılığı olmayabilir
        try {
            List<Address> addressList = gc.getFromLocation(latLng.latitude,latLng.longitude,1);
            if(addressList.get(0).getThoroughfare() != null){
                address+=addressList.get(0).getThoroughfare() + " ";
                if(addressList.get(0).getSubThoroughfare() != null){
                    address+=addressList.get(0).getSubThoroughfare() + " ";
                }

            }else{
                address ="New Place";
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        mMap.clear();
        //tıklanılan yere işaretçi ekledik
        mMap.addMarker(new MarkerOptions().title(address).position(latLng));


    }
}
