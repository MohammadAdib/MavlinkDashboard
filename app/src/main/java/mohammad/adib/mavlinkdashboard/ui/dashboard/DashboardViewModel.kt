package mohammad.adib.mavlinkdashboard.ui.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.JsonObject
import mohammad.adib.mavlinkdashboard.MavlinkComm
import mohammad.adib.mavlinkdashboard.MavlinkDashboardApp

class DashboardViewModel : ViewModel(), MavlinkComm.MavlinkListener {

    companion object {
        private const val ATTITUDE = "Attitude"
        private const val GPS_RAW_INT = "GpsRawInt"
        private const val LAT = "lat"
        private const val LON = "lon"
        private const val ALT = "alt"
        private const val FIX_TYPE = "fixType"
        private const val VALUE = "value"
    }

    private val _location = MutableLiveData<LocationMetadata>()
    val location: LiveData<LocationMetadata> = _location

    private val _heading = MutableLiveData<Double>()
    val heading: LiveData<Double> = _heading

    private fun getMavlinkData(): HashMap<String, JsonObject> {
        return MavlinkDashboardApp.getInstance().mavlinkComm.mavlinkData
    }

    init {
        MavlinkDashboardApp.getInstance().mavlinkComm.listeners.add(this)
    }

    override fun onCleared() {
        super.onCleared()
        MavlinkDashboardApp.getInstance().mavlinkComm.listeners.remove(this)
    }

    override fun onNewType() {
        // Ignore
    }

    override fun onUpdate(type: String) {
        val data = getMavlinkData()[type]
        if (type == GPS_RAW_INT) {
            _location.postValue(
                LocationMetadata(
                    (data?.get(LAT)?.asLong ?: 0) / 10000000.0,
                    (data?.get(LON)?.asLong ?: 0) / 10000000.0,
                    data?.get(ALT)?.asInt ?: 0,
                    data?.getAsJsonObject(FIX_TYPE)?.get(VALUE)?.asInt ?: 0
                )
            )
        }
        if (type == ATTITUDE) {
            data?.get("yaw")?.asDouble?.let {
                _heading.postValue(Math.toDegrees(it))
            }
        }
    }
}