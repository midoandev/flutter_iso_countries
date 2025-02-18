package com.anoop4real.iso_countries

import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.BinaryMessenger
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class IsoCountriesPlugin : FlutterPlugin, MethodChannel.MethodCallHandler {

    private var channel: MethodChannel? = null

    override fun onAttachedToEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        registerWith(binding.binaryMessenger)
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        channel?.setMethodCallHandler(null)
        channel = null
    }

    private fun registerWith(messenger: BinaryMessenger) {
        channel = MethodChannel(messenger, "com.anoop4real.iso_countries")
        channel?.setMethodCallHandler(this) // Jangan buat instance baru
    }

    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        when (call.method) {
            "getPlatformVersion" -> result.success("Android ${android.os.Build.VERSION.RELEASE}")
            "getISOCountries" -> result.success(CountryDataStore.getIsoCountries())
            "getISOCountriesForLocale" -> {
                val args = call.arguments as? HashMap<String, String>
                val identifier = args?.get("locale_identifier") ?: "en_US"
                result.success(CountryDataStore.getIsoCountries(identifier))
            }
            "getCountryForCountryCodeWithLocaleIdentifier" -> {
                val args = call.arguments as? HashMap<String, String>
                val identifier = args?.get("locale_identifier") ?: ""
                val code = args?.get("countryCode") ?: ""
                result.success(CountryDataStore.getCountryForCountryCode(code, identifier))
            }
            else -> result.notImplemented()
        }
    }
}

class CountryDataStore private constructor() {

    companion object {

        fun getIsoCountries(localeIdentifier: String = "en-US"): ArrayList<HashMap<String, String>> {
            val countriesList = arrayListOf<HashMap<String, String>>()
            for (countryCode in Locale.getISOCountries()) {
                val locale = Locale(localeIdentifier, countryCode)
                val countryName = locale.getDisplayCountry(Locale.forLanguageTag(localeIdentifier))
                    .takeIf { it.isNotEmpty() } ?: "Unidentified"
                countriesList.add(hashMapOf("name" to countryName, "countryCode" to countryCode))
            }
            return ArrayList(countriesList.sortedWith(compareBy { it["name"] }))
        }

        fun getCountryForCountryCode(code: String, localeIdentifier: String = ""): HashMap<String, String> {
            if (code.isEmpty() || code.length > 2) return hashMapOf()

            val locale = Locale(localeIdentifier, code)
            val countryName = locale.getDisplayCountry(Locale.forLanguageTag(localeIdentifier))
                .takeIf { it.isNotEmpty() } ?: return hashMapOf()

            return hashMapOf("name" to countryName, "countryCode" to code)
        }
    }
}