package jp.ohwada.andorid.imagerecognition1;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageLoader.ImageContainer;
import com.android.volley.toolbox.ImageLoader.ImageListener;
import com.android.volley.toolbox.Volley;

/**
 * ContentAdapter
 */  
public class ContentAdapter extends ArrayAdapter<ContentData> {

	private ImageLoader mImageLoader;
	private LayoutInflater mInflater = null;
 
 	/**
	 * === Constructor ===
	 */ 
 	public ContentAdapter( Context context, int textViewResourceId, List<ContentData> objects ) {
 		super( context, textViewResourceId, objects );
 		mInflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        mImageLoader = new ImageLoader( 
        	Volley.newRequestQueue( context ), 
        	new BitmapCache() );
 	}

 	/**
	 * === getView ===
	 * http://qiita.com/gari_jp/items/829a54bfa937f4733e29
	 */  
 	@Override
 	public View getView( int position, View convertView, ViewGroup parent ) {
        View view = convertView;
        ContentHolder holder = null;  

		// once at first
		if ( view == null ) {
			// get view form xml
			view = mInflater.inflate( R.layout.content_row, null );                        
			// save 
			holder = new ContentHolder(); 
			holder.tv_title = (TextView) view.findViewById( R.id.TextView_content_title ); 
			holder.iv_image = (ImageView) view.findViewById( R.id.ImageView_content_image ); 
			view.setTag( holder ); 
		} else {
			// load  
			holder = (ContentHolder) view.getTag();  
		}  
     
 		// get the data of position row 
 		ContentData item = (ContentData) getItem( position );

		// set value
		holder.tv_title.setText( item.title ) ;

		// Cancellation processing of request
    	ImageContainer imageContainer = (ImageContainer) holder.iv_image.getTag();
    	if ( imageContainer != null ) {
        	imageContainer.cancelRequest();
    	}

		// get the remote image   
    	ImageListener listener = ImageLoader.getImageListener( 
    		holder.iv_image, 
    		android.R.drawable.spinner_background, 
    		android.R.drawable.ic_dialog_alert );
    	holder.iv_image.setTag( mImageLoader.get( item.imageUrl, listener ) );

		return view;
	}
	
	/**
	 * holder
	 */     
	static class ContentHolder { 
		public TextView tv_title;
		public ImageView iv_image;
    }
 
}
