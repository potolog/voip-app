package com.okawa.voip.utils

import android.provider.ContactsContract

object CursorUtils {
    val PROJECTION = arrayOf(
            ContactsContract.Data.DISPLAY_NAME,
            ContactsContract.Contacts.Photo.PHOTO
    )

    val SELECTION_ARGUMENTS = null

    val SELECTION_CLAUSE = null

    const val SORT_ORDER = ContactsContract.Data.DISPLAY_NAME

}