/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
/*
 * This code was generated by https://code.google.com/p/google-apis-client-generator/
 * (build: 2014-11-17 18:43:33 UTC)
 * on 2014-11-29 at 12:45:50 UTC 
 * Modify at your own risk.
 */

package com.trip.expensemanager.tripendpoint;

/**
 * Service definition for Tripendpoint (v1).
 *
 * <p>
 * This is an API
 * </p>
 *
 * <p>
 * For more information about this service, see the
 * <a href="" target="_blank">API Documentation</a>
 * </p>
 *
 * <p>
 * This service uses {@link TripendpointRequestInitializer} to initialize global parameters via its
 * {@link Builder}.
 * </p>
 *
 * @since 1.3
 * @author Google, Inc.
 */
@SuppressWarnings("javadoc")
public class Tripendpoint extends com.google.api.client.googleapis.services.json.AbstractGoogleJsonClient {

  // Note: Leave this static initializer at the top of the file.
  static {
    com.google.api.client.util.Preconditions.checkState(
        com.google.api.client.googleapis.GoogleUtils.MAJOR_VERSION == 1 &&
        com.google.api.client.googleapis.GoogleUtils.MINOR_VERSION >= 15,
        "You are currently running with version %s of google-api-client. " +
        "You need at least version 1.15 of google-api-client to run version " +
        "1.18.0-rc of the tripendpoint library.", com.google.api.client.googleapis.GoogleUtils.VERSION);
  }

  /**
   * The default encoded root URL of the service. This is determined when the library is generated
   * and normally should not be changed.
   *
   * @since 1.7
   */
  public static final String DEFAULT_ROOT_URL = "https://healthy-dolphin-679.appspot.com/_ah/api/";

  /**
   * The default encoded service path of the service. This is determined when the library is
   * generated and normally should not be changed.
   *
   * @since 1.7
   */
  public static final String DEFAULT_SERVICE_PATH = "tripendpoint/v1/";

  /**
   * The default encoded base URL of the service. This is determined when the library is generated
   * and normally should not be changed.
   */
  public static final String DEFAULT_BASE_URL = DEFAULT_ROOT_URL + DEFAULT_SERVICE_PATH;

  /**
   * Constructor.
   *
   * <p>
   * Use {@link Builder} if you need to specify any of the optional parameters.
   * </p>
   *
   * @param transport HTTP transport, which should normally be:
   *        <ul>
   *        <li>Google App Engine:
   *        {@code com.google.api.client.extensions.appengine.http.UrlFetchTransport}</li>
   *        <li>Android: {@code newCompatibleTransport} from
   *        {@code com.google.api.client.extensions.android.http.AndroidHttp}</li>
   *        <li>Java: {@link com.google.api.client.googleapis.javanet.GoogleNetHttpTransport#newTrustedTransport()}
   *        </li>
   *        </ul>
   * @param jsonFactory JSON factory, which may be:
   *        <ul>
   *        <li>Jackson: {@code com.google.api.client.json.jackson2.JacksonFactory}</li>
   *        <li>Google GSON: {@code com.google.api.client.json.gson.GsonFactory}</li>
   *        <li>Android Honeycomb or higher:
   *        {@code com.google.api.client.extensions.android.json.AndroidJsonFactory}</li>
   *        </ul>
   * @param httpRequestInitializer HTTP request initializer or {@code null} for none
   * @since 1.7
   */
  public Tripendpoint(com.google.api.client.http.HttpTransport transport, com.google.api.client.json.JsonFactory jsonFactory,
      com.google.api.client.http.HttpRequestInitializer httpRequestInitializer) {
    this(new Builder(transport, jsonFactory, httpRequestInitializer));
  }

  /**
   * @param builder builder
   */
  Tripendpoint(Builder builder) {
    super(builder);
  }

  @Override
  protected void initialize(com.google.api.client.googleapis.services.AbstractGoogleClientRequest<?> httpClientRequest) throws java.io.IOException {
    super.initialize(httpClientRequest);
  }

  /**
   * Create a request for the method "getTrip".
   *
   * This request holds the parameters needed by the tripendpoint server.  After setting any optional
   * parameters, call the {@link GetTrip#execute()} method to invoke the remote operation.
   *
   * @param id
   * @return the request
   */
  public GetTrip getTrip(java.lang.Long id) throws java.io.IOException {
    GetTrip result = new GetTrip(id);
    initialize(result);
    return result;
  }

  public class GetTrip extends TripendpointRequest<com.trip.expensemanager.tripendpoint.model.Trip> {

    private static final String REST_PATH = "trip/{id}";

    /**
     * Create a request for the method "getTrip".
     *
     * This request holds the parameters needed by the the tripendpoint server.  After setting any
     * optional parameters, call the {@link GetTrip#execute()} method to invoke the remote operation.
     * <p> {@link
     * GetTrip#initialize(com.google.api.client.googleapis.services.AbstractGoogleClientRequest)} must
     * be called to initialize this instance immediately after invoking the constructor. </p>
     *
     * @param id
     * @since 1.13
     */
    protected GetTrip(java.lang.Long id) {
      super(Tripendpoint.this, "GET", REST_PATH, null, com.trip.expensemanager.tripendpoint.model.Trip.class);
      this.id = com.google.api.client.util.Preconditions.checkNotNull(id, "Required parameter id must be specified.");
    }

    @Override
    public com.google.api.client.http.HttpResponse executeUsingHead() throws java.io.IOException {
      return super.executeUsingHead();
    }

    @Override
    public com.google.api.client.http.HttpRequest buildHttpRequestUsingHead() throws java.io.IOException {
      return super.buildHttpRequestUsingHead();
    }

    @Override
    public GetTrip setAlt(java.lang.String alt) {
      return (GetTrip) super.setAlt(alt);
    }

    @Override
    public GetTrip setFields(java.lang.String fields) {
      return (GetTrip) super.setFields(fields);
    }

    @Override
    public GetTrip setKey(java.lang.String key) {
      return (GetTrip) super.setKey(key);
    }

    @Override
    public GetTrip setOauthToken(java.lang.String oauthToken) {
      return (GetTrip) super.setOauthToken(oauthToken);
    }

    @Override
    public GetTrip setPrettyPrint(java.lang.Boolean prettyPrint) {
      return (GetTrip) super.setPrettyPrint(prettyPrint);
    }

    @Override
    public GetTrip setQuotaUser(java.lang.String quotaUser) {
      return (GetTrip) super.setQuotaUser(quotaUser);
    }

    @Override
    public GetTrip setUserIp(java.lang.String userIp) {
      return (GetTrip) super.setUserIp(userIp);
    }

    @com.google.api.client.util.Key
    private java.lang.Long id;

    /**

     */
    public java.lang.Long getId() {
      return id;
    }

    public GetTrip setId(java.lang.Long id) {
      this.id = id;
      return this;
    }

    @Override
    public GetTrip set(String parameterName, Object value) {
      return (GetTrip) super.set(parameterName, value);
    }
  }

  /**
   * Create a request for the method "insertTrip".
   *
   * This request holds the parameters needed by the tripendpoint server.  After setting any optional
   * parameters, call the {@link InsertTrip#execute()} method to invoke the remote operation.
   *
   * @param content the {@link com.trip.expensemanager.tripendpoint.model.Trip}
   * @return the request
   */
  public InsertTrip insertTrip(com.trip.expensemanager.tripendpoint.model.Trip content) throws java.io.IOException {
    InsertTrip result = new InsertTrip(content);
    initialize(result);
    return result;
  }

  public class InsertTrip extends TripendpointRequest<com.trip.expensemanager.tripendpoint.model.Trip> {

    private static final String REST_PATH = "trip";

    /**
     * Create a request for the method "insertTrip".
     *
     * This request holds the parameters needed by the the tripendpoint server.  After setting any
     * optional parameters, call the {@link InsertTrip#execute()} method to invoke the remote
     * operation. <p> {@link
     * InsertTrip#initialize(com.google.api.client.googleapis.services.AbstractGoogleClientRequest)}
     * must be called to initialize this instance immediately after invoking the constructor. </p>
     *
     * @param content the {@link com.trip.expensemanager.tripendpoint.model.Trip}
     * @since 1.13
     */
    protected InsertTrip(com.trip.expensemanager.tripendpoint.model.Trip content) {
      super(Tripendpoint.this, "POST", REST_PATH, content, com.trip.expensemanager.tripendpoint.model.Trip.class);
    }

    @Override
    public InsertTrip setAlt(java.lang.String alt) {
      return (InsertTrip) super.setAlt(alt);
    }

    @Override
    public InsertTrip setFields(java.lang.String fields) {
      return (InsertTrip) super.setFields(fields);
    }

    @Override
    public InsertTrip setKey(java.lang.String key) {
      return (InsertTrip) super.setKey(key);
    }

    @Override
    public InsertTrip setOauthToken(java.lang.String oauthToken) {
      return (InsertTrip) super.setOauthToken(oauthToken);
    }

    @Override
    public InsertTrip setPrettyPrint(java.lang.Boolean prettyPrint) {
      return (InsertTrip) super.setPrettyPrint(prettyPrint);
    }

    @Override
    public InsertTrip setQuotaUser(java.lang.String quotaUser) {
      return (InsertTrip) super.setQuotaUser(quotaUser);
    }

    @Override
    public InsertTrip setUserIp(java.lang.String userIp) {
      return (InsertTrip) super.setUserIp(userIp);
    }

    @Override
    public InsertTrip set(String parameterName, Object value) {
      return (InsertTrip) super.set(parameterName, value);
    }
  }

  /**
   * Create a request for the method "listTrip".
   *
   * This request holds the parameters needed by the tripendpoint server.  After setting any optional
   * parameters, call the {@link ListTrip#execute()} method to invoke the remote operation.
   *
   * @return the request
   */
  public ListTrip listTrip() throws java.io.IOException {
    ListTrip result = new ListTrip();
    initialize(result);
    return result;
  }

  public class ListTrip extends TripendpointRequest<com.trip.expensemanager.tripendpoint.model.CollectionResponseTrip> {

    private static final String REST_PATH = "trip";

    /**
     * Create a request for the method "listTrip".
     *
     * This request holds the parameters needed by the the tripendpoint server.  After setting any
     * optional parameters, call the {@link ListTrip#execute()} method to invoke the remote operation.
     * <p> {@link
     * ListTrip#initialize(com.google.api.client.googleapis.services.AbstractGoogleClientRequest)}
     * must be called to initialize this instance immediately after invoking the constructor. </p>
     *
     * @since 1.13
     */
    protected ListTrip() {
      super(Tripendpoint.this, "GET", REST_PATH, null, com.trip.expensemanager.tripendpoint.model.CollectionResponseTrip.class);
    }

    @Override
    public com.google.api.client.http.HttpResponse executeUsingHead() throws java.io.IOException {
      return super.executeUsingHead();
    }

    @Override
    public com.google.api.client.http.HttpRequest buildHttpRequestUsingHead() throws java.io.IOException {
      return super.buildHttpRequestUsingHead();
    }

    @Override
    public ListTrip setAlt(java.lang.String alt) {
      return (ListTrip) super.setAlt(alt);
    }

    @Override
    public ListTrip setFields(java.lang.String fields) {
      return (ListTrip) super.setFields(fields);
    }

    @Override
    public ListTrip setKey(java.lang.String key) {
      return (ListTrip) super.setKey(key);
    }

    @Override
    public ListTrip setOauthToken(java.lang.String oauthToken) {
      return (ListTrip) super.setOauthToken(oauthToken);
    }

    @Override
    public ListTrip setPrettyPrint(java.lang.Boolean prettyPrint) {
      return (ListTrip) super.setPrettyPrint(prettyPrint);
    }

    @Override
    public ListTrip setQuotaUser(java.lang.String quotaUser) {
      return (ListTrip) super.setQuotaUser(quotaUser);
    }

    @Override
    public ListTrip setUserIp(java.lang.String userIp) {
      return (ListTrip) super.setUserIp(userIp);
    }

    @com.google.api.client.util.Key
    private java.lang.String cursor;

    /**

     */
    public java.lang.String getCursor() {
      return cursor;
    }

    public ListTrip setCursor(java.lang.String cursor) {
      this.cursor = cursor;
      return this;
    }

    @com.google.api.client.util.Key
    private java.lang.Integer limit;

    /**

     */
    public java.lang.Integer getLimit() {
      return limit;
    }

    public ListTrip setLimit(java.lang.Integer limit) {
      this.limit = limit;
      return this;
    }

    @Override
    public ListTrip set(String parameterName, Object value) {
      return (ListTrip) super.set(parameterName, value);
    }
  }

  /**
   * Create a request for the method "removeTrip".
   *
   * This request holds the parameters needed by the tripendpoint server.  After setting any optional
   * parameters, call the {@link RemoveTrip#execute()} method to invoke the remote operation.
   *
   * @param id
   * @return the request
   */
  public RemoveTrip removeTrip(java.lang.Long id) throws java.io.IOException {
    RemoveTrip result = new RemoveTrip(id);
    initialize(result);
    return result;
  }

  public class RemoveTrip extends TripendpointRequest<Void> {

    private static final String REST_PATH = "trip/{id}";

    /**
     * Create a request for the method "removeTrip".
     *
     * This request holds the parameters needed by the the tripendpoint server.  After setting any
     * optional parameters, call the {@link RemoveTrip#execute()} method to invoke the remote
     * operation. <p> {@link
     * RemoveTrip#initialize(com.google.api.client.googleapis.services.AbstractGoogleClientRequest)}
     * must be called to initialize this instance immediately after invoking the constructor. </p>
     *
     * @param id
     * @since 1.13
     */
    protected RemoveTrip(java.lang.Long id) {
      super(Tripendpoint.this, "DELETE", REST_PATH, null, Void.class);
      this.id = com.google.api.client.util.Preconditions.checkNotNull(id, "Required parameter id must be specified.");
    }

    @Override
    public RemoveTrip setAlt(java.lang.String alt) {
      return (RemoveTrip) super.setAlt(alt);
    }

    @Override
    public RemoveTrip setFields(java.lang.String fields) {
      return (RemoveTrip) super.setFields(fields);
    }

    @Override
    public RemoveTrip setKey(java.lang.String key) {
      return (RemoveTrip) super.setKey(key);
    }

    @Override
    public RemoveTrip setOauthToken(java.lang.String oauthToken) {
      return (RemoveTrip) super.setOauthToken(oauthToken);
    }

    @Override
    public RemoveTrip setPrettyPrint(java.lang.Boolean prettyPrint) {
      return (RemoveTrip) super.setPrettyPrint(prettyPrint);
    }

    @Override
    public RemoveTrip setQuotaUser(java.lang.String quotaUser) {
      return (RemoveTrip) super.setQuotaUser(quotaUser);
    }

    @Override
    public RemoveTrip setUserIp(java.lang.String userIp) {
      return (RemoveTrip) super.setUserIp(userIp);
    }

    @com.google.api.client.util.Key
    private java.lang.Long id;

    /**

     */
    public java.lang.Long getId() {
      return id;
    }

    public RemoveTrip setId(java.lang.Long id) {
      this.id = id;
      return this;
    }

    @Override
    public RemoveTrip set(String parameterName, Object value) {
      return (RemoveTrip) super.set(parameterName, value);
    }
  }

  /**
   * Create a request for the method "updateTrip".
   *
   * This request holds the parameters needed by the tripendpoint server.  After setting any optional
   * parameters, call the {@link UpdateTrip#execute()} method to invoke the remote operation.
   *
   * @param content the {@link com.trip.expensemanager.tripendpoint.model.Trip}
   * @return the request
   */
  public UpdateTrip updateTrip(com.trip.expensemanager.tripendpoint.model.Trip content) throws java.io.IOException {
    UpdateTrip result = new UpdateTrip(content);
    initialize(result);
    return result;
  }

  public class UpdateTrip extends TripendpointRequest<com.trip.expensemanager.tripendpoint.model.Trip> {

    private static final String REST_PATH = "trip";

    /**
     * Create a request for the method "updateTrip".
     *
     * This request holds the parameters needed by the the tripendpoint server.  After setting any
     * optional parameters, call the {@link UpdateTrip#execute()} method to invoke the remote
     * operation. <p> {@link
     * UpdateTrip#initialize(com.google.api.client.googleapis.services.AbstractGoogleClientRequest)}
     * must be called to initialize this instance immediately after invoking the constructor. </p>
     *
     * @param content the {@link com.trip.expensemanager.tripendpoint.model.Trip}
     * @since 1.13
     */
    protected UpdateTrip(com.trip.expensemanager.tripendpoint.model.Trip content) {
      super(Tripendpoint.this, "PUT", REST_PATH, content, com.trip.expensemanager.tripendpoint.model.Trip.class);
    }

    @Override
    public UpdateTrip setAlt(java.lang.String alt) {
      return (UpdateTrip) super.setAlt(alt);
    }

    @Override
    public UpdateTrip setFields(java.lang.String fields) {
      return (UpdateTrip) super.setFields(fields);
    }

    @Override
    public UpdateTrip setKey(java.lang.String key) {
      return (UpdateTrip) super.setKey(key);
    }

    @Override
    public UpdateTrip setOauthToken(java.lang.String oauthToken) {
      return (UpdateTrip) super.setOauthToken(oauthToken);
    }

    @Override
    public UpdateTrip setPrettyPrint(java.lang.Boolean prettyPrint) {
      return (UpdateTrip) super.setPrettyPrint(prettyPrint);
    }

    @Override
    public UpdateTrip setQuotaUser(java.lang.String quotaUser) {
      return (UpdateTrip) super.setQuotaUser(quotaUser);
    }

    @Override
    public UpdateTrip setUserIp(java.lang.String userIp) {
      return (UpdateTrip) super.setUserIp(userIp);
    }

    @Override
    public UpdateTrip set(String parameterName, Object value) {
      return (UpdateTrip) super.set(parameterName, value);
    }
  }

  /**
   * Builder for {@link Tripendpoint}.
   *
   * <p>
   * Implementation is not thread-safe.
   * </p>
   *
   * @since 1.3.0
   */
  public static final class Builder extends com.google.api.client.googleapis.services.json.AbstractGoogleJsonClient.Builder {

    /**
     * Returns an instance of a new builder.
     *
     * @param transport HTTP transport, which should normally be:
     *        <ul>
     *        <li>Google App Engine:
     *        {@code com.google.api.client.extensions.appengine.http.UrlFetchTransport}</li>
     *        <li>Android: {@code newCompatibleTransport} from
     *        {@code com.google.api.client.extensions.android.http.AndroidHttp}</li>
     *        <li>Java: {@link com.google.api.client.googleapis.javanet.GoogleNetHttpTransport#newTrustedTransport()}
     *        </li>
     *        </ul>
     * @param jsonFactory JSON factory, which may be:
     *        <ul>
     *        <li>Jackson: {@code com.google.api.client.json.jackson2.JacksonFactory}</li>
     *        <li>Google GSON: {@code com.google.api.client.json.gson.GsonFactory}</li>
     *        <li>Android Honeycomb or higher:
     *        {@code com.google.api.client.extensions.android.json.AndroidJsonFactory}</li>
     *        </ul>
     * @param httpRequestInitializer HTTP request initializer or {@code null} for none
     * @since 1.7
     */
    public Builder(com.google.api.client.http.HttpTransport transport, com.google.api.client.json.JsonFactory jsonFactory,
        com.google.api.client.http.HttpRequestInitializer httpRequestInitializer) {
      super(
          transport,
          jsonFactory,
          DEFAULT_ROOT_URL,
          DEFAULT_SERVICE_PATH,
          httpRequestInitializer,
          false);
    }

    /** Builds a new instance of {@link Tripendpoint}. */
    @Override
    public Tripendpoint build() {
      return new Tripendpoint(this);
    }

    @Override
    public Builder setRootUrl(String rootUrl) {
      return (Builder) super.setRootUrl(rootUrl);
    }

    @Override
    public Builder setServicePath(String servicePath) {
      return (Builder) super.setServicePath(servicePath);
    }

    @Override
    public Builder setHttpRequestInitializer(com.google.api.client.http.HttpRequestInitializer httpRequestInitializer) {
      return (Builder) super.setHttpRequestInitializer(httpRequestInitializer);
    }

    @Override
    public Builder setApplicationName(String applicationName) {
      return (Builder) super.setApplicationName(applicationName);
    }

    @Override
    public Builder setSuppressPatternChecks(boolean suppressPatternChecks) {
      return (Builder) super.setSuppressPatternChecks(suppressPatternChecks);
    }

    @Override
    public Builder setSuppressRequiredParameterChecks(boolean suppressRequiredParameterChecks) {
      return (Builder) super.setSuppressRequiredParameterChecks(suppressRequiredParameterChecks);
    }

    @Override
    public Builder setSuppressAllChecks(boolean suppressAllChecks) {
      return (Builder) super.setSuppressAllChecks(suppressAllChecks);
    }

    /**
     * Set the {@link TripendpointRequestInitializer}.
     *
     * @since 1.12
     */
    public Builder setTripendpointRequestInitializer(
        TripendpointRequestInitializer tripendpointRequestInitializer) {
      return (Builder) super.setGoogleClientRequestInitializer(tripendpointRequestInitializer);
    }

    @Override
    public Builder setGoogleClientRequestInitializer(
        com.google.api.client.googleapis.services.GoogleClientRequestInitializer googleClientRequestInitializer) {
      return (Builder) super.setGoogleClientRequestInitializer(googleClientRequestInitializer);
    }
  }
}
