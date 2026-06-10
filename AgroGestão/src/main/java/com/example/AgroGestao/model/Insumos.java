package com.example.AgroGestao.model;

import jakarta.persistence.*;

@Entity
@Table(name = "insumos")
public class Insumos {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;
    private String tipo;
    private Integer quantidade;

    // 🟢 GARANTIA: Mudei para EAGER pro Hibernate carregar o galpão da fazenda na hora!
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "propriedade_id")
    private Propriedade propriedade;

    public Insumos() {}

    public Insumos(Long id, String nome, String tipo, Integer quantidade, Propriedade propriedade) {
        this.id = id;
        this.nome = nome;
        this.tipo = tipo;
        this.quantidade = quantidade;
        this.propriedade = propriedade;
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public Integer getQuantidade() { return quantidade; }
    public void setQuantity(Integer quantidade) { this.quantidade = quantidade; }
    // Deixei o setter clássico mapeado caso seu form use setQuantidade
    public void setQuantidade(Integer quantidade) { this.quantidade = quantidade; }

    public Propriedade getPropriedade() { return propriedade; }
    public void setPropriedade(Propriedade propriedade) { this.propriedade = propriedade; }
}