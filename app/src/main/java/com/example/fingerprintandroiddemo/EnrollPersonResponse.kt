package com.example.fingerprintandroiddemo

import com.google.gson.annotations.SerializedName

data class PersonResponse(
    @SerializedName("ClientID") val clientID: String,
    @SerializedName("CustomID") val customID: String,
    @SerializedName("BioPassID") val bioPassID: String
)

data class EnrollPersonResponse(
    @SerializedName("Person") val person: PersonResponse,
    @SerializedName("Message") val message: String
)