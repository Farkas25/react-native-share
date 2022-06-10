package cl.json.social;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.os.Build;

import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactContext;

/**
 * Receiver to record the chosen component when sharing an Intent.
 */
public class TargetChosenReceiver extends BroadcastReceiver {
    private static final Object LOCK = new Object();

    private static String sTargetChosenReceiveAction;

    private static Callback successCallback;
    private static Callback failureCallback;

    public static boolean isSupported() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1;
    }

    public static void registerCallbacks(Callback success, Callback failure) {
        successCallback = success;
        failureCallback = failure;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP_MR1)
    public static IntentSender getSharingSenderIntent(ReactContext reactContext) {
        synchronized (LOCK) {
            if (sTargetChosenReceiveAction == null) {
                sTargetChosenReceiveAction = reactContext.getPackageName() + "/" + TargetChosenReceiver.class.getName() + "_ACTION";
            }
        }

        Intent intent = new Intent(sTargetChosenReceiveAction);
        intent.setPackage(reactContext.getPackageName());
        intent.setClass(reactContext.getApplicationContext(), TargetChosenReceiver.class);
        final PendingIntent callback = PendingIntent.getBroadcast(reactContext, 0, intent,
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ?
                PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE :
                PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_ONE_SHOT);

        return callback.getIntentSender();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        ComponentName target = intent.getParcelableExtra(Intent.EXTRA_CHOSEN_COMPONENT);
        if (target != null) {
            sendCallback(true, true, target.flattenToString());
        } else {
            sendCallback(true, true, "OK");
        }
    }

    public static void sendCallback(boolean isSuccess, Object... reply) {
        if (isSuccess) {
            if (successCallback != null) {
                successCallback.invoke(reply);
            }
        } else if (failureCallback != null) {
            failureCallback.invoke(reply);
        }
        successCallback = null;
        failureCallback = null;
    }
}
