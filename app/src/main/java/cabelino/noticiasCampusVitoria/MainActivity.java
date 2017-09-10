package cabelino.noticiasCampusVitoria;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import cabelino.meuprimeiroapp.R;

import static android.drm.DrmInfoStatus.STATUS_ERROR;
import static android.drm.DrmInfoStatus.STATUS_OK;
import static cabelino.meuprimeiroapp.R.layout.main;


public class MainActivity extends Activity implements NewsResultReceiver.Receiver {
    private String[] resultado;
    private ListView listaNoticias;
    private Button button;
    private ArrayList<String> list = new ArrayList<String>();
    private boolean temNoticias = false;
    private boolean firstRun = true;

    private ImageView logoIfes;
    public NewsResultReceiver mReceiver;
    private Intent intent;
    private ImageView logoIfesfundo;
    private final List<Map<String, String>> data = new ArrayList<>();
    private Timer timer = new Timer();
    private TimerTask timerTask;
    private Handler handler = new Handler();
    private ListViewAdapter listviewadapter;
    private List<ApresentaNoticia> listadenoticias = new ArrayList<ApresentaNoticia>();
    private BancoNoticias bancoNoticias;
    private SQLiteDatabase db, dbRead;
    private ContentValues camposNoticias = new ContentValues();
    private String [] noticiasBancoLocal;
    public static Boolean connected = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(main);
        mReceiver = new NewsResultReceiver(new Handler());
        mReceiver.setReceiver(this);
        button = (Button) findViewById(R.id.button);
        listaNoticias = (ListView) findViewById(R.id.listaNoticias);
        logoIfes = (ImageView) findViewById(R.id.imageView2);
        logoIfes.setImageResource(R.drawable.ifes_vitoria);
        logoIfesfundo = (ImageView) findViewById(R.id.ifesfundo);
        logoIfesfundo.setImageResource(R.drawable.fundocampusvitoria);

        try {
            bancoNoticias = new BancoNoticias(this);
            db = bancoNoticias.getWritableDatabase();
            dbRead = bancoNoticias.getReadableDatabase();
            //db.execSQL("delete from NoticiasLocais");
            //db.execSQL("vacuum");
        } catch (SQLiteException e) {
            Log.e("MainActivity", "Erro ao conectar com o banco de dados: " + e);
        }
        //consultar banco de dados para carregar noticias locais
        String [] colunasNoticias = {
                "_id",
                "Data",
                "Hora",
                "Titulo",
                "Resumo",
                "Conteudo"
        };
        String ordenacao = "_id DESC";
        Cursor resultadoQuery = dbRead.query(
                "NoticiasLocais",   //nome da tabela
                colunasNoticias,    //colunas para retornar na pesquisa
                null,               //colunas da clausula WHERE
                null,               //valores das colunas da clausula WHERE
                null,               // nao agrupar as colunas
                null,               // nao filtrar grupos de colunas
                ordenacao           //ordem de classificacao do resultado
        );
        resultadoQuery.moveToFirst();
        Log.i("MainActivity", "Leitura de " + resultadoQuery.getCount() + " noticias locais com sucesso ...");
        noticiasBancoLocal = new String[resultadoQuery.getCount()];
        int indiceNoticia = 0;
        while (!resultadoQuery.isAfterLast()) {
            noticiasBancoLocal[indiceNoticia] = resultadoQuery.getString(resultadoQuery.getColumnIndexOrThrow("_id")) + "_" +
                    resultadoQuery.getString(resultadoQuery.getColumnIndexOrThrow("Data")) + "_" +
                    resultadoQuery.getString(resultadoQuery.getColumnIndexOrThrow("Hora")) + "_" +
                    resultadoQuery.getString(resultadoQuery.getColumnIndexOrThrow("Titulo")) + "_" +
                    resultadoQuery.getString(resultadoQuery.getColumnIndexOrThrow("Resumo")) + "_" +
                    resultadoQuery.getString(resultadoQuery.getColumnIndexOrThrow("Conteudo"));
            indiceNoticia ++;
            resultadoQuery.moveToNext();
        }

        intent = new Intent(this, ConsultaNoticia.class);
        intent.putExtra("receiver", mReceiver);
        intent.putExtra("NEWS", "");
        startService(intent);
    }

    public void clicouBotao (View v) {
        button.setText("Conectado!");
        intent.putExtra("receiver", mReceiver);
        intent.putExtra("NEWS", resultado);
        startService(intent);
        temNoticias=false;
    }

    //inicio do codigo do timertask

    @Override
    protected void onResume() {
        super.onResume();
        //onResume we start our timer_example so it can start when the app comes from the background
        startTimer();
    }
    public void startTimer() {
        //initialize the TimerTask's job
        initializeTimerTask();
        //schedule the timer_example, after the first 5000ms the TimerTask will run every 6000000ms
        timer.scheduleAtFixedRate(timerTask, 10000, 60000);
        //
    }
    public void stoptimertask(View v) {
        //stop the timer_example, if it's not already null
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }
    public void initializeTimerTask() {
        timerTask = new TimerTask() {
            public void run() {
                //use a handler to run a toast that shows the current timestamp
                handler.post(new Runnable() {
                    public void run() {
                        button.callOnClick();
                    }
                });
            }
        };
    }
    //fim do codigo do timertask

    public void onReceiveResult(int resultCode, Bundle resultData) {

        switch (resultCode) {
            case STATUS_OK:
                ArrayList <String> results = resultData.getStringArrayList("result");

                String[] novasNoticias = new String[0];
                String[] ultimasNoticias = new String[results.size()];
                results.toArray(ultimasNoticias);
                int j = 0;
                if ((!firstRun) && (connected)) {
                    if (!comparaNoticias(ultimasNoticias, resultado  )) {
                        temNoticias = true;
                        novasNoticias = new String[(ultimasNoticias.length - resultado.length)];
                        for (int i = resultado.length; i <= (ultimasNoticias.length -1); i++) {
                            novasNoticias[j] = ultimasNoticias[i];
                            j++;
                        }
                        temNoticias=true;
                        resultado = ultimasNoticias;
                    }
                } else {
                    if (firstRun) {
                        novasNoticias = new String[noticiasBancoLocal.length + ultimasNoticias.length];
                        String[] oldNews, freshNews;
                        novasNoticias = noticiasBancoLocal;
                        int m = noticiasBancoLocal.length;
                        for (int k = 0; k > ultimasNoticias.length; k++) {
                            freshNews = ultimasNoticias[k].split("_");
                            for (int l = 0; l < noticiasBancoLocal.length; l++) {
                                oldNews = noticiasBancoLocal[l].split("_");
                                if (!oldNews.equals(freshNews)) {
                                    novasNoticias[m] = ultimasNoticias[k];
                                    m++;
                                }
                            }
                        }
                        Log.i("MainActivity", "Total de mensagens atualizadas: " + novasNoticias.length);

                        resultado = novasNoticias;
                        temNoticias = true;
                    }

                }
                if (temNoticias) {
                    if ((!firstRun) && (connected)) {
                        for (int i=0; i < novasNoticias.length; i++) {
                            String [] linha = novasNoticias[i].split("_");
                            ApresentaNoticia umaNoticia = new ApresentaNoticia(R.drawable.logoifes, linha[3], linha[1] + "  " + linha[2]);
                            listadenoticias.add(umaNoticia);
                            //inserir dados de noticias no banco aqui
                            camposNoticias.put("_id",linha[0]);
                            camposNoticias.put("Data",linha[1]);
                            camposNoticias.put("Hora", linha[2]);
                            camposNoticias.put("Titulo", linha[3]);
                            camposNoticias.put("Resumo", linha[4]);
                            camposNoticias.put("Conteudo", linha[5]);
                            long newRowId = db.insert("NoticiasLocais", null, camposNoticias);
                            Log.i("ConsultaNoticia","Dados inseridos no banco rowId: " + newRowId);
                        }
                    } else {
                        if (firstRun) {
                            for (int i = 0; i < novasNoticias.length; i++) {
                                String[] linha = novasNoticias[i].split("_");
                                ApresentaNoticia umaNoticia = new ApresentaNoticia(R.drawable.logoifes, linha[3], linha[1] + "  " + linha[2]);
                                listadenoticias.add(umaNoticia);
                                firstRun = false;
                            }
                        }

                    }
                    listviewadapter = new ListViewAdapter(this, R.layout.listview_item, listadenoticias);
                    listaNoticias.setAdapter(listviewadapter);
                    listaNoticias.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);

                    // Capture ListView item click
                    listaNoticias.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {

                        @Override
                        public void onItemCheckedStateChanged(ActionMode mode,
                                                              int position, long id, boolean checked) {
                            // Capture total checked items
                            final int checkedCount = listaNoticias.getCheckedItemCount();
                            // Set the CAB title according to total checked items
                            mode.setTitle(checkedCount + " Selected");
                            // Calls toggleSelection method from ListViewAdapter Class
                            listviewadapter.toggleSelection(position);
                        }

                        @Override
                        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.delete:
                                    // Calls getSelectedIds method from ListViewAdapter Class
                                    SparseBooleanArray selected = listviewadapter
                                            .getSelectedIds();
                                    // Captures all selected ids with a loop
                                    for (int i = (selected.size() - 1); i >= 0; i--) {
                                        if (selected.valueAt(i)) {
                                            ApresentaNoticia selecteditem = listviewadapter
                                                    .getItem(selected.keyAt(i));
                                            // Remove selected items following the ids
                                            listviewadapter.remove(selecteditem);
                                            //remover do bando de dados a noticia selecionada aqui
                                            String selection = "Titulo LIKE ?";
                                            String[] selectionArgs = {selecteditem.getTitulo()};
                                            db.delete("NoticiasLocais", selection, selectionArgs);
                                        }
                                    }
                                    // Close CAB
                                    mode.finish();
                                    return true;
                                default:
                                    return false;
                            }
                        }

                        @Override
                        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                            mode.getMenuInflater().inflate(R.menu.activity_main, menu);
                            return true;
                        }

                        @Override
                        public void onDestroyActionMode(ActionMode mode) {
                            // TODO Auto-generated method stub
                            listviewadapter.removeSelection();
                        }

                        @Override
                        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                            // TODO Auto-generated method stub
                            return false;
                        }
                    });

                    temNoticias = false;
                    notificaUsuario("Informes do Campus Vitória", "Você tem novos informes ...");
                } else {
                    Log.i("MainActivity", "Não há novas notícias ...");
                }
                break;
            case STATUS_ERROR:
                Log.i("MainActivity", "Não foi possível acessar o servidor de notícias.");
                notificaUsuario("Informes do Campus Vitória","Não foi possível acessar o provedor de notícias.");
                break;
        }
        temNoticias = false;
    }

    //inicio do codigo de comparacao de strings array
    public static boolean comparaNoticias(String[] newData, String[] intialData) {
        if (newData.length != intialData.length)
            return false;
        Set<String> crunchifySet = new HashSet<>(Arrays.asList(intialData));
        for (String currentValue : newData)
            if (!crunchifySet.contains(currentValue))
                return false;
        return true;
    }
    //fim do codigo de comparacao de strings array

    private void notificaUsuario (String titulo, String texto) {
        //inicio do codigo para notificacao da noticia na GUI
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification noti = new Notification.Builder(this)
                .setContentTitle(titulo)
                .setContentText(texto).setSmallIcon(R.drawable.ic_undobar_undo)
                .setContentIntent(pIntent)
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE | Notification.DEFAULT_LIGHTS)
                .addAction(R.drawable.ic_undobar_undo, "Ler", pIntent).build();
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        // hide the notification after its selected
        noti.flags |= Notification.FLAG_AUTO_CANCEL;

        notificationManager.notify(0, noti);
        //fim do codigo para notificacao da noticia na GUI
    }

}

