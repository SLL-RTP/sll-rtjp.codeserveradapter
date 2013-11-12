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
package se.sll.codeserveradapter.paymentresponsible.rs;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.springframework.beans.factory.annotation.Autowired;

import se.sll.codeserveradapter.jmx.StatusBean;
import se.sll.codeserveradapter.paymentresponsible.service.HSAMappingService;

/**
 * Admin service to trigger revalidation of index data.
 * 
 * @author Peter
 */
@Path("/")
public class AdminService {
    @Autowired
    private HSAMappingService hsaMappingService;

    @Autowired
    private StatusBean statusBean;

    @GET
    @Produces("application/json")
    @Path("/revalidate-index")
    public Response rebuildIndex() {
        boolean success = false;
        statusBean.start("/revalidate-index");
        try {
            hsaMappingService.revalidate();
            return Response.ok().build();
        } finally {
            statusBean.stop(success);
        }
    }
}
