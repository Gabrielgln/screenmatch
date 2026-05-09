package br.com.alura.screenmatch.service;

import br.com.alura.screenmatch.domain.Episodio;
import br.com.alura.screenmatch.domain.Serie;
import br.com.alura.screenmatch.dto.EpisodioDTO;
import br.com.alura.screenmatch.dto.SerieDTO;
import br.com.alura.screenmatch.enums.Categoria;
import br.com.alura.screenmatch.repository.SerieRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SerieService {
    @Autowired
    private SerieRepository repository;

    public List<SerieDTO> obterTodasAsSeries(){
        return this.converteDados(repository.findAll());
    }

    public List<SerieDTO> obterTop5Series() {
        return this.converteDados(repository.findTop5ByOrderByAvaliacaoDesc());
    }

    public List<SerieDTO> obterLancamentos() {
        return this.converteDados(repository.lancamentosMaisRecentes());
    }

    public SerieDTO obterPorId(Long id) {
        return this.converteDados(repository.findById(id).orElse(null));
    }

    public List<EpisodioDTO> obterTodasTemporadas(Long id) {
        Serie serie = repository.findById(id).orElse(null);

        if (serie == null)
            return null;

        return serie.getEpisodios().stream()
                .map(this::converteDados)
                .toList();
    }

    public List<EpisodioDTO> obterTemporadasPorNumero(Long id, Long numero) {
        return repository.obterEpisodiosPorTemporada(id, numero).stream()
                .map(this::converteDados)
                .toList();
    }

    public List<SerieDTO> obterSeriesPorCategoria(String nomeGenero) {
        Categoria categoria = Categoria.fromPortugues(nomeGenero);
        return this.converteDados(repository.findByGenero(categoria));
    }

    private EpisodioDTO converteDados(Episodio episodio){
        if(episodio == null)
            return null;

        return new EpisodioDTO(
                episodio.getTemporada(),
                episodio.getTitulo(),
                episodio.getNumero()
        );
    }

    private SerieDTO converteDados(Serie serie){
        if(serie == null)
            return null;

        return new SerieDTO(
                serie.getId(),
                serie.getTitulo(),
                serie.getTotalTemporadas(),
                serie.getAvaliacao(),
                serie.getGenero(),
                serie.getAtores(),
                serie.getPoster(),
                serie.getSinopse()
        );
    }

    private List<SerieDTO> converteDados(List<Serie> series){
        return series.stream()
                .map(s -> new SerieDTO(
                                s.getId(),
                                s.getTitulo(),
                                s.getTotalTemporadas(),
                                s.getAvaliacao(),
                                s.getGenero(),
                                s.getAtores(),
                                s.getPoster(),
                                s.getSinopse()
                        )
                )
                .toList();
    }
}
