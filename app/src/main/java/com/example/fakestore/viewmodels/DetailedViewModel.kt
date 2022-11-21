package com.example.fakestore.viewmodels

import androidx.lifecycle.viewModelScope
import com.example.fakestore.redux.reducer.CartProductReducer
import com.example.fakestore.redux.reducer.UiProductReducer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailedViewModel @Inject constructor(
    val cartProductReducer: CartProductReducer,
    val uiProductReducer: UiProductReducer
) : BaseViewModel() {

    fun updateCartQuantity(productId: Int, updatedQuantity: Int) = viewModelScope.launch {
        store.update { applicationState ->
            return@update cartUpdater.updateWithQuantity(
                applicationState,
                productId,
                updatedQuantity
            )
        }
    }

}
