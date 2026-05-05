package br.com.alura.screenmatch.principal;

import br.com.alura.screenmatch.domain.Episodio;
import br.com.alura.screenmatch.domain.Serie;
import br.com.alura.screenmatch.domain.SerieDTO;
import br.com.alura.screenmatch.domain.TemporadaDTO;
import br.com.alura.screenmatch.enums.Categoria;
import br.com.alura.screenmatch.repository.SerieRepository;
import br.com.alura.screenmatch.service.ConsumoApi;
import br.com.alura.screenmatch.service.ConverteDados;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;

public class Principal {
    private Scanner leitura = new Scanner(System.in);
    private ConsumoApi consumo = new ConsumoApi();
    private ConverteDados conversor = new ConverteDados(new ObjectMapper());
    private static final String ENDERECO = "https://www.omdbapi.com/?t=";
    private static final String API_KEY = "&apikey=402e2999";
    private SerieRepository serieRepository;
    private List<Serie> series = new ArrayList<>();
    private Optional<Serie> serieBusca;

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
                    4 - Buscar série por título
                    5 - Buscar séries por ator
                    6 - Top 5 séries
                    7 - Buscar séries por categoria
                    8 - Filtrar séries
                    9 - Buscar episódios por trecho
                    10 - Top 5 episódios por série
                    11 - Buscar episódios a partir de uma data
                    
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
                case 4:
                    this.buscarSeriePorTitulo();
                    break;
                case 5:
                    this.buscarSeriesPorAtor();
                    break;
                case 6:
                    this.buscarTop5Series();
                    break;
                case 7:
                    this.buscarSeriesPorCategoria();
                    break;
                case 8:
                    this.filtrarSeriesPorTemporadaEAvaliacao();
                    break;
                case 9:
                    this.buscarEpisodioPorTrecho();
                    break;
                case 10:
                    this.topEpisodiosPorSerie();
                    break;
                case 11:
                    this.buscarEpisodiosDepoisDeUmaData();
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

        Optional<Serie> serie = serieRepository.findByTituloContainingIgnoreCase(nomeSerie);

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

    private void buscarSeriePorTitulo() {
        System.out.println("Escolha uma serie pelo nome: ");
        String nomeSerie = leitura.nextLine();
        serieBusca = serieRepository.findByTituloContainingIgnoreCase(nomeSerie);

        if(serieBusca.isPresent())
            System.out.println("Dados da série: " + serieBusca.get());
        else
            System.out.println("Série não encontrada.");
    }

    private void buscarSeriesPorAtor() {
        System.out.println("Qual o nome para busca?");
        String ator = leitura.nextLine();
        System.out.println("Avaliações a partir de que valor");
        Double avaliacao = leitura.nextDouble();
        leitura.nextLine();
        List<Serie> seriesEncontradas = serieRepository.findByAtoresContainingIgnoreCaseAndAvaliacaoGreaterThanEqual(ator, avaliacao);
        System.out.println("Séries em que " + ator + " trabalhou");
        seriesEncontradas.forEach(System.out::println);
    }

    private void buscarTop5Series() {
        List<Serie> seriesTop5 = serieRepository.findTop5ByOrderByAvaliacaoDesc();
        seriesTop5.forEach(System.out::println);
    }

    private void buscarSeriesPorCategoria() {
        System.out.println("Deseja buscar séries de que categoria/gênero? ");
        String genero = leitura.nextLine();
        Categoria categoria = Categoria.fromPortugues(genero);
        List<Serie> seriesPorCategoria = serieRepository.findByGenero(categoria);
        System.out.println("Séries da categoria: " + genero);
        seriesPorCategoria.forEach(System.out::println);
    }

    private void filtrarSeriesPorTemporadaEAvaliacao() {
        System.out.println("Filtrar séries até quantas temporadas? ");
        int totalTemporadas = leitura.nextInt();
        leitura.nextLine();
        System.out.println("Com avaliação a partir de que valor? ");
        double avaliacao = leitura.nextDouble();
        leitura.nextLine();
        List<Serie> filtroSeries = serieRepository.seriesPorTemporadaEAvaliacao(totalTemporadas, avaliacao);
        System.out.println("Séries filtradas:");
        filtroSeries.forEach(System.out::println);
    }

    private void buscarEpisodioPorTrecho() {
        System.out.println("Qual o nome do episódio para busca?");
        String trecho = leitura.nextLine();
        List<Episodio> episodiosEncontrados = serieRepository.episodiosPorTrecho(trecho);
        episodiosEncontrados.forEach(e ->
                System.out.printf("Série: %s Temporada %s - Episódio %s - %s\n",
                        e.getSerie().getTitulo(), e.getTemporada(), e.getNumero(), e.getTitulo()));
    }

    private void topEpisodiosPorSerie() {
        this.buscarSeriePorTitulo();

        if(serieBusca.isPresent()){
            Serie serie = serieBusca.get();
            List<Episodio> topEpisodios = this.serieRepository.topEpisodiosPorSerie(serie);
            topEpisodios.forEach(e ->
                    System.out.printf("Série: %s, temporada: %s, episódio: %s - %s, avaliação: %s\n",
                            e.getSerie().getTitulo(), e.getTemporada(), e.getNumero(), e.getTitulo(), e.getAvaliacao()));
        }
    }

    private void buscarEpisodiosDepoisDeUmaData() {
        this.buscarSeriePorTitulo();

        if(serieBusca.isPresent()){
            System.out.println("Digite o ano limite de lançamento");
            int anoLancamento = leitura.nextInt();
            leitura.nextLine();

            List<Episodio> episodiosAno = this.serieRepository.episodiosPorSerieEAno(serieBusca.get(), anoLancamento);
            episodiosAno.forEach(System.out::println);
        }
    }
}
