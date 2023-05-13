package com.example.razorepayintegration

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.razorepayintegration.databinding.ActivityMainBinding
import com.razorpay.BaseRazorpay
import com.razorpay.BaseRazorpay.PaymentMethodsCallback
import com.razorpay.PaymentResultListener
import com.razorpay.Razorpay
import org.json.JSONException
import org.json.JSONObject
import java.lang.Error


class MainActivity : AppCompatActivity(),PaymentResultListener {
    private var upiList = arrayListOf<String>()
    private var walletList = arrayListOf<String>()
    private var bankList = arrayListOf<String>()
    private var banksCodeList = arrayListOf<String>()
    lateinit var razorpay : Razorpay
    lateinit var payload : JSONObject
    lateinit var binding: ActivityMainBinding
    private var adapterBankList : ArrayAdapter<String>?=null
    private var walletsListAdapter : ArrayAdapter<String>?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_main)


        adapterBankList = ArrayAdapter<String>(this,android.R.layout.simple_dropdown_item_1line,bankList)
        walletsListAdapter = ArrayAdapter<String>(this,android.R.layout.simple_dropdown_item_1line,walletList)

        binding.spinnerWalletList.adapter = walletsListAdapter
        binding.spinnerNetBankingList.adapter = adapterBankList

        initRazorpay()
        performOnClick()
    }
    private fun initRazorpay(){
        razorpay = Razorpay(this)
        razorpay.getPaymentMethods(object : PaymentMethodsCallback {
            override fun onPaymentMethodsReceived(result: String) {
                /**
                 * This returns JSON data
                 * The structure of this data can be seen at the following link:
                 * https://api.razorpay.com/v1/methods?key_id=rzp_test_1DP5mmOlF5G5ag
                 *
                 */
                Log.d("Result", "" + result)
                inflateLists(result)
            }

            override fun onError(error: String) {
                Log.d("Get Payment error", error)
            }
        })
        razorpay.changeApiKey("rzp_test_Ux45SWVLLFU8J4");
//        razorpay.setWebView(binding.webView)


    }

    override fun onPaymentSuccess(p0: String?) {
        TODO("Not yet implemented")
    }

    override fun onPaymentError(p0: Int, p1: String?) {
        TODO("Not yet implemented")
    }
    private fun inflateLists(result : String){
        val paymentMethods = JSONObject(result)
        val banksListJSON = paymentMethods.getJSONObject("netbanking")
        val walletListJSON = paymentMethods.getJSONObject("wallet")
//        val upiListJSON = paymentMethods.getJSONObject("upi")
        Log.e("Result", "inflateLists: $result" )
        val itr1 = banksListJSON.keys()
        while (itr1.hasNext()){
            banksCodeList.add(itr1.next())
            try {
                bankList.add(banksListJSON.getString(itr1.next()))
            } catch (e: JSONException) {
                Log.d("Reading Banks List", "" + e.message)
            }
        }
        Log.e("bankList", "inflateLists: $bankList")

        val itr2 = walletListJSON.keys()
        while (itr2.hasNext()) {
            val key = itr2.next()
            try {
                if (walletListJSON.getBoolean(key)) {
                    walletList .add(key)
                }
            } catch (e: JSONException) {
                Log.d("Reading Wallets List", "" + e.message)
            }
        }
        Log.e("bankList", "inflateLists: $walletList")

        adapterBankList?.notifyDataSetChanged()
        walletsListAdapter?.notifyDataSetChanged()

    }
    private fun performOnClick(){

        binding.submitCardDetails.setOnClickListener {
            submitCardDetails()
        }

        binding.rgRadioGroup.setOnCheckedChangeListener { radioGroup, i ->
            when(i){
                R.id.rbPhonePe->
                    Log.e("TAG", "performOnClick: phonepe" )
                R.id.rbGooglePe->
                    Log.e("TAG", "performOnClick: google pay", )
            }
        }
    }
    private fun submitCardDetails(){
        try {
            payload = JSONObject("{currency: 'INR'}")
            payload.put("amount", "100")
            payload.put("contact", "9781767938")
            payload.put("email", "rajeev1995rajan@gmail.com")

            val index: Int = binding.expiry.text.toString().indexOf('/')
            val month: String = binding.expiry.text.toString().substring(0, index)
            val year: String = binding.expiry.text.toString().substring(index + 1)

            payload.put("method", "card")
            payload.put("card[name]", binding.name.text.toString())
            payload.put("card[number]", binding.cardNumber.text.toString())
            payload.put("card[expiry_month]", month)
            payload.put("card[expiry_year]", year)
            payload.put("card[cvv]", binding.cvv.text.toString())
            sendRequest()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun sendRequest(){
        razorpay.validateFields(payload,object : BaseRazorpay.ValidationListener {
            override fun onValidationSuccess() {
                try {
                    razorpay.submit(payload, this@MainActivity)
                } catch (e: java.lang.Exception) {
                    Log.e("com.example", "Exception: ", e)
                }
            }

            override fun onValidationError(error: MutableMap<String, String>?) {
                Log.d("com.example", "Validation failed: " + error?.get("field") + " " + error?.get("description"));
                Toast.makeText(this@MainActivity, "Validation: " + error?.get("field") + " " + error?.get("description"), Toast.LENGTH_SHORT).show();
            }

        })
    }
}