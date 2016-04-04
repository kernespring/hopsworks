package se.kth.hopsworks.rest;


import org.apache.commons.beanutils.BeanUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONObject;
import se.kth.hopsworks.controller.ResponseMessages;
import se.kth.hopsworks.filters.AllowedRoles;
import se.kth.hopsworks.workflows.Node;
import se.kth.hopsworks.workflows.NodeFacade;
import se.kth.hopsworks.workflows.NodePK;
import se.kth.hopsworks.workflows.Workflow;

import javax.ejb.EJB;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.enterprise.context.RequestScoped;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;
import se.kth.hopsworks.workflows.nodes.*;
import static com.google.common.base.CaseFormat.*;

@RequestScoped
@TransactionAttribute(TransactionAttributeType.NEVER)
public class NodeService {
    private final static Logger logger = Logger.getLogger(NodeService.class.
            getName());


    @EJB
    private NodeFacade nodeFacade;

    @EJB
    private NoCacheResponse noCacheResponse;

    public void setWorkflow(Workflow workflow) {
        this.workflow = workflow;
    }

    private Workflow workflow;

    public NodeService(){

    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @AllowedRoles(roles = {AllowedRoles.DATA_SCIENTIST, AllowedRoles.DATA_OWNER})
    public Response index() throws AppException {
        List<Node> nodes = nodeFacade.findAll();
        return noCacheResponse.getNoCacheResponseBuilder(Response.Status.OK).entity(nodes).build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @AllowedRoles(roles = {AllowedRoles.DATA_SCIENTIST, AllowedRoles.DATA_OWNER})
    public Response create(
            String stringParams,
            @Context HttpServletRequest req) throws AppException, ClassNotFoundException, IllegalAccessException, InstantiationException, InvocationTargetException, IOException {

        HashMap<String,Object> paramsMap =
                new ObjectMapper().readValue(stringParams, HashMap.class);
        String className = LOWER_HYPHEN.to(UPPER_CAMEL, paramsMap.get("type").toString());
        Class nodeClass = Class.forName("se.kth.hopsworks.workflows.nodes." + className);
        Node node = (Node)nodeClass.newInstance();
        paramsMap.put("data", new JSONObject((HashMap)paramsMap.get("data")));
        BeanUtils.populate(node, paramsMap);
        node.setWorkflowId(workflow.getId());


        nodeFacade.persist(node);
        return noCacheResponse.getNoCacheResponseBuilder(Response.Status.OK).entity(nodeClass.cast(node)).build();

    }

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @AllowedRoles(roles = {AllowedRoles.DATA_SCIENTIST, AllowedRoles.DATA_OWNER})
    public Response show(
            @PathParam("id") String id) throws AppException, ClassNotFoundException {
        NodePK nodePk = new NodePK(id, workflow.getId());

        Node node = nodeFacade.findById(nodePk);
        if (node == null) {
            throw new AppException(Response.Status.BAD_REQUEST.getStatusCode(),
                    ResponseMessages.NODE_NOT_FOUND);
        }

        String className = LOWER_HYPHEN.to(UPPER_CAMEL, node.getType());
        Class nodeClass = Class.forName("se.kth.hopsworks.workflows.nodes." + className);

        return noCacheResponse.getNoCacheResponseBuilder(Response.Status.OK).entity(nodeClass.cast(node)).build();
    }

    @PUT
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @AllowedRoles(roles = {AllowedRoles.DATA_SCIENTIST, AllowedRoles.DATA_OWNER})
    public Response update(
            String stringParams,
            @PathParam("id") String id
    ) throws AppException {
        JSONObject params = new JSONObject(stringParams);
        NodePK nodePk = new NodePK(id, workflow.getId());
        Node node = nodeFacade.findById(nodePk);
        if (node == null) {
            throw new AppException(Response.Status.BAD_REQUEST.getStatusCode(),
                    ResponseMessages.NODE_NOT_FOUND);
        }
        String className = LOWER_HYPHEN.to(UPPER_CAMEL, params.get("type").toString());
        params.put("classname", className);
        nodeFacade.update(node, params);

        return noCacheResponse.getNoCacheResponseBuilder(Response.Status.OK).entity(nodeFacade.refresh(node)).build();
    }

    @DELETE
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @AllowedRoles(roles = {AllowedRoles.DATA_SCIENTIST, AllowedRoles.DATA_OWNER})
    public Response delete(
            @PathParam("id") String id) throws AppException {
        NodePK nodePk = new NodePK(id, workflow.getId());
        Node node = nodeFacade.findById(nodePk);
        if (node == null) {
            throw new AppException(Response.Status.BAD_REQUEST.getStatusCode(),
                    ResponseMessages.NODE_NOT_FOUND);
        }
        nodeFacade.remove(node);
        return noCacheResponse.getNoCacheResponseBuilder(Response.Status.OK).build();
    }
}