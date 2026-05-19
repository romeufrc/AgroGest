package com.example.AgroGestao.model;

import jakarta.persistence.*;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;
    private String telefone;
    private String senha;

    // Um usuário pode ter várias propriedades
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL)
    @JsonIgnore // Evita loop infinito na serialização do JSON
    private List<Propriedade> propriedades;

    public Usuario() {
    }

    public Usuario(Long id, String nome, String telefone, String senha) {
        this.id = id;
        this.nome = nome;
        this.telefone = telefone;
        this.senha = senha;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }

    public List<Propriedade> getPropriedades() { return propriedades; }
    public void setPropriedades(List<Propriedade> propriedades) { this.propriedades = propriedades; }
}