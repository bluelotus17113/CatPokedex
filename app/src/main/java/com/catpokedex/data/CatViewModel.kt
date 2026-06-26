package com.catpokedex.data

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import com.catpokedex.CatPokedexApp

class CatViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = (application as CatPokedexApp).database.catDao()

    private val _cats = MutableLiveData<List<Cat>>()
    val cats: LiveData<List<Cat>> = _cats

    private val _catCount = MutableLiveData<Int>()
    val catCount: LiveData<Int> = _catCount

    init {
        viewModelScope.launch {
            dao.getAllCats().collectLatest { list ->
                _cats.value = list
                _catCount.value = list.size
            }
        }
    }

    fun insertCat(cat: Cat) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.insertCat(cat)
        }
    }

    fun deleteCat(cat: Cat) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.deleteCat(cat)
        }
    }

    fun getCatById(id: Int, callback: (Cat?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val cat = dao.getCatById(id)
            launch(Dispatchers.Main) { callback(cat) }
        }
    }
}
