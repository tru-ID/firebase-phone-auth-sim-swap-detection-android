package com.example.firebaseandroid.API.data

import com.google.gson.annotations.SerializedName

data class SIMCheckPost(@SerializedName("phone_number") val phone_number: String)

data class SIMCheckResult(@SerializedName("no_sim_change") val no_sim_change: Boolean)