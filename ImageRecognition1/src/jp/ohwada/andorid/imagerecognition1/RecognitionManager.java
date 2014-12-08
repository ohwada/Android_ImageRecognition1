package jp.ohwada.andorid.imagerecognition1;

import java.util.List;

import jp.ne.docomo.smt.dev.common.http.AuthApiKey;
import jp.ne.docomo.smt.dev.imagerecognition.constants.Recog;
import jp.ne.docomo.smt.dev.imagerecognition.param.ImageRecognitionRequestParam;

import android.app.AlertDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ProgressBar;

/**
 * RecognitionManager
 */  
public class RecognitionManager {

	// timer
    private static final int TIMER_MSG_WHAT = 100;
    private static final int TIMER_INTERVAL = 500;  // 0.5 sec
		
    private Context mContext;
    		
	private RecognitionAsyncTask mTask;
	private ProgressBar mProgressBar;

    // timer
    private boolean isStart = false;
    private boolean isRunning = false;
   
	// callback
    private OnChangedListener mChangedListener = null;
    
    /**
     * The callback interface 
     */
    public interface OnChangedListener {
        public void onFinished( List<ContentData> list );
    }

  	/**
	 * === Constructor ===
	 */        
	public RecognitionManager( Context context ) {
        mContext = context;
		AuthApiKey.initializeAuth( ApiConfig.APIKEY );
	}

  	/**
	 * setProgressBar
	 */ 
	public void setProgressBar( ProgressBar view ) {
		mProgressBar = view;
	}

  	/**
	 * stop
	 */
    public void stop() {
    	if ( mTask != null ) {
    		mTask.cancel( true );
    	}
    	stopHandler();
    }

  	/**
	 * recognizePhoto
	 */
	public void recognizePhoto( String path ) {
		// parameter			
		ImageRecognitionRequestParam requestParam = new ImageRecognitionRequestParam();
		requestParam.setRecog( Recog.ALL );
		requestParam.setFilePath( path );		
		// excute
		mTask = new RecognitionAsyncTask();
		mTask.execute( requestParam );
		startHandler();
	}

	/**
	 * start Handler
	 */    
	private void startHandler() {
		isStart = true; 
		updateRunning();
		if ( mProgressBar != null ) {
			mProgressBar.setVisibility( View.VISIBLE );
		}
	}
        
	/**
	 * stop Handler
	 */ 
	private void stopHandler() {     
		isStart = false;
		updateRunning();
		if ( mProgressBar != null ) {
			mProgressBar.setVisibility( View.GONE );
		}	
	}

	/**
	 * updateRunning 
	 */             
    private void updateRunning() {
        boolean running = isStart;
        if ( running != isRunning ) {
			// restart running    
            if ( running ) {
                timerHandler.sendMessageDelayed( 
                	Message.obtain( timerHandler, TIMER_MSG_WHAT ), 
                	TIMER_INTERVAL );              
             // stop running             
             } else {
                timerHandler.removeMessages( TIMER_MSG_WHAT );
            }
            isRunning = running;
        }
    }

	/**
	 * timer handler class
	 */         
    private Handler timerHandler = new Handler() {
        public void handleMessage( Message m ) {
            if ( isRunning ) {
				if ( updateStatus() ) {
                	sendMessageDelayed( 
                		Message.obtain( this, TIMER_MSG_WHAT ), TIMER_INTERVAL );
                }
            }
        }
    };
        
	/**
	 * update Status
	 */     
    private synchronized boolean updateStatus() { 
		if ( mTask.getStatus() != AsyncTask.Status.FINISHED ) {
			return true;
		}
		stopHandler(); 
		List<ContentData> list = mTask.getList(); 
		if ( list == null ) {
			showErrorDialog( mTask.getErrorCode(), mTask.getErrorMessage() );
		} else {
			notifyFinished( list );
		}
		return false;	
	}

	/**
	 * showErrorDialog
	 */     
    private void showErrorDialog( int code, String msg ) { 
		int title = R.string.dialog_title_error;
		switch( code ) {
			case RecognitionAsyncTask.CODE_SDK:
				title = R.string.dialog_title_sdk;
				break;
			case RecognitionAsyncTask.CODE_SERVER:
				title = R.string.dialog_title_server;
				break; 
			default:
				msg = getString( R.string.dialog_msg_cannot );	
				break; 
		}
		AlertDialog.Builder dialog = new AlertDialog.Builder( mContext );
		dialog.setTitle( title );
		dialog.setMessage( msg ); 
		dialog.show(); 
	}

     /**
     * getString
     */
	private String getString( int id ) {
		return mContext.getResources().getString( id );	
	}

// --- callback ---		
     /**
     * setOnChangedListener
     * @param OnChangedListener listener
     */
    public void setOnChangedListener( OnChangedListener listener ) {
        mChangedListener = listener;
    }

	/**
	 * notifyFinished
	 * @param List<ContentData> list
	 */
	protected void notifyFinished( List<ContentData> list ) {
		if ( mChangedListener != null ) {
			mChangedListener.onFinished( list );
		}
	}
	
}
