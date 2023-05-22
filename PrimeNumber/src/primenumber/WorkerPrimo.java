package primenumber;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class WorkerPrimo extends Thread {

    private static ArrayList<File> tarefas = new ArrayList<>();
    private static Object chaveTarefas = new Object();
    private static Object chaveRecurso = new Object();
    private static boolean existeTrabalho = true;

    private long maiorPrimo = 2;

    public WorkerPrimo(ArrayList tarefas) {
        this.tarefas = tarefas;
    }

    public long getMaiorPrimo() {
        return maiorPrimo;
    }

    @Override
    public void run() {
        File arquivoTexto = null;

        while (existeTrabalho || !tarefas.isEmpty()) {
            arquivoTexto = null;
            synchronized (chaveTarefas) {
                if (!tarefas.isEmpty()) {
                    //"peguei" o valor da primeira posicao
                    arquivoTexto = tarefas.remove(0);

                }
            }

            if (arquivoTexto != null) {
                //acessar o conteúdo do arquivo
                try {
                    FileReader marcaLeitura = new FileReader(arquivoTexto);
                    BufferedReader bufLeitura = new BufferedReader(marcaLeitura);

                    //leitura das linhas do arquivo
                    String linha = null;
                    long numero;

                    linha = bufLeitura.readLine();
                    while (linha != null) {
                        String[] palavras = linha.split("[,:;?!'(){}\\s]"); // Separar palavras com base em espaços em branco e em alguns caracteres especiais

                        for (String palavra : palavras) {
                            // Realizar alguma ação com a palavra separada
                            //System.out.println(palavra);
                            //System.out.println(palavra.replaceAll("[,:;!?{}'()]-", ""));

                            numero = enontrarNumero(palavra);
                            if (numero > maiorPrimo && isPrimo(numero)) {
                                maiorPrimo = numero;
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

            if (arquivoTexto == null && existeTrabalho) {
                //deve ocorrer a modificação do status da thread para "aguandado" novas tarefas
                aguarde();
            }
        }
    }

    //encontrar se a palavra encontrada em uma linha é um número
    private long enontrarNumero(String str) {
        long numero;
        try {
            numero = Long.parseLong(str);
        } catch (NumberFormatException e) {
            numero = 0;
        }
        return numero;
    }

    //achar um jeito melhor de encontrar o primo, não é pra usar esse
    private boolean isPrimo(long numero) {
        for (long i = 2; i <= numero / 2; i++) {

            if ((numero % i) == 0) {
                return false; //Assim que encontra um divisor, sabe que o número não é primo.
            }
        }
        return true; //não encontrou divisores, o número é primo

        /*int quantDivExatas = 0;

        for (int div = 2; div <= numero - 1; div++) {
            if (numero % div == 0) {
                quantDivExatas++;
            }
        }

        return quantDivExatas == 0;
         */
    }

    public void aguarde() {
        synchronized (chaveRecurso) {
            try {
                //não temos mais trabalho vamos aguardar novos dados...
                chaveRecurso.wait();
            } catch (InterruptedException ex) {
                System.err.println("existe threads aguardando recurso");
            }
        }
    }

    public static void acordaThreads() {
        synchronized (chaveRecurso) {
            chaveRecurso.notifyAll();
        }
    }

    public static void termina() {
        existeTrabalho = false;
        acordaThreads();
    }

}

