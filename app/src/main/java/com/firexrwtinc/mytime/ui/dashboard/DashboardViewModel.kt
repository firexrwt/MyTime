package com.firexrwtinc.mytime.ui.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.firexrwtinc.mytime.R

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val _text = MutableLiveData<String>().apply {
        value = application.getString(R.string.dashboard_fragment_text)
    }
    val text: LiveData<String> = _text
}