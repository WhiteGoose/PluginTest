/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package SkytapPlugin;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import javax.xml.parsers.*;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;


//
//import org.apache.http.client.HttpResponseException;
import org.apache.http.client.*;
import org.apache.http.client.methods.HttpRequestBase;

import org.apache.http.client.methods.*;

import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

//import org.w3c.dom.*;

/**
 *
 * @author Administrator
 */
public class SkytapUtils {
    /*
    get the Auth key from Xml, and return the encoded value
    */
    public static String getAuthentication()
    {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        Document document;

        try{
          DocumentBuilder db = dbf.newDocumentBuilder();
          document =db.parse("./settings.xml");
          
          Element rootXml= document.getDocumentElement();
          NodeList noList= rootXml.getChildNodes();
          Node userName= noList.item(1);//xml username
          Node passWord = noList.item(3);//xml password 
                 
          if(userName==null||passWord==null)
          {
              throw new Exception("username or password missing, please check the settings.xml");
          }
          
          String authKey= userName.getTextContent()+":"+ passWord.getTextContent();
          
          JenkinsLogger.log(authKey);
          
          String encodedAuth = encodeAuth(authKey);
          return encodedAuth;//+password.item(0).toString();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }
    
    /*
    Skytap auth Credentical encoded to use in API request
    */
    public static String encodeAuth(String unencodedAuth)
    {
        byte[] encoded =Base64.encodeBase64(unencodedAuth.getBytes());
        String encodedCredential=new String(encoded);
        return encodedCredential;
    }
/**
	 * This method packages an http get request object, given a url and the
	 * encoded Skytap authorization token.
	 * 
	 * @param requestUrl
	 * @param AuthToken
	 * @return
	 */
	public static HttpGet buildHttpGetRequest(String requestUrl,
			String AuthToken) {

		HttpGet hg = new HttpGet(requestUrl);
		String authHeaderValue = "Basic " + AuthToken;

		hg.addHeader("Authorization", authHeaderValue);
		hg.addHeader("Accept", "application/json");//Accept Json Format Result
		hg.addHeader("Content-Type", "application/json");
                
                String Headers=null;
                
                for(int i=0;i<hg.getAllHeaders().length;i++)
                {
                    Headers+=hg.getAllHeaders()[i];
                }

		JenkinsLogger.log("HTTP GET Request: "+ Headers + hg.toString());
		return hg;
	}

	/**
	 * This method packages an http post request object, given a url and the
	 * encoded Skytap authorization token.
	 * 
	 * @param requestUrl
	 * @param AuthToken
	 * @return
	 */
	public static HttpPost buildHttpPostRequest(String requestUrl,
			String AuthToken) {

		HttpPost hp = new HttpPost(requestUrl);
		String authHeaderValue = "Basic " + AuthToken;

		hp.addHeader("Authorization", authHeaderValue);
		hp.addHeader("Accept", "application/json");
		hp.addHeader("Content-Type", "application/json");

		JenkinsLogger.log("HTTP POST Request: " + hp.toString());

		return hp;
	}

	/**
	 * This method returns an http put request object, given a url and the
	 * encoded Skytap authorization token.
	 * 
	 * @param requestUrl
	 * @param AuthToken
	 * @return
	 */
	public static HttpPut buildHttpPutRequest(String requestUrl,
			String AuthToken) {

		HttpPut httpput = new HttpPut(requestUrl);
		String authHeaderValue = "Basic " + AuthToken;

		httpput.addHeader("Authorization", authHeaderValue);
		httpput.addHeader("Accept", "application/json");
		httpput.addHeader("Content-Type", "application/json");

		JenkinsLogger.log("HTTP PUT Request: " + httpput.toString());

		return httpput;
	}

	/**
	 * This method returns an http delete request object, given a url and the
	 * encoded Skytap authorization token.
	 * 
	 * @param requestUrl
	 * @param AuthToken
	 * @return
	 */
	public static HttpDelete buildHttpDeleteRequest(String requestUrl,
			String AuthToken) {

		HttpDelete hd = new HttpDelete(requestUrl);
		String authHeaderValue = "Basic " + AuthToken;

		hd.addHeader("Authorization", authHeaderValue);
		hd.addHeader("Accept", "application/json");
		hd.addHeader("Content-Type", "application/json");

		JenkinsLogger.log("HTTP DELETE Request: " + hd.toString());

		return hd;
	}

	/**
	 * Utility method to execute any type of http request (except delete), to
	 * catch any exceptions thrown and return the response string.
	 * 
	 * @param hr
	 * @return
	 * @throws SkytapException
	 * @throws IOException
	 * @throws ParseException
	 */
	public static String executeHttpRequest(HttpRequestBase hr)
			throws SkytapException {

		boolean retryHttpRequest = true;
		int retryCount = 1;
		String responseString = "";
		while (retryHttpRequest == true) {
			HttpClient httpclient = new DefaultHttpClient();
			//
			// Set timeouts for httpclient requests to 60 seconds
			//
			HttpConnectionParams.setConnectionTimeout(httpclient.getParams(),
					60000);
			HttpConnectionParams.setSoTimeout(httpclient.getParams(), 60000);
			//
			responseString = "";
			HttpResponse response = null;
			try {
				Date myDate = new Date();
				SimpleDateFormat sdf = new SimpleDateFormat(
						"yyyy-MM-dd:HH-mm-ss");
				String myDateString = sdf.format(myDate);

				JenkinsLogger.log(myDateString + "\n" + "Executing Request: "
						+ hr.getRequestLine());
				response = httpclient.execute(hr);

				String responseStatusLine = response.getStatusLine().toString();
				if (responseStatusLine.contains("423 Locked")) {
					retryCount = retryCount + 1;
					if (retryCount > 5) {
						retryHttpRequest = false;
						JenkinsLogger
								.error("Object busy too long - giving up.");
					} else {
						JenkinsLogger.log("Object busy - Retrying...");
						try {
							Thread.sleep(15000);
						} catch (InterruptedException e1) {
							JenkinsLogger.error(e1.getMessage());
						}
					}
				} else if (responseStatusLine.contains("409 Conflict")) {

					throw new SkytapException(responseStatusLine);

				} else {

					JenkinsLogger.log(response.getStatusLine().toString());
					HttpEntity entity = response.getEntity();
					responseString = EntityUtils.toString(entity, "UTF-8");
					retryHttpRequest = false;
				}

			}/* catch (HttpResponseException e) {
				retryHttpRequest = false;
				JenkinsLogger.error("HTTP Response Code: " + e.getStatusCode());

			}*/ catch (InterruptedIOException e) {
				Date myDate = new Date();
				SimpleDateFormat sdf = new SimpleDateFormat(
						"yyyy-MM-dd:HH-mm-ss");
				String myDateString = sdf.format(myDate);

				retryCount = retryCount + 1;
				if (retryCount > 5) {
					retryHttpRequest = false;
					JenkinsLogger.error("API Timeout - giving up. "
							+ e.getMessage());
				} else {
					JenkinsLogger.log(myDateString + "\n" + e.getMessage()
							+ "\n" + "API Timeout - Retrying...");
				}
			} catch (IOException e) {
				retryHttpRequest = false;
				JenkinsLogger.error(e.getMessage());
			} finally {
				if (response != null) {
					// response will be null if this is a timeout retry
					HttpEntity entity = response.getEntity();
					try {
						responseString = EntityUtils.toString(entity, "UTF-8");
					} catch (IOException e) {
						JenkinsLogger.error(e.getMessage());
					}
				}

				httpclient.getConnectionManager().shutdown();
			}
		}

		return responseString;

	}
    
        
        	/**
	 * Utility method to extract errors, if any, from the Skytap json response,
	 * and throw an exception which can be handled by the caller.
	 * 
	 * @param response
	 * @throws SkytapException
	 */
	public static void checkResponseForErrors(String response)
			throws SkytapException {

		// check skytap response body for errors
		JsonParser parser = new JsonParser();
		JsonElement je = parser.parse(response);
		JsonObject jo = null;

		if (je.isJsonNull()) {
			return;
		} else if (je.isJsonArray()) {
			return;
		}

		je = parser.parse(response);
		jo = je.getAsJsonObject();

		if (!(jo.has("error") || jo.has("errors"))) {
			return;
		}

		// handle case where skytap returns an array of errors
		if (jo.has("errors")) {

			String errorString = "";

			JsonArray skytapErrors = (JsonArray) je.getAsJsonObject().get(
					"errors");

			Iterator itr = skytapErrors.iterator();
			while (itr.hasNext()) {
				JsonElement errorElem = (JsonElement) itr.next();
				String errMsg = errorElem.toString();

				errorString += errMsg + "\n";

			}

			throw new SkytapException(errorString);

		}

		if (jo.has("error")) {

			// handle case where 'error' element is null value
			if (jo.get("error").isJsonNull()) {
				return;
			}

			// handle case where 'error' element is a boolean OR quoted string
			if (jo.get("error").isJsonPrimitive()) {

				String error = jo.get("error").getAsString();

				// handle boolean cases
				if (error.equals("false")) {
					return;
				}

				// TODO: find out where the error msg would be in this case
				if (error.equals("true")) {
					throw new SkytapException(error);
				}

				if (!error.equals("")) {
					throw new SkytapException(error);
				}
			}

		}

	}
     
        public static String GetConfigure()
         {
        String response;
        
        String auth= getAuthentication();
        
        String rqConf="https://cloud.skytap.com/users/63076";
        
        HttpGet hg;
        
        JenkinsLogger.log(auth);
        
        hg=buildHttpGetRequest(rqConf, auth);
        try{
        response= executeHttpRequest(hg);
        }
        catch (Exception e)
        {
              JenkinsLogger.log(e.toString());
              response="Error";
        }
        
        return response; 
        }
 
}
