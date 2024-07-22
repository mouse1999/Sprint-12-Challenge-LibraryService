package com.bloomtech.library.services;

import com.bloomtech.library.exceptions.LibraryNotFoundException;
import com.bloomtech.library.exceptions.ResourceExistsException;
import com.bloomtech.library.models.*;
import com.bloomtech.library.models.checkableTypes.Checkable;
import com.bloomtech.library.models.checkableTypes.Media;
import com.bloomtech.library.repositories.LibraryRepository;
import com.bloomtech.library.models.CheckableAmount;
import com.bloomtech.library.views.LibraryAvailableCheckouts;
import com.bloomtech.library.views.OverdueCheckout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class LibraryService {
    @Autowired
    private LibraryRepository libraryRepository;
    @Autowired
    private CheckableService checkableService;

    public List<Library> getLibraries() {
        List<Library> libraries = libraryRepository.findAll();

        return new ArrayList<>(libraries);
    }

    public Library getLibraryByName(String name) {
        Optional<Library> libraries = libraryRepository.findByName(name);
        if (libraries.isPresent()) {
            return libraries.get();

        } else {
            throw new LibraryNotFoundException("No library found by the name "+ name);
        }
    }

    public void save(Library library) {
        List<Library> libraries = libraryRepository.findAll();
        if (libraries.stream().filter(p->p.getName().equals(library.getName())).findFirst().isPresent()) {
            throw new ResourceExistsException("Library with name: " + library.getName() + " already exists!");
        }
        libraryRepository.save(library);
    }

    public CheckableAmount getCheckableAmount(String libraryName, String checkableIsbn) {
        Checkable checkable = checkableService.getByIsbn(checkableIsbn);
        Optional<Library> libraryOptional = libraryRepository.findByName(libraryName);

        Library library = libraryOptional.get();
        List<CheckableAmount> checkableAmounts = library.getCheckables();

        for (CheckableAmount checkableAmount : checkableAmounts) {
            if (checkableAmount.getCheckable().equals(checkable)) {
                return checkableAmount;

            }
        } return new CheckableAmount(checkable,0 );
    }

    public List<LibraryAvailableCheckouts> getLibrariesWithAvailableCheckout(String isbn) {
        Checkable checkable = checkableService.getByIsbn(isbn);
        List<Library> libraries = libraryRepository.findAll();

        List<LibraryAvailableCheckouts> available = new ArrayList<>();
        for (Library library : libraries) {
            LibraryAvailableCheckouts temp;
            for (CheckableAmount checkableAmount : library.getCheckables()) {
                if (checkableAmount.getCheckable().equals(checkable)) {
                    temp = new LibraryAvailableCheckouts(checkableAmount.getAmount(), library.getName());
                    available.add(temp);
                }
            }
        } return available;
    }

    public List<OverdueCheckout> getOverdueCheckouts(String libraryName) {
        List<OverdueCheckout> overdueCheckouts = new ArrayList<>();
        Optional<Library> libraryOptional = libraryRepository.findByName(libraryName);
//
        Set<LibraryCard> libraryCards = libraryOptional.get().getLibraryCards();
        for (LibraryCard libraryCard : libraryCards) {
            for (Checkout checkout : libraryCard.getCheckouts()) {
                if (checkout != null && checkout.getDueDate().isBefore(LocalDateTime.now())) {
                    overdueCheckouts.add(new OverdueCheckout(libraryCard.getPatron(), checkout));
                }
            }
        } return overdueCheckouts;
    }
}
