package jp.ohwada.andorid.imagerecognition1;

import jp.ne.docomo.smt.dev.imagerecognition.data.ImageRecognitionRelatedContentData;
import jp.ne.docomo.smt.dev.imagerecognition.data.ImageRecognitionSiteData;

/**
 * ContentData
 */ 
public class ContentData {

	public String title;
	public  String url;
	public  String imageUrl;

 	/**
	 * === Constructor ===
	 */  
	public ContentData( ImageRecognitionSiteData data ) {
		title = data.getTitle();
		url = data.getUrl();
		imageUrl = data.getImageUrl();
	}	

 	/**
	 * === Constructor ===
	 */ 
	public ContentData( ImageRecognitionRelatedContentData data ) {
		title = data.getTitle();
		url = data.getUrl();
		imageUrl = data.getImageUrl();
	}	

}
