package mohammad.adib.mavlinkdashboard.util

import android.annotation.SuppressLint
import android.graphics.Color
import android.util.Log
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import io.dronefleet.mavlink.Mavlink2Message
import io.dronefleet.mavlink.MavlinkConnection
import io.dronefleet.mavlink.MavlinkMessage
import io.dronefleet.mavlink.common.Heartbeat
import mohammad.adib.mavlinkdashboard.MavlinkDashboardApp
import mohammad.adib.mavlinkdashboard.ui.livedata.RawDataFragment
import java.io.IOException
import java.io.OutputStream
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetSocketAddress
import java.net.SocketAddress
import java.util.concurrent.Executors


class MavlinkComm {

    var lastHeartbeat: Long = -1
    var mavlinkData = HashMap<String, JsonObject>()
    var historicalData = HashMap<String, LineData>()
    var updates = HashMap<String, Long>()
    var type = "UNKNOWN"
    private val gson: Gson =
        GsonBuilder().serializeSpecialFloatingPointValues().serializeNulls().create()
    var listeners = mutableListOf<MavlinkListener>()

    @SuppressLint("NewApi")
    fun start(deviceIP: String, hostIP: String, port: Int) {
        try {
            val localAddress: SocketAddress = InetSocketAddress(deviceIP, port)
            val remoteAddress: SocketAddress = InetSocketAddress(hostIP, port)
            val bufferSize = 65535
            val datagramSocket = DatagramSocket(localAddress)
            val udpIn = PipedInputStream()
            val udpOut: OutputStream = object : OutputStream() {
                val buffer = ByteArray(bufferSize)
                var position = 0

                @Throws(IOException::class)
                override fun write(i: Int) {
                    write(byteArrayOf(i.toByte()), 0, 1)
                }

                @Synchronized
                @Throws(IOException::class)
                override fun write(b: ByteArray, off: Int, len: Int) {
                    if (position + len > buffer.size) {
                        flush()
                    }
                    System.arraycopy(b, off, buffer, position, len)
                    position += len
                }

                @Synchronized
                @Throws(IOException::class)
                override fun flush() {
                    val packet = DatagramPacket(buffer, 0, position, remoteAddress)
                    datagramSocket.send(packet)
                    position = 0
                }
            }
            val appOut = PipedOutputStream(udpIn)
            val service = Executors.newSingleThreadExecutor()
            service.execute {
                try {
                    val packet = DatagramPacket(ByteArray(bufferSize), bufferSize)
                    while (!datagramSocket.isClosed) {
                        datagramSocket.receive(packet)
                        appOut.write(packet.data, packet.offset, packet.length)
                        appOut.flush()
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                } finally {
                    try {
                        appOut.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                    if (!datagramSocket.isClosed) {
                        datagramSocket.close()
                    }
                    if (!service.isShutdown) {
                        service.shutdown()
                    }
                }
            }
            val connection = MavlinkConnection.create(udpIn, udpOut)
            var message: MavlinkMessage<*>
            while (connection.next().also { message = it } != null) {
                if (message is Mavlink2Message<*>) {
                    val message2 = message as Mavlink2Message<*>
                    if (!message2.isSigned) {
                        val type = message2.payload.javaClass.simpleName
                        val size = mavlinkData.size
                        val json = gson.toJsonTree(message2.payload).asJsonObject
                        if (message2.payload.toString().contains("MAV_TYPE_GCS")) continue
                        mavlinkData[type] = json
                        if (mavlinkData.size > size) {
                            listeners.forEach {
                                it.onNewType()
                            }
                        }
                        listeners.forEach {
                            it.onUpdate(type)
                        }
                        json.entrySet().forEach {
                            if (MavlinkDashboardApp.getInstance().isPinned(type, it.key)) {
                                it.value.toString().toFloatOrNull()?.let { rawValue ->
                                    val pinnedKey = "$type#${it.key}"
                                    if (!historicalData.containsKey(pinnedKey)) {
                                        historicalData[pinnedKey] = LineData().also { lineData ->
                                            lineData.addDataSet(createSet(it.key))
                                            lineData.setDrawValues(false)
                                        }
                                        updates[pinnedKey] = System.currentTimeMillis()
                                    }
                                    historicalData[pinnedKey]?.getDataSetByIndex(0)?.apply {
                                        if (entryCount > RawDataFragment.MAX_ENTRIES) {
                                            removeEntry(0)
                                        }
                                        addEntry(
                                            Entry(
                                                (System.currentTimeMillis() - (updates[pinnedKey]
                                                    ?: 0)).toFloat(),
                                                rawValue
                                            )
                                        )
                                    }
                                    historicalData[pinnedKey]?.notifyDataChanged()
                                }
                            }
                        }
                    }
                } else {
                    println("Mavlink1: " + message.payload.toString())
                }
                if (message.payload is Heartbeat) {
                    val heartbeatMessage = message as MavlinkMessage<Heartbeat>
                    lastHeartbeat = System.currentTimeMillis()
                    Log.d("Heartbeat", heartbeatMessage.payload.toString())
                    val json = gson.toJsonTree(message.payload).asJsonObject
                    type = json.get("type").asJsonObject.get("entry").asString
                    Log.d("Type", type)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun createSet(key: String): LineDataSet {
        val set = LineDataSet(null, key)
        set.lineWidth = 2.5f
        set.setDrawCircles(false)
        set.color = Color.WHITE
        set.highLightColor = Color.rgb(240, 99, 99)
        set.axisDependency = YAxis.AxisDependency.LEFT
        set.valueTextSize = 10f
        return set
    }

    interface MavlinkListener {
        fun onNewType()
        fun onUpdate(type: String)
    }
}