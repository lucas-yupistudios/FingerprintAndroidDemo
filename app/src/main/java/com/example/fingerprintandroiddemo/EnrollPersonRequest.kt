package com.example.fingerprintandroiddemo

import com.google.gson.annotations.SerializedName

data class FingerPersonRequest(
    @SerializedName("Finger-1") val finger1: String? = null,
    @SerializedName("Finger-2") val finger2: String? = null,
    @SerializedName("Finger-3") val finger3: String? = null,
    @SerializedName("Finger-4") val finger4: String? = null,
    @SerializedName("Finger-5") val finger5: String? = null,
    @SerializedName("Finger-6") val finger6: String? = null,
    @SerializedName("Finger-7") val finger7: String? = null,
    @SerializedName("Finger-8") val finger8: String? = null,
    @SerializedName("Finger-9") val finger9: String? = null,
    @SerializedName("Finger-10") val finger10: String? = null,
)

data class PersonRequest(
    @SerializedName("CustomID") val customID: String,
    @SerializedName("Fingers") val fingers: List<FingerPersonRequest>
)

data class EnrollPersonRequest(
    @SerializedName("Person") val person: PersonRequest
)