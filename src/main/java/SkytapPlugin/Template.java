/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SkytapPlugin;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.http.HttpResponse;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;

/**
 *
 * @author Administrator
 */
public class Template {

    private final String templateID;
    private static final int NUMBER_OF_RETRIES = 18;
    private static final int RETRY_INTERVAL_SECONDS = 10;

    Template(String tplID) {
        this.templateID = tplID;
    }

    public String getTemplateID() {
        return templateID;
    }

    private static String buildCheckTemplateURL(String id) {

        JenkinsLogger.log("Building request url ...");

        StringBuilder sb = new StringBuilder("https://cloud.skytap.com/");
        sb.append("templates/");
        sb.append(id);

        JenkinsLogger.log("Request URL: " + sb.toString());
        return sb.toString();

        // https://cloud.skytap.com/templates/322249
    }

    /*
     Check if the template is avaliable
     */
    public Boolean checkTemplate() {
        Boolean templateAvailable = false;

        JenkinsLogger.log("Checking availability of template with id: "
                + templateID);

        // build busy check request
        String requestURL = buildCheckTemplateURL(templateID);
        String authCredentials = SkytapUtils.getAuthentication();

        HttpGet hg = SkytapUtils.buildHttpGetRequest(requestURL,
                authCredentials);

        // repeatedly execute request until template is not busy
        String httpRespBody;

        try {
            int attemptsTime = 0;
            while (!templateAvailable && (attemptsTime < NUMBER_OF_RETRIES)) {
                httpRespBody = SkytapUtils.executeHttpRequest(hg);

                JsonParser parser = new JsonParser();

                JsonElement je = parser.parse(httpRespBody);
                JsonObject jo = je.getAsJsonObject();

                if (jo.get("busy").isJsonNull()) {
                    templateAvailable = true;
                    JenkinsLogger.log("Template is available.");
                } else {
                    templateAvailable = false;
                    JenkinsLogger.log("Template is busy.");

                    // wait before trying again
                    int sleepTime = RETRY_INTERVAL_SECONDS;
                    JenkinsLogger.log("Sleeping for " + sleepTime + " seconds.");
                    Thread.sleep(sleepTime * 1000);

                    if (attemptsTime == NUMBER_OF_RETRIES - 1) {
                        JenkinsLogger.log("Load Template Time Out.");
                    }
                }

                attemptsTime++;
            }
        } catch (SkytapException ex) {
            JenkinsLogger.error("Request returned an error: " + ex.getError());
            JenkinsLogger.error("Failing build step.");
            return false;
        } catch (InterruptedException e1) {
            JenkinsLogger.error("Request: " + e1.getMessage());
            return false;
        }

        return templateAvailable;
    }

    /*
     This function is for get the first configration ID by create first VM from Template, If return null mean create fail 
     */
    public String createConf() {
        String confID = null;

        if (!this.checkTemplate()) {
            JenkinsLogger.error("Template Not avliable" + this.templateID);
            return confID;
        }

        String requestURL = buildCreateConfigRequestURL(templateID);

        String auth = SkytapUtils.getAuthentication();

        HttpPost hp = SkytapUtils.buildHttpPostRequest(requestURL,
                auth);

        String httpRespBody = "";

        try {
            httpRespBody = SkytapUtils.executeHttpRequest(hp);
        } catch (SkytapException e1) {
            JenkinsLogger.error("Skytap Exception: " + e1.getMessage());
            return null;
        }

        try {
            SkytapUtils.checkResponseForErrors(httpRespBody);
        } catch (SkytapException ex) {
            JenkinsLogger.error("Request returned an error: " + ex.getError());
            JenkinsLogger.error("Failing build step.");
            return null;
        }

        // get json object from the response
        JsonParser parser = new JsonParser();
        JsonElement je = parser.parse(httpRespBody);
        JsonObject jo = je.getAsJsonObject();

        confID = jo.get("id").getAsString();

        return confID;
    }

    private String buildCreateConfigRequestURL(String tplId) {

        JenkinsLogger.log("Building request url ...");

        StringBuilder sb = new StringBuilder("https://cloud.skytap.com/");
        sb.append("configurations/");
        sb.append("?template_id=");
        sb.append(tplId);

        JenkinsLogger.log("Request URL: " + sb.toString());
        return sb.toString();

    }
}
