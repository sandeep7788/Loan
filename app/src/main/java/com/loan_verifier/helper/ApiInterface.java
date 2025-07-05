package com.loan_verifier.helper;

import com.google.gson.JsonObject;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

public interface ApiInterface {

//    http://cbisolar.vidhyalaya.co.in/Api/Api/customerlogin?mobno=9828775444&password=1234&remember_token=1234
//    http://cbisolar.vidhyalaya.co.in/Api/Api/customerlogin?mobno="9828775444"&password="1234"&remember_token="1234"

    @GET(ApiContants.PREF_ser_req_status)
    Call<JsonObject> ser_req_status(
            @Query("reff_no") String reff_no);

    @GET(ApiContants.PREF_customerlogin)
    Call<JsonObject> signIn(
            @Query("mobno") String mobno,
            @Query("password") String password,
            @Query("deviceID") String deviceID);

    @GET("CaseList")
    Call<JsonObject> getCaseList(@Query("verifier_id") int verifierId, @Header("Cookie") String cookie);


    @GET(ApiContants.PREF_profile)
    Call<JsonObject> profile(
            @Query("verifier_id") String customer_id);

    @FormUrlEncoded
    @POST(ApiContants.PREF_customer_regis)
    Call<JsonObject> signUp(
            @Field("name") String name,
            @Field("phone") String phone,
            @Field("gender") String gender,
            @Field("address") String address,
            @Field("password") String password);

    @FormUrlEncoded
    @POST(ApiContants.PREF_ser_req)
    Call<JsonObject> ser_req(
            @Field("customer_id") String customer_id,
            @Field("service_for") String service_for,
            @Field("des") String des,
            @Field("req_date") String req_date);

//    @FormUrlEncoded
    @GET(ApiContants.PREF_profileupdate)
    Call<JsonObject> profileupdate(
            @Query("customer_id") String customer_id,
            @Query("address") String address);

//    @FormUrlEncoded
    @GET(ApiContants.PREF_passwordchange)
    Call<JsonObject> passwordchange(
            @Query("verifier_id") String customer_id,
            @Query("old_password") String old_password,
            @Query("new_password") String new_password,
            @Query("confirm_password") String confirm_password);

    @GET(ApiContants.PREF_StoreList)
    Call<JsonObject> listStore();

    @GET(ApiContants.PREF_notification)
    Call<JsonObject> notification();

    @GET("CaseList")
    Call<JsonObject> product_list(@Query("verifier_id") String verifier_id);

    @POST(ApiContants.PREF_SUBMIT_LOAN_DETAILS)
    Call<JsonObject> submitLoanDetails(@Body JsonObject loanDetails);

    @POST(ApiContants.LIPCaseFinalReport)
    Call<JsonObject> submitBussinessDetails(@Body JsonObject loanDetails);

    @Multipart
    @POST("upload_image")
    Call<JsonObject> uploadImage(@Part MultipartBody.Part image,
                                 @Part("file_name") String description);



    @GET(ApiContants.PREF_banner_list)
    Call<JsonObject> banner_list();

    @FormUrlEncoded
    @POST(ApiContants.PREF_UserAPI)
    Call<JsonObject> agent_otp_login(@Field("action") String action,
                                     @Field("cell") String cell);

    @FormUrlEncoded
    @POST(ApiContants.PREF_ClinicAPI)
    Call<JsonObject> list_doc_clinic(@Field("action") String action,
                                     @Field("clinicid") String cell,
                                     @Field("ref_code") String ref_code);

    @FormUrlEncoded
    @POST(ApiContants.PREF_UserAPI)
    Call<JsonObject> otp_login(@Field("action") String action,
                               @Field("cell") String cell);

    @FormUrlEncoded
    @POST(ApiContants.PREF_ClinicAPI)
    Call<JsonObject> list_clinic(@Field("action") String action,
                               @Field("ref_code") String ref_code,
                               @Field("ref_id") String ref_id
    );

    @FormUrlEncoded
    @POST(ApiContants.PREF_ClinicAPI)
    Call<JsonObject> get_clinic(@Field("action") String action,
                               @Field("clinicid") String ref_code
    );

    @FormUrlEncoded
    @POST(ApiContants.PREF_new_prospect)
    Call<JsonObject> new_prospect(
            @Field("action") String action,
            @Field("ref_code") String ref_code,
            @Field("providername") String providername,
            @Field("email") String email,
            @Field("dob") String dob,
            @Field("gender") String gender,
            @Field("cell") String cell);

    @FormUrlEncoded
    @POST(ApiContants.PREF_ClinicAPI)
    Call<JsonObject>    new_clinic(
            @Field("action") String action,
            @Field("ref_code") String ref_code,
            @Field("ref_id") String ref_id,
            @Field("name") String name,
            @Field("address1") String address1,
            @Field("address2") String address2,
            @Field("address3") String address3,
            @Field("city") String city,
            @Field("st") String st,
            @Field("pin") String pin,
            @Field("cell") String cell,
            @Field("telephone") String telephone,
            @Field("email") String email,
            @Field("status") String status,
            @Field("website") String website,
            @Field("gps_location") String gps_location,
            @Field("whatsapp") String whatsapp,
            @Field("facebook") String facebook,
            @Field("twitter") String twitter,
            @Field("primary") String primary,
            @Field("dentalchairs") String dentalchairs,
            @Field("auto_clave") String auto_clave,
            @Field("implantology") String implantology,
            @Field("instrument_sterilization") String instrument_sterilization,
            @Field("waste_displosal") String waste_displosal,
            @Field("suction_machine") String suction_machine,
            @Field("laser") String laser,
            @Field("RVG_OPG") String RVG_OPG,
            @Field("radiation_protection") String radiation_protection,
            @Field("computers") String computers,
            @Field("network") String network,
            @Field("internet") String internet,
            @Field("air_conditioned") String air_conditioned,
            @Field("waiting_area") String waiting_area,
            @Field("backup_power") String backup_power,
            @Field("toilet") String toilet,
            @Field("water_filter") String water_filter,
            @Field("parking_facility") String parking_facility,
            @Field("receptionist") String receptionist,
            @Field("credit_card") String credit_card,
            @Field("certifcates") String certifcates,
            @Field("emergency_drugs") String emergency_drugs,
            @Field("infection_control") String infection_control,
            @Field("daily_autoclaved") String daily_autoclaved,
            @Field("patient_records") String patient_records,
            @Field("patient_consent") String patient_consent,
            @Field("patient_traffic") String patient_traffic,
            @Field("nabh_iso_certifcation") String nabh_iso_certifcation,
            @Field("mdp_registration") String mdp_registration,
            @Field("intra_oral_camera") String intra_oral_camera,
            @Field("rotary_endodontics") String rotary_endodontics
    );

    @FormUrlEncoded
    @POST(ApiContants.PREF_appointmentAPI)
    Call<JsonObject> new_appointment(
            @Field("action") String action,
            @Field("providerid") String providerid,
            @Field("doctorid") String doctorid,
            @Field("clinicid") String clinicid,
            @Field("memberid") String memberid,
            @Field("patientid") String patientid,
            @Field("cell") String cell,
            @Field("complaint") String complaint,
            @Field("appointment_start") String appointment_start,
            @Field("duration") String duration,
            @Field("notes") String notes
    );

    @FormUrlEncoded
    @POST(ApiContants.PREF_ClinicAPI)
    Call<JsonObject> delete_clinic(
            @Field("action") String action,
            @Field("clinicid") String clinicid
    );

/*
            @Part("auto_clave") RequestBody auto_clave,
            @Part("implantology") RequestBody implantology,
            @Part("instrument_sterilization") RequestBody instrument_sterilization,
            @Part("waste_displosal") RequestBody waste_displosal,
            @Part("suction_machine") RequestBody suction_machine,
            @Part("laser") RequestBody laser,
            @Part("RVG_OPG") RequestBody RVG_OPG,
            @Part("radiation_protection") RequestBody radiation_protection,
            @Part("computers") RequestBody computers,
            @Part("network") RequestBody network,
            @Part("internet") RequestBody internet,
            @Part("air_conditioned") RequestBody air_conditioned,
            @Part("waiting_area") RequestBody waiting_area,
            @Part("backup_power") RequestBody backup_power,
            @Part("toilet") RequestBody toilet,
            @Part("water_filter") RequestBody water_filter,
            @Part("parking_facility") RequestBody parking_facility,
            @Part("receptionist") RequestBody receptionist,
            @Part("credit_card") RequestBody credit_card,
            @Part("certifcates") RequestBody certifcates,
            @Part("emergency_drugs") RequestBody emergency_drugs,
            @Part("infection_control") RequestBody infection_control,
            @Part("daily_autoclaved") RequestBody daily_autoclaved,
            @Part("patient_records") RequestBody patient_records,
            @Part("patient_consent") RequestBody patient_consent,
            @Part("patient_traffic") RequestBody patient_traffic,
            @Part("nabh_iso_certifcation") RequestBody nabh_iso_certifcation,
            @Part("mdp_registration") RequestBody mdp_registration,
            @Part("intra_oral_camera") RequestBody intra_oral_camera,
            @Part("rotary_endodontics") RequestBody rotary_endodontics
   /*         @Part MultipartBody.Part file);
*/

}