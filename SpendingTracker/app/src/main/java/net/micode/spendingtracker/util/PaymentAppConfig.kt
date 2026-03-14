package net.micode.spendingtracker.util

/**
 * Configuration registry for supported payment and banking applications.
 */
object PaymentAppConfig {
    
    // Set of package names for faster lookup O(1)
    private val supportedPackages = hashSetOf(
        "com.eg.android.AlipayGphone",      // Alipay
        "com.tencent.mm",                   // WeChat
        "com.android.shell",                // ADB Test
        "com.paypal.android.p2pmobile",     // PayPal
        "com.revolut.revolut",              // Revolut
        "com.transferwise.android",         // Wise
        "com.nu.production",                // Nubank
        "com.mercadopago.wallet",           // Mercado Pago
        "com.google.android.apps.nbu.paisa.user", // Google Pay (GPay)
        "com.apple.wallet",                 // Apple Wallet (for some ROMs)
        "com.bbva.mobile",                  // BBVA
        "com.santander.app",                // Santander
        // Add more banking/payment apps here as needed
    )

    /**
     * Checks if the given package name belongs to a supported payment app.
     */
    fun isSupported(packageName: String): Boolean {
        return supportedPackages.contains(packageName)
    }

    /**
     * Returns a human-readable name for the supported app.
     */
    fun getAppName(packageName: String): String {
        return when {
            packageName.contains("alipay", ignoreCase = true) -> "Alipay"
            packageName.contains("tencent.mm", ignoreCase = true) -> "WeChat"
            packageName.contains("paypal", ignoreCase = true) -> "PayPal"
            packageName.contains("revolut", ignoreCase = true) -> "Revolut"
            packageName.contains("wise", ignoreCase = true) -> "Wise"
            packageName.contains("shell", ignoreCase = true) -> "ADB Test"
            else -> "Payment App"
        }
    }
}
