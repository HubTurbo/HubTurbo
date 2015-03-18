package service.updateservice;

import static org.eclipse.egit.github.core.client.IGitHubConstants.CONTENT_TYPE_JSON;
import static org.eclipse.egit.github.core.client.IGitHubConstants.SEGMENT_REPOS;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.client.GitHubRequest;
import org.eclipse.egit.github.core.client.NoSuchPageException;
import org.eclipse.egit.github.core.client.PageIterator;
import org.eclipse.egit.github.core.client.PagedRequest;
import org.eclipse.egit.github.core.service.GitHubService;

import service.GitHubClientExtended;
import util.Utility;

/**
 * Given a type of item and the current ETag, fetches a list of updated items.
 * Returns auxillary results in the form of an updated ETag and the time of response.
 * Only provides the basic framework for fetching updates: details of how updates are
 * gotten are left to subclasses.
 * */
public class UpdateService<T> extends GitHubService{
	private static final Logger logger = LogManager.getLogger(UpdateService.class.getName());

	private final GitHubClientExtended client;
	private final String apiSuffix;
	private final String lastETag;

	// Auxillary results of calling getUpdatedItems
	private String updatedETag = null;
	private Date updatedCheckTime = new Date();

	// Caches results of calling getUpdatedItems
	private ArrayList<T> updatedItems = null;

	/**
	 * @param client a non-null GitHubClient
	 * @param apiSuffix the API URI for the type of item; should be defined by subclasses
	 * @param lastETag the current ETag; may be null
	 */
	public UpdateService(GitHubClientExtended client, String apiSuffix, String lastETag){
		this.client = client;
		this.apiSuffix = apiSuffix;
		this.lastETag = lastETag;
	}

	/**
	 * To be overridden by subclasses to specify additional information required by
	 * the EGit API, such as the types of the results expected (for deserialisation
	 * purposes). Should be called by overriding implementations.
	 * Will be called by getUpdatedItems.
	 * @param repoId the repository to make the request for
	 * @return the request to make
	 */
	protected PagedRequest<T> createUpdatedRequest(IRepositoryIdProvider repoId){
		PagedRequest<T> request = new PagedRequest<>();
		String path = SEGMENT_REPOS + "/" + repoId.generateId() + apiSuffix;
		request.setUri(path);
		request.setResponseContentType(CONTENT_TYPE_JSON);
		return request;
	}

	/**
	 * Retrieves the requested items from GitHub. Sets the auxillary fields.
	 * @param repoId the repository to get the items from
	 * @return a list of requested items
	 */
	public ArrayList<T> getUpdatedItems(IRepositoryIdProvider repoId){

		// Return cached results if available
		if (updatedItems != null)  {
			return updatedItems;
		}

		ArrayList<T> result = new ArrayList<>();

		logger.info(String.format("Starting update of %s with ETag %s", apiSuffix, lastETag));
		try {

			PagedRequest<T> request = createUpdatedRequest(repoId);
			HttpURLConnection connection = createUpdatedConnection(request);
			int responseCode = connection.getResponseCode();

			if (client.isError(responseCode)) {
				logger.warn(String.format("%s: error getting updated items (%d)",
					getClass().getSimpleName(), responseCode));

				// The internal state of this object is unchanged, so it can be reused
				return new ArrayList<>();
			}

			logger.info("UpdateService response: " + responseCode);
			if(responseCode == GitHubClientExtended.NO_UPDATE_RESPONSE_CODE){
				logger.info("Nothing to update");
			} else {
				result = new ArrayList<>(getPagedItems(new PageIterator<>(request, client)));
				updatedETag = Utility.stripQuotes(connection.getHeaderField("ETag"));
				logger.info(String.format("New ETag for resource %s: %s", apiSuffix, updatedETag));
			}

			updateCheckTime(connection);

		} catch (IOException e) {
			if(!(e instanceof UnknownHostException || e instanceof SocketTimeoutException)){
				logger.error(e.getLocalizedMessage(), e);
			}
		}

		updatedItems = result;

		return result;
	}

	/**
	 * A specialised version of GitHubService::getPage that does logging.
	 * @param iterator the paged request to iterate through
	 * @return a list of items
	 * @throws IOException
	 */
	private List<T> getPagedItems(PageIterator<T> iterator) throws IOException {
		logger.info("Getting paged items from " + apiSuffix + ": " + (iterator.getLastPage()+1) + " pages");
		List<T> elements = new ArrayList<>();
		int length = 0;
		int page = 0;
		try {
			while (iterator.hasNext()) {
				elements.addAll(iterator.next());
				int diff = elements.size() - length;
				length = elements.size();
				logger.info("Page " + page++ + ": " + diff + " items");
			}
		} catch (NoSuchPageException pageException) {
			throw pageException.getCause();
		}
		return elements;
	}

	/**
	 * Tests to see if getUpdatedItems succeeded. If so, the auxillary fields are
	 * guaranteed to be set.
	 * @return true if getUpdatedItems succeeded
	 */
	public boolean succeeded() {
		return updatedItems != null;
	}

	/**
	 * Returns the ETag for the updated items.
	 * Will be null in the event of failure, guaranteed not to be on success.
	 * @return ETag for updated items
	 */
	public String getUpdatedETag() {
		if (succeeded()) {
			assert updatedETag != null;
		}
		return updatedETag;
	}

	/**
	 * Returns the time at which the updated items were sent from the server.
	 * Has a reasonable default in the event of failure
	 * @return time at which updated items were sent from server
	 */
	public Date getUpdatedCheckTime() {
		return updatedCheckTime;
	}

	private void updateCheckTime(HttpURLConnection connection) {
		String date = connection.getHeaderField("Date");
		updatedCheckTime = Utility.parseHTTPLastModifiedDate(date);
	}

	private HttpURLConnection createUpdatedConnection(GitHubRequest request) throws IOException{
		HttpURLConnection connection = client.createConnection(request);
		if (lastETag != null && !lastETag.isEmpty()) {
			assert !lastETag.startsWith("\"");
			assert !lastETag.endsWith("\"");
			connection.setRequestProperty("If-None-Match", "\"" + lastETag + "\"");
		}
		return connection;
	}
}
