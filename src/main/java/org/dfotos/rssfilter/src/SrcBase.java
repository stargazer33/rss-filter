package org.dfotos.rssfilter.src;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;


public abstract class SrcBase 
implements SrcIntf {

	private static final Logger log = Logger.getLogger( SrcBase.class.getName() );	
	
	protected String url;
	
	protected String name;
	
	OkHttpClient client = null;
		
	/**
	 * Performs the HTTP GET request using "url" field
	 * Returns the response body or throws the IOException in case the url can not be retrieved
	 *   
	 * @return the response body 
	 * @throws IOException
	 */
	public String doHttpGet() 
	throws IOException 
	{
		log.log( Level.INFO, "{0}, reading...", new Object[]{ getName() } );
		
		if (client==null){
			client = new OkHttpClient();
		}
		
		Request request = new Request.Builder().url(url).build();
		Response response = client.newCall(request).execute();
		//look inside the response
		long respLength = 0;
		if ( response.body()!=null ){
			respLength=response.body().contentLength();
		}
		
		if (!response.isSuccessful()){
			log.log( Level.INFO, "HTTP response: " + response + " length: " +respLength);
			//can not continue
			throw new IOException("Server error: " + response);
		}
		
		String tmp=response.body().string();
		log.log( Level.FINE, "{0}, {1} bytes read", new Object[]{ getName(), tmp.length() } );
		return tmp;
	}
	
	@Override
	public String getUrl() {
		return url;
	}

	@Override
	public void setUrl(String pUrl) {
		url=pUrl;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String pName) {
		name=pName;
	}

}
