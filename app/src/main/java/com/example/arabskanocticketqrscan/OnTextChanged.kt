package com.example.arabskanocticketqrscan

import android.text.Editable
import android.text.TextWatcher

class OnTextChanged(
    val onTextChangedListener: (s: CharSequence?) -> Unit
) : TextWatcher {
    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }
    override fun afterTextChanged(s: Editable?) { }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        onTextChangedListener(s)
    }
}