package com.squall.doodlekong_android.ui.drawing

import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.navArgs
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.squall.doodlekong_android.R
import com.squall.doodlekong_android.data.remote.ws.models.GameError
import com.squall.doodlekong_android.data.remote.ws.models.JoinRoomHandshake
import com.squall.doodlekong_android.databinding.ActivityDrawingBinding
import com.squall.doodlekong_android.util.Constants
import com.tinder.scarlet.WebSocket
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class DrawingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDrawingBinding
    private val viewModel by viewModels<DrawingViewModel>()

    private val args by navArgs<DrawingActivityArgs>()

    @Inject
    lateinit var clientId:String

    private lateinit var toggle: ActionBarDrawerToggle

    private lateinit var rvPlayers: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDrawingBinding.inflate(layoutInflater)
        setContentView(binding.root)


        subscribeToUiStateUpdates()
        listenToConnectionEvents()
        listenToSocketEvents()

        toggle = ActionBarDrawerToggle(this, binding.root, R.string.open, R.string.close).apply {
            syncState()
        }

        val header = layoutInflater.inflate(R.layout.nav_drawer_header, binding.navView)
        rvPlayers = header.findViewById<RecyclerView>(R.id.rvPlayers)
        binding.root.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)

        binding.ibPlayers.setOnClickListener {
            binding.root.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
            binding.root.openDrawer(GravityCompat.START)
        }

        binding.root.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) = Unit

            override fun onDrawerOpened(drawerView: View): Unit = Unit

            override fun onDrawerClosed(drawerView: View) {
                binding.root.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
            }

            override fun onDrawerStateChanged(newState: Int): Unit = Unit
        })

        binding.apply {
            colorGroup.setOnCheckedChangeListener { _, checkedId ->
                viewModel.checkRadioButton(checkedId)
            }
        }
    }

    private fun selectColor(color: Int): Unit {
        binding.apply {
            drawingView.setColor(color)
            drawingView.setThickness(Constants.DEFAULT_PAINT_THICKNESS)
        }
    }

    private fun subscribeToUiStateUpdates() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.selectedColorButtonId.collect { id ->
                    binding.apply {
                        colorGroup.check(id)
                        when (id) {
                            R.id.rbBlack -> selectColor(Color.BLACK)
                            R.id.rbBlue -> selectColor(Color.BLUE)
                            R.id.rbGreen -> selectColor(Color.GREEN)
                            R.id.rbOrange -> selectColor(
                                ContextCompat.getColor(
                                    this@DrawingActivity,
                                    R.color.orange
                                )
                            )
                            R.id.rbRed -> selectColor(Color.RED)
                            R.id.rbYellow -> selectColor(Color.YELLOW)
                            R.id.rbEraser -> {
                                drawingView.setColor(Color.WHITE)
                                drawingView.setThickness(40f)
                            }
                        }
                    }
                }

            }
        }
        lifecycleScope.launchWhenStarted {
            viewModel.connectionProgressBarVisible.collect { isVisible ->
                binding.connectionProgressBar.isVisible = isVisible
            }
        }
        lifecycleScope.launchWhenStarted {
            viewModel.chooseWordOverlayVisible.collect { isVisible ->
                binding.chooseWordOverlay.isVisible = isVisible
            }
        }
    }

    private fun listenToSocketEvents() = lifecycleScope.launchWhenStarted {
        viewModel.socketEvent.collect { event ->
            when (event) {
                is DrawingViewModel.SocketEvent.GameErrorEvent -> {
                    when (event.data.errorType) {
                        GameError.ERROR_ROOM_NOT_FOUND -> finish()
                    }
                }
                else -> Unit
            }
        }
    }

    private fun listenToConnectionEvents() = lifecycleScope.launchWhenStarted {
        viewModel.connectionEvent.collect { event ->
            when (event) {
                is WebSocket.Event.OnConnectionOpened<*> -> {
                    viewModel.sendBaseModel(
                        JoinRoomHandshake(
                            args.username,args.roomName,clientId
                        )
                    )
                    viewModel.setConnectionProgressBarVisibility(false)
                }
                is WebSocket.Event.OnConnectionFailed -> {
                    viewModel.setConnectionProgressBarVisibility(false)
                    Snackbar.make(
                        binding.root,
                        R.string.error_connection_failed,
                        Snackbar.LENGTH_LONG
                    ).show()
                    event.throwable.printStackTrace()
                }
                is WebSocket.Event.OnConnectionClosed -> {
                    viewModel.setConnectionProgressBarVisibility(false)

                }
                else -> Unit
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}