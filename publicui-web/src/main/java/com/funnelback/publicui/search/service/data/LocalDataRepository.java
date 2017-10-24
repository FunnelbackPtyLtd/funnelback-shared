package com.funnelback.publicui.search.service.data;

import static com.funnelback.common.io.file.FileUtils.getFileExtensionLowerCase;

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

import lombok.extern.log4j.Log4j2;

import org.apache.commons.io.FileUtils;
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

import com.funnelback.common.config.Collection.Type;
import com.funnelback.common.config.DefaultValues;
import com.funnelback.common.config.Keys;
import com.funnelback.common.io.store.RawBytesRecord;
import com.funnelback.common.io.store.Record;
import com.funnelback.common.io.store.Store;
import com.funnelback.common.io.store.Store.RecordAndMetadata;
import com.funnelback.common.io.store.StoreType;
import com.funnelback.common.io.store.XmlRecord;
import com.funnelback.common.url.VFSURLUtils;
import com.funnelback.common.utils.XMLUtils;
import com.funnelback.common.views.StoreView;
import com.funnelback.common.views.View;
import com.funnelback.publicui.i18n.I18n;
import com.funnelback.publicui.search.lifecycle.data.fetchers.padre.AbstractPadreForking.EnvironmentKeys;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.service.DataRepository;
import com.funnelback.publicui.search.service.data.exception.TRIMException;
import com.funnelback.publicui.utils.ExecutionReturn;
import com.funnelback.publicui.utils.jna.WindowsFileInputStream;
import com.funnelback.publicui.utils.jna.WindowsNativeExecutor;
import com.funnelback.publicui.utils.jna.WindowsNativeExecutor.ExecutionException;
import com.google.common.collect.ArrayListMultimap;

/**
 * {@link DataRepository} implementation against the 
 * local filesystem
 */
@Repository
@Log4j2
public class LocalDataRepository implements DataRepository {
    
    /** Name of the parameter containing the record id for database collections */
    private final static String RECORD_ID = "record_id";

    /** How long to wait (in ms.) to get a document out of TRIM */
    private static final int GET_DOCUMENT_WAIT_TIMEOUT = 1000*60;
    
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
    
    /** Base folder containing the the binaries to get a document from TRIM, one per TRIM version */
    private final File getDocumentBinaryBase;
    
    /** Environment used when calling the binary to get a document */
    private final Map<String, String> getDocumentEnvironment;
    
    private final String WARC_FILE_EXTENSION = "warc";

    @Autowired
    private I18n i18n;

    /**
     * @param searchHome Funnelback installation folder, to be able to locate
     * the TRIM binaries from <code>wbin/trim/</code>.
     */
    @Autowired
    public LocalDataRepository(File searchHome) {
        getDocumentBinaryBase = new File(searchHome, DefaultValues.FOLDER_WINDOWS_BIN);

        // Copy ALL the environment here. The TRIM SDK requires some environment
        // variables to be set, such as "SystemRoot" and "CommonProgramFiles"
        getDocumentEnvironment = new HashMap<String, String>(System.getenv());
        getDocumentEnvironment.put(EnvironmentKeys.SEARCH_HOME.toString(), searchHome.getAbsolutePath());
    }
    
    @Override
    public RecordAndMetadata<? extends Record<?>> getDocument(Collection collection, StoreView view,
        String url, File relativePath, long offset, int length) {
        
        String fileExtension =
            getFileExtensionLowerCase(relativePath);

        if (fileExtension.equals(WARC_FILE_EXTENSION)) {
            // FUN-5956 WARC files not supported yet
            return null;
        }
        
        if (relativePath.isAbsolute()) {
            throw new IllegalArgumentException("Invalid path '" + relativePath + "'");
        }
        
        byte[] content = null;
        try {
            File path = getDocumentFile(collection, relativePath, view);
            if (path == null) {
                // Not found
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
            log.error("Error while accessing cached document '"+relativePath
                +"' on collection '"+collection.getId()+"'", ioe);
            content = null;
        }
        
        if (content != null) {
            if (fileExtension.equals(XMLUtils.XML)) {
                return new RecordAndMetadata<XmlRecord>(new XmlRecord(
                    XMLUtils.fromString(new String(content)), url),
                    ArrayListMultimap.create());
            } else {
                return new RecordAndMetadata<RawBytesRecord>(new RawBytesRecord(content, url),
                    ArrayListMultimap.create());
            }
        }
        
        return null;
    }
    
    /**
     * Attempt to get a document from the data folder of a collection for the given view,
     * looking a the secondary data folder first (for instant updates) then the main one
     * @param collection Collection object
     * @param relativePath Relative (to the <code>live/data/</code> folder) path of the file to get 
     * @param view View to look at
     * @return The path to the actual file, either from the main data folder or the secondary one, or
     * null if the file is not found
     * @throws IOException
     */
    private File getDocumentFile(Collection collection, File relativePath, StoreView view) throws IOException {
        File[] candidatesFolders = new File[] {
            new File(collection.getConfiguration().getCollectionRoot(),
                view.toString() + File.separator + DefaultValues.Secondary.FOLDER_DATA),
            new File(collection.getConfiguration().getCollectionRoot(),
                view.toString() + File.separator + DefaultValues.FOLDER_DATA)};
        
        if (Type.local.equals(collection.getType())) {
            candidatesFolders = new File[] {collection.getConfiguration().getDataRoot()};
        }
        
        for (File folder: candidatesFolders) {
            File path = new File(folder, relativePath.getPath());
            
            if(!com.funnelback.common.io.file.FileUtils.isChildOf(folder, path)) {
                throw new IllegalArgumentException("Invalid path '" + relativePath + "'");
            } else if (path.exists()) {
                return path;
            }
        }
        
        return null;
    }
    
    @Override
    public RecordAndMetadata<? extends Record<?>> getCachedDocument(
            Collection collection, StoreView view, String url) {
        
        try (Store<? extends Record<?>> store = StoreType.getStore(collection.getConfiguration(), view)) {
            log.trace("Got a store for collection '"+collection.getId()+"', it is a: " 
                + store.getClass().getCanonicalName());
            store.open();
            return store.getRecordAndMetadata(extractPrimaryKey(collection, url));        
        } catch (ClassNotFoundException cnfe) {
            log.error("Error while getting store for collection '"+collection.getId()+"'", cnfe);
        } catch (IOException ioe) {
            log.error("Couldn't access stored content on collection '"+collection.getId()+"' for URL '"+url+"'", ioe);
        } catch (UnsupportedOperationException uoe) {
            // Ignore, some collection types (local) are not supported
            log.debug("Unsupported operation on the store for collection '"+collection.getId()+"'", uoe);
        }
    
        return new RecordAndMetadata<>(null, null);

    }
    
    @Override
    public InputStream getFilecopyDocument(Collection collection, URI uri,
            boolean withDls) throws IOException {
        if (withDls) {
            // Convert the URI to a Windows path, taking care to preserve plus signs
            String windowsPath = VFSURLUtils.vfsUrlToSystemUrl(uri, true);
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
            // Rebuild the URL from its component, so that getPath() returns the *decoded* path.
            // e.g. URI(smb://server/%C3%A9.txt).toString() would stay encoded and wouldn't match the real
            // file on disk "é.txt". getPath() decodes the path part.
            // In addition, Apache VFS expects % (and only %...) to be encoded, so encode it with vfsUrlEncode()
            FileObject file = manager.resolveFile(VFSURLUtils.vfsUrlEncode(uri.getScheme() + "://"
                + ((uri.getHost() != null) ? uri.getHost() : "")
                + uri.getPath()), options);
            return file.getContent().getInputStream();
        }
    }
    
    @Override
    public File getTemporaryTrimDocument(Collection collection, int trimUri) throws TRIMException, IOException {
        File tempFolder = new File(collection.getConfiguration().getCollectionRoot()
            + File.separator + View.live,
            File.separator + DefaultValues.FOLDER_TMP);

        File getDocumentBinary = new File(getDocumentBinaryBase
                + File.separator + collection.getConfiguration().value(Keys.Trim.VERSION, DefaultValues.Trim.VERSION),
                GET_DOCUMENT_BINARY);
        
        List<String> cmdLine = new ArrayList<String>(
            Arrays.asList(new String[] {
                getDocumentBinary.getAbsolutePath(),
                "-i", Integer.toString(trimUri), "-f",
                tempFolder.getAbsolutePath(), collection.getId() }));

        try {
            ExecutionReturn er = new WindowsNativeExecutor(i18n, GET_DOCUMENT_WAIT_TIMEOUT)
                .execute(cmdLine, getDocumentEnvironment, 32, Integer.MAX_VALUE, getDocumentBinary.getParentFile());
            
            Map<String, String> executionOutput = parseExecutionOutput(new String(er.getOutBytes(), er.getCharset()));
            
            if (er.getReturnCode() != GET_DOCUMENT_SUCCESS) {
                String error = executionOutput.get(ERROR_KEY);
                if (error != null) {
                    throw TRIMException.fromGetDocumentExitCode(er.getReturnCode(), error, trimUri);
                } else {
                    // Unknown error
                    log.error("Document fetcher returned a non-zero status ("
                        + er.getReturnCode()+") with command line '"
                        + cmdLine + "'. Output was '"+ new String(er.getOutBytes(), er.getCharset()) + "'");
                    throw new TRIMException("Error while retrieving document: "
                        + error);
                }
            } else {
                File trimDoc = new File(executionOutput.get(FILE_KEY));
                
                if (trimDoc.canRead() && trimDoc.isFile()) {
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
    private String extractPrimaryKey(Collection collection, String url) {
        try {
            switch (collection.getType()) {
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
        catch (UnsupportedEncodingException exception) {
            throw new RuntimeException(exception);
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
