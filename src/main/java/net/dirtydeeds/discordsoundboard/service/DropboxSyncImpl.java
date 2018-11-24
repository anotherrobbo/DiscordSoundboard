package net.dirtydeeds.discordsoundboard.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.DbxUserFilesRequests;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.users.FullAccount;

import net.dirtydeeds.discordsoundboard.util.SystemPropertyUtil;

public class DropboxSyncImpl {
    
    private final Logger LOG = LoggerFactory.getLogger(this.getClass());
    private DbxClientV2 client;
    private String soundFileDir;
    private String dropboxDir;
    
    public static void main(String args[]) throws DbxException, IOException {
        new DropboxSyncImpl().performDropboxSync();
    }
    
    public DropboxSyncImpl() throws DbxException {
        Properties appProperties = SystemPropertyUtil.loadProperties();
        
        String dropboxToken = appProperties.getProperty("dropbox_token");
        // Create Dropbox client
        DbxRequestConfig config = DbxRequestConfig.newBuilder("soundbot-prime").build();
        this.client = new DbxClientV2(config, dropboxToken);
        // Get current account info
        FullAccount account = client.users().getCurrentAccount();
        LOG.info("Logged in as: " + account.getName().getDisplayName());
        
        this.soundFileDir = appProperties.getProperty("sounds_directory");
        if (soundFileDir == null || soundFileDir.isEmpty())  {
            soundFileDir = System.getProperty("user.dir") + "/sounds";
        }
        
        this.dropboxDir = appProperties.getProperty("dropbox_sounds_directory");
    }
    
    public void performDropboxSync() throws DbxException, IOException {
        DbxUserFilesRequests fileRequest = client.files();
        ListFolderResult listFolderResult = fileRequest.listFolder(dropboxDir);
        downloadFiles(fileRequest, listFolderResult);
        LOG.info("DOWNLOAD COMPLETE");
    }
    
    private void downloadFiles(DbxUserFilesRequests fileRequest, ListFolderResult listFolderResult) throws DbxException, IOException {
        for (Metadata md : listFolderResult.getEntries()) {
            LOG.info("DOWNLOADING: " + md.getPathDisplay());
            File outputDir = new File(soundFileDir);
            if (!outputDir.exists()) {
                boolean mkdirs = outputDir.mkdirs();
                LOG.info("Created output folder " + soundFileDir + ": " + mkdirs);
            }
            File file = new File(soundFileDir + "/" + md.getName());
            FileOutputStream os = new FileOutputStream(file);
            fileRequest.download(md.getPathLower()).download(os);
        }
        if (listFolderResult.getHasMore()) {
            downloadFiles(fileRequest, fileRequest.listFolderContinue(listFolderResult.getCursor()));
        }
    }

}
