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
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.springframework.stereotype.Service;

import se.sll.codeserveradapter.paymentresponsible.model.HSAMappingBean;
import se.sll.codeserveradapter.paymentresponsible.util.HSAMappingIndexBuilder;

//@Service
public class HSAMappingService {

    private String fileName = "/tmp/hsa-index.dat.gz";
    private boolean busy;
    private static HSAMappingService instance;

    private Map<String, List<HSAMappingBean>> currentIndex;

    public HSAMappingService() {
        if (instance == null) {
            instance = this;
        }
        if (getCurrentIndex() == null) {
            revalidate();
        }
     }

    private Map<String, List<HSAMappingBean>> build() {
        HSAMappingIndexBuilder builder = new HSAMappingIndexBuilder()
        .withCommissionFile("src/test/resources/test-files/SAMVERKS-REL.xml")
        .withCommissionTypeFile("src/test/resources/test-files/UPPDRAGSTYP.xml")
        .withFacilityFile("src/test/resources/test-files/AVD-REL.xml")
        .withMekFile("src/test/resources/test-files/MEK.xml");

        final Map<String, List<HSAMappingBean>> index = builder.build();

        return index;
    }

    public boolean isBusy() {
        return busy;
    }

    public void setBusy(boolean busy) {
        this.busy = busy;
    }

    public synchronized void revalidate() {
        if (isBusy()) {
            return;
        }
        setBusy(true);
        try {
            final Map<String, List<HSAMappingBean>> index = build();
            save(index);
            setCurrentIndex(index);
        } finally {
            setBusy(false);            
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T read() {
        ObjectInputStream is = null;
        try {
            is = new ObjectInputStream(new ZipInputStream(new FileInputStream(fileName)));
            return (T) is.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close(is);
        }
        return null;            
    }

    private <T> T save(T index) {
        ObjectOutputStream os = null;
        try {
            os = new ObjectOutputStream(new ZipOutputStream(new FileOutputStream(fileName)));
            os.writeObject(index);
        } catch (Exception e) {
            e.printStackTrace();
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

    public synchronized Map<String, List<HSAMappingBean>> getCurrentIndex() {
        if (currentIndex == null) {
            Map<String, List<HSAMappingBean>> index = read();
            setCurrentIndex(index);
        }
        return this.currentIndex;
    }

    public synchronized void setCurrentIndex(Map<String, List<HSAMappingBean>> currentIndex) {
        this.currentIndex = currentIndex;
    }
}
