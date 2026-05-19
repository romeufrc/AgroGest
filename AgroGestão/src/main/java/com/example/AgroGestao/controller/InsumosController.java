package com.example.AgroGestao.controller;

import com.example.AgroGestao.model.Insumos;
import com.example.AgroGestao.repository.InsumosRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/insumos")
public class InsumosController {

    @Autowired
    private InsumosRepository repository;

    // Cadastrar insumo (RF04)
    @PostMapping
    public Insumos cadastrar(@RequestBody Insumos insumo) {
        return repository.save(insumo);
    }

    // Listar insumos disponíveis
    @GetMapping
    public List<Insumos> listar() {
        return repository.findAll();
    }

    // Remover insumo do estoque (RF11)
    @DeleteMapping("/{id}")
    public void excluir(@PathVariable Long id) {
        repository.deleteById(id);
    }
}
