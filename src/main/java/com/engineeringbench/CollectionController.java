package com.engineeringbench;

import com.engineeringbench.model.CollectionRequest;
import com.engineeringbench.service.QdrantCollectionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/collections")
public class CollectionController {

    private final QdrantCollectionService service;

    public CollectionController(
            QdrantCollectionService service) {

        this.service = service;
    }

    @PostMapping
    public void create(
            @RequestBody
            CollectionRequest request)
            throws Exception {

        service.createCollection(
                request.collectionName());
    }

    @DeleteMapping("/{name}")
    public void delete(
            @PathVariable String name)
            throws Exception {

        service.deleteCollection(name);
    }

    @GetMapping
    public List<String> list()
            throws Exception {

        return service.listCollections();
    }
}
