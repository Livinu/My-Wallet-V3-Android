package piuk.blockchain.android.data.datamanagers;

import info.blockchain.wallet.api.data.Settings;
import info.blockchain.wallet.settings.SettingsManager;

import java.util.List;

import io.reactivex.Observable;
import piuk.blockchain.android.data.rxjava.RxUtil;
import piuk.blockchain.android.data.services.SettingsService;

public class SettingsDataManager {

    private SettingsService settingsService;

    public SettingsDataManager(SettingsService settingsService) {
        this.settingsService = settingsService;
    }

    /**
     * Updates the settings object by syncing it with the server
     *
     * @param guid      The user's GUID
     * @param sharedKey The shared key
     * @return {@link Observable<Settings>} wrapping the Settings object
     */
    public Observable<Settings> initSettings(String guid, String sharedKey) {
        settingsService.initSettings(guid, sharedKey);
        return fetchSettings();
    }

    /**
     * Update the user's email and fetches an updated {@link Settings} object.
     *
     * @param email The email to be stored
     * @return {@link Observable<Settings>} wrapping the Settings object
     */
    public Observable<Settings> updateEmail(String email) {
        return settingsService.updateEmail(email)
                .flatMap(responseBody -> fetchSettings());
    }

    /**
     * Update the user's phone number and fetches an updated {@link Settings} object.
     *
     * @param sms The phone number to be stored
     * @return {@link Observable<Settings>} wrapping the Settings object
     */
    public Observable<Settings> updateSms(String sms) {
        return settingsService.updateSms(sms)
                .flatMap(responseBody -> fetchSettings());
    }

    /**
     * Verify the user's phone number with a verification code and fetches an updated {@link
     * Settings} object.
     *
     * @param code The verification code
     * @return {@link Observable<Settings>} wrapping the Settings object
     */
    public Observable<Settings> verifySms(String code) {
        return settingsService.verifySms(code)
                .flatMap(responseBody -> fetchSettings());
    }

    /**
     * Update the user's Tor blocking preference and fetches an updated {@link Settings} object.
     *
     * @param blocked The user's preference for blocking Tor requests
     * @return {@link Observable<Settings>} wrapping the Settings object
     */
    public Observable<Settings> updateTor(boolean blocked) {
        return settingsService.updateTor(blocked)
                .flatMap(responseBody -> fetchSettings());
    }

    /**
     * Update the user's two factor status
     *
     * @param authType The auth type being used for 2FA
     * @return {@link Observable<Settings>} wrapping the Settings object
     * @see SettingsManager for notification types
     */
    public Observable<Settings> updateTwoFactor(int authType) {
        return settingsService.updateTwoFactor(authType)
                .flatMap(responseBody -> fetchSettings());
    }

    /**
     * Update the user's notification preferences and fetches an updated {@link Settings} object.
     *
     * @param notificationType The type of notification to enable
     * @param notifications    An ArrayList of the currently enabled notifications
     * @return {@link Observable<Settings>} wrapping the Settings object
     * @see SettingsManager for notification types
     */
    public Observable<Settings> enableNotification(int notificationType, List<Integer> notifications) {
        if (notifications.isEmpty() || notifications.contains(SettingsManager.NOTIFICATION_TYPE_NONE)) {
            // No notification type registered, enable
            return settingsService.enableNotifications(true)
                    .flatMap(responseBody -> updateNotifications(notificationType));
        } else if (notifications.size() == 1
                && ((notifications.contains(SettingsManager.NOTIFICATION_TYPE_EMAIL)
                && notificationType == SettingsManager.NOTIFICATION_TYPE_SMS)
                || (notifications.contains(SettingsManager.NOTIFICATION_TYPE_SMS)
                && notificationType == SettingsManager.NOTIFICATION_TYPE_EMAIL))) {
            // Contains another type already, send "All"
            return settingsService.enableNotifications(true)
                    .flatMap(responseBody -> updateNotifications(SettingsManager.NOTIFICATION_TYPE_ALL));
        } else {
            return settingsService.enableNotifications(true)
                    .flatMap(responseBody -> fetchSettings());
        }
    }

    /**
     * Update the user's notification preferences and fetches an updated {@link Settings} object.
     *
     * @param notificationType The type of notification to disable
     * @param notifications    An ArrayList of the currently enabled notifications
     * @return {@link Observable<Settings>} wrapping the Settings object
     * @see SettingsManager for notification types
     */
    public Observable<Settings> disableNotification(int notificationType, List<Integer> notifications) {
        if (notifications.isEmpty() || notifications.contains(SettingsManager.NOTIFICATION_TYPE_NONE)) {
            // No notifications anyway, return Settings
            return fetchSettings();
        } else if (notifications.contains(SettingsManager.NOTIFICATION_TYPE_ALL)
                || (notifications.contains(SettingsManager.NOTIFICATION_TYPE_EMAIL)
                && notifications.contains(SettingsManager.NOTIFICATION_TYPE_SMS))) {
            // All types enabled, disable passed type and enable other
            return updateNotifications(notificationType == SettingsManager.NOTIFICATION_TYPE_EMAIL
                    ? SettingsManager.NOTIFICATION_TYPE_SMS : SettingsManager.NOTIFICATION_TYPE_EMAIL);
        } else if (notifications.size() == 1) {
            if (notifications.get(0).equals(notificationType)) {
                // Remove all
                return settingsService.enableNotifications(false)
                        .flatMap(responseBody -> updateNotifications(SettingsManager.NOTIFICATION_TYPE_NONE));
            } else {
                // Notification type not present, no need to remove it
                return fetchSettings();
            }
        } else {
            // This should never be reached
            return fetchSettings();
        }
    }

    /**
     * Updates a passed notification type and then fetches the current settings object.
     *
     * @param notificationType The notification type you wish to enable/disable
     * @return {@link Observable<Settings>} wrapping the Settings object
     * @see SettingsManager for notification types
     */
    private Observable<Settings> updateNotifications(int notificationType) {
        return settingsService.updateNotifications(notificationType)
                .flatMap(responseBody -> fetchSettings());
    }

    /**
     * Fetches the latest user {@link Settings} object.
     *
     * @return A {@link Observable<Settings>} object
     */
    private Observable<Settings> fetchSettings() {
        return settingsService.getSettings()
                .compose(RxUtil.applySchedulersToObservable());
    }

}