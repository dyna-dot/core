package com.dotcms.rest.api.v3.contenttype;

import com.dotcms.contenttype.business.FieldAPI;
import com.dotcms.contenttype.model.field.Field;
import com.dotcms.contenttype.model.field.layout.FieldLayout;
import com.dotcms.contenttype.model.type.ContentType;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.dotcms.rest.exception.NotFoundException;
import org.glassfish.jersey.server.JSONP;
import com.dotcms.rest.InitDataObject;
import com.dotcms.rest.ResponseEntityView;
import com.dotcms.rest.WebResource;
import com.dotcms.rest.annotation.NoCache;
import com.dotmarketing.business.APILocator;
import com.dotmarketing.exception.DotDataException;
import com.dotmarketing.exception.DotSecurityException;
import com.liferay.portal.model.User;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static com.dotcms.util.CollectionsUtils.list;
import static com.dotcms.util.CollectionsUtils.map;

/**
 * Resource for handle fields operations, all this end point check if the {@link ContentType}'s layout is valid
 *
 * @see FieldLayout
 */
@Path("/v3/contenttype/{typeIdOrVarName}/fields")
public class FieldResource {
    private final WebResource webResource;
    private final FieldAPI fieldAPI;

    public FieldResource() {
        this.webResource = new WebResource();
        this.fieldAPI = APILocator.getContentTypeFieldAPI();
    }

    /**
     * Update a set of field and return the new {@link ContentType}'s layout in the response
     *
     * @param typeIdOrVarName
     * @param fieldId
     * @param updateFieldForm
     * @param req
     * @return
     * @throws DotDataException
     * @throws DotSecurityException
     */
    @PUT
    @JSONP
    @NoCache
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({ MediaType.APPLICATION_JSON, "application/javascript" })
    @Path("/{id}")
    public Response updateField(
            @PathParam("typeIdOrVarName") final String typeIdOrVarName,
            @PathParam("id") final String fieldId,
            final UpdateFieldForm updateFieldForm,
            @Context final HttpServletRequest req)
            throws DotDataException, DotSecurityException {

        final InitDataObject initData =
                this.webResource.init(null, true, req, true, null);
        final User user = initData.getUser();

        final Field fieldToUpdate = updateFieldForm.getField();
        final ContentType contentType = APILocator.getContentTypeAPI(user).find(typeIdOrVarName);

        checkFieldExists(fieldToUpdate, contentType);

        final FieldLayout fieldLayout = new FieldLayout(contentType.fields());
        final FieldLayout fieldLayoutUpdated = fieldLayout.update(list(fieldToUpdate));

        fieldLayoutUpdated.validate();
        fieldAPI.save(fieldToUpdate, user);

        final List<Field> contentTypeFields = fieldAPI.byContentTypeId(contentType.id());
        final FieldLayout fieldLayoutFromDB = new FieldLayout(contentTypeFields);

        return Response.ok(new ResponseEntityView(fieldLayoutFromDB.getRows())).build();
    }


    /**
     * Create a new field and return the new {@link ContentType}'s layout in the response
     *
     * @param typeIdOrVarName
     * @param updateFieldForm
     * @param req
     * @return
     * @throws DotDataException
     * @throws DotSecurityException
     */
    @POST
    @JSONP
    @NoCache
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({ MediaType.APPLICATION_JSON, "application/javascript" })
    public Response createField(
            @PathParam("typeIdOrVarName") final String typeIdOrVarName,
            final UpdateFieldForm updateFieldForm,
            @Context final HttpServletRequest req)
            throws DotDataException, DotSecurityException {

        final InitDataObject initData =
                this.webResource.init(null, true, req, true, null);
        final User user = initData.getUser();

        final Field fieldToUpdate = updateFieldForm.getField();
        final ContentType contentType = APILocator.getContentTypeAPI(user).find(typeIdOrVarName);

        final FieldLayout fieldLayout = new FieldLayout(contentType.fields());
        final FieldLayout fieldLayoutUpdated = fieldLayout.update(list(fieldToUpdate));

        fieldLayoutUpdated.validate();
        fieldAPI.save(fieldToUpdate, user);

        final List<Field> contentTypeFields = fieldAPI.byContentTypeId(contentType.id());
        final FieldLayout fieldLayoutFromDB = new FieldLayout(contentTypeFields);

        return Response.ok(new ResponseEntityView(fieldLayoutFromDB.getRows())).build();
    }

    private void checkFieldExists(final Field fieldToUpdate, final ContentType contentType) {
        Optional<Field> optionalField = contentType.fields()
                .stream()
                .filter(field -> field.id().equals(fieldToUpdate.id()))
                .findFirst();

        if (!optionalField.isPresent()) {
            throw new NotFoundException(String.format("Field %s does not exists", fieldToUpdate.variable()));
        }
    }

    @PUT
    @JSONP
    @NoCache
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({ MediaType.APPLICATION_JSON, "application/javascript" })
    @Path("/move")
    public Response moveFields(
            @PathParam("typeIdOrVarName") final String typeIdOrVarName,
            final MoveFieldsForm moveFieldsForm,
            @Context final HttpServletRequest req)
            throws DotDataException, DotSecurityException {

        final InitDataObject initData =
                this.webResource.init(null, true, req, true, null);
        final User user = initData.getUser();
        final ContentType contentType = APILocator.getContentTypeAPI(user).find(typeIdOrVarName);

        final FieldLayout layout = moveFieldsForm.getRows(contentType.id());

        fieldAPI.saveFields(layout.getFields(), user);

        final List<Field> contentTypeFields = fieldAPI.byContentTypeId(contentType.id());
        final FieldLayout fieldLayoutFromDB = new FieldLayout(contentTypeFields);
        return Response.ok(new ResponseEntityView(fieldLayoutFromDB.getRows())).build();
    }

    /**
     * Update a set of fields and return the new {@link ContentType}'s layout in the response
     *
     * @param typeIdOrVarName
     * @param updateFieldsForm
     * @param req
     * @return
     * @throws DotDataException
     * @throws DotSecurityException
     */
    @PUT
    @JSONP
    @NoCache
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({ MediaType.APPLICATION_JSON, "application/javascript" })
    public Response updateFields(
            @PathParam("typeIdOrVarName") final String typeIdOrVarName,
            final UpdateFieldsForm updateFieldsForm,
            @Context final HttpServletRequest req)
                throws DotDataException, DotSecurityException {

        final InitDataObject initData =
                this.webResource.init(null, true, req, true, null);
        final User user = initData.getUser();

        final List<Field> fieldsToUpdate = updateFieldsForm.getFields();
        final ContentType contentType = APILocator.getContentTypeAPI(user).find(typeIdOrVarName);
        final FieldLayout fieldLayout = new FieldLayout(contentType.fields());
        final FieldLayout fieldLayoutUpdated = fieldLayout.update(fieldsToUpdate);

        fieldLayoutUpdated.validate();
        fieldAPI.saveFields(fieldsToUpdate, user);

        final List<Field> contentTypeFields = fieldAPI.byContentTypeId(contentType.id());
        final FieldLayout fieldLayoutFromDB = new FieldLayout(contentTypeFields);

        return Response.ok(new ResponseEntityView(fieldLayoutFromDB.getRows())).build();
    }


    /**
     * Return the {@link ContentType}'s layout
     *
     * @param typeIdOrVarName
     * @param req
     * @return
     * @throws DotDataException
     * @throws DotSecurityException
     */
    @GET
    @JSONP
    @NoCache
    @Produces({ MediaType.APPLICATION_JSON, "application/javascript" })
    public final Response getContentTypeFields(
            @PathParam("typeIdOrVarName") final String typeIdOrVarName,
            @Context final HttpServletRequest req
    ) throws DotDataException, DotSecurityException {

        final InitDataObject initData =
                this.webResource.init(null, true, req, true, null);
        final User user = initData.getUser();

        final ContentType contentType = APILocator.getContentTypeAPI(user).find(typeIdOrVarName);
        final FieldLayout fieldLayout = new FieldLayout(contentType.fields());

        return Response.ok(new ResponseEntityView(fieldLayout.getRows())).build();
    }

    /**
     * Delete a set of fields from the {@link ContentType} and return the new {@link ContentType}'s layout
     * @param typeIdOrVarName
     * @param deleteFieldsForm
     * @param req
     * @return
     * @throws DotDataException
     * @throws DotSecurityException
     */
    @DELETE
    @JSONP
    @NoCache
    @Produces({ MediaType.APPLICATION_JSON, "application/javascript" })
    public Response deleteFields(
            @PathParam("typeIdOrVarName") final String typeIdOrVarName,
            final DeleteFieldsForm deleteFieldsForm,
            @Context final HttpServletRequest req
    )
            throws DotDataException, DotSecurityException {
        final InitDataObject initData =
                this.webResource.init(null, true, req, true, null);
        final User user = initData.getUser();

        final List<String> fieldsID = deleteFieldsForm.getFieldsID();
        final ContentType contentType = APILocator.getContentTypeAPI(user).find(typeIdOrVarName);
        final FieldLayout fieldLayout = new FieldLayout(contentType.fields());
        final FieldLayout fieldLayoutUpdated = fieldLayout.remove(fieldsID);

        fieldLayoutUpdated.validate();
        final Collection<String> ids = fieldAPI.deleteFields(fieldsID, user);
        return Response.ok(new ResponseEntityView(
                map(
                   "fields", fieldLayoutUpdated.getRows(), "deletedIds", ids
                )
        )).build();
    }
}
