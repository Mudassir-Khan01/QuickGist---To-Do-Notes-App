package com.example.quickgist.Activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.example.quickgist.R
import com.example.quickgist.databinding.ActivityLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class Login : AppCompatActivity() {
    private val binding: ActivityLoginBinding by lazy {
        ActivityLoginBinding.inflate(layoutInflater)
    }
    lateinit var googlesignin:Button
    lateinit var googlesignInClient:GoogleSignInClient
    lateinit var auth:FirebaseAuth
    private val RC_SIGN_IN=100
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        auth=FirebaseAuth.getInstance()
//checkuser()
        val googleSignInOptions=GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("1094445284031-pshs72b9uj5qqt4218jbbr2na5s2kicr.apps.googleusercontent.com")
            .requestEmail()
            .build()
        googlesignInClient=GoogleSignIn.getClient(this,googleSignInOptions)

        binding.googleSignin.setOnClickListener {
            val signInclient=googlesignInClient.signInIntent
            launcher.launch(signInclient)
        }
    }
    private val launcher=registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            result->
        if(result.resultCode== Activity.RESULT_OK){
            val task=GoogleSignIn.getSignedInAccountFromIntent(result.data)
            if(task.isSuccessful){
                val account:GoogleSignInAccount?=task.result
                val credential=GoogleAuthProvider.getCredential(account?.idToken,null)
                auth.signInWithCredential(credential).addOnCompleteListener{
                    if(it.isSuccessful){
                        //  Toast.makeText(this,"DOne",Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this,NoteActivity::class.java))
                    }else{
                        Toast.makeText(this,"FAILED",Toast.LENGTH_SHORT).show()

                    }
                }
            }
        }else{
            Toast.makeText(this,"FAILED",Toast.LENGTH_SHORT).show()
        }
    }

//    @Deprecated("Deprecated in Java")
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        when(requestCode){
//            RC_SIGN_IN->{
//                val accountTask=GoogleSignIn.getSignedInAccountFromIntent(data)
//                try {
//                    val account=accountTask.getResult(ApiException::class.java)
//                    firebaseAuthWithGoogleAccount(account)
//
//                }catch (e:Exception){
//                    Toast.makeText(this,"SignIn Error",Toast.LENGTH_SHORT).show()
//                }
//            }
//        }
//    }

//    private fun firebaseAuthWithGoogleAccount(account: GoogleSignInAccount?) {
//        val credential=GoogleAuthProvider.getCredential(account!!.idToken,null)
//        firebaseauth.signInWithCredential(credential)
//            .addOnSuccessListener {
//                startActivity(Intent(this,NoteActivity::class.java))
//                overridePendingTransition(0,0)
//                finish()
//            }.addOnFailureListener{
//                Toast.makeText(this,it.message,Toast.LENGTH_SHORT).show()
//            }
//    }

    private fun checkuser() {
        val firebaseuser=auth.currentUser
        if(firebaseuser!=null) {
            startActivity(Intent(this, NoteActivity::class.java))
            overridePendingTransition(0, 0)
            finish()
        }else{
            auth.signOut()
        }
    }
}