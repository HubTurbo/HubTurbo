package github.update;

import static org.eclipse.egit.github.core.client.IGitHubConstants.PARAM_PAGE;
import github.GitHubClientExtended;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.eclipse.egit.github.core.client.GitHubRequest;
import org.eclipse.egit.github.core.client.GitHubResponse;
import org.eclipse.egit.github.core.client.NoSuchPageException;
import org.eclipse.egit.github.core.util.UrlUtils;

/**
 * Iterator for getting values of a header field from paged responses
 * @see org.eclipse.egit.github.core.client.PageIterator
 */
public class PageHeaderIterator implements Iterable<String>, Iterator<String> {
    private final GitHubRequest request;
    private final GitHubClientExtended client;
    private final String headerField;

    private int nextPage; // Current page number
    private String next; // Next uri to be fetched

    public PageHeaderIterator(GitHubRequest request, GitHubClientExtended client,
                              String headerField) {
        this.request = request;
        this.client = client;
        this.headerField = headerField;

        next = request.getUri();
        nextPage = parsePageNumber(next);
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

        GitHubResponse response;
        try {
            response = client.head(request);
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