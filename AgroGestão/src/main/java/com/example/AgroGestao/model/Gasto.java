package com.example.AgroGestao.model;

import jakarta.persistence.*;

@Entity
@Table(name = "gastos") // A tabela no banco pode continuar como 'gastos' (plural) que é o padrão de BD, mas a classe fica no singular
public class Gasto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String description; // Ou 'descricao', certifique-se de alinhar com o HTML (usei descricao no HTML anterior, se preferir mude aqui para descricao)
    private String descricao;
    private Double valor;
    private String dataGasto;

    @ManyToOne
    @JoinColumn(name = "propriedade_id")
    private Propriedade propiedad; // Relacionamento com a classe Propriedade

    @Transient // Caso queira usar um mapeamento direto, mude para o atributo abaixo:
    private Propriedade propriedade;

    // Construtor Padrão (Obrigatório para o Hibernate)
    public Gasto() {}

    // Construtor Completo
    public Gasto(String descricao, Double valor, String dataGasto, Propriedade propriedade) {
        this.descricao = descricao;
        this.valor = valor;
        this.dataGasto = dataGasto;
        this.propriedade = propriedade;
    }

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Double getValor() {
        return valor;
    }

    public void setValor(Double valor) {
        this.valor = valor;
    }

    public String getDataGasto() {
        return dataGasto;
    }

    public void setDataGasto(String dataGasto) {
        this.dataGasto = dataGasto;
    }

    public Propriedade getPropriedade() {
        return propriedade;
    }

    public void setPropriedade(Propriedade propriedade) {
        this.propriedade = propriedade;
    }
}