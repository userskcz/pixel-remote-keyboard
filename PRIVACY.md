# Privacy

Pixel Remote Keyboard has no analytics SDK, advertising SDK, user account, text database, or application-operated backend.

Text is transmitted using Google Play services' Wearable MessageClient to the paired companion application. Platform connectivity and Google's services are governed by their own settings and terms. The app does not promise that every message remains on Bluetooth.

The phone stores metadata for the latest request, including the peer ID, session ID, destination app label, password flag, and arrival timestamp. Requests expire after ten minutes for sending purposes; expiration is not automatic deletion of their metadata. Clear app storage or uninstall to remove it.

Typed text is held transiently in memory and in the destination text field. It is not written to preferences or logged by this app. Phone keyboards, clipboard services, the operating system, and the destination app have their own behavior. Input fields may retain received text.

The clipboard is read only on an explicit Paste action. Notification content does not contain entered text. Password input requires an explicit action on the watch, is masked on the phone, and enables Android's secure-window flag.

Application backup and device-transfer exclusions are provided. Drafts are not restored after process death. Session identifiers prevent an old response from being accepted by a new editor session.
