package com.example.firebaseandroid

import android.os.Bundle
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.example.firebaseandroid.utils.isPhoneNumberFormatValid
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.TimeUnit
import id.tru.sdk.TruSDK
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.URL
import com.example.firebaseandroid.API.retrofit.RetrofitService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.example.firebaseandroid.API.data.SIMCheckPost
import com.example.firebaseandroid.API.data.SIMCheckResult

class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private var verificationId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        TruSDK.initializeSdk(this.applicationContext)
        auth = FirebaseAuth.getInstance()

        SubmitHandler.setOnClickListener {
            // get phone number
            val phoneNumber = phoneInput.text.toString()
                .replace(Regex("\\s+"), "")
            Log.d("phone number is", phoneNumber)

            // close virtual keyboard
            phoneInput.onEditorAction(EditorInfo.IME_ACTION_DONE)

            // if it's a valid phone number begin createSIMCheck
            if (!isPhoneNumberFormatValid(phoneNumber)) {
                renderMessage("Invalid Phone Number", "Invalid Phone Number")
            } else {
                setUIStatus(SubmitHandler, phoneInput, false)
                 
                CoroutineScope(Dispatchers.IO).launch {
                    val resp: JSONObject = TruSDK.getInstance().openWithDataCellular(URL("https://eu.api.tru.id/public/coverage/v0.1/device_ip"), false)
                    var supportsSimCheck = false

                    if (resp.optString("error") != "") {
                        println("not reachable: ${resp.optString("error_description","No error description found")}")
                    } else {
                        val status = resp.optInt("http_status")
                        if (status == 200) {
                            val body = resp.optJSONObject("response_body")
                            if (body != null) {
                                val products = body.optJSONArray("products")

                                if (products != null) {
                                    (0 until products.length()).forEach {
                                        val product = products.getJSONObject(it)

                                        if (product.get("product_id") == "SCK") {
                                            supportsSimCheck = true
                                        }
                                    }
                                }
                            }
                        }

                        if (!supportsSimCheck) {
                            renderMessage("Something went wrong.", "Please contact support.")
                            setUIStatus(SubmitHandler, phoneInput, true);
                            return@launch
                        }

                        val response = rf().createSIMCheck(SIMCheckPost(phoneNumber))

                        if (response.isSuccessful && response.body() != null) {
                            val simCheckResult = response.body() as SIMCheckResult

                            // update the UI if the SIM has changed recently
                            if (!simCheckResult.no_sim_change) {
                                renderMessage("SIM Changed Recently. Cannot Proceed 😥", "SIM-Changed")
                                setUIStatus(SubmitHandler, phoneInput, true);
                                return@launch
                            }

                            // proceed with Firebase Phone Auth
                            val options = PhoneAuthOptions.newBuilder(auth!!)
                                .setPhoneNumber(phoneNumber)       // Phone number to verify
                                .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                                .setActivity(this@MainActivity)                 // Activity (for callback binding)
                                .setCallbacks(callbacks)          // OnVerificationStateChangedCallbacks
                                .build()
                            PhoneAuthProvider.verifyPhoneNumber(options)
                        }
                    }
                }
            }
        }
    }

    //Firebase callbacks
    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            // This callback will be invoked in two situations:
            // 1 - Instant verification. In some cases the phone number can be instantly
            //     verified without needing to send or enter a verification code.
            // 2 - Auto-retrieval. On some devices Google Play services can automatically
            //     detect the incoming verification SMS and perform verification without
            //     user action.
            Log.d("MainActivity ", "onVerificationCompleted:$credential")
            val code = credential.smsCode

            if (code != null) {
                verifyVerificationCode(code)
            }

            // TODO: What happens if the code is null?
        }

        private fun verifyVerificationCode(code: String) {
            //creating the credential
            val credential = PhoneAuthProvider.getCredential(verificationId!!, code)

            //signing the user in
            signInWithPhoneAuthCredential(credential)
        }

        private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
            auth!!.signInWithCredential(credential)
                .addOnCompleteListener(
                    this@MainActivity,
                    OnCompleteListener<AuthResult?> { task ->
                        if (task.isSuccessful) {
                            // verification successful we will start the profile activity
                            renderMessage("Successfully logged in ✔", "Success")
                        } else {
                            renderMessage("Failed to log in.", "Failure")
                        }
                    })
        }

        override fun onVerificationFailed(e: FirebaseException) {
            // This callback is invoked in an invalid request for verification is made,
            // for instance if the the phone number format is not valid.
            Log.w("MainActivity ", "onVerificationFailed", e)

            if (e is FirebaseAuthInvalidCredentialsException) {
                // Invalid request
                renderMessage("Invalid Request", "Invalid")

                return
            } else if (e is FirebaseTooManyRequestsException) {
                // The SMS quota for the project has been exceeded
                renderMessage("SMS Quota exceeded", "Quota")

                return
            }

            // Show a message and update the UI
            renderMessage("Verification Failed", "Failed Verification")
        }

        override fun onCodeSent(
            verificationCode: String,
            token: PhoneAuthProvider.ForceResendingToken
        ) {
            // The SMS verification code has been sent to the provided phone number, we
            // now need to ask the user to enter the code and then construct a credential
            // by combining the code with a verification ID.
            Log.d("MainActivity", "onCodeSent:$verificationCode")

            // Save verification ID so we can it them later
            verificationId = verificationCode
        }
    }
   
    // render dialog
    private fun renderMessage(message: String, tagName: String) {
        val alertFragment = AlertDialogFragment(message)

        alertFragment.show(supportFragmentManager, tagName)
    }

    private fun setUIStatus (button: Button?, input: EditText, enabled: Boolean) {
        runOnUiThread {
            button?.isClickable = enabled
            button?.isEnabled = enabled
            input.isEnabled = enabled
        }
    }
}

//retrofit setup
private fun rf(): RetrofitService {
    return  Retrofit.Builder().baseUrl(RetrofitService.base_url).addConverterFactory(
        GsonConverterFactory.create()).build().create(RetrofitService::class.java)
}