package example.cashcard;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import java.security.Principal;
import java.net.URI;
import java.util.*;

@RestController
@RequestMapping("/cashcards")
class CashCardController {
    private final CashCardRepository cashCardRepository;

    private CashCardController(CashCardRepository cashCardRepository) {
        this.cashCardRepository = cashCardRepository;
    }

    @GetMapping("/{requestedId}")
    private ResponseEntity<CashCard> findById(@PathVariable Long requestedId, Principal principal) {
        CashCard cashCard = findCashCard(requestedId, principal);
        if (cashCard != null) {
            return ResponseEntity.ok(cashCard);
        } else {
            return ResponseEntity.notFound().build();
        }
    }



    @GetMapping
    private ResponseEntity<List<CashCard>> findAll(Pageable pageable, Principal principal) {
        Page<CashCard> page = cashCardRepository.findByOwner(principal.getName(),
            PageRequest.of(pageable.getPageNumber(),
            pageable.getPageSize(),
           ( pageable.getSort().isSorted()? pageable.getSort() :Sort.by(Sort.Order.desc("amount")))
        ));
        return ResponseEntity.ok(page.getContent());
    }
    @PostMapping
    private ResponseEntity<Void> createCashCard(@RequestBody CashCard newCashCardRequest, UriComponentsBuilder ucb, Principal principal) {
        CashCard cashCardWithOwner = new CashCard(null, newCashCardRequest.amount(), principal.getName());
        CashCard savedCashCard = cashCardRepository.save(cashCardWithOwner);
        URI locationOfNewCashCard = ucb
                .path("cashcards/{id}")
                .buildAndExpand(savedCashCard.id())
                .toUri();
        return ResponseEntity.created(locationOfNewCashCard).build();
    }

    @PutMapping("/{requestedId}")
    private ResponseEntity<Void> putCashCard(@PathVariable Long requestedId, @RequestBody CashCard cashCardUpdate, Principal principal) {
        CashCard cashCard = findCashCard(requestedId, principal);
        if(cashCard != null) {
            CashCard updatedCashCard = new CashCard(cashCard.id(), cashCardUpdate.amount(), principal.getName());        
            cashCardRepository.save(updatedCashCard);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();

    }

    private CashCard findCashCard(Long requestId, Principal principal) {
        return cashCardRepository.findByIdAndOwner(requestId, principal.getName());
    }

    @DeleteMapping("/{id}")
    private ResponseEntity<Void> deleteCashCard(@PathVariable Long id, Principal principal) {
        


        if(!cashCardRepository.existsByIdAndOwner(id, principal.getName())){
          
            return ResponseEntity.notFound().build();
        }
        cashCardRepository.deleteById(id);
        return ResponseEntity.noContent().build();
       
    }

}