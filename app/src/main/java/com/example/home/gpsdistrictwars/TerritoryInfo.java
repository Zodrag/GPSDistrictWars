package com.example.home.gpsdistrictwars;

import com.google.android.gms.maps.model.LatLng;

class TerritoryInfo {
        String districtName;
        LatLng latLng;

        TerritoryInfo (String districtName, LatLng latLng){
            this.latLng = latLng;
            this.districtName = districtName;
        }


}
