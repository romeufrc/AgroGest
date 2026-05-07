package com.example.AgroGestao.controller;

import com.example.AgroGestao.model.Atividade;
import com.example.AgroGestao.repository.AtividadeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/atividades")
public class AtividadeController {

    @Autowired
    private AtividadeRepository repository;

    // cadastrar atividade
    @PostMapping
    public Atividade cadastrar(@RequestBody Atividade atividade) {
        return repository.save(atividade);
    }

    // listar atividades
    @GetMapping
    public List<Atividade> listar() {
        return repository.findAll();
    }
}