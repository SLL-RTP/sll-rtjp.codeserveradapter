/**
 *  Copyright (c) 2013 SLL <http://sll.se/>
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package se.sll.codeserveradapter.paymentresponsible.service;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Service executing batch jobs. Currently it's only about fetching master data files, and rebuild
 * the index.
 * 
 * @author Peter
 */
@Service
public class JobService {
    //
    private static final Logger log = LoggerFactory.getLogger(JobService.class);

    @Value("${pr.ftp.script:}")
    private String script;
    
    @Value("${pr.ftp.localPath}")
    private String localPath;
    
    @Autowired
    private HSAMappingService hsaMappingService;

    /**
     * Invokes and externally managed script to fetch master data, and
     * then revalidates the index. <p>
     * 
     * The actual cron expression is configurable.
     */
    @Scheduled(cron="${pr.ftp.cron}")
    public void ftpFetchScript() {
        if (script.length() == 0) {
            log.warn("Batch ftp script has not been defined, please check configuration property \"pr.ftp.script\"");
            return;
        }
        log.info("Fetch files using script {}", script);
        try {
            final Process p = Runtime.getRuntime().exec(script, null, new File(localPath));
            close(p.getOutputStream());
            handleInputStream(p.getInputStream(), false);
            handleInputStream(p.getErrorStream(), true);
            p.waitFor();
            if (p.exitValue() != 0) {
                log.error("Script {} returned with exit code {}", script, p.exitValue());
            } else {
                log.info("Script {} completed successfully", script);
                hsaMappingService.revalidate();
            }
        } catch (Exception e) {
            log.error("Unable to update from master data " + script, e);
        }
    }
    

    /**
     * Logs input from an input stream to error or info level.
     * 
     * @param is the input stream.
     * @param err if it's error, otherwise is info assumed.
     */
    private void log(final InputStream is, final boolean err) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(is));
            String line = reader.readLine();
            while (line != null) {
                if (err) {
                    log.error(line);
                }  else {
                    log.info(line);
                }
                line = reader.readLine();
            }    
        } catch (Exception e) {
            log.error("Error while reading input stream", e);
        } finally {
            close(reader);
        }
    }

    /**
     * Force a close operation and ignore errors.
     * 
     * @param c the closeable to close.
     */
    private void close(Closeable c) {
        try {
            if (c != null) {
                c.close();
            }
        } catch (IOException e) {
        }
    }
    
    /**
     * Reads an input stream in the background (separate thread).
     * 
     * @param is the input stream.
     * @param err if it's about errors, otherwise is info assumed.
     */
    private void handleInputStream(final InputStream is, final boolean err) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                log(is, err);
            }

        }).run();        
    }
}
