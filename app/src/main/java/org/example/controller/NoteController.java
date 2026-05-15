package org.example.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.example.models.Note;
import org.example.repositories.NoteRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.logging.Level;
import java.util.logging.Logger;

@Path("notes")
@Produces(MediaType.APPLICATION_JSON) 
@Consumes(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class NoteController {
    
    @Inject
    Logger logger;

    public void setLogger(Logger logger) {
        this.logger = logger;
    }
    

    @Inject
    NoteRepository repository;

    @GET
    public List<Note> getAll() {
        return repository.findAll();
    }

    @GET
    @Path("{id}")
    public Note getOne(@PathParam("id") long id) {
        return repository.findById(id);
    }

    @POST
    public Response create(Note note) {
        try {
            repository.save(note);
        }catch (jakarta.validation.ConstraintViolationException e) {
            Map<String, String> message = new HashMap<>();
            e.getConstraintViolations().forEach(violation -> {
                message.put("field", violation.getPropertyPath().toString());
                message.put("mensage", violation.getMessage());
                message.put("value", violation.getInvalidValue().toString());
            });
            return Response.status(Response.Status.BAD_REQUEST).entity(message).build();
        }
        return Response.status(Response.Status.CREATED).entity(note).build();
    }

    @PUT
    @Path("{id}")
    public Response update(@PathParam("id") long id, Note note) {
        note.setId(id);
        try {
            repository.save(note);
        }catch (jakarta.validation.ConstraintViolationException e) {
            Map<String, String> message = new HashMap<>();
            e.getConstraintViolations().forEach(violation -> {
                message.put("field", violation.getPropertyPath().toString());
                message.put("mensage", violation.getMessage());
                message.put("value", violation.getInvalidValue().toString());
            });
            return Response.status(Response.Status.BAD_REQUEST).entity(message).build();
        } catch (Exception e) {

            Map<String, String> message = new HashMap<>();
            message.put("set_extension", e.getClass().getName());
            return Response.status(Response.Status.BAD_REQUEST).entity(message).build();
        }
        return Response.status(Response.Status.CREATED).entity(note).build();
    }

    @DELETE
    @Path("{id}")
    public Response delete(@PathParam("id") long id) {
        repository.delete(id);
        return Response.noContent().build();
    }
}

