package com.cbi_solar;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class StoreModel {

    @SerializedName("store_name")
    @Expose
    private String store_name;
    @SerializedName("contact_no")
    @Expose
    private String contact_no;
    @SerializedName("address")
    @Expose
    private String address;

    public String getStore_name() {
        return store_name;
    }

    public void setStore_name(String store_name) {
        this.store_name = store_name;
    }

    public String getContact_no() {
        return contact_no;
    }

    public void setContact_no(String contact_no) {
        this.contact_no = contact_no;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}