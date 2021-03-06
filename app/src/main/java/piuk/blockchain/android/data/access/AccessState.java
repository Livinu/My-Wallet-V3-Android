package piuk.blockchain.android.data.access;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.support.annotation.Nullable;

import info.blockchain.wallet.api.data.Settings;
import info.blockchain.wallet.api.data.WalletOptions;
import info.blockchain.wallet.metadata.Metadata;
import io.reactivex.subjects.AsyncSubject;
import io.reactivex.subjects.ReplaySubject;
import piuk.blockchain.android.data.rxjava.RxBus;
import piuk.blockchain.android.ui.auth.LogoutActivity;
import piuk.blockchain.android.ui.base.BaseAuthActivity;
import piuk.blockchain.android.util.PrefsUtil;


public class AccessState {

    public static final String LOGOUT_ACTION = "info.blockchain.wallet.LOGOUT";

    private static final int SHOW_BTC = 1;
    private static final int SHOW_FIAT = 2;
    private static final long LOGOUT_TIMEOUT_MILLIS = 1000L * 30L;

    private static AccessState instance;

    private PrefsUtil prefs;
    private RxBus rxBus;

    private String pin;
    private PendingIntent logoutPendingIntent;
    private boolean isLoggedIn = false;
    private boolean canAutoLogout = true;

    public void initAccessState(Context context, PrefsUtil prefs, RxBus rxBus) {
        this.prefs = prefs;
        this.rxBus = rxBus;

        Intent intent = new Intent(context, LogoutActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.setAction(AccessState.LOGOUT_ACTION);
        logoutPendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
    }

    public static AccessState getInstance() {
        if (instance == null)
            instance = new AccessState();
        return instance;
    }

    public void setPIN(@Nullable String pin) {
        this.pin = pin;
    }

    public String getPIN() {
        return pin;
    }

    /**
     * Called from {@link BaseAuthActivity#onPause()}
     */
    public void startLogoutTimer(Context context) {
        if (canAutoLogout) {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + LOGOUT_TIMEOUT_MILLIS, logoutPendingIntent);
        }
    }

    /**
     * Called from {@link BaseAuthActivity#onResume()}
     */
    public void stopLogoutTimer(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(logoutPendingIntent);
    }

    public void logout(Context context) {
        pin = null;
        Intent intent = new Intent(context, LogoutActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.setAction(LOGOUT_ACTION);
        context.startActivity(intent);
    }

    public boolean isBtc() {
        final int balanceDisplayState = prefs.getValue(PrefsUtil.KEY_BALANCE_DISPLAY_STATE, SHOW_BTC);
        return balanceDisplayState != SHOW_FIAT;
    }

    public void setIsBtc(boolean isBtc) {
        prefs.setValue(PrefsUtil.KEY_BALANCE_DISPLAY_STATE, isBtc ? SHOW_BTC : SHOW_FIAT);
    }

    public boolean isLoggedIn() {
        return isLoggedIn;
    }

    public void setIsLoggedIn(boolean loggedIn) {
        prefs.logIn();
        isLoggedIn = loggedIn;
        if (isLoggedIn) {
            rxBus.emitEvent(AuthEvent.class, AuthEvent.LOGIN);
        } else {
            rxBus.emitEvent(AuthEvent.class, AuthEvent.LOGOUT);
        }
    }

    public void unpairWallet() {
        rxBus.emitEvent(AuthEvent.class, AuthEvent.UNPAIR);
    }

    public void disableAutoLogout() {
        canAutoLogout = false;
    }

    public void enableAutoLogout() {
        canAutoLogout = true;
    }
}
