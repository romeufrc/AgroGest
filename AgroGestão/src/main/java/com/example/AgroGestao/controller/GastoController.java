package com.example.AgroGestao.controller;

import com.example.AgroGestao.model.Gasto;
import com.example.AgroGestao.repository.GastoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/gastos")
public class GastoController {

    @Autowired
    private GastoRepository repository;

    // Cadastrar novo gasto (RF03)
    @PostMapping
    public Gasto cadastrar(@RequestBody Gasto gasto) {
        return repository.save(gasto);
    }

    // Listar todos os gastos
    @GetMapping
    public List<Gasto> listar() {
        return repository.findAll();
    }

    // Excluir um gasto (Caso queira deletar das tabelas/cards)
    @DeleteMapping("/{id}")
    public void excluir(@PathVariable Long id) {
        repository.deleteById(id);
    }
}