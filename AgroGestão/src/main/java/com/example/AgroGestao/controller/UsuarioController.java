package com.example.AgroGestao.controller;

import com.example.AgroGestao.model.Usuario;
import com.example.AgroGestao.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioRepository repository;

    // cadastrar usuário
    @PostMapping
    public Usuario cadastrar(@RequestBody Usuario usuario) {
        return repository.save(usuario);
    }

    // listar usuários
    @GetMapping
    public List<Usuario> listar() {
        return repository.findAll();
    }
}