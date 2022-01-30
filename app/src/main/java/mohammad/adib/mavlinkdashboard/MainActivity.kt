package mohammad.adib.mavlinkdashboard

import android.os.Bundle
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.VERTICAL
import mohammad.adib.mavlinkdashboard.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity(), MavlinkComm.MavlinkListener,
    RawDataAdapter.OnSelectionChangedListener {

    private lateinit var binding: ActivityMainBinding
    private val adapter = RawDataAdapter(MavlinkDashboardApp.getInstance().mavlinkComm, this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        MavlinkDashboardApp.getInstance().mavlinkComm.listeners.add(this)

        with(binding) {
            content.visibility = GONE
            val layoutManager = LinearLayoutManager(this@MainActivity)
            layoutManager.orientation = VERTICAL
            recycler.layoutManager = layoutManager
            recycler.adapter = adapter
        }
    }

    override fun onNewType() {
        runOnUiThread {
            adapter.refresh()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        MavlinkDashboardApp.getInstance().mavlinkComm.listeners.add(this)
    }

    override fun onUpdate(type: String) {
        // Ignore
    }

    private inner class RawDataPagerAdapter(
        private val type: String,
        private val fm: FragmentManager
    ) : FragmentStatePagerAdapter(fm) {
        val dataSet =
            MavlinkDashboardApp.getInstance().mavlinkComm.mavlinkData[type]?.keySet()?.toList()

        override fun getCount(): Int = dataSet?.size ?: 0

        override fun getPageTitle(position: Int): CharSequence {
            return dataSet?.get(position).orEmpty()
        }

        override fun getItem(position: Int): Fragment {
            val fragment = RawDataFragment()
            fragment.arguments = Bundle().apply {
                putString("type", type)
                putString(
                    "key",
                    MavlinkDashboardApp.getInstance().mavlinkComm.mavlinkData[type]?.keySet()
                        ?.toList()?.get(position).orEmpty()
                )
            }
            return fragment
        }
    }

    override fun onSelectionChanged(type: String) {
        binding.viewpager.adapter = RawDataPagerAdapter(type, supportFragmentManager)
        binding.content.visibility = VISIBLE
    }
}