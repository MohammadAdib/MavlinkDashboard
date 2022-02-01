package mohammad.adib.mavlinkdashboard.ui.adapter

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import mohammad.adib.mavlinkdashboard.MavlinkDashboardApp
import mohammad.adib.mavlinkdashboard.ui.livedata.RawDataFragment

public class RawDataPagerAdapter(
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