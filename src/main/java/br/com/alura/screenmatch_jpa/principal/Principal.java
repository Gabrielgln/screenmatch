package br.com.alura.screenmatch_jpa.principal;

import br.com.alura.screenmatch_jpa.domain.Episodio;
import br.com.alura.screenmatch_jpa.domain.Serie;
import br.com.alura.screenmatch_jpa.domain.SerieDTO;
import br.com.alura.screenmatch_jpa.domain.TemporadaDTO;
import br.com.alura.screenmatch_jpa.repository.SerieRepository;
import br.com.alura.screenmatch_jpa.service.ConsumoApi;
import br.com.alura.screenmatch_jpa.service.ConverteDados;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

public class Principal {
    private Scanner leitura = new Scanner(System.in);
    private ConsumoApi consumo = new ConsumoApi();
    private ConverteDados conversor = new ConverteDados(new ObjectMapper());
    private static final String ENDERECO = "https://www.omdbapi.com/?t=";
    private static final String API_KEY = "&apikey=402e2999";
    private SerieRepository serieRepository;
    private List<Serie> series = new ArrayList<>();

    public Principal(SerieRepository serieRepository){
        this.serieRepository = serieRepository;
    }

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
        serieRepository.save(new Serie(serieDTO));
        System.out.println(serieDTO);
    }

    private SerieDTO getDadosSerie(){
        System.out.println("Digite o nome da série para busca:");
        String nomeSerie = leitura.nextLine();
        String json = consumo.obterDados(ENDERECO + nomeSerie.replace(" ", "+") + API_KEY);
        return conversor.obterDados(json, SerieDTO.class);
    }

    private void buscarEpisodioPorSerie(){
        this.listarSeriesBuscadas();
        System.out.println("Escolha uma serie pelo nome: ");
        String nomeSerie = leitura.nextLine();

        Optional<Serie> serie = series.stream()
                .filter(s -> s.getTitulo().toLowerCase().contains(nomeSerie.toLowerCase()))
                .findFirst();

        if(serie.isPresent()){
            Serie serieEncontrada = serie.get();
            List<TemporadaDTO> temporadas = new ArrayList<>();

            for (int i = 1; i <= serieEncontrada.getTotalTemporadas(); i++) {
                String json = consumo.obterDados(ENDERECO + serieEncontrada.getTitulo().replace(" ", "+") + "&season=" + i + API_KEY);
                temporadas.add(conversor.obterDados(json, TemporadaDTO.class));
            }
            temporadas.forEach(System.out::println);

            List<Episodio> episodios = temporadas.stream()
                    .flatMap(t -> t.episodios().stream()
                            .map(e -> new Episodio(t.numero(), e))
                    )
                    .toList();

            serieEncontrada.setEpisodios(episodios);
            serieRepository.save(serieEncontrada);
        } else {
            System.out.println("Série não encontrada");
        }
    }

    private void listarSeriesBuscadas() {
        series = serieRepository.findAll();
        series.stream()
                .sorted(Comparator.comparing(Serie::getGenero))
                .forEach(System.out::println);
    }
}
