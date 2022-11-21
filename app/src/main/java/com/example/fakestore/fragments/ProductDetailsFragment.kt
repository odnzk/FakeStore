package com.example.fakestore.fragments

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.asLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import com.example.fakestore.R
import com.example.fakestore.databinding.FragmentProductDetailsBinding
import com.example.fakestore.epoxy.decorators.SimpleHorizontalDividerItemDecorator
import com.example.fakestore.menufragments.ProductListFragment
import com.example.fakestore.model.ui.CartUiProduct
import com.example.fakestore.model.ui.UiProduct
import com.example.fakestore.utils.uimanager.MainUiManager
import com.example.fakestore.utils.uimanager.MainUiManager.formatToPrice
import com.example.fakestore.utils.uimanager.MainUiManager.setBtnToCartStyle
import com.example.fakestore.utils.uimanager.MainUiManager.setFavoriteIcon
import com.example.fakestore.viewmodels.DetailedViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged

@AndroidEntryPoint
class ProductDetailsFragment : Fragment(R.layout.fragment_product_details) {
    private var _binding: FragmentProductDetailsBinding? = null
    private val binding: FragmentProductDetailsBinding by lazy { _binding!! }

    private val viewModel: DetailedViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentProductDetailsBinding.bind(view)

        arguments?.let {
            val productId: Int = it.getInt(MainUiManager.KEY_PRODUCT_ID, -1)
            if (productId != -1) {
                observeUiProduct(productId)
            }
        }
        bottomNavIsVisible(false)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        bottomNavIsVisible(true)
        _binding = null
    }

    private fun observeUiProduct(productId: Int) {
        viewModel.cartProductReducer.reduce(viewModel.store).distinctUntilChanged().asLiveData()
            .observe(viewLifecycleOwner) { cartProducts ->
                displayUiProduct(cartProducts.first { it.uiProduct.product.id == productId })
            }
        viewModel.uiProductReducer.reduce(viewModel.store).distinctUntilChanged().asLiveData()
            .observe(viewLifecycleOwner) { uiProducts ->
                val selectedCategory: String? =
                    uiProducts.find { it.product.id == productId }?.product?.category
                selectedCategory?.let { displayProductSuggestions(uiProducts.filter { it.product.category == selectedCategory }) }
            }
    }

    private fun displayProductSuggestions(listSuggestions: List<UiProduct>) {
        binding.rvYouMightAlsLike.run {
            if (!isDirty) {
                addItemDecoration(
                    SimpleHorizontalDividerItemDecorator(ProductListFragment.MARGIN_BOTTOM_RECYCLER_VIEW_ITEM)
                )
                layoutManager =
                    LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
//                val controller = FavoriteItemEpoxyController(viewModel, findNavController())
//                controller.setData(FavFragmentUiState.NonEmpty(listSuggestions))
//                setController(controller)
            }
        }
    }


    private fun displayUiProduct(cartProduct: CartUiProduct) {
        with(binding) {
            btnQuantity.tvQuantity.text = cartProduct.quantityInCart.toString()
            cartProduct.uiProduct.product.run {
                btnQuantity.btnPlus.setOnClickListener {
                    viewModel.updateCartQuantity(
                        id,
                        cartProduct.quantityInCart + 1
                    )
                }

                btnQuantity.btnMinus.setOnClickListener {
                    viewModel.updateCartQuantity(
                        id,
                        cartProduct.quantityInCart - 1
                    )
                }


                tvHeadline.text = title
                tvDescription.text = "$description $description $description $description"
                ratingBar.rating = rating.rate
                tvReviews.text = getString(R.string.count_of_reviews, rating.count)

                tvPrice.text = price.formatToPrice()

                pbLoadingImage.isVisible = true
                ivExpanded.load(data = image) {
                    listener { _, _ ->
                        pbLoadingImage.isVisible = false
                    }
                }

                btnToFavorites.setOnClickListener {
                    viewModel.updateFavoriteSet(id)
                }
                btnToCart.setOnClickListener {
                    viewModel.updateCartProductsId(id)
                }
            }

            cartProduct.uiProduct.run {
                btnToCart.setBtnToCartStyle(isInCart, root.context)
                btnToFavorites.setFavoriteIcon(isInFavorites)
            }
        }
    }

    private fun bottomNavIsVisible(isVisible: Boolean) {
        activity?.findViewById<BottomNavigationView>(R.id.bottom_navigation)?.isVisible =
            isVisible
    }

}
