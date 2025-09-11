package com.firexrwtinc.mytime.ui.notifications

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.firexrwtinc.mytime.R

class NotificationsViewModel(application: Application) : AndroidViewModel(application) {

    private val _text = MutableLiveData<String>().apply {
        value = application.getString(R.string.notifications_fragment_text)
    }
    val text: LiveData<String> = _text
}