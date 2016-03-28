package github.update;

import static org.eclipse.egit.github.core.client.IGitHubConstants.PARAM_PAGE;

import github.GitHubClientEx;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.logging.log4j.Logger;
import org.eclipse.egit.github.core.client.GitHubRequest;
import org.eclipse.egit.github.core.client.GitHubResponse;
import org.eclipse.egit.github.core.client.NoSuchPageException;
import org.eclipse.egit.github.core.util.UrlUtils;

import util.HTLog;

/**
 * Iterator for getting values of a header field from paged responses
 *
 * @see org.eclipse.egit.github.core.client.PageIterator
 */
public class PageHeaderIterator implements Iterable<String>, Iterator<String> {
    private static final Logger logger = HTLog.get(PageHeaderIterator.class);

    private final GitHubRequest request;
    private final GitHubClientEx client;
    private final String headerField;

    private int nextPage; // Current page number
    private String next; // Next uri to be fetched
    private HttpURLConnection lastConnection;

    public PageHeaderIterator(GitHubRequest request, GitHubClientEx client,
                              String headerField) {
        this.request = request;
        this.client = client;
        this.headerField = headerField;

        next = request.getUri();
        nextPage = parsePageNumber(next);
    }

    public GitHubRequest getRequest() {
        return request;
    }

    public HttpURLConnection getLastConnection() {
        return lastConnection;
    }

    /**
     * Parses page number from uri
     *
     * @param uri
     * @return page number
     */
    private int parsePageNumber(String uri) {
        if (uri == null || uri.length() == 0) {
            return -1;
        }

        final URI parsed;
        try {
            parsed = new URI(uri);
        } catch (URISyntaxException e) {
            return -1;
        }

        final String param = UrlUtils.getParam(parsed, PARAM_PAGE);
        if (param == null || param.length() == 0) {
            return -1;
        }

        try {
            return Integer.parseInt(param);
        } catch (NumberFormatException nfe) {
            return -1;
        }
    }

    @Override
    public boolean hasNext() {
        return nextPage == 0 || next != null;
    }

    /**
     * Returns the value of headerField of the next response page
     *
     * @return the value of headerField of the next response page
     */
    @Override
    public String next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }

        if (next != null) {
            if (nextPage < 1) {
                request.setUri(next);
            } else {
                try {
                    request.setUri(new URL(next).getFile());
                } catch (MalformedURLException e) {
                    request.setUri(next);
                }
            }
        }

        logger.info(String.format("Getting header page %d for %s", nextPage, request.getUri()));

        ImmutablePair<HttpURLConnection, GitHubResponse> requestResult;
        GitHubResponse response;
        try {
            requestResult = client.head(request);
            lastConnection = requestResult.getLeft();
            response = requestResult.getRight();
        } catch (IOException e) {
            throw new NoSuchPageException(e);
        }

        nextPage++;
        next = response.getNext();
        nextPage = parsePageNumber(next);

        return response.getHeader(headerField);
    }

    @Override
    public Iterator<String> iterator() {
        return this;
    }
}
