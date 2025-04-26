package com.cbi_solar;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ResponseBodye {

    @SerializedName("CaseId")
    @Expose
    private String CaseId;

    @SerializedName("ApplicantName")
    @Expose
    private String ApplicantName;

    @SerializedName("ApplicationNo")
    @Expose
    private String ApplicationNo;

    @SerializedName("MobileNo")
    @Expose
    private String MobileNo;

    @SerializedName("VisitAddress")
    @Expose
    private String VisitAddress;

    @SerializedName("Activity")
    @Expose
    private String Activity;

    @SerializedName("CustomerProfile")
    @Expose
    private String CustomerProfile;

    @SerializedName("CaseStatus")
    @Expose
    private String CaseStatus;

    public String getCaseId() {
        return CaseId;
    }

    public void setCaseId(String caseId) {
        CaseId = caseId;
    }

    public String getApplicantName() {
        return ApplicantName;
    }

    public void setApplicantName(String applicantName) {
        ApplicantName = applicantName;
    }

    public String getApplicationNo() {
        return ApplicationNo;
    }

    public void setApplicationNo(String applicationNo) {
        ApplicationNo = applicationNo;
    }

    public String getMobileNo() {
        return MobileNo;
    }

    public void setMobileNo(String mobileNo) {
        MobileNo = mobileNo;
    }

    public String getVisitAddress() {
        return VisitAddress;
    }

    public void setVisitAddress(String visitAddress) {
        VisitAddress = visitAddress;
    }

    public String getActivity() {
        return Activity;
    }

    public void setActivity(String activity) {
        Activity = activity;
    }

    public String getCustomerProfile() {
        return CustomerProfile;
    }

    public void setCustomerProfile(String customerProfile) {
        CustomerProfile = customerProfile;
    }

    public String getCaseStatus() {
        return CaseStatus;
    }

    public void setCaseStatus(String caseStatus) {
        CaseStatus = caseStatus;
    }
}
