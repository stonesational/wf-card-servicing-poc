/**
 * Copyright (c) 2011, salesforce.com, inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided
 * that the following conditions are met:
 *
 *    Redistributions of source code must retain the above copyright notice, this list of conditions and the
 *    following disclaimer.
 *
 *    Redistributions in binary form must reproduce the above copyright notice, this list of conditions and
 *    the following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 *    Neither the name of salesforce.com, inc. nor the names of its contributors may be used to endorse or
 *    promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package servlets;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.PostMethod;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * A Servlet for handeling OAuth flow.
 * This OAuth Servlet  is only provided as an example and is provided as-is
 */
//@WebServlet("/oauthwebserverflow")
public class OAuthServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    public static final String ACCESS_TOKEN = "ACCESS_TOKEN";
    public static final String INSTANCE_URL = "INSTANCE_URL";

    private String clientId     = null;
    private String clientSecret = null;
    private String redirectUri  = null;
    private String authUrl      = null;
    private String tokenUrl     = null;

    public void init() throws ServletException {
        System.out.println("STONE: START on servlet init()");
        String environment;

        /*clientId = this.getInitParameter("clientId");
        clientSecret = this.getInitParameter("clientSecret");
        redirectUri = this.getInitParameter("redirectUri");    // https://canvas.herokuapp.com/oauth/_callback
        environment = this.getInitParameter("environment");    // https://login.salesforce.com */

        clientId = "3MVG9A2kN3Bn17hudSDZs_Sd4e_URGVd3.aT9D9yvsykf9KR5683gzcLmzKXxOSyk7rVsewCN4DSdLbvBELi3";
        clientSecret = "4415624878435633409";
        redirectUri = "https://wf-card-servicing-poc.herokuapp.com/oauth_callback";    // https://canvas.herokuapp.com/oauth/_callback
        environment = "https://login.salesforce.com";    // https://login.salesforce.com


        try {
            authUrl = environment
                    + "/services/oauth2/authorize?response_type=code&client_id="
                    + clientId + "&redirect_uri="
                    + URLEncoder.encode(redirectUri, "UTF-8");

            // Nots: &scope=email,read_chatter,... would be added here for oauth scope

        } catch (UnsupportedEncodingException e) {
            System.out.println("STONE: Error on servlet init()");
            throw new ServletException(e);
        }

        tokenUrl = environment + "/services/oauth2/token";
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        System.out.println("Begin OAuth");

        String accessToken = (String) request.getSession().getAttribute(ACCESS_TOKEN);

        System.out.println("ACCESS_TOKEN: "+accessToken);

        if (accessToken == null) {

            String instanceUrl = null;


            System.out.println("RequestURI: "+request.getRequestURI());
            

            if (request.getRequestURI().endsWith("oauth")) {
                // we need to send the user to authorize
                response.sendRedirect(authUrl);
                return;
            } else {
                System.out.println("Auth successful - got callback");

                String code = request.getParameter("code");

                HttpClient httpclient = new HttpClient();

                PostMethod post = new PostMethod(tokenUrl);
                post.addParameter("code", code);
                post.addParameter("grant_type", "authorization_code");
                post.addParameter("client_id", clientId);
                post.addParameter("client_secret", clientSecret);
                post.addParameter("redirect_uri", redirectUri);

                try {
                    httpclient.executeMethod(post);

                    try {
                        JSONObject authResponse = new JSONObject(
                                new JSONTokener(new InputStreamReader(
                                        post.getResponseBodyAsStream())));
                        System.out.println("xAuth response: "
                                + authResponse.toString(2));

                        accessToken = authResponse.getString("access_token");

                        // Instance URL is Salesforce specific.
                        instanceUrl = authResponse.getString("instance_url");

                        System.out.println("Got access token: " + accessToken);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        throw new ServletException(e);
                    }
                } catch (HttpException e) {
                    e.printStackTrace();
                    throw new ServletException(e);
                }
                finally {
                    post.releaseConnection();
                }
            }

            // Set a session attribute so that other servlets can get the access
            // token
            request.getSession().setAttribute(ACCESS_TOKEN, accessToken);

            // We also get the instance URL from the OAuth response, so set it
            // in the session too
            request.getSession().setAttribute(INSTANCE_URL, instanceUrl);
        }

        //response.sendRedirect(request.getContextPath() + "/index.html");
        response.sendRedirect(request.getContextPath() + "/sdk/callback.html");
    }
}
