package jp.ohwada.andorid.imagerecognition1;

import java.util.ArrayList;
import java.util.List;

import jp.ne.docomo.smt.dev.common.exception.SdkException;
import jp.ne.docomo.smt.dev.common.exception.ServerException;
import jp.ne.docomo.smt.dev.imagerecognition.ImageRecognition;
import jp.ne.docomo.smt.dev.imagerecognition.data.ImageRecognitionCandidateData;
import jp.ne.docomo.smt.dev.imagerecognition.data.ImageRecognitionRelatedContentData;
import jp.ne.docomo.smt.dev.imagerecognition.data.ImageRecognitionResultData;
import jp.ne.docomo.smt.dev.imagerecognition.data.ImageRecognitionSiteData;
import jp.ne.docomo.smt.dev.imagerecognition.param.ImageRecognitionRequestParam;

import android.os.AsyncTask;
import android.util.Log;

/**
 * RecognitionAsyncTask
 * https://dev.smt.docomo.ne.jp/?p=docs.api.page&api_docs_id=102
 * TODO rewrite to loaders
 */  
public class RecognitionAsyncTask extends 		
	AsyncTask<ImageRecognitionRequestParam, Integer, ImageRecognitionResultData> {

    private static final String TAG_SUB = RecognitionAsyncTask.class.getSimpleName();

    public static final int CODE_NONE = 0;
    public static final int CODE_SDK = 1;
    public static final int CODE_SERVER = 2;
 
	private List<ContentData> mList = null;

	private int mErrorCode = 0;
	private String mErrorMessage = "";

  	/**
	 * === Constructor ===
	 */
	public RecognitionAsyncTask() {
		super();
	}

  	/**
	 * === doInBackground ===
	 */	 
	@Override
	protected ImageRecognitionResultData doInBackground( ImageRecognitionRequestParam... params ) {
		ImageRecognitionResultData resultData = null;
		ImageRecognitionRequestParam requestParam = params[0];
		try {
			ImageRecognition recognition = new ImageRecognition();
			resultData = recognition.request( requestParam );
		} catch ( SdkException ex ) {
			mErrorCode = CODE_SDK;
			mErrorMessage = ex.getMessage();
			if (Constant.D) ex.printStackTrace(); 
		} catch ( ServerException ex ) {
			mErrorCode = CODE_SERVER;
			mErrorMessage = ex.getMessage();
			if (Constant.D) ex.printStackTrace(); 
		}
		return resultData;
	}

  	/**
	 * === onCancelled ===
	 */
	@Override
	protected void onCancelled() {
		// dummy
	}

  	/**
	 * === onPostExecute ===
	 */		
	@Override
	protected void onPostExecute( ImageRecognitionResultData resultData ) {
		if ( resultData == null ) return;
		List<ImageRecognitionCandidateData> candidateList = 
			resultData.getCandidateDataList();
		if ( candidateList == null ) return;
		mList = new ArrayList<ContentData>();	
		for ( ImageRecognitionCandidateData candidateData : candidateList ) {
			List<ImageRecognitionSiteData> siteList =
				candidateData.getSiteDataList();
			if ( siteList != null)  {
				for (ImageRecognitionSiteData siteData : siteList) {
					mList.add( new ContentData( siteData ) );	
					log_d( "onPostExecute site " + siteData.getTitle() );
				}
				List<ImageRecognitionRelatedContentData> relatedList = 
					candidateData.getRelatedContentDataList();
				if ( relatedList != null ) {
					for ( ImageRecognitionRelatedContentData relatedData : relatedList ) {
						mList.add( new ContentData( relatedData ) );
						log_d( "onPostExecute Related " + relatedData.getTitle() );
					}
				}
			}
		} // for end
	}

  	/**
	 * getList
	 */	
	public List<ContentData> getList() {
		return mList;
	}

  	/**
	 * getErrorCode
	 */	
	public int getErrorCode() {		
		return mErrorCode;
	}

  	/**
	 * getErrorMessage
	 */
	public String getErrorMessage() {	
		return mErrorMessage;
	}

	/**
	 * log_d
	 * @param String msg
	 */ 
	private void log_d( String msg ) {
		if (Constant.D) Log.d( Constant.TAG,  TAG_SUB + " " + msg );
	}
	
}
