package com.example.fakestore.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fakestore.MainViewModel
import com.example.fakestore.R
import com.example.fakestore.databinding.FragmentProductListBinding
import com.example.fakestore.epoxy.UiProductEpoxyController
import com.example.fakestore.model.mapper.ProductMapper
import com.example.fakestore.model.ui.UiProduct
import com.example.fakestore.network.NetworkService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@AndroidEntryPoint
class ProductListFragment : Fragment(R.layout.fragment_product_list) {
    private var _binding: FragmentProductListBinding? = null
    private val binding: FragmentProductListBinding by lazy { _binding!! }

    @Inject
    lateinit var service: NetworkService

    @Inject
    lateinit var productMapper: ProductMapper


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mainViewModel: MainViewModel by viewModels()
        val epoxyController = UiProductEpoxyController(resources, mainViewModel)
        // setting an empty state for shimmer ??
        epoxyController.setData(emptyList())

        combine(mainViewModel.store.stateFlow.map { it.products },
            mainViewModel.store.stateFlow.map { it.favorites }) { listProducts, listFavorites ->
            listProducts.map { product ->
                UiProduct(product = product, isInFavorites = listFavorites.contains(product.id))
            }
        }.distinctUntilChanged().asLiveData().observe(viewLifecycleOwner) { products ->
            epoxyController.setData(products)
        }
        mainViewModel.refreshProducts()
//        lifecycleScope.launchWhenStarted {
//            val response = service.getAllProducts()
//            val domainProductList = response.body()?.let { listNetworkProducts ->
//                listNetworkProducts.map {
//                    productMapper.buildFrom(networkProduct = it)
//                }
//            }
//            epoxyController.setData(domainProductList)
//        }

        with(binding) {
            val itemDecorator =
                DividerItemDecoration(context, LinearLayoutManager.VERTICAL)
            rvProducts.addItemDecoration(itemDecorator)
            rvProducts.setController(epoxyController)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
