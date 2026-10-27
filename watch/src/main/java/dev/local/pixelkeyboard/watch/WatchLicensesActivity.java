package dev.local.pixelkeyboard.watch;
import android.widget.ScrollView;
public final class WatchLicensesActivity extends dev.local.pixelkeyboard.ui.LicensesActivity {
    @Override protected ScrollView createScrollView(){return new RotaryScrollView(this);}
}
