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
 * on 2014-11-29 at 12:45:15 UTC 
 * Modify at your own risk.
 */

package com.trip.expensemanager.expenseendpoint;

/**
 * Service definition for Expenseendpoint (v1).
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
 * This service uses {@link ExpenseendpointRequestInitializer} to initialize global parameters via its
 * {@link Builder}.
 * </p>
 *
 * @since 1.3
 * @author Google, Inc.
 */
@SuppressWarnings("javadoc")
public class Expenseendpoint extends com.google.api.client.googleapis.services.json.AbstractGoogleJsonClient {

  // Note: Leave this static initializer at the top of the file.
  static {
    com.google.api.client.util.Preconditions.checkState(
        com.google.api.client.googleapis.GoogleUtils.MAJOR_VERSION == 1 &&
        com.google.api.client.googleapis.GoogleUtils.MINOR_VERSION >= 15,
        "You are currently running with version %s of google-api-client. " +
        "You need at least version 1.15 of google-api-client to run version " +
        "1.18.0-rc of the expenseendpoint library.", com.google.api.client.googleapis.GoogleUtils.VERSION);
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
  public static final String DEFAULT_SERVICE_PATH = "expenseendpoint/v1/";

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
  public Expenseendpoint(com.google.api.client.http.HttpTransport transport, com.google.api.client.json.JsonFactory jsonFactory,
      com.google.api.client.http.HttpRequestInitializer httpRequestInitializer) {
    this(new Builder(transport, jsonFactory, httpRequestInitializer));
  }

  /**
   * @param builder builder
   */
  Expenseendpoint(Builder builder) {
    super(builder);
  }

  @Override
  protected void initialize(com.google.api.client.googleapis.services.AbstractGoogleClientRequest<?> httpClientRequest) throws java.io.IOException {
    super.initialize(httpClientRequest);
  }

  /**
   * An accessor for creating requests from the ExpenseEndpoint collection.
   *
   * <p>The typical use is:</p>
   * <pre>
   *   {@code Expenseendpoint expenseendpoint = new Expenseendpoint(...);}
   *   {@code Expenseendpoint.ExpenseEndpoint.List request = expenseendpoint.expenseEndpoint().list(parameters ...)}
   * </pre>
   *
   * @return the resource collection
   */
  public ExpenseEndpoint expenseEndpoint() {
    return new ExpenseEndpoint();
  }

  /**
   * The "expenseEndpoint" collection of methods.
   */
  public class ExpenseEndpoint {

    /**
     * Create a request for the method "expenseEndpoint.removeTripExpense".
     *
     * This request holds the parameters needed by the expenseendpoint server.  After setting any
     * optional parameters, call the {@link RemoveTripExpense#execute()} method to invoke the remote
     * operation.
     *
     * @param id
     * @return the request
     */
    public RemoveTripExpense removeTripExpense(java.lang.Long id) throws java.io.IOException {
      RemoveTripExpense result = new RemoveTripExpense(id);
      initialize(result);
      return result;
    }

    public class RemoveTripExpense extends ExpenseendpointRequest<Void> {

      private static final String REST_PATH = "tripexpense/{id}";

      /**
       * Create a request for the method "expenseEndpoint.removeTripExpense".
       *
       * This request holds the parameters needed by the the expenseendpoint server.  After setting any
       * optional parameters, call the {@link RemoveTripExpense#execute()} method to invoke the remote
       * operation. <p> {@link RemoveTripExpense#initialize(com.google.api.client.googleapis.services.Ab
       * stractGoogleClientRequest)} must be called to initialize this instance immediately after
       * invoking the constructor. </p>
       *
       * @param id
       * @since 1.13
       */
      protected RemoveTripExpense(java.lang.Long id) {
        super(Expenseendpoint.this, "DELETE", REST_PATH, null, Void.class);
        this.id = com.google.api.client.util.Preconditions.checkNotNull(id, "Required parameter id must be specified.");
      }

      @Override
      public RemoveTripExpense setAlt(java.lang.String alt) {
        return (RemoveTripExpense) super.setAlt(alt);
      }

      @Override
      public RemoveTripExpense setFields(java.lang.String fields) {
        return (RemoveTripExpense) super.setFields(fields);
      }

      @Override
      public RemoveTripExpense setKey(java.lang.String key) {
        return (RemoveTripExpense) super.setKey(key);
      }

      @Override
      public RemoveTripExpense setOauthToken(java.lang.String oauthToken) {
        return (RemoveTripExpense) super.setOauthToken(oauthToken);
      }

      @Override
      public RemoveTripExpense setPrettyPrint(java.lang.Boolean prettyPrint) {
        return (RemoveTripExpense) super.setPrettyPrint(prettyPrint);
      }

      @Override
      public RemoveTripExpense setQuotaUser(java.lang.String quotaUser) {
        return (RemoveTripExpense) super.setQuotaUser(quotaUser);
      }

      @Override
      public RemoveTripExpense setUserIp(java.lang.String userIp) {
        return (RemoveTripExpense) super.setUserIp(userIp);
      }

      @com.google.api.client.util.Key
      private java.lang.Long id;

      /**

       */
      public java.lang.Long getId() {
        return id;
      }

      public RemoveTripExpense setId(java.lang.Long id) {
        this.id = id;
        return this;
      }

      @Override
      public RemoveTripExpense set(String parameterName, Object value) {
        return (RemoveTripExpense) super.set(parameterName, value);
      }
    }

  }

  /**
   * Create a request for the method "getExpense".
   *
   * This request holds the parameters needed by the expenseendpoint server.  After setting any
   * optional parameters, call the {@link GetExpense#execute()} method to invoke the remote operation.
   *
   * @param id
   * @return the request
   */
  public GetExpense getExpense(java.lang.Long id) throws java.io.IOException {
    GetExpense result = new GetExpense(id);
    initialize(result);
    return result;
  }

  public class GetExpense extends ExpenseendpointRequest<com.trip.expensemanager.expenseendpoint.model.Expense> {

    private static final String REST_PATH = "expense/{id}";

    /**
     * Create a request for the method "getExpense".
     *
     * This request holds the parameters needed by the the expenseendpoint server.  After setting any
     * optional parameters, call the {@link GetExpense#execute()} method to invoke the remote
     * operation. <p> {@link
     * GetExpense#initialize(com.google.api.client.googleapis.services.AbstractGoogleClientRequest)}
     * must be called to initialize this instance immediately after invoking the constructor. </p>
     *
     * @param id
     * @since 1.13
     */
    protected GetExpense(java.lang.Long id) {
      super(Expenseendpoint.this, "GET", REST_PATH, null, com.trip.expensemanager.expenseendpoint.model.Expense.class);
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
    public GetExpense setAlt(java.lang.String alt) {
      return (GetExpense) super.setAlt(alt);
    }

    @Override
    public GetExpense setFields(java.lang.String fields) {
      return (GetExpense) super.setFields(fields);
    }

    @Override
    public GetExpense setKey(java.lang.String key) {
      return (GetExpense) super.setKey(key);
    }

    @Override
    public GetExpense setOauthToken(java.lang.String oauthToken) {
      return (GetExpense) super.setOauthToken(oauthToken);
    }

    @Override
    public GetExpense setPrettyPrint(java.lang.Boolean prettyPrint) {
      return (GetExpense) super.setPrettyPrint(prettyPrint);
    }

    @Override
    public GetExpense setQuotaUser(java.lang.String quotaUser) {
      return (GetExpense) super.setQuotaUser(quotaUser);
    }

    @Override
    public GetExpense setUserIp(java.lang.String userIp) {
      return (GetExpense) super.setUserIp(userIp);
    }

    @com.google.api.client.util.Key
    private java.lang.Long id;

    /**

     */
    public java.lang.Long getId() {
      return id;
    }

    public GetExpense setId(java.lang.Long id) {
      this.id = id;
      return this;
    }

    @Override
    public GetExpense set(String parameterName, Object value) {
      return (GetExpense) super.set(parameterName, value);
    }
  }

  /**
   * Create a request for the method "insertExpense".
   *
   * This request holds the parameters needed by the expenseendpoint server.  After setting any
   * optional parameters, call the {@link InsertExpense#execute()} method to invoke the remote
   * operation.
   *
   * @param content the {@link com.trip.expensemanager.expenseendpoint.model.Expense}
   * @return the request
   */
  public InsertExpense insertExpense(com.trip.expensemanager.expenseendpoint.model.Expense content) throws java.io.IOException {
    InsertExpense result = new InsertExpense(content);
    initialize(result);
    return result;
  }

  public class InsertExpense extends ExpenseendpointRequest<com.trip.expensemanager.expenseendpoint.model.Expense> {

    private static final String REST_PATH = "expense";

    /**
     * Create a request for the method "insertExpense".
     *
     * This request holds the parameters needed by the the expenseendpoint server.  After setting any
     * optional parameters, call the {@link InsertExpense#execute()} method to invoke the remote
     * operation. <p> {@link InsertExpense#initialize(com.google.api.client.googleapis.services.Abstra
     * ctGoogleClientRequest)} must be called to initialize this instance immediately after invoking
     * the constructor. </p>
     *
     * @param content the {@link com.trip.expensemanager.expenseendpoint.model.Expense}
     * @since 1.13
     */
    protected InsertExpense(com.trip.expensemanager.expenseendpoint.model.Expense content) {
      super(Expenseendpoint.this, "POST", REST_PATH, content, com.trip.expensemanager.expenseendpoint.model.Expense.class);
    }

    @Override
    public InsertExpense setAlt(java.lang.String alt) {
      return (InsertExpense) super.setAlt(alt);
    }

    @Override
    public InsertExpense setFields(java.lang.String fields) {
      return (InsertExpense) super.setFields(fields);
    }

    @Override
    public InsertExpense setKey(java.lang.String key) {
      return (InsertExpense) super.setKey(key);
    }

    @Override
    public InsertExpense setOauthToken(java.lang.String oauthToken) {
      return (InsertExpense) super.setOauthToken(oauthToken);
    }

    @Override
    public InsertExpense setPrettyPrint(java.lang.Boolean prettyPrint) {
      return (InsertExpense) super.setPrettyPrint(prettyPrint);
    }

    @Override
    public InsertExpense setQuotaUser(java.lang.String quotaUser) {
      return (InsertExpense) super.setQuotaUser(quotaUser);
    }

    @Override
    public InsertExpense setUserIp(java.lang.String userIp) {
      return (InsertExpense) super.setUserIp(userIp);
    }

    @Override
    public InsertExpense set(String parameterName, Object value) {
      return (InsertExpense) super.set(parameterName, value);
    }
  }

  /**
   * Create a request for the method "listExpense".
   *
   * This request holds the parameters needed by the expenseendpoint server.  After setting any
   * optional parameters, call the {@link ListExpense#execute()} method to invoke the remote
   * operation.
   *
   * @return the request
   */
  public ListExpense listExpense() throws java.io.IOException {
    ListExpense result = new ListExpense();
    initialize(result);
    return result;
  }

  public class ListExpense extends ExpenseendpointRequest<com.trip.expensemanager.expenseendpoint.model.CollectionResponseExpense> {

    private static final String REST_PATH = "expense";

    /**
     * Create a request for the method "listExpense".
     *
     * This request holds the parameters needed by the the expenseendpoint server.  After setting any
     * optional parameters, call the {@link ListExpense#execute()} method to invoke the remote
     * operation. <p> {@link
     * ListExpense#initialize(com.google.api.client.googleapis.services.AbstractGoogleClientRequest)}
     * must be called to initialize this instance immediately after invoking the constructor. </p>
     *
     * @since 1.13
     */
    protected ListExpense() {
      super(Expenseendpoint.this, "GET", REST_PATH, null, com.trip.expensemanager.expenseendpoint.model.CollectionResponseExpense.class);
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
    public ListExpense setAlt(java.lang.String alt) {
      return (ListExpense) super.setAlt(alt);
    }

    @Override
    public ListExpense setFields(java.lang.String fields) {
      return (ListExpense) super.setFields(fields);
    }

    @Override
    public ListExpense setKey(java.lang.String key) {
      return (ListExpense) super.setKey(key);
    }

    @Override
    public ListExpense setOauthToken(java.lang.String oauthToken) {
      return (ListExpense) super.setOauthToken(oauthToken);
    }

    @Override
    public ListExpense setPrettyPrint(java.lang.Boolean prettyPrint) {
      return (ListExpense) super.setPrettyPrint(prettyPrint);
    }

    @Override
    public ListExpense setQuotaUser(java.lang.String quotaUser) {
      return (ListExpense) super.setQuotaUser(quotaUser);
    }

    @Override
    public ListExpense setUserIp(java.lang.String userIp) {
      return (ListExpense) super.setUserIp(userIp);
    }

    @com.google.api.client.util.Key
    private java.lang.String cursor;

    /**

     */
    public java.lang.String getCursor() {
      return cursor;
    }

    public ListExpense setCursor(java.lang.String cursor) {
      this.cursor = cursor;
      return this;
    }

    @com.google.api.client.util.Key
    private com.google.api.client.util.DateTime date;

    /**

     */
    public com.google.api.client.util.DateTime getDate() {
      return date;
    }

    public ListExpense setDate(com.google.api.client.util.DateTime date) {
      this.date = date;
      return this;
    }

    @com.google.api.client.util.Key
    private java.lang.Integer limit;

    /**

     */
    public java.lang.Integer getLimit() {
      return limit;
    }

    public ListExpense setLimit(java.lang.Integer limit) {
      this.limit = limit;
      return this;
    }

    @com.google.api.client.util.Key
    private java.lang.Long userId;

    /**

     */
    public java.lang.Long getUserId() {
      return userId;
    }

    public ListExpense setUserId(java.lang.Long userId) {
      this.userId = userId;
      return this;
    }

    @com.google.api.client.util.Key
    private java.lang.Long tripId;

    /**

     */
    public java.lang.Long getTripId() {
      return tripId;
    }

    public ListExpense setTripId(java.lang.Long tripId) {
      this.tripId = tripId;
      return this;
    }

    @Override
    public ListExpense set(String parameterName, Object value) {
      return (ListExpense) super.set(parameterName, value);
    }
  }

  /**
   * Create a request for the method "removeExpense".
   *
   * This request holds the parameters needed by the expenseendpoint server.  After setting any
   * optional parameters, call the {@link RemoveExpense#execute()} method to invoke the remote
   * operation.
   *
   * @param id
   * @return the request
   */
  public RemoveExpense removeExpense(java.lang.Long id) throws java.io.IOException {
    RemoveExpense result = new RemoveExpense(id);
    initialize(result);
    return result;
  }

  public class RemoveExpense extends ExpenseendpointRequest<Void> {

    private static final String REST_PATH = "expense/{id}";

    /**
     * Create a request for the method "removeExpense".
     *
     * This request holds the parameters needed by the the expenseendpoint server.  After setting any
     * optional parameters, call the {@link RemoveExpense#execute()} method to invoke the remote
     * operation. <p> {@link RemoveExpense#initialize(com.google.api.client.googleapis.services.Abstra
     * ctGoogleClientRequest)} must be called to initialize this instance immediately after invoking
     * the constructor. </p>
     *
     * @param id
     * @since 1.13
     */
    protected RemoveExpense(java.lang.Long id) {
      super(Expenseendpoint.this, "DELETE", REST_PATH, null, Void.class);
      this.id = com.google.api.client.util.Preconditions.checkNotNull(id, "Required parameter id must be specified.");
    }

    @Override
    public RemoveExpense setAlt(java.lang.String alt) {
      return (RemoveExpense) super.setAlt(alt);
    }

    @Override
    public RemoveExpense setFields(java.lang.String fields) {
      return (RemoveExpense) super.setFields(fields);
    }

    @Override
    public RemoveExpense setKey(java.lang.String key) {
      return (RemoveExpense) super.setKey(key);
    }

    @Override
    public RemoveExpense setOauthToken(java.lang.String oauthToken) {
      return (RemoveExpense) super.setOauthToken(oauthToken);
    }

    @Override
    public RemoveExpense setPrettyPrint(java.lang.Boolean prettyPrint) {
      return (RemoveExpense) super.setPrettyPrint(prettyPrint);
    }

    @Override
    public RemoveExpense setQuotaUser(java.lang.String quotaUser) {
      return (RemoveExpense) super.setQuotaUser(quotaUser);
    }

    @Override
    public RemoveExpense setUserIp(java.lang.String userIp) {
      return (RemoveExpense) super.setUserIp(userIp);
    }

    @com.google.api.client.util.Key
    private java.lang.Long id;

    /**

     */
    public java.lang.Long getId() {
      return id;
    }

    public RemoveExpense setId(java.lang.Long id) {
      this.id = id;
      return this;
    }

    @Override
    public RemoveExpense set(String parameterName, Object value) {
      return (RemoveExpense) super.set(parameterName, value);
    }
  }

  /**
   * Create a request for the method "updateExpense".
   *
   * This request holds the parameters needed by the expenseendpoint server.  After setting any
   * optional parameters, call the {@link UpdateExpense#execute()} method to invoke the remote
   * operation.
   *
   * @param content the {@link com.trip.expensemanager.expenseendpoint.model.Expense}
   * @return the request
   */
  public UpdateExpense updateExpense(com.trip.expensemanager.expenseendpoint.model.Expense content) throws java.io.IOException {
    UpdateExpense result = new UpdateExpense(content);
    initialize(result);
    return result;
  }

  public class UpdateExpense extends ExpenseendpointRequest<com.trip.expensemanager.expenseendpoint.model.Expense> {

    private static final String REST_PATH = "expense";

    /**
     * Create a request for the method "updateExpense".
     *
     * This request holds the parameters needed by the the expenseendpoint server.  After setting any
     * optional parameters, call the {@link UpdateExpense#execute()} method to invoke the remote
     * operation. <p> {@link UpdateExpense#initialize(com.google.api.client.googleapis.services.Abstra
     * ctGoogleClientRequest)} must be called to initialize this instance immediately after invoking
     * the constructor. </p>
     *
     * @param content the {@link com.trip.expensemanager.expenseendpoint.model.Expense}
     * @since 1.13
     */
    protected UpdateExpense(com.trip.expensemanager.expenseendpoint.model.Expense content) {
      super(Expenseendpoint.this, "PUT", REST_PATH, content, com.trip.expensemanager.expenseendpoint.model.Expense.class);
    }

    @Override
    public UpdateExpense setAlt(java.lang.String alt) {
      return (UpdateExpense) super.setAlt(alt);
    }

    @Override
    public UpdateExpense setFields(java.lang.String fields) {
      return (UpdateExpense) super.setFields(fields);
    }

    @Override
    public UpdateExpense setKey(java.lang.String key) {
      return (UpdateExpense) super.setKey(key);
    }

    @Override
    public UpdateExpense setOauthToken(java.lang.String oauthToken) {
      return (UpdateExpense) super.setOauthToken(oauthToken);
    }

    @Override
    public UpdateExpense setPrettyPrint(java.lang.Boolean prettyPrint) {
      return (UpdateExpense) super.setPrettyPrint(prettyPrint);
    }

    @Override
    public UpdateExpense setQuotaUser(java.lang.String quotaUser) {
      return (UpdateExpense) super.setQuotaUser(quotaUser);
    }

    @Override
    public UpdateExpense setUserIp(java.lang.String userIp) {
      return (UpdateExpense) super.setUserIp(userIp);
    }

    @Override
    public UpdateExpense set(String parameterName, Object value) {
      return (UpdateExpense) super.set(parameterName, value);
    }
  }

  /**
   * Builder for {@link Expenseendpoint}.
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

    /** Builds a new instance of {@link Expenseendpoint}. */
    @Override
    public Expenseendpoint build() {
      return new Expenseendpoint(this);
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
     * Set the {@link ExpenseendpointRequestInitializer}.
     *
     * @since 1.12
     */
    public Builder setExpenseendpointRequestInitializer(
        ExpenseendpointRequestInitializer expenseendpointRequestInitializer) {
      return (Builder) super.setGoogleClientRequestInitializer(expenseendpointRequestInitializer);
    }

    @Override
    public Builder setGoogleClientRequestInitializer(
        com.google.api.client.googleapis.services.GoogleClientRequestInitializer googleClientRequestInitializer) {
      return (Builder) super.setGoogleClientRequestInitializer(googleClientRequestInitializer);
    }
  }
}
