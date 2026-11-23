package centralize.individual_research.controller;

import centralize.individual_research.dto.TerminologyRequest;
import centralize.individual_research.dto.TerminologyResponse;
import centralize.individual_research.service.TerminologyService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/public/terminology-store")
public class TerminologyController {

    private final TerminologyService service;

    public TerminologyController(TerminologyService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TerminologyResponse create(@Valid @RequestBody TerminologyRequest request) {
        return service.create(request);
    }

    @GetMapping
    public List<TerminologyResponse> getAll() {
        return service.getAll();
    }

    @GetMapping("/search")
    public List<TerminologyResponse> search(@RequestParam("keyword") String keyword) {
        return service.searchByKeyword(keyword);
    }

    @GetMapping("/{id}")
    public TerminologyResponse getById(@PathVariable("id") String id) {
        return service.getById(id);
    }

    @PutMapping("/{id}")
    public TerminologyResponse update(@PathVariable("id") String id, @Valid @RequestBody TerminologyRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("id") String id) {
        service.delete(id);
    }
}
