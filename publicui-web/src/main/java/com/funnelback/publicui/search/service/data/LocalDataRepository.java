package com.funnelback.publicui.search.service.data;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.FileSystemOptions;
import org.apache.commons.vfs.UserAuthenticator;
import org.apache.commons.vfs.VFS;
import org.apache.commons.vfs.auth.StaticUserAuthenticator;
import org.apache.commons.vfs.impl.DefaultFileSystemConfigBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Repository;

import com.funnelback.common.Xml;
import com.funnelback.common.config.DefaultValues;
import com.funnelback.common.config.Keys;
import com.funnelback.common.io.store.RawBytesRecord;
import com.funnelback.common.io.store.Record;
import com.funnelback.common.io.store.Store;
import com.funnelback.common.io.store.Store.RecordAndMetadata;
import com.funnelback.common.io.store.Store.View;
import com.funnelback.common.io.store.StoreType;
import com.funnelback.common.io.store.XmlRecord;
import com.funnelback.common.io.warc.WarcConstants;
import com.funnelback.common.utils.VFSURLUtils;
import com.funnelback.publicui.i18n.I18n;
import com.funnelback.publicui.search.lifecycle.data.fetchers.padre.AbstractPadreForking.EnvironmentKeys;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.service.DataRepository;
import com.funnelback.publicui.search.service.data.exception.TRIMException;
import com.funnelback.publicui.utils.ExecutionReturn;
import com.funnelback.publicui.utils.jna.WindowsFileInputStream;
import com.funnelback.publicui.utils.jna.WindowsNativeExecutor;
import com.funnelback.publicui.utils.jna.WindowsNativeExecutor.ExecutionException;

/**
 * {@link DataRepository} implementation against the 
 * local filesystem
 */
@Repository
@Log4j
public class LocalDataRepository implements DataRepository {
    
    /** Name of the parameter containing the record id for database collections */
    private final static String RECORD_ID = "record_id";

    /** How long to wait (in ms.) to get a document out of TRIM */
    private static final int GET_DOCUMENT_WAIT_TIMEOUT = 1000*60;

    /**
     * Folder containing the binary to get a TRIM document,
     * relative to SEARCH_HOME
     */
    private final static String GET_DOCUMENT_BINARY_PATH =
        DefaultValues.FOLDER_WINDOWS_BIN + File.separator + DefaultValues.FOLDER_TRIM;
    
    /** File name of the program to get a TRIM document*/
    private final static String GET_DOCUMENT_BINARY = "Funnelback.TRIM.GetDocument.exe";

    /** Exit code when retrieving the document was successful */
    private static final int GET_DOCUMENT_SUCCESS = 0;
    
    /** Separator between the lines returned by the document fetcher binary */
    private static final String GET_DOCUMENT_OUTPUT_LINE_SEP = "\r?\n";
    
    /** Separator between the keys and values of fields returned by the document fetcher */
    private static final String GET_DOCUMENT_OUTPUT_KV_SEP = ": ";
    
    /** Document fetcher output field containing the error message */
    private static final String ERROR_KEY = "ERROR";
    
    /** Document fetcher output field containing the file path */
    private static final String FILE_KEY = "File";
    
    /** Document fetcher output field indicating a Trim HTML email */
    private static final String IS_EMAIL_KEY = "IsTRIMMail";
    
    /** Pattern to detect HTML links in TRIM emails */
    private static final Pattern HTML_LINK_PATTERN = Pattern.compile("<a[^>]*>(.*?)</a>",
        Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    /** Full path to the binary to get a document from TRIM */
    private final File getDocumentBinary;
    
    /** Environment used when calling the binary to get a document */
    private final Map<String, String> getDocumentEnvironment;
    
    @Autowired
    private I18n i18n;

    /**
     * @param searchHome Funnelback installation folder, to be able to locate
     * the TRIM binaries from <code>wbin/trim/</code>.
     */
    @Autowired
    public LocalDataRepository(File searchHome) {
        getDocumentBinary = new File(searchHome
            + File.separator + GET_DOCUMENT_BINARY_PATH,
            GET_DOCUMENT_BINARY);

        getDocumentEnvironment = new HashMap<String, String>();
        getDocumentEnvironment.put(EnvironmentKeys.SEARCH_HOME.toString(), searchHome.getAbsolutePath());
        // SystemRoot environment variable is MANDATORY.
        // The TRIM SDK uses WinSock to connect to the remote server, and 
        // WinSock needs SystemRoot to initialise itself.
        if (System.getenv(EnvironmentKeys.SystemRoot.toString()) != null) {
            getDocumentEnvironment.put(EnvironmentKeys.SystemRoot.toString(),
                System.getenv(EnvironmentKeys.SystemRoot.toString()));
        }
    }
    
    @Override
    public RecordAndMetadata<? extends Record<?>> getDocument(Collection collection, View view,
        String url, File relativePath, int offset, int length) {
        
        if (WarcConstants.WARC.equals(FilenameUtils.getExtension(relativePath.getName()))) {
            // FUN-5956 WARC files not supported yet
            return null;
        }
        
        File root =  new File(collection.getConfiguration().getCollectionRoot(),
            view.toString() + File.separator + DefaultValues.FOLDER_DATA);
        File path = new File(root, relativePath.getPath());

        byte[] content = null;
        try {
            if (relativePath.isAbsolute()
                || ! com.funnelback.common.utils.FileUtils.isChildOf(root, path)) {
                throw new IllegalArgumentException("Invalid path '" + relativePath + "'");
            } else if (! path.exists()) {
                return null;
            }

            if (length > 0) {
                try (RandomAccessFile f = new RandomAccessFile(path, "r")) {
                    content = new byte[length];
                    f.seek(offset);
                    f.readFully(content);
                }
            } else {
                // Read complete file
                content = FileUtils.readFileToByteArray(path);
            }
        } catch (IOException ioe) {
            log.error("Error while accessing cached document '"+path.getAbsolutePath()+"'", ioe);
            content = null;
        }
        
        if (content != null) {
            if (Xml.XML.equals(FilenameUtils.getExtension(relativePath.getName()))) {
                return new RecordAndMetadata<XmlRecord>(new XmlRecord(
                    Xml.fromString(new String(content)), url),
                    new HashMap<String, String>());
            } else {
                return new RecordAndMetadata<RawBytesRecord>(new RawBytesRecord(content, url),
                    new HashMap<String, String>());
            }
        }
        
        return null;
    }
    
    @Override
    public RecordAndMetadata<? extends Record<?>> getCachedDocument(
            Collection collection, View view, String url) {
        
        try (Store<? extends Record<?>> store = StoreType.getStore(collection.getConfiguration(), view)) {
            store.open();
            return store.getRecordAndMetadata(extractPrimaryKey(collection, url));        
        } catch (ClassNotFoundException cnfe) {
            log.error("Error while getting store for collection '"+collection.getId()+"'", cnfe);
        } catch (IOException ioe) {
            log.error("Couldn't access stored content on collection '"+collection.getId()+"' for URL '"+url+"'", ioe);
        }
    
        return new RecordAndMetadata<Record<?>>(null, null);

    }
    
    @Override
    public InputStream getFilecopyDocument(Collection collection, URI uri,
            boolean withDls) throws IOException {
        if (withDls) {
            // Convert the URI to a Windows path, taking care
            // of preserving plus signs
            String windowsPath = VFSURLUtils.vfsUrlToSystemUrl(
                            URLDecoder.decode(uri.toString().replace("+", "%2B"), "UTF-8"), true);
            return new WindowsFileInputStream(windowsPath);
        } else {
            // Use Filecopy credentials to fetch the content
            FileSystemOptions options = new FileSystemOptions();
            UserAuthenticator ua = new StaticUserAuthenticator(
                            collection.getConfiguration().value(Keys.FileCopy.DOMAIN),
                            collection.getConfiguration().value(Keys.FileCopy.USERNAME),
                            collection.getConfiguration().value(Keys.FileCopy.PASSWORD));
            DefaultFileSystemConfigBuilder.getInstance().setUserAuthenticator(options, ua);
            
            FileSystemManager manager = VFS.getManager();
            FileObject file = manager.resolveFile(uri.toString(), options);
            return file.getContent().getInputStream();
        }
    }
    
    @Override
    public File getTemporaryTrimDocument(Collection collection, int trimUri) throws TRIMException, IOException {
        File tempFolder = new File(collection.getConfiguration().getCollectionRoot()
            + File.separator + DefaultValues.VIEW_LIVE,
            File.separator + DefaultValues.FOLDER_TMP);
        
        List<String> cmdLine = new ArrayList<String>(Arrays.asList(new String[] { getDocumentBinary.getAbsolutePath(),
                        "-i", Integer.toString(trimUri), "-f", tempFolder.getAbsolutePath(), collection.getId() }));

        try {
            ExecutionReturn er = new WindowsNativeExecutor(i18n, GET_DOCUMENT_WAIT_TIMEOUT)
                .execute(cmdLine, getDocumentEnvironment, getDocumentBinary.getParentFile());
            
            Map<String, String> executionOutput = parseExecutionOutput(er.getOutput());
            
            if (er.getReturnCode() != GET_DOCUMENT_SUCCESS) {
                String error = executionOutput.get(ERROR_KEY);
                if (error != null) {
                    throw TRIMException.fromGetDocumentExitCode(er.getReturnCode(), error, trimUri);
                } else {
                    // Unknown error
                    log.error("Document fetcher returned a non-zero status ("
                        + er.getReturnCode()+") with command line '"
                        + cmdLine + "'. Output was '"+er.getOutput() + "'");
                    throw new TRIMException("Error while retrieving document: "
                        + error);
                }
            } else {
                File trimDoc = new File(executionOutput.get(FILE_KEY));
                
                if (trimDoc.canRead() && trimDoc.isFile()) {
                    if (Boolean.parseBoolean(executionOutput.get(IS_EMAIL_KEY))) {
                        // Strip links to attachments
                        
                        FileUtils.writeStringToFile(trimDoc,
                            HTML_LINK_PATTERN.matcher(FileUtils.readFileToString(trimDoc))
                                .replaceAll("$1"));
                    }
                    return trimDoc;
                } else {
                    throw new IllegalStateException("Could not read temporary file '"+trimDoc.getAbsolutePath()+"'");
                }
            }
        } catch (ExecutionException ee) {
            log.error("Error while running document fetcher", ee);
            throw new TRIMException(ee);
        }
    }

    @Override
    @Async
    public void releaseTemporaryTrimDocument(File f) {
        if (f != null) {
            // The temporary file was created in a temporary folder that
            // we need to delete as well
            try {
                FileUtils.deleteDirectory(f.getParentFile());
            } catch (IOException ioe) {
                log.warn("Unable to delete temporary document '"+f.getAbsolutePath()+"'");
            }
        }
    }
    
    /**
     * Resolves the primary key used to store the document from its URL,
     * depending on the collection type. For example database collections use
     * the database ID as primary key (12), but the actual document URL will be
     * something like <code>local://serve-db-document?...&amp;record_id=12</code>
     * 
     * @param url URL of the document
     * @return Corresponding primary key
     */
    @SneakyThrows(UnsupportedEncodingException.class)
    private String extractPrimaryKey(Collection collection, String url) {
        switch (collection.getType()) {
        case database:
        case directory:
            return URLDecoder.decode(url.replaceFirst(".*[&?;]"+RECORD_ID+"=([^&]+).*", "$1"), "UTF-8");
        case custom:
        case trimpush:
            return URLDecoder.decode(url, "UTF-8");
        case meta:
        case unknown:
            throw new IllegalArgumentException("'"+collection.getType()+"' collections don't support cached copies.");
        default:
            return url;
        }
    }

    private Map<String, String> parseExecutionOutput(String str) {
        Map<String, String> out = new HashMap<String, String>();
        for (String s: str.split(GET_DOCUMENT_OUTPUT_LINE_SEP)) {
            String[] kv = s.split(GET_DOCUMENT_OUTPUT_KV_SEP);
            if (kv.length == 2) {
                out.put(kv[0], kv[1]);
            } else {
                log.warn("Ignoring invalid output line '"+s+"'");
            }
        }
        return out;
    }

}
