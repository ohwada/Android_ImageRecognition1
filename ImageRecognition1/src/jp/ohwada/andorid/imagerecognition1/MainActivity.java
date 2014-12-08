package jp.ohwada.andorid.imagerecognition1;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

/**
 * MainActivity
 */
public class MainActivity extends Activity {

    private static final String LF = "\n";
    
	private int	mScreenHeight = 0;
		
	private ImageView mImageView;
	private ListView mListView;
	
	private RecognitionManager mRecognitionManager;
	private PhotoPicker mPhotoPicker;

	private List<ContentData> mList;
	private ContentAdapter mAdapter;
	private String mPath;

 	/**
	 * === onCreate ===
	 */ 
    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // TODO rewrite to listview header
        mImageView = (ImageView) findViewById( R.id.ImageView_image ); 
        mImageView.setScaleType( ImageView.ScaleType.MATRIX );
		
		Button btnPhoto = (Button) findViewById( R.id.Button_photo );
		btnPhoto.setOnClickListener( new View.OnClickListener() {
			@Override
			public void onClick( View view ) {
				selectPhoto();
			}
		});

		Button btnCamera = (Button) findViewById( R.id.Button_camera );
		btnCamera.setOnClickListener( new View.OnClickListener() {
			@Override
			public void onClick( View view ) {
				startCamera();
			}
		});
		
		Button btnRecognize = (Button)findViewById( R.id.Button_recognize );
		btnRecognize.setOnClickListener( new View.OnClickListener() {
			@Override
			public void onClick( View view ) {
				recognizePhoto();
			}
		});

       ProgressBar pb = (ProgressBar) findViewById( R.id.ProgressBar );

       mRecognitionManager = new RecognitionManager( this );
		mRecognitionManager.setProgressBar( pb );
		mRecognitionManager.setOnChangedListener( new RecognitionManager.OnChangedListener() {
			@Override
			public void onFinished( List<ContentData> list ) {
				updateAdapter( list );
			}
		});

        mList = new ArrayList<ContentData>();
		mAdapter = new ContentAdapter( this, 0, mList );

		mListView = (ListView) findViewById( R.id.ListView );
     	mListView.setAdapter( mAdapter );
        mListView.setOnItemClickListener( new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick( AdapterView<?> parent, View view, int position, long id ) {
            		startBrawser( position );
            }
        });
       
       mPhotoPicker = new PhotoPicker( this );
		
		getScreenSize();
    }

 	/**
	 * === onPause ===
	 */ 				
    @Override
    public void onPause(){
    	super.onPause();
    	mRecognitionManager.stop();
    }

 	/**
	 * getScreenSize
	 */
    private void getScreenSize() {
		WindowManager wm = (WindowManager)getSystemService(WINDOW_SERVICE);
		Display disp = wm.getDefaultDisplay();
		Point size = new Point();
		disp.getSize( size );
		mScreenHeight = size.y;
    }

 	/**
	 * selectPhoto
	 */    
    private void selectPhoto() {
		clearView();
		clearAdapter();
    	mPhotoPicker.startActivityPhotoGallery();
	}

 	/**
	 * startCamera
	 */
    private void startCamera() {
		clearView();
		clearAdapter();
		mPhotoPicker.startActivityCamera();
	}

 	/**
	 * clearView
	 */
    private void clearView() {
		mPath = null;
    	mImageView.setImageURI( null );
	}

 	/**
	 * recognizePhoto
	 */	
	private void recognizePhoto() {
		if ( mPath == null ) {
			toast_show( R.string.msg_please_select );
			return;
		}
		clearAdapter();
		mRecognitionManager.recognizePhoto( mPath );
	}

 	/**
	 * clearAdapter
	 */ 
 	private void clearAdapter() { 
		updateAdapter( new ArrayList<ContentData>() );
	}
	
 	/**
	 * updateAdapter
	 */ 
 	private void updateAdapter( List<ContentData> list ) { 
		mList = list;
		mAdapter.clear();
		mAdapter.addAll( mList );
		mAdapter.notifyDataSetChanged();
	}

 	/**
	 * startBrawser
	 */
	private void startBrawser( int position ) {
		ContentData data = mList.get( position );                    
		Uri uri = Uri.parse( data.url );
		Intent intent = new Intent( Intent.ACTION_VIEW, uri );
		startActivity( intent );
	}

 	/**
	 * === onActivityResult ===
	 */ 				
    @Override
	protected void onActivityResult( int requestCode, int resultCode, Intent data ) {
		boolean ret = mPhotoPicker.convResultToPath(
			requestCode, resultCode, data );
		if ( ret ) {
			showImage( mPhotoPicker.getResultUri(), mPhotoPicker.getResultPath() );
		}
    }

 	/**
	 * showImage
	 */	
	private void showImage( Uri uri, String path ) {
		if ( path == null ) {
			String msg = getString( R.string.msg_cannot_get  ) + LF + uri.toString();
			toast_show( msg );
		} else {
			mPath = path;
			setImageView( path );
			recognizePhoto();
		}	
	}

 	/**
	 * setImageView
	 * http://blog.livedoor.jp/esper776/archives/65811038.html
	 */
	private void setImageView( String path ) {
		Bitmap bm = BitmapFactory.decodeFile( path );
		mImageView.setImageBitmap( bm );
		float factor =  0.25f * mScreenHeight / bm.getHeight();
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
			(int)(bm.getWidth()*factor), (int)(bm.getHeight()*factor));
		lp.gravity = Gravity.CENTER;
		mImageView.setLayoutParams( lp );
		Matrix m = mImageView.getImageMatrix();
		m.reset();
		m.postScale( factor, factor );
		mImageView.setImageMatrix( m );
	}
  		
 	/**
	 * toast_show
	 * @param int res_id
	 */ 
	private  void toast_show( int res_id ) {
		ToastMaster.showText( this, res_id, Toast.LENGTH_SHORT );
	}

 	/**
	 * toast_show
	 * @param int res_id
	 */ 
	private  void toast_show( String mes  ) {
		ToastMaster.showText( this, mes, Toast.LENGTH_SHORT );
	}
	
}
