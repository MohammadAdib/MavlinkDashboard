package mohammad.adib.mavlinkdashboard.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.LineChart
import mohammad.adib.mavlinkdashboard.MavlinkDashboardApp
import mohammad.adib.mavlinkdashboard.R

class PinnedLiveDataAdapter() : RecyclerView.Adapter<PinnedLiveDataAdapter.ViewHolder>() {

    private val dataSet = mutableListOf<String>().also {
        it.addAll(
            MavlinkDashboardApp.getInstance().mavlinkComm.historicalData.keys.toMutableList()
                .sorted()
        )
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val chart: LineChart = view.findViewById(R.id.chart)
        val name: TextView = view.findViewById(R.id.name)
        val value: TextView = view.findViewById(R.id.value)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.pinned_live_data_item, viewGroup, false)

        return ViewHolder(view)
    }

    fun refresh() {
        dataSet.clear()
        dataSet.addAll(
            MavlinkDashboardApp.getInstance().mavlinkComm.historicalData.keys.toMutableList()
                .sorted()
        )
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        with(viewHolder.chart) {
            setTouchEnabled(false)
            axisRight.isEnabled = false
            xAxis.isEnabled = false
            axisLeft.isEnabled = false
            legend.isEnabled = false
            description.isEnabled = false
            data = MavlinkDashboardApp.getInstance().mavlinkComm.historicalData[dataSet[position]]
        }
        viewHolder.name.text = dataSet[position].replace("#", ".")
        viewHolder.value.text =
            MavlinkDashboardApp.getInstance().mavlinkComm.mavlinkData[dataSet[position].split("#")[0]]?.get(
                dataSet[position].split("#")[1]
            ).toString()
    }

    override fun getItemCount() = dataSet.size
}