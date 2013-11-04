package se.sll.codeserveradapter.paymentresponsible.rs;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import se.sll.codeserveradapter.paymentresponsible.service.HSAMappingService;

@Path("/")
public class AdminService {

    @GET
    @Produces("application/json")
    @Path("/rebuild-index")
    public Response  rebuildIndex() {
        HSAMappingService.getInstance().revalidate();
        return Response.ok().build();
    }
}
