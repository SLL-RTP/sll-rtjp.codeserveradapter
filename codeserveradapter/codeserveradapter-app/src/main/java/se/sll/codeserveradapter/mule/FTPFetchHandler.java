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
package se.sll.codeserveradapter.mule;

import org.mule.api.MuleEventContext;
import org.mule.api.lifecycle.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.sll.codeserveradapter.paymentresponsible.service.HSAMappingService;

public class FTPFetchHandler implements Callable {

    private static final Logger log = LoggerFactory.getLogger(FTPFetchHandler.class);

    public FTPFetchHandler() {
        log.info("Created.");
    }

    @Override
    public Object onCall(MuleEventContext eventContext) throws Exception {
        log.info("Revalidate index.");

        HSAMappingService.getInstance().revalidate();
        
        return eventContext.getMessage();
    }

}