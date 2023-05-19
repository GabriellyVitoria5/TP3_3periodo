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

    private StringBuilder palavraSemCharEspecial = new StringBuilder();
    private String[] palavras;
    private int quantPrimos;
    private long maiorPrimo = 2;

    public WorkerPrimo(ArrayList tarefas) {
        this.tarefas = tarefas;
        this.quantPrimos = 0;
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
                        palavras = linha.split("[,:;?!'(){}\\s]"); // Separar palavras com base em espaços em branco e em alguns caracteres especiais

                        for (String palavra : palavras) {
                            // Realizar alguma ação com a palavra separada
                            //System.out.println(palavra);
                            //System.out.println(palavra.replaceAll("[,.:;!?{}'()]", ""));

                            //não sei se usar StringBuilder é melhor
                            //palavraSemCharEspecial.append(palavra.replaceAll("[,:;!?{}'()]", ""));
                            //System.out.println(palavraSemCharEspecial.toString());
                            //palavraSemCharEspecial.setLength(0);
                            
                            
                            numero = enontrarNumero(palavra);
                            if(numero > maiorPrimo && isPrimo(numero)){
                                maiorPrimo = numero;
                            }
                            
                        }
                        linha = bufLeitura.readLine();
                    }


                    
                    /*
                    tava dando erro de NullPointerException na linha 85 
                    acho que é porque todo final de arquivo e linha é nula
                    enfim, aí só troquei o do while pelo while aí de cima
                    pode deixar assim mesmo?
                    */
                    /*do {
                        linha = bufLeitura.readLine();
                        //System.out.println(linha);

                        //tem erro aqui de NullPointerException
                        palavras = linha.split("[,:;?!'(){}\\s]"); // Separar palavras com base em espaços em branco e em alguns caracteres especiais

                        for (String palavra : palavras) {
                            // Realizar alguma ação com a palavra separada
                            //System.out.println(palavra);
                            //System.out.println(palavra.replaceAll("[,.:;!?{}'()]", ""));

                            //não sei se usar StringBuilder é melhor
                            //palavraSemCharEspecial.append(palavra.replaceAll("[,.:;!?{}'()]", ""));
                            //System.out.println(palavraSemCharEspecial.toString());
                            //palavraSemCharEspecial.setLength(0);
                            
                            
                            numero = pegarNumero(palavra);
                            if(numero > maiorPrimo && isPrimo(numero)){
                                maiorPrimo = numero;
                            }
                            
                        }

                    } while (linha != null);
                    */

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
        int quantDivExatas = 0;

        for (int div = 2; div <= numero - 1; div++) {
            if (numero % div == 0) {
                quantDivExatas++;
            }
        }

        return quantDivExatas == 0;

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
