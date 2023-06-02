package primenumber;

import java.io.File;
import java.util.ArrayDeque;
import java.util.ArrayList;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

public class Main {

    //seleciona um diretório raiz por meio de uma janela de seleção de arquivo e retorna o diretório selecionado
    public static File selecionaDiretorioRaiz() {
        JFileChooser janelaSelecao = new JFileChooser(".");
        //janelaSelecao.setControlButtonsAreShown(false);
        janelaSelecao.setControlButtonsAreShown(true); //atributo foi alterado para true para ser possível selecionar uma pasta

        //conf. do filtro de selecao
        janelaSelecao.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File arquivo) {
                return arquivo.isDirectory();
            }

            @Override
            public String getDescription() {
                return "Diretório";
            }
        });

        janelaSelecao.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        //avaliando a acao do usuario na selecao da pasta de inicio da busca
        int acao = janelaSelecao.showOpenDialog(null);

        if (acao == JFileChooser.APPROVE_OPTION) {
            return janelaSelecao.getSelectedFile();
        } else {
            return null;
        }
    }

    public static void main(String[] args) {

        //selecao de um diretorio para iniciar a busca
        File pastaInicial = selecionaDiretorioRaiz();

        if (pastaInicial == null) {
            JOptionPane.showMessageDialog(null, "Você deve selecionar uma pasta para o processamento",
                    "Selecione o arquivo", JOptionPane.WARNING_MESSAGE);
        } else {
            //...Modifique a partir daqui
            //AQUI você deve explorar a pasta, arquivos e subpastas...

            //guardar o tempo de quando a execução, depois de escolher o diretório, começou
            long tempoI = System.currentTimeMillis();

            //calcula a quantidade de threads ("trabalhadores") com base no número de processadores disponíveis
            WorkerPrimo threads[] = new WorkerPrimo[Runtime.getRuntime().availableProcessors()];

            //array para adicionar o diretório principal no topo da pilha
            ArrayDeque<File> explorar = new ArrayDeque<>();
            explorar.push(pastaInicial);

            //array para armazenar todos os arquivos de texto encontrados dentro do diretório principal 
            ArrayList<File> tarefas = new ArrayList<>();

            //processo de busca pelo arquivo
            while (!explorar.isEmpty()) {

                //desempilha o diretorio do topo
                File diretorioAtual = explorar.pop();

                //array que contém os arquivos e diretórios dentro do diretório atual
                File arquivosDir[] = diretorioAtual.listFiles();

                //passando por todos os arquivos e subDiretórios
                for (File arq : arquivosDir) {
                    /*
                    verificar se é uma "pasta", caso afirmativo empilha 
                    caso contrário é preciso verificar se é um arquivo de texto
                    */
                    if (arq.isDirectory()) {
                        explorar.push(arq);
                    } 
                    else {
                        if (arq.getAbsolutePath().endsWith(".txt")) {
                            //arquivo de texto adicionado no vetor de tarefas
                            tarefas.add(arq);
                        }
                    }
                }

            }

            //instancia e executa as threads
            for (int i = 0; i < threads.length; i++) {
                threads[i] = new WorkerPrimo(tarefas);
                threads[i].start();
            }

            /*
            Do jeito que foi implementado, todas as threads já recebem o trabalho todo de uma vez,
            que é analisar os arquivos txt do array de tarefas.
            Então é preciso sinalizar que não há mais trabalho a ser adicionado a partir daqui
            */
            WorkerPrimo.termina();

            //espetar todas as threads terminarem seu trabalho
            for (WorkerPrimo w : threads) {
                try {
                    w.join();
                } catch (InterruptedException ex) {
                    System.err.println("Alguma thread ainda em execução");
                }
            }

            /*
            todas as threads terminaram de executar e cada uma achou o seu maior número primo
            agora é preciso verificar qual deles é o maior de todos
            */
            long numPrimo = 2; //o menor primo possível é 2
            String arquivoMaiorPrimo = null; //enccontrar qual o arquivo em que foi encontrado o maior primo
            for (WorkerPrimo thread : threads) {
                //System.out.println(thread.getMaiorPrimo());
                if (thread.getMaiorPrimo() > numPrimo) {
                    numPrimo = thread.getMaiorPrimo();
                    arquivoMaiorPrimo = thread.getArquivoMaiorPrimo();
                }
            }

            //guardar o tempo em que o programa encontrou o maior pimo
            long tempoF = System.currentTimeMillis();
            
            //exibe o resultado encontrado
            JOptionPane.showMessageDialog(null, "O maior número primo é:" + numPrimo, 
                    "resultado", JOptionPane.INFORMATION_MESSAGE);
            JOptionPane.showMessageDialog(null, "Encontrado no arquivo: " + arquivoMaiorPrimo, 
                    "resultado", JOptionPane.INFORMATION_MESSAGE);

            //exibe o tempo de execução
            JOptionPane.showMessageDialog(null, ("Tempo de execução: "
                    + ((tempoF - tempoI) / 3600000) + "h "
                    + ((tempoF - tempoI) % 3600000 / 60000) + "min "
                    + ((tempoF - tempoI) % 60000 / 1000) + "seg "
                    + ((tempoF - tempoI) % 1000) + "ms"));
        }
    }
}
