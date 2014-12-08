package jp.ohwada.andorid.imagerecognition1;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.MediaStore.MediaColumns;
import android.util.Log;

/**
 * PhotoPicker
 */  
public class PhotoPicker {

    private static final String TAG_SUB = PhotoPicker.class.getSimpleName();
 
	private static final int REQUEST_PHOTO_KITKAT = Constant.REQUEST_PHOTO_KITKAT;
	private static final int REQUEST_PHOTO_DEFAULT = Constant.REQUEST_PHOTO_DEFAULT;
	private static final int REQUEST_CAMERA = Constant.REQUEST_CAMERA;

	private Activity mActivity ;
	private ContentResolver mContentResolver;
	
	private Uri mCameraUri = null;
	private Uri mResultUri = null;
	private String mResultPath = null;

 	/**
	 * === Constructor ===
	 */ 
	public PhotoPicker( Activity activity ) {
		mActivity = activity; 
		mContentResolver = mActivity.getContentResolver();
	}

 	/**
	 * getResultUri
	 */ 
	public Uri getResultUri() {
		return mResultUri ;
	}

 	/**
	 * getResultPath
	 */ 
	public String getResultPath() {
		return mResultPath;
	}

 	/**
	 * startActivityPhotoGallery
	 */ 
    public void startActivityPhotoGallery() {
		if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT ){
			startPhotoKitkat();
		} else {
			startPhotoDefault();
		}
	}

 	/**
	 * startPhotoKitkat
	 * http://ka-zoo.net/2014/07/garelly-content-uri-on-kitkat/
	 */ 
    @TargetApi(Build.VERSION_CODES.KITKAT) 
    private void startPhotoKitkat() {
		Intent intent = new Intent( Intent.ACTION_OPEN_DOCUMENT );
		intent.addCategory( Intent.CATEGORY_OPENABLE );
		intent.setType( "image/*" );
		mActivity.startActivityForResult( intent, REQUEST_PHOTO_KITKAT );
	}

 	/**
	 * startPhotoDefault
	 */ 
    private void startPhotoDefault() {
		Intent intent = new Intent( Intent.ACTION_PICK );
		intent.setAction( Intent.ACTION_GET_CONTENT );
		intent.setType( "image/*" );
		mActivity.startActivityForResult( intent, REQUEST_PHOTO_DEFAULT );
	}

 	/**
	 * startActivityCamera
	 */             		
    public void startActivityCamera() {
    	insertCameraUri();
		Intent intent = new Intent( MediaStore.ACTION_IMAGE_CAPTURE );  
    	intent.putExtra( MediaStore.EXTRA_OUTPUT, mCameraUri ); 
    	mActivity.startActivityForResult( intent, REQUEST_CAMERA );
	}

 	/**
	 * convResultToPath
	 */
	public boolean convResultToPath( int requestCode, int resultCode, Intent data ) {
		if ( requestCode == REQUEST_PHOTO_KITKAT ) {
			if ( resultCode == Activity.RESULT_OK ) {
				checkPermissionUri( data );
				mResultUri = data.getData();
				mResultPath = searchPathKitkat( mResultUri );
         		return true;
         	}
        } else if ( requestCode == REQUEST_PHOTO_DEFAULT ) {
        	if ( resultCode == Activity.RESULT_OK ) {
				checkPermissionUri( data );
				mResultUri = data.getData();
				mResultPath = searchPathDefault( mResultUri );
				return true;
			}	
        } else if ( requestCode == REQUEST_CAMERA ) {
        	if ( resultCode == Activity.RESULT_OK ) {
				if ( data == null ) {
					mResultUri = mCameraUri;
				} else {
					mResultUri = data.getData();
					deleteCameraUri();
				}
				mResultPath = searchPathDefault( mResultUri );
				return true;
			} else {
				deleteCameraUri();
			}		
		}
        return false;
    }

 	/**
	 * checkPermissionUri
	 */	
	private void checkPermissionUri( Intent data ) {
		Uri uri = data.getData();
		final int takeFlags = data.getFlags()
			& (Intent.FLAG_GRANT_READ_URI_PERMISSION
			| Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
		// Check for the freshest data.
		mContentResolver.takePersistableUriPermission( uri, takeFlags );
	}

 	/**
	 * searchPathKitkat
	 */
	private String searchPathKitkat( Uri uri ) {
		log_d( "searchPathKitkat " + uri.toString() );
		// http://ka-zoo.net/2014/07/garelly-content-uri-on-kitkat/
		String path = null;
		String id = DocumentsContract.getDocumentId( uri );
		String selection = "_id=?";
		String[] selectionArgs = new String[]{ id.split(":")[1] };
		Cursor cursor = mContentResolver.query(
			MediaStore.Images.Media.EXTERNAL_CONTENT_URI, 
			new String[]{MediaColumns.DATA}, selection, selectionArgs, null );
    	if ( cursor.moveToFirst() ) {
    		path = cursor.getString(0);
    	}
    	cursor.close();
		log_path( path );
    	return path;
   	}

 	/**
	 * searchPathKitkat
	 */
	private String searchPathDefault( Uri uri ) {
		log_d( "searchPathDefault " + uri.toString() );
		String path = null;
		String[] columns = { MediaStore.Images.Media.DATA };
		Cursor cursor = mContentResolver.query( uri, columns, null, null, null );
    	if ( cursor.moveToFirst() ) {    			
			path = cursor.getString(0);
		}
    	cursor.close();
		log_path( path );
		return path;
   }

 	/**
	 * insertCameraUri
	 * http://labs.i3design.jp/blog/android/intent-camera/
	 */             		
    private void insertCameraUri() {
		// TODO yyyy-mm-dd 
		String filename = System.currentTimeMillis() + ".jpg";  
		ContentValues values = new ContentValues();  
		values.put( MediaStore.Images.Media.TITLE, filename ); 
		values.put( MediaStore.Images.Media.MIME_TYPE, "image/jpeg" );  
		values.put( MediaStore.Images.Media.TITLE, filename ); 
		// TODO not set date 
		values.put( MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis() ); 
		mCameraUri = mContentResolver.insert( 
			MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values );
	}

 	/**
	 * deleteCameraUri
	 */  
	private void deleteCameraUri() {
		mContentResolver.delete( mCameraUri, null, null );
	}

 	/**
	 * log_path
	 */ 		
	private void log_path( String path ) {
		if ( path == null ) {
			log_d( "path null" );
		} else {
			log_d( "path " + path );
		}	
	}
 
	/**
	 * log_d
	 * @param String msg
	 */ 
	private void log_d( String msg ) {
		if (Constant.D) Log.d( Constant.TAG,  TAG_SUB + " " + msg );
	}
	
}
