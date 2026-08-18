package com.example.vocanote.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.vocanote.data.VocabDao

class VocabViewModelFactory(private val dao: VocabDao) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return VocabViewModel(dao) as T
    }
}
