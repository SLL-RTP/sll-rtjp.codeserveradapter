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

import java.io.Closeable;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import se.sll.codeserveradapter.parser.TermItem;
import se.sll.codeserveradapter.paymentresponsible.model.HSAMappingState;
import se.sll.codeserveradapter.paymentresponsible.util.HSAMappingIndexBuilder;

@Service
public class HSAMappingService {

    @Value("${pr.ftp.localPath:}")
    private String localPath;

    @Value("${pr.indexFile:/tmp/hsa-index.gz}")
    private String fileName;

    @Value("${pr.commissionFile}")
    private String commissionFile;

    @Value("${pr.commissionTypeFile}")
    private String commissionTypeFile;

    @Value("${pr.facilityFile}")
    private String facilityFile;

    @Value("${pr.mekFile}")
    private String mekFile;

    private boolean busy;
    private static HSAMappingService instance;
    private static final Logger log = LoggerFactory.getLogger(HSAMappingService.class);


    private Map<String, List<TermItem<HSAMappingState>>> currentIndex;
    private final Object buildLock = new Object();

    public HSAMappingService() {
        if (instance == null) {
            instance = this;
        }
    }

    private String path(String name) {
        return localPath + (localPath.endsWith("/") ? "" : "/") + name;
    }

    private Map<String, List<TermItem<HSAMappingState>>> build() {
        HSAMappingIndexBuilder builder = new HSAMappingIndexBuilder()
        .withCommissionFile(path(commissionFile))
        .withCommissionTypeFile(path(commissionTypeFile))
        .withFacilityFile(path(facilityFile))
        .withMekFile(path(mekFile));

        final Map<String, List<TermItem<HSAMappingState>>> index = builder.build();

        return index;
    }

    public boolean isBusy() {
        return busy;
    }

    public void setBusy(boolean busy) {
        this.busy = busy;
    }

    public void revalidate() {
        if (isBusy()) {
            return;
        }
        synchronized (buildLock) {
            if (isBusy()) {
                return;
            }
            setBusy(true);
            try {
                final Map<String, List<TermItem<HSAMappingState>>> index = build();
                save(index);
                setCurrentIndex(index);
            } finally {
                setBusy(false);            
            }
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T read() {
        ObjectInputStream is = null;
        try {
            is = new ObjectInputStream(new GZIPInputStream(new FileInputStream(fileName)));
            return (T) is.readObject();
        } catch (Exception e) {
            log.warn(e.toString());
        } finally {
            close(is);
        }
        return null;            
    }

    private <T> T save(T index) {
        ObjectOutputStream os = null;
        try {
            os = new ObjectOutputStream(new GZIPOutputStream(new FileOutputStream(fileName)));
            os.writeObject(index);
        } catch (Exception e) {
            log.error("Unable to save HSA index", e);
        } finally {
            close(os);
        }
        return index;
    }

    //
    private void close(Closeable c) {
        try {
            if (c != null) {
                c.close();
            }
        } catch (IOException e) {}
    }

    public static HSAMappingService getInstance() {
        return instance;
    }

    public synchronized Map<String, List<TermItem<HSAMappingState>>> getCurrentIndex() {
        if (currentIndex == null) {
            Map<String, List<TermItem<HSAMappingState>>> index = read();
            setCurrentIndex(index);
        }
        return this.currentIndex;
    }

    public synchronized void setCurrentIndex(Map<String, List<TermItem<HSAMappingState>>> currentIndex) {
        this.currentIndex = currentIndex;
    }
}
