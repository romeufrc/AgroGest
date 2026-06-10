package com.example.AgroGestao.model;

import jakarta.persistence.*;

@Entity
@Table(name = "safra")
public class Safra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;
    private String cultura;
    private String status; // ATIVO, EM MATURAÇÃO, FINALIZADO

    // NOVOS CAMPOS FINANCEIROS DO FORMULÁRIO:
    private Integer producaoEstimadaSacas; // Quantidade de sacas que o produtor espera colher
    private Double precoSacaEsperado;      // Preço de venda estimado de cada saca no mercado

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "propriedade_id")
    private Propriedade propriedade;

    public Safra() {}

    public Safra(Long id, String nome, String cultura, String status, Integer producaoEstimadaSacas, Double precoSacaEsperado, Propriedade propriedade) {
        this.id = id;
        this.nome = nome;
        this.cultura = cultura;
        this.status = status;
        this.producaoEstimadaSacas = producaoEstimadaSacas;
        this.precoSacaEsperado = precoSacaEsperado;
        this.propriedade = propriedade;
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCultura() { return cultura; }
    public void setCultura(String cultura) { this.cultura = cultura; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getProducaoEstimadaSacas() { return producaoEstimadaSacas; }
    public void setProducaoEstimadaSacas(Integer producaoEstimadaSacas) { this.producaoEstimadaSacas = producaoEstimadaSacas; }

    public Double getPrecoSacaEsperado() { return precoSacaEsperado; }
    public void setPrecoSacaEsperado(Double precoSacaEsperado) { this.precoSacaEsperado = precoSacaEsperado; }

    public Propriedade getPropriedade() { return propriedade; }
    public void setPropriedade(Propriedade propriedade) { this.propriedade = propriedade; }
}