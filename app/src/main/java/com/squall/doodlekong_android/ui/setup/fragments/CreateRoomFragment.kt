package com.squall.doodlekong_android.ui.setup.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ArrayAdapter
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.squall.doodlekong_android.R
import com.squall.doodlekong_android.data.remote.ws.Room
import com.squall.doodlekong_android.databinding.FragmentCreateRoomBinding
import com.squall.doodlekong_android.ui.setup.CreateRoomViewModel
import com.squall.doodlekong_android.ui.setup.CreateRoomViewModel.SetupEvent.*
import com.squall.doodlekong_android.util.Constants
import com.squall.doodlekong_android.util.hideKeyboard
import com.squall.doodlekong_android.util.navigateSafely
import com.squall.doodlekong_android.util.snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class CreateRoomFragment : Fragment() {

    private var _binding: FragmentCreateRoomBinding? = null
    private val binding get() = _binding!!

    private val viewModel by viewModels<CreateRoomViewModel>()
    private val args by navArgs<CreateRoomFragmentArgs>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateRoomBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        setupRoomSizeSpinner()
        listenToEvents()

        binding.apply {
            btnCreateRoom.setOnClickListener {
                createRoomProgressBar.isVisible = true
                viewModel.createRoom(
                    Room(
                        etRoomName.text.toString(),
                        tvMaxPersons.text.toString().toInt()
                    )
                )
                requireActivity().hideKeyboard(root)
            }
        }
    }

    private fun listenToEvents() {
        lifecycleScope.launchWhenStarted {
            viewModel.setupEvent.collect { event ->
                when (event) {
                    is CreateRoomEvent -> {
                        viewModel.joinRoom(args.username, event.room.name)
                    }
                    InputEmptyError ->{
                        binding.createRoomProgressBar.isVisible = false
                        snackbar(R.string.error_field_empty)
                    }
                    InputTooShortError ->{
                        binding.createRoomProgressBar.isVisible = false
                        snackbar(getString(R.string.error_room_name_too_short,Constants.MIN_ROOM_NAME_LENGTH))
                    }
                    InputTooLongError ->{
                        binding.createRoomProgressBar.isVisible = false
                        snackbar(getString(R.string.error_room_name_too_long,Constants.MAX_ROOM_NAME_LENGTH))
                    }
                    is CreateRoomErrorEvent ->{
                        binding.createRoomProgressBar.isVisible = false
                        snackbar(event.error)
                    }
                    is JoinRoomEvent ->{
                        binding.createRoomProgressBar.isVisible = false
                        findNavController().navigateSafely(
                            R.id.action_createRoomFragment_to_drawingActivity,
                            args = Bundle().apply {
                                putString("username", args.username)
                                putString("roomName",event.roomName)
                            }
                        )
                    }
                    is JoinRoomErrorEvent ->{
                        binding.createRoomProgressBar.isVisible = false
                        snackbar(event.error)
                    }
                }
            }
        }
    }

    private fun setupRoomSizeSpinner() {
        val roomSizes = resources.getStringArray(R.array.room_size_array)
        val adapter = ArrayAdapter(requireContext(), R.layout.textview_room_size, roomSizes)
        binding.tvMaxPersons.setAdapter(adapter)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}