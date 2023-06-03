package primenumber;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class WorkerPrimo extends Thread {

    private static ArrayList<File> tarefas = new ArrayList<>(); //array contento os arquivos txt a serem lidos pelas threads
    private static Object chaveTarefas = new Object(); //lock para acessar a lista de tarefas
    private static Object chaveRecurso = new Object(); //lock para que as threads aguardem por novas tarefas 
    private static boolean existeTrabalho = true; //variável de controle para indicar que há trabalho a ser feito

    //variáveis para guardar o maior númro primo encontrado por uma thread e o diretório em que ela se encontra
    private long maiorPrimo = 2; 
    private String arquivoMaiorPrimo = null;

    //construtor recebendo o array de tarefas a serem feitas
    public WorkerPrimo(ArrayList tarefas) {
        this.tarefas = tarefas;
    }

    public long getMaiorPrimo() {
        return maiorPrimo;
    }

    public String getArquivoMaiorPrimo() {
        return arquivoMaiorPrimo;
    }

    @Override
    public void run() {
        File arquivoTexto = null;

        //while executa enquanto houver trabalho ou arquivos a serem lidos no array
        while (existeTrabalho || !tarefas.isEmpty()) {
            arquivoTexto = null;
            
            //trecho sincronizado que determina que apenas uma thread pode pegar um arquivo do array por vez
            synchronized (chaveTarefas) {
                if (!tarefas.isEmpty()) {
                    //"peguei" o valor da primeira posicao
                    arquivoTexto = tarefas.remove(0);

                }
            }

            //se não for nulo, o arquivo deve ser lido para encontrar o maior número primo
            if (arquivoTexto != null) {
                //acessar o conteúdo do arquivo
                try {
                    //variáveis para permitir a leitura do arquivo de texto
                    FileReader marcaLeitura = new FileReader(arquivoTexto);
                    BufferedReader bufLeitura = new BufferedReader(marcaLeitura);

                    //leitura das linhas do arquivo
                    String linha = null;
                    linha = bufLeitura.readLine();
                    while (linha != null) {
                        //separar a linha em palavras sempere que houver espaços em branco, "-" e ","
                        String palavras[] = linha.split("[-,;\\s]"); 
                        
                        /*
                        Ainda é presiso "limpar" cada palavra para não ter nenhum caractere junto a ela, 
                        pois se houver um número junto com outro caractere haverá erro na conversão de String para long 
                        Por exemplo: (1234), o número 1234 deve ser separado dos parêntesis
                        */
                        for (String novaPalavra : palavras) {
                            /*
                            Para cada palavra são trocados os caracteres especificados por String nula
                            Só depois é chamado o método encontrarNumero que vai tentar converser a palavra encontrada para um número
                            */
                            double numero = enontrarNumero(novaPalavra.replaceAll("[:;+()]", "")); 
                            long parteInteira = (long) Math.floor(numero);
                            //System.out.println(numero + " - " + parteInteira);
                            
                            /*
                            se o número encontrado não for maior que o primo atual ele já não se candidata a ser o maior primo
                            só depois é chamado o método para verificar se o número é um primo
                            */
                            if (parteInteira > maiorPrimo && isPrimo(parteInteira)) {
                                maiorPrimo = parteInteira;
                                //arquivoMaiorPrimo = arquivoTexto.getAbsolutePath();
                            }

                        }
                        linha = bufLeitura.readLine();
                    }
                } catch (FileNotFoundException ex) {
                    System.err.println("Arquivo não existe no dir.");
                } catch (IOException ex) {
                    System.err.println("Seu arquivo esta corrompido");
                }
            }

            /*
            Não há mais arquivos textos a serem lido, mas pode existir mais trabalho a ser adicionado no futuro, 
            Então deve ocorrer a modificação do status da thread para "aguandado" novas tarefas
            */
            if (arquivoTexto == null && existeTrabalho) {
                aguarde();
            }
        }
    }

    //verificar se a palavra encontrada em uma linha é um número e retornar esse número
    private double enontrarNumero(String str) {
        double numero;
        try {
            numero = Double.parseDouble(str); //conversão de String para long foi um sucesso, encontrou um número
        } catch (NumberFormatException e) {
            numero = 0; //exceção na conversão de String para long, tentou converter uma palavra para número 
        }
        return numero;
    }

    //verificar se um número é primo com base em sua raiz
    private boolean isPrimo(long numero) {
        for (long i = 2; i <= numero / 2; i++) {
            if ((numero % i) == 0) {
                return false; //assim que encontra um divisor, sabe-se que o número não é primo.
            }
        }
        return true; //não encontrou divisores, o número é primo
    }

    //sinalizar que as threads devem aguardar a adição de mais trabalho no futuro, assim ela não "morre"  
    public void aguarde() {
        synchronized (chaveRecurso) {
            try {
                //não temos mais trabalho vamos aguardar novos dados...
                chaveRecurso.wait();
            } catch (InterruptedException ex) {
                System.err.println("existem threads aguardando recurso");
            }
        }
    }

    //sinalizar para as threads que elas devem acordar, pode ser que haja trabalho a fazer
    public static void acordaThreads() {
        synchronized (chaveRecurso) {
            chaveRecurso.notifyAll();
        }
    }

    //sinalizar para as threads que não existe mais trabalho a ser executado
    public static void termina() {
        existeTrabalho = false;
        acordaThreads();
    }
}
