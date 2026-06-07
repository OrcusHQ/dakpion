package com.orcuspay.dakpion.presentation.screens.splash

import androidx.lifecycle.ViewModel
import com.orcuspay.dakpion.util.DakpionPreference
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val dakpionPreference: DakpionPreference,
) : ViewModel() {

    init {
        val isFirstLaunch = dakpionPreference.isFirstLaunch()
        if (isFirstLaunch) {
            dakpionPreference.setFirstLaunch()
        }
    }

}
