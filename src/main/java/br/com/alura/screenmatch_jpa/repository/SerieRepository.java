package br.com.alura.screenmatch_jpa.repository;

import br.com.alura.screenmatch_jpa.domain.Serie;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SerieRepository extends JpaRepository<Serie, Long> {
}
