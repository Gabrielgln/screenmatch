package br.com.alura.screenmatch_jpa.principal;

import br.com.alura.screenmatch_jpa.domain.SerieDTO;
import br.com.alura.screenmatch_jpa.domain.TemporadaDTO;
import br.com.alura.screenmatch_jpa.service.ConsumoApi;
import br.com.alura.screenmatch_jpa.service.ConverteDados;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Principal {
    private Scanner leitura = new Scanner(System.in);
    private ConsumoApi consumo = new ConsumoApi();
    private ConverteDados conversor = new ConverteDados(new ObjectMapper());
    private static final String ENDERECO = "https://www.omdbapi.com/?t=";
    private static final String API_KEY = "&apikey=402e2999";
    private List<SerieDTO> series = new ArrayList<>();

    public void exibeMenu(){
        int opcao = -1;
        while (opcao != 0){
            String menu = """
                    1 - Buscar séries
                    2 - Buscar episódios
                    3 - Listar séries buscadas
                    
                    0 - Sair
                    """;

            System.out.println(menu);
            opcao = leitura.nextInt();
            leitura.nextLine();

            switch (opcao){
                case 1:
                    this.buscarSerieWeb();
                    break;
                case 2:
                    this.buscarEpisodioPorSerie();
                    break;
                case 3:
                    this.listarSeriesBuscadas();
                    break;
                case 0:
                    System.out.println("Saindo...");
                    break;
                default:
                    System.out.println("Opção inválida");
            }
        }
    }

    private void buscarSerieWeb(){
        SerieDTO serieDTO = this.getDadosSerie();
        series.add(serieDTO);
        System.out.println(serieDTO);
    }

    private SerieDTO getDadosSerie(){
        System.out.println("Digite o nome da série para busca:");
        String nomeSerie = leitura.nextLine();
        String json = consumo.obterDados(ENDERECO + nomeSerie.replace(" ", "+") + API_KEY);
        return conversor.obterDados(json, SerieDTO.class);
    }

    private void buscarEpisodioPorSerie(){
        SerieDTO serieDTO = this.getDadosSerie();
        List<TemporadaDTO> temporadas = new ArrayList<>();

        for (int i = 1; i <= serieDTO.totalTemporadas(); i++) {
            String json = consumo.obterDados(ENDERECO + serieDTO.titulo().replace(" ", "+") + "&season=" + i + API_KEY);
            temporadas.add(conversor.obterDados(json, TemporadaDTO.class));
        }

        temporadas.forEach(System.out::println);
    }

    private void listarSeriesBuscadas() {
        series.forEach(System.out::println);
    }
}
