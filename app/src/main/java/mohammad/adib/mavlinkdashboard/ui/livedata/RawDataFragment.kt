package mohammad.adib.mavlinkdashboard.ui.livedata

import android.graphics.Color
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.components.YAxis.AxisDependency
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import mohammad.adib.mavlinkdashboard.util.MavlinkComm
import mohammad.adib.mavlinkdashboard.MavlinkDashboardApp
import mohammad.adib.mavlinkdashboard.R
import mohammad.adib.mavlinkdashboard.databinding.FragmentRawDataBinding
import kotlin.math.roundToInt


class RawDataFragment : Fragment(), MavlinkComm.MavlinkListener {

    companion object {
        const val MAX_ENTRIES = 150
    }

    private lateinit var binding: FragmentRawDataBinding
    private lateinit var type: String
    private lateinit var key: String
    var count = 0
    var rawValue = 0f
    var min = Float.MAX_VALUE
    var max = Float.MIN_VALUE
    var lastValueT = -1L
    var firstValueT = -1L
    var hz = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRawDataBinding.inflate(layoutInflater)
        with(binding.pin) {
            setOnClickListener {
                if (MavlinkDashboardApp.getInstance().isPinned(type, key)) {
                    MavlinkDashboardApp.getInstance().unpinItem(type, key)
                } else {
                    MavlinkDashboardApp.getInstance().pinItem(type, key)
                }
                refreshPinnedState()
            }
        }
        with(binding.chart) {
            setTouchEnabled(false)
            axisRight.isEnabled = false
            xAxis.isEnabled = false
            axisLeft.textColor = Color.WHITE
            legend.isEnabled = false
            description.isEnabled = false
        }
        return binding.root
    }

    private fun refreshPinnedState() {
        binding.pin.setImageResource(
            if (MavlinkDashboardApp.getInstance().isPinned(type, key)) {
                R.drawable.pin_closed
            } else {
                R.drawable.pin_open
            }
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        type = arguments?.getString("type").orEmpty()
        key = arguments?.getString("key").orEmpty()
        MavlinkDashboardApp.getInstance().mavlinkComm.listeners.add(this)
        refreshPinnedState()
    }

    override fun onNewType() {
        // Safe to ignore
    }

    override fun onUpdate(type: String) {
        if (type == this.type) {
            activity?.runOnUiThread {
                count++
                val rawValueString =
                    MavlinkDashboardApp.getInstance().mavlinkComm.mavlinkData[type]?.get(key)
                        .toString()
                binding.data.text = rawValueString
                with(binding.chart) {
                    if (data == null) {
                        data = LineData().also {
                            it.addDataSet(createSet())
                            it.setDrawValues(false)
                        }
                    }
                    rawValueString.toFloatOrNull()?.let {
                        rawValue = it
                        min = rawValue.coerceAtMost(min)
                        max = rawValue.coerceAtLeast(max)
                        binding.data.text =
                            Html.fromHtml(
                                "<b>Value:</b> ${rawValue.roundToDecimals(4)} | <b>Min:</b> ${
                                    min.roundToDecimals(
                                        4
                                    )
                                } | <b>Max:</b> ${max.roundToDecimals(4)} | ${hz}<b>hz</b>"
                            )
                        if (lastValueT == -1L) {
                            lastValueT = System.currentTimeMillis()
                            firstValueT = lastValueT
                        }
                        val dT = System.currentTimeMillis() - firstValueT
                        hz = (count / (dT / 1000.0)).roundToInt()
                        with(data.getDataSetByIndex(0)) {
                            addEntry(Entry(dT.toFloat(), rawValue))
                            if (entryCount > MAX_ENTRIES) {
                                removeEntry(0)
                            }
                        }
                        data.notifyDataChanged()
                        notifyDataSetChanged()
                        invalidate()
                        lastValueT = System.currentTimeMillis()
                    }
                }
            }
        }
    }

    private fun createSet(): LineDataSet {
        val set = LineDataSet(null, key)
        set.lineWidth = 2.5f
        set.setDrawCircles(false)
        set.color = Color.WHITE
        set.highLightColor = Color.rgb(240, 99, 99)
        set.axisDependency = AxisDependency.LEFT
        set.valueTextSize = 10f
        return set
    }

    private fun Float.roundToDecimals(decimals: Int): Float {
        var dotAt = 1
        repeat(decimals) { dotAt *= 10 }
        val roundedValue = (this * dotAt).roundToInt()
        return (roundedValue / dotAt) + (roundedValue % dotAt).toFloat() / dotAt
    }
}