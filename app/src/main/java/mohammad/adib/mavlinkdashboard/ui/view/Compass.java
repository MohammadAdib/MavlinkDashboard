package mohammad.adib.mavlinkdashboard.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;

import com.google.gson.JsonObject;

import mohammad.adib.mavlinkdashboard.MavlinkDashboardApp;
import mohammad.adib.mavlinkdashboard.R;

public class Compass extends FrameLayout {

    private View vehicle;

    public Compass(Context context) {
        super(context);
        init();
    }

    public Compass(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public Compass(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.compass, this, true);
        vehicle = findViewById(R.id.vehicle);
    }

    public void onUpdate(JsonObject data) {
        double yaw = data.get("yaw").getAsDouble();
        float yawDegress = (float) Math.toDegrees(yaw);
        vehicle.setRotation(yawDegress);
    }
}
