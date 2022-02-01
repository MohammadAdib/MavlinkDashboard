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

public class AttitudeIndicator extends FrameLayout implements MavlinkComm.MavlinkListener {

    private View main;
    private View rollIndicator;
    private View ground;

    public AttitudeIndicator(Context context) {
        super(context);
        init();
    }

    public AttitudeIndicator(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AttitudeIndicator(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        MavlinkDashboardApp.getInstance().mavlinkComm.getListeners().add(this);
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.attitude_indicator, this, true);
        main = findViewById(R.id.main);
        ground = findViewById(R.id.ground);
        rollIndicator = findViewById(R.id.roll);
        main.setScaleX(5);
        main.setScaleY(5);
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
            String pitchString = MavlinkDashboardApp.getInstance().mavlinkComm.getMavlinkData().get(type).get("pitch").toString();
            String rollString = MavlinkDashboardApp.getInstance().mavlinkComm.getMavlinkData().get(type).get("roll").toString();
            double pitch = Double.parseDouble(pitchString);
            double roll = Double.parseDouble(rollString);
            int halfHeight = getHeight() / 2;
            double transY = (halfHeight * (Math.toDegrees(pitch) / 45)) / 3.0;
            ground.setTranslationY((float) Math.max(-getHeight(), Math.min(getHeight(), transY)));
            main.setRotation((float) -Math.toDegrees(roll));
            rollIndicator.setRotation((float) -Math.toDegrees(roll));
        }
    }
}
