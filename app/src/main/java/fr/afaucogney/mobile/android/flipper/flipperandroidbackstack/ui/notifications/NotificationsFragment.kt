package fr.afaucogney.mobile.android.flipper.flipperandroidbackstack.ui.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import fr.afaucogney.mobile.android.flipper.flipperandroidbackstack.R
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class NotificationsFragment : Fragment() {

    private lateinit var notificationsViewModel: NotificationsViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        notificationsViewModel =
            ViewModelProvider(this).get(NotificationsViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_notifications, container, false)
        val textView: TextView = root.findViewById(R.id.text_notifications)
        val textViewState: TextView = root.findViewById(R.id.text_notifications_state)
        val textViewShared: TextView = root.findViewById(R.id.text_notifications_shared)

        notificationsViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })
        notificationsViewModel.state.onEach {
            textViewState.text = it
        }.launchIn(lifecycleScope)

        notificationsViewModel.shared.onEach {
            textViewShared.text = it
        }.launchIn(lifecycleScope)
        return root
    }
}