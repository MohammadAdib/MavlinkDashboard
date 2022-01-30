package mohammad.adib.mavlinkdashboard

import android.annotation.SuppressLint
import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import io.dronefleet.mavlink.Mavlink2Message
import io.dronefleet.mavlink.MavlinkConnection
import io.dronefleet.mavlink.MavlinkMessage
import io.dronefleet.mavlink.common.Heartbeat
import java.io.IOException
import java.io.OutputStream
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetSocketAddress
import java.net.SocketAddress
import java.util.*
import java.util.concurrent.Executors


class MavlinkComm {

    var lastHeartbeat: Long = -1
    var mavlinkData = HashMap<String, JsonObject>()
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
                        mavlinkData[type] = gson.toJsonTree(message2.payload).asJsonObject
                        if (mavlinkData.size > size) {
                            listeners.forEach {
                                it.onNewType()
                            }
                        }
                        listeners.forEach {
                            it.onUpdate(type)
                        }
                    }
                } else {
                    println("Mavlink1: " + message.payload.toString())
                }
                if (message.payload is Heartbeat) {
                    val heartbeatMessage = message as MavlinkMessage<Heartbeat>
                    lastHeartbeat = System.currentTimeMillis()
                    Log.d("Heartbeat", heartbeatMessage.payload.toString())
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    interface MavlinkListener {
        fun onNewType()
        fun onUpdate(type: String)
    }
}