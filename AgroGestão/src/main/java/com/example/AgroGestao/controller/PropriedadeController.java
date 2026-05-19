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

    @PostMapping
    public Propriedade cadastrar(@RequestBody Propriedade propriedade) {
        return repository.save(propriedade);
    }

    @GetMapping
    public List<Propriedade> listar() {
        return repository.findAll();
    }

    // Editar Propriedade
    @PutMapping("/{id}")
    public Propriedade editar(@PathVariable Long id, @RequestBody Propriedade novaPropriedade) {
        return repository.findById(id)
                .map(propriedade -> {
                    propriedade.setNome(novaPropriedade.getNome());
                    propriedade.setLocalizacao(novaPropriedade.getLocalizacao());
                    propriedade.setTamanho(novaPropriedade.getTamanho());
                    propriedade.setUsuario(novaPropriedade.getUsuario());
                    return repository.save(propriedade);
                }).orElseGet(() -> {
                    novaPropriedade.setId(id);
                    return repository.save(novaPropriedade);
                });
    }

    // Excluir Propriedade
    @DeleteMapping("/{id}")
    public void excluir(@PathVariable Long id) {
        repository.deleteById(id);
    }
}