package com.example.fakestore.menufragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.asLiveData
import androidx.navigation.fragment.findNavController
import com.example.fakestore.R
import com.example.fakestore.databinding.FragmentProductListBinding
import com.example.fakestore.epoxy.controllers.UiProductListFragmentController
import com.example.fakestore.model.mapper.ProductMapper
import com.example.fakestore.model.ui.ProductListFragmentUiState
import com.example.fakestore.model.ui.UiFilter
import com.example.fakestore.network.NetworkService
import com.example.fakestore.viewmodels.MainViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@AndroidEntryPoint
class ProductListFragment : Fragment(R.layout.product_list_layout) {
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

        with(binding){
            val bottomSheetBehavior = BottomSheetBehavior.from(productListLayout.root).apply {
                isFitToContents = false
                isHideable = false //prevents the bottom sheet from completely hiding off the screen
                setState(BottomSheetBehavior.STATE_EXPANDED) //initially state to fully expanded
            }
            productListLayout.btnShowFilters.setOnClickListener {
                toggleFilters(bottomSheetBehavior)
            }
        }

        val viewModel: MainViewModel by viewModels()
        val epoxyController =
            UiProductListFragmentController(resources, viewModel, findNavController())

        // todo fix shimmer

        combine(
            viewModel.uiProductReducer.reduce(viewModel.store),
            viewModel.store.stateFlow.map { it.productFilterInfo }
        ) { uiProducts, productFilterInfo ->
            if (uiProducts.isEmpty()) {
                return@combine ProductListFragmentUiState.Loading
            }

            val uiFilters: Set<UiFilter> =
                productFilterInfo.filters.map { filter ->
                    UiFilter(
                        filter = filter,
                        isSelected = productFilterInfo.selectedFilter?.equals(filter) == true
                    )
                }.toSet()

            val filteredProducts = if (productFilterInfo.selectedFilter == null) {
                uiProducts
            } else {
                uiProducts.filter { uiProduct ->
                    uiProduct.product.category == productFilterInfo.selectedFilter.title
                }
            }

            return@combine ProductListFragmentUiState.Success(
                products = filteredProducts,
                filters = uiFilters
            )
        }.distinctUntilChanged().asLiveData()
            .observe(viewLifecycleOwner) { productListFragmentUiState ->
                epoxyController.setData(productListFragmentUiState)
            }

        viewModel.refreshProducts()
        with(binding) {
            productListLayout.rvProducts.setController(epoxyController)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun toggleFilters(bottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>) {
        if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HALF_EXPANDED)
        } else {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED)
        }
    }

}
