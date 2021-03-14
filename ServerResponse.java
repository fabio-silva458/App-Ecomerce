package com.example.admin.jprod;

import com.google.gson.annotations.SerializedName;

class ServerResponse {

    // variable name should be same as in the json response from php
    @SerializedName("success")
    boolean success;
    @SerializedName("message")
    String message;
    @SerializedName("pimgs")
    String pimgs;

    String getMessage() {
        return message;
    }

    boolean getSuccess() {
        return success;
    }

    String getPimgs() {
        return pimgs;
    }

}
