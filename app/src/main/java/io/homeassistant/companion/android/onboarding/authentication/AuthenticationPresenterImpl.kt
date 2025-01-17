package io.homeassistant.companion.android.onboarding.authentication

import android.net.Uri
import android.util.Log
import io.homeassistant.companion.android.domain.authentication.AuthenticationUseCase
import kotlinx.coroutines.*
import javax.inject.Inject


class AuthenticationPresenterImpl @Inject constructor(
    private val view: AuthenticationView,
    private val authenticationUseCase: AuthenticationUseCase
) : AuthenticationPresenter {

    companion object {
        private const val TAG = "AuthenticationPresenter"
        private const val AUTH_CALLBACK = "homeassistant://auth-callback"
    }

    private val mainScope: CoroutineScope = CoroutineScope(Dispatchers.Main + Job())

    override fun onViewReady() {
        mainScope.launch {
            view.loadUrl(authenticationUseCase.buildAuthenticationUrl(AUTH_CALLBACK).toString())
        }
    }

    override fun onRedirectUrl(redirectUrl: String): Boolean {
        val code = Uri.parse(redirectUrl).getQueryParameter("code")
        return if (redirectUrl.contains(AUTH_CALLBACK) && !code.isNullOrBlank()) {
            mainScope.launch {
                try {
                    authenticationUseCase.registerAuthorizationCode(code)
                } catch (e: Exception) {
                    Log.e(TAG, "unable to register code")
                }
                view.openWebview()
            }
            true
        } else {
            false
        }
    }

    override fun onFinish() {
        mainScope.cancel()
    }

}
