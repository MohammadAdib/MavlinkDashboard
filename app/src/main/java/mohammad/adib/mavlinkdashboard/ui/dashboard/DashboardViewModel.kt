package mohammad.adib.mavlinkdashboard.ui.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.JsonObject
import mohammad.adib.mavlinkdashboard.MavlinkDashboardApp
import mohammad.adib.mavlinkdashboard.util.MavlinkComm
import mohammad.adib.mavlinkdashboard.util.ModeHelper

class DashboardViewModel : ViewModel() {

    companion object {
        private const val ATTITUDE = "Attitude"
        private const val GPS_RAW_INT = "GpsRawInt"
        private const val HEARTBEAT = "Heartbeat"
        private const val LAT = "lat"
        private const val LON = "lon"
        private const val ALT = "alt"
        private const val YAW = "yaw"
        private const val CUSTOM_MODE = "customMode"
        private const val FIX_TYPE = "fixType"
        private const val VALUE = "value"
    }

    private val listener = object : MavlinkComm.MavlinkListener {
        override fun onNewType() {
            // Ignore
        }

        override fun onUpdate(type: String) {
            val data = getMavlinkData()[type]
            when (type) {
                GPS_RAW_INT -> {
                    _location.postValue(
                        LocationMetadata(
                            (data?.get(LAT)?.asLong ?: 0) / 10000000.0,
                            (data?.get(LON)?.asLong ?: 0) / 10000000.0,
                            data?.get(ALT)?.asInt ?: 0,
                            data?.getAsJsonObject(FIX_TYPE)?.get(VALUE)?.asInt ?: 0
                        )
                    )
                }
                ATTITUDE -> {
                    data?.get(YAW)?.asDouble?.let {
                        _heading.postValue(Math.toDegrees(it))
                    }
                    data?.let { _attitude.postValue(it) }
                }
                HEARTBEAT -> {
                    data?.get(CUSTOM_MODE)?.asInt?.let {
                        _mode.postValue(
                            ModeHelper.getMode(
                                it,
                                MavlinkDashboardApp.getInstance().mavlinkComm.type
                            )
                        )
                    }
                }
            }
        }
    }

    private val _location = MutableLiveData<LocationMetadata>()
    val location: LiveData<LocationMetadata> = _location

    private val _heading = MutableLiveData<Double>()
    val heading: LiveData<Double> = _heading

    private val _attitude = MutableLiveData<JsonObject>()
    val attitude: LiveData<JsonObject> = _attitude

    private val _mode = MutableLiveData<String>()
    val mode: LiveData<String> = _mode

    private fun getMavlinkData(): HashMap<String, JsonObject> {
        return MavlinkDashboardApp.getInstance().mavlinkComm.mavlinkData
    }

    init {
        MavlinkDashboardApp.getInstance().mavlinkComm.listeners.add(listener)
    }

    override fun onCleared() {
        super.onCleared()
        MavlinkDashboardApp.getInstance().mavlinkComm.listeners.remove(listener)
    }
}