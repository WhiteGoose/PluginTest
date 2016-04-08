/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package SkytapPlugin;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.http.client.methods.HttpGet;

/**
 *
 * @author Administrator
 */
public class Configuration {
    
    private String configurationID;
    private String configurationStatus;
    private VirtualMachine[] vmList;
    
    
    
    Configuration(String confID) {
        this.configurationID = confID;

        String auth = SkytapUtils.getAuthentication();
        String req = buildConfRequestURL(confID);
        HttpGet hg = SkytapUtils.buildHttpGetRequest(req, auth);

        String httpRespBody = "";
        try {
            httpRespBody = SkytapUtils.executeHttpRequest(hg);
        } catch (SkytapException e1) {
            JenkinsLogger.error("Skytap Error: " + e1.getMessage());
        }
        
        JsonParser parser = new JsonParser();
        JsonElement je = parser.parse(httpRespBody);
        JsonObject jo = je.getAsJsonObject();
        
        this.configurationStatus = jo.get("runstate").getAsString();

    }
    
    public String getID()
    {
        return this.configurationID;
    }

    public String getSatus() {
        return this.configurationStatus;
    }

    public VirtualMachine[] getVMList() {
        return this.vmList;
    }

    public String buildConfRequestURL(String ConfID)
    {
      
        JenkinsLogger.log("Building Configuration request url ...");

        StringBuilder sb = new StringBuilder("https://cloud.skytap.com/");
        sb.append("configurations/");
        sb.append(ConfID);
        sb.append(".json");
        

        JenkinsLogger.log("Request URL: " + sb.toString());
        return sb.toString();
    
    }
}
