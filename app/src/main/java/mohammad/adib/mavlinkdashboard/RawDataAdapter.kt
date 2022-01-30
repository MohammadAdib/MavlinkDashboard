package mohammad.adib.mavlinkdashboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class RawDataAdapter(
    private val mavlinkComm: MavlinkComm,
    private val listener: OnSelectionChangedListener
) :
    RecyclerView.Adapter<RawDataAdapter.ViewHolder>() {

    private val dataSet = mutableListOf<String>().also {
        it.addAll(mavlinkComm.mavlinkData.keys.toMutableList().sorted())
    }
    var selectedIndex = -1

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(R.id.label)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.raw_item, viewGroup, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.textView.text = dataSet[position]
        viewHolder.textView.setOnClickListener { selectIndex(position) }
        viewHolder.textView.setBackgroundResource(if (selectedIndex != position) R.color.brandDark else R.color.brand)
    }

    private fun selectIndex(position: Int) {
        if (selectedIndex != position) {
            selectedIndex = position
            listener.onSelectionChanged(getSelectedType())
            notifyDataSetChanged()
        }
    }

    fun refresh() {
        dataSet.clear()
        dataSet.addAll(mavlinkComm.mavlinkData.keys.toMutableList().sorted())
        notifyDataSetChanged()
    }

    fun getSelectedType() = if (selectedIndex == -1) "" else dataSet[selectedIndex]

    override fun getItemCount() = dataSet.size

    interface OnSelectionChangedListener {
        fun onSelectionChanged(type: String)
    }

}