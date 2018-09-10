package com.okawa.voip.db

import android.app.Application
import android.content.ContentProviderOperation
import android.content.ContentResolver
import android.content.ContentValues
import android.net.Uri
import android.provider.ContactsContract
import com.okawa.voip.model.Contact
import com.okawa.voip.model.History
import com.okawa.voip.utils.executors.AppExecutors
import com.okawa.voip.utils.extensions.toInt
import com.okawa.voip.utils.mapper.HistoryMapper
import javax.inject.Inject
import javax.inject.Singleton
import com.okawa.voip.utils.utils.BitmapUtils
import java.io.FileNotFoundException


@Singleton
class DatabaseManager @Inject constructor(private val appExecutors: AppExecutors, application: Application, private val bitmapUtils: BitmapUtils, private val historyMapper: HistoryMapper) {

    companion object {
        const val VOIP_APP_PROFILE = "VoIP App Profile"
        const val VOIP_APP_VIEW_PROFILE = "View Profile"
    }

    private val contentResolver: ContentResolver by lazy {
        application.contentResolver
    }

    private val packageName: String by lazy {
        application.packageName
    }

    /**
     * Inserts the contact based on the given data
     *
     * @param name contact name
     * @param name contact number
     * @param name contact photo
     */
    fun insertContact(name: String, number: String, photo: Uri?) {
        appExecutors.getDiskIO().execute {
            val operations = ArrayList<ContentProviderOperation>()
            var photoBlob: ByteArray? = null
            photo?.let {
                try {
                    photoBlob = bitmapUtils.convertCompact(contentResolver.openInputStream(it))
                } catch (exception: FileNotFoundException) {
                    exception.printStackTrace()
                }
            }

            /* RAW CONTACT */
            operations.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, name)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, packageName)
                    .build())

            /* CONTACT */
            operations.add(ContentProviderOperation.newInsert(ContactsContract.Settings.CONTENT_URI.buildUpon()
                    .appendQueryParameter(ContactsContract.CALLER_IS_SYNCADAPTER, "true").build())
                    .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, name)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, packageName)
                    .withValue(ContactsContract.Settings.UNGROUPED_VISIBLE, 1)
                    .build())

            /* NAME */
            operations.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI.buildUpon()
                    .appendQueryParameter(ContactsContract.CALLER_IS_SYNCADAPTER, "true").build())
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, name)
                    .build())

            /* NUMBER */
            operations.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI.buildUpon()
                    .appendQueryParameter(ContactsContract.CALLER_IS_SYNCADAPTER, "true").build())
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, number)
                    .build())

            /* PHOTO */
            operations.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI.buildUpon()
                    .appendQueryParameter(ContactsContract.CALLER_IS_SYNCADAPTER, "true").build())
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.Photo.PHOTO, photoBlob)
                    .build())

            /* CUSTOM DATA */
            operations.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI.buildUpon()
                    .appendQueryParameter(ContactsContract.CALLER_IS_SYNCADAPTER, "true").build())
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE, DatabaseHelper.MIME_TYPE)
                    .withValue(ContactsContract.Data.DATA1, number)
                    .withValue(ContactsContract.Data.DATA2, VOIP_APP_PROFILE)
                    .withValue(ContactsContract.Data.DATA3, VOIP_APP_VIEW_PROFILE)
                    .build())

            contentResolver.applyBatch(ContactsContract.AUTHORITY, operations)

            operations.clear()
        }
    }

    /**
     * Updates the contact based on the given id and data
     *
     * @param id contact data id
     * @param name contact name
     * @param name contact photo
     */
    fun updateContact(id: String, name: String, photo: Uri?) {
        appExecutors.getDiskIO().execute {
            val operations = ArrayList<ContentProviderOperation>()
            var photoBlob: ByteArray? = null
            photo?.let {
                try {
                    photoBlob = bitmapUtils.convertCompact(contentResolver.openInputStream(it))
                } catch (exception: FileNotFoundException) {
                    exception.printStackTrace()
                }
            }

            /* NAME */
            operations.add(ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                    .withSelection("${ContactsContract.Data.CONTACT_ID} = ? AND ${ContactsContract.Data.MIMETYPE} = ?", arrayOf(id, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE))
                    .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, name)
                    .build())

            /* PHOTO */
            operations.add(ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                    .withSelection("${ContactsContract.Data.CONTACT_ID} = ? AND ${ContactsContract.Data.MIMETYPE} = ?", arrayOf(id, ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE))
                    .withValue(ContactsContract.CommonDataKinds.Photo.PHOTO, photoBlob)
                    .build())

            contentResolver.applyBatch(ContactsContract.AUTHORITY, operations)

            operations.clear()
        }
    }

    /**
     * Deletes the contact based on the given id
     *
     * @param id contact data id
     */
    fun deleteContact(id: String) {
        appExecutors.getDiskIO().execute {
            val operations = ArrayList<ContentProviderOperation>()

            operations.add(ContentProviderOperation.newDelete(ContactsContract.Data.CONTENT_URI)
                    .withSelection("${ContactsContract.Data.CONTACT_ID} = ?", arrayOf(id))
                    .build())

            contentResolver.applyBatch(ContactsContract.AUTHORITY, operations)

            operations.clear()
        }
    }

    /**
     * Inserts contact call to the History
     *
     * @param contact to be inserted to the call log
     */
    fun insertHistory(contact: Contact) {
        appExecutors.getDiskIO().execute {
            val history = historyMapper.convert(contact)

            val contentValues = ContentValues()

            contentValues.put(History.COLUMN_NAME, history.name)
            contentValues.put(History.COLUMN_NUMBER, history.number)
            contentValues.put(History.COLUMN_PHOTO, history.photo.toString())
            contentValues.put(History.COLUMN_IS_VOIP_APP, history.isVoIPApp.toInt())
            contentValues.put(History.COLUMN_DATE, history.date.time)

            contentResolver.insert(History.CONTENT_URI, contentValues)
        }
    }

}