package org.example.controller;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.example.models.Note;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class NoteControllerTest {

    private static Client client;
    private static WebTarget target;
    private static Long createdNoteId;

    @BeforeAll
    static void setup() {
        String baseUri = System.getProperty("base.uri", "http://nginx-dev/api");
        client = ClientBuilder.newClient();
        target = client.target(baseUri).path("notes");
    }

    @AfterAll
    static void cleanup() {
        if (client != null) {
            client.close();
        }
    }

    @Test
    @Order(1)
    void testCreate() {
        Note note = new Note();
        note.setTitle("Test Note");
        note.setContent("Test Content");

        try (Response response = target
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(note))) {


            assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus(),
                    "Deveria retornar 201 Created");

            Note created = response.readEntity(Note.class);
            assertNotNull(created);
            assertNotNull(created.getId(), "ID deveria ser gerado");
            assertTrue(created.getId() > 0, "ID deveria ser positivo");
            assertEquals("Test Note", created.getTitle());
            assertEquals("Test Content", created.getContent());

            createdNoteId = created.getId();
        } 
    }

    //@Test
    //@Order(2)
    //void testCreate_InvalidTitle() {
    //    Note note = new Note();
    //    note.setTitle("invalido@aqui!");   // regex falha: apenas alfanumerico, espaco e underscore
    //    note.setContent("Content");

    //    try (Response response = target
    //            .request(MediaType.APPLICATION_JSON)
    //            .post(Entity.json(note))) {

    //        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus(),
    //                "Deveria retornar 400 Bad Request para titulo invalido");

    //        Map<String, String> errors = response.readEntity(new GenericType<Map<String, String>>() {});
    //        assertTrue(errors.containsKey("mensage"), "Resposta de erro deveria conter 'mensage'");
    //    }
    //}

    @Test
    @Order(2)
    void testGetAll() {
        List<Note> notes = target
                .request(MediaType.APPLICATION_JSON)
                .get(new GenericType<List<Note>>() {});

        assertNotNull(notes);
        assertFalse(notes.isEmpty(), "Lista de notas nao deveria estar vazia");

        // A nota criada no testCreate deve estar presente
        if (createdNoteId != null) {
            boolean found = notes.stream().anyMatch(n -> n.getId() == createdNoteId);
            assertTrue(found, "Nota criada deveria estar presente na lista");
        }
    }

    @Test
    @Order(3)
    void testGetOne() {
        assertNotNull(createdNoteId, "Depende de testCreate");

        Note note = target
                .path(String.valueOf(createdNoteId))
                .request(MediaType.APPLICATION_JSON)
                .get(Note.class);

        assertNotNull(note);
        assertEquals(createdNoteId.longValue(), note.getId());
        assertEquals("Test Note", note.getTitle());
    }

    @Test
    @Order(4)
    void testUpdate() {
        assertNotNull(createdNoteId, "Depende de testCreate");

        Note update = new Note();
        update.setTitle("Updated Title");
        update.setContent("Updated Content");

        Note result = target
                .path(String.valueOf(createdNoteId))
                .request(MediaType.APPLICATION_JSON)
                .put(Entity.json(update), Note.class);

        assertNotNull(result);
        assertEquals(createdNoteId.longValue(), result.getId());
        assertEquals("Updated Title", result.getTitle());
        assertEquals("Updated Content", result.getContent());
    }

    @Test
    @Order(5)
    void testDelete() {
        assertNotNull(createdNoteId, "Depende de testCreate");

        try (Response response = target
                .path(String.valueOf(createdNoteId))
                .request()
                .delete()) {

            assertEquals(Response.Status.NO_CONTENT.getStatusCode(), response.getStatus(),
                    "Deveria retornar 204 No Content");
        }
    }

    @Test
    @Order(6)
    void testGetOne_NotFound() {
        try (Response response = target
                .path(String.valueOf(createdNoteId))
                .request(MediaType.APPLICATION_JSON)
                .get()) {

            assertEquals(Response.Status.NO_CONTENT.getStatusCode(), response.getStatus(),
                    "Nota deletada deveria retornar 204 No Content");
        }
    }
}
