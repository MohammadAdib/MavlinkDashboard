package mohammad.adib.mavlinkdashboard.util

object ModeHelper {

    const val UNKNOWN = "Unknown mode"

    fun getMode(mode: Int, type: String): String {
        return when (type) {
            "MAV_TYPE_FIXED_WING" -> getFixedWingMode(mode)
            else -> "Unknown mode"
        }
    }

    private fun getFixedWingMode(mode: Int): String {
        val modes = listOf(
            "Manual",
            "Circle",
            "Stabilize",
            "Training",
            "Acro",
            "FBW A",
            "FBW B",
            "Cruise",
            "Autotune",
            "",
            "Auto",
            "RTL",
            "Loiter",
            "Takeoff",
            "",
            "Guided",
            "Quadplane Stabilize",
            "Quadplane Hover",
            "Quadplane Loiter",
            "Quadplane Land",
            "Quadplane RTL",
            "Quadplane Autotune",
            "Quadplane Acro",
            "Thermal",
        )
        if (mode < modes.size)
            return modes[mode]
        else
            return UNKNOWN
    }
}