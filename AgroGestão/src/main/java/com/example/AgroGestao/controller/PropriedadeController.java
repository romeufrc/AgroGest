package com.example.AgroGestao.controller;

import com.example.AgroGestao.model.Propriedade;
import com.example.AgroGestao.repository.PropriedadeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/propriedades")
public class PropriedadeController {

    @Autowired
    private PropriedadeRepository repository;

    // cadastrar propriedade
    @PostMapping
    public Propriedade cadastrar(@RequestBody Propriedade propriedade) {
        return repository.save(propriedade);
    }

    // listar propriedades
    @GetMapping
    public List<Propriedade> listar() {
        return repository.findAll();
    }
}