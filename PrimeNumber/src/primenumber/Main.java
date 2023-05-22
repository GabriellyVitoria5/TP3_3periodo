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
        janelaSelecao.setControlButtonsAreShown(true);

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
            
            long tempoI = System.currentTimeMillis();
            
            //calcula a quantidade de "trabalhadores", de threads, com base no número de processadores siponíveis
            WorkerPrimo threads[] = new WorkerPrimo[Runtime.getRuntime().availableProcessors()];

            //array para  armazenar todos 
            ArrayDeque<File> explorar = new ArrayDeque<>();
            explorar.push(pastaInicial);
            
            //array para armazenar todos os arquivos de texto a serem analizados 
            ArrayList<File> tarefas = new ArrayList<>();

            //processo de busca pelo arquivo
            while (!explorar.isEmpty()) {

                //desemp. o diretorio do topo
                File diretorioAtual = explorar.pop();

                File arquivosDir[] = diretorioAtual.listFiles();

                //passando por todos os arq. e subDir.
                for (File arq : arquivosDir) {

                    //verificamos se é uma "pasta", caso afirmativo empilha
                    if (arq.isDirectory()) {
                        explorar.push(arq);
                    } else {  
                        if (arq.getAbsolutePath().endsWith(".txt")) {
                            tarefas.add(arq);
                        }

                    }
                }

            }
            
            //insta. e executa as threads
            for(int i = 0; i < threads.length;i++){
                threads[i] = new WorkerPrimo(tarefas);
                threads[i].start();
            }
            
            WorkerPrimo.acordaThreads();
            WorkerPrimo.termina();
            
            for(WorkerPrimo w : threads){
                try {
                    w.join();
                } catch (InterruptedException ex) {
                    System.err.println("Alguma thread ainda em execução");
                }
            }

            /*
            todas as threads terminaram de executar e cada uma achou o maior número primo
            agora é preciso verificar qual desse números é o maior de todos
            */            
            long numPrimo = 2;
            for (int i = 0; i < threads.length; i++) {
                if(threads[i].getMaiorPrimo() > numPrimo){
                    numPrimo = threads[i].getMaiorPrimo();
                }
            }
            
            long tempoF = System.currentTimeMillis();
            // exibe o tempo de execução
            
            JOptionPane.showMessageDialog(null, "O maior número primo é:" + numPrimo,
                    "resultado", JOptionPane.INFORMATION_MESSAGE);
            
            JOptionPane.showMessageDialog(null, ("Tempo de execução: " + 
                    ((tempoF - tempoI) / 3600000) + "h "
                    + ((tempoF - tempoI) % 3600000 / 60000) + "min "
                    + ((tempoF - tempoI) % 60000 / 1000) + "seg "
                    + ((tempoF - tempoI) % 1000) + "ms"));
        }
    }
}

