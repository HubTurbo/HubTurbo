package util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class Browse {
	private static final Logger logger = LogManager.getLogger(Browse.class.getName());
	public static void browse(String htmlUrl) {
		
		if (htmlUrl == null || htmlUrl.isEmpty()) return;

		if (PlatformSpecific.isOnMac() || PlatformSpecific.isOnWindows()) {
			browseWithDesktop(htmlUrl);
		} else {
			// Assume *nix
			browseUnix(htmlUrl);
		}
	}
	
	private static void browseWithDesktop(String htmlUrl) {
		try {
			if (Desktop.isDesktopSupported()) {
		        Desktop desktop = Desktop.getDesktop();
		        if (desktop.isSupported(Desktop.Action.BROWSE)) {

		            URI uri = new URI(htmlUrl);
		            desktop.browse(uri);
		        }
	        }
	    } catch (IOException ex) {
	        logger.error(ex.getLocalizedMessage(), ex);
	    } catch (URISyntaxException ex) {
	        logger.info("Invalid URI syntax: " + ex.getInput());
	    }
	}

	private static void browseUnix(String url) {

		final String[] UNIX_BROWSE_CMDS = new String[] {"google-chrome", "firefox", "www-browser", "opera", "konqueror", "epiphany", "mozilla", "netscape", "w3m", "lynx" };
		for (final String cmd : UNIX_BROWSE_CMDS) {
			
			if (unixCommandExists(cmd)) {
				try {
					Runtime.getRuntime().exec(new String[] {cmd, url.toString()});
				} catch (IOException e) {
					logger.error(e.getLocalizedMessage(), e);
				}
				return;
			}
		}
	}

	private static boolean unixCommandExists(final String cmd) {
		Process whichProcess;
		try {
			whichProcess = Runtime.getRuntime().exec(new String[] { "which", cmd });
			boolean finished = false;
			do {
				try {
					whichProcess.waitFor();
					finished = true;
				} catch (InterruptedException e) {
					return false;
				}
			} while (!finished);

			return whichProcess.exitValue() == 0;
		} catch (IOException e1) {
			return false;
		}
	}
}
