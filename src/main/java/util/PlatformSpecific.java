package util;

public class PlatformSpecific {
	
//	private static final PlatformSpecific instance = new PlatformSpecific();
	private PlatformSpecific() {}
	private static final String osName = System.getProperty("os.name");
	
	public static boolean isOnWindows() {
		return osName.startsWith("Windows");
	}
	
	public static boolean isOnMac() {
		return osName.startsWith("Mac OS");
	}

	public static boolean isOnLinux() {
		return !isOnWindows() && !isOnMac();
	}

//	public static PlatformSpecific ifWindows(Runnable code) {
//		if (isOnWindows()) {
//			code.run();
//		}
//		return instance;
//	}

//	public static PlatformSpecific ifMac(Runnable code) {
//		if (isOnMac()) {
//			code.run();
//		}
//		return instance;
//	}

//	public static PlatformSpecific ifLinux(Runnable code) {
//		if (isOnLinux()) {
//			code.run();
//		}
//		return instance;
//	}
}
