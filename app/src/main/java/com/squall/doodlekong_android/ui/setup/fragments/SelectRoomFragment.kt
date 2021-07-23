package com.squall.doodlekong_android.ui.setup.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.squall.doodlekong_android.R
import com.squall.doodlekong_android.adapters.RoomAdapter
import com.squall.doodlekong_android.databinding.FragmentSelectRoomBinding
import com.squall.doodlekong_android.ui.setup.SelectRoomViewModel
import com.squall.doodlekong_android.ui.setup.SelectRoomViewModel.SetupEvent.*
import com.squall.doodlekong_android.util.Constants.SEARCH_DELAY
import com.squall.doodlekong_android.util.navigateSafely
import com.squall.doodlekong_android.util.snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SelectRoomFragment : Fragment() {

    private var _binding: FragmentSelectRoomBinding? = null
    private val binding get() = _binding!!

    private val viewModel by viewModels<SelectRoomViewModel>()

    private val args by navArgs<SelectRoomFragmentArgs>()

    @Inject
    lateinit var roomAdapter: RoomAdapter

    private var updateRoomsJob:Job? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSelectRoomBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        subscribeToObservers()
        listenToEvents()

        viewModel.getRooms("")


        var searchJob: Job? = null
        binding.etRoomName.addTextChangedListener {
            searchJob?.cancel()
            searchJob = lifecycleScope.launch {
                delay(SEARCH_DELAY)
                viewModel.getRooms(it.toString())
            }
        }

        binding.apply {
            ibReload.setOnClickListener {
                roomsProgressBar.isVisible = true
                ivNoRoomsFound.isVisible = false
                tvNoRoomsFound.isVisible = false
                viewModel.getRooms(etRoomName.text.toString())
            }
        }

        binding.btnCreateRoom.setOnClickListener {
            findNavController().navigateSafely(
                R.id.action_selectRoomFragment_to_createRoomFragment,
                Bundle().apply { putString("username",args.username) }
            )
        }
        roomAdapter.setOnRoomClickListener {
            viewModel.joinRoom(args.username, it.name)
        }
    }

    private fun listenToEvents() = lifecycleScope.launchWhenStarted {
        viewModel.setupEvent.collect { event ->
            when (event) {
                is JoinRoomEvent -> {
                    findNavController().navigateSafely(
                        R.id.action_selectRoomFragment_to_drawingActivity,
                        args = Bundle().apply {
                            putString("username", args.username)
                            putString("roomName", event.roomName)
                        }
                    )
                }
                is JoinRoomErrorEvent -> {
                    snackbar(event.error)
                }
                is GetRoomErrorEvent -> {
                    binding.apply {
                        roomsProgressBar.isVisible = false
                        tvNoRoomsFound.isVisible = false
                        ivNoRoomsFound.isVisible = false
                    }
                    snackbar(event.error)
                }
                else -> Unit
            }
        }
    }

    private fun subscribeToObservers() = lifecycleScope.launchWhenStarted {
        viewModel.rooms.collect { event ->
            when (event) {
                GetRoomLoadingEvent -> {
                    binding.roomsProgressBar.isVisible = true
                }
                is GetRoomEvent -> {
                    binding.roomsProgressBar.isVisible = false
                    val isRoomsEmpty = event.rooms.isEmpty()
                    binding.tvNoRoomsFound.isVisible = isRoomsEmpty
                    binding.ivNoRoomsFound.isVisible = isRoomsEmpty
                    updateRoomsJob?.cancel()
                    updateRoomsJob = lifecycleScope.launch {
                        roomAdapter.updateDataset(event.rooms)
                    }
                }
                else -> Unit
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupRecyclerView() {
        binding.rvRooms.apply {
            adapter = roomAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }
}