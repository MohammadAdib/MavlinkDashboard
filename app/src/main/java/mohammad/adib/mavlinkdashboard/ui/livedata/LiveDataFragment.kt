package mohammad.adib.mavlinkdashboard.ui.livedata

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import mohammad.adib.mavlinkdashboard.MavlinkComm
import mohammad.adib.mavlinkdashboard.MavlinkDashboardApp
import mohammad.adib.mavlinkdashboard.databinding.FragmentLiveDataBinding
import mohammad.adib.mavlinkdashboard.ui.adapter.RawDataPagerAdapter

class LiveDataFragment : Fragment(), MavlinkComm.MavlinkListener,
    LiveDataAdapter.OnSelectionChangedListener {

    private val adapter = LiveDataAdapter(MavlinkDashboardApp.getInstance().mavlinkComm, this)
    private lateinit var viewModel: LiveDataViewModel
    private var _binding: FragmentLiveDataBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this)[LiveDataViewModel::class.java]

        _binding = FragmentLiveDataBinding.inflate(inflater, container, false)
        val root: View = binding.root
        MavlinkDashboardApp.getInstance().mavlinkComm.listeners.add(this)

        with(binding) {
            content.visibility = View.GONE
            val layoutManager = LinearLayoutManager(requireContext())
            layoutManager.orientation = LinearLayoutManager.VERTICAL
            recycler.layoutManager = layoutManager
            recycler.adapter = adapter
        }
        return root
    }

    override fun onNewType() {
        activity?.runOnUiThread {
            adapter.refresh()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        MavlinkDashboardApp.getInstance().mavlinkComm.listeners.add(this)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onUpdate(type: String) {
        // Ignore
    }

    override fun onSelectionChanged(type: String) {
        binding.viewpager.adapter = RawDataPagerAdapter(type, childFragmentManager)
        binding.content.visibility = View.VISIBLE
    }
}