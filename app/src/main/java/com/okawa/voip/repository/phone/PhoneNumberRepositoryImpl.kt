package com.okawa.voip.repository.phone

import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.okawa.voip.model.CountryCode
import com.okawa.voip.utils.executors.AppExecutors
import java.util.Locale
import javax.inject.Inject

class PhoneNumberRepositoryImpl @Inject constructor(private val appExecutors: AppExecutors) : PhoneNumberRepository {

    companion object {

        const val NO_REGION = ""
        const val UNKNOWN_REGION = "ZZ"

    }

    private val phoneNumberUtil = PhoneNumberUtil.getInstance()

    override fun retrieveCountryCodes(listener: (List<CountryCode>) -> Unit) {
        appExecutors.getDiskIO().execute {
            listener.invoke(phoneNumberUtil.supportedRegions.mapNotNull { region ->
                convertToCountryCode(region)
            })
        }
    }

    override fun validateNumber(region: String, number: String) = phoneNumberUtil.isPossibleNumber(number, region)

    /**
     * Converts the region to a country code with display name and code
     *
     * @param region region abbreviation
     *
     * @return converted country code
     */
    private fun convertToCountryCode(region: String?) : CountryCode? {
        val locale = Locale.getDefault()
        val code = phoneNumberUtil.getCountryCodeForRegion(region ?: return null)
        val name = if(region == UNKNOWN_REGION || region == PhoneNumberUtil.REGION_CODE_FOR_NON_GEO_ENTITY) NO_REGION else Locale(locale.language, region).displayCountry
        return CountryCode(code, region, name)
    }

}