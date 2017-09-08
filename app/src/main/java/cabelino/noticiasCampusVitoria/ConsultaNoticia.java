package cabelino.noticiasCampusVitoria;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

import static android.drm.DrmInfoStatus.STATUS_ERROR;
import static android.drm.DrmInfoStatus.STATUS_OK;
import static junit.framework.Assert.assertEquals;


/**
 * Created by cabelino on 26/01/17.
 */

public class ConsultaNoticia extends IntentService {

    //private static final String URL_STRING = "http://api.openweathermap.org/data/2.5/weather?q=Vitoria,BR&appid=b621f9de087c67078a1216bf86a7109c";
    private static final String URL_STRING = "http://172.16.16.9/nuntius/ScholaService.svc/GetAllNews";
    private String [] noticias;


    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public ConsultaNoticia() {
        super("ConsultaNoticia");
    }

    private String [] interpretaMensagem(String resultado) throws JSONException {
        String[] noticias;
        if (resultado.equals("")) {
            noticias = new String[0];
            return noticias;
        } else {
            JSONArray jsonArray = new JSONArray(resultado);
            int newsSize =  jsonArray.length();
            noticias = new String[newsSize];
            for(int i=0; i< newsSize; i++){
                JSONObject obj = (JSONObject)jsonArray.get(i);
                noticias[i] = obj.getString("Id") + "_" + obj.getString("Data") + "_" + obj.getString("Hora")+ "_" + obj.getString("Titulo")+ "_" + obj.getString("Resumo")+ "_" + obj.getString("Conteudo");
            }
            return noticias;
        }

    }

    private String consultaServidor() throws IOException {

        InputStream is;
        StringBuilder builder = new StringBuilder();
        is = null;

        try {
            URL url = new URL(URL_STRING);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            try {
                conn.connect();
                assertEquals(HttpURLConnection.HTTP_OK, conn.getResponseCode());
                is = conn.getInputStream();

                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                String linha;
                linha = reader.readLine();
                while (linha != null) {
                    builder.append(linha);
                    linha = reader.readLine();
                }
                return new String(builder);

            } catch (IOException e) {
                Log.e("error", "Não foi possível se conectar ao servidor de notícias", e);
                return "";
            }
        } finally {
            if (is != null) {
                is.close();
            }
        }

    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            String resultado = consultaServidor();
            noticias =  interpretaMensagem(resultado);
            ArrayList<String> stringList = new ArrayList<String>(Arrays.asList(noticias));

            final ResultReceiver receiver = intent.getParcelableExtra("receiver");
            Bundle b = new Bundle();
            try {
                // get some data or something
                b.putStringArrayList("result", stringList);
                receiver.send(STATUS_OK, b);
            } catch(Exception e) {
                b.putString(Intent.EXTRA_TEXT, e.toString());
                receiver.send(STATUS_ERROR, b);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e1) {
            e1.printStackTrace();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
