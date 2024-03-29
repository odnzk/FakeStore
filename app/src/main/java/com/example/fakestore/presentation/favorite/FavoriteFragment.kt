package com.example.fakestore.presentation.favorite

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.asLiveData
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.fakestore.R
import com.example.fakestore.databinding.FragmentFavoriteBinding
import com.example.fakestore.presentation.catalog.ProductListViewModel
import com.example.fakestore.presentation.util.epoxy.decorators.SimpleGridDividerItemDecorator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged

@AndroidEntryPoint
class FavoriteFragment : Fragment(R.layout.fragment_favorite) {
    private var _binding: FragmentFavoriteBinding? = null
    private val binding: FragmentFavoriteBinding by lazy { _binding!! }

    private val viewModel: ProductListViewModel by activityViewModels()
    private val epoxyController by lazy {
        FavoriteItemEpoxyController(viewModel, findNavController())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initRecycler()
        initObservers()
    }

    private fun initObservers() {

        viewModel.uiProductReducer.reduce(viewModel.store).distinctUntilChanged().asLiveData()
            .observe(viewLifecycleOwner) { listUiProducts ->

                val favProducts = listUiProducts.filter { it.isInFavorites }

                epoxyController.setData(
                    if (favProducts.isEmpty()) FavFragmentUiState.Empty else FavFragmentUiState.NonEmpty(
                        favProducts
                    )
                )

                with(binding) {
                    rvFavorite.run {
                        if (!isDirty) {
                            addItemDecoration(
                                SimpleGridDividerItemDecorator(
                                    MARGIN_RECYCLER_VIEW_ITEM, 2
                                )
                            )
                        }
                        layoutManager = GridLayoutManager(requireContext(), 2)
                        setController(epoxyController)
                    }
                }
            }
    }

    private fun initRecycler() {
        with(binding) {
            rvFavorite.run {
                if (!isDirty) {
                    addItemDecoration(
                        SimpleGridDividerItemDecorator(
                            MARGIN_RECYCLER_VIEW_ITEM, 2
                        )
                    )
                }
                layoutManager = GridLayoutManager(requireContext(), 2)
                setController(epoxyController)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoriteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object{
        private const val MARGIN_RECYCLER_VIEW_ITEM = 16
    }
}
