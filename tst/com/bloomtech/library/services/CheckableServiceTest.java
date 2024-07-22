package com.bloomtech.library.services;

import com.bloomtech.library.exceptions.CheckableNotFoundException;
import com.bloomtech.library.exceptions.ResourceExistsException;
import com.bloomtech.library.models.Library;
import com.bloomtech.library.models.checkableTypes.*;
import com.bloomtech.library.repositories.CheckableRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
public class CheckableServiceTest {

    //TODO: Inject dependencies and mocks
    @MockBean
    private CheckableRepository checkableRepository;
    @Autowired
    private CheckableService checkableService;

    private List<Checkable> checkables;

    @BeforeEach
    void init() {
        //Initialize test data
        checkables = new ArrayList<>();

        checkables.addAll(
                Arrays.asList(
                        new Media("1-0", "The White Whale", "Melvin H", MediaType.BOOK),
                        new Media("1-1", "The Sorcerer's Quest", "Ana T", MediaType.BOOK),
                        new Media("1-2", "When You're Gone", "Complaining at the Disco", MediaType.MUSIC),
                        new Media("1-3", "Nature Around the World", "DocuSpecialists", MediaType.VIDEO),
                        new ScienceKit("2-0", "Anatomy Model"),
                        new ScienceKit("2-1", "Robotics Kit"),
                        new Ticket("3-0", "Science Museum Tickets"),
                        new Ticket("3-1", "National Park Day Pass")
                )
        );
    }

    @Test
    void getAll_noConditions_allCheckablesReturned() {
        when(checkableRepository.findAll()).thenReturn(checkables);

        assertEquals(checkables, checkableService.getAll());

    }
    @Test
    void getByIsbn_isbnExists_checkableReturned() {
        Checkable checkable = checkables.get(0);

        when(checkableRepository.findByIsbn(checkable.getIsbn())).thenReturn(Optional.of(checkable));

        Checkable checkableFromService = checkableService.getByIsbn(checkable.getIsbn());
        assertEquals(checkable, checkableFromService);
        verify(checkableRepository).findByIsbn(anyString());

    }

    @Test
    void getByIsbn_isbnDoesNotExist_checkableNotFoundExceptionThrown() {


        Checkable checkable = new Media("1-0", "The Black Whale", "Melvin H", MediaType.BOOK);

        when(checkableRepository.findByIsbn(checkable.getIsbn())).thenReturn(Optional.empty());
        assertThrows(CheckableNotFoundException.class, () -> {
            checkableService.getByIsbn(checkable.getIsbn());
        });
    }


    @Test
    void getByType_mediaTypeExists_firstCheckableReturned() {
        when(checkableRepository.findByType(ScienceKit.class)).thenReturn(Optional.of(checkables.get(4)));
        Checkable checkableByType = checkableService.getByType(ScienceKit.class);
        assertEquals(checkables.get(4), checkableByType);

    }

    @Test
    void getByType_ticketTypeExists_firstCheckableReturned() {
        when(checkableRepository.findByType(Ticket.class)).thenReturn(Optional.of(checkables.get(6)));

        Checkable checkableByType = checkableService.getByType(Ticket.class);
        assertEquals(checkables.get(6), checkableByType);

    }
    @Test
    void getByType_typeDoesNotExist_checkableNotFoundExceptionThrown() {
        when(checkableRepository.findByType(Ticket.class)).thenReturn(Optional.empty());

        assertThrows(CheckableNotFoundException.class, () -> {
            checkableService.getByType(Ticket.class);
        });

    }
    @Test
    void save_uniqueIsbn_checkableSaved() {
        Checkable checkable = new Media("222", "The Black Whale", "Edward", MediaType.BOOK);
        when(checkableRepository.findAll()).thenReturn(checkables);
        checkableService.save(checkable);
        verify(checkableRepository).save(checkable);
    }
    @Test
    void save_duplicateIsbn_resourceExistsExceptionThrown() {
        Checkable checkable = checkables.get(0);
        when(checkableRepository.findAll()).thenReturn(checkables);

        assertThrows(ResourceExistsException.class, () -> {
            checkableService.save(checkable);
        });




    }

}