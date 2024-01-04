package com.blackmidori.familyexpenses.android

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.credentials.CredentialOption
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.blackmidori.familyexpenses.android.infrastructure.http.HttpClientImpl
import com.blackmidori.familyexpenses.android.repositories.AuthRepository
import com.blackmidori.familyexpenses.android.screens.HomeActivity
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.UUID


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        TopBar()
                    }
                ) { innerPadding ->
                    Box(Modifier.padding(innerPadding)) {
                        Body()
                    }
                }
            }
        }
    }

    @Composable
    fun TopBar() {
        Box(
            Modifier
                .background(MaterialTheme.colorScheme.primary)
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Login", Modifier.align(Alignment.Center))
        }
    }

    @Composable
    fun Body() {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            val coroutineScope = rememberCoroutineScope()
            val context = LocalContext.current
            Button(
                modifier = Modifier.align(Alignment.Center),
                onClick = {
                    val googleIdRequest = getGoogleIdRequest()

                    coroutineScope.launch {
                        try {
                            val credentialManager = CredentialManager.create(context)
                            val result = credentialManager.getCredential(
                                context,
                                googleIdRequest,
                            )
                            when (val credential = result.credential) {
                                is GoogleIdTokenCredential -> {
                                    handleIdToken(context,credential.idToken)
                                }
                            }
                        } catch (e: GetCredentialException) {
                            Log.w(TAG, "handleSignInResult:error", e)
                        }
                    }
                },
                contentPadding = PaddingValues(0.dp),
            ) {
                Image(
                    painter = painterResource(id = R.drawable.android_light_rd_ctn),
                    contentDescription = ""
                )
            }
        }
    }

    @Preview
    @Composable
    fun Preview() {
        MyApplicationTheme {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                topBar = {
                    TopBar()
                }
            ) { innerPadding ->
                Box(Modifier.padding(innerPadding)) {
                    Body()
                }
            }
        }
    }


    private fun getGoogleIdRequest(): GetCredentialRequest {
        val googleIdOption: CredentialOption = GetGoogleIdOption.Builder()
            .setServerClientId(getString(R.string.google_oauth_server_client_id))
            .setNonce(UUID.randomUUID().toString())
            .build()
        return GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()
    }

    private fun handleIdToken(context: Context, idToken: String) {
        Thread {
            try {
                val userTokensResult =
                    AuthRepository(httpClient = HttpClientImpl()).signInWithToken(idToken)
                if (userTokensResult.isFailure) {
                    Log.w(TAG, "handleIdToken: " + userTokensResult.exceptionOrNull())
                    return@Thread;
                }
                val response =  userTokensResult.getOrNull()!!
                Session.appUser = AppUser(
                    response.user.id,
                    AppUserTokens(
                        response.accessTokenExpirationDateTime,
                        response.accessToken,
                        response.refreshToken
                    ),
                )
                context.startActivity(
                    Intent(
                        context,
                        HomeActivity::class.java
                    ).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                )
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }.start()
    }

    companion object {
        private val TAG = MainActivity::class.java.simpleName
    }
}