package github;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.io.input.NullInputStream;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.logging.log4j.Logger;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.client.GitHubRequest;
import org.eclipse.egit.github.core.client.GitHubResponse;
import util.HTLog;
import util.IOUtilities;
import util.Utility;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.util.Map;

public class GitHubClientEx extends GitHubClient {
    private static final Logger logger = HTLog.get(GitHubClientEx.class);

    protected static final int CONNECTION_TIMEOUT = 30000;

    // Request method for HEAD API call
    protected static final String METHOD_HEAD = "HEAD";

    public GitHubClientEx() {
        super();
    }

    public GitHubClientEx(String hostname, int port, String scheme) {
        super(hostname, port, scheme);
    }

    /**
     * Extends superclass method with connection timeout parameters.
     */
    @Override
    protected HttpURLConnection createConnection(String uri) throws IOException {
        HttpURLConnection connection = super.createConnection(uri);
        connection.setConnectTimeout(CONNECTION_TIMEOUT);
        connection.setReadTimeout(CONNECTION_TIMEOUT);
        return connection;
    }

    /**
     * Creates a HEAD request connection to the URI
     *
     * @param uri
     * @return connection
     * @throws IOException
     */
    protected HttpURLConnection createHead(String uri) throws IOException {
        return createConnection(uri, METHOD_HEAD);
    }

    /**
     * Exposes the sendJson method (which is private in the superclass).
     *
     * @param <V>
     * @param request
     * @param params
     * @param type
     * @return
     * @throws IOException
     */
    public <V> V sendJson(final HttpURLConnection request, final Object params, final Type type)
            throws IOException {

        sendParams(request, params);
        final int code = request.getResponseCode();
        updateRateLimits(request);
        if (isOk(code)) {
            if (type != null) {
                return parseJson(getStream(request), type);
            } else {
                return null;
            }
        }
        if (isEmpty(code)) return null;
        throw createException(getStream(request), code, request.getResponseMessage());
    }

    /**
     * Serves the same purpose as GitHubClient::get, with the added
     * functionality of returning a GitHubEventResponse (containing
     * event-specific information) instead of a GitHubResponse.
     *
     * @param request
     * @return response
     * @throws IOException
     */
    public GitHubEventsResponse getEvent(GitHubRequest request, String currentETag) throws IOException {
        HttpURLConnection httpRequest = createGet(request.generateUri());

        // Headers for the request
        httpRequest.setRequestProperty("If-None-Match", "\"" + currentETag + "\"");
        String accept = request.getResponseContentType();
        if (accept != null) {
            httpRequest.setRequestProperty(HEADER_ACCEPT, accept);
        }
        // We send the request here.
        final int code = httpRequest.getResponseCode();

        // Then we process the response.
        updateRateLimits(httpRequest);
        if (isOk(code)) { // 200 OK
            String updatedEtag = Utility.stripQuotes(httpRequest.getHeaderField("ETag"));

            // Copy the httpRequest input stream into a byte array
            InputStream reqIS = getStream(httpRequest);
            ByteArrayOutputStream buffer = IOUtilities.inputStreamToByteArrayOutputStream(reqIS);
            InputStream reqIS2 = new ByteArrayInputStream(buffer.toByteArray());
            InputStream reqIS3 = new ByteArrayInputStream(buffer.toByteArray());

            // The first copy is used to produce the GitHubResponse
            GitHubResponse ghResponse = new GitHubResponse(httpRequest, getBody(request, reqIS2));

            // The second is parsed again for event-specific information
            return new GitHubEventsResponse(ghResponse, reqIS3, updatedEtag);
        } else if (isNotModified(code)) { // 304 Not Modified
            GitHubResponse ghResponse = new GitHubResponse(httpRequest, null);
            return new GitHubEventsResponse(ghResponse, new NullInputStream(0), currentETag);
        } else if (isEmpty(code)) {
            GitHubResponse ghResponse = new GitHubResponse(httpRequest, null);
            return new GitHubEventsResponse(ghResponse, new NullInputStream(0), "");
        } else {
            throw createException(getStream(httpRequest), code, httpRequest.getResponseMessage());
        }
    }

    /**
     * Accesses the Rate Limit API endpoint to retrieve the number of remaining requests for the hour,
     * as well as the next reset time. Calling this function itself does not count towards the API limit.
     *
     * @return A pair consisting of the number of requests remaining for the hour and the next reset time.
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    public ImmutablePair<Integer, Long> getRateLimitResetTime() throws IOException {
        HttpURLConnection httpRequest = createGet("/rate_limit");
        if (isOk(httpRequest.getResponseCode())) {
            // We extract from rate, which is similar to resources.core
            String json = String.valueOf(
                    IOUtilities.inputStreamToByteArrayOutputStream(getStream(httpRequest)));
            Map<String, Object> map =
                    new Gson().fromJson(json, new TypeToken<Map<String, Object>>() {
                    }.getType());
            Map<String, Double> mapRate = (Map<String, Double>) map.get("rate");

            long reset = mapRate.get("reset").longValue() * 1000; // seconds to milliseconds
            int remaining = mapRate.get("remaining").intValue();

            return new ImmutablePair<>(remaining, reset);
        } else {
            throw new IOException(httpRequest.getResponseCode() + " " + httpRequest.getResponseMessage());
        }
    }

    /**
     * Gets a pair of HTTP connection and corresponding response from a header-only API call
     *
     * @param request for the API call
     * @return a pair of HTTP connection and response for the API call
     * @throws IOException
     */
    public ImmutablePair<HttpURLConnection, GitHubResponse> head(GitHubRequest request) throws IOException {
        HttpURLConnection httpRequest = createHead(request.generateUri());
        String accept = request.getResponseContentType();
        if (accept != null) {
            httpRequest.setRequestProperty(HEADER_ACCEPT, accept);
        }
        logger.info(String.format("Requesting: %s %s",
                                  httpRequest.getRequestMethod(), httpRequest.getURL().getFile()));

        final int code = httpRequest.getResponseCode();
        updateRateLimits(httpRequest);

        logger.info(String.format("%s responded with %d %s",
                                  httpRequest.getURL().getPath(), code, httpRequest.getResponseMessage()));
        if (isOk(code) || code == HttpURLConnection.HTTP_NOT_MODIFIED || isEmpty(code)) {
            return new ImmutablePair<>(httpRequest, new GitHubResponse(httpRequest, null));
        }

        throw createException(getStream(httpRequest), code,
                              httpRequest.getResponseMessage());
    }

    /**
     * Overridden to make public.
     */
    @Override
    @SuppressWarnings("PMD")
    public IOException createException(InputStream response, int code, String status) {
        return super.createException(response, code, status);
    }

    /**
     * Overridden to make public.
     */
    @Override
    @SuppressWarnings("PMD")
    public HttpURLConnection createPost(String uri) throws IOException {
        return super.createPost(uri);
    }

    /**
     * Overridden to make public.
     */
    @Override
    @SuppressWarnings("PMD")
    public InputStream getStream(HttpURLConnection request) throws IOException {
        return super.getStream(request);
    }

    /**
     * Overridden to make public.
     */
    @Override
    @SuppressWarnings("PMD")
    public Object getBody(GitHubRequest request, InputStream stream) throws IOException {
        return super.getBody(request, stream);
    }

    /**
     * Overridden to make public.
     */
    @Override
    @SuppressWarnings("PMD")
    public boolean isError(final int code) {
        return super.isError(code);
    }

    /**
     * Overridden to make public.
     *
     * @param code
     * @return
     */
    @Override
    @SuppressWarnings("PMD")
    public boolean isOk(final int code) {
        return super.isOk(code);
    }

    /**
     * Overridden to make public.
     *
     * @param request
     * @param params
     * @throws IOException
     */
    @Override
    public void sendParams(HttpURLConnection request, Object params) throws IOException {
        super.sendParams(request, params);
    }

    /**
     * Checks whether there is new updates.
     *
     * @param code
     * @return
     */
    public boolean isNotModified(final int code) {
        return code == 304;
    }
}
