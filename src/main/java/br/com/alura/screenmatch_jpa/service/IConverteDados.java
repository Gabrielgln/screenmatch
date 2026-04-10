package br.com.alura.screenmatch_jpa.service;

public interface IConverteDados {
    <T> T obterDados(String json, Class<T> classe);
}
