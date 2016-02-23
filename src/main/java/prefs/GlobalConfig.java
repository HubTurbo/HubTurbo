package prefs;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.Optional;

/**
 * Abstractions for the contents of the global config file.
 */
@SuppressWarnings("unused")
public class GlobalConfig {

    private static final Logger logger = LogManager.getLogger(GlobalConfig.class.getName());

    private List<PanelInfo> lastSessionPanels = new ArrayList<>();
    private String lastViewedRepository = "";
    private String lastLoginUsername = "";
    private byte[] lastLoginPassword = new byte[0];
    private Optional<String> lastOpenBoard = Optional.empty();
    private final Map<String, List<PanelInfo>> savedBoards = new LinkedHashMap<>();
    private final Map<String, Map<Integer, LocalDateTime>> markedReadTimes = new HashMap<>();
    private Map<String, String> keyboardShortcuts = new HashMap<>();

    public Map<String, String> getKeyboardShortcuts() {
        return new HashMap<>(keyboardShortcuts);
    }

    public void setKeyboardShortcuts(Map<String, String> keyboardShortcuts) {
        this.keyboardShortcuts = new HashMap<>(keyboardShortcuts);
    }

    public void setMarkedReadAt(String repoId, int issue, LocalDateTime time) {
        if (!markedReadTimes.containsKey(repoId)) {
            markedReadTimes.put(repoId, new HashMap<>());
        }
        markedReadTimes.get(repoId).put(issue, time);
    }

    public Optional<LocalDateTime> getMarkedReadAt(String repoId, int issue) {
        if (!markedReadTimes.containsKey(repoId)) {
            return Optional.empty();
        }
        return Optional.ofNullable(markedReadTimes.get(repoId).get(issue));
    }

    public void clearMarkedReadAt(String repoId, int issue) {
        if (!markedReadTimes.containsKey(repoId)) {
            // No need to do anything
            return;
        }
        if (markedReadTimes.get(repoId).containsKey(issue)) {
            markedReadTimes.get(repoId).remove(issue);
        }
    }

    public void addBoard(String name, List<PanelInfo> panels) {
        savedBoards.put(name, panels);
    }

    public List<PanelInfo> getBoardPanels(String name) {
        return savedBoards.get(name);
    }

    public Map<String, List<PanelInfo>> getAllBoards() {
        return new HashMap<>(savedBoards);
    }

    public void removeBoard(String name) {
        savedBoards.remove(name);
    }
    
    public void setLastOpenBoard(String board) {
        lastOpenBoard = Optional.of(board);
    }
    
    public Optional<String> getLastOpenBoard() {
        return lastOpenBoard;
    }
    
    public void clearLastOpenBoard() {
        lastOpenBoard = Optional.empty();
    }

    public void clearAllBoards() {
        clearLastOpenBoard();
        savedBoards.clear();
    }

    public List<String> getLastOpenFilters() {
        return lastSessionPanels.stream().map(PanelInfo::getPanelFilter).collect(Collectors.toList());
    }
    
    public List<String> getPanelNames() {
        return lastSessionPanels.stream().map(PanelInfo::getPanelName).collect(Collectors.toList());
    }
    
    public void setPanelInfo(List<PanelInfo> panelInfo) {
        this.lastSessionPanels = new ArrayList<>(panelInfo);
    }
    
    public List<PanelInfo> getPanelInfo() {
        return new ArrayList<>(lastSessionPanels);
    }

    public void setLastViewedRepository(String repository) {
        lastViewedRepository = repository;
    }

    public String getLastViewedRepository() {
        return lastViewedRepository;
    }

    public String getLastLoginUsername() {
        return lastLoginUsername;
    }

    public String getLastLoginPassword() {
        return decrypt(lastLoginPassword);
    }

    public void setLastLoginCredentials(String username, String password) {
        this.lastLoginUsername = username;
        this.lastLoginPassword = encrypt(password);
    }

    private static byte[] encrypt(String lastPassword) {
        byte[] result = new byte[0];
        try {
            String key = "HubTurboHubTurbo";
            Key aesKey = new SecretKeySpec(key.getBytes("UTF-8"), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, aesKey);
            result = cipher.doFinal(lastPassword.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException
            | IllegalBlockSizeException | BadPaddingException | UnsupportedEncodingException e) {

            logger.error("Cannot encrypt data " + e.getMessage(), e);
        }
        return result;
    }

    private static String decrypt(byte[] lastLoginEncrypted) {
        String result = "";
        try {
            String key = "HubTurboHubTurbo";
            Key aesKey = new SecretKeySpec(key.getBytes("UTF-8"), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, aesKey);
            result = new String(cipher.doFinal(lastLoginEncrypted), "UTF-8");
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException
            | IllegalBlockSizeException | BadPaddingException | UnsupportedEncodingException e) {

            logger.error("Cannot encrypt data " + e.getMessage(), e);
        }
        return result;
    }
}
