package br.com.alura.screenmatch_jpa.domain;

import br.com.alura.screenmatch_jpa.enums.Categoria;
import java.util.OptionalDouble;

public class Serie {
    private String titulo;
    private Integer totalTemporadas;
    private Double avaliacao;
    private Categoria genero;
    private String atores;
    private String poster;
    private String sinopse;

    public Serie(SerieDTO serieDTO){
        this.titulo = serieDTO.titulo();
        this.totalTemporadas = serieDTO.totalTemporadas();
        this.avaliacao = OptionalDouble.of(Double.parseDouble(serieDTO.avaliacao())).orElse(0);
        this.genero = Categoria.fromString(serieDTO.genero().split(",")[0].trim());
        this.atores = serieDTO.atores();
        this.poster = serieDTO.poster();
        this.sinopse = serieDTO.sinopse();
    }
}
