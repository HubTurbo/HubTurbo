package github;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.client.GitHubRequest;
import org.eclipse.egit.github.core.client.GitHubResponse;

import util.IOUtilities;
import util.Utility;

public class GitHubClientExtended extends GitHubClient {
    public static final int NO_UPDATE_RESPONSE_CODE = 304;
    protected static final int CONNECTION_TIMEOUT = 30000;

    public GitHubClientExtended() {
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
     * Utility method for creating a connection from a GitHubRequest.
     *
     * @param request
     * @return
     * @throws IOException
     */
    public HttpURLConnection createConnection(GitHubRequest request) throws IOException {
        return createGet(request.generateUri());
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
        if (isOk(code))
            if (type != null)
                return parseJson(getStream(request), type);
            else
                return null;
        if (isEmpty(code))
            return null;
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
    public GitHubEventsResponse getEvent(GitHubRequest request) throws IOException {
        HttpURLConnection httpRequest = createGet(request.generateUri());
        String accept = request.getResponseContentType();
        if (accept != null)
            httpRequest.setRequestProperty(HEADER_ACCEPT, accept);
        final int code = httpRequest.getResponseCode();
        updateRateLimits(httpRequest);
        if (isOk(code)) {

            // Copy the httpRequest input stream into a byte array
            InputStream reqIS = getStream(httpRequest);
            ByteArrayOutputStream buffer = IOUtilities.inputStreamToByteArrayOutputStream(reqIS);
            InputStream reqIS2 = new ByteArrayInputStream(buffer.toByteArray());
            InputStream reqIS3 = new ByteArrayInputStream(buffer.toByteArray());

            // The first copy is used to produce the GitHubResponse
            GitHubResponse ghResponse = new GitHubResponse(httpRequest, getBody(request, reqIS2));

            // The second is parsed again for event-specific information
            return new GitHubEventsResponse(ghResponse, reqIS3);
        } else if (isEmpty(code)) {
            GitHubResponse ghResponse = new GitHubResponse(httpRequest, null);
            return new GitHubEventsResponse(ghResponse, null);
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
                    new Gson().fromJson(json, new TypeToken<Map<String, Object>>() {}.getType());
            Map<String, Double> mapRate = (Map<String, Double>) map.get("rate");

            long reset = mapRate.get("reset").longValue() * 1000; // seconds to milliseconds
            int remaining = mapRate.get("remaining").intValue();

            return new ImmutablePair<>(remaining, reset);
        } else {
            throw new IOException(httpRequest.getResponseCode() + " " + httpRequest.getResponseMessage());
        }
    }

    /**
     * Overridden to make public.
     */
    @Override
    public IOException createException(InputStream response, int code, String status) {
        return super.createException(response, code, status);
    }

    /**
     * Overridden to make public.
     */
    @Override
    public HttpURLConnection createPost(String uri) throws IOException {
        return super.createPost(uri);
    }

    /**
     * Overridden to make public.
     */
    @Override
    public InputStream getStream(HttpURLConnection request) throws IOException {
        return super.getStream(request);
    }

    /**
     * Overridden to make public.
     */
    @Override
    public Object getBody(GitHubRequest request, InputStream stream) throws IOException {
        return super.getBody(request, stream);
    }

    /**
     * Overridden to make public.
     */
    @Override
    public boolean isError(final int code) {
        return super.isError(code);
    }

    /**
     * Overridden to make public.
     *
     * @param code
     * @return
     */
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
    public void sendParams(HttpURLConnection request, Object params) throws IOException {
        super.sendParams(request, params);
    }
}
