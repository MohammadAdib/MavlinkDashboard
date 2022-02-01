package mohammad.adib.mavlinkdashboard.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import mohammad.adib.mavlinkdashboard.MavlinkComm;
import mohammad.adib.mavlinkdashboard.MavlinkDashboardApp;
import mohammad.adib.mavlinkdashboard.R;

public class Compass extends FrameLayout implements MavlinkComm.MavlinkListener {

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
        MavlinkDashboardApp.getInstance().mavlinkComm.getListeners().add(this);
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.compass, this, true);
        vehicle = findViewById(R.id.vehicle);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        MavlinkDashboardApp.getInstance().mavlinkComm.getListeners().remove(this);
    }

    @Override
    public void onNewType() {
        // Ignore
    }

    @Override
    public void onUpdate(@NonNull String type) {
        if (type.equals("Attitude")) {
            double yaw = MavlinkDashboardApp.getInstance().mavlinkComm.getMavlinkData().get(type).get("yaw").getAsDouble();
            float yawDegress = (float) Math.toDegrees(yaw);
            vehicle.setRotation(yawDegress + 45f);
        }
    }
}
