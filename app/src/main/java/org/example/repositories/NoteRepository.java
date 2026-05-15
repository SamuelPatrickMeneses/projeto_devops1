package org.example.repositories;

import java.util.logging.Level;
import java.util.List;
import java.util.logging.Logger;

import org.example.models.Note;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class NoteRepository {
    
    @Inject
    Logger logger;

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    @PersistenceContext
    private EntityManager em;

    public List<Note> findAll() {
        return em.createQuery("SELECT n FROM Note n", Note.class).getResultList();
    }

    @Transactional
    public void save(Note note) {
        try {
            if (note.getId() == 0) {
                em.persist(note);
            } else {

                em.merge(note);
            }
        }catch (jakarta.validation.ConstraintViolationException e) {
            e.getConstraintViolations().forEach(violation -> {
                logger.log(Level.WARNING,"Erro no campo: " + violation.getPropertyPath());
                logger.log(Level.WARNING,"Mensagem: " + violation.getMessage());
                logger.log(Level.WARNING,"Valor inválido: " + violation.getInvalidValue());
                if (violation.getInvalidValue().getClass() == String.class ) {
                    String value = (String) violation.getInvalidValue();
                    logger.log(Level.WARNING,"Valor inválido length: " +  String.valueOf(value.length()));
                }
            });
            throw e;
        }
    }

    public Note findById(long id) {
        return em.find(Note.class, id);
    }

    @Transactional
    public void delete(long id) {
        Note note = findById(id);
        if (note != null) {
            em.remove(note);
        }
    }
}

