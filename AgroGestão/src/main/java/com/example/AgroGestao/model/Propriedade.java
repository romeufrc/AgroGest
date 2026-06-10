package com.example.AgroGestao.model;

import jakarta.persistence.*;

@Entity
@Table(name = "propriedade")
public class Propriedade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;
    private String localizacao;
    private Double tamanho;
    private Double limiteGasto;
    private Integer limiteEstoqueBaixo;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    // CONSTRUTOR VAZIO (OBRIGATÓRIO PARA O SPRING/THYMELEAF NÃO DAR ERRO 500)
    public Propriedade() {}

    // Construtor Completo
    public Propriedade(Long id, String nome, String localizacao, Double tamanho, Double limiteGasto, Integer limiteEstoqueBaixo, Usuario usuario) {
        this.id = id;
        this.nome = nome;
        this.localizacao = localizacao;
        this.tamanho = tamanho;
        this.limiteGasto = limiteGasto;
        this.limiteEstoqueBaixo = limiteEstoqueBaixo;
        this.usuario = usuario;
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getLocalizacao() { return localizacao; }
    public void setLocalizacao(String localizacao) { this.localizacao = localizacao; }
    public Double getTamanho() { return tamanho; }
    public void setTamanho(Double tamanho) { this.tamanho = tamanho; }
    public Double getLimiteGasto() { return limiteGasto; }
    public void setLimiteGasto(Double limiteGasto) { this.limiteGasto = limiteGasto; }
    public Integer getLimiteEstoqueBaixo() { return limiteEstoqueBaixo; }
    public void setLimiteEstoqueBaixo(Integer limiteEstoqueBaixo) { this.limiteEstoqueBaixo = limiteEstoqueBaixo; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
}