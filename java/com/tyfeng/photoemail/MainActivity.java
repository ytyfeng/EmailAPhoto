package com.tyfeng.photoemail;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends AppCompatActivity {

    private static final int ACTIVITY_SELECT_PICTURE = 1;
    private static final int ACTIVITY_SELECT_CONTACT = 2;
    private static final int ACTIVITY_SEND_EMAIL = 3;
    private Uri selectedURI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // start gallery to select a photo to email
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, ACTIVITY_SELECT_PICTURE);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        switch(requestCode) {
            case ACTIVITY_SELECT_PICTURE:
                if (resultCode == Activity.RESULT_OK) {
                    // get selected picture data
                    selectedURI = data.getData();
                    // start contacts activity
                    Intent contactIntent = new Intent(Intent.ACTION_PICK,
                            ContactsContract.Contacts.CONTENT_URI);
                    startActivityForResult(contactIntent, ACTIVITY_SELECT_CONTACT);
                }
                else {
                    Log.i("IN onActivityResult", "Picture not selected");
                }
                break;
            case ACTIVITY_SELECT_CONTACT:
                if (resultCode == Activity.RESULT_OK) {
                    // get name, id, email address from contact
                    Uri contactData = data.getData();
                    Cursor contactsCursor = getContentResolver().query(contactData,
                            null,null,null,null);
                    String contactName;
                    String[] emailAddress = new String[1];
                    if (contactsCursor.moveToFirst()) {
                        contactName = contactsCursor.getString(
                                contactsCursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                        int contactId = contactsCursor.getInt(
                                contactsCursor.getColumnIndex(ContactsContract.Contacts._ID));
                        contactsCursor.close();
                        emailAddress[0] = searchForEmailAddressById(contactId);
                    }
                    else {
                        contactName = null;
                        emailAddress[0] = null;
                        Log.i("IN onActivityResult", "No contact data found");
                    }
                    // start email activity
                    Intent emailIntent = new Intent(Intent.ACTION_SEND);
                    emailIntent.setType("text/plain");
                    emailIntent.putExtra(Intent.EXTRA_EMAIL, emailAddress);
                    emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Photo from Ty");
                    emailIntent.putExtra(Intent.EXTRA_TEXT, "Hello, "
                            + contactName + ".  Here's an awesome photo I wanted to share with you. ");
                    emailIntent.putExtra(Intent.EXTRA_STREAM, selectedURI);
                    startActivityForResult(emailIntent, ACTIVITY_SEND_EMAIL);
                }
                else {
                    Log.i("IN onActivityResult", "Contact not selected");
                }
                break;
            case ACTIVITY_SEND_EMAIL:
                // end the app once the email is sent
                finish();
                break;
        }

    }

    public String searchForEmailAddressById(int contactId) {

        // want to fetch the contact id and the email address
        String[] projection = new String[] {
                ContactsContract.CommonDataKinds.Email.CONTACT_ID,
                ContactsContract.CommonDataKinds.Email.DATA
        };

        Cursor emailCursor = getContentResolver().query(
                ContactsContract.CommonDataKinds.Email.CONTENT_URI,projection,"CONTACT_ID = ?",
                new String[]{Integer.toString(contactId)},null);

        // get email address
        String emailAddress;
        if (emailCursor.moveToFirst()) {
            emailAddress = emailCursor.getString(emailCursor.getColumnIndex(
                    ContactsContract.CommonDataKinds.Email.DATA));
        } else {
            emailAddress = null;
            Log.i("IN searchForEmail", "No email address found");
        }
        emailCursor.close();
        return(emailAddress);

    }
}