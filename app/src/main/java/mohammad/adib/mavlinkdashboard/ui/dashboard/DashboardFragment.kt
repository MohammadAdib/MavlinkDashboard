package mohammad.adib.mavlinkdashboard.ui.dashboard

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.VERTICAL
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import mohammad.adib.mavlinkdashboard.R
import mohammad.adib.mavlinkdashboard.databinding.FragmentDashboardBinding
import mohammad.adib.mavlinkdashboard.ui.activity.LiveDataActivity
import mohammad.adib.mavlinkdashboard.ui.adapter.PinnedLiveDataAdapter

class DashboardFragment : Fragment() {

    private lateinit var homeViewModel: DashboardViewModel
    private var _binding: FragmentDashboardBinding? = null

    private val binding get() = _binding!!
    private lateinit var map: MapboxMap
    private lateinit var mapView: MapView
    private lateinit var pointAnnotationManager: PointAnnotationManager
    private val pinnedLiveDataAdapter = PinnedLiveDataAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
            ViewModelProvider(this).get(DashboardViewModel::class.java)

        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root
        mapView = binding.mapView
        map = binding.mapView.getMapboxMap()
        val annotationApi = mapView.annotations
        pointAnnotationManager = annotationApi.createPointAnnotationManager()
        map.loadStyleUri(Style.SATELLITE_STREETS) {
            addAnnotationToMap()
            homeViewModel.location.observe(viewLifecycleOwner, {
                val annotation = pointAnnotationManager.annotations[0]
                val point = Point.fromLngLat(it.lon, it.lat)
                annotation.point = point
                pointAnnotationManager.update(annotation)
                mapView.getMapboxMap().setCamera(CameraOptions.Builder().center(point).build())
                pinnedLiveDataAdapter.refresh()
            })
            homeViewModel.heading.observe(viewLifecycleOwner, {
                val annotation = pointAnnotationManager.annotations[0]
                annotation.iconRotate = it + 45 - map.cameraState.bearing
                pointAnnotationManager.update(annotation)
            })
        }

        with(binding.pinnedLiveData) {
            val manager = LinearLayoutManager(requireContext())
            manager.orientation = VERTICAL
            layoutManager = manager
            adapter = pinnedLiveDataAdapter
        }

        homeViewModel.attitude.observe(viewLifecycleOwner, {
            binding.attitudeIndicator.onUpdate(it)
            binding.compassIndicator.onUpdate(it)
        })

        homeViewModel.mode.observe(viewLifecycleOwner, {
            binding.mode.text = it
        })

        binding.liveDataFab.setOnClickListener {
            activity?.startActivity(Intent(requireActivity(), LiveDataActivity::class.java))
        }
        return root
    }

    private fun addAnnotationToMap() {
        bitmapFromDrawableRes(
            requireContext(),
            R.drawable.glider
        )?.let {
            val pointAnnotationOptions: PointAnnotationOptions = PointAnnotationOptions()
                .withPoint(Point.fromLngLat(0.0, 0.0))
                .withIconImage(it)
            pointAnnotationManager.create(pointAnnotationOptions)
        }
    }

    private fun bitmapFromDrawableRes(context: Context, @DrawableRes resourceId: Int) =
        convertDrawableToBitmap(AppCompatResources.getDrawable(context, resourceId))

    private fun convertDrawableToBitmap(sourceDrawable: Drawable?): Bitmap? {
        if (sourceDrawable == null) {
            return null
        }
        return if (sourceDrawable is BitmapDrawable) {
            sourceDrawable.bitmap
        } else {
            val constantState = sourceDrawable.constantState ?: return null
            val drawable = constantState.newDrawable().mutate()
            val bitmap: Bitmap = Bitmap.createBitmap(
                drawable.intrinsicWidth, drawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            bitmap
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}