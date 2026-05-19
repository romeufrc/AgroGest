package com.example.AgroGestao.model;

import jakarta.persistence.*;

@Entity
public class Propriedade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;
    private String localizacao;
    private Double tamanho;

    // Muitas propriedades pertencem a um Usuário
    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    public Propriedade() {
    }

    public Propriedade(Long id, String nome, String localizacao, Double tamanho, Usuario usuario) {
        this.id = id;
        this.nome = nome;
        this.localizacao = localizacao;
        this.tamanho = tamanho;
        this.usuario = usuario;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getLocalizacao() { return localizacao; }
    public void setLocalizacao(String localizacao) { this.localizacao = localizacao; }

    public Double getTamanho() { return tamanho; }
    public void setTamanho(Double tamanho) { this.tamanho = tamanho; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
}