package com.cbi_solar;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import okhttp3.ResponseBody;

public class ExampleModel {

    @SerializedName("status")
    @Expose
    private Boolean status;
    @SerializedName("msg")
    @Expose
    private String msg;
    @SerializedName("responseBody")
    @Expose
    private List<ResponseBodye> responseBody;

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public List<ResponseBodye> getResponseBody() {
        return responseBody;
    }

    public void setResponseBody(List<ResponseBodye> responseBody) {
        this.responseBody = responseBody;
    }

}