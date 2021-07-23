package com.squall.doodlekong_android.ui.setup.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.squall.doodlekong_android.R
import com.squall.doodlekong_android.databinding.FragmentUsernameBinding
import com.squall.doodlekong_android.ui.setup.UsernameViewModel.SetupEvent.*
import com.squall.doodlekong_android.ui.setup.UsernameViewModel
import com.squall.doodlekong_android.util.Constants.MAX_USERNAME_LENGTH
import com.squall.doodlekong_android.util.Constants.MIN_USERNAME_LENGTH
import com.squall.doodlekong_android.util.navigateSafely
import com.squall.doodlekong_android.util.snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class UsernameFragment : Fragment() {

    private var _binding: FragmentUsernameBinding? = null
    private val binding get() = _binding!!

    private val viewModel by viewModels<UsernameViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUsernameBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        listenToEvents()
        binding.apply {
            btnNext.setOnClickListener {
                viewModel.validateUsernameAndNavigateToSelectRoom(
                    etUsername.text.toString()
                )
            }
        }
    }

    private fun listenToEvents(): Unit {
        lifecycleScope.launchWhenStarted {
            viewModel.setupEvent.collect { event ->
                when (event) {
                    is NavigateToSelectRoomEvent -> {
                        findNavController().navigateSafely(
                            R.id.action_usernameFragment_to_selectRoomFragment,
                            args = Bundle().apply { putString("username",event.username) }
                        )
                    }
                    InputEmptyError -> {
                        snackbar(R.string.error_field_empty)
                    }
                    InputTooShortError -> {
                        snackbar(getString(R.string.error_username_too_short,MIN_USERNAME_LENGTH))
                    }
                    InputTooLongError -> {
                        snackbar(getString(R.string.error_username_too_long,MAX_USERNAME_LENGTH))
                    }
                    else -> Unit
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}